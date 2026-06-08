/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;

/**
 * Publishes federated API discovery task state change events to all CP nodes via the JMS notification topic.
 * <p>
 * Each event encodes task ID, environment key, status (PENDING / COMPLETED / FAILED) and — for completed
 * events — the full discovered API result list.  Receiving nodes deserialize the event in
 * {@code FederatedDiscoveryJMSMessageListener} and update their local task stores so that every node can
 * serve status / result polls without requiring sticky load-balancer sessions.
 */
public class FederatedDiscoveryNotifier extends AbstractNotifier {

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        publishEventToEventHub(event);
        return true;
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.FEDERATED_DISCOVERY.name();
    }
}
