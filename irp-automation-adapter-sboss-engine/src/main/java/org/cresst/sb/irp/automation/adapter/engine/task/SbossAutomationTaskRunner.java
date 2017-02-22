package org.cresst.sb.irp.automation.adapter.engine.task;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.AutomationPhase;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.AutomationInitializer;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.AutomationPreloader;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.AutomationTestSimulator;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.statusreporting.SbossAutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.tdsreport.extractor.TdsReportExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Runs Automation Tasks against the Smarter Balanced Open Source Test Delivery System
 */
public class SbossAutomationTaskRunner implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(SbossAutomationTaskRunner.class);

    private final AutomationInitializer automationInitializer;
    private final AutomationPreloader automationPreloader;
    private final AutomationTestSimulator automationTestSimulator;
    private final TdsReportExtractor tdsReportExtractor;

    private AdapterAutomationTicket adapterAutomationTicket;

    public SbossAutomationTaskRunner(AutomationInitializer automationInitializer,
                                     AutomationPreloader automationPreloader,
                                     AutomationTestSimulator automationTestSimulator,
                                     TdsReportExtractor tdsReportExtractor) {

        this.automationInitializer = automationInitializer;
        this.automationPreloader = automationPreloader;
        this.automationTestSimulator = automationTestSimulator;
        this.tdsReportExtractor = tdsReportExtractor;
    }

    @Override
    public void run() {
        if (adapterAutomationTicket == null) {
            throw new RuntimeException("Adapter Automation Ticket was not set");
        }
        
        adapterAutomationTicket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());

        AutomationStatusReporter initializationStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.INITIALIZATION,
                adapterAutomationTicket);
        AutomationStatusReporter preloadingStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.PRELOADING,
                adapterAutomationTicket);
        AutomationStatusReporter simulationStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION,
                adapterAutomationTicket);
        AutomationStatusReporter extractionStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.EXTRACTION,
                adapterAutomationTicket);
        AutomationStatusReporter cleanupStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.CLEANUP,
                adapterAutomationTicket);

        try {
            String tenantId = automationInitializer.initialize(initializationStatusReporter);

            AutomationPreloadResults automationPreloadResults =
                    automationPreloader.preload(preloadingStatusReporter, tenantId);

            Date startTimeOfSimulation = new Date();
            Map<Integer, Integer> completedTests = automationTestSimulator.simulate(simulationStatusReporter, automationPreloadResults);
            adapterAutomationTicket.setStartTimeOfSimulation(startTimeOfSimulation);
            adapterAutomationTicket.setCompletedTests(completedTests);

            // Extract the TDS Reports
            extractionStatusReporter.status("Initial TDS Reports extraction. This will attempt to extract as many TDS Reports as possible.");
            Collection<Integer> tdsReportIds = tdsReportExtractor.getTdsReports(startTimeOfSimulation, completedTests);
            adapterAutomationTicket.setTdsReportIds(tdsReportIds);
            extractionStatusReporter.status("Extraction completed. Extracted "
                    + (tdsReportIds != null ? tdsReportIds.size() : 0) + " TDS Reports. "
                    + "This allowed TIS enough time to compute scores before cleaning up.");

        } catch (Exception ex) {
            logger.error("Ending automation task because of exception", ex);
            cleanupStatusReporter.markAutomationError();
        } finally {
            cleanupStatusReporter.status("Rolling back data from preloading phase.");

            automationPreloader.rollback(cleanupStatusReporter);

            logger.info("Automation task for {} is complete.", adapterAutomationTicket);
            cleanupStatusReporter.status("Cleanup complete.");
            cleanupStatusReporter.markAutomationComplete();
        }
        
    }

    /**
     * Set the AdapterAutomationTicket before executing run
     * @param adapterAutomationTicket The automation ticket to update automation status
     */
    public void setAdapterAutomationTicket(AdapterAutomationTicket adapterAutomationTicket) {
        this.adapterAutomationTicket = adapterAutomationTicket;
    }
}

