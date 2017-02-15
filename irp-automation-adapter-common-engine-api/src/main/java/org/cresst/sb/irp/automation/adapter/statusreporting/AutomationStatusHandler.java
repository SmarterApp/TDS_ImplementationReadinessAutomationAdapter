package org.cresst.sb.irp.automation.adapter.statusreporting;


import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;

public interface AutomationStatusHandler {
    void handleAutomationStatus(final AdapterAutomationTicket adapterAutomationTicket, final AdapterAutomationStatusReport adapterAutomationStatusReport);
}
