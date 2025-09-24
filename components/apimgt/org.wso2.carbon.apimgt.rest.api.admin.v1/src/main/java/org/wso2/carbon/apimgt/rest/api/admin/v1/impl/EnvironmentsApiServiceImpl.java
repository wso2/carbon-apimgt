package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.GatewayVisibilityPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.EnvironmentsApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.VHostDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.EnvironmentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.impl.utils.GatewayManagementUtils;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayInstanceDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayInstanceListDTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;


public class EnvironmentsApiServiceImpl implements EnvironmentsApiService {

    private static final Log log = LogFactory.getLog(EnvironmentsApiServiceImpl.class);

    /**
     * Delete gateway environment
     *
     * @param environmentId  environment ID
     * @param messageContext message context
     * @return 200 with empty response body
     * @throws APIManagementException if failed to delete
     */
    public Response environmentsEnvironmentIdDelete(String environmentId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        //String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        log.info("Deleting gateway environment: " + environmentId + " from organization: " + organization);
        if (apiAdmin.hasExistingDeployments(organization, environmentId)) {
            log.warn("Cannot delete environment " + environmentId + " due to active deployments");
            RestApiUtil.handleConflict("Cannot delete the environment with id: " + environmentId
                    + " as active gateway policy deployment exist", log);
        }
        apiAdmin.deleteEnvironment(organization, environmentId);
        log.info("Successfully deleted environment: " + environmentId);
        String info = "{'id':'" + environmentId + "'}";
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.GATEWAY_ENVIRONMENTS, info,
                APIConstants.AuditLogConstants.DELETED, RestApiCommonUtil.getLoggedInUsername());
        return Response.ok().build();
    }

    @Override
    public Response environmentsEnvironmentIdGatewaysGet(String environmentId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving gateway instances for environment: " + environmentId);
        }
        Environment environment = apiAdmin.getEnvironment(organization, environmentId);
        if (environment == null) {
            log.warn("Gateway environment not found: " + environmentId);
            throw new APIManagementException("Requested Gateway Environment not found",
                    ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND);
        }

        GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
        List<GatewayManagementDAO.GatewayInstanceInfo> instances =
                dao.getGatewayInstancesByEnvironment(environment.getName(), organization);

        List<GatewayInstanceDTO> gatewayList = new ArrayList<>();
        for (GatewayManagementDAO.GatewayInstanceInfo info : instances) {
            GatewayInstanceDTO dto = new GatewayInstanceDTO();
            dto.setGatewayId(info.gatewayId);
            dto.setLastActive(info.lastUpdated.toInstant().toString());
            String status = GatewayManagementUtils.validateGatewayStatus(info.lastUpdated);
            dto.setStatus(GatewayInstanceDTO.StatusEnum.fromValue(status));
            gatewayList.add(dto);
        }
        GatewayInstanceListDTO listDTO = new GatewayInstanceListDTO();
        listDTO.setCount(gatewayList.size());
        listDTO.setList(gatewayList);
        if (log.isDebugEnabled()) {
            log.debug("Successfully retrieved " + gatewayList.size() + " gateway instances for environment: " + environmentId);
        }
        return Response.ok().entity(listDTO).build();
    }

    @Override
    public Response environmentsEnvironmentIdGet(String environmentId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving gateway environment: " + environmentId);
        }
        Environment environment = apiAdmin.getEnvironment(organization, environmentId);
        if (environment != null) {
            EnvironmentDTO environmentDTO = EnvironmentMappingUtil.fromEnvToEnvDTO(environment);
            return Response.ok().entity(environmentDTO).build();
        }
        log.warn("Gateway environment not found: " + environmentId);
        throw new APIManagementException("Requested Gateway Environment not found",
                ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND);
    }

    /**
     * Update gateway environment
     *
     * @param environmentId  environment ID
     * @param body           environment to be updated
     * @param messageContext message context
     * @return updated environment
     * @throws APIManagementException if failed to update
     */
    public Response environmentsEnvironmentIdPut(String environmentId, EnvironmentDTO body, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        body.setId(environmentId);
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String envName = body.getName() != null ? body.getName() : "null";
        log.info("Updating gateway environment: " + envName + " with ID: " + environmentId);
        Environment env = EnvironmentMappingUtil.fromEnvDtoToEnv(body);
        GatewayVisibilityPermissionConfigurationDTO gatewayVisibilityPermissionConfigurationDTO =
                env.getPermissions();
        URI location = null;
        try {
        this.validatePermissions(gatewayVisibilityPermissionConfigurationDTO);
        apiAdmin.updateEnvironment(organization, env);
        log.info("Successfully updated gateway environment: " + envName);
        APIUtil.validateAndScheduleFederatedGatewayAPIDiscovery(env, organization);
        location = new URI(RestApiConstants.RESOURCE_PATH_ENVIRONMENT + "/" + environmentId);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while updating Environment : " + environmentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (IllegalArgumentException e) {
            String error = "Error while storing gateway visibility permissions with name "
                    + body.getName() + " in tenant " + organization;
            throw new APIManagementException(error, e, ExceptionCodes.ROLE_DOES_NOT_EXIST);
        }
        String info = "{'id':'" + environmentId + "'}";
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.GATEWAY_ENVIRONMENTS, info,
                APIConstants.AuditLogConstants.UPDATED, RestApiCommonUtil.getLoggedInUsername());
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
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        log.info("Retrieving all gateway environments for organization: " + organization);
        List<Environment> envList = apiAdmin.getAllEnvironments(organization);
        EnvironmentListDTO envListDTO = EnvironmentMappingUtil.fromEnvListToEnvListDTO(envList);
        if (log.isDebugEnabled()) {
            log.debug("Successfully retrieved " + envList.size() + " gateway environments");
        }
        return Response.ok().entity(envListDTO).build();
    }

    /**
     * Create a dynamic gateway environment
     *
     * @param body           environment to be created
     * @param messageContext message context
     * @return created environment
     * @throws APIManagementException if failed to create
     */
    public Response environmentsPost(EnvironmentDTO body, MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String envName = body.getName() != null ? body.getName() : "null";
        log.info("Creating new gateway environment: " + envName + " for organization: " + organization);
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String gatewayType = body.getGatewayType();

            List<String> gatewayTypes = APIUtil.getGatewayTypes();
            if (!gatewayTypes.contains(gatewayType)) {
                throw new APIManagementException("Invalid gateway type: " + gatewayType);
            }

            if (APIConstants.API_GATEWAY_TYPE_APK.equals(gatewayType) && hasUnsupportedVhostConfiguration(body.getVhosts())) {
                throw new APIManagementException("Unsupported Vhost Configuration for gateway type: " + gatewayType);
            }
            Environment env = EnvironmentMappingUtil.fromEnvDtoToEnv(body);
            GatewayVisibilityPermissionConfigurationDTO gatewayVisibilityPermissionConfigurationDTO =
                    env.getPermissions();
            validatePermissions(gatewayVisibilityPermissionConfigurationDTO);
            Environment addedEnv = apiAdmin.addEnvironment(organization, env);
            EnvironmentDTO envDTO = EnvironmentMappingUtil.fromEnvToEnvDTO(addedEnv);
            log.info("Successfully created gateway environment: " + envName + " with ID: " + addedEnv.getId());
            APIUtil.validateAndScheduleFederatedGatewayAPIDiscovery(addedEnv, organization);
            URI location = new URI(RestApiConstants.RESOURCE_PATH_ENVIRONMENT + "/" + envDTO.getId());
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.GATEWAY_ENVIRONMENTS, new Gson().toJson(envDTO),
                    APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
            return Response.created(location).entity(envDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding gateway environment : " + body.getName() + "-" + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (IllegalArgumentException e) {
            String error = "Error while storing gateway visibility permission roles with name "
                    + body.getName() + " in tenant " + organization;
            throw new APIManagementException(error, e, ExceptionCodes.ROLE_DOES_NOT_EXIST);
        }
        return null;
    }

    private void validatePermissions(GatewayVisibilityPermissionConfigurationDTO permissionDTO)
            throws IllegalArgumentException, APIManagementException {

        if (permissionDTO != null && permissionDTO.getRoles() != null) {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String[] allowedPermissionTypes = {"PUBLIC", "ALLOW", "DENY"};
            String permissionType = permissionDTO.getPermissionType();
            if (!Arrays.stream(allowedPermissionTypes).anyMatch(permissionType::equals)) {
                throw new APIManagementException("Invalid permission type");
            }
            List<String> invalidRoles = new ArrayList<>();
            for (String role : permissionDTO.getRoles()) {
                if (!APIUtil.isRoleNameExist(username, role)) {
                    invalidRoles.add(role);
                }
            }
            if (!invalidRoles.isEmpty()) {
                throw new APIManagementException("Invalid user roles found in visibleRoles list");
            }
        }
    }

    /**
     * Check whether the vhost configuration is supported for APK gateway type
     *
     * @param vhosts
     * @return boolean
     */
    private boolean hasUnsupportedVhostConfiguration(List<VHostDTO> vhosts) {
        if (vhosts != null && !vhosts.isEmpty()) {
            for (VHostDTO vhost : vhosts) {
                if (hasUnsupportedConfiguration(vhost)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the vhost configuration is supported for APK gateway type
     *
     * @param vhost
     * @return boolean
     */
    private boolean hasUnsupportedConfiguration(VHostDTO vhost) {
        return vhost.getWsHost() != null || vhost.getWssHost() != null || vhost.getWsPort() != null || vhost.getWssPort() != null;
    }
}
