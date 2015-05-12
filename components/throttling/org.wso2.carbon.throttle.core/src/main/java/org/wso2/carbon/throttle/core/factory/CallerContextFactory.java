/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*/
package org.wso2.carbon.throttle.core.factory;

import org.wso2.carbon.throttle.core.CallerContext;
import org.wso2.carbon.throttle.core.ThrottleConstants;
import org.wso2.carbon.throttle.core.ThrottleException;
import org.wso2.carbon.throttle.core.impl.domainbase.DomainBaseCallerContext;
import org.wso2.carbon.throttle.core.impl.ipbase.IPBaseCallerContext;
import org.wso2.carbon.throttle.core.impl.rolebase.RoleBaseCallerContext;

/**
 * Factory for creating a CallerContext
 */

public class CallerContextFactory {

    /**
     * To create a CallerContext(the run time data holder for a remote caller)
     * for the given throttle type.
     * Needs to provide the ID(ip | domain) of the remote caller (ip/domain according to the policy)
     *
     * @param throttletype - The type of the throttle
     * @param id           - The id of the caller
     * @return caller       - The corresponding caller context for the given throttle type
     * @throws ThrottleException - Throws for if the throttle type is unknown
     */
    public static CallerContext createCaller(int throttletype, String id) throws ThrottleException {
        if (ThrottleConstants.IP_BASE == throttletype) {
            return new IPBaseCallerContext(id);
        } else if (ThrottleConstants.DOMAIN_BASE == throttletype) {
            return new DomainBaseCallerContext(id);
        } else if (ThrottleConstants.ROLE_BASE == throttletype) {
            return new RoleBaseCallerContext(id);
        } else {
            throw new ThrottleException("Unknown throttle type");
        }
    }
}
