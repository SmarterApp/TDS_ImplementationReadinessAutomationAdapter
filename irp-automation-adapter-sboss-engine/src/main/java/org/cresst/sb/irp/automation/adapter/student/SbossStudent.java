package org.cresst.sb.irp.automation.adapter.student;

import AIR.Common.data.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.cresst.sb.irp.automation.adapter.student.data.ApprovalInfo;
import org.cresst.sb.irp.automation.adapter.student.data.LoginInfo;
import org.cresst.sb.irp.automation.adapter.student.data.OpportunityInfoJsonModel;
import org.cresst.sb.irp.automation.adapter.student.data.PageContents;
import org.cresst.sb.irp.automation.adapter.student.data.TestSelection;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.List;


/**
@author Ernesto De La Luz Martinez
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
            		requestEntity, new ParameterizedTypeReference<ResponseData<LoginInfo>>() {});

            if (responseIsValid(response)) {
                List<String> rawCookies = response.getHeaders().get("Set-Cookie");
                studentRestTemplate.setCookies(rawCookies);

                loginInfo = response.getBody().getData();

                return true;
            }
        } catch (RestClientException e) {
            logger.error("Error while logging in student: {}. Message: {}", keyValues, e.getMessage());
        }

        return false;
	}

	/**
	 *
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
                    requestEntity, new ParameterizedTypeReference<ResponseData<OpportunityInfoJsonModel>>() {});

            List<String> rawCookies = response.getHeaders().get("Set-Cookie");
            studentRestTemplate.setCookies(rawCookies);
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
        form.add("formKey", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        URI starTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "startTest")
                .build()
                .toUri();


        try {
            //ResponseEntity<ResponseData<OpportunityInfoJsonModel>> response = studentRestTemplate.exchange(openTestUri, HttpMethod.POST,
            //        requestEntity, new ParameterizedTypeReference<ResponseData<OpportunityInfoJsonModel>>() {});
            ResponseEntity<String> response = studentRestTemplate.exchange(starTestUri, HttpMethod.POST,
                    requestEntity, String.class);

            List<String> rawCookies = response.getHeaders().get("Set-Cookie");
            studentRestTemplate.setCookies(rawCookies);

            logger.info(response.getBody());
            return true;//responseIsValid(response);
        } catch (RestClientException e) {
            logger.error("Could not start test selection. Reason: {}", e.getMessage());
            return false;
        }
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

    private PageContents getPageContent(int page) {
	    URI completeTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "getPageContent")
                .queryParam("page", page)
                .queryParam("attempt", 1)
                .build()
                .toUri();

        ResponseEntity<ResponseData<String>> response = studentRestTemplate.exchange(completeTestUri, HttpMethod.POST,
                HttpEntity.EMPTY, new ParameterizedTypeReference<ResponseData<String>>() {});

        List<String> rawCookies = response.getHeaders().get("Set-Cookie");
        studentRestTemplate.setCookies(rawCookies);

        if (responseIsValid(response)) {
            return new PageContents(response.getBody().getData());
        } else {
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
                requestEntity, new ParameterizedTypeReference<ResponseData<String>>() {});

        List<String> rawCookies = response.getHeaders().get("Set-Cookie");
        studentRestTemplate.setCookies(rawCookies);

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
                requestEntity, new ParameterizedTypeReference<ResponseData<String>>() {});

        List<String> rawCookies = response.getHeaders().get("Set-Cookie");
        studentRestTemplate.setCookies(rawCookies);

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

        ResponseEntity<ResponseData<ApprovalInfo>> response = studentRestTemplate.exchange(checkApprovalUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<ApprovalInfo>>() {});

        List<String> rawCookies = response.getHeaders().get("Set-Cookie");
        studentRestTemplate.setCookies(rawCookies);

        return responseIsValid(response) && approvalAccepted(response.getBody().getData());
	}

	/**
	 * Calls `getPageContents` and randomly answers questions
	 * based on `responseFile` answers
	 * @param page to automatically fill responses for
	 * @param accs the student's accessibilities
	 * @return the xml response to the call to `updateResponses`
	 */
	@Override
	public String updateResponsesForPage(int page, String accs) {
	    PageContents pageContents = getPageContent(page);
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

        ResponseEntity<ResponseData<String>> response = studentRestTemplate.exchange(updateResponsesUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<String>>() {});

        List<String> rawCookies = response.getHeaders().get("Set-Cookie");
        studentRestTemplate.setCookies(rawCookies);

        if (responseIsValid(response)) {
            return response.getBody().getData();
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

        URI openTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "getTests")
                .build()
                .toUri();

        ResponseEntity<ResponseData<List<TestSelection>>> response = studentRestTemplate.exchange(openTestUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<List<TestSelection>>>() {});

        List<String> rawCookies = response.getHeaders().get("Set-Cookie");
        studentRestTemplate.setCookies(rawCookies);

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
