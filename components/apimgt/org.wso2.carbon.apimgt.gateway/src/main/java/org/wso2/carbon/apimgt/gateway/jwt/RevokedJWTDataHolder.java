/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *  Singleton which stores the revoked JWT map
 */
public class RevokedJWTDataHolder {

    private static final Log log = LogFactory.getLog(RevokedJWTDataHolder.class);
    private static Map<String, Long> revokedJWTMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> revokedConsumerKeyMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> revokedSubjectEntityAppMap = new ConcurrentHashMap<>();
    // User UUID (jwt claim) -> revoked timestamp
    private static final Map<String, Long> revokedSubjectEntityUserMap = new ConcurrentHashMap<>();
    private static RevokedJWTDataHolder instance = new RevokedJWTDataHolder();

    /**
     * Adds a given key,value pair to the revoke map.
     * @param key key to be added.
     * @param value value to be added.
     */
    public void addRevokedJWTToMap(String key, Long value) {
        if (key != null && value != null) {
            log.debug("Adding revoked JWT key, value pair to the revoked map :" + key + " , " + value);
            revokedJWTMap.put(key, value);
        }
    }

    /**
     * Checks whether a given signature is in the map.
     * @param jwtSignature signature to be checked.
     * @return true if it exists and false otherwise.
     */
    public static boolean isJWTTokenSignatureExistsInRevokedMap(String jwtSignature) {
        return revokedJWTMap.containsKey(jwtSignature);
    }

    private RevokedJWTDataHolder() {

    }

    /**
     * Fetches the revoke map.
     * @return
     */
    Map<String, Long> getRevokedJWTMap() {
        return revokedJWTMap;
    }

    /**
     * This method can be used to get the singleton instance of this class.
     * @return the singleton instance.
     */
    public static RevokedJWTDataHolder getInstance() {
        return instance;
    }

    public void addRevokedConsumerKeyToMap(String consumerKey, Long revocationTime) {

        if (log.isDebugEnabled()) {
            log.debug("Adding internal revoked JWT client Id, revocation time pair to the " + "revoked map :"
                    + consumerKey + " , revocationTime:" + revocationTime);
        }
        revokedConsumerKeyMap.put(consumerKey, revocationTime);
    }

    public boolean isRevokedConsumerKeyExists(String consumerKey, Long jwtGeneratedTimestamp) {

        Long jwtRevokedTime = revokedConsumerKeyMap.get(consumerKey);

        if (jwtRevokedTime != null) {
            Timestamp jwtRevokedTimestamp = new Timestamp(jwtRevokedTime);
            jwtRevokedTimestamp.toLocalDateTime();
            return jwtRevokedTimestamp.after(new Timestamp(jwtGeneratedTimestamp));
        }
        return false;
    }

    public void addRevokedSubjectEntityConsumerAppToMap(String consumerKey, Long revocationTime) {

        if (log.isDebugEnabled()) {
            log.debug("Adding internal revoked JWT client Id, revocation time pair to the revoked app only map :"
                    + consumerKey + " , revocationTime:" + revocationTime);
        }
        revokedSubjectEntityAppMap.put(consumerKey, revocationTime);
    }

    public boolean isRevokedSubjectEntityConsumerAppExists(String consumerKey, Long jwtGeneratedTimestamp) {

        Long jwtRevokedTime = revokedSubjectEntityAppMap.get(consumerKey);

        if (jwtRevokedTime != null) {
            Timestamp jwtRevokedTimestamp = new Timestamp(jwtRevokedTime);
            return jwtRevokedTimestamp.after(new Timestamp(jwtGeneratedTimestamp));
        }

        return false;
    }

    public void addRevokedSubjectEntityUserToMap(String userUUID, Long revocationTime) {

        if (log.isDebugEnabled()) {
            log.debug("Adding internal revoked JWT user id, revocation time value pair to the revoked map :"
                    + userUUID + " , revocationTime: " + revocationTime);
        }
        revokedSubjectEntityUserMap.put(userUUID, revocationTime);
    }

    public boolean isRevokedSubjectEntityUserExists(String user, Long jwtGeneratedTimestamp) {

        Long jwtRevokedTime = revokedSubjectEntityUserMap.get(user);
        if (jwtRevokedTime != null) {
            Timestamp jwtRevokedTimestamp = new Timestamp(jwtRevokedTime);
            return jwtRevokedTimestamp.after(new Timestamp(jwtGeneratedTimestamp));
        }
        return false;
    }
}