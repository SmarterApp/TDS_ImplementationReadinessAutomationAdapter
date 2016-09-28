package org.cresst.sb.irp.automation.adapter.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.net.URI;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DiscoverController.class)
public class DiscoverControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void discoverAsJSON() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/discover")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();


        String body = mvcResult.getResponse().getContentAsString();
        assertNotNull(body);
    }

    @Test
    public void discoverAsXML() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/discover")
                .accept(MediaType.APPLICATION_XML);

        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        assertNotNull(body);
    }

    @Test
    public void discoverAsHAL() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/discover")
                .accept(MediaTypes.HAL_JSON);

        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        assertNotNull(body);
    }

    @Test
    public void discoverAsAtomXML() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/discover")
                .accept(MediaType.APPLICATION_ATOM_XML);

        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        assertNotNull(body);
    }

}