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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceHelper;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.RegistryConstants;

import junit.framework.Assert;

/**
 * Tests that verify write/delete/update operations construct correct registry paths
 * after an API provider change.
 *
 * Since the actual write methods (saveWSDL, saveGraphQLSchema, saveThumbnail, deleteAPI, etc.)
 * have deep dependencies on carbon registry internals that are hard to mock, these tests verify
 * the path construction logic directly — confirming that the source path and original provider
 * are correctly derived from the artifact path regardless of what's in the provider attribute.
 *
 * This complements RegistryPersistenceImplProviderChangeTestCase (read operations) and
 * RegistryPersistenceUtilTestCase (utility methods).
 */
@RunWith(JUnit4.class)
public class RegistryPersistenceImplProviderChangePathTestCase {

    /**
     * Creates a provider-changed artifact and returns the expected paths that write
     * operations should use.
     */
    private static class ProviderChangePathVerifier {
        final String apiSourcePath;
        final String originalProvider;
        final String apiName;
        final String apiVersion;

        ProviderChangePathVerifier(String newProvider, String oldProviderEncoded)
                throws GovernanceException, APIPersistenceException {
            GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
            artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, newProvider);
            apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            // Simulate the registry path with old provider
            String oldPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + oldProviderEncoded + RegistryConstants.PATH_SEPARATOR + apiName
                    + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR + "api";

            // These are exactly what the production code computes via extractApiSourcePath/extractProviderFromPath
            apiSourcePath = RegistryPersistenceUtil.extractApiSourcePath(oldPath);
            originalProvider = RegistryPersistenceUtil.extractProviderFromPath(oldPath, apiName, apiVersion);
        }

