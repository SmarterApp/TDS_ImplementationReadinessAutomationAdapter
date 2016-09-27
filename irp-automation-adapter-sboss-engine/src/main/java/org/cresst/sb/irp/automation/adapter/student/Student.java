package org.cresst.sb.irp.automation.adapter.student;

import java.util.List;


/**
@author Ernesto De La Luz Martinez
*/

public interface Student {
	boolean login(String sessionID, String keyValues,String forbiddenApps);
	boolean startTestSession(String testKey, String testId);
    boolean checkApproval(String testKey);
    boolean completeTest(String testKey, String testId);
    boolean scoreTest(String testKey, String testId);
    String updateResponses(String requestString);
    String respondToItem(String itemId);
    String updateResponsesForPage(int page);
}
