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

import java.io.File;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClient;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

public class VFSRequestResponseClient extends VFSClient implements RequestResponseTestClient<byte[],byte[]> {
    private @Transient File replyFile;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(VFSRequestResponseFileChannel channel) {
        replyFile = channel.getReplyFile();
    }
    
    public IncomingMessage<byte[]> sendMessage(ClientOptions options, ContentType contentType, byte[] message) throws Exception {
        send(message);
        File requestFile = getRequestFile();
        if (VFSTestUtils.waitForFileDeletion(requestFile, 5000)) {
            return new IncomingMessage<byte[]>(contentType, VFSTestUtils.readFile(replyFile));
        } else {
            requestFile.delete();
            return null;
        }
    }
}
