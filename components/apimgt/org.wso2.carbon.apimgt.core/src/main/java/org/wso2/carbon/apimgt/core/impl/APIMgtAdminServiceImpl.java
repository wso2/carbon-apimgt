package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.dao.impl.LabelDAOImpl;
import org.wso2.carbon.apimgt.core.exception.APIConfigRetrievalException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.core.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of APIMgtAdminService
 */
public class APIMgtAdminServiceImpl implements APIMgtAdminService {

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);

    private APIMConfigurations apimConfiguration;
    private APIGateway apiGateway;
    private DAOFactory daoFactory;

    public APIMgtAdminServiceImpl(DAOFactory daoFactory, APIGateway apiGateway) {
        this.daoFactory = daoFactory;
        this.apimConfiguration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        this.apiGateway = apiGateway;
    }

    protected PolicyDAO getPolicyDAO() throws APIMgtDAOException {
        return daoFactory.getPolicyDAO();
    }

    protected APISubscriptionDAO getAPISubscriptionDAO() throws APIMgtDAOException {
        return daoFactory.getAPISubscriptionDAO();
    }

    protected LabelDAO getLabelDAO() throws APIMgtDAOException {
        return daoFactory.getLabelDAO();
    }

    protected ApiDAO getApiDAO() throws APIMgtDAOException {
        return daoFactory.getApiDAO();
    }

    protected ApplicationDAO getApplicationDAO() throws APIMgtDAOException {
        return daoFactory.getApplicationDAO();
    }

    protected WorkflowDAO getWorkflowDAO() throws APIMgtDAOException {
        return daoFactory.getWorkflowDAO();
    }

    public ThreatProtectionDAO getThreatProtectionDAO() {
        return daoFactory.getThreatProtectionDAO();
    }

    @Override
    public List<Policy> getPoliciesByLevel(PolicyLevel policyLevel) throws APIManagementException {
        try {
            return getPolicyDAO().getPoliciesByLevel(policyLevel);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Throttle Policies with level: " + policyLevel.name();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public Policy getPolicyByLevelAndName(PolicyLevel policyLevel, String policyName) throws APIManagementException {
        try {
            return getPolicyDAO().getPolicyByLevelAndName(policyLevel, policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Throttle Policy with level: " + policyLevel.name() + ", name: "
                    + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }

    @Override
    public List<SubscriptionValidationData> getAPISubscriptions(int limit) throws APIManagementException {
        return getAPISubscriptionDAO().getAPISubscriptionsOfAPIForValidation(limit);
    }

    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfApi(String apiContext, String apiVersion)
            throws APIManagementException {
        return getAPISubscriptionDAO().getAPISubscriptionsOfAPIForValidation(apiContext, apiVersion);
    }

    @Override
    public String addApiPolicy(APIPolicy policy) throws APIManagementException {
        try {
            String policyUuid = policy.getUuid();
            if (policyUuid == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Policy id is null, hence generating a new UUID for the policy with name: " + policy
                            .getPolicyName());
                }
                policyUuid = UUID.randomUUID().toString();
                policy.setUuid(policyUuid);
            }
            getPolicyDAO().addApiPolicy(policy);
            PolicyValidationData policyValidationData = new PolicyValidationData(policyUuid, policy.getPolicyName(),
                    false);
            apiGateway.addPolicy(policyValidationData);
            return policyUuid;

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't add API policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public String addApplicationPolicy(ApplicationPolicy policy) throws APIManagementException {
        try {
            String policyUuid = policy.getUuid();
            if (policyUuid == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Policy id is null, hence generating a new UUID for the policy with name: " + policy
                            .getPolicyName());
                }
                policyUuid = UUID.randomUUID().toString();
                policy.setUuid(policyUuid);
            }
            getPolicyDAO().addApplicationPolicy(policy);
            PolicyValidationData policyValidationData = new PolicyValidationData(policyUuid, policy.getPolicyName(),
                    false);
            apiGateway.addPolicy(policyValidationData);
            return policyUuid;

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't add Application for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public String addSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException {
        try {
            String policyUuid = policy.getUuid();
            if (policyUuid == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Policy id is null, hence generating a new UUID for the policy with name: " + policy
                            .getPolicyName());
                }
                policyUuid = UUID.randomUUID().toString();
                policy.setUuid(policyUuid);
            }
            getPolicyDAO().addSubscriptionPolicy(policy);
            PolicyValidationData policyValidationData = new PolicyValidationData(policyUuid, policy.getPolicyName(),
                    policy.isStopOnQuotaReach());
            apiGateway.addPolicy(policyValidationData);
            return policyUuid;

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't add Subscription policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void updateApiPolicy(APIPolicy policy) throws APIManagementException {
        try {
            getPolicyDAO().updateApiPolicy(policy);
            PolicyValidationData policyValidationData = new PolicyValidationData(policy.getUuid(), policy
                    .getPolicyName(), false);
            apiGateway.updatePolicy(policyValidationData);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update API policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException {
        try {
            getPolicyDAO().updateSubscriptionPolicy(policy);
            PolicyValidationData policyValidationData = new PolicyValidationData(policy.getUuid(), policy
                    .getPolicyName(), policy.isStopOnQuotaReach());
            apiGateway.updatePolicy(policyValidationData);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update Subscription policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void updateApplicationPolicy(ApplicationPolicy policy) throws APIManagementException {
        try {
            getPolicyDAO().updateApplicationPolicy(policy);
            PolicyValidationData policyValidationData = new PolicyValidationData(policy.getUuid(), policy
                    .getPolicyName(), false);
            apiGateway.updatePolicy(policyValidationData);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update Application policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void deletePolicy(String policyName, PolicyLevel policyLevel) throws APIManagementException {
        try {
            Policy policy = getPolicyDAO().getPolicyByLevelAndName(policyLevel, policyName);
            getPolicyDAO().deletePolicy(policyLevel, policyName);
            PolicyValidationData policyValidationData = new PolicyValidationData(policy.getUuid(), policy
                    .getPolicyName(), false);
            apiGateway.deletePolicy(policyValidationData);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't delete policy with name: " + policyName + ", level: " +
                    policyLevel;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void deletePolicyByUuid(String uuid, PolicyLevel policyLevel) throws APIManagementException {
        try {
            getPolicyDAO().deletePolicyByUuid(policyLevel, uuid);
            PolicyValidationData policyValidationData = new PolicyValidationData(uuid, "", false);
            apiGateway.deletePolicy(policyValidationData);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't delete policy with id: " + uuid + ", level: " + policyLevel;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public APIPolicy getApiPolicy(String policyName) throws APIManagementException {

        try {
            return getPolicyDAO().getApiPolicy(policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve API policy with name: " + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String policyName) throws APIManagementException {

        try {
            return getPolicyDAO().getSubscriptionPolicy(policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Subscription policy with name: " + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String policyName) throws APIManagementException {

        try {
            return getPolicyDAO().getApplicationPolicy(policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Application policy with name: " + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public APIPolicy getApiPolicyByUuid(String uuid) throws APIManagementException {
        try {
            return getPolicyDAO().getApiPolicyByUuid(uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve API policy with id: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUuid(String uuid) throws APIManagementException {
        try {
            return getPolicyDAO().getApplicationPolicyByUuid(uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Application policy with id: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }

    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUuid(String uuid) throws APIManagementException {
        try {
            return getPolicyDAO().getSubscriptionPolicyByUuid(uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Subscription policy with id: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public List<APIPolicy> getApiPolicies() throws APIManagementException {
        try {
            return getPolicyDAO().getApiPolicies();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve API policies";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public List<ApplicationPolicy> getApplicationPolicies() throws APIManagementException {
        try {
            return getPolicyDAO().getApplicationPolicies();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Application policies";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public List<SubscriptionPolicy> getSubscriptionPolicies() throws APIManagementException {
        try {
            return getPolicyDAO().getSubscriptionPolicies();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Subscription policies";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public List<Label> getLabels() throws APIManagementException {
        try {
             return getLabelDAO().getLabels();
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while Getting all  labels";
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public Label getLabelByID(String labelID) throws APIManagementException {
        Label  label = null;
        try {
            label = getLabelDAO().getLabelByID(labelID);
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while getting the label by ID";
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return label;
    }
    @Override
    public void deleteLabel(String labelId) throws APIManagementException {

        try {
            getLabelDAO().deleteLabel(labelId);
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while deleting label [labelId] " + labelId;
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }
   @Override
    public Label addLabel(Label label) throws APIManagementException {
      try {
         return LabelDAOImpl.addLabel(label);
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while adding the labels";
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    public Label updateLabel(Label updatedLabel) throws APIManagementException {
        try {
            return getLabelDAO().updateLabel(updatedLabel);
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while updating the label -" + updatedLabel.getId();
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void registerGatewayLabels(List<Label> labels, String overwriteLabels) throws APIManagementException {

        if (!labels.isEmpty()) {
            List<String> labelNames = new ArrayList<>();
            boolean overwriteValues = Boolean.parseBoolean(overwriteLabels);

            for (Label label : labels) {
                labelNames.add(label.getName());
            }

            try {
                List<Label> existingLabels = getLabelDAO().getLabelsByName(labelNames);

                if (!existingLabels.isEmpty()) {
                    List<Label> labelsToRemove = new ArrayList<>();

                    for (Label existingLabel : existingLabels) {
                        for (Label label : labels) {
                            if (existingLabel.getName().equals(label.getName())) {
                                if (overwriteValues) {
                                    getLabelDAO().updateLabel(label);
                                }
                                labelsToRemove.add(label);
                            }
                        }
                    }
                    labels.removeAll(labelsToRemove);    // Remove already existing labels from the list
                }
                getLabelDAO().addLabels(labels);
            } catch (APIMgtDAOException e) {
                String msg = "Error occurred while registering gateway labels";
                log.error(msg, e);
                throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        }

    }

    @Override
    public String getAPIGatewayServiceConfig(String apiId) throws APIConfigRetrievalException {
        try {
            return getApiDAO().getGatewayConfigOfAPI(apiId);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve gateway configuration for apiId " + apiId;
            log.error(errorMessage, e);
            throw new APIConfigRetrievalException(errorMessage, e.getErrorHandler());
        }
    }

    @Override
    public List<UriTemplate> getAllResourcesForApi(String apiContext, String apiVersion) throws APIManagementException {
        try {
            return getApiDAO().getResourcesOfApi(apiContext, apiVersion);
        } catch (APIMgtDAOException e) {
            String msg = "Couldn't retrieve resources for Api Name: " + apiContext;
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public List<API> getAPIsByStatus(List<String> gatewayLabels, String status)
            throws APIManagementException {
        List<API> apiList;
        try {
            if (gatewayLabels != null && status != null) {
                apiList = getApiDAO().getAPIsByStatus(gatewayLabels, status);
            } else {
                if (gatewayLabels == null) {
                    String msg = "Gateway labels cannot be null";
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.GATEWAY_LABELS_CANNOT_BE_NULL);
                } else {
                    String msg = "Status cannot be null";
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.STATUS_CANNOT_BE_NULL);
                }
            }
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while getting the API list in given states";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiList;
    }

    @Override
    public List<API> getAPIsByGatewayLabel(List<String> gatewayLabels) throws APIManagementException {
        List<API> apiList;
        try {
            if (gatewayLabels != null) {
                apiList = getApiDAO().getAPIsByGatewayLabel(gatewayLabels);
            } else {
                String msg = "Gateway labels cannot be null";
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.GATEWAY_LABELS_CANNOT_BE_NULL);
            }
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while getting the API list in given gateway labels";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiList;
    }

    @Override
    public RegistrationSummary getRegistrationSummary() {
        return new RegistrationSummary(apimConfiguration);
    }

    @Override
    public List<Application> getAllApplications() throws APIManagementException {
        try {
            return getApplicationDAO().getAllApplications();
        } catch (APIMgtDAOException ex) {
            String msg = "Error occurred while getting the Application list";
            log.error(msg, ex);
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public List<Endpoint> getAllEndpoints() throws APIManagementException {
        try {
            return getApiDAO().getEndpoints();
        } catch (APIMgtDAOException ex) {
            String msg = "Error occurred while getting the Endpoint list";
            log.error(msg, ex);
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public String getEndpointGatewayConfig(String endpointId) throws APIManagementException {
        try {
            return getApiDAO().getEndpointConfig(endpointId);
        } catch (APIMgtDAOException ex) {
            String msg = "Error occurred while getting the Endpoint Configuration";
            log.error(msg, ex);
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    @Override
    public Set<PolicyValidationData> getAllPolicies() throws APIManagementException {
        try {
            return getPolicyDAO().getAllPolicies();
        } catch (APIMgtDAOException ex) {
            String msg = "Error occurred while retrieving policies";
            log.error(msg, ex);
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public String addBlockCondition(BlockConditions blockConditions) throws APIManagementException {
        try {
            if (StringUtils.isEmpty(blockConditions.getUuid())) {
                blockConditions.setUuid(UUID.randomUUID().toString());
            }
            getPolicyDAO().addBlockConditions(blockConditions);
            apiGateway.addBlockCondition(blockConditions);
            return blockConditions.getUuid();
        } catch (APIMgtDAOException e) {
            String errorMessage =
                    "Couldn't add block condition with condition type: " + blockConditions.getConditionType()
                            + ", condition value: " + blockConditions.getConditionValue();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public boolean updateBlockConditionStateByUUID(String uuid, Boolean state) throws APIManagementException {
        try {
            if (getPolicyDAO().updateBlockConditionStateByUUID(uuid, state)) {
                BlockConditions blockConditions = getBlockConditionByUUID(uuid);
                apiGateway.updateBlockCondition(blockConditions);
                return true;
            } else {
                return false;
            }
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update block condition with UUID: " + uuid + ", state: " + state;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public boolean deleteBlockConditionByUuid(String uuid) throws APIManagementException {
        try {
            BlockConditions blockConditions = getBlockConditionByUUID(uuid);
            if (getPolicyDAO().deleteBlockConditionByUuid(uuid)) {
                apiGateway.deleteBlockCondition(blockConditions);
                return true;
            } else {
                return false;
            }
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't delete block condition with UUID: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public List<BlockConditions> getBlockConditions() throws APIManagementException {
        try {
            return getPolicyDAO().getBlockConditions();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't get list of block conditions.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public BlockConditions getBlockConditionByUUID(String uuid) throws APIManagementException {
        try {
            return getPolicyDAO().getBlockConditionByUUID(uuid);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't get block condition by UUID: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public String addCustomRule(CustomPolicy customPolicy) throws APIManagementException {
        try {
            //todo: deploy policy in CEP
            return getPolicyDAO().addCustomPolicy(customPolicy);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't add custom policy with policy name: " + customPolicy.getPolicyName();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void updateCustomRule(CustomPolicy customPolicy) throws APIManagementException {
        try {
            getPolicyDAO().updateCustomPolicy(customPolicy);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update custom policy with UUID: " + customPolicy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void deleteCustomRule(String uuid) throws APIManagementException {
        try {
            getPolicyDAO().deleteCustomPolicy(uuid);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't delete custom policy with UUID: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public List<CustomPolicy> getCustomRules() throws APIManagementException {
        try {
            return getPolicyDAO().getCustomPolicies();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't get list of custom policy.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public CustomPolicy getCustomRuleByUUID(String uuid) throws APIManagementException {
        try {
            return getPolicyDAO().getCustomPolicyByUuid(uuid);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't get custom policy by UUID: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public Workflow retrieveWorkflow(String workflowRefId) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving workflow for refId: " + workflowRefId);
        }
        try {
            return getWorkflowDAO().retrieveWorkflow(workflowRefId);
        } catch (APIMgtDAOException e) {
            String message = "Error while retrieving workflow entry for :" + workflowRefId;
            log.error(message, e);
            throw new APIManagementException(message, e, e.getErrorHandler());
        }    
    }

    @Override
    public WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor, Workflow workflow)
            throws APIManagementException {
        if (workflow.getWorkflowReference() == null) {
            String message = "Error while changing the workflow. Missing reference";
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.WORKFLOW_EXCEPTION);
        }
        return workflow.completeWorkflow(workflowExecutor);
    }

    @Override
    public List<Workflow> retrieveUncompletedWorkflowsByType(String type) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Requesting for uncompleted workflow information for type: " + type);
        }
        if (type == null) {
            String message = "Error while retrieving workflow information. Missing workflow type";
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.WORKFLOW_RETRIEVE_EXCEPTION);
        }
        try {
            return getWorkflowDAO().retrieveUncompleteWorkflows(type);
        } catch (APIMgtDAOException e) {
            String message = "Error while retrieving workflow information";
            log.error(message, e);
            throw new APIManagementException(message, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }  
    }

    @Override
    public List<Workflow> retrieveUncompletedWorkflows() throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Requesting all uncompleted workflow information ");
        }
        try {
            return getWorkflowDAO().retrieveUncompleteWorkflows();
        } catch (APIMgtDAOException e) {
            String message = "Error while retrieving workflow information";
            log.error(message, e);
            throw new APIManagementException(message, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public ThreatProtectionPolicy getThreatProtectionPolicy(String policyId) throws APIManagementException {
        try {
            return getThreatProtectionDAO().getPolicy(policyId);
        } catch (APIMgtDAOException e) {
            String message = "Error while retrieving threat protection policy";
            log.error(message, e);
            throw new APIManagementException(message, e);
        }
    }

    @Override
    public List<ThreatProtectionPolicy> getThreatProtectionPolicyList() throws APIManagementException {
        try {
            return getThreatProtectionDAO().getPolicies();
        } catch (APIMgtDAOException e) {
            String message = "Error while retrieving threat protection policy list";
            log.error(message, e);
            throw new APIManagementException(message, e);
        }
    }

    @Override
    public void addThreatProtectionPolicy(ThreatProtectionPolicy policy) throws APIManagementException {
        try {
            if (StringUtils.isBlank(policy.getUuid())) {
                policy.setUuid(UUID.randomUUID().toString());
                getThreatProtectionDAO().addPolicy(policy);
                apiGateway.addThreatProtectionPolicy(policy);
            } else {
                getThreatProtectionDAO().updatePolicy(policy);
                apiGateway.updateThreatProtectionPolicy(policy);
            }
        } catch (APIMgtDAOException e) {
            String message = "Error adding threat protection policy";
            log.error(message, e);
            throw new APIManagementException(message, e);
        }
    }

    @Override
    public void deleteThreatProtectionPolicy(String policyId) throws APIManagementException {
        try {
            getThreatProtectionDAO().deletePolicy(policyId);

            ThreatProtectionPolicy policy = new ThreatProtectionPolicy();
            policy.setUuid(policyId);
            apiGateway.deleteThreatProtectionPolicy(policy);
        } catch (APIMgtDAOException e) {
            String message = "Error deleting threat protection policy";
            log.error(message, e);
            throw new APIManagementException(message, e);
        }
    }
}
