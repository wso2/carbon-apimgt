/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.persistence;

import static org.mockito.ArgumentMatchers.anyString;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.DocumentationType;
import org.wso2.carbon.apimgt.persistence.exceptions.DocumentationPersistenceException;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceDocUtil;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;

/**
 * Tests for the private {@code deleteDocumentationContent} helper of {@link RegistryPersistenceImpl}, which
 * removes the separate registry resource backing FILE and INLINE/MARKDOWN documentation content (URL documents
 * have no such resource). The helper is invoked via reflection since it is private; {@link RegistryPersistenceDocUtil}
 * is static-mocked so each case can drive the branch under test directly, without needing a real registry artifact.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ RegistryPersistenceDocUtil.class })
public class RegistryPersistenceDocumentContentDeletionTestCase {

    private static final String API_ID = "admin-TestAPI-1.0.0";
    private static final String DOC_ID = "doc-id-1234";
    private static final String DOC_NAME = "TestDoc";
    private static final String METHOD_NAME = "deleteDocumentationContent";

    private RegistryPersistenceImpl persistence;
    private Registry registry;
    private GenericArtifact artifact;

    @Before
    public void setUp() {
        persistence = new RegistryPersistenceImpl();
        registry = Mockito.mock(Registry.class);
        artifact = Mockito.mock(GenericArtifact.class);
        PowerMockito.mockStatic(RegistryPersistenceDocUtil.class);
    }

    private Documentation newDocumentation(Documentation.DocumentSourceType sourceType) {
        Documentation documentation = new Documentation(DocumentationType.HOWTO, DOC_NAME);
        documentation.setSourceType(sourceType);
        return documentation;
    }

    private void mockDocumentation(Documentation documentation) throws Exception {
        PowerMockito.when(RegistryPersistenceDocUtil.getDocumentation(artifact)).thenReturn(documentation);
    }

    private void invokeDeleteContent() throws Exception {
        Whitebox.invokeMethod(persistence, METHOD_NAME, registry, artifact, API_ID, DOC_ID);
    }

    // =====================================================================
    // FILE source type
    // =====================================================================

    @Test
    public void testDeleteDocumentationContent_FileTypeResourceExists() throws Exception {
        String contentSuffix = "/apimgt/applicationdata/admin/apis/TestAPI1.0.0/docs/sample.txt";
        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.FILE);
        documentation.setFilePath(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + contentSuffix);
        mockDocumentation(documentation);
        Mockito.when(registry.resourceExists(contentSuffix)).thenReturn(true);

        invokeDeleteContent();

        Mockito.verify(registry, Mockito.times(1)).delete(contentSuffix);
    }

    @Test
    public void testDeleteDocumentationContent_FileTypeResourceMissing() throws Exception {
        String contentSuffix = "/apimgt/applicationdata/admin/apis/TestAPI1.0.0/docs/sample.txt";
        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.FILE);
        documentation.setFilePath(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + contentSuffix);
        mockDocumentation(documentation);
        Mockito.when(registry.resourceExists(anyString())).thenReturn(false);

        invokeDeleteContent();

        Mockito.verify(registry, Mockito.never()).delete(anyString());
    }

    @Test(expected = DocumentationPersistenceException.class)
    public void testDeleteDocumentationContent_FileTypeNullFilePath() throws Exception {
        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.FILE);
        documentation.setFilePath(null);
        mockDocumentation(documentation);

        invokeDeleteContent();
    }

    @Test(expected = DocumentationPersistenceException.class)
    public void testDeleteDocumentationContent_FileTypeInvalidResourcePath() throws Exception {
        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.FILE);
        documentation.setFilePath("/some/unrelated/path/sample.txt");
        mockDocumentation(documentation);

        invokeDeleteContent();
    }

    // =====================================================================
    // INLINE / MARKDOWN source types
    // =====================================================================

    @Test
    public void testDeleteDocumentationContent_InlineTypeResourceExists() throws Exception {
        String artifactPath = "/_system/governance/apimgt/applicationdata/admin/apis/TestAPI1.0.0/documentation/"
                + DOC_NAME;
        String expectedContentPath = "/_system/governance/apimgt/applicationdata/admin/apis/TestAPI1.0.0/documentation"
                + RegistryConstants.PATH_SEPARATOR + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                + RegistryConstants.PATH_SEPARATOR + DOC_NAME;

        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.INLINE);
        mockDocumentation(documentation);
        Mockito.when(artifact.getPath()).thenReturn(artifactPath);
        Mockito.when(registry.resourceExists(expectedContentPath)).thenReturn(true);

        invokeDeleteContent();

        Mockito.verify(registry, Mockito.times(1)).delete(expectedContentPath);
    }

    @Test
    public void testDeleteDocumentationContent_MarkdownTypeResourceExists() throws Exception {
        String artifactPath = "/_system/governance/apimgt/applicationdata/admin/apis/TestAPI1.0.0/documentation/"
                + DOC_NAME;
        String expectedContentPath = "/_system/governance/apimgt/applicationdata/admin/apis/TestAPI1.0.0/documentation"
                + RegistryConstants.PATH_SEPARATOR + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                + RegistryConstants.PATH_SEPARATOR + DOC_NAME;

        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.MARKDOWN);
        mockDocumentation(documentation);
        Mockito.when(artifact.getPath()).thenReturn(artifactPath);
        Mockito.when(registry.resourceExists(expectedContentPath)).thenReturn(true);

        invokeDeleteContent();

        Mockito.verify(registry, Mockito.times(1)).delete(expectedContentPath);
    }

    /**
     * Regression test: the document name here ("docs") also occurs as an earlier path segment (the provider name).
     * A naive String.replace("/docs", "") would strip both occurrences and corrupt the base path; only the
     * trailing occurrence (the actual doc name segment) must be stripped.
     */
    @Test
    public void testDeleteDocumentationContent_InlineTypeDocNameCollidesWithPathSegment() throws Exception {
        String docName = "docs";
        String artifactPath = "/_system/governance/apimgt/applicationdata/docs/docsAPI/1.0.0/documentation/" + docName;
        String expectedContentPath = "/_system/governance/apimgt/applicationdata/docs/docsAPI/1.0.0/documentation"
                + RegistryConstants.PATH_SEPARATOR + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                + RegistryConstants.PATH_SEPARATOR + docName;

        Documentation documentation = new Documentation(DocumentationType.HOWTO, docName);
        documentation.setSourceType(Documentation.DocumentSourceType.INLINE);
        mockDocumentation(documentation);
        Mockito.when(artifact.getPath()).thenReturn(artifactPath);
        Mockito.when(registry.resourceExists(anyString())).thenReturn(true);

        invokeDeleteContent();

        Mockito.verify(registry, Mockito.times(1)).delete(expectedContentPath);
    }

    @Test(expected = DocumentationPersistenceException.class)
    public void testDeleteDocumentationContent_InlineTypeNullArtifactPath() throws Exception {
        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.INLINE);
        mockDocumentation(documentation);
        Mockito.when(artifact.getPath()).thenReturn(null);

        invokeDeleteContent();
    }

    @Test(expected = DocumentationPersistenceException.class)
    public void testDeleteDocumentationContent_InlineTypeMissingDocNameSegment() throws Exception {
        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.INLINE);
        mockDocumentation(documentation);
        Mockito.when(artifact.getPath())
                .thenReturn("/_system/governance/apimgt/applicationdata/admin/apis/TestAPI1.0.0/documentation/"
                        + "SomeOtherDocName");

        invokeDeleteContent();
    }

    // =====================================================================
    // URL source type
    // =====================================================================

    @Test
    public void testDeleteDocumentationContent_UrlTypeNoContent() throws Exception {
        Documentation documentation = newDocumentation(Documentation.DocumentSourceType.URL);
        documentation.setSourceUrl("https://example.org/doc");
        mockDocumentation(documentation);

        invokeDeleteContent();

        Mockito.verify(registry, Mockito.never()).delete(anyString());
        Mockito.verify(registry, Mockito.never()).resourceExists(anyString());
    }
}
