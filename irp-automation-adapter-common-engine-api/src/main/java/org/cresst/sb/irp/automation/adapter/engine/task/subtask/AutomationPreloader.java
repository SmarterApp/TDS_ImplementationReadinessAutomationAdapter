package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.engine.task.AutomationPreloadResults;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;

public interface AutomationPreloader {
    AutomationPreloadResults preload(AutomationStatusReporter preloadingStatusReporter, String tenantId) throws Exception;
    void rollback(AutomationStatusReporter cleanupStatusReporter);
}
