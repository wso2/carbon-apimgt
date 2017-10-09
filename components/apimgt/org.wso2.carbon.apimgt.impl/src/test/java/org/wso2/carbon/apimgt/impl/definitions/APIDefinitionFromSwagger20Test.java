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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.LinkedHashSet;
import java.util.Set;

public class APIDefinitionFromSwagger20Test {
    @Test
    public void getURITemplates() throws Exception {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
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
        uriTemplates.add(getUriTemplate("POST","Application_User","/*"));
        uriTemplates.add(getUriTemplate("GET","Application","/*"));
        uriTemplates.add(getUriTemplate("PUT","None","/*"));
        uriTemplates.add(getUriTemplate("DELETE","Any","/*"));
        uriTemplates.add(getUriTemplate("GET","Any","/abc"));
        API api = new API(new APIIdentifier("admin","PhoneVerification","1.0.0"));
        Set<URITemplate> uriTemplateSet = apiDefinitionFromSwagger20.getURITemplates(api,swagger);
        Assert.assertEquals(uriTemplateSet,uriTemplates);
    }

    protected URITemplate getUriTemplate(String httpVerb,String authType,String uriTemplateString) {
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