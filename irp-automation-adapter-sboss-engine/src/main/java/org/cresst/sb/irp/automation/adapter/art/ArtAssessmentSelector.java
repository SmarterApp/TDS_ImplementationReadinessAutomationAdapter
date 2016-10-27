package org.cresst.sb.irp.automation.adapter.art;

import org.cresst.sb.irp.automation.adapter.rollback.Rollbacker;
import org.cresst.sb.irp.automation.adapter.tsb.TestSpecBankData;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.opentestsystem.delivery.testreg.domain.Assessment;
import org.opentestsystem.delivery.testreg.domain.ImplicitEligibilityRule;
import org.opentestsystem.shared.search.domain.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArtAssessmentSelector implements Rollbacker {
    private final static Logger logger = LoggerFactory.getLogger(ArtAssessmentSelector.class);

    private final static int NUM_OPPORTUNITIES = 1000;
    private static final String IRP_SEARCH_STRING = "IRP-";

    private final AutomationRestTemplate automationRestTemplate;
    private final URI artAssessmentUri;
    private final String stateAbbreviation;

    private List<String> selectedAssessmentIds = new ArrayList<>();

    public ArtAssessmentSelector(AutomationRestTemplate automationRestTemplate, URL artUrl, String stateAbbreviation) {
        this.automationRestTemplate = automationRestTemplate;
        this.artAssessmentUri = UriComponentsBuilder.fromHttpUrl(artUrl.toString())
                .pathSegment("rest", "assessment")
                .build(true)
                .toUri();
        this.stateAbbreviation = stateAbbreviation;
    }

    /**
     * In ART, selects/registers the given Assessments that was side-loaded into TSB.
     * @param testSpecBankData The data that was side-loaded into the vendor's TSB
     * @return The number of assessments that were selected
     */
    public int selectAssessments(List<TestSpecBankData> testSpecBankData) {

        final DateTime testWindowStart = DateTime.now();
        final DateTime testWindowEnd = testWindowStart.plus(Duration.standardDays(2));

        selectedAssessmentIds.clear();
        for (TestSpecBankData tsbData : testSpecBankData) {
            Assessment assessment = constructAssessment(tsbData, stateAbbreviation, testWindowStart, testWindowEnd);

            HttpEntity<Assessment> request = SbossAutomationRestTemplate.constructHttpEntity(assessment);
            Assessment assessmentResponse = automationRestTemplate.postForObject(artAssessmentUri, request, Assessment.class);

            String assessmentId = assessmentResponse.getId();
            selectedAssessmentIds.add(assessmentId);

            logger.debug("Selected Assessment {} and received ID={}", assessment.getEntityId(), assessmentId);
        }

        return selectedAssessmentIds.size();
    }

    /**
     * Removes any existing IRP Test Packages that have been selected so they can be selected with new Test Windows.
     * @param testSpecBankData The Test Packages that were side-loaded into TSB
     */
    public int deleteExistingAssessments(List<TestSpecBankData> testSpecBankData) {

        int numberRemoved = 0;

        if (testSpecBankData == null || testSpecBankData.size() == 0) {
            return numberRemoved;
        }

        URI searchAssessmentUri = UriComponentsBuilder.fromUri(artAssessmentUri)
                .queryParam("currentPage", 0)
                .queryParam("pageSize", 100)
                .queryParam("entityId", IRP_SEARCH_STRING)
                .queryParam("sortDir", "asc")
                .queryParam("sortKey", "entityId")
                .queryParam("tenantId", testSpecBankData.get(0).getTenantId())
                .build(true)
                .toUri();

        UriComponents deleteAssessmentUriComponents = UriComponentsBuilder.fromUri(artAssessmentUri)
                .pathSegment("{assessmentId}")
                .build();

        try {
            SearchResponse<Assessment> searchResult = automationRestTemplate.exchange(searchAssessmentUri, HttpMethod.GET, null,
                    new ParameterizedTypeReference<SearchResponse<Assessment>>() {})
                    .getBody();

            if (searchResult != null && searchResult.getReturnCount() > 0) {
                Set<String> tsbNameSet = new HashSet<>();
                for (TestSpecBankData tsbData : testSpecBankData) {
                    tsbNameSet.add(tsbData.getName());
                }

                for (Assessment assessment : searchResult.getSearchResults()) {
                    if (tsbNameSet.contains(assessment.getTestName())) {
                        URI deleteAssessmentUri = deleteAssessmentUriComponents
                                .expand(assessment.getId())
                                .toUri();

                        automationRestTemplate.delete(deleteAssessmentUri);
                        numberRemoved++;
                    }
                }
            }

        } catch (RestClientException e) {
            logger.error("Unable to search for and delete existing IRP assessments", e);
        }

        return numberRemoved;
    }

    /**
     * Searches vendor's ART application for all the Test Packages that were loaded into TSB
     *
     * @param tenantId The Tenant ID
     * @param testSpecBankData The TSB Test Package data that was loaded into the vendor's TSB
     * @return true if all the TSB data has been selected successfully in ART; false otherwise.
     */
    public boolean verifySelectedAssessments(String tenantId, List<TestSpecBankData> testSpecBankData) {
        // ?currentPage=0&pageSize=15&entityId=IRP-&sortDir=asc&sortKey=entityId&tenantId=55661e17e4b0c4ebd30aa19f
        URI searchAssessmentUri = UriComponentsBuilder.fromUri(artAssessmentUri)
                .queryParam("currentPage", 0)
                .queryParam("pageSize", testSpecBankData.size())
                .queryParam("entityId", IRP_SEARCH_STRING)
                .queryParam("sortDir", "asc")
                .queryParam("sortKey", "entityId")
                .queryParam("tenantId", tenantId)
                .build(true)
                .toUri();

        SearchResponse<Assessment> searchResult = automationRestTemplate.exchange(searchAssessmentUri, HttpMethod.GET, null,
                new ParameterizedTypeReference<SearchResponse<Assessment>>() {}).getBody();

        if (searchResult.getReturnCount() != testSpecBankData.size()) {
            return false;
        }

        Set<String> assessmentEntityIdSet = new HashSet<>();
        for (Assessment assessment : searchResult.getSearchResults()) {
            assessmentEntityIdSet.add(assessment.getEntityId());
        }

        Set<String> tsbNameSet = new HashSet<>();
        for (TestSpecBankData tsbData : testSpecBankData) {
            tsbNameSet.add(tsbData.getName());
        }

        return assessmentEntityIdSet.containsAll(tsbNameSet);
    }

    @Override
    public void rollback() {

        UriComponents deleteAssessmentUriComponents = UriComponentsBuilder.fromUri(artAssessmentUri)
                .pathSegment("{assessmentId}")
                .build();

        for (String assessmentId : selectedAssessmentIds) {
            URI deleteAssessmentUri = deleteAssessmentUriComponents.expand(assessmentId).toUri();

            try {
                automationRestTemplate.delete(deleteAssessmentUri);
            } catch (RestClientException e) {
                logger.error("Unable to delete assessment (" + assessmentId + ")", e);
            }
        }

        logger.info("Rolled back ART Test Package selection");
    }

    /**
     * Example data to send
{
  "id":null,
  "entityId":"(SBAC_PT)IRP-Perf-ELA-11-Summer-2015-2016",
  "numGlobalOpportunities":"9999",
  "testWindow":[{
    "beginWindow":"2016-07-19T08:47:38.428Z",
    "endWindow":"2016-07-19T16:00:00.000Z",
    "numOpportunities":"9999",
    "beginWindowOpened":false,
    "endWindowOpened":false
    }],
  "delayRule":"0",
  "eligibilityType":"IMPLICIT",
  "implicitEligibilityRules":[{
    "field":"stateAbbreviation",
    "value":"CA",
    "operatorType":"EQUALS",
    "ruleType":"ENABLER"
    }],
  "subjectCode":"ELA",
  "testName":"(SBAC_PT)IRP-Perf-ELA-11-Summer-2015-2016",
  "version":"8185.0",
  "tenantId":"55661e17e4b0c4ebd30aa19f",
  "exists":false,
  "updatedVersion":false,
  "testForm":null,
  "grade":"11",
  "academicYear":"2016",
  "category":"",
  "type":"summative",
  "testLabel":"Grade 11 ELA",
  "atLeastOneImplicitRule":"",
  "sourceTsb":"https://tsb.smarterapp.cresst.net:8443/rest/",
  "alternateKey":"testName: (SBAC_PT)IRP-Perf-ELA-11-Summer-2015-2016, version: 8185.0, testLabel: Grade 11 ELA, sourceTsb: https://tsb.smarterapp.cresst.net:8443/rest/, tenantId: 55661e17e4b0c4ebd30aa19f",
  "formatType":"ASSESSMENT",
  "enablerRules":[],
  "disablerRules":[],
  "action":null,
  "url":"/assessment/null"
 }
     */
    private Assessment constructAssessment(final TestSpecBankData tsbData, final String stateAbbreviation,
                                           final DateTime testWindowStart, final DateTime testWindowEnd) {

        final String grade = tsbData.getGrade().get(0);

        Assessment assessment = new Assessment();

        assessment.setEntityId(tsbData.getName());
        assessment.setNumGlobalOpportunities(NUM_OPPORTUNITIES);
        assessment.setTestWindow(new Assessment.TestWindow[] {
                new Assessment.TestWindow(testWindowStart, testWindowEnd, NUM_OPPORTUNITIES)
        });
        assessment.setDelayRule(0);
        assessment.setEligibilityType(Assessment.EligibilityType.IMPLICIT);
        assessment.setImplicitEligibilityRules(new ImplicitEligibilityRule[] {
                new ImplicitEligibilityRule("stateAbbreviation", stateAbbreviation, ImplicitEligibilityRule.RuleType.ENABLER),
                new ImplicitEligibilityRule("gradeLevelWhenAssessed", grade, ImplicitEligibilityRule.RuleType.ENABLER)
        });
        assessment.setSubjectCode(tsbData.getSubjectAbbreviation());
        assessment.setTestName(tsbData.getName());
        assessment.setVersion(tsbData.getVersion());
        assessment.setTenantId(tsbData.getTenantId());
        assessment.setExists(false);
        assessment.setUpdatedVersion(false);
        assessment.setTestForm(null);
        assessment.setGrade(grade);
        assessment.setAcademicYear(Integer.toString(testWindowStart.getYear()));
        assessment.setCategory(tsbData.getCategory());
        assessment.setType(tsbData.getType());
        assessment.setTestLabel(tsbData.getLabel());

        return assessment;
    }
}
