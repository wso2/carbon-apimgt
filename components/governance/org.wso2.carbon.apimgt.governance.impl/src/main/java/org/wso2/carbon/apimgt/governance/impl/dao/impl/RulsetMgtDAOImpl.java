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
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.impl.dao.RulsetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementation of the RulsetMgtDAO interface.
 */
public class RulsetMgtDAOImpl implements RulsetMgtDAO {

    private static final Log log = LogFactory.getLog(RulsetMgtDAOImpl.class);
    private static RulsetMgtDAO INSTANCE = null;

    private RulsetMgtDAOImpl() {
    }

    /**
     * Get an instance of the RulsetMgtDAO
     *
     * @return RulsetMgtDAO instance
     */
    public static RulsetMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RulsetMgtDAOImpl();
        }
        return INSTANCE;
    }

    /**
     * Create a new Governance Ruleset
     *
     * @param organization Organization
     * @param ruleset      Ruleset object
     * @return
     * @throws GovernanceException
     */
    @Override
    public Ruleset createRuleset(String organization, Ruleset ruleset) throws GovernanceException {


        Connection connection = null;
        PreparedStatement prepStmt = null;

        InputStream rulesetContent = new ByteArrayInputStream(
                ruleset.getRulesetContent().getBytes(Charset.defaultCharset()));

        // Check whether the ruleset name is occupied by a default ruleset.
//        Map<String, Ruleset> defaultRulesetsForOrg = defaultRulesetService.getDefaultRulesetListForOrg(organization);
//        if (!defaultRulesetsForOrg.containsKey(ruleset.getId())) {
//            defaultRulesetService.isRulesetNameOccupied(ruleset.getName(), organization);
//        }

        String sqlQuery = SQLConstants.CREATE_RULESET;
        try {
            connection = GovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, ruleset.getId());
            prepStmt.setString(2, ruleset.getName());
            prepStmt.setString(3, ruleset.getDescription());
            prepStmt.setBlob(4, rulesetContent);
            prepStmt.setString(5, ruleset.getAppliesTo());
            prepStmt.setString(6, ruleset.getDocumentationLink());
            prepStmt.setString(7, ruleset.getProvider());
            prepStmt.setString(8, organization);
            prepStmt.setString(9, ruleset.getCreatedBy());
            prepStmt.execute();
//          insertRules(ruleset.getId(), ruleset.getRulesetContent(), connection);
            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                // Rollback failed. Exception will be thrown later for upper exception
                log.error("Failed to rollback the adding Ruleset: " + ruleset.getName());
            }
            handleException("Error while adding the Ruleset: " + ruleset.getName() + " to the database", e);
        } finally {
            GovernanceDBUtil.closeAllConnections(prepStmt, connection, null);
        }
        return ruleset;
    }

    /**
     * Handle  governance exceptions
     *
     * @param msg error message
     * @param t   throwable
     * @throws GovernanceException governance exception
     */
    private void handleException(String msg, Throwable t) throws GovernanceException {
        log.error(msg, t);
        throw new GovernanceException(msg, t);
    }


}

