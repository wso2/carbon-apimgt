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

import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.Service;
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
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ServiceDiscoveryConstants;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kubernetes and OpenShift implementation of Service Discoverer
 */
public class ServiceDiscovererKubernetes extends ServiceDiscoverer {

    private final Logger log  = LoggerFactory.getLogger(ServiceDiscovererKubernetes.class);

    //Constants that are also used in the configuration as keys of key-value pairs
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
    private HashMap<String, String> implParameters;

    private Boolean includeClusterIP;
    private Boolean includeExternalNameTypeServices;


    /**
     * Initializes OpenShiftClient (extended KubernetesClient) and sets the necessary parameters
     *
     * @param implementationParameters implementation parameters provided in the configuration
     * @throws ServiceDiscoveryException if an error occurs while initializing the client
     */
    @Override
    public void init(HashMap<String, String> implementationParameters) throws ServiceDiscoveryException {
        super.init(implementationParameters);
        implParameters = implementationParameters;
        includeClusterIP = Boolean.parseBoolean(implParameters.get(INCLUDE_CLUSTER_IPS));
        includeExternalNameTypeServices = Boolean.parseBoolean(implParameters.get(INCLUDE_EXTERNAL_NAME_SERVICES));
        try {
            client = new DefaultOpenShiftClient(buildConfig());
        } catch (KubernetesClientException | APIMgtDAOException e) {
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
    private Config buildConfig() throws ServiceDiscoveryException, APIMgtDAOException {
        System.setProperty("kubernetes.auth.tryKubeConfig", "false");
        System.setProperty("kubernetes.auth.tryServiceAccount", "true");

        /*
         *  Common to both situations
         *      - Token found inside APIM pod
         *      - Token stored in APIM resources/security folder }
         */
        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(implParameters.get(MASTER_URL))
                .withCaCertFile(implParameters.get(CA_CERT_PATH));

        /*
         *  Check if a service account token File Name is given in the configuration
         *      - if not : assume APIM is running inside a pod and look for the pod's token
         */
        String externalSATokenFileName = implParameters.get(EXTERNAL_SA_TOKEN_FILE_NAME);
        if ("".equals(externalSATokenFileName)) {
            log.debug("Looking for service account token in " + POD_MOUNTED_SA_TOKEN_FILE_PATH);
            String podMountedSAToken = APIFileUtils.readFileContentAsText(
                    implParameters.get(POD_MOUNTED_SA_TOKEN_FILE_PATH));
            return configBuilder.withOauthToken(podMountedSAToken).build();
        } else {
            log.info("Using externally stored service account token");
            return configBuilder.withOauthToken(resolveToken("encrypted" + externalSATokenFileName)).build();
        }
    }

    /**
     * Get the token after decrypting using {@link FileEncryptionUtility#readFromEncryptedFile(java.lang.String)}
     *
     * @return service account token
     * @throws ServiceDiscoveryException if an error occurs while resolving the token
     */
    private String resolveToken(String encryptedTokenFileName) throws ServiceDiscoveryException {
        String token;
        try {
            String externalSATokenFilePath = System.getProperty("carbon.home") + FileEncryptionUtility.SECURITY_DIR
                    + File.separator + encryptedTokenFileName;
            token = FileEncryptionUtility.getInstance().readFromEncryptedFile(externalSATokenFilePath);
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
                List<Service> serviceList = client.services().inAnyNamespace().list().getItems();
                addServiceEndpointsToList(serviceList);
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
                List<Service> serviceList = client.services().inNamespace(namespace).list().getItems();
                addServiceEndpointsToList(serviceList);
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
                List<Service> serviceList = client.services().inNamespace(namespace).withLabels(criteria)
                        .list().getItems();
                addServiceEndpointsToList(serviceList);
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
                List<Service> serviceList = client.services().inNamespace(null).withLabels(criteria).list().getItems();
                addServiceEndpointsToList(serviceList);
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
     * For each service in {@code serviceList} list, methods are called to add endpoints of different types,
     * for each of service's ports
     *
     * @param serviceList   filtered list of services
     */
    private void addServiceEndpointsToList(List<Service> serviceList) {
        for (Service service : serviceList) {
            //Set the parameters that does not change with the service port
            String serviceName = service.getMetadata().getName();
            String namespace = service.getMetadata().getNamespace();
            Map<String, String> labelsMap = service.getMetadata().getLabels();
            String labels = (labelsMap != null) ? labelsMap.toString() : "";
            ServiceSpec serviceSpec = service.getSpec();
            String serviceType = serviceSpec.getType();

            for (ServicePort servicePort : serviceSpec.getPorts()) {
                String protocol = servicePort.getName();
                if (APIMgtConstants.HTTP.equals(protocol) || APIMgtConstants.HTTPS.equals(protocol)) {
                    int port = servicePort.getPort();

                    if (includeClusterIP) {
                        // Almost every service has a cluster IP. Hence, only "includeClusterIP" value is checked
                        addClusterIPEndpoint(serviceSpec, serviceName, port, protocol, namespace, labels);
                    }
                    if (includeExternalNameTypeServices && EXTERNAL_NAME.equals(serviceType)) {
                        // Since only a "ExternalName" type service can have an "externalName" (the alias in kube-dns)
                        addExternalNameEndpoint(serviceSpec, serviceName, protocol, namespace, labels);
                    }
                    if (NODE_PORT.equals(serviceType) || LOAD_BALANCER.equals(serviceType)) {
                        // Because both "NodePort" and "LoadBalancer" types of services have "NodePort" type URLs
                        addNodePortEndpoint(serviceName, servicePort, protocol, namespace, labels);
                    }
                    if (LOAD_BALANCER.equals(serviceType)) {
                        // Since only "LoadBalancer" type services have "LoadBalancer" type URLs
                        addLoadBalancerEndpoint(service, serviceName, port, protocol, namespace, labels);
                    }
                    // A Special case (can be any of the service types above)
                    addExternalIPEndpoint(serviceSpec, serviceName, port, protocol, namespace, labels);
                } else if (log.isDebugEnabled()) {
                    log.debug("Service:{} Namespace:{} Port:{}/{}  Application level protocol not defined.",
                            serviceName, namespace, servicePort.getPort(), protocol);
                }
            }
        }
    }

    private void addClusterIPEndpoint(ServiceSpec serviceSpec, String serviceName, int port,
                                      String protocol, String namespace, String labels) {
        String clusterIP = serviceSpec.getClusterIP();
        try {
            URL url = new URL(protocol, clusterIP, port, "");
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
                                     String namespace, String labels) {
        String nodeIP = this.client.getMasterUrl().getHost();
        try {
            URL url = new URL(protocol, nodeIP, servicePort.getNodePort(), "");
            Endpoint endpoint = constructEndpoint(serviceName, namespace, protocol, NODE_PORT, url, labels);
            if (endpoint != null) {
                this.servicesList.add(endpoint);
            }
        } catch (MalformedURLException e) {
            log.error("Service:{} Namespace:{} URLType:NodePort   URL malformed", serviceName, namespace);
        }
    }

    private void addLoadBalancerEndpoint(Service service, String serviceName, int port, String protocol,
                                         String namespace, String labels) {
        List<LoadBalancerIngress> loadBalancerIngresses = service.getStatus().getLoadBalancer().getIngress();
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
        List<String> externalIPs = serviceSpec.getExternalIPs();
        if (!externalIPs.isEmpty()) {
            for (String externalIP : externalIPs) {
                try {
                    URL url = new URL(protocol, externalIP, port, "");
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

}
