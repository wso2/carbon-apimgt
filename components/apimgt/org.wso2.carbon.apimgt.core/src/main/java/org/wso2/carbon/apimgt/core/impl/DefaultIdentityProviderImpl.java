/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import feign.Response;
import feign.gson.GsonDecoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.UserNameMapper;
import org.wso2.carbon.apimgt.core.auth.SCIMServiceStub;
import org.wso2.carbon.apimgt.core.auth.SCIMServiceStubFactory;
import org.wso2.carbon.apimgt.core.auth.dto.SCIMGroup;
import org.wso2.carbon.apimgt.core.auth.dto.SCIMUser;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API Manager default implementation of {@link IdentityProvider}
 */
public class DefaultIdentityProviderImpl implements IdentityProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultIdentityProviderImpl.class);

    private SCIMServiceStub scimServiceStub;
    private UserNameMapper userNameMapper;
    private static final String FILTER_PREFIX_USER = "userName Eq ";
    private static final String FILTER_PREFIX_ROLE = "displayName Eq ";
    private static final String HOME_EMAIL = "home";
    private static final String RESOURCES = "Resources";
    private static final String ID = "id";
    private static final String EMPTY_STRING = "";
    private static final String USERNAME = "userName";
    private static final String GROUPNAME = "displayName";

    DefaultIdentityProviderImpl() throws APIManagementException {
        this(SCIMServiceStubFactory.getSCIMServiceStub(), APIManagerFactory.getInstance().getUserNameMapper());
    }

    DefaultIdentityProviderImpl(SCIMServiceStub scimServiceStub, UserNameMapper userNameMapper) throws
            APIManagementException {
        this.scimServiceStub = scimServiceStub;
        this.userNameMapper = userNameMapper;
    }

    @Override
    public String getIdOfUser(String userName) throws IdentityProviderException {
        //Retrieve User ID before call identity provider(as that was external call).
        //should not user id outside this domain and should not log that id.
        try {
            userName = userNameMapper.getLoggedInUserIDFromPseudoName(userName);
        } catch (APIManagementException e) {
            throw new IdentityProviderException(e.getMessage(), ExceptionCodes.USER_MAPPING_RETRIEVAL_FAILED);
        }
        Response userResponse = scimServiceStub.searchUsers(FILTER_PREFIX_USER + userName);
        String userId;
        if (userResponse == null) {
            String errorMessage =
                    "Error occurred while retrieving Id of user " + userName + ". Error : Response is null.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        if (userResponse.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            String responseBody = userResponse.body().toString();
            JsonParser parser = new JsonParser();
            JsonObject parsedResponseBody = (JsonObject) parser.parse(responseBody);
            JsonArray user = (JsonArray) parsedResponseBody.get(RESOURCES);
            JsonObject scimUser = (JsonObject) user.get(0);
            userId = scimUser.get(ID).getAsString();
            String message = "Id " + userId + " of user " + scimUser.get(USERNAME).getAsString()
                    + " is successfully retrieved from SCIM endpoint.";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
        } else {
            String errorMessage =
                    "Error occurred while retrieving Id of user " + userName + ". Error : " + getErrorMessage(
                            userResponse);
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        return userId;
    }

    @Override
    public String getEmailOfUser(String userId) throws IdentityProviderException {
        Response userResponse = scimServiceStub.getUser(userId);
        String userEmail;
        if (userResponse == null) {
            String errorMessage =
                    "Error occurred while retrieving Id of user " + userId + ". Error : Response is null.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        if (userResponse.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            String responseBody = userResponse.body().toString();
            JsonParser parser = new JsonParser();
            JsonObject parsedResponseBody = (JsonObject) parser.parse(responseBody);
            userEmail = parsedResponseBody.get("emails").toString().replaceAll("[\\[\\]\"]", "");

            log.debug("Email {} of user {} is successfully retrieved from SCIM endpoint.",
                    userEmail, parsedResponseBody.get(USERNAME).getAsString());

        } else {
            String errorMessage =
                    "Error occurred while retrieving Id of user " + userId + ". Error : " + getErrorMessage(
                            userResponse);
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        return userEmail;
    }

    @Override
    public List<String> getRoleNamesOfUser(String userId) throws IdentityProviderException {
        List<String> roleNames = new ArrayList<>();
        Response response = scimServiceStub.getUser(userId);
        if (response == null) {
            String errorMessage =
                    "Error occurred while retrieving user with Id " + userId + ". Error : Response is null.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        try {
            if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
                SCIMUser scimUser = (SCIMUser) new GsonDecoder().decode(response, SCIMUser.class);
                if (scimUser != null) {
                    List<SCIMUser.SCIMUserGroups> roles = scimUser.getGroups();
                    if (roles != null) {
                        roles.forEach(role -> roleNames.add(role.getDisplay()));
                        String message =
                                "Role names of user " + scimUser.getName() + " are successfully retrieved as " +
                                        StringUtils.join(roleNames, ", ") + ".";
                        if (log.isDebugEnabled()) {
                            log.debug(message);
                        }
                    }
                } else {
                    String errorMessage =
                            "Error occurred while retrieving user with user Id " + userId + " from SCIM endpoint. "
                                    + "Response body is null or empty.";
                    log.error(errorMessage);
                    throw new IdentityProviderException(
                            "Error occurred while retrieving user with user Id " + userId + " from SCIM endpoint. "
                                    + "Response body is null or empty.", ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
                }
            } else {
                String errorMessage =
                        "Error occurred while retrieving role names of user with Id " + userId + ". Error : " +
                                getErrorMessage(response);
                log.error(errorMessage);
                throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while parsing response from SCIM endpoint.";
            log.error(errorMessage);
            throw new IdentityProviderException("Error occurred while parsing response from SCIM endpoint for ", e,
                    ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        return roleNames;
    }

    @Override
    public boolean isValidRole(String roleName) {
        return scimServiceStub.searchGroups(FILTER_PREFIX_ROLE + roleName).status()
                == APIMgtConstants.HTTPStatusCodes.SC_200_OK;
    }

    @Override
    public List<String> getRoleIdsOfUser(String userId) throws IdentityProviderException {
        List<String> roleIds = new ArrayList<>();
        Response response = scimServiceStub.getUser(userId);
        if (response == null) {
            String errorMessage =
                    "Error occurred while retrieving user with Id " + userId + ". Error : Response is null.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        try {
            if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
                SCIMUser scimUser = (SCIMUser) new GsonDecoder().decode(response, SCIMUser.class);
                if (scimUser != null) {
                    List<SCIMUser.SCIMUserGroups> roles = scimUser.getGroups();
                    if (roles != null) {
                        roles.forEach(role -> roleIds.add(role.getValue()));
                        String message = "Role Ids of user " + scimUser.getName() + " are successfully retrieved as " +
                                StringUtils.join(roleIds, ", ") + ".";
                        if (log.isDebugEnabled()) {
                            log.debug(message);
                        }
                    }
                } else {
                    String errorMessage =
                            "Error occurred while retrieving user with user Id " + userId + " from SCIM endpoint. "
                                    + "Response body is null or empty.";
                    log.error(errorMessage);
                    throw new IdentityProviderException(
                            "Error occurred while retrieving user with user Id " + userId + " from SCIM endpoint. "
                                    + "Response body is null or empty.", ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
                }
            } else {
                String errorMessage =
                        "Error occurred while retrieving role Ids of user with Id " + userId + ". Error : " +
                                getErrorMessage(response);
                log.error(errorMessage);
                throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while parsing response from SCIM endpoint.";
            log.error(errorMessage);
            throw new IdentityProviderException("Error occurred while parsing response from SCIM endpoint for ", e,
                    ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        return roleIds;
    }

    @Override
    public String getRoleId(String roleName) throws IdentityProviderException {
        Response roleResponse = scimServiceStub.searchGroups(FILTER_PREFIX_ROLE + roleName);
        String roleId;
        if (roleResponse == null) {
            String errorMessage =
                    "Error occurred while retrieving Id of role " + roleName + ". Error : Response is null.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        if (roleResponse.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            String responseBody = roleResponse.body().toString();
            JsonParser parser = new JsonParser();
            JsonObject parsedResponseBody = (JsonObject) parser.parse(responseBody);
            JsonArray role = (JsonArray) parsedResponseBody.get(RESOURCES);
            JsonObject scimGroup = (JsonObject) role.get(0);
            roleId = scimGroup.get(ID).getAsString();
            String message = "Id " + roleId + " of role " + scimGroup.get(GROUPNAME).getAsString()
                    + " is successfully retrieved from SCIM endpoint.";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
        } else {
            String errorMessage =
                    "Error occurred while retrieving Id of role " + roleName + ". Error : " + getErrorMessage(
                            roleResponse);
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        return roleId;
    }

    @Override
    public String getRoleName(String roleId) throws IdentityProviderException {
        Response response = scimServiceStub.getGroup(roleId);
        if (response == null) {
            String errorMessage =
                    "Error occurred while retrieving name of role with Id " + roleId + ". Error : Response is null.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        String displayName;
        try {
            if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
                SCIMGroup scimGroup = (SCIMGroup) new GsonDecoder().decode(response, SCIMGroup.class);
                if (scimGroup != null) {
                    displayName = scimGroup.getDisplayName();
                    String message =
                            "Display name of role with Id " + roleId + " is successfully retrieved as " + displayName;
                    if (log.isDebugEnabled()) {
                        log.debug(message);
                    }
                } else {
                    String errorMessage =
                            "Error occurred while retrieving role name with role Id " + roleId + " from SCIM endpoint. "
                                    + "Response body is null or empty.";
                    log.error(errorMessage);
                    throw new IdentityProviderException(
                            "Error occurred while retrieving role name with role Id " + roleId + " from SCIM endpoint. "
                                    + "Response body is null or empty.", ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
                }
            } else {
                String errorMessage = "Error occurred while retrieving name of role with Id " + roleId + ". Error : "
                        + getErrorMessage(response);
                log.error(errorMessage);
                throw new IdentityProviderException(errorMessage, ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while parsing response from SCIM endpoint.";
            log.error(errorMessage);
            throw new IdentityProviderException("Error occurred while parsing response from SCIM endpoint for ", e,
                    ExceptionCodes.RESOURCE_RETRIEVAL_FAILED);
        }
        return displayName;
    }

    @Override
    public void registerUser(User user) throws IdentityProviderException {
        SCIMUser scimUser = new SCIMUser();
        scimUser.setUsername(user.getUsername());
        scimUser.setPassword(new String(user.getPassword()));
        scimUser.setName(new SCIMUser.SCIMName(user.getFirstName(), user.getLastName()));
        List<SCIMUser.SCIMUserEmails> emails = new ArrayList<>();
        emails.add(new SCIMUser.SCIMUserEmails(user.getEmail(), HOME_EMAIL, true));
        scimUser.setEmails(emails);
        Response response = scimServiceStub.addUser(scimUser);
        if (response == null || response.status() != APIMgtConstants.HTTPStatusCodes.SC_201_CREATED) {
            StringBuilder errorMessage = new StringBuilder("Error occurred while creating user. ");
            if (response == null) {
                errorMessage.append("Response is null");
            } else {
                String msg = getErrorMessage(response);
                if (!StringUtils.isEmpty(msg)) {
                    errorMessage.append(msg);
                }
            }
            throw new IdentityProviderException(errorMessage.toString(), ExceptionCodes.USER_CREATION_FAILED);
        } else {
            String message = "User  " + user.getUsername() + " is successfully created";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
        }
    }

    private String getErrorMessage(Response response) {
        StringBuilder errorMessage = new StringBuilder(EMPTY_STRING);
        if (response != null && response.body() != null) {
            try {
                String errorDescription = new Gson().fromJson(response.body().toString(), JsonElement.class)
                        .getAsJsonObject().get("Errors").getAsJsonArray().get(0).getAsJsonObject().get("description")
                        .getAsString();
                errorMessage.append(errorDescription);
            } catch (Exception ex) {
                log.error("Error occurred while parsing error response", ex);
            }
        }
        return errorMessage.toString();
    }

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest)
            throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().createApplication(oauthAppRequest);
    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthApplicationInfo oAuthApplicationInfo)
            throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().updateApplication(oAuthApplicationInfo);
    }

    @Override
    public void deleteApplication(String consumerKey) throws KeyManagementException {
        APIManagerFactory.getInstance().getKeyManager().deleteApplication(consumerKey);
    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().retrieveApplication(consumerKey);
    }

    @Override
    public AccessTokenInfo getNewAccessToken(AccessTokenRequest tokenRequest) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().getNewAccessToken(tokenRequest);
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().getTokenMetaData(accessToken);
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().getKeyManagerConfiguration();
    }

    @Override
    public void revokeAccessToken(String accessToken, String clientId, String clientSecret)
            throws KeyManagementException {
        APIManagerFactory.getInstance().getKeyManager().revokeAccessToken(accessToken, clientId, clientSecret);
    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws KeyManagementException {
        APIManagerFactory.getInstance().getKeyManager().loadConfiguration(configuration);
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().registerNewResource(api, resourceAttributes);
    }

    @Override
    public Map getResourceByApiId(String apiId) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().getResourceByApiId(apiId);
    }

    @Override
    public boolean updateRegisteredResource(API api, Map resourceAttributes) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().updateRegisteredResource(api, resourceAttributes);
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String apiID) throws KeyManagementException {

    }

    @Override
    public void deleteMappedApplication(String consumerKey) throws KeyManagementException {
        APIManagerFactory.getInstance().getKeyManager().deleteMappedApplication(consumerKey);
    }

    @Override
    public boolean registerScope(Scope scope) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().registerScope(scope);
    }

    @Override
    public Scope retrieveScope(String name) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().retrieveScope(name);
    }

    @Override
    public boolean updateScope(Scope scope) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().updateScope(scope);
    }

    @Override
    public boolean deleteScope(String name) throws KeyManagementException {
        return APIManagerFactory.getInstance().getKeyManager().deleteScope(name);
    }
}
