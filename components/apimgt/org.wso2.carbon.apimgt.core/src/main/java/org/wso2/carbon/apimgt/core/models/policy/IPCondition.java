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
 * contains {@link IPCondition} policy attributes
 */
public class IPCondition extends Condition {

    private String specificIP;
    private String startingIP;
    private String endingIP;

    public IPCondition(String conditionType) {
        setType(conditionType);
        this.queryAttributeName = PolicyConstants.START_QUERY + PolicyConstants.IP_QUERY + PolicyConstants
                .END_QUERY_LONG;
    }

    @Override
    public void populateDataInPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, getStartingIP());
        preparedStatement.setString(2, getEndingIP());
        preparedStatement.setString(3, getSpecificIP());
    }

    public String getSpecificIP() {
        return specificIP;
    }

    public void setSpecificIP(String specificIP) {
        this.specificIP = specificIP;
    }

    public String getStartingIP() {
        return startingIP;
    }

    public void setStartingIP(String startingIP) {
        this.startingIP = startingIP;
    }

    public String getEndingIP() {
        return endingIP;
    }

    public void setEndingIP(String endingIP) {
        this.endingIP = endingIP;
    }

    public long ipToLong(String ip) {
        long ipAddressinLong = 0;
        if (ip != null) {
            //convert ipaddress into a long
            String[] ipAddressArray = ip.split("\\.");    //split by "." and add to an array

            for (int i = 0; i < ipAddressArray.length; i++) {
                int power = 3 - i;
                long ipAddress = Long.parseLong(ipAddressArray[i]);   //parse to long
                ipAddressinLong += ipAddress * Math.pow(256, power);
            }
        }
        return ipAddressinLong;
    }


    @Override
    public String getCondition() {
        String condition = null;
        if (PolicyConstants.IP_SPECIFIC_TYPE.equalsIgnoreCase(getType())) {
            long ip = ipToLong(getSpecificIP());
            condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeName() + PolicyConstants.EQUAL
                    + ip + PolicyConstants.END_LONG + PolicyConstants.CLOSE_BRACKET;
            if (isInvertCondition()) {
                condition = PolicyConstants.INVERT_CONDITION + condition;
            }
        }

        if (PolicyConstants.IP_RANGE_TYPE.equalsIgnoreCase(getType())) {
            long ipStart = ipToLong(getStartingIP());
            long ipEnd = ipToLong(getEndingIP());
            condition = PolicyConstants.OPEN_BRACKET + ipStart + PolicyConstants.END_LONG + PolicyConstants.LESS_THAN
                    + getQueryAttributeName() + PolicyConstants.AND + ipEnd + PolicyConstants.END_LONG +
                    PolicyConstants.GREATER_THAN + getQueryAttributeName()
                    + PolicyConstants.CLOSE_BRACKET;
            if (isInvertCondition()) {
                condition = PolicyConstants.INVERT_CONDITION + condition;
            }
        }

        return condition;
    }

    @Override
    public String getNullCondition() {
        return null;
    }


    @Override
    public String toString() {
        String msg = "";
        if (PolicyConstants.IP_SPECIFIC_TYPE.equalsIgnoreCase(getType())) {
            msg = "IPCondition [specificIP=" + specificIP + ", toString()=" + super.toString() + "]";
        }

        if (PolicyConstants.IP_RANGE_TYPE.equalsIgnoreCase(getType())) {
            msg = "IPRangeCondition [startingIP=" + startingIP + ", endingIP=" + endingIP + ", toString()="
                    + super.toString() + "]";
        }
        return msg;
    }


}
