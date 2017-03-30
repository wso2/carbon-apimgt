package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.util.ArrayList;
import java.util.List;

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
     * @see #getAPISubscriptions(int)
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptions(int limit) throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(limit);
    }

    /**
     * @see #getAPISubscriptionsOfApi(String, String)
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfApi(String apiContext, String apiVersion)
            throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(apiContext, apiVersion);
    }

    /**
     * @see #getAPIInfo()
     */
    @Override
    public List<APISummary> getAPIInfo() throws APIManagementException {
        List<API> apiList = apiDAO.getAPIs();
        List<APISummary> apiSummaryList = new ArrayList<APISummary>();
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
     * @see #addPolicy(String, Policy)
     */
    @Override
    public void addPolicy(String policyLevel, Policy policy) throws APIManagementException {
        policyDAO.addPolicy(policyLevel, policy);
    }

    /**
     * @see #updatePolicy(Policy)
     */
    @Override
    public void updatePolicy(Policy policy) throws APIManagementException {

    }

    /**
     * @see #deletePolicy(Policy)
     */
    @Override
    public void deletePolicy(String policyName, String policyLevel) throws APIManagementException {
        policyDAO.deletePolicy(policyName, policyLevel);
    }

    /**
     * @see #getPolicy(String, String)
     */
    @Override
    public Policy getPolicy(String policyLevel, String policyName) throws APIManagementException {
        return policyDAO.getPolicy(policyLevel, policyName);
    }

    /**
     * @see #getAllPoliciesByLevel(String)
     */
    @Override
    public List<Policy> getAllPoliciesByLevel(String policyLevel) throws APIManagementException {
        return policyDAO.getPolicies(policyLevel);
    }

    /**
     * @see #deleteLabel(String)
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
}
