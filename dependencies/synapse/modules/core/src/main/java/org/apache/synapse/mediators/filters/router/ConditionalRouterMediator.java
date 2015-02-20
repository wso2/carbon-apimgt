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

package org.apache.synapse.mediators.filters.router;

import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.evaluators.EvaluatorContext;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks whether the route condition evaluates to true and mediates using the target if it evaluates to true.
 * Matching route will break the router if the <code>breakRoute</code> is set to true on the evaluated route
 *
 * @see org.apache.synapse.Mediator
 */
public class ConditionalRouterMediator extends AbstractMediator {

    private List<ConditionalRoute> conditionalRoutes = new ArrayList<ConditionalRoute>();

    private boolean continueAfter;
    
    private boolean continueAfterExplicitlySet;
    
    public boolean mediate(MessageContext synCtx) {
        
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

        String url = synCtx.getTo().getAddress();
        EvaluatorContext context = new EvaluatorContext(url, evaluatorHeaders);
        context.setProperties(((Axis2MessageContext) synCtx).getProperties());
        context.setMessageContext(((Axis2MessageContext) synCtx).getAxis2MessageContext());

        try {
            for (ConditionalRoute conditionalRoute : conditionalRoutes) {
                if (conditionalRoute.getEvaluator().evaluate(context)) {
                    conditionalRoute.getTarget().mediate(synCtx);
                    if (conditionalRoute.isBreakRoute()) {
                        break;
                    }
                }
            }
        } catch (EvaluatorException ee) {
            handleException("Couldn't evaluate the route condition", ee, synCtx);
        }
        return continueAfter;
    }

    public List<ConditionalRoute> getConditionalRoutes() {
        return conditionalRoutes;
    }

    public void addRoute(ConditionalRoute conditionalRoute) {
        conditionalRoutes.add(conditionalRoute);
    }

    public boolean isContinueAfter() {
        return continueAfter;
    }

    public void setContinueAfter(boolean continueAfter) {
        this.continueAfterExplicitlySet = true;
        this.continueAfter = continueAfter;
    }

    public boolean isContinueAfterExplicitlySet() {
        return continueAfterExplicitlySet;
    }
}
