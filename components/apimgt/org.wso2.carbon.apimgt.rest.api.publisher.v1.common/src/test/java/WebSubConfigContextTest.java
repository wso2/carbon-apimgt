/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.Constants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.WebsubSubscriptionConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.APITemplateBuilderImpl;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class WebSubConfigContextTest {

    private APIManagerConfiguration config;

    @Test
    public void testWithoutSecretConfigContextForAPI() throws Exception {

        API api = new API(new APIIdentifier("admin", "websubAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/websub");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointSecured(false);
        api.setUriTemplates(setAPIUriTemplates());
        api.setType(APIConstants.APITransportType.WEBSUB.toString());
        WebsubSubscriptionConfiguration webSubConfig =
                new WebsubSubscriptionConfiguration("", "", "");
        api.setWebsubSubscriptionConfiguration(webSubConfig);
        Environment environment = new Environment();
        environment.setType("production");

        config = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        String templatePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                File.separator + "resources" + File.separator;
        System.setProperty("carbon.home", templatePath);

        APITemplateBuilderImpl apiTemplateBuilder = new APITemplateBuilderImpl(api, null,
                null);
        String updatedTemplate = apiTemplateBuilder.getConfigStringForTemplate(environment);
        Assert.assertFalse("The websub velocity template is not updated correctly",
                updatedTemplate.contains("generated_signature"));
    }

    @Test
    public void testWithSecretConfigContextForAPI() throws Exception {

        API api = new API(new APIIdentifier("admin", "websubAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/websub");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointSecured(false);
        api.setUriTemplates(setAPIUriTemplates());
        api.setType(APIConstants.APITransportType.WEBSUB.toString());
        WebsubSubscriptionConfiguration webSubConfig =
                new WebsubSubscriptionConfiguration("9207975e1fef9c41fab41645f81dbf0f", "SHA1",
                        "x-hub-signature");
        api.setWebsubSubscriptionConfiguration(webSubConfig);
        Environment environment = new Environment();
        environment.setType("production");

        config = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        String templatePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                File.separator + "resources" + File.separator;
        System.setProperty("carbon.home", templatePath);

        APITemplateBuilderImpl apiTemplateBuilder = new APITemplateBuilderImpl(api, null,
                null);
        String updatedTemplate = apiTemplateBuilder.getConfigStringForTemplate(environment);
        Assert.assertTrue("The websub velocity template is not updated correctly",
                updatedTemplate.contains("generated_signature"));
    }

    private Set<URITemplate> setAPIUriTemplates() {

        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        URITemplate template = new URITemplate();
        template.setUriTemplate("/test");
        template.setHTTPVerb("GET");
        template.setThrottlingTier("Unlimited");
        template.setAuthType("Application");
        uriTemplates.add(template);
        return uriTemplates;
    }
}
