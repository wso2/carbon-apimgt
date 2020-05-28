package org.wso2.carbon.apimgt.impl.containermgt;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import okhttp3.Dispatcher;
import okhttp3.internal.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryEndpoints;
import org.wso2.carbon.apimgt.api.model.Services;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.executors.APIExecutor;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({APIProviderImpl.class, APIExecutor.class, APIUtil.class})
public class K8sServiceDiscoveryTestCase {
    protected String masterURL = "https://192.168.99.104:8443";
    protected String type="Kubernetes";
    List<Services> allServices = new ArrayList<>();
    ServiceDiscoveryEndpoints endpointObj = new ServiceDiscoveryEndpoints();



@Rule
public KubernetesServer server = new KubernetesServer(false,true);

    @Test
    public void listServices(){
        server.before();
        Mockito.mock(OpenShiftClient.class);
        JSONObject propertiesJson = new JSONObject();
       ServiceSpec testServiceSpec = new ServiceSpec();
       testServiceSpec.setType("ClusterIP");
        List<String> testExternalIP = new ArrayList<>();
        testServiceSpec.setExternalIPs(testExternalIP);


        Service service1 = new ServiceBuilder().withNewMetadata().withName("myservice1").withNamespace("default").endMetadata().
                withNewSpec().withExternalIPs("102.435.232").withType("ClusterIP").withPorts().endSpec().build();

List<Service> expextedServices = new ArrayList<>();  expextedServices.add(service1);
server.expect().get().withPath("/api/v1/namespaces/default/services").andReturn(HttpURLConnection.HTTP_OK, expextedServices)
                .once();
//Service resService = openShiftClient.services().inNamespace("default").create(service1);
//        openShiftClient = (OpenShiftClient) server.getClient();
        KubernetesClient client = server.getClient();

//        openShiftClient.services().inAnyNamespace().create(new ServiceBuilder().withNewMetadata().withName("pod1").endMetadata().build());
        List<Service> myServices = client.services().inAnyNamespace().list().getItems();
//myServices.add(service1);
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
        Assert.assertNotNull(service1.getMetadata());
        Assert.assertNotNull(endpointObj.getServices());


    }


    @Test
    public void testlistSubSetOfServices() throws IllegalAccessException, ParseException, InstantiationException, ClassNotFoundException, UserStoreException, APIManagementException, RegistryException {
        K8sServiceDiscovery k8Object =Mockito.mock(K8sServiceDiscovery.class);

        int offset = 0;
        int limit = 25;
        ServiceDiscoveryEndpoints subEndpointObj = new ServiceDiscoveryEndpoints();
        ServiceDiscoveryEndpoints testsubEndpointObj = new ServiceDiscoveryEndpoints();
        List<Services> testServices = new ArrayList<>();
        Services servicesObj = new Services();
        servicesObj.setServiceURL("https://192.168.99.104:8443");
        servicesObj.setServiceName("myservice1");
        JSONObject propertiesJson = new JSONObject();
        propertiesJson.put("Namespace","default");
        propertiesJson.put("ServiceType","LoadBalancer");
        propertiesJson.put("ExternalIPs","2324.433.243.");
        propertiesJson.put("Protocol", "TCP");
        propertiesJson.put("TargetPort","24334");
        servicesObj.setProperties(propertiesJson.toString());
        testsubEndpointObj.setType("Kubernetes");
        testsubEndpointObj.setServices(testServices);
        Mockito.mock(ServiceDiscovery.class);
        Mockito.when(k8Object.listServices()).thenReturn(testsubEndpointObj);



        endpointObj = k8Object.listServices();
        int length = endpointObj.getServices().size();
        subEndpointObj.setType(endpointObj.getType());
        List<Services> services = endpointObj.getServices();
        List<Services> subServices = new ArrayList<>();
        if(limit >= length){ limit = length;}
        for(int j = offset ; j< offset+limit ; j++ ){
            subServices.add(services.get(j));
        }
        subEndpointObj.setServices(subServices);
        Assert.assertEquals(testsubEndpointObj.getServices(),subEndpointObj.getServices());


    }

    @Test
    public void testgetNumberOfServices(){
        K8sServiceDiscovery k8Object = Mockito.mock(K8sServiceDiscovery.class);
        int totalNumberOfServices = 0;
        int testNoOfServices = 8;
        Mockito.mock(ServiceDiscovery.class);
        ServiceDiscoveryEndpoints testsubEndpointObj = new ServiceDiscoveryEndpoints();
        List<Services> testServices = new ArrayList<>();
        Services servicesObj = new Services();
        servicesObj.setServiceURL("https://192.168.99.104:8443");
        servicesObj.setServiceName("myservice1");
        JSONObject propertiesJson = new JSONObject();
        propertiesJson.put("Namespace","default");
        propertiesJson.put("ServiceType","LoadBalancer");
        propertiesJson.put("ExternalIPs","2324.433.243.");
        propertiesJson.put("Protocol", "TCP");
        propertiesJson.put("TargetPort","24334");
        servicesObj.setProperties(propertiesJson.toString());
        testsubEndpointObj.setType("Kubernetes");
        testServices.add(servicesObj);
        testsubEndpointObj.setServices(testServices);

        Mockito.when(k8Object.listServices()).thenReturn(testsubEndpointObj);

        endpointObj = k8Object.listServices();
        totalNumberOfServices = endpointObj.getServices().size();
        Assert.assertEquals(1,totalNumberOfServices);
    }





}
