/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

import org.apache.axis2.Constants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.APITemplateBuilderImpl;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class EndpointsFaultSequenceTemplateTest {

    @Before
    public void init() {

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        System.setProperty("carbon.home", System.getProperty("user.dir") + File.separator + "src"
                + File.separator + "test" + File.separator + "resources" + File.separator);
    }

    @After
    public void tearDown() {

        APIManagerConfiguration.getAiApiConfigurationsDTO().setCustomErrorResponseSequence(null);
    }

    @Test
    public void testDelegatedFaultsUseTheAiCustomErrorSequenceLikeTheResourceDoes() throws Exception {

        // The resource itself would use this sequence; the fault sequence's delegate branch must match.
        APIManagerConfiguration.getAiApiConfigurationsDTO().setCustomErrorResponseSequence("_ai_custom_error_");

        String rendered = render(APIConstants.APIEndpoint.PRODUCTION);

        Assert.assertTrue(rendered, rendered.contains("<sequence key=\"_ai_custom_error_\"/>"));
        Assert.assertFalse(rendered, rendered.contains("<sequence key=\"fault\"/>"));
    }

    @Test
    public void testDelegatedFaultsFallBackToTheDefaultFaultSequence() throws Exception {

        String rendered = render(APIConstants.APIEndpoint.PRODUCTION);

        Assert.assertTrue(rendered, rendered.contains("<sequence key=\"fault\"/>"));
    }

    @Test
    public void testFaultSequenceIsNamedAndReentersTheEndpointsSequenceForItsStage() throws Exception {

        String rendered = render(APIConstants.APIEndpoint.SANDBOX);

        Assert.assertTrue(rendered, rendered.contains("name=\"ChatAPI--v1.0.0_EndpointsFaultSeqSANDBOX\""));
        Assert.assertTrue(rendered, rendered.contains("<sequence key=\"ChatAPI--v1.0.0_EndpointsSeqSANDBOX\"/>"));
        Assert.assertTrue(rendered, rendered.contains("<property name=\"direction\" value=\"FAULT\""));
    }

    // Renders the fault sequence; the template's macros leave whitespace inside tags, so it's collapsed.
    private String render(String stage) throws Exception {

        String rendered = new APITemplateBuilderImpl(createAiApi(), null, null)
                .getStringForEndpointsFaultSequence(stage);
        return rendered.replaceAll("=\\s+", "=").replaceAll("\\s+/>", "/>");
    }

    private API createAiApi() {

        API api = new API(new APIIdentifier("admin", "ChatAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/chat");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointSecured(false);
        api.setSubtype(APIConstants.API_SUBTYPE_AI_API);
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        URITemplate template = new URITemplate();
        template.setUriTemplate("/chat/completions");
        template.setHTTPVerb("POST");
        template.setThrottlingTier("Unlimited");
        template.setAuthType("Application");
        uriTemplates.add(template);
        api.setUriTemplates(uriTemplates);
        return api;
    }

    @Test
    public void testRenderingWithoutAnApiFailsWithAClearErrorInsteadOfANullPointer() throws Exception {

        try {
            new APITemplateBuilderImpl(null, null, null).getStringForEndpointsFaultSequence("PRODUCTION");
            Assert.fail("expected an APITemplateException");
        } catch (org.wso2.carbon.apimgt.impl.template.APITemplateException e) {
            Assert.assertTrue(e.getMessage().contains("no API is set"));
        }
    }

    @Test
    public void testRenderingFailsWithATemplateExceptionWhenTheTemplateIsMissing() throws Exception {

        // A customised or migrated api_templates folder without the fault template.
        APITemplateBuilderImpl builder = new APITemplateBuilderImpl(createAiApi(), null, null) {
            @Override
            public String getEndpointsFaultSequenceTemplatePath() {

                return "repository" + File.separator + "resources" + File.separator + "api_templates"
                        + File.separator + "no_such_fault_seq_template.xml";
            }
        };
        try {
            builder.getStringForEndpointsFaultSequence(APIConstants.APIEndpoint.PRODUCTION);
            Assert.fail("expected an APITemplateException");
        } catch (org.wso2.carbon.apimgt.impl.template.APITemplateException e) {
            Assert.assertEquals("Velocity Error", e.getMessage());
            // The caller recognises this cause and deploys without the feature instead of failing.
            Assert.assertTrue("the cause must be the missing template",
                    e.getCause() instanceof org.apache.velocity.exception.ResourceNotFoundException);
        }
    }

}
