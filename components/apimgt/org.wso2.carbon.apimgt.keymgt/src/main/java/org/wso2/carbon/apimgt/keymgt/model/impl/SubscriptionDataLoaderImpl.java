/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.keymgt.model.impl;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.APIList;
import org.wso2.carbon.apimgt.keymgt.model.entity.APIPolicyList;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMappingList;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationList;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicyList;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.model.entity.ScopesList;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionList;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicyList;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDataLoaderImpl implements SubscriptionDataLoader {

    private static final Log log = LogFactory.getLog(SubscriptionDataLoaderImpl.class);
    private final EventHubConfigurationDto getEventHubConfigurationDto;

    public static final int retrievalTimeoutInSeconds = 15;
    public static final int retrievalRetries = 15;
    public static final String UTF8 = "UTF-8";

    public SubscriptionDataLoaderImpl() {

        this.getEventHubConfigurationDto = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getEventHubConfigurationDto();
    }

    @Override
    public List<Subscription> loadAllSubscriptions(String tenantDomain) throws DataLoadingException {

        String subscriptionsEP = APIConstants.SubscriptionValidationResources.SUBSCRIPTIONS;
        List<Subscription> subscriptions = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(subscriptionsEP, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + subscriptionsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            subscriptions = (new Gson().fromJson(responseString, SubscriptionList.class)).getList();
        }
        return subscriptions;
    }

    @Override
    public List<Application> loadAllApplications(String tenantDomain) throws DataLoadingException {

        String applicationsEP = APIConstants.SubscriptionValidationResources.APPLICATIONS;
        List<Application> applications = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(applicationsEP, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + applicationsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            applications = (new Gson().fromJson(responseString, ApplicationList.class)).getList();
        }
        return applications;
    }

    @Override
    public List<ApplicationKeyMapping> loadAllKeyMappings(String tenantDomain) throws DataLoadingException {

        String applicationsEP = APIConstants.SubscriptionValidationResources.APPLICATION_KEY_MAPPINGS;
        List<ApplicationKeyMapping> applicationKeyMappings = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(applicationsEP, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + applicationsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            applicationKeyMappings = (new Gson().fromJson(responseString, ApplicationKeyMappingList.class)).getList();
        }
        return applicationKeyMappings;
    }

    @Override
    public List<API> loadAllApis(String tenantDomain) throws DataLoadingException {

        String apisEP = APIConstants.SubscriptionValidationResources.APIS;
        List<API> apis = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(apisEP, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + apisEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            apis = (new Gson().fromJson(responseString, APIList.class)).getList();
        }
        if (log.isDebugEnabled()) {
            log.debug("apis :" + apis.get(0).toString());
        }
        return apis;
    }

    @Override
    public List<SubscriptionPolicy> loadAllSubscriptionPolicies(String tenantDomain) throws DataLoadingException {

        String subscriptionPoliciesEP = APIConstants.SubscriptionValidationResources.SUBSCRIPTION_POLICIES;
        List<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(subscriptionPoliciesEP, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + subscriptionPoliciesEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            subscriptionPolicies = (new Gson().fromJson(responseString, SubscriptionPolicyList.class)).getList();
        }
        return subscriptionPolicies;
    }

    @Override
    public List<ApiPolicy> loadAllAPIPolicies(String tenantDomain) throws DataLoadingException {

        String apiPoliciesEP = APIConstants.SubscriptionValidationResources.API_POLICIES;
        List<ApiPolicy> apiPolicies = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(apiPoliciesEP, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + apiPoliciesEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            apiPolicies = (new Gson().fromJson(responseString, APIPolicyList.class)).getList();
        }
        return apiPolicies;
    }

    @Override
    public List<ApplicationPolicy> loadAllAppPolicies(String tenantDomain) throws DataLoadingException {

        String applicationsEP = APIConstants.SubscriptionValidationResources.APPLICATION_POLICIES;
        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(applicationsEP, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + applicationsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            applicationPolicies = (new Gson().fromJson(responseString, ApplicationPolicyList.class)).getList();
        }
        return applicationPolicies;
    }

    @Override
    public Subscription getSubscriptionById(String apiId, String appId) throws DataLoadingException {

        String endPoint =
                APIConstants.SubscriptionValidationResources.SUBSCRIPTIONS + "?apiId=" + apiId + "&appId=" + appId;
        Subscription subscription = null;
        String responseString;
        try {
            responseString = invokeService(endPoint, null);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            SubscriptionList list = new Gson().fromJson(responseString, SubscriptionList.class);
            if (list.getList() != null && !list.getList().isEmpty()) {
                subscription = list.getList().get(0);
            }
        }
        return subscription;
    }

    @Override
    public Application getApplicationById(int appId) throws DataLoadingException {

        String endPoint = APIConstants.SubscriptionValidationResources.APPLICATIONS + "?appId=" + appId;
        Application application = null;
        String responseString;
        try {
            responseString = invokeService(endPoint, null);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ApplicationList list = new Gson().fromJson(responseString, ApplicationList.class);
            if (list.getList() != null && !list.getList().isEmpty()) {
                application = list.getList().get(0);
            }
        }
        return application;
    }

    @Override
    public ApplicationKeyMapping getKeyMapping(String consumerKey) throws DataLoadingException {
        return getKeyMapping(consumerKey, null, null);
    }

    @Override
    public ApplicationKeyMapping getKeyMapping(String consumerKey, String keymanager, String tenantDomain)
            throws DataLoadingException {
        ApplicationKeyMapping application = null;
        String responseString;
        String endPoint = null;
        try {
            if (keymanager != null) {
                keymanager = URLEncoder.encode(keymanager, APIConstants.DigestAuthConstants.CHARSET);
                keymanager = keymanager.replace("\\+", "%20");
                endPoint = APIConstants.SubscriptionValidationResources.APPLICATION_KEY_MAPPINGS + "?consumerKey="
                        + consumerKey + "&keymanager=" + keymanager;
            } else {
                endPoint = APIConstants.SubscriptionValidationResources.APPLICATION_KEY_MAPPINGS + "?consumerKey="
                        + consumerKey;
            }
            responseString = invokeService(endPoint, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ApplicationKeyMappingList list = new Gson().fromJson(responseString, ApplicationKeyMappingList.class);
            if (list.getList() != null && !list.getList().isEmpty()) {
                application = list.getList().get(0); 
            }
        }
        return application;
    }

    @Override
    public API getApi(String context, String version,String tenantDomain) throws DataLoadingException {

        String endPoint = APIConstants.SubscriptionValidationResources.APIS + "?context=" + context +
                "&version=" + version;
        API api = new API();
        String responseString;
        try {
            responseString = invokeService(endPoint, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            APIList list = new Gson().fromJson(responseString, APIList.class);
            if (list.getList() != null && !list.getList().isEmpty()) {
                api = list.getList().get(0);
            }
        }
        return api;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String policyName, String tenantDomain) throws DataLoadingException {

        String endPoint = APIConstants.SubscriptionValidationResources.SUBSCRIPTION_POLICIES + "?policyName=" +
                policyName;
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy();
        if (log.isDebugEnabled()) {
            log.debug("getSubscriptionPolicy for " + policyName + " for tenant " + tenantDomain);
        }
        String responseString;
        try {
            responseString = invokeService(endPoint, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            SubscriptionPolicyList list = new Gson().fromJson(responseString, SubscriptionPolicyList.class);
            if (list.getList() != null && !list.getList().isEmpty()) {
                subscriptionPolicy = list.getList().get(0);
            }
        }
        return subscriptionPolicy;
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String policyName, String tenantDomain) throws DataLoadingException {

        String endPoint = APIConstants.SubscriptionValidationResources.APPLICATION_POLICIES + "?policyName=" +
                policyName;
        ApplicationPolicy applicationPolicy = new ApplicationPolicy();
        if (log.isDebugEnabled()) {
            log.debug("getApplicationPolicy for " + policyName + " for tenant " + tenantDomain);
        }
        String responseString;
        try {
            responseString = invokeService(endPoint, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ApplicationPolicyList list = new Gson().fromJson(responseString, ApplicationPolicyList.class);
            if (list.getList() != null && !list.getList().isEmpty()) {
                applicationPolicy = list.getList().get(0);
            }
        }
        return applicationPolicy;
    }
    
    @Override
    public ApiPolicy getAPIPolicy(String policyName, String tenantDomain) throws DataLoadingException {

        String endPoint = APIConstants.SubscriptionValidationResources.API_POLICIES + "?policyName=" +
                policyName;
        ApiPolicy apiPolicy = new ApiPolicy();
        String responseString;
        if (log.isDebugEnabled()) {
            log.debug("getAPIPolicy for " + policyName + " for tenant " + tenantDomain);
        }
        try {
            responseString = invokeService(endPoint, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            APIPolicyList list = new Gson().fromJson(responseString, APIPolicyList.class);
            if (list.getList() != null && !list.getList().isEmpty()) {
                apiPolicy = list.getList().get(0);
            }
        }
        return apiPolicy;
    }

    @Override
    public List<Scope> loadAllScopes(String tenantDomain) throws DataLoadingException {
        String scopesEp = APIConstants.SubscriptionValidationResources.SCOPES;
        List<Scope> scopes = new ArrayList<>();
        String responseString;
        try {
            responseString = invokeService(scopesEp, tenantDomain);
        } catch (IOException e) {
            String msg = "Error while executing the HTTP client " + scopesEp;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            scopes = new Gson().fromJson(responseString, ScopesList.class).getList();
        }
        return scopes;

    }

    private String invokeService(String path, String tenantDomain) throws DataLoadingException, IOException {

        String serviceURLStr = getEventHubConfigurationDto.getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP);
        HttpGet method = new HttpGet(serviceURLStr + path);

            URL serviceURL = new URL(serviceURLStr + path);
            byte[] credentials = getServiceCredentials(getEventHubConfigurationDto);
            int servicePort = serviceURL.getPort();
            String serviceProtocol = serviceURL.getProtocol();
            method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                    APIConstants.AUTHORIZATION_BASIC +
                            new String(credentials, StandardCharsets.UTF_8));
            if (tenantDomain != null) {
                method.setHeader(APIConstants.HEADER_TENANT, tenantDomain);
            }
            HttpClient httpClient = APIUtil.getHttpClient(servicePort, serviceProtocol);

            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry = false;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    retry = false;
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < retrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving " + path + " from remote endpoint: " + ex.getMessage()
                                + ". Retrying after " + retrievalTimeoutInSeconds +
                                " seconds.");
                        try {
                            Thread.sleep(retrievalTimeoutInSeconds * 1000);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    } else {
                        throw ex;
                    }
                }
            } while (retry);
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("Could not retrieve subscriptions for tenantDomain : " + tenantDomain);
                throw new DataLoadingException("Error while retrieving subscription from " + path);
            }
            String responseString = EntityUtils.toString(httpResponse.getEntity(), UTF8);
            if (log.isDebugEnabled()) {
                log.debug("Response : " + responseString);
            }
            return responseString;

    }

    private byte[] getServiceCredentials(EventHubConfigurationDto eventHubConfigurationDto) {

        String username = eventHubConfigurationDto.getUsername();
        String pw = eventHubConfigurationDto.getPassword();
        return Base64.encodeBase64((username + APIConstants.DELEM_COLON + pw).getBytes
                (StandardCharsets.UTF_8));
    }

}
