/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayBridge.utils;

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

