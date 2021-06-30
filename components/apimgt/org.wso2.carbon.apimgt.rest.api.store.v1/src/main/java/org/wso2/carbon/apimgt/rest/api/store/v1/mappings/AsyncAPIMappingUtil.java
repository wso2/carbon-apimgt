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
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WebhookSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WebhookSubscriptionListDTO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for Mapping Async API related entities to
 * REST API related DTOs.
 */
public class AsyncAPIMappingUtil {

    /**
     * Converts Set of Topic objects to DTO.
     *
     * @param topics set of Topic objects
     * @return TopicListDTO containing TopicDTOs
     */
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

    /**
     * Converts Topic object to DTO.
     *
     * @param topic Topic object
     * @return TopicDTO
     */
    public static TopicDTO fromTopicToDTO(Topic topic) {
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setApiId(topic.getApiId());
        topicDTO.setName(topic.getName());
        topicDTO.setType(topic.getType());
        return topicDTO;
    }

    /**
     * Converts Set of Subscription objects to SubscriptionListDTO.
     *
     * @param subscriptions Set of Subscription objects
     * @return WebhookSubscriptionListDTO containing SubscriptionDTOs
     */
    public static WebhookSubscriptionListDTO fromSubscriptionListToDTO(Set<Subscription> subscriptions) {
        WebhookSubscriptionListDTO webhookSubscriptionListDTO = new WebhookSubscriptionListDTO();
        List<WebhookSubscriptionDTO> subscriptionDTOs = webhookSubscriptionListDTO.getList();
        webhookSubscriptionListDTO.setCount(subscriptions.size());

        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            webhookSubscriptionListDTO.setList(subscriptionDTOs);
        }

        for (Subscription subscription: subscriptions) {
            subscriptionDTOs.add(fromSubscriptionToDTO(subscription));
        }
        return webhookSubscriptionListDTO;
    }

    /**
     * Converts a Subscription object to DTO.
     *
     * @param subscription Subscription object
     * @return WebhookSubscriptionDTO object
     */
    public static WebhookSubscriptionDTO fromSubscriptionToDTO(Subscription subscription) {

        WebhookSubscriptionDTO webhookSubscriptionDTO = new WebhookSubscriptionDTO();
        webhookSubscriptionDTO.setTopic(subscription.getTopic());
        webhookSubscriptionDTO.setApiId(subscription.getApiUuid());
        webhookSubscriptionDTO.setAppId(subscription.getAppID());
        webhookSubscriptionDTO.setCallBackUrl(subscription.getCallback());
        webhookSubscriptionDTO.setDeliveryTime(getDateAsString(subscription.getLastDelivery()));
        webhookSubscriptionDTO.setDeliveryStatus(subscription.getLastDeliveryState());
        return webhookSubscriptionDTO;
    }

    /**
     * Converts Java Date to standard String.
     *
     * @param date Standard java date object
     * @return String representation of the date
     */
    private static String getDateAsString(Date date) {
        // do not change this format. It is binding to the format specified in the dev portal.
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return dateFormat.format(date);
        }
        return null;
    }
}
