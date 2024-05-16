/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.AbstractMap;
import java.util.Map;

/**
 * This is a custom extension, written to add reset functionality to existing sum function.
 * Upon arrival of a reset request, if the second argument of the sum function is true,
 * the counter related to the specific throttle key will be reset to zero.
 * <p/>
 * Usage:
 * throttler:sum(messageSize, true)
 * <p/>
 * Example on usage:
 * FROM EligibilityStream[isEligible==true]#throttler:timeBatch(1 hour, 0)
 * select throttleKey,
 * (throttler:sum(cast(map:get(propertiesMap,'messageSize'),'long'),
 * cast(map:get(propertiesMap,'reset'),'bool')) >= 1024L) as isThrottled, expiryTimeStamp group by throttleKey
 * INSERT ALL EVENTS into ResultStream;
 */
public class SumAttributeAggregatorWithReset extends AttributeAggregator {
    private SumAttributeAggregatorWithReset sumOutputAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length > 2) {
            throw new OperationNotSupportedException("Sum aggregator has to have 1 or 2 parameters, currently "
                    + attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
        case FLOAT:
            sumOutputAttributeAggregator = new SumAttributeAggregatorFloat();
            break;
        case INT:
            sumOutputAttributeAggregator = new SumAttributeAggregatorInt();
            break;
        case LONG:
            sumOutputAttributeAggregator = new SumAttributeAggregatorLong();
            break;
        case DOUBLE:
            sumOutputAttributeAggregator = new SumAttributeAggregatorDouble();
            break;
        default:
            throw new OperationNotSupportedException("Sum not supported for " + type);
        }

    }

    public Attribute.Type getReturnType() {
        return sumOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return sumOutputAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        //reset the counter to zero if the second parameter is true
        if (Boolean.TRUE.equals(data[1])){
            return sumOutputAttributeAggregator.reset();
        }
        return sumOutputAttributeAggregator.processAdd(data[0]);
    }

    @Override
    public Object processRemove(Object data) {
        return sumOutputAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        return sumOutputAttributeAggregator.processRemove(data[0]);
    }

    @Override
    public Object reset() {
        return sumOutputAttributeAggregator.reset();
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Object[] currentState() {
        return sumOutputAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        sumOutputAttributeAggregator.restoreState(state);
    }

    class SumAttributeAggregatorDouble extends SumAttributeAggregatorWithReset {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += (Double) data;
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= (Double) data;
            return value;
        }

        @Override
        public Object reset() {
            value = 0.0;
            return value;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", value)};
        }

        @Override
        public void restoreState(Object[] state) {
            Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
            value = (Double) stateEntry.getValue();
        }

    }

    class SumAttributeAggregatorFloat extends SumAttributeAggregatorWithReset {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += ((Float) data).doubleValue();
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= ((Float) data).doubleValue();
            return value;
        }

        public Object reset() {
            value = 0.0;
            return value;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", value)};
        }

        @Override
        public void restoreState(Object[] state) {
            Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
            value = (Double) stateEntry.getValue();
        }

    }

    class SumAttributeAggregatorInt extends SumAttributeAggregatorWithReset {

        private final Attribute.Type type = Attribute.Type.LONG;
        private long value = 0L;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += ((Integer) data).longValue();
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= ((Integer) data).longValue();
            return value;
        }

        public Object reset() {
            value = 0L;
            return value;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", value)};
        }

        @Override
        public void restoreState(Object[] state) {
            Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
            value = (Long) stateEntry.getValue();
        }

    }

    class SumAttributeAggregatorLong extends SumAttributeAggregatorWithReset {

        private final Attribute.Type type = Attribute.Type.LONG;
        private long value = 0L;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += (Long) data;
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= (Long) data;
            return value;
        }

        public Object reset() {
            value = 0L;
            return value;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { new AbstractMap.SimpleEntry<String, Object>("Value", value) };
        }

        @Override
        public void restoreState(Object[] state) {
            Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
            value = (Long) stateEntry.getValue();
        }

    }
}
