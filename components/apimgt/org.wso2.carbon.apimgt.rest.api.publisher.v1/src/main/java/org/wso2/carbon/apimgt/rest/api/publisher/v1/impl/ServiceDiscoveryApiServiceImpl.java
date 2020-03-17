package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import io.fabric8.kubernetes.api.model.Service;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants;
import org.wso2.carbon.apimgt.impl.containermgt.K8sServiceDiscovery;
import org.wso2.carbon.apimgt.impl.containermgt.ServiceDiscovery;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.base.MultitenantConstants;

import java.util.*;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getServices;


public class ServiceDiscoveryApiServiceImpl implements ServiceDiscoveryApiService {

    //  private static final Log log = LogFactory.getLog(ServiceDiscoveryApiServiceImpl.class);
    //private Object MultititenantConstaints;



    public Response serviceDiscoveryEndpointsTypeGet(String type, MessageContext messageContext) {
        ServiceDiscoveriesInfoDTO serviceListDTO = new ServiceDiscoveriesInfoDTO();
        Stack<String> serviceStack = new Stack<>();

          //setting default type values if they are not set
          type = type != null ? type : ContainerBasedConstants.SYSTEM_TYPE;
          String tenantDomain;
              try{
                  String newSearchQuery = APIUtil.constructApisGetQuery(type);
                  APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
                  String username = RestApiUtil.getLoggedInUsername();
                  if(newSearchQuery.startsWith(ContainerBasedConstants.SYSTEM_TYPE)){

                      // newSearchQuery = newSearchQuery.replace();
                      //getServices(APIUtil.getClusterInfoFromConfig());

                  }
                  JSONArray serviceDiscovery;
                  APIManagerConfiguration obj = new APIManagerConfiguration();
                  serviceDiscovery= obj.getServiceDiscoveryConf();
                  Map<String, String> serviceDiscoveryConfig = new ObjectMapper().convertValue(serviceDiscovery.get(0),
                          Map.class);
                  K8sServiceDiscovery obj1 = new K8sServiceDiscovery();
                 // JSONObject f = obj1.getServices(serviceDiscoveryConfig);

                  APIUtil obj2 = new APIUtil();//
                  JSONObject f = obj2.getServices(serviceDiscoveryConfig);
                  listToDTO(f,serviceListDTO);
                //  APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

              } catch (Exception e) {
                  e.printStackTrace();
              }




          return Response.ok().entity(serviceListDTO).build();
  }

    private void listToDTO(JSONObject f, ServiceDiscoveriesInfoDTO serviceListDTO) {
        String serviceName = (String) f.get("serviceName");
        String serviceURL = (String) f.get("serviceURL");
        //Object properties = f.get("properties");
        Map properties = ((Map)f.get("properties"));
        String Namespace = (String)properties.get("Namespace");
        String ServiceType = (String)properties.get("ServiceType");

        String proDetails = properties.toString();
        System.out.println(serviceName+"\n"+serviceURL+"\n"+proDetails);


        //String Namespace = properties.get();

       serviceListDTO.setServiceName(serviceName);
       serviceListDTO.setServiceURL(serviceURL);
       serviceListDTO.setProperties(proDetails);
      //  serviceListDTO.set

    }

//    private void listToDTO(ArrayList list,ServiceDiscoveriesInfoDTO dtoObj){
//
//            dtoObj.setServiceName(list.get(0));
//
//    }

  public  void verifyTenant (int tenantId, String tenantDomain) throws APIManagementException {
      if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
           JSONArray serviceDiscovery;
         // serviceDiscovery = APIManagerConfiguration.getServiceDiscoveryConf();
          APIManagerConfiguration obj = new APIManagerConfiguration();
          serviceDiscovery= obj.getServiceDiscoveryConf();
          Map<String, String> serviceDiscoveryConfig = new ObjectMapper().convertValue(serviceDiscovery.get(0),
                  Map.class);
          // APIUtil.getServices();
         // ArrayList s = APIUtil.getServices(serviceDiscoveryConfig);


      }
      else{
        //  APIUtil.getClusterInfoFromConfig(clusterProperties);
        //  getClusterInfoFromConfig(JSONObject tenantConf)
      }
  }




}
