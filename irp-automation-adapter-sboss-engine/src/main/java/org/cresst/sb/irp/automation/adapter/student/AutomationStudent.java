package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.student.data.TestSelection;

import java.util.List;

public interface AutomationStudent {
    boolean login(String sessionID);
    boolean checkApproval(String testKey);
    boolean completeTest(String testKey, String testId);
    boolean scoreTest(String testKey, String testId);
    String updateResponses(String requestString);
    String respondToItem(String itemId);
    List<TestSelection> getTests();
    boolean startTestSelection(TestSelection testSelection);
    boolean openTestSelection(String testKey, String testId);
    boolean openTestSelection(TestSelection testSelection);
}
