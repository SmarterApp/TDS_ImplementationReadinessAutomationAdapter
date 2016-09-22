package org.cresst.sb.irp.automation.adapter.student;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class StudentResponseService {
    private final String CDATA_START = "<![CDATA[";
    private final String CDATA_END = "]]";

    private Map<String, String> responseDataMap = new HashMap<String, String>();

    public StudentResponseService(String responseData) {
        String lines[] = responseData.split("\\r?\\n");
        for(String line : lines) {
            String parts[] = line.split("\\s+");
            // todo: what to do if not length 2
            responseDataMap.put(parts[0], parts[1]);
        }
    }

    public StudentResponseService() {

    }

    private Document getDocumentFromString(String stringXml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input =  new ByteArrayInputStream(
                stringXml.getBytes("UTF-8"));
        Document doc = builder.parse(input);
        return doc;
    }

    /**
     * Gets a random student response for a given item
     * @param itemId of the response we want a random item from
     * @return a random item (in a string that is possibly xml)
     */
    public String getRandomResponse(String itemId) {
        String response = this.getItemResponse(itemId);
        try {
            Document doc = getDocumentFromString(response);
            DOMImplementationLS ls = (DOMImplementationLS)doc.getImplementation();
            LSSerializer ser = ls.createLSSerializer();
            ser.getDomConfig().setParameter("xml-declaration", false);
            Element root = doc.getDocumentElement();
            NodeList valueNodes = root.getElementsByTagName("response");
            Random r = new Random();
            int randomValue = r.nextInt(valueNodes.getLength());
            return ser.writeToString(valueNodes.item(randomValue));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }

    public String getItemResponse(String itemId) {
        Pattern regex = Pattern.compile(Pattern.quote(CDATA_START) + "(.*?)" + Pattern.quote(CDATA_END));
        String rawResponse = responseDataMap.get(itemId);
        Matcher m = regex.matcher(rawResponse);
        // Get the text between CDATA tag if matches
        if(m.find()) {
            return m.group(1);
        }
            return rawResponse;
    }
}
