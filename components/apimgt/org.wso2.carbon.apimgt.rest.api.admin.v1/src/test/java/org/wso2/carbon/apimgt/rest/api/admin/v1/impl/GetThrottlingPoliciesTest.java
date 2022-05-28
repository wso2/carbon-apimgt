package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDetailsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDetailsListDTO;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class) @PrepareForTest({
        ThrottlingApiServiceImpl.class, }) public class GetThrottlingPoliciesTest {

    @Test
    public void getThrottlePolicyTest() throws Exception {

        ThrottlePolicyDetailsDTO mockPolicy = new ThrottlePolicyDetailsDTO();
        mockPolicy.setIsDeployed(true);
        mockPolicy.setUuid("99d46f88-96a6-4a46-924a-773f3cae3d8b");
        mockPolicy.setPolicyId(1);
        mockPolicy.setPolicyName("Test Policy");
        mockPolicy.setDisplayName("Test Policy");
        mockPolicy.setDescription("This policy is a Testing Policy");
        mockPolicy.setType(PolicyConstants.POLICY_LEVEL_SUB);
        List<ThrottlePolicyDetailsDTO> list = new ArrayList<>();
        list.add(mockPolicy);

        ThrottlePolicyDetailsListDTO expectedResult = new ThrottlePolicyDetailsListDTO();
        expectedResult.setList(list);

        ThrottlingApiServiceImpl throttlingApiService = new ThrottlingApiServiceImpl();
        ThrottlingApiServiceImpl throttlingApiServiceMock = PowerMockito.spy(throttlingApiService);

        PowerMockito.doReturn(list).when(throttlingApiServiceMock, "getThrottlingPolicies", Mockito.anyString());

        Response response = throttlingApiServiceMock.throttlingPolicySearch( "null", null);

        Assert.assertEquals(expectedResult, response.getEntity());
    }

}
