package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.ResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.core.dto.ResourcesListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class ResourcesApiServiceImpl extends ResourcesApiService {
    private APIMgtAdminService apiMgtAdminService;

    private static final Logger log = LoggerFactory.getLogger(ResourcesApiServiceImpl.class);

    public ResourcesApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    @Override
    public Response resourcesGet(String apiContext
            , String apiVersion
            , String accept
            , Request request) throws NotFoundException {
        try {
            List<UriTemplate> resourcesOfApi = new ArrayList<>();
            if (!StringUtils.isEmpty(apiContext) && !StringUtils.isEmpty(apiVersion)) {
                resourcesOfApi = apiMgtAdminService.getAllResourcesForApi(apiContext, apiVersion);
            }
            ResourcesListDTO resourcesListDTO = new ResourcesListDTO();
            resourcesListDTO.setList(MappingUtil.convertToResourceListDto(resourcesOfApi));
            return Response.ok(resourcesListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving resources.";
            Map<String, String> paramList = new HashMap<String, String>();
            if (!StringUtils.isEmpty(apiContext)) {
                paramList.put(APIMgtConstants.ExceptionsConstants.API_CONTEXT, apiContext);
            }
            if (!StringUtils.isEmpty(apiVersion)) {
                paramList.put(APIMgtConstants.ExceptionsConstants.API_VERSION, apiVersion);
            }
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler
                    (), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
