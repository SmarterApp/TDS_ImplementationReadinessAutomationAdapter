package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class SbossStudentTest {

    @Mock private AutomationRestTemplate mockStudentRestTemplate;
    private URL url;
    private AutomationStudent student;

    @Before
    public void setUp() throws Exception {
        Resource itemResponsesResource = new ClassPathResource("IRPv2_generated_item_responses.txt");

        url = new URL("https://server.test");
        mockStudentRestTemplate = mock(AutomationRestTemplate.class);

        StudentResponseGenerator responseService = new StudentResponseGenerator(itemResponsesResource);
        student = new SbossStudent(mockStudentRestTemplate, url, responseService, "ASSID", "FirstName");
    }

    @Test
    public void Student_ResponsesToItem() {
        assertNotNull(student.respondToItem("1973"));
    }

}
