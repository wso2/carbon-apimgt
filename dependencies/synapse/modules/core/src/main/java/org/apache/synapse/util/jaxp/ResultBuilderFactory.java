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

import org.apache.synapse.core.SynapseEnvironment;

/**
 * Factory for {@link ResultBuilder} instances.
 */
public interface ResultBuilderFactory {
    /**
     * Enumeration defining the output type to be expected by {@link ResultBuilder}.
     * This influences the exact behavior of
     * {@link ResultBuilder#getNode(java.nio.charset.Charset)}.
     */
    public enum Output {
        /**
         * The expected output is a simple {@link org.apache.axiom.om.OMElement}.
         * In this case, the return value of
         * {@link ResultBuilder#getNode(java.nio.charset.Charset)} will be
         * the root element of the AXIOM tree representing the XML infoset written
         * to the {@link javax.xml.transform.Result} object.
         */
        ELEMENT,
        
        /**
         * The expected output is a SOAP envelope.
         * If this output type is used, an invocation of {@link ResultBuilder#getNode()}
         * must return a {@link org.apache.axiom.soap.SOAPEnvelope} object.
         */
        SOAP_ENVELOPE,
        
        /**
         * The expected output is text. If this output type is used,
         * {@link ResultBuilder#getNode(java.nio.charset.Charset)} must return a
         * text wrapper with the output written to the {@link javax.xml.transform.Result}
         * object.
         */
        TEXT
    };
    
    /**
     * Create a new {@link ResultBuilder} instance.
     * 
     * @param synEnv the Synapse environment
     * @param expectedOutput specifies the expected type of output that will be written
     *           to {@link javax.xml.transform.Result} objects produced by {@link ResultBuilder}
     *           instances returned by this method
     * @return the newly created instance
     */
    ResultBuilder createResultBuilder(SynapseEnvironment synEnv, Output expectedOutput);
}
