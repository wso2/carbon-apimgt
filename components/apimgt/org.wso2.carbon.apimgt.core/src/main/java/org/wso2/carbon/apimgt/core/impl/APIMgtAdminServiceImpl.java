package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationInfo;

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
     * Validates a subscription
     *
     * @param apiContext  Context of the API
     * @param apiVersion  Version of the API
     * @param consumerKey Consumer key of the application
     * @return Subscription Validation Information
     * @throws APIManagementException
     */
    @Override
    public SubscriptionValidationInfo validateSubscription(String apiContext, String apiVersion, String consumerKey)
            throws APIManagementException {
        //validate parameters
        if (StringUtils.isEmpty(apiContext) || StringUtils.isEmpty(apiVersion) || StringUtils.isEmpty(consumerKey)) {
            String message = "None of API Context, API Version and Consumer Key can be null nor empty. " +
                    "Received: Api Context = " + apiContext + " Api Version = " + apiVersion + " Consumer Key = " +
                    consumerKey;
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        return apiSubscriptionDAO.validateSubscription(apiContext, apiVersion, consumerKey);
    }
}
