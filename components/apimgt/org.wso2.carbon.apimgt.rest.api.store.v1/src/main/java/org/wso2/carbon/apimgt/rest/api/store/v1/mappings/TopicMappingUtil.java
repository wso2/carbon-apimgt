/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.api.model.webhooks.Topic;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicSubscriptionListDTO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TopicMappingUtil {

    public static TopicListDTO fromTopicListToDTO(Set<Topic> topics) {

        TopicListDTO topicListDTO = new TopicListDTO();
        List<TopicDTO> topicDTOs = topicListDTO.getList();
        topicListDTO.setCount(topics.size());

        if (topicDTOs == null) {
            topicDTOs = new ArrayList<>();
            topicListDTO.setList(topicDTOs);
        }
        for (Topic topic : topics) {
            topicDTOs.add(fromTopicToDTO(topic));
        }
        return topicListDTO;
    }

    public static TopicDTO fromTopicToDTO(Topic topic) {
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setApiId(topic.getApiId());
        topicDTO.setName(topic.getName());
        topicDTO.setSubscribeURL(topic.getSubscribeURL());
        return topicDTO;
    }

    public static TopicSubscriptionListDTO fromSubscriptionListToDTO(Set<Subscription> subscriptions) {
        TopicSubscriptionListDTO topicSubscriptionListDTO = new TopicSubscriptionListDTO();
        List<TopicSubscriptionDTO> subscriptionDTOs = topicSubscriptionListDTO.getList();
        topicSubscriptionListDTO.setCount(subscriptions.size());

        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            topicSubscriptionListDTO.setList(subscriptionDTOs);
        }

        for (Subscription subscription: subscriptions) {
            subscriptionDTOs.add(fromSubscriptionToDTO(subscription));
        }
        return topicSubscriptionListDTO;
    }

    public static TopicSubscriptionDTO fromSubscriptionToDTO(Subscription subscription) {

        TopicSubscriptionDTO topicSubscriptionDTO = new TopicSubscriptionDTO();
        topicSubscriptionDTO.setApiId(subscription.getApiUuid());
        topicSubscriptionDTO.setAppId(subscription.getAppID());
        topicSubscriptionDTO.setCallBackUrl(subscription.getCallback());
        topicSubscriptionDTO.setDeliveryTime(getDateAsString(subscription.getLastDelivery()));
        topicSubscriptionDTO.setDeliveryStatus(subscription.getLastDeliveryState());
        return topicSubscriptionDTO;
    }

    private static String getDateAsString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dateFormat.format(date);
    }
}
