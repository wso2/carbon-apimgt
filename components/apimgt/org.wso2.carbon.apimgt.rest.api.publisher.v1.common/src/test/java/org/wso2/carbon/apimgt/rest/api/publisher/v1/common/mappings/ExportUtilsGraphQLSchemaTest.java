/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Covers the GraphQL branch of {@link ExportUtils#addAPIMetaInformationToArchive}. A GraphQL API whose schema
 * was never stored (for example one created through {@code POST /apis}) has a {@code null} schema; export, and
 * revision creation which exports internally, must not fail on it.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIMappingUtil.class})
public class ExportUtilsGraphQLSchemaTest {

    private static final String API_UUID = "9bcc22df-810d-4998-b230-95fa43ae0e99";
    private static final String ORGANIZATION = "carbon.super";
    private static final String SCHEMA = "type Query { hello: String }";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private APIProvider apiProvider;
    private APIIdentifier apiIdentifier;
    private String archivePath;

    @Before
    public void init() throws Exception {

        apiProvider = Mockito.mock(APIProvider.class);
        apiIdentifier = new APIIdentifier("admin", "GraphQLAPI", "1.0.0");
        archivePath = temporaryFolder.newFolder("archive").getAbsolutePath();

        PowerMockito.mockStatic(APIMappingUtil.class);
        PowerMockito.when(APIMappingUtil.fromDTOtoAPI(Mockito.any(APIDTO.class), Mockito.anyString()))
                .thenReturn(new API(apiIdentifier));
        Mockito.when(apiProvider.getComplexityDetails(API_UUID)).thenReturn(new GraphqlComplexityInfo());
    }

    private APIDTO graphQLApiDto() {

        APIDTO apiDto = new APIDTO();
        apiDto.setName("GraphQLAPI");
        apiDto.setVersion("1.0.0");
        apiDto.setProvider("admin");
        apiDto.setType(APIDTO.TypeEnum.GRAPHQL);
        return apiDto;
    }

    private File schemaFile() {

        return new File(archivePath + ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION);
    }

    // java.io only: java.nio.file.Files cannot be reflectively opened by PowerMock's class loader on JDK 17+
    private String readFile(File file) throws IOException {

        try (InputStream in = new FileInputStream(file);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private File apiFile() {

        return new File(archivePath + ImportExportConstants.API_FILE_LOCATION + ".yaml");
    }

    @Test
    public void testExportGraphQLApiWithNullSchemaDoesNotThrowAndWritesNoSchemaFile() throws Exception {

        Mockito.when(apiProvider.getGraphqlSchemaDefinition(API_UUID, ORGANIZATION)).thenReturn(null);

        ExportUtils.addAPIMetaInformationToArchive(archivePath, new APIDTOTypeWrapper(graphQLApiDto()),
                ExportFormat.YAML, apiProvider, apiIdentifier, ORGANIZATION, API_UUID);

        Assert.assertFalse("No schema file should be written when no schema is stored", schemaFile().exists());
        Assert.assertTrue("The API metadata must still be exported", apiFile().exists());
    }

    @Test
    public void testExportGraphQLApiWithSchemaWritesSchemaFile() throws Exception {

        Mockito.when(apiProvider.getGraphqlSchemaDefinition(API_UUID, ORGANIZATION)).thenReturn(SCHEMA);

        ExportUtils.addAPIMetaInformationToArchive(archivePath, new APIDTOTypeWrapper(graphQLApiDto()),
                ExportFormat.YAML, apiProvider, apiIdentifier, ORGANIZATION, API_UUID);

        Assert.assertTrue("The schema file should be written", schemaFile().exists());
        Assert.assertEquals(SCHEMA, readFile(schemaFile()));
        Assert.assertTrue(apiFile().exists());
    }

    @Test
    public void testExportGraphQLApiWithEmptySchemaStillWritesSchemaFile() throws Exception {

        // The guard is for "no schema stored" (null) only; an empty string keeps the previous behaviour.
        Mockito.when(apiProvider.getGraphqlSchemaDefinition(API_UUID, ORGANIZATION)).thenReturn("");

        ExportUtils.addAPIMetaInformationToArchive(archivePath, new APIDTOTypeWrapper(graphQLApiDto()),
                ExportFormat.YAML, apiProvider, apiIdentifier, ORGANIZATION, API_UUID);

        Assert.assertTrue(schemaFile().exists());
        Assert.assertEquals(0, schemaFile().length());
    }

    @Test
    public void testExportGraphQLApiWithNullSchemaAndNoOrganizationUsesTenantDomain() throws Exception {

        // organization == null takes the tenant-domain branch; the null schema must be tolerated there too
        Mockito.when(apiProvider.getGraphqlSchemaDefinition(Mockito.eq(API_UUID), Mockito.anyString()))
                .thenReturn(null);

        ExportUtils.addAPIMetaInformationToArchive(archivePath, new APIDTOTypeWrapper(graphQLApiDto()),
                ExportFormat.YAML, apiProvider, apiIdentifier, null, API_UUID);

        Assert.assertFalse(schemaFile().exists());
        Assert.assertTrue(apiFile().exists());
    }
}
