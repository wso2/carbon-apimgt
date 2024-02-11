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
package org.wso2.carbon.apimgt.impl.publishers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.token.TokenRevocationNotifier;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Publisher class to notify the token revocation requests
 */
public class RevocationRequestPublisher {

    private static final Log log = LogFactory.getLog(RevocationRequestPublisher.class);

    private static RevocationRequestPublisher revocationRequestPublisher = null;
    private boolean realtimeNotifierEnabled;
    private boolean persistentNotifierEnabled;
    private TokenRevocationNotifier tokenRevocationNotifier;

    private RevocationRequestPublisher() {

        Properties realtimeNotifierProperties = APIManagerConfiguration.getRealtimeTokenRevocationNotifierProperties();
        Properties persistentNotifierProperties =
                APIManagerConfiguration.getPersistentTokenRevocationNotifiersProperties();
        realtimeNotifierEnabled = realtimeNotifierProperties != null;
        persistentNotifierEnabled = persistentNotifierProperties != null;
        String className = APIManagerConfiguration.getTokenRevocationClassName();
        try {
            tokenRevocationNotifier = (TokenRevocationNotifier) Class.forName(className).getConstructor()
                    .newInstance();
            log.debug("Oauth interceptor initialized");
            tokenRevocationNotifier.init(realtimeNotifierProperties, persistentNotifierProperties);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                | ClassNotFoundException e) {
            log.error("Oauth interceptor object creation error", e);
        }
    }

    public static synchronized RevocationRequestPublisher getInstance() {

        if (revocationRequestPublisher == null) {
            revocationRequestPublisher = new RevocationRequestPublisher();
        }
        return revocationRequestPublisher;
    }

    public void publishRevocationEvents(String token, Properties properties) {

        if (realtimeNotifierEnabled) {
            log.debug("Realtime message sending is enabled");
            tokenRevocationNotifier.sendMessageOnRealtime(token, properties);
        } else {
            log.debug("Realtime message sending isn't enabled or configured properly");
        }
        if (persistentNotifierEnabled) {
            log.debug("Persistent message sending is enabled");
            tokenRevocationNotifier.sendMessageToPersistentStorage(token, properties);
        } else {
            log.debug("Persistent message sending isn't enabled or configured properly");
        }
    }
}
