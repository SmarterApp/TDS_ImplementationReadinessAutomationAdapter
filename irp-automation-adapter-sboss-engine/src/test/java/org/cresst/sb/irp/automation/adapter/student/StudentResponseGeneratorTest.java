package org.cresst.sb.irp.automation.adapter.student;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class StudentResponseGeneratorTest {
    private StudentResponseGenerator studentService;
    private final String studentTestData = "ItemID Response\n1\t<![CDATA[a]]\n" +
            "2\t<![CDATA[b]]\n" +
            "2\t<![CDATA[c]]\n";

    private StudentResponseGenerator studentServiceSpaces;
    private final String studentTestDataSpaces = "ItemID Response\n1\t<![CDATA[Hello world]]\n" +
            "2\t<![CDATA[b]]\n" +
            "2\t<![CDATA[c]]\n";

    private StudentResponseGenerator generatedItems;

    @Before
    public void setupStudentResponseGenerator() throws IOException {
        studentService = new StudentResponseGenerator(studentTestData);
        studentServiceSpaces = new StudentResponseGenerator(studentTestDataSpaces);
        generatedItems = new StudentResponseGenerator(new ClassPathResource("IRPv2_generated_item_responses.txt"));
    }

    @Test
    public void item_1996_exists() {
        assertNotNull(generatedItems.getRandomResponse("1996"));
    }

    @Test
    public void initialize_WithInputstream() throws IOException {
        ByteArrayInputStream insTestData = new ByteArrayInputStream(studentTestData.getBytes());
        assertNotNull(new StudentResponseGenerator(insTestData));
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
        assertNotNull(new StudentResponseGenerator());
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
