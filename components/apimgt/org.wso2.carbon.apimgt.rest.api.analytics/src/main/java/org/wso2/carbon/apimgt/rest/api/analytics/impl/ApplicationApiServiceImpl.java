package org.wso2.carbon.apimgt.rest.api.analytics.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.rest.api.analytics.ApplicationApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.mappings.AnalyticsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.util.List;

import static org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil.fromISO8601ToInstant;

/**
 * Application related analytics API implementation.
 */
public class ApplicationApiServiceImpl extends ApplicationApiService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationApiServiceImpl.class);

    /**
     * Get list of Application count information
     *
     * @param startTime Filter for start time stamp
     * @param endTime   Filter for end time stamp
     * @param createdBy Filter for application creator
     * @param request   MSF4J request
     * @return Application count over time
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response applicationCountOverTimeGet(String startTime, String endTime, String createdBy, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            log.debug("Retrieving applications created over time. [From: {} to: {} Created By: {}]", startTime,
                    endTime, createdBy);
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            ZoneId requestTimezone = RestApiUtil.getRequestTimeZone(startTime);
            List<ApplicationCount> applicationCountList = analyzer
                    .getApplicationCount(fromISO8601ToInstant(startTime), fromISO8601ToInstant(endTime), createdBy);
            ApplicationCountListDTO applicationCountListDTO = AnalyticsMappingUtil
                    .fromApplicationCountToListDTO(applicationCountList, requestTimezone);
            return Response.ok().entity(applicationCountListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving application created over time info";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
