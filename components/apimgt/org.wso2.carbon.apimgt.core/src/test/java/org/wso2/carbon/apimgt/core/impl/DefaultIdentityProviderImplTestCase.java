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
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import feign.Response;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.auth.DCRMServiceStub;
import org.wso2.carbon.apimgt.core.auth.OAuth2ServiceStubs;
import org.wso2.carbon.apimgt.core.auth.SCIMServiceStub;
import org.wso2.carbon.apimgt.core.auth.dto.SCIMGroup;
import org.wso2.carbon.apimgt.core.auth.dto.SCIMUser;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.any;

public class DefaultIdentityProviderImplTestCase {

    @Test
    public void testGetRolesOfUser() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        SCIMUser user = new SCIMUser();
        String user1Id = "a42b4760-120d-432e-8042-4a7f12e3346c";
        user.setId(user1Id);
        ArrayList<SCIMUser.SCIMUserGroups> userGroups = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();
        roleNames.add("engineer");
        roleNames.add("team_lead");
        roleNames.add("support_engineer");
        String role1Id = "69d87bc2-b694-41b2-a5d3-4ff018e3f7d5";
        String role2Id = "978ab859-2cbc-4f6f-9510-92f44dc34215";
        String role3Id = "b62ae605-fca1-4975-9f2f-2a81ea47d8dc";
        SCIMUser.SCIMUserGroups role1 = new SCIMUser.SCIMUserGroups(role1Id, roleNames.get(0));
        userGroups.add(role1);
        SCIMUser.SCIMUserGroups role2 = new SCIMUser.SCIMUserGroups(role2Id, roleNames.get(1));
        userGroups.add(role2);
        SCIMUser.SCIMUserGroups role3 = new SCIMUser.SCIMUserGroups(role3Id, roleNames.get(2));
        userGroups.add(role3);
        user.setGroups(userGroups);

        Mockito.when(scimServiceStub.getUser(user1Id)).thenReturn(user);

        List<String> roles = idpImpl.getRolesOfUser(user1Id);
        Assert.assertEquals(roleNames.size(), roles.size());
        roles.forEach(roleName -> Assert.assertTrue(roleNames.contains(roleName)));

        String invalidUserId = "invalid-user-id";

        Mockito.when(scimServiceStub.getUser(invalidUserId)).thenReturn(null);