        String getExpectedWsdlFilePath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + RegistryPersistenceUtil.createWsdlFileName(originalProvider, apiName, apiVersion);
        }

        String getExpectedWsdlArchivePath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_WSDL_ARCHIVE_LOCATION + originalProvider
                    + APIConstants.WSDL_PROVIDER_SEPERATOR + apiName + apiVersion
                    + APIConstants.ZIP_FILE_EXTENSION;
        }

        String getExpectedGraphQLSchemaPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + originalProvider + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR
                    + apiName + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        }

        String getExpectedThumbnailPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        }

        String getExpectedOASDefinitionPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
        }

        String getExpectedAsyncDefinitionPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;
        }

        String getExpectedDocPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
        }

        String getExpectedSoapToRestPath(String direction) {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + "soap_to_rest" + RegistryConstants.PATH_SEPARATOR + direction;
        }

        // Resource policy (updateResourcePolicyFromRegistryResourceId)
        String getExpectedResourcePolicyBasePath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.SOAP_TO_REST_RESOURCE;
        }

        // deleteAPI paths
        String getExpectedDeleteApiThumbnailPath() {
            return getExpectedThumbnailPath();
        }

        String getExpectedDeleteApiWsdlArchivePath() {
            return getExpectedWsdlArchivePath();
        }

        String getExpectedDeleteApiSwaggerDocPath() {
            return APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + apiName + '-' + apiVersion + '-' + originalProvider;
        }

        String getExpectedDeleteApiCollectionPath() {
            return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + originalProvider + RegistryConstants.PATH_SEPARATOR + apiName;
        }

        // API Product paths (same base path structure)
        String getExpectedProductOASDefinitionPath() {
            return getExpectedOASDefinitionPath();
        }

        String getExpectedProductCollectionPath() {
            return getExpectedDeleteApiCollectionPath();
        }

        String getExpectedProductProviderPath() {
            return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + originalProvider;
        }
    }

    /**
     * Verifies that all expected paths use the original provider's base path,
     * not the new provider's path, after a provider change.
     */
    private void verifyPathsUseOriginalProvider(String newProvider, String oldProviderEncoded,
            String wrongProviderEncoded) throws Exception {
        ProviderChangePathVerifier v = new ProviderChangePathVerifier(newProvider, oldProviderEncoded);

        // WSDL file path should contain original provider
        Assert.assertTrue("WSDL file path should contain original provider",
                v.getExpectedWsdlFilePath().contains(oldProviderEncoded));
        Assert.assertFalse("WSDL file path should NOT contain new provider",
                v.getExpectedWsdlFilePath().contains(wrongProviderEncoded));

        // WSDL archive path should contain original provider
        Assert.assertTrue("WSDL archive path should contain original provider",
                v.getExpectedWsdlArchivePath().contains(oldProviderEncoded));

        // GraphQL schema path should contain original provider
        Assert.assertTrue("GraphQL schema path should contain original provider",
                v.getExpectedGraphQLSchemaPath().contains(oldProviderEncoded));
        Assert.assertFalse("GraphQL schema path should NOT contain new provider",
                v.getExpectedGraphQLSchemaPath().contains(wrongProviderEncoded));

        // Thumbnail path should be under original provider's source path
        Assert.assertTrue("Thumbnail path should be under original provider",
                v.getExpectedThumbnailPath().contains(oldProviderEncoded));

        // OAS definition path should be under original provider's source path
        Assert.assertTrue("OAS definition path should be under original provider",
                v.getExpectedOASDefinitionPath().contains(oldProviderEncoded));

        // Async definition path should be under original provider's source path
        Assert.assertTrue("Async definition path should be under original provider",
                v.getExpectedAsyncDefinitionPath().contains(oldProviderEncoded));

        // Doc path should be under original provider's source path
        Assert.assertTrue("Doc path should be under original provider",
                v.getExpectedDocPath().contains(oldProviderEncoded));

        // Soap-to-rest path should be under original provider's source path
        Assert.assertTrue("Soap-to-rest IN path should be under original provider",
                v.getExpectedSoapToRestPath("in").contains(oldProviderEncoded));
        Assert.assertTrue("Soap-to-rest OUT path should be under original provider",
                v.getExpectedSoapToRestPath("out").contains(oldProviderEncoded));

        // Resource policy path (updateResourcePolicyFromRegistryResourceId)
        Assert.assertTrue("Resource policy base path should be under original provider",
                v.getExpectedResourcePolicyBasePath().contains(oldProviderEncoded));

        // deleteAPI paths
        Assert.assertTrue("Delete API thumbnail path should use original provider",
                v.getExpectedDeleteApiThumbnailPath().contains(oldProviderEncoded));
        Assert.assertTrue("Delete API WSDL archive path should use original provider",
                v.getExpectedDeleteApiWsdlArchivePath().contains(oldProviderEncoded));
        Assert.assertTrue("Delete API swagger doc path should use original provider",
                v.getExpectedDeleteApiSwaggerDocPath().contains(oldProviderEncoded));
        Assert.assertTrue("Delete API collection path should use original provider",
                v.getExpectedDeleteApiCollectionPath().contains(oldProviderEncoded));

        // API Product paths
        Assert.assertTrue("Product OAS definition path should use original provider",
                v.getExpectedProductOASDefinitionPath().contains(oldProviderEncoded));
        Assert.assertTrue("Product collection path should use original provider",
                v.getExpectedProductCollectionPath().contains(oldProviderEncoded));
        Assert.assertTrue("Product provider path should use original provider",
                v.getExpectedProductProviderPath().contains(oldProviderEncoded));
    }

    // =====================================================================
    // Super tenant — no email domain
    // =====================================================================

    @Test
    public void testPaths_SuperTenant_PrimaryToPrimary() throws Exception {
        verifyPathsUseOriginalProvider("publisher", "admin", "publisher");
    }

    @Test
    public void testPaths_SuperTenant_PrimaryToSecondary() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/publisher", "admin", "WSO2.COM/publisher");
    }

    @Test
    public void testPaths_SuperTenant_SecondaryToPrimary() throws Exception {
        verifyPathsUseOriginalProvider("publisher", "WSO2.COM/admin", "publisher");
    }

    @Test
    public void testPaths_SuperTenant_SecondaryToSecondary() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/temp", "WSO2.COM/admin", "WSO2.COM/temp");
    }

    // =====================================================================
    // Tenant — no email domain — pre-fix (raw @) and post-fix (-AT-)
    // =====================================================================

    @Test
    public void testPaths_Tenant_PrimaryToPrimary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("publisher@abc.com", "admin-AT-abc.com",
                "publisher@abc.com");
    }

    @Test
    public void testPaths_Tenant_PrimaryToPrimary_Encoded() throws Exception {
        verifyPathsUseOriginalProvider("publisher-AT-abc.com", "admin-AT-abc.com",
                "publisher-AT-abc.com");
    }

    @Test
    public void testPaths_Tenant_PrimaryToSecondary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/publisher@abc.com", "admin-AT-abc.com",
                "WSO2.COM/publisher");
    }

    @Test
    public void testPaths_Tenant_SecondaryToPrimary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("publisher@abc.com", "WSO2.COM/admin-AT-abc.com",
                "publisher@abc.com");
    }

    @Test
    public void testPaths_Tenant_SecondaryToSecondary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/temp@abc.com", "WSO2.COM/admin-AT-abc.com",
                "WSO2.COM/temp");
    }

    @Test
    public void testPaths_Tenant_SecondaryToSecondary_Encoded() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/temp-AT-abc.com", "WSO2.COM/admin-AT-abc.com",
                "WSO2.COM/temp-AT-abc.com");
    }

    // =====================================================================
    // Super tenant — email domain — pre-fix and post-fix
    // =====================================================================

    @Test
    public void testPaths_SuperTenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("publisher@gmail.com", "user-AT-gmail.com",
                "publisher@gmail.com");
    }

    @Test
    public void testPaths_SuperTenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyPathsUseOriginalProvider("publisher-AT-gmail.com", "user-AT-gmail.com",
                "publisher-AT-gmail.com");
    }

    @Test
    public void testPaths_SuperTenantEmail_PrimaryToSecondary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/publisher@gmail.com", "user-AT-gmail.com",
                "WSO2.COM/publisher");
    }

    @Test
    public void testPaths_SuperTenantEmail_SecondaryToPrimary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("publisher@gmail.com", "WSO2.COM/user-AT-gmail.com",
                "publisher@gmail.com");
    }

    @Test
    public void testPaths_SuperTenantEmail_SecondaryToSecondary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/temp@gmail.com", "WSO2.COM/user-AT-gmail.com",
                "WSO2.COM/temp");
    }

    @Test
    public void testPaths_SuperTenantEmail_SecondaryToSecondary_Encoded() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/temp-AT-gmail.com", "WSO2.COM/user-AT-gmail.com",
                "WSO2.COM/temp-AT-gmail.com");
    }

    // =====================================================================
    // Tenant — email domain — pre-fix and post-fix
    // =====================================================================

    @Test
    public void testPaths_TenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("publisher@gmail.com@abc.com",
                "user-AT-gmail.com-AT-abc.com", "publisher@gmail.com@abc.com");
    }

    @Test
    public void testPaths_TenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyPathsUseOriginalProvider("publisher-AT-gmail.com-AT-abc.com",
                "user-AT-gmail.com-AT-abc.com", "publisher-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testPaths_TenantEmail_PrimaryToSecondary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/publisher@gmail.com@abc.com",
                "user-AT-gmail.com-AT-abc.com", "WSO2.COM/publisher");
    }

    @Test
    public void testPaths_TenantEmail_SecondaryToPrimary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("publisher@gmail.com@abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com", "publisher@gmail.com@abc.com");
    }

    @Test
    public void testPaths_TenantEmail_SecondaryToSecondary_RawAt() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/temp@gmail.com@abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com", "WSO2.COM/temp");
    }

    @Test
    public void testPaths_TenantEmail_SecondaryToSecondary_Encoded() throws Exception {
        verifyPathsUseOriginalProvider("WSO2.COM/temp-AT-gmail.com-AT-abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com", "WSO2.COM/temp-AT-gmail.com-AT-abc.com");
    }

    // =====================================================================
    // Specific path format verifications
    // =====================================================================

    @Test
    public void testWsdlFileName_UsesOriginalProviderInFilename() throws Exception {
        ProviderChangePathVerifier v = new ProviderChangePathVerifier(
                "publisher@abc.com", "admin-AT-abc.com");
        String wsdlPath = v.getExpectedWsdlFilePath();
        // WSDL filename format: {provider}--{name}{version}.wsdl
        Assert.assertTrue("WSDL filename should contain original provider",
                wsdlPath.endsWith("admin-AT-abc.com--" + v.apiName + v.apiVersion + ".wsdl"));
    }

    @Test
    public void testGraphQLSchemaFileName_UsesOriginalProviderInFilename() throws Exception {
        ProviderChangePathVerifier v = new ProviderChangePathVerifier(
                "publisher@abc.com", "admin-AT-abc.com");
        String schemaPath = v.getExpectedGraphQLSchemaPath();
        Assert.assertTrue("GraphQL schema filename should contain original provider",
                schemaPath.contains("admin-AT-abc.com" + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR));
    }

    @Test
    public void testWsdlArchivePath_UsesOriginalProviderInFilename() throws Exception {
        ProviderChangePathVerifier v = new ProviderChangePathVerifier(
                "publisher@gmail.com", "user-AT-gmail.com");
        String archivePath = v.getExpectedWsdlArchivePath();
        Assert.assertTrue("WSDL archive should contain original provider",
                archivePath.contains("user-AT-gmail.com" + APIConstants.WSDL_PROVIDER_SEPERATOR));
    }

    @Test
    public void testAllPaths_ShareSameSourcePath() throws Exception {
        ProviderChangePathVerifier v = new ProviderChangePathVerifier(
                "publisher@gmail.com@abc.com", "user-AT-gmail.com-AT-abc.com");
        String sourcePath = v.apiSourcePath;

        // All paths should start with the same source path
        Assert.assertTrue(v.getExpectedWsdlFilePath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedWsdlArchivePath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedGraphQLSchemaPath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedThumbnailPath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedOASDefinitionPath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedAsyncDefinitionPath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedDocPath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedSoapToRestPath("in").startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedSoapToRestPath("out").startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedResourcePolicyBasePath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedDeleteApiThumbnailPath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedDeleteApiWsdlArchivePath().startsWith(sourcePath));
        Assert.assertTrue(v.getExpectedProductOASDefinitionPath().startsWith(sourcePath));
    }
}
