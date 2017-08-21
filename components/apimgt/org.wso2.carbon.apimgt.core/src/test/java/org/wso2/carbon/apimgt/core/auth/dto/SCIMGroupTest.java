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
   Test cases for SCIMGroup class
 */
public class SCIMGroupTest {
    SCIMGroup scimGroup = new SCIMGroup();

    @Test
    public void scimGroupMembersTest() throws Exception {
        List<SCIMGroup.SCIMGroupMembers> scimGroupMembers = new ArrayList<>();

        SCIMGroup.SCIMGroupMembers scimGroupMember1 = new SCIMGroup.SCIMGroupMembers();
        scimGroupMember1.setValue("member_0001");
        Assert.assertEquals(scimGroupMember1.getValue(), "member_0001");

        scimGroupMember1.setDisplay("MEMBER1");
        Assert.assertEquals(scimGroupMember1.getDisplay(), "MEMBER1");

        SCIMGroup.SCIMGroupMembers scimGroupMember2 = new SCIMGroup.SCIMGroupMembers();
        scimGroupMember2.setValue("member_0002");
        scimGroupMember2.setDisplay("MEMBER2");

        scimGroupMembers.add(scimGroupMember1);
        scimGroupMembers.add(scimGroupMember2);

        scimGroup.setMembers(scimGroupMembers);
        Assert.assertEquals(scimGroup.getMembers(), scimGroupMembers);
    }

    @Test
    public void schemasGetterAndSetterTest() throws Exception {
        List<String> schemas = new ArrayList<>();
        schemas.add("schema1");
        schemas.add("schema2");
        scimGroup.setSchemas(schemas);
        Assert.assertEquals(scimGroup.getSchemas(), schemas);
    }
}
