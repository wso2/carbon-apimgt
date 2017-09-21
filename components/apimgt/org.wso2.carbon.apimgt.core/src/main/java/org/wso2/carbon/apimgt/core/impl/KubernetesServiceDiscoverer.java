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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.ServiceDiscoverer;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfigurations;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kubernetes and Openshift implementation of Service Discoverer
 */
public class KubernetesServiceDiscoverer implements ServiceDiscoverer {

    private OpenShiftClient client;
    private ServiceDiscoveryConfigurations serviceDiscoveryConfigurations;
    private final Logger log  = LoggerFactory.getLogger(KubernetesServiceDiscoverer.class);


    private String serviceAccountToken;
    private String caCertLocation;
    private Boolean insidePod;

    private List<Endpoint> servicesList;
    private Boolean endpointsAvailable; //when false, will not look for NodePort urls for the remaining ports.
    private int kubeEndpointIndex;

    public static KubernetesServiceDiscoverer getInstance() throws ServiceDiscoveryException {
        return new KubernetesServiceDiscoverer();
    }

    @Override
    public Boolean isEnabled() {
        return serviceDiscoveryConfigurations.isServiceDiscoveryEnabled();
    }

    private KubernetesServiceDiscoverer() throws ServiceDiscoveryException {
        serviceDiscoveryConfigurations = ServiceReferenceHolder.getInstance()
                .getAPIMConfiguration().getServiceDiscoveryConfigurations();
        Map<String, String> security = serviceDiscoveryConfigurations.getSecurity();
        Map<String, String> cmsProperties = serviceDiscoveryConfigurations.getCmsSpecificParameters();
        serviceAccountToken = security.get("serviceAccountToken");
        caCertLocation = security.get("caCertLocation");
        insidePod = Boolean.parseBoolean(cmsProperties.get("insidePod"));
        try {
            this.client = new DefaultOpenShiftClient(buildConfig(serviceDiscoveryConfigurations.getMasterUrl()));
        } catch (KubernetesClientException e) {
            String msg = "Error occurred while creating Kubernetes client";
            throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_INITIALIZING_SERVICE_DISCOVERY);
        }
        servicesList = new ArrayList<>();
        kubeEndpointIndex = 0;
    }

    private Config buildConfig(String masterUrl) throws ServiceDiscoveryException {
        System.setProperty("kubernetes.auth.tryKubeConfig", "false");
        System.setProperty("kubernetes.auth.tryServiceAccount", "true");
        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(masterUrl)
                .withCaCertFile(caCertLocation);
        Config config;
        if (insidePod) {
            log.debug("Using mounted service account token");
            try {
                String saMountedToken = IOUtils.toString(KubernetesServiceDiscoverer.class
                        .getResourceAsStream("/var/run/secrets/kubernetes.io/serviceaccount/token"),
                        "UTF-8");
                config = configBuilder.withOauthToken(saMountedToken).build();
            } catch (IOException e) {
                String msg = "Token file not found";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e);
            }
        } else {
            log.debug("Using externally stored service account token");
            config = configBuilder.withOauthToken(serviceAccountToken).build();
        }
        return config;
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
        return this.servicesList;
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
        return this.servicesList;
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
            }
        }
        return this.servicesList;
    }

    @Override
    public List<Endpoint> listServices(HashMap<String, String> criteria) throws ServiceDiscoveryException {
        if (client != null) {
            log.debug("Looking for services, with the specified labels, in all namespaces");
            try {
                ServiceList services = client.services().withLabels(criteria).list();
                addServiceEndpointsToList(services, null);
            } catch (KubernetesClientException e) {
                String msg = "Error occurred while trying to list services using Kubernetes client";
                log.error(msg, e);
                throw new ServiceDiscoveryException(msg, e, ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES);
            }
        }
        return this.servicesList;
    }


    private void addServiceEndpointsToList(ServiceList services, String filterNamespace) {
        List<Service> serviceItems = services.getItems();
        for (Service service : serviceItems) {
            String serviceName = service.getMetadata().getName();
            ServiceSpec serviceSpec = service.getSpec();
            endpointsAvailable = true;
            for (ServicePort servicePort : serviceSpec.getPorts()) {
                String protocol = servicePort.getName();
                if (protocol != null && (protocol.equals("http") || protocol.equals("https"))) {
                    int port = servicePort.getPort();
                    String namespace = service.getMetadata().getNamespace();
                    if (insidePod) {
                        discoverClusterIPURL(serviceSpec, serviceName, port, protocol, namespace);

                        if (serviceSpec.getType().equals("ExternalName")) {
                            discoverExternalNameURL(service, serviceName, protocol, namespace);
                        }
                    }
                    if (!serviceSpec.getType().equals("ClusterIP") && endpointsAvailable) {
                        discoverNodePortURL(serviceName, servicePort, protocol, filterNamespace, namespace);
                    }
                    discoverLoadBalancerURL(service, serviceName, port, protocol, namespace);
                    discoverExternalIPURL(service, serviceName, port, protocol, namespace);

                } else if (log.isDebugEnabled()) {
                    log.debug("Service:{} Namespace:{} Port:{}/{}  Application level protocol not defined.",
                            serviceName, service.getMetadata().getNamespace(),
                            servicePort.getPort(), protocol);
                }
            }
        }
    }

    private void discoverClusterIPURL(ServiceSpec serviceSpec, String serviceName, int port,
                                      String protocol, String namespace) {
        URL clusterIPServiceURL = null;
        try {
            clusterIPServiceURL = new URL(protocol, serviceSpec.getClusterIP(), port, "");
        } catch (MalformedURLException e) {
            log.error("Service:{} Namespace:{} URLType:ClusterIP   URL malformed",
                    serviceName, namespace);
        }
        Endpoint clusterIPEndpoint = constructEndpoint(serviceName, namespace, protocol,
                "ClusterIP", clusterIPServiceURL);
        if (clusterIPEndpoint != null) {
            this.servicesList.add(clusterIPEndpoint);
        }
    }

    private void discoverExternalNameURL(Service service, String serviceName, String protocol,
                                         String namespace) {
        String externalName = (String) service.getSpec()
                .getAdditionalProperties().get("externalName");
        URL externalNameServiceURL = null;
        try {
            externalNameServiceURL = new URL(protocol + "://" + externalName);
        } catch (MalformedURLException e) {
            log.error("Service:{} Namespace:{} URLType:ExternalName   URL malformed",
                    serviceName, namespace);
        }
        Endpoint externalNameEndpoint = constructEndpoint(serviceName,
                namespace, protocol, "ExternalName", externalNameServiceURL);
        if (externalNameEndpoint != null) {
            this.servicesList.add(externalNameEndpoint);
        }
    }

    private void discoverNodePortURL(String serviceName, ServicePort servicePort, String protocol,
                                     String filterNamespace, String namespace) {
        URL nodePortServiceURL = null;
        try {
            nodePortServiceURL = findNodePortServiceURLForAPort(filterNamespace,
                    serviceName, protocol, servicePort.getNodePort());
        } catch (MalformedURLException e) {
            log.error("Service:{} Namespace:{} URLType:NodePort   URL malformed", serviceName, namespace);
        }
        Endpoint nodePortEndpoint = constructEndpoint(serviceName, namespace,
                protocol, "NodePort", nodePortServiceURL);
        if (nodePortEndpoint != null) {
            this.servicesList.add(nodePortEndpoint);
        }
    }

    private void discoverLoadBalancerURL(Service service, String serviceName, int port, String protocol,
                                         String namespace) {
        if (service.getSpec().getType().equals("LoadBalancer")) {
            List<LoadBalancerIngress> loadBalancerIngresses = service.getStatus()
                    .getLoadBalancer().getIngress();
            if (!loadBalancerIngresses.isEmpty()) {
                URL loadBalancerServiceURL = null;
                try {
                    loadBalancerServiceURL = new URL(protocol,
                            loadBalancerIngresses.get(0).getIp(), port, "");
                } catch (MalformedURLException e) {
                    log.error("Service:{} Namespace:{} URLType:LoadBalancer   URL malformed", serviceName, namespace);
                }
                Endpoint loadBalancerEndpoint = constructEndpoint(serviceName,
                        namespace, protocol, "LoadBalancer", loadBalancerServiceURL);
                if (loadBalancerEndpoint != null) {
                    this.servicesList.add(loadBalancerEndpoint);
                }
            } else {
                log.debug("Service:{}  Namespace:{}  Port:{}/{} " +
                                "has no loadbalancer ingresses available.",
                        serviceName, namespace, port, protocol);
            }
        }
    }

    private void discoverExternalIPURL(Service service, String serviceName, int port, String protocol,
                                       String namespace) {
        List<String> specialExternalIps = service.getSpec().getExternalIPs();
        if (!specialExternalIps.isEmpty()) {
            URL externalIpServiceURL = null;
            try {
                externalIpServiceURL = new URL(protocol, specialExternalIps.get(0), port, "");
            } catch (MalformedURLException e) {
                log.error("Service:{} Namespace:{} URLType:ExternalIP   URL malformed", serviceName, namespace);
            }
            Endpoint externalIpEndpoint = constructEndpoint(serviceName, namespace,
                    protocol, "ExternalIP", externalIpServiceURL);
            if (externalIpEndpoint != null) {
                this.servicesList.add(externalIpEndpoint);
            }
        }
    }


    private Endpoint constructEndpoint(String serviceName, String namespace, String portType,
                                                 String urlType, URL url) {
        //todo check if empty
        if (url == null) {
            return null;
        }
        String endpointConfig = String.format("{\"serviceUrl\": \"%s\"," +
                                                " \"urlType\": \"%s\"," +
                                                "\"namespace\": \"%s\"}",
                                                url.toString(), urlType, namespace);
        String endpointIndex = String.format("ds-%d", kubeEndpointIndex);
        return createEndpoint(endpointIndex, serviceName, endpointConfig,
                1000L, portType, "{\"enabled\": false}", APIMgtConstants.GLOBAL_ENDPOINT);
    }

    private Endpoint createEndpoint(String id, String name, String endpointConfig, Long maxTps,
                                    String type, String endpointSecurity, String applicableLevel) {
        Endpoint.Builder endpointBuilder = new Endpoint.Builder();
        endpointBuilder.id(id);
        endpointBuilder.name(name);
        endpointBuilder.endpointConfig(endpointConfig);
        endpointBuilder.maxTps(maxTps);
        endpointBuilder.type(type);
        endpointBuilder.security(endpointSecurity);
        endpointBuilder.applicableLevel(applicableLevel);

        kubeEndpointIndex++;
        return endpointBuilder.build();
    }

    private URL findNodePortServiceURLForAPort(String filerNamespace, String serviceName,
                                               String protocol, int nodePort) throws MalformedURLException {
        URL url;
        Endpoints endpoint = findEndpoint(filerNamespace, serviceName);
        List<EndpointSubset> endpointSubsets = endpoint.getSubsets();
        if (endpointSubsets == null) {
            //no endpoints for the service : when LoadBalancer type or pods not selected
            log.debug("Service:{}   No endpoints found for the service.", serviceName);
            endpointsAvailable = false;
            return null;
        }
        for (EndpointSubset endpointSubset : endpointSubsets) {
            List<EndpointAddress> endpointAddresses = endpointSubset.getAddresses();
            if (endpointAddresses.isEmpty()) {  //no endpoints for the service : when NodePort type
                log.debug("Service:{}   No endpoints found for the service.", serviceName);
                endpointsAvailable = false;
                return null;
            }
            for (EndpointAddress endpointAddress : endpointAddresses) {
                String podName = endpointAddress.getTargetRef().getName();
                Pod pod = findPod(filerNamespace, podName);
                try {
                    url = new URL(protocol, pod.getStatus().getHostIP(), nodePort, "");
                    return url;
                } catch (NullPointerException e) { //no pods available for this address
                    log.debug("Service:{}  Pod {}  not available", serviceName, podName);
                }
            }
        }
        return null;
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
            pod = client.pods().inAnyNamespace()
                    .withField("metadata.name", podName).list().getItems().get(0);
        } else {
            pod = client.pods().inNamespace(filterNamespace).withName(podName).get();
        }
        return pod;
    }
}
