package org.cresst.sb.irp.automation.adapter.student.data;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents the page contents from getPageContents call
 */
public class PageContents {
    private Document doc;
    private String groupId;
    private String segmentId;
    private String layout;
    private String language;
    private ArrayList<PageItem> pageItems = new ArrayList<>();
    private String pageKey;
    private int pageNumber;

    private static XPathExpression groupIdExpression;
    private static XPathExpression groupKeyExpression;
    private static XPathExpression contentExpression;
    private static XPathExpression itemsExpression;

    static {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        try {
            groupIdExpression = xpath.compile("//group/@id");
            groupKeyExpression = xpath.compile("//group/@key");
            contentExpression = xpath.compile("/contents/content");
            itemsExpression = xpath.compile("//item");
        } catch (XPathExpressionException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public PageContents(String xmlString, int pageNumber, String pageKey) throws Exception {
        this.pageNumber = pageNumber;
        this.pageKey = pageKey;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlString));
        this.doc = builder.parse(is);
        parseXml();
    }

    public PageContents(File xmlFile, int pageNumber, String pageKey) throws Exception {
        this.pageNumber = pageNumber;
        this.pageKey = pageKey;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        this.doc = builder.parse(xmlFile);
        parseXml();
    }

    public PageContents(int pageNumber, String pageKey) {
        this.pageNumber = pageNumber;
        this.pageKey = pageKey;
    }

    private void parseXml() throws Exception {
        parseContent();
        parsePageItems();
    }

    private void parseContent() throws Exception {

        this.groupId = (String) groupIdExpression.evaluate(doc, XPathConstants.STRING);

        String tempPageKey = (String) groupKeyExpression.evaluate(doc, XPathConstants.STRING);
        if (tempPageKey != null && !tempPageKey.isEmpty()) this.pageKey = tempPageKey;
        //this.pageNumber = (String) xpath.compile("//page/@number").evaluate(doc, XPathConstants.STRING);

        NodeList contentNodeList = (NodeList) contentExpression.evaluate(doc, XPathConstants.NODESET);
        if (contentNodeList.getLength() != 1) {
            throw new Exception("Found more than one content when parsing getPageContent");
        }

        Element content = (Element) contentNodeList.item(0);
        if (this.groupId == null || this.groupId.equals("")) {
            this.groupId = content.getAttribute("groupID");
        }

        this.segmentId = content.getAttribute("segmentID");
        this.layout = content.getAttribute("layout");
        this.language = content.getAttribute("language");
    }

    private void parsePageItems() throws Exception {

        NodeList items = (NodeList) itemsExpression.evaluate(doc, XPathConstants.NODESET);
        PageItem currItem;
        for (int i = 0; i < items.getLength(); i++) {
            currItem = new PageItem(items.item(i), this.pageKey, this.groupId, this.pageNumber);
            pageItems.add(currItem);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageContents that = (PageContents) o;
        return pageNumber == that.pageNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber);
    }

    @Override
    public String toString() {
        return "PageContents [doc=" + doc + ", groupId=" + groupId + ", segmentId=" + segmentId + ", layout=" + layout
                + ", language=" + language + ", pageItems=" + pageItems + ", pageKey=" + pageKey + ", pageNumber="
                + pageNumber + "]";
    }
}
