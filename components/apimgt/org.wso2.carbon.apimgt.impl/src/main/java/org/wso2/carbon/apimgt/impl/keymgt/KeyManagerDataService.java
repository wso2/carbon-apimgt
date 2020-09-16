/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.apimgt.impl.keymgt;

import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ScopeEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;

/**
 * 
 * Interface to call keymanager data maps using events
 *
 */
public interface KeyManagerDataService {
    
    void addOrUpdateApplication(ApplicationEvent event);

    void addOrUpdateAPI(APIEvent event);

    void addOrUpdateSubscription(SubscriptionEvent event);

    void addOrUpdateApplicationKeyMapping(ApplicationRegistrationEvent event);
    
    void addOrUpdateSubscriptionPolicy(SubscriptionPolicyEvent event);
    
    void addOrUpdateApplicationPolicy(ApplicationPolicyEvent event);
    
    void addOrUpdateAPIPolicy(APIPolicyEvent policyEvent);
    
    void removeApplication(ApplicationEvent event);

    void removeAPI(APIEvent event);

    void removeSubscription(SubscriptionEvent event);

    void removeApplicationKeyMapping(ApplicationRegistrationEvent event);
    
    void removeSubscriptionPolicy(SubscriptionPolicyEvent event);
    
    void removeApplicationPolicy(ApplicationPolicyEvent event);

    void removeAPIPolicy(APIPolicyEvent policyEvent);

    void addScope(ScopeEvent event);

    void deleteScope(ScopeEvent event);
}
