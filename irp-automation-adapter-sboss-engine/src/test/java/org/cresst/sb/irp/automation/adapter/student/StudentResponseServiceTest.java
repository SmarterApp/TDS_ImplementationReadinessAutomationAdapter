package org.cresst.sb.irp.automation.adapter.student;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class StudentResponseServiceTest {
    private StudentResponseService studentService;
    private final String studentTestData = "ItemID Response\n1 <![CDATA[<response></response>]]\n" +
            "2 <![CDATA[<itemResponse><response>a</response></itemResponse>]]\n" +
            "3 <![CDATA[<itemResponse><response>a</response><response>b</response></itemResponse>]]";

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

    @Test
    public void getRandomResponse_OneResponse() {
        String expected = "<response>a</response>";
        String response = studentService.getRandomResponse("2");
        assertEquals(expected, response);
    }
    @Test
    public void getRandomResponse_MultipleResponses() {
        String optionOne = "<response>a</response>";
        String optionTwo = "<response>b</response>";
        String response = studentService.getRandomResponse("3");
        assertTrue(response.equals(optionOne) || response.equals(optionTwo));
    }
}
