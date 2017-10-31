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
 * contains {@link BandwidthLimit} attributes
 */
public class BandwidthLimit extends Limit {

    private int dataAmount;
    private String dataUnit;

    public BandwidthLimit (String timeUnit, int unitTime, int dataAmount, String dataUnit) {
        super(timeUnit, unitTime);
        this.dataAmount = dataAmount;
        this.dataUnit = dataUnit;
    }

    @Override
    public void populateDataInPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(6, getDataAmount());
        preparedStatement.setString(7, getDataUnit());
    }

    public int getDataAmount() {
        return dataAmount;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    @Override
    public String toString() {
        return "BandwidthLimit [dataAmount=" + dataAmount + ", dataUnit=" + dataUnit + ", toString()="
                + super.toString() + "]";
    }

    /**
     * To get the data amount in single standard unit.
     *
     * @return
     */
    public long getStandardDataAmount() {
        if (PolicyConstants.MB.equalsIgnoreCase(dataUnit)) {
            return (long) dataAmount * 1024 * 1024;
        } else if (PolicyConstants.KB.equalsIgnoreCase(dataUnit)) {
            return (long) dataAmount * 1024;
        }
        return dataAmount;
    }
}
