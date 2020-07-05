/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.apimgt.impl.token;

import java.util.Properties;

/**
 * This interface can be used to send a revoked token on realtime or to a persistent storage.
 */
public interface TokenRevocationNotifier {

    /**
     * Method to send the revoked token on realtime
     *
     * @param revokedToken revoked token to be sent
     * @param properties realtime notifier properties read from the config
     */
    void sendMessageOnRealtime(String revokedToken, Properties properties);

    /**
     * Method to send the revoked token to the persistent storage
     *
     * @param revokedToken token to be revoked
     * @param properties persistent notifier properties read from the config
     */
    void sendMessageToPersistentStorage(String revokedToken, Properties properties);

}
