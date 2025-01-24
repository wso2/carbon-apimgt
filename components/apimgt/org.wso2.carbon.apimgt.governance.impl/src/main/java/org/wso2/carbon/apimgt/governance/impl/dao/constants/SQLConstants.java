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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.dao.constants;

/**
 * This class represents the SQL Constants
 */
public class SQLConstants {

    public static final String CREATE_RULESET =
            "INSERT INTO GOV_RULESET (RULESET_ID, NAME, DESCRIPTION, " +
                    "RULESET_CONTENT, RULE_CATEGORY, RULE_TYPE, ARTIFACT_TYPE, " +
                    "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, CREATED_BY) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String ADD_RULES =
            "INSERT INTO GOV_RULESET_RULE (RULESET_RULE_ID, RULESET_ID, " +
                    "RULE_CODE, RULE_MESSAGE, RULE_DESCRIPTION, SEVERITY, RULE_CONTENT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String GET_RULESETS =
            "SELECT RULESET_ID, NAME, DESCRIPTION, RULESET_CONTENT, RULE_CATEGORY, RULE_TYPE, ARTIFACT_TYPE, " +
                    "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, CREATED_BY, CREATED_TIME, " +
                    "UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_RULESET WHERE ORGANIZATION = ?";

    public static final String GET_RULESET_BY_NAME =
            "SELECT RULESET_ID, NAME, DESCRIPTION, RULESET_CONTENT, RULE_CATEGORY, RULE_TYPE, ARTIFACT_TYPE, " +
                    "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, CREATED_BY, CREATED_TIME, " +
                    "UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_RULESET WHERE NAME = ? AND ORGANIZATION = ?";

    public static final String GET_RULESETS_BY_ID =
            "SELECT RULESET_ID, NAME, DESCRIPTION, RULESET_CONTENT,  RULE_CATEGORY, " +
                    "RULE_TYPE, ARTIFACT_TYPE, DOCUMENTATION_LINK, " +
                    "PROVIDER, ORGANIZATION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_RULESET WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String GET_RULESET_CONTENT =
            "SELECT RULESET_CONTENT FROM GOV_RULESET WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String UPDATE_RULESET =
            "UPDATE GOV_RULESET SET NAME = ?, DESCRIPTION = ?, RULESET_CONTENT = ?, " +
                    "RULE_CATEGORY = ?, RULE_TYPE = ?, ARTIFACT_TYPE = " +
                    "?, DOCUMENTATION_LINK = ?, PROVIDER = ?, UPDATED_BY = ?, " +
                    "LAST_UPDATED_TIME = CURRENT_TIMESTAMP " +
                    "WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String DELETE_RULESET =
            "DELETE FROM GOV_RULESET WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String DELETE_RULES =
            "DELETE FROM GOV_RULESET_RULE WHERE RULESET_ID = ?";

    public static final String GET_RULES_WITHOUT_CONTENT =
            "SELECT RULESET_RULE_ID, RULESET_ID, RULE_CODE, RULE_MESSAGE, RULE_DESCRIPTION, SEVERITY" +
                    "FROM GOV_RULESET_RULE WHERE RULESET_ID = ?";

    public static final String CREATE_POLICY =
            "INSERT INTO GOV_POLICY (POLICY_ID, NAME, DESCRIPTION, " +
                    "ORGANIZATION, CREATED_BY, CREATED_TIME) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    public static final String CREATE_POLICY_RULESET_MAPPING =
            "INSERT INTO GOV_POLICY_RULESET_MAPPING (POLICY_RULESET_MAPPING_ID, " +
                    "POLICY_ID, RULESET_ID) VALUES (?, ?, ?)";

    public static final String CREATE_GOVERNANCE_POLICY_LABEL_MAPPING =
            "INSERT INTO GOV_POLICY_LABEL (POLICY_LABEL_ID, POLICY_ID, LABEL) VALUES (?, ?, ?)";

    public static final String CREATE_GOVERNANCE_POLICY_STATE_MAPPING =
            "INSERT INTO GOV_POLICY_GOVERNABLE_STATE (POLICY_STATE_ID, POLICY_ID, STATE) VALUES (?, ?, ?)";

    public static final String CREATE_GOVERNANCE_POLICY_ACTION_MAPPING =
            "INSERT INTO GOV_POLICY_ACTION (POLICY_ACTION_ID, POLICY_ID, STATE, SEVERITY, TYPE) VALUES (?, ?, ?, ?, ?)";

