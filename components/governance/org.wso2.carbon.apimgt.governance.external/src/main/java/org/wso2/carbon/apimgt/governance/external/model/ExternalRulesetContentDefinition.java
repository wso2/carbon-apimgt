/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.external.model;

import java.util.Map;

/**
 * Rule container for the external governance ruleset.
 */
public class ExternalRulesetContentDefinition {

    private Map<String, ExternalRuleDefinition> rules;

    public Map<String, ExternalRuleDefinition> getRules() {

        return rules;
    }

    public void setRules(Map<String, ExternalRuleDefinition> rules) {

        this.rules = rules;
    }
}
