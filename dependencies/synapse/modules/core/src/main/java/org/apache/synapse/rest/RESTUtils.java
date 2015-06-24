/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.synapse.rest;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.dispatch.RESTDispatcher;
import org.apache.synapse.rest.dispatch.DefaultDispatcher;
import org.apache.synapse.rest.dispatch.URLMappingBasedDispatcher;
import org.apache.synapse.rest.dispatch.URITemplateBasedDispatcher;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RESTUtils {

    private static final Log log = LogFactory.getLog(RESTUtils.class);

    private static final List<RESTDispatcher> dispatchers = new ArrayList<RESTDispatcher>();

    static {
        dispatchers.add(new URLMappingBasedDispatcher());
        dispatchers.add(new URITemplateBasedDispatcher());
        dispatchers.add(new DefaultDispatcher());
    }

    public static String trimSlashes(String url) {
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        if (url.startsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String trimTrailingSlashes(String url) {
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String getFullRequestPath(MessageContext synCtx) {
        Object obj = synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (obj != null) {
            return (String) obj;
        }

        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();
        String url = (String) msgCtx.getProperty(Constants.Configuration.TRANSPORT_IN_URL);
        if (url == null) {
            url = (String) synCtx.getProperty(NhttpConstants.SERVICE_PREFIX);
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                url = new URL(url).getPath();
            } catch (MalformedURLException e) {
                handleException("Request URL: " + url + " is malformed", e);
            }
        }
        synCtx.setProperty(RESTConstants.REST_FULL_REQUEST_PATH, url);
        return url;
    }

    public static String getSubRequestPath(MessageContext synCtx) {
        return (String) synCtx.getProperty(RESTConstants.REST_SUB_REQUEST_PATH);
    }

    public static List<RESTDispatcher> getDispatchers() {
        return dispatchers;
    }

    private static void handleException(String msg, Throwable t) {
        log.error(msg, t);
        throw new SynapseException(msg, t);
    }

}
