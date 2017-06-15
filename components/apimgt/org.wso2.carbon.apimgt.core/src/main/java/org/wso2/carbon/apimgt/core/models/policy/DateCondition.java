/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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


package org.wso2.carbon.apimgt.core.models.policy;

/**
 * Contains {@link DateCondition} attributes
 */
public class DateCondition extends Condition {
    private String specificDate;

    public DateCondition() {
        setType(PolicyConstants.DATE_SPECIFIC_CONDITION_TYPE);
        this.queryAttributeName = PolicyConstants.START_QUERY + PolicyConstants.DATE_QUERY + PolicyConstants.END_QUERY;
        // "cast(map:get(properties,’"+value+"’),’string’)";
    }

    public String getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(String specificDate) {
        this.specificDate = specificDate;
    }

    @Override
    public String getCondition() {
        String condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeName() + PolicyConstants.EQUAL
                + PolicyConstants.QUOTE + getSpecificDate() + PolicyConstants.QUOTE + PolicyConstants.CLOSE_BRACKET;
        // "("+queryAttribute+"=="+value+")"
        if (isInvertCondition()) {
            condition = PolicyConstants.INVERT_CONDITION + condition;  // "!"+condition
        }
        return condition;
    }

    @Override
    public String getNullCondition() {
        return null;
    }

    @Override
    public String toString() {
        return "DateCondition [specificDate=" + specificDate + ", toString()=" + super.toString() + "]";
    }

}
