package org.cresst.sb.irp.automation.adapter.student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StudentResponseService {

    private Map<String, List<String>> responseDataMap = new HashMap<String, List<String>>();

    public StudentResponseService(String responseData) {
        String lines[] = responseData.split("\\r?\\n");
        for(String line : lines) {
            String parts[] = line.split("\\s+");
            // todo: what to do if not length 2
            String key = parts[0];
            String value = parts[1];
            // Add response to list if already in hashmap
            if(responseDataMap.containsKey(key)) {
                responseDataMap.get(key).add(value);
            } else {
                ArrayList<String> newList = new ArrayList<String>();
                newList.add(value);
                responseDataMap.put(key, newList);
            }
        }
    }

    public StudentResponseService() {

    }

    /**
     * Gets a random student response for a given item
     * @param itemId of the response we want a random item from
     * @return a random item
     */
    public String getRandomResponse(String itemId) {
        List<String> responses = responseDataMap.get(itemId);
        Random r = new Random();
        int randIndex = r.nextInt(responses.size());
        return responses.get(randIndex);
     }
}
