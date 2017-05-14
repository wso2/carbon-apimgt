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

import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.auth.SCIMServiceStub;
import org.wso2.carbon.apimgt.core.auth.SCIMServiceStubFactory;
import org.wso2.carbon.apimgt.core.auth.dto.SCIMUser;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * API Manager default implementation of {@link IdentityProvider}
 */
public class DefaultIdentityProviderImpl extends DefaultKeyManagerImpl implements IdentityProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultIdentityProviderImpl.class);

    private SCIMServiceStub scimServiceStub;
    private static final String FILTER_PREFIX = "displayName Eq ";
    private static final String HOME_EMAIL = "home";

    public DefaultIdentityProviderImpl() throws APIManagementException {
        scimServiceStub = SCIMServiceStubFactory.getSCIMServiceStub();
    }

    protected DefaultIdentityProviderImpl(SCIMServiceStub scimServiceStub) {
        this.scimServiceStub = scimServiceStub;
    }

    @Override
    public List<String> getRolesOfUser(String userId) throws IdentityProviderException {
        List<String> roleNames = new ArrayList<>();
        SCIMUser scimUser = scimServiceStub.getUser(userId);
        if (scimUser != null) {
            List<org.wso2.carbon.apimgt.core.auth.dto.SCIMUser.SCIMUserGroups> roles = scimUser.getGroups();
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
        return scimServiceStub.searchGroups(FILTER_PREFIX + roleName).status() == 200;
    }

    @Override
    public void registerUser(User user) throws IdentityProviderException {
        SCIMUser scimUser = new SCIMUser();
        scimUser.setUsername(user.getUsername());
        scimUser.setPassword(user.getPassword());
        scimUser.setName(new SCIMUser.SCIMName(user.getFirstName(), user.getLastName()));
        List<SCIMUser.SCIMUserEmails> emails = new ArrayList<>();
        emails.add(new SCIMUser.SCIMUserEmails(user.getEmail(), HOME_EMAIL, true));
        scimUser.setEmails(emails);
        Response response = scimServiceStub.addUser(scimUser);
        if (response == null || response.status() != 201) {
            String errorMessage = "Error occurred while creating user. "
                    + ((response == null) ? "response is null" : "Status Code: " + response.status() + ' '
                    + response.body().toString());
            log.error(errorMessage);
            throw new IdentityProviderException(errorMessage, ExceptionCodes.USER_CREATION_FAILED);
        }
    }

}
