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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton which stores the rule maps for revoked JWTs
 */
public class InternalRevokedJWTDataHolder {
    private static final Log log = LogFactory.getLog(InternalRevokedJWTDataHolder.class);
    private static final Map<String, Long> internalRevokedConsumerKeyMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> internalRevokedConsumerKeyAppOnlyMap = new ConcurrentHashMap<>();
    // User UUID (jwt claim) -> revoked timestamp
    private static final Map<String, Long> internalRevokedUserEventRuleMap = new ConcurrentHashMap<>();
    private static final InternalRevokedJWTDataHolder instance = new InternalRevokedJWTDataHolder();

    private InternalRevokedJWTDataHolder() {

    }

    /**
     * This method can be used to get the singleton instance of this class.
     * @return the singleton instance.
     */
    public static InternalRevokedJWTDataHolder getInstance() {
        return instance;
    }


    public void addInternalRevokedJWTClientIDToMap(String consumerKey, Long revocationTime) {

        if (log.isDebugEnabled()) {
            log.debug("Adding internal revoked JWT client Id, revocation time pair to the " +
                    "revoked map :" + consumerKey + " , revocationTime:" + revocationTime);
        }
        internalRevokedConsumerKeyMap.put(consumerKey, revocationTime);
    }

    public boolean isJWTTokenClientIdExistsInRevokedMap(String consumerKey, Long jwtGeneratedTimestamp) {

        if (internalRevokedConsumerKeyMap.containsKey(consumerKey)) {
            Long jwtRevokedTime = internalRevokedConsumerKeyMap.get(consumerKey);

            if (jwtRevokedTime != null) {
                Timestamp jwtRevokedTimestamp = new Timestamp(jwtRevokedTime);
                return jwtRevokedTimestamp.after(new Timestamp(jwtGeneratedTimestamp));
            }
        }
        return false;
    }

    public void addInternalRevokedJWTClientIDToAppOnlyMap(String consumerKey, Long revocationTime) {

        if (log.isDebugEnabled()) {
            log.debug("Adding internal revoked JWT client Id, revocation time pair to the " +
                    "revoked app only map :" + consumerKey + " , revocationTime:" + revocationTime);
        }
        internalRevokedConsumerKeyAppOnlyMap.put(consumerKey, revocationTime);
    }

    public boolean isJWTTokenClientIdExistsInRevokedAppOnlyMap(String consumerKey, Long jwtGeneratedTimestamp) {

        if (internalRevokedConsumerKeyAppOnlyMap.containsKey(consumerKey)) {
            Long jwtRevokedTime = internalRevokedConsumerKeyAppOnlyMap.get(consumerKey);

            if (jwtRevokedTime != null) {
                Timestamp jwtRevokedTimestamp = new Timestamp(jwtRevokedTime);
                return jwtRevokedTimestamp.after(new Timestamp(jwtGeneratedTimestamp));
            }
        }
        return false;
    }

    public void addInternalRevokedJWTUserIDToMap(String userUUID, Long revocationTime) {

        if (log.isDebugEnabled()) {
            log.debug("Adding internal revoked JWT user id, revocation time value pair to the " +
                    "revoked map :" + userUUID + " , revocationTime: " + revocationTime);
        }
        internalRevokedUserEventRuleMap.put(userUUID, revocationTime);
    }

    public boolean isJWTTokenUserIdExistsInRevokedMap(String user, Long jwtGeneratedTimestamp) {

        if (internalRevokedUserEventRuleMap.containsKey(user)) {
            Long jwtRevokedTime = internalRevokedUserEventRuleMap.get(user);

            if (jwtRevokedTime != null) {
                Timestamp jwtRevokedTimestamp = new Timestamp(jwtRevokedTime);
                return jwtRevokedTimestamp.after(new Timestamp(jwtGeneratedTimestamp));
            }
        }
        return false;
    }
}