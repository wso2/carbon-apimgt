/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.OrgAccessControl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIManagerConfiguration.class})
public class APIMappingUtilTest {

    private APIManagerConfiguration config;
    private static final String url = "http://maps.googleapis.com/maps/api/geocode/json?address=Colombo";
    private static final String PROTOTYPED = "prototyped";
    private static final String PROVIDER = "admin";
    private static final String ENABLE_API_POLICIES = "false";
    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String ALLOW_METHOD = "GET";
    private static final String ALLOW_ORIGIN = "*";
    private static final String TARGET = "/*";
    private final APIDTO apidto = new APIDTO();

    @Before
    public void setup() {

        apidto.setName("testPrototypedAPI");
        apidto.setContext("/test");
        apidto.setVersion("1.0.0");
        APIOperationsDTO apiOperationsDTO = new APIOperationsDTO();
        apiOperationsDTO.setId("");
        apiOperationsDTO.setTarget(TARGET);
        apiOperationsDTO.setVerb(ALLOW_METHOD);
        apiOperationsDTO.setAuthType(APIConstants.OASResourceAuthTypes.APPLICATION_OR_APPLICATION_USER);
        apiOperationsDTO.setThrottlingPolicy(APIConstants.UNLIMITED_TIER);
        APIOperationPoliciesDTO apiOperationPoliciesDTO = new APIOperationPoliciesDTO();
        apiOperationsDTO.setOperationPolicies(apiOperationPoliciesDTO);
        List<APIOperationsDTO> operationList = new ArrayList<>();
        operationList.add(apiOperationsDTO);
        apidto.setOperations(operationList);
        apidto.setLifeCycleStatus(APIConstants.CREATED);
        config = Mockito.mock(APIManagerConfiguration.class);
        OrgAccessControl orgAccessControl = Mockito.mock(OrgAccessControl.class);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Mockito.when(config.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS))
                .thenReturn(AUTHORIZATION_HEADER);
        Mockito.when(config.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS))
                .thenReturn(ALLOW_METHOD);
        Mockito.when(config.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN))
                .thenReturn(ALLOW_ORIGIN);
        Mockito.when(config.getOrgAccessControl()).thenReturn(orgAccessControl);
        Mockito.when(orgAccessControl.isEnabled()).thenReturn(false);
    }

    @Test
    public void testPrototypeEndpointConfig() throws Exception {

        LinkedHashMap<String, Object> endpointConfigObj = new LinkedHashMap<>();
        LinkedHashMap<String, String> endpointObj = new LinkedHashMap<>();
        endpointObj.put(APIConstants.ENDPOINT_URL, url);
        endpointConfigObj.put(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE,
                APIConstants.HTTP_TRANSPORT_PROTOCOL_NAME);
        endpointConfigObj.put(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS, endpointObj);
        endpointConfigObj.put(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS, endpointObj);
        endpointConfigObj.put(APIConstants.IMPLEMENTATION_STATUS, PROTOTYPED);
        apidto.setEndpointConfig(endpointConfigObj);
        API model = APIMappingUtil.fromDTOtoAPI(apidto, PROVIDER);
        JsonParser jsonParser = new JsonParser();
        JsonObject endpointConfig = jsonParser.parse(model.getEndpointConfig()).getAsJsonObject();
        Assert.assertFalse("implementation_status flag not found",
                endpointConfig.has(APIConstants.IMPLEMENTATION_STATUS));
    }

    @Test
    public void testWsdlUrlMappingFromDtoToApiModel() throws APIManagementException {
        String expectedUrl = "http://example.com/service?wsdl";
        apidto.setWsdlUrl(expectedUrl);

        API apiModel = APIMappingUtil.fromDTOtoAPI(apidto, PROVIDER);
        Assert.assertEquals("WSDL URL not set on API model", expectedUrl, apiModel.getWsdlUrl());
    }
}
