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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.admin.v1.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.mediation.MediationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PoliciesApiServiceImpl implements PoliciesApiService {

    private static final Log log = LogFactory.getLog(PoliciesApiServiceImpl.class);

    /**
     * Returns list of global Mediation policies
     *
     * @param limit       maximum number of mediation returns
     * @param offset      starting index
     * @param query       search condition
     * @param accept      accept header value
     * @return Matched global mediation policies for given search condition
     */
    @Override
    public Response policiesMediationGet(Integer limit, Integer offset, String query, String accept,
                                         MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            List<Mediation> mediationList = apiProvider.getAllGlobalMediationPolicies();
            MediationListDTO mediationListDTO = MediationMappingUtil.fromMediationListToDTO(mediationList, offset,
                    limit);
            return Response.ok().entity(mediationListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving all global mediation policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
            return null;
        }
    }

    /**
     * Deletes an existing global mediation policy
     *
     * @param mediationPolicyId Uuid of mediation policy resource
     * @return 200 response if deleted successfully
     */
    @Override
    public Response policiesMediationMediationPolicyIdDelete(String mediationPolicyId, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //Delete given global mediation policy
            boolean deleteState = apiProvider.deleteGlobalMediationPolicy(mediationPolicyId);
            if (deleteState) {
                return Response.ok().build();
            } else {
                //If registry resource not found
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_POLICY, mediationPolicyId, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting the global mediation policy with uuid " + mediationPolicyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Returns a specific global mediation policy by identifier
     *
     * @param mediationPolicyId Mediation policy uuid
     * @param accept            Accept header value
     * @return returns the matched mediation
     */
    @Override
    public Response policiesMediationMediationPolicyIdGet(String mediationPolicyId, String accept,
                                                          MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //Get given global mediation policy
            Mediation mediation = apiProvider.getGlobalMediationPolicy(mediationPolicyId);
            if (mediation != null) {
                MediationDTO mediationDTO = MediationMappingUtil.fromMediationToDTO(mediation);
                return Response.ok().entity(mediationDTO).build();
            } else {
                //If global mediation policy not exists
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_POLICY, mediationPolicyId, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the global mediation policy with id " + mediationPolicyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates an existing global mediation policy
     *
     * @param mediationPolicyId uuid of mediation policy
     * @param body              updated MediationDTO
     * @param contentType       Content-Type header
     * @return updated mediation DTO as response
     */
    @Override
    public Response policiesMediationMediationPolicyIdPut(String mediationPolicyId, String contentType,
                  MediationDTO body, MessageContext messageContext) {
        InputStream contentStream = null;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //Get registry resource correspond to given uuid
            Resource mediationResource = apiProvider.getCustomMediationResourceFromUuid(mediationPolicyId);
            if (mediationResource != null) {

                //extracting already existing name of the mediation policy
                String contentString = IOUtils.toString(mediationResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
                //Get policy name from the mediation config
                OMElement omElement = AXIOMUtil.stringToOM(contentString);
                OMAttribute attribute = omElement.getAttribute(new QName(PolicyConstants.MEDIATION_NAME_ATTRIBUTE));
                String existingMediationPolicyName = attribute.getAttributeValue();

                //replacing the name of the body with existing name
                body.setName(existingMediationPolicyName);

                //Getting mediation config to be update from the body
                contentStream = new ByteArrayInputStream(body.getConfig().getBytes(StandardCharsets.UTF_8));
                //Creating new resource file
                ResourceFile contentFile = new ResourceFile(contentStream, contentType);
                //Getting registry path of the existing resource
                String resourcePath = mediationResource.getPath();
                //Updating the existing global mediation policy
                // No need to check API permission, hence null as api identifier
                String updatedPolicyUrl = apiProvider.addResourceFile(null, resourcePath, contentFile);
                if (StringUtils.isNotBlank(updatedPolicyUrl)) {
                    //Getting uuid of updated global mediation policy
                    String uuid = apiProvider.getCreatedResourceUuid(resourcePath);
                    //Getting updated mediation
                    Mediation updatedMediation = apiProvider.getGlobalMediationPolicy(uuid);
                    MediationDTO updatedMediationDTO = MediationMappingUtil.fromMediationToDTO(updatedMediation);
                    URI uploadedMediationUri = new URI(updatedPolicyUrl);
                    return Response.ok(uploadedMediationUri).entity(updatedMediationDTO).build();
                }
            } else {
                //If resource not exists
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_POLICY, mediationPolicyId, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating the global mediation policy " + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while getting location header for uploaded " + "mediation policy " + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (XMLStreamException e) {
            String errorMessage = "Error occurred while converting the existing content stream of " + " mediation " +
                    "policy to string";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (RegistryException e) {
            String errorMessage = "Error occurred while getting the existing content stream ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (IOException e) {
            String errorMessage = "Error occurred while converting content stream in to string ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
        return null;
    }

    /**
     * Add a global mediation policy
     *
     * @param body              Mediation DTO as request body
     * @param contentType       Content-Type header
     * @return created mediation DTO as response
     */
    @Override
    public Response policiesMediationPost(String contentType, MediationDTO body, MessageContext messageContext) {
        InputStream contentStream = null;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String content = body.getConfig();
            contentStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            ResourceFile contentFile = new ResourceFile(contentStream, contentType);
            //Extracting mediation policy name from the mediation config
            String fileName = this.getMediationNameFromConfig(content);
            //constructing the registry resource path
            String mediationPolicyPath =
                    APIConstants.API_CUSTOM_SEQUENCE_LOCATION + RegistryConstants.PATH_SEPARATOR + body.getType() + RegistryConstants.PATH_SEPARATOR + fileName;
            if (apiProvider.checkIfResourceExists(mediationPolicyPath)) {
                RestApiUtil.handleConflict("Mediation policy already exists", log);
            }
            //Adding new global mediation sequence
            // No need to check API permission, hence null as api identifier
            String mediationPolicyUrl = apiProvider.addResourceFile(null, mediationPolicyPath, contentFile);
            if (StringUtils.isNotBlank(mediationPolicyUrl)) {
                //Getting the uuid of the created global mediation policy
                String uuid = apiProvider.getCreatedResourceUuid(mediationPolicyPath);
                //Getting created mediation policy
                Mediation createdMediation = apiProvider.getGlobalMediationPolicy(uuid);
                MediationDTO createdPolicy = MediationMappingUtil.fromMediationToDTO(createdMediation);
                URI uploadedMediationUri = new URI(mediationPolicyUrl);
                return Response.created(uploadedMediationUri).entity(createdPolicy).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding the global mediation policy " + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while getting location header for created " + "mediation policy " + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
        return null;
    }

    /**
     * Returns the mediation policy name specify inside mediation config
     *
     * @param config mediation config content
     * @return name of the mediation policy or null
     */
    private String getMediationNameFromConfig(String config) {
        try {
            //convert xml content in to json
            String configInJson = XML.toJSONObject(config).toString();
            JSONParser parser = new JSONParser();
            //Extracting mediation policy name from the json string
            JSONObject jsonObject = (JSONObject) parser.parse(configInJson);
            JSONObject rootObject = (JSONObject) jsonObject.get(APIConstants.MEDIATION_SEQUENCE_ELEM);
            String name = rootObject.get(APIConstants.POLICY_NAME_ELEM).toString();
            //adding .xml extension explicitly
            return name + APIConstants.MEDIATION_CONFIG_EXT;
        } catch (JSONException e) {
            log.error("Error occurred while converting the mediation config string to json", e);
        } catch (ParseException e) {
            log.error("Error occurred while parsing config json string in to json object", e);
        }
        return null;
    }
}
