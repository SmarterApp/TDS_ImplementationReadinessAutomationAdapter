package org.cresst.sb.irp.automation.adapter.web;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.cresst.sb.irp.automation.adapter.service.AdapterAutomationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


/**
 * This is a REST controller to process TDSReport generation requests.
 */
@RestController
@RequestMapping("/tdsReports")
public class AdapterController {
    private final static Logger logger = LoggerFactory.getLogger(AdapterController.class);

    private AdapterAutomationService adapterAutomationService;

    public AdapterController(AdapterAutomationService adapterAutomationService) {
        this.adapterAutomationService = adapterAutomationService;
    }

    @PostMapping
    public HttpEntity<AdapterAutomationTicket> createTdsReports(@Valid @RequestBody final AdapterAutomationTicket adapterAutomationTicket) {
        logger.info("Automation Request: " + adapterAutomationTicket);

        AdapterAutomationTicket serviceTicket = adapterAutomationService.automate(adapterAutomationTicket);

        logger.info("Automation Ticket: " + serviceTicket);

        // TODO: Find a better way to build relative Location path
        URI location = UriComponentsBuilder.fromPath(linkTo(AdapterController.class)
                .slash("queue")
                .slash(serviceTicket.getAdapterAutomationToken())
                .toUri()
                .getPath())
                .build()
                .toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(location);

        ResponseEntity<AdapterAutomationTicket> responseEntity = new ResponseEntity<>(serviceTicket, httpHeaders, HttpStatus.ACCEPTED);

        return responseEntity;
    }

    @GetMapping(value = "/queue/{adapterAutomationToken}")
    public HttpEntity<AdapterAutomationTicket> getAutomationTicketStatus(@PathVariable("adapterAutomationToken") final int adapterAutomationToken) {
        logger.info("Automation Ticket Requested: " + adapterAutomationToken);

        AdapterAutomationTicket ticket = adapterAutomationService.getAdapterAutomationTicket(adapterAutomationToken);

        ResponseEntity<AdapterAutomationTicket> responseEntity;

        if (ticket.getAdapterAutomationStatusReport().isAutomationComplete()) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(linkTo(AdapterController.class).toUri());

            responseEntity = new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);

            logger.info("TDSReport generation is complete. Sending client to list of TDSReports");
        } else {
            responseEntity = new ResponseEntity<>(ticket, HttpStatus.OK);

            logger.info("Responding with " + ticket);
        }

        return responseEntity;
    }

    @GetMapping
    public List<TdsReportResource> getAllTdsReports() {

        Collection<TDSReport> tdsReports = adapterAutomationService.getTdsReports();

        TdsReportResourceAssembler assembler = new TdsReportResourceAssembler();
        List<TdsReportResource> tdsReportResources = assembler.toResources(tdsReports);

        return tdsReportResources;
    }

    @GetMapping(value = "/{tdsReportId}")
    public TDSReport getTdsReport(@PathVariable int tdsReportId) {
        return adapterAutomationService.getTdsReport(tdsReportId);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        logger.warn("Returning HTTP 400 Bad Request", e);
    }
}
