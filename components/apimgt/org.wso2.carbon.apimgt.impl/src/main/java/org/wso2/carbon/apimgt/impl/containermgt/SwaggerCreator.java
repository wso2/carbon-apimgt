/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.containermgt;

import io.swagger.models.SwaggerVersion;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;

import java.util.*;


/**
 * This class extends the OAS3Parser class in order to override its method
 * "getOASDefinitionForPrivateJetMode".
 */
public class SwaggerCreator {

    private static final Log log = LogFactory.getLog(SwaggerCreator.class);
    private static final String OPENAPI_SECURITY_SCHEMA_KEY = "default";
    private boolean securityOauth2 = false;
    private boolean securityBasicAuth = false;
    private String basicSecurityName;
    private String jwtSecurityName;
    private String oauthSecurityName;

    public SwaggerCreator(String basicSecurityName, String jwtSecurityName, String oauthSecurityName) {

        this.basicSecurityName = basicSecurityName;
        this.jwtSecurityName = jwtSecurityName;
        this.oauthSecurityName = oauthSecurityName;
    }

    public boolean isSecurityOauth2() {
        return securityOauth2;
    }

    public boolean isSecurityBasicAuth() {
        return securityBasicAuth;
    }

    /**
     * This method returns the swagger definition of an api
     * which suits for k8s_apim_operator
     *
     * @param api               API
     * @param oasDefinition     Open API definition
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     * @throws ParseException         throws if the oasDefinition is not in json format
     */

    public String getOASDefinitionForPrivateJetMode(API api, String oasDefinition)
            throws APIManagementException, ParseException {

        APIDefinition oasParser = OASParserUtil.getOASParser(oasDefinition);
        String apiDefinition = oasParser.getOASDefinitionForPublisher(api, oasDefinition);

        OASParserUtil.SwaggerVersion swaggerVersion = OASParserUtil.getSwaggerVersion(apiDefinition);
        if (swaggerVersion == OASParserUtil.SwaggerVersion.SWAGGER) {
            //parsing swagger 2.0 to openAPI 3.0.1
            OpenAPIParser openAPIParser = new OpenAPIParser();
            SwaggerParseResult swaggerParseResult = openAPIParser.readContents(apiDefinition, null, null);
            if (CollectionUtils.isNotEmpty(swaggerParseResult.getMessages())) {
                log.debug("Errors found when parsing OAS definition");
            }
            OpenAPI openAPI = swaggerParseResult.getOpenAPI();
            apiDefinition = Json.pretty(openAPI);
        }

        //get Json object from parsed openAPI definition
        JSONParser jsonParser = new JSONParser();
        JSONObject apiDefinitionJsonObject = (JSONObject) jsonParser.parse(apiDefinition);

        /**
         * Removing the "security" key from the JSONObject
         */
        apiDefinitionJsonObject.remove(ContainerBasedConstants.SECURITY);
        ((JSONObject) ((JSONObject) apiDefinitionJsonObject.get(ContainerBasedConstants.COMPONENTS))
                .get(ContainerBasedConstants.SECURITY_SCHEMES)).remove(ContainerBasedConstants.DEFAULT);
        Set<String> paths = ((JSONObject) apiDefinitionJsonObject.get(ContainerBasedConstants.PATHS)).keySet();
        Iterator iterator = paths.iterator();

        /**
         * Removing the "security" attribute from each RESTAPI verb of each path in the swagger
         */
        while (iterator.hasNext()) {
            String path = (String) iterator.next();
            Set verbs = ((JSONObject) ((JSONObject) apiDefinitionJsonObject.get(ContainerBasedConstants.PATHS))
                    .get(path)).keySet();
            Iterator verbIterator = verbs.iterator();
            while (verbIterator.hasNext()) {
                String verb = (String) verbIterator.next();
                ((JSONObject) ((JSONObject) ((JSONObject) apiDefinitionJsonObject.get(ContainerBasedConstants.PATHS)).
                        get(path)).get(verb)).remove(ContainerBasedConstants.SECURITY);
            }
        }

        String securityType = api.getApiSecurity()
                .replace(ContainerBasedConstants.OAUTH_BASICAUTH_APIKEY_MANDATORY, "");
        Boolean securityTypeOauth2 = isAPISecurityTypeOauth2(securityType);
        Boolean securityTypeBasicAuth = isAPISecurityBasicAuth(securityType);

        if (basicSecurityName != null && securityTypeBasicAuth && !securityTypeOauth2 && !"".equals(basicSecurityName)) {

            SecurityRequirement basicOauthSecurityReq = referBasicAuthInSwagger(basicSecurityName);
            List<SecurityRequirement> basicAuth = new ArrayList<SecurityRequirement>();
            basicAuth.add(basicOauthSecurityReq);
            apiDefinitionJsonObject.put(ContainerBasedConstants.SECURITY, basicAuth);
        } else if (securityTypeOauth2 && !securityTypeBasicAuth) {

            if (oauthSecurityName != null && !"".equals(oauthSecurityName) || jwtSecurityName != null && !"".equals(jwtSecurityName)) {

                SecurityRequirement oauth2SecurityReq = referOauth2InSwagger(oauthSecurityName, jwtSecurityName);
                List<SecurityRequirement> oauth2 = new ArrayList<SecurityRequirement>();
                oauth2.add(oauth2SecurityReq);
                apiDefinitionJsonObject.put(ContainerBasedConstants.SECURITY, oauth2);
            }
        } else if (securityTypeBasicAuth && securityTypeOauth2) {

            if (oauthSecurityName != null && !"".equals(oauthSecurityName) ||
                    basicSecurityName != null && !"".equals(basicSecurityName) ||
                    jwtSecurityName != null && !"".equals(jwtSecurityName)) {
                List<SecurityRequirement> basicOauthJWT = new ArrayList<SecurityRequirement>();
                SecurityRequirement basicOauthJWTSecurityReq = referBasicOAuth2JWTInSwagger(basicSecurityName,
                        oauthSecurityName, jwtSecurityName);
                basicOauthJWT.add(basicOauthJWTSecurityReq);
                apiDefinitionJsonObject.put(ContainerBasedConstants.SECURITY, basicOauthJWT);
            }
        }
        return Json.pretty(apiDefinitionJsonObject);
    }

