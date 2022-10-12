/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.apk.apimgt.user.mgt.internal;

import org.wso2.apk.apimgt.user.mgt.UserManager;
import org.wso2.apk.apimgt.user.mgt.impl.CarbonUserManagerImpl;

public class UserManagerHolder {
    private static UserManager userManager = null;

    public static UserManager getUserManager() {
        if (userManager == null) {
            userManager = new CarbonUserManagerImpl();
        }
        return userManager;
    }
}
