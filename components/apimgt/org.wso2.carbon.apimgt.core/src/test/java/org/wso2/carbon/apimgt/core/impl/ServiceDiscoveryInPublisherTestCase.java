package org.wso2.carbon.apimgt.core.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryImplConfig;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test that has to be included in APIPublisherImplTestCase.
 * Not included because it does not use powermock, whereas this test does.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceDiscoveryConfigBuilder.class)
public class ServiceDiscoveryInPublisherTestCase {

    @Test
    public void testDiscoverServiceEndpoints() throws Exception {
        /* Create a impl config without any namespace or criteria */
        ServiceDiscoveryImplConfig sdImplConfig = new ServiceDiscoveryImplConfig();
        sdImplConfig.setImplClass("org.wso2.carbon.apimgt.core.impl.ServiceDiscovererTestClass");
        sdImplConfig.setImplParameters(new HashMap<>());

        /* Create a impl config with a namespace set */
        Map<String, String> implParamWithNamespace = new HashMap<>();
        implParamWithNamespace.put(APIMgtConstants.ServiceDiscoveryConstants.NAMESPACE, "dev");
        ServiceDiscoveryImplConfig sdImplConfigWithNamespace = new ServiceDiscoveryImplConfig();
        sdImplConfigWithNamespace.setImplClass("org.wso2.carbon.apimgt.core.impl.ServiceDiscovererTestClass");
        sdImplConfigWithNamespace.setImplParameters(implParamWithNamespace);

        /* Create a impl config with a criteria set */
        Map<String, String> implParamWithCriteria = new HashMap<>();
        implParamWithCriteria.put(APIMgtConstants.ServiceDiscoveryConstants.CRITERIA, "app=web");
        ServiceDiscoveryImplConfig sdImplConfigWitCriteria = new ServiceDiscoveryImplConfig();
        sdImplConfigWitCriteria.setImplClass("org.wso2.carbon.apimgt.core.impl.ServiceDiscovererTestClass");
        sdImplConfigWitCriteria.setImplParameters(implParamWithCriteria);

        /* Create a impl config with both a namespace and a criteria is set */
        Map<String, String> implParamWithNamespaceAndCriteria = new HashMap<>();
        implParamWithNamespaceAndCriteria.put(APIMgtConstants.ServiceDiscoveryConstants.NAMESPACE, "dev");
        implParamWithNamespaceAndCriteria.put(APIMgtConstants.ServiceDiscoveryConstants.CRITERIA, "app=web");
        ServiceDiscoveryImplConfig sdImplConfigWithNamespaceNCriteria = new ServiceDiscoveryImplConfig();
        sdImplConfigWithNamespaceNCriteria.setImplClass("org.wso2.carbon.apimgt.core.impl.ServiceDiscovererTestClass");
        sdImplConfigWithNamespaceNCriteria.setImplParameters(implParamWithNamespaceAndCriteria);

        /* Create the implementationsList adding all four impl configs */
        List<ServiceDiscoveryImplConfig> implementationsList = new ArrayList<>();
        implementationsList.add(sdImplConfig);
        implementationsList.add(sdImplConfigWithNamespace);
        implementationsList.add(sdImplConfigWitCriteria);
        implementationsList.add(sdImplConfigWithNamespaceNCriteria);

        /* Create the final configuration */
        ServiceDiscoveryConfigurations sdConfig = new ServiceDiscoveryConfigurations();
        sdConfig.setEnabled(true);
        sdConfig.setImplementationsList(implementationsList);

        PowerMockito.mockStatic(ServiceDiscoveryConfigBuilder.class);
        PowerMockito.when(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration()).thenReturn(sdConfig);

        /* Check whether all four types of #listServices methods have been called */
        APIPublisherImpl apiPublisher = getApiPublisherImpl();
        List<Endpoint> endpoints = apiPublisher.discoverServiceEndpoints();
        Assert.assertEquals(endpoints.size(), 4);
    }

    @Test
    public void testDiscoverServiceEndpointsWhenDisabled() throws Exception {
        ServiceDiscoveryConfigurations sdConfig = new ServiceDiscoveryConfigurations();
        sdConfig.setEnabled(false);

        PowerMockito.mockStatic(ServiceDiscoveryConfigBuilder.class);
        PowerMockito.when(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration()).thenReturn(sdConfig);

        APIPublisherImpl apiPublisher = getApiPublisherImpl();
        List<Endpoint> endpoints = apiPublisher.discoverServiceEndpoints();
        Assert.assertTrue(endpoints.isEmpty());
    }

    private APIPublisherImpl getApiPublisherImpl() {
        return new APIPublisherImpl("admin", null, null, null, null, null, null, null, null, null, null,
                null, null, null);
    }
}
