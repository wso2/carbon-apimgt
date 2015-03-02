/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.synapse.deployers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.libraries.util.LibDeployerUtils;

public class LibraryArtifactDeployer extends AbstractSynapseArtifactDeployer {
    private static final Log log = LogFactory.getLog(LibraryArtifactDeployer.class);

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
	String libFilePath = FilenameUtils.normalize(deploymentFileData.getAbsolutePath());
	if (log.isDebugEnabled()) {
	    log.debug("Deployment of the synapse library artifact from file : " + libFilePath
		    + " : STARTED");
	}

	// if (getServerContextInformation().getServerState() !=
	// ServerState.STARTED) {
	// // synapse server has not yet being started
	// if (log.isDebugEnabled()) {
	// log.debug("Skipped the library artifact deployment (since the Synapse "
	// +
	// "server doesn't seem to be started yet), from file : "
	// + deploymentFileData.getAbsolutePath());
	// }
	// return;
	// }
	try {
	    SynapseArtifactDeploymentStore deploymentStore = getSynapseConfiguration()
		    .getArtifactDeploymentStore();

	    Library lib = LibDeployerUtils.createSynapseLibrary(libFilePath);
	    String libArtifactName = lib.getQName().toString();
	    if (log.isDebugEnabled()) {
		log.debug("Created the Synapse Library : " + libArtifactName + "  from : "
			+ libFilePath);
	    }

	    if (deploymentStore.isUpdatingArtifact(libFilePath)) {

		if (log.isDebugEnabled()) {
		    log.debug("Updating Library artifact detected with filename : " + libFilePath);
		}
		// this is an hot-update case
		String existingArtifactName = deploymentStore
			.getUpdatingArtifactWithFileName(libFilePath);
		deploymentStore.removeUpdatingArtifact(libFilePath);
		undeploySynapseArtifact(existingArtifactName);

		// deploy from beginning
		// add the library to synapse Config
		completeDeployment(lib, libArtifactName);

	    } else {
		// new artifact hot-deployment case
		try {
		    // add the library to synapse Config
		    completeDeployment(lib, libArtifactName);
		} catch (SynapseArtifactDeploymentException sade) {
		    log.error("Deployment of the Synapse Artifact from file : " + libFilePath
			    + " : Failed!", sade);
		    /*
		     * log.info("The file has been backed up into : " +
		     * backupFile(deploymentFileData.getFile()));
		     */
		}
	    }
	    if (libArtifactName != null) {
		deploymentStore.addArtifact(libFilePath, libArtifactName);
	    }

	    log.info("Synapse Library named '" + lib.toString()
		    + "' has been deployed from file : " + libFilePath);

	} catch (IOException ex) {
	    handleDeploymentError("Deployment of synapse artifact failed. Error reading "
		    + libFilePath + " : " + ex.getMessage(), ex);
	} catch (Exception ex) {
	    handleDeploymentError("Deployment of synapse artifact failed for synapse libray at : "
		    + libFilePath + " : " + ex.getMessage(), ex);
	}

