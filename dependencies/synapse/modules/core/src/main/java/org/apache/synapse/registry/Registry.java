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

package org.apache.synapse.registry;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.synapse.config.Entry;

import java.util.Properties;

/**
 * This is the interface to a Registry from Synapse.
 */
public interface Registry {

    /**
     * Initializes the registry with given properties
     *
     * @param properties The configuration properties
     */
    public void init(Properties properties);

    /**
     * Perform an actual lookup for for an XML resource as an OMNode for the given key
     *
     * @param key the key for the registry lookup
     * @return the XML content from the registry as an OMNode
     */
    public OMNode lookup(String key);

    /**
     * This is the publicly used interface to the registry. It will fetch
     * the content from the registry and cache if required.
     *
     * @param entry the registry Entry
     * @param properties
     * @return the value from the registry or local cache
     * @see AbstractRegistry
     */
    public Object getResource(Entry entry, Properties properties);

    /**
     * Get the registry entry for the given key
     *
     * @param key the registry key
     * @return The registry entry for the given key
     */



    public RegistryEntry getRegistryEntry(String key);


     public OMElement getFormat(Entry entry);

   public OMNode lookupFormat(String key);
    /**
     * Returns the child elements of a given registry entry
     *
     * @param entry - parent registry entry
     * @return Array of child registry entries of the given parent registry entry
     */
    public RegistryEntry[] getChildren(RegistryEntry entry);

    /**
     * Returns all descendant entries of the given registry entry
     *
     * @param entry - parent registry entry
     * @return Array of decendant registry entries of the given registry entry
     */
    public RegistryEntry[] getDescendants(RegistryEntry entry);

    /**
     * Return the name of the implementation class
     *
     * @return name of the registry provider implementation class name
     */
    public String getProviderClass();

    /**
     * Return the list of configuration properties set on this instance
     *
     * @return a Map of configuration properties
     */
    public Properties getConfigurationProperties();

    /**
     * Deletes a resource in the given path
     *
     * @param path The path the of resource
     */
    public void delete(String path);

    /**
     * Creates a new resource in the given path
     *
     * @param path        The new resource path
     * @param isDirectory Whether resource is a collection or not
     */
    public void newResource(String path, boolean isDirectory);

    /**
     * Updates the value of a resource
     *
     * @param path  The resource to be updated
     * @param value The value to be set
     */
    public void updateResource(String path, Object value);

    /**
     * Updates the registry enrty (metadata about a resource)
     *
     * @param entry The registry entry
     */
    public void updateRegistryEntry(RegistryEntry entry);

    /**
     * Get the resource properties of a given resource
     *
     * @param entryKey The registry entry
     */
    public Properties getResourceProperties(String entryKey);
}
