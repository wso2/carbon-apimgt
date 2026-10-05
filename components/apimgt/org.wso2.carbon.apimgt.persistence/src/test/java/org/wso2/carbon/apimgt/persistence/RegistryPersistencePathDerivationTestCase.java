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
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.registry.core.RegistryConstants;

import junit.framework.Assert;

/**
 * Tests that verify registry path derivation utility methods correctly extract
 * the API source path and provider from artifact paths for all provider formats
 * (simple user, email domain, tenant, secondary userstore).
 *
 * These tests validate that extractApiSourcePath() and extractProviderFromPath()
 * produce correct resource paths (WSDL, GraphQL, thumbnail, OAS, async, docs,
 * soap-to-rest) for each provider format.
 */
@RunWith(JUnit4.class)
public class RegistryPersistencePathDerivationTestCase {

    private static final String API_NAME = "PizzaShackAPI";
    private static final String API_VERSION = "1.0.0";

    /**
     * Helper that derives paths from a given provider format and verifies correctness.
     */
    private static class PathVerifier {
        final String apiSourcePath;
        final String provider;
        final String apiName;
        final String apiVersion;

        PathVerifier(String providerEncoded) throws APIPersistenceException {
            this(providerEncoded, API_NAME, API_VERSION);
        }

        PathVerifier(String providerEncoded, String apiName, String apiVersion)
                throws APIPersistenceException {
            this.apiName = apiName;
            this.apiVersion = apiVersion;

            // Build the artifact path as it would exist in registry
            String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + providerEncoded + RegistryConstants.PATH_SEPARATOR + apiName
                    + RegistryConstants.PATH_SEPARATOR + apiVersion + APIConstants.API_RESOURCE_NAME;

            // Derive using the same utility methods production code uses
            apiSourcePath = RegistryPersistenceUtil.extractApiSourcePath(artifactPath);
            provider = RegistryPersistenceUtil.extractProviderFromPath(artifactPath, apiName, apiVersion);
        }

        String getWsdlFilePath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + RegistryPersistenceUtil.createWsdlFileName(provider, apiName, apiVersion);
        }

