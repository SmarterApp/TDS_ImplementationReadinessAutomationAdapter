package org.cresst.sb.irp.automation.adapter.student;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Need to parse the item response
    public String getRandomResponse(String itemId) {
        String response = this.getItemResponse(itemId);
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
