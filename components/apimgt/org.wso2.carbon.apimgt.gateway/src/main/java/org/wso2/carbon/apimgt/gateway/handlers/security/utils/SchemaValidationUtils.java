/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.utils;

import com.google.common.net.HttpHeaders;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

public class SchemaValidationUtils {

    public static final String XML_CONTENT_TYPE = "application/xml";
    public static final String XML_DECLARATION =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    /**
     * Method to build message payload ( only when payload is exists).
     *
     * @param axis2MC Axis2 Message context.
     * @param headers Map of headers
     * @return Optional String of payload
     * @throws APIManagementException When failed to decode the payload.
     */
    public static Optional<String> buildMessagePayload(org.apache.axis2.context.MessageContext axis2MC, Map headers)
            throws APIManagementException {

        String requestPayload = null;
        boolean isMessageContextBuilt = isMessageBuilt(axis2MC);
        if (!isMessageContextBuilt) {
            // Build Axis2 Message.
            try {
                RelayUtils.buildMessage(axis2MC);
            } catch (IOException | XMLStreamException e) {
                throw new APIManagementException("Unable to build axis2 message", e);
            }
        }

        if (headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            if (headers.get(HttpHeaders.CONTENT_TYPE).toString().contains(XML_CONTENT_TYPE)) {

                OMElement xmlPayload = axis2MC.getEnvelope().getBody().getFirstElement();
                if (xmlPayload != null) {
                    requestPayload = XML_DECLARATION + xmlPayload.toString();
                }
            } else {
                // Get JSON Stream and cast to string
                try {
                    InputStream jsonPayload = JsonUtil.getJsonPayload(axis2MC);
                    if (jsonPayload != null) {
                        requestPayload = IOUtils.toString(JsonUtil.getJsonPayload(axis2MC),
                                StandardCharsets.UTF_8.name());
                    }

                } catch (IOException e) {
                    throw new APIManagementException("Unable to read payload stream", e);
                }
            }
        }
        return Optional.ofNullable(requestPayload);
    }

    /**
     * Method to check message payload built or not.
     *
     * @param axis2MC Axis2 Message context.
     * @return boolean indicated message built or not.
     */
    public static boolean isMessageBuilt(org.apache.axis2.context.MessageContext axis2MC) {

        boolean isMessageContextBuilt = false;
        Object messageContextBuilt = axis2MC.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        if (messageContextBuilt != null) {
            isMessageContextBuilt = (Boolean) messageContextBuilt;
        }

        return isMessageContextBuilt;
    }

    /**
     * Utility function to extract collection of String from Map when the key is given.
     *
     * @param map  Map of String, Collection<String>
     * @param name key
     * @return Collection of Strings
     */
    public static Collection<String> getFromMapOrEmptyList(Map<String, Collection<String>> map, String name) {

        if (name != null && map.containsKey(name)) {

            return map.get(name).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        } else {
            return Collections.emptyList();
        }

    }

    /**
     * Method to remove query params from request path.
     *
     * @param subResourcePath full request path.
     * @return cleaned request path.
     */
    public static String getRestSubRequestPath(String subResourcePath) {

        if (subResourcePath.contains("?")) {
            return subResourcePath.split("\\?")[0];
        } else if (subResourcePath.contains("#")) {
            return subResourcePath.split("#")[0];
        } else {
            return subResourcePath;
        }
    }

    /**
     * Utility function to extract query params from resource path.
     *
     * @param apiResource reource path with query params.
     * @param path        path without query params.
     * @return Map of Query params.
     * @throws UnsupportedEncodingException When failed to decode
     */
    public static Map<String, Collection<String>> getQueryParams(String apiResource, String path)
            throws UnsupportedEncodingException {

        Map<String, String> queryParams = new HashMap<>();
        if (!apiResource.equals(path) && apiResource.contains("?")) {
            String queryString = apiResource.replace(path + "?", "");
            String[] query = queryString.split("&");
            for (String keyValue : query) {
                int idx = keyValue.indexOf("=");
                queryParams.put(
                        URLDecoder.decode(keyValue.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(keyValue.substring(idx + 1), "UTF-8"));
            }
        }
        return queryParams.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> Collections.singleton(entry.getValue())));
    }

}
