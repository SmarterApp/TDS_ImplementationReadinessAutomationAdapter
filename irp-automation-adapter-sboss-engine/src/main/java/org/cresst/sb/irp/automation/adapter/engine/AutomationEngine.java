package org.cresst.sb.irp.automation.adapter.engine;


import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;

/**
 * Using the AutomationRequest, implementing classes will run IRP Automation against a vendor's Smarter Balanced
 * Open Source Assessment Delivery System.
 */
public interface AutomationEngine {
    /**
     * Performs the IRP Automation
     */
    boolean automate(AdapterAutomationTicket adapterAutomationTicket);
}
