package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.cresst.sb.irp.automation.testhelpers.IntegrationTests;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore("Ignoring since this integration test requires an environment variable and access to a live TDS")
@Category(IntegrationTests.class)
@RunWith(Theories.class)
public class SbossStudentTest {

    private final static String ENV_STUDENT_URL = "ADAPTER_AUTOMATION_STUDENTURL";

    @DataPoint
    public final static Map<StudentTestTypeEnum, String> grade3ELA = new HashMap<>();
    @DataPoint
    public final static Map<StudentTestTypeEnum, String> grade3MATH = new HashMap<>();
    @DataPoint
    public final static Map<StudentTestTypeEnum, String> grade7ELA = new HashMap<>();
    @DataPoint
    public final static Map<StudentTestTypeEnum, String> grade7MATH = new HashMap<>();
    @DataPoint
    public final static Map<StudentTestTypeEnum, String> grade11ELA = new HashMap<>();
    @DataPoint
    public final static Map<StudentTestTypeEnum, String> grade11MATH = new HashMap<>();

    // All of these tests have to be loaded into ART and available to the Student logging into the Student application
    static {
        grade3ELA.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016");
        grade3ELA.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016");

        grade3MATH.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016");
        grade3MATH.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-CAT-MATH-3-Summer-2015-2016");

        grade7ELA.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)IRP-Perf-ELA-7-Summer-2015-2016");
        grade7ELA.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-CAT-ELA-7-Summer-2015-2016");

        grade7MATH.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)SBAC-IRP-Perf-MATH-7-Summer-2015-2016");
        grade7MATH.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-Mathematics-7-Summer-2015-2016");

        grade11ELA.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)IRP-Perf-ELA-11-Summer-2015-2016");
        grade11ELA.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-CAT-ELA-11-Summer-2015-2016");

        grade11MATH.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)SBAC-IRP-Perf-MATH-11-Summer-2015-2016");
        grade11MATH.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-CAT-MATH-11-Summer-2015-2016");
    }

    private URL studentUrl;
    private AutomationRestTemplate studentRestTemplate;
    private StudentResponseGenerator responseGenerator;

    public SbossStudentTest() throws Exception {
        try {
            studentUrl = new URL(System.getenv(ENV_STUDENT_URL));
        } catch (Exception e) {
            throw e;
        }

        responseGenerator = new StudentResponseGenerator(new ClassPathResource("IRPv2_generated_item_responses.txt"));
    }

    @Before
    public void setup() throws Exception {

        studentRestTemplate = new SbossAutomationRestTemplate();
    }

    @Test
    public void loginAsGuest_WithGuestSession() throws Exception {
        AutomationStudent student = new SbossStudent(studentRestTemplate, studentUrl, null, null, "GUEST", "GUEST");
        boolean loginSuccessful = student.login("GUEST Session");
        assertTrue(loginSuccessful);
    }

    @Test
    public void loginAsIRPStudentA_WithGuestSession() throws Exception {
        AutomationStudent student = new SbossStudent(
                studentRestTemplate,
                studentUrl,
                null,
                null,
                "AIRP39990001",
                "IRPStudentA");

        boolean loginSuccessful = student.login("GUEST Session");
        assertTrue(loginSuccessful);
    }

    @Test
    public void loginAsIRPStudentA_WithInvalidSessionId() throws Exception {
        AutomationStudent student = new SbossStudent(
                studentRestTemplate,
                studentUrl,
                null,
                null,
                "AIRP39990001", "IRPStudentA");

        boolean loginSuccessful = student.login("Invalid Session ID");
        assertFalse(loginSuccessful);
    }

    /**
     * This test only works if tests have been selected in ART with a valid test window
     */
    @Theory
    public void takeFixedTestAsIRPStudentA_WithGuestSession(Map<StudentTestTypeEnum, String> testNames) throws Exception {

        AutomationStudent student = new SbossStudent(
                studentRestTemplate,
                studentUrl,
                testNames,
                responseGenerator,
                "AIRP39990001",
                "IRPStudentA");

        assertTrue(student.login("GUEST Session"));
        assertTrue(student.openTest(StudentTestTypeEnum.FIXED));
        assertTrue(student.takeTest());
    }

    /**
     * This test only works if the tests have been selected in ART with a valid test window
     */
    @Theory
    public void takeCATTestAsIRPStudentA_WithGuestSession(Map<StudentTestTypeEnum, String> testNames) throws Exception {

        AutomationStudent student = new SbossStudent(
                studentRestTemplate,
                studentUrl,
                testNames,
                responseGenerator,
                "AIRP39990001",
                "IRPStudentA");

        assertTrue(student.login("GUEST Session"));
        assertTrue(student.openTest(StudentTestTypeEnum.CAT));
        assertTrue(student.takeTest());
    }
}
