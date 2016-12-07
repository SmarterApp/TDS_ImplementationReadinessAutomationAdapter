package org.cresst.sb.irp.automation.adapter.student;

import org.cresst.sb.irp.automation.adapter.student.data.PageContents;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class UpdateResponsesBuilderTest {

    private StudentResponseGenerator responseGenerator;
    private final String studentTestData = "ItemID\tResponse\n" +
            "1\t<![CDATA[a]]>\n" +
            "2700\t<![CDATA[b]]>\n" +
            "2701\t<![CDATA[c]]>\n" +
            "2702\t<![CDATA[test]]>\n" +
            "411\t<![CDATA[testing]]>\n";
    private SortedSet<PageContents> pages;

    public UpdateResponsesBuilderTest() throws Exception {
        // Load the sample page contents response
        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(classLoader.getResource("getPageContent_sample_response.xml").getFile());
        File file2 = new File(classLoader.getResource("getPageContent_sample_response2.xml").getFile());

        PageContents pageContents = new PageContents(file, 1, "a150e622-24c8-4f4d-84f1-acd92ea33adc");
        PageContents pageContents2 = new PageContents(file2, 2, "8e03f3ed-3c08-4556-9e80-46a165a017b8");

        pages = new TreeSet<>(new Comparator<PageContents>() {
            @Override
            public int compare(PageContents o1, PageContents o2) {
                return o1.getPageNumber() - o2.getPageNumber();
            }
        });
        pages.add(pageContents);
        pages.add(pageContents2);

        responseGenerator = new StudentResponseGenerator(studentTestData);
    }

    @Test
    public void createRequest_WithRandomResponse() throws Exception {

        Document request = UpdateResponsesBuilder.createRequest(responseGenerator, null, pages, 1);

        XPath xPath = XPathFactory.newInstance().newXPath();

        String page1 = (String) xPath.evaluate("/request/responses/response[@id='187-2700']/@page", request, XPathConstants.STRING);
        String page2 = (String) xPath.evaluate("/request/responses/response[@id='200-411']/@page", request, XPathConstants.STRING);

        String pageKey1 = (String) xPath.evaluate("/request/responses/response[@id='187-2700']/@pageKey", request, XPathConstants.STRING);
        String pageKey2 = (String) xPath.evaluate("/request/responses/response[@id='200-411']/@pageKey", request, XPathConstants.STRING);

        assertEquals("1", page1);
        assertEquals("2", page2);
        assertEquals("a150e622-24c8-4f4d-84f1-acd92ea33adc", pageKey1);
        assertEquals("8e03f3ed-3c08-4556-9e80-46a165a017b8", pageKey2);
    }

}
