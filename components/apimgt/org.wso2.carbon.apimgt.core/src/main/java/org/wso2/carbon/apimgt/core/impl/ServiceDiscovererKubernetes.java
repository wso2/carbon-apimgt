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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ServiceDiscoveryConstants;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kubernetes and OpenShift implementation of Service Discoverer
 */
public class ServiceDiscovererKubernetes extends ServiceDiscoverer {

    private final Logger log  = LoggerFactory.getLogger(ServiceDiscovererKubernetes.class);

    public static final String MASTER_URL = "masterUrl";
    public static final String INCLUDE_CLUSTER_IPS = "includeClusterIPs";
    public static final String INCLUDE_EXTERNAL_NAME_SERVICES = "includeExternalNameServices";
    public static final String POD_MOUNTED_SA_TOKEN_FILE_PATH = "podMountedSATokenFilePath";
    public static final String EXTERNAL_SA_TOKEN_FILE_NAME = "externalSATokenFileName";
    public static final String CA_CERT_PATH = "CACertPath";

    private static final String CLUSTER_IP = "ClusterIP";
    private static final String NODE_PORT = "NodePort";
    private static final String EXTERNAL_NAME = "ExternalName";
    private static final String LOAD_BALANCER = "LoadBalancer";
    private static final String EXTERNAL_IP = "ExternalIP";


    private OpenShiftClient client;
    private HashMap<String, String> implConfig;
    private Boolean includeClusterIPs;
    private Boolean includeExternalNameServices;
    private Boolean endpointsAvailable; //when false, will not look for NodePort urls for the remaining ports.


