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

import java.io.File;

/**
 * This class represents the Governance Constants
 */
public class GovernanceConstants {

    public static final String YAML_FILE_TYPE = ".yaml";
    public static final String MIGRATE = "migrate";

    public static final String DEFINITIONS_FOLDER = "Definitions/";
    public static final String SWAGGER_FILE_NAME = "swagger.yaml";
    public static final String ASYNC_API_FILE_NAME = "asyncapi.yaml";
    public static final String API_FILE_NAME = "api.yaml";

    public static final String DEFAULT_RULESET_LOCATION = "repository" + File.separator
            + "resources" + File.separator + "governance" + File.separator + "default-rulesets";
    public static final String GLOBAL_LABEL = "GLOBAL";

}
