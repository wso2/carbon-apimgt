package org.wso2.carbon.apimgt.rest.api.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.model.*;


import org.wso2.carbon.apimgt.rest.api.model.Error;
import org.wso2.carbon.apimgt.rest.api.model.API;
import org.wso2.carbon.apimgt.rest.api.model.Document;

import java.util.*;

import org.wso2.carbon.apimgt.rest.api.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.model.Tag;
import org.wso2.carbon.apimgt.rest.api.model.Tier;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl extends ApisApiService {
  
      @Override
      public Response apisGet(String limit,String offset,String query,String type,String sort,String accept,String ifNoneMatch)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisPost(API body,String contentType)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisChangeLifecyclePost(String newState,String publishToGateway,String resubscription,String apiId,String ifMatch,String ifUnmodifiedSince)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisCopyApiPost(String newVersion,String apiId)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince)
      throws NotFoundException {

          String[] apiIdDetails = apiId.split("-");
          String apiName = apiIdDetails[0];
          String version = apiIdDetails[1];
          String providerName = apiIdDetails[2];
          String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);

          APIIdentifier apiIdentifier = new APIIdentifier(providerNameEmailReplaced, apiName, version);
          APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(providerName);
          boolean isTenantFlowStarted = false;

          API apiToReturn = new API();

          try {
              String tenantDomain = MultitenantUtils.getTenantDomain(providerName);
              //how to get login details?
              CarbonContext.getThreadLocalCarbonContext().getTenantDomain(); //<- can this be used as userTenantDomain?
              String userTenantDomain = MultitenantUtils
                      .getTenantDomain(APIUtil.replaceEmailDomainBack(((APIProviderHostObject) thisObj).getUsername()));
              if (!tenantDomain.equals(userTenantDomain)) {
                  return Response.status(Response.Status.FORBIDDEN)
                          .entity("Cannot access API:" + apiId + " from current tenant")
                          .type(MediaType.APPLICATION_JSON)
                          .build();
              }
              if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                  isTenantFlowStarted = true;
                  PrivilegedCarbonContext.startTenantFlow();
                  PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
              }
              org.wso2.carbon.apimgt.api.model.API api = apiProvider.getAPI(apiIdentifier);
              if (api != null) {

                  ===============================================================
                  apiToReturn.setName(api.getId().getApiName());
                  apiToReturn.setVersion(api.getId().getVersion());
                  apiToReturn.setProvider(api.getId().getProviderName());
                  apiToReturn.setContext(api.getContext());
                  apiToReturn.setDescription(api.getDescription());

                  //how to get environments
                  apiToReturn.getEnvironments();

                  apiToReturn.setIsDefaultVersion(api.isDefaultVersion());
                  apiToReturn.setResponseCaching(api.getResponseCache());

                  List<Sequence> sequences = null;

                  String inSequenceName = api.getInSequence();
                  if (inSequenceName != null && !inSequenceName.isEmpty()) {
                      Sequence inSequence = new Sequence();
                      inSequence.setName(inSequenceName);
                      inSequence.setType("IN");
                      sequences.add(inSequence);
                  }

                  String outSequenceName = api.getOutSequence();
                  if (outSequenceName != null && !outSequenceName.isEmpty()) {
                      Sequence outSequence = new Sequence();
                      outSequence.setName(outSequenceName);
                      outSequence.setType("OUT");
                      sequences.add(outSequence);
                  }

                  String faultSequenceName = api.getFaultSequence();
                  if (faultSequenceName != null && !faultSequenceName.isEmpty()) {
                      Sequence faultSequence = new Sequence();
                      faultSequence.setName(faultSequenceName);
                      faultSequence.setType("FAULT");
                      sequences.add(faultSequence);
                  }

                  apiToReturn.setSequences(sequences);

                  apiToReturn.setStatus(api.getStatus().getStatus());
                  apiToReturn.setSubscriptionAvailability(api.getSubscriptionAvailability());
                  if (api.getSubscriptionAvailability() != null &&
                      api.getSubscriptionAvailability().equals(APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS)) {
                      apiToReturn.setVisibleTenants(Arrays.asList(api.getVisibleTenants().split(",")));
                  }
                  //Get Swagger definition which has URL templates and resource details
                  // should we use another approach?
                  APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();
                  RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
                  int tenantId;
                  UserRegistry registry;
                  String apiSwaggerDefinition;

                  tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                          .getTenantId(userTenantDomain);
                  registry = registryService.getGovernanceSystemRegistry(tenantId);

                  apiSwaggerDefinition = definitionFromSwagger20.getAPIDefinition(api.getId(), registry);

                  apiToReturn.setSwagger(apiSwaggerDefinition);

                  Set<String> apiTags = api.getTags();
                  List<Tag> tagsToReturn = null;
                  for (String tag : apiTags) {
                      Tag newTag = new Tag();
                      newTag.setName(tag);
                      tagsToReturn.add(newTag);
                  }
                  apiToReturn.setTags(tagsToReturn);

                  Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = api.getAvailableTiers();
                  List<String> tiersToReturn = null;
                  for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
                      tiersToReturn.add(tier.getName());
                  }
                  apiToReturn.setTiers(tiersToReturn);

                  apiToReturn.setTransport(Arrays.asList(api.getTransports().split(",")));
                  //apiToReturn.setType("");   how to get type?
                  apiToReturn.setVisibility((API.VisibilityEnum) api.getVisibility());
                  apiToReturn.setVisibleRoles(Arrays.asList(api.getVisibleRoles().split(",")));

                  ===============================================================

                  Set<APIStore> storesSet = apiProvider.getExternalAPIStores(api.getId());
                  if (storesSet != null && storesSet.size() != 0) {
                      NativeArray apiStoresArray = new NativeArray(0);
                      int i = 0;
                      for (APIStore store : storesSet) {
                          NativeObject storeObject = new NativeObject();
                          storeObject.put("name", storeObject, store.getName());
                          storeObject.put("displayName", storeObject, store.getDisplayName());
                          storeObject.put("published", storeObject, store.isPublished());
                          apiStoresArray.put(i, apiStoresArray, storeObject);
                          i++;
                      }
                      myn.put(29, myn, apiStoresArray);
                  }

                  myn.put(36, myn, checkValue(Integer.toString(api.getCacheTimeout())));

                  myn.put(39, myn, checkValue(api.getDestinationStatsEnabled()));

                  Set<Scope> scopes = api.getScopes();
                  JSONArray scopesNative = new JSONArray();
                  for (Scope scope : scopes) {
                      JSONObject scopeNative = new JSONObject();
                      scopeNative.put("id", scope.getId());
                      scopeNative.put("key", scope.getKey());
                      scopeNative.put("name", scope.getName());
                      scopeNative.put("roles", scope.getRoles());
                      scopeNative.put("description", scope.getDescription());
                      scopesNative.add(scopeNative);
                  }

              } else {
                  //log the error
                  return Response.status(Response.Status.NOT_FOUND)
                      .entity("Cannot find the requested API- " + apiName +
                          "-" + version)
                      .type(MediaType.APPLICATION_JSON)
                      .build();
              }
          } catch (RegistryException e) {
              //500
              System.out.println("Error when create registry instance ");
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                  .entity("Internal server error")
                  .type(MediaType.APPLICATION_JSON)
                  .build();
          } catch (UserStoreException e) {
              //500
              System.out.println("Error while reading tenant information ");
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                  .entity("Internal server error")
                  .type(MediaType.APPLICATION_JSON)
                  .build();
          } catch (APIManagementException e){
              //500
              e.printStackTrace();
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                  .entity("Internal server error")
                  .type(MediaType.APPLICATION_JSON)
                  .build();
          } finally {
              if (isTenantFlowStarted) {
                  PrivilegedCarbonContext.endTenantFlow();
              }
          }

          return Response.ok().entity(apiToReturn).build();
      }
  
      @Override
      public Response apisApiIdPut(String apiId,API body,String contentType,String ifMatch,String ifUnmodifiedSince)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisApiIdDocumentsGet(String limit,String offset,String query,String accept,String ifNoneMatch)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisApiIdDocumentsPost(Document body,String contentType)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisApiIdDocumentsDocumentIdGet(String accept,String ifNoneMatch,String ifModifiedSince)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisApiIdDocumentsDocumentIdPut(Document body,String contentType,String ifMatch,String ifUnmodifiedSince)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response apisApiIdDocumentsDocumentIdDelete(String ifMatch,String ifUnmodifiedSince)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
}
