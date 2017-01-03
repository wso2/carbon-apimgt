package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Subscription;

import java.util.List;

/**
 * Implementation of APIMgtAdminService
 */
public class APIMgtAdminServiceImpl implements APIMgtAdminService {

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);

    private APISubscriptionDAO apiSubscriptionDAO;

    public APIMgtAdminServiceImpl(APISubscriptionDAO apiSubscriptionDAO)  {
        this.apiSubscriptionDAO = apiSubscriptionDAO;
    }

    /**
     * Return all API subscriptions
     *
     * @return all subscriptions
     * @throws APIManagementException
     */
    @Override
    public List<Subscription> getAPISubscriptions() throws APIManagementException {
        return apiSubscriptionDAO.getAPISubscriptions();
    }
}
