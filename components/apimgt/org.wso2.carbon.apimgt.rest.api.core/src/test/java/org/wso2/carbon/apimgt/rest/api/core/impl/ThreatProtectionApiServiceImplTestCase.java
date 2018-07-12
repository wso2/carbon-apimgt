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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class ThreatProtectionApiServiceImplTestCase {
    @Test
    public void threatProtectionPoliciesGetTestCase() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        List<ThreatProtectionPolicy> list = new ArrayList<>();
        list.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());
        list.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());
        list.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());

        Mockito.when(apiMgtAdminService.getThreatProtectionPolicyList()).thenReturn(list);

        ThreatProtectionPoliciesApiServiceImpl threatProtectionApiService =
                new ThreatProtectionPoliciesApiServiceImpl(apiMgtAdminService);
        Response response = threatProtectionApiService.threatProtectionPoliciesGet(getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        int apiCount = ( (ThreatProtectionPolicyListDTO) response.getEntity()).getList().size();
        Assert.assertEquals(apiCount, 3);
    }

    @Test
    public void threatProtectionPoliciesGetExceptionTestCase() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        Mockito.when(apiMgtAdminService.getThreatProtectionPolicyList()).thenThrow(
                new APIManagementException("", ExceptionCodes.APIMGT_DAO_EXCEPTION));

        ThreatProtectionPoliciesApiServiceImpl threatProtectionApiService =
                new ThreatProtectionPoliciesApiServiceImpl(apiMgtAdminService);
        Response response = threatProtectionApiService.threatProtectionPoliciesGet(getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Request getRequest() throws Exception {
        return Mockito.mock(Request.class);
    }
}
