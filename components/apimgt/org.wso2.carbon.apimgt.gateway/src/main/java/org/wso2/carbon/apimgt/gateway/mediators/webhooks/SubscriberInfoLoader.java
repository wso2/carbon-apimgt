/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.gateway.mediators.webhooks;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.WebhooksDTO;

import java.util.List;

/**
 * This mediator would load the subscriber's information from the subscribers list according to the index of the list.
 */
public class SubscriberInfoLoader extends AbstractMediator {

    @Override
    public boolean mediate(MessageContext messageContext) {
        List<WebhooksDTO> subscribersList = (List<WebhooksDTO>) messageContext.
                getProperty(APIConstants.Webhooks.SUBSCRIBERS_LIST_PROPERTY);
        int index = (Integer) messageContext.getProperty(APIConstants.CLONED_ITERATION_INDEX_PROPERTY);
        WebhooksDTO subscriber = subscribersList.get(index - 1);
        if (subscriber != null) {
            messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_CALLBACK_PROPERTY, subscriber.getCallbackURL());
            messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_SECRET_PROPERTY, subscriber.getSecret());
            messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_APPLICATION_ID_PROPERTY, subscriber.getAppID());
        }
        return true;
    }
}
