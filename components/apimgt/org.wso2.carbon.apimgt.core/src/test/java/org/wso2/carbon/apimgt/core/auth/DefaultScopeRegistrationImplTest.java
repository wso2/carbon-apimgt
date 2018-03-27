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

import com.google.gson.Gson;
import feign.Response;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.auth.dto.ScopeInfo;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DefaultScopeRegistrationImplTest {


    @Test
    public void testRegisterScope() throws KeyManagementException {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);
        Scope scopeInfo = new Scope();
        scopeInfo.setName("abc");
        scopeInfo.setDescription("cde");
        Mockito.when(defaultScopeRegistrationServiceStub.registerScope(getScopeInfo(scopeInfo))).thenReturn(Response
                .builder().status(201)
                .headers(new HashMap<>()).body(new Gson().toJson(scopeInfo), feign.Util.UTF_8).build());
        Scope scope = new Scope();
        scope.setName("abc");
        scope.setDescription("cde");
        Assert.assertTrue(defaultScopeRegistration.registerScope(scope));
    }

    @Test
    public void testScopeRegistrationFailed() {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);

        ScopeInfo scopeInfo = new ScopeInfo();
        scopeInfo.setName("abc");
        scopeInfo.setDescription("cde");
        Mockito.when(defaultScopeRegistrationServiceStub.registerScope(scopeInfo)).thenReturn(Response.builder()
                .status(400)
                .headers(new HashMap<>()).body(new Gson().toJson(scopeInfo), feign.Util.UTF_8).build());
        Scope scope = new Scope();
        scope.setName("abc");
        scope.setDescription("cde");
        try {
            defaultScopeRegistration.registerScope(scope);
            Assert.fail();
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getMessage(), "Scope Registration Failed");
        }
    }

    @Test
    public void testRetrievescope() throws KeyManagementException {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);
        ScopeInfo scopeInfo = new ScopeInfo();
        scopeInfo.setName("abc");
        scopeInfo.setDescription("cde");
        Mockito.when(defaultScopeRegistrationServiceStub.getScopeByName(scopeInfo.getName())).thenReturn(Response
                .builder()
                .status(200).headers(new HashMap<>()).body(new Gson().toJson(scopeInfo), feign.Util.UTF_8).build());
        Scope retrievedScope = defaultScopeRegistration.getScopeByName(scopeInfo.getName());
        Assert.assertEquals(scopeInfo.getName(), retrievedScope.getName());
        Assert.assertEquals(scopeInfo.getDescription(), retrievedScope.getDescription());
        Assert.assertEquals(Collections.emptyList(), retrievedScope.getBindings());
    }

    @Test
    public void testRetrieveScopeWithBindings() throws KeyManagementException {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);
        ScopeInfo scopeInfo = new ScopeInfo();
        scopeInfo.setName("abc");
        scopeInfo.setDescription("cde");
        List<String> list = new ArrayList<>();
        list.add("apim:api_create");
        scopeInfo.setBindings(list);
        Mockito.when(defaultScopeRegistrationServiceStub.getScopeByName(scopeInfo.getName())).thenReturn(Response
                .builder()
                .status(200).headers(new HashMap<>()).body(new Gson().toJson(scopeInfo), feign.Util.UTF_8).build());
        Scope retrievedScope = defaultScopeRegistration.getScopeByName(scopeInfo.getName());
        Assert.assertEquals(scopeInfo.getName(), retrievedScope.getName());
        Assert.assertEquals(scopeInfo.getDescription(), retrievedScope.getDescription());
        Assert.assertEquals(list, retrievedScope.getBindings());
    }

    @Test
    public void testRetrieveNonExistingScope() {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);

        Mockito.when(defaultScopeRegistrationServiceStub.getScopeByName("abc")).thenReturn(Response.builder()
                .status(404).headers(new HashMap<>()).build());
        try {
            defaultScopeRegistration.getScopeByName("abc");
            Assert.fail();
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler().getErrorCode(), 900981);

        }
    }


    @Test
    public void testRetrieveExistingScopeWhileBackendsendInternalServerError() {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);
        Mockito.when(defaultScopeRegistrationServiceStub.getScopeByName("abc")).thenReturn(Response.builder()
                .status(500).headers(new HashMap<>()).build());
        try {
            defaultScopeRegistration.getScopeByName("abc");
            Assert.fail();
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler().getErrorCode(), 900967);

        }
    }


    @Test
    public void testUpdateScope() throws KeyManagementException {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);
        ScopeInfo scopeInfo = new ScopeInfo();
        scopeInfo.setDescription("cde");
        scopeInfo.setDisplayName("abc");
        Scope scope = new Scope();
        scope.setName("abc");
        scope.setDescription("cde");
        Mockito.when(defaultScopeRegistrationServiceStub.updateScope(Mockito.any(ScopeInfo.class), Mockito.anyString()))
                .thenReturn(Response.builder().status(200).headers(new HashMap<>()).body(new Gson().toJson(scopeInfo)
                        , feign.Util.UTF_8).build());
        boolean status = defaultScopeRegistration.updateScope(scope);
        Assert.assertTrue(status);
    }


    @Test
    public void testUpdateScopeWhileAuthorizationServerThrowsInternalError() {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);
        ScopeInfo scopeInfo = new ScopeInfo();
        scopeInfo.setDescription("cde");
        scopeInfo.setDisplayName("abc");
        Scope scope = new Scope();
        scope.setName("abc");

        scope.setDescription("cde");
        Mockito.when(defaultScopeRegistrationServiceStub.updateScope(Mockito.any(ScopeInfo.class), Mockito.anyString
                ())).thenReturn(Response.builder().status(500).headers(new HashMap<>()).build());
        try {
            defaultScopeRegistration.updateScope(scope);
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler().getErrorCode(), 900967);
        }
    }

    @Test
    public void testDeleteScope() throws KeyManagementException {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);
        Mockito.when(defaultScopeRegistrationServiceStub.deleteScope("abc")).thenReturn(Response
                .builder()
                .status(200).headers(new HashMap<>()).build());
        boolean status = defaultScopeRegistration.deleteScope("abc");
        Assert.assertTrue(status);
    }

    @Test
    public void testDeleteScopeWhileAuthorizationServerThrowsInternalError() {
        DefaultScopeRegistrationServiceStub defaultScopeRegistrationServiceStub = Mockito.mock
                (DefaultScopeRegistrationServiceStub.class);
        DefaultScopeRegistrationImpl defaultScopeRegistration = new DefaultScopeRegistrationImpl
                (defaultScopeRegistrationServiceStub);

        Mockito.when(defaultScopeRegistrationServiceStub.deleteScope("abc")).thenReturn(Response
                .builder().status(500).headers(new HashMap<>()).build());
        try {
            boolean status = defaultScopeRegistration.deleteScope("abc");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler().getErrorCode(), 900983);
        }
    }

    private ScopeInfo getScopeInfo(Scope scope) {
        ScopeInfo scopeInfo = new ScopeInfo();
        scopeInfo.setName(scope.getName());
        scopeInfo.setDescription(scope.getDescription());
        scopeInfo.setBindings(scope.getBindings());
        return scopeInfo;
    }
}
