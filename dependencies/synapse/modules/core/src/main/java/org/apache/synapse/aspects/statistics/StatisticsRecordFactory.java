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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;

/**
 * Factory for creating  <code>StatisticsRecord</code> instances
 */
public class StatisticsRecordFactory {

    private static final Log log = LogFactory.getLog(StatisticsRecordFactory.class);

    /**
     * Factory method to create <code>StatisticsRecord</code> instances
     *
     * @param synCtx Current Message through synapse
     * @return StatisticsRecord instance
     */
    public static StatisticsRecord getStatisticsRecord(MessageContext synCtx) {

        String messageId = synCtx.getMessageID();
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();
        String remoteIP = (String) axisMC.getPropertyNonReplicable(
                org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        String domainName = (String) axisMC.getPropertyNonReplicable(NhttpConstants.REMOTE_HOST);
        StatisticsRecord statisticsRecord = new StatisticsRecord(messageId, remoteIP, domainName);
        if (log.isDebugEnabled()) {
            log.debug("Created a StatisticsRecord :  " + statisticsRecord);
        }
        return statisticsRecord;
    }
}
