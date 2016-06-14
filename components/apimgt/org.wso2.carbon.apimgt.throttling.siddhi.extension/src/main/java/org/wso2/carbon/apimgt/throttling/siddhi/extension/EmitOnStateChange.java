/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.throttling.siddhi.extension;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a custom extension, written for a certain throttler.
 * Upon arrival of a request, looking at the key in the request, this throttler first decides whether to throttle the request or not.
 * If that decision is different to what it was for the previous request (with the same key),
 * then this processor emits this request as an event; hence the name emitOnStateChange.
 * <p/>
 * If this request is the first request from a certain key, then that requested will be emitted out.
 * <p/>
 * This is useful when the throttler needs to alert only when the throttling decision is changed, in contrast to alerting about every decision taken.
 * <p/>
 * Usage:
 * throttler:emitOnStateChange(key, isThrottled)
 * <p/>
 * Parameters:
 * key: The key coming in the request, based on which throttling decision was made.
 * isThrottled: The throttling decision made.
 * <p/>
 * Example on usage:
 * from DecisionStream#throttler:emitOnStateChange(key, isThrottled)
 * select *
 * insert into AlertStream;
 */
public class EmitOnStateChange extends StreamProcessor {
    private VariableExpressionExecutor keyExpressionExecutor;
    private VariableExpressionExecutor isThrottledExpressionExecutor;
    private Map<String, Boolean> throttleStateMap = new HashMap<String, Boolean>();

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        while (streamEventChunk.hasNext()) {
            StreamEvent event = streamEventChunk.next();
            Boolean currentThrottleState = (Boolean) isThrottledExpressionExecutor.execute(event);
            String key = (String) keyExpressionExecutor.execute(event);
            Boolean lastThrottleState = throttleStateMap.get(key);
            if (lastThrottleState == currentThrottleState && !currentThrottleState) {
                streamEventChunk.remove();
            } else {
                throttleStateMap.put(key, currentThrottleState);
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to throttler:emitOnStateChange" +
                                                       "(key,isThrottled), required 2, but found "
                                                       + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the argument of " +
                                                       "throttler:emitOnStateChange(key,isThrottled), " +
                                                       "required " + Attribute.Type.STRING + ", " +
                                                       "but found " + attributeExpressionExecutors[0].getReturnType());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.BOOL) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the argument of " +
                                                       "throttler:emitOnStateChange(key,isThrottled), " +
                                                       "required " + Attribute.Type.BOOL + ", but found " +
                                                       attributeExpressionExecutors[1].getReturnType());
        }
        keyExpressionExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[0];
        isThrottledExpressionExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[1];
        return new ArrayList<Attribute>();    //this does not introduce any additional output attributes, hence returning an empty list.
    }

    @Override
    public void start() {
        //Nothing to do.
    }

    @Override
    public void stop() {
        //Nothing to do.
    }

    @Override
    public Object[] currentState() {
        return new Object[]{throttleStateMap};
    }

    @Override
    public void restoreState(Object[] state) {
        throttleStateMap = (HashMap<String, Boolean>) state[0];
    }
}
