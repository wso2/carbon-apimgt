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
 * Contains {@link IPRangeCondition} attributes
 */
public class IPRangeCondition extends Condition {
    private String startingIP;
    private String endingIP;

    public IPRangeCondition() {
        setType(PolicyConstants.IP_RANGE_TYPE);
        this.queryAttributeName = PolicyConstants.START_QUERY + PolicyConstants.IP_QUERY + PolicyConstants.END_QUERY;
        // "cast(map:get(properties,’"+value+"’),’string’)";
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
        long ipStart = ipToLong(getStartingIP());
        long ipEnd = ipToLong(getEndingIP());
        String condition = PolicyConstants.OPEN_BRACKET + PolicyConstants.QUOTE + ipStart + PolicyConstants.QUOTE
                + PolicyConstants.LESS_THAN + getQueryAttributeName() + PolicyConstants.AND + PolicyConstants.QUOTE
                + ipEnd + PolicyConstants.QUOTE + PolicyConstants.GREATER_THAN + getQueryAttributeName()
                + PolicyConstants.CLOSE_BRACKET; // "("+queryAttribute+">="+value+"AND""+queryAttribute+"<="+value+)"
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
        return "IPRangeCondition [startingIP=" + startingIP + ", endingIP=" + endingIP + ", toString()="
                + super.toString() + "]";
    }

}
