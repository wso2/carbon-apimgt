/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.token;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {CarbonUtils.class})
public class JWTSignatureAlgTestCase {

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(CarbonUtils.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty(APIConstants.PORT_OFFSET_CONFIG)).thenReturn("2");
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(
                new APIManagerConfigurationServiceImpl(config));
    }

    @Test
    public void testJwtSignatureAlgorithm(){

        String noneAlg = APIUtil.getJWSCompliantAlgorithmCode(null);

        Assert.assertTrue("Expected 'none', but was " + noneAlg, "none".equals(noneAlg));

        noneAlg = APIUtil.getJWSCompliantAlgorithmCode("NONE");

        Assert.assertTrue("Expected 'none', but was " + noneAlg, "none".equals(noneAlg));

        String shaWithRsa256Code = APIUtil.getJWSCompliantAlgorithmCode("SHA256withRSA");

        Assert.assertTrue("Expected 'RS256' but was " + shaWithRsa256Code, "RS256".equals(shaWithRsa256Code));

        shaWithRsa256Code = APIUtil.getJWSCompliantAlgorithmCode("RS256");

        Assert.assertTrue("Expected 'RS256' but was " + shaWithRsa256Code, "RS256".equals(shaWithRsa256Code));
    }
}
