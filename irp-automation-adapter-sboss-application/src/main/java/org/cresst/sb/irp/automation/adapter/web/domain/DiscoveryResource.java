package org.cresst.sb.irp.automation.adapter.web.domain;

import org.springframework.hateoas.ResourceSupport;

public class DiscoveryResource extends ResourceSupport {

    private static final String NAME = "Smarter Balanced Open Source Test System IRP Adapter";

    public static String getNAME() {
        return NAME;
    }
}
