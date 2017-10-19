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
 * Contains {@link DateRangeCondition} attributes
 */
public class DateRangeCondition extends Condition {
    private String startingDate;
    private String endingDate;

    public DateRangeCondition() {
        setType(PolicyConstants.DATE_RANGE_CONDITION_TYPE);
        this.queryAttributeName = PolicyConstants.START_QUERY + PolicyConstants.DATE_QUERY + PolicyConstants.END_QUERY;
        // "cast(map:get(properties,’"+value+"’),’string’)";
    }

    public String getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(String endingDate) {
        this.endingDate = endingDate;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }

    @Override
    public String getCondition() {
        String condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeName() + PolicyConstants.GREATER_THAN
                + PolicyConstants.QUOTE + getStartingDate() + PolicyConstants.QUOTE + PolicyConstants.AND
                + getQueryAttributeName() + PolicyConstants.LESS_THAN + PolicyConstants.QUOTE + getEndingDate()
                + PolicyConstants.QUOTE + PolicyConstants.CLOSE_BRACKET; // "
        // ("+queryAttribute+">="+value+"AND""+queryAttribute+"<="+value+)"
        if (isInvertCondition()) {
            condition = PolicyConstants.INVERT_CONDITION + condition; // "!"+condition
        }
        return condition;
    }

    @Override
    public String getNullCondition() {
        return null;
    }

    @Override
    public String toString() {
        return "DateRangeCondition [startingDate=" + startingDate + ", endingDate=" + endingDate + ", toString()="
                + super.toString() + "]";
    }

}

