package org.wso2.carbon.apimgt.rest.api.analytics.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.rest.api.analytics.ApiApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APICountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.mappings.AnalyticsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.List;

public class ApiApiServiceImpl extends ApiApiService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationApiServiceImpl.class);

    @Override
    public Response apiApisCreatedOverTimeGet(String from, String to, String createdBy, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving APIs created over time. From: " + from + " to: " + to + " " +
                        "created by: " + createdBy);
            }
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            List<APICount> apiCountList = analyzer.getAPICount(createdBy, from, to);
            APICountListDTO apiCountListDTO = AnalyticsMappingUtil
                    .fromAPICountToListDTO(apiCountList);
            return Response.ok().entity(apiCountListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API created over time info";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response apiApiInfoGet(String from, String to, String createdBy, String apiFilter, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving API information. From: " + from + " to: " + to + " created by: " + createdBy);
            }
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            List<APIInfo> apiInfoList = analyzer.getAPIInfo(createdBy, from, to);
            APIInfoListDTO apiInfoListDTO = AnalyticsMappingUtil
                    .fromAPIInfoToListDTO(apiInfoList);
            return Response.ok().entity(apiInfoListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API information";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
