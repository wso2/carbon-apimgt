/*
 *
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  n compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.correlation;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigPropertyDTO;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.logging.correlation.bean.CorrelationLogConfig;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
/**
 * Correlation Config Manager to configure correlation components and
 * invoke the internal API.
 */
public class CorrelationConfigManager {
    private static final int RETRIEVAL_RETRIES = 15;
    private static final String UTF8 = "UTF-8";
    private static final String DENIED_THREADS = "deniedThreads";
    private static final Log log = LogFactory.getLog(CorrelationConfigManager.class);
    private static final CorrelationConfigManager correlationConfigManager = new CorrelationConfigManager();
    private final EventHubConfigurationDto eventHubConfigurationDto;
    private String[] deniedThreads = new String[0];

    private CorrelationConfigManager() {
        this.eventHubConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getEventHubConfigurationDto();
    }

    public static CorrelationConfigManager getInstance() {
        return correlationConfigManager;
    }

    public void initializeCorrelationComponentList() {
        try {
            String responseString = invokeService("/correlation-configs");
            JSONObject responseJson = new JSONObject(responseString);
            List<CorrelationConfigDTO> correlationConfigDTOList = new ArrayList<>();
            JSONArray correlationComponentsArray = responseJson.getJSONArray("components");
            for (int i = 0; i < correlationComponentsArray.length(); i++) {
                JSONObject correlationComponentObject = correlationComponentsArray.getJSONObject(i);
                CorrelationConfigDTO correlationConfigDTO = new CorrelationConfigDTO();
                correlationConfigDTO.setName(correlationComponentObject.getString("name"));
                correlationConfigDTO.setEnabled(correlationComponentObject.getString("enabled"));

                List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList = new ArrayList<>();
                JSONArray correlationConfigPropertiesArray = correlationComponentObject.getJSONArray("properties");
                for (int j = 0; j < correlationConfigPropertiesArray.length(); j++) {
                    JSONObject correlationConfigPropertyObject = correlationConfigPropertiesArray.getJSONObject(j);
                    CorrelationConfigPropertyDTO correlationConfigPropertyDTO = new CorrelationConfigPropertyDTO();
                    correlationConfigPropertyDTO.setName(correlationConfigPropertyObject.getString("name"));

                    List<String> propertyValueList = new ArrayList<>();
                    JSONArray propertyValueArray = correlationConfigPropertyObject.getJSONArray("value");
                    for (int k = 0; k < propertyValueArray.length(); k++) {
                        String propertyValue = propertyValueArray.getString(k);
                        propertyValueList.add(propertyValue);
                    }
                    correlationConfigPropertyDTO.setValue(
                            propertyValueList.toArray(new String[0]));
                    if (correlationConfigPropertyDTO.getName().equals(DENIED_THREADS)) {
                        deniedThreads = correlationConfigPropertyDTO.getValue();
                    }
                    correlationConfigPropertyDTOList.add(correlationConfigPropertyDTO);
                }
                correlationConfigDTO.setProperties(correlationConfigPropertyDTOList);
                correlationConfigDTOList.add(correlationConfigDTO);
            }
            log.debug("Updating Correlation Config in the gateway Start Up");
            updateCorrelationConfigs(correlationConfigDTOList);

            if (log.isDebugEnabled()) {
                log.debug("Response : " + responseString);
            }
        } catch (IOException | APIManagementException e) {
            log.error("Error while calling internal service API for correlation configs", e);
        }
    }

    public void updateCorrelationConfigs(List<CorrelationConfigDTO> correlationConfigDTOList) {

        boolean configEnable = true;
        List<String> configComponentNames = new ArrayList<>();
        String[] configDeniedThreads = deniedThreads;
        for (CorrelationConfigDTO correlationConfigDTO : correlationConfigDTOList) {
            String componentName = correlationConfigDTO.getName();
            String enabled = correlationConfigDTO.getEnabled();
            if (Boolean.parseBoolean(enabled)) {
                configComponentNames.add(componentName);
            }
            List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList = correlationConfigDTO.getProperties();
            String[] deniedThreads;
            for (CorrelationConfigPropertyDTO correlationConfigPropertyDTO : correlationConfigPropertyDTOList) {
                if (correlationConfigPropertyDTO.getName().equals(DENIED_THREADS)) {
                    deniedThreads = correlationConfigPropertyDTO.getValue();
                    configDeniedThreads = deniedThreads;
                }
            }
        }
        CorrelationLogConfig correlationLogConfig = new CorrelationLogConfig(configEnable,
                configComponentNames.toArray(new String[0]), configDeniedThreads);
        CorrelationLogHolder.getInstance().setCorrelationLogServiceConfigs(correlationLogConfig);
    }

    private byte[] getServiceCredentials(EventHubConfigurationDto eventHubConfigurationDto) {

        String username = eventHubConfigurationDto.getUsername();
        String pw = eventHubConfigurationDto.getPassword();
        return Base64.encodeBase64((username + APIConstants.DELEM_COLON + pw).getBytes(StandardCharsets.UTF_8));
    }

    private String invokeService(String path) throws IOException, APIManagementException {

        String serviceURLStr = eventHubConfigurationDto.getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP);
        HttpGet method = new HttpGet(serviceURLStr + path);

        URL serviceURL = new URL(serviceURLStr + path);
        byte[] credentials = getServiceCredentials(eventHubConfigurationDto);
        int servicePort = serviceURL.getPort();
        String serviceProtocol = serviceURL.getProtocol();
        method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                APIConstants.AUTHORIZATION_BASIC + new String(credentials, StandardCharsets.UTF_8));
        method.setHeader(APIConstants.HEADER_TENANT, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        HttpClient httpClient = APIUtil.getHttpClient(servicePort, serviceProtocol);

        HttpResponse httpResponse = null;
        int retryCount = 0;
        boolean retry;
        try {
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        return EntityUtils.toString(httpResponse.getEntity(), UTF8);
                    } else {
                        retry = true;
                    }
                } catch (IOException ex) {
                    if (retryCount < RETRIEVAL_RETRIES) {
                        retry = true;
                    } else {
                        throw new APIManagementException("Error while calling internal service", ex);
                    }
                }
                if (retry) {
                    if (retryCount < RETRIEVAL_RETRIES) {
                        retryCount++;
                        long retryTimeout = (long) Math.min(Math.pow(2, retryCount), 300);
                        log.warn("Failed retrieving correlation configs. Retrying after " + retryTimeout
                                + " seconds...");
                        Thread.sleep(retryTimeout * 1000);
                    } else {
                        throw new APIManagementException("Could not retrieve Correlation Log Configs " + path);
                    }
                }
            } while (retry);
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("Could not retrieve Correlation Log Configs - Internal service API");
                throw new APIManagementException("Could not retrieve Correlation Log Configs " + path);
            }
        } catch (InterruptedException e) {
            log.error("Error while retrieving correlation configs", e);
            throw new APIManagementException("Could not retrieve Correlation Log Configs" + path);
        }
        return null;
    }
}
