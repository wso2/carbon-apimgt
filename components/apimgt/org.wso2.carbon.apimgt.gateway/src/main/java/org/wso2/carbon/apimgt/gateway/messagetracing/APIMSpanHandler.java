/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.messageTrace;

import org.apache.log4j.Logger;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SequenceType;
import org.apache.synapse.aspects.flow.statistics.data.raw.BasicStatisticDataUnit;
import org.apache.synapse.aspects.flow.statistics.data.raw.StatisticDataUnit;
import org.apache.synapse.aspects.flow.statistics.opentracing.management.handling.span.OpenTracingSpanHandler;
import org.apache.synapse.aspects.flow.statistics.opentracing.management.scoping.TracingScopeManager;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class APIMSpanHandler implements OpenTracingSpanHandler {

    Logger LOG = Logger.getLogger(APIMSpanHandler.class);

    /**
     * Manages tracing scopes.
     * Useful during cases like when an API is called within Proxy service.
     */

    private TracingScopeManager tracingScopeManager;

    public APIMSpanHandler(TracingScopeManager tracingScopeManager) {
        this.tracingScopeManager = tracingScopeManager;
    }

    @Override
    public void handleAddCallback(MessageContext messageContext, String callbackId) {

    }

    @Override
    public void handleCallbackCompletionEvent(MessageContext oldMessageContext, String callbackId) {

    }

    @Override
    public void handleUpdateParentsForCallback(MessageContext oldMessageContext, String callbackId) {

    }

    @Override
    public void handleReportCallbackHandlingCompletion(MessageContext synapseOutMsgCtx, String callbackId) {

    }

    @Override
    public void handleCloseEntryEvent(BasicStatisticDataUnit basicStatisticDataUnit, MessageContext synCtx) {

    }

    @Override
    public void handleCloseFlowForcefully(BasicStatisticDataUnit basicStatisticDataUnit, MessageContext synCtx) {

    }

    @Override
    public void handleTryEndFlow(BasicStatisticDataUnit basicStatisticDataUnit, MessageContext synCtx) {

    }

    @Override
    public void handleStateStackInsertion(MessageContext synCtx, String seqName, SequenceType seqType) {

    }

    @Override
    public void handleStateStackRemoval(ContinuationState continuationState, MessageContext synCtx) {

    }

    @Override
    public void handleStateStackClearance(MessageContext synCtx) {

    }

    @Override
    public void handleOpenEntryEvent(StatisticDataUnit statisticDataUnit, MessageContext synCtx) {
        logEvent(statisticDataUnit, (Axis2MessageContext) synCtx);
        LOG.info("OPEN EVENT COLLECTED");
    }

    @Override
    public void handleOpenChildEntryEvent(StatisticDataUnit statisticDataUnit, MessageContext synCtx) {
        logEvent(statisticDataUnit, (Axis2MessageContext) synCtx);
        LOG.info("OPEN CHILD EVENT COLLECTED");
    }

    private void logEvent(StatisticDataUnit statisticDataUnit, Axis2MessageContext synCtx) {
        String componentId=statisticDataUnit.getComponentId();
        String componenetName=statisticDataUnit.getComponentName();

        System.out.print( "\n" );
        Map<String, Object> transportHeaders = statisticDataUnit.getTransportPropertyMap();
        LOG.info("TRANSPORT HEADERS :: " + transportHeaders);
        System.out.print( "\n" );
        Map<String, Object> synMsg = statisticDataUnit.getContextPropertyMap();
        if (synMsg != null){
            synMsg.remove("OPEN_API_OBJECT");
            synMsg.remove("OPEN_API_STRING");
        }
        LOG.info("OPEN_API_OBJECT = class OpenAPI {\"openapi\" : \"3.0.1\",...}");
        System.out.print( "\n" );
        LOG.info("SYNAPSE MESSAGE CONTEXT :: " + synMsg);
        System.out.print( "\n" );
        Map<String, Object> propertyMap = new TreeMap<>();
        Iterator<String> propertyIterator = synCtx.getAxis2MessageContext().getPropertyNames();
        while (propertyIterator.hasNext()) {
            String propName = propertyIterator.next();
            propertyMap.put(propName, synCtx.getAxis2MessageContext().getProperty(propName));
        }
        LOG.info("AXIS2 MESSAGE CONTEXT :: " + propertyMap);
        System.out.print( "\n" );
        LOG.info("PAYLOAD :: " + statisticDataUnit.getPayload());
        System.out.print( "\n" );
    }

    @Override
    public void handleOpenFlowContinuableEvent(StatisticDataUnit statisticDataUnit, MessageContext synCtx) {

    }

    @Override
    public void handleOpenFlowSplittingEvent(StatisticDataUnit statisticDataUnit, MessageContext synCtx) {

    }

    @Override
    public void handleOpenFlowAggregateEvent(StatisticDataUnit statisticDataUnit, MessageContext synCtx) {

    }

    @Override
    public void handleOpenFlowAsynchronousEvent(BasicStatisticDataUnit statisticDataUnit, MessageContext synCtx) {

    }

    @Override
    public void handleOpenContinuationEvents(BasicStatisticDataUnit statisticDataUnit, MessageContext synCtx) {

    }
}
