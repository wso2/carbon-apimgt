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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.auth.DCRMServiceStub;
import org.wso2.carbon.apimgt.core.auth.DCRMServiceStubFactory;
import org.wso2.carbon.apimgt.core.auth.OAuth2ServiceStubs;
import org.wso2.carbon.apimgt.core.auth.OAuth2ServiceStubsFactory;
import org.wso2.carbon.apimgt.core.auth.SCIMServiceStub;
import org.wso2.carbon.apimgt.core.auth.SCIMServiceStubFactory;
import org.wso2.carbon.apimgt.core.auth.dto.SCIMGroup;
import org.wso2.carbon.apimgt.core.auth.dto.SCIMUser;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * API Manager default implementation of {@link IdentityProvider}
 */
public class DefaultIdentityProviderImpl extends DefaultKeyManagerImpl implements IdentityProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultIdentityProviderImpl.class);

    private SCIMServiceStub scimServiceStub;
    private static final String FILTER_PREFIX_USER = "userName Eq ";
    private static final String FILTER_PREFIX_ROLE = "displayName Eq ";
    private static final String HOME_EMAIL = "home";

    DefaultIdentityProviderImpl() throws APIManagementException {
        this(SCIMServiceStubFactory.getSCIMServiceStub(), DCRMServiceStubFactory.getDCRMServiceStub(),
                OAuth2ServiceStubsFactory.getOAuth2ServiceStubs());
    }

    DefaultIdentityProviderImpl(SCIMServiceStub scimServiceStub, DCRMServiceStub dcrmServiceStub,
                                OAuth2ServiceStubs oAuth2ServiceStubs) throws APIManagementException {
        super(dcrmServiceStub, oAuth2ServiceStubs);
        this.scimServiceStub = scimServiceStub;
    }

    @Override
    public String getIdOfUser(String userName) throws IdentityProviderException {
        Response user = scimServiceStub.searchUsers(FILTER_PREFIX_USER + userName);
        String userId;
        if (user.status() == 200) {
            String responseBody = user.body().toString();
            JsonParser parser = new JsonParser();
            JsonObject parsedResponseBody = (JsonObject) parser.parse(responseBody);
            JsonArray userList = (JsonArray) parsedResponseBody.get("Resources");
            if (userList.size() == 1) {
                JsonObject scimUser = (JsonObject) userList.get(0);
                userId = scimUser.get("id").getAsString();
            } else {
                String errorMessage = "Multiple users with " + userName + " exist.";
                log.error(errorMessage);
                throw new IdentityProviderException(errorMessage, ExceptionCodes.MULTIPLE_USERS_EXIST);
            }
        } else {
            String errorMessage = "User " + userName + " does not exist in the system.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.USER_DOES_NOT_EXIST);
        }
        return userId;
    }

    @Override
    public List<String> getRoleNamesOfUser(String userId) throws IdentityProviderException {
        List<String> roleNames = new ArrayList<>();
        SCIMUser scimUser = scimServiceStub.getUser(userId);
        if (scimUser != null) {
            List<SCIMUser.SCIMUserGroups> roles = scimUser.getGroups();
            if (roles != null) {
                roles.forEach(role -> roleNames.add(role.getDisplay()));
            }
        } else {
            String errorMessage = "User id " + userId + " does not exist in the system.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.USER_DOES_NOT_EXIST);
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
        SCIMUser scimUser = scimServiceStub.getUser(userId);
        if (scimUser != null) {
            List<SCIMUser.SCIMUserGroups> roles = scimUser.getGroups();
            if (roles != null) {
                roles.forEach(role -> roleIds.add(role.getValue()));
            }
        } else {
            String errorMessage = "User id " + userId + " does not exist in the system.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.USER_DOES_NOT_EXIST);
        }
        return roleIds;
    }

    @Override
    public String getRoleId(String roleName) throws IdentityProviderException {
        Response role = scimServiceStub.searchGroups(FILTER_PREFIX_ROLE + roleName);
        String roleId;
        if (role.status() == 200) {
            String responseBody = role.body().toString();
            JsonParser parser = new JsonParser();
            JsonObject parsedResponseBody = (JsonObject) parser.parse(responseBody);
            JsonArray roleList = (JsonArray) parsedResponseBody.get("Resources");
            if (roleList.size() == 1) {
                JsonObject scimGroup = (JsonObject) roleList.get(0);
                roleId = scimGroup.get("id").getAsString();
            } else {
                String errorMessage = "More than one role with role name " + roleName + " exist.";
                log.error(errorMessage);
                throw new IdentityProviderException(errorMessage, ExceptionCodes.MULTIPLE_ROLES_EXIST);
            }
        } else {
            String errorMessage = "Role with name " + roleName + " does not exist in the system.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.ROLE_DOES_NOT_EXIST);
        }
        return roleId;
    }

    @Override
    public String getRoleName(String roleId) throws IdentityProviderException {
        SCIMGroup scimGroup = scimServiceStub.getGroup(roleId);
        String displayName;
        if (scimGroup != null) {
            displayName = scimGroup.getDisplayName();
        } else {
            String errorMessage = "Role with role Id " + roleId + " does not exist in the system.";
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.ROLE_DOES_NOT_EXIST);
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
        }
    }

    private String getErrorMessage(Response response) {
        StringBuilder errorMessage = new StringBuilder("");
        if (response != null && response.body() != null) {
            try {
                String errorDescription = new Gson().fromJson(response.body().toString(), JsonElement.class)
                        .getAsJsonObject().get("Errors").getAsJsonArray().get(0).getAsJsonObject()
                        .get("description").getAsString();
                errorMessage.append(errorDescription);
            } catch (Exception ex) {
                log.error("Error occurred while parsing error response", ex);
            }
        }
        return errorMessage.toString();
    }

}
