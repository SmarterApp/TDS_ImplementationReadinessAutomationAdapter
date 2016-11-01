package org.cresst.sb.irp.automation.adapter.web;

import org.cresst.sb.irp.automation.adapter.accesstoken.AccessToken;
import org.springframework.web.client.RestOperations;

public interface AutomationRestTemplate extends RestOperations {
    void addAccessToken(AccessToken accessToken);
}
