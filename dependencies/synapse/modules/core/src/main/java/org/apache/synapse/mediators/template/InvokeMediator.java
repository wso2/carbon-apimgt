/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.synapse.mediators.template;

import org.apache.synapse.ContinuationState;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.continuation.ReliantContinuationState;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.eip.EIPUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class handles invocation of a synapse function template. <invoke
 * target=""> <parameter name="p1" value="{expr} | {{expr}} | value" />* ..
 * </invoke>
 */
public class InvokeMediator extends AbstractMediator implements
                                                     ManagedLifecycle,FlowContinuableMediator {
	/**
	 * refers to the target template this is going to invoke this is a read only
	 * attribute of the mediator
	 */
	private String targetTemplate;

	/**
	 * Refers to the parent package qualified reference
	 * 
	 */
	private String packageName;

	/**
	 * maps each parameter name to a Expression/Value this is a read only
	 * attribute of the mediator
	 */
	private Map<String, Value> pName2ExpressionMap;

	private boolean dynamicMediator = false;

	/** The local registry key which is used to pick a sequence definition */
	private Value key = null;

    /** Reference to the synapse environment */
    private SynapseEnvironment synapseEnv;

	public InvokeMediator() {
		// LinkedHashMap is used to preserve tag order
		pName2ExpressionMap = new LinkedHashMap<String, Value>();
	}


    public boolean mediate(MessageContext synCtx) {
        return mediate(synCtx, true);
    }

	private boolean mediate(MessageContext synCtx, boolean executePreFetchingSequence) {
		SynapseLog synLog = getLog(synCtx);

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Invoking Target EIP Sequence " + targetTemplate +
			                    " paramNames : " + pName2ExpressionMap.keySet());
			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + synCtx.getEnvelope());
			}
		}
		// get the target function template and invoke by passing populated
		// parameters
		Mediator mediator = synCtx.getSequenceTemplate(targetTemplate);
		
		if (this.isDynamicMediator() && mediator ==null) {
			handleException("Failed to locate the configure element for connector [" +
			                targetTemplate + "]", synCtx);
		}

		// executing key reference if found defined at configuration.
		if (executePreFetchingSequence && key != null) {
			String defaultConfiguration = key.evaluateValue(synCtx);
			Mediator m = synCtx.getDefaultConfiguration(defaultConfiguration);
			if (m == null) {
				handleException("Sequence named " + key + " cannot be found", synCtx);

			} else {
				if (synLog.isTraceOrDebugEnabled()) {
					synLog.traceOrDebug("Executing with key " + key);
				}
                ContinuationStackManager.addReliantContinuationState(
                        synCtx, 1, getMediatorPosition());
				boolean result = m.mediate(synCtx);

                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                } else {
                    return false;
                }
            }
		}

		if (mediator != null && mediator instanceof TemplateMediator) {
			populateParameters(synCtx, ((TemplateMediator) mediator).getName());
            if (executePreFetchingSequence) {
                ContinuationStackManager.addReliantContinuationState(
                        synCtx, 0, getMediatorPosition());
            }
            boolean result = mediator.mediate(synCtx);
			if (result && executePreFetchingSequence) {
				ContinuationStackManager.removeReliantContinuationState(synCtx);
			}
			return result;
		}
		return false;
	}

    public boolean mediate(MessageContext synCtx, ContinuationState continuationState) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Invoke mediator : Mediating from ContinuationState");
        }

        boolean result;
        int subBranch = ((ReliantContinuationState) continuationState).getSubBranch();

        if (subBranch == 0) {
             // Default flow
            TemplateMediator templateMediator =
                                        (TemplateMediator) synCtx.getSequenceTemplate(targetTemplate);

            if (!continuationState.hasChild()) {
                result = templateMediator.mediate(synCtx, continuationState.getPosition() + 1);
                if (result) {
                    templateMediator.popFuncContextFrom(synCtx);
                }
            } else {
                FlowContinuableMediator mediator =
                        (FlowContinuableMediator) templateMediator.getChild(
                                continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        } else {
            // Pre fetching invoke mediator flow
            String prefetchInvokeKey = key.evaluateValue(synCtx);
            InvokeMediator prefetchInvoke =
                    (InvokeMediator) synCtx.getDefaultConfiguration(prefetchInvokeKey);

            ContinuationState childContinuationState = continuationState.getChildContState();
            result = prefetchInvoke.mediate(synCtx, childContinuationState);

            if (result && !childContinuationState.hasChild()) {
                // Pre fetching invoke mediator flow completed.
                // Remove ContinuationState represent the prefetchInvoke mediator and
                // flip the subbranch to default flow
                continuationState.removeLeafChild();
                ((ReliantContinuationState) continuationState).setSubBranch(0);
                // after prefetch invoke mediator flow, execute default flow
                result = mediate(synCtx, false);
            }
        }
        return result;
    }

	/**
	 * poplulate declared parameters on temp synapse properties
	 * 
	 * @param synCtx
	 * @param templateQualifiedName
	 */
	private void populateParameters(MessageContext synCtx, String templateQualifiedName) {
		Iterator<String> params = pName2ExpressionMap.keySet().iterator();
		while (params.hasNext()) {
			String parameter = params.next();
			if (!"".equals(parameter)) {
				Value expression = pName2ExpressionMap.get(parameter);
				if (expression != null) {
					EIPUtils.createSynapseEIPTemplateProperty(synCtx, templateQualifiedName,
					                                          parameter, expression);
				}
			}
		}
	}

	public String getTargetTemplate() {
		return targetTemplate;
	}

	public void setTargetTemplate(String targetTemplate) {
		this.targetTemplate = targetTemplate;
	}

	public Map<String, Value> getpName2ExpressionMap() {
		return pName2ExpressionMap;
	}

	public void addExpressionForParamName(String pName, Value expr) {
		pName2ExpressionMap.put(pName, expr);
	}

	public boolean isDynamicMediator() {
		return dynamicMediator;
	}

	public void setDynamicMediator(boolean dynamicMediator) {
		this.dynamicMediator = dynamicMediator;
	}

	public Value getKey() {
		return key;
	}

	public void setKey(Value key) {
		this.key = key;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

    public void init(SynapseEnvironment se) {
        synapseEnv = se;

        TemplateMediator templateMediator =
                se.getSynapseConfiguration().getSequenceTemplate(targetTemplate);
        if (templateMediator == null || templateMediator.isDynamic()) {
            // undefined or dynamic templates are treated as unavailable
            // in the environment.
            // At the time of their initialization, these will be marked as available.
            se.addUnavailableArtifactRef(targetTemplate);
        }
    }

    public void destroy() {
        TemplateMediator templateMediator =
                synapseEnv.getSynapseConfiguration().getSequenceTemplate(targetTemplate);
        if (templateMediator == null || templateMediator.isDynamic()) {
            synapseEnv.removeUnavailableArtifactRef(targetTemplate);
        }
    }
}
