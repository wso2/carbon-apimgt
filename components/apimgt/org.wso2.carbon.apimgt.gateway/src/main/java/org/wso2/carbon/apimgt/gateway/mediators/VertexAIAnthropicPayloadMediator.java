/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.gateway.mediators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

/**
 * Adapts an Anthropic Messages request body for the Vertex AI {@code rawPredict} endpoint.
 * <p>
 * When Anthropic Claude models are served through Vertex, the request body must differ from the direct
 * Anthropic API in two ways: it has to carry {@code "anthropic_version": "vertex-2023-10-16"}, and it must
 * <em>not</em> contain a {@code "model"} field (on Vertex the model is encoded in the request URL, and
 * including it in the body is rejected). Since the AI Gateway forwards the client's body as a passthrough,
 * this mediator performs that small adaptation so callers can send a standard Anthropic body: it injects the
 * {@code anthropic_version} when absent and strips {@code model}, leaving every other field untouched.
 * <p>
 * The mediator is engaged only for Anthropic-on-Vertex endpoints (selected in the endpoint sequence template
 * by the {@code publishers/anthropic} URL segment), so Gemini and other traffic never incur body parsing. It
 * is defensive by design: a missing, empty, or non-object payload is forwarded unchanged, and any failure is
 * logged rather than propagated, so a malformed body never breaks mediation.
 */
public class VertexAIAnthropicPayloadMediator extends AbstractMediator {

    private static final String ANTHROPIC_VERSION_FIELD = "anthropic_version";
    private static final String VERTEX_ANTHROPIC_VERSION = "vertex-2023-10-16";
    private static final String MODEL_FIELD = "model";

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("VertexAIAnthropicPayloadMediator is invoked...");
        }
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        try {
            if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
                return true;
            }
            String jsonPayload = JsonUtil.jsonPayloadToString(axis2MessageContext);
            if (StringUtils.isBlank(jsonPayload)) {
                return true;
            }
            JsonElement root = JsonParser.parseString(jsonPayload);
            if (!root.isJsonObject()) {
                return true;
            }
            JsonObject body = root.getAsJsonObject();
            boolean modified = false;
            if (!body.has(ANTHROPIC_VERSION_FIELD)) {
                body.addProperty(ANTHROPIC_VERSION_FIELD, VERTEX_ANTHROPIC_VERSION);
                modified = true;
            }
            if (body.has(MODEL_FIELD)) {
                // Vertex encodes the model in the URL; a model field in the body is rejected on rawPredict.
                body.remove(MODEL_FIELD);
                modified = true;
            }
            if (modified) {
                JsonUtil.removeJsonPayload(axis2MessageContext);
                JsonUtil.getNewJsonPayload(axis2MessageContext, body.toString(), true, true);
            }
            return true;
        } catch (Exception e) {
            // Never fail mediation on an edge/malformed payload - forward the original body and log.
            log.error("Error while adapting the Anthropic request body for Vertex AI; "
                    + "forwarding the original payload.", e);
            return true;
        }
    }

    @Override
    public boolean isContentAware() {

        return true;
    }
}
