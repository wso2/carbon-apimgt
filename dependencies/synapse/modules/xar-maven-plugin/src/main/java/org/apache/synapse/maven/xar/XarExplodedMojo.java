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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.dir.DirectoryArchiver;

/**
 * Generate the exploded XAR.
 *
 * @goal exploded
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class XarExplodedMojo extends AbstractXarMojo {
    /**
     * The directory where the exploded XAR is built.
     *
     * @parameter expression="${project.build.directory}/xar"
     * @required
     */
    private File xarDirectory;
    
    public void execute() throws MojoExecutionException {
        Archiver archiver = new DirectoryArchiver();
        archiver.setDestFile(xarDirectory);
        try {
            buildArchive(archiver);
            archiver.createArchive();
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Unable to build archive", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to build archive", e);
        }
    }
}
