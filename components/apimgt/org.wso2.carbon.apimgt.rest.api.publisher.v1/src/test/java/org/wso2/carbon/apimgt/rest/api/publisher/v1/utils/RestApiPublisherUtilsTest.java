/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

// Enclosed class is used to feed multiple tests with different Parameterized test data
@RunWith(Enclosed.class)
public class RestApiPublisherUtilsTest {

    @RunWith(PowerMockRunner.class)
    @PowerMockRunnerDelegate(Parameterized.class)
    @PrepareForTest({APIDTO.class, RestApiUtil.class, APIUtil.class})
    public static class RestApiPublisherUtilsValidateUserRolesTest {

        private static String[] validUserRoles;
        private static String[] validTenantRoles;
        private boolean hasPermission;
        private boolean compareRoleListWithUserRoleList;
        private boolean compareRoleListWithTenantRoleList;
        private String expectedMessage;
        private String[] roles;
        private String[] tenantRoles;
        private List<String> inputRoles;


        public RestApiPublisherUtilsValidateUserRolesTest(List<String> inputRoles, String[] roles, String[] tenantRoles,
                                                          boolean hasPermission,
                                                          boolean compareRoleListWithUserRoleList,
                                                          boolean compareRoleListWithTenantRoleList,
                                                          String expectedMessage) {
            this.inputRoles = inputRoles;
            this.roles = roles;
            this.tenantRoles = tenantRoles;
            this.hasPermission = hasPermission;
            this.compareRoleListWithUserRoleList = compareRoleListWithUserRoleList;
            this.compareRoleListWithTenantRoleList = compareRoleListWithTenantRoleList;
            this.expectedMessage = expectedMessage;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            validUserRoles = new String[] {APIConstants.Permissions.APIM_ADMIN, APIConstants.Permissions.API_PUBLISH};
            validTenantRoles = new String[] {APIConstants.Permissions.APIM_ADMIN, APIConstants.Permissions.API_CREATE};
            List<String> validInputRoles = new ArrayList<String>();
            validInputRoles.add(APIConstants.Permissions.APIM_ADMIN);
            validInputRoles.add(APIConstants.Permissions.API_PUBLISH);
            List<String> emptyInputRoles = new ArrayList<String>();
            String[] emptyUserRoles = new String[] {};

            return Arrays.asList(new Object[][] {
                // inputRoles, userRoleList , tenantRoles, hasPermission, compareRoleListWithUserRoleList,
                // compareRoleListWithTenantRoleList, expectedMessage
                {validInputRoles, null, validTenantRoles, true, true, true, ""},
                {validInputRoles, validUserRoles, validTenantRoles, true, true, true, ""},
                {validInputRoles, validUserRoles, validTenantRoles, true, false, false, "Invalid user roles found in " +
                                                                                        "accessControlRole list"},
                {validInputRoles, null, null, true, true, true, "Invalid user roles found"},
                {validInputRoles, validUserRoles, null, true, true, true, "Invalid user roles found"},
                {null, validUserRoles, null, true, true, true, ""},
                {emptyInputRoles, validUserRoles, null, true, true, true, ""},
                {validInputRoles, validUserRoles, validTenantRoles, false, true, true, ""},
                {validInputRoles, validUserRoles, validTenantRoles, false, false, false, "Invalid user roles found in" +
                                                                                         " accessControlRole list"},
                {validInputRoles, validUserRoles, validTenantRoles, false, false, true, "This user does not have at " +
                                                                                        "least one role specified in " +
                                                                                        "API access control."},
                {validInputRoles, emptyUserRoles, validTenantRoles, false, true, true, "This user does not have at " +
                                                                                       "least one role specified in " +
                                                                                       "API access control."},
                {validInputRoles, validUserRoles, emptyUserRoles, false, true, true, "Invalid user roles found in " +
                                                                                     "accessControlRole list"},
                {validInputRoles, validUserRoles, validTenantRoles, false, true, false, "Invalid user roles found in " +
                                                                                        "accessControlRole list"}
            });
        }

