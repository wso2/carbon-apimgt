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

package org.apache.synapse.core.axis2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.util.Map;

/**
 * 
 */
public class CustomURIBasedDispatcher extends AbstractDispatcher {

    private static final Log log = LogFactory.getLog(CustomURIBasedDispatcher.class);
    public static final String NAME = "CustomURIBasedDispatcher";
    
    public AxisOperation findOperation(AxisService axisService, MessageContext messageContext)
            throws AxisFault {
        // no need to do any processing, since this is not for operation dispatching
        return null;
    }

    public AxisService findService(MessageContext messageContext) throws AxisFault {

        EndpointReference toEPR = messageContext.getTo();
        if (toEPR != null) {
            String toAddress = toEPR.getAddress();
            int index = toAddress.indexOf("//");
            if (index != -1) {
                toAddress = toAddress.substring(index + 2);
                index = toAddress.indexOf("/");
                toAddress = toAddress.substring(index + 1);
            }
            if (toAddress.startsWith("/")) {
                toAddress = toAddress.substring(1);
            }
            ConfigurationContext cfgCtx = messageContext.getConfigurationContext();
            Object property = cfgCtx.getProperty(NhttpConstants.EPR_TO_SERVICE_NAME_MAP);
            if (property != null && property instanceof Map) {
                Map eprToserviceNameMap = (Map) property;
                if (eprToserviceNameMap.containsKey(toAddress)) {
                    return cfgCtx.getAxisConfiguration().getService(
                            eprToserviceNameMap.get(toAddress).toString());
                }
            }
        } else {
            log.debug("Unable to dispatch using the custom URI the " +
                    "To header has not been specified");
        }
        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
