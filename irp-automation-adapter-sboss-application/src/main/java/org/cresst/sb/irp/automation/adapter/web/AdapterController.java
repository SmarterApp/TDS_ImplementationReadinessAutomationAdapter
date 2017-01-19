package org.cresst.sb.irp.automation.adapter.web;

import org.cresst.sb.irp.automation.adaptar.util.AdapterUtils;
import org.cresst.sb.irp.automation.adapter.dao.DocumentXmlRepository;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.cresst.sb.irp.automation.adapter.service.AdapterAutomationService;
import org.cresst.sb.irp.automation.adapter.web.domain.TdsReportResource;
import org.cresst.sb.irp.automation.adapter.web.domain.assembler.TdsReportResourceAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;


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
    public HttpEntity<AdapterAutomationTicket> createTdsReports() {
        logger.info("Automation Request");

        AdapterAutomationTicket serviceTicket = adapterAutomationService.automate();

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
    public Callable<HttpEntity<AdapterAutomationTicket>> getAutomationTicketStatus(
            @PathVariable("adapterAutomationToken") final String adapterAutomationToken) {
        logger.info("Automation Ticket Requested: " + adapterAutomationToken);
        Callable<HttpEntity<AdapterAutomationTicket>> responseCallable = new Callable<HttpEntity<AdapterAutomationTicket>>() {        	
        	@Override        	
        	public HttpEntity<AdapterAutomationTicket> call() throws Exception {
 
        		UUID token = UUID.fromString(adapterAutomationToken);
                AdapterAutomationTicket ticket = adapterAutomationService.getAdapterAutomationTicket(token);
                
                ResponseEntity<AdapterAutomationTicket> responseEntity;
 
                if (ticket != null
                        && ticket.getAdapterAutomationStatusReport().isAutomationComplete()
                        && !ticket.getAdapterAutomationStatusReport().isError()) {

                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setLocation(linkTo(AdapterController.class).toUri());

                    responseEntity = new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);

                    logger.info("TDSReport generation is complete. Sending client to list of TDSReports");

                } else if (ticket != null
                        && ticket.getAdapterAutomationStatusReport().isAutomationComplete()
                        && ticket.getAdapterAutomationStatusReport().isError()) {
                    responseEntity = new ResponseEntity<>(ticket, HttpStatus.INTERNAL_SERVER_ERROR);
                    
                } else {
                	                	
                    responseEntity = new ResponseEntity<>(ticket, HttpStatus.OK);
                     
                }
 
                return responseEntity;
        	}
		};
        
		return responseCallable;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TdsReportResource> getAllTdsReports(final HttpEntity<byte[]>  requestEntity
    		) {
    	String startTimeOfSimulation = requestEntity.getHeaders().getFirst("startTimeOfSimulation");
    	logger.info("*-startTimeOfSimulation:" + startTimeOfSimulation);
    	    	
        
        Collection<Integer> tdsReports = adapterAutomationService.getTdsReports(AdapterUtils.parseStringToDate(startTimeOfSimulation));
        TdsReportResourceAssembler assembler = new TdsReportResourceAssembler();
        List<TdsReportResource> tdsReportResources = assembler.toResources(tdsReports);

        return tdsReportResources;
    }

    // IRP makes calls to http://<irp adapter hostname and port>/tdsReports/{tdsReportId}
    // {tdsReportId} is a key that maps to a TDS Report
    @GetMapping(value = "/{tdsReportId}", produces = MediaType.TEXT_XML_VALUE)
    public TDSReport getTdsReport(@PathVariable int tdsReportId) {
        final TDSReport tdsReport = adapterAutomationService.getTdsReport(tdsReportId);
        logger.info("Sending TDSReport XML for {}", tdsReport);
        return tdsReport;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        logger.warn("Returning HTTP 400 Bad Request", e);
    }
}
