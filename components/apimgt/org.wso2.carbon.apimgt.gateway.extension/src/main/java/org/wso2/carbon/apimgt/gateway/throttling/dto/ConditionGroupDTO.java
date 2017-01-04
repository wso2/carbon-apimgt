/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.throttling.dto;

import java.util.Arrays;

/**
 * DTO used to represent a ConditionGroup.
 * This is analogous to {@code Pipeline} class and has a subset of attributes defined in that.
 */
public class ConditionGroupDTO {

    private String conditionGroupId;
    private ConditionDTO [] conditions;

    public ConditionGroupDTO() {
        conditions = new ConditionDTO[]{new ConditionDTO()};
    }

    public String getConditionGroupId() {
        return conditionGroupId;
    }

    public void setConditionGroupId(String conditionGroupId) {
        this.conditionGroupId = conditionGroupId;
    }

    public ConditionDTO[] getConditions() {
        return Arrays.copyOf(conditions, conditions.length);
    }

    public void setConditions(ConditionDTO[] conditions) {
        this.conditions = Arrays.copyOf(conditions, conditions.length);
    }
}
