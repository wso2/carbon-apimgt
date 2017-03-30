package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
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

    public APIMgtAdminServiceImpl(APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO, ApiDAO apiDAO) {
        this.apiSubscriptionDAO = apiSubscriptionDAO;
        this.policyDAO = policyDAO;
        this.apiDAO = apiDAO;

    }

    /**
     * Return all API subscriptions
     *
     * @param limit Subscription Limit
     * @return all subscriptions
     * @throws APIManagementException   If failed to retrieve subscription list.
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptions(int limit) throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(limit);
    }

    /**
     * Return all API subscriptions of a given API
     *
     * @param apiContext Context of API
     * @param apiVersion Version of API
     * @return all subscriptions
     * @throws APIManagementException   If failed to retrieve subscription list.
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfApi(String apiContext, String apiVersion)
            throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(apiContext, apiVersion);
    }

    /**
     * Load api info from db
     *
     * @return List summery of al the available apis
     * @throws APIManagementException If failed to get API information.
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

    @Override
    public void addPolicy(String policyLevel, Policy policy) throws APIManagementException {
        policyDAO.addPolicy(policyLevel, policy);
    }

    @Override
    public void updatePolicy(Policy policy) throws APIManagementException {

    }

    @Override
    public void deletePolicy(String policyName, String policyLevel) throws APIManagementException {
        policyDAO.deletePolicy(policyName, policyLevel);
    }

    @Override
    public Policy getPolicy(String policyLevel, String policyName) throws APIManagementException {
        return policyDAO.getPolicy(policyLevel, policyName);
    }

    @Override
    public List<Policy> getAllPoliciesByLevel(String policyLevel) throws APIManagementException {
        return policyDAO.getPolicies(policyLevel);
    }
}
