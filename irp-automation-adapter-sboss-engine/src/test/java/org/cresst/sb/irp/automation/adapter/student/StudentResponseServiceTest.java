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

    private StudentResponseService generatedItems;

    @Before
    public void setupStudentResponseService() throws IOException {
        studentService = new StudentResponseService(studentTestData);
        studentServiceSpaces = new StudentResponseService(studentTestDataSpaces);
        generatedItems = new StudentResponseService(getClass().getClassLoader().getResourceAsStream("IRPv2_generated_item_responses.txt"));
    }

    @Test
    public void item_1996_exists() {
        assertNotNull(generatedItems.getRandomResponse("1996"));
    }

    @Test
    public void initialize_WithInputstream() throws IOException {
        ByteArrayInputStream insTestData = new ByteArrayInputStream(studentTestData.getBytes());
        assertNotNull(new StudentResponseService(insTestData));
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
