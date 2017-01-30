package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-18T15:27:32.639+05:30")
public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionsApiServiceImpl.class);

    @Override
    public Response subscriptionsGet(String apiContext
            , String apiVersion
            , Integer limit
    ) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            List<SubscriptionValidationData> subscriptionsOfApi;
            if (StringUtils.isEmpty(apiContext) || StringUtils.isEmpty(apiVersion)) {
                APIUtils.logDebug("API Context or version is null or empty. Retrieving subscriptions of all APIs", log);
                subscriptionsOfApi = apiMgtAdminService.getAPISubscriptions(limit);
            } else {
                subscriptionsOfApi = apiMgtAdminService.getAPISubscriptionsOfApi(apiContext, apiVersion);
            }
            SubscriptionListDTO subscriptionsList = MappingUtil.convertToSubscriptionListDto(subscriptionsOfApi);
            return Response.ok(subscriptionsList).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving subscriptions.";
            HashMap<String, String> paramList = new HashMap<String, String>();
            if (!StringUtils.isEmpty(apiContext)) {
                paramList.put(APIMgtConstants.ExceptionsConstants.API_CONTEXT, apiContext);
            }
            if (!StringUtils.isEmpty(apiVersion)) {
                paramList.put(APIMgtConstants.ExceptionsConstants.API_VERSION, apiVersion);
            }
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
