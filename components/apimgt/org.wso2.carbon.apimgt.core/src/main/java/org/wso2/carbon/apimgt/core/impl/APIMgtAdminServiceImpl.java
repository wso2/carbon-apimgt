package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIConfigRetrievalException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
<<<<<<< fa2fe480f6fd3d66f9b14330e5b0bbc3ffef255e
import org.wso2.carbon.apimgt.core.models.Application;
=======
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
>>>>>>> Fixing end to end blacklist condition adding,updating and deleting.
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of APIMgtAdminService
 */
public class APIMgtAdminServiceImpl implements APIMgtAdminService {

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);

    private APISubscriptionDAO apiSubscriptionDAO;
    private PolicyDAO policyDAO;
    private ApiDAO apiDAO;
    private LabelDAO labelDAO;
    private ApplicationDAO applicationDAO;
    private APIMConfigurations apimConfiguration;

    public APIMgtAdminServiceImpl(APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO, ApiDAO apiDAO,
                                  LabelDAO labelDAO , ApplicationDAO applicationDAO) {
        this.apiSubscriptionDAO = apiSubscriptionDAO;
        this.policyDAO = policyDAO;
        this.apiDAO = apiDAO;
        this.labelDAO = labelDAO;
        this.apimConfiguration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        this.applicationDAO = applicationDAO;
    }

    @Override
    public List<Policy> getPoliciesByLevel(PolicyLevel policyLevel) throws APIManagementException {
        try {
            return policyDAO.getPoliciesByLevel(policyLevel);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Throttle Policies with level: " + policyLevel.name();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public Policy getPolicyByLevelAndName(PolicyLevel policyLevel, String policyName) throws APIManagementException {
        try {
            return policyDAO.getPolicyByLevelAndName(policyLevel, policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Throttle Policy with level: " + policyLevel.name() + ", name: "
                    + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }

    @Override
    public List<SubscriptionValidationData> getAPISubscriptions(int limit) throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(limit);
    }

    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfApi(String apiContext, String apiVersion)
            throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(apiContext, apiVersion);
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
            policyDAO.addApiPolicy(policy);
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
            policyDAO.addApplicationPolicy(policy);
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
            policyDAO.addSubscriptionPolicy(policy);
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
            policyDAO.updateApiPolicy(policy);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update API policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException {
        try {
            policyDAO.updateSubscriptionPolicy(policy);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update Subscription policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void updateApplicationPolicy(ApplicationPolicy policy) throws APIManagementException {
        try {
            policyDAO.updateApplicationPolicy(policy);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update Application policy for uuid: " + policy.getUuid();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void deletePolicy(String policyName, PolicyLevel policyLevel) throws APIManagementException {
        try {
            policyDAO.deletePolicy(policyLevel, policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update application policy with name: " + policyName + ", level: " +
                    policyLevel;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void deletePolicyByUuid(String uuid, PolicyLevel policyLevel) throws APIManagementException {
        try {
            policyDAO.deletePolicyByUuid(policyLevel, uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update application policy with id: " + uuid + ", level: " + policyLevel;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public APIPolicy getApiPolicy(String policyName) throws APIManagementException {

        try {
            return policyDAO.getApiPolicy(policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve API policy with name: " + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String policyName) throws APIManagementException {

        try {
            return policyDAO.getSubscriptionPolicy(policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Subscription policy with name: " + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String policyName) throws APIManagementException {

        try {
            return policyDAO.getApplicationPolicy(policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Application policy with name: " + policyName;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public APIPolicy getApiPolicyByUuid(String uuid) throws APIManagementException {
        try {
            return policyDAO.getApiPolicyByUuid(uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve API policy with id: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUuid(String uuid) throws APIManagementException {
        try {
            return policyDAO.getApplicationPolicyByUuid(uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Application policy with id: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }

    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUuid(String uuid) throws APIManagementException {
        try {
            return policyDAO.getSubscriptionPolicyByUuid(uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Subscription policy with id: " + uuid;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public List<APIPolicy> getApiPolicies() throws APIManagementException {
        try {
            return policyDAO.getApiPolicies();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve API policies";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public List<ApplicationPolicy> getApplicationPolicies() throws APIManagementException {
        try {
            return policyDAO.getApplicationPolicies();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Application policies";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public List<SubscriptionPolicy> getSubscriptionPolicies() throws APIManagementException {
        try {
            return policyDAO.getSubscriptionPolicies();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve Subscription policies";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, e.getErrorHandler());
        }
    }

    @Override
    public void deleteLabel(String labelId) throws APIManagementException {

        try {
            labelDAO.deleteLabel(labelId);
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while deleting label [labelId] " + labelId;
            log.error(msg, e);
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
                List<Label> existingLabels = labelDAO.getLabelsByName(labelNames);

                if (!existingLabels.isEmpty()) {
                    List<Label> labelsToRemove = new ArrayList<>();

                    for (Label existingLabel : existingLabels) {
                        for (Label label : labels) {
                            if (existingLabel.getName().equals(label.getName())) {
                                if (overwriteValues) {
                                    labelDAO.updateLabel(label);
                                }
                                labelsToRemove.add(label);
                            }
                        }
                    }
                    labels.removeAll(labelsToRemove);    // Remove already existing labels from the list
                }
                labelDAO.addLabels(labels);
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
            return apiDAO.getGatewayConfigOfAPI(apiId);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve gateway configuration for apiId " + apiId;
            log.error(errorMessage, e);
            throw new APIConfigRetrievalException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public List<UriTemplate> getAllResourcesForApi(String apiContext, String apiVersion) throws APIManagementException {
        try {
            return apiDAO.getResourcesOfApi(apiContext, apiVersion);
        } catch (APIManagementException e) {
            String msg = "Couldn't retrieve resources for Api Name: " + apiContext;
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override public List<API> getAPIsByStatus(List<String> gatewayLabels, String status)
            throws APIManagementException {
        List<API> apiList;
        try {
            if (gatewayLabels != null && status != null) {
                apiList = apiDAO.getAPIsByStatus(gatewayLabels, status);
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
            throw new APIManagementException(msg, ExceptionCodes.APIM_DAO_EXCEPTION);
        }
        return apiList;
    }

    @Override public List<API> getAPIsByGatewayLabel(List<String> gatewayLabels) throws APIManagementException {
        List<API> apiList;
        try {
            if (gatewayLabels != null) {
                apiList = apiDAO.getAPIsByGatewayLabel(gatewayLabels);
            } else {
                String msg = "Gateway labels cannot be null";
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.GATEWAY_LABELS_CANNOT_BE_NULL);
            }
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while getting the API list in given gateway labels";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.APIM_DAO_EXCEPTION);
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
            return applicationDAO.getAllApplications();
        } catch (APIMgtDAOException ex) {
            String msg = "Error occurred while getting the Application list";
            log.error(msg, ex);
            throw new APIManagementException(msg, ExceptionCodes.APIM_DAO_EXCEPTION);
        }
    }

    @Override
    public List<String> getAllEndpoints() throws APIManagementException {
        try {
            return apiDAO.getUUIDsOfGlobalEndpoints();
        } catch (APIMgtDAOException ex) {
            String msg = "Error occurred while getting the Endpoint list";
            log.error(msg, ex);
            throw new APIManagementException(msg, ExceptionCodes.APIM_DAO_EXCEPTION);
        }
    }

    @Override
    public String getEndpointGatewayConfig(String endpointId) throws APIManagementException {
        try {
            return apiDAO.getEndpointConfig(endpointId);
        } catch (APIMgtDAOException ex) {
            String msg = "Error occurred while getting the Endpoint Configuration";
            log.error(msg, ex);
            throw new APIManagementException(msg, ExceptionCodes.APIM_DAO_EXCEPTION);
        }

    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#addBlockCondition(String, String)
     */
    @Override public String addBlockCondition(String conditionType, String conditionValue)
            throws APIManagementException {
        try {
            return policyDAO.addBlockConditions(conditionType, conditionValue);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't add block condition.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#updateBlockConditionStateByUUID(String, Boolean)
     */
    @Override public boolean updateBlockConditionStateByUUID(String uuid, Boolean state) throws APIManagementException {
        try {
            return policyDAO.updateBlockConditionStateByUUID(uuid, state);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update block condition.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#deleteBlockConditionByUuid(String)
     */
    @Override public boolean deleteBlockConditionByUuid(String uuid) throws APIManagementException {
        try {
            return policyDAO.deleteBlockConditionByUuid(uuid);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't delete block condition.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getBlockConditions()
     */
    @Override public List<BlockConditions> getBlockConditions() throws APIManagementException {
        try {
            return policyDAO.getBlockConditions();
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't get list of block conditions.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getBlockConditionByUUID(String)
     */
    @Override public BlockConditions getBlockConditionByUUID(String uuid) throws APIManagementException {
        try {
            return policyDAO.getBlockConditionByUUID(uuid);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't get block condition by UUID.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }
}
