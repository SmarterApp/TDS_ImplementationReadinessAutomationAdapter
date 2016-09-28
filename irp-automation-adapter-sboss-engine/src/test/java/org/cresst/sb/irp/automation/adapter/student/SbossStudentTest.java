package org.cresst.sb.irp.automation.adapter.student;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        String itemResponseFile = classLoader.getResource("IRPv2_generated_item_responses.txt").getFile();
        url = new URL("https://server.test");
        mockStudentRestTemplate = mock(AutomationRestTemplate.class);
        StudentResponseService responseService = new StudentResponseService(new FileInputStream(itemResponseFile));
        student = new SbossStudent(mockStudentRestTemplate, url, responseService);
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
