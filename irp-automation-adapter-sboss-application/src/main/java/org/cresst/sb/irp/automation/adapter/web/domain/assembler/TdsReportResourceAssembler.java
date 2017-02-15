package org.cresst.sb.irp.automation.adapter.web.domain.assembler;

import org.cresst.sb.irp.automation.adapter.web.AdapterController;
import org.cresst.sb.irp.automation.adapter.web.domain.TdsReportResource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

public class TdsReportResourceAssembler extends ResourceAssemblerSupport<Integer, TdsReportResource> {

    public TdsReportResourceAssembler() {
        super(AdapterController.class, TdsReportResource.class);
    }

    @Override
    public TdsReportResource toResource(Integer tdsReportId) {
        TdsReportResource tdsReportResource = createResourceWithId(tdsReportId, tdsReportId);
        return tdsReportResource;
    }

    @Override
    protected TdsReportResource instantiateResource(Integer entity) {
        // From this a URL such as http://<server>:<port>/tdsReports/<id defined above> is created
        return new TdsReportResource(entity, "Not implemented", "Not implemented");
    }
}
