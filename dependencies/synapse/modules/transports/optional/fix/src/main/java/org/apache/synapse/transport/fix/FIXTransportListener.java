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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.base.AbstractTransportListener;

/**
 * The FIX transport listener implementation. A FIX Transport Listner will hold
 * a FIX session factory, which would be created at initialization
 * time. This implementation supports the creation of FIX sessions at runtime
 * as and when required. This FIX Listener registers with Axis to be notified
 * of service deployment/undeployment/start/stop, and enables or disables
 * listening for FIX messages on the destinations as appropriate.
 * <p/>
 * Service must state where the FIX configuration file is located so
 * that  the required FIX sessions can be initialized for the service.
 * FIX configuration file should be a valid Quickfix/J session
 * configuration file. A URL to the file should be provided.
 * <p/>
 * <parameter name="transport.fix.AcceptorConfigURL">
 * http://www.mycompany.org/fixconfig/file.cfg</parameter>
 */
public class FIXTransportListener extends AbstractTransportListener {

    /**
     * The FIXSessionFactory takes care of creating and managing all the
     * FIX sessions.
     */
    private FIXSessionFactory fixSessionFactory;

    /**
     * This is the TransportListener initialization method invoked by Axis2
     *
     * @param cfgCtx    the Axis configuration context
     * @param trpInDesc the TransportIn description
     */
    public void init(ConfigurationContext cfgCtx,
                     TransportInDescription trpInDesc) throws AxisFault {

        super.init(cfgCtx, trpInDesc);
        fixSessionFactory = FIXSessionFactory.getInstance(new FIXApplicationFactory(cfgCtx));
        fixSessionFactory.setListenerThreadPool(this.workerPool);
        log.info("FIX transport listener initialized...");
    }

    /**
     * Prepare to listen for FIX messages on behalf of the given service
     * by first creating and starting a FIX session
     *
     * @param service the service for which to listen for messages
     */
    public void startListeningForService(AxisService service) {
        try {
            boolean acceptorCreated = fixSessionFactory.createFIXAcceptor(service);
            boolean initiatorCreated = fixSessionFactory.createFIXInitiator(service);

            if (!acceptorCreated && !initiatorCreated) {
                log.warn("No acceptor or initiator has been configured for the " +
                        "service " + service.getName() + " - Disabling the FIX transport for " +
                        "this service");
                disableTransportForService(service);
            }
        } catch (AxisFault axisFault) {
            disableTransportForService(service);
        }
    }

    /**
     * Stops listening for messages for the service thats undeployed or stopped
     * by stopping and disposing the appropriate FIX session
     *
     * @param service the service that was undeployed or stopped
     */
    protected void stopListeningForService(AxisService service) {
        fixSessionFactory.disposeFIXAcceptor(service);
    }

    /**
     * Returns EPRs for the given service and IP over the FIX transport
     *
     * @param serviceName service name
     * @param ip          ignored
     * @return the EPR for the service
     * @throws AxisFault
     */
    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {

        if (serviceName.indexOf('.') != -1) {
            serviceName = serviceName.substring(0, serviceName.indexOf('.'));
        }
        
        //Try to get the list of EPRs from the FIXSessionFactory
        String[] serviceEPRStrings = fixSessionFactory.getServiceEPRs(serviceName, ip);
        if (serviceEPRStrings != null) {
            EndpointReference[] serviceEPRs = new EndpointReference[serviceEPRStrings.length];
            for (int i = 0; i < serviceEPRStrings.length; i++) {
                serviceEPRs[i] = new EndpointReference(serviceEPRStrings[i]);
            }
            return serviceEPRs;
        }
        throw new AxisFault("Unable to get EPRs for the service " + serviceName);
    }
}