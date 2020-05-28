package org.wso2.carbon.apimgt.impl.containermgt;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConfigurations;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryEndpoints;
import org.wso2.carbon.apimgt.api.model.Services;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;

public class K8sServiceDiscovery  implements ServiceDiscovery {
    protected String masterURL;
    protected String saToken;
    protected String type;
    protected OpenShiftClient openShiftClient;
    List<Services> allServices = new ArrayList<>();
    ServiceDiscoveryEndpoints endpointObj = new ServiceDiscoveryEndpoints();




    private static final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);

    @Override
    public void initManager(Map implParametersDetails){

        setValues(implParametersDetails);
        setClient();
    }

    protected void setClient(){
        Config serviceConfig = new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(saToken).withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();
        this.openShiftClient = new DefaultOpenShiftClient(serviceConfig);

    }

    protected void setValues( Map<String, String> implParametersDetails) {

        this.masterURL = implParametersDetails.get(MASTER_URL);
        this.saToken = implParametersDetails.get(SATOKEN);
    }


    public ServiceDiscoveryEndpoints listServices(){

        JSONObject propertiesJson = new JSONObject();
        List<Service> myServices = openShiftClient.services().inAnyNamespace().list().getItems();

        for (Service service : myServices) {
            String serviceName = service.getMetadata().getName();
            String namespace = service.getMetadata().getNamespace();


            ServiceSpec serviceSpec = service.getSpec();

            String serviceType = serviceSpec.getType();

            List<String> externalIP = serviceSpec.getExternalIPs();
            List<ServicePort> portSpec = serviceSpec.getPorts();



            for(ServicePort portList:portSpec){
                ContainerBasedConstants.TARGET_PORT = String.valueOf(portList.getTargetPort().getIntVal());
                PROTOCOL = portList.getProtocol();

            }

            Services servicesListObj = new Services();

            servicesListObj.setServiceName(serviceName);
            servicesListObj.setServiceURL(masterURL);


            propertiesJson.put("Namespace",namespace);
            propertiesJson.put("ServiceType",serviceType);
            propertiesJson.put("ExternalIPs",externalIP);
            propertiesJson.put("Protocol", PROTOCOL);
            propertiesJson.put("TargetPort",ContainerBasedConstants.TARGET_PORT);

            servicesListObj.setProperties(propertiesJson.toString());
            allServices.add(servicesListObj);

        }
        endpointObj.setType(type);
        endpointObj.setServices(allServices);

        return endpointObj ;
    }

    public ServiceDiscoveryEndpoints listSubSetOfServices( int offset, int limit) throws IllegalAccessException, ParseException, InstantiationException, ClassNotFoundException, UserStoreException, APIManagementException, RegistryException {
        ServiceDiscoveryEndpoints subEndpointObj = new ServiceDiscoveryEndpoints();
        endpointObj = listServices();
        int length = endpointObj.getServices().size();
        subEndpointObj.setType(endpointObj.getType());
        List<Services> services = endpointObj.getServices();
        List<Services> subServices = new ArrayList<>();
        if(limit >= length){ limit = length;}
        for(int j = offset ; j< offset+limit ; j++ ){
            subServices.add(services.get(j));
        }
        subEndpointObj.setServices(subServices);


        return subEndpointObj;

    }

    public int getNumberOfServices(){
        int totalNumberOfServices = 0;
        endpointObj = listServices();
        totalNumberOfServices = endpointObj.getServices().size();
        return totalNumberOfServices ;
    }
}
