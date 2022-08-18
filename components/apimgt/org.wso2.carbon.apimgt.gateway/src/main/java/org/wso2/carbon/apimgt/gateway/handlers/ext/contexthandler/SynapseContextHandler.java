/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.ext.contexthandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ContextHandler;

public class SynapseContextHandler implements ContextHandler {

    private static final Log log = LogFactory.getLog(SynapseContextHandler.class);

    private MessageContext messageContext;

    public SynapseContextHandler(MessageContext messageContext) {

        this.messageContext = messageContext;
    }

    @Override
    public Object getProperty(String key) {
        return this.messageContext.getProperty(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        if (value != null) {
            this.messageContext.setProperty(key, value);
        }
    }

    @Override
    public String getAPIKeyAsQueryParam()  {
        String apiKey = null;
        try {
            apiKey = new SynapseXPath("$url:apikey").stringValueOf(messageContext);
        } catch (JaxenException er) {
            log.error("Error occurred while reading a apiKey query param", er);
        }
        return apiKey;
    }
}
