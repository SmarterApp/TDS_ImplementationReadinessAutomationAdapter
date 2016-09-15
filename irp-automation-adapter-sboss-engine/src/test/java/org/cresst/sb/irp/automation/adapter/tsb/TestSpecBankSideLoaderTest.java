package org.cresst.sb.irp.automation.adapter.tsb;

import org.cresst.sb.irp.automation.adapter.accesstoken.AccessToken;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.cresst.sb.irp.automation.testhelpers.IntegrationTests;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.PathResource;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSpecBankSideLoaderTest {

    @Ignore("Further test harness structure needs to be build out to get test parameters")
    @Category(IntegrationTests.class)
    @Test
    public void sideLoadRegistrationTestPackagesTest() throws Exception {

        AccessToken accessToken = AccessToken.buildAccessToken(
                new RestTemplate(),
                new URL(""),
                "",
                "",
                "",
                ""
        );

        Path artRegistrationTestPackages = Paths.get("");
        AutomationRestTemplate restTemplate = new SbossAutomationRestTemplate();
        restTemplate.addAccessToken(accessToken);
        TestSpecBankSideLoader sut = new TestSpecBankSideLoader(new PathResource(artRegistrationTestPackages),
                restTemplate,
                new URL(""),
                "");

        sut.sideLoadRegistrationTestPackages();
    }

}