package org.cresst.sb.irp.automation.adapter.statusreporting;


import org.cresst.sb.irp.automation.adapter.domain.AutomationPhase;
import org.cresst.sb.irp.automation.adapter.domain.AutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AutomationToken;
import org.joda.time.Instant;

public class SbossAutomationStatusReporter implements AutomationStatusReporter {

    private final AutomationToken automationToken;
    private final AutomationStatusReport automationStatusReport;
    private final AutomationStatusHandler automationStatusHandler;
    private final AutomationPhase automationPhase;

    public SbossAutomationStatusReporter(AutomationPhase automationPhase,
                                         AutomationToken automationToken,
                                         AutomationStatusReport automationStatusReport,
                                         AutomationStatusHandler automationStatusHandler) {
        this.automationPhase = automationPhase;
        this.automationToken = automationToken;
        this.automationStatusReport = automationStatusReport;
        this.automationStatusHandler = automationStatusHandler;
    }

    @Override
    public void status(String message) {
        automationStatusReport.appendMessage(automationPhase, message, Instant.now().getMillis());
        automationStatusHandler.handleAutomationStatus(automationToken, automationStatusReport);
    }

    @Override
    public void markAutomationComplete() {
        automationStatusReport.markAutomationComplete();
    }
}
