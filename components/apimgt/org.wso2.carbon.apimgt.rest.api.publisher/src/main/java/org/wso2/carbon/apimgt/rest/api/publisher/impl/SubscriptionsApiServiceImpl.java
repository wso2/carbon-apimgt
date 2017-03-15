package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.ETagGenerator;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;

import javax.ws.rs.core.Response;
import java.util.List;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:47:43.416+05:30")
public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionsApiService.class);

    @Override
    public Response subscriptionsBlockSubscriptionPost(String subscriptionId
            , String blockState
            , String ifMatch
            , String ifUnmodifiedSince
            , String minorVersion
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.updateSubscriptionStatus(subscriptionId, APIMgtConstants.SubscriptionStatus.valueOf(blockState));
            Subscription newSubscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(newSubscription);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            String msg = "Error while blocking the subscription " + subscriptionId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        }
        return null;
    }

    @Override
    public Response subscriptionsGet(String apiId
            , Integer limit
            , Integer offset
            , String accept
            , String ifNoneMatch
            , String minorVersion
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            List<Subscription> subscriptionList = apiPublisher.getSubscriptionsByAPI(apiId);
            SubscriptionListDTO subscriptionListDTO = MappingUtil.fromSubscriptionListToDTO(subscriptionList, limit,
                    offset);
            return Response.ok().entity(subscriptionListDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String msg = "Error while retrieving subscriptions of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId
            , String accept
            , String ifNoneMatch
            , String ifModifiedSince
            , String minorVersion
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = subscriptionsSubscriptionIdGetFingerprint(subscriptionId, accept, ifNoneMatch,
                    ifModifiedSince, minorVersion);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            Subscription subscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            if (subscription == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
            }
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(subscription);
            return Response.ok().header("Etag", "\"" + existingFingerprint + "\"").entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            String msg = "Error while getting the subscription " + subscriptionId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        }
        return null;
    }

    public String subscriptionsSubscriptionIdGetFingerprint(String subscriptionId, String accept, String ifNoneMatch,
            String ifModifiedSince, String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username)
                    .getLastUpdatedTimeOfSubscription(subscriptionId);
            return ETagGenerator.getETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of subscription " + subscriptionId;
            log.error(errorMessage, e);
            return null;
        }
    }

    @Override
    public Response subscriptionsUnblockSubscriptionPost(String subscriptionId
            , String ifMatch
            , String ifUnmodifiedSince
            , String minorVersion
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
