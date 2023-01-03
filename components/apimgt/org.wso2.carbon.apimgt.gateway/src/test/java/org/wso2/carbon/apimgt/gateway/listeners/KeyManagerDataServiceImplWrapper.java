/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.gateway.listeners;

import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.keymgt.service.KeyManagerDataServiceImpl;

/*
 * A wrapper class related to GatewayJMSMessageListenerTest to check whether addOrUpdateSubscriptionPolicy method
 * call was executed.
 */
public class KeyManagerDataServiceImplWrapper extends KeyManagerDataServiceImpl {
    static boolean subscriptionPolicyUpdate = false;

    @Override
    public void addOrUpdateSubscriptionPolicy(SubscriptionPolicyEvent event){
        subscriptionPolicyUpdate = true;
    }

    public boolean isSubscriptionPolicyUpdated() {
        return subscriptionPolicyUpdate;
    }

}
