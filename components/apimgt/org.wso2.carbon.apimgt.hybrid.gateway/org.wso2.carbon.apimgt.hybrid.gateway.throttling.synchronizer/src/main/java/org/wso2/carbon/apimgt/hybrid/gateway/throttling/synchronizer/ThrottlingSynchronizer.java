/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.OnPremiseGatewayInitListener;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.AccessTokenDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.OAuthApplicationInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.MicroGatewayCommonUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.mapping.throttling.AdvancedThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.mapping.throttling.SubscriptionThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.AdvancedThrottlePolicyInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.exception.ThrottlingSynchronizerException;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.ThrottlingConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.mapping.throttling.ApplicationThrottlePolicyMappingUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * This class synchronizes throttling policies
 */
public class ThrottlingSynchronizer implements OnPremiseGatewayInitListener {

    private static final Log log = LogFactory.getLog(ThrottlingSynchronizer.class);
    private String applicationPolicyUrl = ThrottlingConstants.EMPTY_STRING;
    private String subscriptionPolicyUrl = ThrottlingConstants.EMPTY_STRING;
    private String advancedPolicyUrl = ThrottlingConstants.EMPTY_STRING;
    private String blockingPolicyUrl = ThrottlingConstants.EMPTY_STRING;

    @Override
    public void completedInitialization() {
        synchronize();
    }

