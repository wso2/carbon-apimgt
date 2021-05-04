/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.EmptyCallbackURLForCodeGrantsException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.dto.TokenHandlingDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mostly common features of  keyManager implementations will be handle here.
 * This class should be extended by Key manager implementation class.
 */
public abstract class AbstractKeyManager implements KeyManager {
    private static Log log = LogFactory.getLog(AbstractKeyManager.class);
    protected KeyManagerConfiguration configuration;
    protected String tenantDomain;
    public AccessTokenRequest buildAccessTokenRequestFromJSON(String jsonInput, AccessTokenRequest tokenRequest)
            throws APIManagementException {

        if (jsonInput == null || jsonInput.isEmpty()) {
            log.debug("JsonInput is null or Empty.");
            return tokenRequest;
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        if (tokenRequest == null) {
            log.debug("Input request is null. Creating a new Request Object.");
            tokenRequest = new AccessTokenRequest();
        }

        try {
            jsonObject = (JSONObject) parser.parse(jsonInput);
            // Getting parameters from input string and setting in TokenRequest.
            if (jsonObject != null && !jsonObject.isEmpty()) {
                Map<String, Object> params = (Map<String, Object>) jsonObject;

                if (null != params.get(ApplicationConstants.OAUTH_CLIENT_ID)) {
                    tokenRequest.setClientId((String) params.get(ApplicationConstants.OAUTH_CLIENT_ID));
                }

                if (null != params.get(ApplicationConstants.OAUTH_CLIENT_SECRET)) {
                    tokenRequest.setClientSecret((String) params.get(ApplicationConstants.OAUTH_CLIENT_SECRET));
                }

                if (null != params.get(ApplicationConstants.VALIDITY_PERIOD)) {
                    tokenRequest.setValidityPeriod(Long.parseLong((String) params.get(ApplicationConstants.VALIDITY_PERIOD)));
                }

                return tokenRequest;
            }
        } catch (ParseException e) {
            handleException("Error occurred while parsing JSON String", e);
        }
        return null;
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws APIManagementException {

        return true;
    }

    /**
     * This method will accept json String and will do the json parse will set oAuth application properties to OAuthApplicationInfo object.
     *
     * @param jsonInput this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will be return.
     * @throws APIManagementException
     */
    public OAuthApplicationInfo buildFromJSON(OAuthApplicationInfo oAuthApplicationInfo, String jsonInput) throws
            APIManagementException {

        //initiate json parser.
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        try {
            //parse json String
            jsonObject = (JSONObject) parser.parse(jsonInput);
            if (jsonObject != null) {
                //create a map to hold json parsed objects.
                Map<String, Object> params = (Map) jsonObject;
                if (params.get(APIConstants.JSON_CALLBACK_URL) != null) {
                    oAuthApplicationInfo.setCallBackURL((String) params.get(APIConstants.JSON_CALLBACK_URL));
                }
                if (params.get(APIConstants.JSON_GRANT_TYPES) != null) {
                    String grantTypeString = params.get(APIConstants.JSON_GRANT_TYPES).toString();
                    if (StringUtils.isEmpty(oAuthApplicationInfo.getCallBackURL()) &&
                            (grantTypeString.contains("implicit") || grantTypeString.contains("authorization_code"))) {
                        throw new EmptyCallbackURLForCodeGrantsException("The callback url must have at least one URI "
                                + "value when using Authorization code or implicit grant types.");
                    }
                }
                //set client Id
                if (params.get(APIConstants.JSON_CLIENT_ID) != null) {
                    oAuthApplicationInfo.setClientId((String) params.get(APIConstants.JSON_CLIENT_ID));
                }
                //set client secret
                if (params.get(APIConstants.JSON_CLIENT_SECRET) != null) {
                    oAuthApplicationInfo.setClientSecret((String) params.get(APIConstants.JSON_CLIENT_SECRET));
                }
                //copy all params map in to OAuthApplicationInfo's Map object.
                oAuthApplicationInfo.putAll(params);
                validateOAuthAppCreationProperties(oAuthApplicationInfo);
                return oAuthApplicationInfo;
            }
        } catch (ParseException e) {
            handleException("Error occurred while parsing JSON String", e);
        }
        return null;
    }

    public AccessTokenRequest buildAccessTokenRequestFromOAuthApp(OAuthApplicationInfo oAuthApplication,
                                                                  AccessTokenRequest tokenRequest) throws
            APIManagementException {
        if (oAuthApplication == null) {
            return tokenRequest;
        }
        if (tokenRequest == null) {
            tokenRequest = new AccessTokenRequest();
        }

        if (oAuthApplication.getClientId() == null || oAuthApplication.getClientSecret() == null) {
            throw new APIManagementException("Consumer key or Consumer Secret missing.");
        }
        tokenRequest.setClientId(oAuthApplication.getClientId());
        tokenRequest.setClientSecret(oAuthApplication.getClientSecret());


        if (oAuthApplication.getParameter("tokenScope") != null) {
            String[] tokenScopes = (String[]) oAuthApplication.getParameter("tokenScope");
            tokenRequest.setScope(tokenScopes);
            oAuthApplication.addParameter("tokenScope", Arrays.toString(tokenScopes));
        }

        if (oAuthApplication.getParameter(ApplicationConstants.VALIDITY_PERIOD) != null) {
            tokenRequest.setValidityPeriod(Long.parseLong((String) oAuthApplication.getParameter(ApplicationConstants
                    .VALIDITY_PERIOD)));
        }

        return tokenRequest;
    }

    @Override
    public boolean canHandleToken(String accessToken) throws APIManagementException {

        boolean result = false;
        boolean canHandle = false;
        Object tokenHandlingScript = configuration.getParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING);
        if (tokenHandlingScript != null && StringUtils.isNotEmpty((String) tokenHandlingScript)) {
            TokenHandlingDto[] tokenHandlers = new Gson().fromJson(
                    (String) tokenHandlingScript, TokenHandlingDto[].class);
            if (tokenHandlers.length == 0) {
                return true;
            }
            for (TokenHandlingDto tokenHandler : tokenHandlers) {
                if (tokenHandler.getEnable()) {
                    if (TokenHandlingDto.TypeEnum.REFERENCE.equals(tokenHandler.getType())) {
                        if (tokenHandler.getValue() != null &&
                                StringUtils.isNotEmpty(String.valueOf(tokenHandler.getValue()))) {
                            Pattern pattern = Pattern.compile((String) tokenHandler.getValue());
                            Matcher matcher = pattern.matcher(accessToken);
                            canHandle = matcher.find();
                        }
                    } else if (TokenHandlingDto.TypeEnum.JWT.equals(tokenHandler.getType()) &&
                            accessToken.contains(APIConstants.DOT)) {
                        Map<String, Map<String, String>> validationJson =
                                (Map<String, Map<String, String>>) tokenHandler.getValue();
                        try {
                            SignedJWT signedJWT = SignedJWT.parse(accessToken);
                            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
                            for (Map.Entry<String, Map<String, String>> entry : validationJson.entrySet()) {
                                if (APIConstants.KeyManager.VALIDATION_ENTRY_JWT_BODY.equals(entry.getKey())) {
                                    boolean state = false;
                                    for (Map.Entry<String, String> e : entry.getValue().entrySet()) {
                                        String key = e.getKey();
                                        String value = e.getValue();
                                        Object claimValue = jwtClaimsSet.getClaim(key);
                                        if (claimValue != null) {
                                            Pattern pattern = Pattern.compile(value);
                                            Matcher matcher = pattern.matcher((String) claimValue);
                                            state = matcher.find();
                                        } else {
                                            state = false;
                                        }
                                    }
                                    canHandle = state;
                                }
                            }
                        } catch (java.text.ParseException e) {
                            log.warn("Error while parsing Token", e);
                        }
                    }
                    if (canHandle) {
                        result = true;
                        break;
                    }
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    @Override
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * common method to throw exceptions.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws APIManagementException
     */
    protected void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }

    protected void validateOAuthAppCreationProperties(OAuthApplicationInfo oAuthApplicationInfo)
            throws APIManagementException {

        String type = getType();
        if (!APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(type)) {

            List<String> missedRequiredValues = new ArrayList<>();
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration =
                    ServiceReferenceHolder.getInstance().getKeyManagerConnectorConfiguration(type);
            if (keyManagerConnectorConfiguration != null) {
                List<ConfigurationDto> applicationConfigurationDtoList =
                        keyManagerConnectorConfiguration.getApplicationConfigurations();
                Object additionalProperties =
                        oAuthApplicationInfo.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES);
                if (additionalProperties != null) {
                    JsonObject additionalPropertiesJson =
                            (JsonObject) new JsonParser().parse((String) additionalProperties);
                    for (ConfigurationDto configurationDto : applicationConfigurationDtoList) {
                        JsonElement value = additionalPropertiesJson.get(configurationDto.getName());
                        if (value == null) {
                            if (configurationDto.isRequired()) {
                                missedRequiredValues.add(configurationDto.getName());
                            }
                        }
                    }
                    if (!missedRequiredValues.isEmpty()) {
                        throw new APIManagementException("Missing required properties to create/update oauth " +
                                "application",
                                ExceptionCodes.KEY_MANAGER_MISSING_REQUIRED_PROPERTIES_IN_APPLICATION);
                    }
                }
            } else {
                throw new APIManagementException("Invalid Key Manager Type " + type,
                        ExceptionCodes.KEY_MANAGER_NOT_FOUND);
            }
        }
    }
}
