/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.rest.api.core.dto.PolicyListDTO;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerFactory.class)
public class PoliciesApiServiceImplTestCase {

    @Test
    public void policiesGetTest() throws Exception {
        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();

        PolicyValidationData policyValidationDataOne = new PolicyValidationData(UUID.randomUUID().toString(),
                "APPLICATION_POLICY", true);
        PolicyValidationData policyValidationDataTwo = new PolicyValidationData(UUID.randomUUID().toString(),
                "APPLICATION_POLICY", true);
        PolicyValidationData policyValidationDataThree = new PolicyValidationData(UUID.randomUUID().toString(),
                "APPLICATION_POLICY", true);

        Set<PolicyValidationData> policyValidationDataHashSet = new LinkedHashSet<>();
        policyValidationDataHashSet.add(policyValidationDataOne);
        policyValidationDataHashSet.add(policyValidationDataTwo);
        policyValidationDataHashSet.add(policyValidationDataThree);

        Mockito.when(apiMgtAdminService.getAllPolicies()).thenReturn(policyValidationDataHashSet);

        Response response = policiesApiService.policiesGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(((PolicyListDTO) response.getEntity()).getList().size(), 3);

    }

    @Test
    public void policiesGetExceptionTest() throws Exception {
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        Response response = policiesApiService.policiesGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }
}
