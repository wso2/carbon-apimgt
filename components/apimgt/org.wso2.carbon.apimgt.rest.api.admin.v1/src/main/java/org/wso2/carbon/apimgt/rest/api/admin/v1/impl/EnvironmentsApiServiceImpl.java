package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FederatedApiKeyConnector;
import org.wso2.carbon.apimgt.api.dto.GatewayVisibilityPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.ExternalSubscriptionPolicy;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.federated.gateway.FederatedApiKeyConnectorFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.EnvironmentsApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RemotePlanDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RemotePlanListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RemotePlanLookupRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.VHostDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.EnvironmentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.impl.utils.GatewayManagementUtils;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayInstanceDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayInstanceListDTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        apiAdmin.deleteEnvironment(organization, environmentId);
        String info = "{'id':'" + environmentId + "'}";
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.GATEWAY_ENVIRONMENTS, info,
                APIConstants.AuditLogConstants.DELETED, RestApiCommonUtil.getLoggedInUsername());
        return Response.ok().build();
    }

    @Override
    public Response environmentsEnvironmentIdGatewaysGet(String environmentId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        Environment environment = apiAdmin.getEnvironment(organization, environmentId);
        if (environment == null) {
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

        return Response.ok().entity(listDTO).build();
    }

    @Override
    public Response environmentsEnvironmentIdGet(String environmentId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        Environment environment = apiAdmin.getEnvironment(organization, environmentId);
        if (environment != null) {
            EnvironmentDTO environmentDTO = EnvironmentMappingUtil.fromEnvToEnvDTO(environment);
            return Response.ok().entity(environmentDTO).build();
        }

        // Fallback: check if it's a platform gateway ID
        PlatformGatewayService platformGatewayService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        if (platformGatewayService != null) {
            try {
                PlatformGateway gateway = platformGatewayService.getGatewayById(environmentId);
                if (gateway != null && organization.equals(gateway.getOrganizationId())) {
                    // Fetch permissions from corresponding environment if exists
                    GatewayVisibilityPermissionConfigurationDTO permissions = null;
                    Environment env = apiAdmin.getEnvironment(organization, gateway.getId());
                    if (env != null) {
                        permissions = env.getPermissions();
                    }
                    EnvironmentDTO dto = EnvironmentMappingUtil.fromPlatformGatewayToEnvDTO(
                            gateway, APIConstants.WSO2_API_PLATFORM_GATEWAY, permissions);
                    return Response.ok().entity(dto).build();
                }
            } catch (APIManagementException e) {
                log.debug("Platform gateway not found for id: " + environmentId, e);
            }
        }

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
        Environment env = EnvironmentMappingUtil.fromEnvDtoToEnv(body);
        GatewayVisibilityPermissionConfigurationDTO gatewayVisibilityPermissionConfigurationDTO =
                env.getPermissions();
        URI location = null;
        try {
        this.validatePermissions(gatewayVisibilityPermissionConfigurationDTO);
        apiAdmin.updateEnvironment(organization, env);
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
     * Get list of gateway environments (non–platform-gateway only).
     * Returns environments from config and DB excluding Platform gateway type.
     * Platform gateways are loaded via GET /gateways; the UI should call both endpoints and combine as needed.
     *
     * @param messageContext message context
     * @return list of environments (Regular, APK, etc.; no platform gateways)
     * @throws APIManagementException if failed to get list
     */
    public Response environmentsGet(MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        List<Environment> envList = apiAdmin.getAllEnvironments(organization);
        List<Environment> envListFiltered = envList.stream()
                .filter(env -> !APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType()))
                .collect(Collectors.toList());
        EnvironmentListDTO envListDTO = EnvironmentMappingUtil.fromEnvListToEnvListDTO(envListFiltered);

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

    @Override
    public Response getEnvironmentRemotePlans(RemotePlanLookupRequestDTO request, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        if (request == null) {
            throw new APIManagementException("Remote plan lookup request is required",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        final boolean hasEnvironmentId = StringUtils.isNotBlank(request.getEnvironmentId());
        final boolean hasEnvironmentPayload = request.getEnvironment() != null;
        if (!hasEnvironmentId && !hasEnvironmentPayload) {
            throw new APIManagementException("Provide environmentId, environment payload, or both for remote plan lookup",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        Environment environment;
        boolean transientLookup = false;
        APIAdminImpl apiAdmin = new APIAdminImpl();
        Environment persistedEnvironment = null;
        if (hasEnvironmentId) {
            persistedEnvironment = apiAdmin.getEnvironmentWithoutPropertyMasking(organization, request.getEnvironmentId());
            if (persistedEnvironment == null) {
                throw new APIManagementException("Requested Gateway Environment not found",
                        ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND);
            }
            persistedEnvironment = apiAdmin.decryptGatewayConfigurationValues(persistedEnvironment);
        }

        if (hasEnvironmentPayload) {
            EnvironmentDTO environmentDTO = request.getEnvironment();
            environment = EnvironmentMappingUtil.fromEnvDtoToEnv(environmentDTO);
            validateRemotePlanLookupEnvironment(environmentDTO, environment);
            transientLookup = true;

            if (persistedEnvironment != null) {
                mergeMaskedGatewayConfigurationValues(environment, persistedEnvironment);
            }
        } else {
            environment = persistedEnvironment;
        }
        FederatedApiKeyConnector connector = transientLookup
                ? FederatedApiKeyConnectorFactory.getTransientApiKeyConnector(environment, organization)
                : FederatedApiKeyConnectorFactory.getApiKeyConnector(environment, organization);
        ensureRemotePlanListingSupported(connector);
        List<ExternalSubscriptionPolicy> rateLimitPolicies = connector.listRateLimitPolicies(environment);
        return Response.ok().entity(buildRemotePlanListDTO(rateLimitPolicies)).build();
    }

    private void validateRemotePlanLookupEnvironment(EnvironmentDTO environmentDTO, Environment environment)
            throws APIManagementException {
        List<String> gatewayTypes = APIUtil.getGatewayTypes();
        if (!gatewayTypes.contains(environment.getGatewayType())) {
            throw new APIManagementException("Invalid gateway type: " + environment.getGatewayType(),
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        if (APIConstants.API_GATEWAY_TYPE_APK.equals(environment.getGatewayType())
                && hasUnsupportedVhostConfiguration(environmentDTO.getVhosts())) {
            throw new APIManagementException("Unsupported Vhost Configuration for gateway type: "
                    + environment.getGatewayType(), ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
    }

    private void ensureRemotePlanListingSupported(FederatedApiKeyConnector connector) {
        if (connector != null && connector.supportsRemotePlanListing()) {
            return;
        }
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(501L);
        errorDTO.setMessage("Not Implemented");
        errorDTO.setDescription("The gateway type does not support listing remote plans.");
        throw new javax.ws.rs.WebApplicationException(Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(errorDTO).build());
    }

    private void mergeMaskedGatewayConfigurationValues(Environment draftEnvironment, Environment persistedEnvironment) {
        GatewayAgentConfiguration gatewayConfiguration = ServiceReferenceHolder.getInstance()
                .getExternalGatewayConnectorConfiguration(draftEnvironment.getGatewayType());
        if (gatewayConfiguration == null) {
            return;
        }

        Map<String, String> draftProperties = draftEnvironment.getAdditionalProperties();
        if (draftProperties == null) {
            draftProperties = new HashMap<>();
            draftEnvironment.setAdditionalProperties(draftProperties);
        }
        Map<String, String> persistedProperties = persistedEnvironment.getAdditionalProperties();
        if (persistedProperties == null || persistedProperties.isEmpty()) {
            return;
        }

        List<ConfigurationDto> connectionConfigurations = gatewayConfiguration.getConnectionConfigurations();
        if (connectionConfigurations == null || connectionConfigurations.isEmpty()) {
            return;
        }

        for (ConfigurationDto configurationDto : connectionConfigurations) {
            mergeMaskedGatewayConfigurationValue(configurationDto, draftProperties, persistedProperties);
        }
    }

    private void mergeMaskedGatewayConfigurationValue(ConfigurationDto configurationDto,
            Map<String, String> draftProperties, Map<String, String> persistedProperties) {
        if (configurationDto.isMask()) {
            String draftValue = draftProperties.get(configurationDto.getName());
            if (APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD.equals(draftValue)
                    && persistedProperties.containsKey(configurationDto.getName())) {
                draftProperties.put(configurationDto.getName(), persistedProperties.get(configurationDto.getName()));
            }
        }

        List<Object> nestedConfigurationValues = configurationDto.getValues();
        if (nestedConfigurationValues == null || nestedConfigurationValues.isEmpty()) {
            return;
        }

        for (Object nestedConfiguration : nestedConfigurationValues) {
            if (nestedConfiguration instanceof ConfigurationDto) {
                mergeMaskedGatewayConfigurationValue((ConfigurationDto) nestedConfiguration, draftProperties,
                        persistedProperties);
            }
        }
    }

    private RemotePlanListDTO buildRemotePlanListDTO(List<ExternalSubscriptionPolicy> rateLimitPolicies) {
        List<RemotePlanDTO> planDTOs = new ArrayList<>();
        if (rateLimitPolicies == null) {
            rateLimitPolicies = new ArrayList<>();
        }
        for (ExternalSubscriptionPolicy policy : rateLimitPolicies) {
            RemotePlanDTO dto = new RemotePlanDTO();
            dto.setId(policy.getId());
            dto.setName(policy.getName());
            dto.setDescription(policy.getDescription());
            dto.setLimits(policy.getLimits());
            planDTOs.add(dto);
        }
        RemotePlanListDTO listDTO = new RemotePlanListDTO();
        listDTO.setCount(planDTOs.size());
        listDTO.setList(planDTOs);
        return listDTO;
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
