package org.cresst.sb.irp.automation.adapter.engine;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.engine.task.SbossAutomationTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

/**
 * Handles IRP Automation requests making sure only a single execution of automation against a vendor system is run.
 * It runs IRP Automation asynchronously while handling messages back to the client.
 */
public abstract class SbossAutomationEngine implements AutomationEngine {
    private final static Logger logger = LoggerFactory.getLogger(AutomationEngine.class);

    private final TaskExecutor taskExecutor;

    public SbossAutomationEngine(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public boolean automate(AdapterAutomationTicket adapterAutomationTicket) {
        return startAutomationTask(adapterAutomationTicket);
    }

    private boolean startAutomationTask(AdapterAutomationTicket adapterAutomationTicket) {
        try {
            SbossAutomationTaskRunner automationTaskRunner = createAutomationTaskRunner();
            automationTaskRunner.setAdapterAutomationTicket(adapterAutomationTicket);

            taskExecutor.execute(automationTaskRunner);
        } catch (TaskRejectedException e) {
            logger.error("Unable to start automation task", e);
            return false;
        }

        return true;
    }

    /**
     * Created using Spring's "lookup method injection"
     * @return An AutomationTaskRunner
     */
    protected abstract SbossAutomationTaskRunner createAutomationTaskRunner();
}
