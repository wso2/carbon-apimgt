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

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

@Name("mock")
public class VFSMockAsyncEndpoint implements AsyncEndpoint<byte[]> {
    private @Transient VFSFileChannel channel;
    private @Transient ContentType contentType;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(VFSFileChannel channel, ClientOptions options) throws Exception {
        this.channel = channel;
        contentType = options.getTransportContentType();
    }
    
    public void clear() throws Exception {
        channel.getRequestFile().delete();
    }

    public IncomingMessage<byte[]> waitForMessage(int timeout) throws Throwable {
        byte[] data = VFSTestUtils.waitForFile(channel.getRequestFile(), timeout);
        return data == null ? null : new IncomingMessage<byte[]>(contentType, data);
    }
}
