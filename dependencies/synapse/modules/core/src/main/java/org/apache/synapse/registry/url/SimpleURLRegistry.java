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

package org.apache.synapse.registry.url;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.registry.AbstractRegistry;
import org.apache.synapse.registry.Registry;
import org.apache.synapse.registry.RegistryEntry;
import org.apache.synapse.registry.RegistryEntryImpl;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Properties;

/**
 * A Simple HTTP GET based registry which will work with a Web Server / WebDAV
 * <p/>
 * This saves the root server URL, and appends the a given key to construct the
 * full URL to locate resources
 */
public class SimpleURLRegistry extends AbstractRegistry implements Registry {

    private static final Log log = LogFactory.getLog(SimpleURLRegistry.class);

    private static final int MAX_KEYS = 200;
    private String root = "";
    private final OMFactory omFactory = OMAbstractFactory.getOMFactory();

    public OMNode lookup(String key) {

        if (log.isDebugEnabled()) {
            log.debug("==> Repository fetch of resource with key : " + key);

        }
        URL url = SynapseConfigUtils.getURLFromPath(root + key, properties.get(
                SynapseConstants.SYNAPSE_HOME) != null ?
                properties.get(SynapseConstants.SYNAPSE_HOME).toString() : "");
        if (url == null) {
            return null;
        }

        BufferedInputStream inputStream;
        try {
            URLConnection connection = SynapseConfigUtils.getURLConnection(url);
            if (connection == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot create a URLConnection for given URL : " + url);
                }
                return null;
            }
            connection.connect();
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            return null;
        }

        OMNode result = null;

        if (inputStream != null) {

            try {

                XMLStreamReader parser = XMLInputFactory.newInstance().
                        createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                result = builder.getDocumentElement();

            } catch (OMException ignored) {

                if (log.isDebugEnabled()) {
                    log.debug("The resource at the provided URL isn't " +
                            "well-formed XML,So,takes it as a text");
                }

                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error in closing the input stream. ", e);
                }
                result = SynapseConfigUtils.readNonXML(url);

            } catch (XMLStreamException ignored) {

                if (log.isDebugEnabled()) {
                    log.debug("The resource at the provided URL isn't " +
                            "well-formed XML,So,takes it as a text");
                }

                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error in closing the input stream. ", e);
                }
                result = SynapseConfigUtils.readNonXML(url);

            } finally {
                try {
                    if (result != null && result.getParent() != null) {
                        //TODO Replace following code with the correct code when synapse is moving to AXIOM 1.2.9
                        result.detach();
                        OMDocument omDocument = omFactory.createOMDocument();
                        omDocument.addChild(result);
                    }
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error in closing the input stream.", e);
                }

            }

        }
        return result;
    }

    public RegistryEntry getRegistryEntry(String key) {

        if (log.isDebugEnabled()) {
            log.debug("Perform RegistryEntry lookup for key : " + key);
        }
        URL url = SynapseConfigUtils.getURLFromPath(root + key, properties.get(
                SynapseConstants.SYNAPSE_HOME) != null ?
                properties.get(SynapseConstants.SYNAPSE_HOME).toString() : "");
        if (url == null) {
            return null;
        }
        URLConnection connection = SynapseConfigUtils.getURLConnection(url);
        if (connection == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot create a URLConnection for given URL : " + url);
            }
            return null;
        }

        RegistryEntryImpl wre = new RegistryEntryImpl();
        wre.setKey(key);
        wre.setName(url.getFile());
        wre.setType(connection.getContentType());
        wre.setDescription("Resource at : " + url.toString());
        wre.setLastModified(connection.getLastModified());
        wre.setVersion(connection.getLastModified());
        if (connection.getExpiration() > 0) {
            wre.setCachableDuration(
                    connection.getExpiration() - System.currentTimeMillis());
        } else {
            wre.setCachableDuration(getCachableDuration());
        }
        return wre;
    }

    public OMElement getFormat(Entry entry) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public OMNode lookupFormat(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init(Properties properties) {
        super.init(properties);
        String value = properties.getProperty("root");
        if (value != null) {
            // if the root is folder, it should always end with '/'
            // therefore, property keys do not have to begin with '/', which could be misleading
            try {
                new URL(value);
                if (!value.endsWith("/")) {
                    value = value + "/";
                }
            } catch (MalformedURLException e) {
                // don't do any thing if this is not a valid URL
            }
            root = value;
        } else {
            handleException("Parameter root is null");
        }

    }


    public void delete(String path) {
        //TODO
    }

    public void newResource(String path, boolean isDirectory) {
        //TODO
    }

    public void updateResource(String path, Object value) {
        //TODO
    }

    public void updateRegistryEntry(RegistryEntry entry) {
        //TODO
    }


    private long getCachableDuration() {
        String cachableDuration = (String) properties.get("cachableDuration");
        return cachableDuration == null ? 1500 : Long.parseLong(cachableDuration);
    }

    public RegistryEntry[] getChildren(RegistryEntry entry) {
        URL url;
        if (entry == null) {
            RegistryEntryImpl entryImpl = new RegistryEntryImpl();
            entryImpl.setKey("");
            entry = entryImpl;
        }
        url = SynapseConfigUtils.getURLFromPath(root + entry.getKey(), properties.get(
                SynapseConstants.SYNAPSE_HOME) != null ?
                properties.get(SynapseConstants.SYNAPSE_HOME).toString() : "");
        if (url == null) {
            return null;
        }
        if (url.getProtocol().equals("file")) {

            File file = new File(url.getFile());
            if (!file.isDirectory()) {
                return null;
            }
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
                ArrayList<RegistryEntry> entryList = new ArrayList<RegistryEntry>();
                String key;
                while ((key = reader.readLine()) != null) {
                    RegistryEntryImpl registryEntryImpl = new RegistryEntryImpl();
                    if (entry.getKey().equals("")) {
                        registryEntryImpl.setKey(key);
                    } else {
                        if (entry.getKey().endsWith("/")) {
                            registryEntryImpl.setKey(entry.getKey() + key);
                        } else {
                            registryEntryImpl.setKey(entry.getKey() + "/" + key);
                        }
                    }

                    entryList.add(registryEntryImpl);
                }

                RegistryEntry[] entries = new RegistryEntry[entryList.size()];
                for (int i = 0; i < entryList.size(); i++) {
                    entries[i] = entryList.get(i);
                }
                return entries;
            } catch (Exception e) {
                throw new SynapseException("Error in reading the URL.");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } else {
            throw new SynapseException("Invalid protocol.");
        }
    }

    public RegistryEntry[] getDescendants(RegistryEntry entry) {

        ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>();
        fillDescendants(entry, list);

        RegistryEntry[] descendants = new RegistryEntry[list.size()];
        for (int i = 0; i < list.size(); i++) {
            descendants[i] = list.get(i);
        }

        return descendants;
    }

    private void fillDescendants(RegistryEntry parent, ArrayList<RegistryEntry> list) {

        RegistryEntry[] children = getChildren(parent);
        if (children != null) {
            for (RegistryEntry child : children) {
                if (child == null) {
                    continue;
                }
                if (list.size() > MAX_KEYS) {
                    break;
                }
                fillDescendants(child, list);
            }
        } else {
            list.add(parent);
        }
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
