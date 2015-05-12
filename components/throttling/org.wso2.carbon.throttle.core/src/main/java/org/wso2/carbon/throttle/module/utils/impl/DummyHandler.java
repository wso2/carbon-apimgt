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
package org.wso2.carbon.throttle.module.utils.impl;


import org.wso2.carbon.throttle.module.utils.AuthenticationFuture;
import org.wso2.carbon.throttle.module.utils.UserPriviligesHandler;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class DummyHandler implements UserPriviligesHandler {
    private static String[] TIERS = {"bronze","silver","platinum"};
    public static Map apiKey2roleMap = new ConcurrentHashMap();

    public boolean authenticateUser(AuthenticationFuture callback) {
        callback.setAuthenticated(true);
        ArrayList roles = new ArrayList();
        Random rnd = new Random();
        if(apiKey2roleMap.get(callback.getAPIKey()) != null){
            roles.add(apiKey2roleMap.get(callback.getAPIKey()));
        }else{
            String role = TIERS[rnd.nextInt(TIERS.length)];
            apiKey2roleMap.put(callback.getAPIKey() ,role);
            roles.add(role);
        }

        callback.setAuthorizedRoles(roles);
        return callback.isAuthenticated();
    }
}
