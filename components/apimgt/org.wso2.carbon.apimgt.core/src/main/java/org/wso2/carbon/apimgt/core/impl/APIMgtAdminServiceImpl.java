package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of APIMgtAdminService
 */
public class APIMgtAdminServiceImpl implements APIMgtAdminService {

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);

    private APISubscriptionDAO apiSubscriptionDAO;
    private ApiDAO apiDAO;

    public APIMgtAdminServiceImpl(APISubscriptionDAO apiSubscriptionDAO, ApiDAO apiDAO)  {
        this.apiSubscriptionDAO = apiSubscriptionDAO;
        this.apiDAO = apiDAO;

    }

    /**
     * Return all API subscriptions
     *
     * @return all subscriptions
     * @throws APIManagementException
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptions() throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation();
    }

    /**
     * Return all API subscriptions of a given API
     *
     * @param apiContext
     * @param apiVersion
     * @return all subscriptions
     * @throws APIManagementException
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
     * @throws APIManagementException
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
            apiSummary.setUriTemplates(apiInfo.getUriTemplates());
            apiSummaryList.add(apiSummary);
        });
        return apiSummaryList;
    }
}
