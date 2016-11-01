package org.cresst.sb.irp.automation.adapter.proctor;

import java.util.Set;

public interface AutomationProctor {
    boolean login();
    boolean logout();
    boolean startTestSession(Set<String> irpTestKeys);
    boolean pauseTestSession();
    String getSessionId();
    boolean approveTestOpportunity(String sessionKey, String oppId, String accs);
    boolean approveAllTestOpportunities();
    boolean autoRefreshData();
}
