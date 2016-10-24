package org.cresst.sb.irp.automation.adapter.student.data;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UpdateResponsePageContents {
    private final static Logger logger = LoggerFactory.getLogger(UpdateResponsePageContents.class);

    private Document doc;

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

        NodeList pageNodes = (NodeList) xpath.compile("//page").evaluate(doc,  XPathConstants.NODESET);

        for (int i = 0; i < pageNodes.getLength(); i++) {
            String groupId = (String) xpath.compile("group/@id").evaluate(pageNodes.item(i), XPathConstants.STRING);
            String pageKey = (String) xpath.compile("group/@key").evaluate(pageNodes.item(i), XPathConstants.STRING);
            int pageNumber = Integer.parseInt(pageNodes.item(i).getAttributes().getNamedItem("number").getNodeValue());
            pages.put(pageNumber, new UpdateResponsePage(groupId, pageKey, pageNumber));
        }

        //int pageNumber = ((Number) xpath.compile("@number").evaluate(doc, XPathConstants.NUMBER)).intValue();

        int pageLength = ((NodeList) xpath.compile("//page").evaluate(doc, XPathConstants.NODESET)).getLength();
        logger.debug("Found {} pages", pageLength);
        //NodeList contentNodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        //if (contentNodeList.getLength() != 1) {
        //    return;
        //}

        //Element content = (Element) contentNodeList.item(0);
        //this.groupID = content.getAttribute("id");
        //this.pageKey = content.getAttribute("key");
    }

    public UpdateResponsePage getFirstPage () {
        return pages.get(Collections.min(pages.keySet()));
    }

    public Map<Integer, UpdateResponsePage> getPages() {
        return pages;
    }

    public void setPages(Map<Integer, UpdateResponsePage> pages) {
        this.pages = pages;
    }
}