        String getWsdlArchivePath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_WSDL_ARCHIVE_LOCATION + provider
                    + APIConstants.WSDL_PROVIDER_SEPERATOR + apiName + apiVersion
                    + APIConstants.ZIP_FILE_EXTENSION;
        }

        String getGraphQLSchemaPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + provider + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR
                    + apiName + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        }

        String getThumbnailPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        }

        String getOASDefinitionPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
        }

        String getAsyncDefinitionPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;
        }

        String getDocPath() {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
        }

        String getSoapToRestPath(String direction) {
            return apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.SOAP_TO_REST_RESOURCE + RegistryConstants.PATH_SEPARATOR + direction;
        }
    }

    /**
     * Verifies all resource paths are correctly derived for the given provider format.
     */
    private void verifyPathDerivation(String providerEncoded) throws Exception {
        PathVerifier v = new PathVerifier(providerEncoded);

        // Provider should be correctly extracted
        Assert.assertEquals("Provider should be correctly extracted",
                providerEncoded, v.provider);

        // All paths should contain the provider
        Assert.assertTrue("WSDL file path should contain provider",
                v.getWsdlFilePath().contains(providerEncoded));
        Assert.assertTrue("WSDL archive path should contain provider",
                v.getWsdlArchivePath().contains(providerEncoded));
        Assert.assertTrue("GraphQL schema path should contain provider",
                v.getGraphQLSchemaPath().contains(providerEncoded));
        Assert.assertTrue("Thumbnail path should contain provider",
                v.getThumbnailPath().contains(providerEncoded));
        Assert.assertTrue("OAS definition path should contain provider",
                v.getOASDefinitionPath().contains(providerEncoded));
        Assert.assertTrue("Async definition path should contain provider",
                v.getAsyncDefinitionPath().contains(providerEncoded));
        Assert.assertTrue("Doc path should contain provider",
                v.getDocPath().contains(providerEncoded));
        Assert.assertTrue("Soap-to-rest IN path should contain provider",
                v.getSoapToRestPath("in").contains(providerEncoded));
        Assert.assertTrue("Soap-to-rest OUT path should contain provider",
                v.getSoapToRestPath("out").contains(providerEncoded));

        // All paths should share the same source path
        String sourcePath = v.apiSourcePath;
        Assert.assertTrue(v.getWsdlFilePath().startsWith(sourcePath));
        Assert.assertTrue(v.getWsdlArchivePath().startsWith(sourcePath));
        Assert.assertTrue(v.getGraphQLSchemaPath().startsWith(sourcePath));
        Assert.assertTrue(v.getThumbnailPath().startsWith(sourcePath));
        Assert.assertTrue(v.getOASDefinitionPath().startsWith(sourcePath));
        Assert.assertTrue(v.getAsyncDefinitionPath().startsWith(sourcePath));
        Assert.assertTrue(v.getDocPath().startsWith(sourcePath));
        Assert.assertTrue(v.getSoapToRestPath("in").startsWith(sourcePath));
    }

    // =====================================================================
    // Simple users (super tenant, no email domain)
    // =====================================================================

    @Test
    public void testPathDerivation_SimpleUser() throws Exception {
        verifyPathDerivation("admin");
    }

    @Test
    public void testPathDerivation_SecondaryUserstoreUser() throws Exception {
        verifyPathDerivation("WSO2.COM/admin");
    }

    // =====================================================================
    // Email domain users (super tenant)
    // =====================================================================

    @Test
    public void testPathDerivation_EmailDomainUser() throws Exception {
        verifyPathDerivation("user-AT-gmail.com");
    }

    @Test
    public void testPathDerivation_SecondaryUserstoreEmailUser() throws Exception {
        verifyPathDerivation("WSO2.COM/user-AT-gmail.com");
    }

    // =====================================================================
    // Tenant users (no email domain)
    // =====================================================================

    @Test
    public void testPathDerivation_TenantUser() throws Exception {
        verifyPathDerivation("admin-AT-abc.com");
    }

    @Test
    public void testPathDerivation_SecondaryUserstoreTenantUser() throws Exception {
        verifyPathDerivation("WSO2.COM/admin-AT-abc.com");
    }

    // =====================================================================
    // Tenant users (with email domain)
    // =====================================================================

    @Test
    public void testPathDerivation_TenantEmailUser() throws Exception {
        verifyPathDerivation("user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testPathDerivation_SecondaryUserstoreTenantEmailUser() throws Exception {
        verifyPathDerivation("WSO2.COM/user-AT-gmail.com-AT-abc.com");
    }

    // =====================================================================
    // Edge cases: provider name same as API name
    // =====================================================================

    @Test
    public void testPathDerivation_ProviderSameAsApiName() throws Exception {
        PathVerifier v = new PathVerifier("admin", "admin", "1.0");
        Assert.assertEquals("admin", v.provider);
    }

    @Test
    public void testPathDerivation_SecondaryUserstoreProviderSameAsApiName() throws Exception {
        PathVerifier v = new PathVerifier("WSO2.COM/admin", "admin", "1.0");
        Assert.assertEquals("WSO2.COM/admin", v.provider);
    }

    // =====================================================================
    // Specific filename format verifications
    // =====================================================================

    @Test
    public void testWsdlFileName_Format() throws Exception {
        PathVerifier v = new PathVerifier("admin-AT-abc.com");
        Assert.assertTrue("WSDL filename should be provider--nameversion.wsdl",
                v.getWsdlFilePath().endsWith("admin-AT-abc.com--" + API_NAME + API_VERSION + ".wsdl"));
    }

    @Test
    public void testGraphQLSchemaFileName_Format() throws Exception {
        PathVerifier v = new PathVerifier("admin-AT-abc.com");
        Assert.assertTrue("GraphQL schema should contain provider separator",
                v.getGraphQLSchemaPath().contains("admin-AT-abc.com"
                        + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR));
    }
}
