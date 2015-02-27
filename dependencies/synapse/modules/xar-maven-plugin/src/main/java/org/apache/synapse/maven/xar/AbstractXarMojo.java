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

package org.apache.synapse.maven.xar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * Abstract base class for all the mojos in the XAR plugin.
 */
public abstract class AbstractXarMojo extends AbstractMojo implements LogEnabled {
    /**
     * List of dependencies to be excluded by default because the corresponding APIs are provided
     * by the Synapse runtime.
     */
    private static final String[] defaultRuntimeExcludes = {
        "org.apache.synapse:synapse-core:jar",
        "commons-logging:commons-logging-api:jar",
    }; 
    
    private static final String[] serviceClassNames = {
        "org.apache.synapse.config.xml.MediatorFactory",
        "org.apache.synapse.config.xml.MediatorSerializer",
        "org.apache.synapse.config.xml.StartupFactory",
    };
    
    /**
     * The projects base directory.
     *
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File baseDir;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Local maven repository.
     * 
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
    
    /**
     * Remote repositories.
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteArtifactRepositories;
    
    /**
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource" hint="maven"
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * Artifact collector, needed to resolve dependencies.
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactCollector"
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;
    
    /**
     * Project builder.
     * 
     * @component role="org.apache.maven.project.MavenProjectBuilder"
     * @required
     * @readonly
     */
    private MavenProjectBuilder projectBuilder;
    
    /**
     * Artifact factory.
     * 
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;
    
    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File buildOutputDirectory;

    /**
     * The directory where temporary files for inclusion in the XAR are stored.
     *
     * @parameter expression="${project.build.directory}/xar-files"
     * @required
     */
    private File tmpDirectory;

    /**
     * Whether the dependency jars should be included in the XAR.
     *
     * @parameter expression="${includeDependencies}" default-value="true"
     */
    private boolean includeDependencies;
    
    /**
     * Whether metadata for the extensions should be generated automatically.
     * 
     * @parameter expression="${generateMetadata}" default-value="true"
     */
    private boolean generateMetadata;
    
    private Logger logger;
    
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Build the XAR using the provided archiver.
     *
     * @throws MojoExecutionException
     */
    protected void buildArchive(Archiver archiver)
            throws ArchiverException, MojoExecutionException {
        
        Log log = getLog();
        log.debug("Using base directory: " + baseDir);
        archiver.addDirectory(buildOutputDirectory);
        if (includeDependencies) {
            log.debug("Adding dependencies ...");
            addDependencies(archiver);
        }
        if (generateMetadata) {
            log.debug("Generating XAR metadata ...");
            generateMetadata(archiver);
        }
    }
    
