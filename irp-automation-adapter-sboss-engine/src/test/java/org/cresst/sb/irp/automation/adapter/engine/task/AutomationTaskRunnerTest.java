package org.cresst.sb.irp.automation.adapter.engine.task;

import static org.junit.Assert.*;

import org.cresst.sb.irp.automation.adapter.domain.AutomationRequest;
import org.cresst.sb.irp.automation.adapter.domain.AutomationToken;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusHandler;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusHandlerProxy;
import org.junit.Before;
import org.junit.Test;

public class AutomationTaskRunnerTest {

    private AutomationTaskRunner taskRunner;

    @Before
    public void setUp() throws Exception {
        AutomationRequest automationRequest = new AutomationRequest();
        AutomationToken automationToken = new AutomationToken(automationRequest);
        // Note: Does anything actually implement this??
        // Checked type hierarchy and it doesn't seem like it.
        AutomationStatusHandler automationStatusHandler = new AutomationStatusHandlerProxy();
        taskRunner = new AutomationTaskRunner(automationRequest, automationToken, automationStatusHandler);
    }

    @Test
    public void test_init() {
        assertNotNull(taskRunner);
    }

    @Test
    public void test_run_taskrunner() {
        taskRunner.run();
    }

}
