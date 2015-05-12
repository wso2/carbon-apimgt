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

package org.wso2.carbon.throttle.core;

/**
 * All the configuration data for a caller – this data have been defined according to the policy
 */

public abstract class CallerConfiguration {

    /* The time window */
    private long unitTime;

    /* The maximum number of Request that should have allowed for caller */
    private int maximumRequest;

    /* The Time Period which access of caller should have denied if Maximum Number
    of Request had reached */
    private long prohibitTimePeriod;

    /* The int value that indicate that access is fully denied or allowed or controlled for this IP */
    private int accessState = ThrottleConstants.ACCESS_CONTROLLED;

    protected CallerConfiguration() {
        super();

    }

    /**
     * The Constructor with all configuration data
     *
     * @param unitTime           - long value which represents Unit Time Window
     * @param maximumRequest     - int value which represents Maximum Request
     * @param prohibitTimePeriod - long value which represents Prohibit Time after Max request came
     * @param ID                 - String value which represents ID
     */
    protected CallerConfiguration(long unitTime, int maximumRequest, long prohibitTimePeriod, String ID) {
        this();
        this.unitTime = unitTime;
        this.maximumRequest = maximumRequest;
        this.prohibitTimePeriod = prohibitTimePeriod;
        setID(ID);
    }

    /**
     * To get access state
     *
     * @return int value indicate access state
     */
    public int getAccessState() {
        return accessState;
    }

    /**
     * To get Maximum Request
     *
     * @return int value of Maximum Request Count
     */
    public int getMaximumRequestPerUnitTime() {
        return maximumRequest;
    }

    /**
     * To get UnitTime
     *
     * @return long value of Unit Time
     */
    public long getUnitTime() {
        return unitTime;

    }

    /**
     * To get prohibit time period
     *
     * @return long value of prohibit time period
     */
    public long getProhibitTimePeriod() {
        return prohibitTimePeriod;
    }


    /**
     * To set Maximum Request
     *
     * @param maximumRequest -int value
     */
    public void setMaximumRequestPerUnitTime(int maximumRequest) {
        this.maximumRequest = maximumRequest;

    }

    /**
     * To set Unit Time
     *
     * @param unitTime - long value
     */
    public void setUnitTime(long unitTime) {
        this.unitTime = unitTime;
    }

    /**
     * To set Prohibit Time Period
     *
     * @param prohibitTimePeriod -long value
     */
    public void setProhibitTimePeriod(long prohibitTimePeriod) {
        this.prohibitTimePeriod = prohibitTimePeriod;
    }

    /**
     * To set access state
     *
     * @param accessState caller access state , allow,deny and control
     */
    public void setAccessState(int accessState) {
        this.accessState = accessState;
    }

    /**
     * To get ID
     *
     * @return String value of ID
     */
    public abstract String getID();


    /**
     * To set ID
     *
     * @param ID The id of caller
     */
    public abstract void setID(String ID);

    /**
     * To get the type of the throttle
     *
     * @return the type of the throttle
     */
    public abstract int getType();


}
