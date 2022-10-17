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
package org.wso2.apk.apimgt.impl;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wso2.apk.apimgt.api.*;
import org.wso2.apk.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.apk.apimgt.api.model.*;
import org.wso2.apk.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.apk.apimgt.api.model.policy.*;
import org.wso2.apk.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.dao.impl.*;
import org.wso2.apk.apimgt.impl.dto.ThrottleProperties;
import org.wso2.apk.apimgt.impl.dto.WorkflowProperties;
import org.wso2.apk.apimgt.impl.monetization.DefaultMonetizationImpl;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This class provides the core API admin functionality.
 */
public class APIAdminImpl implements APIAdmin {

    private static final Log log = LogFactory.getLog(APIAdminImpl.class);
    protected EnvironmentDAOImpl environmentDAOImpl;
    protected ApplicationDAOImpl applicationDAOImpl;
    protected AdminDAOImpl adminDAOImpl;
    protected KeyManagerDAOImpl keyManagerDAOImpl;
    protected WorkflowDAOImpl workflowDAOImpl;
    protected PolicyDAOImpl policyDAOImpl;
    protected BlockConditionDAOImpl blockConditionDAOImpl;

    public APIAdminImpl() {
        environmentDAOImpl = EnvironmentDAOImpl.getInstance();
        applicationDAOImpl = ApplicationDAOImpl.getInstance();
        adminDAOImpl = AdminDAOImpl.getInstance();
        keyManagerDAOImpl = KeyManagerDAOImpl.getInstance();
        workflowDAOImpl = WorkflowDAOImpl.getInstance();
        policyDAOImpl = PolicyDAOImpl.getInstance();
        blockConditionDAOImpl = BlockConditionDAOImpl.getInstance();
    }

    @Override
    public List<Environment> getAllEnvironments(String tenantDomain) throws APIManagementException {
        List<Environment> dynamicEnvs = environmentDAOImpl.getAllEnvironments(tenantDomain);
        // gateway environment name should be unique, ignore environments defined in api-manager.xml with the same name
        // if a dynamic (saved in database) environment exists.
        List<String> dynamicEnvNames = dynamicEnvs.stream().map(Environment::getName).collect(Collectors.toList());
        List<Environment> allEnvs = new ArrayList<>(dynamicEnvs.size() + APIUtil.getReadOnlyEnvironments().size());
        // add read only environments first and dynamic environments later
        APIUtil.getReadOnlyEnvironments().values().stream().filter(env -> !dynamicEnvNames.contains(env.getName())).forEach(allEnvs::add);
        allEnvs.addAll(dynamicEnvs);
        return allEnvs;
    }