    public static final String GET_RULESETS_BY_IDS =
            "SELECT * FROM GOV_RULESET WHERE RULESET_ID IN (%s) AND ORGANIZATION = ?";

    public static final String DELETE_GOVERNANCE_POLICY_LABEL_MAPPING =
            "DELETE FROM GOV_POLICY_LABEL WHERE POLICY_ID = ? AND LABEL = ?";

    public static final String DELETE_GOVERNANCE_POLICY_STATE_MAPPING =
            "DELETE FROM GOV_POLICY_GOVERNABLE_STATE WHERE POLICY_ID = ? AND STATE = ?";

    public static final String DELETE_GOVERNANCE_POLICY_ACTION_MAPPING =
            "DELETE FROM GOV_POLICY_ACTION WHERE POLICY_ID = ? AND STATE = ? AND SEVERITY = ? AND TYPE = ?";

    public static final String GET_POLICY_BY_NAME =
            "SELECT POLICY_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_POLICY WHERE ORGANIZATION = ? AND NAME = ?";

    public static final String GET_POLICY_BY_ID =
            "SELECT POLICY_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_POLICY WHERE ORGANIZATION = ? AND POLICY_ID = ?";

    public static final String GET_POLICIES =
            "SELECT POLICY_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_POLICY WHERE ORGANIZATION = ?";

    public static final String GET_RULESETS_IDS_FOR_POLICY =
            "SELECT RULESET.RULESET_ID FROM GOV_POLICY_RULESET_MAPPING MAPPING " +
                    "JOIN GOV_RULESET RULESET ON MAPPING.RULESET_ID = RULESET.RULESET_ID WHERE MAPPING.POLICY_ID = ?";

    public static final String GET_LABELS_FOR_POLICY =
            "SELECT LABEL FROM GOV_POLICY_LABEL WHERE POLICY_ID = ?";

    public static final String DELETE_GOVERNANCE_POLICY =
            "DELETE FROM GOV_POLICY WHERE POLICY_ID = ? AND ORGANIZATION = ?";

    public static final String UPDATE_POLICY =
            "UPDATE GOV_POLICY SET NAME = ?, DESCRIPTION = ?, UPDATED_BY = ?, LAST_UPDATED_TIME = ? " +
                    "WHERE POLICY_ID = ? AND ORGANIZATION = ?";

    public static final String GET_RULESET_IDS_BY_POLICY_ID =
            "SELECT RULESET_ID FROM GOV_POLICY_RULESET_MAPPING WHERE POLICY_ID = ?";

    public static final String DELETE_POLICY_RULESET_MAPPING =
            "DELETE FROM GOV_POLICY_RULESET_MAPPING WHERE POLICY_ID = ? AND RULESET_ID = ?";

    public static final String GET_LABELS_BY_POLICY_ID =
            "SELECT LABEL FROM GOV_POLICY_LABEL WHERE POLICY_ID = ?";

    public static final String GET_STATES_BY_POLICY_ID =
            "SELECT STATE FROM GOV_POLICY_GOVERNABLE_STATE WHERE POLICY_ID = ?";

    public static final String GET_ACTIONS_BY_POLICY_ID =
            "SELECT STATE,SEVERITY,TYPE FROM GOV_POLICY_ACTION WHERE POLICY_ID = ?";

    public static final String GET_POLICIES_FOR_RULESET =
            "SELECT POLICY_ID FROM GOV_POLICY_RULESET_MAPPING WHERE RULESET_ID = ?";

    public static final String GET_RULESETS_BY_POLICY_ID =
            "SELECT RULESET.RULESET_ID, RULESET.RULESET_CONTENT, " +
                    "RULESET.RULE_CATEGORY, RULESET.RULE_TYPE, RULESET.ARTIFACT_TYPE " +
                    "FROM GOV_RULESET RULESET " +
                    "JOIN GOV_POLICY_RULESET_MAPPING POLICY_RULESET_MAPPING ON RULESET.RULESET_ID = " +
                    "POLICY_RULESET_MAPPING.RULESET_ID " +
                    "WHERE POLICY_RULESET_MAPPING.POLICY_ID = ?";

    public static final String GET_POLICIES_BY_LABEL =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID " +
                    "FROM GOV_POLICY " +
                    "LEFT JOIN GOV_POLICY_LABEL ON GOV_POLICY.POLICY_ID = GOV_POLICY_LABEL.POLICY_ID " +
                    "WHERE GOV_POLICY_LABEL.LABEL = ? " +
                    "AND GOV_POLICY.ORGANIZATION = ?";

