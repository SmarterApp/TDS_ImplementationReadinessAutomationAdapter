package org.cresst.sb.irp.automation.adapter.student;

import AIR.Common.data.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.cresst.sb.irp.automation.adapter.student.data.*;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;


/**
 * @author Ernesto De La Luz Martinez
 */

public class SbossStudent implements Student {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudent.class);

    private AutomationRestTemplate studentRestTemplate;
    private URL studentBaseUrl;
    private StudentResponseService responseService;

    private LoginInfo loginInfo;

    public SbossStudent(AutomationRestTemplate studentRestTemplate, URL studentBaseUrl, StudentResponseService studentResponseService) {
        this.studentRestTemplate = studentRestTemplate;
        this.studentBaseUrl = studentBaseUrl;
        this.responseService = studentResponseService;
    }

    @Override
    public boolean login(String sessionID, String stateSSID, String firstname, String forbiddenApps) {

        String keyValues = studentKeyValues(stateSSID, firstname);
        logger.info("Student login started for student {} for session {}", keyValues, sessionID);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("sessionID", sessionID);
        form.add("keyValues", keyValues);
        form.add("forbiddenApps", forbiddenApps);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI loginStudentUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "loginStudent")
                .build()
                .toUri();

        try {
            // LoginInfo class needs to be completed
            ResponseEntity<ResponseData<LoginInfo>> response = studentRestTemplate.exchange(loginStudentUri, HttpMethod.POST,
                    requestEntity, new ParameterizedTypeReference<ResponseData<LoginInfo>>() {
                    });

            if (responseIsValid(response)) {
                loginInfo = response.getBody().getData();

                return true;
            }
        } catch (RestClientException e) {
            logger.error("Error while logging in student: {}. Message: {}", keyValues, e.getMessage());
        }

        return false;
    }

    /**
     * @param stateSSID
     * @param firstname
     * @return keyValues format (ID:000000;FirstName:Student) for the given login information
     */
    private String studentKeyValues(String stateSSID, String firstname) {
        String keyValues = "ID:" + stateSSID + ";FirstName:" + firstname;
        return keyValues;
    }

    // Requests approval from proctor
    @Override
    public boolean openTestSelection(TestSelection testSelection) {
        String testKey = testSelection.getTestKey();
        String testId = testSelection.getTestID();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);
        form.add("testID", testId);
        form.add("grade", testSelection.getGrade());
        form.add("subject", testSelection.getSubject());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        URI openTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "openTest")
                .build()
                .toUri();


        try {
            ResponseEntity<ResponseData<OpportunityInfoJsonModel>> response = studentRestTemplate.exchange(openTestUri, HttpMethod.POST,
                    requestEntity, new ParameterizedTypeReference<ResponseData<OpportunityInfoJsonModel>>() {
                    });

            return responseIsValid(response);
        } catch (RestClientException e) {
            logger.error("Could not open test selection {}. Reason: {}", testSelection.getDisplayName(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean startTestSelection(TestSelection testSelection) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("testeeKey", String.valueOf(loginInfo.getTestee().getKey()));
        form.add("testeeToken", loginInfo.getTestee().getToken().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        URI startTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "startTest")
                .build()
                .toUri();


        try {
            ResponseEntity<ResponseData<TestInfo>> response = studentRestTemplate.exchange(startTestUri, HttpMethod.POST,
                    requestEntity, new ParameterizedTypeReference<ResponseData<TestInfo>>() {});

            if (responseIsValid(response)) {
                TestInfo testInfo = response.getBody().getData();
                logger.info("Test started for: " + testInfo.getTestName());
                int testLength = testInfo.getTestLength();
                String initReq = UpdateResponsesBuilder.initialRequest("", testLength);

                logger.debug("Initial request: "  +  initReq);
                String updateResp = updateResponses(initReq);
                logger.debug("Update Responses response: " + updateResp);
                UpdateResponsePageContents upRespPageContents = new UpdateResponsePageContents(updateResp);

                logger.debug("Group id: {}. Page Key: {}. Page Number: {}", upRespPageContents.getGroupId(), upRespPageContents.getPageKey(), upRespPageContents.getPageNumber());

                // Create student response service
                StudentResponseService studentResponseService = null;
                try {
                    studentResponseService = new StudentResponseService(getClass().getClassLoader().getResourceAsStream("IRPv2_generated_item_responses.txt"));
                } catch (IOException e) {
                    logger.error("Unable to read the student generated item responses");
                }

                UpdateResponsePageContents updateContents = new UpdateResponsePageContents(updateResp);

                // Get page contents
                String pageContentsString = getPageContent(updateContents.getPageNumber(), "", updateContents.getGroupId(), updateContents.getPageKey());
                logger.debug("pageContentsString: " + pageContentsString);
                PageContents pageContents = new PageContents(pageContentsString, updateContents.getPageNumber());

                logger.debug("Page Contents: " + pageContents);
                String responseReq = UpdateResponsesBuilder.docToString(UpdateResponsesBuilder.createRequest(studentResponseService, "", pageContents, testSelection.getTestKey()));

                // See what UpdateResponse gives back,
                logger.debug("Request data: " + responseReq);

                // Update with real data
                logger.debug(updateResponses(responseReq));

                return true;
            }
        } catch (RestClientException e) {
            logger.error("Could not start test selection. Reason: {}", e.getMessage());
        }
        return false;
    }

    // Note: This calls getTests to look up the test selection
    // Don't think this is need but it was implemented in this was previously
    // See startTestSession(TestSelection testSelection) for new approach
    @Override
    public boolean openTestSelection(String testKey, String testId) {
        TestSelection testSelection = getTestSelection(testKey, testId);
        if (testSelection == null) {
            logger.info("Unable to get available Tests");
            return false;
        }
        return openTestSelection(testSelection);
    }

    private String getPageContent(int page, String accs, String groupID, String pageKey) {
        URI getPageContentUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "getPageContent")
                .queryParam("page", page)
                .queryParam("new", false)
                .queryParam("attempt", 1)
                .queryParam("groupID", groupID)
                .queryParam("pageKey", pageKey)
                .build()
                .toUri();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("accs", accs);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = studentRestTemplate.exchange(getPageContentUri, HttpMethod.POST,
                requestEntity, String.class);

        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            logger.info("Succesfully got page contents for page: " + String.valueOf(page));
            return response.getBody();
        } else {
            logger.error("Failed to get page contents for page: " + String.valueOf(page));
            return null;
        }
    }

    @Override
    public boolean scoreTest(String testKey, String testId) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);
        form.add("testID", testId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI scoreTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "scoreTest")
                .build()
                .toUri();

        // TODO: Replace ResponseData<String> with TestSummary if needed
        ResponseEntity<ResponseData<String>> response = studentRestTemplate.exchange(scoreTestUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<String>>() {
                });

        return responseIsValid(response);
    }

    @Override
    public boolean completeTest(String testKey, String testId) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);
        form.add("testID", testId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI completeTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "completeTest")
                .build()
                .toUri();

        ResponseEntity<ResponseData<String>> response = studentRestTemplate.exchange(completeTestUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<String>>() {
                });


        return responseIsValid(response);
    }

    @Override
    public boolean checkApproval(String testKey) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI checkApprovalUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "checkApproval")
                .build()
                .toUri();

        ResponseEntity<String> response = studentRestTemplate.exchange(checkApprovalUri, HttpMethod.POST, requestEntity, String.class);
        logger.info("checkApproval response: " + response);
        return response.getStatusCode() == HttpStatus.OK;
    }

    /**
     * Calls `getPageContents` and randomly answers questions
     * based on `responseFile` answers
     *
     * @param page to automatically fill responses for
     * @param accs the student's accessibilities
     * @return the xml response to the call to `updateResponses`
     */
    @Override
    public String updateResponsesForPage(int page, String accs) {
        // TODO: Fix the nulls
        PageContents pageContents = new PageContents(getPageContent(page, accs, null, null), page);
        Document xmlRequest = UpdateResponsesBuilder.createRequest(responseService, accs, pageContents);
        String stringRequest = UpdateResponsesBuilder.docToString(xmlRequest);
        return updateResponses(stringRequest);
    }

    @Override
    public String updateResponses(String requestString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestString, headers);

        URI updateResponsesUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "updateResponses")
                .build()
                .toUri();

        ResponseEntity<String> response = studentRestTemplate.exchange(updateResponsesUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<String>() {
                });

        if (response != null && response.hasBody() && response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return null;
        }
    }

    @Override
    public List<TestSelection> getTests() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("testeeKey", String.valueOf(loginInfo.getTestee().getKey()));
        form.add("testeeToken", loginInfo.getTestee().getToken().toString());
        form.add("sessionKey", loginInfo.getSession().getKey().toString());
        if (loginInfo.getTestee().getGrade() != null) {
            form.add("grade", loginInfo.getTestee().getGrade().toString());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        URI getTestsUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "getTests")
                .build()
                .toUri();

        ResponseEntity<ResponseData<List<TestSelection>>> response = studentRestTemplate.exchange(getTestsUri,
                HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResponseData<List<TestSelection>>>() {
                });

        TestSelection testSelection = null;

        if (responseIsValid(response)) {
            return response.getBody().getData();
        } else {
            return null;
        }
    }

    private TestSelection getTestSelection(String testKey, String testId) {
        List<TestSelection> availableTests = getTests();
        TestSelection testSelection = null;

        for (TestSelection availableTest : availableTests) {
            if (StringUtils.equalsIgnoreCase(availableTest.getTestKey(), testKey) &&
                    StringUtils.equalsIgnoreCase(availableTest.getTestID(), testId)) {

                testSelection = availableTest;
                break;
            }
        }

        return testSelection;
    }

    private boolean approvalAccepted(ApprovalInfo approvalInfo) {
        return approvalInfo.getNumericStatus() == 1;
    }

    private <T> boolean responseIsValid(ResponseEntity<ResponseData<T>> response) {
        return responseHasData(response) && response.getStatusCode() == HttpStatus.OK;
    }

    private <T> boolean responseHasData(ResponseEntity<ResponseData<T>> response) {
        return response != null && response.hasBody() && response.getBody().getData() != null;
    }

    @Override
    public String respondToItem(String itemId) {
        return responseService.getRandomResponse(itemId);
    }

    public StudentResponseService getResponseService() {
        return responseService;
    }

    public void setResponseService(StudentResponseService responseService) {
        this.responseService = responseService;
    }

}
