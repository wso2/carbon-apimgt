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
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThrottleStreamProcessor extends StreamProcessor implements SchedulingProcessor, FindableProcessor {

    private long timeInMilliSeconds;
    private ComplexEventChunk<StreamEvent> expiredEventChunk = new ComplexEventChunk<StreamEvent>(true);
    private Scheduler scheduler;
    private ExecutionPlanContext executionPlanContext;
    private long expireEventTime = -1;
    private long startTime = -1;

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;

        if (attributeExpressionExecutors.length == 1) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                    timeInMilliSeconds = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();

                } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                    timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
                } else {
                    throw new ExecutionPlanValidationException("Throttle batch window's 1st parameter attribute should be " +
                                                               "either int or long, but found "
                                                               + attributeExpressionExecutors[0].getReturnType());
                }
            } else {
                throw new ExecutionPlanValidationException("Throttle batch window 1st parameter needs to be constant " +
                                                           "parameter attribute but found a dynamic attribute "
                                                           + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }
        } else if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                    timeInMilliSeconds = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();

                } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                    timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
                } else {
                    throw new ExecutionPlanValidationException("Throttle batch window's 1st parameter attribute should be " +
                                                               "either int or long, but found "
                                                               + attributeExpressionExecutors[0].getReturnType());
                }
            } else {
                throw new ExecutionPlanValidationException("Throttle batch window 1st parameter needs to be constant " +
                                                           "attribute but found a dynamic attribute "
                                                           + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }

            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                startTime = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor)
                                                                     attributeExpressionExecutors[1]).getValue()));
            } else if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.LONG) {
                startTime = Long.parseLong(String.valueOf(((ConstantExpressionExecutor)
                                                                   attributeExpressionExecutors[1]).getValue()));
            } else {
                throw new ExecutionPlanValidationException("Throttle batch window 2nd parameter needs to be a Long " +
                                                           "or Int type but found a " + attributeExpressionExecutors[2].getReturnType());
            }
        } else {
            throw new ExecutionPlanValidationException("Throttle batch window should only have one/two parameter " +
                                                       "(<int|long|time> windowTime (and <int|long> startTime), but found "
                                                       + attributeExpressionExecutors.length + " input attributes");
        }

        List<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(new Attribute("expiryTimeStamp", Attribute.Type.LONG));
        return attributeList;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        synchronized (this) {
            if (expireEventTime == -1) {
                long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
                if (startTime != -1) {
                    expireEventTime = addTimeShift(currentTime);
                } else {
                    expireEventTime = executionPlanContext.getTimestampGenerator().currentTime() + timeInMilliSeconds;
                }
                scheduler.notifyAt(expireEventTime);
            }
            long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
            boolean sendEvents;
            if (currentTime >= expireEventTime) {
                expireEventTime += timeInMilliSeconds;
                scheduler.notifyAt(expireEventTime);
                sendEvents = true;
            } else {
                sendEvents = false;
            }

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                if (streamEvent.getType() != ComplexEvent.Type.CURRENT) {
                    continue;
                }

                complexEventPopulater.populateComplexEvent(streamEvent, new Object[]{expireEventTime});
                StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
                clonedStreamEvent.setType(StreamEvent.Type.EXPIRED);
                clonedStreamEvent.setTimestamp(expireEventTime);
                expiredEventChunk.add(clonedStreamEvent);
            }
            if (sendEvents) {
                expiredEventChunk.reset();
                if (expiredEventChunk.getFirst() != null) {
                    streamEventChunk.add(expiredEventChunk.getFirst());
                }
                expiredEventChunk.clear();
            }
        }
        if (streamEventChunk.getFirst() != null) {
            streamEventChunk.setBatch(true);
            nextProcessor.process(streamEventChunk);
            streamEventChunk.setBatch(false);
        }
    }

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }

    @Override
    public Object[] currentState() {
        return new Object[]{expiredEventChunk.getFirst()};
    }

    @Override
    public void restoreState(Object[] state) {
        expiredEventChunk.clear();
        expiredEventChunk.add((StreamEvent) state[0]);
    }

    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) {
        return OperatorParser.constructOperator(expiredEventChunk, expression, matchingMetaStateHolder, executionPlanContext, variableExpressionExecutors, eventTableMap);
    }

    private long addTimeShift(long currentTime) {
        long timePassedUntilNow = (currentTime - startTime) % timeInMilliSeconds;
        return currentTime + (timeInMilliSeconds - timePassedUntilNow);
    }

}