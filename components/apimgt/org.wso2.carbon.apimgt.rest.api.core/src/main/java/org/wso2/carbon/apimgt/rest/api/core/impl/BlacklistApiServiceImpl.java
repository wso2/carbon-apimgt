package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.BlacklistApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Service for retrieving black list
 *
 */
public class BlacklistApiServiceImpl extends BlacklistApiService {
    private APIMgtAdminService apiMgtAdminService;
    private static final Logger log = LoggerFactory.getLogger(BlacklistApiServiceImpl.class);

    public BlacklistApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    @Override
    public Response blacklistGet(String accept, Request request) throws NotFoundException {
        try {
            List<BlockConditions> blockConditionsList = apiMgtAdminService.getBlockConditions();
            BlockingConditionListDTO blockingConditionListDTO = MappingUtil.fromBlockConditionListToListDTO
                    (blockConditionsList);
            return Response.ok(blockingConditionListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving block conditions";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).header(HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON).entity(errorDTO).build();
        }
    }
}
