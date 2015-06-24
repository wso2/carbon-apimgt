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

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SynapseLibrary implements Library {
    private String packageN = null;

    /**
     * this is the logical name of the Synapse library which constitutes of
     * [package + library name]
     */
    protected QName qualifiedName = null;

    public List<LibraryArtifact.Dependency> dependencies;

    private Map<String, LibraryArtifact> depNameToArtifactIndex = new HashMap<String, LibraryArtifact>();

    private Map<String, Object> libComponentIndex = new HashMap<String, Object>();

    private Map<String, String> libArtifactDetails = new HashMap<String, String>();

    private final Map<String, Object> localEntryArtifacts = new ConcurrentHashMap<String, Object>();

    private List<String> localEntries = new ArrayList<String>();

    private String description;

    private boolean isLoaded = false;

    private ClassLoader libClassLoader = null;
    private String fileName;

    private boolean libStatus = false;

    public SynapseLibrary(String name, String packageName) {
        this.packageN = packageName;
        if (packageName != null && !"".equals(packageName)) {
            qualifiedName = new QName(packageName, name);
        } else {
            qualifiedName = new QName(name);
        }
        dependencies = new ArrayList<LibraryArtifact.Dependency>();
    }

    public QName getQName() {
        return qualifiedName;
    }

    public String getName() {
        return qualifiedName.getLocalPart();
    }

    public void addDependency(LibraryArtifact.Dependency artifactDep) {
        dependencies.add(artifactDep);
    }

    public void addComponent(String qualifiedName, Object libComponent) {
        libComponentIndex.put(qualifiedName, libComponent);
    }

    public void addArtifactDescription(LibraryArtifact artifact) {
        libArtifactDetails.put(artifact.getName(), artifact.getDescription());
    }

    public String getArtifactDescription(String artifactName) {
        return libArtifactDetails.get(artifactName);
    }

    public Map<String, String> getLibArtifactDetails() {
        return libArtifactDetails;
    }

    public void removeComponent(String qualifiedName) {
        libComponentIndex.remove(qualifiedName);
    }

    public boolean resolveDependencies(List<LibraryArtifact> unresolvedPrincipalArtifactList) {
        int unresolvedDeps = dependencies.size();
        for (LibraryArtifact.Dependency dependency : dependencies) {
            for (LibraryArtifact current : unresolvedPrincipalArtifactList) {
                if (dependency.resolveWith(current)) {
                    unresolvedDeps--;
                    depNameToArtifactIndex.put(dependency.getName(), current);
                    break;
                }
            }
        }

        if (unresolvedDeps == 0) {
            dependencies.clear();
            return true;
        } else {
            return false;
        }
    }

    /**
     * load all library artifacts on this library this should be called when a
     * import is taking place
     *
     * @return success
     */
    public synchronized boolean loadLibrary() {
        if (!isLoaded) {
            for (String artifactName : depNameToArtifactIndex.keySet()) {
                loadLibrary(artifactName);
            }
        }
        isLoaded = true;
        return true;
    }

    /**
     * load all library artifacts on this library for the given QName this
     * should be called when a import is taking place
     *
     * @return success
     */
    public synchronized void loadLibrary(String artifactDependencyName) {
        LibraryArtifact libAr = depNameToArtifactIndex.get(artifactDependencyName);
        libAr.loadComponentsInto(this);

        // TODO once all components are loaded iterate and initialize Lifecycle
        // method #init() ?
    }

    /**
     * unload all library artifacts on this library for the given QName this
     * should be called when a import is no longer valid/ non-existent
     *
     * @return success
     */
    public synchronized boolean unLoadLibrary() {
        // TODO when components are un-loaded iterate and execute Lifecycle
        // method #destroy() ?
        libComponentIndex.clear();
        isLoaded = false;
        return true;
    }

    public ClassLoader getLibClassLoader() {
        return libClassLoader;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void clear() {
        depNameToArtifactIndex.clear();
        libArtifactDetails.clear();
        libComponentIndex.clear();
    }

    /**
     * return synapse lib artifact deployed by this library with the given Local
     * name
     *
     * @param artifacName
     * @return
     */
    public Object getArtifact(String artifacName) {
        if (libComponentIndex.containsKey(artifacName)) {
            return libComponentIndex.get(artifacName);
        }
        return null;
    }


    public Map<String, Object> getArtifacts() {
        return libComponentIndex;
    }

    public String toString() {
        return qualifiedName.toString();
    }

    public String getPackage() {
        return packageN;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setLibClassLoader(ClassLoader libClassLoader) {
        this.libClassLoader = libClassLoader;
    }

    public Map<String, Object> getLocalEntryArtifacts() {
        return localEntryArtifacts;
    }

    public List<String> getLocalEntries() {
        return localEntries;
    }

    public boolean getLibStatus() {
        return libStatus;
    }

    public void setLibStatus(boolean status) {
        this.libStatus = status;
    }

}
