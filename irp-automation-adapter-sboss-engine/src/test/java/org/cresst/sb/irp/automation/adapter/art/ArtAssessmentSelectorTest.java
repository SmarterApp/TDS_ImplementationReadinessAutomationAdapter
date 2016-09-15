package org.cresst.sb.irp.automation.adapter.art;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.accesstoken.AccessToken;
import org.cresst.sb.irp.automation.adapter.tsb.TestSpecBankData;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ArtAssessmentSelectorTest {

    @Ignore("Enable when data can be supplied to arguments")
    @Test
    public void selectAssessments() throws Exception {

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
}