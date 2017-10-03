/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RoleBasedScopesIssuerTestCase {

    @Test
    public void testGetPrefix() throws Exception {

        String ISSUER_PREFIX = "default";
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        Assert.assertEquals(ISSUER_PREFIX, roleBasedScopesIssuer.getPrefix());
    }

    @Test
    public void testGetAllowedScopes() throws Exception {

        ArrayList<String> scopeSkipList = new ArrayList<String>();
        ArrayList<String> requestedScopes = new ArrayList<String>();
        scopeSkipList.add("scope 1");
        scopeSkipList.add("scope 2");
        requestedScopes.add("scope 1");
        requestedScopes.add("scope 3");
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        List<String> authorizedScopes = roleBasedScopesIssuer.getAllowedScopes(scopeSkipList, requestedScopes);

        Assert.assertEquals(1, authorizedScopes.size());
        Assert.assertEquals("scope 1", authorizedScopes.get(0));
    }

    @Test
    public void testGetAllowedScopesWhenAuthorizedScopesEmpty() throws Exception {

        ArrayList<String> scopeSkipList = new ArrayList<String>();
        ArrayList<String> requestedScopes = new ArrayList<String>();
        scopeSkipList.add("scope 1");
        scopeSkipList.add("scope 2");
        requestedScopes.add("scope 3");
        requestedScopes.add("scope 4");
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        List<String> authorizedScopes = roleBasedScopesIssuer.getAllowedScopes(scopeSkipList, requestedScopes);

        Assert.assertEquals(1, authorizedScopes.size());
        Assert.assertEquals("default", authorizedScopes.get(0));
    }

}