package org.cresst.sb.irp.automation.adapter.web;

import org.cresst.sb.irp.automation.adapter.web.domain.DiscoveryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/discover")
public class DiscoverController {
    private final static Logger logger = LoggerFactory.getLogger(DiscoverController.class);

    @GetMapping
    public HttpEntity<DiscoveryResource> discover() {
        logger.info("Discovering API");

        Link tdsReportsLink = linkTo(AdapterController.class).withRel("tdsreports");
        Links links = new Links(tdsReportsLink);
        DiscoveryResource discoveryResource = new DiscoveryResource();
        discoveryResource.add(links);

        ResponseEntity<DiscoveryResource> responseEntity = new ResponseEntity<>(discoveryResource, HttpStatus.OK);

        return responseEntity;
    }

}
