package org.cresst.sb.irp.automation.adapter.domain;

import java.util.*;

/**
 *
 * Contains the data about the status of the TDS report
 */
public class AdapterAutomationStatusReport {
    private boolean automationComplete;
    private long lastUpdateTimestamp;
    private Map<AutomationPhase, List<String>> phaseStatuses = new EnumMap<>(AutomationPhase.class);
    private boolean error;

    public void appendMessage(AutomationPhase automationPhase, String message, long timestamp) {
        lastUpdateTimestamp = timestamp;

        List<String> messages = phaseStatuses.get(automationPhase);
        if (messages == null) {
            messages = new ArrayList<>();
            phaseStatuses.put(automationPhase, messages);
        }
        messages.add(message);
    }

    public void markAutomationComplete() {
        automationComplete = true;
    }

    public boolean isAutomationComplete() {
        return automationComplete;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public Map<AutomationPhase, List<String>> getPhaseStatuses() {
        return phaseStatuses;
    }

    public void markAutomationError() {
        error = true;
    }

    public boolean isError() {
        return error;
    }
}
