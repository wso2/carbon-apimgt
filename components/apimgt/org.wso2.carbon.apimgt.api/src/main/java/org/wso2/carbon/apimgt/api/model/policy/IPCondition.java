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

package org.wso2.carbon.apimgt.api.model.policy;

public class IPCondition extends Condition {
    private String specificIP;

    public IPCondition() {
        setType(PolicyConstants.IP_SPECIFIC_TYPE);
        setQueryAttributeName(PolicyConstants.IP_QUERY);
    }

    public String getSpecificIP() {
        return specificIP;
    }

    public void setSpecificIP(String specificIP) {
        this.specificIP = specificIP;
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
        long ip = ipToLong(getSpecificIP());
        String condition = getQueryAttributeName()+" == "+ip;
        return condition;
    }
}
