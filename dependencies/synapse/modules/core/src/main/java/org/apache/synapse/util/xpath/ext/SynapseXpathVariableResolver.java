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
 * <p>This XPath Variable Resolver Interface must be implemented when resolving custom XPath
 *  Variable contexts
 * Any xpath function that can't be resolved by <code>SynapseXPathVariableContext</code> will be
 * delegated to this interface.
 * Users should implement this API to resolve custom variable contexts
 * ie:- expression="$Custom_Property_Scope:C_PROPERTY" OR expression="$CUSTOM_RESP/urn:child" , ...
 *
 * Extensions can be registered in synapse.properties under  synapse.xpath.var.extensions
 * @see org.apache.synapse.util.xpath.SynapseXPathVariableContext
 */
public interface SynapseXpathVariableResolver {

    /**
     * This method should implement the resolving code for custom xpath variable for the registered
     * QName given by #getResolvingQName().
     * @param msgCtxt Synapse Message Context
     * @return resolved object for custom xpath variable
     */
    public Object resolve(MessageContext msgCtxt);

    /**
     * Should Implement this API to return supported custom expression
     * @return This should return the supported QName
     * (localname + prefix + namespace URI combination ) for this extension
     */
    public QName getResolvingQName();

}
