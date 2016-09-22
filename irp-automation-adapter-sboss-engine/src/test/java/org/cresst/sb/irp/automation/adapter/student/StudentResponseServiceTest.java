package org.cresst.sb.irp.automation.adapter.student;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class StudentResponseServiceTest {
    private StudentResponseService studentService;
    private final String studentTestData = "ItemID Response\n1 <![CDATA[<response></response>]]\n2 <![CDATA[<itemResponse><response><value>a</value><value>b</value></response></itemResponse>]]";

    @Before
    public void setupStudentResponseService() {
        studentService = new StudentResponseService(studentTestData);
    }

    @Test
    public void initializeStudentResponseService_EmptyString() {
        assertNotNull(new StudentResponseService());
    }

    @Test
    public void initializeStudentResponseService_WithStringData() {
        assertNotNull(studentService);
    }

    // todo: invalid string data

    @Test
    public void getItemResponse() {
        assertEquals("<response></response>", studentService.getItemResponse("1"));
    }

    @Test
    public void getItemResponse_NotFound() {
        // What to do when not found
    }

    @Ignore
    @Test
    public void getRandomResponse() {
        String optionOne = "<value>a</value>";
        String optionTwo = "<value>b</value>";
        String response = studentService.getRandomResponse("2");
        assertTrue(response == optionOne || response == optionTwo);
    }
}
