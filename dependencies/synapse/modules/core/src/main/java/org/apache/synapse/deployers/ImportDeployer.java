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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.SynapseImportFactory;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.libraries.util.LibDeployerUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ImportDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(ImportDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Synapse Import Deployment from file : " + fileName + " : Started");
        }

        try {
            SynapseImport synImport = SynapseImportFactory.createImport(artifactConfig, properties);
            String synImportQualfiedName = LibDeployerUtils.getQualifiedName(synImport);

            SynapseImport existingImport = getSynapseConfiguration().getSynapseImports().get(synImportQualfiedName);

            if (existingImport != null) {
                //a synapse import with the same name (name + version) already exists
                //we should not allow multiple such imports
                log.warn("Synapse Import with the name : " + synImportQualfiedName + " already exists! " +
                         "Could not load multiple Imports of same type.");
                String backedUp = backupFile(new File(fileName));
                log.info("Synapse Import with the name : " + synImportQualfiedName + " is now backed up in : "
                         + backedUp);
                return null;
            } else {
                if (synImport != null) {
                    synImport.setFileName((new File(fileName)).getName());
                    getSynapseConfiguration().addSynapseImport(synImportQualfiedName, synImport);
                    //get corresponding library for loading imports if available
                    Library synLib = getSynapseConfiguration().getSynapseLibraries()
                            .get(synImportQualfiedName);
					if (synLib != null) {
						if (synImport.isStatus()) {
							LibDeployerUtils.loadLibArtifacts(synImport, synLib);
						} else {
							synLib.setLibStatus(false);
							synLib.unLoadLibrary();
						}
					}
                    log.info("Synapse Library Import named '" + synImportQualfiedName +
                             " has been deployed from file : "
                             + fileName);
                    return synImportQualfiedName;
                } else {
                    handleSynapseArtifactDeploymentError("Synapse Import Deployment Failed. " +
                                                         "The artifact described in the file " +
                                                         fileName + " is not a valid import");
                }
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Sequence Deployment from the file : " + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Synapse Import Deployment from file : " + fileName + " : Started");
        }

        try {
            SynapseImport synImport = SynapseImportFactory.createImport(artifactConfig, properties);
            String synImportQualfiedName = LibDeployerUtils.getQualifiedName(synImport);

            if (synImport == null) {
                handleSynapseArtifactDeploymentError("Synapse Import update failed. The artifact " +
                                                     "defined in the file: " + fileName + " is not a valid import.");
                return null;
            }

            if (log.isDebugEnabled()) {
                log.debug("Synapse Import: " + synImportQualfiedName + " has been built from the file: " + fileName);
            }

            if (existingArtifactName.equals(synImportQualfiedName)) {
                //normal update ,import Qualified Name(lib name + version) has not changed
                synImport.setFileName((new File(fileName)).getName());
                getSynapseConfiguration().addSynapseImport(synImportQualfiedName, synImport);
                //get corresponding library for loading imports if available
                Library synLib = getSynapseConfiguration().getSynapseLibraries()
                        .get(synImportQualfiedName);
                if (synLib != null) {
                    //this is a important step -> we need to unload what ever the components loaded previously
                    //then reload
					if (synImport.isStatus()) {
						synLib.unLoadLibrary();
						LibDeployerUtils.loadLibArtifacts(synImport, synLib);
					}else{
						synLib.setLibStatus(false);
						synLib.unLoadLibrary();
					}
                }
                log.info("Synapse Library Import named '" + synImportQualfiedName +
                         " has been deployed from file : "
                         + fileName);
            } else {
                //when updating ,import Qualified Name has been changed !!
                //check for any other import with the same name
                SynapseImport existingImport = getSynapseConfiguration().getSynapseImports().get(synImportQualfiedName);
                if (existingImport != null) {
                    //a synapse import with the same name (name + version) already exists
                    //we should not allow multiple such imports
                    log.warn("Synapse Import with the name : " + synImportQualfiedName + " already exists! " +
                             "Could not load multiple Imports of same type.");
                    String backedUp = backupFile(new File(fileName));
                    log.info("Synapse Import with the name : " + synImportQualfiedName + " is now backed up in : "
                             + backedUp);
                    return null;
                }else {
                    synImport.setFileName((new File(fileName)).getName());
                    getSynapseConfiguration().addSynapseImport(synImportQualfiedName, synImport);
                    //get corresponding library for loading imports if available
                    Library synLib = getSynapseConfiguration().getSynapseLibraries()
                            .get(synImportQualfiedName);
                    //this is a important step -> we need to unload what ever the components loaded previously
                    synLib.unLoadLibrary();
                    //then reload
                    if (synLib != null) {
                        LibDeployerUtils.loadLibArtifacts(synImport, synLib);
                    }
                    log.info("Synapse Library Import named '" + synImportQualfiedName +
                             " has been deployed from file : "
                             + fileName);
                }
            }

            log.info("Synapse Import: " + synImportQualfiedName + " has been updated from the file: " + fileName);

            waitForCompletion(); // Give some time for worker threads to release the old sequence
            return synImportQualfiedName;

        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("Error while updating the Synapse Import from the " +
                                                 "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Undeployment of the Synapse Import named : "
                      + artifactName + " : Started");
        }
        try {
            SynapseImport undeployingImport = getSynapseConfiguration().getSynapseImports().get(artifactName);
            if (undeployingImport != null) {
                getSynapseConfiguration().removeSynapseImport(artifactName);
                //get corresponding library for un-loading this import
                Library synLib = getSynapseConfiguration().getSynapseLibraries().get(artifactName);
                if (synLib != null) {
                    //this is a important step -> we need to unload what ever the components loaded thru this import
                    synLib.unLoadLibrary();
                    synLib.setLibStatus(false);
                }
                log.info("Synapse Import : " + artifactName + "' has been undeployed");
            } else {
                log.warn("Synapse Import : " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Undeployement of Synapse Import named : " + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        //TODO implement --> need to implement the serializer

/*
        if (log.isDebugEnabled()) {
            log.debug("Restoring the Sequence with name : " + artifactName + " : Started");
        }

        try {
            SequenceMediator seq
                    = getSynapseConfiguration().getDefinedSequences().get(artifactName);
            OMElement seqElem = MediatorSerializerFinder.getInstance().getSerializer(seq).
                    serializeMediator(null, seq);
            if (seq.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                                  + File.separator + MultiXMLConfigurationBuilder.SEQUENCES_DIR
                                  + File.separator + seq.getFileName();
                writeToFile(seqElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the Sequence with name : " + artifactName + " : Completed");
                }
                log.info("Sequence named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the sequence named '"
                                                     + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the sequence named '" + artifactName + "' has failed", e);
        }
*/
    }

    private String backupFile(File file) throws DeploymentException {
        String filePath = FilenameUtils.normalize(file.getAbsolutePath());

        String backupFilePath = filePath + ".back";
        int backupIndex = 0;
        while (backupIndex >= 0) {
            if (new File(backupFilePath).exists()) {
                backupIndex++;
                backupFilePath = filePath + "." + backupIndex + ".back";
            } else {
                backupIndex = -1;
                try {
                    FileUtils.moveFile(file, new File(backupFilePath));
                } catch (IOException e) {
                    handleSynapseArtifactDeploymentError("Error while backing up the artifact: " +
                                                         file.getName(), e);
                }
            }
        }
        return backupFilePath;
    }

}
