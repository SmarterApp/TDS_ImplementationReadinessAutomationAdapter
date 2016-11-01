package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;

public interface AutomationInitializer {
    String initialize(AutomationStatusReporter initializationStatusReporter) throws Exception;
}
