package org.cresst.sb.irp.automation.adapter.statusreporting;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.AutomationPhase;
import org.joda.time.Instant;

public class SbossAutomationStatusReporter implements AutomationStatusReporter {

    private final AdapterAutomationTicket adapterAutomationTicket;
    private final AutomationPhase automationPhase;

    public SbossAutomationStatusReporter(AutomationPhase automationPhase,
                                         AdapterAutomationTicket adapterAutomationTicket) {
        this.automationPhase = automationPhase;
        this.adapterAutomationTicket = adapterAutomationTicket;
    }

    @Override
    public void status(String message) {
        adapterAutomationTicket
                .getAdapterAutomationStatusReport()
                .appendMessage(automationPhase, message, Instant.now().getMillis());
    }

    @Override
    public void markAutomationComplete() {
        adapterAutomationTicket.getAdapterAutomationStatusReport().markAutomationComplete();
    }

    @Override
    public void markAutomationError() {
        adapterAutomationTicket.getAdapterAutomationStatusReport().markAutomationError();
    }
}
