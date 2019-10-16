/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.conditiongroup;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.gateway.dto.ConditionDTO;
import org.wso2.carbon.apimgt.gateway.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class which is responsible to fetch the condition groups via webservice from database during startup
 */
public class ConditionGroupsRetriever extends TimerTask {

    private static final Log log = LogFactory.getLog(ConditionGroupsRetriever.class);
    private static final int conditionGroupsRetrievalTimeoutInSeconds = 15;
    private static final int conditionGroupsRetrievalRetries = 15;

    @Override
    public void run() {

        log.debug("Starting web service based condition groups retrieving process.");
        loadConditionGroupsFromWebService();
    }

    /**
     * This method will retrieve condition groups by calling a web service.
     *
     * @return List of ConditionGroupDTOs.
     */
    private ConditionGroupDTO[] retrieveConditionGroupsData() {

        try {
            // The resource resides in the throttle web app. Hence reading throttle configs
            ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = getThrottleProperties()
                    .getBlockCondition();
            String url = blockConditionRetrieverConfiguration.getServiceUrl() + "/conditionGroups";
            HttpGet method = new HttpGet(url);
            byte[] credentials = Base64.encodeBase64((blockConditionRetrieverConfiguration.getUsername() + ":" +
                    blockConditionRetrieverConfiguration.getPassword()).getBytes
                    (StandardCharsets.UTF_8));
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL keyMgtURL = new URL(url);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    retry = false;
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < conditionGroupsRetrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving condition groups from remote endpoint: " +
                                ex.getMessage() + ". Retrying after " + conditionGroupsRetrievalTimeoutInSeconds +
                                " seconds...");
                        Thread.sleep(conditionGroupsRetrievalTimeoutInSeconds * 1000);
                    } else {
                        throw ex;
                    }
                }
            } while (retry);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                return new Gson().fromJson(responseString, org.wso2.carbon.apimgt.gateway.dto.ConditionGroupDTO[].class);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception when retrieving revoked JWT tokens from remote endpoint ", e);
        }
        return null;
    }

    private void loadConditionGroupsFromWebService() {
        ConditionGroupDTO[] conditionGroupDTOS = retrieveConditionGroupsData();
        if (conditionGroupDTOS != null && conditionGroupDTOS.length > 0) {
            for (ConditionGroupDTO conditionGroupDTO : conditionGroupDTOS) {
                ConditionGroupsDataHolder.getInstance().addConditionGroupToMap(
                        conditionGroupDTO.getTenantId() + "-" + conditionGroupDTO.getPolicyName(),
                        convertConditionGroupDTO(conditionGroupDTO));
                if (log.isDebugEnabled()) {
                    log.debug("Condition Group for throttle policy : " + conditionGroupDTO.getPolicyName()
                            + " is added to the map.");
                }
            }
        } else {
            log.debug("No condition groups are retrieved via web service");
        }
    }

    /**
     *  Initiates the timer task to fetch data from the web service.
     *  Timer task will not run after the retry count is completed.
     */
    public void startConditionGroupsRetriever() {
        //using same initDelay as in keytemplates,blocking conditions retriever
        new Timer().schedule(this, getThrottleProperties().getBlockCondition().getInitDelay());
    }

    protected ThrottleProperties getThrottleProperties() {
        return ServiceReferenceHolder
                .getInstance().getThrottleProperties();
    }

    private org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO convertConditionGroupDTO(ConditionGroupDTO retrievedConditionGroupDTO) {
        org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO convertedConditionGroupDTO = new org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO();

        //Convert
        convertedConditionGroupDTO.setConditionGroupId(retrievedConditionGroupDTO.getConditionGroupId());

        org.wso2.carbon.apimgt.api.dto.ConditionDTO[] conditions =
                new org.wso2.carbon.apimgt.api.dto.ConditionDTO[retrievedConditionGroupDTO.getConditions().length];
        int i = 0;
        for (ConditionDTO retrievedConditionDTO: retrievedConditionGroupDTO.getConditions()) {
            org.wso2.carbon.apimgt.api.dto.ConditionDTO conditionDTO = new org.wso2.carbon.apimgt.api.dto.ConditionDTO();
            conditionDTO.isInverted(retrievedConditionDTO.isInverted());

            if ("IP".equals(retrievedConditionDTO.getCategory())) {
                if (retrievedConditionDTO.getSpecificIP() != null && !"".equals(retrievedConditionDTO.getSpecificIP())) {
                    conditionDTO.setConditionType(PolicyConstants.IP_SPECIFIC_TYPE);
                    conditionDTO.setConditionName(PolicyConstants.IP_SPECIFIC_TYPE);
                    conditionDTO.setConditionValue(retrievedConditionDTO.getSpecificIP());

                } else if (retrievedConditionDTO.getStartingIP() != null &&
                        !"".equals(retrievedConditionDTO.getStartingIP())) {
                    conditionDTO.setConditionType(PolicyConstants.IP_RANGE_TYPE);
                    conditionDTO.setConditionName(retrievedConditionDTO.getStartingIP());
                    conditionDTO.setConditionValue(retrievedConditionDTO.getEndingIP());

                }
            } else if ("HEADER".equals(retrievedConditionDTO.getCategory())) {
                conditionDTO.setConditionType(PolicyConstants.HEADER_TYPE);
                conditionDTO.setConditionName(retrievedConditionDTO.getHeaderFieldName());
                conditionDTO.setConditionValue(retrievedConditionDTO.getHeaderFieldValue());
            } else if ("JWT_CLAIM".equals(retrievedConditionDTO.getCategory())) {
                conditionDTO.setConditionType(PolicyConstants.JWT_CLAIMS_TYPE);
                conditionDTO.setConditionName(retrievedConditionDTO.getClaimUri());
                conditionDTO.setConditionValue(retrievedConditionDTO.getClaimAttrib());
            } else if ("QUERY_PARAM".equals(retrievedConditionDTO.getCategory())) {
                conditionDTO.setConditionType(PolicyConstants.QUERY_PARAMETER_TYPE);
                conditionDTO.setConditionName(retrievedConditionDTO.getParameterName());
                conditionDTO.setConditionValue(retrievedConditionDTO.getParameterValue());
            }
            conditions[i] = conditionDTO;
            i++;
        }

        convertedConditionGroupDTO.setConditions(conditions);
        return convertedConditionGroupDTO;
    }

}
