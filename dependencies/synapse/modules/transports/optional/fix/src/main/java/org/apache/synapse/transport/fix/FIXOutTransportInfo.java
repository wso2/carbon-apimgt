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

package org.apache.synapse.transport.fix;

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.SessionID;

import java.util.Hashtable;

/**
 * FIXOutTransportInfo is a holder for FIX SessionID to send an outgoing message
 * to a FIX engine.
 */
public class FIXOutTransportInfo implements OutTransportInfo {

    private SessionID sessionID;
    private Log log;

    public FIXOutTransportInfo(SessionID sessionID) {
        this.sessionID = sessionID;
        this.log = LogFactory.getLog(this.getClass());
    }

    /**
     * Constructs a FIXOutTransportInfo out of a FIX EPR. Reads the parameters in the given
     * EPR and creates a SessionID out of them
     *
     * @param targetEPR a valid FIX EPR
     */
    public FIXOutTransportInfo(String targetEPR) {
        if (!targetEPR.startsWith(FIXConstants.FIX_PREFIX)) {
            handleException("Invalid FIX EPR " + targetEPR + ". The EPR prefix must be " +
                    FIXConstants.FIX_PREFIX);
        }
        else {
            Hashtable<String,String> properties = BaseUtils.getEPRProperties(targetEPR);
            this.sessionID = new SessionID(
                    properties.get(FIXConstants.BEGIN_STRING),
                    properties.get(FIXConstants.SENDER_COMP_ID),
                    properties.get(FIXConstants.SENDER_SUB_ID),
                    properties.get(FIXConstants.SENDER_LOCATION_ID),
                    properties.get(FIXConstants.TARGET_COMP_ID),
                    properties.get(FIXConstants.TARGET_SUB_ID),
                    properties.get(FIXConstants.TARGET_LOCATION_ID),
                    properties.get(FIXConstants.SESSION_QUALIFIER));
            this.log = LogFactory.getLog(this.getClass());
        }
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public void setContentType(String s) {

    }

    private void handleException(String s) {
        log.error(s);
        throw new AxisFIXException(s);
    }
}