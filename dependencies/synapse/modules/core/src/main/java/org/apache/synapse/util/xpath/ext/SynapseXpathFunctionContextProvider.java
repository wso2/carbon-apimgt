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
package org.apache.synapse.util.xpath.ext;

import org.apache.synapse.MessageContext;
import org.jaxen.Function;

import javax.xml.namespace.QName;

/**
 * <p>This XPath Function Context provider Interface must be implemented when resolving custom
 * XPath Function contexts .Any xpath function that can't be resolved by
 * <code>SynapseXPathFunctionContext</code> will be delegated to this interface.
 * Users should implement this API as well as jaxen based <code>Function</code> API .
 *
 * Extensions can be registered in synapse.properties under  synapse.xpath.func.extensions
 * @see org.jaxen.Function
 * @see org.apache.synapse.util.xpath.SynapseXPathFunctionContext
 */
public interface SynapseXpathFunctionContextProvider {

    /**
     * This method should implement instatntiation code for custom xpath function for the registered
     * QNames given by #getResolvingQName().Note that this extension provider is responsible for
     * initalizing custom xpath function and returning a fresh function instance to Synapse. Callers
     * should be responsible for invoking the function explicitly.
     * @see org.jaxen.Function#call(org.jaxen.Context, java.util.List)
     *
     * @param msgCtxt Synapse Message Context
     *
     * @return extension Function constructed with message
     */
     public Function getInitializedExtFunction(MessageContext msgCtxt);

    /**
     * Should Implement this API to return supported custom expression
     * @return This should return the supported QName (localname + prefix + namespace URI combination ) for
     * this extension
     */
     public QName getResolvingQName();
}
