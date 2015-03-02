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
package org.apache.synapse.libraries.model;

import org.apache.synapse.SynapseArtifact;

import javax.xml.namespace.QName;

import java.util.List;
import java.util.Map;

/**
 * @author dushan
 * 
 */
/**
 * @author dushan
 * 
 */
public interface Library extends SynapseArtifact {

    /**
     * get Fully qualified Name of the Library
     * 
     * @return returns the logical name of the Synapse library which constitutes
     *         of [package + library name]
     */
    public QName getQName();

    /**
     * returns the package that this Library belongs to
     * 
     * @return package name
     */
    public String getPackage();

    /**
     * return synapse lib artifact deployed by this library with the given
     * artifact name
     * 
     * @param artifacName
     * @return
     */
    public Object getArtifact(String artifacName);


    public Map<String, Object> getArtifacts();

    /**
     * gives the Artifact description for the given artifact name (if available)
     * 
     * @param artifactName
     * @return
     */
    public String getArtifactDescription(String artifactName);

    public Map<String, String> getLibArtifactDetails();

    /**
     * load all library artifacts on this library for each and every namespace
     * this should be called when a import is taking place
     * 
     * @return success
     */
    public boolean loadLibrary();

    /**
     * unload all library artifacts on this library this should be called when a
     * import is no longer valid/ non-existent or library being undeployed
     * 
     * @return success
     */
    public boolean unLoadLibrary();

    /**
     * 
     * @return the Class loader that can be used to load classes/resources under
     *         this library
     */
    public ClassLoader getLibClassLoader();

    public String getFileName();

    public void setFileName(String fileName);

    /**
     * Returns the list of local entry definitions
     * 
     * @return
     */
    public Map<String, Object> getLocalEntryArtifacts();

    public List<String> getLocalEntries();

    /**
     * Gets the library status which can determine whether the library should be
     * available during run time
     * 
     * @return
     */
    public boolean getLibStatus();

    /**
     * @param status
     * @return
     */
    public void setLibStatus(boolean status);

}
