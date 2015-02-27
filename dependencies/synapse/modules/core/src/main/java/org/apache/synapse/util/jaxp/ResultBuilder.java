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

import java.nio.charset.Charset;

import javax.xml.transform.Result;

import org.apache.axiom.om.OMElement;

/**
 * Interface encapsulating a strategy to transform an XML infoset written to
 * a {@link Result} object into an AXIOM tree.
 * Implementations of this interface should be used in the following way:
 * <ol>
 *   <li>Create a new instance using the appropriate {@link ResultBuilderFactory}.</li>
 *   <li>Call {@link #getResult()} to get an {@link Result} instance.</li>
 *   <li>Use the {@link Result} object in the invocation the XSL transformer.</li>
 *   <li>Call {@link #getNode()} to retrieve the root element of the AXIOM tree.</li>
 *   <li>Call {@link #release()} when the AXIOM tree is no longer needed or when it
 *       has been fully built.</li>
 * </ol>
 * Note that implementations of this interface are always stateful. Therefore a single
 * instance must never be used concurrently.
 */
public interface ResultBuilder {
    /**
     * Get a {@link Result} implementation that can be used with an XSL transformer.
     * 
     * @return the result object
     */
    Result getResult();
    
    /**
     * Get the result written to the {@link Result} as an {@link OMElement}.
     * Note that the exact behavior of this methos depends on the specified
     * {@link ResultBuilderFactory.Output}.
     * 
     * @param charset The charset encoding of the data that has been written
     *                to the {@link Result} object. This information should only
     *                be used in conjunction with {@link ResultBuilderFactory.Output#TEXT}.
     * @return the root element of the AXIOM tree
     */
    OMElement getNode(Charset charset);
    
    /**
     * Release any resources associated with this object.
     */
    void release();
}
