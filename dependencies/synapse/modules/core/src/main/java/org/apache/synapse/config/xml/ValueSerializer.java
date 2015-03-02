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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.mediators.Value;

/**
 * Serializer for {@link org.apache.synapse.mediators.Value} instances.
 */
public class ValueSerializer {
    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace nullNS
            = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    /**
     * Serialize the Value object to an OMElement representing the entry
     *
     * @param key  Value to serialize
     * @param elem OMElement
     * @return OMElement
     */
    public OMElement serializeValue(Value key, String name, OMElement elem) {
        if (key != null) {
            if (key.getExpression() == null) {
                //static key
                elem.addAttribute(fac.createOMAttribute(name, nullNS, key.getKeyValue()));
            } else {
                String startChar = "{" , endChar = "}";
                //if this is an expr type key we add an additional opening and closing brace
                if(key.hasExprTypeKey()){
                    startChar = startChar + "{";
                    endChar = endChar + "}";
                }
                //dynamic key
                SynapseXPathSerializer.serializeXPath(key.getExpression(), startChar +
                        key.getExpression().toString() + endChar, elem, name);
            }
        }
        return elem;
    }
    
    /**
     * Serialize the Value object to an OMElement representing the entry
     *
     * @param key  Value to serialize
     * @param elem OMElement
     * @return OMElement
     */
	public OMElement serializeTextValue(Value key, String name, OMElement elem) {
		if (key != null) {
			if (key.getExpression() == null) {
				// static key
				elem.setText(key.getKeyValue());
			} else {
				String startChar = "{", endChar = "}";
				// if this is an expr type key we add an additional opening and
				// closing brace
				if (key.hasExprTypeKey()) {
					startChar = startChar + "{";
					endChar = endChar + "}";
				}
				// dynamic key
				SynapseXPathSerializer.serializeTextXPath(key.getExpression(), startChar
						+ key.getExpression().toString() + endChar, elem, name);
			}
		}
		return elem;
	}

}
