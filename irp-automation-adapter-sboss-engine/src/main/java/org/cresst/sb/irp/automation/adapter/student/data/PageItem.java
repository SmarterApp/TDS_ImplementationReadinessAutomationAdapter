package org.cresst.sb.irp.automation.adapter.student.data;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * Representation of an item from getPageContents xml
 */
public class PageItem {
    private String bankKey;
    private String itemKey;
    private String subject;
    private String grade;
    private String format;
    private boolean marked;
    private boolean disabled;
    private boolean printable;
    private boolean printed;
    private String responseType;
    private String position;
    private String positionOnPage;
    private String filePath;
    private Document doc;

    public PageItem(String xmlString) {
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

    public PageItem(File xmlFile) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            this.doc = builder.parse(xmlFile);
            parseXml();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void parseXml() {
        Element root = doc.getDocumentElement();
        this.bankKey = root.getAttribute("bankKey");
        this.itemKey = root.getAttribute("itemKey");
        this.subject = root.getAttribute("subject");
        this.grade = root.getAttribute("grade");
        this.format = root.getAttribute("format");
        this.marked = root.getAttribute("marked").equals("true");
        this.disabled = root.getAttribute("disabled").equals("true");
        this.printable = root.getAttribute("printable").equals("true");
        this.printed = root.getAttribute("printed").equals("true");
        this.responseType = root.getAttribute("responseType");
        this.position = root.getAttribute("position");
        this.positionOnPage = root.getAttribute("positionOnPage");

        parseChildNodes(root.getChildNodes());
    }

    // <tutorial> and <qti> are some other tags that appear in child nodes
    // currently not parsing them but could be added if needed
    private void parseChildNodes(NodeList nodes) {
        for(int i = 0; i < nodes.getLength(); i++) {
            Node curr = nodes.item(i);
            switch (curr.getNodeName()) {
            case "filePath":
                this.filePath = curr.getTextContent();
                break;
            default:
                break;
            }
        }
    }

    public String getBankKey() {
        return bankKey;
    }

    public void setBankKey(String bankKey) {
        this.bankKey = bankKey;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isPrintable() {
        return printable;
    }

    public void setPrintable(boolean printable) {
        this.printable = printable;
    }

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPositionOnPage() {
        return positionOnPage;
    }

    public void setPositionOnPage(String positionOnPage) {
        this.positionOnPage = positionOnPage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }
}
