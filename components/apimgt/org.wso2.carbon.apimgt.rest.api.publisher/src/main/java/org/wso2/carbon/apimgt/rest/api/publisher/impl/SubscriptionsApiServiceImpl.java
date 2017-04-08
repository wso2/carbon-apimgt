package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2016-11-01T13:47:43.416+05:30")
public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionsApiService.class);

    /**
     * Block an existing subscription
     *
     * @param subscriptionId    ID of the subscription
     * @param blockState        Subscription block state
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           ms4j request object
     * @return Updated subscription DTO as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionsBlockSubscriptionPost(String subscriptionId, String blockState, String ifMatch,
                                                       String ifUnmodifiedSince, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.updateSubscriptionStatus(subscriptionId, APIMgtConstants.SubscriptionStatus.valueOf
                    (blockState));
            Subscription newSubscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(newSubscription);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            String msg = "Error while blocking the subscription " + subscriptionId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        }
        return null;
    }

    /**
     * Retrieve all subscriptions for a particular API
     *
     * @param apiId       ID of the API
     * @param limit       Maximum subscriptions to return
     * @param offset      Starting position of the pagination
     * @param accept      Accept header value
     * @param ifNoneMatch If-Match header value
     * @param request     ms4j request object
     * @return List of qualifying subscriptions DTOs as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionsGet(String apiId, Integer limit, Integer offset, String accept, String ifNoneMatch,
                                     Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        List<Subscription> subscriptionList;
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            if (StringUtils.isNotEmpty(apiId)) {
                subscriptionList = apiPublisher.getSubscriptionsByAPI(apiId);
                SubscriptionListDTO subscriptionListDTO = MappingUtil.fromSubscriptionListToDTO(subscriptionList, limit,
                        offset);
                return Response.ok().entity(subscriptionListDTO).build();
            } else {
                RestApiUtil.handleBadRequest("API ID can not be null", log);
            }

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

    /**
     * Retrieves a single subscription
     *
     * @param subscriptionId  ID of the subscription
     * @param accept          Accept header value
     * @param ifNoneMatch     If-Match header value
     * @param ifModifiedSince If-Modified-Since value
     * @param request         ms4j request object
     * @return Requested subscription details
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId, String accept, String ifNoneMatch, String
            ifModifiedSince, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = subscriptionsSubscriptionIdGetFingerprint(subscriptionId, accept, ifNoneMatch,
                    ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            Subscription subscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            if (subscription == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
            }
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(subscription);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(subscriptionDTO)
                    .build();
        } catch (APIManagementException e) {
            String msg = "Error while getting the subscription " + subscriptionId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        }
        return null;
    }

    /**
     * Retrieve the fingerprint of the subscription
     *
     * @param subscriptionId  ID of the subscription
     * @param accept          Accept header value
     * @param ifNoneMatch     If-Match header value
     * @param ifModifiedSince If-Modified-Since value
     * @param request         ms4j request object
     * @return Fingerprint of the subscription
     */
    public String subscriptionsSubscriptionIdGetFingerprint(String subscriptionId, String accept, String ifNoneMatch,
                                                            String ifModifiedSince, Request request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username)
                    .getLastUpdatedTimeOfSubscription(subscriptionId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of subscription " + subscriptionId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * @param subscriptionId    ID of the subscription
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Modified-Since value
     * @param request           ms4j request object
     * @return ms4j request object
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionsUnblockSubscriptionPost(String subscriptionId, String ifMatch, String
            ifUnmodifiedSince, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.updateSubscriptionStatus(subscriptionId, APIMgtConstants.SubscriptionStatus.ACTIVE);
            Subscription newSubscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(newSubscription);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            String msg = "Error while unblocking the subscription " + subscriptionId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        }
        return null;
    }
}
