package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.student.data.PageContents;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class UpdateResponsesBuilderTest {
    private StudentResponseGenerator responseService;
    private final String studentTestData = "ItemID Response\n1 <![CDATA[a]]\n" +
            "2700 <![CDATA[b]]\n" +
            "2701 <![CDATA[c]]\n" +
            "2702 <![CDATA[test]]";
    private PageContents pageContents;

    @Before
    public void setUp() throws Exception {
        // Load the sample page contents response
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("getPageContent_sample_response.xml").getFile());
        pageContents = new PageContents(file, 1);

        responseService = new StudentResponseGenerator(studentTestData);
    }

    @Test
    public void createRequest_WithRandomResponse() {
        Document request = UpdateResponsesBuilder.createRequest(responseService, "![CDATA[language:ENU;Other:TDS_Other;Print Size:TDS_PS_L3]]", pageContents);
        assertNotNull(request);
    }

}
