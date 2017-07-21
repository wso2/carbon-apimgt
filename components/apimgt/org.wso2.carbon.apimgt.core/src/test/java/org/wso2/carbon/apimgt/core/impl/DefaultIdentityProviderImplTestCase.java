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
import org.wso2.carbon.apimgt.core.auth.dto.SCIMUser;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.any;

public class DefaultIdentityProviderImplTestCase {

    @Test
    public void testGetIdOfUser() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        String validUserName = "John";
        final String validUserSearchQuery = "userName Eq " + validUserName;
        final String expectedUserId = "cfbde56e-8422-498e-b6dc-85a6f1f8b058";

        String invalidUserName = "invalid_user";
        final String invalidUserSearchQuery = "userName Eq " + invalidUserName;

        String userReturningNullResponse = "invalid_user_giving_null_response";
        final String userReturningNullResponseSearchQuery = "userName Eq " + userReturningNullResponse;

        //happy path
        String responseBody = "{\"totalResults\":1,\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"Resources\":"
                + "[{\"meta\":{\"created\":\"2017-06-02T10:12:26\",\"location\":"
                + "\"https://localhost:9443/wso2/scim/Users/cfbde56e-8422-498e-b6dc-85a6f1f8b058\",\"lastModified\":"
                + "\"2017-06-02T10:12:26\"},\"id\":\"cfbde56e-8422-498e-b6dc-85a6f1f8b058\",\"userName\":\"John\"}]}";
        Response createdResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body(responseBody.getBytes()).build();
        Mockito.when(scimServiceStub.searchUsers(validUserSearchQuery)).thenReturn(createdResponse);

        try {
            String userId = idpImpl.getIdOfUser(validUserName);
            Assert.assertEquals(userId, expectedUserId);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error path
        //Assuming the user cannot be found - When the request did not return a 200 OK response
        String errorResponse = "{\"Errors\":[{\"code\":\"404\",\"description\":\"User not found in the user "
                + "store.\"}]}";
        Response createdResponseNoSuchUser = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_404_NOT_FOUND)
                .headers(new HashMap<>()).body(errorResponse.getBytes()).build();
        Mockito.when(scimServiceStub.searchUsers(invalidUserSearchQuery)).thenReturn(createdResponseNoSuchUser);

