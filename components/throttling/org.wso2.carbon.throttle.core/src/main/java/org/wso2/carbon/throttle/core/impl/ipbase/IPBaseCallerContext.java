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

import org.wso2.carbon.throttle.core.CallerContext;
import org.wso2.carbon.throttle.core.ThrottleConstants;

import java.io.Serializable;

/**
 * Caller Context implementation for ip name based throttle type caller
 */

public class IPBaseCallerContext extends CallerContext implements Serializable {

    private static final long serialVersionUID = 635051645003581667L;

    public IPBaseCallerContext(String ID) {
        super(ID);
    }

    /**
     * @return int value that indicate the type of throttle
     */
    public int getType() {
        return ThrottleConstants.IP_BASE;
    }
}