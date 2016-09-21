package org.cresst.sb.irp.automation.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.web.AdapterController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AdapterController.class)
public class AdapterControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void whenRequestToCreateTdsReport_LocationToQueueReturned() throws Exception {

        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setIrpResources(Lists.<URI>newArrayList());

        String body = objectMapper.writeValueAsString(ticket);

        MockHttpServletRequestBuilder requestBuilder = post("/tdsReports", ticket)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().isAccepted())
                .andReturn();

        assertTrue(mvcResult.getResponse().containsHeader("Location"));
        String actualLocation = mvcResult.getResponse().getHeader("Location");
        assertTrue(actualLocation.contains("/tdsReports/queue/"));
    }

    @Test
    public void whenTdsReportsCreated_ListOfTdsReportLocationsReturned() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/tdsReports");

        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();


        String body = mvcResult.getResponse().getContentAsString();
        assertNotNull(body);
    }
}