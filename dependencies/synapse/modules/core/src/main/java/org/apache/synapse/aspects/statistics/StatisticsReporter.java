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
package org.apache.synapse.aspects.statistics;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Identifiable;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.aspects.ComponentType;

/**
 * A utility to report statistics at various check points in the message flow
 * TODO - This class should be removed after a flow based statistics collection is done.
 */

public class StatisticsReporter {

    private static final Log log = LogFactory.getLog(StatisticsReporter.class);

    /**
     * Collects statistics for the given component.This is the starting point of
     * collecting stats for a particular component
     *
     * @param synCtx        the current message being passed through the synapse
     * @param configurable  a component that can be configured it's audit
     * @param componentType the type of the component which needs to collect statistics
     */
    public static void reportForComponent(MessageContext synCtx,
                                          StatisticsConfigurable configurable,
                                          ComponentType componentType) {

        if (!(configurable instanceof Identifiable)) {
            // provided configuration is not a Identifiable
            return;
        }

        if (!configurable.isStatisticsEnable()) {
            // statistics is disabled
            return;
        }

        StatisticsRecord record = null;
        if(synCtx.getProperty(SynapseConstants.STATISTICS_STACK) instanceof StatisticsRecord){
            record =  (StatisticsRecord) synCtx.getProperty(SynapseConstants.STATISTICS_STACK);
        }
        if (record == null) {

            if (log.isDebugEnabled()) {
                log.debug("Setting a statistics stack on the message context.");
            }
            record = StatisticsRecordFactory.getStatisticsRecord(synCtx);
            synCtx.setProperty(SynapseConstants.STATISTICS_STACK, record);
        }

        record.setOwner(componentType);
        record.collect(createStatisticsLog((Identifiable) configurable, componentType, synCtx));

    }

    /**
     * Collects statistics for any component when a response for a request is received.
     * Any component means that this statistics log (check point) is valid for sequence, endpoint,
     * and proxy.
     *
     * @param synCtx the current message being passed through the synapse
     */
    public static void reportForAllOnResponseReceived(MessageContext synCtx) {

        // remove the property that have been set when sending the request
        synCtx.setProperty(SynapseConstants.SENDING_REQUEST, false);

        //  if there is a statistics record
        StatisticsRecord statisticsRecord = null;
        if (synCtx.getProperty(SynapseConstants.STATISTICS_STACK) instanceof StatisticsRecord) {
            statisticsRecord = (StatisticsRecord) synCtx.getProperty(
                    SynapseConstants.STATISTICS_STACK);
        }

        if (statisticsRecord != null) {

            if (log.isDebugEnabled()) {
                log.debug("Reporting a statistics on a response is received : " +
                        statisticsRecord);
            }

            AspectConfiguration configuration = new AspectConfiguration(
                    SynapseConstants.SYNAPSE_ASPECTS);
            configuration.enableStatistics();
            statisticsRecord.collect(createStatisticsLog(configuration, ComponentType.ANY, synCtx));
        }
    }

    /**
     * Reporting a fault for any component when a response for a request is received.
     * Any component means that this statistics log (check point) is valid for sequence, endpoint,
     * and proxy.
     *
     * @param synCtx   synCtx  Current Message through synapse
     * @param errorLog the received error information
     */
    public static void reportFaultForAll(MessageContext synCtx, ErrorLog errorLog) {

        StatisticsRecord statisticsRecord = null;
        if (synCtx.getProperty(SynapseConstants.STATISTICS_STACK) instanceof StatisticsRecord) {
            statisticsRecord = (StatisticsRecord) synCtx.getProperty(
                    SynapseConstants.STATISTICS_STACK);
        }
        if (statisticsRecord != null) {

            if (log.isDebugEnabled()) {
                log.debug("Reporting a fault : " + statisticsRecord);
            }

            StatisticsLog statisticsLog = new StatisticsLog(SynapseConstants.SYNAPSE_ASPECTS,
                    ComponentType.ANY);
            statisticsLog.setResponse(synCtx.isResponse() || synCtx.isFaultResponse());
            statisticsLog.setFault(true);
            statisticsLog.setErrorLog(errorLog);
            statisticsRecord.collect(statisticsLog);
        }
    }

    /**
     * Reports statistics  for any component on the response message is sent
     * Any component means that this statistics log (check point) is valid for sequence, endpoint,
     * and proxy.
     *
     * @param synCtx MessageContext instance
     */
    public static void reportForAllOnResponseSent(MessageContext synCtx) {
        endReportForAll(synCtx);
    }

