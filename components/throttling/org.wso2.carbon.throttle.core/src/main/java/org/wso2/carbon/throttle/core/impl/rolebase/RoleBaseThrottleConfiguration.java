/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.throttle.core.impl.rolebase;

import org.wso2.carbon.throttle.core.CallerConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConstants;
import org.wso2.carbon.throttle.core.impl.rolebase.RoleBaseCallerConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleBaseThrottleConfiguration implements ThrottleConfiguration {
    /* To hold configurations
    *  maps role to The Relevant Caller Configuration
    * */
    private Map configurationsMap;


    public RoleBaseThrottleConfiguration() {
        this.configurationsMap = new HashMap();
    }


    public void addCallerConfiguration(CallerConfiguration callerConfiguration) {
        RoleBaseCallerConfiguration roleBaseCallerConfiguration = (RoleBaseCallerConfiguration)
                                                                                callerConfiguration;
        List roles = roleBaseCallerConfiguration.getRoles();
        if (roles == null) {
            return;
        }
        for (Object role : roles) {
            String roleKey = ((String) role).trim();
            configurationsMap.put(roleKey, roleBaseCallerConfiguration);
        }

    }

    public CallerConfiguration getCallerConfiguration(String roleID) {
        //get the key for this roleID
        String key = getConfigurationKeyOfCaller(roleID);

        return (CallerConfiguration) configurationsMap.get(key);
    }

    public String getConfigurationKeyOfCaller(String roleID) {
        if (roleID != null) {
            roleID = roleID.trim();
            if (configurationsMap.containsKey(roleID)) {
                return roleID;
            }
        }
        return null;
    }

    public int getType() {
        return ThrottleConstants.ROLE_BASE;
    }
}
