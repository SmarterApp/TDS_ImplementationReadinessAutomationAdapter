package org.cresst.sb.irp.automation.adapter.student;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.net.URL;

import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SbossStudentTest {

    @Mock private AutomationRestTemplate mockStudentRestTemplate;
    private URL url;
    private Student student;

    @Before
    public void setUp() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        url = new URL("https://server.test");
        mockStudentRestTemplate = mock(AutomationRestTemplate.class);

        student = new SbossStudent(mockStudentRestTemplate, url, classLoader.getResource("IRPv2_generated_item_responses.txt").getFile());
    }

    @Test
    public void SbossStudentCreate() {
        assertNotNull(student);
    }

    @Test
    public void Student_ResponsesToItem() {
        assertNotNull(student.respondToItem("1973"));
    }

}
