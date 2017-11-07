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

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServiceListBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.OpenShiftClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;


import java.util.HashMap;


public class ServiceDiscoveryKubernetesTestCase {

    @Test
    public void testExternalTokenFileNameNotGiven() throws Exception {
        ServiceDiscoveryClientFactory sdClientFactory = Mockito.mock(ServiceDiscoveryClientFactory.class);
        Config config = Mockito.mock(Config.class);
        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class);

        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();

        Mockito.when(sdClientFactory.createDefaultOpenShiftClient(config)).thenReturn(openShiftClient);
        try {
            sdKubernetes.init(sdClientFactory, createServiceDiscoveryK8ImplParametersMap());
        } catch (ServiceDiscoveryException e) {
            Assert.assertEquals(e.getCause().getMessage(),
                    "Error while reading file /var/run/secrets/kubernetes.io/serviceaccount/token");
        }
    }

    @Test
    public void testListServicesWhenClientIsNull() throws Exception {
        ServiceDiscovererKubernetes sdKubernetes = new ServiceDiscovererKubernetes();
        String namespace = "dev";
        HashMap<String, String> criteria = new HashMap<>();

        //list method called without init method
        Assert.assertEquals(true, sdKubernetes.listServices().isEmpty());
        Assert.assertEquals(true, sdKubernetes.listServices(namespace).isEmpty());
        Assert.assertEquals(true, sdKubernetes.listServices(criteria).isEmpty());
        Assert.assertEquals(true, sdKubernetes.listServices(namespace, criteria).isEmpty());
    }

    @Test
    public void testListServicesWhenBothNamespaceCriteriaNotGiven() throws Exception{
        KubernetesList kubernetesList = new KubernetesListBuilder();
    }





    private HashMap<String, String> createServiceDiscoveryK8ImplParametersMap() {
        HashMap<String, String> implParameters = new HashMap<>();
        implParameters.put(ServiceDiscovererKubernetes.MASTER_URL, "");
        implParameters.put(ServiceDiscovererKubernetes.CA_CERT_PATH,
                "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt");
        implParameters.put(ServiceDiscovererKubernetes.INCLUDE_CLUSTER_IPS, "true");
        implParameters.put(ServiceDiscovererKubernetes.INCLUDE_EXTERNAL_NAME_SERVICES, "true");
        implParameters.put(ServiceDiscovererKubernetes.EXTERNAL_SA_TOKEN_FILE_NAME, "");
        implParameters.put(ServiceDiscovererKubernetes.POD_MOUNTED_SA_TOKEN_FILE_PATH,
                "/var/run/secrets/kubernetes.io/serviceaccount/token");
        return implParameters;
    }

    private ServiceList createServiceList() {
        HashMap<String, String> oneLabel = new HashMap<>();
        oneLabel.put("app", "web");

        HashMap<String, String> twoLabels = new HashMap<>();
        twoLabels.put("app", "web");
        twoLabels.put("db", "mysql");

        ServicePortBuilder httpPortBuilder = new ServicePortBuilder().withName("http").withPort(80);
        ServicePortBuilder httpsPortBuilder = new ServicePortBuilder().withName("https").withPort(443);
        ServicePort httpPort = httpPortBuilder.build();
        ServicePort httpsPort = httpsPortBuilder.build();
        ServicePort nodePort2 = httpsPortBuilder.withNodePort(30002).build();
        ServicePort nodePort4 = httpsPortBuilder.withNodePort(30004).build();
        ServicePort nodePort6 = httpsPortBuilder.withNodePort(30006).build();
        ServicePort nodePort8 = httpPortBuilder.withNodePort(30008).build();


        Service service1 = new ServiceBuilder().withNewMetadata()
                .withName("service1").withNamespace("dev").withLabels(oneLabel).and()
                .withNewSpec()
                .withType("ClusterIP").withClusterIP("1.1.1.1").withPorts(httpPort).and()
                .build();

        Service service2 = new ServiceBuilder().withNewMetadata()
                .withName("service2").withNamespace("dev").withLabels(twoLabels).and()
                .withNewSpec()
                .withType("NodePort").withClusterIP("1.1.1.2").withPorts(nodePort2).and()
                .build();

        Service service3 = new ServiceBuilder().withNewMetadata()
                .withName("service3").withNamespace("dev").and()
                .withNewSpec()
                .withType("ExternalName").withClusterIP("1.1.1.1").withPorts(httpPort).and()
                .build();

        ServiceList serviceList = new ServiceList();
        return serviceList;
    }
}
