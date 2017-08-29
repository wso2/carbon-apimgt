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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.ServiceDiscovery;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfiurations;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
*
* ----------------------------------------------------------------
* for a service to be discovred
* port name must be : http/https
* Todo ---> if 443 then https
*
*-----------------------------------------------------------------
* viewing only service,endpoints,pods so to avoid viewing nodes
*
* ----------------------------------------------------------------
* Endpoint applicable level- always GLOBAL. Api specific not used.
*
* ----------------------------------------------------------------
* Endpoint security not assinged
*
* ----------------------------------------------------------------
* Todo ---> filter by service type
*
* */

public class KubernetesServiceDiscovery implements ServiceDiscovery {

    //private OpenShiftClient client;
    private KubernetesClient client;
    private ServiceDiscoveryConfiurations serviceDiscoveryConfiurations;
    private final Logger log  = LoggerFactory.getLogger(KubernetesServiceDiscovery.class);

    private List<Endpoint> servicesList;
    private Boolean endpointsAvailable; //when false, will not look for NodePort urls for the remaining ports.
    private int kubeEndpointIndex;

    KubernetesServiceDiscovery() throws Exception {
        serviceDiscoveryConfiurations =new ServiceDiscoveryConfiurations();
        try {
            //this.client = new DefaultOpenShiftClient(buildConfig(serviceDiscoveryConfiurations.masterUrl));
            this.client = new DefaultKubernetesClient(buildConfig(serviceDiscoveryConfiurations.getMasterUrl()));
        } catch (KubernetesClientException e) {
            e.printStackTrace();
        }
        servicesList = new ArrayList<>();
        kubeEndpointIndex =0;
    }

    private Config buildConfig(String masterUrl){
        System.setProperty("kubernetes.auth.tryKubeConfig", "false");
        System.setProperty("kubernetes.auth.tryServiceAccount", "true");
        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(masterUrl).withTrustCerts(true)
                .withClientCertFile(serviceDiscoveryConfiurations.getClientCertLocation());
        Config config;
        if(serviceDiscoveryConfiurations.isInsidePod()){
            // config = new ConfigBuilder().withMasterUrl(masterUrl.toString()).build();
            try {
                config = configBuilder.withOauthToken(new String(Files.readAllBytes(Paths
                        .get("/var/run/secrets/kubernetes.io/serviceaccount/token")))).build();
            } catch (IOException e) {
                config = null;
                log.error("Token file not found");
                e.printStackTrace();
            }
        }else{
            config = configBuilder.withOauthToken(serviceDiscoveryConfiurations
                    .getServiceAccountToken()).build();
        }
        return config;
    }

    @Override
    public List<Endpoint> listServices() throws MalformedURLException {
        log.debug("Looking for services in all namespaces");
        try{
            ServiceList services = client.services().inAnyNamespace().list();
            addServicesToList(services, null);
        }catch (KubernetesClientException e){
            e.printStackTrace();
        }
        return this.servicesList;
    }

    @Override
    public List<Endpoint> listServices(String namespace) throws MalformedURLException {
        log.debug("Looking for services in namespace "+namespace);
        try {
            ServiceList services = client.services().inNamespace(namespace).list();
            addServicesToList(services, namespace);
        }catch (KubernetesClientException e){
            e.printStackTrace();
        }
        return this.servicesList;
    }

    @Override
    public List<Endpoint> listServices(String namespace, HashMap<String, String> criteria)
            throws MalformedURLException {
        log.debug("Looking for services, with the specified labels, in namespace "+namespace);
        try{
            ServiceList services = client.services().inNamespace(namespace).withLabels(criteria).list();
            addServicesToList(services, namespace);
        }catch (KubernetesClientException e){
            e.printStackTrace();
        }
        return this.servicesList;
    }

    @Override
    public List<Endpoint> listServices(HashMap<String, String> criteria)
            throws MalformedURLException {
        log.debug("Looking for services, with the specified labels, in all namespaces");
        try{
            ServiceList services = client.services().withLabels(criteria).list();
            addServicesToList(services,null);
        }catch (KubernetesClientException e){
            e.printStackTrace();
        }
        return this.servicesList;
    }

