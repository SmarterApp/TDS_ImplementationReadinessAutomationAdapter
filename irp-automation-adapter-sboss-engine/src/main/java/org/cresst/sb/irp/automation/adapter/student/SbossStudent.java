package org.cresst.sb.irp.automation.adapter.student;

import AIR.Common.data.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.cresst.sb.irp.automation.adapter.student.data.ApprovalInfo;
import org.cresst.sb.irp.automation.adapter.student.data.LoginInfo;
import org.cresst.sb.irp.automation.adapter.student.data.OpportunityInfoJsonModel;
import org.cresst.sb.irp.automation.adapter.student.data.TestSelection;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

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

    public SbossStudent(AutomationRestTemplate studentRestTemplate, URL studentBaseUrl) {
        this.studentRestTemplate = studentRestTemplate;
        this.studentBaseUrl = studentBaseUrl;
        this.responseService = new StudentResponseService();
    }

    public SbossStudent(AutomationRestTemplate studentRestTemplate, URL studentBaseUrl, String responseFile) {
        this.studentRestTemplate = studentRestTemplate;
        this.studentBaseUrl = studentBaseUrl;
        try {
            this.responseService = new StudentResponseService(new FileInputStream(responseFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

	@Override
	public boolean login(String sessionID, String keyValues, String forbiddenApps) {
		logger.info("Student login started for {}", keyValues);

		MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
		form.add("sessionID", sessionID);
		form.add("keyValues", keyValues);
		form.add("forbiddenApps", forbiddenApps);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.set("Content-Type", "application/x-www-form-urlencoded");

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI loginStudentUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "loginStudent")
                .build()
                .toUri();

		// LoginInfo class needs to be completed
		ResponseEntity<ResponseData<LoginInfo>> response = studentRestTemplate.exchange(loginStudentUri, HttpMethod.POST,
				requestEntity, new ParameterizedTypeReference<ResponseData<LoginInfo>>() {});

        if (response.getStatusCode() == HttpStatus.OK && response.hasBody() && response.getBody().getData() != null) {
            List<String> rawCookies = response.getHeaders().get("Set-Cookie");
            studentRestTemplate.setCookies(rawCookies);

            loginInfo = response.getBody().getData();

            return true;
        }

        return false;
	}

	@Override
	public boolean startTestSession(String testKey, String testId) {

        TestSelection testSelection = getTestSelection(testKey, testId);

        if (testSelection == null) {
            logger.info("Unable to get available Tests");
            return false;
        }

		MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
		form.add("testKey", testKey);
		form.add("testID", testId);
		form.add("grade", testSelection.getGrade());
		form.add("subject", testSelection.getSubject());

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.set("Content-Type", "application/x-www-form-urlencoded");

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI openTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "openTest")
                .build()
                .toUri();

		ResponseEntity<ResponseData<OpportunityInfoJsonModel>> response = studentRestTemplate.exchange(openTestUri, HttpMethod.POST,
				requestEntity, new ParameterizedTypeReference<ResponseData<OpportunityInfoJsonModel>>() {});

        return responseIsValid(response);
	}

	@Override
	public boolean scoreTest(String testKey, String testId) {
       MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);
        form.add("testID", testId);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI scoreTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "scoreTest")
                .build()
                .toUri();

        // TODO: Replace ResponseData<String> with TestSummary if needed
        ResponseEntity<ResponseData<String>> response = studentRestTemplate.exchange(scoreTestUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<String>>() {});

        return responseIsValid(response);
	}

   @Override
    public boolean completeTest(String testKey, String testId) {
       MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);
        form.add("testID", testId);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI completeTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "TestShell.axd", "completeTest")
                .build()
                .toUri();

        ResponseEntity<ResponseData<String>> response = studentRestTemplate.exchange(completeTestUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<String>>() {});

        return responseIsValid(response);
    }

	@Override
	public boolean checkApproval(String testKey) {
	    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testKey", testKey);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI checkApprovalUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "checkApproval")
                .build()
                .toUri();

        ResponseEntity<ResponseData<ApprovalInfo>> response = studentRestTemplate.exchange(checkApprovalUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<ApprovalInfo>>() {});

        return responseIsValid(response) && approvalAccepted(response.getBody().getData());
	}

    private TestSelection getTestSelection(String testKey, String testId) {

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("testeeKey", loginInfo.getTestee().getKey());
        form.add("testeeToken", loginInfo.getTestee().getToken());
        form.add("sessionKey", loginInfo.getSession().getKey());
        form.add("grade", loginInfo.getTestee().getGrade());

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);

        URI openTestUri = UriComponentsBuilder.fromHttpUrl(studentBaseUrl.toString())
                .pathSegment("Pages", "API", "MasterShell.axd", "getTests")
                .build()
                .toUri();

        ResponseEntity<ResponseData<List<TestSelection>>> response = studentRestTemplate.exchange(openTestUri, HttpMethod.POST,
                requestEntity, new ParameterizedTypeReference<ResponseData<List<TestSelection>>>() {});

        TestSelection testSelection = null;

        if (responseIsValid(response)) {

            List<TestSelection> availableTests = response.getBody().getData();

            for (TestSelection availableTest : availableTests) {
                if (StringUtils.equalsIgnoreCase(availableTest.getTestKey(), testKey) &&
                        StringUtils.equalsIgnoreCase(availableTest.getTestID(), testId)) {

                    testSelection = availableTest;
                    break;
                }
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
