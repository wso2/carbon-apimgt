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

package org.apache.synapse.transport.nhttp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.testkit.axis2.client.AxisTestClientConfigurator;
import org.apache.axis2.transport.testkit.name.Key;

public class HttpAxisTestClientConfigurator implements AxisTestClientConfigurator {
    private final boolean forceHTTP10;
    
    public HttpAxisTestClientConfigurator(boolean forceHTTP10) {
        this.forceHTTP10 = forceHTTP10;
    }

    @Key("forceHTTP10")
    public boolean isForceHTTP10() {
        return forceHTTP10;
    }

    public void setupRequestMessageContext(MessageContext msgContext) throws AxisFault {
        msgContext.setProperty(NhttpConstants.FORCE_HTTP_1_0, forceHTTP10);
    }
}
