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
import org.wso2.carbon.throttle.core.ThrottleException;
import org.wso2.carbon.throttle.core.impl.domainbase.DomainBaseThrottleConfiguration;
import org.wso2.carbon.throttle.core.impl.ipbase.IPBaseThrottleConfiguration;
import org.wso2.carbon.throttle.core.impl.rolebase.RoleBaseThrottleConfiguration;

/**
 * Factory for creating a ThrottleConfiguration - holds all callers controle parameters
 */

public class ThrottleConfigurationFactory {

    /**
     * To create a ThrottleConfiguration for the given throttle type
     *
     * @param throttletype - The type of the throttle
     * @return ThrottleConfiguration  - The corresponding ThrottleConfiguration for
     *         the given throttle type
     * @throws ThrottleException - Throws for if the throttle type is unknown
     */
    public static ThrottleConfiguration createThrottleConfiguration(int throttletype) throws ThrottleException {
        if (ThrottleConstants.IP_BASE == throttletype) {
            return new IPBaseThrottleConfiguration();
        } else if (ThrottleConstants.DOMAIN_BASE == throttletype) {
            return new DomainBaseThrottleConfiguration();
        } else if (ThrottleConstants.ROLE_BASE == throttletype) {
            return new RoleBaseThrottleConfiguration();
        } else {
            throw new ThrottleException("Unknown throttle type");
        }
    }

}
