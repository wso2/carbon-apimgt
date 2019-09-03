package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class OAS3ParserTest {

    @Test
    public void getURITemplates() throws Exception {
        //todo: to be implement
        /*String oas3 = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v3.yaml"),
                        "UTF-8");
        OAS3Parser oas3Parser = new OAS3Parser();
        Set<URITemplate> uriTemplates = oas3Parser.getURITemplates(null, oas3);
        System.out.println();*/
    }

    @Test
    public void getScopes() {
    }

    @Test
    public void saveAPIDefinition() {
    }

    @Test
    public void getAPIDefinition() {
    }

    @Test
    public void generateAPIDefinition() {
    }

    @Test
    public void generateAPIDefinition1() throws Exception {
        //todo: to be implement
        /*
        String oas3 = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "swagger.json"),
                        "UTF-8");
        Optional<APIDefinition> optional = OASParserUtil.getOASParser(oas3);
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(optional.get() instanceof OAS3Parser);

        String apiData = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "apiData.json"),
                        "UTF-8");*/

    }

    @Test
    public void getAPIOpenAPIDefinitionTimeStamps() {
    }

    @Test
    public void validateAPIDefinition() {
    }

    @Test
    public void testGetURITemplatesOfOpenAPI300Spec() throws APIManagementException {

        OAS3Parser apiDefinitionFromOpenAPI300 = new OAS3Parser();
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
        uriTemplates.add(getUriTemplate("POST", "Application User", "/*"));
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
        OAS3Parser apiDef = new OAS3Parser();
        String openApi =
                "{\n"
                        + "  \"openapi\": \"3.0.0\",\n"
                        + "  \"info\": {\n"
                        + "    \"title\": \"OAPI\",\n"
                        + "    \"version\": \"1.0.0\"\n"
                        + "  },\n"
                        + "  \"paths\": {\n"
                        + "    \"/item\": {\n"
                        + "      \"parameters\": [],\n"
                        + "      \"servers\": [],\n"
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
        return uriTemplate;
    }
}