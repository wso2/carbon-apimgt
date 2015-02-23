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

import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.config.Entry;
import org.apache.synapse.SynapseException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * Factory for {@link Entry} instances.
 */
public class EntryFactory implements XMLToObjectMapper {

    private static Log log = LogFactory.getLog(EntryFactory.class);

    private static final QName DESCRIPTION_Q
            = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "description");

    public static Entry createEntry(OMElement elem, Properties properties) {
        String customFactory = SynapsePropertiesLoader.getPropertyValue("synapse.entry.factory", "");
        if (customFactory != null && !"".equals(customFactory)) {
            try {
                Class c = Class.forName(customFactory);
                Object o = c.newInstance();
                if (o instanceof IEntryFactory) {
                    return ((IEntryFactory)o).createEntry(elem);
                }
            } catch (ClassNotFoundException e) {
                 handleException("Class specified by the synapse.entry.factory " +
                         "synapse property not found: " + customFactory, e);
            } catch (InstantiationException e) {
                handleException("Class specified by the synapse.entry.factory " +
                         "synapse property cannot be instantiated: " + customFactory, e);
            } catch (IllegalAccessException e) {
                handleException("Class specified by the synapse.entry.factory " +
                         "synapse property cannot be accessed: " + customFactory, e);
            }
        }

        OMAttribute key = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
        if (key == null) {
            handleException("The 'key' attribute is required for a local registry entry");
            return null;

        } else {

            Entry entry = new Entry(key.getAttributeValue());

            OMElement descriptionElem = elem.getFirstChildWithName(DESCRIPTION_Q);
            if (descriptionElem != null) {
                entry.setDescription(descriptionElem.getText());
                descriptionElem.detach();
            }

            String src  = elem.getAttributeValue(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "src"));

            // if a src attribute is present, this is a URL source resource,
            // it would now be loaded from the URL source, as all static properties
            // are initialized at startup
            if (src != null) {
                try {
                    entry.setSrc(new URL(src.trim()));
                    entry.setType(Entry.URL_SRC);
                    entry.setValue(SynapseConfigUtils.getObject(entry.getSrc(), properties));
                } catch (MalformedURLException e) {
                    handleException("The entry with key : " + key + " refers to an invalid URL");
                }

            } else {
                OMNode    nodeValue = elem.getFirstOMChild();
                OMElement elemValue = elem.getFirstElement();

                if (elemValue != null) {
                    entry.setType(Entry.INLINE_XML);
                    entry.setValue(elemValue);
                } else if (nodeValue != null && nodeValue instanceof OMText) {
                    entry.setType(Entry.INLINE_TEXT);
                	entry.setValue(elem.getText());
				}
            }

            return entry;
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    public Object getObjectFromOMNode(OMNode om, Properties properties) {
        if (om instanceof OMElement) {
            return createEntry((OMElement) om, properties);
        } else {
            handleException("Invalid XML configuration for an Entry. OMElement expected");
        }
        return null;
    }
}
