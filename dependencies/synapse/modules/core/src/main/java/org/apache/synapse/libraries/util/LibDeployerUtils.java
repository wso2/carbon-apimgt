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
package org.apache.synapse.libraries.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.deployers.SynapseArtifactDeploymentException;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.libraries.model.LibraryArtifact;
import org.apache.synapse.libraries.model.SynapseLibrary;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LibDeployerUtils {

    public static final String APP_UNZIP_DIR;

    static {
        String javaTempDir = System.getProperty("java.io.tmpdir");
        APP_UNZIP_DIR = javaTempDir.endsWith(File.separator) ?
                        javaTempDir + LibDeployerConstants.SYNAPSE_LIBS :
                        javaTempDir + File.separator + LibDeployerConstants.SYNAPSE_LIBS;
        createDir(APP_UNZIP_DIR);
    }

    private static final Log log = LogFactory.getLog(LibDeployerUtils.class);



    public static Library createSynapseLibrary(String libPath) {
        String libFilePath = LibDeployerUtils.formatPath(libPath);
        //extract
        String extractPath = LibDeployerUtils.extractSynapseLib(libFilePath);
        //create synapse lib metadata
        SynapseLibrary synapseLib = LibDeployerUtils.populateDependencies(extractPath +
                                                                          LibDeployerConstants.ARTIFACTS_XML);

        //create a ClassLoader for loading this synapse lib classes/resources
        try {
            ClassLoader libLoader = Utils.getClassLoader(LibDeployerUtils.class.getClassLoader(),
                                                         extractPath, false);
            synapseLib.setLibClassLoader(libLoader);
        } catch (DeploymentException e) {
            throw new SynapseArtifactDeploymentException("Error setting up lib classpath for Synapse" +
                                                         " Library  : " + libFilePath, e);
        }
        //resolve synapse lib artifacts
        LibDeployerUtils.searchAndResolveDependencies(extractPath, synapseLib);
        
        //TODO:reslove local-entry references
        LibDeployerUtils.populateLocalEnties(synapseLib,extractPath+LibDeployerConstants.LOCAL_ENTRIES);
        
        synapseLib.setFileName(libFilePath);
        return synapseLib;
    }

    private static void populateLocalEnties(SynapseLibrary synapseLibrary,String localEntriesFilePath) {
		// TODO Auto-generated method stub
    	  File dir = new File(localEntriesFilePath);
    	  if(dir.isDirectory()){
    		  File [] entries = dir.listFiles();
    		  for(File file :entries){
    			  synapseLibrary.getLocalEntryArtifacts().put(file.getName(), file);
    		  }
    	  }
		
	}

	/**
     * populate Dependencies using main root artifacts.xml.. Schema for artifacts.xml is follwing
     *
     *<artifacts>
         <artifact name="SampleLib" package="synapse.sample" >
                <dependency artifact="templates1" /> +
                <description>sample synapse library</description> ?
         </artifact>
    </artifacts>
     *
     * @param libXmlPath
     * @return
     */
    private static SynapseLibrary populateDependencies(String libXmlPath) {
        File f = new File(libXmlPath);
        if (!f.exists()) {
            throw new SynapseException("artifacts.xml file not found at : " + libXmlPath);
        }
        InputStream xmlInputStream = null;
        try {
            xmlInputStream = new FileInputStream(f);
            OMElement documentElement = new StAXOMBuilder(xmlInputStream).getDocumentElement();
            if (documentElement == null) {
                throw new SynapseArtifactDeploymentException("Document element for artifacts.xml is " +
                                                             "null. Can't build " +
                                                             "the synapse library configuration");
            }
            Iterator artifactItr = documentElement.getChildrenWithLocalName(LibDeployerConstants.ARTIFACT);
            SynapseLibrary mainSynLibArtifact = null;
            mainSynLibArtifact = createSynapseLibraryWithDeps(((OMElement) artifactItr.next()));
            if (mainSynLibArtifact == null) {
                throw new SynapseArtifactDeploymentException("artifacts.xml is invalid. <artifact> element" +
                                                             " Not Found ");
            }
            return mainSynLibArtifact;
        } catch (FileNotFoundException e) {
            throw new SynapseArtifactDeploymentException("artifacts.xml File cannot be loaded from " + libXmlPath, e);

        } catch (XMLStreamException e) {
            throw new SynapseArtifactDeploymentException("Error while parsing the artifacts.xml file ", e);
        } finally {
            if (xmlInputStream != null) {
                try {
                    xmlInputStream.close();
                } catch (IOException e) {
                    log.error("Error while closing input stream.", e);
                }
            }
        }
    }

    /**
     * Builds the Artifact object when an artifact element is given
     *
     * @param artifactEle - artifact OMElement
     * @return created Artifact object
     */
    private static SynapseLibrary createSynapseLibraryWithDeps(OMElement artifactEle) {
        if (artifactEle == null) {
            return null;
        }
        SynapseLibrary synLib = new SynapseLibrary(readAttribute(artifactEle, LibDeployerConstants.NAME),
                                                   readAttribute(artifactEle, LibDeployerConstants.PACKAGE_ATTR));
        synLib.setDescription(readChildText(artifactEle,LibDeployerConstants.DESCRIPTION_ELEMENT));
        // read the dependencies
        Iterator itr = artifactEle.getChildrenWithLocalName(LibDeployerConstants.DEPENDENCY);
        while (itr.hasNext()) {
            OMElement depElement = (OMElement) itr.next();
            // create a synLib for each dependency and add to the root synLib
            LibraryArtifact.Dependency dep = new LibraryArtifact.Dependency(readAttribute(depElement,
                                                                     LibDeployerConstants.ARTIFACT));
            synLib.addDependency(dep);
        }

        return synLib;
    }


    /**
     * Deploys all artifacts under a root artifact..
     *
     * @param rootDirPath - root dir of the extracted artifact
     * @param library     - lib instance
     */
    private static void searchAndResolveDependencies(String rootDirPath,
                                                    SynapseLibrary library) {
        List<LibraryArtifact> libraryArtifacts = new ArrayList<LibraryArtifact>();
        File extractedDir = new File(rootDirPath);
        File[] allFiles = extractedDir.listFiles();
        if (allFiles == null) {
            return;
        }


        // search for all directories under the extracted path
        for (File artifactDirectory : allFiles) {
            if (!artifactDirectory.isDirectory()) {
                continue;
            }

            String directoryPath = formatPath(artifactDirectory.getAbsolutePath());
            String artifactXmlPath = directoryPath + File.separator + LibDeployerConstants.ARTIFACT_XML;

            File f = new File(artifactXmlPath);
            // if the artifact.xml not found, ignore this dir
            if (!f.exists()) {
                continue;
            }

            LibraryArtifact artifact = null;
            InputStream xmlInputStream = null;
            try {
                xmlInputStream = new FileInputStream(f);
                artifact = buildArtifact(library, xmlInputStream, directoryPath);
            } catch (FileNotFoundException e) {
                log.warn("Error while resolving synapse lib dir :"
                                                             + artifactDirectory.getName() +
                                                             " artifacts.xml File cannot be loaded " +
                                                             "from " + artifactXmlPath, e);
            } catch (Exception e) {
                log.warn("Error ocurred while resolving synapse lib dir :"
                                                             + artifactDirectory.getName() +
                                                             " for artifacts.xml path" + artifactXmlPath, e);
            } finally {
                if (xmlInputStream != null) {
                    try {
                        xmlInputStream.close();
                    } catch (IOException e) {
                        log.error("Error while closing input stream.", e);
                    }
                }
            }

            if (artifact == null) {
                log.warn("Could not build lib artifact for path : " + directoryPath + " Synapse Library :" +
                         library.getQName() + ". Continue searching for other lib artifacts");
                continue;

            }
            libraryArtifacts.add(artifact);
        }
        boolean isDepsResolved = library.resolveDependencies(libraryArtifacts);
        if (!isDepsResolved) {
            throw new SynapseArtifactDeploymentException("Error when resolving Dependencies for lib : " + library.toString());
        }
    }

    /**
     * Builds the artifact from the given input steam and adds it as a dependency in the provided
     *  parent Synapse library artifact
     *
     * @param library
     * @param artifactXmlStream - xml input stream of the artifact.xml
     * @param directoryPath
     * @return - Artifact instance if successfull. otherwise null..
     */
    private static LibraryArtifact buildArtifact(SynapseLibrary library, InputStream artifactXmlStream, String directoryPath) {
        LibraryArtifact artifact = null;
        try {
            OMElement artElement = new StAXOMBuilder(artifactXmlStream).getDocumentElement();

            if (LibDeployerConstants.ARTIFACT.equals(artElement.getLocalName())) {
                artifact = populateLibraryArtifact(artElement, directoryPath, null, library);
            } else {
                log.error("artifact.xml is invalid. Error occurred while resolving Synapse Library : "
                          + library.getQName());
                return null;
            }
        } catch (XMLStreamException e) {
            throw new SynapseArtifactDeploymentException("Error parsing artifact.xml for path : " +
                                                         directoryPath ,e);
        }

        if (artifact == null || artifact.getName() == null) {
            log.error("Invalid artifact found in Synapse Library : "
                      + library.getQName() );
            return null;
        }
        return artifact;
    }


    /**
     * Builds the Artifact object when an root artifact element is given . Schema for artifact.xml
     * is as follows
     * <artifact name="templates1" type="synapse/template" >

        <subArtifacts>
            <artifact name="greet_func1" >
                    <file>templ1_ns1.xml</file>
                    <description>sample synapse library artifact Description</description> ?
            </artifact> *
        </subArtifacts> *

        <description>sample synapse library artifact Description</description> ?
    </artifact>
     *
     * @param artifactEle - artifact OMElement
     * @return created Artifact object
     */
    private static LibraryArtifact populateLibraryArtifact(OMElement artifactEle, String artifactPath,
                                                          LibraryArtifact parent , SynapseLibrary library) {
        if (artifactEle == null || artifactPath == null ) {
            return null;
        }

        LibraryArtifact artifact = new LibraryArtifact(readAttribute(artifactEle, LibDeployerConstants.NAME));
        artifact.setParent(parent);
        artifact.setType(readAttribute(artifactEle, LibDeployerConstants.TYPE));
        artifact.setPath(artifactPath);

        artifact.setDescription(readChildText(artifactEle,LibDeployerConstants.DESCRIPTION_ELEMENT));
        //add a description of this artifact(if availalbe) to Synapse Library
        library.addArtifactDescription(artifact);
        // read the subArtifacts
        OMElement subArtifactsElement = artifactEle
                .getFirstChildWithName(new QName(LibDeployerConstants.SUB_ARTIFACTS));
        if (subArtifactsElement != null) {
            Iterator subArtItr = subArtifactsElement.getChildrenWithLocalName(LibDeployerConstants.ARTIFACT);
            while (subArtItr.hasNext()) {
                // as this is also an artifact, use recursion
                LibraryArtifact subArtifact = populateLibraryArtifact((OMElement) subArtItr.next(), artifactPath, artifact, library);
                artifact.addSubArtifact(subArtifact);
            }
        }

        // read and check for files
        Iterator fileItr = artifactEle.getChildrenWithLocalName(LibDeployerConstants.FILE);
        while (fileItr.hasNext()) {
            OMElement fileElement = (OMElement) fileItr.next();
            artifact.setupFile(fileElement.getText());
        }
        return artifact;
    }

    public static void loadLibArtifacts(SynapseImport synImport, Library library) {
        if (synImport.getLibName().equals(library.getQName().getLocalPart()) &&
            synImport.getLibPackage().equals(library.getPackage())) {
            library.setLibStatus(synImport.isStatus());
            library.loadLibrary();
        }
    }

    public static <T> T getLibArtifact(Map<String, Library> librarySet, String key, Class<T> type) {
        for (Library synapseLibrary : librarySet.values()) {
            try {
                T artifact = (T) synapseLibrary.getArtifact(key);
                if (artifact != null) {
                    return artifact;
                }
            } catch (Exception e) {
                //ignore
            }
        }
        return null;
    }

    public static String getQualifiedName(SynapseImport synImport){
        return new QName(synImport.getLibPackage(),synImport.getLibName()).toString();
    }


    ///////////////////////
    ////////////////// Start Common Utility Methods
    /**
     * Reads an attribute in the given element and returns the value of that attribute
     *
     * @param element - Element to search
     * @param attName - attribute name
     * @return if the attribute found, return value. else null.
     */
    public static String readAttribute(OMElement element, String attName) {
        if (element == null) {
            return null;
        }
        OMAttribute temp = element.getAttribute(new QName(attName));
        if (temp != null) {
            return temp.getAttributeValue();
        }
        return null;
    }

    public static String readChildText(OMElement element, String ln) {
        return readChildText(element, ln, null);
    }

    /**
     * Reads a text node which is in a child element of the given element and returns the text
     * value.
     *
     * @param element - Element to search
     * @param ln      - Child element name
     * @param ns      - Child element namespace
     * @return if the child text element found, return text value. else null.
     */
    public static String readChildText(OMElement element, String ln, String ns) {
        if (element == null) {
            return null;
        }
        OMElement temp = element.getFirstChildWithName(new QName(ns, ln));
        if (temp != null) {
            return temp.getText();
        }
        return null;
    }


    /**
     * Extract the Synapse Library at the provided path to the java temp dir. Return the
     * extracted location
     *
     * @param libPath - Absolute path of the Synapse Lib archive file
     * @return - extracted location
     * @throws SynapseException - error on extraction
     */
    public static String extractSynapseLib(String libPath) throws SynapseException {
        libPath = formatPath(libPath);
        String fileName = libPath.substring(libPath.lastIndexOf('/') + 1);
        String dest = APP_UNZIP_DIR + File.separator + System.currentTimeMillis() +
                      fileName + File.separator;
        createDir(dest);

        try {
            extract(libPath, dest);
        } catch (IOException e) {
            throw new SynapseException("Error while extracting Synapse Library : " + fileName, e);
        }
        return dest;
    }

    /**
     * Format the string paths to match any platform.. windows, linux etc..
     *
     * @param path - input file path
     * @return formatted file path
     */
    public static String formatPath(String path) {
        // removing white spaces
        path = path.replaceAll("\\b\\s+\\b", "%20");
        // replacing all "\" with "/"
        return path.replace('\\', '/');
    }

    private static void extract(String sourcePath, String destPath) throws IOException {
        Enumeration entries;
        ZipFile zipFile;

        zipFile = new ZipFile(sourcePath);
        entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            // we don't need to copy the META-INF dir
            if (entry.getName().startsWith("META-INF/")) {
                continue;
            }
            // if the entry is a directory, create a new dir
            if (entry.isDirectory()) {
                createDir(destPath + entry.getName());
                continue;
            }
            // if the entry is a file, write the file
            copyInputStream(zipFile.getInputStream(entry),
                            new BufferedOutputStream(new FileOutputStream(destPath + entry.getName())));
        }
        zipFile.close();
    }


    public static void createDir(String path) {
        File temp = new File(path);
        if (!temp.exists() && !temp.mkdir()) {
            log.error("Error while creating directory : " + path);
        }
    }
    
    
    public static void deployingLocalEntries(Library library,SynapseConfiguration config) {
		Properties properties = SynapsePropertiesLoader.loadSynapseProperties();
		for (Map.Entry<String, Object> libararyEntryMap : library.getLocalEntryArtifacts()
				.entrySet()) {
			File localEntryFileObj = (File) libararyEntryMap.getValue();
			OMElement document = getOMElement(localEntryFileObj);
			try {
			     SynapseXMLConfigurationFactory.defineEntry(config,
						document, properties,library);
			} catch (Exception ex) {
				handleDeploymentError("Error while deploying local entries", ex);
			}
		}

	}
    
    private static void handleDeploymentError(String msg, Exception e){
        log.error(msg, e);
    }
    
    
    private static OMElement getOMElement(File file) {
        FileInputStream is;
        OMElement document = null;

        try {
            is = FileUtils.openInputStream(file);
        } catch (IOException e) {
            handleException("Error while opening the file: " + file.getName() + " for reading", e);
            return null;
        }

        try {
            document = new StAXOMBuilder(is).getDocumentElement();
            document.build();
            is.close();
        } catch (XMLStreamException e) {
            handleException("Error while parsing the content of the file: " + file.getName(), e);
        } catch (IOException e) {
            log.warn("Error while closing the input stream from the file: " + file.getName(), e);
        }

        return document;
    }
    
    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[40960];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    /////////////////// End Of Common Utility Methods


    public static void main(String[] args) {
        new SynapseLibrary(null, null).resolveDependencies(null);
    }
}
