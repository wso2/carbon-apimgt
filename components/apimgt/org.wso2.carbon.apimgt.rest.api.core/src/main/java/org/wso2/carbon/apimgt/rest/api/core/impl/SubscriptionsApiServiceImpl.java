package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-18T15:27:32.639+05:30")
public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {

    private APIMgtAdminService apiMgtAdminService;

    private static final Logger log = LoggerFactory.getLogger(SubscriptionsApiServiceImpl.class);

    public SubscriptionsApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    /**
     * Retrieve subscriptions
     *
     * @param apiContext Context of the API
     * @param apiVersion API version
     * @param limit      Limit value
     * @return Subscriptions of the API
     * @throws NotFoundException If failed to retrieve subscriptions
     */
    @Override
    public Response subscriptionsGet(String apiContext, String apiVersion,
                                     Integer limit, String accept,
                                     Request request) throws NotFoundException {
        try {
            List<SubscriptionValidationData> subscriptionsOfApi;
            if (StringUtils.isEmpty(apiContext) || StringUtils.isEmpty(apiVersion)) {
                APIUtils.logDebug("API Context or version is null or empty. Retrieving subscriptions of all APIs", log);
                subscriptionsOfApi = apiMgtAdminService.getAPISubscriptions(limit);
            } else {
                subscriptionsOfApi = apiMgtAdminService.getAPISubscriptionsOfApi(apiContext, apiVersion);
            }
            SubscriptionListDTO subscriptionsList = new SubscriptionListDTO();
            subscriptionsList.setList(MappingUtil.convertToSubscriptionListDto(subscriptionsOfApi));
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
