/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.databridge.commons.Event;

import java.util.Arrays;
import java.util.Set;

/**
 * A service client implementation for the APIAuthenticationService (an admin service offered
 * by the API gateway).
 */
public class APIAuthenticationAdminClient {

    private static final Log log = LogFactory.getLog(APIAuthenticationAdminClient.class);

    // NO REG USAGE
    public void invalidateResourceCache(String apiContext, String apiVersion, Set<URITemplate> uriTemplates) {

        JSONObject api = new JSONObject();
        api.put("apiContext", apiContext);
        api.put("apiVersion", apiVersion);
        JSONArray resources = new JSONArray();
        for (URITemplate uriTemplate : uriTemplates) {
            JSONObject resource = new JSONObject();
            resource.put("resourceURLContext", uriTemplate.getUriTemplate());
            resource.put("httpVerb", uriTemplate.getHTTPVerb());
            resources.add(resource);
        }
        api.put("resources", resources);

        Object[] objectData = new Object[]{APIConstants.RESOURCE_CACHE_NAME, api.toJSONString()};
        log.debug("Sending Resource Invalidation Message");
        Event event = new Event(APIConstants.CACHE_INVALIDATION_STREAM_ID, System.currentTimeMillis(),
                null, null, objectData);
        APIUtil.publishEventToEventHub(null, event);
    }

    /**
     * Removes the active tokens that are cached on the API Gateway
     * @param activeTokens - The active access tokens to be removed from the gateway cache.
     */
    public void invalidateCachedTokens(Set<String> activeTokens) {

        JSONArray tokenArray = new JSONArray();
        tokenArray.addAll(activeTokens);
        Object[] objectData = new Object[]{APIConstants.GATEWAY_KEY_CACHE_NAME, tokenArray.toJSONString()};
        Event event = new Event(APIConstants.CACHE_INVALIDATION_STREAM_ID, System.currentTimeMillis(),
                null, null, objectData);
        APIUtil.publishEventToEventHub(null, event);
    }

    /**
     * Removes a given username that is cached on the API Gateway
     *
     * @param username - The username to be removed from the gateway cache.
     */
    public void invalidateCachedUsername(String username) {

        invalidateCachedUsernames(new String[]{username});
    }

    /**
     * Removes given usernames that is cached on the API Gateway
     *
     * @param username_list - The list of usernames to be removed from the gateway cache.
     */
    public void invalidateCachedUsernames(String[] username_list) {

        JSONArray userArray = new JSONArray();
        userArray.addAll(Arrays.asList(username_list));
        Object[] objectData = new Object[]{APIConstants.GATEWAY_USERNAME_CACHE_NAME, userArray.toJSONString()};
        Event event = new Event(APIConstants.CACHE_INVALIDATION_STREAM_ID, System.currentTimeMillis(),
                null, null, objectData);
        APIUtil.publishEventToEventHub(null, event);
    }

}
