/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.message.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.ThrottlingCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.utils.RestApiAdminUtils;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.utils.mappings.throttling.BlockingConditionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.utils.mappings.throttling.SubscriptionThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionDTO.ConditionTypeEnum;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * This is the service implementation class for Admin Portal Throttling related operations
 */
public class ThrottlingApiServiceImpl implements ThrottlingApiService {

    private static final Log log = LogFactory.getLog(ThrottlingApiServiceImpl.class);
    private static final String ALL_TYPES = "all";

    /**
     * Retrieves all Advanced level policies
     *
     * @param accept Accept header value
     * @return All matched Advanced Throttle policies to the given request
     * @throws APIManagementException When an internal error occurs
     */
    @Override
    public Response getAllAdvancedPolicy(String accept, MessageContext messageContext) throws APIManagementException {
        AdvancedThrottlePolicyListDTO listDTO = ThrottlingCommonImpl.getAllAdvancedPolicy();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add an Advanced Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response addAdvancedPolicy(String contentType, AdvancedThrottlePolicyDTO body,
                                      MessageContext messageContext) throws APIManagementException {
        try {
            AdvancedThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.addAdvancedPolicy(body);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_ADVANCED
                    + RestApiConstants.PATH_DELIMITER
                    + policyDTO.getPolicyId())).entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Advanced Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get a specific Advanced Level Policy
     *
     * @param policyId uuid of the policy
     * @return Required policy specified by name
     */
    @Override
    public Response getAdvancedPolicyById(String policyId, MessageContext messageContext)
            throws APIManagementException {
        AdvancedThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.getAdvancedPolicyById(policyId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Advanced level policy specified by uuid
     *
     * @param policyId    uuid of the policy
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateAdvancedPolicy(String policyId, String contentType,
                                         AdvancedThrottlePolicyDTO body, MessageContext messageContext)
            throws APIManagementException {
        AdvancedThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.updateAdvancedPolicy(policyId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete an Advanced level policy specified by uuid
     *
     * @param policyId uuid of the policy
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeAdvancedPolicy(String policyId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ThrottlingCommonImpl.removeAdvancedPolicy(policyId, organization);
        return Response.ok().build();
    }

    /**
     * Retrieves all Application Throttle Policies
     *
     * @param accept Accept header value
     * @return Retrieves all Application Throttle Policies
     */
    @Override
    public Response getApplicationThrottlePolicies(String accept, MessageContext messageContext)
            throws APIManagementException {
        ApplicationThrottlePolicyListDTO listDTO = ThrottlingCommonImpl.getApplicationThrottlePolicies();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add an Application Level Throttle Policy
     *
     * @param body        DTO of the Application Policy to add
     * @param contentType Content-Type header
     * @return Newly created Application Throttle Policy with the location with the Location header
     */
    @Override
    public Response addApplicationThrottlePolicy(String contentType, ApplicationThrottlePolicyDTO body,
                                                 MessageContext messageContext) throws APIManagementException {
        try {
            ApplicationThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.addApplicationThrottlePolicy(body);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_APPLICATION
                    + RestApiConstants.PATH_DELIMITER
                    + policyDTO.getPolicyId())).entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving Application Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get a specific Application Policy by its uuid
     *
     * @param policyId uuid of the policy
     * @return Matched Application Throttle Policy by the given name
     */
    @Override
    public Response getApplicationThrottlePolicyById(String policyId, MessageContext messageContext)
            throws APIManagementException {
        ApplicationThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.getApplicationThrottlePolicyById(policyId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Application level policy specified by uuid
     *
     * @param policyId    uuid of the policy
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateApplicationThrottlePolicy(String policyId, String contentType,
                                                    ApplicationThrottlePolicyDTO body, MessageContext messageContext)
            throws APIManagementException {

        ApplicationThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.updateApplicationThrottlePolicy(policyId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete an Application level policy specified by uuid
     *
     * @param policyId uuid of the policy
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeApplicationThrottlePolicy(String policyId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ThrottlingCommonImpl.removeApplicationThrottlePolicy(policyId, organization);
        return Response.ok().build();
    }

    /**
     * Retrieves all Subscription level policies
     *
     * @param accept Accept header value
     * @return All matched Subscription Throttle policies to the given request
     */
    @Override
    public Response getAllSubscriptionThrottlePolicies(String accept, MessageContext messageContext)
            throws APIManagementException {
        SubscriptionThrottlePolicyListDTO listDTO = ThrottlingCommonImpl.getAllSubscriptionThrottlePolicies();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add a Subscription Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response addSubscriptionThrottlePolicy(String contentType, SubscriptionThrottlePolicyDTO body,
                                                  MessageContext messageContext) throws APIManagementException {
        SubscriptionThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.addSubscriptionThrottlePolicy(body);
        try {
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_SUBSCRIPTION
                    + RestApiConstants.PATH_DELIMITER
                    + policyDTO.getPolicyId())).entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving Subscription Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Returns list of throttling policy details filtered using query parameters
     *
     * @param query filtering parameters
     * @return Throttle Policies List filtered using query
     */
    @Override
    public Response throttlingPolicySearch(String query, MessageContext messageContext)
            throws APIManagementException {
        ThrottlePolicyDetailsListDTO resultListDTO = ThrottlingCommonImpl.throttlingPolicySearch(query);
        return Response.ok().entity(resultListDTO).build();
    }

    /**
     * Validates the permission element of the subscription throttle policy
     *
     * @param body subscription throttle policy
     * @throws APIManagementException when there are validation errors
     */
    private void validatePolicyPermissions(SubscriptionThrottlePolicyDTO body) throws APIManagementException {
        SubscriptionThrottlePolicyPermissionDTO policyPermissions = body.getPermissions();
        if (policyPermissions != null && policyPermissions.getRoles().size() == 0) {
            throw new APIManagementException(ExceptionCodes.ROLES_CANNOT_BE_EMPTY);
        }
    }

    /**
     * Update APIM with the subscription throttle policy permission
     *
     * @param body subscription throttle policy
     * @throws APIManagementException when there are validation errors or error while updating the permissions
     */
    private void updatePolicyPermissions(SubscriptionThrottlePolicyDTO body) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        SubscriptionThrottlePolicyPermissionDTO policyPermissions = body.getPermissions();
        if (policyPermissions != null) {
            if (policyPermissions.getRoles().size() > 0) {
                String roles = StringUtils.join(policyPermissions.getRoles(), ",");
                String permissionType;
                if (policyPermissions.getPermissionType() ==
                        SubscriptionThrottlePolicyPermissionDTO.PermissionTypeEnum.ALLOW) {
                    permissionType = APIConstants.TIER_PERMISSION_ALLOW;
                } else {
                    permissionType = APIConstants.TIER_PERMISSION_DENY;
                }
                apiProvider.updateThrottleTierPermissions(body.getPolicyName(), permissionType, roles);
            } else {
                throw new APIManagementException(ExceptionCodes.ROLES_CANNOT_BE_EMPTY);
            }
        } else {
            apiProvider.deleteTierPermissions(body.getPolicyName());
        }
    }

    /**
     * Set subscription throttle policy permission info into the DTO
     *
     * @param policyDTO subscription throttle policy DTO
     * @throws APIManagementException error while setting/retrieve the permissions to the DTO
     */
    private void setPolicyPermissionsToDTO(SubscriptionThrottlePolicyDTO policyDTO) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        TierPermissionDTO addedPolicyPermission =
                (TierPermissionDTO) apiProvider.getThrottleTierPermission(policyDTO.getPolicyName());
        if (addedPolicyPermission != null) {
            SubscriptionThrottlePolicyPermissionDTO addedPolicyPermissionDTO =
                    SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyPermissionToDTO(addedPolicyPermission);
            policyDTO.setPermissions(addedPolicyPermissionDTO);
        }
    }

    /**
     * Get a specific Subscription Policy by its uuid
     *
     * @param policyId uuid of the policy
     * @return Matched Subscription Throttle Policy by the given name
     */
    @Override
    public Response getSubscriptionThrottlePolicyById(String policyId, MessageContext messageContext)
            throws APIManagementException {
        SubscriptionThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.getSubscriptionThrottlePolicyById(policyId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Subscription level policy specified by uuid
     *
     * @param policyId    u
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateSubscriptionThrottlePolicy(String policyId, String contentType,
                                                     SubscriptionThrottlePolicyDTO body, MessageContext messageContext)
            throws APIManagementException {
        SubscriptionThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.updateSubscriptionThrottlePolicy(policyId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete a Subscription level policy specified by uuid
     *
     * @param policyId uuid of the policyu
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeSubscriptionThrottlePolicy(String policyId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ThrottlingCommonImpl.removeSubscriptionThrottlePolicy(policyId, organization);
        return Response.ok().build();
    }

    /**
     * Retrieves all Global level policies
     *
     * @param accept Accept header value
     * @return All matched Global Throttle policies to the given request
     */
    @Override
    public Response getAllCustomRoles(String accept, MessageContext messageContext) throws APIManagementException {
        CustomRuleListDTO listDTO = ThrottlingCommonImpl.getAllCustomRules();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add an Global Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response addCustomRule(String contentType, CustomRuleDTO body, MessageContext messageContext)
            throws APIManagementException {

        RestApiAdminUtils
                .validateCustomRuleRequiredProperties(body, (String) messageContext.get(Message.HTTP_REQUEST_METHOD));

        try {
            CustomRuleDTO policyDTO = ThrottlingCommonImpl.addCustomRule(body);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_GLOBAL
                            + RestApiConstants.PATH_DELIMITER + policyDTO.getPolicyId()))
                    .entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Global Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get a specific custom rule by its name
     *
     * @param ruleId uuid of the policy
     * @return Matched Global Throttle Policy by the given name
     */
    @Override
    public Response getCustomRuleById(String ruleId, MessageContext messageContext) throws APIManagementException {
        CustomRuleDTO policyDTO = ThrottlingCommonImpl.getCustomRuleById(ruleId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Global level policy/custom rule specified by uuid
     *
     * @param ruleId      uuid of the policy
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateCustomRule(String ruleId, String contentType, CustomRuleDTO body,
                                     MessageContext messageContext) throws APIManagementException {

        RestApiAdminUtils
                .validateCustomRuleRequiredProperties(body, (String) messageContext.get(Message.HTTP_REQUEST_METHOD));
        CustomRuleDTO policyDTO = ThrottlingCommonImpl.updateCustomRule(ruleId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete a Global level policy/custom rule specified by uuid
     *
     * @param ruleId uuid of the policy
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeCustomRule(String ruleId, MessageContext messageContext) throws APIManagementException {
        ThrottlingCommonImpl.removeCustomRule(ruleId);
        return Response.ok().build();
    }

    /**
     * Export a Throttling Policy by the policy name with/without specifying the policy type
     * If policy type is not specified first found throttling policy is returned
     *
     * @param policyId   UUID of the throttling policy to be exported(for future use)
     * @param policyName Name of the policy to be exported
     * @param type       type of the policy to be exported
     * @param format     format of the policy details
     * @return Throttling Policy details in ExportThrottlePolicyDTO format
     */
    @Override
    public Response exportThrottlingPolicy(String policyId, String policyName, String type, String format,
                                           MessageContext messageContext) throws APIManagementException {
        ExportThrottlePolicyDTO exportPolicy = ThrottlingCommonImpl.exportThrottlingPolicy(policyName, type);
        return Response.ok().entity(exportPolicy).build();
    }

    /**
     * Returns the ExportThrottlePolicyDTO by reading the file from input stream
     *
     * @param uploadedInputStream Input stream from the REST request
     * @param fileDetail          Details of the file received via InputStream
     * @return ExportThrottlePolicyDTO of the file to be imported
     */
    public static ExportThrottlePolicyDTO getImportedPolicy(InputStream uploadedInputStream, Attachment fileDetail)
            throws ParseException, APIImportExportException, IOException {
        File importFolder = CommonUtil.createTempDirectory(null);
        String uploadFileName = fileDetail.getContentDisposition().getFilename();
        String fileType = (uploadFileName.contains(ImportExportConstants.YAML_EXTENSION)) ?
                ImportExportConstants.EXPORT_POLICY_TYPE_YAML :
                ImportExportConstants.EXPORT_POLICY_TYPE_JSON;
        String absolutePath = importFolder.getAbsolutePath() + File.separator + uploadFileName;
        File targetFile = new File(absolutePath);
        FileUtils.copyInputStreamToFile(uploadedInputStream, targetFile);
        return preprocessImportedArtifact(absolutePath, fileType);
    }

    /**
     * Preprocesses either yaml or json file into the ExportThrottlePolicyDTO
     *
     * @param absolutePath temporary location of the throttle policy file
     * @param fileType     Type of the file to be imported (.yaml/.json)
     * @return ExportThrottlePolicyDTO from the file
     */
    private static ExportThrottlePolicyDTO preprocessImportedArtifact(String absolutePath, String fileType)
            throws IOException, ParseException {
        ExportThrottlePolicyDTO importPolicy;
        FileReader fileReader = new FileReader(absolutePath);
        if (ImportExportConstants.EXPORT_POLICY_TYPE_YAML.equals(fileType)) {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            importPolicy = yamlMapper.readValue(fileReader, ExportThrottlePolicyDTO.class);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(fileReader);
            JSONObject jsonObject = (JSONObject) obj;
            importPolicy = mapper.convertValue(jsonObject, ExportThrottlePolicyDTO.class);
        }
        return importPolicy;
    }

    /**
     * Imports a Throttling policy with the overwriting capability
     *
     * @param fileInputStream Input stream from the REST request
     * @param fileDetail      exportThrottlePolicyDTO Exported Throttling policy details
     * @param overwrite       User can either update an existing throttling policy with the same name or let the conflict happen
     * @return Response with message indicating the status of the importation and the imported/updated policy name
     */
    @Override
    public Response importThrottlingPolicy(InputStream fileInputStream, Attachment fileDetail,
                                           Boolean overwrite, MessageContext messageContext) throws APIManagementException {
        ExportThrottlePolicyDTO exportThrottlePolicyDTO = null;
        String policyType = "";
        try {
            exportThrottlePolicyDTO = getImportedPolicy(fileInputStream, fileDetail);
        } catch (APIImportExportException | IOException | ParseException e) {
            String errorMessage = "Error retrieving Throttling policy";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        if (exportThrottlePolicyDTO != null) {
            policyType = exportThrottlePolicyDTO.getSubtype();
        } else {
            String errorMessage = "Error resolving ExportThrottlePolicyDTO";
            RestApiUtil.handleInternalServerError(errorMessage, log);
        }
        return resolveUpdateThrottlingPolicy(policyType, overwrite, exportThrottlePolicyDTO, messageContext);
    }

    /**
     * Checks if the policy exists to either update the policy or indicate the conflict or import a new policy
     *
     * @param policyType              Throttling policy type
     * @param overwrite               User can either update an existing throttling policy with the same name or let the conflict happen
     * @param exportThrottlePolicyDTO the policy to be imported
     * @return Response with  message indicating the status of the importation and the imported/updated policy name
     */
    private Response resolveUpdateThrottlingPolicy(String policyType, boolean overwrite,
                                                   ExportThrottlePolicyDTO exportThrottlePolicyDTO, MessageContext messageContext)
            throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        if (RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY.equals(policyType)) {
            SubscriptionThrottlePolicyDTO subscriptionPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(),
                    SubscriptionThrottlePolicyDTO.class);
            Policy policyIfExists = apiProvider.getSubscriptionPolicy(username, subscriptionPolicy.getPolicyName());
            if (policyIfExists != null) {
                if (overwrite) {
                    String uuid = policyIfExists.getUUID();
                    Response resp = updateSubscriptionThrottlePolicy(uuid, RestApiConstants.APPLICATION_JSON,
                            subscriptionPolicy, messageContext);
                    String message = "Successfully updated Subscription Throttling Policy : "
                            + subscriptionPolicy.getPolicyName();
                    return Response.fromResponse(resp).entity(message).build();
                } else {
                    RestApiUtil.handleResourceAlreadyExistsError(
                            "Subscription Policy with name " + subscriptionPolicy.getPolicyName() + " already exists",
                            log);
                }
            } else {
                Response resp = addSubscriptionThrottlePolicy(RestApiConstants.APPLICATION_JSON,
                        subscriptionPolicy, messageContext);
                String message =
                        "Successfully imported Subscription Throttling Policy : " + subscriptionPolicy.getPolicyName();
                return Response.fromResponse(resp).entity(message).build();
            }

        } else if (RestApiConstants.RESOURCE_APP_POLICY.equals(policyType)) {
            ApplicationThrottlePolicyDTO applicationPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(),
                    ApplicationThrottlePolicyDTO.class);
            Policy policyIfExists = apiProvider.getApplicationPolicy(username, applicationPolicy.getPolicyName());
            if (policyIfExists != null) {
                if (overwrite) {
                    String uuid = policyIfExists.getUUID();
                    Response resp = updateApplicationThrottlePolicy(uuid, RestApiConstants.APPLICATION_JSON,
                            applicationPolicy, messageContext);
                    String message = "Successfully updated Application Throttling Policy : "
                            + applicationPolicy.getPolicyName();
                    return Response.fromResponse(resp).entity(message).build();
                } else {
                    RestApiUtil.handleResourceAlreadyExistsError(
                            "Application Policy with name " + applicationPolicy.getPolicyName() + " already exists",
                            log);
                }
            } else {
                Response resp = addApplicationThrottlePolicy(RestApiConstants.APPLICATION_JSON, applicationPolicy,
                        messageContext);
                String message =
                        "Successfully imported Application Throttling Policy : " + applicationPolicy.getPolicyName();
                return Response.fromResponse(resp).entity(message).build();
            }
        } else if (RestApiConstants.RESOURCE_CUSTOM_RULE.equals(policyType)) {
            CustomRuleDTO customPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(), CustomRuleDTO.class);
            Policy policyIfExists = apiProvider.getGlobalPolicy(customPolicy.getPolicyName());
            if (policyIfExists != null) {
                if (overwrite) {
                    String uuid = policyIfExists.getUUID();
                    Response resp = updateCustomRule(uuid, RestApiConstants.APPLICATION_JSON,
                            customPolicy, messageContext);
                    String message = "Successfully updated Custom Throttling Policy : " + customPolicy.getPolicyName();
                    return Response.fromResponse(resp).entity(message).build();
                } else {
                    RestApiUtil.handleResourceAlreadyExistsError(
                            "Custom Policy with name " + customPolicy.getPolicyName() + " already exists", log);
                }
            } else {
                Response resp = addCustomRule(RestApiConstants.APPLICATION_JSON, customPolicy,
                        messageContext);
                String message = "Successfully imported Custom Throttling Policy : " + customPolicy.getPolicyName();
                return Response.fromResponse(resp).entity(message).build();
            }
        } else if (RestApiConstants.RESOURCE_ADVANCED_POLICY.equals(policyType)) {
            AdvancedThrottlePolicyDTO advancedPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(),
                    AdvancedThrottlePolicyDTO.class);
            Policy policyIfExists = apiProvider.getAPIPolicy(username, advancedPolicy.getPolicyName());
            if (policyIfExists != null) {
                if (overwrite) {
                    String uuid = policyIfExists.getUUID();
                    Response resp = updateAdvancedPolicy(uuid, RestApiConstants.APPLICATION_JSON,
                            advancedPolicy, messageContext);
                    String message =
                            "Successfully updated Advanced Throttling Policy : " + advancedPolicy.getPolicyName();
                    return Response.fromResponse(resp).entity(message).build();
                } else {
                    RestApiUtil.handleResourceAlreadyExistsError(
                            "Advanced Policy with name " + advancedPolicy.getPolicyName() + " already exists", log);
                }
            } else {
                Response resp = addAdvancedPolicy(RestApiConstants.APPLICATION_JSON, advancedPolicy,
                        messageContext);
                String message = "Successfully imported Advanced Throttling Policy : " + advancedPolicy.getPolicyName();
                return Response.fromResponse(resp).entity(message).build();
            }
        } else {
            String errorMessage = "Error with Throttling Policy Type : " + policyType;
            RestApiUtil.handleInternalServerError(errorMessage, log);
        }
        return null;
    }

    /**
     * Retrieves all Block Conditions
     *
     * @param accept Accept header value
     * @return All matched block conditions to the given request
     */
    @Override
    public Response getAllDenyPolicies(String accept, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            List<BlockConditionsDTO> blockConditions = apiProvider.getBlockConditions();
            BlockingConditionListDTO listDTO =
                    BlockingConditionMappingUtil.fromBlockConditionListToListDTO(blockConditions);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while retrieving Block Conditions";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Add a Block Condition
     *
     * @param body        DTO of new block condition to be created
     * @param contentType Content-Type header
     * @return Created block condition along with the location of it with Location header
     */
    @Override
    public Response addDenyPolicy(String contentType, BlockingConditionDTO body,
                                  MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //Add the block condition. It will throw BlockConditionAlreadyExistsException if the condition already
            //  exists in the system
            String uuid = null;
            if (ConditionTypeEnum.API.equals(body.getConditionType()) ||
                    ConditionTypeEnum.APPLICATION.equals(body.getConditionType()) ||
                    ConditionTypeEnum.USER.equals(body.getConditionType())) {
                uuid = apiProvider.addBlockCondition(body.getConditionType().toString(),
                        (String) body.getConditionValue(), body.isConditionStatus());
            } else if (ConditionTypeEnum.IP.equals(body.getConditionType()) ||
                    ConditionTypeEnum.IPRANGE.equals(body.getConditionType())) {
                if (body.getConditionValue() instanceof Map) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.putAll((Map) body.getConditionValue());

                    if (ConditionTypeEnum.IP.equals(body.getConditionType())) {
                        RestApiAdminUtils.validateIPAddress(jsonObject.get("fixedIp").toString());
                    }
                    if (ConditionTypeEnum.IPRANGE.equals(body.getConditionType())) {
                        RestApiAdminUtils.validateIPAddress(jsonObject.get("startingIp").toString());
                        RestApiAdminUtils.validateIPAddress(jsonObject.get("endingIp").toString());
                    }
                    uuid = apiProvider.addBlockCondition(body.getConditionType().toString(),
                            jsonObject.toJSONString(), body.isConditionStatus());
                }
            }

            //retrieve the new blocking condition and send back as the response
            BlockConditionsDTO newBlockingCondition = apiProvider.getBlockConditionByUUID(uuid);
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(newBlockingCondition);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_BLOCK_CONDITIONS + "/"
                    + uuid)).entity(dto).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceAlreadyExists(e)) {
                RestApiUtil.handleResourceAlreadyExistsError("A black list item with type: "
                        + body.getConditionType() + ", value: " + body.getConditionValue() + " already exists", e, log);
            } else {
                String errorMessage = "Error while adding Blocking Condition. Condition type: "
                        + body.getConditionType() + ", " + "value: " + body.getConditionValue() + ". " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException | ParseException e) {
            String errorMessage = "Error while retrieving Blocking Condition resource location: Condition type: "
                    + body.getConditionType() + ", " + "value: " + body.getConditionValue() + ". " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get a specific Block condition by its id
     *
     * @param conditionId Id of the block condition
     * @return Matched block condition for the given Id
     */
    @Override
    public Response getDenyPolicyById(String conditionId, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();

            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            BlockConditionsDTO blockCondition = apiProvider.getBlockConditionByUUID(conditionId);
            if (!RestApiAdminUtils.isBlockConditionAccessibleToUser(username, blockCondition)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId, log);
            }
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(blockCondition);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId, e, log);
            } else {
                String errorMessage = "Error while retrieving Block Condition. Id : " + conditionId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (ParseException e) {
            String errorMessage = "Error while retrieving Blocking Conditions";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Delete a block condition specified by the condition Id
     *
     * @param conditionId Id of the block condition
     * @return 200 OK response if successfully deleted the block condition
     */
    @Override
    public Response removeDenyPolicy(String conditionId, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();

            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            BlockConditionsDTO existingCondition = apiProvider.getBlockConditionByUUID(conditionId);
            if (!RestApiAdminUtils.isBlockConditionAccessibleToUser(username, existingCondition)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId, log);
            }
            apiProvider.deleteBlockConditionByUUID(conditionId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId, e, log);
            } else {
                String errorMessage = "Error while deleting Block Condition. Id : " + conditionId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Updates an existing condition status of a blocking condition
     *
     * @param conditionId Id of the block condition
     * @param body        content to update
     * @param contentType Content-Type header
     * @return 200 response if successful
     */
    @Override
    public Response updateDenyPolicy(String conditionId, String contentType,
                                     BlockingConditionStatusDTO body, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();

            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            BlockConditionsDTO existingCondition = apiProvider.getBlockConditionByUUID(conditionId);
            if (!RestApiAdminUtils.isBlockConditionAccessibleToUser(username, existingCondition)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId, log);
            }

            //update the status
            apiProvider.updateBlockConditionByUUID(conditionId, String.valueOf(body.isConditionStatus()));

            //retrieve the new blocking condition and send back as the response
            BlockConditionsDTO newBlockingCondition = apiProvider.getBlockConditionByUUID(conditionId);
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(newBlockingCondition);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException | ParseException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId, e, log);
            } else {
                String errorMessage = "Error while updating Block Condition Status. Id : " + conditionId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Checks if the logged in user belongs to super tenant and throws 403 error if not
     *
     * @throws ForbiddenException
     */
    private void checkTenantDomainForCustomRules() throws ForbiddenException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            RestApiUtil.handleAuthorizationFailure("You are not allowed to access this resource",
                    new APIManagementException("Tenant " + tenantDomain + " is not allowed to access custom rules. " +
                            "Only super tenant is allowed"), log);
        }
    }
}
