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

package org.apache.synapse.transport.vfs;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;

public class VFSTransportDescriptionFactory implements TransportDescriptionFactory {
    public TransportInDescription createTransportInDescription() throws Exception {
        TransportInDescription trpInDesc = new TransportInDescription("vfs");
        trpInDesc.setReceiver(new VFSTransportListener());
        trpInDesc.addParameter(new Parameter(VFSConstants.TRANSPORT_FILE_LOCKING,
                VFSConstants.TRANSPORT_FILE_LOCKING_ENABLED));
        return trpInDesc;
    }

    public TransportOutDescription createTransportOutDescription() throws Exception {
        TransportOutDescription trpOutDesc = new TransportOutDescription("vfs");
        trpOutDesc.setSender(new VFSTransportSender());
        trpOutDesc.addParameter(new Parameter(VFSConstants.TRANSPORT_FILE_LOCKING,
                VFSConstants.TRANSPORT_FILE_LOCKING_ENABLED));
        return trpOutDesc;
    }

}
