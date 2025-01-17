/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public class ComplianceManagerImpl implements ComplianceManager {
    private static final Log log = LogFactory.getLog(ComplianceManagerImpl.class);

    private ComplianceMgtDAO complianceMgtDAO;
    private GovernancePolicyMgtDAO governancePolicyMgtDAO;

    private RulesetMgtDAO rulesetMgtDAO;

    public ComplianceManagerImpl() {
        complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
        governancePolicyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
        rulesetMgtDAO = RulesetMgtDAOImpl.getInstance();
    }


    /**
     * Handle Policy Change Event
     *
     * @param policyId     Policy ID
     * @param organization Organization
     */
    @Override
    public void handlePolicyChangeEvent(String policyId, String organization) throws GovernanceException {

        // Get labels for policy
        List<String> labels = governancePolicyMgtDAO.getLabelsByPolicyId(policyId);
        List<String> artifacts = new ArrayList<>();

        for (String label : labels) {
            // TODO: Get artifacts by label and state from APIM
        }

        for (String artifactId : artifacts) {
            String artifactType = "REST_API"; //TODO: Get artifact type from APIM
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }
    }

    /**
     * Handle Ruleset Change Event
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     */
    @Override
    public void handleRulesetChangeEvent(String rulesetId, String organization) throws GovernanceException {
        List<String> policies = rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId);

        for (String policyId : policies) {
            handlePolicyChangeEvent(policyId, organization);
        }
    }

    /**
     * Handle API Compliance Evaluation Request Async
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param govPolicies  List of governance policies to be evaluated
     * @param organization Organization
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    @Override
    public void handleComplianceEvaluationAsync(String artifactId, String artifactType,
                                                List<String> govPolicies,
                                                String organization) throws GovernanceException {

        for (String policyId : govPolicies) {
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }

    }


}
