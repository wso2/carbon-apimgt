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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.InMemorySubscriptionValidationConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.SubscriptionValidationConfig;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscriber;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubscriptionDataLoaderImpl implements SubscriptionDataLoader {

    private static final Log log = LogFactory.getLog(SubscriptionDataLoaderImpl.class);
    private static SubscriptionValidationConfig subscriptionValidationConfig;

    public static final int retrievalTimeoutInSeconds = 15;
    public static final int retrievalRetries = 15;
    public static final String UTF8 = "UTF-8";

    public SubscriptionDataLoaderImpl() {

        this.subscriptionValidationConfig = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getSubscriptionValidationConfig();
    }

    @Override
    public List<Subscriber> loadAllSubscribers(int tenantId) throws DataLoadingException {

        String subscribersEP = appendParam(APIConstants.SubscriptionValidationResources.SUBSCRIBERS, tenantId);
        List<Subscriber> subscribers = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(subscribersEP, tenantId);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + subscribersEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                subscribers = Arrays.asList(mapper.readValue(responseString, Subscriber[].class));
            } catch (IOException e) {
                log.error("Exception when processing tenant subscriptions data for tenant: " + tenantId +
                        " /n " + responseString, e);
            }
        }
        return subscribers;
    }

    @Override
    public List<Subscription> loadAllSubscriptions(int tenantId) throws DataLoadingException {

        String subscriptionsEP = appendParam(APIConstants.SubscriptionValidationResources.SUBSCRIPTIONS, tenantId);
        List<Subscription> subscriptions = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(subscriptionsEP, tenantId);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + subscriptionsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                subscriptions = Arrays.asList(mapper.readValue(responseString, Subscription[].class));
            } catch (IOException e) {
                log.error("Exception when processing subscriptions data for tenant: " + tenantId +
                        " /n " + responseString, e);
            }
        }
        return subscriptions;
    }

    @Override
    public List<Application> loadAllApplications(int tenantId) throws DataLoadingException {

        String applicationsEP = appendParam(APIConstants.SubscriptionValidationResources.APPLICATIONS, tenantId);
        List<Application> applications = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(applicationsEP, tenantId);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + applicationsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                applications = Arrays.asList(mapper.readValue(responseString, Application[].class));
            } catch (IOException e) {
                log.error("Exception when processing applications data for tenant: " + tenantId +
                        " /n " + responseString, e);
            }
        }
        return applications;
    }

    @Override
    public List<ApplicationKeyMapping> loadAllKeyMappings(int tenantId) throws DataLoadingException {

        String applicationsEP =
                appendParam(APIConstants.SubscriptionValidationResources.APPLICATION_KEY_MAPPINGS, tenantId);
        List<ApplicationKeyMapping> applicationKeyMappings = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(applicationsEP, tenantId);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + applicationsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                applicationKeyMappings = Arrays.asList(mapper.readValue(responseString, ApplicationKeyMapping[].class));
            } catch (IOException e) {
                log.error("Exception when processing application key mappings data for tenant: " + tenantId +
                        " /n " + responseString, e);
            }
        }
        return applicationKeyMappings;
    }

    @Override
    public List<API> loadAllApis(int tenantId) throws DataLoadingException {

        String apisEP = appendParam(APIConstants.SubscriptionValidationResources.APIS, tenantId);
        List<API> apis = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(apisEP, tenantId);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + apisEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                apis = Arrays.asList(mapper.readValue(responseString, API[].class));
            } catch (IOException e) {
                log.error("Exception when processing apis data for tenant: " + tenantId +
                        " /n " + responseString, e);
            }
        }
        return apis;
    }

    @Override
    public List<SubscriptionPolicy> loadAllSubscriptionPolicies(int tenantId) throws DataLoadingException {

        String subscriptionPoliciesEP =
                appendParam(APIConstants.SubscriptionValidationResources.SUBSCRIPTION_POLICIES, tenantId);
        List<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(subscriptionPoliciesEP, tenantId);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + subscriptionPoliciesEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                subscriptionPolicies = Arrays.asList(mapper.readValue(responseString, SubscriptionPolicy[].class));
            } catch (IOException e) {
                log.error("Exception when processing subscription policies data for tenant: " + tenantId +
                        " /n " + responseString, e);
            }
        }
        return subscriptionPolicies;
    }

    @Override
    public List<ApplicationPolicy> loadAllAppPolicies(int tenantId) throws DataLoadingException {

        String applicationsEP =
                appendParam(APIConstants.SubscriptionValidationResources.APPLICATION_POLICIES, tenantId);
        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        String responseString = null;
        try {
            responseString = invokeService(applicationsEP, tenantId);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + applicationsEP;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                applicationPolicies = Arrays.asList(mapper.readValue(responseString, ApplicationPolicy[].class));
            } catch (IOException e) {
                log.error("Exception when processing application policies data for tenant: " + tenantId +
                        " /n " + responseString, e);
            }
        }
        return applicationPolicies;
    }

    @Override
    public Subscriber getSubscriberById(int subscriberId) throws DataLoadingException {

        String endPoint =
                appendParam(APIConstants.SubscriptionValidationResources.SUBSCRIBERS, subscriberId);
        Subscriber subscriber = new Subscriber();
        String responseString = null;
        try {
            responseString = invokeService(endPoint, -1);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                subscriber = mapper.readValue(responseString, Subscriber.class);
            } catch (IOException e) {
                log.error("Exception when processing subscriber data for subscriberId: " + subscriberId +
                        " /n " + responseString, e);
            }
        }
        return subscriber;
    }

    @Override
    public Subscription getSubscriptionById(int subscriptionId) throws DataLoadingException {

        String endPoint =
                appendParam(APIConstants.SubscriptionValidationResources.SUBSCRIPTIONS, subscriptionId);
        Subscription subscription = new Subscription();
        String responseString = null;
        try {
            responseString = invokeService(endPoint, -1);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                subscription = mapper.readValue(responseString, Subscription.class);
            } catch (IOException e) {
                log.error("Exception when processing subscription data for subscription id: " + subscriptionId +
                        " /n " + responseString, e);
            }
        }
        return subscription;
    }

    @Override
    public Application getApplicationById(int appId) throws DataLoadingException {

        String endPoint =
                appendParam(APIConstants.SubscriptionValidationResources.APPLICATIONS, appId);
        Application application = new Application();
        String responseString = null;
        try {
            responseString = invokeService(endPoint, -1);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                application = mapper.readValue(responseString, Application.class);
            } catch (IOException e) {
                log.error("Exception when processing application data for application id: " + appId +
                        " /n " + responseString, e);
            }
        }
        return application;
    }

    @Override
    public ApplicationKeyMapping getKeyMapping(int applicationId, String keyType) throws DataLoadingException {

        String key = applicationId + InMemorySubscriptionValidationConstants.DELEM_PERIOD + keyType;
        String endPoint =
                appendParam(APIConstants.SubscriptionValidationResources.APPLICATION_KEY_MAPPINGS, 0, key);
        ApplicationKeyMapping application = new ApplicationKeyMapping();
        String responseString = null;
        try {
            responseString = invokeService(endPoint, -1);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                application = mapper.readValue(responseString, ApplicationKeyMapping.class);
            } catch (IOException e) {
                log.error("Exception when processing application data for application id: " + applicationId +
                        " /n " + responseString, e);
            }
        }
        return application;
    }

    @Override
    public API getApiById(int apiId) throws DataLoadingException {

        String endPoint =
                appendParam(APIConstants.SubscriptionValidationResources.APIS, apiId);
        API api = new API();
        String responseString = null;
        try {
            responseString = invokeService(endPoint, -1);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                api = mapper.readValue(responseString, API.class);
            } catch (IOException e) {
                log.error("Exception when processing api data for api id: " + apiId +
                        " /n " + responseString, e);
            }
        }
        return api;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyById(int policyId) throws DataLoadingException {

        String endPoint =
                appendParam(APIConstants.SubscriptionValidationResources.SUBSCRIPTION_POLICIES, policyId);
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy();
        String responseString = null;
        try {
            responseString = invokeService(endPoint, -1);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                subscriptionPolicy = mapper.readValue(responseString, SubscriptionPolicy.class);
            } catch (IOException e) {
                log.error("Exception when processing subscription policy data for policyId: " + policyId +
                        " /n " + responseString, e);
            }
        }
        return subscriptionPolicy;
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(int policyId) throws DataLoadingException {

        String endPoint =
                appendParam(APIConstants.SubscriptionValidationResources.APPLICATION_POLICIES, policyId);
        ApplicationPolicy subscriptionPolicy = new ApplicationPolicy();
        String responseString = null;
        try {
            responseString = invokeService(endPoint, -1);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + endPoint;
            log.error(msg, e);
            throw new DataLoadingException(msg, e);
        }
        if (responseString != null && !responseString.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                subscriptionPolicy = mapper.readValue(responseString, ApplicationPolicy.class);
            } catch (IOException e) {
                log.error("Exception when processing application policy data for policyId: " + policyId +
                        " /n " + responseString, e);
            }
        }
        return subscriptionPolicy;
    }

    private String invokeService(String path, int tenantId) throws DataLoadingException, IOException {

        String serviceURLStr = subscriptionValidationConfig.getServiceURL();
        HttpGet method = new HttpGet(serviceURLStr + path);

        if (null != subscriptionValidationConfig && subscriptionValidationConfig.isEnabled()) {
            URL serviceURL = new URL(serviceURLStr + path);
            byte[] credentials = getServiceCredentials(subscriptionValidationConfig);
            int servicePort = serviceURL.getPort();
            String serviceProtocol = serviceURL.getProtocol();
            method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                    APIConstants.AUTHORIZATION_BASIC +
                            new String(credentials, StandardCharsets.UTF_8));
            if (tenantId > 0) {
                method.setHeader(APIConstants.HEADER_TENANT, String.valueOf(tenantId));
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
                log.error("Could not retrieve subscriptions for tenantId : " + tenantId);
                throw new DataLoadingException("Error while retrieving subscription from " + path);
            }
            return EntityUtils.toString(httpResponse.getEntity(), UTF8);
        }
        return null;

    }

    private byte[] getServiceCredentials(SubscriptionValidationConfig subscriptionValidationConfig) {

        String username = subscriptionValidationConfig.getUsername();
        String pw = subscriptionValidationConfig.getPassword();
        return Base64.encodeBase64((username + APIConstants.DELEM_COLON + pw).getBytes
                (StandardCharsets.UTF_8));
    }

    private String appendParam(String ep, int param, String... key) {

        if (key.length == 0) {
            return ep + "/" + "{" + param + "}";
        } else {
            return ep + "/" + "{" + key[0] + "}";
        }
    }

}
