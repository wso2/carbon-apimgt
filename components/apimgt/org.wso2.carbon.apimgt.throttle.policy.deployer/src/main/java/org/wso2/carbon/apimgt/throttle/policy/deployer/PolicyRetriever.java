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
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.exception.ThrottlePolicyDeployerException;

import java.io.IOException;
import java.net.URL;

/**
 * Used to retrieve policy metadata using internal REST APIs
 */
public class PolicyRetriever {

    private static final Log log = LogFactory.getLog(PolicyRetriever.class);
    protected EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getEventHubConfigurationDto();
    private final String baseURL = eventHubConfigurationDto.getServiceUrl() +
            APIConstants.INTERNAL_WEB_APP_EP;

    /**
     * Get a subscription policy given the name.
     *
     * @param policyName   policy name
     * @param tenantDomain tenant domain
     * @return subscription policy
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public SubscriptionPolicy getSubscriptionPolicy(String policyName, String tenantDomain)
            throws ThrottlePolicyDeployerException {

        String path = APIConstants.SubscriptionValidationResources.SUBSCRIPTION_POLICIES +
                "?policyName=" + policyName;
        SubscriptionPolicyList subscriptionPolicyList = getPolicies(path, tenantDomain, SubscriptionPolicyList.class);
        if (subscriptionPolicyList.getList() != null && !subscriptionPolicyList.getList().isEmpty()) {
            return subscriptionPolicyList.getList().get(0);
        }
        return null;
    }

    /**
     * Get all the subscription policies.
     *
     * @return subscription policy list
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public SubscriptionPolicyList getAllSubscriptionPolicies()
            throws ThrottlePolicyDeployerException {

        String path = APIConstants.SubscriptionValidationResources.SUBSCRIPTION_POLICIES;
        return getPolicies(path, APIConstants.ORG_ALL_QUERY_PARAM, SubscriptionPolicyList.class);
    }

    /**
     * Get a application policy given the name.
     *
     * @param policyName   policy name
     * @param tenantDomain tenant domain
     * @return application policy
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public ApplicationPolicy getApplicationPolicy(String policyName, String tenantDomain)
            throws ThrottlePolicyDeployerException {

        String path = APIConstants.SubscriptionValidationResources.APPLICATION_POLICIES +
                "?policyName=" + policyName;
        ApplicationPolicyList applicationPolicyList = getPolicies(path, tenantDomain, ApplicationPolicyList.class);
        if (applicationPolicyList.getList() != null && !applicationPolicyList.getList().isEmpty()) {
            return applicationPolicyList.getList().get(0);
        }
        return null;
    }

    /**
     * Get all the application policies.
     *
     * @return application policy list
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public ApplicationPolicyList getAllApplicationPolicies()
            throws ThrottlePolicyDeployerException {

        String path = APIConstants.SubscriptionValidationResources.APPLICATION_POLICIES;
        return getPolicies(path, APIConstants.ORG_ALL_QUERY_PARAM, ApplicationPolicyList.class);
    }

    /**
     * Get a API policy given the name.
     *
     * @param policyName   policy name
     * @param tenantDomain tenant domain
     * @return API policy
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public ApiPolicy getApiPolicy(String policyName, String tenantDomain) throws ThrottlePolicyDeployerException {
        String path = APIConstants.SubscriptionValidationResources.API_POLICIES +
                "?policyName=" + policyName;
        ApiPolicyList apiPolicyList = getPolicies(path, tenantDomain, ApiPolicyList.class);
        if (apiPolicyList.getList() != null && !apiPolicyList.getList().isEmpty()) {
            return apiPolicyList.getList().get(0);
        }
        return null;
    }

    /**
     * Get all the API policies.
     *
     * @return API policy list
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public ApiPolicyList getAllApiPolicies()
            throws ThrottlePolicyDeployerException {

        String path = APIConstants.SubscriptionValidationResources.API_POLICIES;
        return getPolicies(path, APIConstants.ORG_ALL_QUERY_PARAM, ApiPolicyList.class);
    }

    /**
     * Get all the global policies.
     *
     * @return global policy list
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public GlobalPolicyList getAllGlobalPolicies()
            throws ThrottlePolicyDeployerException {

        String path = APIConstants.SubscriptionValidationResources.GLOBAL_POLICIES;
        return getPolicies(path, APIConstants.ORG_ALL_QUERY_PARAM, GlobalPolicyList.class);
    }

    /**
     * Get a global policy given the name.
     *
     * @param policyName   policy name
     * @param tenantDomain tenant domain
     * @return global policy
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    public GlobalPolicy getGlobalPolicy(String policyName, String tenantDomain) throws ThrottlePolicyDeployerException {
        String path = APIConstants.SubscriptionValidationResources.GLOBAL_POLICIES +
                "?policyName=" + policyName;
        GlobalPolicyList globalPolicyList = getPolicies(path, tenantDomain, GlobalPolicyList.class);
        if (globalPolicyList.getList() != null && !globalPolicyList.getList().isEmpty()) {
            return globalPolicyList.getList().get(0);
        }
        return null;
    }

    /**
     * Get policies given a query path, tenant domain and the policy class to be mapped
     *
     * @param path         REST API request path
     * @param tenantDomain tenant domain
     * @param policyClass  class of the policy list type e.g.- ApiPolicyList.class
     * @return throttle policy list of given class type
     * @throws ThrottlePolicyDeployerException if failure occurs
     */
    private <T> T getPolicies(String path, String tenantDomain, Class<T> policyClass)
            throws ThrottlePolicyDeployerException {

        String endpoint = baseURL + path;
        try (CloseableHttpResponse httpResponse = invokeService(endpoint, tenantDomain)) {
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                String errorMessage = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
                throw new ThrottlePolicyDeployerException(errorMessage + "Event-Hub status code is : "
                        + httpResponse.getStatusLine().getStatusCode());
            }
            if (httpResponse.getEntity() != null) {
                String responseString = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
                if (log.isDebugEnabled()) {
                    log.debug("Response: " + responseString);
                }
                if (responseString != null && !responseString.isEmpty()) {
                    return new Gson().fromJson(responseString, policyClass);
                }
            } else {
                throw new ThrottlePolicyDeployerException("HTTP response is empty");
            }
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ThrottlePolicyDeployerException(msg, e);
        }
        return null;
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
            return APIUtil.executeHTTPRequestWithRetries(method, httpClient);
        } catch (APIManagementException e) {
            throw new ThrottlePolicyDeployerException(e);
        }
    }
}
