package org.cresst.sb.irp.automation.adapter.student;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StudentResponseService {

    private Map<String, List<String>> responseDataMap = new HashMap<String, List<String>>();

    public StudentResponseService(String responseData) {
        this(new ByteArrayInputStream(responseData.getBytes()));
    }

    public StudentResponseService(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while((line = reader.readLine()) != null){
                line = reader.readLine();

                String parts[] = line.split("\\s+");
                String key = parts[0];
                String value = parts[1];

                appendValueResponseData(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StudentResponseService() {

    }

    /**
     * Helper method to append a value to the list in key-list pair of responseDataMap
     * @param key in ResponseData to either create, or append to
     * @param value to append to the array list for given key
     */
    private void appendValueResponseData(String key, String value) {
        if(responseDataMap.containsKey(key)) {
            responseDataMap.get(key).add(value);
        } else {
            ArrayList<String> newList = new ArrayList<String>();
            newList.add(value);
            responseDataMap.put(key, newList);
        }
    }

    /**
     * Gets a random student response for a given item
     * @param itemId of the response we want a random item from
     * @return a random item
     */
    public String getRandomResponse(String itemId) {
        List<String> responses = responseDataMap.get(itemId);
        if(responses == null) return null;
        Random r = new Random();
        int randIndex = r.nextInt(responses.size());
        return responses.get(randIndex);
     }
}
