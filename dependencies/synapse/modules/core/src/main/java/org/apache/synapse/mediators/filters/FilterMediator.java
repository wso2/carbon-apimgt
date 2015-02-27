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

package org.apache.synapse.mediators.filters;

import org.apache.synapse.ContinuationState;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.xml.AnonymousListMediator;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.continuation.ReliantContinuationState;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractListMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.mediators.ListMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The filter mediator combines the regex and xpath filtering functionality. If an xpath
 * is set, it is evaluated; else the given regex is evaluated against the source xpath.
 */
public class FilterMediator extends AbstractListMediator implements
    org.apache.synapse.mediators.FilterMediator, FlowContinuableMediator {

    private SynapsePath source = null;
    private Pattern regex = null;
    private SynapsePath xpath = null;
    private AnonymousListMediator elseMediator = null;
    private boolean thenElementPresent = false;
    private String thenKey = null;
    private String elseKey = null;
    private SynapseEnvironment synapseEnv;

    @Override
    public void init(SynapseEnvironment se) {
        super.init(se);
        synapseEnv = se;
        if (elseMediator != null) {
            elseMediator.init(se);
        } else if (elseKey != null) {
            SequenceMediator elseSequence =
                    (SequenceMediator) se.getSynapseConfiguration().
                            getSequence(elseKey);

            if (elseSequence == null || elseSequence.isDynamic()) {
                se.addUnavailableArtifactRef(elseKey);
            }
        }

        if (thenKey != null) {
            SequenceMediator thenSequence =
                    (SequenceMediator) se.getSynapseConfiguration().
                            getSequence(thenKey);

            if (thenSequence == null || thenSequence.isDynamic()) {
                se.addUnavailableArtifactRef(thenKey);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (elseMediator != null) {
            elseMediator.destroy();
        } else if (elseKey != null) {
            SequenceMediator elseSequence =
                    (SequenceMediator) synapseEnv.getSynapseConfiguration().
                            getSequence(elseKey);

            if (elseSequence == null || elseSequence.isDynamic()) {
                synapseEnv.removeUnavailableArtifactRef(elseKey);
            }
        }

        if (thenKey != null) {
            SequenceMediator thenSequence =
                    (SequenceMediator) synapseEnv.getSynapseConfiguration().
                            getSequence(thenKey);

            if (thenSequence == null || thenSequence.isDynamic()) {
                synapseEnv.removeUnavailableArtifactRef(thenKey);
            }
        }
    }

    /**
     * Executes the list of sub/child mediators, if the filter condition is satisfied
     *
     * @param synCtx the current message
     * @return true if filter condition fails. else returns as per List mediator semantics
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Filter mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        boolean result = false;
        if (test(synCtx)) {

            if (thenKey != null) {

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug((xpath == null ?
                        "Source : " + source + " against : " + regex.pattern() + " matches" :
                        "XPath expression : "  + xpath + " evaluates to true") +
                        " - executing then sequence with key : " + thenKey);
                }

                ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
                Mediator seq = synCtx.getSequence(thenKey);
                if (seq != null) {
                    result = seq.mediate(synCtx);
                } else {
                    handleException("Couldn't find the referred then sequence with key : "
                        + thenKey, synCtx);
                }
                
            } else {

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug((xpath == null ?
                        "Source : " + source + " against : " + regex.pattern() + " matches" :
                        "XPath expression : "  + xpath + " evaluates to true") +
                        " - executing child mediators");
                }

                ContinuationStackManager.
                        addReliantContinuationState(synCtx, 0, getMediatorPosition());
                result = super.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }

            }

        } else {

            if (elseKey != null) {

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug((xpath == null ?
                        "Source : " + source + " against : " + regex.pattern() + " does not match" :
                        "XPath expression : "  + xpath + " evaluates to false") +
                        " - executing the else sequence with key : " + elseKey);
                }

                ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
                Mediator elseSeq = synCtx.getSequence(elseKey);

                if (elseSeq != null) {
                    result = elseSeq.mediate(synCtx);
                } else {
                    handleException("Couldn't find the referred else sequence with key : "
                        + elseKey, synCtx);
                }
                
            } else if (elseMediator != null) {

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug((xpath == null ?
                        "Source : " + source + " against : " + regex.pattern() + " does not match" :
                        "XPath expression : "  + xpath + " evaluates to false") +
                        " - executing the else path child mediators");
                }
                ContinuationStackManager.addReliantContinuationState(synCtx, 1, getMediatorPosition());
                result = elseMediator.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }

            } else {

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug((xpath == null ?
                        "Source : " + source + " against : " + regex.pattern() + " does not match" :
                        "XPath expression : "  + xpath + " evaluates to false and no else path") +
                        " - skipping child mediators");
                }
                result = true;
            }
        }

        synLog.traceOrDebug("End : Filter mediator ");
        return result;
    }

    public boolean mediate(MessageContext synCtx,
                           ContinuationState continuationState) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Filter mediator : Mediating from ContinuationState");
        }

        boolean result;
        int subBranch = ((ReliantContinuationState) continuationState).getSubBranch();
        if (subBranch == 0) {
           if (!continuationState.hasChild()) {
               result = super.mediate(synCtx, continuationState.getPosition() + 1);
           } else {
               FlowContinuableMediator mediator =
                       (FlowContinuableMediator) getChild(continuationState.getPosition());
               result = mediator.mediate(synCtx, continuationState.getChildContState());
           }
        } else {
            if (!continuationState.hasChild()) {
                result = elseMediator.mediate(synCtx, continuationState.getPosition() + 1);
            } else {
                FlowContinuableMediator mediator =
                        (FlowContinuableMediator) elseMediator.getChild(
                                continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        }

        return result;
    }

    /**
     * Tests the supplied condition after evaluation against the given XPath
     * or Regex (against a source XPath). When a regular expression is supplied
     * the source XPath is evaluated into a String value, and matched against
     * the given regex
     *
     * @param synCtx the current message for evaluation of the test condition
     * @return true if evaluation of the XPath/Regex results in true
     */
    public boolean test(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (xpath != null) {
            try {
                return xpath.booleanValueOf(synCtx);
            } catch (JaxenException e) {
                handleException("Error evaluating XPath expression : " + xpath, e, synCtx);
            }

        } else if (source != null && regex != null) {
            String sourceString = source.stringValueOf(synCtx);
            if (sourceString == null) {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Source String : " + source + " evaluates to null");
                }
                return false;
            }
            Matcher matcher = regex.matcher(sourceString);
            if (matcher == null) {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Regex pattern matcher for : " + regex.pattern() +
                        "against source : " + sourceString + " is null");
                }
                return false;
            }
            return matcher.matches();
        }

        return false; // never executes
    }


    public SynapsePath getSource() {
        return source;
    }

    public void setSource(SynapsePath source) {
        this.source = source;
    }

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    public SynapsePath getXpath() {
        return xpath;
    }

    public void setXpath(SynapsePath xpath) {
        this.xpath = xpath;
    }

    public ListMediator getElseMediator() {
        return elseMediator;
    }

    public void setElseMediator(AnonymousListMediator elseMediator) {
        this.elseMediator = elseMediator;
    }

    public boolean isThenElementPresent() {
        return thenElementPresent;
    }

    public void setThenElementPresent(boolean thenElementPresent) {
        this.thenElementPresent = thenElementPresent;
    }

    public String getThenKey() {
        return thenKey;
    }

    public void setThenKey(String thenKey) {
        this.thenKey = thenKey;
    }

    public String getElseKey() {
        return elseKey;
    }

    public void setElseKey(String elseKey) {
        this.elseKey = elseKey;
    }

    @Override
    public boolean isContentAware() {
        if (xpath != null) {
            return xpath.isContentAware();
        } else if (source != null) {
            return source.isContentAware();
        }
        return false;
    }

}
