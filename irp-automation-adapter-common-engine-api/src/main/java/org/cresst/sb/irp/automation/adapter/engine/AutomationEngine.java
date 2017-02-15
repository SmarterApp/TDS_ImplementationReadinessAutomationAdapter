package org.cresst.sb.irp.automation.adapter.engine;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;

/**
 * Using the AdapterAutomationTicket, implementors will run Adapter Automation against their own
 * implementation of the Smarter Balanced Test Delivery System to generate TDS Reports.
 * 
 * The AdapterAutomationTicket is to be used to populate the status of the automation
 * process. Implementors should fill this data the client can use to monitor the progress.
 */
public interface AutomationEngine {
    /**
     * Performs the automation to generate TDS Reports
     */
    boolean automate(AdapterAutomationTicket adapterAutomationTicket);
}
