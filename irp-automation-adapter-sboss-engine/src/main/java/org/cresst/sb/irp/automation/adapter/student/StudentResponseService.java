package org.cresst.sb.irp.automation.adapter.student;

import java.util.HashMap;
import java.util.Map;

public class StudentResponseService {

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
        return responseDataMap.get(itemId);
    }
}
