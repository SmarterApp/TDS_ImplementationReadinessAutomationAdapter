package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.junit.Assert.assertTrue;


/**
 @author Ernesto De La Luz Martinez
 */

@Ignore("TDS Server is IP restricted. Remove Ignore when machine has access to sever.")
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration( locations = { "classpath*:root-context.xml"})
public class StudentLoginTest {
	private final static Logger logger = LoggerFactory.getLogger(StudentLoginTest.class);

    @Mock
    AutomationRestTemplate mockStudentRestTemplate;

	@Test
	public void login() throws Exception {

		Student studentLogin = new SbossStudent(mockStudentRestTemplate, new URL("http://test.server/student"), null);

		boolean loginSuccessful = studentLogin.login("GUEST Session", "ID:GUEST;FirstName:GUEST", "");

        assertTrue(loginSuccessful);
	}
}