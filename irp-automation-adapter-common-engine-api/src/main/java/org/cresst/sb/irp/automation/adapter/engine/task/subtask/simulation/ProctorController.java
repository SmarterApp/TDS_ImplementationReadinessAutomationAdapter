package org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation;

import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;

import java.util.Set;

public interface ProctorController {
    void setStatusReporter(AutomationStatusReporter simulationStatusReporter);
    String initialize(Set<String> irpTestKeys) throws Exception;
    void approvalAll() throws Exception;
    void leave();
}
