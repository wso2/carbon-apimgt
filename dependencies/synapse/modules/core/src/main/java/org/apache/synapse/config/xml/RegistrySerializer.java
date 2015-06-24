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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.registry.Registry;

import java.util.Iterator;

/**
 * &lt;registry [name="string"] provider="provider.class"&gt;
 *   &lt;property name="string" value="string"&gt;
 * &lt;/registry&gt;
 */
public class RegistrySerializer {

    private static final Log log = LogFactory.getLog(RegistrySerializer.class);

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    protected static final OMNamespace nullNS = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    public static OMElement serializeRegistry(OMElement parent, Registry registry) {

        OMElement reg = fac.createOMElement("registry", synNS);

        if (registry.getProviderClass() != null) {
            reg.addAttribute(fac.createOMAttribute(
                "provider", nullNS, registry.getProviderClass()));
        } else {
            handleException("Invalid registry. Provider is required");
        }

        Iterator iter = registry.getConfigurationProperties().keySet().iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            String value = (String) registry.getConfigurationProperties().get(name);
            OMElement property = fac.createOMElement("parameter", synNS);
            property.addAttribute(fac.createOMAttribute(
                "name", nullNS, name));
            property.setText(value.trim());
            reg.addChild(property);
        }

        if (parent != null) {
            parent.addChild(reg);
        }
        return reg;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
