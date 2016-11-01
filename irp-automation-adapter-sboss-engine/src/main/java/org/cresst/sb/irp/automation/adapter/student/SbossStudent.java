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

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SbossStudent implements AutomationStudent {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudent.class);

    private final AutomationRestTemplate studentRestTemplate;
    private final URL studentBaseUrl;
    private final StudentResponseGenerator studentResponseGenerator;
    private final String alternateSSID;
    private final String firstName;

    private LoginInfo loginInfo;

    public SbossStudent(AutomationRestTemplate studentRestTemplate,
                        URL studentBaseUrl,
                        StudentResponseGenerator studentResponseGenerator,
                        String alternateSSID,
                        String firstName) {

        this.studentRestTemplate = studentRestTemplate;
        this.studentBaseUrl = studentBaseUrl;
        this.studentResponseGenerator = studentResponseGenerator;
        this.alternateSSID = alternateSSID;
        this.firstName = firstName;
    }

    @Override
    public boolean login(String sessionID) {

        String keyValues = studentKeyValues(alternateSSID, firstName);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("sessionID", sessionID);
        form.add("keyValues", keyValues);
        form.add("forbiddenApps", "");

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
                    requestEntity, new ParameterizedTypeReference<ResponseData<LoginInfo>>() {});

            if (responseIsValid(response)) {
                logger.info("Student {} logged in for session {}", keyValues, sessionID);

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

                return takeTest(testInfo, testSelection.getTestKey());
            }
        } catch (RestClientException e) {
            logger.error("Could not start test selection. Reason: {}", e.getMessage());
        }
        return false;
    }

    private boolean takeTest(TestInfo testInfo, String testKey) {
        int testLength = testInfo.getTestLength();

        String initReq = UpdateResponsesBuilder.initialRequest();

        logger.debug("Initial request: "  +  initReq);

        String updateResp = updateResponses(initReq);

        logger.debug("Update Responses response: " + updateResp);
        UpdateResponsePageContents updateContents = new UpdateResponsePageContents(updateResp);
        // Try to get initial pages 3 times before failing
        int tries = 3;
        while(updateContents.pageCount() == 0 && --tries > 0) {
            updateResp = updateResponses(initReq);

            logger.debug("Try {}: Update Responses response: ", tries, updateResp);
            updateContents = new UpdateResponsePageContents(updateResp);
        }
        if (updateContents.pageCount() == 0) {
            logger.error("Could not find pages from initial request");
            return false;
        }

        UpdateResponsePage respCurrentPage = updateContents.getFirstPage();
        Map<Integer, PageContents> allPages = new HashMap<>();
        int pageNumber = respCurrentPage.getPageNumber();
        while(updateContents != null && pageNumber <= testLength && !updateContents.isFinished()) {
            logger.debug("page number: {}/{}", pageNumber, testLength);

            // Get page contents for all the returned pages
            for(int page : updateContents.getPages().keySet()) {
                if(allPages.containsKey(page)) continue;
                UpdateResponsePage currPage = updateContents.getPages().get(page);

                String pageContentsString = getPageContent(page, currPage.getGroupId(), currPage.getPageKey());
                logger.debug("pageContentsString: " + pageContentsString);
                PageContents pageContents = new PageContents(pageContentsString, page, currPage.getPageKey());
                allPages.put(page, pageContents);
            }

            if (! allPages.containsKey(pageNumber)) {
                logger.error("Could not find page contents for page: {}", pageNumber);
                return false;
            }

            logger.debug("Page Contents: " + allPages.get(pageNumber));
            String responseReq = UpdateResponsesBuilder.createRequestString(studentResponseGenerator, "", allPages.get(pageNumber), testKey);

            // See what UpdateResponse gives back,
            logger.debug("Request data: " + responseReq);

            // Update with real data
            updateResp = updateResponses(responseReq);
            logger.debug(updateResp);

            pageNumber += 1;
            updateContents = new UpdateResponsePageContents(updateResp);

        }
        if (updateContents == null) {
            logger.error("Current page is null, page contents are: {}, update response is: ", updateContents, updateResp);
            return false;
        }

        logger.debug("Finished on page: {}/{}. Finish status: {}", pageNumber, testLength, updateContents.isFinished());
        return updateContents.isFinished();
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

    private String getPageContent(int page, String groupID, String pageKey) {
        URI getPageContentUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "getPageContent")
                .queryParam("page", page)
                .queryParam("new", false)
                .queryParam("attempt", 1)
                .queryParam("groupID", groupID)
                .queryParam("pageKey", pageKey)
                .build()
                .toUri();

        ResponseEntity<String> response = studentRestTemplate.exchange(getPageContentUri, HttpMethod.POST,
                HttpEntity.EMPTY, String.class);

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
        return studentResponseGenerator.getRandomResponse(itemId);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SbossStudent{");
        sb.append("alternateSSID='").append(alternateSSID).append('\'');
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
