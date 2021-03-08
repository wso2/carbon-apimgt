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

package org.wso2.carbon.apimgt.gateway.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.dto.ResourceCacheInvalidationDto;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.List;

import javax.jms.*;

public class APIMgtGatewayCacheMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(APIMgtGatewayCacheMessageListener.class);

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    JsonNode payloadData =  new ObjectMapper().readTree(textMessage).path(APIConstants.EVENT_PAYLOAD).
                            path(APIConstants.EVENT_PAYLOAD_DATA);
                    if (APIConstants.TopicNames.TOPIC_CACHE_INVALIDATION
                            .equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.CACHE_INVALIDATION_TYPE).asText() != null) {
                            String value = payloadData.get("value").asText();
                            JSONParser jsonParser = new JSONParser();
                            if (APIConstants.RESOURCE_CACHE_NAME
                                    .equalsIgnoreCase(payloadData.get(APIConstants.CACHE_INVALIDATION_TYPE).asText())) {
                                handleResourceCacheInvalidationMessage((JSONObject) jsonParser.parse(value));
                            } else if (APIConstants.GATEWAY_KEY_CACHE_NAME
                                    .equalsIgnoreCase(payloadData.get(APIConstants.CACHE_INVALIDATION_TYPE).asText())) {
                                handleKeyCacheInvalidationMessage((JSONArray) jsonParser.parse(value));
                            } else if (APIConstants.GATEWAY_USERNAME_CACHE_NAME
                                    .equalsIgnoreCase(payloadData.get(APIConstants.CACHE_INVALIDATION_TYPE).asText())) {
                                handleUserCacheInvalidationMessage((JSONArray) jsonParser.parse(value));
                            }
                        }
                    }
                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException | JsonProcessingException e) {
            log.error("JMSException occurred when processing the received message ", e);
        } catch (ParseException e) {
            log.error("Error while processing evaluatedConditions", e);
        }
    }

    private void handleUserCacheInvalidationMessage(JSONArray jsonValue) throws ParseException {
        ServiceReferenceHolder.getInstance().getCacheInvalidationService()
                .invalidateCachedUsernames((String[]) jsonValue.toArray(new String[0]));
    }

    private void handleKeyCacheInvalidationMessage(JSONArray jsonValue) throws ParseException {
        ServiceReferenceHolder.getInstance().getCacheInvalidationService()
                .invalidateCachedTokens((String[]) jsonValue.toArray(new String[0]));
    }

    private void handleResourceCacheInvalidationMessage(JSONObject jsonValue) throws ParseException {
            String apiContext = (String) jsonValue.get("apiContext");
            String apiVersion = (String) jsonValue.get("apiVersion");
            JSONArray resources = (JSONArray) jsonValue.get("resources");
            List<ResourceCacheInvalidationDto> resourceCacheInvalidationDtoList = new ArrayList<>();
            for (Object resource : resources) {
                JSONObject uriTemplate = (JSONObject) resource;
                String resourceURLContext = (String) uriTemplate.get("resourceURLContext");
                String httpVerb = (String) uriTemplate.get("httpVerb");
                ResourceCacheInvalidationDto uriTemplateDto = new ResourceCacheInvalidationDto();
                uriTemplateDto.setHttpVerb(httpVerb);
                uriTemplateDto.setResourceURLContext(resourceURLContext);
                resourceCacheInvalidationDtoList.add(uriTemplateDto);
            }
            ServiceReferenceHolder.getInstance().getCacheInvalidationService().invalidateResourceCache(apiContext,
                    apiVersion, resourceCacheInvalidationDtoList.toArray(new ResourceCacheInvalidationDto[0]));

    }

}
