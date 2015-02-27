/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.eventing;

import org.apache.axiom.util.UIDGenerator;
import org.wso2.eventing.EventingConstants;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.SubscriptionData;

/**
 * Bean that keep subscription and subscription metadata. Addition to the subscription in the core
 * API SynapseSubscription add the UUID as the subscription ID, add a new constructor to define the
 * delivery mode. 
 */
public class SynapseSubscription extends Subscription {


    public SynapseSubscription() {
        this.setId(UIDGenerator.generateURNString());
        this.setDeliveryMode(EventingConstants.WSE_DEFAULT_DELIVERY_MODE);
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setProperty(SynapseEventingConstants.STATIC_ENTRY, "false");
        this.setSubscriptionData(subscriptionData);
    }

    public SynapseSubscription(String deliveryMode) {
        this.setId(UIDGenerator.generateURNString());
        this.setDeliveryMode(deliveryMode);
    }
}