    public static final String GET_POLICIES_WITHOUT_LABELS =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID " +
                    "FROM GOV_POLICY " +
                    "LEFT JOIN GOV_POLICY_LABEL ON GOV_POLICY.POLICY_ID = GOV_POLICY_LABEL.POLICY_ID " +
                    "WHERE GOV_POLICY_LABEL.POLICY_ID IS NULL " +
                    "AND GOV_POLICY.ORGANIZATION = ?";

    public static final String GET_POLICIES_BY_LABEL_AND_STATE =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID " +
                    "FROM GOV_POLICY " +
                    "LEFT JOIN GOV_POLICY_LABEL ON GOV_POLICY.POLICY_ID = GOV_POLICY_LABEL.POLICY_ID " +
                    "LEFT JOIN GOV_POLICY_GOVERNABLE_STATE ON GOV_POLICY.POLICY_ID = GOV_POLICY_GOVERNABLE_STATE.POLICY_ID " +
                    "WHERE GOV_POLICY_LABEL.LABEL = ? " +
                    "AND GOV_POLICY_GOVERNABLE_STATE.STATE = ? " +
                    "AND GOV_POLICY.ORGANIZATION = ?";

    public static final String GET_POLICIES_WITHOUT_LABELS_BY_STATE =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID " +
                    "FROM GOV_POLICY " +
                    "LEFT JOIN GOV_POLICY_LABEL ON GOV_POLICY.POLICY_ID = GOV_POLICY_LABEL.POLICY_ID " +
                    "LEFT JOIN GOV_POLICY_GOVERNABLE_STATE ON GOV_POLICY.POLICY_ID = GOV_POLICY_GOVERNABLE_STATE.POLICY_ID " +
                    "WHERE GOV_POLICY_LABEL.POLICY_ID IS NULL " +
                    "AND GOV_POLICY_GOVERNABLE_STATE.STATE = ? " +
                    "AND GOV_POLICY.ORGANIZATION = ?";

    public static final String ADD_GOV_ARTIFACT_INFO = "INSERT INTO GOV_ARTIFACT_INFO " +
            "(ARTIFACT_ID, ARTIFACT_TYPE, ORGANIZATION) " +
            "VALUES (?, ?, ?)";

    public static final String ADD_GOV_EVALUATION_REQUEST = "INSERT INTO GOV_EVALUATION_REQUEST " +
            "(REQUEST_ID, ARTIFACT_ID, ARTIFACT_TYPE, POLICY_ID) " +
            "VALUES (?, ?, ?, ?)";

    public static final String GET_PENDING_EVALUATION_REQUESTS =
            "SELECT " +
                    "ER.REQUEST_ID, ER.ARTIFACT_ID, ER.ARTIFACT_TYPE, ER.POLICY_ID, AI.ORGANIZATION " +
                    "FROM GOV_EVALUATION_REQUEST ER " +
                    "LEFT JOIN GOV_ARTIFACT_INFO AI ON ER.ARTIFACT_ID = AI.ARTIFACT_ID " +
                    "WHERE ER.EVALUATION_STATUS = 'PENDING'";

    public static final String UPDATE_GOV_EVALUATION_REQUEST_STATUS = "UPDATE GOV_EVALUATION_REQUEST " +
            "SET EVALUATION_STATUS = ? WHERE REQUEST_ID = ?";

    public static final String DELETE_GOV_EVALUATION_REQUEST = "DELETE FROM GOV_EVALUATION_REQUEST" +
            " WHERE REQUEST_ID = ?";

    public static final String DELETE_GOV_EVALUATION_REQUEST_BY_ARTIFACT = "DELETE FROM GOV_EVALUATION_REQUEST WHERE " +
            "ARTIFACT_ID = ?";

    public static final String ADD_GOV_COMPLIANCE_EVALUATION_RESULT = "INSERT INTO GOV_EVALUATION_RESULT " +
            "(RESULT_ID, ARTIFACT_ID, POLICY_ID, RULESET_ID, EVALUATION_RESULT) " +
            "VALUES (?, ?, ?, ?, ?)";

    public static final String GET_GOV_COMPLIANCE_EVALUATION_RESULT = "SELECT * FROM GOV_EVALUATION_RESULT " +
            "WHERE ARTIFACT_ID = ? AND POLICY_ID = ? AND RULESET_ID = ?";