    private void addServicesToList(ServiceList services, String filterNamespace)
            throws MalformedURLException {
        List<Service> serviceItems = services.getItems();
        for (Service service : serviceItems) {
            String serviceName = service.getMetadata().getName();
            ServiceSpec serviceSpec = service.getSpec();
            endpointsAvailable = true;
            for (ServicePort servicePort : serviceSpec.getPorts()) {
                String protocol = servicePort.getName();
                if (protocol !=null && (protocol.equals("http") || protocol.equals("https"))) {
                    int port = servicePort.getPort();
                    if(serviceDiscoveryConfiurations.isInsidePod()){
                        //ClusterIP Service
                        URL clusterIPServiceURL = new URL(protocol, serviceSpec.getClusterIP(), port, "");
                        this.servicesList.add(constructDiscoveredEndpoint(serviceName, protocol,
                                "ClusterIP", clusterIPServiceURL));


                        //ExternalName Service
                        if(serviceSpec.getType().equals("ExternalName")){
                            String externalName = (String)service.getSpec().getAdditionalProperties().get("externalName");
                            URL externalNameServiceURL = new URL(protocol + "://" + externalName);
                            this.servicesList.add(constructDiscoveredEndpoint(serviceName, protocol,
                                    "ExternalName", externalNameServiceURL));
                        }
                    }
                    //NodePort Service
                    if(!service.getSpec().getType().equals("ClusterIP") && endpointsAvailable) {
                        URL nodePortServiceURL = findNodePortServiceURLForAPort(filterNamespace,
                                serviceName, protocol, servicePort.getNodePort());
                        if (nodePortServiceURL != null) {
                            this.servicesList.add(constructDiscoveredEndpoint(serviceName, protocol,
                                    "NodePort", nodePortServiceURL));
                        }
                    }
                    //LoadBlancer Service
                    if(service.getSpec().getType().equals("LoadBalancer")) {
                        List<LoadBalancerIngress> loadBalancerIngresses = service.getStatus().getLoadBalancer().getIngress();
                        if (!loadBalancerIngresses.isEmpty()) {
                            URL loadBalancerServiceURL = new URL(protocol,
                                    loadBalancerIngresses.get(0).getIp(), port, "");
                            this.servicesList.add(constructDiscoveredEndpoint(serviceName, protocol,
                                    "LoadBalancer", loadBalancerServiceURL));
                        } else if (log.isDebugEnabled()) {
                            log.debug("Service:{}  Namespace:{}  Port:{}/{} has no loadbalancer ingresses available.",
                                    serviceName, service.getMetadata().getNamespace(),
                                    port, protocol);
                        }
                    }
                    //ExternalName - Special case. Not managed by Kubernetes but the cluster administrator.
                    List<String> specialExternalIps = service.getSpec().getExternalIPs();
                    if (!specialExternalIps.isEmpty()) {
                        URL externalIpServiceURL = new URL(protocol, specialExternalIps.get(0), port, "");
                        this.servicesList.add(constructDiscoveredEndpoint(serviceName, protocol,
                                "ExternalIP", externalIpServiceURL));
                    }

                }else if(log.isDebugEnabled()){
                    log.debug("Service:{}  Port:{}/{}     Application level protocol not defined.",
                            serviceName, service.getMetadata().getNamespace(),
                            servicePort.getPort(), protocol);
                }
            }
        }
    }

    private Endpoint constructDiscoveredEndpoint(String serviceName, String portType,
                                                 String urlType, URL url){
        //todo check if empty
        String endpointConfig = String.format("{url: %s, urlType: %s}", url.toString(), urlType);
        return constructEndPoint(String.format("kube-%d", kubeEndpointIndex), serviceName, endpointConfig,
                (long)1000, portType,"","GLOBAL");
    }

    private Endpoint constructEndPoint(String id, String name, String endpointConfig,
                                       Long maxTps, String type, String security, String applicableLevel)  {
        Endpoint.Builder endpointBuilder = new Endpoint.Builder();
        endpointBuilder.id(id);
        endpointBuilder.name(name);
        endpointBuilder.endpointConfig(endpointConfig);
        endpointBuilder.maxTps(maxTps);
        endpointBuilder.type(type);
        endpointBuilder.security(security);
        endpointBuilder.applicableLevel(applicableLevel);

        kubeEndpointIndex++;
        return endpointBuilder.build();
    }

    private URL findNodePortServiceURLForAPort(String filerNamespace, String serviceName,
                                               String protocol, int nodePort) throws MalformedURLException {
        URL url;
        Endpoints endpoint = findEndpoint(filerNamespace,serviceName);
        List<EndpointSubset> endpointSubsets = endpoint.getSubsets();
        if(endpointSubsets == null) {
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
                Pod pod = findPod(filerNamespace,podName);
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

    private Endpoints findEndpoint(String filerNamespace, String serviceName){
        Endpoints endpoint;
        if(filerNamespace==null){
            //Below, method ".inAnyNamespace()" did not support ".withName()".
            //Therefore ".withField()" is used.
            //It returns a single item list which has the only endpoint created for the service.
            endpoint = client.endpoints().inAnyNamespace()
                    .withField("metadata.name",serviceName).list().getItems().get(0);
        }else {
            endpoint = client.endpoints().inNamespace(filerNamespace).withName(serviceName).get();
        }
        return endpoint;
    }

    private Pod findPod(String filerNamespace, String podName){
        Pod pod;
        if(filerNamespace==null){
            pod = client.pods().inAnyNamespace()
                    .withField("metadata.name",podName).list().getItems().get(0);
        }else {
            pod = client.pods().inNamespace(filerNamespace).withName(podName).get();
        }
        return pod;
    }
}
