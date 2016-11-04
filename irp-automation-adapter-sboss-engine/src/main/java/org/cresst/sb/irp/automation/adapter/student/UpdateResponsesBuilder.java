package org.cresst.sb.irp.automation.adapter.student;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.cresst.sb.irp.automation.adapter.student.data.*;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;
import java.util.SortedSet;

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

    public static String initialRequest(ApprovalInfo approvalInfo) {
        return "<request action=\"update\" eventID=\"1\" currentPage=\"0\" lastPage=\"0\" prefetch=\"99\" pageDuration=\"0\">"
                + "<accs><![CDATA[" + serializeAccs(approvalInfo) + "]]></accs><responses></responses></request>";
    }

    public static String createRequestString(StudentResponseGenerator responseGenerator, ApprovalInfo approvalInfo,
                                             SortedSet<PageContents> pages, int attemptNum) {

        return docToString(createRequest(responseGenerator, approvalInfo, pages, attemptNum));
    }

    public static Document createRequest(StudentResponseGenerator responseGenerator, ApprovalInfo approvalInfo,
                                         SortedSet<PageContents> pages, int attemptNum) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element requestElement = doc.createElement("request");
            requestElement.setAttribute("action", "update");
            requestElement.setAttribute("eventID", String.valueOf(attemptNum));
            requestElement.setAttribute("timestamp", String.valueOf(Instant.now().getMillis()));
            requestElement.setAttribute("currentPage", String.valueOf(pages.first().getPageNumber() - 1));
            requestElement.setAttribute("lastPage", String.valueOf(pages.last().getPageNumber()));
            requestElement.setAttribute("prefetch", "99");
            requestElement.setAttribute("pageDuration", "1000");
            doc.appendChild(requestElement);

            Element accsElement = doc.createElement("accs");
            accsElement.appendChild(doc.createCDATASection(serializeAccs(approvalInfo)));
            requestElement.appendChild(accsElement);

            Element responses = doc.createElement("responses");
            requestElement.appendChild(responses);

            for (PageContents pageContents : pages) {
                for (PageItem pageItem : pageContents.getPageItems()) {
                    responses.appendChild(createResponse(responseGenerator,
                            pageItem, pageContents.getPageNumber(), doc, pageContents.getSegmentId()));
                }
            }

            return doc;
        } catch (DOMException | ParserConfigurationException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    private static Element createResponse(StudentResponseGenerator responseGenerator, PageItem pageItem, int pageNumber,
                                          Document doc, String segmentId) {

        Element currResponse;
        Element filePath;
        Element valueElement;
        CDATASection cdataSection;

        // Create response and attributes
        currResponse = doc.createElement("response");
        currResponse.setAttribute("id", createResponseId(pageItem.getBankKey(), pageItem.getItemKey()));
        currResponse.setAttribute("bankKey", pageItem.getBankKey());
        currResponse.setAttribute("itemKey", pageItem.getItemKey());
        currResponse.setAttribute("segmentID", segmentId);
        currResponse.setAttribute("pageKey", pageItem.getPageKey());
        currResponse.setAttribute("page", String.valueOf(pageNumber));
        currResponse.setAttribute("position", pageItem.getPosition());
        currResponse.setAttribute("sequence", "1");
        currResponse.setAttribute("selected", "true");
        currResponse.setAttribute("valid", "true");

        // Add filepath tag
        filePath = doc.createElement("filePath");
        filePath.setTextContent(pageItem.getFilePath());
        currResponse.appendChild(filePath);

        // Add random response to item from the response generator
        String itemResp = responseGenerator.getRandomResponseWithoutCDATA(pageItem.getItemKey());
        logger.debug("item response for item {} : {}", pageItem.getItemKey(), itemResp);

        cdataSection = doc.createCDATASection(itemResp);
        valueElement = doc.createElement("value");
        valueElement.appendChild(cdataSection);
        currResponse.appendChild(valueElement);

        return currResponse;
    }


    // Format bank and item key into response id
    private static String createResponseId(String bankKey, String itemKey) {
        return bankKey + "-" + itemKey;
    }

    private static String serializeAccs(ApprovalInfo approvalInfo) {

        if (approvalInfo != null && approvalInfo.getSegmentsAccommodations() != null) {
            List<Accommodations> list = approvalInfo.getSegmentsAccommodations();
            if (list.size() > 0) {
                Accommodations accs = list.get(0); // get test accs

                ListMultimap<String, String> typeCodes = ArrayListMultimap.create();
                for (AccommodationType accommodationType : accs.getTypes()) {
                    if (accommodationType.isSelectable()) {
                        String name = accommodationType.getName();

                        List<String> codes = Lists.newArrayList();
                        if (accommodationType.getValues() != null) {
                            for (AccommodationValue accommodationValue : accommodationType.getValues()) {
                                codes.add(accommodationValue.getCode());
                            }
                        }

                        typeCodes.putAll(name, codes);
                    }
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (String name : typeCodes.keySet()) {
                    stringBuilder.append(name).append(":").append(StringUtils.join(typeCodes.get(name), ",")).append(";");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                return stringBuilder.toString();
            }
        }

        return "";
    }
}
