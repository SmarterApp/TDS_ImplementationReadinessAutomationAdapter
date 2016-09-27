package org.cresst.sb.irp.automation.adapter.student.data;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Represents the page contents from getPageContents call
 *
 */
public class PageContents {
    private Document doc;
    private String groupId;
    private String segmentId;
    private String layout;
    private String language;
    private ArrayList<PageItem> pageItems = new ArrayList<>();

    public PageContents(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            this.doc = builder.parse(is);
            parseXml();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public PageContents(File xmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            this.doc = builder.parse(xmlFile);
            parseXml();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private boolean parseXml() {
        return parseContent() && parsePageItems();
    }

    private boolean parseContent() {
        try {
           XPathFactory xPathfactory = XPathFactory.newInstance();
           XPath xpath = xPathfactory.newXPath();
           XPathExpression expr = xpath.compile("/contents/content");
           NodeList contentNodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
           if (contentNodeList.getLength() != 1) {
               return false;
           }
           Element content = (Element) contentNodeList.item(0);
           this.groupId = content.getAttribute("groupID");
           this.segmentId = content.getAttribute("segmentID");
           this.layout = content.getAttribute("layout");
           this.language = content.getAttribute("language");
           return true;
        } catch (XPathExpressionException e) {
            return false;
        }
    }

    private boolean parsePageItems() {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/contents/content/items/item");
            NodeList items = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            PageItem currItem;
            for(int i = 0; i < items.getLength(); i++) {
                currItem = new PageItem(items.item(i));
                pageItems.add(currItem);
            }
            return true;
        } catch (XPathExpressionException e) {
            return false;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public ArrayList<PageItem> getPageItems() {
        return pageItems;
    }

    public void setPageItems(ArrayList<PageItem> pageItems) {
        this.pageItems = pageItems;
    }
}
