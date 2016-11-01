package org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation;

import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.student.StudentTestTypeEnum;

public interface StudentController {
    void setStatusReporter(AutomationStatusReporter simulationStatusReporter);
    void initialize(String sessionId, StudentTestTypeEnum testType) throws Exception;
    void takeTests() throws Exception;
    void leave();
}
