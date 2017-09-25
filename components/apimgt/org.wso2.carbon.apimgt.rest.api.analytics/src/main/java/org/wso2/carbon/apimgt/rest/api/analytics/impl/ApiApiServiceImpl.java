package org.wso2.carbon.apimgt.rest.api.analytics.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.rest.api.analytics.ApiApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APICountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APISubscriptionCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.mappings.AnalyticsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.util.List;

import static org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil.fromISO8601ToInstant;

public class ApiApiServiceImpl extends ApiApiService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationApiServiceImpl.class);

    /**
     * Get list of API Info
     *
     * @param startTime Filter for start time stamp
     * @param endTime   Filter for end time stamp
     * @param createdBy Filter for created user
     * @param request   MSF4J request
     * @return API List
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apiListGet(String startTime, String endTime, String createdBy, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            log.debug("Retrieving API information. [From: {} To: {} Created By:{} ]", startTime, endTime, createdBy);
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            ZoneId requestTimezone = RestApiUtil.getRequestTimeZone(startTime);
            List<APIInfo> apiInfoList = analyzer.getAPIInfo(fromISO8601ToInstant(startTime), fromISO8601ToInstant
                    (endTime), createdBy);
            APIInfoListDTO apiInfoListDTO = AnalyticsMappingUtil
                    .fromAPIInfoListToDTO(apiInfoList, requestTimezone);
            return Response.ok().entity(apiInfoListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API information";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Get list of API count information
     *
     * @param startTime Filter for start time stamp
     * @param endTime   Filter for end time stamp
     * @param createdBy Filter for created user
     * @param request   MSF4J request
     * @return API Count information
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apiCountOverTimeGet(String startTime, String endTime, String createdBy, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            log.debug("Retrieving APIs created over time. [From: {}  To: {} Created By: {}]", startTime, endTime,
                    createdBy);
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            ZoneId requestTimezone = RestApiUtil.getRequestTimeZone(startTime);
            List<APICount> apiCountList = analyzer.getAPICount(fromISO8601ToInstant(startTime),
                    fromISO8601ToInstant(endTime), createdBy);
            APICountListDTO apiCountListDTO = AnalyticsMappingUtil.fromAPICountToListDTO(apiCountList, requestTimezone);
            return Response.ok().entity(apiCountListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API created over time info";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Get list of subscriptions for an API
     *
     * @param startTime Filter for start time stamp
     * @param endTime   Filter for end time stamp
     * @param apiId     Filter for apiId
     * @param request   MSF4J request
     * @return API subscriptions count
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apiSubscriberCountByApiGet(String startTime, String endTime, String apiId, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            log.debug("Retrieving APIs created over time. [From: {} To: {} API Id: {}]");
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            List<APISubscriptionCount> apiSubscriptionCountList = analyzer.getAPISubscriptionCount
                    (fromISO8601ToInstant(startTime), fromISO8601ToInstant(endTime), apiId);
            APISubscriptionCountListDTO apiSubscriptionListDTO = AnalyticsMappingUtil
                    .fromAPISubscriptionCountListToDTO(apiSubscriptionCountList);
            return Response.ok().entity(apiSubscriptionListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API subscription info";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

}
