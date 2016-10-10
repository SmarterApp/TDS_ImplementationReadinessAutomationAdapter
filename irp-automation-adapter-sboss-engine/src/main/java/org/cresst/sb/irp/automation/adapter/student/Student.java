package org.cresst.sb.irp.automation.adapter.student;

import java.util.List;

import org.cresst.sb.irp.automation.adapter.student.data.TestSelection;

/**
@author Ernesto De La Luz Martinez
*/

public interface Student {
    boolean login(String sessionID, String stateSSID, String firstname, String forbiddenApps);
    boolean checkApproval(String testKey);
    boolean completeTest(String testKey, String testId);
    boolean scoreTest(String testKey, String testId);
    String updateResponses(String requestString);
    String respondToItem(String itemId);
    String updateResponsesForPage(int page, String accs);
    List<TestSelection> getTests();
    boolean startTestSelection(TestSelection testSelection);
    boolean openTestSelection(String testKey, String testId);
    boolean openTestSelection(TestSelection testSelection);
}
