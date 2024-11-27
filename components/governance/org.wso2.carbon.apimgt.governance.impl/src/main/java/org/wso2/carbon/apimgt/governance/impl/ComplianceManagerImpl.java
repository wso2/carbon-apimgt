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

package org.wso2.carbon.apimgt.governance.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.manager.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.client.apim.APIM;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public class ComplianceManagerImpl implements ComplianceManager {
    private static final Log log = LogFactory.getLog(ComplianceManagerImpl.class);

    private ComplianceMgtDAO complianceMgtDAO;

    public ComplianceManagerImpl() {
        complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
    }

    /**
     * Get the associated rulesets by policy
     *
     * @param organization Organization Name
     * @return Map of associated rulesets
     * @throws GovernanceException If an error occurs while getting the associated rulesets
     */
    @Override
    public Map<String, Map<String, String>> getAssociatedRulesetsByPolicy(String organization)
            throws GovernanceException {
        return complianceMgtDAO.getAssociatedRulesetsByPolicy(null, organization);
    }

    /**
     * Assess the compliance of an API
     *
     * @param apiId                       API ID
     * @param organization                Organization Name
     * @param policyToRulesetToContentMap Map of policy to ruleset to content
     * @param authHeader                  Auth header
     */
    @Override
    public void assessAPICompliance(String apiId, String organization,
                                    Map<String, Map<String, String>> policyToRulesetToContentMap,
                                    String authHeader) {
        try {
            InputStream apimProject = APIM.getAPIMProject(apiId, authHeader);
            if (apimProject != null) {
                // Extract swagger content from apim project.
                byte[] projectBytes = GovernanceUtil.toByteArray(apimProject);
                String swaggerContent = GovernanceUtil.getSwaggerFileFromZip(projectBytes, apiId);
                if (swaggerContent == null) {
                    log.debug("API definition not found for api: " + apiId + " in the organization: " +
                            organization);
                    throw new GovernanceException(GovernanceExceptionCodes.API_DEFINITION_NOT_FOUND, apiId,
                            organization);
                } else {
                    log.info(swaggerContent);
                }
            }
        } catch (GovernanceException | IOException e) {
            log.error(e);
        }
        log.info("Assessment Completed.");
    }


}
