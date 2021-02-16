package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.WebhooksApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WebhookSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.AsyncAPIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Set;
import javax.ws.rs.core.Response;


public class WebhooksApiServiceImpl implements WebhooksApiService {

    private static final Log log = LogFactory.getLog(WebhooksApiServiceImpl.class);

    public Response webhooksSubscriptionsGet(String applicationId, String apiId, String xWSO2Tenant, MessageContext messageContext) {

        if (StringUtils.isNotEmpty(applicationId)) {
            String username = RestApiCommonUtil.getLoggedInUsername();
            try {
                APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
                WebhookSubscriptionListDTO WebhookSubscriptionListDTO;
                Set<Subscription> subscriptionSet = apiConsumer.getTopicSubscriptions(applicationId, apiId);
                WebhookSubscriptionListDTO = AsyncAPIMappingUtil.fromSubscriptionListToDTO(subscriptionSet);
                return Response.ok().entity(WebhookSubscriptionListDTO).build();
            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else {
                    RestApiUtil.handleInternalServerError("Failed to get topic subscriptions of Async API " + apiId, e, log);
                }
            }
        } else {
            RestApiUtil.handleBadRequest("Application Id cannot be empty", log);
        }
        return null;
    }
}
