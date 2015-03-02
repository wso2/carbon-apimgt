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

package org.apache.synapse.eventing.filters;

import org.apache.axis2.context.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.SynapseException;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.eventing.EventFilter;
import org.wso2.eventing.Event;
import org.jaxen.JaxenException;

/**
 * Topic baed event filter that match the subscription based on a given topic
 */
public class TopicBasedEventFilter implements EventFilter<MessageContext> {

    private AXIOMXPath sourceXpath;
    private String resultValue;
    private static final String FILTER_SEP = "/";

    private static final Log log = LogFactory.getLog(TopicBasedEventFilter.class);

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String toString() {
        return resultValue;
    }

    public AXIOMXPath getSourceXpath() {
        return sourceXpath;
    }

    public void setSourceXpath(SynapseXPath sourceXpath) {
        this.sourceXpath = sourceXpath;
    }

    public boolean match(Event<MessageContext> event) {
        MessageContext messageContext = event.getMessage();
        String evaluatedValue = null;
        try {
            OMElement topicNode = (OMElement) sourceXpath.selectSingleNode(
                    messageContext.getEnvelope());
            if (topicNode != null) {
                evaluatedValue = topicNode.getText();
            }
        } catch (JaxenException e) {
            handleException("Error creating topic xpath",e);
        }
        if (evaluatedValue != null){
            if (evaluatedValue.equals(resultValue)) {
                return true;
            } else if (evaluatedValue.startsWith((resultValue + FILTER_SEP).trim())) {
                return true;
            }
        }
        return false;
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new SynapseException(message, e);
    }
}