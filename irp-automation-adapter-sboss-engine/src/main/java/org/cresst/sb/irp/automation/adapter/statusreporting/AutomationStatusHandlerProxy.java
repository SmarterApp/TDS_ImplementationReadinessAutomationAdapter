package org.cresst.sb.irp.automation.adapter.statusreporting;


import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;

public class AutomationStatusHandlerProxy implements AutomationStatusHandler {

    private AutomationStatusHandler handler;

    public void setAutomationStatusHandler(AutomationStatusHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleAutomationStatus(AdapterAutomationTicket adapterAutomationTicket, AdapterAutomationStatusReport adapterAutomationStatusReport) {
        handler.handleAutomationStatus(adapterAutomationTicket, adapterAutomationStatusReport);
    }
}
