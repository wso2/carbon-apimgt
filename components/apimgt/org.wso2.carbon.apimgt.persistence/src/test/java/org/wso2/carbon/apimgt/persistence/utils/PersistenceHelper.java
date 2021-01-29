/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.persistence.utils;

import javax.xml.namespace.QName;

import org.wso2.carbon.apimgt.persistence.GenericArtifactWrapper;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;

public class PersistenceHelper {
    
    public static GenericArtifact getSampleAPIArtifact() throws GovernanceException {

        GenericArtifact artifact = new GenericArtifactWrapper(new QName("", "PizzaShackAPI", ""),
                "application/vnd.wso2-api+xml");
        artifact.setAttribute("overview_endpointSecured","false");
        artifact.setAttribute("overview_transports","http,https");
        artifact.setAttribute("URITemplate_authType3","Application & Application User");
        artifact.setAttribute("overview_wadl",null);
        artifact.setAttribute("URITemplate_authType4","Application & Application User");
        artifact.setAttribute("overview_authorizationHeader","Authorization");
        artifact.setAttribute("URITemplate_authType1","Application & Application User");
        artifact.setAttribute("overview_visibleTenants",null);
        artifact.setAttribute("URITemplate_authType2","Application & Application User");
        artifact.setAttribute("overview_wsdl",null);
        artifact.setAttribute("overview_apiSecurity","oauth2,oauth_basic_auth_api_key_mandatory");
        artifact.setAttribute("URITemplate_authType0","Application & Application User");
        artifact.setAttribute("overview_keyManagers","[\"all\"]");
        artifact.setAttribute("overview_environments","Production and Sandbox");
        artifact.setAttribute("overview_context","/pizzashack/1.0.0");
        artifact.setAttribute("overview_visibility","restricted");
        artifact.setAttribute("overview_isLatest","true");
        artifact.setAttribute("overview_outSequence","log_out_message");
        artifact.setAttribute("overview_provider","admin");
        artifact.setAttribute("apiCategories_categoryName","testcategory");
        artifact.setAttribute("overview_thumbnail",
                "/registry/resource/_system/governance/apimgt/applicationdata/provider/admin/PizzaShackAPI/1.0.0/icon");
        artifact.setAttribute("overview_contextTemplate","/pizzashack/{version}");
        artifact.setAttribute("overview_description","This is a simple API for Pizza Shack online pizza delivery store.");
        artifact.setAttribute("overview_technicalOwner","John Doe");
        artifact.setAttribute("overview_type","HTTP");
        artifact.setAttribute("overview_technicalOwnerEmail","architecture@pizzashack.com");
        artifact.setAttribute("URITemplate_httpVerb4","DELETE");
        artifact.setAttribute("overview_inSequence","log_in_message");
        artifact.setAttribute("URITemplate_httpVerb2","GET");
        artifact.setAttribute("URITemplate_httpVerb3","PUT");
        artifact.setAttribute("URITemplate_httpVerb0","POST");
        artifact.setAttribute("URITemplate_httpVerb1","GET");
        artifact.setAttribute("labels_labelName","gwlable");
        artifact.setAttribute("overview_businessOwner","Jane Roe");
        artifact.setAttribute("overview_version","1.0.0");
        artifact.setAttribute("overview_endpointConfig",
                "{\"endpoint_type\":\"http\",\"sandbox_endpoints\":{\"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\"},"
                + "\"endpoint_security\":{\"production\":{\"password\":\"admin\",\"tokenUrl\":null,\"clientId\":null,"
                + "\"clientSecret\":null,\"customParameters\":\"{}\",\"additionalProperties\":{},\"type\":\"BASIC\","
                + "\"grantType\":null,\"enabled\":true,\"uniqueIdentifier\":null,\"username\":\"admin\"},"
                + "\"sandbox\":{\"password\":null,\"tokenUrl\":null,\"clientId\":null,\"clientSecret\":null,"
                + "\"customParameters\":\"{}\",\"additionalProperties\":{},\"type\":null,\"grantType\":null,\"enabled\":false,"
                + "\"uniqueIdentifier\":null,\"username\":null}},\"production_endpoints\":"
                + "{\"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\"}}");
        artifact.setAttribute("overview_tier","Bronze||Silver||Gold||Unlimited");
        artifact.setAttribute("overview_sandboxTps","1000");
        artifact.setAttribute("overview_apiOwner",null);
        artifact.setAttribute("overview_businessOwnerEmail","marketing@pizzashack.com");
        artifact.setAttribute("isMonetizationEnabled","false");
        artifact.setAttribute("overview_implementation","ENDPOINT");
        artifact.setAttribute("overview_deployments","null");
        artifact.setAttribute("overview_redirectURL",null);
        artifact.setAttribute("monetizationProperties","{}");
        artifact.setAttribute("overview_name","PizzaShackAPI");
        artifact.setAttribute("overview_subscriptionAvailability","current_tenant");
        artifact.setAttribute("overview_productionTps","1000");
        artifact.setAttribute("overview_cacheTimeout","300");
        artifact.setAttribute("overview_visibleRoles","admin,internal/subscriber");
        artifact.setAttribute("overview_testKey",null);
        artifact.setAttribute("overview_corsConfiguration",
                "{\"corsConfigurationEnabled\":true,\"accessControlAllowOrigins\":[\"*\"],"
                + "\"accessControlAllowCredentials\":false,\"accessControlAllowHeaders\":[\"authorization\","
                + "\"Access-Control-Allow-Origin\",\"Content-Type\",\"SOAPAction\",\"apikey\",\"testKey\"],"
                + "\"accessControlAllowMethods\":[\"GET\",\"PUT\",\"POST\",\"DELETE\",\"PATCH\",\"OPTIONS\"]}");
        artifact.setAttribute("overview_advertiseOnly","false");
        artifact.setAttribute("overview_versionType","context");
        artifact.setAttribute("overview_status","PUBLISHED");
        artifact.setAttribute("overview_endpointPpassword",null);
        artifact.setAttribute("overview_tenants",null);
        artifact.setAttribute("overview_endpointAuthDigest","false");
        artifact.setAttribute("overview_faultSequence","json_fault");
        artifact.setAttribute("overview_responseCaching","Enabled");
        artifact.setAttribute("URITemplate_urlPattern4","/order/{orderId}");
        artifact.setAttribute("overview_isDefaultVersion","true");
        artifact.setAttribute("URITemplate_urlPattern2","/order/{orderId}");
        artifact.setAttribute("URITemplate_urlPattern3","/order/{orderId}");
        artifact.setAttribute("URITemplate_urlPattern0","/order");
        artifact.setAttribute("URITemplate_urlPattern1","/menu");
        artifact.setAttribute("overview_enableStore","true");
        artifact.setAttribute("overview_enableSchemaValidation","true");
        artifact.setAttribute("overview_endpointUsername",null);
        artifact.setAttribute("overview_status", "PUBLISHED");
        artifact.setId("88e758b7-6924-4e9f-8882-431070b6492b");
        
        return artifact;
    }
    public static GenericArtifact getSampleAPIArtifactForTenant() throws GovernanceException {

        GenericArtifact artifact = new GenericArtifactImpl(new QName("", "PizzaShackAPI", ""),
                "application/vnd.wso2-api+xml");
        artifact.setAttribute("overview_endpointSecured","false");
        artifact.setAttribute("overview_transports","http,https");
        artifact.setAttribute("URITemplate_authType3","Application & Application User");
        artifact.setAttribute("overview_wadl",null);
        artifact.setAttribute("URITemplate_authType4","Application & Application User");
        artifact.setAttribute("overview_authorizationHeader","Authorization");
        artifact.setAttribute("URITemplate_authType1","Application & Application User");
        artifact.setAttribute("overview_visibleTenants",null);
        artifact.setAttribute("URITemplate_authType2","Application & Application User");
        artifact.setAttribute("overview_wsdl",null);
        artifact.setAttribute("overview_apiSecurity","oauth2,oauth_basic_auth_api_key_mandatory");
        artifact.setAttribute("URITemplate_authType0","Application & Application User");
        artifact.setAttribute("overview_keyManagers","[\"all\"]");
        artifact.setAttribute("overview_environments","Production and Sandbox");
        artifact.setAttribute("overview_context","/t/wso2.com/pizzashack/1.0.0");
        artifact.setAttribute("overview_visibility","restricted");
        artifact.setAttribute("overview_isLatest","true");
        artifact.setAttribute("overview_outSequence","log_out_message");
        artifact.setAttribute("overview_provider","admin-AT-wso2.com");
        artifact.setAttribute("apiCategories_categoryName","testcategory");
        artifact.setAttribute("overview_thumbnail",
                "/t/wso2.com/t/wso2.com/registry/resource/_system/governance/apimgt/applicationdata/provider/admin-AT-wso2.com/PizzaShackAPI/1.0.0/icon");
        artifact.setAttribute("overview_contextTemplate","/t/wso2.com/pizzashack/{version}");
        artifact.setAttribute("overview_description","This is a simple API for Pizza Shack online pizza delivery store.");
        artifact.setAttribute("overview_technicalOwner","John Doe");
        artifact.setAttribute("overview_type","HTTP");
        artifact.setAttribute("overview_technicalOwnerEmail","architecture@pizzashack.com");
        artifact.setAttribute("URITemplate_httpVerb4","DELETE");
        artifact.setAttribute("overview_inSequence","log_in_message");
        artifact.setAttribute("URITemplate_httpVerb2","GET");
        artifact.setAttribute("URITemplate_httpVerb3","PUT");
        artifact.setAttribute("URITemplate_httpVerb0","POST");
        artifact.setAttribute("URITemplate_httpVerb1","GET");
        artifact.setAttribute("labels_labelName","gwlable");
        artifact.setAttribute("overview_businessOwner","Jane Roe");
        artifact.setAttribute("overview_version","1.0.0");
        artifact.setAttribute("overview_endpointConfig",
                "{\"endpoint_type\":\"http\",\"sandbox_endpoints\":{\"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\"},"
                + "\"endpoint_security\":{\"production\":{\"password\":\"admin\",\"tokenUrl\":null,\"clientId\":null,"
                + "\"clientSecret\":null,\"customParameters\":\"{}\",\"additionalProperties\":{},\"type\":\"BASIC\","
                + "\"grantType\":null,\"enabled\":true,\"uniqueIdentifier\":null,\"username\":\"admin\"},"
                + "\"sandbox\":{\"password\":null,\"tokenUrl\":null,\"clientId\":null,\"clientSecret\":null,"
                + "\"customParameters\":\"{}\",\"additionalProperties\":{},\"type\":null,\"grantType\":null,\"enabled\":false,"
                + "\"uniqueIdentifier\":null,\"username\":null}},\"production_endpoints\":"
                + "{\"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\"}}");
        artifact.setAttribute("overview_tier","Bronze||Silver||Gold||Unlimited");
        artifact.setAttribute("overview_sandboxTps","1000");
        artifact.setAttribute("overview_apiOwner","admin@wso2.com");
        artifact.setAttribute("overview_businessOwnerEmail","marketing@pizzashack.com");
        artifact.setAttribute("isMonetizationEnabled","false");
        artifact.setAttribute("overview_implementation","ENDPOINT");
        artifact.setAttribute("overview_deployments","null");
        artifact.setAttribute("overview_redirectURL",null);
        artifact.setAttribute("monetizationProperties","{}");
        artifact.setAttribute("overview_name","PizzaShackAPI");
        artifact.setAttribute("overview_subscriptionAvailability","current_tenant");
        artifact.setAttribute("overview_productionTps","1000");
        artifact.setAttribute("overview_cacheTimeout","300");
        artifact.setAttribute("overview_visibleRoles","admin,internal/subscriber");
        artifact.setAttribute("overview_testKey",null);
        artifact.setAttribute("overview_corsConfiguration",
                "{\"corsConfigurationEnabled\":true,\"accessControlAllowOrigins\":[\"*\"],"
                + "\"accessControlAllowCredentials\":false,\"accessControlAllowHeaders\":[\"authorization\","
                + "\"Access-Control-Allow-Origin\",\"Content-Type\",\"SOAPAction\",\"apikey\",\"testKey\"],"
                + "\"accessControlAllowMethods\":[\"GET\",\"PUT\",\"POST\",\"DELETE\",\"PATCH\",\"OPTIONS\"]}");
        artifact.setAttribute("overview_advertiseOnly","false");
        artifact.setAttribute("overview_versionType","context");
        artifact.setAttribute("overview_status","PUBLISHED");
        artifact.setAttribute("overview_endpointPpassword",null);
        artifact.setAttribute("overview_tenants",null);
        artifact.setAttribute("overview_endpointAuthDigest","false");
        artifact.setAttribute("overview_faultSequence","json_fault");
        artifact.setAttribute("overview_responseCaching","Enabled");
        artifact.setAttribute("URITemplate_urlPattern4","/order/{orderId}");
        artifact.setAttribute("overview_isDefaultVersion","true");
        artifact.setAttribute("URITemplate_urlPattern2","/order/{orderId}");
        artifact.setAttribute("URITemplate_urlPattern3","/order/{orderId}");
        artifact.setAttribute("URITemplate_urlPattern0","/order");
        artifact.setAttribute("URITemplate_urlPattern1","/menu");
        artifact.setAttribute("overview_enableStore","true");
        artifact.setAttribute("overview_enableSchemaValidation","true");
        artifact.setAttribute("overview_endpointUsername",null);
        artifact.setAttribute("overview_status", "PUBLISHED");
        artifact.setId("88e758b7-6924-4e9f-8882-431070b6492b");
        return artifact;
    }
    
