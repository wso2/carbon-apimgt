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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;

public class SchemaValidationUtils {

    private static final Log logger = LogFactory.getLog(SchemaValidationUtils.class);
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
            String queryString = apiResource.split("\\?")[1];
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

    /**
     * Get the Request/Response messageContent as a JsonObject.
     *
     * @param messageContext Message context
     * @return JsonElement which contains the request/response message content
     */
    public static Optional<String> getMessageContent(MessageContext messageContext) {

        Optional<String> payloadObject = Optional.empty();
        org.apache.axis2.context.MessageContext axis2Context = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        boolean isMessageContextBuilt = isMessageContextBuilt(axis2Context);
        if (!isMessageContextBuilt) {
            // Build Axis2 Message.
            try {
                RelayUtils.buildMessage(axis2Context);
            } catch (IOException | XMLStreamException e) {
                logger.error(" Unable to build axis2 message");
            }
        }

        if (JsonUtil.hasAJsonPayload(axis2Context)) {
            payloadObject = Optional.of(JsonUtil.jsonPayloadToString(axis2Context));
        } else if (messageContext.getEnvelope().getBody() != null) {
            Object objFirstElement = messageContext.getEnvelope().getBody().getFirstElement();
            if (objFirstElement != null) {
                OMElement xmlResponse = messageContext.getEnvelope().getBody().getFirstElement();
                try {
                    payloadObject = Optional.of(JsonUtil.toJsonString(xmlResponse).toString());
                } catch (AxisFault axisFault) {
                    logger.error(" Error occurred while converting the String payload to Json");
                }
            }
        }
        return payloadObject;
    }

    public static boolean isMessageContextBuilt(org.apache.axis2.context.MessageContext axis2MC) {

        boolean isMessageContextBuilt = false;
        Object messageContextBuilt = axis2MC.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        if (messageContextBuilt != null) {
            isMessageContextBuilt = (Boolean) messageContextBuilt;
        }

        return isMessageContextBuilt;
    }
}
