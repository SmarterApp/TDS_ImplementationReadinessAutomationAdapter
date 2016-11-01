package org.cresst.sb.irp.automation.adapter.proctor;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.proctor.data.SessionDTO;
import org.cresst.sb.irp.automation.adapter.proctor.data.TestSession;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.cresst.sb.irp.automation.testhelpers.IntegrationTests;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SbossProctorTest {

    @Mock
    private AutomationRestTemplate accessTokenAutomationRestTemplate;

    @Mock
    private AutomationRestTemplate proctorAutomationRestTemplate;

    private final URL testProctorUrl = new URL("https://tds.server.net/proctor");
    private final URL testGetInitDataUrl = new URL("https://tds.server.net/proctor/Services/XHR.axd/GetInitData");
    private final URL testInsertSessionTestsUrl = new URL("https://tds.server.net/proctor/Services/XHR.axd/InsertSessionTests");
    private final String testProctorUserId = "testProctorUserId@server.net";

    public SbossProctorTest() throws MalformedURLException {
    }

    /**
     * Reusable code to initiate a Proctor login
     * @return The logged in Proctor
     * @throws URISyntaxException Not likely to be thrown
     */
    private AutomationProctor loginMockProctor() throws URISyntaxException {
        // Proctor Under Test
        final AutomationProctor proctorUT = new SbossProctor(proctorAutomationRestTemplate,
                testProctorUrl,
                testProctorUserId);

        final URI proctorUri = new URI(testProctorUrl.toString());
        final URI testGetInitDataUri = new URI(testGetInitDataUrl.toString());

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Set-Cookie", Lists.newArrayList("testcookie1=true", "testcookie2=1"));
        ResponseEntity<String> mockCookieResponse = new ResponseEntity<>("", new LinkedMultiValueMap<>(headers), HttpStatus.OK);

        List<org.cresst.sb.irp.automation.adapter.proctor.data.Test> proctorTests = new ArrayList<>();
        org.cresst.sb.irp.automation.adapter.proctor.data.Test t1 = new org.cresst.sb.irp.automation.adapter.proctor.data.Test();
        t1.set_key("key1");
        t1.setId("id1");
        proctorTests.add(t1);

        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setTests(proctorTests);
        ResponseEntity<SessionDTO> mockGetInitDataResponse = new ResponseEntity<>(sessionDTO, HttpStatus.OK);

        when(proctorAutomationRestTemplate.getForEntity(eq(proctorUri), eq(String.class))).thenReturn(mockCookieResponse);
        when(proctorAutomationRestTemplate.getForEntity(eq(testGetInitDataUri), eq(SessionDTO.class)))
                .thenReturn(mockGetInitDataResponse);
        return proctorUT;
    }

    @Test
    public void whenLoginSuccessful() throws Exception {
        final AutomationProctor proctorUT = loginMockProctor();

        assertTrue(proctorUT.login());
    }

    @Test
    public void approveTestOpportunity_NoTestsToApprove () throws Exception {
        final AutomationProctor proctorUT = loginMockProctor();
        // login proctor
        proctorUT.login();
        // Should not have any tests to approve
        assertFalse(proctorUT.approveAllTestOpportunities());
    }

    @Test
    public void whenCredentialsIncorrect_LoginUnsuccessful() throws Exception {

        // Proctor Under Test
        final AutomationProctor proctorUT = new SbossProctor(proctorAutomationRestTemplate,
                testProctorUrl,
                "badUserId");

        final URI proctorUri = new URI(testProctorUrl.toString());
        final URI testGetInitDataUri = new URI(testGetInitDataUrl.toString());

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Set-Cookie", Lists.newArrayList("testcookie1=true", "testcookie2=1"));
        ResponseEntity<String> mockCookieResponse = new ResponseEntity<>("", new LinkedMultiValueMap<>(headers), HttpStatus.OK);

        SessionDTO sessionDTO = new SessionDTO();
        ResponseEntity<SessionDTO> mockGetInitDataResponse = new ResponseEntity<>(sessionDTO, HttpStatus.BAD_REQUEST);

        when(proctorAutomationRestTemplate.getForEntity(eq(proctorUri), eq(String.class))).thenReturn(mockCookieResponse);
        when(proctorAutomationRestTemplate.getForEntity(eq(testGetInitDataUri), eq(SessionDTO.class)))
                .thenReturn(mockGetInitDataResponse);

        assertFalse(proctorUT.login());
    }

    @Test
    public void whenCredentialsIncorrect_RestTemplateThrowsException_LoginUnsuccessful() throws Exception {

        // Proctor Under Test
        final AutomationProctor proctorUT = new SbossProctor(proctorAutomationRestTemplate,
                testProctorUrl,
                "badUserId");

        final URI proctorUri = new URI(testProctorUrl.toString());

        when(proctorAutomationRestTemplate.getForEntity(eq(proctorUri), eq(String.class))).thenThrow(new RestClientException("Bad Request"));

        assertFalse(proctorUT.login());
    }

    @Ignore("Ignoring integration test with IP restricted server.")
    @Test
    @Category(IntegrationTests.class)
    public void usingRealServer_whenCredentialsIncorrect_LoginUnsuccessful() throws Exception {

        AutomationRestTemplate realProctorRestTemplate = new SbossAutomationRestTemplate();

        // Fill in with real server URLs
        URL realProctorUrl = new URL("");

        // Proctor Under Test
        final AutomationProctor proctorUT = new SbossProctor(realProctorRestTemplate,
                realProctorUrl,
                "badUserId");

        assertFalse(proctorUT.login());
    }

    @Test
    public void whenInitiatingTestSessionSuccessful_SessionIdAvailable() throws Exception {
        // Proctor Under Test
        final AutomationProctor proctorUT = loginMockProctor();

        final URI testInsertSessionTestsUri = new URI(testInsertSessionTestsUrl.toString());

        Set<String> testKeys = new HashSet<>();
        testKeys.add("key1");

        TestSession testSession = new TestSession();
        testSession.setId("test-session-id");

        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setSession(testSession);

        ResponseEntity<SessionDTO> mockInsertSessionTestResponse = new ResponseEntity<>(sessionDTO, HttpStatus.OK);

        when(proctorAutomationRestTemplate.postForEntity(eq(testInsertSessionTestsUri), any(), eq(SessionDTO.class)))
                .thenReturn(mockInsertSessionTestResponse);

        proctorUT.login();
        proctorUT.startTestSession(testKeys);

        assertEquals("test-session-id", proctorUT.getSessionId());
    }
}