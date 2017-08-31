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
package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.UserDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class SelfSignupApiServiceImplTestCase {

    private static final String USER = "admin";
    private static final String contentType = "application/json";

    @Test
    public void testSelfSignupPost() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();

        SelfSignupApiServiceImpl selfSignupApiService = new SelfSignupApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer()).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        User user = new User();
        user.setEmail("john@john.doe");
        user.setPassword(UUID.randomUUID().toString().toCharArray());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("johnd");

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("john@john.doe");
        userDTO.setPassword(UUID.randomUUID().toString());
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setUsername("johnd");

        Mockito.doNothing().when(apiStore).selfSignUp(user);

        Response response = selfSignupApiService.selfSignupPost(userDTO, request);

        Assert.assertEquals(200, response.getStatus());
    }
}