    private void addDependencies(Archiver archiver)
            throws ArchiverException, MojoExecutionException {
        
        Log log = getLog();
        AndArtifactFilter filter = new AndArtifactFilter();
        filter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));
        filter.add(new ArtifactFilter() {
            public boolean include(Artifact artifact) {
                return !artifact.isOptional();
            }
        });
        filter.add(new TypeArtifactFilter("jar"));
        filter.add(buildSynapseRuntimeArtifactFilter());
        for (Artifact artifact : filterArtifacts(project.getArtifacts(), filter)) {
            String targetFileName = artifact.getArtifactId() + "-" + artifact.getVersion() + "." +
                    artifact.getArtifactHandler().getExtension();
            log.info("Adding " + targetFileName + " (scope " + artifact.getScope() + ")");
            archiver.addFile(artifact.getFile(), "lib/" + targetFileName);
        }
    }
    
    private void generateMetadata(Archiver archiver)
            throws ArchiverException, MojoExecutionException {
        
        Log log = getLog();
        File tmpServicesDir = new File(new File(tmpDirectory, "META-INF"), "services");
        File buildServicesDir = new File(new File(buildOutputDirectory, "META-INF"), "services");
        if (!tmpServicesDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating the directory: " +
                    tmpServicesDir.getPath());
        }
        
        log.debug("Initializing class scanner ...");
        ClassScanner scanner = new ClassScanner(buildOutputDirectory);
        for (Artifact artifact : filterArtifacts(project.getArtifacts(),
                new ScopeArtifactFilter(Artifact.SCOPE_COMPILE))) {
            scanner.addToClasspath(artifact.getFile());
        }
        List<ServiceLocator> serviceLocators =
            new ArrayList<ServiceLocator>(serviceClassNames.length);
        for (String serviceClassName : serviceClassNames) {
            // If the user provided its own service file, skip generation
            File file = new File(buildServicesDir, serviceClassName);
            if (file.exists()) {
                log.debug(file + " exists; don't scan for " + serviceClassName +
                        " implementation");
            } else {
                ServiceLocator sl = new ServiceLocator(serviceClassName);
                serviceLocators.add(sl);
                scanner.addVisitor(sl);
            }
        }
        try {
            scanner.scan();
        } catch (ClassScannerException e) {
            throw new MojoExecutionException("Failed to scan classes for services", e);
        }
        for (ServiceLocator sl : serviceLocators) {
            File file = new File(tmpServicesDir, sl.getServiceClassName());
            if (!sl.getImplementations().isEmpty()) {
                String destFileName = "META-INF/services/" + sl.getServiceClassName();
                log.info("Generating " + destFileName);
                try {
                    Writer out = new OutputStreamWriter(new FileOutputStream(file));
                    try {
                        for (String impl : sl.getImplementations()) {
                            log.debug("  " + impl);
                            out.write(impl);
                            out.write("\n");
                        }
                    } finally {
                        out.close();
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to create temporary file " + file, e);
                }
                archiver.addFile(file, destFileName);
            }
        }
    }
    
    /**
     * Build a filter that excludes all artifacts that are provided by Synapse at runtime.
     * 
     * @return
     * @throws MojoExecutionException
     */
    private ArtifactFilter buildSynapseRuntimeArtifactFilter() throws MojoExecutionException {
        final Map<String,Artifact> artifacts = new HashMap<String,Artifact>();
        for (Artifact artifact : getSynapseRuntimeArtifacts()) {
            artifacts.put(artifact.getDependencyConflictId(), artifact);
        }
        final Set<String> defaultExclusionSet
                = new HashSet<String>(Arrays.asList(defaultRuntimeExcludes));
        return new ArtifactFilter() {
            public boolean include(Artifact artifact) {
                Artifact runtimeArtifact = artifacts.get(artifact.getDependencyConflictId());
                if (runtimeArtifact == null) {
                    return !defaultExclusionSet.contains(artifact.getDependencyConflictId());
                } else {
                    if (!runtimeArtifact.getVersion().equals(artifact.getVersion())) {
                        getLog().warn("Possible runtime version conflict for "
                                + artifact.getArtifactId() + ": XAR depends on "
                                + artifact.getVersion() + ", Synapse runtime provides "
                                + runtimeArtifact.getVersion());
                    }
                    return false;
                }
            }
        };
    }
    
    /**
     * Get the set of artifacts that are provided by Synapse at runtime.
     * 
     * @return
     * @throws MojoExecutionException
     */
    private Set<Artifact> getSynapseRuntimeArtifacts() throws MojoExecutionException {
        Log log = getLog();
        log.debug("Looking for synapse-core artifact in XAR project dependencies ...");
        Artifact synapseCore = null;
        for (Iterator<?> it = project.getDependencyArtifacts().iterator(); it.hasNext(); ) {
            Artifact artifact = (Artifact)it.next();
            if (artifact.getGroupId().equals("org.apache.synapse")
                    && artifact.getArtifactId().equals("synapse-core")) {
                synapseCore = artifact;
                break;
            }
        }
        if (synapseCore == null) {
            throw new MojoExecutionException("Could not locate dependency on synapse-core");
        }
        
        log.debug("Loading project data for " + synapseCore + " ...");
        MavenProject synapseCoreProject;
        try {
            synapseCoreProject = projectBuilder.buildFromRepository(synapseCore,
                    remoteArtifactRepositories, localRepository);
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("Unable to retrieve project information for "
                    + synapseCore, e);
        }
        Set<Artifact> synapseRuntimeDeps;
        try {
            synapseRuntimeDeps = synapseCoreProject.createArtifacts(artifactFactory,
                    Artifact.SCOPE_RUNTIME, new TypeArtifactFilter("jar"));
        } catch (InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Unable to get project dependencies for "
                    + synapseCore, e);
        }
        log.debug("Direct runtime dependencies for " + synapseCore + " :");
        logArtifacts(synapseRuntimeDeps);
        
        log.debug("Resolving transitive dependencies for " + synapseCore + " ...");
        try {
            synapseRuntimeDeps = artifactCollector.collect(synapseRuntimeDeps,
                    synapseCoreProject.getArtifact(), synapseCoreProject.getManagedVersionMap(),
                    localRepository, remoteArtifactRepositories, artifactMetadataSource, null,
                    Collections.singletonList(new DebugResolutionListener(logger))).getArtifacts();
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Unable to resolve transitive dependencies for "
                    + synapseCore);
        }
        log.debug("All runtime dependencies for " + synapseCore + " :");
        logArtifacts(synapseRuntimeDeps);
        
        return synapseRuntimeDeps;
    }
    
    private void logArtifacts(Collection<Artifact> collection) {
        List<Artifact> artifacts = new ArrayList<Artifact>(collection);
        Collections.sort(artifacts, new Comparator<Artifact>() {
            public int compare(Artifact o1, Artifact o2) {
                return o1.getArtifactId().compareTo(o2.getArtifactId());
            }
        });
        for (Artifact artifact : artifacts) {
            getLog().debug("  " + artifact.getArtifactId() + "-" + artifact.getVersion()
                    + "." + artifact.getArtifactHandler().getExtension());
        }
    }
    
    private static Set<Artifact> filterArtifacts(Set<Artifact> artifacts, ArtifactFilter filter) {
        Set<Artifact> result = new HashSet<Artifact>();
        for (Artifact artifact : artifacts) {
            if (filter.include(artifact)) {
                result.add(artifact);
            }
        }
        return result;
    }
}
