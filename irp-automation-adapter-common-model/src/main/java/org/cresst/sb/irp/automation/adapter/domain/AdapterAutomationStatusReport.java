package org.cresst.sb.irp.automation.adapter.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AdapterAutomationStatusReport {
    private boolean automationComplete;
    private long lastUpdateTimestamp;
    private Map<AutomationPhase, List<String>> phaseStatuses = new EnumMap<>(AutomationPhase.class);

    public AdapterAutomationStatusReport() {}

    public boolean isAutomationComplete() {
        return automationComplete;
    }

    public void setAutomationComplete(boolean automationComplete) {
        this.automationComplete = automationComplete;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public Map<AutomationPhase, List<String>> getPhaseStatuses() {
        return phaseStatuses;
    }

    public void setPhaseStatuses(Map<AutomationPhase, List<String>> phaseStatuses) {
        this.phaseStatuses = phaseStatuses;
    }
}
