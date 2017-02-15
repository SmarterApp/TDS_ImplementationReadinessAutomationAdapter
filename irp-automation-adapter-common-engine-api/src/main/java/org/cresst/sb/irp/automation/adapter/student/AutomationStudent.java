package org.cresst.sb.irp.automation.adapter.student;

public interface AutomationStudent {
    /**
     * Instructs the Student to login
     * @param sessionID Session ID for the Test Session
     * @return True if login was successful; false otherwise.
     */
    boolean login(String sessionID);

    /**
     * Open the specified type of Test (Fixed or CAT). The Student should know which Tests it
     * is responsible to take.
     * @param studentTestTypeEnum The type of Test the Student should take.
     * @return True if opening the Test was successful; false otherwise.
     */
    boolean openTest(StudentTestTypeEnum studentTestTypeEnum);

    /**
     * The Student should remember which Test it opened. This instructs the Student to take the Test to completion.
     * @return True if the Student was able to complete and submit the test; false otherwise.
     */
    boolean takeTest();

    /**
     * Let's the caller know the name of the Test (Test Key) for the specified Test type (Fixed or CAT).
     * @param studentTestTypeEnum The type of Test the caller wants to know the name
     * @return The name of the Test of the specified Test type.
     */
    String getTestName(StudentTestTypeEnum studentTestTypeEnum);
}
