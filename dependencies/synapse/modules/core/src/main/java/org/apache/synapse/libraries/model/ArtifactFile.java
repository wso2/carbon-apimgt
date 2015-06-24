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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.deployers.SynapseArtifactDeploymentException;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Properties;

public abstract class ArtifactFile {

    private static final Log log = LogFactory.getLog(ArtifactFile.class);

    protected static String fileName;

    protected OMElement configurationElement;

    public abstract Object build();

    protected Properties properties;

    public ArtifactFile(String fileXmlPath){
        fileName = fileXmlPath;
        File f = new File(fileXmlPath);
        if (!f.exists()) {
            throw new SynapseArtifactDeploymentException("file not found at : " + fileXmlPath);
        }
        InputStream xmlInputStream = null;
        try {
            xmlInputStream = new FileInputStream(f);
            configurationElement = new StAXOMBuilder(xmlInputStream).getDocumentElement();
            configurationElement.build();
        } catch (FileNotFoundException e) {
               throw new SynapseArtifactDeploymentException("file not found at : " + fileXmlPath);
        } catch (XMLStreamException e) {
            throw new SynapseArtifactDeploymentException("Error while parsing the artifacts.xml file : " + fileXmlPath , e);
        } finally {
            if (xmlInputStream != null) {
                try {
                    xmlInputStream.close();
                } catch (IOException e) {
                    log.error("Error while closing input stream for file artifact.", e);
                }
            }
        }
    }

    public OMElement getConfigurationElement(){
        return configurationElement;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