    /**
     * Ends statistics reporting for any component. Only at this point, the statistics record is put
     * into the <code>StatisticsCollector </code>
     * Any component means that this statistics log (check point) is valid for sequence, endpoint,
     * and proxy.
     *
     * @param synCtx MessageContext instance
     */
    private static void endReportForAll(MessageContext synCtx) {

        StatisticsRecord record = null;
        if(synCtx.getProperty(SynapseConstants.STATISTICS_STACK) instanceof StatisticsRecord){
            record =  (StatisticsRecord) synCtx.getProperty(SynapseConstants.STATISTICS_STACK);
        }
        if (record == null) {
            //There is no statistics record.
            return;
        }

        if (record.isEndReported()) {
            if (log.isDebugEnabled()) {
                log.debug("The statistics record has been already reported.");
            }
            return;
        }

        StatisticsLog statisticsLog = new StatisticsLog(SynapseConstants.SYNAPSE_ASPECTS,
                ComponentType.ANY);
        statisticsLog.setResponse(synCtx.isResponse() || synCtx.isFaultResponse());

        if (isFault(synCtx)) {
            statisticsLog.setFault(true);
            statisticsLog.setErrorLog(ErrorLogFactory.createErrorLog(synCtx));
        }

        statisticsLog.setEndAnyLog(true);
        record.collect(statisticsLog);
        record.setEndReported(true);

        StatisticsCollector collector = synCtx.getEnvironment().getStatisticsCollector();
        if (collector == null) {

            if (log.isDebugEnabled()) {
                log.debug("Setting statistics collector in the synapse environment.");
            }
            collector = new StatisticsCollector();
            synCtx.getEnvironment().setStatisticsCollector(collector);
        }

        synCtx.getPropertyKeySet().remove(SynapseConstants.STATISTICS_STACK);

        if (!collector.contains(record)) {
            collector.collect(record);
        }
    }

    /**
     * Ends statistics reporting for any component after the request processed.
     * Any component means that this statistics log (check point) is valid for sequence, endpoint,
     * and proxy.
     * Only if the message is out-only, the stats are reported
     *
     * @param synCtx MessageContext instance
     */
    public static void endReportForAllOnRequestProcessed(MessageContext synCtx) {

        StatisticsRecord statisticsRecord = null;
        if (synCtx.getProperty(SynapseConstants.STATISTICS_STACK) instanceof StatisticsRecord) {
            statisticsRecord = (StatisticsRecord) synCtx.getProperty(
                    SynapseConstants.STATISTICS_STACK);
        }
        if (statisticsRecord == null) {
            //There is no statistics record.
            return;
        }

        boolean isOutOnly = Boolean.parseBoolean(
                String.valueOf(synCtx.getProperty(SynapseConstants.OUT_ONLY)));
        if (!isOutOnly) {
            isOutOnly = (!Boolean.parseBoolean(
                    String.valueOf(synCtx.getProperty(SynapseConstants.SENDING_REQUEST)))
                    && !synCtx.isResponse());
        }

        if (isOutOnly) {
            endReportForAll(synCtx);
        }
    }

    /**
     * Reports statistics on the end of the out flow
     *
     * @param synCtx MessageContext instance
     */
    public static void reportForAllOnOutFlowEnd(MessageContext synCtx) {

        endReportForAll(synCtx);

    }

    /**
     * Factory method to create  <code>StatisticsLog</code> instances
     *
     * @param identifiable  component
     * @param componentType component type
     * @param synCtx        MessageContext instance
     * @return a StatisticsLog
     */
    private static StatisticsLog createStatisticsLog(Identifiable identifiable,
                                                     ComponentType componentType,
                                                     MessageContext synCtx) {
        if (isValid(identifiable)) {
            String auditID = identifiable.getId();
            StatisticsLog statisticsLog = new StatisticsLog(auditID, componentType);
            statisticsLog.setResponse(synCtx.isResponse() || synCtx.isFaultResponse());
            if (isFault(synCtx)) {
                statisticsLog.setFault(true);
                statisticsLog.setErrorLog(ErrorLogFactory.createErrorLog(synCtx));
            }
            if (log.isDebugEnabled()) {
                log.debug("Created statistics log : " + statisticsLog);
            }
            return statisticsLog;
        }
        return null;
    }

    /**
     * Checks the validity of a component
     *
     * @param identifiable component
     * @return <code>true</code> if the component is valid
     */
    private static boolean isValid(Identifiable identifiable) {

        if (identifiable == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid aspects configuration , It is null.");
            }
            return false;
        }

        String auditID = identifiable.getId();
        if (auditID == null || "".equals(auditID)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid aspects configuration , Audit name is null.");
            }
            return false;
        }
        return true;
    }

    /**
     * Detects a fault
     *
     * @param context MessageContext context
     * @return <code>true</code>  if this is a fault
     */
    private static boolean isFault(MessageContext context) {
        boolean isFault = context.isFaultResponse();
        if (!isFault) {
            isFault = context.getProperty(SynapseConstants.ERROR_CODE) != null;

            if (!isFault) {
                SOAPEnvelope envelope = context.getEnvelope();
                if (envelope != null) {
                    isFault = envelope.hasFault();
                }
            }
        }
        return isFault;
    }
}
