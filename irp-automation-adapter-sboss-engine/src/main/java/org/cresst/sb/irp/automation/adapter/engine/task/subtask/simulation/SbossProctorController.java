package org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation;

import org.cresst.sb.irp.automation.adapter.proctor.AutomationProctor;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

public class SbossProctorController implements ProctorController {
    private final static Logger logger = LoggerFactory.getLogger(SbossProctorController.class);

    private final AutomationProctor proctor;
    private AutomationStatusReporter simulationStatusReporter;

    public SbossProctorController(AutomationProctor proctor) {
        this.proctor = proctor;
    }

    @Override
    public void setStatusReporter(AutomationStatusReporter simulationStatusReporter) {
        this.simulationStatusReporter = simulationStatusReporter;
    }

    @Override
    public String initialize(Set<String> irpTestKeys) throws Exception {

        simulationStatusReporter.status(String.format("Logging in as %s", proctor));
        if (!proctor.login()) {
            throwError("Proctor login was unsuccessful");
        }

        logMessage("Proctor login successful. Initiating Test Session.");
        logger.debug("Available tests: " + Arrays.toString(irpTestKeys.toArray()));

        if (!proctor.startTestSession(irpTestKeys)) {
            throwError("Proctor was unable to start a Test Session");
        }

        String sessionId = proctor.getSessionId();
        logMessage("Test Session has been initiated by the Proctor with Session ID = " + sessionId);

        return sessionId;
    }

    @Override
    public void approvalAll() throws Exception {
        if (!proctor.approveAllTestOpportunities()) {
            throwError("Proctor failed to approve all test opportunities");
        }
        logMessage("Proctor approved all tests");
    }

    @Override
    public void leave() {
        if (proctor.pauseTestSession()) {
            logMessage("Test Session has been paused by the Proctor");
        }
        proctor.logout();
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
