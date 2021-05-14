package org.wso2.carbon.apimgt.gatewaybridge.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * Generate Basic HttpClient.
 */
public class HttpUtil {
    private static final Log log = LogFactory.getLog(HttpUtil.class);

    public HttpUtil() {
        log.debug("HttpUtilities: ");
    }

    /**
     * Returns a CloseableHttpClient instance
     * Always returns immediately, whether or not the
     * CloseableHttpClient exists.
     * @return an executable CloseableHttpClient
     */
    public static CloseableHttpClient getService() {
        try {

            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            return httpClientBuilder.build();
        } catch (Exception e) {
            log.debug("HttpException");
        }
        return null;
    }

}

//https://localhost:9443/internal/data/v1/gatewaybridge-subscription