    public void synchronize() {

        log.info("Started synchronizing policies.");
        String username;
        AccessTokenDTO accessTokenDTO;
        try {
            loadTenant();
            username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();

            //Initialize Rest API urls
            String apiAdminUrl = ConfigManager.getConfigManager()
                    .getProperty(OnPremiseGatewayConstants.API_ADMIN_URL_PROPERTY_KEY);
            String apiVersion = ConfigManager.getConfigManager()
                    .getProperty(ThrottlingConstants.API_VERSION_PROPERTY);
            if (apiVersion == null) {
                apiVersion = ThrottlingConstants.API_DEFAULT_VERSION;
                if (log.isDebugEnabled()) {
                    log.debug("Using default API version: " + apiVersion);
                }
            } else if (ThrottlingConstants.CLOUD_API.equals(apiVersion)) {
                apiVersion = ThrottlingConstants.EMPTY_STRING;
                if (log.isDebugEnabled()) {
                    log.debug("Cloud API doesn't have an version. Therefore, removing the version: " + apiVersion);
                }
            }
            if (apiAdminUrl == null) {
                apiAdminUrl = ThrottlingConstants.DEFAULT_API_ADMIN_URL;
                if (log.isDebugEnabled()) {
                    log.debug("Using default API Admin URL." + apiAdminUrl);
                }
            }
            //Remove '//' which is created in the cloud case.
            applicationPolicyUrl = apiAdminUrl + ThrottlingConstants.APPLICATION_POLICIES_SUFFIX
                    .replace(ThrottlingConstants.API_VERSION_PARAM, apiVersion)
                    .replace("//", ThrottlingConstants.URL_PATH_SEPARATOR);

            subscriptionPolicyUrl = apiAdminUrl + ThrottlingConstants.SUBSCRIPTION_POLICIES_SUFFIX
                    .replace(ThrottlingConstants.API_VERSION_PARAM, apiVersion)
                    .replace("//", ThrottlingConstants.URL_PATH_SEPARATOR);

            advancedPolicyUrl = apiAdminUrl + ThrottlingConstants.ADVANCED_POLICIES_SUFFIX
                    .replace(ThrottlingConstants.API_VERSION_PARAM, apiVersion)
                    .replace("//", ThrottlingConstants.URL_PATH_SEPARATOR);

            blockingPolicyUrl = apiAdminUrl + ThrottlingConstants.BLOCKING_POLICIES_SUFFIX
                    .replace(ThrottlingConstants.API_VERSION_PARAM, apiVersion)
                    .replace("//", ThrottlingConstants.URL_PATH_SEPARATOR);

            //fist we need to get an access token to invoke the Admin Rest API
            try {
                OAuthApplicationInfoDTO responseDto = TokenUtil.registerClient();
                String combinedScopes = ThrottlingConstants.TOKEN_TIER_VIEW_SCOPE + " "
                        + ThrottlingConstants.BLOCKING_CONDITION_VIEW_SCOPE;
                accessTokenDTO = TokenUtil.generateAccessToken(responseDto.getClientId(),
                        responseDto.getClientSecret().toCharArray(), combinedScopes);
            } catch (OnPremiseGatewayException e) {
                log.error("Error occurred while creating policies. Unable to generate Access Token.", e);
                return;
            }

            createSubscriptionPolicies(username, accessTokenDTO);
            createApplicationPolicies(username, accessTokenDTO);
            createAdvancedPolicies(username, accessTokenDTO);
        } catch (ThrottlingSynchronizerException | OnPremiseGatewayException e) {
            log.error("Error occurred while creating policies.", e);
            return;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        //Blocking conditions cannot be modified in tenant flow, hence using super tenant flow
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            createBlackListPolicies(username, accessTokenDTO);
        } catch (ThrottlingSynchronizerException e) {
            log.error("Error occurred while creating Blocking Conditions.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        log.info("Synchronizing policies completed.");
    }

    private void createSubscriptionPolicies(String username, AccessTokenDTO accessTokenDTO)
            throws ThrottlingSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Subscription policies.");
        }

        try {
            //Remove any default subscription policies
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
            Policy[] policies = apiProvider.getPolicies(username, PolicyConstants.POLICY_LEVEL_SUB);
            if (log.isDebugEnabled()) {
                log.debug("Deleting existing Subscription policies.");
            }
            for (Policy policy : policies) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting : " + policy.getPolicyName());
                }
                //Don't change the Unlimited policy
                if (!APIConstants.UNLIMITED_TIER.equals(policy.getPolicyName())) {
                    apiProvider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_SUB, policy.getPolicyName());
                }
            }

            //Get the list of subscription policies calling the Admin Rest API
            SubscriptionThrottlePolicyListDTO policyListDTO = getSubscriptionPolicies(accessTokenDTO);

            //Create the subscription policies
            for (SubscriptionThrottlePolicyDTO policyDTO : policyListDTO.getList()) {
                //Don't change the Unlimited policy
                if (!APIConstants.UNLIMITED_TIER.equals(policyDTO.getPolicyName())) {
                    SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil
                            .fromSubscriptionThrottlePolicyDTOToModel(policyDTO);
                    apiProvider.addPolicy(subscriptionPolicy);
                }
            }
        } catch (APIManagementException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Subscription policies.", e);
        } catch (OnPremiseGatewayException | IOException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Subscription policies." +
                    " Unable to get the Subscription policies from REST API", e);
        }
    }

    private SubscriptionThrottlePolicyListDTO getSubscriptionPolicies(AccessTokenDTO accessTokenDTO)
            throws OnPremiseGatewayException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting Subscription Policies using Admin REST API.");
        }

        String apiAdminUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_ADMIN_URL_PROPERTY_KEY);
        if (apiAdminUrl == null) {
            apiAdminUrl = ThrottlingConstants.DEFAULT_API_ADMIN_URL;
            if (log.isDebugEnabled()) {
                log.debug("Using default API Admin URL." + apiAdminUrl);
            }
        }
        URL apiAdminUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiAdminUrl);
        HttpClient httpClient = APIUtil.getHttpClient(apiAdminUrlValue.getPort(), apiAdminUrlValue.getProtocol());

        HttpGet httpGet = new HttpGet(subscriptionPolicyUrl);
        String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER + accessTokenDTO.getAccessToken();
        httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);


        String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
        if (log.isDebugEnabled()) {
            log.debug("Received response from GET Subscription Policies : " + response);
        }

        InputStream is = new ByteArrayInputStream(response.getBytes(
                Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(is, SubscriptionThrottlePolicyListDTO.class);

    }

    private void createApplicationPolicies(String username, AccessTokenDTO accessTokenDTO)
            throws ThrottlingSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Application policies.");
        }

        try {
            //Remove any default application policies
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
            Policy[] policies = apiProvider.getPolicies(username, PolicyConstants.POLICY_LEVEL_APP);
            if (log.isDebugEnabled()) {
                log.debug("Deleting existing Application policies.");
            }
            for (Policy policy : policies) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting : " + policy.getPolicyName());
                }
                //Don't change the Unlimited policy
                if (!APIConstants.UNLIMITED_TIER.equals(policy.getPolicyName())) {
                    apiProvider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_APP, policy.getPolicyName());
                }
            }

            //Get the list of subscription policies calling the Admin Rest API
            ApplicationThrottlePolicyListDTO policyListDTO = getApplicationPolicies(accessTokenDTO);

            //Create the subscription policies
            for (ApplicationThrottlePolicyDTO policyDTO : policyListDTO.getList()) {
                //Don't change the Unlimited policy
                if (!APIConstants.UNLIMITED_TIER.equals(policyDTO.getPolicyName())) {
                    ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil
                            .fromApplicationThrottlePolicyDTOToModel(policyDTO);
                    apiProvider.addPolicy(applicationPolicy);
                }
            }
        } catch (APIManagementException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Application policies.", e);
        } catch (OnPremiseGatewayException | IOException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Application policies." +
                    " Unable to get the Application policies from REST API", e);
        }
    }

    private ApplicationThrottlePolicyListDTO getApplicationPolicies(AccessTokenDTO accessTokenDTO)
            throws OnPremiseGatewayException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting Subscription Policies using Admin REST API.");
        }
        String apiAdminUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_ADMIN_URL_PROPERTY_KEY);
        if (apiAdminUrl == null) {
            apiAdminUrl = ThrottlingConstants.DEFAULT_API_ADMIN_URL;
            if (log.isDebugEnabled()) {
                log.debug("Using default API Admin URL." + apiAdminUrl);
            }
        }
        URL apiAdminUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiAdminUrl);
        HttpClient httpClient = APIUtil.getHttpClient(apiAdminUrlValue.getPort(), apiAdminUrlValue.getProtocol());

        HttpGet httpGet = new HttpGet(applicationPolicyUrl);
        String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER + accessTokenDTO.getAccessToken();
        httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);


        String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
        if (log.isDebugEnabled()) {
            log.debug("Received response from GET Application Policies : " + response);
        }

        InputStream is = new ByteArrayInputStream(response.getBytes(
                Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(is, ApplicationThrottlePolicyListDTO.class);
    }

    private void createAdvancedPolicies(String username, AccessTokenDTO accessTokenDTO)
            throws ThrottlingSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Advanced policies.");
        }

        try {
            //Remove any advanced policies
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
            Policy[] policies = apiProvider.getPolicies(username, PolicyConstants.POLICY_LEVEL_API);
            if (log.isDebugEnabled()) {
                log.debug("Deleting existing Advanced policies.");
            }
            for (Policy policy : policies) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting : " + policy.getPolicyName());
                }
                //Don't change the Unlimited policy
                if (!APIConstants.UNLIMITED_TIER.equals(policy.getPolicyName())) {
                    apiProvider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_API, policy.getPolicyName());
                }
            }

            //Get the list of advance policies calling the Admin Rest API
            AdvancedThrottlePolicyListDTO policyListDTO = getAdvancedPolicies(accessTokenDTO);
            //Get detailed advanced policy list
            List<AdvancedThrottlePolicyDTO> policyDTOList = getAdvancedPolicyList(accessTokenDTO, policyListDTO);
            //Create the advanced policies
            for (AdvancedThrottlePolicyDTO policyDTO : policyDTOList) {
                //Don't change the Unlimited policy
                if (!APIConstants.UNLIMITED_TIER.equals(policyDTO.getPolicyName())) {
                    APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(policyDTO);
                    apiProvider.addPolicy(apiPolicy);
                }
            }
        } catch (APIManagementException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Advanced policies.", e);
        } catch (OnPremiseGatewayException | IOException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Advanced policies." +
                    " Unable to get the Advanced policies from REST API", e);
        }
    }

    private AdvancedThrottlePolicyListDTO getAdvancedPolicies(AccessTokenDTO accessTokenDTO)
            throws OnPremiseGatewayException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting Advanced Policies using Admin REST API.");
        }

        String apiAdminUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_ADMIN_URL_PROPERTY_KEY);
        if (apiAdminUrl == null) {
            apiAdminUrl = ThrottlingConstants.DEFAULT_API_ADMIN_URL;
            if (log.isDebugEnabled()) {
                log.debug("Using default API Admin URL." + apiAdminUrl);
            }
        }
        URL apiAdminUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiAdminUrl);
        HttpClient httpClient = APIUtil.getHttpClient(apiAdminUrlValue.getPort(), apiAdminUrlValue.getProtocol());
        HttpGet httpGet = new HttpGet(advancedPolicyUrl);
        String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER + accessTokenDTO.getAccessToken();
        httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);


        String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
        if (log.isDebugEnabled()) {
            log.debug("Received response from GET Advanced Policies : " + response);
        }

        InputStream is = new ByteArrayInputStream(response.getBytes(
                Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(is, AdvancedThrottlePolicyListDTO.class);
    }

    private List<AdvancedThrottlePolicyDTO> getAdvancedPolicyList(AccessTokenDTO accessTokenDTO,
                                                                  AdvancedThrottlePolicyListDTO policyListDTO)
            throws OnPremiseGatewayException {
        List<AdvancedThrottlePolicyDTO> policyDTOList = new ArrayList<>();
        String apiAdminUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_ADMIN_URL_PROPERTY_KEY);
        if (apiAdminUrl == null) {
            apiAdminUrl = ThrottlingConstants.DEFAULT_API_ADMIN_URL;
            if (log.isDebugEnabled()) {
                log.debug("Using default API Admin URL." + apiAdminUrl);
            }
        }
        URL apiAdminUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiAdminUrl);
        HttpClient httpClient = APIUtil.getHttpClient(apiAdminUrlValue.getPort(), apiAdminUrlValue.getProtocol());

        for (AdvancedThrottlePolicyInfoDTO infoDTO : policyListDTO.getList()) {
            HttpGet httpGet = new HttpGet(advancedPolicyUrl + ThrottlingConstants.URL_PATH_SEPARATOR + infoDTO.getPolicyId());
            String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER + accessTokenDTO.getAccessToken();
            httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);

            String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                    OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
            if (log.isDebugEnabled()) {
                log.debug("Received response from GET Advanced Policy : " + response);
            }
            InputStream is = new ByteArrayInputStream(response.getBytes(
                    Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
            ObjectMapper mapper = new ObjectMapper();
            try {
                policyDTOList.add(mapper.readValue(is, AdvancedThrottlePolicyDTO.class));
            } catch (IOException e) {
                throw new OnPremiseGatewayException("Error occurred while getting Advanced Policies.", e);
            }
        }
        return policyDTOList;
    }

    private void createBlackListPolicies(String username, AccessTokenDTO accessTokenDTO)
            throws ThrottlingSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Blacklist policies.");
        }

        try {
            //Remove any blocking conditions
            if (log.isDebugEnabled()) {
                log.debug("Deleting existing Blocking conditions.");
            }
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
            List<BlockConditionsDTO> blockingConditionDTOList = apiProvider.getBlockConditions();
            for (BlockConditionsDTO dto : blockingConditionDTOList) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting : " + dto.getConditionType() + ":" + dto.getConditionValue());
                }
                apiProvider.deleteBlockCondition(dto.getConditionId());
            }

            //Get blocking conditions
            BlockingConditionListDTO blockingConditionListDTO = getBlockingConditions(accessTokenDTO);
            //Add blocking conditions
            for (BlockingConditionDTO dto : blockingConditionListDTO.getList()) {
                apiProvider.addBlockCondition(dto.getConditionType(), dto.getConditionValue());
            }
        } catch (APIManagementException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Blocking Conditions.", e);
        } catch (OnPremiseGatewayException | IOException e) {
            throw new ThrottlingSynchronizerException("Error occurred while adding Blocking Conditions." +
                    " Unable to get the Blocking Conditions from REST API", e);
        }
    }

    private BlockingConditionListDTO getBlockingConditions(AccessTokenDTO accessTokenDTO)
            throws OnPremiseGatewayException, IOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting Blocking conditions using Admin REST API.");
        }
        String apiAdminUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_ADMIN_URL_PROPERTY_KEY);
        if (apiAdminUrl == null) {
            apiAdminUrl = ThrottlingConstants.DEFAULT_API_ADMIN_URL;
            if (log.isDebugEnabled()) {
                log.debug("Using default API Admin URL." + apiAdminUrl);
            }
        }
        URL apiAdminUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiAdminUrl);
        HttpClient httpClient = APIUtil.getHttpClient(apiAdminUrlValue.getPort(), apiAdminUrlValue.getProtocol());
        HttpGet httpGet = new HttpGet(blockingPolicyUrl);
        String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER + accessTokenDTO.getAccessToken();
        httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);

        String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
        if (log.isDebugEnabled()) {
            log.debug("Received response from GET Blocking Conditions : " + response);
        }

        InputStream is = new ByteArrayInputStream(response.getBytes(
                Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(is, BlockingConditionListDTO.class);

    }

    private void loadTenant() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            ConfigurationContext context = ServiceReferenceHolder.getInstance()
                    .getConfigContextService().getServerConfigContext();
            TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, context);
            if (log.isDebugEnabled()) {
                log.debug("Tenant was loaded into Carbon Context. Tenant : " + tenantDomain
                        + ", Username : " + username);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Skipping loading super tenant space since execution is currently in super tenant flow.");
            }
        }
    }

}
