package org.cresst.sb.irp.automation.adapter.engine.task;

import java.util.Set;

public class AutomationPreloadResults {
    private Set<String> irpTestKeys;

    public AutomationPreloadResults(Set<String> irpTestKeys) {
        this.irpTestKeys = irpTestKeys;
    }

    public Set<String> getIrpTestKeys() {
        return irpTestKeys;
    }

    public void setIrpTestKeys(Set<String> irpTestKeys) {
        this.irpTestKeys = irpTestKeys;
    }
}
