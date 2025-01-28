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

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the compliance information of an artifact
 */
public class ArtifactComplianceInfo {

    boolean isBlockingNecessary;
    List<RuleViolation> blockingRuleViolations = new ArrayList<>();
    List<RuleViolation> nonBlockingViolations = new ArrayList<>();

    public boolean isBlockingNecessary() {
        return isBlockingNecessary;
    }

    public void setBlockingNecessary(boolean blockingNecessary) {
        isBlockingNecessary = blockingNecessary;
    }

    public List<RuleViolation> getBlockingRuleViolations() {
        return blockingRuleViolations;
    }

    public void addBlockingViolations(List<RuleViolation> blockingRuleViolations) {
        this.blockingRuleViolations.addAll(blockingRuleViolations);
    }

    public List<RuleViolation> getNonBlockingViolations() {
        return nonBlockingViolations;
    }

    public void addNonBlockingViolations(List<RuleViolation> nonBlockingViolations) {
        this.nonBlockingViolations.addAll(nonBlockingViolations);
    }
}
