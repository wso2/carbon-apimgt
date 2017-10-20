/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.template;

import org.apache.axis2.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.mockito.Mockito;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

public class SecurityConfigContextTest {

    private APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);

    @Test
    public void testSecurityConfigContext() throws Exception {

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIStatus.CREATED);
        api.setContextTemplate("/");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointUTUsername("admin");
        api.setEndpointUTPassword("admin");
        api.setEndpointSecured(true);
        api.setEndpointAuthDigest(true);
        ConfigContext configcontext = new APIConfigContext(api);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        SecurityConfigContext securityConfigContext = new SecurityConfigContextWrapper(configcontext, api, apiManagerConfiguration);
        VelocityContext velocityContext = securityConfigContext.getContext();

        Assert.assertTrue("Property isEndpointSecured cannot be false.",
                velocityContext.get("isEndpointSecured").equals(true));
        Assert.assertTrue("Property isEndpointAuthDigest cannot be false.",
                velocityContext.get("isEndpointAuthDigest").equals(true));
        Assert.assertTrue("Property username does not match.",
                "admin".equalsIgnoreCase(velocityContext.get("username").toString()));
        Assert.assertTrue("Property securevault_alias does not match.",
                "admin--TestAPI1.0.0".equalsIgnoreCase(velocityContext.get("securevault_alias").toString()));
        Assert.assertTrue("Property base64unpw does not match. ",
                new String(Base64.encodeBase64("admin:admin".getBytes())).
                        equalsIgnoreCase(velocityContext.get("base64unpw").toString()));
        Assert.assertTrue("Property isSecureVaultEnabled cannot be false. ",
                velocityContext.get("isSecureVaultEnabled").equals(true));
    }
}
