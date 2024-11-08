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

package org.wso2.carbon.apimgt.governance.api.manager;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;

/**
 * This interface represents the Governance Ruleset Manager
 */
public interface RulesetManager {

    /**
     * Create a new Governance Ruleset
     *
     * @param organization Organization
     * @param ruleset      Ruleset object
     * @return Ruleset Created object
     * @throws GovernanceException If an error occurs while creating the ruleset
     */
    Ruleset createNewRuleset(String organization, Ruleset ruleset) throws GovernanceException;

    /**
     * Get all the Governance Rulesets
     *
     * @param organization Organization
     * @return RulesetList object
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    RulesetList getRulesets(String organization) throws GovernanceException;
}
