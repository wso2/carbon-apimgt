/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.UserNameMapper;
import org.wso2.carbon.apimgt.core.dao.UserMappingDAO;

public class UserDataMappingImplTestCase {

    @Test
    public void testGetIdOfUser() throws Exception {
        UserMappingDAO userMappingDAO = Mockito.mock(UserMappingDAO.class);
        Mockito.when(userMappingDAO.getPseudoNameByUserID("test_user")).thenReturn("XXXXX");
        UserNameMapper userNameMapper = new UserNameMapperImpl(userMappingDAO);
        String pseudoName = userNameMapper.getLoggedInPseudoNameFromUserID("test_user");
        Assert.assertNotNull(pseudoName);
        Assert.assertEquals("XXXXX", pseudoName);
        userNameMapper.getLoggedInUserIDFromPseudoName("XXXXX");
    }
}
