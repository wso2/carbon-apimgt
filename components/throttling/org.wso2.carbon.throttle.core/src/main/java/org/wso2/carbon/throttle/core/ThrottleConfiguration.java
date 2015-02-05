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
 * The abstraction to holds all the controlling policy parameter
 */

public interface ThrottleConfiguration {

    /**
     * To add a CallerConfiguration - The controlling policy for a caller
     *
     * @param callerConfiguration The caller configuration data
     */
    public void addCallerConfiguration(CallerConfiguration callerConfiguration);

    /**
     * To get a CallerConfiguration - The controlling policy for a caller
     *
     * @param ID The ID of the caller (ip/domain name)
     * @return CallerConfiguration
     */
    public CallerConfiguration getCallerConfiguration(String ID);

    /**
     * To get a access key for a caller (In the case of group ID)
     *
     * @param callerID - The ID of the caller (ip/domain name)
     * @return String  -The pre-define key with in policy
     */

    public String getConfigurationKeyOfCaller(String callerID);

    /**
     * To get the type of the throttle
     *
     * @return the type of the throttle
     */
    public int getType();

}
