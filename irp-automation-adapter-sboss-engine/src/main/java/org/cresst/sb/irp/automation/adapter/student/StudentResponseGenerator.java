package org.cresst.sb.irp.automation.adapter.student;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.*;

public class StudentResponseGenerator {
    private final static Logger logger = LoggerFactory.getLogger(StudentResponseGenerator.class);

    private Map<String, List<String>> responseDataMap = new HashMap<String, List<String>>();

    public StudentResponseGenerator(Resource studentResponses) throws IOException {
        this(studentResponses.getInputStream());
    }

    StudentResponseGenerator(String responseData) throws IOException {
        this(new ByteArrayInputStream(responseData.getBytes()));
    }

    public StudentResponseGenerator(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            // Skip the first line
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String parts[] = line.split("\t");
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];

                    appendValueResponseData(key, value);
                } else {
                    // Line not formatted correctly
                    logger.error("Line \"{}\" was not formatted correctly", line);
                }
            }
        }
    }

    public StudentResponseGenerator() {

    }

    /**
     * Helper method to append a value to the list in key-list pair of responseDataMap
     *
     * @param key   in ResponseData to either create, or append to
     * @param value to append to the array list for given key
     */
    private void appendValueResponseData(String key, String value) {
        if (responseDataMap.containsKey(key)) {
            responseDataMap.get(key).add(value);
        } else {
            List<String> newList = new ArrayList<>();
            newList.add(value);
            responseDataMap.put(key, newList);
        }
    }

    /**
     * Gets a random student response for a given item
     *
     * @param itemId of the response we want a random item from
     * @return a random item
     */
    public String getRandomResponse(String itemId) {
        List<String> responses = responseDataMap.get(itemId);
        if (responses == null) return null;
        Random r = new Random();
        int randIndex = r.nextInt(responses.size());
        return responses.get(randIndex);
    }

    @Override
    public String toString() {
        return responseDataMap.toString();
    }
}
