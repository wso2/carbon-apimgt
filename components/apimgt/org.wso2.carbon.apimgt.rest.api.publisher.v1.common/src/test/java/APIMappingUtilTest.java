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
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.OrgAccessControl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesMapDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Builds a minimal API model with two additional properties - one plain and one carrying the
     * "__display" suffix - to exercise the expandProperties mapping path of the API listing.
     */
    private API createApiWithAdditionalProperties() {

        API api = new API(new APIIdentifier(PROVIDER, "PizzaShackAPI", "1.0.0"));
        api.setUUID("873a103f-684b-470e-a4cb-a4b2a3d87ea1");
        api.setContextTemplate("/pizzashack");
        api.setStatus(APIConstants.PUBLISHED);
        api.setType(APIConstants.API_TYPE_HTTP);

        JSONObject additionalProperties = new JSONObject();
        additionalProperties.put("dept", "finance");
        additionalProperties.put("owner" + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX, "jane");
        api.setAdditionalProperties(additionalProperties);
        return api;
    }

    @Test
    public void testFromAPIToInfoDTOWithoutExpandPropertiesKeepsDefaults() {

        APIInfoDTO apiInfoDTO = APIMappingUtil.fromAPIToInfoDTO(createApiWithAdditionalProperties(), false);

        Assert.assertTrue("additionalProperties must stay empty when expandProperties is false",
                apiInfoDTO.getAdditionalProperties().isEmpty());
        Assert.assertTrue("additionalPropertiesMap must stay empty when expandProperties is false",
                apiInfoDTO.getAdditionalPropertiesMap().isEmpty());
    }

    @Test
    public void testFromAPIToInfoDTOSingleArgumentOverloadKeepsIncludingAvailableProperties() {

        APIInfoDTO apiInfoDTO = APIMappingUtil.fromAPIToInfoDTO(createApiWithAdditionalProperties());

        Assert.assertEquals("The single argument overload must keep including the additional properties it "
                + "has always included", 2, apiInfoDTO.getAdditionalProperties().size());
        Assert.assertEquals("The single argument overload must keep including the additional properties it "
                + "has always included", 2, apiInfoDTO.getAdditionalPropertiesMap().size());
    }

    @Test
    public void testFromAPIToInfoDTOSingleArgumentOverloadHandlesAbsentProperties() {

        API api = createApiWithAdditionalProperties();
        api.setAdditionalProperties(null);

        APIInfoDTO apiInfoDTO = APIMappingUtil.fromAPIToInfoDTO(api);

        Assert.assertTrue("An API without additional properties must map to an empty list",
                apiInfoDTO.getAdditionalProperties().isEmpty());
        Assert.assertTrue("An API without additional properties must map to an empty map",
                apiInfoDTO.getAdditionalPropertiesMap().isEmpty());
    }

    @Test
    public void testFromAPIListToDTOSingleArgumentOverloadKeepsIncludingAvailableProperties()
            throws APIManagementException {

        List<API> apiList = new ArrayList<>();
        apiList.add(createApiWithAdditionalProperties());

        APIListDTO apiListDTO = (APIListDTO) APIMappingUtil.fromAPIListToDTO(apiList);

        Assert.assertEquals(2, apiListDTO.getList().get(0).getAdditionalProperties().size());
        Assert.assertEquals(2, apiListDTO.getList().get(0).getAdditionalPropertiesMap().size());
    }

    @Test
    public void testFromAPIToInfoDTOWithExpandPropertiesPopulatesBothFields() {

        APIInfoDTO apiInfoDTO = APIMappingUtil.fromAPIToInfoDTO(createApiWithAdditionalProperties(), true);

        List<APIInfoAdditionalPropertiesDTO> additionalProperties = apiInfoDTO.getAdditionalProperties();
        Assert.assertEquals(2, additionalProperties.size());
        for (APIInfoAdditionalPropertiesDTO property : additionalProperties) {
            if ("dept".equals(property.getName())) {
                Assert.assertEquals("finance", property.getValue());
                Assert.assertFalse("A property without the __display suffix is not displayable",
                        property.isDisplay());
            } else if ("owner".equals(property.getName())) {
                Assert.assertEquals("jane", property.getValue());
                Assert.assertTrue("A property with the __display suffix is displayable", property.isDisplay());
            } else {
                Assert.fail("Unexpected additional property " + property.getName());
            }
        }

        Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap =
                apiInfoDTO.getAdditionalPropertiesMap();
        Assert.assertEquals(2, additionalPropertiesMap.size());

        APIInfoAdditionalPropertiesMapDTO deptProperty = additionalPropertiesMap.get("dept");
        Assert.assertEquals("dept", deptProperty.getName());
        Assert.assertEquals("finance", deptProperty.getValue());
        Assert.assertFalse(deptProperty.isDisplay());

        // The map is keyed by the raw property name, so the __display suffix is carried by the key while the
        // name is stripped. display is intentionally always false here - fromDTOtoAPI rebuilds the stored key
        // as <mapKey> + "__display" whenever isDisplay() is true, so a true value would round-trip a
        // displayable property back as "owner__display__display".
        APIInfoAdditionalPropertiesMapDTO ownerProperty = additionalPropertiesMap
                .get("owner" + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX);
        Assert.assertEquals("The map entry name is stripped of the __display suffix", "owner",
                ownerProperty.getName());
        Assert.assertEquals("jane", ownerProperty.getValue());
        Assert.assertFalse("display must stay false so that fromDTOtoAPI does not append __display twice",
                ownerProperty.isDisplay());
    }

    @Test
    public void testFromAPIListToDTOPropagatesExpandProperties() throws APIManagementException {

        List<API> apiList = new ArrayList<>();
        apiList.add(createApiWithAdditionalProperties());

        // This is the call McpServersApiServiceImpl makes, so it pins the MCP server listing to its
        // pre-existing response shape even though the API model now carries the additional properties.
        APIListDTO notExpanded = (APIListDTO) APIMappingUtil.fromAPIListToDTO(apiList, false);
        Assert.assertTrue(notExpanded.getList().get(0).getAdditionalProperties().isEmpty());
        Assert.assertTrue(notExpanded.getList().get(0).getAdditionalPropertiesMap().isEmpty());

        APIListDTO expanded = (APIListDTO) APIMappingUtil.fromAPIListToDTO(apiList, true);
        Assert.assertEquals(2, expanded.getList().get(0).getAdditionalProperties().size());
        Assert.assertEquals(2, expanded.getList().get(0).getAdditionalPropertiesMap().size());
    }
}
