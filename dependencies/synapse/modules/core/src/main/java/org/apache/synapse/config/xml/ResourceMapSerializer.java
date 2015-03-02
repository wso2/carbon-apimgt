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

import java.util.Map;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.util.resolver.ResourceMap;

/**
 * Creates a sequence of <tt>&lt;resource&gt;</tt> elements from a ResourceMap object:
 * <pre>
 * &lt;resource location="..." key="..."/&gt;*
 * </pre>
 */
public class ResourceMapSerializer {
    private static final OMFactory fac = OMAbstractFactory.getOMFactory();
    
    public static void serializeResourceMap(OMElement parent, ResourceMap resourceMap) {
        if (resourceMap != null) {
        	for (Map.Entry<String,String> entry : resourceMap.getResources().entrySet()) {
                OMElement resource = fac.createOMElement("resource",
                    SynapseConstants.SYNAPSE_OMNAMESPACE);
                resource.addAttribute("location", (String)entry.getKey(), null);
                resource.addAttribute("key", (String)entry.getValue(), null);
                parent.addChild(resource);
            }
        }
    }
}
