// /*
//  * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
//  *
//  * WSO2 LLC. licenses this file to you under the Apache License,
//  * Version 2.0 (the "License"); you may not use this file except
//  * in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing,
//  * software distributed under the License is distributed on an
//  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  * KIND, either express or implied.  See the License for the
//  * specific language governing permissions and limitations
//  * under the License.
//  */

// package org.wso2.carbon.apimgt.impl.definitions;

// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Test;
// import org.mockito.Mockito;
// import org.wso2.carbon.apimgt.api.APIManagementException;
// import org.wso2.carbon.apimgt.api.model.API;
// import org.wso2.carbon.apimgt.api.model.URITemplate;
// import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;

// import java.util.List;

// /**
//  * Unit tests for {@link OASDefinitionProcessor}.
//  */
// public class OASDefinitionProcessorTest {

//     private OASDefinitionProcessor processor;

//     @Before
//     public void init() {
//         processor = new OASDefinitionProcessor();
//     }

//     @Test
//     public void testGetType() {
//         API api = Mockito.mock(API.class);
//         Mockito.when(api.getType()).thenReturn("HTTP");
//         Assert.assertEquals("HTTP", processor.getType(api));
//     }

//     @Test
//     public void testGetTypeGraphQL() {
//         API api = Mockito.mock(API.class);
//         Mockito.when(api.getType()).thenReturn("GRAPHQL");
//         Assert.assertEquals("GRAPHQL", processor.getType(api));
//     }

//     @Test
//     public void testIsAsyncReturnsFalse() {
//         API api = Mockito.mock(API.class);
//         Assert.assertFalse(processor.isAsync(api));
//     }

//     @Test
//     public void testGetDefinitionFromAPI() {
//         API api = Mockito.mock(API.class);
//         String swaggerDef = "{\"openapi\": \"3.0.0\"}";
//         Mockito.when(api.getSwaggerDefinition()).thenReturn(swaggerDef);
//         Assert.assertEquals(swaggerDef, processor.getDefinitionFromAPI(api));
//     }

//     @Test
//     public void testGetDefinitionFromAPIReturnsNull() {
//         API api = Mockito.mock(API.class);
//         Mockito.when(api.getSwaggerDefinition()).thenReturn(null);
//         Assert.assertNull(processor.getDefinitionFromAPI(api));
//     }

//     @Test
//     public void testSetDefinitionToAPI() {
//         API api = Mockito.mock(API.class);
//         String definition = "{\"openapi\": \"3.0.0\"}";
//         processor.setDefinitionToAPI(api, definition);
//         Mockito.verify(api).setSwaggerDefinition(definition);
//     }

//     @Test
//     public void testExtractEndpointUrlOAS3WithServers() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": [\n" +
//                 "    {\n" +
//                 "      \"url\": \"https://api.example.com/v1\"\n" +
//                 "    }\n" +
//                 "  ]\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertEquals("https://api.example.com/v1", url);
//     }

//     @Test
//     public void testExtractEndpointUrlOAS3WithVariables() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": [\n" +
//                 "    {\n" +
//                 "      \"url\": \"https://api.example.com/{basePath}\",\n" +
//                 "      \"variables\": {\n" +
//                 "        \"basePath\": {\n" +
//                 "          \"default\": \"v2\"\n" +
//                 "        }\n" +
//                 "      }\n" +
//                 "    }\n" +
//                 "  ]\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertEquals("https://api.example.com/v2", url);
//     }

//     @Test
//     public void testExtractEndpointUrlOAS3WithBasePathVariable() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": [\n" +
//                 "    {\n" +
//                 "      \"url\": \"https://api.example.com/{basePath}/resources\",\n" +
//                 "      \"variables\": {\n" +
//                 "        \"basePath\": {\n" +
//                 "          \"default\": \"api\"\n" +
//                 "        }\n" +
//                 "      }\n" +
//                 "    }\n" +
//                 "  ]\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertEquals("https://api.example.com/api/resources", url);
//     }

//     @Test
//     public void testExtractEndpointUrlNoServers() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"info\": { \"title\": \"Test\", \"version\": \"1.0\" }\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertNull(url);
//     }

//     @Test
//     public void testExtractEndpointUrlEmptyServers() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": []\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertNull(url);
//     }

//     @Test
//     public void testExtractEndpointUrlNullDefinition() {
//         Assert.assertNull(processor.extractEndpointUrl(null));
//     }

//     @Test
//     public void testExtractEndpointUrlEmptyDefinition() {
//         Assert.assertNull(processor.extractEndpointUrl(""));
//     }

//     @Test
//     public void testExtractEndpointUrlWhitespaceDefinition() {
//         Assert.assertNull(processor.extractEndpointUrl("   "));
//     }

//     @Test
//     public void testExtractEndpointUrlInvalidJson() {
//         String url = processor.extractEndpointUrl("not a json");
//         Assert.assertNull(url);
//     }

