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
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kubernetes and OpenShift implementation of Service Discoverer
 */
public class ServiceDiscovererKubernetes extends ServiceDiscoverer {
    private final Logger log = LoggerFactory.getLogger(ServiceDiscovererKubernetes.class);

    //Constants that are also used in the configuration as keys of key-value pairs
    public static final String MASTER_URL = "masterUrl";
    public static final String CA_CERT_PATH = "CACertPath";
    public static final String INCLUDE_CLUSTER_IPS = "includeClusterIPs";
    public static final String INCLUDE_EXTERNAL_NAME_SERVICES = "includeExternalNameServices";
    public static final String EXTERNAL_SA_TOKEN_FILE_NAME = "externalSATokenFileName";
    public static final String POD_MOUNTED_SA_TOKEN_FILE_PATH = "podMountedSATokenFilePath";

    private static final String CLUSTER_IP = "ClusterIP";
    private static final String NODE_PORT = "NodePort";
    private static final String EXTERNAL_NAME = "ExternalName";
    private static final String LOAD_BALANCER = "LoadBalancer";
    private static final String EXTERNAL_IP = "ExternalIP";
    private static final String TRY_KUBE_CONFIG = "kubernetes.auth.tryKubeConfig";
    private static final String TRY_SERVICE_ACCOUNT = "kubernetes.auth.tryServiceAccount";

    private OpenShiftClient client;
    private Boolean includeClusterIP;
    private Boolean includeExternalNameTypeServices;

