package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.EnvironmentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.Response;


public class EnvironmentsApiServiceImpl implements EnvironmentsApiService {

    private static final Log log = LogFactory.getLog(EnvironmentsApiServiceImpl.class);

    /**
     * Delete gateway envirionment
     *
     * @param environmentId environment ID
     * @param messageContext message context
     * @return 200 with empty response body
     * @throws APIManagementException if failed to delete
     */
    public Response environmentsEnvironmentIdDelete(String environmentId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        apiAdmin.deleteEnvironment(tenantDomain, environmentId);
        return Response.ok().build();
    }

    /**
     * Update gateway environment
     *
     * @param environmentId environment ID
     * @param body environment to be updated
     * @param messageContext message context
     * @return updated environment
     * @throws APIManagementException if failed to update
     */
    public Response environmentsEnvironmentIdPut(String environmentId, EnvironmentDTO body, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        body.setId(environmentId);
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Environment env = EnvironmentMappingUtil.fromEnvDtoToEnv(body);
        apiAdmin.updateEnvironment(tenantDomain, env);
        URI location = null;
        try {
            location = new URI(RestApiConstants.RESOURCE_PATH_ENVIRONMENT + "/" + environmentId);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while updating Environment : " + environmentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok(location).entity(body).build();
    }

    /**
     * Get list of gateway environments from config api-manager.xml and dynamic environments (from DB)
     *
     * @param messageContext message context
     * @return created environment
     * @throws APIManagementException if failed to get list
     */
    public Response environmentsGet(MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        List<Environment> envList = apiAdmin.getAllEnvironments(tenantDomain);
        EnvironmentListDTO envListDTO = EnvironmentMappingUtil.fromEnvListToEnvListDTO(envList);
        return Response.ok().entity(envListDTO).build();
    }

    /**
     * Create a dynamic gateway environment
     * @param body environment to be created
     * @param messageContext message context
     * @return created environment
     * @throws APIManagementException if failed to create
     */
    public Response environmentsPost(EnvironmentDTO body, MessageContext messageContext) throws APIManagementException {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            Environment env = EnvironmentMappingUtil.fromEnvDtoToEnv(body);
            EnvironmentDTO envDTO = EnvironmentMappingUtil.fromEnvToEnvDTO(apiAdmin.addEnvironment(tenantDomain, env));
            URI location = new URI(RestApiConstants.RESOURCE_PATH_ENVIRONMENT + "/" + envDTO.getId());
            return Response.created(location).entity(envDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding gateway environment : " + body.getName() + "-" + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
