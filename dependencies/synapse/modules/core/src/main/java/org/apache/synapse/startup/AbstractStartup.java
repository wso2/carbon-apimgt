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

package org.apache.synapse.startup;

import org.apache.synapse.Startup;

/**
 * 
 */
public abstract class AbstractStartup implements Startup {

    /**
     * Holds the name of a Startup
     */
    protected String name = null;

    /**
     * Holds the name of the file where this startup is defined
     */
    protected String fileName;

    /**
     * Holds the description of the startup
     */
    protected String description;

    /**
     * This will return the name of the startup
     *
     * @return String representing the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * This will set the name of a Startup
     *
     * @param name
     *          String name to be set to the startup
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the file where this startup is defined
     *
     * @return a file name as a string or null
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the name of the file name where this startup is defined
     *
     * @param fileName the name of the file as a string
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the description of the startup
     *
     * @return description of the startup
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the startup
     *
     * @param description tobe set to the artifact
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
