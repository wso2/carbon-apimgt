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

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

        OAS3Parser oas3Parser = new OAS3Parser();
        String apiDefinition = oas3Parser.getOASDefinitionForPublisher(api, oasDefinition);
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

        if (securityTypeBasicAuth && !securityTypeOauth2 && !basicSecurityName.equals("")) {

            SecurityRequirement basicOauthSecurityReq = referBasicAuthInSwagger(basicSecurityName);
            List<SecurityRequirement> basicAuth = new ArrayList<SecurityRequirement>();
            basicAuth.add(basicOauthSecurityReq);
            apiDefinitionJsonObject.put(ContainerBasedConstants.SECURITY, basicAuth);
        } else if (securityTypeOauth2 && !securityTypeBasicAuth) {

            if (!oauthSecurityName.equals("") || !jwtSecurityName.equals("")) {

                SecurityRequirement oauth2SecurityReq = referOauth2InSwagger(oauthSecurityName, jwtSecurityName);
                List<SecurityRequirement> oauth2 = new ArrayList<SecurityRequirement>();
                oauth2.add(oauth2SecurityReq);
                apiDefinitionJsonObject.put(ContainerBasedConstants.SECURITY, oauth2);
            }
        } else if (securityTypeBasicAuth && securityTypeOauth2) {

            if (!oauthSecurityName.equals("") || !basicSecurityName.equals("") || !jwtSecurityName.equals("")) {
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

        if (!oauthSecurityName.equals("") && !jwtSecurityName.equals("")) {

            securityRequirement.addList(oauthSecurityName, new ArrayList<String>());
            securityRequirement.addList(jwtSecurityName, new ArrayList<String>());
        } else if (!oauthSecurityName.equals("") && jwtSecurityName.equals("")) {

            securityRequirement.addList(oauthSecurityName, new ArrayList<String>());
        } else if (oauthSecurityName.equals("") && !jwtSecurityName.equals("")) {

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
        securityRequirement.addList(basicSecurityName, new ArrayList<String>());

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

        if (!basicSecurityName.equals("")) {
            securityRequirement.addList(basicSecurityName, new ArrayList<String>());
        }

        return securityRequirement;
    }

}