	if (log.isDebugEnabled()) {
	    log.debug("Deployment of the synapse artifact from file : " + libFilePath
		    + " : COMPLETED");
	}

    }

    private void completeDeployment(Library lib, String libArtifactName) throws DeploymentException {
	getSynapseConfiguration().addSynapseLibrary(lib.getQName().toString(), lib);
	if (log.isDebugEnabled()) {
	    log.debug("Synapse Library Deployment for lib: " + libArtifactName + " Completed");
	}

	// each time a library is deployed we check with available imports and
	// if necessary (ie:- relevant import is available) load the libraries
	SynapseImport synImport = getSynapseConfiguration().getSynapseImports()
		.get(libArtifactName);

	if (synImport != null && synImport.isStatus()) {
	    LibDeployerUtils.loadLibArtifacts(synImport, lib);
	    if (log.isDebugEnabled()) {
		log.debug("Loading Synapse Library: " + libArtifactName + " into memory for Import");
	    }
	    
	    LibDeployerUtils.deployingLocalEntries(lib, getSynapseConfiguration());
	}

	
    }

    public void undeploy(String fileName) throws DeploymentException {
	fileName = FilenameUtils.normalize(fileName);
	if (log.isDebugEnabled()) {
	    log.debug("UnDeployment of the synapse library from file : " + fileName + " : STARTED");
	}

	SynapseArtifactDeploymentStore deploymentStore = getSynapseConfiguration()
		.getArtifactDeploymentStore();

	if (deploymentStore.containsFileName(fileName)) {
	    File undeployingFile = new File(fileName);
	    // axis2 treats Hot-Update as (Undeployment + deployment), where
	    // synapse needs to differentiate the Hot-Update from the above two, since it needs
	    // some validations for a real undeployment. Also this makes sure a zero downtime of the
	    // synapse artifacts which are being Hot-deployed
	    if (undeployingFile.exists()) {
		if (log.isDebugEnabled()) {
		    log.debug("Marking artifact as updating from file : " + fileName);
		}
		// if the file exists, which means it has been updated and is a
		// Hot-Update case
		if (!deploymentStore.isRestoredFile(fileName)) {
		    deploymentStore.addUpdatingArtifact(fileName,
			    deploymentStore.getArtifactNameForFile(fileName));
		    deploymentStore.removeArtifactWithFileName(fileName);
		}
	    } else {
		// if the file doesn't exists then it is an actual undeployment
		String artifactName = deploymentStore.getArtifactNameForFile(fileName);
		try {

		    // CarbonApplication instance to delete
		    Library currentMediationLib = null;

		    // undeploying the local entries
		    Collection<Library> appList = getSynapseConfiguration().getSynapseLibraries()
			    .values();
		    for (Library mediationLib : appList) {
			if (artifactName.equals(mediationLib.getQName().toString())) {
			    currentMediationLib = mediationLib;
			}
		    }

		    if (currentMediationLib != null) {
			for (String localEntry : currentMediationLib.getLocalEntries()) {
			    getSynapseConfiguration().removeEntry(localEntry);
			}
		    }
		    // do un-deployment
		    undeploySynapseArtifact(artifactName);
		    
		    deploymentStore.removeArtifactWithFileName(fileName);

		    log.info("Synapse Library named '" + artifactName + "' has been undeployed");
		} catch (SynapseArtifactDeploymentException sade) {
		    log.error("Unable to undeploy the synapse library artifact from file : "
			    + fileName, sade);
		}
	    }
	} else {
	    String msg = "Artifact representing the filename " + fileName
		    + " is not deployed on Synapse";
	    log.error(msg);
	    throw new DeploymentException(msg);
	}

	if (log.isDebugEnabled()) {
	    log.debug("UnDeployment of the synapse library artifact from file : " + fileName
		    + " : COMPLETED");
	}
    }

    public void undeploySynapseArtifact(String artifactName) {
	// get Old Lib config
	Library existingLib = null;
	try {
	    existingLib = getSynapseConfiguration().getSynapseLibraries().get(artifactName);
	    existingLib.unLoadLibrary();
	    getSynapseConfiguration().removeSynapseImport(artifactName);
	    getSynapseConfiguration().removeSynapseLibrary(artifactName);
	} catch (DeploymentException e) {
	    handleDeploymentError(e.getMessage(), e);
	}
    }

    private void handleDeploymentError(String msg, Exception e) {
	log.error(msg, e);
    }

    // avoid implementing any of the below methods since these are unusable in
    // this library deployment
    // scenario . we just want to inherit some of the methods from
    // AbstractSynapseArtifactDeployer
    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
	    Properties properties) {
	return null;
    }

    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
	    String existingArtifactName, Properties properties) {
	return null;
    }

    public void restoreSynapseArtifact(String artifactName) {
    }
}