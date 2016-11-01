package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.junit.Assert.assertTrue;

@Ignore("TDS Server is IP restricted. Remove Ignore when machine has access to sever.")
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration( locations = { "classpath*:root-context.xml"})
public class StudentLoginTest {
	private final static Logger logger = LoggerFactory.getLogger(StudentLoginTest.class);
	private final static String ENV_STUDENT_URL = "TDS_STUDENT_URL";

    AutomationRestTemplate studentRestTemplate;
    AutomationStudent studentLogin;

    @Before
    public void setup() throws Exception {
        URL studentUrl = null;
        try {
            studentUrl = new URL(System.getenv(ENV_STUDENT_URL));
        } catch (Exception e) {
            logger.error("Environment variable " + ENV_STUDENT_URL + " not set.");
            throw e;
        }
        studentRestTemplate = new SbossAutomationRestTemplate();
        studentLogin = new SbossStudent(studentRestTemplate, studentUrl, null, "GUEST", "GUEST");
    }

	@Test
	public void login() throws Exception {
		boolean loginSuccessful = studentLogin.login("GUEST Session");

        assertTrue(loginSuccessful);
	}
}