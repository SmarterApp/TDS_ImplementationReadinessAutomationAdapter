package org.cresst.sb.irp.automation.adapter.art;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.accesstoken.AccessToken;
import org.cresst.sb.irp.automation.adapter.tsb.TestSpecBankData;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opentestsystem.delivery.testreg.domain.Assessment;
import org.opentestsystem.shared.search.domain.AbstractSearchRequest;
import org.opentestsystem.shared.search.domain.SearchFilter;
import org.opentestsystem.shared.search.domain.SearchResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArtAssessmentSelectorTest {

    @Mock
    private AutomationRestTemplate restTemplate;

    @Ignore("Enable when data can be supplied to arguments")
    @Test
    public void whenSelectAssessments_() throws Exception {

        AccessToken accessToken = AccessToken.buildAccessToken(
                new RestTemplate(),
                new URL(""),
                "",
                "",
                "",
                ""
        );

        List<TestSpecBankData> testNames = new ArrayList<>();
        TestSpecBankData testSpecBankData = new TestSpecBankData();
        testNames.add(testSpecBankData);

        testSpecBankData.setCategory("");
        testSpecBankData.setGrade(Lists.newArrayList("7"));
        testSpecBankData.setLabel("Grade 7 ELA");
        testSpecBankData.setName("(SBAC_PT)SBAC-IRP-CAT-ELA-7-Summer-2015-2016");
        testSpecBankData.setSubjectAbbreviation("ELA");
        testSpecBankData.setVersion("8085.0");
        testSpecBankData.setTenantId("55661e17e4b0c4ebd30aa19f");
        testSpecBankData.setType("summative");


        AutomationRestTemplate restTemplate = new SbossAutomationRestTemplate();
        restTemplate.addAccessToken(accessToken);
        ArtAssessmentSelector sut = new ArtAssessmentSelector(restTemplate,
                new URL(""),
                "CA");
        sut.selectAssessments(testNames);
    }

    @Test
    public void whenTestPackageAlreadySelected_RemoveSelectedTestPackage() throws Exception {

        final String BASE_ART_URL = "http://art.server.net/";
        final String TEST_ID = "1234567890";
        final String TEST_NAME = "(SBAC_PT)SBAC-IRP-CAT-ELA-7-Summer-2015-2016";
        final String TEST_VERSION = "8085.0";
        final String TENANT_ID = "55661e17e4b0c4ebd30aa19f";

        // Setup
        List<TestSpecBankData> tsbDataCollection = new ArrayList<>();
        TestSpecBankData testSpecBankData = new TestSpecBankData();
        tsbDataCollection.add(testSpecBankData);

        testSpecBankData.setCategory("");
        testSpecBankData.setGrade(Lists.newArrayList("7"));
        testSpecBankData.setLabel("Grade 7 ELA");
        testSpecBankData.setName(TEST_NAME);
        testSpecBankData.setSubjectAbbreviation("ELA");
        testSpecBankData.setVersion(TEST_VERSION);
        testSpecBankData.setTenantId(TENANT_ID);
        testSpecBankData.setType("summative");

        Assessment assessment1 = new Assessment(TEST_NAME, TEST_VERSION, TENANT_ID);
        assessment1.setId(TEST_ID);
        assessment1.setEntityId(TEST_ID);

        Assessment assessment2 = new Assessment("NON TSB TEST", TEST_VERSION, TENANT_ID);
        assessment2.setId("NON TSB TEST NAME");
        assessment2.setEntityId("NON TSB TEST ENTITY ID");

        List<Assessment> assessments = new ArrayList<>();
        assessments.add(assessment1);
        assessments.add(assessment2);

        SearchResponse<Assessment> searchResponse = new SearchResponse<>(assessments,
                new AbstractSearchRequest(new HashMap<String, String[]>()) {
                    @Override
                    protected List<SearchFilter> getSearchFilters() {
                        return new ArrayList<>();
                    }

                    @Override
                    protected String getSearchResource() {
                        return "";
                    }
                },
                1);

        ResponseEntity<SearchResponse<Assessment>> responseEntity = new ResponseEntity<>(searchResponse, HttpStatus.OK);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), (HttpEntity)any(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // Test
        ArtAssessmentSelector sut = new ArtAssessmentSelector(restTemplate, new URL(BASE_ART_URL), "CA");
        sut.deleteExistingAssessments(tsbDataCollection);

        // Assert
        verify(restTemplate).delete(new URI(BASE_ART_URL + "rest/assessment/" + TEST_ID));
    }
}