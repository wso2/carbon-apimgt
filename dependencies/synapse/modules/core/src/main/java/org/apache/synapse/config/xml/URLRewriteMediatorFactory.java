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

import org.apache.synapse.Mediator;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.config.EvaluatorFactoryFinder;
import org.apache.synapse.mediators.transform.url.URLRewriteMediator;
import org.apache.synapse.mediators.transform.url.RewriteRule;
import org.apache.synapse.mediators.transform.url.RewriteAction;
import org.apache.synapse.mediators.transform.url.URIFragments;
import org.apache.axiom.om.OMElement;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

/**
 * Creates an instance of the URLRewriteMediator given an XML configuration which
 * adheres to the following grammar.
 *
 * <pre>
 *  &lt;rewrite [inProperty="inputURL"] [outProperty="outputURL"]&gt;
 *      &lt;rewriterule&gt;
 *          &lt;condition&gt;
 *              evaluator configuration
 *          &lt;/condition&gt; ?
 *          &lt;action
 *              value="value"
 *              xpath="xpath"
 *              [type="set | append | prepend | replace | remove"]
 *              [fragment="protocol | user | host | port | path | query | ref | full"]
 *              [regex="regex"] /&gt; +
 *      &lt;/rewriterule&gt; *
 *  &lt;/rewrite&gt;
 * </pre>
 */
public class URLRewriteMediatorFactory extends AbstractMediatorFactory {

    private static final QName REWRITE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "rewrite");

    private static final QName RULE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "rewriterule");
    private static final QName CONDITION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "condition");
    private static final QName ACTION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "action");

    private static final QName ATT_IN_PROPERTY = new QName("inProperty");
    private static final QName ATT_OUT_PROPERTY = new QName("outProperty");
    private static final QName ATT_TYPE = new QName("type");
    private static final QName ATT_FRAGMENT = new QName("fragment");

    public static final String ACTION_SET = "set";
    public static final String ACTION_APPEND = "append";
    public static final String ACTION_PREPEND = "prepend";
    public static final String ACTION_REPLACE = "replace";
    public static final String ACTION_REMOVE = "remove";

    public static final String FRAGMENT_PROTOCOL = "protocol";
    public static final String FRAGMENT_USER_INFO = "user";
    public static final String FRAGMENT_HOST = "host";
    public static final String FRAGMENT_PORT = "port";
    public static final String FRAGMENT_PATH = "path";
    public static final String FRAGMENT_QUERY = "query";
    public static final String FRAGMENT_REF = "ref";
    public static final String FRAGMENT_FULL_URI = "full";

    protected Mediator createSpecificMediator(OMElement element, Properties properties) {
        Iterator rules = element.getChildrenWithName(RULE_Q);
        String inputProperty = element.getAttributeValue(ATT_IN_PROPERTY);
        String outputProperty = element.getAttributeValue(ATT_OUT_PROPERTY);

        URLRewriteMediator mediator = new URLRewriteMediator();
        if (inputProperty != null) {
            mediator.setInputProperty(inputProperty);
        }
        if (outputProperty != null) {
            mediator.setOutputProperty(outputProperty);
        }

        while (rules.hasNext()) {
            mediator.addRule(parseRule((OMElement) rules.next()));
        }
        processAuditStatus(mediator, element);
        
        return mediator;
    }

    private RewriteRule parseRule(OMElement ruleElement) {
        Iterator actions = ruleElement.getChildrenWithName(ACTION_Q);
        if (actions == null) {
            handleException("At least one rewrite action is required per rule");
            return null;
        }

        RewriteRule rule = new RewriteRule();
        while (actions.hasNext()) {
            rule.addRewriteAction(parseAction((OMElement) actions.next()));
        }

        OMElement condition = ruleElement.getFirstChildWithName(CONDITION_Q);
        if (condition != null) {
            OMElement child = condition.getFirstElement();
            if (child != null) {
                try {
                    Evaluator eval = EvaluatorFactoryFinder.getInstance().getEvaluator(child);
                    rule.setCondition(eval);
                } catch (EvaluatorException e) {
                    handleException("Error while parsing the rule condition", e);
                }
            }
        }

        return rule;
    }

    private RewriteAction parseAction(OMElement actionElement) {
        String value = actionElement.getAttributeValue(ATT_VALUE);
        String xpath = actionElement.getAttributeValue(ATT_XPATH);
        String type = actionElement.getAttributeValue(ATT_TYPE);
        QName xpath_Q  = new QName(XMLConfigConstants.NULL_NAMESPACE, "xpath");
        
        if (value == null && xpath == null && !ACTION_REMOVE.equals(type)) {
            handleException("value or xpath attribute is required on the action element");
        }

        RewriteAction action = new RewriteAction();
        if (xpath != null) {
            try {        
            	action.setXpath(SynapseXPathFactory.getSynapseXPath(actionElement, xpath_Q));
            } catch (JaxenException e) {
                handleException("Error while parsing the XPath expression: " + xpath, e);
            }
        } else if (value != null) {
            action.setValue(value);
        }

        String fragment = actionElement.getAttributeValue(ATT_FRAGMENT);
        if (fragment != null) {
            if (FRAGMENT_PROTOCOL.equals(fragment)) {
                action.setFragmentIndex(URIFragments.PROTOCOL);
            } else if (FRAGMENT_USER_INFO.equals(fragment)) {
                action.setFragmentIndex(URIFragments.USER_INFO);
            } else if (FRAGMENT_HOST.equals(fragment)) {
                action.setFragmentIndex(URIFragments.HOST);
            } else if (FRAGMENT_PORT.equals(fragment)) {
                action.setFragmentIndex(URIFragments.PORT);
            } else if (FRAGMENT_PATH.equals(fragment)) {
                action.setFragmentIndex(URIFragments.PATH);
            } else if (FRAGMENT_QUERY.equals(fragment)) {
                action.setFragmentIndex(URIFragments.QUERY);
            } else if (FRAGMENT_REF.equals(fragment)) {
                action.setFragmentIndex(URIFragments.REF);
            } else if (FRAGMENT_FULL_URI.equals(fragment)) {
                action.setFragmentIndex(URIFragments.FULL_URI);
            } else {
                handleException("Unknown URL fragment name: " + fragment);
            }
        }

        if (type != null) {
            if (ACTION_SET.equals(type)) {
                action.setActionType(RewriteAction.ACTION_SET);
            } else if (ACTION_APPEND.equals(type)) {
                action.setActionType(RewriteAction.ACTION_APPEND);
            } else if (ACTION_PREPEND.equals(type)) {
                action.setActionType(RewriteAction.ACTION_PREPEND);
            } else if (ACTION_REPLACE.equals(type)) {
                action.setActionType(RewriteAction.ACTION_REPLACE);
                String regex = actionElement.getAttributeValue(ATT_REGEX);
                if (regex != null) {
                    action.setRegex(regex);
                } else {
                    handleException("regex attribute is required for replace action");
                }
            } else if (ACTION_REMOVE.equals(type)) {
                action.setActionType(RewriteAction.ACTION_REMOVE);
            } else {
                handleException("Unknown URL rewrite action type: " + type);
            }
        }

        return action;
    }

    public QName getTagQName() {
        return REWRITE_Q;
    }
}
