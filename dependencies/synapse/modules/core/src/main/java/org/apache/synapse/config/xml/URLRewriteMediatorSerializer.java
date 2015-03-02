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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.config.EvaluatorSerializer;
import org.apache.synapse.commons.evaluators.config.EvaluatorSerializerFinder;
import org.apache.synapse.mediators.transform.url.URLRewriteMediator;
import org.apache.synapse.mediators.transform.url.RewriteRule;
import org.apache.synapse.mediators.transform.url.RewriteAction;
import org.apache.synapse.mediators.transform.url.URIFragments;

import java.util.List;

public class URLRewriteMediatorSerializer extends AbstractMediatorSerializer {

    protected OMElement serializeSpecificMediator(Mediator m) {
        if (!(m instanceof URLRewriteMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
            return null;
        }

        URLRewriteMediator mediator = (URLRewriteMediator) m;
        OMElement rewrite = fac.createOMElement("rewrite", synNS);        
        
        String inProperty = mediator.getInputProperty();
        String outProperty = mediator.getOutputProperty();
        
        if (inProperty != null) {
        	rewrite.addAttribute(fac.createOMAttribute("inProperty", nullNS,
        	                                              inProperty));
        }
        if (outProperty != null) {
        	rewrite.addAttribute(fac.createOMAttribute("outProperty", nullNS,
        	                                              outProperty));
        }

        saveTracingState(rewrite, mediator);
        
        List<RewriteRule> rules = mediator.getRules();
        try {
            for (RewriteRule r : rules) {
                OMElement rule = serializeRule(r);
                rewrite.addChild(rule);
            }
        } catch (EvaluatorException e) {
            handleException("Error while serializing the rewrite rule", e);
        }

        return rewrite;
    }

    private OMElement serializeRule(RewriteRule r) throws EvaluatorException {
        OMElement rule = fac.createOMElement("rewriterule", synNS);
        Evaluator condition = r.getCondition();
        if (condition != null) {
            OMElement conditionElt = fac.createOMElement("condition", synNS);
            EvaluatorSerializer serializer = EvaluatorSerializerFinder.getInstance().
                    getSerializer(condition.getName());
            serializer.serialize(conditionElt, condition);
            rule.addChild(conditionElt);
        }

        List<RewriteAction> actions = r.getActions();
        for (RewriteAction a : actions) {
            OMElement action = serializeAction(a);
            rule.addChild(action);
        }

        return rule;
    }

    private OMElement serializeAction(RewriteAction a) {
        OMElement action = fac.createOMElement("action", synNS);
        if (a.getValue() != null) {
            action.addAttribute("value", a.getValue(), null);
        } else if (a.getXpath() != null) {         
            SynapseXPathSerializer.serializeXPath(a.getXpath(), action, "xpath");
        }

        if (a.getRegex() != null) {
            action.addAttribute("regex", a.getRegex(), null);
        }

        int type = a.getActionType();
        String typeStr;
        switch (type) {
            case RewriteAction.ACTION_APPEND:
                typeStr = URLRewriteMediatorFactory.ACTION_APPEND;
                break;

            case RewriteAction.ACTION_PREPEND:
                typeStr = URLRewriteMediatorFactory.ACTION_PREPEND;
                break;

            case RewriteAction.ACTION_REPLACE:
                typeStr = URLRewriteMediatorFactory.ACTION_REPLACE;
                break;

            case RewriteAction.ACTION_REMOVE:
                typeStr = URLRewriteMediatorFactory.ACTION_REMOVE;
                break;

            default:
                typeStr = URLRewriteMediatorFactory.ACTION_SET;
        }
        action.addAttribute("type", typeStr, null);

        int fragment = a.getFragmentIndex();
        String fragmentStr;
        switch (fragment) {
            case URIFragments.PROTOCOL:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_PROTOCOL;
                break;

            case URIFragments.USER_INFO:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_USER_INFO;
                break;

            case URIFragments.HOST:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_HOST;
                break;

            case URIFragments.PORT:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_PORT;
                break;

            case URIFragments.PATH:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_PATH;
                break;

            case URIFragments.QUERY:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_QUERY;
                break;

            case URIFragments.REF:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_REF;
                break;

            default:
                fragmentStr = URLRewriteMediatorFactory.FRAGMENT_FULL_URI;
        }
        action.addAttribute("fragment", fragmentStr, null);
        
        return action;
    }

    public String getMediatorClassName() {
        return URLRewriteMediator.class.getName();
    }
}
