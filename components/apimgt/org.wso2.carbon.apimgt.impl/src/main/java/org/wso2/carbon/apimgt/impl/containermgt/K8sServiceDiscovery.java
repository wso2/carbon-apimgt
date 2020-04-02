package org.wso2.carbon.apimgt.impl.containermgt.k8service;

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants;
import org.wso2.carbon.apimgt.impl.containermgt.K8sManager;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;

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

      public Service getServices(Map<String , Object> clusterProperties){
          Service servicesListObj = new Service();
        List<Object> servicesList = servicesListObj.getInnerService();
        JSONObject responses = new JSONObject();
        JSONObject propertiesJson = new JSONObject();
          JSONArray serviceDetails = new JSONArray();

          List<List<Object>> servicesLists = new ArrayList<>();
          List<Service> allServices = new ArrayList<>();



          JSONObject implParameters = (JSONObject) clusterProperties.get("ImplParameters");
        String masterURL = (String) implParameters.get("MasterURL");
        String saToken = (String) implParameters.get("SAToken");
        String type = (String) clusterProperties.get("type");


        if(type.equalsIgnoreCase("Kubernetes")){
        }


        Config serviceConfig = new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(saToken).withClientKeyPassphrase(System.getProperty("javax.net.ssl.keyStorePassword")).build();
        //Config serviceConfig = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
        //      .withOauthToken(clusterProperties.get(SATOKEN)).withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();


        OpenShiftClient client = new DefaultOpenShiftClient(serviceConfig);
        //CustomResourceDefinition clusterService = client.customResourceDefinitions().withName(SERVICE).get();

        //  List<Service> myServices = client.services().list().getItems();
        List<io.fabric8.kubernetes.api.model.Service> myServices = client.services().inNamespace(null).list().getItems();



        for (io.fabric8.kubernetes.api.model.Service service : myServices) {
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
            servicesListObj.setServiceName(serviceName);
            servicesListObj.setServiceURL(masterURL);

            propertiesJson.put("Namespace",namespace);
            propertiesJson.put("ServiceType",serviceType);
            propertiesJson.put("ExternalIPs",externalIP);
            propertiesJson.put("Protocol", PROTOCOL);

            servicesListObj.setType(type);
            servicesListObj.setProperties(propertiesJson.toString());

            List<Object> innerServiceList = new ArrayList<>();
            innerServiceList.add(serviceName);
            innerServiceList.add(masterURL);
            innerServiceList.add(propertiesJson.toString());

            servicesListObj.setInnerService(innerServiceList);


            servicesLists.add(innerServiceList);

            servicesListObj.setServicesLists(servicesLists);

            //allServices.add(servicesListObj);



            // constructServiceResponse(serviceName,namespace,serviceType, clusterProperties.get(MASTER_URL),externalIP, PROTOCOL);
            // System.out.println(serviceName +"\n" +namespace +"\n" + serviceType +"\n"+externalIP+"\n"+clusterIP + "\n"+ TARGET_PORT+"\n"+"\n");
            // System.out.println( constructServiceResponse(serviceName,namespace,serviceType, clusterProperties.get(MASTER_URL),externalIP, PROTOCOL));
            //System.out.println(PROTOCOL);
            //responses.put("serviceName",serviceName);
           // responses.put("serviceURL",masterURL);

          //  responses.put("properties",propertiesJson);

           // System.out.println(responses.toString());
           // serviceDetails.add(responses);



        }
        return servicesListObj ;


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





}