        @Test
        public void testValidateUserRoles() throws APIManagementException {

            String userName = "Micheal";
            mockStatic(RestApiUtil.class);
            mockStatic(APIUtil.class);

            when(RestApiUtil.getLoggedInUsername()).thenReturn(userName);
            when(APIUtil.getRoleNames(userName)).thenReturn(tenantRoles);
            when(APIUtil.getListOfRoles(userName)).thenReturn(roles);

            when(APIUtil.hasPermission(eq(userName), Mockito.any())).thenReturn(hasPermission);
            when(APIUtil.compareRoleList(eq(validUserRoles), Mockito.any()))
                .thenReturn(compareRoleListWithUserRoleList);
            when(APIUtil.compareRoleList(eq(validTenantRoles), Mockito.any()))
                .thenReturn(compareRoleListWithTenantRoleList);

            Assert.assertEquals(expectedMessage, RestApiPublisherUtils.validateUserRoles(inputRoles));
        }
    }

    @RunWith(Parameterized.class)
    public static class RestApiPublisherUtilsValidateAdditionalPropertiesTest {

        private static String attributeKey;
        private static String attributeValue;
        private static String expectedMessage;

        public RestApiPublisherUtilsValidateAdditionalPropertiesTest(String attributeKey, String attributeValue,
                                                                     String expectedMessage) {
            this.attributeKey = attributeKey;
            this.attributeValue = attributeValue;
            this.expectedMessage = expectedMessage;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            String longKey = "Invalid_Property_KEY_Something_Sample_Property_KEY_Something_Sample_Property_KEY_";
            String longValue = "This is sample Attribute Value @!@#$%^&*+_()<>?,./.   This is sample Attribute Value " +
                               "@!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is " +
                               "sample Attribute Value @!@#$%^&*+_()<>?,./. This is sample Attribute Value " +
                               "@!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is " +
                               "sample Attribute Value @!@#$%^&*+_()<>?,./.   ______This is sample Attribute Value " +
                               "@!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is " +
                               "sample Attribute Value @!@#$%^&*+_()<>?,./. @!@#$%^&*+_()<>?,./. This is sample " +
                               "Attribute Value @!@#$%^&*+_()<>?,d./. This is sample Attribute Value @!@#$%^&*+_()" +
                               "<>?,./.   ______This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is sample " +
                               "Attribute Value @!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_()<>?," +
                               "./. This is sample Attribute ValueThis is sample Attribute Value Value @!@#$%^&*+_().";

            return Arrays.asList(new Object[][] {
                {"validKey", "validValue", ""},
                {"valid_Key", "valid_Value", ""},
                {"valid@Key", "valid Some value", ""},
                {"Sample_Property_KEY_Something_Sample_Property_KEY_SomethingSample_Property_KEY_!", "valid Some " +
                                                                                                     "value", ""},
                {"valid_Key", "This is sample Attribute Value @!@#$%^&*+_()<>?,./.   This is sample Attribute Value " +
                              "@!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is " +
                              "sample Attribute Value @!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_" +
                              "()<>?,./. This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is sample Attribute" +
                              " Value @!@#$%^&*+_()<>?,./.   ______This is sample Attribute Value @!@#$%^&*+_()<>?,./" +
                              ". This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is sample Attribute Value " +
                              "@!@#$%^&*+_()<>?,./. @!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_()" +
                              "<>?,d./. This is sample Attribute Value @!@#$%^&*+_()<>?,./.   ______This is sample " +
                              "Attribute Value @!@#$%^&*+_()<>?,./. This is sample Attribute Value @!@#$%^&*+_()<>?," +
                              "./. This is sample Attribute Value @!@#$%^&*+_()<>?,./. This is sample Attribute " +
                              "ValueThis is sample Attribute Value Value @!@#$%^&*+_()", ""},
                {"Invalid Key", "validValue", "Property names should not contain space character. Property 'Invalid " +
                                              "Key' contains space in it."},
                {APIConstants.API_DESCRIPTION, "validValue", "Property '" + APIConstants.API_DESCRIPTION + "' " +
                                                             "conflicts with the " + "reserved keywords. Reserved " +
                                                             "keywords are [" + Arrays
                                                                 .toString(APIConstants.API_SEARCH_PREFIXES) + "]"},
                {longKey, "valid Some value",
                 "Property name can have maximum of 80 characters. Property '" + longKey + "' + contains " + longKey
                     .length() + "characters"},
                {"validKey", longValue,
                 "Property value can have maximum of 900 characters. Property '" + "validKey" + "' + "
                 + "contains a value with " + longValue.length() + "characters"}
            });
        }

