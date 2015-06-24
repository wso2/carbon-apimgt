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

package org.apache.synapse.deployers;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the artifacts deployed with files inside the synapse repository</p>
 *
 * <p>For hot deployment to properly work we need to, keep track fo not only the artifacts
 * deployed by deployers but also the artifacts deployed from files at the startup as well. Otherwise
 * it is not possible to track the hot update cases. This is introduced as a <code>singleton</code>
 * for the startup to report back for the deployed artifacts at startup apart from the deployers.</p>
 *
 * @see org.apache.synapse.deployers.AbstractSynapseArtifactDeployer
 * @see org.apache.synapse.config.xml.MultiXMLConfigurationBuilder
 */
public final class SynapseArtifactDeploymentStore {

    /** Keeps track of the deployed artifacts in the synapse environment */
    private Map<String, String> fileName2ArtifactName = new HashMap<String, String>();

    /** Keeps track of the updating artifacts in the synapse environment in a particular instance */
    private Map<String, String> updatingArtifacts = new HashMap<String, String>();

    /** Keeps track of the restored artifacts in the synapse environment in a particular instance */
    private Set<String> restoredFiles = new HashSet<String>();

    /** Keeps track of the backed up artifacts in the synapse environment in a particular instance */
    private Set<String> backedUpFiles = new HashSet<String>();

    /** Keeps track of last updated file of main sequence */
    private String mainSeqLstUpdatedFile = SynapseConstants.MAIN_SEQUENCE_XML;

    /** Keeps track of last updated file of fault sequence */
    private String faultSeqLstUpdatedFile = SynapseConstants.FAULT_SEQUENCE_XML;
   
    private static final Log log = LogFactory.getLog(SynapseArtifactDeploymentStore.class);

    private Map<String, ClassLoader> classMediatorClassLoaders = new HashMap<String, ClassLoader>();

    /**
     * Adds artifacts indexed with the respective filename
     * 
     * @param fileName name of the file from which the artifact being added is loaded
     * @param artifactName name of the artifact being added
     */
    public void addArtifact(String fileName, String artifactName) {

        fileName = getNormalizedAbsolutePath(fileName);
        if (!fileName2ArtifactName.containsKey(fileName)) {
            if (log.isDebugEnabled()) {
                log.debug("Added deployment artifact with file : " + fileName);
            }
            fileName2ArtifactName.put(fileName, artifactName);
        } else {
            log.error("An artifact has already been loaded from the file : " + fileName);
        }
    }

    /**
     * Checks whether there is an artifact indexed with the given <code>filename</code>
     * 
     * @param fileName artifact filename to be checked for the existence
     * @return boolean <code>true</code> if it is available, <code>false</code> if not
     */
    public boolean containsFileName(String fileName) {
        return fileName2ArtifactName.containsKey(getNormalizedAbsolutePath(fileName));
    }

    /**
     * Retrieves the artifact name indexed with the given <code>filename</code>
     *
     * @param fileName name of the file which maps to the artifact
     * @return String artifact name mapped with the give <code>filename</code>
     */
    public String getArtifactNameForFile(String fileName) {
        return fileName2ArtifactName.get(getNormalizedAbsolutePath(fileName));
    }

    /**
     * Removes the indexed artifacts to the <code>filename</code> mapping from the holder
     * 
     * @param fileName name of the file of which the artifact required to be removed
     */
    public void removeArtifactWithFileName(String fileName) {
        fileName = getNormalizedAbsolutePath(fileName);
        if (log.isDebugEnabled()) {
            log.debug("Removing deployment artifact with file : " + fileName);
        }
        fileName2ArtifactName.remove(fileName);
    }

    /**
     * Adds an updating artifact for the given instance
     * 
     * @param fileName name of the file from which the artifact has been loaded
     * @param artifactName name of the actual artifact being updated
     */
    public void addUpdatingArtifact(String fileName, String artifactName) {
        fileName = getNormalizedAbsolutePath(fileName);
        if (log.isDebugEnabled()) {
            log.debug("Added updating file : " + fileName);
        }
        updatingArtifacts.put(fileName, artifactName);
    }

    /**
     * Checks whether the given artifact is at the updating state in the given instance
     * 
     * @param fileName name of the file which describes the artifact to be checked
     * @return boolean <code>true</code> if it is at the updating state, <code>false</code> otherwise
     */
    public boolean isUpdatingArtifact(String fileName) {
        return updatingArtifacts.containsKey(getNormalizedAbsolutePath(fileName));
    }

