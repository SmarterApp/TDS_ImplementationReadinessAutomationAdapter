package org.cresst.sb.irp.automation.adapter.engine.task;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@Ignore
public class AutomationTaskRunnerTest {

    private AutomationTaskRunner taskRunner;

    @Before
    public void setUp() throws Exception {
        taskRunner = new AutomationTaskRunner(null, null, null);
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