        @Test
        public void testValidateAdditionalProperties() {

            Map<String, String> inputAdditionalProperties = new HashMap<>();

            inputAdditionalProperties.put("environment", "preprod");
            inputAdditionalProperties.put("secured", "true");
            inputAdditionalProperties.put(attributeKey, attributeValue);

            String actualMessage = RestApiPublisherUtils.validateAdditionalProperties(inputAdditionalProperties);

            Assert.assertEquals(expectedMessage, actualMessage);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PrepareForTest({APIDTO.class, RestApiUtil.class, APIUtil.class})
    public static class RestApiPublisherUtilsTestOthers {

        private org.json.simple.JSONObject endpoint;

        @Test //TODO
        public void testIsValidWSAPI() throws ParseException {

            String endPointString = "{\n" +
                                    "  \"production_endpoints\": {\n" +
                                    "    \"template_not_supported\": false,\n" +
                                    "    \"config\": null,\n" +
                                    "    \"url\": \"" + "endpointUrlprod" + "\"\n" +
                                    "  },\n" +
                                    "  \"sandbox_endpoints\": {\n" +
                                    "    \"url\": \"" + "endpointUrl sand" + "\",\n" +
                                    "    \"config\": null,\n" +
                                    "    \"template_not_supported\": false\n" +
                                    "  },\n" +
                                    "  \"endpoint_type\": \"http\"\n" +
                                    "}";

            JSONParser parser = new JSONParser();
            this.endpoint = (org.json.simple.JSONObject) parser.parse(endPointString);

            APIDTO api = Mockito.mock(APIDTO.class);
            PowerMockito.mockStatic(APIDTO.class);
            PowerMockito.when(api.getEndpointConfig()).thenReturn(endpoint);

//        Assert.assertTrue(RestApiPublisherUtils.isValidWSAPI(api));
        }

        @Test
        public void testValidateRoles() throws APIManagementException {

            String userName = "Daniel";
            List<String> inputRoles = new ArrayList<String>();
            inputRoles.add(APIConstants.Permissions.APIM_ADMIN);
            inputRoles.add(APIConstants.Permissions.API_CREATE);
            String roleString = APIConstants.Permissions.APIM_ADMIN + "," + APIConstants.Permissions.API_CREATE;

            PowerMockito.mockStatic(RestApiUtil.class);
            when(RestApiUtil.getLoggedInUsername()).thenReturn(userName);
            PowerMockito.mockStatic(APIUtil.class);
            when(APIUtil.isRoleNameExist(userName, roleString)).thenReturn(true);

            String actualMessage = RestApiPublisherUtils.validateRoles(inputRoles);

            Assert.assertEquals("", actualMessage);
        }

        @Test
        public void testValidateRolesWithEmptyRoles() throws APIManagementException {

            String userName = "Daniel";
            List<String> inputRoles = new ArrayList<String>();

            PowerMockito.mockStatic(RestApiUtil.class);
            when(RestApiUtil.getLoggedInUsername()).thenReturn(userName);
            PowerMockito.mockStatic(APIUtil.class);
            when(APIUtil.isRoleNameExist(Mockito.any(), Mockito.anyString())).thenReturn(true);

            String actualMessage = RestApiPublisherUtils.validateRoles(inputRoles);

            Assert.assertEquals("", actualMessage);
        }

        @Test
        public void testValidateRolesRoleNameDoesntExist() throws APIManagementException {

            String userName = "Daniel";
            List<String> inputRoles = new ArrayList<String>();
            inputRoles.add(APIConstants.Permissions.APIM_ADMIN);
            inputRoles.add(APIConstants.Permissions.API_CREATE);

            PowerMockito.mockStatic(RestApiUtil.class);
            when(RestApiUtil.getLoggedInUsername()).thenReturn(userName);
            PowerMockito.mockStatic(APIUtil.class);
            when(APIUtil.isRoleNameExist(Mockito.any(), Mockito.anyString())).thenReturn(false);

            String actualMessage = RestApiPublisherUtils.validateRoles(inputRoles);

            Assert.assertEquals("Invalid user roles found in visibleRoles list", actualMessage);
        }
    }
}