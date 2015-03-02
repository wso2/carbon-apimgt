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

import javax.xml.transform.Source;

import org.apache.axiom.om.OMElement;

/**
 * Interface encapsulating a strategy to transform an AXIOM tree into a
 * {@link Source} object.
 * Implementations of this interface should be used in the following way:
 * <ol>
 *   <li>Create a new instance using the appropriate {@link SourceBuilderFactory}.</li>
 *   <li>Call {@link #getSource(OMElement)} to get a {@link Source} object for
 *       the AXIOM tree.</li>
 *   <li>Use the {@link Source} object in the invocation the XSL transformer or
 *       schema validator, etc.</li>
 *   <li>Call {@link #release()}.</li>
 * </ol>
 * Note that implementations of this interface may be stateful. Therefore a single
 * instance must never be used concurrently.
 */
public interface SourceBuilder {
    /**
     * Get a {@link Source} implementation for the given AXIOM tree.
     * 
     * @param node the root node of the AXIOM tree
     * @return the source object
     */
    Source getSource(OMElement node);
    
    /**
     * Release any resources associated with this object.
     */
    void release();
}
