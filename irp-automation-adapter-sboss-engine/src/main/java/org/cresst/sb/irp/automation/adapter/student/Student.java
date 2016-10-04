package org.cresst.sb.irp.automation.adapter.student;

/**
@author Ernesto De La Luz Martinez
*/

public interface Student {
    boolean login(String sessionID, String stateSSID, String firstname, String forbiddenApps);
    boolean startTestSession(String testKey, String testId);
    boolean checkApproval(String testKey);
    boolean completeTest(String testKey, String testId);
    boolean scoreTest(String testKey, String testId);
    String updateResponses(String requestString);
    String respondToItem(String itemId);
    String updateResponsesForPage(int page, String accs);
}
