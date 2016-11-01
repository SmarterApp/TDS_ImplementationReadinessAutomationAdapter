package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

@Ignore("TDS Server is IP restricted. Remove Ignore when machine has access to sever.")
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration( locations = { "classpath*:root-context.xml"})
public class StudentStartTestSessionTest {
	private final static Logger logger = LoggerFactory.getLogger(StudentStartTestSessionTest.class);
    private final static String ENV_STUDENT_URL = "TDS_STUDENT_URL";
    private final static String ENV_STATE_SSID = "TDS_STATE_SSID";
    private final static String ENV_STUDENT_FIRSTNAME = "TDS_STUDENT_FIRSTNAME";

    AutomationRestTemplate studentRestTemplate;
    AutomationStudent studentLogin;

    @Before
    public void setup() throws Exception {
        URL studentUrl = null;
        try {
            studentUrl = new URL(System.getenv(ENV_STUDENT_URL));
        } catch (Exception e) {
            logger.error("Environment variable " + ENV_STUDENT_URL + " not set.");
        }
        studentRestTemplate = new SbossAutomationRestTemplate();
        studentLogin = new SbossStudent(studentRestTemplate, studentUrl, null, "TestAltSSID", "TestFirstName");
    }

	@Test
	public void startTestSessionTest() throws Exception {
	    boolean loginSuccessful = studentLogin.login("GUEST Session");

		//To login the ws needs these cookies
		List<String> cookies = new ArrayList<>();
		cookies.add(  "JSESSIONID=AD61A325FF8C44F99C01F50F3B7DA622" );
		cookies.add(  "JSESSIONID=D3DB83BA77852F6FAA16DCFF1B2E3B0A" );
		cookies.add(  "Student-SBAC_PT-Auth=Ji7O7hK9PGB4V2S9PRjSD3N8oksAatzi63aypDED5uPa%2FTsjt9YpyYY5m8WyeysFWNHPr5uEEhuaSy%2F%2FXqgBi8%2BbAqg7OUosfMBzJMc%2F1DA%253D" );
		cookies.add(  "TDS-Student-Browser=screen" );
		cookies.add( "TDS-Student-Client=SBAC_PT" );
		cookies.add( "TDS-Student-Data=T_GR%26S_NAME%3DTDS+Session%26TI_S%3DELA%26O_BKEY%3D5c8bbfab-0c0e-4de9-a150-f018a06f8f1c%26T_ID%3DGUEST+-772%26T_LN%3DGUEST%26T_FN%3DGUEST%26S_KEY%3Dc238d726-fc39-4ed4-8af3-fe47b9c1e6dd%26S_ID%3DGUEST+Session%26TI_G%3D4%26	" );


		String testKey = "(SBAC_PT)SBAC ELA 3-ELA-3-Spring-2014-2015";
		String testId = "SBAC ELA 3-ELA-3";
		String grade = "4";
		String subject = "ELA";

		boolean startSessionSuccessful =  studentLogin.openTestSelection(testKey, testId);
        assertTrue(startSessionSuccessful);
	}
}