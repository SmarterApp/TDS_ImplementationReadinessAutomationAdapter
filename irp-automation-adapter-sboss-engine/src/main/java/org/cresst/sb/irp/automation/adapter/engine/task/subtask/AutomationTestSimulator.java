package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.engine.task.AutomationPreloadResults;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;

public interface AutomationTestSimulator {
    void simulate(AutomationStatusReporter simulationStatusReporter, AutomationPreloadResults automationPreloadResults)
            throws Exception;
}