        try {
            idpImpl.getRolesOfUser(invalidUserId);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IdentityProviderException);
            Assert.assertEquals(ex.getMessage(), "User id " + invalidUserId + " does not exist in the system.");
        }
    }

    @Test
    public void testIsValidRole() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        final String validRole = "engineer";
        final String validRoleSearchQuery = "displayName Eq " + validRole;
        Response okResponse = Response.builder().status(200).headers(new HashMap<>()).build();
        Mockito.when(scimServiceStub.searchGroups(validRoleSearchQuery)).thenReturn(okResponse);

        Assert.assertTrue(idpImpl.isValidRole(validRole));

        final String invalidRole = "invalid-role";
        final String invalidRoleSearchQuery = "displayName Eq " + invalidRole;
        Response notFoundResponse = Response.builder().status(404).headers(new HashMap<>()).build();
        Mockito.when(scimServiceStub.searchGroups(invalidRoleSearchQuery)).thenReturn(notFoundResponse);

        Assert.assertFalse(idpImpl.isValidRole(invalidRole));
    }

    @Test
    public void testGetRoleIdsOfUser() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        SCIMUser user = new SCIMUser();
        String user1Id = "a42b4760-120d-432e-8042-4a7f12e3346c";
        user.setId(user1Id);
        ArrayList<SCIMUser.SCIMUserGroups> userGroups = new ArrayList<>();
        String roleName1 = "engineer";
        String roleName2 = "team_lead";
        String roleName3 = "support_engineer";
        List<String> roleIds = new ArrayList<>();
        roleIds.add("69d87bc2-b694-41b2-a5d3-4ff018e3f7d5");
        roleIds.add("978ab859-2cbc-4f6f-9510-92f44dc34215");
        roleIds.add("b62ae605-fca1-4975-9f2f-2a81ea47d8dc");
        SCIMUser.SCIMUserGroups role1 = new SCIMUser.SCIMUserGroups(roleIds.get(0), roleName1);
        userGroups.add(role1);
        SCIMUser.SCIMUserGroups role2 = new SCIMUser.SCIMUserGroups(roleIds.get(1), roleName2);
        userGroups.add(role2);
        SCIMUser.SCIMUserGroups role3 = new SCIMUser.SCIMUserGroups(roleIds.get(2), roleName3);
        userGroups.add(role3);
        user.setGroups(userGroups);

        Mockito.when(scimServiceStub.getUser(user1Id)).thenReturn(user);

        List<String> roles = idpImpl.getRoleIdsOfUser(user1Id);
        Assert.assertEquals(roleIds.size(), roles.size());
        roles.forEach(roleId -> Assert.assertTrue(roleIds.contains(roleId)));

        String invalidUserId = "invalid-user-id";

        Mockito.when(scimServiceStub.getUser(invalidUserId)).thenReturn(null);

        try {
            idpImpl.getRoleIdsOfUser(invalidUserId);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IdentityProviderException);
            Assert.assertEquals(ex.getMessage(), "User id " + invalidUserId + " does not exist in the system.");
        }
    }

    @Test
    public void testGetRoleId() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        String validRoleName = "engineer";
        final String validRoleSearchQuery = "displayName Eq " + validRoleName;
        final String expectedRoleId = "ac093278-9343-466c-8a71-af47921a575b";

        String invalidRoleName = "invalid_role";
        final String invalidRoleSearchQuery = "displayName Eq " + invalidRoleName;

        //happy path
        String responseBody = "{\"totalResults\":1,\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"Resources\":"
                + "[{\"displayName\":\"PRIMARY/engineer\",\"meta\":{\"created\":\"2017-06-02T10:14:42\","
                + "\"location\":\"https://localhost:9443/wso2/scim/Groups/ac093278-9343-466c-8a71-af47921a575b\","
                + "\"lastModified\":\"2017-06-02T10:14:42\"},\"id\":\"ac093278-9343-466c-8a71-af47921a575b\"}]}";
        Response createdResponse = Response.builder().status(200).headers(new HashMap<>()).body(responseBody.getBytes())
                .build();
        Mockito.when(scimServiceStub.searchGroups(validRoleSearchQuery)).thenReturn(createdResponse);

        try {
            String roleId = idpImpl.getRoleId(validRoleName);
            Assert.assertNotNull(roleId);
            Assert.assertEquals(roleId, expectedRoleId);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error path
        //Assuming the role cannot be found
        Response createdResponseNoSuchRole = Response.builder().status(404).headers(new HashMap<>()).build();
        Mockito.when(scimServiceStub.searchGroups(invalidRoleSearchQuery)).thenReturn(createdResponseNoSuchRole);

        try {
            String roleId = idpImpl.getRoleId(invalidRoleName);
            Assert.assertNull(roleId);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //Assuming there are multiple roles returned
        String responseBodyMultipleResults = "{\"totalResults\":2,\"schemas\":[\"urn:scim:schemas:core:1.0\"],"
                + "\"Resources\":[{\"displayName\":\"PRIMARY/engineer\",\"meta\":{\"created\":\"2017-06-02T10:14:42\","
                + "\"location\":\"https://localhost:9443/wso2/scim/Groups/ac093278-9343-466c-8a71-af47921a575b\","
                + "\"lastModified\":\"2017-06-02T10:14:42\"},\"id\":\"ac093278-9343-466c-8a71-af47921a575b\"},"
                + "{\"displayName\":\"PRIMARY/engineer\",\"meta\":{\"created\":\"2017-06-02T10:14:42\",\"location\":"
                + "\"https://localhost:9443/wso2/scim/Groups/0ca93278-9343-466c-8a71-af47921a575b\",\"lastModified\":"
                + "\"2017-06-02T10:14:42\"},\"id\":\"0ca93278-9343-466c-8a71-af47921a575b\"}]}";
        Response createdResponseMultipleResults = Response.builder().status(200).headers(new HashMap<>())
                .body(responseBodyMultipleResults.getBytes()).build();
        Mockito.when(scimServiceStub.searchGroups(validRoleSearchQuery)).thenReturn(createdResponseMultipleResults);

        try {
            String roleId = idpImpl.getRoleId(validRoleName);
            Assert.assertNull(roleId);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test(expectedExceptions = IdentityProviderException.class)
    public void testGetRoleName() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        String validRoleId = "ac093278-9343-466c-8a71-af47921a575b";
        String expectedRoleName = "engineer";

        String nonExistingRoleId = "ac093278-3493-466c-8a71-af47921a575b";

        //happy path
        SCIMGroup group = new SCIMGroup();
        group.setDisplayName(expectedRoleName);
        group.setId(validRoleId);

        Mockito.when(scimServiceStub.getGroup(validRoleId)).thenReturn(group);

        try {
            String roleName = idpImpl.getRoleName(validRoleId);
            Assert.assertEquals(roleName, expectedRoleName);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error path
        Mockito.when(scimServiceStub.getGroup(nonExistingRoleId)).thenReturn(null);

        String roleName = idpImpl.getRoleName(nonExistingRoleId);
        Assert.assertNull(roleName);
    }

    @Test
    public void testRegisterUser() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        User user = new User();

        //happy path
        Response createdResponse = Response.builder().status(201).headers(new HashMap<>()).build();
        Mockito.when(scimServiceStub.addUser(any(SCIMUser.class))).thenReturn(createdResponse);

        try {
            idpImpl.registerUser(user);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error path
        final int errorSc = 409;
        final String errorMsg = "{\"Errors\":[{\"code\":\"409\",\"description\":\"Error in adding the user: test to " +
                "the user store.\"}]}";
        Response errorResponse = Response.builder().status(errorSc).headers(new HashMap<>())
                .body(errorMsg.getBytes()).build();
        Mockito.when(scimServiceStub.addUser(any(SCIMUser.class))).thenReturn(errorResponse);

        try {
            idpImpl.registerUser(user);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IdentityProviderException);
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while creating user."));
        }
    }

}