    /**
     * Initializes OpenShiftClient (extended KubernetesClient) and sets the necessary parameters
     *
     * @param implParameters implementation parameters added by the super class #initImpl(java.util.Map) method
     * @throws ServiceDiscoveryException if an error occurs while initializing the client
     */
    @Override
    public void initImpl(Map<String, String> implParameters) throws ServiceDiscoveryException {
        try {
            setClient(new DefaultOpenShiftClient(buildConfig(implParameters)));
        } catch (KubernetesClientException | APIMgtDAOException e) {
            String msg = "Error occurred while creating Kubernetes client";
            throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_INITIALIZING_SERVICE_DISCOVERY);
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = "Error occurred while reading filtering criteria from the configuration";
            throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_INITIALIZING_SERVICE_DISCOVERY);
        }
        includeClusterIP = Boolean.parseBoolean(implParameters.get(INCLUDE_CLUSTER_IPS));
        includeExternalNameTypeServices = Boolean.parseBoolean(implParameters.get(INCLUDE_EXTERNAL_NAME_SERVICES));
    }

    /**
     * Builds the Config required by DefaultOpenShiftClient
     * Also sets the system properties
     * (1) to not refer .kube/config file and
     * (2) the client to use service account procedure to get authenticated and authorised
     *
     * @return {@link io.fabric8.kubernetes.client.Config} object to build the client
     * @throws ServiceDiscoveryException if an error occurs while building the config using externally stored token
     */
    private Config buildConfig(Map<String, String> implParameters)
            throws ServiceDiscoveryException, APIMgtDAOException {

        System.setProperty(TRY_KUBE_CONFIG, "false");
        System.setProperty(TRY_SERVICE_ACCOUNT, "true");

        /*
         *  Common to both situations,
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
        if (StringUtils.isEmpty(externalSATokenFileName)) {
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
            String externalSATokenFilePath = System.getProperty(FileEncryptionUtility.CARBON_HOME)
                    + FileEncryptionUtility.SECURITY_DIR + File.separator + encryptedTokenFileName;
            token = FileEncryptionUtility.getInstance().readFromEncryptedFile(externalSATokenFilePath);
        } catch (APIManagementException e) {
            String msg = "Error occurred while resolving externally stored token";
            throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_INITIALIZING_SERVICE_DISCOVERY);
        }
        return StringUtils.replace(token, "\n", "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Endpoint> listServices() throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        if (client != null) {
            log.debug("Looking for services in all namespaces");
            try {
                List<Service> serviceList = client.services().inNamespace(null).list().getItems();
                addServicesToEndpointList(serviceList, endpointList);
            } catch (KubernetesClientException | MalformedURLException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return endpointList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Endpoint> listServices(String namespace) throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        if (client != null) {
            log.debug("Looking for services in namespace {}", namespace);
            try {
                List<Service> serviceList = client.services().inNamespace(namespace).list().getItems();
                addServicesToEndpointList(serviceList, endpointList);
            } catch (KubernetesClientException | MalformedURLException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return endpointList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Endpoint> listServices(String namespace, Map<String, String> criteria)
            throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        if (client != null) {
            log.debug("Looking for services, with the specified labels, in namespace {}", namespace);
            try {
                List<Service> serviceList = client.services().inNamespace(namespace).withLabels(criteria)
                        .list().getItems();
                addServicesToEndpointList(serviceList, endpointList);
            } catch (KubernetesClientException | MalformedURLException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            } catch (NoSuchMethodError e) {
                String msg = "Filtering criteria in the deployment yaml includes unwanted characters";
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return endpointList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Endpoint> listServices(Map<String, String> criteria) throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        if (client != null) {
            log.debug("Looking for services, with the specified labels, in all namespaces");
            try {
                //namespace has to be set to null to check all allowed namespaces
                List<Service> serviceList = client.services().inNamespace(null).withLabels(criteria)
                        .list().getItems();
                addServicesToEndpointList(serviceList, endpointList);
            } catch (KubernetesClientException | MalformedURLException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            } catch (NoSuchMethodError e) {
                String msg = "Filtering criteria in the deployment yaml includes unwanted characters";
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return endpointList;
    }

    /**
     * For each service in {@code serviceList} list, methods are called to add endpoints of different types,
     * for each of service's ports
     *
     * @param serviceList filtered list of services
     */
    private void addServicesToEndpointList(List<Service> serviceList, List<Endpoint> endpointList)
            throws MalformedURLException {
        for (Service service : serviceList) {
            //Set the parameters that does not change with the service port
            String serviceName = service.getMetadata().getName();
            String namespace = service.getMetadata().getNamespace();
            Map<String, String> labelsMap = service.getMetadata().getLabels();
            String labels = (labelsMap != null) ? labelsMap.toString() : "";
            ServiceSpec serviceSpec = service.getSpec();
            String serviceType = serviceSpec.getType();

            if (includeExternalNameTypeServices && EXTERNAL_NAME.equals(serviceType)) {
                // Since only a "ExternalName" type service can have an "externalName" (the alias in kube-dns)
                addExternalNameEndpoint(serviceName, serviceSpec.getExternalName(), namespace, labels, endpointList);
            }

            for (ServicePort servicePort : serviceSpec.getPorts()) {
                String protocol = servicePort.getName();
                if (APIMgtConstants.HTTP.equals(protocol) || APIMgtConstants.HTTPS.equals(protocol)) {
                    int port = servicePort.getPort();

                    if (includeClusterIP && !EXTERNAL_NAME.equals(serviceType)) {
                        // Since almost every service has a cluster IP, except for ExternalName type
                        addClusterIPEndpoint(serviceName, serviceSpec.getClusterIP(), port, protocol,
                                namespace, labels, endpointList);
                    }
                    if (NODE_PORT.equals(serviceType) || LOAD_BALANCER.equals(serviceType)) {
                        // Because both "NodePort" and "LoadBalancer" types of services have "NodePort" type URLs
                        addNodePortEndpoint(serviceName, servicePort.getNodePort(), protocol,
                                namespace, labels, endpointList);
                    }
                    if (LOAD_BALANCER.equals(serviceType)) {
                        // Since only "LoadBalancer" type services have "LoadBalancer" type URLs
                        addLoadBalancerEndpoint(serviceName, service, port, protocol, namespace, labels, endpointList);
                    }
                    // A Special case (can be any of the service types above)
                    addExternalIPEndpoint(serviceName, serviceSpec.getExternalIPs(), port, protocol,
                            namespace, labels, endpointList);
                } else if (log.isDebugEnabled()) {
                    log.debug("Service:{} Namespace:{} Port:{}/{}  Application level protocol not defined.",
                            serviceName, namespace, servicePort.getPort(), protocol);
                }
            }
        }
    }

    /**
     * Adds the ExternalName Endpoint to the endpointList
     *
     * @param serviceName  name of the service the endpoint belongs to
     * @param externalName externalName found in service's spec
     * @param namespace    namespace of the service
     * @param labels       labels of the service
     * @param endpointList endpointList which the endpoint has to be added to
     * @throws MalformedURLException if protocol unknown or URL spec is null, therefore will not get thrown
     */
    private void addExternalNameEndpoint(String serviceName, String externalName,
                                         String namespace, String labels, List<Endpoint> endpointList)
            throws MalformedURLException {
        URL url = new URL("http://" + externalName);
        endpointList.add(constructEndpoint(serviceName, namespace, "http", EXTERNAL_NAME, url, labels));
    }

    /**
     * Adds the ClusterIP Endpoint to the endpointList
     *
     * @param serviceName  name of the service the endpoint belongs to
     * @param clusterIP    ClusterIP of the service
     * @param port         port number
     * @param protocol     whether http or https
     * @param namespace    namespace of the service
     * @param labels       labels of the service
     * @param endpointList endpointList which the endpoint has to be added to
     * @throws MalformedURLException if protocol unknown, therefore will not get thrown
     */
    private void addClusterIPEndpoint(String serviceName, String clusterIP, int port, String protocol,
                                      String namespace, String labels, List<Endpoint> endpointList)
            throws MalformedURLException {
        URL url = new URL(protocol, clusterIP, port, "");
        endpointList.add(constructEndpoint(serviceName, namespace, protocol, CLUSTER_IP, url, labels));
    }

    /**
     * Adds the NodePort Endpoint to the endpointList
     *
     * @param serviceName  name of the service the endpoint belongs to
     * @param nodePort     nodePort of the service
     * @param protocol     whether http or https
     * @param namespace    namespace of the service
     * @param labels       labels of the service
     * @param endpointList endpointList which the endpoint has to be added to
     * @throws MalformedURLException if protocol unknown, therefore will not get thrown
     */
    private void addNodePortEndpoint(String serviceName, int nodePort, String protocol,
                                     String namespace, String labels, List<Endpoint> endpointList)
            throws MalformedURLException {
        String nodeIP = client.getMasterUrl().getHost();
        URL url = new URL(protocol, nodeIP, nodePort, "");
        endpointList.add(constructEndpoint(serviceName, namespace, protocol, NODE_PORT, url, labels));
    }

    /**
     * Adds the LoadBalancer Endpoint to the endpointList
     *
     * @param serviceName  name of the service the endpoint belongs to
     * @param service      service object instance
     * @param port         port number
     * @param protocol     whether http or https
     * @param namespace    namespace of the service
     * @param labels       labels of the service
     * @param endpointList endpointList which the endpoint has to be added to
     * @throws MalformedURLException if protocol unknown, therefore will not get thrown
     */
    private void addLoadBalancerEndpoint(String serviceName, Service service, int port, String protocol,
                                         String namespace, String labels, List<Endpoint> endpointList)
            throws MalformedURLException {
        List<LoadBalancerIngress> loadBalancerIngresses = service.getStatus().getLoadBalancer().getIngress();
        if (!loadBalancerIngresses.isEmpty()) {
            for (LoadBalancerIngress loadBalancerIngress : loadBalancerIngresses) {
                String hostname = loadBalancerIngress.getHostname();
                String host = (hostname == null || "".equals(hostname)) ? loadBalancerIngress.getIp() : hostname;
                URL url = new URL(protocol, host, port, "");
                endpointList.add(constructEndpoint(serviceName, namespace, protocol, LOAD_BALANCER, url, labels));
            }
        } else {
            log.debug("Service:{}  Namespace:{}  Port:{}/{} has no loadBalancer ingresses available.",
                    serviceName, namespace, port, protocol);
        }
    }

    /**
     * Adds the ExternalIP Endpoint to the endpointList
     *
     * @param serviceName  name of the service the endpoint belongs to
     * @param externalIPs  service object instance
     * @param port         port number
     * @param protocol     whether http or https
     * @param namespace    namespace of the service
     * @param labels       labels of the service
     * @param endpointList endpointList which the endpoint has to be added to
     * @throws MalformedURLException if protocol unknown, therefore will not get thrown
     */
    private void addExternalIPEndpoint(String serviceName, List<String> externalIPs, int port, String protocol,
                                       String namespace, String labels, List<Endpoint> endpointList)
            throws MalformedURLException {
        if (!externalIPs.isEmpty()) {
            for (String externalIP : externalIPs) {
                URL url = new URL(protocol, externalIP, port, "");
                endpointList.add(constructEndpoint(serviceName, namespace, protocol, EXTERNAL_IP, url, labels));
            }
        }
    }

    /**
     * Populates the necessary parameters required by the Endpoint object,
     * and call buildEndpoint method in the super class.
     *
     * @param serviceName service name as defined in the cluster
     * @param namespace   service's namespace as defined in the cluster
     * @param portType    whether http or https
     * @param urlType     type of the service URL (eg. NodePort)
     * @param url         endpoint URL
     * @param labels      service's labels as defined in the cluster
     * @return {@link org.wso2.carbon.apimgt.core.models.Endpoint} object
     */
    private Endpoint constructEndpoint(String serviceName, String namespace, String portType,
                                       String urlType, URL url, String labels) {
        JSONObject endpointConfig = new JSONObject();
        endpointConfig.put("serviceUrl", url.toString());
        endpointConfig.put("urlType", urlType);
        endpointConfig.put(ServiceDiscoveryConstants.NAMESPACE, namespace);
        endpointConfig.put(ServiceDiscoveryConstants.CRITERIA, labels);

        String endpointIndex = String.format("kubernetes-%d", this.serviceEndpointIndex);

        return buildEndpoint(endpointIndex, serviceName, endpointConfig.toString(),
                1000L, portType, "{\"enabled\": false}", APIMgtConstants.GLOBAL_ENDPOINT);
    }

    void setClient(OpenShiftClient openShiftClient) {
        this.client = openShiftClient;
    }

    void setIncludeClusterIP(Boolean includeClusterIP) {
        this.includeClusterIP = includeClusterIP;
    }

    void setIncludeExternalNameTypeServices(Boolean includeExternalNameTypeServices) {
        this.includeExternalNameTypeServices = includeExternalNameTypeServices;
    }
}
