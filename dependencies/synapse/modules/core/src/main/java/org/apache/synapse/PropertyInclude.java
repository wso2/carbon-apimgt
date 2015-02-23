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

package org.apache.synapse;

import org.apache.synapse.mediators.MediatorProperty;

import java.util.Collection;

/**
 * This inteface makes an entity to have properties. Usually properties are specified
 * in the XML configuration langugae.
 */
public interface PropertyInclude {
    /**
     * Add a property
     * @param property property to be added
     */
    void addProperty(MediatorProperty property);

    /**
     * Add a set of properties
     * @param properties <code>Collection</code> of properties to be added
     */
    void addProperties(Collection<MediatorProperty> properties);

    /**
     * Retrieve the property with the specific name
     * @param name name of the parameter
     * @return the value of the parameter if present otherwise <code>null</code>
     */
    MediatorProperty getProperty(String name);

    /**
     * Remove a property and return it
     * @param name name of the property to be removed
     * @return Property which is removed
     */
    MediatorProperty removeProperty(String name);

    /**
     * Get all the parameters as a <code>Collection</code>
     * @return retrieve the parameters as a <code>Collection</code>
     */
    Collection<MediatorProperty> getProperties();
}
