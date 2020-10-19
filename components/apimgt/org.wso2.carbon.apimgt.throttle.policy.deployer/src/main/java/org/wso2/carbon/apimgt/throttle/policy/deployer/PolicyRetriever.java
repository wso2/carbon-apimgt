/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.throttle.policy.deployer;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.exception.ThrottlePolicyDeployerException;

import java.io.IOException;
import java.net.URL;

public class PolicyRetriever {

    private static final Log log = LogFactory.getLog(PolicyRetriever.class);
    protected EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getEventHubConfigurationDto();
    private String baseURL = eventHubConfigurationDto.getServiceUrl() +
            APIConstants.INTERNAL_WEB_APP_EP;


    public SubscriptionPolicy retrieveSubscriptionPolicy(String policyName, String tenantDomain)
            throws ThrottlePolicyDeployerException {
        CloseableHttpResponse httpResponse;

        try {

            String path = APIConstants.SubscriptionValidationResources.SUBSCRIPTION_POLICIES +
                    "?policyName=" + policyName;
            String endpoint = baseURL + path;
            httpResponse = invokeService(endpoint, tenantDomain);

            SubscriptionPolicy subscriptionPolicy = null;
            if (httpResponse.getEntity() != null) {
                String responseString = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
                if (responseString != null && !responseString.isEmpty()) {
                    SubscriptionPolicyList list = new Gson().fromJson(responseString, SubscriptionPolicyList.class);
                    if (list.getList() != null && !list.getList().isEmpty()) {
                        subscriptionPolicy = list.getList().get(0);
                    }
                }
                httpResponse.close();
            } else {
                throw new ThrottlePolicyDeployerException("HTTP response is empty");
            }
            subscriptionPolicy.setTenantDomain(APIUtil.getTenantDomainFromTenantId(subscriptionPolicy.getTenantId()));
            return subscriptionPolicy;
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ThrottlePolicyDeployerException(msg, e);
        }
    }


    private CloseableHttpResponse invokeService(String endpoint, String tenantDomain)
            throws IOException, ThrottlePolicyDeployerException {
        HttpGet method = new HttpGet(endpoint);
        URL url = new URL(endpoint);
        String username = eventHubConfigurationDto.getUsername();
        String password = eventHubConfigurationDto.getPassword();
        byte[] credentials = Base64.encodeBase64((username + APIConstants.DELEM_COLON + password).
                getBytes(APIConstants.DigestAuthConstants.CHARSET));
        int port = url.getPort();
        String protocol = url.getProtocol();
        method.setHeader(APIConstants.HEADER_TENANT, tenantDomain);
        method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                + new String(credentials, APIConstants.DigestAuthConstants.CHARSET));
        HttpClient httpClient = APIUtil.getHttpClient(port, protocol);
        try {
            return APIUtil.executeHTTPRequest(method, httpClient);
        } catch (APIManagementException e) {
            throw new ThrottlePolicyDeployerException(e);
        }
    }

}
