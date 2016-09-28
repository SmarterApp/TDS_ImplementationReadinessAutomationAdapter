package org.cresst.sb.irp.automation.adapter.student;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class StudentResponseServiceTest {
    private StudentResponseService studentService;
    private final String studentTestData = "ItemID Response\n1\t<![CDATA[a]]\n" +
            "2\t<![CDATA[b]]\n" +
            "2\t<![CDATA[c]]\n";

    private StudentResponseService studentServiceSpaces;
    private final String studentTestDataSpaces = "ItemID Response\n1\t<![CDATA[Hello world]]\n" +
            "2\t<![CDATA[b]]\n" +
            "2\t<![CDATA[c]]\n";

    @Before
    public void setupStudentResponseService() {
        try {
            studentService = new StudentResponseService(studentTestData);
            studentServiceSpaces = new StudentResponseService(studentTestDataSpaces);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void initialize_WithInputstream() {
        ByteArrayInputStream insTestData = new ByteArrayInputStream(studentTestData.getBytes());
        try {
            assertNotNull(new StudentResponseService(insTestData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void initialize_WithSpacesInCdata() {
        assertNotNull(studentServiceSpaces);
    }

    @Test
    public void getRandomResponse_OneResponseWithSpaces() {
        String expected = "<![CDATA[Hello world]]";
        String response = studentServiceSpaces.getRandomResponse("1");
        assertEquals(expected, response);
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
