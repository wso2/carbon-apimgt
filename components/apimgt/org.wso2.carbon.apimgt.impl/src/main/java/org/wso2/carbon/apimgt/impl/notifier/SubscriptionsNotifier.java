/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.notifier;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * The default Subscription notification service implementation in which Subscription creation, update and delete
 * events are published to gateway.
 */
public class SubscriptionsNotifier extends AbstractNotifier {

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (event instanceof SubscriptionEvent) {
            SubscriptionEvent subscriptionEvent = (SubscriptionEvent) event;
            if (APIConstants.EventType.SUBSCRIPTIONS_CREATE.name().equals(subscriptionEvent.getType())
                    && APIConstants.SubscriptionStatus.UNBLOCKED.equals(subscriptionEvent.getSubscriptionState())) {
                event.setTimeStamp(event.getTimeStamp() + 10l);
            }
        }
        publishEventToEventHub(event);
        return true;
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.SUBSCRIPTIONS.name();
    }
}
