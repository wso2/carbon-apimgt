/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.template;

import junit.framework.TestCase;

public class TemplateBuilderTest extends TestCase {
    
    public void testBasicAPI() throws Exception {
//        Map<String,String> apiMappings = new HashMap<String, String>();
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_NAME, "TestAPI");
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_CONTEXT, "/test");
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_VERSION, "1.0.0");
//
//        List<Map<String, String>> resourceMappings = new ArrayList<Map<String, String>>();
//        Map<String,String> resource = new HashMap<String, String>();
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, "/*");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, "GET");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, "http://wso2.org");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_SANDBOX_URI, null);
//        resourceMappings.add(resource);
//
//        Map<String, String> testHandlerMappings_1 = new HashMap<String, String>();
//        testHandlerMappings_1.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler");
//
//        Map<String, String> testHandlerMappings_2 = new HashMap<String, String>();
//        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler");
//
//        Map<String, String> testHandlerMappings_3 = new HashMap<String, String>();
//        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler");
//        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER_POLICY_KEY,
//                "conf:/basic-throttle-policy.xml");
//
//        List<Map<String, String>> handlerMappings = new ArrayList<Map<String, String>>();
//        handlerMappings.add(testHandlerMappings_1);
//        handlerMappings.add(testHandlerMappings_2);
//        handlerMappings.add(testHandlerMappings_3);
//
//        BasicTemplateBuilder builder = new BasicTemplateBuilder(apiMappings, resourceMappings, handlerMappings);
//        String output = builder.getConfigStringForTemplate(new Environment());
//        String expected = "<api xmlns=\"http://ws.apache.org/ns/synapse\" name=\"TestAPI\" context=\"/test\" " +
//                "version=\"1.0.0\" version-type=\"url\"><resource url-mapping=\"/*\" methods=\"GET\"><inSequence>" +
//                "<property name=\"POST_TO_URI\" value=\"true\" scope=\"axis2\"/><filter source=\"$ctx:AM_KEY_TYPE\" regex=\"PRODUCTION\"><then><send><endpoint name=\"TestAPI_APIEndpoint_0\">" +
//                "<address uri=\"http://wso2.org\"/></endpoint></send></then><else><sequence key=\"_sandbox_key_error_\"/>" +
//                "</else></filter></inSequence><outSequence><send/></outSequence></resource><handlers>" +
//                "<handler class=\"org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler\"/>" +
//                "<handler class=\"org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler\"/>" +
//                "<handler class=\"org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler\">" +
//                "<property name=\"id\" value=\"A\"/><property name=\"policyKey\" value=\"conf:/basic-throttle-policy.xml\"/>" +
//                "</handler></handlers></api>";
//
//        String formattedOutput = AXIOMUtil.stringToOM(output).getText();
//        String formattedExpected = AXIOMUtil.stringToOM(expected).getText();
//
//        assertEquals(formattedExpected, formattedOutput);
    }

    public void testURLEncode() throws Exception {
//        Map<String,String> apiMappings = new HashMap<String, String>();
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_NAME, "TestAPI");
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_CONTEXT, "/test");
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_VERSION, "1.0.0");
//
//        List<Map<String, String>> resourceMappings = new ArrayList<Map<String, String>>();
//        Map<String,String> resource = new HashMap<String, String>();
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, "/*");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, "GET");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, "http://search.wso2.com/search.json?q=blue%20angels&rpp=5&include_entities=true&result_type=mixed");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_SANDBOX_URI, null);
//        resourceMappings.add(resource);
//
//        Map<String, String> testHandlerMappings_1 = new HashMap<String, String>();
//        testHandlerMappings_1.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler");
//
//        Map<String, String> testHandlerMappings_2 = new HashMap<String, String>();
//        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler");
//
//        Map<String, String> testHandlerMappings_3 = new HashMap<String, String>();
//        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler");
//        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER_POLICY_KEY,
//                "conf:/basic-throttle-policy.xml");
//
//        List<Map<String, String>> handlerMappings = new ArrayList<Map<String, String>>();
//        handlerMappings.add(testHandlerMappings_1);
//        handlerMappings.add(testHandlerMappings_2);
//        handlerMappings.add(testHandlerMappings_3);
//
//        BasicTemplateBuilder builder = new BasicTemplateBuilder(apiMappings, resourceMappings, handlerMappings);
//        String output = builder.getConfigStringForTemplate(new Environment());
//        String expected = "<api xmlns=\"http://ws.apache.org/ns/synapse\" name=\"TestAPI\" context=\"/test\" " +
//                "version=\"1.0.0\" version-type=\"url\"><resource url-mapping=\"/*\" methods=\"GET\"><inSequence>" +
//                "<property name=\"POST_TO_URI\" value=\"true\" scope=\"axis2\"/><filter source=\"$ctx:AM_KEY_TYPE\" regex=\"PRODUCTION\"><then><send><endpoint name=\"TestAPI_APIEndpoint_0\">" +
//                "<address uri=\"http://search.wso2.com/search.json?q=blue%20angels&amp;rpp=5&amp;include_entities=true&amp;result_type=mixed\"/></endpoint></send></then><else><sequence key=\"_sandbox_key_error_\"/>" +
//                "</else></filter></inSequence><outSequence><send/></outSequence></resource><handlers>" +
//                "<handler class=\"org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler\"/>" +
//                "<handler class=\"org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler\"/>" +
//                "<handler class=\"org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler\">" +
//                "<property name=\"id\" value=\"A\"/><property name=\"policyKey\" value=\"conf:/basic-throttle-policy.xml\"/>" +
//                "</handler></handlers></api>";
//
//        String formattedOutput = AXIOMUtil.stringToOM(output).getText();
//        String formattedExpected = AXIOMUtil.stringToOM(expected).getText();
//
//        assertEquals(formattedExpected, formattedOutput);
    }

    public void testAdvancedAPI() throws Exception {
//        Map<String,String> apiMappings = new HashMap<String, String>();
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_NAME, "TestAPI");
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_CONTEXT, "/test");
//        apiMappings.put(APITemplateBuilder.KEY_FOR_API_VERSION, "1.0.0");
//
//        List<Map<String, String>> resourceMappings = new ArrayList<Map<String, String>>();
//        Map<String,String> resource = new HashMap<String, String>();
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, "/*");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, "GET");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, "http://wso2.org");
//        resource.put(APITemplateBuilder.KEY_FOR_RESOURCE_SANDBOX_URI, "http://staging.wso2.org");
//        resourceMappings.add(resource);
//
//        Map<String, String> testHandlerMappings_1 = new HashMap<String, String>();
//        testHandlerMappings_1.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler");
//
//        Map<String, String> testHandlerMappings_2 = new HashMap<String, String>();
//        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler");
//
//        Map<String, String> testHandlerMappings_3 = new HashMap<String, String>();
//        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER,
//                "org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler");
//        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER_POLICY_KEY,
//                "conf:/basic-throttle-policy.xml");
//
//        List<Map<String, String>> handlerMappings = new ArrayList<Map<String, String>>();
//        handlerMappings.add(testHandlerMappings_1);
//        handlerMappings.add(testHandlerMappings_2);
//        handlerMappings.add(testHandlerMappings_3);
//
//        BasicTemplateBuilder builder = new BasicTemplateBuilder(apiMappings, resourceMappings, handlerMappings);
//        String output = builder.getConfigStringForTemplate(new Environment());
//        String expected = "<api xmlns=\"http://ws.apache.org/ns/synapse\" name=\"TestAPI\" context=\"/test\" " +
//                "version=\"1.0.0\" version-type=\"url\"><resource url-mapping=\"/*\" methods=\"GET\"><inSequence>" +
//                "<property name=\"POST_TO_URI\" value=\"true\" scope=\"axis2\"/><filter source=\"$ctx:AM_KEY_TYPE\" regex=\"PRODUCTION\"><then><send><endpoint name=\"TestAPI_APIEndpoint_0\">" +
//                "<address uri=\"http://wso2.org\"/></endpoint></send></then><else><send><endpoint " +
//                "name=\"TestAPI_APISandboxEndpoint_0\"><address uri=\"http://staging.wso2.org\"/></endpoint>" +
//                "</send></else></filter></inSequence><outSequence><send/></outSequence></resource><handlers>" +
//                "<handler class=\"org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler\"/>" +
//                "<handler class=\"org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler\"/>" +
//                "<handler class=\"org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler\">" +
//                "<property name=\"id\" value=\"A\"/><property name=\"policyKey\" value=\"conf:/basic-throttle-policy.xml\"/>" +
//                "</handler></handlers></api>";
//
//        String formattedOutput = AXIOMUtil.stringToOM(output).getText();
//        String formattedExpected = AXIOMUtil.stringToOM(expected).getText();
//
//        assertEquals(formattedExpected, formattedOutput);
    }
}
