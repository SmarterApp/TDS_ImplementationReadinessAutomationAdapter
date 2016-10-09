package org.cresst.sb.irp.automation.adapter.proctor.data.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.proctor.data.Accommodations.AccType;
import org.cresst.sb.irp.automation.adapter.proctor.data.Accommodations.AccTypes;
import org.cresst.sb.irp.automation.adapter.proctor.data.Accommodations.AccValue;
import org.cresst.sb.irp.automation.adapter.proctor.data.SessionDTO;
import org.cresst.sb.irp.automation.adapter.proctor.data.TestOpportunity;
import org.cresst.sb.irp.automation.adapter.proctor.data.TestOpps;
import org.junit.Test;
import org.springframework.util.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccTypesDeserializerTest {
    @Test
    public void deserializeWithJSONStringTest() throws Exception {
        final String jsonString = "{\"session\":null,\"tests\":null,\"segments\":null,\"sessionTests\":null,\"approvalOpps\":[{\"opp\":1,\"displayStatus\":null,\"lep\":null,\"custAccs\":false,\"waitSegment\":1,\"ssid\":\"AIRP39990002\",\"testName\":null,\"oppKey\":\"92b28831-c8d4-49c5-b894-6d61c4eb4ddf\",\"testID\":\"SBAC-IRP-CAT-MATH-3\",\"testKey\":\"(SBAC_PT)SBAC-IRP-CAT-MATH-3-Summer-2015-2016\",\"itemcount\":-1,\"responseCount\":-1,\"score\":null,\"accs\":null,\"accTypesList\":[[{\"Key\":\"Print Size\",\"Value\":{\"dependOnType\":null,\"allowCombineCount\":0,\"accDepParentTypes\":null,\"Values\":[{\"sortOrder\":0,\"IsSelected\":true,\"AllowCombine\":false,\"Code\":\"TDS_PS_L0\",\"Value\":\"No default zoom applied\"}],\"Label\":null,\"AllowChange\":false,\"IsVisible\":false,\"sOrder\":0,\"IsSelectable\":false,\"Type\":\"Print Size\"}},{\"Key\":\"Other\",\"Value\":{\"dependOnType\":null,\"allowCombineCount\":0,\"accDepParentTypes\":null,\"Values\":[{\"sortOrder\":0,\"IsSelected\":true,\"AllowCombine\":false,\"Code\":\"TDS_Other\",\"Value\":\"None\"}],\"Label\":null,\"AllowChange\":false,\"IsVisible\":false,\"sOrder\":0,\"IsSelectable\":false,\"Type\":\"Other\"}},{\"Key\":\"language\",\"Value\":{\"dependOnType\":null,\"allowCombineCount\":0,\"accDepParentTypes\":null,\"Values\":[{\"sortOrder\":0,\"IsSelected\":true,\"AllowCombine\":false,\"Code\":\"ENU\",\"Value\":\"English\"}],\"Label\":null,\"AllowChange\":false,\"IsVisible\":false,\"sOrder\":0,\"IsSelectable\":false,\"Type\":\"language\"}}]],\"status\":\"pending\",\"requestCount\":0,\"name\":\"IRPLastNameB, IRPStudentB \"}],\"testOpps\":null,\"bReplaceSession\":false,\"bReplaceTests\":false,\"bReplaceSegments\":false,\"bReplaceSessionTests\":false,\"bReplaceApprovalOpps\":true,\"bReplaceTestOpps\":false,\"bReplaceAlertMsgs\":false,\"alertMessages\":null}";

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("TestModule");
        simpleModule.addDeserializer(AccTypes.class, new AccTypesDeserializer());
        objectMapper.registerModule(simpleModule);

        SessionDTO sessionDTO = objectMapper.readValue(jsonString, SessionDTO.class);
        assertEquals(3, sessionDTO.getApprovalOpps().get(0).getAccTypesList().get(0).size());
    }

    @Test
    public void serializeDeserializeTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("TestModule");
        objectMapper.registerModule(simpleModule);


        AccValue accValue = new AccValue();
        accValue.setSortOrder(0);
        accValue.setAllowCombine(true);
        accValue.setCode("TDS_PS_L0");
        accValue.setSelected(true);
        accValue.setValue("No default zoom applied");

        AccType accType = new AccType("Print Size", null, false, true, true, 0);
        accType.add(accValue);

        AccTypes accTypes = new AccTypes();
        accTypes.put("Print Size", accType);

        TestOpportunity testOpportunity = new TestOpportunity();
        testOpportunity.setAccTypesList(Lists.newArrayList(accTypes));

        TestOpps testOpps = new TestOpps();
        testOpps.add(testOpportunity);

        SessionDTO expectedSessionDTO = new SessionDTO();
        expectedSessionDTO.setApprovalOpps(testOpps);

        String jsonStringSessionDTO = objectMapper.writeValueAsString(expectedSessionDTO);

        SessionDTO actualSessionDTO = objectMapper.readValue(jsonStringSessionDTO, SessionDTO.class);

        // Since SessionDTO and rest of classes do not implement equals
        assertTrue(actualSessionDTO.getApprovalOpps().get(0).getAccTypesList().get(0).containsKey("Print Size"));
    }

}