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

package org.apache.synapse.util.jaxp;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link SourceBuilder} implementation that serializes the AXIOM tree
 * to {@link org.apache.axiom.util.blob.OverflowBlob} object and produces a {@link StreamSource}.
 */
public class StreamSourceBuilder implements SourceBuilder {
    private static final Log log = LogFactory.getLog(StreamSourceBuilder.class);
    
    private final SynapseEnvironment synEnv;
    private OverflowBlob tmp;
    private InputStream in;
    
    public StreamSourceBuilder(SynapseEnvironment synEnv) {
        this.synEnv = synEnv;
    }

    public Source getSource(OMElement node) {
        tmp = synEnv.createOverflowBlob();
        OutputStream out = tmp.getOutputStream();
        try {
            node.serialize(out);
        } catch (XMLStreamException e) {
            release();
            handleException("Unable to serialize AXIOM tree", e);
        }
        try {
            out.close();
            in = tmp.getInputStream();
            return new StreamSource(in);
        } catch (IOException e) {
            release();
            handleException("Unable to read temporary file", e);
            return null;
        }
    }

    public void release() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // Ignore
            }
            in = null;
        }
        if (tmp != null) {
            tmp.release();
            tmp = null;
        }
    }

    private static void handleException(String message, Throwable ex) {
        log.error(message, ex);
        throw new SynapseException(message, ex);
    }
}
