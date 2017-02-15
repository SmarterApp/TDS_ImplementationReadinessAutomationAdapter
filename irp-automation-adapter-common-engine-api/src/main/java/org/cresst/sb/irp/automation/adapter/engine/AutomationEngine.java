package org.cresst.sb.irp.automation.adapter.engine;


import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;

/**
 * Using the AdapterAutomationTicket, implementors will run Adapter Automation against their own
 * implementation of the Smarter Balanced Test Delivery System to generate TDS Reports
 */
public interface AutomationEngine {
    /**
     * Performs the IRP Automation
     */
    boolean automate(AdapterAutomationTicket adapterAutomationTicket);
}
