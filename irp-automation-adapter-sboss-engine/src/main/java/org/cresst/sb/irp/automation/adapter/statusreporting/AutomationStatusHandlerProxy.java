package org.cresst.sb.irp.automation.adapter.statusreporting;


import org.cresst.sb.irp.automation.adapter.domain.AutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AutomationToken;

public class AutomationStatusHandlerProxy implements AutomationStatusHandler {

    private AutomationStatusHandler handler;

    public void setAutomationStatusHandler(AutomationStatusHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleAutomationStatus(AutomationToken automationToken, AutomationStatusReport automationStatusReport) {
        handler.handleAutomationStatus(automationToken, automationStatusReport);
    }
}
