package org.cresst.sb.irp.automation.adapter.service;

import org.cresst.sb.irp.automation.adapter.dao.DocumentXmlRepository;
import org.cresst.sb.irp.automation.adapter.dao.TestOpportunityRepository;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.cresst.sb.irp.automation.adapter.engine.AutomationEngine;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SbossAutomationAdapterService implements AdapterAutomationService {
	private final static Logger logger = LoggerFactory.getLogger(SbossAutomationAdapterService.class);

	private final AutomationEngine automationEngine;
    private final DocumentXmlRepository documentXmlRepository;
    private final TestOpportunityRepository testOpportunityRepository;
    private final RetryOperations retryTemplate;

    private AdapterAutomationTicket adapterAutomationTicket;
    private int listStatusSize;
    
    public SbossAutomationAdapterService(AutomationEngine automationEngine,
                                         DocumentXmlRepository documentXmlRepository,
                                         TestOpportunityRepository testOpportunityRepository,
                                         RetryOperations retryTemplate) {
        this.automationEngine = automationEngine;
        this.documentXmlRepository = documentXmlRepository;
        this.testOpportunityRepository = testOpportunityRepository;
        this.retryTemplate = retryTemplate;
    }

    /**
     * Begins automated generation of TDSReports.
     *
     * @return An AdapterAutomationTicket containing information about the automation request.
     */
    @Override
    public AdapterAutomationTicket automate() {

        if (adapterAutomationTicket == null || adapterAutomationTicket.getAdapterAutomationStatusReport().isAutomationComplete()) {
            adapterAutomationTicket = new AdapterAutomationTicket();
            adapterAutomationTicket.setAdapterAutomationToken(UUID.randomUUID());
            listStatusSize = 0;
            if (!automationEngine.automate(adapterAutomationTicket)) {
                adapterAutomationTicket = null;
            }
        }

        return adapterAutomationTicket;
    }

    /**
     * Gets the latest update to the AdapterAutomationTicket for the given Automation Token.
     *
     * @param adapterAutomationToken The token identifying the automation request.
     * @return The AdapterAutomationTicket with the latest automation status.
     */
    @Override
    public AdapterAutomationTicket getAdapterAutomationTicket(UUID adapterAutomationToken) {

        if (adapterAutomationTicket == null ||
                !adapterAutomationTicket.getAdapterAutomationToken().equals(adapterAutomationToken)) {
    	    return null;
        }

        while (true) {
            AdapterAutomationStatusReport report = adapterAutomationTicket.getAdapterAutomationStatusReport();
            if (report.getPhaseStatuses().size() > listStatusSize ||
                    report.isAutomationComplete()) {
                listStatusSize = adapterAutomationTicket.getAdapterAutomationStatusReport().getPhaseStatuses().size();
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        return adapterAutomationTicket;
    }

    /**
     * Gets an individual TDSReport.
     *
     * @param tdsReportId The identifier of the TDSReport to obtain
     * @return The TDSReport
     */
    @Override
    public TDSReport getTdsReport(int tdsReportId) {
        return documentXmlRepository.getTdsReport(tdsReportId);
    }

    /**
     * Gets all of the generated TDSReports.
     *
     * @param startTimeOfSimulation Gets the TDS Reports that were generated since the time specified
     * @return A collection of all the generated TDSReports.
     */
    @Override
    public Collection<Integer> getTdsReports(Date startTimeOfSimulation) {
        if (startTimeOfSimulation == null) {
            logger.info("Attempting to get TDS Reports since previous Test Simulation");
            if (adapterAutomationTicket == null || adapterAutomationTicket.getStartTimeOfSimulation() == null) {
                logger.info("No record of previous Test Simulation so returning empty list");
                return new ArrayList<>();
            }

            startTimeOfSimulation = adapterAutomationTicket.getStartTimeOfSimulation();
            logger.info("Returning records since the previous Test Simulation at {}", startTimeOfSimulation);
        }

        // Calculate expected number of TDSReports based on number of records in the TDS TestOpportunity table
        Map<Integer, Integer> studentTestCount = testOpportunityRepository.getStudentToTestCount(startTimeOfSimulation);
        int expectedTdsReportCount = 0;
        for (Map.Entry<Integer, Integer> entry : studentTestCount.entrySet()) {
            if (entry.getValue() > 1) {
                // When there is more than one record per student then expect a possible COMBO test to be generated
                // The plus one represents the COMBO test
                expectedTdsReportCount += entry.getValue() + 1;
            } else {
                expectedTdsReportCount++;
            }
        }

        final Date startTime = startTimeOfSimulation;
        final int expectedCount = expectedTdsReportCount;

        try {
            List<Integer> reports = retryTemplate.execute(new RetryCallback<List<Integer>, Exception>() {
                @Override
                public List<Integer> doWithRetry(RetryContext retryContext) throws Exception {
                    List<Integer> reports = documentXmlRepository.getXmlRepositoryDataIdentifiers(startTime);
                    logger.info("Found {} records. Expecting {} records", reports != null ? reports.size() : 0, expectedCount);

                    if (reports.size() < expectedCount) {
                        throw new RepositoryException(reports, "XML Repository doesn't contain enough records");
                    }

                    return reports;
                }
            });

            return reports;
        } catch (RepositoryException e) {
            return e.getRecords() != null ? e.getRecords() : new ArrayList<Integer>();
        } catch (Exception e) {
            logger.error("Unable to get TDS Reports", e);
            return new ArrayList<>();
        }
    }

    class RepositoryException extends Exception {

        List<Integer> records;

        public RepositoryException(List<Integer> records, String message) {
            super(message);
            this.records = records;
        }

        public List<Integer> getRecords() {
            return records;
        }
    }
}
