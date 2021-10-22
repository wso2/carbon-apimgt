package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.LoggingMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;

import org.wso2.carbon.apimgt.internal.service.ApiLoggingConfigsApiService;
import org.wso2.carbon.apimgt.internal.service.dto.APILoggingConfigDTO;
import org.wso2.carbon.apimgt.internal.service.dto.APILoggingConfigListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;

import java.util.ArrayList;
import java.util.List;


import javax.ws.rs.core.Response;

/**
 * API logging internal service implementation.
 */
public class ApiLoggingConfigsApiServiceImpl implements ApiLoggingConfigsApiService {

    private static final Log log = LogFactory.getLog(ApiLoggingConfigsApiServiceImpl.class);

    public Response apiLoggingConfigsGet(MessageContext messageContext) {
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode(200);
        errorObject.setMessage(status.toString());

        List<APILoggingConfigDTO> loggingAPIDTOList = new ArrayList<>();
        try {
            List<APILogInfoDTO> apiLoggerList = LoggingMgtDAO.getInstance().retrieveAllAPILoggerList();
            for (APILogInfoDTO apiLogInfo : apiLoggerList) {
                APILoggingConfigDTO apiLoggingConfigDTO = new APILoggingConfigDTO();
                apiLoggingConfigDTO.setContext(apiLogInfo.getContext());
                apiLoggingConfigDTO.setLogLevel(apiLogInfo.getLogLevel());
                loggingAPIDTOList.add(apiLoggingConfigDTO);
            }
        } catch (APIManagementException e) {
            log.error("Error while retrieving api logger list");
        }
        APILoggingConfigListDTO apiLoggingConfigListDTO = new APILoggingConfigListDTO();
        apiLoggingConfigListDTO.apis(loggingAPIDTOList);
        return Response.ok().entity(apiLoggingConfigListDTO).build();
    }
}
