package org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.student.AutomationStudent;
import org.cresst.sb.irp.automation.adapter.student.StudentTestTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SbossStudentController implements StudentController {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudentController.class);

    private final List<AutomationStudent> students;

    private AutomationStatusReporter simulationStatusReporter;
    private StudentTestTypeEnum currentTestType;

    public SbossStudentController(List<AutomationStudent> students) {
        this.students = Lists.newArrayList(Lists.reverse(students));
    }

    @Override
    public void setStatusReporter(AutomationStatusReporter simulationStatusReporter) {
        this.simulationStatusReporter = simulationStatusReporter;
    }

    @Override
    public void initialize(final String sessionId, StudentTestTypeEnum testType) throws Exception {

        currentTestType = testType;

        for (int i = students.size() - 1; i >= 0; i--) {
            AutomationStudent student = students.get(i);

            if (!student.login(sessionId)) {
                logMessage(String.format("%s login unsuccessful.", student));
                students.remove(i);
            } else {
                logMessage(String.format("%s login successful.", student));
            }
        }

        if (students.size() == 0) {
            throwError("All Students failed to login.");
        }

        for (int i = students.size() - 1; i >= 0; i--) {
            AutomationStudent student = students.get(i);

            if (!student.openTest(testType)) {
                logMessage(String.format("%s unable to open test %s", student, student.getTestName(testType)));
                students.remove(i);
            } else {
                logMessage(String.format("%s successfully opened test %s", student, student.getTestName(testType)));
            }
        }

        if (students.size() == 0) {
            throwError("All Students failed to open their test.");
        }
    }

    @Override
    public void takeTests() throws Exception {

        boolean allSucceeded = true;
        for (AutomationStudent student : students) {
            boolean success = student.takeTest();
            allSucceeded = allSucceeded && success;
            logMessage(String.format("%s successfully took test=%s", student, success));
        }

        if (!allSucceeded) {
            throwError(String.format("All Students failed to complete their %s test", currentTestType));
        }
    }

    @Override
    public void leave() {

    }

    private void logMessage(String message) {
        logger.info(message);
        simulationStatusReporter.status(message);
    }

    private void throwError(String message) throws Exception {
        logger.error(message);
        simulationStatusReporter.status(message);
        simulationStatusReporter.markAutomationError();
        throw new Exception(message);
    }
}
