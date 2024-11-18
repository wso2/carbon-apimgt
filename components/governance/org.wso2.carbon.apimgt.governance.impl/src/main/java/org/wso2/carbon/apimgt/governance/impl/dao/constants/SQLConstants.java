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

package org.wso2.carbon.apimgt.governance.impl.dao.constants;

/**
 * This class represents the SQL Constants
 */
public class SQLConstants {
    public static final String CREATE_RULESET = "INSERT INTO GOV_RULESET (RULESET_ID, NAME, DESCRIPTION, " +
            "RULESET_CONTENT, APPLIES_TO, DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, CREATED_BY, IS_DEFAULT) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String INSERT_RULES = "INSERT INTO GOV_RULESET_RULE (RULESET_RULE_ID, RULESET_ID, RULE_CODE, " +
            "RULE_MESSAGE, RULE_DESCRIPTION, SEVERITY) VALUES (?, ?, ?, ?, ?, ?)";

    public static final String GET_RULESETS = "SELECT RULESET_ID, NAME, DESCRIPTION, RULESET_CONTENT, APPLIES_TO, " +
            "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, IS_DEFAULT, CREATED_BY, CREATED_TIME, UPDATED_BY, " +
            "LAST_UPDATED_TIME " +
            "FROM GOV_RULESET WHERE ORGANIZATION = ?";

    public static final String GET_RULESET_BY_NAME = "SELECT RULESET_ID, NAME, DESCRIPTION, RULESET_CONTENT, APPLIES_TO, " +
            "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, IS_DEFAULT, CREATED_BY, CREATED_TIME, UPDATED_BY, " +
            "LAST_UPDATED_TIME " +
            "FROM GOV_RULESET WHERE NAME = ? AND ORGANIZATION = ?";

    public static final String GET_RULESETS_BY_ID = "SELECT RULESET_ID, NAME, DESCRIPTION, RULESET_CONTENT, " +
            "APPLIES_TO, DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, IS_DEFAULT, CREATED_BY, CREATED_TIME, " +
            "UPDATED_BY, " +
            "LAST_UPDATED_TIME FROM GOV_RULESET WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String GET_RULESET_CONTENT = "SELECT RULESET_CONTENT FROM GOV_RULESET WHERE RULESET_ID = ? " +
            "AND ORGANIZATION = ?";

    public static final String UPDATE_RULESET = "UPDATE GOV_RULESET SET NAME = ?, DESCRIPTION = ?, " +
            "RULESET_CONTENT = ?, APPLIES_TO = ?, DOCUMENTATION_LINK = ?, PROVIDER = ?, " +
            "UPDATED_BY = ?, LAST_UPDATED_TIME = CURRENT_TIMESTAMP WHERE RULESET_ID = ? AND ORGANIZATION = ?";
    public static final String DELETE_RULESET = "DELETE FROM GOV_RULESET WHERE RULESET_ID = ? AND ORGANIZATION = ?";
    public static final String DELETE_RULES = "DELETE FROM GOV_RULESET_RULE WHERE RULESET_ID = ?";

    public static final String CREATE_POLICY = "INSERT INTO GOV_POLICY (POLICY_ID, NAME, DESCRIPTION," +
            "ORGANIZATION, CREATED_BY, CREATED_TIME) VALUES (?, ?, ?, ?, ?, ?)";

    public static final String CREATE_POLICY_RULESET_MAPPING = "INSERT INTO GOV_POLICY_RULESET_MAPPING " +
            "(POLICY_RULESET_MAPPING_ID, POLICY_ID, RULESET_ID) VALUES(?,?,?)";
    public static final String CREATE_GOVERNANCE_POLICY_LABEL = "INSERT INTO GOV_POLICY_LABEL " +
            "(POLICY_LABEL_ID, POLICY_ID, LABEL) VALUES(?,?,?)";

    public static final String GET_RULESETS_BY_IDS = "SELECT * FROM GOV_RULESET WHERE RULESET_ID IN (%s) AND " +
            "ORGANIZATION = ?";
    public static final String DELETE_GOVERNANCE_POLICY_LABEL = "DELETE FROM GOV_POLICY_LABEL " +
            "WHERE POLICY_ID = ? AND LABEL = ?";

    public static final String GET_POLICY_BY_NAME =
            "SELECT policy.POLICY_ID, policy.NAME, policy.DESCRIPTION, " +
                    "policy.CREATED_BY, policy.CREATED_TIME, policy.UPDATED_BY, " +
                    "policy.LAST_UPDATED_TIME FROM GOV_POLICY policy WHERE " +
                    "policy.ORGANIZATION = ? AND policy.NAME = ?";

    public static final String GET_POLICY_BY_ID = "SELECT policy.POLICY_ID, policy.NAME, policy.DESCRIPTION, " +
            "policy.CREATED_BY, policy.CREATED_TIME, policy.UPDATED_BY, policy.LAST_UPDATED_TIME FROM GOV_POLICY " +
            "policy WHERE policy.ORGANIZATION = ? AND policy.POLICY_ID = ?";

    public static final String GET_POLICIES = "SELECT policy.POLICY_ID, policy.NAME, policy.DESCRIPTION, " +
            "policy.CREATED_BY, policy.CREATED_TIME, policy.UPDATED_BY, policy.LAST_UPDATED_TIME " +
            "FROM GOV_POLICY policy WHERE policy.ORGANIZATION = ?";


    public static final String GET_RULESETS_FOR_POLICY = "SELECT RULESET.RULESET_ID, RULESET.NAME, RULESET.DESCRIPTION, " +
            "RULESET.APPLIES_TO, RULESET.DOCUMENTATION_LINK, RULESET.PROVIDER, RULESET.CREATED_BY, " +
            "RULESET.CREATED_TIME, RULESET.UPDATED_BY, RULESET.LAST_UPDATED_TIME, RULESET.IS_DEFAULT " +
            "FROM GOV_POLICY_RULESET_MAPPING MAPPING JOIN GOV_RULESET RULESET " +
            "ON MAPPING.RULESET_ID = RULESET.RULESET_ID WHERE MAPPING.POLICY_ID = ?";

    public static final String GET_LABELS_FOR_POLICY = "SELECT LABEL FROM GOV_POLICY_LABEL  " +
            "    WHERE POLICY_ID = ?";
    public static final String DELETE_GOVERNANCE_POLICY = "DELETE FROM GOV_POLICY WHERE POLICY_ID = ? AND " +
            "ORGANIZATION = ?";
    public static final String UPDATE_POLICY = "UPDATE GOV_POLICY SET NAME = ?, DESCRIPTION = ?, " +
            "UPDATED_BY = ?, LAST_UPDATED_TIME = ? WHERE POLICY_ID = ? AND ORGANIZATION = ?";
    public static final String GET_RULESET_IDS_BY_POLICY_ID = "SELECT RULESET_ID " +
            "FROM GOV_POLICY_RULESET_MAPPING WHERE POLICY_ID = ?";
    public static final String DELETE_POLICY_RULESET_MAPPING = "DELETE FROM GOV_POLICY_RULESET_MAPPING " +
            "WHERE POLICY_ID = ? AND RULESET_ID = ?";
    public static final String GET_LABELS_BY_POLICY_ID = "SELECT LABEL FROM GOV_POLICY_LABEL " +
            "WHERE POLICY_ID = ?";
}
