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
import org.wso2.carbon.apimgt.governance.api.GovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.manager.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.RulesetManagerImpl;
import org.wso2.carbon.apimgt.governance.rest.api.RulesetsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.RulesetMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * This is the implementation class for the Rulesets API.
 */
public class RulesetsApiServiceImpl implements RulesetsApiService {

    public Response createRuleset(RulesetDTO rulesetDTO, MessageContext messageContext)
            throws GovernanceException {

        RulesetDTO createdRulesetDTO;
        URI createdRulesetURI;
        try {
            String username = GovernanceAPIUtil.getLoggedInUsername();
            String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

            Ruleset ruleset = RulesetMappingUtil.fromDTOtoRuleset(rulesetDTO);
            ruleset.setCreatedBy(username);

            RulesetManager rulesetManager = new RulesetManagerImpl();
            Ruleset createdRuleset = rulesetManager.createNewRuleset(organization, ruleset);

            createdRulesetDTO = RulesetMappingUtil.fromRulsetToDTO(createdRuleset);
            createdRulesetURI = new URI(
                    GovernanceAPIConstants.RULSET_PATH + "/" + createdRulesetDTO.getId());
            return Response.created(createdRulesetURI).entity(createdRulesetDTO).build();

        } catch (URISyntaxException e) {
            String error = "Error while creating new governance " +
                    "ruleset under organization " + "ORGANIZATION";
            throw new GovernanceException(error, e, GovernanceExceptionCodes.INTERNAL_SERVER_ERROR);
        }
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
        RulesetListDTO rulesetListDTO = RulesetMappingUtil.fromRulsetListToDTO(rulesetList);
        return Response.status(Response.Status.OK).entity(rulesetListDTO).build();
    }
}
