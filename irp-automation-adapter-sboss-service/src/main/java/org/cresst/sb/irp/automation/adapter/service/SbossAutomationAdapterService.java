package org.cresst.sb.irp.automation.adapter.service;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.engine.AutomationEngine;
import org.cresst.sb.irp.automation.adapter.tdsreport.extractor.TdsReportExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Smarter Balanced Open Source Test Delivery System specific implementation of an AdapterAutomationService.
 * This class is responsible for managing the creation and extraction of TDS Reports.
 */
@Service
public class SbossAutomationAdapterService implements AdapterAutomationService {
	private final static Logger logger = LoggerFactory.getLogger(SbossAutomationAdapterService.class);

	private static int MAX_RETRIES = 100;

	private final AutomationEngine automationEngine;
    private final TdsReportExtractor tdsReportExtractor;

    private AdapterAutomationTicket adapterAutomationTicket;
    private int listStatusSize;
    
    public SbossAutomationAdapterService(AutomationEngine automationEngine,
                                         TdsReportExtractor tdsReportExtractor) {
        this.automationEngine = automationEngine;
        this.tdsReportExtractor = tdsReportExtractor;
    }


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

    
    @Override
    public AdapterAutomationTicket getAdapterAutomationTicket(UUID adapterAutomationToken) {

        if (adapterAutomationTicket == null ||
                !adapterAutomationTicket.getAdapterAutomationToken().equals(adapterAutomationToken)) {
    	    return null;
        }

        int retries = 0;
        while (retries++ < MAX_RETRIES) {
            AdapterAutomationStatusReport report = adapterAutomationTicket.getAdapterAutomationStatusReport();

            if (report.getPhaseStatuses().size() > listStatusSize || report.isAutomationComplete()) {
                listStatusSize = adapterAutomationTicket.getAdapterAutomationStatusReport().getPhaseStatuses().size();
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return adapterAutomationTicket;
    }

    @Override
    public String getTdsReport(int tdsReportId) {
        return tdsReportExtractor.getTdsReport(tdsReportId);
    }

    @Override
    public Collection<Integer> getTdsReports(Date startTimeOfSimulation) {
        if (startTimeOfSimulation == null) {
            logger.info("Attempting to get TDS Reports since previous Test Simulation");
            if (adapterAutomationTicket == null || adapterAutomationTicket.getStartTimeOfSimulation() == null) {
                logger.error("No record of previous Test Simulation so returning empty list");
                return new ArrayList<>();
            }

            startTimeOfSimulation = adapterAutomationTicket.getStartTimeOfSimulation();
            logger.info("Returning records since the previous Test Simulation at {}", startTimeOfSimulation);
        }

        // Calculate expected number of TDSReports based on number of completed tests
        Map<Integer, Integer> studentTestCount = adapterAutomationTicket.getCompletedTests();
        if (studentTestCount == null || studentTestCount.size() == 0) {
            logger.error("Can't calculate expected number of TDS Reports");
            return new ArrayList<>();
        }

        Collection<Integer> tdsReportIds = tdsReportExtractor.getTdsReports(startTimeOfSimulation, studentTestCount);
        adapterAutomationTicket.setTdsReportIds(tdsReportIds);

        return tdsReportIds;
    }
}
