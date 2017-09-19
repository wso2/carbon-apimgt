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
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.ServiceDiscoverer;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfiurations;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
*
* ----------------------------------------------------------------
* for a service to be discovred
* port name must be : http/https
* Todo -- if 443 then https
* ----------------------------------------------------------------
* Todo -- filter by service type
*
**/
public class KubernetesServiceDiscoverer implements ServiceDiscoverer {

    //private OpenShiftClient client;
    private KubernetesClient client;
    private ServiceDiscoveryConfiurations serviceDiscoveryConfiurations;
    private final Logger log  = LoggerFactory.getLogger(KubernetesServiceDiscoverer.class);


    private String serviceAccountToken;
    private String caCertLocation;
    private Boolean insidePod;

    private List<Endpoint> servicesList;
    private Boolean endpointsAvailable; //when false, will not look for NodePort urls for the remaining ports.
    private int kubeEndpointIndex;


    private KubernetesServiceDiscoverer() {
        serviceDiscoveryConfiurations = new ServiceDiscoveryConfiurations();
        JSONObject security = new JSONObject(serviceDiscoveryConfiurations.getSecurity());
        JSONObject cmsProperties = new JSONObject(serviceDiscoveryConfiurations.getProperties());
        serviceAccountToken = security.getString("serviceAccountToken");
        caCertLocation = security.getString("caCertLocation");
        insidePod = cmsProperties.getBoolean("insidePod");
        try {
            //this.client = new DefaultOpenShiftClient(buildConfig(serviceDiscoveryConfiurations.getMasterUrl()));
            this.client = new DefaultKubernetesClient(buildConfig(serviceDiscoveryConfiurations.getMasterUrl()));
        } catch (KubernetesClientException e) {
            log.error("Authentication error or config for Kubernetes client not properly built", e.getMessage());
        }
        servicesList = new ArrayList<>();
        kubeEndpointIndex = 0;
    }

    public static KubernetesServiceDiscoverer getInstance() {
        return new KubernetesServiceDiscoverer();
    }

    private Config buildConfig(String masterUrl) {
        System.setProperty("kubernetes.auth.tryKubeConfig", "false");
        System.setProperty("kubernetes.auth.tryServiceAccount", "true");
        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(masterUrl)
                .withCaCertFile(caCertLocation);
        Config config = null;
        if (insidePod) {
            log.debug("Using mounted service account token");
            try {
                String saMountedToken = IOUtils.toString(KubernetesServiceDiscoverer.class
                        .getResourceAsStream("/var/run/secrets/kubernetes.io/serviceaccount/token"),
                        "UTF-8");
                config = configBuilder.withOauthToken(saMountedToken).build();
            } catch (IOException e) {
                log.error("Token file not found", e.getMessage());
            }
        } else {
            log.debug("Using externally stored service account token");
            config = configBuilder.withOauthToken(serviceAccountToken).build();
        }
        return config;
    }


    @Override
    public Boolean isEnabled() {
        return serviceDiscoveryConfiurations.isServiceDiscoveryEnabled();
    }

    @Override
    public List<Endpoint> listServices() throws MalformedURLException {
        if (client != null) {
            log.debug("Looking for services in all namespaces");
            try {
                ServiceList services = client.services().inAnyNamespace().list();
                addServiceEndpointsToList(services, null);
            } catch (KubernetesClientException e) {
                log.error(e.getMessage());
            }
        }
        return this.servicesList;
    }

    @Override
    public List<Endpoint> listServices(String namespace) throws MalformedURLException {
        if (client != null) {
            log.debug("Looking for services in namespace {}", namespace);
            try {
                ServiceList services = client.services().inNamespace(namespace).list();
                addServiceEndpointsToList(services, namespace);
            } catch (KubernetesClientException e) {
                log.error(e.getMessage());
            }
        }
        return this.servicesList;
    }

    @Override
    public List<Endpoint> listServices(String namespace, HashMap<String, String> criteria)
            throws MalformedURLException {
        if (client != null) {
            log.debug("Looking for services, with the specified labels, in namespace {}", namespace);
            try {
                ServiceList services = client.services().inNamespace(namespace).withLabels(criteria).list();
                addServiceEndpointsToList(services, namespace);
            } catch (KubernetesClientException e) {
                log.error(e.getMessage());
            }
        }
        return this.servicesList;
    }

