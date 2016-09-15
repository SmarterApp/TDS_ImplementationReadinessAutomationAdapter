package org.cresst.sb.irp.automation.adapter.engine.async.requestprocessor;


import org.cresst.sb.irp.automation.adapter.domain.AutomationRequest;

public interface AutomationRequestProcessor {
    void processAutomationRequest(AutomationRequest automationRequest);
}
