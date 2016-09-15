package org.cresst.sb.irp.automation.adapter.engine.async;


import org.cresst.sb.irp.automation.adapter.domain.AutomationRequest;

/**
 * Using the AutomationRequest, implementing classes will run IRP Automation against a vendor's Smarter Balanced
 * Open Source Assessment Delivery System.
 */
public interface AsyncAutomationEngine {
    /**
     * Performs the IRP Automation
     * @param automationRequest The information needed to run IRP Automation
     */
    void automate(AutomationRequest automationRequest);
}
