package org.cresst.sb.irp.automation.adapter.web;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.cresst.sb.irp.automation.adapter.accesstoken.AccessToken;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SbossAutomationRestTemplate extends RestTemplate implements AutomationRestTemplate {

    public SbossAutomationRestTemplate() {
        super();

        ClientHttpRequestInterceptor loggingRequestInterceptor = new LoggingRequestInterceptor();

        List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors = new ArrayList<>();
        clientHttpRequestInterceptors.add(loggingRequestInterceptor);

        setInterceptors(clientHttpRequestInterceptors);
        setRequestFactory(new BufferingClientHttpRequestFactory(httpRequestFactory()));
    }

    public SbossAutomationRestTemplate(AccessToken accessToken) {
        this();
        addAccessToken(accessToken);
    }

    @Override
    public void addAccessToken(AccessToken accessToken) {
        AccessTokenRequestInterceptor accessTokenRequestInterceptor = new AccessTokenRequestInterceptor(accessToken);

        List<ClientHttpRequestInterceptor> interceptors = getInterceptors();
        interceptors.add(0, accessTokenRequestInterceptor);
    }

    public static <T> HttpEntity<T> constructHttpEntity(T requestBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Content-Type", "application/json");

        return new HttpEntity<>(requestBody, httpHeaders);
    }

    private ClientHttpRequestFactory httpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    private HttpClient httpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(connectionManager);
        httpClientBuilder.setConnectionManagerShared(true);
        return httpClientBuilder.build();
    }
}