//     @Test
//     public void testExtractEndpointUrlServerWithoutUrl() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": [\n" +
//                 "    {\n" +
//                 "      \"description\": \"No URL here\"\n" +
//                 "    }\n" +
//                 "  ]\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertNull(url);
//     }

//     @Test
//     public void testExtractEndpointUrlMultipleServers() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": [\n" +
//                 "    { \"url\": \"https://primary.example.com\" },\n" +
//                 "    { \"url\": \"https://secondary.example.com\" }\n" +
//                 "  ]\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertEquals("https://primary.example.com", url);
//     }

//     @Test
//     public void testExtractEndpointUrlVariablesWithoutBasePath() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": [\n" +
//                 "    {\n" +
//                 "      \"url\": \"https://api.example.com/v1\",\n" +
//                 "      \"variables\": {\n" +
//                 "        \"port\": {\n" +
//                 "          \"default\": \"8080\"\n" +
//                 "        }\n" +
//                 "      }\n" +
//                 "    }\n" +
//                 "  ]\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertEquals("https://api.example.com/v1", url);
//     }

//     @Test
//     public void testExtractEndpointUrlServersNotArray() {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"servers\": \"not-an-array\"\n" +
//                 "}";
//         String url = processor.extractEndpointUrl(definition);
//         Assert.assertNull(url);
//     }

//     @Test
//     public void testExtractOperationsNullDefinition() throws APIManagementException {
//         List<URITemplate> result = processor.extractOperations(null);
//         Assert.assertNotNull(result);
//         Assert.assertTrue(result.isEmpty());
//     }

//     @Test
//     public void testExtractOperationsEmptyDefinition() throws APIManagementException {
//         List<URITemplate> result = processor.extractOperations("");
//         Assert.assertNotNull(result);
//         Assert.assertTrue(result.isEmpty());
//     }

//     @Test
//     public void testExtractOperationsWithValidOAS3SingleGet() throws APIManagementException {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"info\": { \"title\": \"Test API\", \"version\": \"1.0.0\" },\n" +
//                 "  \"paths\": {\n" +
//                 "    \"/pets\": {\n" +
//                 "      \"get\": {\n" +
//                 "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n" +
//                 "      }\n" +
//                 "    }\n" +
//                 "  }\n" +
//                 "}";
//         List<URITemplate> result = processor.extractOperations(definition);
//         Assert.assertNotNull(result);
//         Assert.assertEquals(1, result.size());
//         Assert.assertEquals("/pets", result.get(0).getUriTemplate());
//         Assert.assertEquals("GET", result.get(0).getHTTPVerb());
//     }

//     @Test
//     public void testExtractOperationsWithMultipleOperations() throws APIManagementException {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"info\": { \"title\": \"Test API\", \"version\": \"1.0.0\" },\n" +
//                 "  \"paths\": {\n" +
//                 "    \"/pets\": {\n" +
//                 "      \"get\": {\n" +
//                 "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n" +
//                 "      },\n" +
//                 "      \"post\": {\n" +
//                 "        \"responses\": { \"201\": { \"description\": \"Created\" } }\n" +
//                 "      }\n" +
//                 "    }\n" +
//                 "  }\n" +
//                 "}";
//         List<URITemplate> result = processor.extractOperations(definition);
//         Assert.assertNotNull(result);
//         Assert.assertEquals(2, result.size());
//     }

//     @Test
//     public void testExtractOperationsWithMultiplePaths() throws APIManagementException {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"info\": { \"title\": \"Test API\", \"version\": \"1.0.0\" },\n" +
//                 "  \"paths\": {\n" +
//                 "    \"/pets\": {\n" +
//                 "      \"get\": {\n" +
//                 "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n" +
//                 "      }\n" +
//                 "    },\n" +
//                 "    \"/pets/{petId}\": {\n" +
//                 "      \"get\": {\n" +
//                 "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n" +
//                 "      },\n" +
//                 "      \"delete\": {\n" +
//                 "        \"responses\": { \"204\": { \"description\": \"Deleted\" } }\n" +
//                 "      }\n" +
//                 "    }\n" +
//                 "  }\n" +
//                 "}";
//         List<URITemplate> result = processor.extractOperations(definition);
//         Assert.assertNotNull(result);
//         Assert.assertEquals(3, result.size());
//     }

//     @Test
//     public void testExtractOperationsWithEmptyPaths() throws APIManagementException {
//         String definition = "{\n" +
//                 "  \"openapi\": \"3.0.0\",\n" +
//                 "  \"info\": { \"title\": \"Test API\", \"version\": \"1.0.0\" },\n" +
//                 "  \"paths\": {}\n" +
//                 "}";
//         List<URITemplate> result = processor.extractOperations(definition);
//         Assert.assertNotNull(result);
//         Assert.assertTrue(result.isEmpty());
//     }

//     @Test
//     public void testGetDefinitionFileName() {
//         Assert.assertEquals(ImportExportConstants.SWAGGER_YAML_FILE_NAME,
//                 processor.getDefinitionFileName());
//     }
// }
