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

/**
 * This interface defines the core information to be returned by a Registry implementation
 * about a resource being managed by it. Every Registry implementation *must* provide valid
 * information for the methods marked below as 'required'
 */
public interface RegistryEntry {

    /** The key for the resource - required */
    public String getKey();

    /** A name for the resource - optional */
    public String getName();

    /** The version of the resource - required */
    public long getVersion();

    /** The type of the resource - optional */
    public String getType();

    /** A description for the resource - optional */
    public String getDescription();

    /** The created time for the resource - optional */
    public long getCreated();

    /** The last updated time for the resource - optional */
    public long getLastModified();

    /** The number of milliseconds this resource could be cached */
    public long getCachableDuration();
}
