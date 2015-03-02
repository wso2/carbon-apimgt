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

package org.apache.synapse.mediators.transform.url;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorContext;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URISyntaxException;

/**
 * Represents a URL rewrite rule. A rule can consist of an optional condition
 * and one or more rewrite actions. If the condition is present, actions will be
 * executed only when the condition evaluates to true. If the condition is not
 * present, all the provided actions will be executed. If an error occurs while
 * evaluating the condition, the condition is treated as evaluated to false.
 * Condition evaluation is handled by Synapse evaluator framework. When executing
 * multiple rewrite actions, they are executed in the specified order.
 */
public class RewriteRule {

    private static final Log log = LogFactory.getLog(RewriteRule.class);

    private Evaluator condition;
    private List<RewriteAction> actions = new ArrayList<RewriteAction>();

    public void rewrite(URIFragments fragments,
                        MessageContext messageContext) throws URISyntaxException {

        if (condition != null) {
            String uriString = fragments.toURIString();
            Map<String, String> headers = getHeaders(messageContext);
            EvaluatorContext ctx = new EvaluatorContext(uriString, headers);
            ctx.setProperties(((Axis2MessageContext) messageContext).getProperties());
            ctx.setMessageContext(((Axis2MessageContext) messageContext).getAxis2MessageContext());
            
            if (log.isTraceEnabled()) {
                log.trace("Evaluating condition with URI: " + uriString);
            }

            try {
                if (!condition.evaluate(ctx)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Condition evaluated to 'false' - Skipping the current action");
                    }
                    return;
                }

                if (log.isTraceEnabled()) {
                    log.trace("Condition evaluated to 'true' - Performing the stated action");
                }
            } catch (EvaluatorException e) {
                log.warn("Error while evaluating the condition - Skipping the rule as it failed", e);
                return;
            }
        }

        for (RewriteAction action : actions) {
            action.execute(fragments, messageContext);
        }
    }

    private Map<String, String> getHeaders(MessageContext synCtx) {
        Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
        org.apache.axis2.context.MessageContext axis2MessageCtx =
                axis2smc.getAxis2MessageContext();
        Object headers = axis2MessageCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map<String, String> evaluatorHeaders = new HashMap<String, String>();

        if (headers != null && headers instanceof Map) {
            Map headersMap = (Map) headers;
            for (Object entryObj : headersMap.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    evaluatorHeaders.put((String) entry.getKey(), (String) entry.getValue());
                }
            }
        }
        return evaluatorHeaders;
    }

    public Evaluator getCondition() {
        return condition;
    }

    public void setCondition(Evaluator condition) {
        this.condition = condition;
    }

    public void addRewriteAction(RewriteAction action) {
        actions.add(action);
    }

    public List<RewriteAction> getActions() {
        return actions;
    }
}
