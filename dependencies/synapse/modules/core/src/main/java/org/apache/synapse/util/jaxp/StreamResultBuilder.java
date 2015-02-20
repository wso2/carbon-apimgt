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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.util.TextFileDataSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * {@link ResultBuilder} implementation that produces a {@link StreamResult} backed by a
 * {@link org.apache.axiom.util.blob.OverflowBlob} object.
 */
public class StreamResultBuilder implements ResultBuilder {
    private static final Log log = LogFactory.getLog(StreamResultBuilder.class);
    
    private final SynapseEnvironment synEnv;
    private final ResultBuilderFactory.Output expectedOutput;
    private OverflowBlob tmp;
    private OutputStream out;
    
    public StreamResultBuilder(SynapseEnvironment synEnv,
            ResultBuilderFactory.Output expectedOutput) {
        this.synEnv = synEnv;
        this.expectedOutput = expectedOutput;
    }

    public Result getResult() {
        tmp = synEnv.createOverflowBlob();
        out = tmp.getOutputStream();
        return new StreamResult(out);
    }

    public OMElement getNode(Charset charset) {
        try {
            out.close();
        } catch (IOException e) {
            handleException("Error while closing output stream", e);
        }
        if (expectedOutput == ResultBuilderFactory.Output.TEXT) {
            return TextFileDataSource.createOMSourcedElement(tmp, charset);
        } else {
            XMLStreamReader reader;
            try {
                reader = StAXUtils.createXMLStreamReader(tmp.getInputStream());
            } catch (XMLStreamException e) {
                handleException("Unable to parse the XML output", e);
                return null;
            } catch (IOException e) {
                handleException("I/O error while reading temporary file", e);
                return null;
            }
            if (expectedOutput == ResultBuilderFactory.Output.SOAP_ENVELOPE) {
                return new StAXSOAPModelBuilder(reader).getSOAPEnvelope();
            } else {
                return new StAXOMBuilder(reader).getDocumentElement();
            }
        }
    }

    public void release() {
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
