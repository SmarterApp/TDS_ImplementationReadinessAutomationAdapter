package org.cresst.sb.irp.automation.adapter.student.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UpdateResponsePageContents {
    private final static Logger logger = LoggerFactory.getLogger(UpdateResponsePageContents.class);

    private Document doc;

    private boolean finished;
    private Map<Integer, UpdateResponsePage> pages;

    public UpdateResponsePageContents(String updateResp) {
        try {
            pages = new HashMap<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(updateResp));
            this.doc = builder.parse(is);
            parseXml();
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            logger.error(e.getMessage());
        }
    }

    private void parseXml() throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        String finishedString = (String) xpath.evaluate("//testsummary/@finished", doc, XPathConstants.STRING);
        if(finishedString != null) {
            this.finished = Boolean.valueOf(finishedString);
        }
        NodeList pageNodes = (NodeList) xpath.compile("//page").evaluate(doc,  XPathConstants.NODESET);

        for (int i = 0; i < pageNodes.getLength(); i++) {
            String groupId = (String) xpath.compile("group/@id").evaluate(pageNodes.item(i), XPathConstants.STRING);
            String pageKey = (String) xpath.compile("group/@key").evaluate(pageNodes.item(i), XPathConstants.STRING);
            int pageNumber = Integer.parseInt(pageNodes.item(i).getAttributes().getNamedItem("number").getNodeValue());
            pages.put(pageNumber, new UpdateResponsePage(groupId, pageKey, pageNumber));
        }

        int pageLength = ((NodeList) xpath.compile("//page").evaluate(doc, XPathConstants.NODESET)).getLength();
        logger.debug("Found {} pages", pageLength);
    }

    public UpdateResponsePage getFirstPage () {
        if (pageCount() <= 0) {
            return null;
        }
        return pages.get(Collections.min(pages.keySet()));
    }

    public Map<Integer, UpdateResponsePage> getPages() {
        return pages;
    }

    public void setPages(Map<Integer, UpdateResponsePage> pages) {
        this.pages = pages;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "UpdateResponsePageContents [doc=" + doc + ", finished=" + finished + ", pages=" + pages + "]";
    }

    public int pageCount() {
        return pages.keySet().size();
    }

    public UpdateResponsePage getLastPage() {
        if (pageCount() <= 0) {
            return null;
        }
        return pages.get(Collections.max(pages.keySet()));
    }

}
