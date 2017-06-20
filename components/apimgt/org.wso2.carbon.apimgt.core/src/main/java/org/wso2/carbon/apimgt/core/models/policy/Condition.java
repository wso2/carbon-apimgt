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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Contains {@link Condition} attributes
 */
public abstract class Condition  {
    private String type;       //type of each condition: eg:IP, DATE, DATE RANGE etc.
    protected String queryAttributeName;   // needed in making condition for sidhdhi query (eg: properties.verb=='POST')
    private boolean invertCondition;     //To check if the condition to be included or excluded
    private String conditionEnabled;

    public String getConditionEnabled() {
        return conditionEnabled;
    }

    public void setConditionEnabled(String conditionEnabled) {
        this.conditionEnabled = conditionEnabled;
    }

    public String getQueryAttributeName() {
        return queryAttributeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInvertCondition() {
        return invertCondition;
    }

    public void setInvertCondition(boolean invertCondition) {
        this.invertCondition = invertCondition;
    }

    public abstract String getCondition();

    public abstract String getNullCondition();

    public void populateDataInPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        // not implemented
    }

    @Override
    public String toString() {
        return "Condition [type=" + type + ", queryAttributeName=" + queryAttributeName + ", invertCondition="
                + invertCondition + "]";
    }

}