    private Boolean isAPISecurityTypeOauth2(String apiSecurity) {
        if (apiSecurity.contains(ContainerBasedConstants.OAUTH2)) {
            this.securityOauth2 = true;
            return true;
        }
        return false;
    }

    private Boolean isAPISecurityBasicAuth(String apiSecurity) {
        if (apiSecurity.contains(ContainerBasedConstants.BASIC_AUTH)) {
            this.securityBasicAuth = true;
            return true;
        }
        return false;
    }

    /**
     * Refers the provided OAuth2 and/or JWT security CR name in the swagger
     * @param oauthSecurityName , OAuth2 Security Custom Resource Name
     * @param jwtSecurityName , JWT Security Custom Resource Name
     * @return
     */
    private SecurityRequirement referOauth2InSwagger(String oauthSecurityName, String jwtSecurityName) {

        SecurityRequirement securityRequirement = new SecurityRequirement();
        if (oauthSecurityName != null && jwtSecurityName != null &&
                !"".equals(oauthSecurityName) && !"".equals(jwtSecurityName)) {
            securityRequirement.addList(oauthSecurityName, new ArrayList<String>());
            securityRequirement.addList(jwtSecurityName, new ArrayList<String>());

        } else if (oauthSecurityName != null && jwtSecurityName != null &&  !"".equals(oauthSecurityName)
                && "".equals(jwtSecurityName)) {
            securityRequirement.addList(oauthSecurityName, new ArrayList<String>());

        } else if (oauthSecurityName != null && jwtSecurityName != null && "".equals(oauthSecurityName)
                && !"".equals(jwtSecurityName)) {
            securityRequirement.addList(jwtSecurityName, new ArrayList<String>());
        }
        return securityRequirement;
    }

    /**
     * Refers the provided BasicAuth security CR name in the swagger
     * @param basicSecurityName , BasicAuth Security Custom Resource Name
     * @return
     */
    private SecurityRequirement referBasicAuthInSwagger(String basicSecurityName) {

        SecurityRequirement securityRequirement = new SecurityRequirement();
        if(basicSecurityName != null){
            securityRequirement.addList(basicSecurityName, new ArrayList<String>());
        }
        return securityRequirement;
    }

    /**
     * Refers the provided BasicAuth, JWT and OAuth2 security CR name in the swagger
     * @param basicSecurityName , BasicAuth Security Custom Resource Name
     * @param oauthSecurityName , OAuth2 Security Custom Resource Name
     * @param jwtSecurityName , JWT Security Custom Resource Name
     * @return
     */
    private SecurityRequirement referBasicOAuth2JWTInSwagger(String basicSecurityName, String oauthSecurityName,
                                                             String jwtSecurityName) {
        SecurityRequirement securityRequirement = referOauth2InSwagger(oauthSecurityName, jwtSecurityName);
        if (basicSecurityName != null && !"".equals(basicSecurityName)) {
            securityRequirement.addList(basicSecurityName, new ArrayList<String>());
        }

        return securityRequirement;
    }

}
