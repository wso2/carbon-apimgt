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

package org.apache.synapse.util;

import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.synapse.core.SynapseEnvironment;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource which will be used to pass the Hessian messages in to SOAP body within axis2/synapse
 *
 * @see javax.activation.DataSource
 */
public class SynapseBinaryDataSource implements DataSource {

    /** Content type of the DataSource */
    private String contentType;

    /** Hessian message is kept inside the DataSource as a byte array */
    private OverflowBlob data;

    /**
     * Constructs the HessianDataSource from the given InputStream. Inside the HessianDataSource,
     * data is stored in a byte[] or in a temp file format inorder to be able to get the stream any
     * number of time, otherwise the stream can only be read once
     *
     * @param inputstream contains the Hessian message for later retrieval
     * @param contentType message content type
     * @throws IOException failure in reading from the InputStream
     */
    public SynapseBinaryDataSource(InputStream inputstream, String contentType) throws IOException {

        this.contentType = contentType;
        this.data = new OverflowBlob(4, 1024, "tmp_", ".dat");

        data.readFrom(inputstream, -1);
        inputstream.close();
    }

    public SynapseBinaryDataSource(InputStream inputstream, String contentType,
        SynapseEnvironment synEnv) throws IOException {

        this.contentType = contentType;
        this.data = synEnv.createOverflowBlob();

        data.readFrom(inputstream, -1);
        inputstream.close();
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() throws IOException {
        return data.getInputStream();
    }

    public String getName() {
        return this.getClass().getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return data.getOutputStream();
    }

}
