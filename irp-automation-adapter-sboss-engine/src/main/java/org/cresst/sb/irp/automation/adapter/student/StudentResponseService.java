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
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudentResponseService {
    private final static Logger logger = LoggerFactory.getLogger(StudentResponseService.class);
    private Map<String, List<String>> responseDataMap = new HashMap<String, List<String>>();

    public StudentResponseService(String responseData) throws IOException {
        this(new ByteArrayInputStream(responseData.getBytes()));
    }

    public StudentResponseService(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while((line = reader.readLine()) != null){
            if(line != null) {
                String parts[] = line.split("\t");
                if(parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];

                    appendValueResponseData(key, value);
                } else {
                    // Line not formatted correctly
                    logger.error("Line: {} was not formatted correctly", line);
                }
            }
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
            ArrayList<String> newList = new ArrayList<>();
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

    @Override
    public String toString() {
        return responseDataMap.toString();
    }
}
