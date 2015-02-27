/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.maven.xar;

import java.io.File;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * Build a XAR.
 *
 * @goal xar
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class XarMojo extends AbstractXarMojo {
    /**
     * The directory for the generated XAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * The name of the generated XAR.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String xarName;

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     */
    private JarArchiver jarArchiver;

    /**
     * The maven archive configuration to use.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment
     * instead.
     *
     * @parameter
     */
    private String classifier;

    /**
     * Whether this is the main artifact being built. Set to <code>false</code> if you don't want to
     * install or deploy it to the local repository instead of the default one in an execution.
     *
     * @parameter expression="${primaryArtifact}" default-value="true"
     */
    private boolean primaryArtifact;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * Executes the XarMojo on the current project.
     *
     * @throws MojoExecutionException if an error occurred while building the XAR
     */
    public void execute() throws MojoExecutionException {

        File xarFile = new File(outputDirectory, xarName + ".xar");

        // generate xar file
        getLog().info("Generating XAR " + xarFile.getAbsolutePath());
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(xarFile);
        try {
            buildArchive(jarArchiver);
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Unable to build archive", e);
        }

        // create archive
        try {
            archiver.createArchive(project, archive);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to create archive", e);
        }

        if (classifier != null) {
            projectHelper.attachArtifact(project, "xar", classifier, xarFile);
        } else {
            Artifact artifact = project.getArtifact();
            if (primaryArtifact) {
                artifact.setFile(xarFile);
            } else if (artifact.getFile() == null || artifact.getFile().isDirectory()) {
                artifact.setFile(xarFile);
            } else {
                projectHelper.attachArtifact(project, "xar", xarFile);
            }
        }
    }
}
