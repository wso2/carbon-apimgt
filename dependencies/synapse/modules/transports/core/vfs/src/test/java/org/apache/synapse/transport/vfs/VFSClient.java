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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.mail.internet.ContentType;

import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.client.TestClient;
import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Name("java.io")
public class VFSClient implements TestClient {
    private static final Log log = LogFactory.getLog(VFSClient.class);
    
    private @Transient File requestFile;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(VFSFileChannel channel) {
        requestFile = channel.getRequestFile();
    }
    
    public File getRequestFile() {
        return requestFile;
    }

    public ContentType getContentType(ClientOptions options, ContentType contentType) {
        return contentType;
    }

    protected void send(byte[] message) throws Exception {
        // Create the file atomically (using move/rename) to avoid problems with the
        // listener starting to read the file too early.
        File tmpFile = new File(requestFile.getParent(), "." + requestFile.getName() + ".tmp");
        log.debug("Writing message to temporary file " + tmpFile);
        OutputStream out = new FileOutputStream(tmpFile);
        out.write(message);
        out.close();
        log.debug("Moving " + tmpFile + " to " + requestFile);
        if (!tmpFile.renameTo(requestFile)) {
            throw new IOException("Unable to rename " + tmpFile + " to " + requestFile);
        }
        log.debug("Done.");
    }
}