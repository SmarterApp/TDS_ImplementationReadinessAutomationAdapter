package org.cresst.sb.irp.automation.adapter.engine;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.engine.task.AutomationTaskRunner;
import org.cresst.sb.irp.automation.adapter.configuration.AdapterResources;
import org.cresst.sb.irp.automation.adapter.configuration.AutomationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

/**
 * Handles IRP Automation requests making sure only a single execution of automation against a vendor system is run.
 * It runs IRP Automation asynchronously while handling messages back to the client.
 */
public class SbossAutomationEngine implements AutomationEngine {
    private final static Logger logger = LoggerFactory.getLogger(AutomationEngine.class);

    private final TaskExecutor taskExecutor;
    private final AutomationProperties automationProperties;
    private final AdapterResources adapterResources;

    public SbossAutomationEngine(TaskExecutor taskExecutor,
                                 AutomationProperties automationProperties,
                                 AdapterResources adapterResources) {
        this.taskExecutor = taskExecutor;
        this.automationProperties = automationProperties;
        this.adapterResources = adapterResources;
    }

    @Override
    public boolean automate(AdapterAutomationTicket adapterAutomationTicket) {
        return startAutomationTask(adapterAutomationTicket);
    }

    private boolean startAutomationTask(AdapterAutomationTicket adapterAutomationTicket) {
        try {
            AutomationTaskRunner automationTaskRunner = new AutomationTaskRunner(
                    adapterAutomationTicket,
                    automationProperties,
                    adapterResources);

            taskExecutor.execute(automationTaskRunner);
        } catch (TaskRejectedException e) {
            logger.error("Unable to start automation task", e);
            return false;
        }

        return true;
    }
}