    @Override
    public List<Endpoint> listServices(HashMap<String, String> criteria)
            throws MalformedURLException {
        if (client != null) {
            log.debug("Looking for services, with the specified labels, in all namespaces");
            try {
                ServiceList services = client.services().withLabels(criteria).list();
                addServiceEndpointsToList(services, null);
            } catch (KubernetesClientException e) {
                log.error(e.getMessage());
            }
        }
        return this.servicesList;
    }


    private void addServiceEndpointsToList(ServiceList services, String filterNamespace)
            throws MalformedURLException {
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
                        //ClusterIP Service
                        URL clusterIPServiceURL = new URL(protocol, serviceSpec.getClusterIP(), port, "");
                        Endpoint clusterIPEndpoint = constructDiscoveredEndpoint(serviceName, namespace, protocol,
                                "ClusterIP", clusterIPServiceURL);
                        if (clusterIPEndpoint != null) {
                            this.servicesList.add(clusterIPEndpoint);
                        }

                        //ExternalName Service
                        if (serviceSpec.getType().equals("ExternalName")) {
                            String externalName = (String) service.getSpec()
                                    .getAdditionalProperties().get("externalName");
                            URL externalNameServiceURL = new URL(protocol + "://" + externalName);
                            Endpoint externalNameEndpoint = constructDiscoveredEndpoint(serviceName,
                                    namespace, protocol, "ExternalName", externalNameServiceURL);
                            if (externalNameEndpoint != null) {
                                this.servicesList.add(externalNameEndpoint);
                            }
                        }
                    }
                    //NodePort Service
                    if (!service.getSpec().getType().equals("ClusterIP") && endpointsAvailable) {
                        URL nodePortServiceURL = findNodePortServiceURLForAPort(filterNamespace,
                                serviceName, protocol, servicePort.getNodePort());
                        Endpoint nodePortEndpoint = constructDiscoveredEndpoint(serviceName, namespace,
                                protocol, "NodePort", nodePortServiceURL);
                        if (nodePortEndpoint != null) {
                            this.servicesList.add(nodePortEndpoint);
                        }
                    }
                    //LoadBlancer Service
                    if (service.getSpec().getType().equals("LoadBalancer")) {
                        List<LoadBalancerIngress> loadBalancerIngresses = service.getStatus()
                                .getLoadBalancer().getIngress();
                        if (!loadBalancerIngresses.isEmpty()) {
                            URL loadBalancerServiceURL = new URL(protocol,
                                    loadBalancerIngresses.get(0).getIp(), port, "");
                            Endpoint loadBalancerEndpoint = constructDiscoveredEndpoint(serviceName,
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
                    //ExternalName - Special case. Not managed by Kubernetes but the cluster administrator.
                    List<String> specialExternalIps = service.getSpec().getExternalIPs();
                    if (!specialExternalIps.isEmpty()) {
                        URL externalIpServiceURL = new URL(protocol, specialExternalIps.get(0), port, "");
                        Endpoint externalIpEndpoint = constructDiscoveredEndpoint(serviceName, namespace,
                                protocol, "ExternalIP", externalIpServiceURL);
                        if (externalIpEndpoint != null) {
                            this.servicesList.add(externalIpEndpoint);
                        }
                    }

                } else if (log.isDebugEnabled()) {
                    log.debug("Service:{} Namespace:{} Port:{}/{}  Application level protocol not defined.",
                            serviceName, service.getMetadata().getNamespace(),
                            servicePort.getPort(), protocol);
                }
            }
        }
    }

    private Endpoint constructDiscoveredEndpoint(String serviceName, String namespace, String portType,
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

        return constructEndPoint(endpointIndex, serviceName, endpointConfig,
                1000L, portType, "{\"enabled\": false}", APIMgtConstants.GLOBAL_ENDPOINT);
    }

    private Endpoint constructEndPoint(String id, String name, String endpointConfig,
                                       Long maxTps, String type, String endpointSecurity,
                                       String applicableLevel) {
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
                                               String protocol, int nodePort)
            throws MalformedURLException {
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
