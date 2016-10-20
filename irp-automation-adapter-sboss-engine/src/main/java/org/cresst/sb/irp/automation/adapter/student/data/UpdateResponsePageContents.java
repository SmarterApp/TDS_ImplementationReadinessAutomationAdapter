package org.cresst.sb.irp.automation.adapter.student.data;

import java.io.IOException;
import java.io.StringReader;

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

    private int pageNumber;
    private String groupID;
    private String pageKey;

    public UpdateResponsePageContents(String updateResp) {
        try {
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
        this.groupID = (String) xpath.compile("//group/@id").evaluate(doc, XPathConstants.STRING);
        this.pageKey = (String) xpath.compile("//group/@key").evaluate(doc, XPathConstants.STRING);
        this.setPageNumber(Integer.parseInt((String) xpath.compile("//page/@number").evaluate(doc, XPathConstants.STRING)));
        //NodeList contentNodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        //if (contentNodeList.getLength() != 1) {
        //    return;
        //}

        //Element content = (Element) contentNodeList.item(0);
        //this.groupID = content.getAttribute("id");
        //this.pageKey = content.getAttribute("key");
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getPageKey() {
        return pageKey;
    }

    public void setPageKey(String pageKey) {
        this.pageKey = pageKey;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
}
