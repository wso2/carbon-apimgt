/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.auth.dto;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/*
   Test cases for SCIMUserTest class
 */
public class SCIMUserTestCase {
    SCIMUser scimUser = new SCIMUser();

    @Test(description = "Test for the setter and getter method of schemas")
    public void schemsSetterAndGetterTest() {
        List<String> schemas = new ArrayList<>();
        schemas.add("SampleSchema1");
        schemas.add("SampleSchema2");
        schemas.add("SampleSchema3");
        scimUser.setSchemas(schemas);
        Assert.assertEquals(scimUser.getSchemas(), schemas);
    }

    @Test(description = "Test for the setter and getter method of emails")
    public void emailsSetterAndGetterTest() {
        final List<SCIMUser.SCIMUserEmails> scimUserEmails = new ArrayList<>();
        scimUserEmails.add(new SCIMUser.SCIMUserEmails("email1", "home", true));
        scimUserEmails.add(new SCIMUser.SCIMUserEmails("email2", "office", true));
        scimUser.setEmails(scimUserEmails);
        Assert.assertEquals(scimUser.getEmails(), scimUserEmails);
    }

    @Test(description = "Test the setter and getter method of user group")
    public void userGoupsSetterAndGetterTest() {
        final List<SCIMUser.SCIMUserGroups> scimGroups = new ArrayList<>();
        scimGroups.add(new SCIMUser.SCIMUserGroups("group_001", "Group1"));
        scimGroups.add(new SCIMUser.SCIMUserGroups("group_002", "Group2"));
        scimUser.setGroups(scimGroups);
        Assert.assertEquals(scimUser.getGroups(), scimGroups);
    }

    @Test(description = "Test for the getters and setters in SCIMUserEmails inner class")
    public void scimUserEmailsTest() {
        SCIMUser.SCIMUserEmails scimUserEmails = new SCIMUser.SCIMUserEmails("email4", "home", true);
        scimUserEmails.setValue("email5");
        Assert.assertEquals(scimUserEmails.getValue(), "email5");
        scimUserEmails.setType("office");
        Assert.assertEquals(scimUserEmails.getType(), "office");
        scimUserEmails.setPrimary(false);
        Assert.assertEquals(scimUserEmails.isPrimary(), false);
    }

    @Test(description = "Test for the getters and setters in SCIMName inner class")
    public void scimNameTest() {
        final String testUserFamilyName = "test-family";
        SCIMUser.SCIMName scimName = new SCIMUser.SCIMName();
        Assert.assertEquals(scimName.getFamilyName(), "");
        Assert.assertEquals(scimName.getGivenName(), "");

        final String testUserName = "TestUser0001";
        scimName = new SCIMUser.SCIMName(testUserName, testUserFamilyName);
        Assert.assertEquals(scimName.getGivenName(), testUserName);
        Assert.assertEquals(scimName.getFamilyName(), testUserFamilyName);
    }

    @Test(description = "Test for the getters and setters in SCIMUserGroups inner class")
    public void scimUserGroupsTest() {
        SCIMUser.SCIMUserGroups scimUserGroups = new SCIMUser.SCIMUserGroups("group_004", "GROUP4");
        Assert.assertEquals(scimUserGroups.getValue(), "group_004");
        Assert.assertEquals(scimUserGroups.getDisplay(), "GROUP4");

        scimUserGroups.setValue("group_005");
        Assert.assertEquals(scimUserGroups.getValue(), "group_005");

        scimUserGroups.setDisplay("GROUP5");
        Assert.assertEquals(scimUserGroups.getDisplay(), "GROUP5");
    }
}

