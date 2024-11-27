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

package org.wso2.carbon.apimgt.governance.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the DAO class related to assessing compliance of APIs
 */
public class ComplianceMgtDAOImpl implements ComplianceMgtDAO {

    private static final Log log = LogFactory.getLog(ComplianceMgtDAOImpl.class);
    private static ComplianceMgtDAO INSTANCE = null;

    private ComplianceMgtDAOImpl() {
    }


    /**
     * Get an instance of the ComplianceMgtDAOImpl class
     *
     * @return Instance of the ComplianceMgtDAOImpl class
     */
    public static ComplianceMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComplianceMgtDAOImpl();
        }
        return INSTANCE;
    }


    /**
     * Get the associated rulesets by policy
     *
     * @param policyId     Policy ID, if null all the policies will be considered
     * @param organization Organization Name
     * @return Map of associated rulesets
     * @throws GovernanceException If an error occurs while getting the associated rulesets
     */
    @Override
    public Map<String, Map<String, String>> getAssociatedRulesetsByPolicy(String policyId,
                                                                          String organization)
            throws GovernanceException {
        Map<String, Map<String, String>> rulesetMap = new HashMap<>();
        String query;
        try (Connection connection = GovernanceDBUtil.getConnection()) {
            PreparedStatement preparedStatement;
            if (policyId != null) {
                // If policyId is not null, get rulesets for that particular policy.
                query = SQLConstants.GET_RULESETS_BY_POLICY_ID;
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, policyId);
            } else {
                // If policyId is null, get rulesets for all policies within the organization.
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving all rulesets as policy id is not specified.");
                }
                query = SQLConstants.GET_RULESETS_BY_POLICY;
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, organization);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String rulesetId = resultSet.getString("RULESET_ID");
                byte[] rulesetContentBytes = resultSet.getBytes("RULESET_CONTENT");
                policyId = resultSet.getString("POLICY_ID");
                // Convert byte array to YAML string
                String rulesetContentYaml = new String(rulesetContentBytes, StandardCharsets.UTF_8);
                // Retrieve the existing map for the current policyId, or create a new one if it doesn't exist.
                Map<String, String> rulesetDetails = rulesetMap.getOrDefault(policyId, new HashMap<>());
                // Add the current rulesetId and its content to the rulesetDetails map.
                rulesetDetails.put(rulesetId, rulesetContentYaml);
                // Put the updated values map back into the rulesetMap for the current policyId.
                rulesetMap.put(policyId, rulesetDetails);
            }
        } catch (SQLException e) {
            throw new GovernanceException(GovernanceExceptionCodes.
                    ERROR_WHILE_RETRIEVING_RULESET_CONTENT_ASSOCIATED_WITH_POLICIES,
                    e, organization);
        }
        return rulesetMap;
    }
}
