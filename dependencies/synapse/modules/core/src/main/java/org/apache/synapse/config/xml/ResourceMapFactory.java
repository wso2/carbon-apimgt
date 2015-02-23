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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.resolver.ResourceMap;

/**
 * Creates a ResourceMap object based on a set of <tt>&lt;resource&gt;</tt> elements:
 * <pre>
 * &lt;resource location="..." key="..."/&gt;*
 * </pre>
 */
public class ResourceMapFactory {
    private static final Log log = LogFactory.getLog(ResourceMapFactory.class);
    
    public static ResourceMap createResourceMap(OMElement elem) {
        ResourceMap resourceMap = null;
        Iterator it = elem.getChildrenWithName(
            new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "resource"));
        while (it.hasNext()) {
            // Lazily create the ResourceMap, so that when no <resource> 
            // elements are found, the method returns null.
            if (resourceMap == null) {
                resourceMap = new ResourceMap();
            }
            OMElement resourceElem = (OMElement)it.next();
            OMAttribute location = resourceElem.getAttribute
                (new QName(XMLConfigConstants.NULL_NAMESPACE, "location"));
            if (location == null) {
                handleException("The 'location' attribute is required for a resource definition");
            }
            OMAttribute key = resourceElem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
            if (key == null) {
                handleException("The 'key' attribute is required for a resource definition");
            }
            resourceMap.addResource(location.getAttributeValue(), key.getAttributeValue());
        }
        return resourceMap;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
