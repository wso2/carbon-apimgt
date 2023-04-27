/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.jwt;

import org.apache.commons.logging.*;
import org.json.*;
import org.wso2.carbon.apimgt.gateway.dto.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class InternalRevokedJWTDataHolder {
    private static final Log log = LogFactory.getLog(RevokedJWTDataHolder.class);
    private static final Map<String, InternalRevokedJWTDTO> internalRevokedJWTMap = new ConcurrentHashMap<>();
    private static final InternalRevokedJWTDataHolder instance = new InternalRevokedJWTDataHolder();

    private InternalRevokedJWTDataHolder(){

    }

    /**
     * This method can be used to get the singleton instance of this class.
     * @return the singleton instance.
     */
    public static InternalRevokedJWTDataHolder getInstance() {
        return instance;
    }


    public void addInternalRevokedJWTClientIDToMap(String consumerKey, Long value) {
        if (consumerKey != null && value != null) {
            log.debug("Adding internal revoked JWT client Id, user sub, revoked timestamp value pair to the " +
                    "revoked map :" + consumerKey + " , value:" + value + " , timestamp: " + value);
            InternalRevokedJWTDTO internalRevokedJWTDTO = new InternalRevokedJWTDTO();
            internalRevokedJWTDTO.setRevokedTimestamp(value);
            internalRevokedJWTMap.put(consumerKey, internalRevokedJWTDTO);
        }
    }

    public boolean isJWTTokenClientIdExistsInRevokedMap(String consumerKey, Long jwtGeneratedTimestamp) {

        if (internalRevokedJWTMap.containsKey(consumerKey)) {
            InternalRevokedJWTDTO internalRevokedJWTDTO = internalRevokedJWTMap.get(consumerKey);
            Long jwtRevokedTime = internalRevokedJWTDTO.getRevokedTimestamp();

            if (jwtRevokedTime != null) {
                Timestamp jwtRevokedTimestamp = new Timestamp(jwtRevokedTime);
                return jwtRevokedTimestamp.after(new Timestamp(jwtGeneratedTimestamp));
            }
        }
        return false;
    }
}