package org.cresst.sb.irp.automation.adapter.proctor;

import TDS.Shared.Data.ReturnStatus;
import org.apache.commons.lang3.StringUtils;
import org.cresst.sb.irp.automation.adapter.accesstoken.AccessToken;

import org.cresst.sb.irp.automation.adapter.proctor.data.*;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SbossProctor implements Proctor {
    private final static Logger logger = LoggerFactory.getLogger(SbossProctor.class);

    private AutomationRestTemplate proctorRestTemplate;
    private URL proctorUrl;
    private SessionDTO sessionDTO;
    private boolean loggedIn;

    public SbossProctor(AutomationRestTemplate accessTokenRestTemplate,
                        AutomationRestTemplate proctorRestTemplate,
                        URL oAuthUrl,
                        URL proctorUrl,
                        String clientId,
                        String clientSecret,
                        String proctorUserId,
                        String proctorPassword) {

        this.proctorUrl = proctorUrl;

        AccessToken proctorAccessToken = AccessToken.buildAccessToken(accessTokenRestTemplate,
                oAuthUrl,
                clientId,
                clientSecret,
                proctorUserId,
                proctorPassword);

        this.proctorRestTemplate = proctorRestTemplate;
        proctorRestTemplate.addAccessToken(proctorAccessToken);
    }

    /**
     * Logins in the Proctor by getting the initial Session data
     * @return True if login was successful; otherwise false.
     */
    @Override
    public boolean login() {

        try {
            List<String> cookies = getCookies();
            proctorRestTemplate.setCookies(cookies);

            final URI getInitDataUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString())
                    .pathSegment("Services", "XHR.axd", "GetInitData")
                    .build()
                    .toUri();

            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();

            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.setRetryPolicy(retryPolicy);
            retryTemplate.setBackOffPolicy(backOffPolicy);

            sessionDTO = retryTemplate.execute(new RetryCallback<SessionDTO, RestClientException>() {
                @Override
                public SessionDTO doWithRetry(RetryContext retryContext) throws RestClientException {
                    SessionDTO sessionDTO = null;
                    ResponseEntity<SessionDTO> response = proctorRestTemplate.getForEntity(getInitDataUri, SessionDTO.class);

                    if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                        sessionDTO = response.getBody();
                    }

                    if (sessionDTO == null || sessionDTO.getTests() == null) {
                        logger.info("Call to GetInitData succeeded with an uninitialized session");
                        throw new RestClientException("Session is not initialized");
                    }
                    return sessionDTO;
                }
            });

            return loggedIn = sessionDTO != null && sessionDTO.getTests() != null;
        } catch (RestClientException ex) {
            logger.info("Unable to login as proctor", ex);
        }

        return false;
    }

    @Override
    public boolean logout() {
        if (loggedIn) {
            URI logoutUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString())
                    .path("/shared/logout.xhtml")
                    .query("exl=true")
                    .build()
                    .toUri();

            try {
                proctorRestTemplate.getForEntity(logoutUri, String.class);
            } catch (RestClientException ex) {
                logger.info("Unable to logout proctor", ex);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean startTestSession(Set<String> irpTestKeys) {

        try {
            URI insertSessionTestsUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString())
                    .pathSegment("Services", "XHR.axd", "InsertSessionTests")
                    .build()
                    .toUri();

            List<String> testKeys = new ArrayList<>();
            List<String> testIDs = new ArrayList<>();

            for (Test test : sessionDTO.getTests()) {
                if (irpTestKeys.contains(test.get_key())) {
                    testKeys.add(test.get_key());
                    testIDs.add(test.getId());
                }
            }

            MultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
            postBody.add("testKeys", StringUtils.join(testKeys, "|"));
            postBody.add("testIDs", StringUtils.join(testIDs, "|"));

            ResponseEntity<SessionDTO> response = proctorRestTemplate.postForEntity(insertSessionTestsUri, postBody, SessionDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                sessionDTO = response.getBody();
                return sessionDTO.getSession() != null && sessionDTO.getSession().getId() != null;
            }
        } catch (RestClientException ex) {
            logger.info("Unable to start test session", ex);
        }

        return false;
    }

    @Override
    public boolean pauseTestSession() {
        if (sessionDTO == null || sessionDTO.getSession() == null || sessionDTO.getSession().getKey() == null) {
            return false;
        }

        URI pauseSessionUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString())
                .pathSegment("Services", "XHR.axd", "PauseSession")
                .build()
                .toUri();

        MultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
        postBody.add("sessionKey", sessionDTO.getSession().getKey().toString());

        try {
            ResponseEntity<SessionDTO> response = proctorRestTemplate.postForEntity(pauseSessionUri, postBody, SessionDTO.class);

            if (response != null && response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                sessionDTO = response.getBody();
                return sessionDTO.getSession() != null && sessionDTO.getSession().getId() != null;
            }
        } catch (RestClientException ex) {
            logger.info("Unable to pause test session", ex);
        }

        return false;
    }

    @Override
    public boolean approveTestOpportunity(String sessionKey, String oppId, String accs) {
        try {
            URI approveOpportunityUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString())
                    .pathSegment("Services", "XHR.axd", "ApproveOpportunity")
                    .build()
                    .toUri();

            MultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
            postBody.add("sessionKey", sessionKey);
            postBody.add("oppKey", oppId);
            postBody.add("accs", accs);

            ResponseEntity<ReturnStatus> response = proctorRestTemplate.postForEntity(approveOpportunityUri, postBody, ReturnStatus.class);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                if (response != null && response.hasBody()) {
                    logger.info("Opportunity Approval failed because {}", response.getBody().getReason());
                } else {
                    logger.info("Opportunity Approval failed for unknown reason.");
                }

                return false;
            }

            return true;
        } catch (RestClientException ex) {
            logger.info("Unable to approve opportunity", ex);
        }

        return false;
    }

    @Override
    public boolean autoRefreshData() {
        ResponseEntity<SessionDTO> response = null;

        try {
            URI autoRefreshDataUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString())
                    .pathSegment("Services", "XHR.axd", "AutoRefreshData")
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String sessionKey = getSessionKey();
            MultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
            postBody.add("bGetCurTestees", "true");
            //postBody.add("sessionKey", sessionKey);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(postBody, headers);

            //proctorRestTemplate.exchange
            //ResponseEntity<SessionDTO> response = proctorRestTemplate.postForEntity(autoRefreshDataUri, requestEntity, SessionDTO.class);
            response = proctorRestTemplate.exchange(autoRefreshDataUri, HttpMethod.POST,
                    requestEntity, SessionDTO.class);

            if (response != null && response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
               sessionDTO = response.getBody();

                logger.info("Found " + response.getBody().getApprovalOpps().getSize() + " tests to approve.");
                return sessionDTO.getSession() != null && sessionDTO.getSession().getId() != null;
            } else {
                if (response == null) {
                    logger.error("Unable to AutoRefreshData due to null response");
                } else if (response.getStatusCode() != HttpStatus.OK) {
                    logger.error("Unable to AutoRefreshData due to: " + response.getStatusCode().toString() + " status code.");
                } else if (!response.hasBody()) {
                    logger.error("Unable to AutoRefreshData due to empty body in response");
                }
            }
        } catch (RestClientException ex) {
            logger.error("Unable to AutoRefreshData. Reason: " + ex.getMessage() + ". " + ex.getCause().toString());
            if(response != null) {
                logger.error("Response: " + response.toString());
            }
        }
        return false;
    }

    // Populates sessionDTO with request from /Services/XHR.axd/GetApprovalOpps
    private boolean getApprovalOpps() {
        try {
            URI getApprovalOppsUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString())
                    .pathSegment("Services", "XHR.axd", "GetApprovalOpps")
                    .build()
                    .toUri();

            String sessionKey = getSessionKey();
            MultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
            postBody.add("sessionKey", sessionKey);

            ResponseEntity<SessionDTO> response = proctorRestTemplate.postForEntity(getApprovalOppsUri, postBody, SessionDTO.class);
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                sessionDTO = response.getBody();
                return sessionDTO.getApprovalOpps() != null;
            }
        } catch (RestClientException ex) {
            logger.info("Unable to get approval opportunities. Reason: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean approveAllTestOpportunities() {
        String sessionKey = getSessionKey();
        if (sessionDTO == null) return false;

        // Populate sessionDTO with approval opportunities
        boolean getApprovalStatus = getApprovalOpps();
        if(!getApprovalStatus) return false;

        TestOpps testOpps = sessionDTO.getApprovalOpps();

        String oppId;
        String accs;
        // Return false if all approvals fail
        boolean testApproveResult = false;
        for(TestOpportunity testOpp : testOpps) {
            oppId = testOpp.getOppKey().toString();
            accs = testOpp.getAccs();

            // Update overall approval status with attempt to approve test opportunity
            testApproveResult = testApproveResult || approveTestOpportunity(sessionKey, oppId, accs);
        }
        return testApproveResult;
    }

    private boolean isValidSession() {
        return sessionDTO.getSession() != null && sessionDTO.getSession().getId() != null;
    }

    @Override
    public String getSessionId() {
        return sessionDTO != null && sessionDTO.getSession() != null ? sessionDTO.getSession().getId() : null;
    }

    private String getSessionKey() {
        return sessionDTO != null && sessionDTO.getSession() != null ? sessionDTO.getSession().getKey().toString() : null;
    }

    /**
     * Gets HTTP Cookies returned from the Proctor application so operations against the Proctor application can be done
     * @return A list of cookies if they exist
     */
    private List<String> getCookies() {

        URI proctorUri = UriComponentsBuilder.fromHttpUrl(proctorUrl.toString()).build(true).toUri();

        ResponseEntity<String> response = proctorRestTemplate.getForEntity(proctorUri, String.class);

        List<String> rawCookies = response.getHeaders().get("Set-Cookie");

        return rawCookies;
    }
}
