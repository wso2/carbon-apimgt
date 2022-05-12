package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.http.client.HttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ExportThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.RestApiAdminUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.ConflictException;
import org.wso2.carbon.context.CarbonContext;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.APIM_VERSION;

@RunWith(PowerMockRunner.class) @PrepareForTest({ CarbonContext.class, APIManagerFactory.class, RestApiCommonUtil.class,
        APIProvider.class, ThrottlingApiServiceImpl.class,
        RestApiAdminUtils.class }) public class ExportImportThrottlingPolicyTest {

    @Test public void exportThrottlePolicyTest() throws IOException, APIManagementException {
        PowerMockito.mockStatic(APIProvider.class);
        APIProvider testApiProvider = Mockito.mock(APIProvider.class);

        PowerMockito.mockStatic(RestApiAdminUtils.class);

        HttpClient httpClient = Mockito.mock(HttpClient.class);

        String mockPolicy = "class ExportThrottlePolicyDTO {\n" + "    type: throttling policy\n"
                + "    subtype: application policy\n" + "    version: v4.1.0\n"
                + "    data: class ApplicationThrottlePolicyDTO {\n" + "        class ThrottlePolicyDTO {\n"
                + "            policyId: 1e360827-6925-4ce6-95f9-12b521109278\n"
                + "            policyName: TestPolicy\n" + "            displayName: TestPolicy\n"
                + "            description: null\n" + "            isDeployed: false\n"
                + "            type: ApplicationThrottlePolicy\n" + "        }\n" + "        defaultLimit: null\n"
                + "    }\n" + "}";

        ApplicationPolicy appPolicy = new ApplicationPolicy("TestPolicy");
        appPolicy.setPolicyName("TestPolicy");
        appPolicy.setUUID("1e360827-6925-4ce6-95f9-12b521109278");
        appPolicy.setDeployed(false);
        appPolicy.setDisplayName("TestPolicy");

        when(testApiProvider.getApplicationPolicy(Mockito.anyString(), Mockito.anyString())).thenReturn(appPolicy);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(testApiProvider);
        when(RestApiCommonUtil.getLoggedInUsername()).thenReturn("admin");
        when(RestApiAdminUtils.isPolicyAccessibleToUser(Mockito.anyString(), Mockito.any())).thenReturn(true);
        ThrottlingApiServiceImpl throttlingApiService = new ThrottlingApiServiceImpl();

        Response response = throttlingApiService.exportThrottlingPolicy("dss", "Test", "app", "YAML", null);

        Assert.assertEquals(response.getEntity().toString(), mockPolicy);

    }

    @Test public void importThrottlePolicyTestWrite() throws IOException, APIManagementException, URISyntaxException {
        ExportThrottlePolicyDTO exportedPolicy = new ExportThrottlePolicyDTO();
        SubscriptionThrottlePolicyDTO testSubPolicy = new SubscriptionThrottlePolicyDTO();
        testSubPolicy.setPolicyName("Test");
        testSubPolicy.setPolicyId("1e360827-6925-4ce6-95f9-12b521109278");
        exportedPolicy.version(APIM_VERSION);
        exportedPolicy.type(RestApiConstants.RESOURCE_THROTTLING_POLICY);
        exportedPolicy.setSubtype(RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY);
        exportedPolicy.data(testSubPolicy);

        APIProvider testApiProvider = Mockito.mock(APIProvider.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(testApiProvider);
        when(RestApiCommonUtil.getLoggedInUsername()).thenReturn("admin");

        ThrottlingApiServiceImpl throttlingApiService = new ThrottlingApiServiceImpl();
        ThrottlingApiServiceImpl throttlingApiServiceMock = spy(throttlingApiService);
        Mockito.doReturn(Response.ok().build()).when(throttlingApiServiceMock)
                .throttlingPoliciesSubscriptionPost(Mockito.any(), Mockito.any(), Mockito.any());
        Response response = throttlingApiServiceMock.importThrottlingPolicy(exportedPolicy, true, null);

        String message = "Successfully imported Subscription Throttling Policy : Test";
        Assert.assertEquals(message, response.getEntity().toString());
    }

    @Test public void importThrottlePolicyTestUpdate() throws IOException, APIManagementException, URISyntaxException {
        ExportThrottlePolicyDTO exportedPolicy = new ExportThrottlePolicyDTO();
        SubscriptionThrottlePolicyDTO testSubPolicy = new SubscriptionThrottlePolicyDTO();
        testSubPolicy.setPolicyName("Test");
        testSubPolicy.setPolicyId("1e360827-6925-4ce6-95f9-12b521109278");
        exportedPolicy.version(APIM_VERSION);
        exportedPolicy.type(RestApiConstants.RESOURCE_THROTTLING_POLICY);
        exportedPolicy.setSubtype(RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY);
        exportedPolicy.data(testSubPolicy);

        SubscriptionPolicy mockSubPolicy=new SubscriptionPolicy("Test");
        APIProvider testApiProvider = Mockito.mock(APIProvider.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(testApiProvider);
        when(RestApiCommonUtil.getLoggedInUsername()).thenReturn("admin");
        when(testApiProvider.getSubscriptionPolicy(Mockito.any(),Mockito.any())).thenReturn(mockSubPolicy);

        ThrottlingApiServiceImpl throttlingApiService = new ThrottlingApiServiceImpl();
        ThrottlingApiServiceImpl throttlingApiServiceMock = spy(throttlingApiService);

        ErrorDTO error =new ErrorDTO();
        ConflictException conflict=new ConflictException(error);
       Mockito.doThrow(conflict).when(throttlingApiServiceMock)
               .throttlingPoliciesSubscriptionPost(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(Response.ok().build()).when(throttlingApiServiceMock)
                .throttlingPoliciesSubscriptionPolicyIdPut(Mockito.any(), Mockito.any(), Mockito.any(),Mockito.any());
        Response response = throttlingApiServiceMock.importThrottlingPolicy(exportedPolicy, true, null);

        String message ="Successfully updated Subscription Throttling Policy : Test";
        Assert.assertEquals(message, response.getEntity().toString());
    }

}
