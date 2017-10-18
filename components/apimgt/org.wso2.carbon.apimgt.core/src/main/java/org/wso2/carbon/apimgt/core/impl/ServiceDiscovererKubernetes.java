/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.impl;

import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.FileEncryptionUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kubernetes and OpenShift implementation of Service Discoverer
 */
public class ServiceDiscovererKubernetes extends ServiceDiscoverer {

    private final Logger log  = LoggerFactory.getLogger(ServiceDiscovererKubernetes.class);

    private OpenShiftClient client;
    private String caCertLocation;
    private String saTokenFileName;
    private Boolean includeClusterIPs;
    private Boolean includeExternalNameServices;
    private Boolean endpointsAvailable; //when false, will not look for NodePort urls for the remaining ports.


    @Override
    public void init(HashMap<String, String> cmsSpecificParameters) throws ServiceDiscoveryException {
        super.init(cmsSpecificParameters);
        saTokenFileName = this.cmsSpecificParameters.get("serviceAccountTokenFile");
        caCertLocation = this.cmsSpecificParameters.get("caCertLocation");
        includeClusterIPs = Boolean.parseBoolean(this.cmsSpecificParameters.get("includeClusterIPs"));
        includeExternalNameServices = Boolean.parseBoolean(this.cmsSpecificParameters
                .get("includeExternalNameServices"));
        try {
            this.client = new DefaultOpenShiftClient(buildConfig(this.cmsSpecificParameters.get("masterUrl")));
        } catch (KubernetesClientException e) {
            String msg = "Error occurred while creating Kubernetes client";
            log.error(msg, e);
            throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_INITIALIZING_SERVICE_DISCOVERY);
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = "Error occurred while reading filtering criteria from the configuration";
            log.error(msg, e);
            throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_INITIALIZING_SERVICE_DISCOVERY);
        }
    }

    private Config buildConfig(String masterUrl) throws ServiceDiscoveryException {
        System.setProperty("kubernetes.auth.tryKubeConfig", "false");
        System.setProperty("kubernetes.auth.tryServiceAccount", "true");
        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(masterUrl)
                .withCaCertFile(Paths.get(caCertLocation).toString());
        Config config;
        log.debug("Using mounted service account token");
        try {
            String saMountedToken = IOUtils.toString(ServiceDiscovererKubernetes.class
                    .getResourceAsStream("/var/run/secrets/kubernetes.io/serviceaccount/token"),
                    "UTF-8");
            config = configBuilder.withOauthToken(saMountedToken).build();
        } catch (IOException | NullPointerException e) {
            log.info("Token not found in /var/run/secrets/kubernetes.io/serviceaccount/token.");
            log.info("Using externally stored service account token");
            config = configBuilder.withOauthToken(resolveToken()).build();
        }
        return config;
    }

    private String resolveToken() throws ServiceDiscoveryException {
        String token;
        String encryptedFilesDir = ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getFileEncryptionConfigurations().getDestinationDirectory();
        try {
            token = FileEncryptionUtil.readFromEncryptedFile(
                    Paths.get(encryptedFilesDir + "/encrypted" + saTokenFileName));
        } catch (APIManagementException e) {
            String msg = "Error occurred while resolving externally stored token";
            log.error(msg, e);
            throw new ServiceDiscoveryException(msg, e);
        }
        return token;
    }


    @Override
    public List<Endpoint> listServices() throws ServiceDiscoveryException {
        if (client != null) {
            log.debug("Looking for services in all namespaces");
            try {
                ServiceList services = client.services().inAnyNamespace().list();
                addServiceEndpointsToList(services, null);
            } catch (KubernetesClientException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return servicesList;
    }

    @Override
    public List<Endpoint> listServices(String namespace) throws ServiceDiscoveryException {
        if (client != null) {
            log.debug("Looking for services in namespace {}", namespace);
            try {
                ServiceList services = client.services().inNamespace(namespace).list();
                addServiceEndpointsToList(services, namespace);
            } catch (KubernetesClientException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return servicesList;
    }

    @Override
    public List<Endpoint> listServices(String namespace, HashMap<String, String> criteria)
            throws ServiceDiscoveryException {
        if (client != null) {
            log.debug("Looking for services, with the specified labels, in namespace {}", namespace);
            try {
                ServiceList services = client.services().inNamespace(namespace).withLabels(criteria).list();
                addServiceEndpointsToList(services, namespace);
            } catch (KubernetesClientException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            } catch (NoSuchMethodError e) {
                String msg = "Filtering criteria in the deployment yaml includes unwanted characters";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return servicesList;
    }

    @Override
    public List<Endpoint> listServices(HashMap<String, String> criteria) throws ServiceDiscoveryException {
        if (client != null) {
            log.debug("Looking for services, with the specified labels, in all namespaces");
            try {
                //namespace has to be set to null to check all allowed namespaces
                ServiceList services = client.services().inNamespace(null).withLabels(criteria).list();
                addServiceEndpointsToList(services, null);
            } catch (KubernetesClientException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            } catch (NoSuchMethodError e) {
                String msg = "Filtering criteria in the deployment yaml includes unwanted characters";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return servicesList;
    }


    private void addServiceEndpointsToList(ServiceList services, String filterNamespace) {
        List<Service> serviceItems = services.getItems();
        for (Service service : serviceItems) {
            String serviceName = service.getMetadata().getName();
            Map<String, String> labelsMap = service.getMetadata().getLabels();
            String labels = (labelsMap != null) ? labelsMap.toString() : "";
            ServiceSpec serviceSpec = service.getSpec();
            endpointsAvailable = true;
            for (ServicePort servicePort : serviceSpec.getPorts()) {
                String protocol = servicePort.getName();
                if (protocol != null && (protocol.equals("http") || protocol.equals("https"))) {
                    int port = servicePort.getPort();
                    String namespace = service.getMetadata().getNamespace();
                    if (includeClusterIPs) {
                        addClusterIPEndpoint(serviceSpec, serviceName, port, protocol, namespace, labels);
                    }
                    if (includeExternalNameServices && serviceSpec.getType().equals("ExternalName")) {
                        addExternalNameEndpoint(serviceSpec, serviceName, protocol, namespace, labels);
                    }
                    if (!serviceSpec.getType().equals("ClusterIP") && endpointsAvailable) {
                        addNodePortEndpoint(serviceName, servicePort, protocol, filterNamespace, namespace, labels);
                    }
                    if (service.getSpec().getType().equals("LoadBalancer")) {
                        addLoadBalancerEndpoint(service, serviceName, port, protocol, namespace, labels);
                    }
                    addExternalIPEndpoint(serviceSpec, serviceName, port, protocol, namespace, labels);
                } else if (log.isDebugEnabled()) {
                    log.debug("Service:{} Namespace:{} Port:{}/{}  Application level protocol not defined.",
                            serviceName, service.getMetadata().getNamespace(), servicePort.getPort(), protocol);
                }
            }
        }
    }

    private void addClusterIPEndpoint(ServiceSpec serviceSpec, String serviceName, int port,
                                      String protocol, String namespace, String labels) {
        try {
            URL url = new URL(protocol, serviceSpec.getClusterIP(), port, "");
            Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol, "ClusterIP", url, labels);
            if (endpoint != null) {
                this.servicesList.add(endpoint);
            }
        } catch (MalformedURLException e) {
            log.error("Service:{} Namespace:{} URLType:ClusterIP   URL malformed",
                    serviceName, namespace);
        }
    }

    private void addExternalNameEndpoint(ServiceSpec serviceSpec, String serviceName, String protocol,
                                         String namespace, String labels) {
        String externalName = (String) serviceSpec.getAdditionalProperties().get("externalName");
        try {
            URL url = new URL(protocol + "://" + externalName);
            Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol, "ExternalName", url, labels);
            if (endpoint != null) {
                this.servicesList.add(endpoint);
            }
        } catch (MalformedURLException e) {
            log.error("Service:{} Namespace:{} URLType:ExternalName   URL malformed", serviceName, namespace);
        }
    }

    private void addNodePortEndpoint(String serviceName, ServicePort servicePort, String protocol,
                                     String filterNamespace, String namespace, String labels) {
        //Node URL is found by getting the pod's IP
        //Pod is found via kubernetes endpoints
        Endpoints kubernetesEndpoint = findEndpoint(filterNamespace, serviceName);
        List<EndpointSubset> endpointSubsets = kubernetesEndpoint.getSubsets();
        if (endpointSubsets == null) {
            //no endpoints for the service : when LoadBalancer type or pods not selected
            log.debug("Service:{}   No endpoints found for the service.", serviceName);
            endpointsAvailable = false;
            return;
        }
        for (EndpointSubset endpointSubset : endpointSubsets) {
            List<EndpointAddress> endpointAddresses = endpointSubset.getAddresses();
            if (endpointAddresses.isEmpty()) {  //no endpoints for the service : when NodePort type
                log.debug("Service:{}   No endpoints found for the service.", serviceName);
                endpointsAvailable = false;
                return;
            }
            for (EndpointAddress endpointAddress : endpointAddresses) {
                String podName = endpointAddress.getTargetRef().getName();
                Pod pod = findPod(filterNamespace, podName);
                try {
                    URL url = new URL(protocol, pod.getStatus().getHostIP(), servicePort.getNodePort(), "");
                    Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol, "NodePort", url, labels);
                    if (endpoint != null) {
                        this.servicesList.add(endpoint);
                    }
                    return;
                } catch (NullPointerException e) { //no pods available for this address
                    log.debug("Service:{}  Pod {}  not available", serviceName, podName);
                } catch (MalformedURLException e) {
                    log.error("Service:{} Namespace:{} URLType:NodePort   URL malformed", serviceName, namespace);
                }
            }
        }
    }

    private void addLoadBalancerEndpoint(Service service, String serviceName, int port, String protocol,
                                         String namespace, String labels) {
        List<LoadBalancerIngress> loadBalancerIngresses = service.getStatus()
                .getLoadBalancer().getIngress();
        if (!loadBalancerIngresses.isEmpty()) {
            for (LoadBalancerIngress loadBalancerIngress : loadBalancerIngresses) {
                try {
                    URL url = new URL(protocol, loadBalancerIngress.getIp(), port, "");
                    Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol,
                            "LoadBalancer", url, labels);
                    if (endpoint != null) {
                        this.servicesList.add(endpoint);
                        return;
                    }
                } catch (MalformedURLException e) {
                    log.error("Service:{} Namespace:{} URLType:LoadBalancer   URL malformed", serviceName, namespace);
                }
            }
        } else {
            log.debug("Service:{}  Namespace:{}  Port:{}/{} has no loadbalancer ingresses available.",
                    serviceName, namespace, port, protocol);
        }
    }

    private void addExternalIPEndpoint(ServiceSpec serviceSpec, String serviceName, int port, String protocol,
                                       String namespace, String labels) {
        List<String> specialExternalIPs = serviceSpec.getExternalIPs();
        if (!specialExternalIPs.isEmpty()) {
            for (String specialExternalIP : specialExternalIPs) {
                try {
                    URL url = new URL(protocol, specialExternalIP, port, "");
                    Endpoint externalIpEndpoint = constructEndpoint(serviceName, namespace,
                            protocol, "ExternalIP", url, labels);
                    if (externalIpEndpoint != null) {
                        this.servicesList.add(externalIpEndpoint);
                        return;
                    }
                } catch (MalformedURLException e) {
                    log.error("Service:{} Namespace:{} URLType:ExternalIP   URL malformed", serviceName, namespace);
                }
            }
        }
    }

    private Endpoint constructEndpoint(String serviceName, String namespace, String portType,
                                                 String urlType, URL url, String labels) {
        //todo check if empty
        if (url == null) {
            return null;
        }
        String endpointConfig = String.format("{\"serviceUrl\": \"%s\"," +
                                                " \"urlType\": \"%s\"," +
                                                " \"namespace\": \"%s\"," +
                                                " \"criteria\": \"%s\"}",
                                                url.toString(), urlType, namespace, labels);
        String endpointIndex = String.format("ds-%d", serviceEndpointIndex);
        return createEndpoint(endpointIndex, serviceName, endpointConfig,
                1000L, portType, "{\"enabled\": false}", APIMgtConstants.GLOBAL_ENDPOINT);
    }

    private Endpoints findEndpoint(String filterNamespace, String serviceName) {
        Endpoints endpoint;
        if (filterNamespace == null) {
            /*
            In the line below, method ".inAnyNamespace()" did not support the extension ".withName()"
            like .inNamespace() does. Therefore ".withField()" is used.
            It returns a single item list which has the only endpoint created for the service.
            */
            endpoint = client.endpoints().inAnyNamespace()
                    .withField("metadata.name", serviceName).list().getItems().get(0);
        } else {
            endpoint = client.endpoints().inNamespace(filterNamespace).withName(serviceName).get();
        }
        return endpoint;
    }

    private Pod findPod(String filterNamespace, String podName) {
        Pod pod;
        if (filterNamespace == null) {
            //same reason as in findEndpoint method
            pod = client.pods().inAnyNamespace()
                    .withField("metadata.name", podName).list().getItems().get(0);
        } else {
            pod = client.pods().inNamespace(filterNamespace).withName(podName).get();
        }
        return pod;
    }

}
