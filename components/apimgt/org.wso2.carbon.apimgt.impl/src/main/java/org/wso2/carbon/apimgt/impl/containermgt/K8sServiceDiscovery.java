package org.wso2.carbon.apimgt.impl.containermgt;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.Endpoint;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.APICustomResourceDefinition;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.APICustomResourceDefinitionList;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.DoneableAPICustomResourceDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;
import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.NAMESPACE;

public class K8sServiceDiscovery extends K8sManager implements ServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);
    private String serviceName;
    private String serviceUrl;
    private Map<String,String > serviceDetails ;

    @Override
    public void initManager(Map parameterDetails){
        setValues(parameterDetails);
        setClient();
    }

    @Override
    protected void setClient(){
        Config serviceConfig = new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(saToken).withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();
                //Get keystore password to connect with local clusters

        this.openShiftClient = new DefaultOpenShiftClient(serviceConfig);
    }

//    protected void getServices(Map<String, String> clusterProperties){
//        Config serviceConfig = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
//                .withOauthToken(clusterProperties.get(SATOKEN)).build();
//
//        OpenShiftClient client = new DefaultOpenShiftClient(serviceConfig);
//        CustomResourceDefinition clusterService = client.customResourceDefinitions().withName(SERVICE).get();
//
//        ServiceList myServices = client.services().list();
//
//
//    }

//    public void getServices(Map<String, String> clusterProperties){
//        Config serviceConfig = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
//                .withOauthToken(clusterProperties.get(SATOKEN)).withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();
//
//        OpenShiftClient client = new DefaultOpenShiftClient(serviceConfig);
//        CustomResourceDefinition clusterService = client.customResourceDefinitions().withName(SERVICE).get();
//
//        ServiceList myServices = client.services().list();
//        log.info(String.valueOf(myServices));
//
//        System.out.println(String.valueOf(myServices));
//    }
//public void getServices(Map<String, String> clusterProperties){
//    Config serviceConfig = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
//            .withOauthToken(clusterProperties.get(SATOKEN)).withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();
//
//    OpenShiftClient client = new DefaultOpenShiftClient(serviceConfig);
//    //CustomResourceDefinition clusterService = client.customResourceDefinitions().withName(SERVICE).get();
//
//    //  List<Service> myServices = client.services().list().getItems();
//    List<Service> myServices = client.services().inNamespace(null).list().getItems();
//
//    // log.info(String.valueOf(myServices));
//
//    //  System.out.println(String.valueOf(myServices));
//
//
//    for (Service service : myServices) {
//        String serviceName = service.getMetadata().getName();
//        String namespace = service.getMetadata().getNamespace();
//        ServiceSpec serviceSpec = service.getSpec();
//        String serviceType = serviceSpec.getType();
//        List<String> externalIP = serviceSpec.getExternalIPs();
//        List<ServicePort> portSpec = serviceSpec.getPorts();
//        String clusterIP = serviceSpec.getClusterIP();
//
//        for(ServicePort portList:portSpec){
//            Integer nodePort = portList.getNodePort();
//            ContainerBasedConstants.TARGET_PORT = String.valueOf(portList.getTargetPort().getIntVal());
//            Integer port = portList.getPort();
//
//        }
//        System.out.println(serviceName +"\n" +namespace +"\n" + serviceType +"\n"+externalIP+"\n"+clusterIP + "\n"+ TARGET_PORT+"\n"+"\n");
//    }
//}

    public JSONObject getServices(Map<String, String> clusterProperties){
        Config serviceConfig = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
                .withOauthToken(clusterProperties.get(SATOKEN)).withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();

        OpenShiftClient client = new DefaultOpenShiftClient(serviceConfig);
        //CustomResourceDefinition clusterService = client.customResourceDefinitions().withName(SERVICE).get();

        //  List<Service> myServices = client.services().list().getItems();
        List<Service> myServices = client.services().inNamespace(null).list().getItems();
        ArrayList<String> endpointList = new ArrayList<>();
        JSONObject responses = new JSONObject();

        // log.info(String.valueOf(myServices));

        //  System.out.println(String.valueOf(myServices));
        //MASTER_URL = clusterProperties.get(MASTER_URL);

        for (Service service : myServices) {
            String serviceName = service.getMetadata().getName();
            String namespace = service.getMetadata().getNamespace();
            ServiceSpec serviceSpec = service.getSpec();
            String serviceType = serviceSpec.getType();
            List<String> externalIP = serviceSpec.getExternalIPs();
            List<ServicePort> portSpec = serviceSpec.getPorts();
            String clusterIP = serviceSpec.getClusterIP();

            for(ServicePort portList:portSpec){
                Integer nodePort = portList.getNodePort();
                ContainerBasedConstants.TARGET_PORT = String.valueOf(portList.getTargetPort().getIntVal());
                Integer port = portList.getPort();
                PROTOCOL = portList.getProtocol();

            }
            endpointList.add(serviceName);
            endpointList.add(namespace);
            endpointList.add(serviceType);
            endpointList.add(clusterProperties.get(MASTER_URL));
           // endpointList.add(externalIP);
            endpointList.add(PROTOCOL);

            //constructServiceResponse(serviceName,namespace,serviceType, clusterProperties.get(MASTER_URL),externalIP, PROTOCOL);
            // System.out.println(serviceName +"\n" +namespace +"\n" + serviceType +"\n"+externalIP+"\n"+clusterIP + "\n"+ TARGET_PORT+"\n"+"\n");
          //  System.out.println( constructServiceResponse(serviceName,namespace,serviceType, clusterProperties.get(MASTER_URL),externalIP, PROTOCOL));
            //System.out.println(PROTOCOL);

            JSONObject properties = new JSONObject();
            responses.put("serviceName",serviceName);
            responses.put("serviceURL",clusterProperties.get(MASTER_URL));
            properties.put("Namespace",namespace);
            properties.put("ServiceType",serviceType);
            properties.put("ExternalIPs",externalIP);
            properties.put("Protocol", PROTOCOL);
            responses.put("properties",properties);
        }
       // return endpointList;
        return responses;
    }

//    public static JSONObject constructServiceResponse(String serviceName, String namespace, String serviceType, String serviceURL, List<String> externalIP, String protocol){
//        JSONObject responses = new JSONObject();
//        JSONObject properties = new JSONObject();
//        responses.put("serviceName",serviceName);
//        responses.put("serviceURL",serviceURL);
//        properties.put("Namespace",namespace);
//        properties.put("ServiceType",serviceType);
//        properties.put("ExternalIPs",externalIP);
//        properties.put("Protocol", protocol);
//        responses.put("properties",properties);
//
//        return responses;
//    }
//
//    protected NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
//            DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
//            DoneableAPICustomResourceDefinition>> getServiceClient(OpenShiftClient client, CustomResourceDefinition service) {
//
//        NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
//                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
//                DoneableAPICustomResourceDefinition>> serviceClient = client
//                .customResources(service, APICustomResourceDefinition.class, APICustomResourceDefinitionList.class,
//                        DoneableAPICustomResourceDefinition.class);
//
//        serviceClient = ((MixedOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
//                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
//                DoneableAPICustomResourceDefinition>>) serviceClient).inNamespace(client.getNamespace());
//
//        return serviceClient;
//    }




}