    public static final String GET_GOV_COMPLIANCE_EVALUATION_RESULTS_FOR_ARTIFACT = "SELECT * FROM " +
            "GOV_COMPLIANCE_EVALUATION_RESULT " +
            "WHERE ARTIFACT_ID = ?";

    public static final String GET_GOV_COMPLIANCE_EVALUATION_RESULTS_BY_ARTIFACT_AND_POLICY = "SELECT * FROM " +
            "GOV_COMPLIANCE_EVALUATION_RESULT " +
            "WHERE ARTIFACT_ID = ? AND POLICY_ID = ?";

    public static final String GET_ALL_COMP_EVALUATED_ARTIFACTS = "SELECT DISTINCT ARTIFACT_ID FROM " +
            "GOV_COMPLIANCE_EVALUATION_RESULT LEFT JOIN ARTIFACT_INFO ON " +
            "GOV_COMPLIANCE_EVALUATION_RESULT.ARTIFACT_ID = ARTIFACT_INFO.ARTIFACT_ID " +
            "WHERE ARTIFACT_INFO.ARTIFACT_TYPE = ? AND ARTIFACT_INFO.ORGANIZATION = ?";

    public static final String GET_NON_COMPLIANT_ARTIFACTS = "SELECT DISTINCT ARTIFACT_ID FROM " +
            "GOV_COMPLIANCE_EVALUATION_RESULT LEFT JOIN ARTIFACT_INFO ON " +
            "GOV_COMPLIANCE_EVALUATION_RESULT.ARTIFACT_ID = ARTIFACT_INFO.ARTIFACT_ID " +
            "WHERE ARTIFACT_INFO.ARTIFACT_TYPE = ? AND ARTIFACT_INFO.ORGANIZATION = ? AND " +
            "GOV_COMPLIANCE_EVALUATION_RESULT.EVALUATION_RESULT = 0 ";

    public static final String GET_EVALUATED_POLICIES_BY_ARTIFACT = "SELECT DISTINCT POLICY_ID FROM " +
            "GOV_COMPLIANCE_EVALUATION_RESULT WHERE ARTIFACT_ID = ?";

    public static final String DELETE_GOV_COMPLIANCE_EVALUATION_RESULT = "DELETE FROM GOV_EVALUATION_RESULT " +
            "WHERE ARTIFACT_ID = ? AND POLICY_ID = ? AND RULESET_ID = ?";

    public static final String ADD_RULE_VIOLATION = "INSERT INTO GOV_RULE_VIOLATION " +
            "(VIOLATION_ID, ARTIFACT_ID, POLICY_ID, RULESET_ID, RULE_CODE, VIOLATED_PATH) " +
            "VALUES (?, ?, ?, ?, ?, ?)";


    public static final String GET_RULE_VIOLATIONS = "SELECT GV.ARTIFACT_ID, GV.POLICY_ID, " +
            "GV.RULESET_ID, GV.RULE_CODE, GRR.SEVERITY " +
            "FROM GOV_RULE_VIOLATION GV " +
            "LEFT JOIN GOV_RULESET_RULE GRR " +
            "ON GV.RULESET_ID = GRR.RULESET_ID AND GV.RULE_CODE = GRR.RULE_CODE " +
            "WHERE GV.ARTIFACT_ID = ? AND GV.POLICY_ID = ? AND GV.RULESET_ID = ?";

    public static final String GET_RULE_VIOLATIONS_BY_ARTIFACT = "SELECT GV.ARTIFACT_ID, GV.POLICY_ID, " +
            "GV.RULESET_ID, GV.RULE_CODE, GRR.SEVERITY " +
            "FROM GOV_RULE_VIOLATION GV " +
            "LEFT JOIN GOV_RULESET_RULE GRR " +
            "ON GV.RULESET_ID = GRR.RULESET_ID AND GV.RULE_CODE = GRR.RULE_CODE " +
            "WHERE GV.ARTIFACT_ID = ?";

    public static final String DELETE_RULE_VIOLATION = "DELETE FROM GOV_RULE_VIOLATION " +
            "WHERE ARTIFACT_ID = ? AND POLICY_ID = ?";

    public static final String GET_ARTIFACT_INFO = "SELECT * FROM GOV_ARTIFACT_INFO WHERE ARTIFACT_ID = ?";

}
