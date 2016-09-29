package org.cresst.sb.irp.automation.adapter.statusreporting;


import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;

public interface AutomationStatusHandler {
    void handleAutomationStatus(final AdapterAutomationTicket adapterAutomationTicket, final AdapterAutomationStatusReport adapterAutomationStatusReport);
}
