/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.cxf.message.*;
import java.util.HashMap;

/**
 *  JWTAuthenticationContext is for conversion purposes. Convert the cxf message context to HashMap and vise versa.
 * */

public class JWTAuthenticationUtils {

    /**
     * To getting the updated inbound message
     * @param inMessage - current inbound message
     * @param authContext - current map that contains authentication properties
     * @return updated cxf Message instance
     */
    public static Message addToMessageContext(Message inMessage, HashMap<String,Object> authContext) {
        for (String key : authContext.keySet()) {
            inMessage.put(key, authContext.get(key));
        }
        return inMessage;
    }

    /**
     * To getting a message properties as a Map
     * @param message - current inbound message
     * @return Map object that contains all properties of cxf inbound message
     */
    public static HashMap<String,Object> addToJWTAuthenticationContext(Message message) {
        HashMap<String,Object> hashMap = new HashMap<>();
        message.forEach(hashMap::put);
        return hashMap;
    }
}
