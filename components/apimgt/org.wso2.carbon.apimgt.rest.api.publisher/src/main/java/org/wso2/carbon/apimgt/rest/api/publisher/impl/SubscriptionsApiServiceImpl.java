package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

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
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Subscription subscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            if (subscription == null) {
                String errorMessage = "Subscription not found : " + subscriptionId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.SUBSCRIPTION_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.SUBSCRIPTION_ID, subscriptionId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            } else if (subscription.getStatus().equals(APIMgtConstants.SubscriptionStatus.REJECTED)
                    || subscription.getStatus().equals(APIMgtConstants.SubscriptionStatus.ON_HOLD)) {
                String errorMessage = "Cannot update subcription from " + subscription.getStatus() + "to " +
                        blockState;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.SUBSCRIPTION_STATE_INVALID);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }
            apiPublisher.updateSubscriptionStatus(subscriptionId, APIMgtConstants.SubscriptionStatus.valueOf
                    (blockState));
            Subscription newSubscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(newSubscription);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (GatewayException e) {
            String errorMessage = "Failed to block subscription :" + subscriptionId + " in gateway";
            log.error(errorMessage, e);
            return Response.status(Response.Status.ACCEPTED).build();
        }
        catch (APIManagementException e) {
            String errorMessage = "Error while blocking the subscription " + subscriptionId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.SUBSCRIPTION_ID, subscriptionId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieve all subscriptions for a particular API
     *
     * @param apiId       ID of the API
     * @param limit       Maximum subscriptions to return
     * @param offset      Starting position of the pagination
     * @param ifNoneMatch If-Match header value
     * @param request     ms4j request object
     * @return List of qualifying subscriptions DTOs as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch,
            Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
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
            String errorMessage = "Error while retrieving subscriptions of API " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return null;
    }

    /**
     * Retrieves a single subscription
     *
     * @param subscriptionId  ID of the subscription
     * @param ifNoneMatch     If-Match header value
     * @param ifModifiedSince If-Modified-Since value
     * @param request         ms4j request object
     * @return Requested subscription details
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = subscriptionsSubscriptionIdGetFingerprint(subscriptionId, ifNoneMatch,
                    ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            Subscription subscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            if (subscription == null) {
                String errorMessage = "Subscription not found : " + subscriptionId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.SUBSCRIPTION_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.SUBSCRIPTION_ID, subscriptionId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(subscription);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(subscriptionDTO)
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while getting the subscription " + subscriptionId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.SUBSCRIPTION_ID, subscriptionId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieve the fingerprint of the subscription
     *
     * @param subscriptionId  ID of the subscription
     * @param ifNoneMatch     If-Match header value
     * @param ifModifiedSince If-Modified-Since value
     * @param request         ms4j request object
     * @return Fingerprint of the subscription
     */
    public String subscriptionsSubscriptionIdGetFingerprint(String subscriptionId, String ifNoneMatch,
            String ifModifiedSince, Request request) {
        String username = RestApiUtil.getLoggedInUsername(request);
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
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Subscription subscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            if (subscription == null) {
                String errorMessage = "Subscription not found : " + subscriptionId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.SUBSCRIPTION_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.SUBSCRIPTION_ID, subscriptionId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            } else if (subscription.getStatus().equals(APIMgtConstants.SubscriptionStatus.REJECTED)
                    || subscription.getStatus().equals(APIMgtConstants.SubscriptionStatus.ON_HOLD)) {
                String errorMessage = "Cannot update subcription from " + subscription.getStatus() + "to " +
                        APIMgtConstants.SubscriptionStatus.ACTIVE;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.SUBSCRIPTION_STATE_INVALID);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }
            apiPublisher.updateSubscriptionStatus(subscriptionId, APIMgtConstants.SubscriptionStatus.ACTIVE);
            Subscription newSubscription = apiPublisher.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(newSubscription);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (GatewayException e) {
            String errorMessage = "Failed to unblock subscription :" + subscriptionId + " in gateway";
            log.error(errorMessage, e);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while unblocking the subscription " + subscriptionId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.SUBSCRIPTION_ID, subscriptionId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
