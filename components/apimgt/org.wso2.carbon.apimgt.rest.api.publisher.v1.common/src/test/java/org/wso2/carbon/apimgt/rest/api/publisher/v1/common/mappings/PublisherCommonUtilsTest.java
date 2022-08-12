/*
 *
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.endpoints.APIEndpointInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiCommonUtil.class, WorkflowExecutorFactory.class})
public class PublisherCommonUtilsTest {

    private static final String PROVIDER = "admin";
    private static final String API_PRODUCT_NAME = "test";
    private static final String API_PRODUCT_VERSION = "1.0.0";
    private static final String ORGANIZATION = "carbon.super";
    private static final String UUID = "63e1e37e-a5b8-4be6-86a5-d6ae0749f131";

    @Test
    public void testGetInvalidTierNames() throws Exception {

        List<String>  currentTiers = Arrays.asList(new String[]{"Unlimitted", "Platinum", "gold"});
        Tier mockTier = Mockito.mock(Tier.class);
        Tier tier1 = new Tier("Gold");
        Tier tier2 = new Tier("Unlimitted");
        Tier tier3 = new Tier("Silver");
        Set<Tier> allTiers = new HashSet<Tier>();
        allTiers.add(tier1);
        allTiers.add(tier2);
        allTiers.add(tier3);
        PowerMockito.whenNew(Tier.class).withAnyArguments().thenReturn(mockTier);
        Mockito.when(mockTier.getName()).thenReturn("Unlimitted");
        List<String> expectedInvalidTier = Arrays.asList(new String[]{"Platinum", "gold"});
        Assert.assertEquals(PublisherCommonUtils.getInvalidTierNames(allTiers, currentTiers), expectedInvalidTier);
    }

    @Test
    public void testChangeApiOrApiProductLifecycleToInvalidState() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Map<String, Object> apiLcData = new HashMap<>();
        String[] nextStates = new String[]{"Block", "Deploy as a Prototype", "Demote to Created", "Deprecate"};
        apiLcData.put(APIConstants.LC_NEXT_STATES, nextStates);
        apiLcData.put(APIConstants.API_STATUS, APIStatus.PUBLISHED.getStatus());
        Mockito.when(apiProvider.getAPILifeCycleData(Mockito.anyString(), Mockito.anyString())).thenReturn(apiLcData);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        try {
            PublisherCommonUtils.changeApiOrApiProductLifecycle("Retire", createMockAPIProduct(),
                    StringUtils.EMPTY, ORGANIZATION);
        } catch (APIManagementException e) {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().contains("Action 'Retire' is not allowed"));
        }
    }

    @Test
    public void testEncryptEndpointSecurityOAuthCredentialsOnlyUserPass() throws Exception {
        APIEndpointDTO apiEndpointDTO = new APIEndpointDTO();
        apiEndpointDTO.setName("TESTING_ENDPOINT");
        apiEndpointDTO.setId("213213-123-21-ee41234");
        apiEndpointDTO.setEndpointType(APIConstants.ENDPOINT_TYPE_AWSLAMBDA);

        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String oldAPISecret = "";

        //User and Pass
        Map<String,Object> endpointConfigUserPass = new HashMap<>();
        endpointConfigUserPass.put("url", "https://google.lk");
        endpointConfigUserPass.put("timeout", 1000);

        Map<String,Object> endpointSecurityUserPass = new HashMap<>();
        endpointSecurityUserPass.put("enabled", true);
        endpointSecurityUserPass.put("username", "test_user");
        endpointSecurityUserPass.put("password", "password123");

        endpointConfigUserPass.put(APIConstants.ENDPOINT_SECURITY, endpointSecurityUserPass);
        apiEndpointDTO.setEndpointConfig(endpointConfigUserPass);
        try {
            //username password check
            PublisherCommonUtils.encryptEndpointSecurityOAuthCredentials(apiEndpointDTO, cryptoUtil, oldAPISecret,
                    endpointConfigUserPass);
        } catch (APIManagementException | CryptoException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testEncryptEndpointSecurityOAuthCredentialsOnlyOAuth() throws Exception {
        APIEndpointDTO apiEndpointDTO = new APIEndpointDTO();
        apiEndpointDTO.setName("TESTING_ENDPOINT");
        apiEndpointDTO.setId("213213-123-21-ee41234");
        apiEndpointDTO.setEndpointType(APIConstants.ENDPOINT_TYPE_AWSLAMBDA);

        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String oldAPISecret = "";

        //Oauth 2
        Map<String,Object> endpointConfigOauth = new HashMap<>();
        endpointConfigOauth.put("url", "https://google.lk");

        Map<String, String> customParameters= new LinkedHashMap<>();
        customParameters.put("testingCustomParam", "{}");

        Map<String,Object> endpointSecurityOauth = new HashMap<>();
        endpointSecurityOauth.put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
        endpointSecurityOauth.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE, APIConstants.OAuthConstants.OAUTH);
        endpointSecurityOauth.put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, "2dawda233wd");

        endpointConfigOauth.put("endpoint_security", endpointSecurityOauth);
        apiEndpointDTO.setEndpointConfig(endpointConfigOauth);
        try {
            // Oauth2 checking
            PublisherCommonUtils.encryptEndpointSecurityOAuthCredentials(apiEndpointDTO, cryptoUtil,
                    oldAPISecret, endpointConfigOauth);
        } catch (APIManagementException | CryptoException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testEncryptEndpointSecurityOAuthCredentialsOnlyOAuthUpdate() throws Exception {
        APIEndpointDTO apiEndpointDTO = new APIEndpointDTO();
        apiEndpointDTO.setName("TESTING_ENDPOINT");
        apiEndpointDTO.setId("213213-123-21-ee41234");
        apiEndpointDTO.setEndpointType(APIConstants.ENDPOINT_TYPE_AWSLAMBDA);

        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String oldAPISecret = "sample-secret";

        //Oauth 2 while Update
        Map<String,Object> endpointConfigOauthUp = new HashMap<>();
        endpointConfigOauthUp.put("url", "https://google.lk");
        endpointConfigOauthUp.put("timeout", 1000);

        Map<String,Object> endpointSecurityOauthUp = new HashMap<>();
        endpointSecurityOauthUp.put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, "{}");
        endpointSecurityOauthUp.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE, APIConstants.OAuthConstants.OAUTH);

        endpointConfigOauthUp.put("endpoint_security", endpointSecurityOauthUp);
        try {
            // Oauth2 checking
            PublisherCommonUtils.encryptEndpointSecurityOAuthCredentials(apiEndpointDTO, cryptoUtil,
                    oldAPISecret, endpointConfigOauthUp);
        } catch (APIManagementException | CryptoException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }



    private ApiTypeWrapper createMockAPIProduct() {

        APIProduct product = new APIProduct(new APIProductIdentifier(PROVIDER, API_PRODUCT_NAME, API_PRODUCT_VERSION,
                UUID));
        product.setState(APIConstants.PUBLISHED);
        return new ApiTypeWrapper(product);
    }
}
