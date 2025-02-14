/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.springframework.http.HttpHeaders;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.RulesetManager;
import org.wso2.carbon.apimgt.governance.rest.api.RulesetsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.RulesetMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * This is the implementation class for the Rulesets API.
 */
public class RulesetsApiServiceImpl implements RulesetsApiService {

    /**
     * Create a new Governance Ruleset
     *
     * @param name                      Name
     * @param rulesetContentInputStream Ruleset content input stream
     * @param rulesetContentDetail      Ruleset content detail
     * @param ruleCategory              Rule category
     * @param ruleType                  Rule type
     * @param artifactType              Artifact type
     * @param provider                  Provider
     * @param description               Description
     * @param documentationLink         Documentation link
     * @param messageContext            MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while creating the ruleset
     */
    @Override
    public Response createRuleset(String name, InputStream rulesetContentInputStream,
                                  Attachment rulesetContentDetail, String ruleType,
                                  String artifactType, String description, String ruleCategory,
                                  String documentationLink, String provider,
                                  MessageContext messageContext) throws APIMGovernanceException {
        RulesetInfoDTO createdRulesetDTO;
        URI createdRulesetURI;
        Ruleset ruleset = new Ruleset();
        try {
            ruleset.setName(name);
            ruleCategory = ruleCategory != null ? ruleCategory : RuleCategory.SPECTRAL.toString();
            ruleset.setRuleCategory(RuleCategory.fromString(ruleCategory));
            ruleset.setRuleType(RuleType.fromString(ruleType));
            ruleset.setArtifactType(ExtendedArtifactType.fromString(artifactType));
            ruleset.setProvider(provider);
            ruleset.setDescription(description);
            ruleset.setDocumentationLink(documentationLink);

            RulesetContent rulesetContent = new RulesetContent();
            rulesetContent.setContent(IOUtils.toByteArray(rulesetContentInputStream));
            rulesetContent.setFileName(rulesetContentDetail.getContentDisposition().getFilename());
            ruleset.setRulesetContent(rulesetContent);

            String username = APIMGovernanceAPIUtil.getLoggedInUsername();
            String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
            ruleset.setCreatedBy(username);

            RulesetManager rulesetManager = new RulesetManager();
            RulesetInfo createdRuleset = rulesetManager.createNewRuleset(ruleset, organization);

            createdRulesetDTO = RulesetMappingUtil.fromRulesetInfoToRulesetInfoDTO(createdRuleset);
            createdRulesetURI = new URI(
                    APIMGovernanceAPIConstants.RULESET_PATH + "/" + createdRulesetDTO.getId());
            return Response.created(createdRulesetURI).entity(createdRulesetDTO).build();

        } catch (URISyntaxException e) {
            String error = String.format("Error while creating URI for new Ruleset %s",
                    name);
            throw new APIMGovernanceException(error, e, APIMGovExceptionCodes.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            throw new APIMGovernanceException("Error while converting ruleset content stream", e);
        } finally {
            IOUtils.closeQuietly(rulesetContentInputStream);
        }
    }


    /**
     * Update a Governance Ruleset
     *
     * @param rulesetId                 Ruleset ID
     * @param name                      Name
     * @param rulesetContentInputStream Ruleset content input stream
     * @param rulesetContentDetail      Ruleset content detail
     * @param ruleCategory              Rule category
     * @param ruleType                  Rule type
     * @param artifactType              Artifact type
     * @param provider                  Provider
     * @param description               Description
     * @param documentationLink         Documentation link
     * @param messageContext            MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while updating the ruleset
     */
    @Override
    public Response updateRulesetById(String rulesetId, String name, InputStream rulesetContentInputStream,
                                      Attachment rulesetContentDetail, String ruleType, String artifactType,
                                      String description, String ruleCategory, String documentationLink,
                                      String provider, MessageContext messageContext)
            throws APIMGovernanceException {

        try {
            Ruleset ruleset = new Ruleset();
            ruleset.setName(name);
            ruleCategory = ruleCategory != null ? ruleCategory : RuleCategory.SPECTRAL.toString();
            ruleset.setRuleCategory(RuleCategory.fromString(ruleCategory));
            ruleset.setRuleType(RuleType.fromString(ruleType));
            ruleset.setArtifactType(ExtendedArtifactType.fromString(artifactType));
            ruleset.setProvider(provider);
            ruleset.setId(rulesetId);
            ruleset.setDescription(description);
            ruleset.setDocumentationLink(documentationLink);

            RulesetContent rulesetContent = new RulesetContent();
            rulesetContent.setContent(IOUtils.toByteArray(rulesetContentInputStream));
            rulesetContent.setFileName(rulesetContentDetail.getContentDisposition().getFilename());
            ruleset.setRulesetContent(rulesetContent);

            String username = APIMGovernanceAPIUtil.getLoggedInUsername();
            String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
            ruleset.setUpdatedBy(username);

            RulesetManager rulesetManager = new RulesetManager();
            RulesetInfo updatedRuleset = rulesetManager.updateRuleset(rulesetId, ruleset, organization);

            // Re-access policy compliance in the background
            new ComplianceManager().handleRulesetChangeEvent(rulesetId, organization);

            return Response.status(Response.Status.OK).entity(RulesetMappingUtil.
                    fromRulesetInfoToRulesetInfoDTO(updatedRuleset)).build();
        } catch (IOException e) {
            throw new APIMGovernanceException("Error while converting ruleset content stream", e);
        } finally {
            IOUtils.closeQuietly(rulesetContentInputStream);
        }

    }

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while deleting the ruleset
     */
    @Override
    public Response deleteRuleset(String rulesetId, MessageContext messageContext) throws APIMGovernanceException {
        RulesetManager rulesetManager = new RulesetManager();

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        rulesetManager.deleteRuleset(rulesetId, organization);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Get a Governance Ruleset by ID
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset
     */
    @Override
    public Response getRulesetById(String rulesetId, MessageContext messageContext) throws APIMGovernanceException {
        RulesetManager rulesetManager = new RulesetManager();

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        RulesetInfo ruleset = rulesetManager.getRulesetById(rulesetId, organization);
        RulesetInfoDTO rulesetInfoDTO = RulesetMappingUtil.fromRulesetInfoToRulesetInfoDTO(ruleset);
        return Response.status(Response.Status.OK).entity(rulesetInfoDTO).build();
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset content
     */
    @Override
    public Response getRulesetContent(String rulesetId, MessageContext messageContext) throws APIMGovernanceException {
        RulesetManager rulesetManager = new RulesetManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        RulesetContent rulesetContent = rulesetManager.getRulesetContent(rulesetId, organization);

        String fileName = rulesetContent.getFileName() != null ? rulesetContent.getFileName() : "ruleset.yaml";
        String contentTypeHeader = "application/x-yaml"; // Default content type

        if (RulesetContent.ContentType.JSON.equals(rulesetContent.getContentType())) {
            contentTypeHeader = "application/json";
        }
        
        return Response.status(Response.Status.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName)
                .header(HttpHeaders.CONTENT_TYPE, contentTypeHeader)
                .entity(new String(rulesetContent.getContent(), StandardCharsets.UTF_8)).build();
    }

    /**
     * Get the list of policies using the Ruleset
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset usage
     */
    @Override
    public Response getRulesetUsage(String rulesetId, MessageContext messageContext) throws APIMGovernanceException {
        RulesetManager rulesetManager = new RulesetManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        List<String> policies = rulesetManager.getRulesetUsage(rulesetId, organization);
        return Response.status(Response.Status.OK).entity(policies).build();
    }

    /**
     * Get all the Governance Rulesets
     *
     * @param limit          Limit
     * @param offset         Offset
     * @param query          Query for filtering
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */
    public Response getRulesets(Integer limit, Integer offset, String query, MessageContext messageContext)
            throws APIMGovernanceException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query != null ? query : "";

        RulesetManager rulesetManager = new RulesetManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        RulesetList rulesetList;
        if (!query.isEmpty()) {
            rulesetList = rulesetManager.searchRulesets(query, organization);
        } else {
            rulesetList = rulesetManager.getRulesets(organization);
        }
        RulesetListDTO paginatedRuleList = getPaginatedRulesets(rulesetList, limit, offset, query);

        return Response.status(Response.Status.OK).entity(paginatedRuleList).build();
    }

    /**
     * Get the paginated list of Governance Rulesets
     *
     * @param rulesetList RulesetList object
     * @param limit       Limit
     * @param offset      Offset
     * @param query       Query for filtering
     * @return RulesetListDTO object
     */
    private RulesetListDTO getPaginatedRulesets(RulesetList rulesetList, int limit, int offset, String query) {
        int rulesetCount = rulesetList.getCount();
        List<RulesetInfoDTO> paginatedRulesets = new ArrayList<>();
        RulesetListDTO paginatedRulesetListDTO = new RulesetListDTO();
        paginatedRulesetListDTO.setCount(Math.min(rulesetCount, limit));

        // If the provided offset value exceeds the offset, reset the offset to default.
        if (offset > rulesetCount) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        // Select only the set of rulesets which matches the given limit and offset values.
        int start = offset;
        int end = Math.min(rulesetCount, start + limit);
        for (int i = start; i < end; i++) {
            RulesetInfo rulesetInfo = rulesetList.getRulesetList().get(i);
            RulesetInfoDTO rulesetInfoDTO = RulesetMappingUtil.fromRulesetInfoToRulesetInfoDTO(rulesetInfo);
            paginatedRulesets.add(rulesetInfoDTO);
        }
        paginatedRulesetListDTO.setList(paginatedRulesets);

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(rulesetCount);
        paginatedRulesetListDTO.setPagination(paginationDTO);

        // Set previous and next URLs for pagination
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, rulesetCount);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = APIMGovernanceAPIUtil.getPaginatedURLWithQuery
                    (APIMGovernanceAPIConstants.RULESETS_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getPaginatedURLWithQuery
                    (APIMGovernanceAPIConstants.RULESETS_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        paginationDTO.setPrevious(paginatedPrevious);
        paginationDTO.setNext(paginatedNext);

        return paginatedRulesetListDTO;
    }
}