        try {
            idpImpl.getIdOfUser(invalidUserName);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IdentityProviderException);
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving Id of user " +
                    invalidUserName + ". Error : User not found in the user store.");
        }

        //error path
        //Assuming the response is null
        Mockito.when(scimServiceStub.searchUsers(userReturningNullResponseSearchQuery)).thenReturn(null);

        try {
            idpImpl.getIdOfUser(userReturningNullResponse);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IdentityProviderException);
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving Id of user " +
                    userReturningNullResponse + ". Error : Response is null.");
        }
    }

    @Test
    public void testGetRoleNamesOfUser() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        String validUserId = "a42b4760-120d-432e-8042-4a7f12e3346c";
        String roleName1 = "subscriber";
        String roleId1 = "fb5aaf9c-1fdf-4b2d-86bc-6e3203b99618";
        String roleName2 = "manager";
        String roleId2 = "097435bc-c460-402b-9137-8ab65fd28c3e";
        String roleName3 = "engineer";
        String roleId3 = "ac093278-9343-466c-8a71-af47921a575b";

        List<String> roleNames = new ArrayList<>();
        roleNames.add(roleName1);
        roleNames.add(roleName2);
        roleNames.add(roleName3);

        String successResponseBody = "{\"emails\":[{\"type\":\"home\",\"value\":\"john_home.com\"},{\"type\":\"work\""
                + ",\"value\":\"john_work.com\"}],\"meta\":{\"created\":\"2017-06-02T10:12:26\",\"location\":"
                + "\"https://localhost:9443/wso2/scim/Users/" + validUserId + "\",\"lastModified\":"
                + "\"2017-06-02T10:12:26\"},\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"name\":{\"familyName\":"
                + "\"Smith\",\"givenName\":\"John\"},\"groups\":[{\"display\":\"" + roleName1 + "\",\"value\":\""
                + roleId1 + "\"},{\"display\":\"" + roleName2 + "\",\"value\":\"" + roleId2 + "\"},{\"display\":\""
                + roleName3 + "\",\"value\":\"" + roleId3 + "\"}],\"id\":\"" + validUserId + "\",\"userName\":"
                + "\"John\"}";
        Response successfulResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body(successResponseBody.getBytes()).build();

        Mockito.when(scimServiceStub.getUser(validUserId)).thenReturn(successfulResponse);

        List<String> roles = idpImpl.getRoleNamesOfUser(validUserId);
        Assert.assertEquals(roleNames.size(), roles.size());
        roles.forEach(roleName -> Assert.assertTrue(roleNames.contains(roleName)));

        //Error case - When response is null
        String invalidUserIdResponseNull = "invalidUserId_Response_Null";

        Mockito.when(scimServiceStub.getUser(invalidUserIdResponseNull)).thenReturn(null);

        try {
            idpImpl.getRoleNamesOfUser(invalidUserIdResponseNull);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving user with Id " +
                    invalidUserIdResponseNull + ". Error : Response is null.");
        }

        //Error case - When the request did not return a 200 OK response
        String invalidUserIdNot200OK = "invalidUserId_Not_200_OK";

        String errorResponseBody = "{\"Errors\":[{\"code\":\"404\",\"description\":\"User not found in the user "
                + "store.\"}]}";
        Response errorResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_404_NOT_FOUND)
                .headers(new HashMap<>()).body(errorResponseBody.getBytes()).build();
        Mockito.when(scimServiceStub.getUser(invalidUserIdNot200OK)).thenReturn(errorResponse);

        try {
            idpImpl.getRoleNamesOfUser(invalidUserIdNot200OK);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(),  "Error occurred while retrieving role names of user with Id "
                    + invalidUserIdNot200OK + ". Error : User not found in the user store.");
        }

        //Error case - When response body is empty
        String invalidUserIdResponseEmpty = "invalidUserId_Response_Empty";
        Response emptyResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body("".getBytes()).build();
        Mockito.when(scimServiceStub.getUser(invalidUserIdResponseEmpty)).thenReturn(emptyResponse);

        try {
            idpImpl.getRoleNamesOfUser(invalidUserIdResponseEmpty);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving user with user Id " +
                    invalidUserIdResponseEmpty + " from SCIM endpoint. Response body is null or empty.");
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
        Response okResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).build();
        Mockito.when(scimServiceStub.searchGroups(validRoleSearchQuery)).thenReturn(okResponse);

        Assert.assertTrue(idpImpl.isValidRole(validRole));

        final String invalidRole = "invalid-role";
        final String invalidRoleSearchQuery = "displayName Eq " + invalidRole;
        Response notFoundResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_404_NOT_FOUND)
                .headers(new HashMap<>()).build();
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

        String validUserId = "a42b4760-120d-432e-8042-4a7f12e3346c";
        String roleName1 = "subscriber";
        String roleId1 = "fb5aaf9c-1fdf-4b2d-86bc-6e3203b99618";
        String roleName2 = "manager";
        String roleId2 = "097435bc-c460-402b-9137-8ab65fd28c3e";
        String roleName3 = "engineer";
        String roleId3 = "ac093278-9343-466c-8a71-af47921a575b";

        List<String> roleIds = new ArrayList<>();
        roleIds.add(roleId1);
        roleIds.add(roleId2);
        roleIds.add(roleId3);

        String successResponseBody = "{\"emails\":[{\"type\":\"home\",\"value\":\"john_home.com\"},{\"type\":\"work\""
                + ",\"value\":\"john_work.com\"}],\"meta\":{\"created\":\"2017-06-02T10:12:26\",\"location\":"
                + "\"https://localhost:9443/wso2/scim/Users/" + validUserId + "\",\"lastModified\":"
                + "\"2017-06-02T10:12:26\"},\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"name\":{\"familyName\":"
                + "\"Smith\",\"givenName\":\"John\"},\"groups\":[{\"display\":\"" + roleName1 + "\",\"value\":\""
                + roleId1 + "\"},{\"display\":\"" + roleName2 + "\",\"value\":\"" + roleId2 + "\"},{\"display\":\""
                + roleName3 + "\",\"value\":\"" + roleId3 + "\"}],\"id\":\"" + validUserId + "\",\"userName\":"
                + "\"John\"}";
        Response successfulResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body(successResponseBody.getBytes()).build();

        Mockito.when(scimServiceStub.getUser(validUserId)).thenReturn(successfulResponse);

        List<String> roles = idpImpl.getRoleIdsOfUser(validUserId);
        Assert.assertEquals(roleIds.size(), roles.size());
        roles.forEach(roleId -> Assert.assertTrue(roleIds.contains(roleId)));

        //Error case - When response is null
        String invalidUserIdResponseNull = "invalidUserId_Response_Null";

        Mockito.when(scimServiceStub.getUser(invalidUserIdResponseNull)).thenReturn(null);

        try {
            idpImpl.getRoleIdsOfUser(invalidUserIdResponseNull);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving user with Id " +
                    invalidUserIdResponseNull + ". Error : Response is null.");
        }

        //Error case - When the request did not return a 200 OK response
        String invalidUserIdNot200OK = "invalidUserId_Not_200_OK";

        String errorResponseBody = "{\"Errors\":[{\"code\":\"404\",\"description\":\"User not found in the user "
                + "store.\"}]}";
        Response errorResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_404_NOT_FOUND)
                .headers(new HashMap<>()).body(errorResponseBody.getBytes()).build();
        Mockito.when(scimServiceStub.getUser(invalidUserIdNot200OK)).thenReturn(errorResponse);

        try {
            idpImpl.getRoleIdsOfUser(invalidUserIdNot200OK);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(),  "Error occurred while retrieving role Ids of user with Id "
                    + invalidUserIdNot200OK + ". Error : User not found in the user store.");
        }

        //Error case - When response body is empty
        String invalidUserIdResponseEmpty = "invalidUserId_Response_Empty";
        Response emptyResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body("".getBytes()).build();
        Mockito.when(scimServiceStub.getUser(invalidUserIdResponseEmpty)).thenReturn(emptyResponse);

        try {
            idpImpl.getRoleIdsOfUser(invalidUserIdResponseEmpty);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving user with user Id " +
                    invalidUserIdResponseEmpty + " from SCIM endpoint. Response body is null or empty.");
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

        String roleReturningNullResponse = "invalid_user_giving_null_response";
        final String roleReturningNullResponseSearchQuery = "displayName Eq " + roleReturningNullResponse;

        //happy path
        String responseBody = "{\"totalResults\":1,\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"Resources\":"
                + "[{\"displayName\":\"PRIMARY/engineer\",\"meta\":{\"created\":\"2017-06-02T10:14:42\","
                + "\"location\":\"https://localhost:9443/wso2/scim/Groups/ac093278-9343-466c-8a71-af47921a575b\","
                + "\"lastModified\":\"2017-06-02T10:14:42\"},\"id\":\"ac093278-9343-466c-8a71-af47921a575b\"}]}";
        Response createdResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body(responseBody.getBytes()).build();
        Mockito.when(scimServiceStub.searchGroups(validRoleSearchQuery)).thenReturn(createdResponse);

        try {
            String roleId = idpImpl.getRoleId(validRoleName);
            Assert.assertEquals(roleId, expectedRoleId);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error path
        //Assuming the role cannot be found - when returning a not 200 response
        String errorResponseBody =
                "{\"Errors\":[{\"code\":\"404\",\"description\":\"Group not found in the user store.\"}]}";
        Response createdResponseNoSuchRole = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_404_NOT_FOUND)
                .headers(new HashMap<>()).body(errorResponseBody.getBytes()).build();
        Mockito.when(scimServiceStub.searchGroups(invalidRoleSearchQuery)).thenReturn(createdResponseNoSuchRole);

        try {
            idpImpl.getRoleId(invalidRoleName);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IdentityProviderException);
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving Id of role " +
                    invalidRoleName + ". Error : Group not found in the user store.");
        }

        //error path
        //Assuming the response is null
        Mockito.when(scimServiceStub.searchGroups(roleReturningNullResponseSearchQuery)).thenReturn(null);

        try {
            idpImpl.getRoleId(roleReturningNullResponse);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IdentityProviderException);
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving Id of role " +
                    roleReturningNullResponse + ". Error : Response is null.");
        }
    }

    @Test
    public void testGetRoleName() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        String validRoleId = "ac093278-9343-466c-8a71-af47921a575b";
        String expectedRoleName = "engineer";

        //happy path
        String successfulResponseBody = "{\"displayName\":\"" + expectedRoleName + "\",\"meta\":{\"created\":"
                + "\"2017-06-26T16:30:42\",\"location\":\"https://localhost:9443/wso2/scim/Groups/" + validRoleId + "\""
                + ",\"lastModified\":\"2017-06-26T16:30:42\"},\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\""
                + validRoleId + "\"}";
        Response successfulResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body(successfulResponseBody.getBytes()).build();
        Mockito.when(scimServiceStub.getGroup(validRoleId)).thenReturn(successfulResponse);

        try {
            String roleName = idpImpl.getRoleName(validRoleId);
            Assert.assertEquals(roleName, expectedRoleName);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error path
        //When response is null
        String invalidRoleIdResponseNull = "invalidRoleId_Response_Null";
        Mockito.when(scimServiceStub.getGroup(invalidRoleIdResponseNull)).thenReturn(null);

        try {
            idpImpl.getRoleName(invalidRoleIdResponseNull);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving name of role with Id " +
                    invalidRoleIdResponseNull + ". Error : Response is null.");
        }

        //error path
        //When the request did not return a 200 OK response
        String invalidRoleIdNot200OK = "invalidRoleId_Not_200_OK";

        String errorResponseBody = "{\"Errors\":[{\"code\":\"404\",\"description\":\"Group not found in the user "
                + "store.\"}]}";
        Response errorResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_404_NOT_FOUND)
                .headers(new HashMap<>()).body(errorResponseBody.getBytes()).build();
        Mockito.when(scimServiceStub.getGroup(invalidRoleIdNot200OK)).thenReturn(errorResponse);

        try {
            idpImpl.getRoleName(invalidRoleIdNot200OK);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving name of role with Id " +
                    invalidRoleIdNot200OK + ". Error : Group not found in the user store.");
        }

        //Error case - When response body is empty
        String invalidRoleIdResponseEmpty = "invalidRoleId_Response_Empty";
        Response emptyResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_200_OK)
                .headers(new HashMap<>()).body("".getBytes()).build();
        Mockito.when(scimServiceStub.getGroup(invalidRoleIdResponseEmpty)).thenReturn(emptyResponse);

        try {
            idpImpl.getRoleName(invalidRoleIdResponseEmpty);
        } catch (IdentityProviderException ex) {
            Assert.assertEquals(ex.getMessage(), "Error occurred while retrieving role name with role Id " +
                    invalidRoleIdResponseEmpty + " from SCIM endpoint. "
                    + "Response body is null or empty.");
        }
    }

    @Test
    public void testRegisterUser() throws Exception {
        SCIMServiceStub scimServiceStub = Mockito.mock(SCIMServiceStub.class);
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        DefaultIdentityProviderImpl idpImpl = new DefaultIdentityProviderImpl(scimServiceStub, dcrmServiceStub,
                oAuth2ServiceStub);

        //happy path
        User user = new User();
        user.setFirstName("john");
        user.setLastName("doe");
        user.setUsername("johnd");
        user.setEmail("john@wso2.com");
        user.setPassword(new char[] {'p', 'a', 's', 's'});

        SCIMUser scimUser = new SCIMUser();
        SCIMUser.SCIMName scimName = new SCIMUser.SCIMName();
        scimName.setGivenName(user.getFirstName());
        scimName.setFamilyName(user.getLastName());
        scimUser.setName(scimName);
        SCIMUser.SCIMUserEmails scimUserEmails = new SCIMUser.SCIMUserEmails(user.getEmail(), "home", true);
        List<SCIMUser.SCIMUserEmails> scimUserEmailList = new ArrayList<>();
        scimUserEmailList.add(scimUserEmails);
        scimUser.setEmails(scimUserEmailList);
        scimUser.setUsername(user.getUsername());
        scimUser.setPassword(String.valueOf(user.getPassword()));

        Response createdResponse = Response.builder().status(APIMgtConstants.HTTPStatusCodes.SC_201_CREATED)
                .headers(new HashMap<>()).build();
        Mockito.when(scimServiceStub.addUser(scimUser)).thenReturn(createdResponse);

        try {
            idpImpl.registerUser(user);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error path
        final int errorSc = APIMgtConstants.HTTPStatusCodes.SC_409_CONFLICT;
        final String errorMsg = "{\"Errors\":[{\"code\":\"409\",\"description\":\"Error in adding the user: test to " +
                "the user store.\"}]}";
        Response errorResponse = Response.builder().status(errorSc).headers(new HashMap<>())
                .body(errorMsg.getBytes()).build();
        Mockito.when(scimServiceStub.addUser(any(SCIMUser.class))).thenReturn(errorResponse);

        try {
            idpImpl.registerUser(user);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (IdentityProviderException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while creating user."));
        }
    }

}