    /**
     * Initializes OpenShiftClient (extended KubernetesClient) and the necessary parameters
     *
     * @param implementationParameters container management specific parameters provided in the configuration
     * @throws ServiceDiscoveryException if an error occurs while initializing the client
     */
    @Override
    public void init(HashMap<String, String> implementationParameters) throws ServiceDiscoveryException {
        super.init(implementationParameters);
        this.implConfig = implementationParameters;
        includeClusterIPs = Boolean.parseBoolean(implConfig.get(INCLUDE_CLUSTER_IPS));
        includeExternalNameServices = Boolean.parseBoolean(implConfig.get(INCLUDE_EXTERNAL_NAME_SERVICES));
        try {
            this.client = new DefaultOpenShiftClient(buildConfig());
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

    /**
     * Builds the Config required by DefaultOpenShiftClient
     * Also sets the system properties
     *  (1) to not refer .kube/config file and
     *  (2) the client to use service account procedure to get authenticated and authorised
     *
     * @return {@link io.fabric8.kubernetes.client.Config} object to build the client
     * @throws ServiceDiscoveryException if an error occurs while building the config using externally stored token
     */
    private Config buildConfig() throws ServiceDiscoveryException {
        System.setProperty("kubernetes.auth.tryKubeConfig", "false");
        System.setProperty("kubernetes.auth.tryServiceAccount", "true");
        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(implConfig.get(MASTER_URL))
                .withCaCertFile(implConfig.get(CA_CERT_PATH));
        Config config;
        log.debug("Using mounted service account token");
        try {
            String saMountedToken = new String(Files.readAllBytes(
                    Paths.get(implConfig.get(POD_MOUNTED_SA_TOKEN_FILE_PATH))), StandardCharsets.UTF_8);
            config = configBuilder.withOauthToken(saMountedToken).build();
        } catch (IOException | NullPointerException e) {
            log.error("Error while building config with pod mounted token");
            log.debug("Mounted token not found", e);
            log.info("Using externally stored service account token");
            config = configBuilder.withOauthToken(resolveToken()).build();
        }
        return config;
    }

    /**
     * Get the token after decrypting using FileEncryptionUtility
     *
     * @return service account token
     * @throws ServiceDiscoveryException if an error occurs while resolving the token
     */
    private String resolveToken() throws ServiceDiscoveryException {
        String token;
        try {
            token = FileEncryptionUtility.getInstance().readFromEncryptedFile(
                    System.getProperty("carbon.home") + FileEncryptionUtility.SECURITY_DIR + File.separator
                    + "encrypted" + implConfig.get(EXTERNAL_SA_TOKEN_FILE_NAME));
        } catch (APIManagementException e) {
            String msg = "Error occurred while resolving externally stored token";
            log.error(msg, e);
            throw new ServiceDiscoveryException(msg, e);
        }
        return token;
    }


    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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


    /**
     * For each service in {@code services} list, calls the methods to add endpoints of different types,
     * for each of service's ports
     *
     * @param services          filtered list of services
     * @param filterNamespace   namespace : if was filtered using namespace, else accepts null
     */
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
                if (protocol != null &&
                        (protocol.equals(APIMgtConstants.HTTP) || protocol.equals(APIMgtConstants.HTTPS))) {
                    int port = servicePort.getPort();
                    String namespace = service.getMetadata().getNamespace();
                    if (includeClusterIPs) {
                        addClusterIPEndpoint(serviceSpec, serviceName, port, protocol, namespace, labels);
                    }
                    if (includeExternalNameServices && serviceSpec.getType().equals(EXTERNAL_NAME)) {
                        addExternalNameEndpoint(serviceSpec, serviceName, protocol, namespace, labels);
                    }
                    if (!serviceSpec.getType().equals(CLUSTER_IP) && endpointsAvailable) {
                        addNodePortEndpoint(serviceName, servicePort, protocol, filterNamespace, namespace, labels);
                    }
                    if (service.getSpec().getType().equals(LOAD_BALANCER)) {
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
            Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol, CLUSTER_IP, url, labels);
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
            Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol, EXTERNAL_NAME, url, labels);
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
                    Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol, NODE_PORT, url, labels);
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
                            LOAD_BALANCER, url, labels);
                    if (endpoint != null) {
                        this.servicesList.add(endpoint);
                        return;
                    }
                } catch (MalformedURLException e) {
                    log.error("Service:{} Namespace:{} URLType:LoadBalancer   URL malformed", serviceName, namespace);
                }
            }
        } else {
            log.debug("Service:{}  Namespace:{}  Port:{}/{} has no loadBalancer ingresses available.",
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
                            protocol, EXTERNAL_IP, url, labels);
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


    /**
     * Populates the necessary parameters required by the Endpoint object,
     * and call buildEndpoint method in the super class.
     *
     * @param serviceName  service name as defined in the cluster
     * @param namespace    service's namespace as defined in the cluster
     * @param portType     whether http or https
     * @param urlType      type of the service URL (eg. NodePort)
     * @param url          endpoint URL
     * @param labels       service's labels as defined in the cluster
     * @return {@link org.wso2.carbon.apimgt.core.models.Endpoint} object
     */
    private Endpoint constructEndpoint(String serviceName, String namespace, String portType,
                                                 String urlType, URL url, String labels) {
        //todo check if empty
        if (url == null) {
            return null;
        }
        JSONObject endpointConfig = new JSONObject();
        endpointConfig.put("serviceUrl", url.toString());
        endpointConfig.put("urlType", urlType);
        endpointConfig.put(ServiceDiscoveryConstants.NAMESPACE, namespace);
        endpointConfig.put(ServiceDiscoveryConstants.CRITERIA, labels);

        String endpointIndex = String.format("kubernetes-%d", this.serviceEndpointIndex);
        return buildEndpoint(endpointIndex, serviceName, endpointConfig.toString(),
                1000L, portType, "{\"enabled\": false}", APIMgtConstants.GLOBAL_ENDPOINT);
    }

    /**
     * Used by {@see #addNodePortEndpoint(String, ServicePort, String, String, String, String)} method,
     * since it is the (fabric8) Endpoints object that has the given service's pod list
     *
     * @param filterNamespace  namespace : if filtering was expected, else accepts null
     * @param serviceName      service name as defined in the cluster
     * @return {@link io.fabric8.kubernetes.api.model.Endpoints} object
     */
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

    /**
     * Used by {@see #addNodePortEndpoint(String, ServicePort, String, String, String, String)} method,
     * in order to find the node URL which the pod resides
     *
     * @param filterNamespace  namespace : if filtering was expected, else accepts null
     * @param podName          name of one of the pods, of the service
     * @return {@link io.fabric8.kubernetes.api.model.Pod} object
     */
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
