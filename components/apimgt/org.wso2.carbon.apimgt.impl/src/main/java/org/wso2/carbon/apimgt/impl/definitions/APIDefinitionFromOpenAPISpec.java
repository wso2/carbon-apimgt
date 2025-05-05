/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

// Keep following function since it is still used in jaggery publisher portal. Can remove this class once publisher
// portal is removed.
@Deprecated
public class APIDefinitionFromOpenAPISpec {

    private static final Log log = LogFactory.getLog(APIDefinitionFromOpenAPISpec.class);

    /**
     * This method returns the oauth scopes according to the given swagger
     *
     * @param resourceConfigsJSON resource json
     * @return scope set
     * @throws APIManagementException
     */
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        JSONObject swaggerObject;
        JSONParser parser = new JSONParser();
        try {
            swaggerObject = (JSONObject) parser.parse(resourceConfigsJSON);

            //Check whether security definitions are defined or not
            if (swaggerObject.get(APIConstants.SWAGGER_X_WSO2_SECURITY) != null) {
                JSONObject securityDefinitionsObjects = (JSONObject) swaggerObject.get(APIConstants.SWAGGER_X_WSO2_SECURITY);

                for (JSONObject securityDefinition : (Iterable<JSONObject>) securityDefinitionsObjects.values()) {
                    //Read scopes from custom wso2 scopes

                    if (securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES) != null) {
                        JSONArray oauthScope = (JSONArray) securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES);
                        for (Object anOauthScope : oauthScope) {
                            Scope scope = new Scope();
                            JSONObject scopeObj = (JSONObject) anOauthScope;
                            scope.setKey((String) scopeObj.get(APIConstants.SWAGGER_SCOPE_KEY));
                            scope.setName((String) scopeObj.get(APIConstants.SWAGGER_NAME));
                            scope.setDescription((String) scopeObj.get(APIConstants.SWAGGER_DESCRIPTION));
                            scope.setRoles(scopeObj.get(APIConstants.SWAGGER_ROLES).toString());

                            scopeList.add(scope);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }
        return scopeList;
    }
}
