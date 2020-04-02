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
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.Endpoint;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.APICustomResourceDefinition;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.APICustomResourceDefinitionList;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.DoneableAPICustomResourceDefinition;

import java.util.*;

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



        List<Services> allServices = new ArrayList<>();

      public ServiceDiscoveryEndpoints getServices(Map<String , Object> clusterProperties){
          ServiceDiscoveryEndpoints endpointObj = new ServiceDiscoveryEndpoints();
        JSONObject responses = new JSONObject();
        JSONObject propertiesJson = new JSONObject();
          JSONArray serviceDetails = new JSONArray();

          List<List<Object>> servicesLists = new ArrayList<>();



          JSONObject implParameters = (JSONObject) clusterProperties.get("ImplParameters");
        String masterURL = (String) implParameters.get("MasterURL");
        String saToken = (String) implParameters.get("SAToken");
        String type = (String) clusterProperties.get("type");


        if(type.equalsIgnoreCase("Kubernetes")){
        }


        Config serviceConfig = new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(saToken).withClientKeyPassphrase(System.getProperty("javax.net.ssl.keyStorePassword")).build();


        OpenShiftClient client = new DefaultOpenShiftClient(serviceConfig);

        List<Service> myServices = client.services().inNamespace(null).list().getItems();



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

            endpointObj.setType(type);

            Services servicesListObj = new Services();


            servicesListObj.setServiceName(serviceName);
            servicesListObj.setServiceURL(masterURL);

            propertiesJson.put("Namespace",namespace);
            propertiesJson.put("ServiceType",serviceType);
            propertiesJson.put("ExternalIPs",externalIP);
            propertiesJson.put("Protocol", PROTOCOL);

            servicesListObj.setProperties(propertiesJson.toString());


            allServices.add(servicesListObj);


        }
          endpointObj.setServices(allServices);

          return endpointObj ;

    }
}
