package org.cresst.sb.irp.automation.adapter.statusreporting;

public interface AutomationStatusReporter {
    /**
     * Reports the current status to the user
     * @param message The status message to send
     */
    void status(String message);

    /**
     * Indicate that Automation has completed
     */
    void markAutomationComplete();

    /**
     * Indicate that an Automation Error has occurred
     */
    void markAutomationError();
}
