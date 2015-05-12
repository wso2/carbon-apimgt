/*
* Copyright 2005,2006 WSO2, Inc. http://wso2.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

package org.wso2.carbon.throttle.core.impl.ipbase;


import org.wso2.carbon.throttle.core.CallerConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConstants;

/**
 * Metadata for controls a caller(IP based) - static data -The data have built form processing policy
 */
public class IPBaseCallerConfiguration extends CallerConfiguration {

    /* The ID of CallerConfiguration */
    private String ID;
    /* The First part of the iprange - group ip  */
    private String firstPartOfIPRange;
    /* The second part of the iprange - group ip  */
    private String secondPartOfIPRange;

    public IPBaseCallerConfiguration() {
        super();

    }

    /**
     * The Constructor with all configuration data
     *
     * @param unitTime           - long value which represents Unit Time Window
     * @param maximumRequest     - int value which represents Maximum Request
     * @param prohibitTimePeriod - long value which represents Prohibit Time after Max request came
     * @param ipRange            - String value which represents IP Range
     */
    public IPBaseCallerConfiguration(long unitTime, int maximumRequest,
                                     long prohibitTimePeriod, String ipRange) {
        super(unitTime, maximumRequest, prohibitTimePeriod, ipRange);
    }

    /**
     * To get IP Range - Group IP
     *
     * @return String value of IP Range
     */
    public String getID() {
        return ID;
    }

    /**
     * To get First Part of IP Range
     *
     * @return String value of First Part Of Ip Range
     */
    public String getFirstPartOfIPRange() {
        return firstPartOfIPRange;
    }

    /**
     * To get Second Part of IP Range
     *
     * @return String value of Second Part Of IP Range
     */
    public String getSecondPartOfIPRange() {
        return secondPartOfIPRange;
    }


    /**
     * To set IP Range
     *
     * @param iprange The string representation of ip (single or group)
     */
    public void setID(String iprange) {
        String ipParts[] = iprange.trim().split("-");
        if (ipParts != null) {
            // if IP Range is unique one IP
            if (ipParts.length == 1) {
                this.firstPartOfIPRange = ipParts[0];
            }
            // else if IP Range is group IP
            else if (ipParts.length == 2) {
                this.firstPartOfIPRange = ipParts[0];
                this.secondPartOfIPRange = ipParts[1];
            }
        }
        this.ID = iprange;
    }

    public int getType() {
        return ThrottleConstants.IP_BASE;
    }

    /**
     * To set First Part of IP Range
     *
     * @param firstPartOfIPRange - String value
     */
    public void setFirstPartOfIPRange(String firstPartOfIPRange) {
        this.firstPartOfIPRange = firstPartOfIPRange;
    }

    /**
     * To set Second Part Of IP Range
     *
     * @param secondPartOfIPRange - String value
     */
    public void setSecondPartOfIPRange(String secondPartOfIPRange) {
        this.secondPartOfIPRange = secondPartOfIPRange;
    }


}
