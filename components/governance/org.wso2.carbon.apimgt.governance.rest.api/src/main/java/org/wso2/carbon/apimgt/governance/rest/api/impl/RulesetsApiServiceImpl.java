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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.http.HttpHeaders;
import org.wso2.carbon.apimgt.governance.api.GovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.manager.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.RulesetManagerImpl;
import org.wso2.carbon.apimgt.governance.rest.api.RulesetsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.RulesetMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


/**
 * This is the implementation class for the Rulesets API.
 */
public class RulesetsApiServiceImpl implements RulesetsApiService {

    /**
     * Create a new Governance Ruleset
     *
     * @param rulesetDTO     Ruleset object
     * @param messageContext MessageContext
     * @return Response object
     * @throws GovernanceException If an error occurs while creating the ruleset
     */
    public Response createRuleset(RulesetDTO rulesetDTO, MessageContext messageContext)
            throws GovernanceException {

        RulesetInfoDTO createdRulesetDTO;
        URI createdRulesetURI;
        try {
            String username = GovernanceAPIUtil.getLoggedInUsername();
            String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

            Ruleset ruleset = RulesetMappingUtil.fromRulesetDTOtoRuleset(rulesetDTO);
            ruleset.setCreatedBy(username);

            RulesetManager rulesetManager = new RulesetManagerImpl();
            RulesetInfo createdRuleset = rulesetManager.createNewRuleset(organization, ruleset);

            createdRulesetDTO = RulesetMappingUtil.fromRulesetInfoToRulesetInfoDTO(createdRuleset);
            createdRulesetURI = new URI(
                    GovernanceAPIConstants.RULSET_PATH + "/" + createdRulesetDTO.getId());
            return Response.created(createdRulesetURI).entity(createdRulesetDTO).build();

        } catch (URISyntaxException e) {
            String error = String.format("Error while creating URI for new Ruleset %s",
                    rulesetDTO.getName());
            throw new GovernanceException(error, e, GovernanceExceptionCodes.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws GovernanceException If an error occurs while deleting the ruleset
     */
    @Override
    public Response deleteRuleset(String rulesetId, MessageContext messageContext) throws GovernanceException {
        RulesetManager rulesetManager = new RulesetManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        rulesetManager.deleteRuleset(organization, rulesetId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Get a Governance Ruleset by ID
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws GovernanceException If an error occurs while getting the ruleset
     */
    @Override
    public Response getRulesetById(String rulesetId, MessageContext messageContext) throws GovernanceException {
        RulesetManager rulesetManager = new RulesetManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        RulesetInfo ruleset = rulesetManager.getRulesetById(organization, rulesetId);
        RulesetInfoDTO rulesetInfoDTO = RulesetMappingUtil.fromRulesetInfoToRulesetInfoDTO(ruleset);
        return Response.status(Response.Status.OK).entity(rulesetInfoDTO).build();
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws GovernanceException If an error occurs while getting the ruleset content
     */
    @Override
    public Response getRulesetContent(String rulesetId, MessageContext messageContext) throws GovernanceException {
        RulesetManager rulesetManager = new RulesetManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        String content = rulesetManager.getRulesetContent(organization, rulesetId);

        return Response.status(Response.Status.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ruleset.yaml")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-yaml")
                .entity(content).build();
    }

    /**
     * Get the list of policies using the Ruleset
     *
     * @param rulesetId      Ruleset ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws GovernanceException If an error occurs while getting the ruleset usage
     */
    @Override
    public Response getRulesetUsage(String rulesetId, MessageContext messageContext) throws GovernanceException {
        RulesetManager rulesetManager = new RulesetManagerImpl();
        List<String> policies = rulesetManager.getRulesetUsage(rulesetId);
        return Response.status(Response.Status.OK).entity(policies).build();
    }

    /**
     * Get all the Governance Rulesets
     *
     * @param messageContext MessageContext
     * @return Response object
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    public Response getRulesets(MessageContext messageContext) throws GovernanceException {

        RulesetManager rulesetManager = new RulesetManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        RulesetList rulesetList = rulesetManager.getRulesets(organization);
        RulesetListDTO rulesetListDTO = RulesetMappingUtil.fromRulesetListToRuleListDTO(rulesetList);
        return Response.status(Response.Status.OK).entity(rulesetListDTO).build();
    }

    /**
     * Update a Governance Ruleset
     *
     * @param rulesetId      Ruleset ID
     * @param rulesetDTO     Ruleset object
     * @param messageContext MessageContext
     * @return Response object
     * @throws GovernanceException If an error occurs while updating the ruleset
     */
    @Override
    public Response updateRulesetById(String rulesetId, RulesetDTO rulesetDTO, MessageContext messageContext) throws GovernanceException {
        String username = GovernanceAPIUtil.getLoggedInUsername();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        Ruleset ruleset = RulesetMappingUtil.fromRulesetDTOtoRuleset(rulesetDTO);
        ruleset.setUpdatedBy(username);
        ruleset.setId(rulesetId);

        RulesetManager rulesetManager = new RulesetManagerImpl();
        RulesetInfo updatedRuleset = rulesetManager.updateRuleset(organization, rulesetId, ruleset);
        // TODO: Retrigger Policy Validation
        return Response.status(Response.Status.OK).entity(RulesetMappingUtil.
                fromRulesetInfoToRulesetInfoDTO(updatedRuleset)).build();
    }
}
