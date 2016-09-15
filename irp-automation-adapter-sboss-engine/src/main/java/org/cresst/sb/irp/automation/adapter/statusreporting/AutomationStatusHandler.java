package org.cresst.sb.irp.automation.adapter.statusreporting;


import org.cresst.sb.irp.automation.adapter.domain.AutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AutomationToken;

public interface AutomationStatusHandler {
    void handleAutomationStatus(final AutomationToken automationToken, final AutomationStatusReport automationStatusReport);
}
