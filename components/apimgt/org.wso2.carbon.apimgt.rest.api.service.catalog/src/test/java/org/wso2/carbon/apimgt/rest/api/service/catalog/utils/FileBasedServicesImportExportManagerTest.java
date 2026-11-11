/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.ExportArchive;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Tests for creating export archives in {@link FileBasedServicesImportExportManager}.
 * An export archive must always be created inside the given archive location.
 */
public class FileBasedServicesImportExportManagerTest {

    private static final String METADATA_FILE = "metadata.yaml";
    private static final String DEFINITION_FILE = "definition.yaml";
    private static final String OUTSIDE_ARCHIVE = "outside-1.0.0.zip";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // <root>/source       : service files to be archived
    // <root>/work/export  : export directory (archive location)
    private File sourceDir;
    private File exportDir;

    @Before
    public void setUp() throws IOException {
        sourceDir = tempFolder.newFolder("source");
        Files.write(new File(sourceDir, METADATA_FILE).toPath(),
                "name: TestSvc\nversion: 1.0.0\n".getBytes(StandardCharsets.UTF_8));
        Files.write(new File(sourceDir, DEFINITION_FILE).toPath(),
                "openapi: 3.0.1\n".getBytes(StandardCharsets.UTF_8));
        exportDir = tempFolder.newFolder("work", "export");
    }

    @Test
    public void testArchiveIsCreatedInsideArchiveLocation() throws Exception {
        ExportArchive exportArchive = export(exportDir, "TestSvc-1.0.0");

        File archive = new File(exportArchive.getArchiveName());
        Assert.assertTrue("Archive should be created", archive.isFile());
        Assert.assertEquals("TestSvc-1.0.0.zip", archive.getName());
        Assert.assertEquals("Archive should be created inside the archive location",
                exportDir.getCanonicalFile(), archive.getCanonicalFile().getParentFile());
        Assert.assertEquals(listOf(DEFINITION_FILE, METADATA_FILE), zipEntryNames(archive));
    }

    @Test
    public void testArchiveNameWithConsecutiveDotsIsAllowed() throws Exception {
        ExportArchive exportArchive = export(exportDir, "my..svc-1.0.0");

        File archive = new File(exportArchive.getArchiveName());
        Assert.assertTrue("Archive should be created", archive.isFile());
        Assert.assertEquals(exportDir.getCanonicalFile(), archive.getCanonicalFile().getParentFile());
    }

    @Test
    public void testArchiveLocationAccessedThroughSymbolicLinkIsAllowed() throws Exception {
        File link = new File(tempFolder.getRoot(), "export-link");
        try {
            Files.createSymbolicLink(link.toPath(), exportDir.toPath());
        } catch (IOException | UnsupportedOperationException e) {
            Assume.assumeNoException("Symbolic links are not supported on this platform", e);
        }

        export(link, "TestSvc-1.0.0");

        Assert.assertTrue("Archive should be created in the linked export directory",
                new File(exportDir, "TestSvc-1.0.0.zip").isFile());
    }

    @Test
    public void testArchiveNameResolvingToParentDirectoryIsRejected() throws Exception {
        assertExportRejected("../outside-1.0.0");
    }

    @Test
    public void testArchiveNameResolvingAboveParentDirectoryIsRejected() throws Exception {
        assertExportRejected("../../outside-1.0.0");
    }

    @Test
    public void testArchiveNameResolvingToSiblingDirectoryWithSamePrefixIsRejected() throws Exception {
        // The sibling directory name starts with the export directory name, so the location check must
        // compare whole path segments rather than string prefixes.
        File sibling = new File(exportDir.getParentFile(), exportDir.getName() + "-other");
        Assert.assertTrue(sibling.mkdir());

        assertExportRejected("../" + sibling.getName() + "/outside-1.0.0");
    }

    private void assertExportRejected(String archiveName) throws IOException {
        Assert.assertThrows("Export should fail for archive name " + archiveName, APIManagementException.class,
                () -> export(exportDir, archiveName));
        Assert.assertEquals("No archive should be created for archive name " + archiveName,
                Collections.emptyList(), findFiles(tempFolder.getRoot(), OUTSIDE_ARCHIVE));
    }

    private ExportArchive export(File archiveLocation, String archiveName) throws APIManagementException {
        FileBasedServicesImportExportManager manager =
                new FileBasedServicesImportExportManager(archiveLocation.getPath());
        return manager.createArchiveFromExportedServices(sourceDir.getPath(), archiveLocation.getPath(),
                archiveName);
    }

    private static List<String> findFiles(File root, String fileName) throws IOException {
        try (Stream<Path> paths = Files.walk(root.toPath())) {
            return paths.filter(p -> p.getFileName().toString().equals(fileName))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    private static List<String> zipEntryNames(File archive) throws IOException {
        List<String> names = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(archive)) {
            for (ZipEntry entry : Collections.list(zipFile.entries())) {
                names.add(entry.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    private static List<String> listOf(String... values) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }
}
