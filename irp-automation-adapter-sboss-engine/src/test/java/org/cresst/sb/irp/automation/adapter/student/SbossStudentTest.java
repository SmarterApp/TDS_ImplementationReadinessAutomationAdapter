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

    @Before
    public void setUp() throws Exception {
        url = new URL("https://server.test");
        mockStudentRestTemplate = mock(AutomationRestTemplate.class);
    }

    @Test
    public void SbossStudentCreate() {
        Student testStudent = new SbossStudent(mockStudentRestTemplate, url);
        assertNotNull(testStudent);
    }

}
