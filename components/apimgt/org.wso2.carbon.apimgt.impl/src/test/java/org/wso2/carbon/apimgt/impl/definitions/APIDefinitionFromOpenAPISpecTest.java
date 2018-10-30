/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.definitions;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.LinkedHashSet;
import java.util.Set;

public class APIDefinitionFromOpenAPISpecTest {
    @Test
    public void testGetURITemplatesOfOpenAPI20Spec() throws Exception {

        APIDefinitionFromOpenAPISpec apiDefinitionFromOpenAPI20 = new APIDefinitionFromOpenAPISpec();
        String swagger = "{\n" +
                "  \"paths\": {\n" +
                "    \"/*\": {\n" +
                "      \"get\": {\n" +
                "        \"x-auth-type\": \"Application\",\n" +
                "        \"x-throttling-tier\": \"Unlimited\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"OK\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"post\": {\n" +
                "        \"x-auth-type\": \"Application User\",\n" +
                "        \"x-throttling-tier\": \"Unlimited\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"OK\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"put\": {\n" +
                "        \"x-auth-type\": \"None\",\n" +
                "        \"x-throttling-tier\": \"Unlimited\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"OK\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"delete\": {\n" +
                "        \"x-throttling-tier\": \"Unlimited\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"OK\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"/abc\": {\n" +
                "      \"get\": {\n" +
                "        \"x-auth-type\": \"Application & Application User\",\n" +
                "        \"x-throttling-tier\": \"Unlimited\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"OK\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"x-wso2-security\": {\n" +
                "    \"apim\": {\n" +
                "      \"x-wso2-scopes\": []\n" +
                "    }\n" +
                "  },\n" +
                "  \"swagger\": \"2.0\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"PhoneVerification\",\n" +
                "    \"description\": \"Verify a phone number\",\n" +
                "    \"contact\": {\n" +
                "      \"email\": \"xx@ee.com\",\n" +
                "      \"name\": \"xx\"\n" +
                "    },\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  }\n" +
                "}";
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        uriTemplates.add(getUriTemplate("POST", "Application_User", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application", "/*"));
        uriTemplates.add(getUriTemplate("PUT", "None", "/*"));
        uriTemplates.add(getUriTemplate("DELETE", "Any", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Any", "/abc"));
        API api = new API(new APIIdentifier("admin", "PhoneVerification", "1.0.0"));
        Set<URITemplate> uriTemplateSet = apiDefinitionFromOpenAPI20.getURITemplates(api, swagger);
        Assert.assertEquals(uriTemplateSet, uriTemplates);
    }

    @Test
    public void testGetURITemplatesOfOpenAPI300Spec() throws APIManagementException {

        APIDefinitionFromOpenAPISpec apiDefinitionFromOpenAPI300 = new APIDefinitionFromOpenAPISpec();
        String openAPISpec300 =
                "{\n" +
                        "   \"openapi\":\"3.0.0\",\n" +
                        "   \"paths\":{\n" +
                        "      \"/*\":{\n" +
                        "         \"get\":{\n" +
                        "            \"responses\":{\n" +
                        "               \"200\":{\n" +
                        "                  \"description\":\"\"\n" +
                        "               }\n" +
                        "            },\n" +
                        "            \"x-auth-type\":\"Application\",\n" +
                        "            \"x-throttling-tier\":\"Unlimited\"\n" +
                        "         },\n" +
                        "         \"post\":{\n" +
                        "            \"requestBody\":{\n" +
                        "               \"content\":{\n" +
                        "                  \"application/json\":{\n" +
                        "                     \"schema\":{\n" +
                        "                        \"type\":\"object\",\n" +
                        "                        \"properties\":{\n" +
                        "                           \"payload\":{\n" +
                        "                              \"type\":\"string\"\n" +
                        "                           }\n" +
                        "                        }\n" +
                        "                     }\n" +
                        "                  }\n" +
                        "               },\n" +
                        "               \"required\":true,\n" +
                        "               \"description\":\"Request Body\"\n" +
                        "            },\n" +
                        "            \"responses\":{\n" +
                        "               \"200\":{\n" +
                        "                  \"description\":\"\"\n" +
                        "               }\n" +
                        "            },\n" +
                        "            \"x-auth-type\":\"Application User\",\n" +
                        "            \"x-throttling-tier\":\"Unlimited\"\n" +
                        "         },\n" +
                        "         \"put\":{\n" +
                        "            \"requestBody\":{\n" +
                        "               \"content\":{\n" +
                        "                  \"application/json\":{\n" +
                        "                     \"schema\":{\n" +
                        "                        \"type\":\"object\",\n" +
                        "                        \"properties\":{\n" +
                        "                           \"payload\":{\n" +
                        "                              \"type\":\"string\"\n" +
                        "                           }\n" +
                        "                        }\n" +
                        "                     }\n" +
                        "                  }\n" +
                        "               },\n" +
                        "               \"required\":true,\n" +
                        "               \"description\":\"Request Body\"\n" +
                        "            },\n" +
                        "            \"responses\":{\n" +
                        "               \"200\":{\n" +
                        "                  \"description\":\"\"\n" +
                        "               }\n" +
                        "            },\n" +
                        "            \"x-auth-type\":\"None\",\n" +
                        "            \"x-throttling-tier\":\"Unlimited\"\n" +
                        "         },\n" +
                        "         \"delete\":{\n" +
                        "            \"responses\":{\n" +
                        "               \"200\":{\n" +
                        "                  \"description\":\"\"\n" +
                        "               }\n" +
                        "            },\n" +
                        "            \"x-throttling-tier\":\"Unlimited\"\n" +
                        "         }\n" +
                        "      },\n" +
                        "      \"/abc\":{\n" +
                        "         \"get\":{\n" +
                        "            \"responses\":{\n" +
                        "               \"200\":{\n" +
                        "                  \"description\":\"\"\n" +
                        "               }\n" +
                        "            },\n" +
                        "            \"x-throttling-tier\":\"Unlimited\"\n" +
                        "         }\n" +
                        "      }\n" +
                        "   },\n" +
                        "   \"info\":{\n" +
                        "      \"title\":\"PhoneVerification\",\n" +
                        "      \"version\":\"1.0.0\"\n" +
                        "   },\n" +
                        "   \"servers\":[\n" +
                        "      {\n" +
                        "         \"url\":\"https://172.19.0.1:8243/phoneVerification/1.0.0\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"url\":\"http://172.19.0.1:8243/phoneVerification/1.0.0\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"components\":{\n" +
                        "      \"securitySchemes\":{\n" +
                        "         \"default\":{\n" +
                        "            \"type\":\"oauth2\",\n" +
                        "            \"flows\":{\n" +
                        "               \"implicit\":{\n" +
                        "                  \"authorizationUrl\":\"https://172.19.0.1:8243/authorize\",\n" +
                        "                  \"scopes\":{\n" +
                        "\n" +
                        "                  }\n" +
                        "               }\n" +
                        "            }\n" +
                        "         }\n" +
                        "      }\n" +
                        "   }\n" +
                        "}";

        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        uriTemplates.add(getUriTemplate("POST", "Application_User", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application", "/*"));
        uriTemplates.add(getUriTemplate("PUT", "None", "/*"));
        uriTemplates.add(getUriTemplate("DELETE", "Any", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Any", "/abc"));
        API api = new API(new APIIdentifier("admin", "PhoneVerification", "1.0.0"));
        Set<URITemplate> uriTemplateSet = apiDefinitionFromOpenAPI300.getURITemplates(api, openAPISpec300);
        Assert.assertEquals(uriTemplateSet, uriTemplates);

    }

    @Test
    public void testOpenApi3WithNonHttpVerbElementInPathItem() throws APIManagementException {
        APIDefinitionFromOpenAPISpec apiDef = new APIDefinitionFromOpenAPISpec();
        String openApi =
                "{\n"
                        + "  \"openapi\": \"3.0.0\",\n"
                        + "  \"info\": {\n"
                        + "    \"title\": \"OAPI\",\n"
                        + "    \"version\": \"1.0.0\"\n"
                        + "  },\n"
                        + "  \"paths\": {\n"
                        + "    \"/item\": {\n"
                        + "      \"parameters\": {},\n"
                        + "      \"servers\": {},\n"
                        + "      \"summary\": \"Valid summary but invalid in WSO2\",\n"
                        + "      \"description\": \"Valid description but invalid in WSO2\",\n"
                        + "      \"x-custom-field\": \"Valid custom field but invalid in WSO2\",\n"
                        + "      \"get\": {\n"
                        + "        \"responses\": {\n"
                        + "          \"200\": {\n"
                        + "            \"description\": \"OK\"\n"
                        + "          }\n"
                        + "        },\n"
                        + "        \"x-auth-type\":\"Application\",\n"
                        + "        \"x-throttling-tier\":\"Unlimited\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}";

        Set<URITemplate> expectedTemplates = new LinkedHashSet<URITemplate>();
        expectedTemplates.add(getUriTemplate("GET", "Application", "/item"));
        API api = new API(new APIIdentifier("admin", "OAPI", "1.0.0"));
        Set<URITemplate> actualTemplates = apiDef.getURITemplates(api, openApi);
        Assert.assertEquals(actualTemplates, expectedTemplates);
    }

    protected URITemplate getUriTemplate(String httpVerb, String authType, String uriTemplateString) {
        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setAuthTypes(authType);
        uriTemplate.setAuthType(authType);
        uriTemplate.setHTTPVerb(httpVerb);
        uriTemplate.setHttpVerbs(httpVerb);
        uriTemplate.setUriTemplate(uriTemplateString);
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setThrottlingTiers("Unlimited");
        uriTemplate.setScope(null);
        uriTemplate.setScopes(null);
        return uriTemplate;
    }

    @Test
    public void getScopes() throws Exception {
    }

    @Test
    public void saveAPIDefinition() throws Exception {
    }

    @Test
    public void getAPIDefinition() throws Exception {
    }

    @Test
    public void generateAPIDefinition() throws Exception {
    }

    @Test
    public void getAPISwaggerDefinitionTimeStamps() throws Exception {
    }

}