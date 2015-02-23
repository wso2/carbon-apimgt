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
import org.apache.synapse.SynapseArtifact;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.TemplateFactory;
import org.apache.synapse.deployers.SynapseArtifactDeploymentException;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.mediators.template.TemplateMediator;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LibraryArtifact implements SynapseArtifact{

    protected String name;
    protected String type;
    protected String description;

    String extractedPath;
    ArtifactFile file;
    int unresolvedDeps = 0;

    Map<String, LibraryArtifact> subArtifacts;

    private LibraryArtifact parent;


    public LibraryArtifact(String  name) {
        this.name = name;
        subArtifacts = new HashMap<String, LibraryArtifact>();
    }


    public void setupFile(String filename) {
        if (filename != null && !"".equals(filename)) {
            if ("synapse/template".equals(getArtifactType())) {
                file = this.new TemplateArtifactFile(filename);
            } else {
                throw new SynapseArtifactDeploymentException("Unsupported Type for synapse lib artifact.");
            }
        } else {
            throw new SynapseArtifactDeploymentException("Invalid file specified for lib artifact.");
        }
    }

    private String getArtifactType() {
        if (type != null) {
            return type;
        } else if (parent != null) {
            return parent.getArtifactType();
        }
        return "";
    }

    public void addSubArtifact(LibraryArtifact artifact) {
        if (artifact != null) {
            subArtifacts.put(artifact.toString(), artifact);
            unresolvedDeps++;
        }
    }

    public String  getName() {
        return name;
    }

    public boolean isLeafArtifact() {
        return file == null ? false : true;
    }

    public void loadComponentsInto(SynapseLibrary library) {
        for (String artifactName : subArtifacts.keySet()) {
            LibraryArtifact artifact = subArtifacts.get(artifactName);
            if (artifact.isLeafArtifact()) {
                delegateClassLoading(artifact, library);
                //this is where actual artifact is constructed to it's ture form
                Object template = artifact.file.build();
                if (artifact.file instanceof TemplateArtifactFile) {

                    if (template instanceof TemplateMediator) {
                        TemplateMediator templateMediator = (TemplateMediator) template;
                        //make template dynamic as it is not directly available to synapse environment
                        templateMediator.setDynamic(true);
                        String templateName = templateMediator.getName();
                        library.addComponent(getQualifiedName(library.getPackage(),
                                                              templateName,
                                                              library.getQName().getLocalPart()),
                                             template);
                    } else if (template instanceof Template) {
                        String templateName = ((Template) template).getName();
                        library.addComponent(getQualifiedName(library.getPackage(),
                                                              templateName,
                                                              library.getQName().getLocalPart()),
                                             template);
                    } else if (template != null) {
                        library.addComponent(getQualifiedName(library.getPackage(),
                                                              artifact.getName(),
                                                              library.getQName().getLocalPart()),
                                             template);
                    } else {
                        throw new SynapseArtifactDeploymentException("Cannot load components into " +
                                                                     "Synapse Library. Component " +
                                                                     "cannot be built for " + artifactName);
                    }
                }
            } else {
                artifact.loadComponentsInto(library);
            }
        }
    }

    private void delegateClassLoading(LibraryArtifact artifact, SynapseLibrary library) {
        Properties classLoadingProperties = new Properties();
        classLoadingProperties.put(SynapseConstants.SYNAPSE_LIB_LOADER, library.getLibClassLoader());
        artifact.file.setProperties(classLoadingProperties);
    }

    private String getQualifiedName(String aPackage, String templateName,String parentArtifact) {
        return aPackage+"."+parentArtifact+ "." + templateName;
    }

    public void setPath(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        this.extractedPath = path;
    }

    public String toString() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParent(LibraryArtifact parent) {
        this.parent = parent;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private class TemplateArtifactFile extends ArtifactFile {
        public TemplateArtifactFile(String fileXmlPath) {
            super(extractedPath + fileXmlPath);
        }

        @Override
        public Object build() {
            Object templateObject = null;
            OMElement element = configurationElement.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "sequence"));
            if (element != null) {
                String name = element.getAttributeValue(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
                try {
                    templateObject = MediatorFactoryFinder.getInstance().
                            getMediator(configurationElement, properties);
                } catch (Exception e) {
                    String msg = "Template configuration : " + name + " cannot be built" +
                            "for Synapse Library artifact : " + LibraryArtifact.this.name;;
//                        handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_TEMPLATES, msg, e);
                    throw new SynapseArtifactDeploymentException(msg,e);
                }
                return templateObject;
            } else {
                element = configurationElement.getFirstChildWithName(
                        new QName(SynapseConstants.SYNAPSE_NAMESPACE, "endpoint"));
                if (element != null) {
                    TemplateFactory templateFactory = new TemplateFactory();
                    String name = element.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE,
                                                                      "name"));
                    try {
                        templateObject = templateFactory.createEndpointTemplate(configurationElement,
                                                                                properties);
                    } catch (Exception e) {
                        String msg = "Endpoint Template: " + name + "configuration cannot be built " +
                                     "for Synapse Library artifact : " + LibraryArtifact.this.name;
//                        handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_TEMPLATES, msg, e);
                        throw new SynapseArtifactDeploymentException(msg,e);
                    }
                }
            }
            return templateObject;
        }

    }

    public static class Dependency {

        private String name;
        boolean markAsResolved = false;

        public Dependency(String  name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean resolveWith(LibraryArtifact artifact) {
            return markAsResolved = name.equals(artifact.name);
        }

        public boolean isResolved() {
            return markAsResolved;
        }
    }

}