    /**
     * Retrieves the artifact name corresponds to the given updating artifact file name
     *
     * @param fileName name of the file from which the artifact is being updated
     * @return String artifact name corresponds to the given file name
     */
    public String getUpdatingArtifactWithFileName(String fileName) {
        return updatingArtifacts.get(getNormalizedAbsolutePath(fileName));
    }

    /**
     * Removes an updating artifact
     *
     * @param fileName name of the file of the artifact to be removed from the updating artifacts
     */
    public void removeUpdatingArtifact(String fileName) {
        fileName = getNormalizedAbsolutePath(fileName);
        if (log.isDebugEnabled()) {
            log.debug("Removing the updating file : " + fileName);
        }
        updatingArtifacts.remove(fileName);
    }

    /**
     * Adds an artifact which is being restored
     *
     * @param fileName name of the file of the artifact which is being restored
     */
    public void addRestoredArtifact(String fileName) {
        fileName = getNormalizedAbsolutePath(fileName);
        if (log.isDebugEnabled()) {
            log.debug("Added restored file : " + fileName);
        }
        restoredFiles.add(fileName);
    }

    /**
     * Checks whether the given artifact is being restored
     * 
     * @param fileName name of the file to be checked
     * @return boolean <code>true</code> if the provided filename describes a restoring artifact,
     * <code>false</code> otherwise
     */
    public boolean isRestoredFile(String fileName) {
        return restoredFiles.contains(getNormalizedAbsolutePath(fileName));
    }

    /**
     * Removes a restored artifact
     *
     * @param fileName name of the file of the artifact to be removed
     */
    public void removeRestoredFile(String fileName) {
        fileName = getNormalizedAbsolutePath(fileName);
        if (log.isDebugEnabled()) {
            log.debug("Removing restored file : " + fileName);
        }
        restoredFiles.remove(fileName);
    }

    /**
     * Adds an artifact to the backedUp artifacts
     *
     * @param fileName name of the file of the artifact to be added into the backedUp artifacts
     */
    public void addBackedUpArtifact(String fileName) {
        fileName = getNormalizedAbsolutePath(fileName);
        if (log.isDebugEnabled()) {
            log.debug("Added backup file : " + fileName);
        }
        backedUpFiles.add(fileName);
    }

    /**
     * Checks whether the given artifact is being backed up
     *
     * @param fileName name of the file of the artifact to be checked
     * @return boolean <code>true</code> if the artifact is being backed up, <code>false</code> otherwise
     */
    public boolean isBackedUpArtifact(String fileName) {
        return backedUpFiles.contains(getNormalizedAbsolutePath(fileName));
    }

    /**
     * Removes a backedUp artifact
     * 
     * @param fileName name of the file of the artifact to be removed
     */
    public void removeBackedUpArtifact(String fileName) {
        fileName = getNormalizedAbsolutePath(fileName);
        if (log.isDebugEnabled()) {
            log.debug("Removing backup file : " + fileName);
        }
        backedUpFiles.remove(fileName);
    }

    public static String getNormalizedAbsolutePath(String fileName) {
        String path;
        File file = new File(fileName);
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            log.warn("Error while computing the canonical path of file: " + fileName);
            path = file.getAbsolutePath();
        }
        return FilenameUtils.normalize(path);
    }

    public String getMainSeqLstUpdatedFile() {
        return mainSeqLstUpdatedFile;
    }

    public void setMainSeqLstUpdatedFile(String mainSeqLstUpdatedFile) {
        String mainSeqFile = mainSeqLstUpdatedFile.substring(mainSeqLstUpdatedFile.lastIndexOf(File.separator) + 1);
        this.mainSeqLstUpdatedFile = mainSeqFile;
    }

    public String getFaultSeqLstUpdatedFile() {
        return faultSeqLstUpdatedFile;
    }

    public void setFaultSeqLstUpdatedFile(String faultSeqLstUpdatedFile) {
        String faultSeqFile = faultSeqLstUpdatedFile.substring(faultSeqLstUpdatedFile.lastIndexOf(File.separator) + 1);
        this.faultSeqLstUpdatedFile = faultSeqFile;
    }

    public Map<String, ClassLoader> getClassMediatorClassLoaders() {
        return classMediatorClassLoaders;
    }

    public void addClassMediatorClassLoader(String path, ClassLoader classLoader) {
        this.classMediatorClassLoaders.put(path, classLoader);
    }

    public void removeClassMediatorClassLoader(String path) {
        this.classMediatorClassLoaders.remove(path);
    }
}
