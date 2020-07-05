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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

public class APIMgtGatewayCacheMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(APIMgtGatewayCacheMessageListener.class);

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }
                    if (APIConstants.TopicNames.TOPIC_CACHE_INVALIDATION
                            .equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.CACHE_INVALIDATION_TYPE) != null) {
                            if (APIConstants.RESOURCE_CACHE_NAME
                                    .equalsIgnoreCase((String) map.get(APIConstants.CACHE_INVALIDATION_TYPE))) {
                                handleResourceCacheInvalidationMessage(map);
                            } else if (APIConstants.GATEWAY_KEY_CACHE_NAME
                                    .equalsIgnoreCase((String) map.get(APIConstants.CACHE_INVALIDATION_TYPE))) {
                                handleKeyCacheInvalidationMessage(map);
                            } else if (APIConstants.GATEWAY_USERNAME_CACHE_NAME
                                    .equalsIgnoreCase((String) map.get(APIConstants.CACHE_INVALIDATION_TYPE))) {
                                handleUserCacheInvalidationMessage(map);
                            }

                        }
                    }
                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        } catch (ParseException e) {
            log.error("Error while processing evaluatedConditions", e);
        }
    }

    private void handleUserCacheInvalidationMessage(Map<String, Object> map) throws ParseException {

        if (map.containsKey("value")) {
            String value = (String) map.get("value");
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonValue = (JSONArray) jsonParser.parse(value);

            ServiceReferenceHolder.getInstance().getCacheInvalidationService()
                    .invalidateCachedUsernames((String[]) jsonValue.toArray(new String[jsonValue.size()]));
        }
    }

    private void handleKeyCacheInvalidationMessage(Map<String, Object> map) throws ParseException {

        if (map.containsKey("value")) {
            String value = (String) map.get("value");
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonValue = (JSONArray) jsonParser.parse(value);

            ServiceReferenceHolder.getInstance().getCacheInvalidationService()
                    .invalidateCachedTokens((String[]) jsonValue.toArray(new String[jsonValue.size()]));
        }
    }

    private void handleResourceCacheInvalidationMessage(Map<String, Object> map) throws ParseException {

        if (map.containsKey("value")) {
            String value = (String) map.get("value");
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonValue = (JSONObject) jsonParser.parse(value);
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
                    apiVersion,
                    resourceCacheInvalidationDtoList
                            .toArray(new ResourceCacheInvalidationDto[resourceCacheInvalidationDtoList.size()]));
        }

    }

}
