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
import org.wso2.carbon.throttle.core.ThrottleConstants;

import java.util.ArrayList;
import java.util.List;

public class RoleBaseCallerConfiguration extends CallerConfiguration {

    String roleID;
    ArrayList<String> roles = new ArrayList<String>();
    @Override
    public String getID() {
        return roleID;
    }

    @Override
    public void setID(String id) {
        roleID = id;
        String parts[] = id.trim().split(",");
        for (String part : parts) {
            if (part != null && !"".equals(part.trim())) {
                roles.add(part);
            }
        }
    }

    @Override
    public int getType() {
        return ThrottleConstants.ROLE_BASE;
    }

    public List getRoles(){
        return roles;
    }
}