    @Override
    public Environment getEnvironment(String tenantDomain, String uuid) throws APIManagementException {
        // priority for configured environments over dynamic environments
        // name is the UUID of environments configured in api-manager.xml
        Environment env = APIUtil.getReadOnlyEnvironments().get(uuid);
        if (env == null) {
            env = environmentDAOImpl.getEnvironment(tenantDomain, uuid);
            if (env == null) {
                String errorMessage = String.format("Failed to retrieve Environment with UUID %s. Environment not found",
                        uuid);
                throw new APIMgtResourceNotFoundException(errorMessage, ExceptionCodes.from(
                        ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND, String.format("UUID '%s'", uuid))
                );
            }
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
        return environmentDAOImpl.addEnvironment(tenantDomain, environment);
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
        environmentDAOImpl.deleteEnvironment(uuid);
    }

    @Override
    public Environment updateEnvironment(String tenantDomain, Environment environment) throws APIManagementException {
        // check if the VHost exists in the tenant domain with given UUID, throw error if not found
        Environment existingEnv = getEnvironment(tenantDomain, environment.getUuid());
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
        return environmentDAOImpl.updateEnvironment(environment);
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

        return applicationDAOImpl.getAllApplicationsOfTenantForMigration(appTenantDomain);
    }

    /**
     * @inheritDoc
     **/
    public Application[] getApplicationsWithPagination(String user, String owner, int tenantId, int limit,
                                                       int offset, String applicationName, String sortBy,
                                                       String sortOrder) throws APIManagementException {

        return applicationDAOImpl.getApplicationsWithPagination(user, owner, tenantId, limit, offset, sortBy, sortOrder,
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

        return applicationDAOImpl.getApplicationsCount(tenantId, searchOwner, searchApplication);
    }

    /**
     * These methods load the monetization implementation class
     *
     * @return monetization implementation class
     * @throws APIManagementException if failed to load monetization implementation class
     */
    public Monetization getMonetizationImplClass() throws APIManagementException {

        // ToDO:// read configs
        APIManagerConfiguration configuration = null;
//        configuration= org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder.
//                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
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

        return adminDAOImpl.getMonetizationUsagePublishInfo();
    }

    /**
     * Updates info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    public void updateMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException {

        adminDAOImpl.updateUsagePublishInfo(monetizationUsagePublishInfo);
    }

    /**
     * Add info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    public void addMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException {

        adminDAOImpl.addMonetizationUsagePublishInfo(monetizationUsagePublishInfo);
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
    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(String organization)
            throws APIManagementException {
        return null;
    }

    @Override
    public Map<String, List<KeyManagerConfigurationDTO>> getAllKeyManagerConfigurations()
            throws APIManagementException {
        return null;
    }

    @Override
    public KeyManagerConfigurationDTO getKeyManagerConfigurationById(String organization, String id)
            throws APIManagementException {
        return null;
    }

    @Override
    public boolean isIDPExistInOrg(String organization, String resourceId) throws APIManagementException {
        return keyManagerDAOImpl.isIDPExistInOrg(organization, resourceId);
    }

    @Override
    public ApplicationInfo getLightweightApplicationByConsumerKey(String consumerKey) throws APIManagementException {
        return applicationDAOImpl.getLightweightApplicationByConsumerKey(consumerKey);
    }

    @Override
    public boolean isKeyManagerConfigurationExistById(String organization, String id) throws APIManagementException {

        return keyManagerDAOImpl.isKeyManagerConfigurationExistById(organization, id);
    }

    @Override
    public KeyManagerConfigurationDTO addKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        return null;
    }

    @Override
    public KeyManagerConfigurationDTO updateKeyManagerConfiguration(
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) throws APIManagementException {
        return null;
    }

    @Override
    public void deleteIdentityProvider(String organization, KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {

    }

    @Override
    public void deleteKeyManagerConfigurationById(String organization,
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) throws APIManagementException {

    }

    @Override
    public KeyManagerConfigurationDTO getKeyManagerConfigurationByName(String organization, String name)
            throws APIManagementException {
        return null;
    }

    // ToDo :  Add KM configuration methods

    @Override
    public void addBotDetectionAlertSubscription(String email) throws APIManagementException {

        adminDAOImpl.addBotDetectionAlertSubscription(email);
    }

    @Override
    public List<BotDetectionData> getBotDetectionAlertSubscriptions() throws APIManagementException {

        return adminDAOImpl.getBotDetectionAlertSubscriptions();
    }

    @Override
    public void deleteBotDetectionAlertSubscription(String uuid) throws APIManagementException {

        adminDAOImpl.deleteBotDetectionAlertSubscription(uuid);
    }

    @Override
    public BotDetectionData getBotDetectionAlertSubscription(String field, String value) throws APIManagementException {

        return adminDAOImpl.getBotDetectionAlertSubscription(field, value);
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
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(messageBody)));
            Node bodyContentNode = document.getFirstChild().getFirstChild();

            //Convert XML form to String
            if (bodyContentNode != null) {
                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
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
            APIUtil.handleExceptionWithCode("Category with name '" + category.getName() + "' already exists",
                    ExceptionCodes.from(ExceptionCodes.CATEGORY_ALREADY_EXISTS, category.getName()));
        }
        return adminDAOImpl.addCategory(category, organization);
    }

    public void updateCategory(APICategory apiCategory) throws APIManagementException {

        adminDAOImpl.updateCategory(apiCategory);
    }

    @Override
    public void deleteCategory(String categoryID, String username) throws APIManagementException {

    }

    public List<APICategory> getAllAPICategoriesOfOrganization(String organization) throws APIManagementException {
        return adminDAOImpl.getAllCategories(organization);
    }

    @Override
    public List<APICategory> getAPICategoriesOfOrganization(String organization) throws APIManagementException {
        return null;
    }

    public boolean isCategoryNameExists(String categoryName, String uuid, String organization) throws APIManagementException {

        return adminDAOImpl.isAPICategoryNameExists(categoryName, uuid, organization);
    }

    public APICategory getAPICategoryByID(String apiCategoryId) throws APIManagementException {

        APICategory apiCategory = adminDAOImpl.getAPICategoryByID(apiCategoryId);
        if (apiCategory != null) {
            return apiCategory;
        } else {
            String msg = "Failed to get APICategory. API category corresponding to UUID " + apiCategoryId
                    + " does not exist";
            throw new APIManagementException(msg,
                    ExceptionCodes.from(ExceptionCodes.CATEGORY_NOT_FOUND, apiCategoryId));
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
        // TODO: // read from Config
        WorkflowProperties workflowConfig = null;
//        WorkflowProperties workflowConfig = org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder.
//                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
        if (workflowConfig.isListTasks()) {
            return workflowDAOImpl.getWorkflows(workflowType, status, tenantDomain);
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
        WorkflowProperties workflowConfig = null;
        // TODO: // read from Config
//        WorkflowProperties workflowConfig = org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder.
//                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
        if (workflowConfig.isListTasks()) {
            workflow = workflowDAOImpl.getWorkflowReferenceByExternalWorkflowReferenceID(externelWorkflowRef,
                    status, tenantDomain);
        }

        if (workflow == null) {
            String msg = "External workflow Reference: " + externelWorkflowRef + " was not found.";
            throw new APIManagementException(msg, ExceptionCodes.WORKFLOW_NOT_FOUND);
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
                if (userRoleList.contains(aRole)) {
                    authorizedScopes.add(entry.getKey());
                }
            }
        }
        return authorizedScopes;
    }

    @Override
    public void addTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        adminDAOImpl.addTenantTheme(tenantId, themeContent);
    }

    @Override
    public void updateTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        adminDAOImpl.updateTenantTheme(tenantId, themeContent);
    }

    @Override
    public InputStream getTenantTheme(int tenantId) throws APIManagementException {

        return adminDAOImpl.getTenantTheme(tenantId);
    }

    @Override
    public boolean isTenantThemeExist(int tenantId) throws APIManagementException {

        return adminDAOImpl.isTenantThemeExist(tenantId);
    }

    @Override
    public void deleteTenantTheme(int tenantId) throws APIManagementException {

        adminDAOImpl.deleteTenantTheme(tenantId);
    }

    @Override
    public String getTenantConfig(String organization) throws APIManagementException {
        return "";
        // ToDO: // read from config
        //return ServiceReferenceHolder.getInstance().getApimConfigService().getTenantConfig(organization);
    }

    @Override
    public void updateTenantConfig(String organization, String config) throws APIManagementException {

        Schema schema = APIUtil.retrieveTenantConfigJsonSchema();
        if (schema != null) {
            try {
                org.json.JSONObject uploadedConfig = new org.json.JSONObject(config);
                schema.validate(uploadedConfig);
                APIUtil.validateRestAPIScopes(config);
                // ToDO: // update through config
                //ServiceReferenceHolder.getInstance().getApimConfigService().updateTenantConfig(organization, config);
            } catch (ValidationException | JSONException e) {
                throw new APIManagementException("tenant-config validation failure",
                        ExceptionCodes.from(ExceptionCodes.INVALID_TENANT_CONFIG, e.getMessage()));
            }
        } else {
            throw new APIManagementException("tenant-config validation failure", ExceptionCodes.INTERNAL_ERROR);
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
            policies = policyDAOImpl.getAPIPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equals(level)) {
            policies = policyDAOImpl.getApplicationPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(level)) {
            policies = policyDAOImpl.getSubscriptionPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(level)) {
            policies = policyDAOImpl.getGlobalPolicies(tenantId);
        }

        //Get the API Manager configurations and check whether the unlimited tier is disabled. If disabled, remove
        // the tier from the array.
        // TODO:// read from apim configuration
        APIManagerConfiguration apiManagerConfiguration = null;
//                = ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
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
            policy = policyDAOImpl.getAPIPolicy(name, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equals(level)) {
            policy = policyDAOImpl.getApplicationPolicy(name, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(level)) {
            policy = policyDAOImpl.getSubscriptionPolicy(name, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(level)) {
            policy = policyDAOImpl.getGlobalPolicy(name);
        }

        //Get the API Manager configurations and check whether the unlimited tier is disabled. If disabled, remove
        // the tier from the array.
        // TODO:// read from apim configuration
        APIManagerConfiguration apiManagerConfiguration = null;
//                ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        ThrottleProperties throttleProperties = apiManagerConfiguration.getThrottleProperties();

        if (policy != null && APIConstants.UNLIMITED_TIER.equals(policy.getPolicyName())
                && !throttleProperties.isEnableUnlimitedTier()) {
            return null;
        }

        return policy;

    }

    private String sanitizeName(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    private String getSubstringOfTen(String inputString) {
        return inputString.length() < 10 ? inputString : inputString.substring(0, 10);
    }

    @Override
    public APIPolicy getAPIPolicy(String username, String policyName) throws APIManagementException {
        return policyDAOImpl.getAPIPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String username, String policyName) throws APIManagementException {
        return policyDAOImpl.getApplicationPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String username, String policyName) throws APIManagementException {
        return policyDAOImpl.getSubscriptionPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public GlobalPolicy getGlobalPolicy(String policyName) throws APIManagementException {
        return policyDAOImpl.getGlobalPolicy(policyName);
    }

    @Override
    public APIPolicy getAPIPolicyByUUID(String uuid) throws APIManagementException {
        APIPolicy policy = policyDAOImpl.getAPIPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Advanced Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUUID(String uuid) throws APIManagementException {
        ApplicationPolicy policy = policyDAOImpl.getApplicationPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Application Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUUID(String uuid) throws APIManagementException {
        SubscriptionPolicy policy = policyDAOImpl.getSubscriptionPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Subscription Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public GlobalPolicy getGlobalPolicyByUUID(String uuid) throws APIManagementException {
        GlobalPolicy policy = policyDAOImpl.getGlobalPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Global Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public BlockConditionsDTO getBlockConditionByUUID(String uuid) throws APIManagementException {
        BlockConditionsDTO blockCondition = blockConditionDAOImpl.getBlockConditionByUUID(uuid);
        if (blockCondition == null) {
            handleBlockConditionNotFoundException("Block condition: " + uuid + " was not found.");
        }
        return blockCondition;
    }

    @Override
    public boolean hasAttachments(String username, String policyName, String policyType, String organization) throws APIManagementException {
        if (PolicyConstants.POLICY_LEVEL_APP.equals(policyType)) {
            return policyDAOImpl.hasApplicationPolicyAttachedToApplication(policyName, organization);
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyType)) {
            return policyDAOImpl.hasSubscriptionPolicyAttached(policyName, organization);
        } else {
            return policyDAOImpl.hasAPIPolicyAttached(policyName, organization);
        }
    }

    /**
     * This method creates a monetization plan for a given subscription policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws APIManagementException if failed to create a monetization plan
     */
    private boolean createMonetizationPlan(SubscriptionPolicy subPolicy) throws APIManagementException {

        Monetization monetizationImplementation = getMonetizationImplClass();
        if (monetizationImplementation != null) {
            try {
                return monetizationImplementation.createBillingPlan(subPolicy);
            } catch (MonetizationException e) {
                String error = "Failed to create monetization plan for : " + subPolicy.getPolicyName();
                APIUtil.handleExceptionWithCode(error, e,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, error));
            }
        }
        return false;
    }

    /**
     * This method updates the monetization plan for a given subscription policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws APIManagementException if failed to update the plan
     */
    private boolean updateMonetizationPlan(SubscriptionPolicy subPolicy) throws APIManagementException {

        Monetization monetizationImplementation = getMonetizationImplClass();
        if (monetizationImplementation != null) {
            try {
                return monetizationImplementation.updateBillingPlan(subPolicy);
            } catch (MonetizationException e) {
                String error = "Failed to update monetization plan for : " + subPolicy.getPolicyName();
                APIUtil.handleExceptionWithCode(error, e,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, error));
            }
        }
        return false;
    }

    protected final void handlePolicyNotFoundException(String msg) throws PolicyNotFoundException {

        throw new PolicyNotFoundException(msg);
    }

    protected final void handleBlockConditionNotFoundException(String msg) throws BlockConditionNotFoundException {

        throw new BlockConditionNotFoundException(msg);
    }

}
