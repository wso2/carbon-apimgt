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
                    "RULE_CATEGORY, RULE_TYPE, ARTIFACT_TYPE, " +
                    "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, CREATED_BY, CREATED_TIME) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String ADD_RULESET_CONTENT =
            "INSERT INTO GOV_RULESET_CONTENT(RULESET_ID, CONTENT, CONTENT_TYPE, FILE_NAME) " +
                    "VALUES (?, ?, ?, ?)";

    public static final String ADD_RULES =
            "INSERT INTO GOV_RULESET_RULE (RULESET_RULE_ID, RULESET_ID, " +
                    "RULE_NAME, RULE_DESCRIPTION, SEVERITY, RULE_CONTENT) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    public static final String GET_RULESETS =
            "SELECT RULESET_ID, NAME, DESCRIPTION, RULE_CATEGORY, RULE_TYPE, ARTIFACT_TYPE, " +
                    "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, CREATED_BY, CREATED_TIME, " +
                    "UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_RULESET WHERE ORGANIZATION = ?";

    public static final String GET_RULESET_BY_NAME =
            "SELECT RULESET_ID, NAME, DESCRIPTION, RULE_CATEGORY, RULE_TYPE, ARTIFACT_TYPE, " +
                    "DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, CREATED_BY, CREATED_TIME, " +
                    "UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_RULESET WHERE NAME = ? AND ORGANIZATION = ?";

    public static final String GET_RULESETS_BY_ID =
            "SELECT RULESET_ID, NAME, DESCRIPTION,  RULE_CATEGORY, " +
                    "RULE_TYPE, ARTIFACT_TYPE, DOCUMENTATION_LINK, " +
                    "PROVIDER, ORGANIZATION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME " +
                    "FROM GOV_RULESET WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String GET_RULESET_CONTENT =
            "SELECT CONTENT, CONTENT_TYPE, FILE_NAME FROM GOV_RULESET_CONTENT " +
                    "JOIN GOV_RULESET ON GOV_RULESET_CONTENT.RULESET_ID = GOV_RULESET.RULESET_ID " +
                    "WHERE GOV_RULESET_CONTENT.RULESET_ID = ? AND GOV_RULESET.ORGANIZATION = ?";

    public static final String UPDATE_RULESET =
            "UPDATE GOV_RULESET SET NAME = ?, DESCRIPTION = ?, " +
                    "RULE_CATEGORY = ?, RULE_TYPE = ?, ARTIFACT_TYPE = " +
                    "?, DOCUMENTATION_LINK = ?, PROVIDER = ?, UPDATED_BY = ?, " +
                    "LAST_UPDATED_TIME = ? " +
                    "WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String UPDATE_RULESET_CONTENT =
            "UPDATE GOV_RULESET_CONTENT SET CONTENT = ?, CONTENT_TYPE = ?, FILE_NAME = ? " +
                    "WHERE RULESET_ID = ?";

    public static final String SEARCH_RULESETS = "SELECT RULESET_ID, NAME, " +
            "DESCRIPTION, RULE_CATEGORY, " +
            "RULE_TYPE, ARTIFACT_TYPE, DOCUMENTATION_LINK, PROVIDER, ORGANIZATION, " +
            "CREATED_BY, CREATED_TIME, " +
            "UPDATED_BY, LAST_UPDATED_TIME FROM GOV_RULESET WHERE ORGANIZATION = ? " +
            "AND NAME LIKE %?% AND RULE_TYPE LIKE %?% " +
            "AND ARTIFACT_TYPE LIKE %?%";

    public static final String DELETE_RULESET =
            "DELETE FROM GOV_RULESET WHERE RULESET_ID = ? AND ORGANIZATION = ?";

    public static final String DELETE_RULES =
            "DELETE FROM GOV_RULESET_RULE WHERE RULESET_ID = ?";

    public static final String DELETE_RULESET_CONTENT =
            "DELETE FROM GOV_RULESET_CONTENT WHERE RULESET_ID = ?";

    public static final String GET_RULES_WITHOUT_CONTENT =
            "SELECT RR.RULESET_RULE_ID, RR.RULESET_ID, RR.RULE_NAME, " +
                    "RR.RULE_DESCRIPTION, RR.SEVERITY " +
                    "FROM GOV_RULESET_RULE RR JOIN GOV_RULESET RS ON RR.RULESET_ID = RS.RULESET_ID " +
                    "WHERE RR.RULESET_ID = ? AND RS.ORGANIZATION = ?";

    public static final String CREATE_POLICY =
            "INSERT INTO GOV_POLICY (POLICY_ID, NAME, DESCRIPTION, " +
                    "ORGANIZATION, CREATED_BY, IS_GLOBAL, CREATED_TIME) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String CREATE_POLICY_RULESET_MAPPING =
            "INSERT INTO GOV_POLICY_RULESET (POLICY_ID, RULESET_ID) VALUES (?, ?)";

    public static final String CREATE_GOVERNANCE_POLICY_LABEL_MAPPING =
            "INSERT INTO GOV_POLICY_LABEL (POLICY_ID, LABEL) VALUES (?, ?)";

    public static final String CREATE_GOVERNANCE_POLICY_STATE_MAPPING =
            "INSERT INTO GOV_POLICY_GOVERNABLE_STATE (POLICY_ID, STATE) VALUES (?, ?)";

    public static final String CREATE_GOVERNANCE_POLICY_ACTION_MAPPING =
            "INSERT INTO GOV_POLICY_ACTION (POLICY_ID, STATE, SEVERITY, TYPE) VALUES (?, ?, ?, ?)";

    public static final String DELETE_GOVERNANCE_POLICY =
            "DELETE FROM GOV_POLICY WHERE POLICY_ID = ? AND ORGANIZATION = ?";

    public static final String DELETE_GOVERNANCE_POLICY_LABEL_MAPPING_BY_POLICY_ID =
            "DELETE FROM GOV_POLICY_LABEL WHERE POLICY_ID = ?";

    public static final String DELETE_GOVERNANCE_POLICY_STATE_MAPPING_BY_POLICY_ID =
            "DELETE FROM GOV_POLICY_GOVERNABLE_STATE WHERE POLICY_ID = ?";

    public static final String DELETE_POLICY_RULESET_MAPPING_BY_POLICY_ID =
            "DELETE FROM GOV_POLICY_RULESET WHERE POLICY_ID = ?";

    public static final String DELETE_GOVERNANCE_POLICY_ACTION_MAPPING_BY_POLICY_ID =
            "DELETE FROM GOV_POLICY_ACTION WHERE POLICY_ID = ?";

    public static final String DELETE_GOVERNANCE_POLICIES_BY_LABEL =
            "DELETE FROM GOV_POLICY_LABEL WHERE LABEL = ? AND ORGANIZATION = ?";

    public static final String GET_POLICY_BY_NAME =
            "SELECT POLICY_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME, IS_GLOBAL " +
                    "FROM GOV_POLICY WHERE ORGANIZATION = ? AND NAME = ?";

    public static final String GET_POLICY_BY_ID =
            "SELECT POLICY_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME, IS_GLOBAL " +
                    "FROM GOV_POLICY WHERE POLICY_ID = ? AND ORGANIZATION = ?";

    public static final String GET_POLICIES =
            "SELECT POLICY_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME, IS_GLOBAL " +
                    "FROM GOV_POLICY WHERE ORGANIZATION = ?";

    public static final String SEARCH_POLICIES =
            "SELECT DISTINCT GP.POLICY_ID, GP.NAME, GP.DESCRIPTION, GP.CREATED_BY, GP.CREATED_TIME, " +
                    "GP.UPDATED_BY, GP.LAST_UPDATED_TIME, GP.IS_GLOBAL " +
                    "FROM GOV_POLICY GP " +
                    "JOIN GOV_POLICY_LABEL GPL ON GP.POLICY_ID = GPL.POLICY_ID " +
                    "JOIN GOV_POLICY_GOVERNABLE_STATE GPS ON GP.POLICY_ID = GPS.POLICY_ID " +
                    "WHERE GP.ORGANIZATION = ? " +
                    "AND GP.NAME LIKE %?% " +
                    "AND GPS.STATE LIKE %?%";

    public static final String UPDATE_POLICY =
            "UPDATE GOV_POLICY SET NAME = ?, DESCRIPTION = ?, UPDATED_BY = ?, IS_GLOBAL = ?, " +
                    "LAST_UPDATED_TIME = ? " +
                    "WHERE POLICY_ID = ? AND ORGANIZATION = ?";

    public static final String GET_RULESET_IDS_BY_POLICY_ID =
            "SELECT RULESET_ID FROM GOV_POLICY_RULESET WHERE POLICY_ID = ?";

    public static final String GET_LABELS_BY_POLICY_ID =
            "SELECT LABEL FROM GOV_POLICY_LABEL WHERE POLICY_ID = ?";

    public static final String GET_STATES_BY_POLICY_ID =
            "SELECT STATE FROM GOV_POLICY_GOVERNABLE_STATE WHERE POLICY_ID = ?";

    public static final String GET_ACTIONS_BY_POLICY_ID =
            "SELECT STATE,SEVERITY,TYPE FROM GOV_POLICY_ACTION WHERE POLICY_ID = ?";

    public static final String GET_POLICIES_FOR_RULESET =
            "SELECT POLICY_ID FROM GOV_POLICY_RULESET GPR " +
                    "JOIN GOV_RULESET GR ON GPR.RULESET_ID = GR.RULESET_ID " +
                    "WHERE GPR.RULESET_ID = ? AND GR.ORGANIZATION = ?";

    public static final String GET_RULESETS_BY_POLICY_ID =
            "SELECT RULESET.RULESET_ID, RULESET.NAME, " +
                    "RULESET.RULE_CATEGORY, RULESET.RULE_TYPE, RULESET.ARTIFACT_TYPE " +
                    "FROM GOV_RULESET RULESET " +
                    "JOIN GOV_POLICY_RULESET POLICY_RULESET_MAPPING ON RULESET.RULESET_ID = " +
                    "POLICY_RULESET_MAPPING.RULESET_ID " +
                    "WHERE POLICY_RULESET_MAPPING.POLICY_ID = ? AND RULESET.ORGANIZATION = ?";
    public static final String GET_RULESETS_WITH_CONTENT_BY_POLICY_ID =
            "SELECT RULESET.RULESET_ID, RULESET.NAME, " +
                    "RULESET.RULE_CATEGORY, RULESET.RULE_TYPE, RULESET.ARTIFACT_TYPE, " +
                    "RC.CONTENT, RC.CONTENT_TYPE, RC.FILE_NAME " +
                    "FROM GOV_RULESET RULESET " +
                    "JOIN GOV_RULESET_CONTENT RC " +
                    "ON RULESET.RULESET_ID = RC.RULESET_ID " +
                    "JOIN GOV_POLICY_RULESET GPR " +
                    "ON RULESET.RULESET_ID = GPR.RULESET_ID " +
                    "WHERE GPR.POLICY_ID = ? AND RULESET.ORGANIZATION = ?";
    public static final String GET_POLICIES_BY_LABEL =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID, GOV_POLICY.NAME " +
                    "FROM GOV_POLICY " +
                    "JOIN GOV_POLICY_LABEL ON GOV_POLICY.POLICY_ID = GOV_POLICY_LABEL.POLICY_ID " +
                    "WHERE GOV_POLICY_LABEL.LABEL = ? " +
                    "AND GOV_POLICY.ORGANIZATION = ?";

    public static final String GET_GLOBAL_POLICIES =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID, GOV_POLICY.NAME " +
                    "FROM GOV_POLICY " +
                    "WHERE GOV_POLICY.ORGANIZATION = ? " +
                    "AND GOV_POLICY.IS_GLOBAL = 1";

    public static final String GET_POLICIES_BY_LABEL_AND_STATE =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID " +
                    "FROM GOV_POLICY " +
                    "JOIN GOV_POLICY_LABEL ON GOV_POLICY.POLICY_ID = GOV_POLICY_LABEL.POLICY_ID " +
                    "JOIN GOV_POLICY_GOVERNABLE_STATE ON GOV_POLICY.POLICY_ID = " +
                    "GOV_POLICY_GOVERNABLE_STATE.POLICY_ID " +
                    "WHERE GOV_POLICY_LABEL.LABEL = ? " +
                    "AND GOV_POLICY_GOVERNABLE_STATE.STATE = ? " +
                    "AND GOV_POLICY.ORGANIZATION = ?";

    public static final String GET_GLOBAL_POLICIES_BY_STATE =
            "SELECT DISTINCT GOV_POLICY.POLICY_ID " +
                    "FROM GOV_POLICY " +
                    "JOIN GOV_POLICY_GOVERNABLE_STATE ON GOV_POLICY.POLICY_ID = " +
                    "GOV_POLICY_GOVERNABLE_STATE.POLICY_ID " +
                    "WHERE GOV_POLICY_GOVERNABLE_STATE.STATE = ? " +
                    "AND GOV_POLICY.ORGANIZATION = ? " +
                    "AND GOV_POLICY.IS_GLOBAL = 1";

    public static final String ADD_GOV_ARTIFACT = "INSERT INTO GOV_ARTIFACT " +
            "(ARTIFACT_KEY, ARTIFACT_REF_ID, ARTIFACT_TYPE, ORGANIZATION) VALUES (?, ?, ?, ?)";

    public static final String DELETE_GOV_ARTIFACT = "DELETE FROM GOV_ARTIFACT WHERE " +
            "ARTIFACT_REF_ID = ? AND ARTIFACT_TYPE = ? AND ORGANIZATION = ?";

    public static final String GET_ARTIFACT_KEY = "SELECT ARTIFACT_KEY " +
            "FROM GOV_ARTIFACT WHERE ARTIFACT_REF_ID = ? AND ARTIFACT_TYPE = ? AND ORGANIZATION = ?";

    public static final String GET_PENDING_REQ_FOR_ARTIFACT = "SELECT REQ_ID FROM GOV_REQUEST GR " +
            "JOIN GOV_ARTIFACT GA ON GR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? AND GR.STATUS = 'PENDING'";

    public static final String GET_COMPLIANCE_PENDING_ARTIFACTS = "SELECT DISTINCT GA.ARTIFACT_REF_ID " +
            "FROM GOV_ARTIFACT GA " +
            "JOIN GOV_REQUEST GR ON GA.ARTIFACT_KEY = GR.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? AND GR.STATUS = 'PENDING'";
    public static final String ADD_GOV_EVAL_REQ = "INSERT INTO GOV_REQUEST " +
            "(REQ_ID, ARTIFACT_KEY, REQ_TIMESTAMP) VALUES (?, ?, ?)";

    public static final String ADD_REQ_POLICY_MAPPING = "INSERT INTO GOV_REQUEST_POLICY " +
            "(REQ_ID, POLICY_ID) VALUES (?, ?)";

    public static final String GET_PENDING_REQ =
            "SELECT REQ_ID, ARTIFACT_REF_ID, ARTIFACT_TYPE, ORGANIZATION FROM GOV_REQUEST GR " +
                    "JOIN GOV_ARTIFACT GA ON GR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
                    "WHERE GR.STATUS = 'PENDING'";

    public static final String GET_REQ_POLICY_MAPPING = "SELECT POLICY_ID FROM GOV_REQUEST_POLICY " +
            "WHERE REQ_ID = ?";

    public static final String UPDATE_GOV_REQ_STATUS_TO_PROCESSING = "UPDATE GOV_REQUEST " +
            "SET STATUS = 'PROCESSING', PROCESSING_TIMESTAMP = ? WHERE REQ_ID = ?" +
            "AND STATUS = 'PENDING'";

    public static final String UPDATE_GOV_REQ_STATUS_FROM_PROCESSING_TO_PENDING = "UPDATE GOV_REQUEST " +
            "SET STATUS = 'PENDING', PROCESSING_TIMESTAMP = NULL WHERE STATUS = 'PROCESSING'";

    public static final String DELETE_GOV_REQ = "DELETE FROM GOV_REQUEST" +
            " WHERE REQ_ID = ?";

    public static final String DELETE_REQ_POLICY_MAPPING = "DELETE FROM GOV_REQUEST_POLICY " +
            "WHERE REQ_ID = ?";

    public static final String DELETE_GOV_REQ_FOR_ARTIFACT = "DELETE FROM GOV_REQUEST " +
            "WHERE ARTIFACT_KEY IN (" +
            "    SELECT GA.ARTIFACT_KEY " +
            "    FROM GOV_ARTIFACT GA " +
            "    WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ?" +
            ")";


    public static final String DELETE_REQ_POLICY_MAPPING_FOR_ARTIFACT =
            "DELETE FROM GOV_REQUEST_POLICY " +
                    "WHERE REQ_ID = (" +
                    "    SELECT GR.REQ_ID " +
                    "    FROM GOV_REQUEST GR " +
                    "    JOIN GOV_ARTIFACT GA ON GR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
                    "    WHERE GA.ARTIFACT_REF_ID = ? " +
                    "    AND GA.ARTIFACT_TYPE = ? " +
                    "    AND GA.ORGANIZATION = ? " +
                    "    LIMIT 1" +
                    ")";


    public static final String DELETE_REQ_POLICY_MAPPING_FOR_POLICY = "DELETE FROM " +
            "GOV_REQUEST_POLICY WHERE POLICY_ID = ?";

    public static final String ADD_POLICY_RUN = "INSERT INTO GOV_POLICY_RUN (ARTIFACT_KEY, POLICY_ID, RUN_TIMESTAMP) " +
            "VALUES (?, ?, ?)";

    public static final String ADD_RULESET_RUN = "INSERT INTO GOV_RULESET_RUN (RULESET_RUN_ID, " +
            "ARTIFACT_KEY, RULESET_ID, RESULT, RUN_TIMESTAMP) VALUES (?, ?, ?, ?, ?)";

    public static final String ADD_RULE_VIOLATION = "INSERT INTO GOV_RULE_VIOLATION (ID, RULESET_RUN_ID, " +
            "RULESET_ID, RULE_NAME, VIOLATED_PATH, MESSAGE) VALUES (?, ?, ?, ?, ?, ?)";

    public static final String DELETE_POLICY_RUN_FOR_ARTIFACT_AND_POLICY = "DELETE FROM GOV_POLICY_RUN " +
            "WHERE ARTIFACT_KEY IN ( SELECT ARTIFACT_KEY FROM GOV_ARTIFACT WHERE ARTIFACT_REF_ID = ? " +
            "AND ARTIFACT_TYPE = ? AND ORGANIZATION = ? ) AND POLICY_ID = ?";

    public static final String DELETE_POLICY_RUN_FOR_POLICY = "DELETE FROM GOV_POLICY_RUN " +
            "WHERE POLICY_ID = ?";

    public static final String GET_POLICY_RUNS_FOR_ARTIFACT = "SELECT DISTINCT POLICY_ID FROM GOV_POLICY_RUN GPR " +
            "JOIN GOV_ARTIFACT GA ON GPR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ?";

    public static final String GET_POLICY_RUNS = "SELECT DISTINCT POLICY_ID FROM GOV_POLICY_RUN GPR " +
            "JOIN GOV_ARTIFACT GA ON GPR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ORGANIZATION = ?";

    public static final String DELETE_POLICY_RUNS_FOR_ARTIFACT = "DELETE FROM GOV_POLICY_RUN WHERE ARTIFACT_KEY " +
            "IN ( SELECT ARTIFACT_KEY FROM GOV_ARTIFACT WHERE ARTIFACT_REF_ID = ? " +
            "AND ARTIFACT_TYPE = ? AND ORGANIZATION = ? )";

    public static final String DELETE_RULESET_RUN_FOR_ARTIFACT_AND_RULESET = "DELETE FROM GOV_RULESET_RUN " +
            "WHERE ARTIFACT_KEY IN ( SELECT GA.ARTIFACT_KEY FROM GOV_ARTIFACT GA WHERE GA.ARTIFACT_REF_ID = ? " +
            "AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? ) AND RULESET_ID = ?";

    public static final String DELETE_RULESET_RUN_FOR_RULESET = "DELETE FROM GOV_RULESET_RUN " +
            "WHERE RULESET_ID = ?";
    public static final String GET_RULESET_RUNS_FOR_ARTIFACT = "SELECT DISTINCT RULESET_ID " +
            "FROM GOV_RULESET_RUN GRR " +
            "JOIN GOV_ARTIFACT GA ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ?";

    public static final String GET_RULESET_RUN_FOR_ARTIFACT_AND_RULESET = "SELECT DISTINCT " +
            "RULESET_ID FROM GOV_RULESET_RUN GRR " +
            "JOIN GOV_ARTIFACT GA ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? AND GRR.RULESET_ID = ?";

    public static final String GET_FAILED_RULESET_RUNS = "SELECT DISTINCT RULESET_ID FROM GOV_RULESET_RUN GRR " +
            "JOIN GOV_ARTIFACT GA ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ORGANIZATION = ? AND GRR.RESULT = 0";

    public static final String GET_FAILED_RULESET_RUNS_FOR_ARTIFACT = "SELECT DISTINCT RULESET_ID " +
            "FROM GOV_RULESET_RUN GRR " +
            "JOIN GOV_ARTIFACT GA ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? AND GRR.RESULT = 0";
    public static final String DELETE_RULESET_RUNS_FOR_ARTIFACT = "DELETE FROM GOV_RULESET_RUN " +
            "WHERE ARTIFACT_KEY IN (SELECT ARTIFACT_KEY FROM GOV_ARTIFACT WHERE ARTIFACT_REF_ID = ? " +
            "AND ARTIFACT_TYPE = ? AND ORGANIZATION = ?);";

    public static final String DELETE_RULE_VIOLATIONS_FOR_ARTIFACT_AND_RULESET = "DELETE FROM GOV_RULE_VIOLATION " +
            "WHERE RULESET_RUN_ID IN ( SELECT GRR.RULESET_RUN_ID FROM GOV_RULESET_RUN GRR JOIN GOV_ARTIFACT GA " +
            "ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? AND GRR.RULESET_ID = ? );";

    public static final String DELETE_RULE_VIOLATIONS_FOR_RULESET = "DELETE FROM GOV_RULE_VIOLATION " +
            "WHERE RULESET_ID = ?";

    public static final String DELETE_RULE_VIOLATIONS_FOR_ARTIFACT = "DELETE FROM GOV_RULE_VIOLATION " +
            "WHERE RULESET_RUN_ID IN ( SELECT GRR.RULESET_RUN_ID FROM GOV_RULESET_RUN GRR " +
            "INNER JOIN GOV_ARTIFACT GA ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY WHERE GA.ARTIFACT_REF_ID = ? " +
            "AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? );";

    public static final String GET_RULE_VIOLATIONS =
            "SELECT DISTINCT GV.RULE_NAME, GV.VIOLATED_PATH, GV.MESSAGE, GRULE.SEVERITY " +
                    "FROM GOV_RULE_VIOLATION GV " +
                    "JOIN GOV_RULESET_RUN GRR ON GV.RULESET_RUN_ID = GRR.RULESET_RUN_ID " +
                    "JOIN GOV_ARTIFACT GA ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
                    "JOIN GOV_RULESET_RULE GRULE ON GRR.RULESET_ID = GRULE.RULESET_ID " +
                    "AND GV.RULE_NAME = GRULE.RULE_NAME " +
                    "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? AND " +
                    "GRR.RULESET_ID = ?";

    public static final String GET_RULE_VIOLATIONS_FOR_ARTIFACT =
            "SELECT DISTINCT GV.RULESET_ID, GV.RULE_NAME, GV.VIOLATED_PATH, GV.MESSAGE, GRULE.SEVERITY " +
                    "FROM GOV_RULE_VIOLATION GV " +
                    "JOIN GOV_RULESET_RUN GRR ON GV.RULESET_RUN_ID = GRR.RULESET_RUN_ID " +
                    "JOIN GOV_ARTIFACT GA ON GRR.ARTIFACT_KEY = GA.ARTIFACT_KEY " +
                    "JOIN GOV_RULESET_RULE GRULE ON GRR.RULESET_ID = GRULE.RULESET_ID " +
                    "AND GV.RULE_NAME = GRULE.RULE_NAME " +
                    "WHERE GA.ARTIFACT_REF_ID = ? AND GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ?";

    public static final String GET_ALL_EVALUTED_ARTIFACTS = "SELECT DISTINCT GA.ARTIFACT_REF_ID " +
            "FROM GOV_ARTIFACT GA " +
            "JOIN GOV_POLICY_RUN GPR ON GA.ARTIFACT_KEY = GPR.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? ";

    public static final String GET_NON_COMPLIANT_ARTIFACTS = "SELECT DISTINCT GA.ARTIFACT_REF_ID " +
            "FROM GOV_ARTIFACT GA " +
            "JOIN GOV_POLICY_RUN GPR ON GA.ARTIFACT_KEY = GPR.ARTIFACT_KEY " +
            "JOIN GOV_RULESET_RUN GRR ON GA.ARTIFACT_KEY = GRR.ARTIFACT_KEY " +
            "WHERE GA.ARTIFACT_TYPE = ? AND GA.ORGANIZATION = ? AND GRR.RESULT = 0";

    public static final String GET_ARITFCATS_FOR_POLICY_RUN = "SELECT DISTINCT GA.ARTIFACT_REF_ID, GA.ARTIFACT_TYPE " +
            "FROM GOV_ARTIFACT GA " +
            "JOIN GOV_POLICY_RUN GPR ON GA.ARTIFACT_KEY = GPR.ARTIFACT_KEY " +
            "WHERE GPR.POLICY_ID = ? AND GA.ORGANIZATION = ?";


}
