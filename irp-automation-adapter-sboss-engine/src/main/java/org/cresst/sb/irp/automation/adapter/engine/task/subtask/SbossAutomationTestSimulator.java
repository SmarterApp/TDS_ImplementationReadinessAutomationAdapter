package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.engine.task.AutomationPreloadResults;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation.ProctorController;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation.StudentController;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.student.StudentTestTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SbossAutomationTestSimulator implements AutomationTestSimulator {
    private final static Logger logger = LoggerFactory.getLogger(SbossAutomationTestSimulator.class);

    private final ProctorController proctorController;
    private final StudentController studentController;

    public SbossAutomationTestSimulator(ProctorController proctorController, StudentController studentController) {
        this.proctorController = proctorController;
        this.studentController = studentController;
    }

    @Override
    public void simulate(AutomationStatusReporter simulationStatusReporter,
                         AutomationPreloadResults automationPreloadResults) throws Exception {

        proctorController.setStatusReporter(simulationStatusReporter);
        studentController.setStatusReporter(simulationStatusReporter);

        try {
            String sessionId = proctorController.initialize(automationPreloadResults.getIrpTestKeys());
            studentController.initialize(sessionId, StudentTestTypeEnum.FIXED);
            proctorController.approvalAll();
            studentController.takeTests();

            studentController.initialize(sessionId, StudentTestTypeEnum.CAT);
            proctorController.approvalAll();
            studentController.takeTests();
        } finally {
            studentController.leave();
            proctorController.leave();
        }
    }
}