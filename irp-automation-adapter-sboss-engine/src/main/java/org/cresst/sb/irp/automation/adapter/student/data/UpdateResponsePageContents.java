package org.cresst.sb.irp.automation.adapter.student.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class UpdateResponsePageContents {
    private final static Logger logger = LoggerFactory.getLogger(UpdateResponsePageContents.class);

    private Document doc;

    private boolean finished;
    private SortedMap<Integer, UpdateResponsePage> pages;

    private static XPathExpression testFinishedExpression;
    private static XPathExpression pageNodesExpression;
    private static XPathExpression segmentIdExpression;
    private static XPathExpression segmentPositionExpression;
    private static XPathExpression groupIdExpression;
    private static XPathExpression groupKeyExpression;

    static {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        try {
            testFinishedExpression = xpath.compile("boolean(//testsummary/@finished='true')");
            pageNodesExpression = xpath.compile("//page");
            segmentIdExpression = xpath.compile("segment/@id");
            segmentPositionExpression = xpath.compile("segment/@position");
            groupIdExpression = xpath.compile("group/@id");
            groupKeyExpression = xpath.compile("group/@key");
        } catch (XPathExpressionException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private UpdateResponsePageContents(String updateResp) throws Exception {
        pages = new TreeMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(updateResp));
        this.doc = builder.parse(is);
        parseXml();
    }

    public static UpdateResponsePageContents buildUpdateResponsePageContents(String updateResponse) {
        UpdateResponsePageContents updateResponsePageContents = null;
        try {
            updateResponsePageContents = new UpdateResponsePageContents(updateResponse);
        } catch (Exception ex) {
            logger.error("Error creating UpdateResponsePageContents", ex);
        }

        return updateResponsePageContents;
    }

    private void parseXml() throws XPathExpressionException {

        finished = (boolean) testFinishedExpression.evaluate(doc, XPathConstants.BOOLEAN);

        NodeList pageNodes = (NodeList) pageNodesExpression.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < pageNodes.getLength(); i++) {
            Node pageNode = pageNodes.item(i);

            String segmentId = (String) segmentIdExpression.evaluate(pageNode, XPathConstants.STRING);
            Double segmentPositionObject = (Double)segmentPositionExpression.evaluate(pageNode, XPathConstants.NUMBER);
            int segmentPosition = 0;
            if (segmentPositionObject != null) {
                segmentPosition = segmentPositionObject.intValue();
            }

            String groupId = (String) groupIdExpression.evaluate(pageNode, XPathConstants.STRING);
            String pageKey = (String) groupKeyExpression.evaluate(pageNode, XPathConstants.STRING);
            int pageNumber = Integer.parseInt(pageNode.getAttributes().getNamedItem("number").getNodeValue());

            pages.put(pageNumber, new UpdateResponsePage(groupId, pageKey, pageNumber, segmentPosition, segmentId));
        }

        logger.debug("Found {} pages", pageNodes.getLength());
    }

    public UpdateResponsePage getFirstPage() {
        if (pageCount() <= 0) {
            return null;
        }
        return pages.get(pages.firstKey());
    }

    public UpdateResponsePage getLastPage() {
        if (pageCount() <= 0) {
            return null;
        }
        return pages.get(pages.lastKey());
    }

    public Map<Integer, UpdateResponsePage> getPages() {
        return pages;
    }

    public boolean isFinished() {
        return finished;
    }

    public int pageCount() {
        return pages.keySet().size();
    }

    @Override
    public String toString() {
        return "UpdateResponsePageContents [doc=" + doc + ", finished=" + finished + ", pages=" + pages + "]";
    }
}
