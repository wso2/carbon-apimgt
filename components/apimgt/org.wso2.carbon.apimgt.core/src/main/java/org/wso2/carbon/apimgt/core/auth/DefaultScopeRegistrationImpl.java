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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.core.auth;

import feign.Response;
import feign.gson.GsonDecoder;
import org.wso2.carbon.apimgt.core.auth.dto.ScopeInfo;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.IOException;
import java.util.Collections;
/**
 * Scope Registration and Management Implementation for WSO2 IS.
 */
public class DefaultScopeRegistrationImpl implements ScopeRegistration {
    private DefaultScopeRegistrationServiceStub scopeRegistrationServiceStub;

    public DefaultScopeRegistrationImpl(DefaultScopeRegistrationServiceStub scopeRegistrationServiceStub) {
        this.scopeRegistrationServiceStub = scopeRegistrationServiceStub;
    }

    @Override
    public boolean registerScope(Scope scope) throws KeyManagementException {
        ScopeInfo scopeInfo = getScopeInfo(scope);

        Response response = scopeRegistrationServiceStub.registerScope(scopeInfo);
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_201_CREATED) {
            return true;
        } else {
            throw new KeyManagementException("Scope Registration Failed", ExceptionCodes.SCOPE_REGISTRATION_FAILED);
        }
    }

    @Override
    public Scope getScopeByName(String name) throws KeyManagementException {
        Response response = scopeRegistrationServiceStub.getScopeByName(name);
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            try {
                return getScope(response);
            } catch (IOException e) {
                throw new KeyManagementException("Couldn't retrieve Scope", e, ExceptionCodes.INTERNAL_ERROR);
            }
        } else if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_404_NOT_FOUND) {
            throw new KeyManagementException("Couldn't retrieve Scope " + name, ExceptionCodes.SCOPE_NOT_FOUND);
        } else {
            throw new KeyManagementException("Couldn't find Scope", ExceptionCodes.INTERNAL_ERROR);
        }
    }

    @Override
    public boolean updateScope(Scope scope) throws KeyManagementException {
        ScopeInfo scopeInfo = getScopeInfo(scope);
        Response response = scopeRegistrationServiceStub.updateScope(scopeInfo, scope.getName());
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            return true;
        } else {
            throw new KeyManagementException("Scope update failed", ExceptionCodes.INTERNAL_ERROR);
        }
    }


    @Override
    public boolean deleteScope(String name) throws KeyManagementException {
        Response response = scopeRegistrationServiceStub.deleteScope(name);
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            return true;
        } else {
            throw new KeyManagementException("Scope deletion failed" + name, ExceptionCodes.SCOPE_DELETE_FAILED);
        }
    }

    @Override
    public boolean isScopeExist(String name) {
        int status = scopeRegistrationServiceStub.isScopeExist(name).status();
        return status == APIMgtConstants.HTTPStatusCodes.SC_200_OK;
    }

    private ScopeInfo getScopeInfo(Scope scope) {
        ScopeInfo scopeInfo = new ScopeInfo();
        scopeInfo.setName(scope.getName());
        scopeInfo.setDescription(scope.getDescription());
        scopeInfo.setBindings(scope.getBindings());
        return scopeInfo;
    }

    private Scope getScope(Response response) throws IOException {
        Scope scope = new Scope();
        ScopeInfo scopeInfoResponse = (ScopeInfo) new GsonDecoder().decode(response, ScopeInfo.class);
        scope.setName(scopeInfoResponse.getName());
        scope.setDescription(scopeInfoResponse.getDescription());
        if (scopeInfoResponse.getBindings() != null) {
            scope.setBindings(scopeInfoResponse.getBindings());
        } else {
            scope.setBindings(Collections.emptyList());
        }
        return scope;
    }
}
