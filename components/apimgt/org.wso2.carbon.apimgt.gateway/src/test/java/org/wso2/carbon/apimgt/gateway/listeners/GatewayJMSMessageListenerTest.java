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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;


import javax.jms.Topic;
import javax.jms.TextMessage;
import javax.jms.JMSException;

import org.wso2.andes.client.AMQTopic;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.andes.client.message.JMSTextMessage;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;

/*
 * Unit test cases related GatewayJMSMessageListener
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class GatewayJMSMessageListenerTest {

    private GatewayJMSMessageListener gatewayJMSMessageListener;
    private ServiceReferenceHolder serviceReferenceHolder;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        ArtifactRetriever artifactRetriever = Mockito.mock(ArtifactRetriever.class);
        Mockito.when(serviceReferenceHolder.getArtifactRetriever()).thenReturn(artifactRetriever);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                Mockito.mock(GatewayArtifactSynchronizerProperties.class);
        Mockito.when(apiManagerConfiguration.getGatewayArtifactSynchronizerProperties())
                .thenReturn(gatewayArtifactSynchronizerProperties);
        EventHubConfigurationDto eventHubConfigurationDto = Mockito.mock(EventHubConfigurationDto.class);
        Mockito.when(apiManagerConfiguration.getEventHubConfigurationDto()).thenReturn(eventHubConfigurationDto);
        gatewayJMSMessageListener = new GatewayJMSMessageListener();
    }

    @Test
    public void testSubscriptionPolicyUpdate() throws JMSException {
        String messageBody = "{\"event\":{\"payloadData\":{\"eventType\"" +
                ":\"POLICY_UPDATE\",\"timestamp\":1670477868131," +
                "\"event\":\"eyJwb2xpY3lJZCI6NSwicG9saWN5TmFtZSI6IlVubGltaXRlZCIsInF1b3RhVHlwZSI6InJlcXVlc3RDb3" +
                "VudCIsInN1YnNjcmliZXJDb3VudCI6MCwicmF0ZUxpbWl0Q291bnQiOjAsInJhdGVMaW1pdFRpbWVVbml0Ijoic2VjIiwic3R" +
                "vcE9uUXVvdGFSZWFjaCI6dHJ1ZSwiZ3JhcGhRTE1heERlcHRoIjowLCJncmFwaFFMTWF4Q29tcGxleGl0eSI6MCwicG9saWN5VHl" +
                "wZSI6IlNVQlNDUklQVElPTiIsImV2ZW50SWQiOiI4NTZjZGMzZC04NDg1LTRjMjAtODZhNC03Mzg1MGQ2NzFlMDYiLCJ0aW1lU3R" +
                "hbXAiOjE2NzA0Nzc4NjgxMzEsInR5cGUiOiJQT0xJQ1lfVVBEQVRFIiwidGVuYW50SWQiOi0xMjM0LCJ0ZW5hbnREb21haW4iOiJ" +
                "jYXJib24uc3VwZXIifQ==\"}}}\n";
        TextMessage textMessage = Mockito.mock(JMSTextMessage.class);
        Topic topic = Mockito.mock(AMQTopic.class);
        Mockito.when(textMessage.getJMSDestination()).thenReturn(topic);
        Mockito.when(textMessage.getText()).thenReturn(messageBody);
        Mockito.when(topic.getTopicName()).thenReturn(APIConstants.TopicNames.TOPIC_NOTIFICATION);
        KeyManagerDataServiceImplWrapper keyManagerDataService = new KeyManagerDataServiceImplWrapper();
        PowerMockito.when(serviceReferenceHolder.getKeyManagerDataService()).thenReturn(keyManagerDataService);
        gatewayJMSMessageListener.onMessage(textMessage);
        assertTrue(keyManagerDataService.isSubscriptionPolicyUpdated());
    }

}
