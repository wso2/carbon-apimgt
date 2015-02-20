/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.util.xpath;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.jaxen.JaxenException;

import java.io.*;

public class SynapseJsonPath extends SynapsePath {

    private static final Log log = LogFactory.getLog(SynapseJsonPath.class);

    private String enableStreamingJsonPath = SynapsePropertiesLoader.loadSynapseProperties().
    getProperty(SynapseConstants.STREAMING_JSONPATH_PROCESSING);

    private JsonPath jsonPath;

    private boolean isWholeBody = false;

    public SynapseJsonPath(String jsonPathExpression)  throws JaxenException {
        super(jsonPathExpression, SynapsePath.JSON_PATH, log);
        this.contentAware = true;
        this.expression = jsonPathExpression;
        jsonPath = JsonPath.compile(jsonPathExpression);
        // Check if the JSON path expression evaluates to the whole payload. If so no point in evaluating the path.
        if ("$".equals(jsonPath.getPath().trim()) || "$.".equals(jsonPath.getPath().trim())) {
            isWholeBody = true;
        }
        this.setPathType(SynapsePath.JSON_PATH);
    }

    public String stringValueOf(final String jsonString) {
        if (jsonString == null) {
            return "";
        }
        if (isWholeBody) {
            return jsonString;
        }
        Object read;
        read = jsonPath.read(jsonString);
        return (null == read ? "null" : read.toString());
    }

    public String stringValueOf(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext amc = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        InputStream stream;
        if (!JsonUtil.hasAJsonPayload(amc) || "true".equals(enableStreamingJsonPath)) {
            try {
                if (null == amc.getEnvelope().getBody().getFirstElement()) {
                    // Get message from PT Pipe.
                    stream = getMessageInputStreamPT(amc);
                    if (stream == null) {
                        stream = JsonUtil.getJsonPayload(amc);
                    } else {
                        JsonUtil.newJsonPayload(amc, stream, true, true);
                    }
                } else {
                    // Message Already built.
                    stream = JsonUtil.toJsonStream(amc.getEnvelope().getBody().getFirstElement());
                }
                return stringValueOf(stream);
            } catch (IOException e) {
                handleException("Could not find JSON Stream in PassThrough Pipe during JSON path evaluation.", e);
            }
        } else {
            stream = JsonUtil.getJsonPayload(amc);
            return stringValueOf(stream);
        }
        return "";
    }

    public String stringValueOf(final InputStream jsonStream) {
        if (jsonStream == null) {
            return "";
        }
        if (isWholeBody) {
            try {
                return IOUtils.toString(jsonStream);
            } catch(IOException e) {
                log.error("#stringValueOf. Could not convert JSON input stream to String.");
                return "";
            }
        }
        Object read;
        try {
            read = jsonPath.read(jsonStream);
            if (log.isDebugEnabled()) {
                log.debug("#stringValueOf. Evaluated JSON path <" + jsonPath.getPath() + "> : <" + (read == null ? null : read.toString()) + ">");
            }
            return (null == read ? "null" : read.toString());
        } catch (IOException e) {
            handleException("Error evaluating JSON Path <" + jsonPath.getPath() + ">", e);
        } catch (Exception e) { // catch invalid json paths that do not match with the existing JSON payload.
            log.error("#stringValueOf. Error evaluating JSON Path <" + jsonPath.getPath() + ">. Returning empty result. Error>>> " + e.getLocalizedMessage());
            return "";
        }
        if (log.isDebugEnabled()) {
            log.debug("#stringValueOf. Evaluated JSON path <" + jsonPath.getPath() + "> : <null>.");
        }
        return "";
    }

    public String getJsonPathExpression() {
        return expression;
    }

    public void setJsonPathExpression(String jsonPathExpression) {
        this.expression = jsonPathExpression;
    }
}
