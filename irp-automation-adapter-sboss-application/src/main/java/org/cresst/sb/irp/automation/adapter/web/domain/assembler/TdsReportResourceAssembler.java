package org.cresst.sb.irp.automation.adapter.web.domain.assembler;

import org.cresst.sb.irp.automation.adapter.domain.Context;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.cresst.sb.irp.automation.adapter.service.SbossAutomationAdapterService;
import org.cresst.sb.irp.automation.adapter.web.AdapterController;
import org.cresst.sb.irp.automation.adapter.web.domain.TdsReportResource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

public class TdsReportResourceAssembler extends ResourceAssemblerSupport<Integer, TdsReportResource> {

    public TdsReportResourceAssembler() {
        super(AdapterController.class, TdsReportResource.class);
    }

    @Override
    public TdsReportResource toResource(Integer tdsReportId) {
        int id = SbossAutomationAdapterService.tdsReportHashCode("", "");
        TdsReportResource tdsReportResource = createResourceWithId(id, tdsReportId);
        return tdsReportResource;
    }

    @Override
    protected TdsReportResource instantiateResource(Integer entity) {
        // From this a URL such as http://<server>:<port>/tdsReports/<id defined above> is created
        return new TdsReportResource(entity, "Not implemented", "Not implemented");
    }
}
