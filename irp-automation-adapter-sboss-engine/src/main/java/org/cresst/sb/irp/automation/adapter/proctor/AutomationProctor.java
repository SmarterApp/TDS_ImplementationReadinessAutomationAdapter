package org.cresst.sb.irp.automation.adapter.proctor;

import java.util.Set;

public interface AutomationProctor {
    boolean login();
    boolean logout();
    boolean startTestSession(Set<String> irpTestKeys);
    boolean pauseTestSession();
    String getSessionId();
    boolean approveAllTestOpportunities();
    boolean autoRefreshData();
}
