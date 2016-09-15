package org.cresst.sb.irp.automation.adapter.web;

import com.google.common.hash.HashCode;
import org.cresst.sb.irp.automation.adapter.domain.Context;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.cresst.sb.irp.automation.adapter.service.SbossAutomationAdapterService;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

public class TdsReportResourceAssembler extends ResourceAssemblerSupport<TDSReport, TdsReportResource> {

    public TdsReportResourceAssembler() {
        super(AdapterController.class, TdsReportResource.class);
    }

    @Override
    public TdsReportResource toResource(TDSReport tdsReport) {
        TdsReportResource tdsReportResource = createResourceWithId(tdsReport.hashCode(), tdsReport);
        return tdsReportResource;
    }

    @Override
    protected TdsReportResource instantiateResource(TDSReport entity) {
        String testName = entity.getTest().getName();
        String studentId = getStudentId(entity.getExaminee());
        int id = SbossAutomationAdapterService.tdsReportHashCode(testName, studentId);

        return new TdsReportResource(id, testName, studentId);
    }

    private String getStudentId(TDSReport.Examinee examinee) {
        for (Object object : examinee.getExamineeAttributeOrExamineeRelationship()) {
            if (object instanceof TDSReport.Examinee.ExamineeAttribute) {
                TDSReport.Examinee.ExamineeAttribute attribute = (TDSReport.Examinee.ExamineeAttribute) object;
                if (attribute.getContext() == Context.FINAL && "StudentIdentifier".equals(attribute.getName())) {
                    return attribute.getValue();
                }
            }
        }

        return "";
    }
}
