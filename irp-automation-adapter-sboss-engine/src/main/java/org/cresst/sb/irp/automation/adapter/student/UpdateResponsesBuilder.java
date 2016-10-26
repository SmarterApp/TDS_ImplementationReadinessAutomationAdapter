package org.cresst.sb.irp.automation.adapter.student;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cresst.sb.irp.automation.adapter.student.data.PageContents;
import org.cresst.sb.irp.automation.adapter.student.data.PageItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UpdateResponsesBuilder {
    private final static Logger logger = LoggerFactory.getLogger(UpdateResponsesBuilder.class);

    public static String docToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        String result = null;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            result = writer.getBuffer().toString();
        } catch (TransformerException e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    public static String initialRequest() {
        return "<request action=\"update\" eventID=\"1\" currentPage=\"0\" lastPage=\"0\" prefetch=\"2\" pageDuration=\"0\">"
                + "<accs></accs><responses></responses></request>";
    }

    public static String getContents(int i, String accs) {
        return "<request action=\"update\" eventID=\"1\" currentPage=\"" + i + "\">"
                + "<accs><![CDATA[" + accs + "]]></accs><responses></responses></request>";
    }

    public static Document createRequest(StudentResponseService responseService, String accs, PageContents pageContents) {
        return createRequest(responseService, accs, pageContents, pageContents.getSegmentId());
    }

    public static String createRequestString(StudentResponseService responseService, String accs, PageContents pageContents, String testKey) {
        return docToString(createRequest(responseService, accs, pageContents, testKey));
    }
    public static Document createRequest(StudentResponseService responseService, String accs, PageContents pageContents, String testKey) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element requestElement = doc.createElement("request");
            requestElement.setAttribute("action", "update");
            requestElement.setAttribute("currentPage", String.valueOf(pageContents.getPageNumber()));
            requestElement.setAttribute("lastPage", String.valueOf(pageContents.getPageNumber()));
            requestElement.setAttribute("prefetch", "2");
            doc.appendChild(requestElement);

            Element accsElement = doc.createElement("accs");

            accsElement.appendChild(doc.createCDATASection(accs));
            requestElement.appendChild(accsElement);

            Element responses = doc.createElement("responses");
            requestElement.appendChild(responses);


            if(pageContents != null) {
                for(PageItem pageItem : pageContents.getPageItems()) {
                    responses.appendChild(createResponse(responseService, pageItem, pageContents.getPageNumber(), doc, testKey));
                }
            }

            return doc;
        } catch (DOMException | ParserConfigurationException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    private static Element createResponse(StudentResponseService responseService, PageItem pageItem, int pageNumber, Document doc, String segmentId) {
        Element currResponse;
        Element filePath;
        Element valueElement;

        // Create response and attributes
        currResponse = doc.createElement("response");
        currResponse.setAttribute("id", createResponseId(pageItem.getBankKey(), pageItem.getItemKey()));
        currResponse.setAttribute("itemKey", pageItem.getItemKey());
        currResponse.setAttribute("bankKey", pageItem.getBankKey());
        currResponse.setAttribute("segmentID", segmentId);
        currResponse.setAttribute("position", pageItem.getPosition());
        currResponse.setAttribute("valid", "true");
        currResponse.setAttribute("page", String.valueOf(pageNumber));
        currResponse.setAttribute("pageKey", pageItem.getPageKey());



        // Add filepath tag
        filePath = doc.createElement("filePath");
        filePath.setTextContent(pageItem.getFilePath());
        currResponse.appendChild(filePath);

        // Add random response to item from the response service
        valueElement = doc.createElement("value");
        String itemResp = responseService.getRandomResponse(pageItem.getItemKey());
        logger.debug("item response for item " + pageItem.getItemKey() +": "+ itemResp);
        valueElement.setTextContent(itemResp);
        currResponse.appendChild(valueElement);

        return currResponse;
    }


    // Format bank and item key into response id
    private static String createResponseId(String bankKey, String itemKey) {
        return bankKey + "-" + itemKey;
    }
}
