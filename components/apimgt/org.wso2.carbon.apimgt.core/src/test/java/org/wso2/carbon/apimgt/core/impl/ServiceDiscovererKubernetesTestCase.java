/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.LoadBalancerIngressBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServiceListBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import io.fabric8.openshift.client.OpenShiftClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDiscovererKubernetesTestCase {

    private static final String MASTER_URL = "https://200.20.20.20/";
    private List<Service> listOfServices;

    @Test(description = "Test init method when external service account token file name is NOT given")
    public void testInitWhileExternalTokenFileNameNotGiven() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        try {
            sdKubernetes.initImpl(createImplParametersMap(""));
        } catch (ServiceDiscoveryException e) {
            //since pod's token is then searched, this is exception msg we get
            Assert.assertEquals(e.getCause().getMessage(),
                    "Error while reading file /var/run/secrets/kubernetes.io/serviceaccount/token");
        }
    }

    @Test(description = "Test init method with external service account token file name")
    public void testInitWhileExternalTokenFileNameGiven() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        try {
            sdKubernetes.initImpl(createImplParametersMap("TestK8Token"));
        } catch (ServiceDiscoveryException e) {
            Assert.assertEquals(e.getCause().getMessage(),
                    "File to decrypt does not exist");
        }
    }

    @Test(description = "Test all listServices methods while client is null")
    public void testListServicesWhileClientIsNull() throws Exception {
        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        String namespace = "dev";
        Map<String, String> criteria = new HashMap<>();

        //list method called without init method
        Assert.assertEquals(true, sdKubernetes.listServices().isEmpty());
        Assert.assertEquals(true, sdKubernetes.listServices(namespace).isEmpty());
        Assert.assertEquals(true, sdKubernetes.listServices(criteria).isEmpty());
        Assert.assertEquals(true, sdKubernetes.listServices(namespace, criteria).isEmpty());
    }

    @Test(description = "Test .listServices() method")
    public void testListServices() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        sdKubernetes.setIncludeClusterIP(true);                 //Include ClusterIPs
        sdKubernetes.setIncludeExternalNameTypeServices(true);  //Include ExternalNames

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(openShiftClient.services().inNamespace(null)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.list()).thenReturn(createServiceList());
        Mockito.when(openShiftClient.getMasterUrl()).thenReturn(new URL(MASTER_URL));
        List<Endpoint> endpoints = sdKubernetes.listServices();

        Assert.assertEquals(endpoints.size(), 10);
        Mockito.verify(openShiftClient, Mockito.times(2)).getMasterUrl(); //2 NodePort URL endpoints
    }

    @Test(description = "Test .listServices() method without ClusterIPs and ExternalNamesURLs")
    public void testListServicesWithoutCLusterIPAndExternalName() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        sdKubernetes.setIncludeClusterIP(false);                //Not include ClusterIPs
        sdKubernetes.setIncludeExternalNameTypeServices(false); //Not include ExternalNames

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(openShiftClient.services().inNamespace(null)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.list()).thenReturn(createServiceList());
        Mockito.when(openShiftClient.getMasterUrl()).thenReturn(new URL(MASTER_URL));
        List<Endpoint> endpoints = sdKubernetes.listServices();

        Assert.assertEquals(endpoints.size(), 5);
        Mockito.verify(openShiftClient, Mockito.times(2)).getMasterUrl();
    }

    @Test(description = "Test .listServices(Namespace) method")
    public void testListServicesWithNamespace() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        sdKubernetes.setIncludeClusterIP(false);                //Not include ClusterIPs
        sdKubernetes.setIncludeExternalNameTypeServices(true);  //Include ExternalNames

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(openShiftClient.services().inNamespace("dev")).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.list()).thenReturn(createServiceListWithNamespace());
        Mockito.when(openShiftClient.getMasterUrl()).thenReturn(new URL(MASTER_URL));
        List<Endpoint> endpoints = sdKubernetes.listServices("dev");

        Assert.assertEquals(endpoints.size(), 4);
    }

    @Test(description = "Test .listServices(Criteria) method")
    public void testListServicesWithCriteria() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        sdKubernetes.setIncludeClusterIP(true);                 //Include ClusterIPs
        sdKubernetes.setIncludeExternalNameTypeServices(false); //Not include ExternalNames
        Map<String, String> oneLabel = createOneLabelHashMap();

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        BaseOperation baseOperation = Mockito.mock(BaseOperation.class);
        Mockito.when(openShiftClient.services().inNamespace(null)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withLabels(oneLabel)).thenReturn(baseOperation);
        Mockito.when(baseOperation.list()).thenReturn(createServiceListWithCriteria());
        Mockito.when(openShiftClient.getMasterUrl()).thenReturn(new URL(MASTER_URL));
        List<Endpoint> endpoints = sdKubernetes.listServices(oneLabel);

        Assert.assertEquals(endpoints.size(), 3);
    }

    @Test(description = "Test .listServices(Namespace, Criteria) method")
    public void testListServicesWithBothNamespaceAndCriteria() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        sdKubernetes.setIncludeClusterIP(true);                 //Include ClusterIPs
        sdKubernetes.setIncludeExternalNameTypeServices(true);  //Include ExternalNames
        Map<String, String> oneLabel = createOneLabelHashMap();

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        BaseOperation baseOperation = Mockito.mock(BaseOperation.class);
        Mockito.when(openShiftClient.services().inNamespace("dev")).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withLabels(oneLabel)).thenReturn(baseOperation);
        Mockito.when(baseOperation.list()).thenReturn(createServiceListWithBothNamespaceAndCriteria());
        List<Endpoint> endpoints = sdKubernetes.listServices("dev", oneLabel);

        Assert.assertEquals(endpoints.size(), 2);
    }

    @Test(description = "Test .listServices() while the list has only one service and its port is not http nor https")
    public void testListServicesWithWrongPortType() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        sdKubernetes.setIncludeExternalNameTypeServices(true); //Include ExternalNames (includeClusterIP not checked)

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(openShiftClient.services().inNamespace(null)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.list()).thenReturn(createMalformedServiceList("somePort"));
        List<Endpoint> endpoints = sdKubernetes.listServices();

        Assert.assertTrue(endpoints.isEmpty());
    }

    @Test(description = "Test .listServices() while the list only has a LoadBalancer type service without any ingress")
    public void testListServicesOfLoadBalancerTypeWithoutIngress() throws Exception {
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        sdKubernetes.setClient(openShiftClient);
        sdKubernetes.setIncludeClusterIP(false);                 //Not include ClusterIPs
        sdKubernetes.setIncludeExternalNameTypeServices(false);  //Not include ExternalNames

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        Mockito.when(openShiftClient.services().inNamespace(null)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.list()).thenReturn(createMalformedServiceList("http"));
        Mockito.when(openShiftClient.getMasterUrl()).thenReturn(new URL(MASTER_URL));
        List<Endpoint> endpoints = sdKubernetes.listServices();

        Assert.assertEquals(endpoints.size(), 1);
    }

    /**
     *  ServiceName  Namespace   Criteria  Type          Ports  LoadBalancer  ExternalIP
     *
     *  service0     dev         app=web   ClusterIP     http       -         included
     *  service1     dev         -         ExternalName  http       -             -
     *  service2     dev         -         LoadBalancer  https      IP            -
     *  service3     prod        app=web   ClusterIP     http       -             -
     *  service4     prod        -         LoadBalancer  http     hostname        -
     */
    @BeforeTest(description = "Create a list of services with all combinations included")
    void init() {
        Map<String, String> oneLabel = createOneLabelHashMap();

        ServicePortBuilder httpPortBuilder = new ServicePortBuilder().withName("http").withPort(80);
        ServicePort httpPort = httpPortBuilder.build();
        ServicePort nodePort2 = new ServicePortBuilder().withName("https").withPort(443).withNodePort(30002).build();
        ServicePort nodePort4 = httpPortBuilder.withNodePort(30004).build();

        List<LoadBalancerIngress> ipIngresses = new ArrayList<>();
        LoadBalancerIngress ingressWithIP = new LoadBalancerIngressBuilder().withIp("100.1.1.2").build();
        ipIngresses.add(ingressWithIP);

        List<LoadBalancerIngress> hostnameIngresses = new ArrayList<>();
        LoadBalancerIngress ingressWithHostname = new LoadBalancerIngressBuilder().withHostname("abc.com").build();
        hostnameIngresses.add(ingressWithHostname);

        Service service0 = new ServiceBuilder().withNewMetadata()
                .withName("service0").withNamespace("dev").withLabels(oneLabel).and()
                .withNewSpec()
                .withType("ClusterIP").withClusterIP("1.1.1.0")
                .withPorts(httpPort).withExternalIPs("100.2.1.0").and()
                .build();

        Service service1 = new ServiceBuilder().withNewMetadata()
                .withName("service1").withNamespace("dev").and()
                .withNewSpec()
                .withType("ExternalName").withExternalName("aaa.com").withPorts(httpPort).and()
                .build();

        Service service2 = new ServiceBuilder().withNewMetadata()
                .withName("service2").withNamespace("dev").and()
                .withNewSpec()
                .withType("LoadBalancer").withClusterIP("1.1.1.2").withPorts(nodePort2).and()
                .withNewStatus().withNewLoadBalancer().withIngress(ipIngresses).endLoadBalancer().and()
                .build();

        Service service3 = new ServiceBuilder().withNewMetadata()
                .withName("service3").withNamespace("prod").withLabels(oneLabel).and()
                .withNewSpec()
                .withType("ClusterIP").withClusterIP("1.1.1.3").withPorts(httpPort).and()
                .build();

        Service service4 = new ServiceBuilder().withNewMetadata()
                .withName("service4").withNamespace("prod").and()
                .withNewSpec()
                .withType("LoadBalancer").withClusterIP("1.1.1.4").withPorts(nodePort4).and()
                .withNewStatus().withNewLoadBalancer().withIngress(hostnameIngresses).endLoadBalancer().and()
                .build();

        List<Service> servicesList = new ArrayList<>();
        servicesList.add(service0);
        servicesList.add(service1);
        servicesList.add(service2);
        servicesList.add(service3);
        servicesList.add(service4);
        this.listOfServices = servicesList;
    }

    /**
     * Creates an implParameters map which contains token locations
     *
     * @param externalTokenFileName external token file name to be added
     * @return  implParameters map
     */
    private Map<String, String> createImplParametersMap(String externalTokenFileName) {
        Map<String, String> implParameters = new HashMap<>();
        implParameters.put(ServiceDiscovererKubernetes.EXTERNAL_SA_TOKEN_FILE_NAME, externalTokenFileName);
        implParameters.put(ServiceDiscovererKubernetes.POD_MOUNTED_SA_TOKEN_FILE_PATH,
                "/var/run/secrets/kubernetes.io/serviceaccount/token");
        return implParameters;
    }

    /**
     * Creates a Map which contains only one label
     *
     * @return Map with one label
     */
    private Map<String, String> createOneLabelHashMap() {
        Map<String, String> oneLabel = new HashMap<>();
        oneLabel.put("app", "web");
        return oneLabel;
    }

    /**
     * Creates a ServiceList containing all services in #listOfServices
     *
     * @return list of all the services in #listOfServices
     */
    private ServiceList createServiceList() {
        return new ServiceListBuilder().withItems(listOfServices).build();
    }

    /**
     * Creates a ServiceList where namespace of each service is "dev"
     *
     * @return list of services, where their namespace is "dev"
     */
    private ServiceList createServiceListWithNamespace() {
        List<Service> servicesList = new ArrayList<>();
        servicesList.add(listOfServices.get(0));
        servicesList.add(listOfServices.get(1));
        servicesList.add(listOfServices.get(2));
        return new ServiceListBuilder().withItems(servicesList).build();
    }

    /**
     * Creates a ServiceList where each service include the label "app=web"
     *
     * @return list of services, where one of the criteria/labels is "app=web"
     */
    private ServiceList createServiceListWithCriteria() {
        List<Service> servicesList = new ArrayList<>();
        servicesList.add(listOfServices.get(0));
        servicesList.add(listOfServices.get(3));
        return new ServiceListBuilder().withItems(servicesList).build();
    }

    /**
     * Creates a ServiceList of namespace "dev", criteria "app=web"
     *
     * @return list of services, of which namespace is "dev", criteria is "app=web"
     */
    private ServiceList createServiceListWithBothNamespaceAndCriteria() {
        List<Service> servicesList = new ArrayList<>();
        servicesList.add(listOfServices.get(0));
        return new ServiceListBuilder().withItems(servicesList).build();
    }

    /**
     * Create ServiceList with the given port type, but without a loadBalancer ingress
     *
     * @param portType http or https or a wrong port to check behavior
     * @return ServiceList containing one service of LoadBalancer type
     */
    private ServiceList createMalformedServiceList(String portType) {
        ServicePort port = new ServicePortBuilder().withName(portType).withPort(80).withNodePort(30005).build();

        Service malformedService5 = new ServiceBuilder().withNewMetadata()
                .withName("service5").withNamespace("prod").and()
                .withNewSpec()
                .withType("LoadBalancer").withClusterIP("1.1.1.5").withPorts(port).and()
                .withNewStatus().withNewLoadBalancer().endLoadBalancer().and()
                .build();

        List<Service> servicesList = new ArrayList<>();
        servicesList.add(malformedService5);
        return new ServiceListBuilder().withItems(servicesList).build();
    }
}
