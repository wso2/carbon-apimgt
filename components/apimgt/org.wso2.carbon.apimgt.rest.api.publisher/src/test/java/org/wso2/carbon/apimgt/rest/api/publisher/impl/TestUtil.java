package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_endpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.msf4j.Request;

import java.util.*;

public class TestUtil {

    APIDTO getAPIDTO(String apiID, String apiName, String apiVersion, String apiContext) {

        EndPointDTO endPointDTO1 = new EndPointDTO();
        endPointDTO1.setId("123");
        endPointDTO1.setName("EP1");
        endPointDTO1.setEndpointConfig("endpointConfig1");

        API_endpointDTO apiEndpointDTO1 = new API_endpointDTO();
        apiEndpointDTO1.setKey("k1");
        apiEndpointDTO1.setType("EP");
        apiEndpointDTO1.setInline(endPointDTO1);

        List<API_endpointDTO> list1 = new ArrayList<>();
        list1.add(apiEndpointDTO1);

        Set<String> transport = new HashSet<>();
        transport.add("http");

        Set<Policy> policies = new HashSet<>();
        policies.add(new SubscriptionPolicy("Silver"));
        policies.add(new SubscriptionPolicy("Bronze"));

        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner("John Doe");
        businessInformation.setBusinessOwnerEmail("john.doe@annonymous.com");
        businessInformation.setTechnicalOwner("Jane Doe");
        businessInformation.setBusinessOwnerEmail("jane.doe@annonymous.com");

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(Arrays.asList("GET", "POST", "DELETE"));
        corsConfiguration.setAllowHeaders(Arrays.asList("Authorization", "X-Custom"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Collections.singletonList("*"));

        APIDTO api = new APIDTO();
        api.setId(apiID);
        api.setName(apiName);
        api.setContext(apiContext);
        api.setVersion(apiVersion);
        api.setProvider("provider");
        api.setDescription("sample descripiton");
        api.setLifeCycleStatus("PUBLISHED");
        api.setEndpoint(list1);
        api.setWsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl");
        api.setResponseCaching("true");
        api.isDefaultVersion(true);
        api.setCacheTimeout(120);
//        api.transport(transport);
//        api.setTags(tags);


        return api;
    }

    Request getMockRequest() {
        Request request = null;
        return request;
    }

//    id(apidto.getId()).
//    context(apidto.getContext()).
//    description(apidto.getDescription()).
//    lifeCycleStatus(apidto.getLifeCycleStatus()).
//    endpoint(fromEndpointListToMap(apidto.getEndpoint())).
//    visibleRoles(new HashSet<>(apidto.getVisibleRoles())).
//    policies(subscriptionPolicies).
//    apiPermission(apidto.getPermission()).
//    tags(new HashSet<>(apidto.getTags())).
//    labels(new HashSet<>(apidto.getLabels())).
//    transport(new HashSet<>(apidto.getTransport())).
//    isResponseCachingEnabled(Boolean.valueOf(apidto.getResponseCaching())).
//    businessInformation(businessInformation).
//    uriTemplates(uriTemplateList).
//    corsConfiguration(corsConfiguration);
}
