package org.cresst.sb.irp.automation.adapter.student;

import AIR.Common.data.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.cresst.sb.irp.automation.adapter.student.data.*;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.util.*;

public class SbossStudent implements AutomationStudent {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudent.class);

    private final AutomationRestTemplate studentRestTemplate;
    private final RetryOperations retryTemplate;
    private final URL studentBaseUrl;
    private final Map<StudentTestTypeEnum, String> testNames;
    private final StudentResponseGenerator studentResponseGenerator;
    private final String alternateSSID;
    private final String firstName;

    private StudentTestTypeEnum currentTestType;
    private TestSelection currentTestSelection;

    private LoginInfo loginInfo;
    private OpportunityInfoJsonModel opportunityInfo;
    private ApprovalInfo approvalInfo;
    private TestInfo testInfo;

    public SbossStudent(AutomationRestTemplate studentRestTemplate,
                        URL studentBaseUrl,
                        Map<StudentTestTypeEnum, String> testNames,
                        StudentResponseGenerator studentResponseGenerator,
                        String alternateSSID,
                        String firstName) {
        this(studentRestTemplate, null, studentBaseUrl, testNames, studentResponseGenerator, alternateSSID, firstName);
    }

    public SbossStudent(AutomationRestTemplate studentRestTemplate,
                        RetryOperations retryTemplate,
                        URL studentBaseUrl,
                        Map<StudentTestTypeEnum, String> testNames,
                        StudentResponseGenerator studentResponseGenerator,
                        String alternateSSID,
                        String firstName) {

        this.studentRestTemplate = studentRestTemplate;
        this.retryTemplate = retryTemplate != null ? retryTemplate : defaultRetryTemplate();
        this.studentBaseUrl = studentBaseUrl;
        this.testNames = testNames;
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
                    requestEntity, new ParameterizedTypeReference<ResponseData<LoginInfo>>() {
                    });

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

    @Override
    public boolean openTest(StudentTestTypeEnum studentTestTypeEnum) {

        String testNameLookup = testNames.get(studentTestTypeEnum);
        currentTestType = studentTestTypeEnum;

        TestSelection selectedTest = null;
        List<TestSelection> testSelections = getTests();
        for (TestSelection testSelection : testSelections) {
            if (testSelection.getTestKey().equals(testNameLookup)) {
                selectedTest = testSelection;
                break;
            }
        }

        if (selectedTest == null) {
            logger.error(String.format("%s unable to select %s type test %s", toString(), studentTestTypeEnum, testNameLookup));
            return false;
        }

        currentTestSelection = selectedTest;

        return openTestSelection();
    }

    @Override
    public boolean takeTest() {
        String currentTestKey = testNames.get(currentTestType);

        if (!checkApproval(currentTestKey)) {
            logger.error("Check approval failed");
            return false;
        }

        if (!startTestSelection()) {
            logger.error("Start Test failed");
            return false;
        }

        if (!answerQuestions()) {
            logger.error("Error answering all questions");
            return false;
        }

        if (!completeTest()) {
            logger.error("Error completing test");
            return false;
        }

        if (!scoreTest()) {
            logger.error("Error scoring test");
            return false;
        }

        return true;
    }

    @Override
    public String getTestName(StudentTestTypeEnum studentTestTypeEnum) {
        return testNames.get(studentTestTypeEnum);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SbossStudent{");
        sb.append("alternateSSID='").append(alternateSSID).append('\'');
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns the formatted string containing the Student ID and First Name to be sent in the POST body of the login
     *
     * @param stateSSID Student's SSID (can be alternate SSID)
     * @param firstName Student's First Name
     * @return keyValues format (ID:000000;FirstName:Student) for the given login information
     */
    private String studentKeyValues(String stateSSID, String firstName) {
        String keyValues = "ID:" + stateSSID + ";FirstName:" + firstName;
        return keyValues;
    }

    List<TestSelection> getTests() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("testeeKey", String.valueOf(loginInfo.getTestee().getKey()));
        form.add("testeeToken", loginInfo.getTestee().getToken().toString());
        form.add("sessionKey", loginInfo.getSession().getKey().toString());
        if (loginInfo.getTestee().getGrade() != null) {
            form.add("grade", loginInfo.getTestee().getGrade());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        URI getTestsUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "getTests")
                .build()
                .toUri();

        try {
            ResponseEntity<ResponseData<List<TestSelection>>> response = studentRestTemplate.exchange(getTestsUri,
                    HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResponseData<List<TestSelection>>>() {
                    });

            return responseIsValid(response) ? response.getBody().getData() : new ArrayList<TestSelection>();
        } catch (RestClientException e) {
            logger.error("Could not get tests for {}", toString());
            return new ArrayList<>();
        }
    }

    boolean openTestSelection() {
        String testKey = currentTestSelection.getTestKey();
        String testId = currentTestSelection.getTestID();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);
        form.add("testID", testId);
        form.add("grade", currentTestSelection.getGrade());
        form.add("subject", currentTestSelection.getSubject());

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

            if (!responseIsValid(response)) {
                return false;
            }

            opportunityInfo = response.getBody().getData();
            return true;
        } catch (RestClientException e) {
            logger.error("Could not open test selection {}. Reason: {}", currentTestSelection.getDisplayName(), e.getMessage());
            return false;
        }
    }

    boolean checkApproval(String testKey) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);
        form.add("oppKey", opportunityInfo.getOppKey().toString());
        form.add("browserKey", opportunityInfo.getBrowserKey().toString());
        form.add("sessionKey", loginInfo.getSession().getKey().toString());
        form.add("sessionID", loginInfo.getSession().getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        final URI checkApprovalUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "checkApproval")
                .build()
                .toUri();

        try {
            boolean success = retryTemplate.execute(new RetryCallback<Boolean, RestClientException>() {
                @Override
                public Boolean doWithRetry(RetryContext retryContext) throws RestClientException {
                    logger.info("Check approval retry count: " + retryContext.getRetryCount());

                    ResponseEntity<ResponseData<ApprovalInfo>> response = studentRestTemplate.exchange(checkApprovalUri,
                            HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResponseData<ApprovalInfo>>() {
                            });

                    logger.debug("checkApproval response: {}", response);

                    if (responseIsValid(response) &&
                            response.getBody().getData().getStatus() == ApprovalInfo.OpportunityApprovalStatus.Approved) {
                        approvalInfo = cloneApprovalInfo(response.getBody().getData());
                        return true;
                    }

                    throw new RestClientException("Check approval failed");
                }
            });

            return success;
        } catch (RestClientException ex) {
            logger.error(toString(), ex);
            return false;
        }
    }

    boolean startTestSelection() {
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
                    requestEntity, new ParameterizedTypeReference<ResponseData<TestInfo>>() {
                    });

            if (responseIsValid(response)) {
                testInfo = response.getBody().getData();
                logger.info("Test started for: " + testInfo.getTestName());
                return true;
            }
        } catch (RestClientException e) {
            logger.error("Could not start test selection.", e.getMessage());
        }
        return false;
    }

    boolean pauseTest() {
        final URI checkApprovalUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "checkApproval")
                .queryParam("currentPage", "1")
                .queryParam("reason", "manual")
                .queryParam("timestamp", Instant.now().getMillis())
                .build()
                .toUri();

        try {
            ResponseEntity<ResponseData<Long>> response = studentRestTemplate.exchange(checkApprovalUri,
                    HttpMethod.POST, null, new ParameterizedTypeReference<ResponseData<Long>>() {
                    });

            return responseIsValid(response);
        } catch (RestClientException ex) {
            logger.error(toString(), ex);
            return false;
        }
    }

    boolean answerQuestions() {

        String initialRequest = UpdateResponsesBuilder.initialRequest(approvalInfo);

        logger.debug("Initial request: {}", initialRequest);

        String updateResponses = updateResponses(initialRequest);

        logger.debug("Initial Update Responses response: {}", updateResponses);
        UpdateResponsePageContents updateResponseContents =
                UpdateResponsePageContents.buildUpdateResponsePageContents(updateResponses);

        if (updateResponseContents == null || updateResponseContents.pageCount() == 0) {
            logger.error("Could not find pages from initial request");
            return false;
        }

        UpdateResponsePage currentPage = updateResponseContents.getFirstPage();

        SortedSet<PageContents> allPagesContents = new TreeSet<>(new Comparator<PageContents>() {
            @Override
            public int compare(PageContents o1, PageContents o2) {
                return o1.getPageNumber() - o2.getPageNumber();
            }
        });

        // Initial request was attemptNum = 1
        int attemptNum = 2;

        while (!updateResponseContents.isFinished()) {

            // Get page contents for all the returned pages (already sorted since underlying Map is TreeMap)
            for (final UpdateResponsePage page : updateResponseContents.getPages().values()) {
                if (allPagesContents.contains(new PageContents(page.getPageNumber(), page.getPageKey()))) continue;

                PageContents pageContents = getPageContent(page.getPageNumber(), page.getGroupId(), page.getPageKey());
                logger.debug("PageContents: {}", pageContents);

                if (pageContents == null) {
                    logger.error("Page contents is null");
                    return false;
                }

                if (currentPage.getSegmentId() != null && page.getSegmentId() != null &&
                        !StringUtils.equalsIgnoreCase(currentPage.getSegmentId(), page.getSegmentId())) {
                    exitSegment(page.getPageNumber(), currentPage.getSegmentPosition());
                }

                allPagesContents.add(pageContents);

                currentPage = page;
            }

            // Create response contains answers for all pages' items
            String responseReq = UpdateResponsesBuilder.createRequestString(studentResponseGenerator, approvalInfo,
                    allPagesContents, attemptNum++);

            // See what UpdateResponse gives back,
            logger.debug("Request data: {}", responseReq);

            // Update with real data
            updateResponses = updateResponses(responseReq);
            logger.debug("Update Responses response: {}", updateResponses);

            updateResponseContents = UpdateResponsePageContents.buildUpdateResponsePageContents(updateResponses);

            if (updateResponseContents == null) {
                logger.error("Current page is null, update response is: {}", updateResponses);
                return false;
            }

            logger.debug("{}", updateResponseContents.getPages());
        }

        return true;
    }

    String updateResponses(String requestString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestString, headers);

        URI updateResponsesUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "updateResponses")
                .build()
                .toUri();

        ResponseEntity<String> response = studentRestTemplate.exchange(updateResponsesUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<String>() {});

        if (response != null && response.hasBody() && response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return null;
        }
    }

    PageContents getPageContent(int page, String groupID, String pageKey) {
        PageContents pageContents = null;

        try {
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
                logger.info("Successfully got page contents for page: " + page);
                pageContents = new PageContents(response.getBody(), page, pageKey);
            } else {
                logger.error("Failed to get page contents for page: " + page);
            }
        } catch (Exception ex) {
            logger.error("Exception getting page contents for page: " + page, ex);
        }

        return pageContents;
    }

    String exitSegment(int currentPageNumber, int segmentPosition) {

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("position", String.valueOf(segmentPosition));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI updateResponsesUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "exitSegment")
                .queryParam("currentPage", currentPageNumber)
                .build()
                .toUri();

        // Actually returns ExitSegment
        ResponseEntity<String> response = studentRestTemplate.exchange(updateResponsesUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<String>() {});

        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return null;
        }
    }

    boolean completeTest() {

        URI completeTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "completeTest")
                .build()
                .toUri();

        try {
            ResponseEntity<ResponseData<String>> response = studentRestTemplate.exchange(completeTestUri, HttpMethod.POST,
                    null, new ParameterizedTypeReference<ResponseData<String>>() {
                    });


            return response != null && response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException ex) {
            logger.error("Error sending completeTest");
        }

        return false;
    }

    boolean scoreTest() {

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("suppressScore", "false");
        form.add("itemScoreReportSummary", "false");
        form.add("itemScoreReportResponses", "false");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI scoreTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "scoreTest")
                .build()
                .toUri();

        try {
            // TODO: Replace String with ResponseData<TestSummary> if needed
            ResponseEntity<String> response = studentRestTemplate.exchange(scoreTestUri, HttpMethod.POST,
                    requestEntity, String.class);

            logger.info("Score Test Summary: {}", response != null ? response.getBody() : "");

            return response != null && response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException ex) {
            logger.error("Error sending scoreTest", ex);
        }

        return false;
    }

    private <T> boolean responseIsValid(ResponseEntity<ResponseData<T>> response) {
        return responseHasData(response) && response.getStatusCode() == HttpStatus.OK;
    }

    private <T> boolean responseHasData(ResponseEntity<ResponseData<T>> response) {
        return response != null && response.hasBody() && response.getBody().getData() != null;
    }

    /**
     * Clones (fixes up) the ApprovalInfo's Accommodations so that their internal Maps are populated with useful values
     * @param approvalInfo The ApprovalInfo to clone Accommodations
     * @return If there are Accommodations, the ApprovalInfo with updated Accommodations; otherwise the original input
     */
    ApprovalInfo cloneApprovalInfo(ApprovalInfo approvalInfo) {
        if (approvalInfo != null && approvalInfo.getSegmentsAccommodations() != null) {
            List<Accommodations> accommodations = approvalInfo.getSegmentsAccommodations();
            for (int i = 0; i < accommodations.size(); i++) {
                Accommodations accommodation = accommodations.get(i);

                if (accommodation.getTypes() != null) {
                    for (AccommodationType type : accommodation.getTypes()) {
                        type.setParentAccommodations(accommodation);

                        if (type.getValues() != null) {
                            for (AccommodationValue value : type.getValues()) {
                                value.setParentType(type);
                            }
                        }
                    }
                }

                accommodations.set(i, accommodation.clone());
            }
        }

        return approvalInfo;
    }

    private RetryOperations defaultRetryTemplate() {

        RetryPolicy retryPolicy = new NeverRetryPolicy();
        BackOffPolicy backOffPolicy = new NoBackOffPolicy();

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
