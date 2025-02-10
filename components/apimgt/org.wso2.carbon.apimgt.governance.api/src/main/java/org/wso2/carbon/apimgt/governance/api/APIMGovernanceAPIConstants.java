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

package org.wso2.carbon.apimgt.governance.api;

/**
 * This class represents a governance API constants
 */
public class APIMGovernanceAPIConstants {
    public static final String ORGANIZATION = "organization";
    public static final String RULESET_PATH = "/rulesets";
    public static final String POLICY_PATH = "/policies";
    public static final String ARTIFACT_COMPLIANCE_PATH = "/artifact-compliance";
    public static final String POLICY_ADHERENCE_PATH = "/policy-adherence";

    public static final String LIMIT_PARAM = "{limit}";
    public static final String OFFSET_PARAM = "{offset}";
    public static final String QUERY_PARAM = "{query}";
    public static final String ARTIFACT_TYPE_PARAM = "{artifactType}";

    public static final String RULESETS_GET_URL =
            RULESET_PATH + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&query=" + QUERY_PARAM;

    public static final String POLICIES_GET_URL =
            POLICY_PATH + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&query=" + QUERY_PARAM;

    public static final String ARTIFACT_COMPLIANCE_GET_URL =
            ARTIFACT_COMPLIANCE_PATH + "/" + ARTIFACT_TYPE_PARAM + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String POLICY_ADHERENCE_GET_URL =
            POLICY_ADHERENCE_PATH + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;
    public static final String YAML_FILE_TYPE = ".yaml";
    public static final String YML_FILE_TYPE = ".yml";
    public static final String JSON_FILE_TYPE = ".json";
    public static final String PATH_SEPARATOR = "/";
    public static final String DELEM_UNDERSCORE = "_";

}

