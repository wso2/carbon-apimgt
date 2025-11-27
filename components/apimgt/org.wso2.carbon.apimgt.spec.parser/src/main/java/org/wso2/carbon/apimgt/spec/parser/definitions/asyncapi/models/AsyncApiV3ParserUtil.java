/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models;

import io.apicurio.datamodels.models.MappedNode;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServer;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper Class utilities for AsyncApiV3Parser to keep parsing and template building logic reusable.
 */
public class AsyncApiV3ParserUtil {
    private static final Log log = LogFactory.getLog(AsyncApiV3ParserUtil.class);

    private AsyncApiV3ParserUtil() {
        // static util
    }

    /**
     * Utility method to safely convert a MappedNode<T> into a Map<String, T>.
     */
    public static <T> Map<String, T> toMap(MappedNode<T> node) {
        if (node == null || node.getItemNames() == null) {
            return Collections.emptyMap();
        }

        List<String> names = node.getItemNames();
        List<T> items = node.getItems();

        return IntStream.range(0, Math.min(names.size(), items.size()))
                .filter(i -> items.get(i) != null)
                .boxed()
                .collect(Collectors.toMap(names::get, items::get, (a, b) -> b, LinkedHashMap::new));
    }

    /**
     * Extract channel name from a $ref like "#/channels/myChannel" (returns "myChannel").
     * Expected format: #/channels/<name>
     */

    public static String extractChannelNameFromRef(String ref) {
        if (ref == null) {
            return null;
        }
        int index = ref.lastIndexOf('/');
        if (index == -1 || index == ref.length() - 1) {
            return null;
        }

        String extracted = ref.substring(index + 1);

        // Need to fix in the Publisher UI
        // Temp fix; if the actual channel is wildcard "/*", preserve the slash
        // This fix currently only works for wildcard "/*"
//        if ("*".equals(extracted) && ref.contains("/channels/*")) {
//            return "/*";
//        }

        return extracted;
    }

    /**
     * Extract only channel name from full absolute path or relative path
     * Remove ALL leading slashes like "#/channels//myChannel" or "#/channels///myChannel" (returns "myChannel").
     */
    public static String normalizeChannelName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name;
    }

    /**
     * Extract and set the Host and Pathname from the full URL
     */
    public static void setAsyncApiServerFromUrl(String url, AsyncApiServer server, String apiType) {

        AsyncApi30Server asyncApiServer = (AsyncApi30Server) server;

        if (url == null || url.isEmpty()) {
            asyncApiServer.setHost("");
            asyncApiServer.setPathname("/");
            if (apiType != null) {
                asyncApiServer.setProtocol(apiType.toLowerCase());
            }
            return;
        }

        String host = "";
        String path = "/";

        try {
            String fixed = url.contains("://") ? url : "http://" + url;
            URI uri = new URI(fixed);

            if (uri.getHost() != null) {
                host = uri.getPort() == -1
                        ? uri.getHost()
                        : uri.getHost() + ":" + uri.getPort();
            } else if (uri.getAuthority() != null) {
                host = uri.getAuthority();
            }

            if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                path = uri.getPath();
            }

        } catch (Exception ignore) {
            String working = url;
            int schemePos = working.indexOf("://");
            int start = schemePos >= 0 ? schemePos + 3 : 0;

            int slashPos = working.indexOf('/', start);
            if (slashPos >= 0) {
                host = working.substring(start, slashPos);
                path = working.substring(slashPos);
            } else {
                host = working.substring(start);
                path = "/";
            }
        }

        asyncApiServer.setHost(host);
        asyncApiServer.setPathname(path);

        if (apiType != null) {
            asyncApiServer.setProtocol(apiType.toLowerCase());
        }
    }


}
