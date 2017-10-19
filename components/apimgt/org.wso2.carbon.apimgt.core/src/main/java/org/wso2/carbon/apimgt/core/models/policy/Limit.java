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
 * Contains Limit based attributes
 */
public abstract class Limit {

    private String timeUnit;
    private int unitTime;

    public Limit (String timeUnit, int unitTime) {
        this.timeUnit = timeUnit;
        this.unitTime = unitTime;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public int getUnitTime() {
        return unitTime;
    }

    public abstract void populateDataInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;

    @Override
    public String toString() {
        return "Limit [timeUnit=" + timeUnit + ", unitTime=" + unitTime + "]";
    }
}
