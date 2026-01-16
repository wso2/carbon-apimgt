/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.GatewayVisibilityPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.dto.KeyManagerPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.OrganizationDetailsDTO;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationInfo;
import org.wso2.carbon.apimgt.api.model.ApplicationInfoKeyManager;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerApplicationUsages;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.api.model.WorkflowTaskService;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.LabelsDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.factory.PersistenceFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.keymgt.KeyMgtNotificationSender;
import org.wso2.carbon.apimgt.impl.monetization.DefaultMonetizationImpl;
import org.wso2.carbon.apimgt.impl.notifier.events.LabelEvent;
import org.wso2.carbon.apimgt.impl.service.KeyMgtRegistrationService;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ContentSearchResultNameComparator;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.AdminApiSearchContent;
import org.wso2.carbon.apimgt.persistence.dto.AdminContentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.SearchContent;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getPaginatedApplicationList;

/**
 * This class provides the core API admin functionality.
 */
public class APIAdminImpl implements APIAdmin {

    private static final Log log = LogFactory.getLog(APIAdminImpl.class);
    protected ApiMgtDAO apiMgtDAO;
    protected LabelsDAO labelsDAO;

    public APIAdminImpl() {
        apiMgtDAO = ApiMgtDAO.getInstance();
        labelsDAO = LabelsDAO.getInstance();
    }

    @Override
    public List<Environment> getAllEnvironments(String tenantDomain) throws APIManagementException {
        List<Environment> dynamicEnvs = apiMgtDAO.getAllEnvironments(tenantDomain);
        // gateway environment name should be unique, ignore environments defined in api-manager.xml with the same name
        // if a dynamic (saved in database) environment exists.
        List<String> dynamicEnvNames = dynamicEnvs.stream().map(Environment::getName).collect(Collectors.toList());
        List<Environment> allEnvs = new ArrayList<>(dynamicEnvs.size() +
                APIUtil.getReadOnlyEnvironments().size());
        // add read only environments first and dynamic environments later
        APIUtil.getReadOnlyEnvironments().values().stream().filter(env ->
                !dynamicEnvNames.contains(env.getName())).forEach(allEnvs::add);
        allEnvs.addAll(dynamicEnvs);

        for (Environment env : allEnvs) {
            if (env.getProvider().equalsIgnoreCase(APIConstants.EXTERNAL_GATEWAY_VENDOR)) {
                maskValues(env);
            }
        }
        return allEnvs;
    }

    @Override
    public Environment getEnvironment(String tenantDomain, String uuid) throws APIManagementException {
        // priority for configured environments over dynamic environments
        // name is the UUID of environments configured in api-manager.xml
        // for backward compatibility, support both plain text and base64 encoded UUIDs
        Environment env = APIUtil.getReadOnlyEnvironments().get(uuid);
        if (env == null) {
            env = apiMgtDAO.getEnvironment(tenantDomain, uuid);
            if (env == null) {
                //try decoding the UUID and search again
                try {
                    String decodedEnvId = new String(Base64.getDecoder().decode(uuid), StandardCharsets.UTF_8);
                    if (log.isDebugEnabled()) {
                        log.debug("Attempting to retrieve environment with decoded UUID: " + decodedEnvId);
                    }
                    env = APIUtil.getReadOnlyEnvironments().get(decodedEnvId);
                    if (env == null) {
                        env = apiMgtDAO.getEnvironment(tenantDomain, decodedEnvId);
                        if (env == null) {
                            String errorMessage = String.format("Failed to retrieve Environment with plain text UUID %s." +
                                    " Environment not found", uuid);
                            log.error(errorMessage);
                            throw new APIMgtResourceNotFoundException(errorMessage, ExceptionCodes.from(
                                    ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND, String.format("UUID '%s'", uuid))
                            );
                        }
                    }
                } catch (IllegalArgumentException e) {
                    //This catches errors if the string is not valid Base64
                    String errorMessage = String.format("Provided env UUID: %s is not a valid Base64 encoded string. " +
                            "Environment not found.", uuid);
                    if (log.isDebugEnabled()) {
                        log.debug(errorMessage, e);
                    }
                    throw new APIMgtResourceNotFoundException(errorMessage, ExceptionCodes.from(
                            ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND, String.format("UUID '%s'", uuid)));
                }
            }
        }
        if (env.getProvider().equalsIgnoreCase(APIConstants.EXTERNAL_GATEWAY_VENDOR)) {
            maskValues(env);
        }
        return env;
    }

