package org.cresst.sb.irp.automation.adapter.student;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

public class StudentResponseServiceTest {
    private StudentResponseService studentService;
    private final String studentTestData = "ItemID Response\n1 <![CDATA[a]]\n" +
            "2 <![CDATA[b]]\n" +
            "2 <![CDATA[c]]\n";

    @Before
    public void setupStudentResponseService() {
        studentService = new StudentResponseService(studentTestData);
    }

    @Test
    public void initialize_with_inputstream() {
        ByteArrayInputStream insTestData = new ByteArrayInputStream(studentTestData.getBytes());
        assertNotNull(new StudentResponseService(insTestData));
    }

    @Test
    public void initializeStudentResponseService_EmptyString() {
        assertNotNull(new StudentResponseService());
    }

    @Test
    public void initializeStudentResponseService_WithStringData() {
        assertNotNull(studentService);
    }

    @Test
    public void getRandomResponse_OneResponse() {
        String expected = "<![CDATA[a]]";
        String response = studentService.getRandomResponse("1");
        assertEquals(expected, response);
    }
    @Test
    public void getRandomResponse_MultipleResponses() {
        String optionOne = "<![CDATA[b]]";
        String optionTwo = "<![CDATA[c]]";
        String response = studentService.getRandomResponse("2");
        assertTrue(response.equals(optionOne) || response.equals(optionTwo));
    }

    @Test
    public void getRandomResponse_Invalid() {
        assertNull(studentService.getRandomResponse("4"));
    }
}
