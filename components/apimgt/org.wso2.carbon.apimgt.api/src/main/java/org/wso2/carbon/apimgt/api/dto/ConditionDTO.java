/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.api.dto;

import java.io.Serializable;

/**
 * This DTO used to represent a specific condition inside a Condition Group. This is analogous to {@code Condition}
 * object.
 */
public class ConditionDTO implements Serializable{

    private static final long serialVersionUID = 1L;

    private String conditionType;
    private String conditionName;
    private String conditionValue;
    private boolean isInverted;

    public String getConditionType() {
        return conditionType;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public void isInverted(boolean invertCondition) {
        this.isInverted = invertCondition;
    }
}
