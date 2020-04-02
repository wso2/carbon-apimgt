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
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.Endpoint;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIMRegistryService;
import org.wso2.carbon.apimgt.impl.APIMRegistryServiceImpl;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
//import org.wso2.carbon.apimgt.impl.APIProviderImpl;
import org.wso2.carbon.apimgt.impl.containermgt.*;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APICategoryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.ServiceDiscoveryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;



public class ServiceDiscoveryApiServiceImpl implements ServiceDiscoveryApiService {

      private static final Log log = LogFactory.getLog(ServiceDiscoveryApiServiceImpl.class);

    Services serviceListObj = new Services();
    ServiceDiscoveryEndpoints endpointObj = new ServiceDiscoveryEndpoints();
    ServiceDiscoveryEndpoints subEndpointObj = new ServiceDiscoveryEndpoints();

   private List<Object> innerService;
   private List<Services> services;


    public Response serviceDiscoveryEndpointsTypeGet(String type, Integer limit, Integer offset,MessageContext messageContext) {
        ServiceDiscoveriesInfoDTO serviceDTO = new ServiceDiscoveriesInfoDTO();
        ServiceDiscoveriesInfoListDTO serviceListDTO = new ServiceDiscoveriesInfoListDTO();


          //setting default type values if they are not set
          type = type != null ? type : ContainerBasedConstants.SYSTEM_TYPE;
         limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
            offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
              try{
                 // String newSearchQuery = APIUtil.constructApisGetQuery(type);
                  APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
                  String username = RestApiUtil.getLoggedInUsername();
                  String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                  String id = RestApiUtil.getLoggedInUserGroupId();

                  Map<String, Object> serviceDiscoveryConfig = null;
                  K8sServiceDiscovery obj1 = new K8sServiceDiscovery();
                  serviceDiscoveryConfig = verifyTenant(tenantDomain);
                  endpointObj = obj1.getServices(serviceDiscoveryConfig);
                  int length = endpointObj.getServices().size();




                  List<ServiceDiscoveriesInfoDTO> list = new ArrayList<>();
                  ServiceDiscoveryMappingUtil.setPaginationParams(serviceListDTO,  offset, limit, length);



                  subEndpointObj.setType(endpointObj.getType());

                  List<Services> services = endpointObj.getServices();
                  List<Services> subServices = new ArrayList<>();

                  for(int i = offset ; i< offset+limit ; i++ ){
                     subServices.add(services.get(i));
                  }

                  subEndpointObj.setServices(subServices);




                  ServiceDiscoveryMappingUtil.listToDTO(serviceListDTO,subEndpointObj);





              } catch (Exception e) {
                  e.printStackTrace();
              }





          return Response.ok().entity(serviceListDTO).build();
  }





  public  Map<String, Object> verifyTenant ( String tenantDomain) throws APIManagementException, UserStoreException, RegistryException, ParseException {
      //String tenantDomain = MultitenantUtils.getTenantDomain(userId);
      Map<String, Object> serviceDiscoveryConfig = null;

      if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
          serviceDiscoveryConfig = APIUtil.getServiceDiscoveryConfigurationFromXML();


      }
      else{
          APIMRegistryService apimRegistryService = new APIMRegistryServiceImpl();
          String content= apimRegistryService.getConfigRegistryResourceContent(tenantDomain, APIConstants.API_TENANT_CONF_LOCATION);
          JSONParser jsonParser = new JSONParser();
        JSONObject tenantConf = (JSONObject) jsonParser.parse(content);
       Map<String, Map<String, Object>> allServiceDiscoverySystems = APIUtil.getServiceDiscoveryTypesFromConfig(tenantConf);
          serviceDiscoveryConfig = allServiceDiscoverySystems.get("implParameters");

      }
      return serviceDiscoveryConfig;
  }





}
