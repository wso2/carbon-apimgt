package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.ValidateSubscriptionApiService;

import java.util.HashMap;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-12-08T15:43:23.237+05:30")
public class ValidateSubscriptionApiServiceImpl extends ValidateSubscriptionApiService {

    private static final Logger log = LoggerFactory.getLogger(ValidateSubscriptionApiServiceImpl.class);

    @Override
    public Response validateSubscriptionGet(String apiContext
, String apiVersion
, String consumerKey
, String accept
 ) throws NotFoundException {
        SubscriptionValidationInfo validationInfo = new SubscriptionValidationInfo(false);
        try {
            APIMgtAdminService adminService = RestApiUtil.getAPIMgtAdminService();
            validationInfo = adminService.validateSubscription(apiContext, apiVersion, consumerKey);
        } catch (APIManagementException e) {
            String errorMessage = "Error while validating subscription for API : " + apiContext +"/" + apiVersion;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_CONTEXT, apiContext);
            paramList.put(APIMgtConstants.ExceptionsConstants.API_VERSION, apiVersion);
            paramList.put(APIMgtConstants.ExceptionsConstants.CONSUMER_KEY, consumerKey);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(validationInfo).build();
    }
}