    public static GenericArtifact getSampleAPIProductArtifact() throws GovernanceException {
        GenericArtifact artifact = new GenericArtifactImpl(new QName("", "APIProductTest", ""),
                "application/vnd.wso2-api+xml");
        artifact.setAttribute("overview_transports","http,https");
        artifact.setAttribute("monetizationProperties","{}");
        artifact.setAttribute("overview_authorizationHeader","Authorization");
        artifact.setAttribute("overview_visibleTenants",null);
        artifact.setAttribute("overview_name","APIProductTest");
        artifact.setAttribute("overview_apiSecurity","oauth2,oauth_basic_auth_api_key_mandatory,basic_auth,api_key");
        artifact.setAttribute("overview_environments","Production and Sandbox");
        artifact.setAttribute("overview_subscriptionAvailability","all_tenants");
        artifact.setAttribute("overview_context","/prodcontext");
        artifact.setAttribute("overview_visibility","restricted");
        artifact.setAttribute("overview_cacheTimeout","300");
        artifact.setAttribute("overview_provider","admin");
        artifact.setAttribute("overview_visibleRoles","admin");
        artifact.setAttribute("apiCategories_categoryName","testcategory");
        artifact.setAttribute("overview_contextTemplate","/prodcontext/{version}");
        artifact.setAttribute("overview_thumbnail",null);
        artifact.setAttribute("overview_description","sample product");
        artifact.setAttribute("overview_technicalOwner",null);
        artifact.setAttribute("overview_type","APIProduct");
        artifact.setAttribute("overview_technicalOwnerEmail",null);
        artifact.setAttribute("overview_corsConfiguration","{\"corsConfigurationEnabled\":false,"
                + "\"accessControlAllowOrigins\":[\"*\"],\"accessControlAllowCredentials\":false,"
                + "\"accessControlAllowHeaders\":[\"authorization\",\"Access-Control-Allow-Origin\",\"Content-Type\","
                + "\"SOAPAction\",\"apikey\",\"testKey\"],\"accessControlAllowMethods\":[\"GET\",\"PUT\",\"POST\","
                + "\"DELETE\",\"PATCH\",\"OPTIONS\"]}");
        artifact.setAttribute("overview_versionType","context");
        artifact.setAttribute("overview_status","PUBLISHED");
        artifact.setAttribute("overview_businessOwner","productOwner");
        artifact.setAttribute("overview_version","1.0.0");
        artifact.setAttribute("overview_tenants",null);
        artifact.setAttribute("overview_responseCaching","Disabled");
        artifact.setAttribute("overview_tier","Bronze||Gold");
        artifact.setAttribute("overview_businessOwnerEmail","owner@test.com");
        artifact.setAttribute("isMonetizationEnabled","false");
        artifact.setAttribute("overview_enableStore","true");
        artifact.setAttribute("overview_enableSchemaValidation","false");
        artifact.setId("88e758b7-6924-4e9f-8882-431070b6492b");
        return artifact;
    }
    
}
