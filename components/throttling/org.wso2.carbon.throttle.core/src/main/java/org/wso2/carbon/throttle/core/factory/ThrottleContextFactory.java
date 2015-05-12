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

import org.wso2.carbon.throttle.core.ThrottleConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConstants;
import org.wso2.carbon.throttle.core.ThrottleContext;
import org.wso2.carbon.throttle.core.ThrottleException;
import org.wso2.carbon.throttle.core.impl.domainbase.DomainBaseThrottleContext;
import org.wso2.carbon.throttle.core.impl.ipbase.IPBaseThrottleContext;
import org.wso2.carbon.throttle.core.impl.rolebase.RoleBaseThrottleContext;

/**
 * Factory for creating a ThrottleContext - holds all callers runtime data - the current state
 */

public class ThrottleContextFactory {

    /**
     * To create a ThrottleContext for the given throttle type
     * Needs to provide a throttle configuration
     *
     * @param throttletype  - The type of the throttle
     * @param configuration - The throttle configuration
     * @return ThrottleContext - The corresponding ThrottleContext for the given throttle type
     * @throws ThrottleException - Throws for if the throttle type is unknown
     */
    public static ThrottleContext createThrottleContext(int throttletype, ThrottleConfiguration configuration) throws ThrottleException {
        if (ThrottleConstants.IP_BASE == throttletype) {
            return new IPBaseThrottleContext(configuration);
        } else if (ThrottleConstants.DOMAIN_BASE == throttletype) {
            return new DomainBaseThrottleContext(configuration);
        } else if (ThrottleConstants.ROLE_BASE == throttletype) {
            return new RoleBaseThrottleContext(configuration);
        } else {
            throw new ThrottleException("Unknown throttle type");
        }
    }
}
