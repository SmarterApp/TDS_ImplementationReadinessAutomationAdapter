package org.cresst.sb.irp.automation.adapter.engine.async;

import org.cresst.sb.irp.automation.adapter.domain.AutomationRequest;
import org.cresst.sb.irp.automation.adapter.engine.async.requestprocessor.AutomationRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles IRP Automation requests making sure only a single execution of automation against a vendor system is run.
 * It runs IRP Automation asynchronously while handling messages back to the client.
 */
public class SbossAsyncAutomationEngine implements AsyncAutomationEngine {
    private final static Logger logger = LoggerFactory.getLogger(AsyncAutomationEngine.class);

    private final AutomationRequestProcessor automationRequestProcessor;

    public SbossAsyncAutomationEngine(AutomationRequestProcessor automationRequestProcessor) {
        this.automationRequestProcessor = automationRequestProcessor;
    }

    @Override
    public void automate(final AutomationRequest automationRequest) {
        logger.info("Automation Requested for {}", automationRequest);
        automationRequestProcessor.processAutomationRequest(automationRequest);
    }
}
