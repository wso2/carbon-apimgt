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

import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.testkit.channel.RequestResponseChannel;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

public class VFSRequestResponseFileChannel extends VFSFileChannel implements RequestResponseChannel {
    private final String replyPath;
    private @Transient File replyFile;
    
    public VFSRequestResponseFileChannel(String path, String replyPath) {
        super(path);
        this.replyPath = replyPath;
    }

    public File getReplyFile() {
        return replyFile;
    }

    @Override
    public void setupService(AxisService service, boolean isClientSide) throws Exception {
        super.setupService(service, isClientSide);
        service.addParameter("transport.vfs.ReplyFileURI", "vfs:" + replyFile.toURL());
    }
    
    @Setup @SuppressWarnings("unused")
    private void setUp(VFSTestEnvironment env) throws Exception {
        replyFile = preparePath(env, replyPath);
    }
}
