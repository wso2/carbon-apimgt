package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIConfigRetrievalException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

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

    public APIMgtAdminServiceImpl(APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO, ApiDAO apiDAO,
                                  LabelDAO labelDAO) {
        this.apiSubscriptionDAO = apiSubscriptionDAO;
        this.policyDAO = policyDAO;
        this.apiDAO = apiDAO;
        this.labelDAO = labelDAO;
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getAPISubscriptions(int)
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptions(int limit) throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(limit);
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getAPISubscriptionsOfApi(String, String)
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfApi(String apiContext, String apiVersion)
            throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(apiContext, apiVersion);
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getAPIInfo()
     */
    @Override
    public List<APISummary> getAPIInfo() throws APIManagementException {
        List<API> apiList = apiDAO.getAPIs(ApiType.STANDARD);
        List<APISummary> apiSummaryList = new ArrayList<>();
        apiList.forEach(apiInfo -> {
            APISummary apiSummary = new APISummary(apiInfo.getId());
            apiSummary.setName(apiInfo.getName());
            apiSummary.setContext(apiInfo.getContext());
            apiSummary.setVersion(apiInfo.getVersion());
            apiSummary.setUriTemplates(new ArrayList<>(apiInfo.getUriTemplates().values()));
            apiSummaryList.add(apiSummary);
        });
        return apiSummaryList;
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#addPolicy(String, Policy)
     */
    @Override
    public String addPolicy(String policyLevel, Policy policy) throws APIManagementException {
        try {
            String policyUuid = policy.getUuid();
            if (policyUuid == null) {
                policyUuid = UUID.randomUUID().toString();
                policy.setUuid(policyUuid);
            }
            policyDAO.addPolicy(policyLevel, policy);
            return policyUuid;

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't add policy for uuid: " + policy.getUuid() + ", level: " + policyLevel;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void updatePolicy(String policyLevel, Policy policy) throws APIManagementException {
        try {
            policyDAO.updatePolicy(policyLevel, policy);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't add policy for uuid: " + policy.getUuid() + ", level: " + policyLevel;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#deletePolicy(String, String)
     */
    @Override
    public void deletePolicy(String policyName, String policyLevel) throws APIManagementException {
        try {
            policyDAO.deletePolicy(policyName, policyLevel);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update application policy with name: " + policyName + ", level: " +
                    policyLevel;
            log.error(errorMessage, e);
            throw new APIConfigRetrievalException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#deletePolicyByUuid(String, String)
     */
    @Override public void deletePolicyByUuid(String uuid, String policyLevel) throws APIManagementException {
        try {
            policyDAO.deletePolicyByUuid(uuid, policyLevel);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't update application policy with id: " + uuid + ", level: " + policyLevel;
            log.error(errorMessage, e);
            throw new APIConfigRetrievalException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getPolicy(String, String)
     */
    @Override
    public Policy getPolicy(String policyLevel, String policyName) throws APIManagementException {

        Policy policy;
        try {
            policy = policyDAO.getPolicy(policyLevel, policyName);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve policy with name: " + policyName + ", level: " + policyLevel;
            log.error(errorMessage, e);
            throw new APIConfigRetrievalException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        if (policy == null) {
            throw new APIManagementException("Unable to find the policy by name: " + policyName + ", level: " +
                    policyLevel, ExceptionCodes.POLICY_NOT_FOUND);
        }

        return policy;
    }

    @Override public Policy getPolicyByUuid(String policyLevel, String uuid) throws APIManagementException {

        Policy policy;
        try {
            policy = policyDAO.getPolicyByUuid(policyLevel, uuid);

        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve policy with id: " + uuid + ", level: " + policyLevel;
            log.error(errorMessage, e);
            throw new APIConfigRetrievalException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        if (policy == null) {
            throw new APIManagementException("Unable to find the policy by id: " + uuid + ", level: " + policyLevel,
                    ExceptionCodes.POLICY_NOT_FOUND);
        }

        return policy;
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getAllPoliciesByLevel(String)
     */
    @Override
    public List<Policy> getAllPoliciesByLevel(String policyLevel) throws APIManagementException {
        return policyDAO.getPolicies(policyLevel);
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#deleteLabel(String)
     */
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

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#registerGatewayLabels(List, String)
     */
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

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getAPIGatewayServiceConfig(String) (String)
     */
    @Override
    public String getAPIGatewayServiceConfig(String apiId) throws APIConfigRetrievalException {
        try {
            return apiDAO.getGatewayConfig(apiId);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Couldn't retrieve gateway configuration for apiId " + apiId;
            log.error(errorMessage, e);
            throw new APIConfigRetrievalException(errorMessage, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.api.APIMgtAdminService#getAllResourcesForApi(String, String)
     */
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

}
