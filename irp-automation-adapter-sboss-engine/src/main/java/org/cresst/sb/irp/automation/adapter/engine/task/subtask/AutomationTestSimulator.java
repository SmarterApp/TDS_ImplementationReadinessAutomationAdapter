package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.engine.task.AutomationPreloadResults;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;

import java.util.Map;

public interface AutomationTestSimulator {
    Map<Integer, Integer> simulate(AutomationStatusReporter simulationStatusReporter, AutomationPreloadResults automationPreloadResults)
            throws Exception;
}
