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

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPCondition extends Condition {
	
    private String specificIP;
    private String startingIP;
    private String endingIP;

    public IPCondition(String conditionType) {
    	setType(conditionType);
        this.queryAttributeName = PolicyConstants.START_QUERY + PolicyConstants.IP_QUERY + PolicyConstants
                .END_QUERY_LONG;
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
    
    private BigInteger ipToBigInteger(String ip) {
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
            byte[] bytes = address.getAddress();
            return new BigInteger(1, bytes);
        } catch (UnknownHostException e) {
            //ignore the error 
        }
        return BigInteger.ZERO;
    }
    
    /**
     * Check whether the version of the IP is v6
     * @param ip
     * @return boolean
     */
    public static boolean isIPv6Address(String ip) {
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
            if (address instanceof Inet6Address) {
                return true;
            }
        } catch (UnknownHostException e) {
            // ignore the error
        }
        return false;
    }

    @Override
    public String getCondition() {
    	String condition = null;
    	if(PolicyConstants.IP_SPECIFIC_TYPE.equalsIgnoreCase(getType())){
            if (isIPv6Address(specificIP)) {
                BigInteger ip = ipToBigInteger(getSpecificIP());
                //(throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '338288524927261089654173758656056328191')==0)
                condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeNameForIPv6(ip) + PolicyConstants.EQUAL + 0
                        + PolicyConstants.CLOSE_BRACKET;
            } else {
                long ip = ipToLong(getSpecificIP());
                condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeName() + PolicyConstants.EQUAL
                        +ip +PolicyConstants.END_LONG +PolicyConstants.CLOSE_BRACKET;    
            }
            if (isInvertCondition()) {
                condition = PolicyConstants.INVERT_CONDITION + condition;
            }
    	}
    	
    	if(PolicyConstants.IP_RANGE_TYPE.equalsIgnoreCase(getType())){
            if (isIPv6Address(startingIP) && isIPv6Address(endingIP)) {
                BigInteger ipStart = ipToBigInteger(getStartingIP());
                BigInteger ipEnd = ipToBigInteger(getEndingIP());
                /*
                (throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '338288524927261089654173758656056315167')>=0 
                   AND throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '338288524927261089654173758656056328191')<=0)
                */
                condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeNameForIPv6(ipStart)
                        + PolicyConstants.GREATER_THAN + 0 + PolicyConstants.AND + getQueryAttributeNameForIPv6(ipEnd)
                        + PolicyConstants.LESS_THAN + 0 + PolicyConstants.CLOSE_BRACKET;
            } else {
                long ipStart = ipToLong(getStartingIP());
                long ipEnd = ipToLong(getEndingIP());
                condition = PolicyConstants.OPEN_BRACKET + ipStart + PolicyConstants.END_LONG
                        + PolicyConstants.LESS_THAN + getQueryAttributeName() + PolicyConstants.AND + ipEnd
                        + PolicyConstants.END_LONG + PolicyConstants.GREATER_THAN + getQueryAttributeName()
                        + PolicyConstants.CLOSE_BRACKET;
            }

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
        if(PolicyConstants.IP_SPECIFIC_TYPE.equalsIgnoreCase(getType())){
        	msg = "IPCondition [specificIP=" + specificIP + ", toString()=" + super.toString() + "]";
        }
        
        if(PolicyConstants.IP_RANGE_TYPE.equalsIgnoreCase(getType())){
        	msg = "IPRangeCondition [startingIP=" + startingIP + ", endingIP=" + endingIP + ", toString()="
        	        + super.toString() + "]";
        }
        return msg;
    }
      
    private String getQueryAttributeNameForIPv6(BigInteger ip) {
        return "throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), " + PolicyConstants.QUOTE + ip
                + PolicyConstants.QUOTE + PolicyConstants.CLOSE_BRACKET;
    }
    
}