    @Override
    public Environment addEnvironment(String tenantDomain, Environment environment) throws APIManagementException {
        if (getAllEnvironments(tenantDomain).stream()
                .anyMatch(e -> StringUtils.equals(e.getName(), environment.getName()))) {
            String errorMessage = String.format("Failed to add Environment. An Environment named %s already exists",
                    environment.getName());
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.EXISTING_GATEWAY_ENVIRONMENT_FOUND,
                            String.format("name '%s'", environment.getName())));
        }
        validateForUniqueVhostNames(environment);
        Environment environmentToStore =  new Environment(environment);
        encryptGatewayConfigurationValues(null, environmentToStore);
        return apiMgtDAO.addEnvironment(tenantDomain, environmentToStore);
    }

    @Override
    public void deleteEnvironment(String tenantDomain, String uuid) throws APIManagementException {
        // check if the VHost exists in the tenant domain with given UUID, throw error if not found
        Environment existingEnv = getEnvironment(tenantDomain, uuid);
        if (existingEnv.isReadOnly()) {
            String errorMessage = String.format("Failed to delete Environment with UUID '%s'. Environment is read only",
                    uuid);
            throw new APIMgtResourceNotFoundException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.READONLY_GATEWAY_ENVIRONMENT, String.format("UUID '%s'", uuid)));
        }
        if (hasExistingDeployments(tenantDomain, uuid)) {
            throw new APIManagementException("Cannot delete the environment with id: " + uuid
                    + " as active gateway policy deployment exist",
                    ExceptionCodes.from(ExceptionCodes.GATEWAY_ENVIRONMENT_ACTIVE_DEPLOYMENTS_EXIST,
                            String.format("UUID '%s'", uuid)));
        }
        if (hasExistingAPIRevisions(tenantDomain, uuid)) {
            throw new APIManagementException("Cannot delete the environment with id: " + uuid
                    + " as API revisions are deployed to it", ExceptionCodes.from(
                    ExceptionCodes.GATEWAY_ENVIRONMENT_API_REVISIONS_EXIST, String.format("UUID '%s'", uuid)));
        }
        apiMgtDAO.deleteEnvironment(uuid);
    }

    public Environment getEnvironmentWithoutPropertyMasking(String tenantDomain, String uuid)
            throws APIManagementException {
        // priority for configured environments over dynamic environments
        // name is the UUID of environments configured in api-manager.xml
        Environment env = APIUtil.getReadOnlyEnvironments().get(uuid);
        if (env == null) {
            env = apiMgtDAO.getEnvironment(tenantDomain, uuid);
            if (env == null) {
                String errorMessage = String.format("Failed to retrieve Environment with UUID %s. " +
                                "Environment not found", uuid);
                throw new APIMgtResourceNotFoundException(errorMessage, ExceptionCodes.from(
                        ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND, String.format("UUID '%s'", uuid))
                );
            }
        }
        return env;
    }

    @Override
    public boolean hasExistingDeployments(String tenantDomain, String uuid) throws APIManagementException {
        Environment existingEnv = getEnvironment(tenantDomain, uuid);
        // check if the policy mapping exists for the given environment
        return StringUtils.isNotEmpty(
                apiMgtDAO.getGatewayPolicyMappingByGatewayLabel(existingEnv.getDisplayName(), tenantDomain));
    }

    private boolean hasExistingAPIRevisions(String tenantDomain, String uuid) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Checking for existing API revisions for "
                    + "gateway environment with UUID '%s' in tenant '%s'", uuid, tenantDomain));
        }
        return apiMgtDAO.hasExistingAPIRevisions(uuid, tenantDomain);
    }

    @Override
    public Environment updateEnvironment(String tenantDomain, Environment environment) throws APIManagementException {
        // check if the VHost exists in the tenant domain with given UUID, throw error if not found
        Environment existingEnv = getEnvironmentWithoutPropertyMasking(tenantDomain, environment.getUuid());
        if (existingEnv.isReadOnly()) {
            String errorMessage = String.format("Failed to update Environment with UUID '%s'. Environment is read only",
                    environment.getUuid());
            throw new APIMgtResourceNotFoundException(errorMessage, ExceptionCodes.from(
                    ExceptionCodes.READONLY_GATEWAY_ENVIRONMENT, String.format("UUID '%s'", environment.getUuid()))
            );
        }

        if (!existingEnv.getName().equals(environment.getName())) {
            String errorMessage = String.format("Failed to update Environment with UUID '%s'. Environment name " +
                            "can not be changed",
                    environment.getUuid());
            throw new APIMgtResourceNotFoundException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.READONLY_GATEWAY_ENVIRONMENT_NAME));
        }

        validateForUniqueVhostNames(environment);
        environment.setId(existingEnv.getId());
        encryptGatewayConfigurationValues(existingEnv, environment);
        Environment updatedEnvironment = apiMgtDAO.updateEnvironment(environment);
        // If the update is successful without throwing an exception
        // Perform a separate task of updating gateway label names
        updateGatewayLabelNameForGatewayPolicies(existingEnv.getDisplayName(), updatedEnvironment.getDisplayName(),
                tenantDomain);
        return updatedEnvironment;
    }

    /**
     * Update the gateway label name for the gateway policies if the environment name is changed.
     *
     * @param oldLabel     Old gateway label name
     * @param newLabel     New gateway label name
     * @param tenantDomain Tenant domain
     * @throws APIManagementException If failed to update the gateway label name
     */
    private void updateGatewayLabelNameForGatewayPolicies(String oldLabel, String newLabel, String tenantDomain)
            throws APIManagementException {
        if (StringUtils.isNotEmpty(apiMgtDAO.getGatewayPolicyMappingByGatewayLabel(oldLabel, tenantDomain))) {
            apiMgtDAO.updateGatewayLabelName(oldLabel, newLabel, tenantDomain);
        }
    }

    private void validateForUniqueVhostNames(Environment environment) throws APIManagementException {
        List<String> hosts = new ArrayList<>(environment.getVhosts().size());
        boolean isDuplicateVhosts = environment.getVhosts().stream().map(VHost::getHost).anyMatch(host -> {
            boolean exist = hosts.contains(host);
            hosts.add(host);
            return exist;
        });
        if (isDuplicateVhosts) {
            String errorMessage = String.format("Failed to add Environment. Virtual Host %s is duplicated",
                    hosts.get(hosts.size() - 1));
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.GATEWAY_ENVIRONMENT_DUPLICATE_VHOST_FOUND));
        }
    }

    @Override
    public Application[] getAllApplicationsOfTenantForMigration(String appTenantDomain) throws APIManagementException {

        return apiMgtDAO.getAllApplicationsOfTenantForMigration(appTenantDomain);
    }

    /**
     * @inheritDoc
     **/
    public Application[] getApplicationsWithPagination(String user, String owner, int tenantId, int limit,
                                                       int offset, String applicationName, String sortBy,
                                                       String sortOrder) throws APIManagementException {

        return apiMgtDAO.getApplicationsWithPagination(user, owner, tenantId, limit, offset, sortBy, sortOrder,
                applicationName);
    }

    /**
     * Get count of the applications for the tenantId.
     *
     * @param tenantId          content to get application count based on tenant_id
     * @param searchOwner       content to search applications based on owners
     * @param searchApplication content to search applications based on application
     * @throws APIManagementException if failed to get application
     */

    public int getApplicationsCount(int tenantId, String searchOwner, String searchApplication)
            throws APIManagementException {

        return apiMgtDAO.getApplicationsCount(tenantId, searchOwner, searchApplication);
    }

    /**
     * These methods load the monetization implementation class
     *
     * @return monetization implementation class
     * @throws APIManagementException if failed to load monetization implementation class
     */
    public Monetization getMonetizationImplClass() throws APIManagementException {

        APIManagerConfiguration configuration = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Monetization monetizationImpl = null;
        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            String monetizationImplClass = configuration.getMonetizationConfigurationDto().getMonetizationImpl();
            if (monetizationImplClass == null) {
                monetizationImpl = new DefaultMonetizationImpl();
            } else {
                try {
                    monetizationImpl = (Monetization) APIUtil.getClassInstance(monetizationImplClass);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    APIUtil.handleException("Failed to load monetization implementation class.", e);
                }
            }
        }
        return monetizationImpl;
    }

    /**
     * Derives info about monetization usage publish job
     *
     * @return ifno about the monetization usage publish job
     * @throws APIManagementException
     */
    public MonetizationUsagePublishInfo getMonetizationUsagePublishInfo() throws APIManagementException {

        return apiMgtDAO.getMonetizationUsagePublishInfo();
    }

    /**
     * Updates info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    public void updateMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException {

        apiMgtDAO.updateUsagePublishInfo(monetizationUsagePublishInfo);
    }

    /**
     * Add info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    public void addMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException {

        apiMgtDAO.addMonetizationUsagePublishInfo(monetizationUsagePublishInfo);
    }

    /**
     * The method converts the date into timestamp
     *
     * @param date
     * @return Timestamp in long format
     */
    public long getTimestamp(String date) {

        SimpleDateFormat formatter = new SimpleDateFormat(APIConstants.Monetization.USAGE_PUBLISH_TIME_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone(APIConstants.Monetization.USAGE_PUBLISH_TIME_ZONE));
        long time = 0;
        Date parsedDate;
        try {
            parsedDate = formatter.parse(date);
            time = parsedDate.getTime();
        } catch (java.text.ParseException e) {
            log.error("Error while parsing the date ", e);
        }
        return time;
    }

    @Override
    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(String organization,
            boolean checkUsages) throws APIManagementException {

        // For Choreo scenario (Choreo organization uses the same super tenant Resident Key Manager
        // Hence no need to register the default key manager per organization)
        String tenantDomain = organization;
        try {
            if (APIUtil.isInternalOrganization(organization)) {
                KeyMgtRegistrationService.registerDefaultKeyManager(organization);
            } else {
                tenantDomain = !APIConstants.KeyManager.ALL_KEY_MANAGERS.equals(organization) ?
                        APIUtil.getInternalOrganizationDomain(organization) : organization;
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while retrieving tenant id for organization "
                    + organization, e);
        }

        List<KeyManagerConfigurationDTO> keyManagerConfigurationsByTenant =
                apiMgtDAO.getKeyManagerConfigurationsByOrganization(tenantDomain);
        Iterator<KeyManagerConfigurationDTO> iterator = keyManagerConfigurationsByTenant.iterator();
        KeyManagerConfigurationDTO defaultKeyManagerConfiguration = null;
        while (iterator.hasNext()) {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO = iterator.next();
            if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerConfigurationDTO.getName()) && (
                    APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerConfigurationDTO.getType())
                            || APIConstants.KeyManager.WSO2_IS_KEY_MANAGER_TYPE.equals(
                            keyManagerConfigurationDTO.getType()))) {
                defaultKeyManagerConfiguration = keyManagerConfigurationDTO;
                iterator.remove();
                break;
            }
        }
        if (defaultKeyManagerConfiguration != null) {
            APIUtil.getAndSetDefaultKeyManagerConfiguration(defaultKeyManagerConfiguration);
            keyManagerConfigurationsByTenant.add(defaultKeyManagerConfiguration);
        }

        // This is the Choreo scenario. Hence, need to retrieve the IdPs of the Choreo organization as well
        // and append those to the previous list
        if (!StringUtils.equals(organization, tenantDomain)) {
            List<KeyManagerConfigurationDTO> keyManagerConfigurationsByOrganization =
                    apiMgtDAO.getKeyManagerConfigurationsByOrganization(organization);
            keyManagerConfigurationsByTenant.addAll(keyManagerConfigurationsByOrganization);
        }

        setAliasForTokenExchangeKeyManagers(keyManagerConfigurationsByTenant, tenantDomain);

        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationsByTenant) {
            decryptKeyManagerConfigurationValues(keyManagerConfigurationDTO);
            if (!StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                    keyManagerConfigurationDTO.getTokenType())) {
                getKeyManagerEndpoints(keyManagerConfigurationDTO);
            }
        }

        setIdentityProviderRelatedInformation(keyManagerConfigurationsByTenant, organization);
        if (checkUsages) {
            setKeyManagerUsageRelatedInformation(keyManagerConfigurationsByTenant, organization);
        }
        // Add missing fields for migrated Key manager configs
        Map<String, KeyManagerConnectorConfiguration> keyManagerConnectorConfigurationMap =
                ServiceReferenceHolder.getInstance().getKeyManagerConnectorConfigurations();
        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationsByTenant) {
            if (keyManagerConnectorConfigurationMap.containsKey(keyManagerConfigurationDTO.getType())) {
                keyManagerConnectorConfigurationMap.get(keyManagerConfigurationDTO.getType())
                        .processConnectorConfigurations(keyManagerConfigurationDTO.getAdditionalProperties());
            }
        }
        return keyManagerConfigurationsByTenant;
    }

    private void setIdentityProviderRelatedInformation(List<KeyManagerConfigurationDTO>
                                                               keyManagerConfigurationsByOrganization,
                                                       String organization)
            throws APIManagementException {

        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationsByOrganization) {
            if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                    keyManagerConfigurationDTO.getTokenType()) ||
                    StringUtils.equals(KeyManagerConfiguration.TokenType.BOTH.toString(),
                    keyManagerConfigurationDTO.getTokenType())) {
                try {
                    if (keyManagerConfigurationDTO.getExternalReferenceId() != null) {
                        IdentityProvider identityProvider = IdentityProviderManager.getInstance()
                                .getIdPByResourceId(keyManagerConfigurationDTO.getExternalReferenceId(),
                                        APIUtil.getTenantDomainFromTenantId(
                                                APIUtil.getInternalOrganizationId(organization)), Boolean.FALSE);
                        keyManagerConfigurationDTO.setDescription(identityProvider.getIdentityProviderDescription());
                        keyManagerConfigurationDTO.setEnabled(identityProvider.isEnable());
                    }
                } catch (IdentityProviderManagementException e) {
                    // handled in this way in order to not break other key managers.
                    log.error("IdP retrieval failed. ", e);
                }
            }
        }

    }

    private void setKeyManagerUsageRelatedInformation(
            List<KeyManagerConfigurationDTO> keyManagerConfigurationsByOrganization, String organization)
            throws APIManagementException {

        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationsByOrganization) {

            KeyManagerApplicationUsages appUsages = getApplicationsOfKeyManager(keyManagerConfigurationDTO.getUuid(), 0,
                    Integer.MAX_VALUE);
            if (appUsages.getApplicationCount() > 0) {
                keyManagerConfigurationDTO.setUsed(true);
                continue;
            }

            AdminContentSearchResult apiUsages = getAPIUsagesByKeyManagerNameAndOrganization(organization,
                    keyManagerConfigurationDTO.getName(), 0, Integer.MAX_VALUE);
            if (apiUsages.getApiCount() > 0) {
                keyManagerConfigurationDTO.setUsed(true);
                continue;
            }
            keyManagerConfigurationDTO.setUsed(false);
        }
    }

    private void setAliasForTokenExchangeKeyManagers(List<KeyManagerConfigurationDTO> keyManagerConfigurationsByTenant,
                                                     String tenantDomain) throws APIManagementException {
        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationsByTenant) {
            if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                    keyManagerConfigurationDTO.getTokenType()) ||
                    StringUtils.equals(KeyManagerConfiguration.TokenType.BOTH.toString(),
                    keyManagerConfigurationDTO.getTokenType())) {
                if (keyManagerConfigurationDTO.getExternalReferenceId() != null) {
                    IdentityProvider identityProvider;
                    try {
                        identityProvider = IdentityProviderManager.getInstance()
                                .getIdPByResourceId(keyManagerConfigurationDTO.getExternalReferenceId(), tenantDomain,
                                        Boolean.FALSE);
                    } catch (IdentityProviderManagementException e) {
                        throw new APIManagementException("IdP retrieval failed. " + e.getMessage(), e,
                                ExceptionCodes.IDP_RETRIEVAL_FAILED);
                    }
                    // Set alias value since this will be used from the Devportal side.
                    keyManagerConfigurationDTO.setAlias(identityProvider.getAlias());
                }
            }
        }
    }

    @Override
    public Map<String, List<KeyManagerConfigurationDTO>> getAllKeyManagerConfigurations()
            throws APIManagementException {

        List<KeyManagerConfigurationDTO> keyManagerConfigurations = apiMgtDAO.getKeyManagerConfigurations();
        Map<String, List<KeyManagerConfigurationDTO>> keyManagerConfigurationsByTenant = new HashMap<>();
        for (KeyManagerConfigurationDTO keyManagerConfiguration : keyManagerConfigurations) {
            List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS;
            if (keyManagerConfigurationsByTenant.containsKey(keyManagerConfiguration.getOrganization())) {
                keyManagerConfigurationDTOS =
                        keyManagerConfigurationsByTenant.get(keyManagerConfiguration.getOrganization());
            } else {
                keyManagerConfigurationDTOS = new ArrayList<>();
            }
            if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerConfiguration.getName()) && (
                    APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerConfiguration.getType())
                            || APIConstants.KeyManager.WSO2_IS_KEY_MANAGER_TYPE.equals(
                            keyManagerConfiguration.getType()))) {
                APIUtil.getAndSetDefaultKeyManagerConfiguration(keyManagerConfiguration);
            }
            keyManagerConfigurationDTOS.add(keyManagerConfiguration);
            keyManagerConfigurationsByTenant
                    .put(keyManagerConfiguration.getOrganization(), keyManagerConfigurationDTOS);
        }
        return keyManagerConfigurationsByTenant;
    }

    @Override
    public KeyManagerConfigurationDTO getKeyManagerConfigurationById(String organization, String id)
            throws APIManagementException {

        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiMgtDAO.getKeyManagerConfigurationByID(organization, id);
        if (keyManagerConfigurationDTO == null){
            return null;
        }
        if (keyManagerConfigurationDTO != null) {
            if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerConfigurationDTO.getName()) && (
                    APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerConfigurationDTO.getType())
                            || APIConstants.KeyManager.WSO2_IS_KEY_MANAGER_TYPE.equals(
                            keyManagerConfigurationDTO.getType()))) {
                APIUtil.getAndSetDefaultKeyManagerConfiguration(keyManagerConfigurationDTO);
            }
            maskValues(keyManagerConfigurationDTO);
        }
        if (!KeyManagerConfiguration.TokenType.valueOf(keyManagerConfigurationDTO.getTokenType().toUpperCase())
                .equals(KeyManagerConfiguration.TokenType.EXCHANGED)) {
            maskValues(keyManagerConfigurationDTO);
        }
        if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                keyManagerConfigurationDTO.getTokenType()) ||
                StringUtils.equals(KeyManagerConfiguration.TokenType.BOTH.toString(),
                keyManagerConfigurationDTO.getTokenType())) {
            try {
                if (keyManagerConfigurationDTO.getExternalReferenceId() != null) {
                    IdentityProvider identityProvider = IdentityProviderManager.getInstance()
                            .getIdPByResourceId(keyManagerConfigurationDTO.getExternalReferenceId(),
                                    APIUtil.getInternalOrganizationDomain(organization), Boolean.FALSE);
                    mergeIdpWithKeyManagerConfiguration(identityProvider, keyManagerConfigurationDTO);
                }
            } catch (IdentityProviderManagementException e) {
                throw new APIManagementException("IdP retrieval failed. " + e.getMessage(), e,
                        ExceptionCodes.IDP_RETRIEVAL_FAILED);
            }
        }
        if (!StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                keyManagerConfigurationDTO.getTokenType())) {
            getKeyManagerEndpoints(keyManagerConfigurationDTO);
        }
        return keyManagerConfigurationDTO;
    }

    @Override
    public KeyManagerConfigurationDTO getGlobalKeyManagerConfigurationById(String id) throws APIManagementException {
        KeyManagerConfigurationDTO keyManagerConfigurationDTO = apiMgtDAO.getKeyManagerConfigurationByID(
                APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN, id);
        if (keyManagerConfigurationDTO != null) {
            maskValues(keyManagerConfigurationDTO);
        }
        return keyManagerConfigurationDTO;
    }

    @Override
    public void deleteGlobalKeyManagerConfigurationById(String id) throws APIManagementException {

        KeyManagerConfigurationDTO keyManagerConfigurationDTO = apiMgtDAO.getKeyManagerConfigurationByID(
                APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN, id);
        if (keyManagerConfigurationDTO != null) {
            apiMgtDAO.deleteKeyManagerConfigurationById(id, APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN);
            new KeyMgtNotificationSender()
                    .notify(keyManagerConfigurationDTO, APIConstants.KeyManager.KeyManagerEvent.ACTION_DELETE);
        }
    }

    @Override
    public LLMProvider addLLMProvider(String organization, LLMProvider provider) throws APIManagementException {

        provider.setId(UUID.randomUUID().toString());
        LLMProvider result = apiMgtDAO.addLLMProvider(organization, provider);
        new LLMProviderNotificationSender().notify(result.getId(), result.getName(), result.getApiVersion(),
                provider.getConfigurations(), organization, APIConstants.EventType.LLM_PROVIDER_CREATE.name());
        return result;
    }

    @Override
    public List<LLMProvider> getLLMProviders(String organization, String name, String apiVersion,
                                             Boolean builtInSupport) throws APIManagementException {

        return apiMgtDAO.getLLMProviders(organization, name, apiVersion, builtInSupport);
    }

    @Override
    public String deleteLLMProvider(String organization, LLMProvider provider, boolean builtIn)
            throws APIManagementException {

        String deletedLLMProviderId = apiMgtDAO.deleteLLMProvider(organization, provider.getId(), builtIn);
        new LLMProviderNotificationSender().notify(provider.getId(), provider.getName(), provider.getApiVersion(),
                provider.getConfigurations(), organization, APIConstants.EventType.LLM_PROVIDER_DELETE.name());
        return deletedLLMProviderId;
    }

    @Override
    public LLMProvider updateLLMProvider(String organization, LLMProvider provider) throws APIManagementException {

        LLMProvider result = apiMgtDAO.updateLLMProvider(organization, provider);
        if (!result.isBuiltInSupport()) {
            new LLMProviderNotificationSender().notify(result.getId(), result.getName(), result.getApiVersion(),
                    provider.getConfigurations(), organization, APIConstants.EventType.LLM_PROVIDER_UPDATE.name());
        }
        return result;
    }

    @Override
    public LLMProvider getLLMProvider(String organization, String llmProviderId) throws APIManagementException {

        LLMProvider llmProvider = apiMgtDAO.getLLMProvider(organization, llmProviderId);
        if (llmProvider== null) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.AI_SERVICE_PROVIDER_NOT_FOUND, llmProviderId));
        }
        return llmProvider;
    }

    @Override
    public boolean isIDPExistInOrg(String organization, String resourceId) throws APIManagementException {
        return apiMgtDAO.isIDPExistInOrg(organization, resourceId);
    }

    @Override
    public ApplicationInfo getLightweightApplicationByConsumerKey(String consumerKey) throws APIManagementException {
        return apiMgtDAO.getLightweightApplicationByConsumerKey(consumerKey);
    }

    @Override
    public boolean isKeyManagerConfigurationExistById(String organization, String id) throws APIManagementException {

        return apiMgtDAO.isKeyManagerConfigurationExistById(organization, id);
    }

    @Override
    public KeyManagerConfigurationDTO addKeyManagerConfiguration(
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) throws APIManagementException {

        if (apiMgtDAO.isKeyManagerConfigurationExistByName(keyManagerConfigurationDTO.getName(),
                keyManagerConfigurationDTO.getOrganization())) {
            throw new APIManagementException(
                    "Key manager Already Exist by Name " + keyManagerConfigurationDTO.getName() + " in tenant " +
                            keyManagerConfigurationDTO.getOrganization(), ExceptionCodes.KEY_MANAGER_ALREADY_EXIST);
        }
        if (!KeyManagerConfiguration.TokenType.valueOf(keyManagerConfigurationDTO.getTokenType().toUpperCase())
                .equals(KeyManagerConfiguration.TokenType.EXCHANGED)) {
            sanitizeKeyManagerConfiguration(keyManagerConfigurationDTO);
            validateKeyManagerConfiguration(keyManagerConfigurationDTO, null);
            validateKeyManagerEndpointConfiguration(keyManagerConfigurationDTO);
        }
        if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                keyManagerConfigurationDTO.getTokenType()) ||
                StringUtils.equals(KeyManagerConfiguration.TokenType.BOTH.toString(),
                keyManagerConfigurationDTO.getTokenType())) {
            keyManagerConfigurationDTO.setUuid(UUID.randomUUID().toString());
            try {
                IdentityProvider identityProvider = IdentityProviderManager.getInstance()
                        .addIdPWithResourceId(createIdp(keyManagerConfigurationDTO),
                                APIUtil.getInternalOrganizationDomain(keyManagerConfigurationDTO.getOrganization()));
                keyManagerConfigurationDTO.setExternalReferenceId(identityProvider.getResourceId());
            } catch (IdentityProviderManagementException e) {
                throw new APIManagementException("IdP adding failed. " + e.getMessage(), e,
                        ExceptionCodes.IDP_ADDING_FAILED);
            }
        }

        if (StringUtils.isBlank(keyManagerConfigurationDTO.getUuid())) {
            keyManagerConfigurationDTO.setUuid(UUID.randomUUID().toString());
        }
        KeyManagerConfigurationDTO keyManagerConfigurationToStore =
                new KeyManagerConfigurationDTO(keyManagerConfigurationDTO);
        encryptKeyManagerConfigurationValues(null, keyManagerConfigurationToStore);
        apiMgtDAO.addKeyManagerConfiguration(keyManagerConfigurationToStore);
        new KeyMgtNotificationSender()
                .notify(keyManagerConfigurationDTO, APIConstants.KeyManager.KeyManagerEvent.ACTION_ADD);
        return keyManagerConfigurationDTO;
    }

    public AdminContentSearchResult getAPIUsagesByKeyManagerNameAndOrganization(String org, String keyManagerName,
            int offset, int limit) throws APIManagementException {

        APIPersistence apiPersistenceInstance = PersistenceFactory.getAPIPersistenceInstance();
        String searchQuery = APIConstants.API_USAGE_BY_KEY_MANAGER_QUERY.replace("$1", keyManagerName);
        try {
            return apiPersistenceInstance.searchContentForAdmin(org, searchQuery, offset, limit, limit);
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while finding the key manager ", e);
        }
    }

    public KeyManagerApplicationUsages getApplicationsOfKeyManager(String keyManagerId, int offset, int limit)
            throws APIManagementException {

        KeyManagerApplicationUsages keyManagerApplicationUsages = new KeyManagerApplicationUsages();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        List<ApplicationInfoKeyManager> applications = apiMgtDAO.getAllApplicationsOfKeyManager(keyManagerId);
        keyManagerApplicationUsages.setApplicationCount(applications.size());
        keyManagerApplicationUsages.setApplications(getPaginatedApplicationList(applications, offset, limit));
        return keyManagerApplicationUsages;
    }

    private void validateKeyManagerEndpointConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        if (!(APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerConfigurationDTO.getName()) && (
                APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerConfigurationDTO.getType())
                        || APIConstants.KeyManager.WSO2_IS_KEY_MANAGER_TYPE.equals(
                        keyManagerConfigurationDTO.getType())))) {
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = ServiceReferenceHolder.getInstance()
                    .getKeyManagerConnectorConfiguration(keyManagerConfigurationDTO.getType());
            if (keyManagerConnectorConfiguration != null) {
                List<String> missingRequiredConfigurations = new ArrayList<>();
                for (ConfigurationDto configurationDto : keyManagerConnectorConfiguration
                        .getEndpointConfigurations()) {
                    if (configurationDto.isRequired()) {
                        if (!keyManagerConfigurationDTO.getEndpoints().containsKey(configurationDto.getName())) {
                            if (configurationDto.getDefaultValue() != null
                                    && configurationDto.getDefaultValue() instanceof String
                                    && StringUtils.isNotEmpty((String) configurationDto.getDefaultValue())) {
                                keyManagerConfigurationDTO.getEndpoints().put(configurationDto.getName(),
                                        (String) configurationDto.getDefaultValue());
                            }
                            missingRequiredConfigurations.add(configurationDto.getName());
                        }
                    }
                }
                if (!missingRequiredConfigurations.isEmpty()) {
                    throw new APIManagementException("Key Manager Endpoint Configuration value for " + String.join(",",
                            missingRequiredConfigurations) + " is/are required",
                            ExceptionCodes.REQUIRED_KEY_MANAGER_CONFIGURATION_MISSING);
                }
            }
        }
    }

    private void encryptConfigurationInNestedFields(List<Object> configurations,
                                                    Map<String, Object> additionalProperties,
                                                    KeyManagerConfigurationDTO retrievedKeyManagerConfigurationDTO) throws APIManagementException {
        if (configurations == null || configurations.isEmpty()) {
            return;
        }
        for (Object configuration : configurations) {
            ConfigurationDto configurationDto = (ConfigurationDto) configuration;
            if (configurationDto.isMask()) {
                String value = (String) additionalProperties.get(configurationDto.getName());
                if (APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD.equals(value)) {
                    if (retrievedKeyManagerConfigurationDTO != null) {
                        Object unModifiedValue = retrievedKeyManagerConfigurationDTO.getAdditionalProperties()
                                .get(configurationDto.getName());
                        additionalProperties.replace(configurationDto.getName(), unModifiedValue);
                    }
                } else if (StringUtils.isNotEmpty(value)) {
                    additionalProperties.replace(configurationDto.getName(), encryptValues(value));
                }
            }
            // Recursively process nested values
            encryptConfigurationInNestedFields(((ConfigurationDto) configuration).getValues(),
                    additionalProperties, retrievedKeyManagerConfigurationDTO);
        }
    }

    private void encryptKeyManagerConfigurationValues(KeyManagerConfigurationDTO retrievedKeyManagerConfigurationDTO,
                                                      KeyManagerConfigurationDTO updatedKeyManagerConfigurationDto)
            throws APIManagementException {

        KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = ServiceReferenceHolder.getInstance()
                .getKeyManagerConnectorConfiguration(updatedKeyManagerConfigurationDto.getType());
        if (keyManagerConnectorConfiguration != null) {
            Map<String, Object> additionalProperties = updatedKeyManagerConfigurationDto.getAdditionalProperties();
            for (ConfigurationDto configurationDto : keyManagerConnectorConfiguration
                    .getConnectionConfigurations()) {
                if (configurationDto.isMask()) {
                    String value = (String) additionalProperties.get(configurationDto.getName());
                    if (APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD.equals(value)) {
                        if (retrievedKeyManagerConfigurationDTO != null) {
                            Object unModifiedValue = retrievedKeyManagerConfigurationDTO.getAdditionalProperties()
                                    .get(configurationDto.getName());
                            additionalProperties.replace(configurationDto.getName(), unModifiedValue);
                        }
                    } else if (StringUtils.isNotEmpty(value)) {
                        additionalProperties.replace(configurationDto.getName(), encryptValues(value));
                    }
                }
            }
            // if authConfiguration array is not empty, encrypt values there as well
            if (keyManagerConnectorConfiguration.getAuthConfigurations() != null
                    && !(keyManagerConnectorConfiguration.getAuthConfigurations().isEmpty())) {
                List<ConfigurationDto> authConfigurations = keyManagerConnectorConfiguration.getAuthConfigurations();
                // Recursively check nested objects in authConfigurations and apply encryption
                for (ConfigurationDto authConfiguration : authConfigurations) {
                    encryptConfigurationInNestedFields(authConfiguration.getValues(), additionalProperties,
                            retrievedKeyManagerConfigurationDTO);
                }
            }
        }
    }

    private void encryptGatewayConfigurationValues(Environment retrievedGatewayConfigurationDTO,
            Environment updatedGatewayConfigurationDto) throws APIManagementException {

        GatewayAgentConfiguration gatewayConfiguration = ServiceReferenceHolder.getInstance()
                .getExternalGatewayConnectorConfiguration(updatedGatewayConfigurationDto.getGatewayType());
        if (gatewayConfiguration != null) {
            Map<String, String> additionalProperties = updatedGatewayConfigurationDto.getAdditionalProperties();
            List<ConfigurationDto> connectionConfigurations = gatewayConfiguration.getConnectionConfigurations();
            if (connectionConfigurations != null && !connectionConfigurations.isEmpty()) {
                for (ConfigurationDto configurationDto : connectionConfigurations) {
                    applyGatewayConfigMaskingAndEncryption(configurationDto, additionalProperties,
                            retrievedGatewayConfigurationDTO);
                }
            }
        }
    }

    /**
     * Masks or encrypts gateway configuration values.
     * If a value is the default masked password, it restores the original value from the
     * retrieved configuration. Otherwise, it encrypts non-empty values.
     * The same process is applied to any nested configurations.
     *
     * @param configurationDto The configuration to process.
     * @param additionalProperties The map of configuration name-value pairs.
     * @param retrievedGatewayConfigurationDTO The existing configuration for restoring values.
     * @throws APIManagementException If an error occurs during processing.
     */
    private void applyGatewayConfigMaskingAndEncryption(ConfigurationDto configurationDto,
            Map<String, String> additionalProperties, Environment retrievedGatewayConfigurationDTO)
            throws APIManagementException {

        if (configurationDto.isMask()) {
            String value = additionalProperties.get(configurationDto.getName());
            if (APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD.equals(value)) {
                if (retrievedGatewayConfigurationDTO != null) {
                    String unModifiedValue = retrievedGatewayConfigurationDTO.getAdditionalProperties()
                            .get(configurationDto.getName());
                    if (unModifiedValue != null) {
                        additionalProperties.replace(configurationDto.getName(), unModifiedValue);
                    }
                }
            } else if (StringUtils.isNotEmpty(value)) {
                additionalProperties.replace(configurationDto.getName(), String.valueOf(encryptValues(value)));
            }
        }

        List<Object> nestedConfigurationValues = configurationDto.getValues();
        if (nestedConfigurationValues != null && !nestedConfigurationValues.isEmpty()) {
            for (Object nestedConfiguration : nestedConfigurationValues) {
                if (nestedConfiguration instanceof ConfigurationDto) {
                    applyGatewayConfigMaskingAndEncryption((ConfigurationDto) nestedConfiguration, additionalProperties,
                            retrievedGatewayConfigurationDTO);
                }
            }
        }
    }

    private KeyManagerConfigurationDTO decryptKeyManagerConfigurationValues(
            KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {

        Map<String, Object> additionalProperties = keyManagerConfigurationDTO.getAdditionalProperties();
        for (Map.Entry<String, Object> entry : additionalProperties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                additionalProperties.replace(key, decryptValue(value));
            }
        }
        return keyManagerConfigurationDTO;
    }

    public Environment decryptGatewayConfigurationValues(Environment environment)
            throws APIManagementException {

        Map<String, String> additionalProperties = environment.getAdditionalProperties();
        for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                additionalProperties.replace(key, String.valueOf(decryptValue(value)));
            }
        }
        return environment;
    }

    private Object decryptValue(Object value) throws APIManagementException {

        if (value instanceof String) {
            return getDecryptedValue((String) value);
        } else if (value instanceof List) {
            List valueList = (List) value;
            List decryptedValues = new ArrayList<>();
            for (Object s : valueList) {
                decryptedValues.add(decryptValue(s));
            }
            return decryptedValues;
        } else if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object entryValue = entry.getValue();
                map.replace(key, decryptValue(entryValue));
            }
            return map;
        }
        return value;
    }

    private String getDecryptedValue(String value) throws APIManagementException {

        try {
            JsonElement encryptedJsonValue = new JsonParser().parse(value);
            if (encryptedJsonValue instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) encryptedJsonValue;
                JsonPrimitive encryptedValue = jsonObject.getAsJsonPrimitive(APIConstants.ENCRYPTED_VALUE);
                if (encryptedValue.isBoolean()) {
                    JsonPrimitive valueElement = jsonObject.getAsJsonPrimitive(APIConstants.VALUE);
                    if (encryptedValue.getAsBoolean()) {
                        if (valueElement.isString()) {
                            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                            return new String(cryptoUtil.decrypt(valueElement.getAsString().getBytes()));
                        }
                    }
                }
            }
        } catch (CryptoException e) {
            throw new APIManagementException("Error while Decrypting value", e);
        } catch (JsonParseException e) {
            // check Element is a json element
            if (log.isDebugEnabled()) {
                log.debug("Error while parsing element " + value, e);
            }
        }
        return value;
    }

    @Override
    public KeyManagerConfigurationDTO updateKeyManagerConfiguration(
            KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        KeyManagerConfigurationDTO oldKeyManagerConfiguration =
                apiMgtDAO.getKeyManagerConfigurationByID(keyManagerConfigurationDTO.getOrganization(),
                        keyManagerConfigurationDTO.getUuid());
        if (oldKeyManagerConfiguration == null) {
            String errorMsg = String.format(
                    "Key Manager configuration not found for id '%s' in organization '%s'",
                    keyManagerConfigurationDTO.getUuid(),
                    keyManagerConfigurationDTO.getOrganization());
            throw new APIMgtResourceNotFoundException(
                    errorMsg,
                    ExceptionCodes.from(ExceptionCodes.KEY_MANAGER_NOT_FOUND,
                            keyManagerConfigurationDTO.getUuid()));
        }
        if (!KeyManagerConfiguration.TokenType.valueOf(keyManagerConfigurationDTO.getTokenType().toUpperCase())
                .equals(KeyManagerConfiguration.TokenType.EXCHANGED)) {
            sanitizeKeyManagerConfiguration(keyManagerConfigurationDTO);
            validateKeyManagerConfiguration(keyManagerConfigurationDTO, oldKeyManagerConfiguration);
            validateKeyManagerEndpointConfiguration(keyManagerConfigurationDTO);
        }
        if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                keyManagerConfigurationDTO.getTokenType()) ||
                StringUtils.equals(KeyManagerConfiguration.TokenType.BOTH.toString(),
                        keyManagerConfigurationDTO.getTokenType())) {
            IdentityProvider identityProvider;
            try {
                if (StringUtils.isNotEmpty(oldKeyManagerConfiguration.getExternalReferenceId())) {
                    IdentityProvider retrievedIDP = IdentityProviderManager.getInstance()
                            .getIdPByResourceId(oldKeyManagerConfiguration.getExternalReferenceId(),
                                    APIUtil.getInternalOrganizationDomain(keyManagerConfigurationDTO.getOrganization()), Boolean.FALSE);
                    identityProvider = IdentityProviderManager.getInstance()
                            .updateIdPByResourceId(oldKeyManagerConfiguration.getExternalReferenceId(),
                                    updatedIDP(retrievedIDP,keyManagerConfigurationDTO),
                                    APIUtil.getInternalOrganizationDomain(keyManagerConfigurationDTO.getOrganization()));
                } else {
                    identityProvider = IdentityProviderManager.getInstance()
                            .addIdPWithResourceId(createIdp(keyManagerConfigurationDTO),
                                    APIUtil.getInternalOrganizationDomain(keyManagerConfigurationDTO.getOrganization()));
                    keyManagerConfigurationDTO.setExternalReferenceId(identityProvider.getResourceId());
                }
            } catch (IdentityProviderManagementException e) {
                throw new APIManagementException("IdP adding failed. " + e.getMessage(), e,
                        ExceptionCodes.IDP_ADDING_FAILED);
            }
            keyManagerConfigurationDTO.setExternalReferenceId(identityProvider.getResourceId());
        }
        if ((StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                oldKeyManagerConfiguration.getTokenType()) ||
                StringUtils.equals(KeyManagerConfiguration.TokenType.BOTH.toString(),
                        oldKeyManagerConfiguration.getTokenType())) &&
                StringUtils.equals(KeyManagerConfiguration.TokenType.DIRECT.toString(),
                        keyManagerConfigurationDTO.getTokenType())) {
            // Delete Identity Provider Created.
            if (StringUtils.isNotEmpty(oldKeyManagerConfiguration.getExternalReferenceId())) {
                try {
                    IdentityProviderManager.getInstance().deleteIdPByResourceId(oldKeyManagerConfiguration.getExternalReferenceId(),
                            APIUtil.getInternalOrganizationDomain(keyManagerConfigurationDTO.getOrganization()));
                    keyManagerConfigurationDTO.setExternalReferenceId(null);
                } catch (IdentityProviderManagementException e) {
                    throw new APIManagementException("IdP deletion failed. " + e.getMessage(), e,
                            ExceptionCodes.IDP_DELETION_FAILED);
                }
            }
        }
        encryptKeyManagerConfigurationValues(oldKeyManagerConfiguration, keyManagerConfigurationDTO);
        apiMgtDAO.updateKeyManagerConfiguration(keyManagerConfigurationDTO);
        KeyManagerConfigurationDTO decryptedKeyManagerConfiguration =
                decryptKeyManagerConfigurationValues(keyManagerConfigurationDTO);
        new KeyMgtNotificationSender()
                .notify(decryptedKeyManagerConfiguration, APIConstants.KeyManager.KeyManagerEvent.ACTION_UPDATE);
        return keyManagerConfigurationDTO;
    }
    @Override
    public KeyManagerPermissionConfigurationDTO getKeyManagerPermissions(String id) throws APIManagementException {

        KeyManagerPermissionConfigurationDTO keyManagerPermissionConfigurationDTO;
        try {
            keyManagerPermissionConfigurationDTO = apiMgtDAO.getKeyManagerPermissions(id);
        } catch (APIManagementException e) {
            throw new APIManagementException("Key Manager Permissions retrieval failed for Key Manager id " + id, e);
        }
        return keyManagerPermissionConfigurationDTO;
    }

    @Override
    public GatewayVisibilityPermissionConfigurationDTO getGatewayVisibilityPermissions(String id) throws APIManagementException {

        GatewayVisibilityPermissionConfigurationDTO gatewayVisibilityPermissionConfigurationDTO;
        try {
            gatewayVisibilityPermissionConfigurationDTO = apiMgtDAO.getGatewayVisibilityPermissions(id);
        } catch (APIManagementException e) {
            throw new APIManagementException("Gateway Visibility Permissions retrieval failed for gateway environment id " + id, e);
        }
        return gatewayVisibilityPermissionConfigurationDTO;
    }

    private IdentityProvider updatedIDP(IdentityProvider retrievedIDP,
                                        KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        IdentityProvider identityProvider = cloneIdentityProvider(retrievedIDP);
        String idpName = sanitizeName(
                getSubstringOfTen(keyManagerConfigurationDTO.getName()) + "_" + keyManagerConfigurationDTO.getOrganization() + "_"
                        + keyManagerConfigurationDTO.getUuid());
        identityProvider.setIdentityProviderName(idpName);
        identityProvider.setDisplayName(keyManagerConfigurationDTO.getDisplayName());
        identityProvider.setPrimary(Boolean.FALSE);
        identityProvider.setIdentityProviderDescription(keyManagerConfigurationDTO.getDescription());
        identityProvider.setAlias(keyManagerConfigurationDTO.getAlias());
        String certificate = null;
        if (keyManagerConfigurationDTO.getAdditionalProperties().containsKey(APIConstants.KeyManager.CERTIFICATE_VALUE)){
            certificate =
                    (String) keyManagerConfigurationDTO.getAdditionalProperties().get(APIConstants.KeyManager.CERTIFICATE_VALUE);

        }
        String certificateType = null;
        if (keyManagerConfigurationDTO.getAdditionalProperties().containsKey(APIConstants.KeyManager.CERTIFICATE_TYPE)) {
            certificateType =
                    (String) keyManagerConfigurationDTO.getAdditionalProperties().get(APIConstants.KeyManager.CERTIFICATE_TYPE);
        }
        List<IdentityProviderProperty> idpProperties = new ArrayList<>();

        if (StringUtils.isNotEmpty(certificate) && StringUtils.isNotEmpty(certificateType)) {
            if (APIConstants.KeyManager.CERTIFICATE_TYPE_JWKS_ENDPOINT.equals(certificateType)) {
                if (StringUtils.isNotBlank(certificate)) {
                    IdentityProviderProperty jwksProperty = new IdentityProviderProperty();
                    jwksProperty.setName(APIConstants.JWKS_URI);
                    jwksProperty.setValue(certificate);
                    idpProperties.add(jwksProperty);
                }
            } else if (APIConstants.KeyManager.CERTIFICATE_TYPE_PEM_FILE.equals(certificateType)) {
                identityProvider.setCertificate(String.join(certificate, ""));
            }
        }

        if (keyManagerConfigurationDTO.getProperty(APIConstants.KeyManager.ISSUER) != null) {
            IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
            identityProviderProperty.setName(IdentityApplicationConstants.IDP_ISSUER_NAME);
            identityProviderProperty.setValue((String) keyManagerConfigurationDTO.getProperty(APIConstants.KeyManager.ISSUER));
            idpProperties.add(identityProviderProperty);
        }

        if (idpProperties.size() > 0) {
            identityProvider.setIdpProperties(idpProperties.toArray(new IdentityProviderProperty[0]));
        }

        identityProvider.setEnable(keyManagerConfigurationDTO.isEnabled());
        Object claims = keyManagerConfigurationDTO.getProperty(APIConstants.KeyManager.CLAIM_MAPPING);
        updateClaims(identityProvider, claims);
        return identityProvider;
    }

    @Override
    public void deleteIdentityProvider(String organization, KeyManagerConfigurationDTO kmConfig)
            throws APIManagementException {
        if (kmConfig != null) {
            if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                    kmConfig.getTokenType()) ||
                    StringUtils.equals(KeyManagerConfiguration.TokenType.BOTH.toString(),
                            kmConfig.getTokenType())) {
                try {
                    if (kmConfig.getExternalReferenceId() != null) {
                        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieving key manager reference IDP for tenant domain : " + tenantDomain);
                        }
                        IdentityProviderManager.getInstance().deleteIdPByResourceId(kmConfig.getExternalReferenceId(),
                                APIUtil.getInternalOrganizationDomain(organization));
                    }
                } catch (IdentityProviderManagementException e) {
                    throw new APIManagementException("IdP deletion failed. " + e.getMessage(), e,
                            ExceptionCodes.IDP_DELETION_FAILED);
                }
            }
        }
    }


    @Override
    public void deleteKeyManagerConfigurationById(String organization, KeyManagerConfigurationDTO kmConfig)
            throws APIManagementException {
        if (kmConfig != null) {
            AdminContentSearchResult apiUsage = getAPIUsagesByKeyManagerNameAndOrganization(organization,
                    kmConfig.getName(), 0, Integer.MAX_VALUE);
            KeyManagerApplicationUsages appUsages = getApplicationsOfKeyManager(kmConfig.getUuid(), 0,
                    Integer.MAX_VALUE);
            if (apiUsage != null && apiUsage.getApiCount() == 0 && appUsages != null
                    && appUsages.getApplicationCount() == 0) {
                if (!(APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(kmConfig.getName()) && (
                        APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(kmConfig.getType())
                                || APIConstants.KeyManager.WSO2_IS_KEY_MANAGER_TYPE.equals(kmConfig.getType())))) {
                    deleteIdentityProvider(organization, kmConfig);
                    apiMgtDAO.deleteKeyManagerConfigurationById(kmConfig.getUuid(), organization);
                    new KeyMgtNotificationSender()
                            .notify(kmConfig, APIConstants.KeyManager.KeyManagerEvent.ACTION_DELETE);
                } else {
                    throw new APIManagementException(APIConstants.KeyManager.DEFAULT_KEY_MANAGER + " couldn't delete",
                            ExceptionCodes.KEY_MANAGER_DELETE_FAILED);
                }
            } else {
                throw new APIManagementException("Key Manager is already used by an API or an Application.",
                        ExceptionCodes.KEY_MANAGER_DELETE_FAILED);
            }
        }
    }

    @Override
    public KeyManagerConfigurationDTO getKeyManagerConfigurationByName(String organization, String name)
            throws APIManagementException {

        KeyManagerConfigurationDTO keyManagerConfiguration =
                apiMgtDAO.getKeyManagerConfigurationByName(organization, name);
        if (keyManagerConfiguration != null) {
            if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerConfiguration.getName()) && (
                    APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerConfiguration.getType())
                            || APIConstants.KeyManager.WSO2_IS_KEY_MANAGER_TYPE.equals(
                            keyManagerConfiguration.getType()))) {
                APIUtil.getAndSetDefaultKeyManagerConfiguration(keyManagerConfiguration);
            }
            maskValues(keyManagerConfiguration);
            if (!StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                    keyManagerConfiguration.getTokenType())) {
                getKeyManagerEndpoints(keyManagerConfiguration);
            }
            return keyManagerConfiguration;
        }
        return null;
    }

    @Override
    public void addBotDetectionAlertSubscription(String email) throws APIManagementException {

        apiMgtDAO.addBotDetectionAlertSubscription(email);
    }

    @Override
    public List<BotDetectionData> getBotDetectionAlertSubscriptions() throws APIManagementException {

        return apiMgtDAO.getBotDetectionAlertSubscriptions();
    }

    @Override
    public void deleteBotDetectionAlertSubscription(String uuid) throws APIManagementException {

        apiMgtDAO.deleteBotDetectionAlertSubscription(uuid);
    }

    @Override
    public BotDetectionData getBotDetectionAlertSubscription(String field, String value) throws APIManagementException {

        return apiMgtDAO.getBotDetectionAlertSubscription(field, value);
    }

    @Override
    public List<BotDetectionData> retrieveBotDetectionData() throws APIManagementException {

        List<BotDetectionData> botDetectionDatalist = new ArrayList<>();
        String appName = AlertMgtConstants.APIM_ALERT_BOT_DETECTION_APP;
        String query = SQLConstants.BotDataConstants.GET_BOT_DETECTED_DATA;

        JSONObject botDataJsonObject = APIUtil.executeQueryOnStreamProcessor(appName, query);
        if (botDataJsonObject != null) {
            JSONArray botDataJsonArray = (JSONArray) botDataJsonObject.get("records");
            if (botDataJsonArray != null && botDataJsonArray.size() != 0) {
                for (Object botData : botDataJsonArray) {
                    JSONArray values = (JSONArray) botData;
                    BotDetectionData botDetectionData = new BotDetectionData();
                    botDetectionData.setCurrentTime((Long) values.get(0));
                    botDetectionData.setMessageID((String) values.get(1));
                    botDetectionData.setApiMethod((String) values.get(2));
                    botDetectionData.setHeaderSet((String) values.get(3));
                    botDetectionData.setMessageBody(extractBotDetectionDataContent((String) values.get(4)));
                    botDetectionData.setClientIp((String) values.get(5));
                    botDetectionDatalist.add(botDetectionData);
                }
            }
        }
        return botDetectionDatalist;
    }

    /**
     * Extract content of the bot detection data
     *
     * @param messageBody message body of bot detection data
     * @return extracted content
     */
    public String extractBotDetectionDataContent(String messageBody) {

        String content = "";
        try {
            //Parse the message body and extract the content in XML form
            DocumentBuilderFactory factory = APIUtil.getSecuredDocumentBuilder();

            factory.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE,
                    true);

            // Enable secure processing
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // Enable namespace awareness
            factory.setNamespaceAware(true);

            // Disable external entities to prevent XXE attacks
            factory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE,
                    false);
            factory.setFeature(Constants.SAX_FEATURE_PREFIX +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(messageBody)));
            Node bodyContentNode = document.getFirstChild().getFirstChild();

            //Convert XML form to String
            if (bodyContentNode != null) {
                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(new DOMSource(bodyContentNode), new StreamResult(writer));
                String output = writer.toString();
                content = output.substring(output.indexOf("?>") + 2); //remove <?xml version="1.0" encoding="UTF-8"?>
            }
        } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
            String errorMessage = "Error while extracting content from " + messageBody;
            log.error(errorMessage, e);
            content = messageBody;
        }
        return content;
    }

    public APICategory addCategory(APICategory category, String userName, String organization) throws APIManagementException {

        if (isCategoryNameExists(category.getName(), null, organization)) {
            APIUtil.handleException("Category with name '" + category.getName() + "' already exists");
        }
        return apiMgtDAO.addCategory(category, organization);
    }

    public void updateCategory(APICategory apiCategory) throws APIManagementException {

        apiMgtDAO.updateCategory(apiCategory);
    }

    public void deleteCategory(String categoryID, String username) throws APIManagementException {

        APICategory category = getAPICategoryByID(categoryID);
        int attchedAPICount = isCategoryAttached(category, username);
        if (attchedAPICount > 0) {
            APIUtil.handleException("Unable to delete the category. It is attached to API(s)");
        }
        apiMgtDAO.deleteCategory(categoryID);
    }

    public List<APICategory> getAllAPICategoriesOfOrganization(String organization) throws APIManagementException {
        return apiMgtDAO.getAllCategories(organization);
    }

    @Override
    public List<APICategory> getAPICategoriesOfOrganization(String organization) throws APIManagementException {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        List<APICategory> categories = getAllAPICategoriesOfOrganization(organization);
        if (categories.size() > 0) {
            for (APICategory category : categories) {
                int length = isCategoryAttached(category, username);
                category.setNumberOfAPIs(length);
            }
        }
        return categories;
    }

    public boolean isCategoryNameExists(String categoryName, String uuid, String organization) throws APIManagementException {

        return apiMgtDAO.isAPICategoryNameExists(categoryName, uuid, organization);
    }

    public APICategory getAPICategoryByID(String apiCategoryId) throws APIManagementException {

        APICategory apiCategory = apiMgtDAO.getAPICategoryByID(apiCategoryId);
        if (apiCategory != null) {
            return apiCategory;
        } else {
            String msg = "Failed to get APICategory. API category corresponding to UUID " + apiCategoryId
                    + " does not exist";
            throw new APIMgtResourceNotFoundException(msg);
        }
    }

    private int isCategoryAttached(APICategory category, String username) throws APIManagementException {

        APIProviderImpl apiProvider = new APIProviderImpl(username);
        //no need to add type prefix here since we need to ge the total number of category associations including both
        //APIs and API categories
        String searchQuery = APIConstants.CATEGORY_SEARCH_TYPE_PREFIX + ":*" + category.getName() + "*";
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        Map<String, Object> result = apiProvider.searchPaginatedAPIs(searchQuery, tenantDomain, 0, Integer.MAX_VALUE);
        return (int) (Integer) result.get("length");
    }

    /**
     * Adds a new label for the tenant
     *
     * @param label      label to add
     * @param tenantDomain tenant domain
     * @throws APIManagementException if failed add label
     */
    public Label addLabel(Label label, String tenantDomain) throws APIManagementException {

        if (!StringUtils.isEmpty(label.getName())) {
            if (label.getName().length() > 255) {
                throw new APIManagementException("Label name is too long.",
                        ExceptionCodes.from(ExceptionCodes.LABEL_ADDING_FAILED, "Label name is too long."));
            }
        } else {
            throw new APIManagementException("Label name is empty.",
                    ExceptionCodes.from(ExceptionCodes.LABEL_ADDING_FAILED, "Label name is empty."));
        }

        if (labelsDAO.isLabelNameExists(label.getName(), tenantDomain)) {
            throw new APIManagementException("Label with name '" + label.getName() + "' already exists",
                    ExceptionCodes.from(ExceptionCodes.LABEL_NAME_ALREADY_EXISTS, label.getName()));
        }

        label.setLabelId(UUID.randomUUID().toString());
        Label newLabel = labelsDAO.addLabel(label, tenantDomain);
        LabelEvent labelEvent = new LabelEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.LABEL_CREATE.name(), tenantDomain, newLabel.getLabelId(), newLabel.getName());
        APIUtil.sendNotification(labelEvent, APIConstants.NotifierType.LABEL.name());
        return newLabel;
    }

    /**
     * Updates a label
     *
     * @param labelID       label ID to update
     * @param updateLabelBody   label data to update
     * @param tenantDomain tenant domain
     * @throws APIManagementException if failed update label
     */
    public Label updateLabel(String labelID, Label updateLabelBody, String tenantDomain)
            throws APIManagementException {
        Label labelOriginal = labelsDAO.getLabelByIdAndTenantDomain(labelID, tenantDomain);
        if (labelOriginal == null) {
            throw new APIManagementException("Label not found for the given label ID: " + labelID,
                    ExceptionCodes.from(ExceptionCodes.LABEL_NOT_FOUND, labelID));
        }
        //Override labelID as it is not allowed to be updated
        updateLabelBody.setLabelId(labelOriginal.getLabelId());

        //We allow to update Label name given that the new label name is not taken yet
        String oldName = labelOriginal.getName();
        String updatedName = updateLabelBody.getName();
        if (!StringUtils.isEmpty(updatedName)) {
            if (updatedName.length() > 255) {
                throw new APIManagementException("Label name is too long.",
                        ExceptionCodes.from(ExceptionCodes.LABEL_UPDATE_FAILED, "Label name is too long."));
            }
        } else {
            throw new APIManagementException("Label name is empty.",
                    ExceptionCodes.from(ExceptionCodes.LABEL_UPDATE_FAILED, "Label name is empty."));
        }

        if (!oldName.equals(updatedName) && labelsDAO.isLabelNameExists(updatedName,
                labelID, tenantDomain)) {
            throw new APIManagementException("Label with name '" + updatedName + "' already exists",
                    ExceptionCodes.from(ExceptionCodes.LABEL_NAME_ALREADY_EXISTS, updatedName));
        }

        labelsDAO.updateLabel(updateLabelBody);
        LabelEvent labelEvent = new LabelEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.LABEL_UPDATE.name(), tenantDomain, labelID, updateLabelBody.getName());
        APIUtil.sendNotification(labelEvent, APIConstants.NotifierType.LABEL.name());
        return labelsDAO.getLabelByIdAndTenantDomain(labelID, tenantDomain);
    }

    /**
     * Delete a label
     *
     * @param labelID       label ID to delete
     * @param tenantDomain tenant domain
     * @throws APIManagementException if failed delete label
     */
    public void deleteLabel(String labelID, String tenantDomain) throws APIManagementException {

        Label labelOriginal = labelsDAO.getLabelByIdAndTenantDomain(labelID, tenantDomain);
        if (labelOriginal == null) {
            throw new APIManagementException("Label not found for the given label ID: " + labelID,
                    ExceptionCodes.from(ExceptionCodes.LABEL_NOT_FOUND, labelID));
        }
        if (labelsDAO.hasAPIsForLabel(labelID)) {
            throw new APIManagementException("Label is attached to APIs and cannot be deleted. Label ID: " + labelID,
                    ExceptionCodes.from(ExceptionCodes.LABEL_CANNOT_DELETE_ASSOCIATED));
        }
        labelsDAO.deleteLabel(labelID);
        LabelEvent labelEvent = new LabelEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.LABEL_DELETE.name(), tenantDomain, labelID, labelOriginal.getName());
        APIUtil.sendNotification(labelEvent, APIConstants.NotifierType.LABEL.name());
    }

    /**
     * Returns all labels of the tenant
     *
     * @param tenantDomain  tenant domain
     * @return List<Label> list of Label objects
     * @throws APIManagementException if failed to get labels
     */
    public List<Label> getAllLabelsOfTenant(String tenantDomain) throws APIManagementException {

        return labelsDAO.getAllLabels(tenantDomain);
    }

    /**
     * Get mapped APIs for the given label
     *
     * @param labelID label UUID
     * @param tenantDomain  tenant domain
     * @return List<ApiResult> list of ApiResult objects
     * @throws APIManagementException
     */
    public List<ApiResult> getMappedApisForLabel(String labelID, String tenantDomain) throws APIManagementException {

        Label labelOriginal = labelsDAO.getLabelByIdAndTenantDomain(labelID, tenantDomain);
        if (labelOriginal == null) {
            throw new APIManagementException("Label not found for the given label ID: " + labelID,
                    ExceptionCodes.from(ExceptionCodes.LABEL_NOT_FOUND, labelID));
        }
        return labelsDAO.getMappedApisForLabel(labelID);
    }

    private void sanitizeKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO) {
        if (keyManagerConfigurationDTO != null && keyManagerConfigurationDTO.getAdditionalProperties() != null) {
            for (Map.Entry<String, Object> entry : keyManagerConfigurationDTO.getAdditionalProperties().entrySet()) {
                if (entry.getValue() instanceof String) {
                    entry.setValue(((String) entry.getValue()).trim());
                }
            }
        }
    }

    protected void validateKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO,
                                                 KeyManagerConfigurationDTO oldKeyManagerConfiguration)
            throws APIManagementException {

        if (StringUtils.isEmpty(keyManagerConfigurationDTO.getName())) {
            throw new APIManagementException("Key Manager Name can't be empty", ExceptionCodes.KEY_MANAGER_NAME_EMPTY);
        }
        if (!(APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerConfigurationDTO.getName()) && (
                APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerConfigurationDTO.getType())
                        || APIConstants.KeyManager.WSO2_IS_KEY_MANAGER_TYPE.equals(
                        keyManagerConfigurationDTO.getType())))) {
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = ServiceReferenceHolder.getInstance()
                    .getKeyManagerConnectorConfiguration(keyManagerConfigurationDTO.getType());
            if (keyManagerConnectorConfiguration != null) {
                List<String> missingRequiredConfigurations = new ArrayList<>();
                for (ConfigurationDto configurationDto : keyManagerConnectorConfiguration
                        .getConnectionConfigurations()) {
                    if (configurationDto.isRequired()) {
                        if (!keyManagerConfigurationDTO.getAdditionalProperties()
                                .containsKey(configurationDto.getName())) {
                            if (StringUtils.isNotEmpty((String) configurationDto.getDefaultValue())) {
                                keyManagerConfigurationDTO.getAdditionalProperties().put(configurationDto.getName(),
                                        configurationDto.getDefaultValue());
                            }
                            missingRequiredConfigurations.add(configurationDto.getName());
                        }
                    }

                    // Check if invoked by update flow and if the configuration is disabled for update
                    boolean hasExistingConfig = oldKeyManagerConfiguration != null;
                    boolean isUpdateDisabled = configurationDto.isUpdateDisabled();

                    if (hasExistingConfig && isUpdateDisabled) {
                        String configName = configurationDto.getName();
                        Object newValue = keyManagerConfigurationDTO.getAdditionalProperties().get(configName);
                        Object defaultValue = configurationDto.getDefaultValue();
                        boolean oldConfigContainsKey = oldKeyManagerConfiguration.getAdditionalProperties()
                                .containsKey(configName);
                        Object oldValue = oldKeyManagerConfiguration.getAdditionalProperties().get(configName);

                        if (newValue == null && oldConfigContainsKey) {
                            newValue = oldValue;
                        } else if (newValue == null) {
                            newValue = defaultValue;
                        }
                        keyManagerConfigurationDTO.getAdditionalProperties().put(configName, newValue);

                        boolean valueChangedFromOld = oldConfigContainsKey && !Objects.equals(newValue, oldValue);
                        boolean valueChangedFromDefault = !Objects.equals(newValue, defaultValue);
                        boolean valueChanged = valueChangedFromOld
                                || (!oldConfigContainsKey && valueChangedFromDefault);
                        if (valueChanged) {
                            throw new APIManagementException(
                                    "Modification of the Key Manager configuration " + configurationDto.getName() +
                                            " is not permitted",
                                    ExceptionCodes.KEY_MANAGER_UPDATE_VIOLATION);
                        }
                    }
                }
                if (keyManagerConnectorConfiguration.getAuthConfigurations() != null
                        && !keyManagerConnectorConfiguration.getAuthConfigurations().isEmpty()) {
                    missingRequiredConfigurations.addAll(keyManagerConnectorConfiguration.validateAuthConfigurations(
                            keyManagerConfigurationDTO.getAdditionalProperties()));
                }
                if (!missingRequiredConfigurations.isEmpty()) {
                    throw new APIManagementException("Key Manager Configuration value for " + String.join(",",
                            missingRequiredConfigurations) + " is/are required",
                            ExceptionCodes.REQUIRED_KEY_MANAGER_CONFIGURATION_MISSING);
                }
                if (!keyManagerConfigurationDTO.getAdditionalProperties()
                        .containsKey(APIConstants.KeyManager.CONSUMER_KEY_CLAIM)) {
                    if (StringUtils.isNotEmpty(keyManagerConnectorConfiguration.getDefaultConsumerKeyClaim())) {
                        keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.CONSUMER_KEY_CLAIM,
                                keyManagerConnectorConfiguration.getDefaultConsumerKeyClaim());
                    }
                }
                if (!keyManagerConfigurationDTO.getAdditionalProperties()
                        .containsKey(APIConstants.KeyManager.SCOPES_CLAIM)) {
                    if (StringUtils.isNotEmpty(keyManagerConnectorConfiguration.getDefaultScopesClaim())) {
                        keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.SCOPES_CLAIM,
                                keyManagerConnectorConfiguration.getDefaultScopesClaim());
                    }
                }
            } else {
                throw new APIManagementException(
                        "Key Manager Type " + keyManagerConfigurationDTO.getType() + " is invalid.",
                        ExceptionCodes.INVALID_KEY_MANAGER_TYPE);
            }
        }
    }

    private Object encryptValues(Object value) throws APIManagementException {

        try {
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            if (value instanceof String) {
                String encryptedValue = new String(cryptoUtil.encrypt(((String) value).getBytes()));
                return getEncryptedValue(encryptedValue);
            } else if (value instanceof List) {
                List valueList = (List) value;
                List encrpytedList = new ArrayList<>();
                for (Object s : valueList) {
                    encrpytedList.add(encryptValues(s));
                }
                return encrpytedList;
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object entryValue = entry.getValue();
                    map.replace(key, encryptValues(entryValue));
                }
                return map;
            }
        } catch (CryptoException e) {
            throw new APIManagementException("Error while encrypting values", e);
        }
        return null;
    }

    private String getEncryptedValue(String value) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(APIConstants.ENCRYPTED_VALUE, true);
        jsonObject.put(APIConstants.VALUE, value);
        return jsonObject.toJSONString();
    }

    private void maskValues(KeyManagerConfigurationDTO keyManagerConfigurationDTO) {
        KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = ServiceReferenceHolder.getInstance()
                .getKeyManagerConnectorConfiguration(keyManagerConfigurationDTO.getType());
        // When the KM is used for Token Exchange,there won't be any connection configurations to mask.
        if (keyManagerConnectorConfiguration != null) {
            Map<String, Object> additionalProperties = keyManagerConfigurationDTO.getAdditionalProperties();
            List<ConfigurationDto> connectionConfigurations =
                    keyManagerConnectorConfiguration.getConnectionConfigurations();
            if (connectionConfigurations != null && !connectionConfigurations.isEmpty()) {
                for (ConfigurationDto connectionConfiguration : connectionConfigurations) {
                    if (connectionConfiguration.isMask()) {
                        additionalProperties.replace(connectionConfiguration.getName(),
                                APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD);
                    }
                }
            }

            // if authConfiguration array is not empty, check for maskable values there as well
            if (keyManagerConnectorConfiguration.getAuthConfigurations() != null
                    && !(keyManagerConnectorConfiguration.getAuthConfigurations().isEmpty())) {
                List<ConfigurationDto> authConfigurations = keyManagerConnectorConfiguration.getAuthConfigurations();
                if (authConfigurations != null && !authConfigurations.isEmpty()) {
                    // Recursively check nested objects in authConfigurations and apply masking
                    for (ConfigurationDto authConfiguration : authConfigurations) {
                        applyMaskToNestedFields(authConfiguration.getValues(), additionalProperties);
                    }
                }
            }
        }
    }

    private void applyMaskToNestedFields(List<Object> configurations, Map<String, Object> additionalProperties) {
        if (configurations == null || configurations.isEmpty()) {
            return;
        }
        for (Object configuration : configurations) {
            if (((ConfigurationDto)configuration).isMask()) {
                additionalProperties.replace(((ConfigurationDto) configuration).getName(),
                        APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD);
            }
            // Recursively process nested values
            applyMaskToNestedFields(((ConfigurationDto)configuration).getValues(), additionalProperties);
        }
    }

    /**
     * Applies masking to nested gateway configuration fields.
     * For each masked configuration, the actual value in {@code additionalProperties}
     * is replaced with the default masked password. The method is applied recursively
     * to handle nested configurations.
     *
     * @param connectorConfigurations The list of gateway configurations to process.
     * @param additionalProperties The map of configuration name-value pairs to update.
     */
    private void applyMaskToNestedGatewayFields(List<Object> connectorConfigurations,
            Map<String, String> additionalProperties) {
        if (connectorConfigurations == null || connectorConfigurations.isEmpty()) {
            return;
        }
        for (Object connectorConfiguration : connectorConfigurations) {
            if (connectorConfiguration instanceof ConfigurationDto) {
                ConfigurationDto connectorConfigurationDto = (ConfigurationDto) connectorConfiguration;
                if (connectorConfigurationDto.isMask()) {
                    additionalProperties.replace(connectorConfigurationDto.getName(),
                            APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD);
                }
                applyMaskToNestedGatewayFields(connectorConfigurationDto.getValues(), additionalProperties);
            }
        }
    }

    private void maskValues(Environment environment) {
        GatewayAgentConfiguration gatewayConfiguration = ServiceReferenceHolder.getInstance()
                .getExternalGatewayConnectorConfiguration(environment.getGatewayType());
        if (gatewayConfiguration != null) {
            Map<String, String> additionalProperties = environment.getAdditionalProperties();
            List<ConfigurationDto> connectionConfigurations = gatewayConfiguration.getConnectionConfigurations();
            if (connectionConfigurations != null && !connectionConfigurations.isEmpty()) {
                for (ConfigurationDto connectionConfiguration : connectionConfigurations) {
                    if (connectionConfiguration.isMask()) {
                        additionalProperties.replace(connectionConfiguration.getName(),
                                APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD);
                    }
                    applyMaskToNestedGatewayFields(connectionConfiguration.getValues(), additionalProperties);
                }
            }
        }
    }

    /**
     * The method converts the date into timestamp
     *
     * @param workflowType workflow Type of workflow pending request
     * @param status       Workflow status of workflow pending request
     * @param tenantDomain tenant domain of user
     * @return Workflow[]  list of workflow pending requests
     * @throws APIManagementException
     */
    public Workflow[] getworkflows(String workflowType, String status, String tenantDomain)
            throws APIManagementException {
        WorkflowProperties workflowConfig = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
        if (workflowConfig.isListTasks()) {
            Workflow[] workflows = apiMgtDAO.getworkflows(workflowType, status, tenantDomain);
            WorkflowTaskService taskService = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.
            getInstance().getWorkflowTaskService();
            return taskService.getFilteredPendingTasks(workflows,
                    CarbonContext.getThreadLocalCarbonContext().getUsername(), tenantDomain);
        } else {
            return new Workflow[0];
        }
    }

    /**
     * The method converts the date into timestamp
     *
     * @param externelWorkflowRef External Workflow Reference of workflow pending request
     * @param status              Workflow status of workflow pending request
     * @param tenantDomain        tenant domain of user
     * @return Workflow pending request
     * @throws APIManagementException
     */
    public Workflow getworkflowReferenceByExternalWorkflowReferenceID(String externelWorkflowRef, String status,
                                                                      String tenantDomain) throws APIManagementException {
        Workflow workflow = null;
        WorkflowProperties workflowConfig = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
        if (workflowConfig.isListTasks()) {
            workflow = apiMgtDAO.getworkflowReferenceByExternalWorkflowReferenceID(externelWorkflowRef,
                    status, tenantDomain);
        }

        return workflow;
    }

    /**
     * This method used to check the existence of the scope name for the particular user
     *
     * @param username  username to be validated
     * @param scopeName scope to be validated
     * @throws APIManagementException
     */
    public boolean isScopeExistsForUser(String username, String scopeName) throws APIManagementException {
        if (APIUtil.isUserExist(username)) {
            Map<String, String> scopeRoleMapping =
                    APIUtil.getRESTAPIScopesForTenant(MultitenantUtils.getTenantDomain(username));
            if (scopeRoleMapping.containsKey(scopeName)) {
                String[] userRoles = APIUtil.getListOfRoles(username);
                return getRoleScopeList(userRoles, scopeRoleMapping).contains(scopeName);
            } else {
                throw new APIManagementException("Scope Not Found.  Scope : " + scopeName + ",",
                        ExceptionCodes.SCOPE_NOT_FOUND);
            }
        } else {
            throw new APIManagementException("User Not Found. Username :" + username + ",",
                    ExceptionCodes.USER_NOT_FOUND);
        }
    }

    /**
     * This method used to check the existence of the scope name
     *
     * @param username  tenant username to get tenant-scope mapping
     * @param scopeName scope to be validated
     * @throws APIManagementException
     */
    public boolean isScopeExists(String username, String scopeName) {
        Map<String, String> scopeRoleMapping = APIUtil.getRESTAPIScopesForTenant(MultitenantUtils
                .getTenantDomain(username));
        return scopeRoleMapping.containsKey(scopeName);
    }

    /**
     * This method used to get the list of scopes of a user roles
     *
     * @param userRoles        roles of a particular user
     * @param scopeRoleMapping scope-role mapping
     * @return scopeList            scope lost of a particular user
     * @throws APIManagementException
     */
    private List<String> getRoleScopeList(String[] userRoles, Map<String, String> scopeRoleMapping) {
        List<String> userRoleList;
        List<String> authorizedScopes = new ArrayList<>();

        if (userRoles == null || userRoles.length == 0) {
            userRoles = new String[0];
        }

        userRoleList = Arrays.asList(userRoles);
        Iterator<Map.Entry<String, String>> iterator = scopeRoleMapping.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            for (String aRole : entry.getValue().split(",")) {
                if (userRoleList.contains(aRole.trim())) {
                    authorizedScopes.add(entry.getKey());
                }
            }
        }
        return authorizedScopes;
    }

    @Override
    public void addTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        apiMgtDAO.addTenantTheme(tenantId, themeContent);
    }

    @Override
    public void updateTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        apiMgtDAO.updateTenantTheme(tenantId, themeContent);
    }

    @Override
    public InputStream getTenantTheme(int tenantId) throws APIManagementException {

        return apiMgtDAO.getTenantTheme(tenantId);
    }

    @Override
    public boolean isTenantThemeExist(int tenantId) throws APIManagementException {

        return apiMgtDAO.isTenantThemeExist(tenantId);
    }

    @Override
    public void deleteTenantTheme(int tenantId) throws APIManagementException {

        apiMgtDAO.deleteTenantTheme(tenantId);
    }

    @Override
    public String getTenantConfig(String organization) throws APIManagementException {
        return ServiceReferenceHolder.getInstance().getApimConfigService().getTenantConfig(organization);
    }

    @Override
    public void importDraftedOrgTheme(String organization, InputStream themeContent) throws APIManagementException {
        apiMgtDAO.importDraftedOrgTheme(organization, themeContent);
    }

    @Override
    public void updateOrgThemeStatus(String organization, String action) throws APIManagementException {
        apiMgtDAO.updateOrgThemeStatus(organization, action);
    }

    @Override
    public void deleteOrgTheme(String organization, String themeId) throws APIManagementException {
        apiMgtDAO.deleteOrgTheme(organization, themeId);
    }

    @Override
    public InputStream getOrgTheme(String uuid, String organization) throws APIManagementException {
        return apiMgtDAO.getOrgTheme(uuid, organization);
    }

    @Override
    public Map<String, String> getOrgThemes(String organization) throws APIManagementException {
        return apiMgtDAO.getOrgThemes(organization);
    }

    @Override
    public void updateTenantConfig(String organization, String config) throws APIManagementException {

        Schema schema = APIUtil.retrieveTenantConfigJsonSchema();
        if (schema != null) {
            try {
                org.json.JSONObject uploadedConfig = new org.json.JSONObject(config);
                schema.validate(uploadedConfig);
                APIUtil.validateRestAPIScopes(config);
                ServiceReferenceHolder.getInstance().getApimConfigService().updateTenantConfig(organization, config);
            } catch (ValidationException | JSONException e) {
                throw new APIManagementException("tenant-config validation failure",
                        ExceptionCodes.from(ExceptionCodes.INVALID_TENANT_CONFIG, e.getMessage()));
            }
        } else {
            throw new APIManagementException("tenant-config validation failure", ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public void updateApiProvider(String apiId, String provider, String organisation) throws APIManagementException {
        APIPersistence apiPersistenceInstance = PersistenceFactory.getAPIPersistenceInstance();
        try {
            ApiMgtDAO.getInstance().updateApiProvider(apiId, provider);
            apiPersistenceInstance.changeApiProvider(provider, apiId, organisation);
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while changing the API provider", e);
        }
    }

    /**
     * get/search paginated APIs in admin portal
     *
     * @param searchQuery API name search query
     * @param organization organization
     * @param start start index of the pagination
     * @param end end index of the pagination
     * @return APIs result object
     * @throws APIManagementException if an error occurs when searching/getting the APIs
     */
    public Map<String, Object> searchPaginatedApis(String searchQuery, String organization, int start, int end)
            throws APIManagementException {
        ArrayList<Object> compoundResult = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        SortedSet<API> apiSet = new TreeSet<>(new APINameComparator());
        String modifiedSearchQuery = buildSearchQuery(searchQuery);
        try {
            APIPersistence apiPersistenceInstance = PersistenceFactory.getAPIPersistenceInstance();
            AdminContentSearchResult results = apiPersistenceInstance.searchContentForAdmin(organization,
                    modifiedSearchQuery, start, end, end);
            if (results != null) {
                List<SearchContent> resultList = results.getApis();
                for (SearchContent item : resultList) {
                    if (APIConstants.API_IDENTIFIER_TYPE.equals(item.getType())) {
                        AdminApiSearchContent adminSearchApi = (AdminApiSearchContent) item;
                        API api = new API(new APIIdentifier(adminSearchApi.getProvider(), adminSearchApi.getName(),
                                adminSearchApi.getVersion()));
                        api.setUuid(adminSearchApi.getId());
                        apiSet.add(api);
                    }
                }
                compoundResult.addAll(apiSet);
                compoundResult.sort(new ContentSearchResultNameComparator());
                result.put(APIConstants.API_DATA_LENGTH, compoundResult.size());
                result.put(APIConstants.ADMIN_API_LIST_RESPONSE_PARAMS_TOTAL, results.getApiTotal());
            } else {
                result.put(APIConstants.API_DATA_LENGTH, compoundResult.size());
            }
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while searching apis ",
                    ExceptionCodes.GET_SEARCH_APIS_IN_ADMIN_FAILED);
        }
        result.put(APIConstants.API_DATA_APIS, compoundResult);
        return result;
    }

    /**
     * If the user provided a search query then it will use that, otherwise it will use the asterix(*) symbol.
     *
     * @param searchQuery searchQuery that the user provided
     * @return modified searchQuery
     */
    private String buildSearchQuery(String searchQuery) {
        if (searchQuery.equals(APIConstants.CHAR_ASTERIX)) {
            return searchQuery;
        } else {
            return APIConstants.CHAR_ASTERIX + searchQuery + APIConstants.CHAR_ASTERIX;
        }
    }

    @Override
    public String getTenantConfigSchema(String organization) {
        return APIUtil.retrieveTenantConfigJsonSchema().toString();
    }

    @Override
    public Policy[] getPolicies(int tenantId, String level) throws APIManagementException {

        Policy[] policies = null;

        if (PolicyConstants.POLICY_LEVEL_API.equals(level)) {
            policies = apiMgtDAO.getAPIPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equals(level)) {
            policies = apiMgtDAO.getApplicationPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(level)) {
            policies = apiMgtDAO.getSubscriptionPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(level)) {
            policies = apiMgtDAO.getGlobalPolicies(tenantId);
        }

        //Get the API Manager configurations and check whether the unlimited tier is disabled. If disabled, remove
        // the tier from the array.
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        ThrottleProperties throttleProperties = apiManagerConfiguration.getThrottleProperties();
        List<Policy> policiesWithoutUnlimitedTier = new ArrayList<Policy>();

        if (policies != null) {
            for (Policy policy : policies) {
                if (APIConstants.UNLIMITED_TIER.equals(policy.getPolicyName())) {
                    if (throttleProperties.isEnableUnlimitedTier()) {
                        policiesWithoutUnlimitedTier.add(policy);
                    }
                } else {
                    policiesWithoutUnlimitedTier.add(policy);
                }
            }
        }
        policies = policiesWithoutUnlimitedTier.toArray(new Policy[0]);
        return policies;
    }

    /**
     * Get Policy with corresponding name and type.
     *
     * @param tenantId tenantId
     * @param level    policy type
     * @param name     policy name
     * @return Policy with corresponding name and type
     * @throws APIManagementException
     */
    @Override public Policy getPolicyByNameAndType(int tenantId, String level, String name)
            throws APIManagementException {

        Policy policy = null;

        if (PolicyConstants.POLICY_LEVEL_API.equals(level)) {
            policy = apiMgtDAO.getAPIPolicy(name, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equals(level)) {
            policy = apiMgtDAO.getApplicationPolicy(name, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(level)) {
            policy = apiMgtDAO.getSubscriptionPolicy(name, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(level)) {
            policy = apiMgtDAO.getGlobalPolicy(name);
        }

        //Get the API Manager configurations and check whether the unlimited tier is disabled. If disabled, remove
        // the tier from the array.
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        ThrottleProperties throttleProperties = apiManagerConfiguration.getThrottleProperties();

        if (policy != null && APIConstants.UNLIMITED_TIER.equals(policy.getPolicyName())
                && !throttleProperties.isEnableUnlimitedTier()) {
            return null;
        }

        return policy;

    }

    private IdentityProvider createIdp(KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        IdentityProvider identityProvider = new IdentityProvider();
        String idpName = sanitizeName(
                getSubstringOfTen(keyManagerConfigurationDTO.getName()) + "_" + keyManagerConfigurationDTO.getOrganization() + "_"
                        + keyManagerConfigurationDTO.getUuid());
        identityProvider.setIdentityProviderName(idpName);
        identityProvider.setDisplayName(keyManagerConfigurationDTO.getDisplayName());
        identityProvider.setPrimary(Boolean.FALSE);
        identityProvider.setIdentityProviderDescription(keyManagerConfigurationDTO.getDescription());
        identityProvider.setAlias(keyManagerConfigurationDTO.getAlias());
        String certificate = null;
        if (keyManagerConfigurationDTO.getAdditionalProperties().containsKey(APIConstants.KeyManager.CERTIFICATE_VALUE)){
            certificate =
                    (String) keyManagerConfigurationDTO.getAdditionalProperties().get(APIConstants.KeyManager.CERTIFICATE_VALUE);

        }
        String certificateType = null;
        if (keyManagerConfigurationDTO.getAdditionalProperties().containsKey(APIConstants.KeyManager.CERTIFICATE_TYPE)) {
            certificateType =
                    (String) keyManagerConfigurationDTO.getAdditionalProperties().get(APIConstants.KeyManager.CERTIFICATE_TYPE);
        }
        List<IdentityProviderProperty> idpProperties = new ArrayList<>();

        if (StringUtils.isNotEmpty(certificate) && StringUtils.isNotEmpty(certificateType)) {
            if (APIConstants.KeyManager.CERTIFICATE_TYPE_JWKS_ENDPOINT.equals(certificateType)) {
                if (StringUtils.isNotBlank(certificate)) {
                    IdentityProviderProperty jwksProperty = new IdentityProviderProperty();
                    jwksProperty.setName(APIConstants.JWKS_URI);
                    jwksProperty.setValue(certificate);
                    idpProperties.add(jwksProperty);
                }
            } else if (APIConstants.KeyManager.CERTIFICATE_TYPE_PEM_FILE.equals(certificateType)) {
                identityProvider.setCertificate(String.join(certificate, ""));
            }
        }

        if (keyManagerConfigurationDTO.getProperty(APIConstants.KeyManager.ISSUER) != null) {
            IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
            identityProviderProperty.setName(IdentityApplicationConstants.IDP_ISSUER_NAME);
            identityProviderProperty.setValue((String) keyManagerConfigurationDTO.getProperty(APIConstants.KeyManager.ISSUER));
            idpProperties.add(identityProviderProperty);
        }

        if (idpProperties.size() > 0) {
            identityProvider.setIdpProperties(idpProperties.toArray(new IdentityProviderProperty[0]));
        }

        identityProvider.setEnable(keyManagerConfigurationDTO.isEnabled());
        Object claims = keyManagerConfigurationDTO.getProperty(APIConstants.KeyManager.CLAIM_MAPPING);
        updateClaims(identityProvider, claims);
        return identityProvider;
    }
    private void updateClaims(IdentityProvider idp, Object claims) {
        if (claims != null) {
            ClaimConfig claimConfig = new ClaimConfig();
            List<ClaimMapping> claimMappings = new ArrayList<>();
            List<org.wso2.carbon.identity.application.common.model.Claim> idpClaims = new ArrayList<>();
            JsonArray claimArray = (JsonArray) claims;
            claimConfig.setLocalClaimDialect(false);

            for (JsonElement claimMappingEntry : claimArray) {
                if (claimMappingEntry instanceof JsonObject){
                    JsonElement idpClaimUri = ((JsonObject) claimMappingEntry).get("remoteClaim");
                    JsonElement localClaimUri = ((JsonObject) claimMappingEntry).get("localClaim");

                    ClaimMapping internalMapping = new ClaimMapping();
                    org.wso2.carbon.identity.application.common.model.Claim remoteClaim =
                            new org.wso2.carbon.identity.application.common.model.Claim();
                    remoteClaim.setClaimUri(idpClaimUri.getAsString());

                    org.wso2.carbon.identity.application.common.model.Claim localClaim =
                            new org.wso2.carbon.identity.application.common.model.Claim();
                    localClaim.setClaimUri(localClaimUri.getAsString());

                    internalMapping.setRemoteClaim(remoteClaim);
                    internalMapping.setLocalClaim(localClaim);
                    claimMappings.add(internalMapping);
                    idpClaims.add(remoteClaim);
                }
            }

            claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[0]));
            claimConfig.setIdpClaims(idpClaims.toArray(new org.wso2.carbon.identity.application.common.model.Claim[0]));
            idp.setClaimConfig(claimConfig);
        }
    }
    private String sanitizeName(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    private String getSubstringOfTen(String inputString) {
        return inputString.length() < 10 ? inputString : inputString.substring(0, 10);
    }
    private void mergeIdpWithKeyManagerConfiguration(IdentityProvider identityProvider, KeyManagerConfigurationDTO keyManagerDTO) {
        keyManagerDTO.setDisplayName(identityProvider.getDisplayName());
        keyManagerDTO.setDescription(identityProvider.getIdentityProviderDescription());

        IdentityProviderProperty identityProviderProperties[] = identityProvider.getIdpProperties();
        if (identityProviderProperties.length > 0) {
            for (IdentityProviderProperty identityProviderProperty :identityProviderProperties) {
                if (StringUtils.equals(identityProviderProperty.getName(), APIConstants.JWKS_URI)) {
                    keyManagerDTO.addProperty(APIConstants.KeyManager.CERTIFICATE_TYPE,APIConstants.KeyManager.CERTIFICATE_TYPE_JWKS_ENDPOINT);
                    keyManagerDTO.addProperty(APIConstants.KeyManager.CERTIFICATE_VALUE,identityProviderProperty.getValue());
                }
                if (StringUtils.equals(identityProviderProperty.getName(),
                        IdentityApplicationConstants.IDP_ISSUER_NAME)) {
                    keyManagerDTO.addProperty(APIConstants.KeyManager.ISSUER,identityProviderProperty.getValue());
                }
            }

        } else if (StringUtils.isNotBlank(identityProvider.getCertificate())) {
            keyManagerDTO.addProperty(APIConstants.KeyManager.CERTIFICATE_TYPE,
                    APIConstants.KeyManager.CERTIFICATE_TYPE_PEM_FILE);
            keyManagerDTO.addProperty(APIConstants.KeyManager.CERTIFICATE_VALUE,
                    identityProvider.getCertificate());
        }

        keyManagerDTO.setEnabled(identityProvider.isEnable());
        keyManagerDTO.setAlias(identityProvider.getAlias());

        ClaimConfig claimConfig = identityProvider.getClaimConfig();
        JsonArray claimArray = new JsonArray();
        for (ClaimMapping claimMapping: claimConfig.getClaimMappings()) {
            JsonObject claimMappingEntryDTO = new JsonObject();
            claimMappingEntryDTO.addProperty("localClaim", claimMapping.getLocalClaim().getClaimUri());
            claimMappingEntryDTO.addProperty("remoteClaim", claimMapping.getRemoteClaim().getClaimUri());
            claimArray.add(claimMappingEntryDTO);
        }
        keyManagerDTO.addProperty(APIConstants.KeyManager.CLAIM_MAPPING, claimArray);
    }
    private void getKeyManagerEndpoints(KeyManagerConfigurationDTO keyManagerConfigurationDTO){

        Map<String, String> endpointConfigurationsMap = new HashMap<>();
        keyManagerConfigurationDTO.setEndpoints(endpointConfigurationsMap);
        if (!APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerConfigurationDTO.getType())) {
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = ServiceReferenceHolder.getInstance()
                    .getKeyManagerConnectorConfiguration(keyManagerConfigurationDTO.getType());
            List<ConfigurationDto> endpointConfigurations =
                    keyManagerConnectorConfiguration.getEndpointConfigurations();
            if (endpointConfigurations != null) {
                for (ConfigurationDto endpointConfiguration : endpointConfigurations) {
                    Object endpointValue = keyManagerConfigurationDTO.getProperty(endpointConfiguration.getName());
                    if (endpointValue instanceof String && StringUtils.isNotEmpty((String) endpointValue)) {
                        endpointConfigurationsMap.put(endpointConfiguration.getName(), (String) endpointValue);
                    }
                }
            }
        }
    }
    /**
     * Create a deep copy of the input identity Provider.
     *
     * @param identityProvider identity Provider.
     * @return Clone of identityProvider.
     */
    private static IdentityProvider cloneIdentityProvider(IdentityProvider identityProvider) {

        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(identityProvider), IdentityProvider.class);
    }

    @Override
    public List<KeyManagerConfigurationDTO> getGlobalKeyManagerConfigurations() throws APIManagementException {
        List<KeyManagerConfigurationDTO> keyManagerConfigurations = apiMgtDAO.getKeyManagerConfigurationsByOrganization(
                APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN);
        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurations) {
            decryptKeyManagerConfigurationValues(keyManagerConfigurationDTO);
        }
        return keyManagerConfigurations;
    }

    public List<KeyManagerConfigurationDTO> getGlobalKeyManagerConfigurations(String organization)
            throws APIManagementException {
        List<KeyManagerConfigurationDTO> keyManagerConfigurations = apiMgtDAO.getKeyManagerConfigurationsByOrganization(
                APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN);
        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurations) {
            decryptKeyManagerConfigurationValues(keyManagerConfigurationDTO);
        }
        setKeyManagerUsageRelatedInformation(keyManagerConfigurations, organization);
        return keyManagerConfigurations;
    }

    @Override
    public List<OrganizationDetailsDTO> getOrganizations(String parentOrgId, String tenantDomain) throws APIManagementException {
        List<OrganizationDetailsDTO> organizationsList = apiMgtDAO.getChildOrganizations(parentOrgId, tenantDomain);
        return organizationsList;
    }

    @Override
    public OrganizationDetailsDTO addOrganization(OrganizationDetailsDTO orgDto, String parentOrgId,
            String tenantDomain) throws APIManagementException {
       
        //If there is an organization entry already available for external reference, update it
        OrganizationDetailsDTO savedOrgInfo = apiMgtDAO.getOrganizationDetalsByExternalOrgId(
                orgDto.getExternalOrganizationReference(), tenantDomain);
        if (savedOrgInfo != null) {
            orgDto.setOrganizationHandle(APIUtil.getOrganizationHandle(orgDto.getName()));
            orgDto.setOrganizationId(savedOrgInfo.getOrganizationId());
            apiMgtDAO.updateOrganizationDetails(orgDto, parentOrgId);
        } else {
            orgDto.setOrganizationHandle(APIUtil.getOrganizationHandle(orgDto.getName()));
            savedOrgInfo = apiMgtDAO.addOrganization(orgDto, parentOrgId, tenantDomain);
            orgDto.setOrganizationId(savedOrgInfo.getOrganizationId());
        }

        return orgDto;
    }

    @Override
    public OrganizationDetailsDTO getOrganizationDetails(String organizationId, String tenantDomain)
            throws APIManagementException {
        return apiMgtDAO.getOrganizationDetails(organizationId, tenantDomain);
    }

    @Override
    public OrganizationDetailsDTO updateOrganization(OrganizationDetailsDTO organizationInfoDTO, String parentOrgId,
            String tenantDomain) throws APIManagementException {
        organizationInfoDTO.setOrganizationHandle(APIUtil.getOrganizationHandle(organizationInfoDTO.getName()));
        apiMgtDAO.updateOrganizationDetails(organizationInfoDTO, parentOrgId);
        return apiMgtDAO.getOrganizationDetails(organizationInfoDTO.getOrganizationId(),
                tenantDomain);
    }

    @Override
    public void deleteOrganization(String organizationId, String tenantDomain) throws APIManagementException {
        apiMgtDAO.deleteOrganizationDetails(organizationId, tenantDomain);
    }
}
