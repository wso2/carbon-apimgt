package org.wso2.carbon.apimgt.rest.api.analytics.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;
import org.wso2.carbon.apimgt.rest.api.analytics.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.analytics.SubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.mappings.AnalyticsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.util.List;

import static org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil.fromISO8601ToInstant;


/**
 * Subscription API implementation.
 */
public class SubscriptionApiServiceImpl extends SubscriptionApiService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionApiServiceImpl.class);

    /**
     * Get list of subscriptions created over time
     *
     * @param startTime Filter for start time stamp
     * @param endTime   Filter for end time stamp
     * @param createdBy Filter for createdBy
     * @param request   MSF4J request
     * @return Subscriptions count over time
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionCountOverTimeGet(String startTime, String endTime, String createdBy, Request request)
            throws NotFoundException {

        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            log.debug("Retrieving subscriptions created over time. [From: {} To: {} Created By: {}]", startTime,
                    endTime, createdBy);
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            ZoneId requestTimezone = RestApiUtil.getRequestTimeZone(startTime);
            List<SubscriptionCount> subscriptionCount = analyzer.getSubscriptionCount(
                    fromISO8601ToInstant(startTime), fromISO8601ToInstant(endTime), createdBy);
            SubscriptionCountListDTO subscriptionListDTO = AnalyticsMappingUtil
                    .fromSubscriptionCountListToDTO(subscriptionCount, requestTimezone);
            return Response.ok().entity(subscriptionListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Subscription Count";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Get list of subscriptions info over time
     *
     * @param startTime Filter for start time stamp
     * @param endTime   Filter for end time stamp
     * @param createdBy Filter for createdBy
     * @param request   MSF4J request
     * @return Subscriptions information over time
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response subscriptionListGet(String startTime, String endTime, String createdBy, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            log.debug("Retrieving subscriptions info. [From: {}  To: {} Created By: {}]", startTime, endTime,
                    createdBy);
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            ZoneId requestTimezone = RestApiUtil.getRequestTimeZone(startTime);
            List<SubscriptionInfo> subscriptionInfoList = analyzer.getSubscriptionInfo(fromISO8601ToInstant
                    (startTime), fromISO8601ToInstant(endTime), createdBy);
            SubscriptionInfoListDTO subscriptionInfoListDTO = AnalyticsMappingUtil
                    .fromSubscriptionInfoListToDTO(subscriptionInfoList, requestTimezone);
            return Response.ok().entity(subscriptionInfoListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving subscription information";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
