/*
 *
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.federated.gateway;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscovery;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscoveryService;
import org.wso2.carbon.apimgt.api.dto.ImportedAPIDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.api.model.DiscoveredAPI;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayMode;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.apimgt.impl.APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS;
import static org.wso2.carbon.apimgt.impl.APIConstants.DELEM_COLON;
import static org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil.fromAPItoDTO;
import static org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayConstants.DISCOVERED_API_LIST;
import static org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayConstants.PUBLISHED_API_LIST;

/**
 * This class is responsible for scheduling and executing the discovery of APIs in a federated gateway environment.
 * It uses a scheduled executor service to periodically discover APIs and process them accordingly.
 */
@Component(
        name = "apim.gateway.federation.service",
        service = FederatedAPIDiscoveryService.class,
        immediate = true
)
public class FederatedAPIDiscoveryRunner implements FederatedAPIDiscoveryService {
    private static Log log = LogFactory.getLog(FederatedAPIDiscoveryRunner.class);

    /**
     * Background scheduled discovery is disabled in favor of manual on-demand API discovery.
     * This method is kept as a no-op to maintain the interface.
     *
     * @param environment  The environment from which APIs will be discovered.
     * @param organization The organization context for the discovery.
     */
    @Override
    public void scheduleDiscovery(Environment environment, String organization) {
        if (log.isDebugEnabled()) {
            log.debug("Background scheduled discovery is disabled. scheduleDiscovery is a no-op for environment: " +
                    environment.getName());
        }
    }

    /**
     * Background scheduled discovery is disabled in favor of manual on-demand API discovery.
     * This method is kept as a no-op to maintain the interface.
     *
     * @param environment  The environment for which the discovery task should be stopped.
     * @param organization The organization context for which the discovery task should be stopped.
     */
    @Override
    public void stopDiscovery(Environment environment, String organization) {
        if (log.isDebugEnabled()) {
            log.debug("Background scheduled discovery is disabled. stopDiscovery is a no-op for environment: " +
                    environment.getName());
        }
    }

    public static void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Background scheduled discovery is disabled. shutdown is a no-op.");
        }
    }

    /**
     * Get the reference object for an existing API.
     *
     * @param environment The environment where the API is deployed.
     * @param apiResult   The API result object.
     * @return The reference object for the API.
     * @throws APIManagementException If an error occurs while retrieving the reference object.
     */
    private String getReferenceObjectForExistingAPIs(Environment environment, ApiResult apiResult)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving reference object for API ID: " + apiResult.getId());
        }
        return APIUtil.getApiExternalApiMappingReferenceByApiId(apiResult.getId(), environment.getUuid());
    }
     /**
  * Helper to initialize the correct Gateway Connector dynamically.
  */
 private FederatedAPIDiscovery getFederatedAPIDiscovery(Environment environment, String organization) throws APIManagementException {
     GatewayAgentConfiguration gatewayConfiguration = ServiceReferenceHolder.getInstance().
             getExternalGatewayConnectorConfiguration(environment.getGatewayType());
     if (gatewayConfiguration == null || StringUtils.isEmpty(gatewayConfiguration.getDiscoveryImplementation())) {
         throw new APIManagementException("No discovery implementation configured for gateway type: " + environment.getGatewayType());
     }
     try {
         FederatedAPIDiscovery federatedAPIDiscovery = (FederatedAPIDiscovery)
                 Class.forName(gatewayConfiguration.getDiscoveryImplementation())
                         .getDeclaredConstructor().newInstance();
         federatedAPIDiscovery.init(environment, organization);
         return federatedAPIDiscovery;
     } catch (Exception e) {
         throw new APIManagementException("Error initializing Federated API Discovery connector", e);
     }
 }
 public Map<String, List<DiscoveredAPI>> discoverExternalAPIs(Environment environment, String organization)
         throws APIManagementException {
     FederatedAPIDiscovery discovery = getFederatedAPIDiscovery(environment, organization);
     List<DiscoveredAPI> allAPIs = discovery.discoverAPI();
     List<DiscoveredAPI> newAPIs = new ArrayList<>();
     List<DiscoveredAPI> updatedAPIs = new ArrayList<>();
     Map<String, List<DiscoveredAPI>> categorizedApis = new HashMap<>();
     categorizedApis.put("NEW", newAPIs);
     categorizedApis.put("UPDATE", updatedAPIs);
     if (allAPIs == null || allAPIs.isEmpty()) {
         return categorizedApis;
     }
     try {
         String adminUsername = APIUtil.getTenantAdminUserName(organization);
         FederatedGatewayUtil.startTenantFlow(organization, adminUsername);
         Map<String, Map<String, ApiResult>> alreadyAvailableAPIs =
                 FederatedGatewayUtil.getDiscoveredAPIsFromFederatedGateway(environment, organization);
         List<String> alreadyDiscoveredAPIsList = new ArrayList<>(alreadyAvailableAPIs.get(DISCOVERED_API_LIST).keySet());
         List<String> publishedAPIsList = new ArrayList<>(alreadyAvailableAPIs.get(PUBLISHED_API_LIST).keySet());
         Map<String, ApiResult> allTrackedAPIs = new ConcurrentHashMap<>();
         allTrackedAPIs.putAll(alreadyAvailableAPIs.get(DISCOVERED_API_LIST));
         allTrackedAPIs.putAll(alreadyAvailableAPIs.get(PUBLISHED_API_LIST));
         
         List<String> discoveredAPIsFromFederatedGW = new ArrayList<>();
         
         for (DiscoveredAPI discoveredAPI : allAPIs) {
             if (discoveredAPI == null || discoveredAPI.getApi() == null) continue;
             
             String apiKey = discoveredAPI.getApi().getId().getApiName() + DELEM_COLON + discoveredAPI.getApi().getId().getVersion();
             String envScopedKey = discoveredAPI.getApi().getId().getApiName() + APIConstants.KEY_SEPARATOR
                     + environment.getName() + DELEM_COLON + discoveredAPI.getApi().getId().getVersion();
             
             // Track gateway APIs to identify deleted ones
             discoveredAPIsFromFederatedGW.add(apiKey);
             discoveredAPIsFromFederatedGW.add(envScopedKey);
             
             boolean isExists = alreadyDiscoveredAPIsList.contains(apiKey)
                 || alreadyDiscoveredAPIsList.contains(envScopedKey)
                 || publishedAPIsList.contains(apiKey)
                 || publishedAPIsList.contains(envScopedKey);
             if (!isExists) {
                 stripHeavyDefinition(discoveredAPI);
                 newAPIs.add(discoveredAPI);
                 continue;
             }
             String matchedKey = null;
             if (allTrackedAPIs.containsKey(envScopedKey)) matchedKey = envScopedKey;
             else if (allTrackedAPIs.containsKey(apiKey)) matchedKey = apiKey;
             if (matchedKey != null) {
                 ApiResult wso2ApiResult = allTrackedAPIs.get(matchedKey);
                 String existingReferenceArtifact = getReferenceObjectForExistingAPIs(environment, wso2ApiResult);
                 
                 if (discovery.isAPIUpdated(existingReferenceArtifact, discoveredAPI.getReferenceArtifact())) {
                     stripHeavyDefinition(discoveredAPI);
                     updatedAPIs.add(discoveredAPI);
                 }
             }
         }
         
         // Cleanup orphaned APIs (previously discovered but no longer on the gateway)
         for (String apiName : alreadyDiscoveredAPIsList) {
             if (!discoveredAPIsFromFederatedGW.contains(apiName)) {
                 try {
                     String apiUUID = FederatedGatewayUtil.getAPIUUID(apiName, adminUsername, organization);
                     if (apiUUID != null) {
                         FederatedGatewayUtil.deleteDeployment(apiUUID, organization, environment);
                         if (log.isDebugEnabled()) {
                             log.debug("Automatically cleaned up orphaned API deployment: " + apiName
                                     + " (UUID: " + apiUUID + ") from environment: " + environment.getName());
                         }
                     } else {
                         if (log.isDebugEnabled()) {
                             log.debug("API UUID not found for: " + apiName
                                     + ". Skipping removal from environment: " + environment.getName());
                         }
                     }
                 } catch (Exception e) {
                     log.error("Failed to delete revision for API: " + apiName + " from environment: "
                             + environment.getName(), e);
                 }
             }
         }
     } finally {
         PrivilegedCarbonContext.endTenantFlow();
     }
     return categorizedApis;
 }
 private void stripHeavyDefinition(DiscoveredAPI discoveredAPI) {
     if (discoveredAPI != null && discoveredAPI.getApi() != null) {
         discoveredAPI.getApi().setSwaggerDefinition(null);
     }
 }
  /**
   * Imports brand-new APIs from the external gateway into WSO2.
   * <p>
   * Calls discoverAPI() on the connector to get the full DiscoveredAPI list. Then filters for
   * the user-selected IDs and imports only those. Re-introduces API versioning logic.
   *
   * @param apiIds       the external gateway API identifiers selected by the user
   * @param environment  the federated environment
   * @param organization the organization
   * @throws APIManagementException if an error occurs during import
   */
  @Override
  public void importNewExternalAPIs(List<String> apiIds, Environment environment, String organization)
          throws APIManagementException {
      FederatedAPIDiscovery discovery = getFederatedAPIDiscovery(environment, organization);
      String adminUsername = APIUtil.getTenantAdminUserName(organization);
      FederatedGatewayUtil.startTenantFlow(organization, adminUsername);
      try {
          // Fetch the full list of DiscoveredAPIs from the connector.
          List<DiscoveredAPI> allDiscoveredAPIs = discovery.discoverAPI();
          // Build a lookup map: key the APIs by multiple identifiers so the UI can match by any of them.
          // Exclude name-only indexing to avoid conflicts between different versions.
          Map<String, DiscoveredAPI> apiLookup = new HashMap<>();
          for (DiscoveredAPI discovered : allDiscoveredAPIs) {
              API api = discovered.getApi();
              // Index by UUID (the gateway's native ID)
              if (api.getUuid() != null) {
                  apiLookup.put(api.getUuid(), discovered);
              }
              // Index by name:version (composite key fallback)
              apiLookup.put(api.getId().getApiName() + ":" + api.getId().getVersion(), discovered);
          }
          
          Map<String, Map<String, ApiResult>> alreadyAvailableAPIs =
                  FederatedGatewayUtil.getDiscoveredAPIsFromFederatedGateway(environment, organization);
          Map<String, ApiResult> alreadyDiscoveredAPIMap = alreadyAvailableAPIs.get(DISCOVERED_API_LIST);
          List<String> alreadyDiscoveredAPIsList = new ArrayList<>(alreadyDiscoveredAPIMap.keySet());
          
          ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
          for (String apiId : apiIds) {
              try {
                  DiscoveredAPI discoveredAPI = apiLookup.get(apiId);
                  if (discoveredAPI == null) {
                      log.error("Could not find discovered API matching ID: " + apiId + ". Skipping.");
                      continue;
                  }
                  API api = discoveredAPI.getApi();
                  APIDTO apidto = fromAPItoDTO(api);
                  if (apidto.getPolicies() == null || apidto.getPolicies().isEmpty()) {
                      apidto.setPolicies(Collections.singletonList(DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS));
                  }

                  String apiKey = apidto.getName() + DELEM_COLON + apidto.getVersion();
                  String envScopedKey = apidto.getName() + APIConstants.KEY_SEPARATOR
                          + environment.getName() + DELEM_COLON + apidto.getVersion();

                  boolean update = false;
                  boolean isNewVersion = false;
                  String existingAPI = null;
                  boolean alreadyExistsWithEnvScope = alreadyDiscoveredAPIsList.contains(envScopedKey);

                  if (!alreadyDiscoveredAPIsList.contains(apiKey) && !alreadyExistsWithEnvScope) {
                      String envPathName = apidto.getName() + APIConstants.KEY_SEPARATOR
                              + environment.getName();
                      Optional<String> existingApiOpt = alreadyDiscoveredAPIsList.stream()
                              .map(String::trim)
                              .map(s -> {
                                  int idx = s.lastIndexOf(DELEM_COLON);
                                  if (idx <= 0 || idx >= s.length() - 1) return null;
                                  String name = s.substring(0, idx);
                                  String version = s.substring(idx + 1);
                                  return new String[]{name, version};
                              })
                              .filter(Objects::nonNull)
                              .filter(parts -> (parts[0].equals(apidto.getName())
                                      || parts[0].equals(envPathName))
                                      && !parts[1].equals(apidto.getVersion()))
                              .map(parts -> parts[0] + DELEM_COLON + parts[1])
                              .findFirst();
                      isNewVersion = existingApiOpt.isPresent();
                      existingAPI = existingApiOpt.orElse(null);
                  }

                  if (alreadyExistsWithEnvScope) {
                      if (api.getDisplayName() == null) {
                          apidto.displayName(apidto.getName());
                      }
                      apidto.setName(apidto.getName() + APIConstants.KEY_SEPARATOR + environment.getName());
                  }

                  API newAPI = null;
                  if (isNewVersion) {
                      String existingApiUUID = FederatedGatewayUtil.getAPIUUID(existingAPI, adminUsername,
                              organization);
                      if (existingApiUUID != null) {
                          newAPI = FederatedGatewayUtil.createNewAPIVersion(existingApiUUID, apidto.getVersion(),
                                  organization);
                          update = true;
                      }
                  }

                  // Build deployment ZIP
                  JsonObject apiJson = (JsonObject) new Gson().toJsonTree(apidto);
                  apiJson = CommonUtil.addTypeAndVersionToFile(ImportExportConstants.TYPE_API,
                          ImportExportConstants.APIM_VERSION, apiJson);
                  InputStream apiZip = FederatedGatewayUtil.createZipAsInputStream(
                          apiJson.toString(), api.getSwaggerDefinition(),
                          FederatedGatewayUtil.createDeploymentYaml(environment),
                          apidto.getName());

                  // Import API
                  ImportedAPIDTO importedApi = importExportAPI.importAPI(apiZip, false,
                          true, update, true,
                          new String[]{APIConstants.APIM_PUBLISHER_SCOPE, APIConstants.APIM_CREATOR_SCOPE},
                          organization);

                  // Record the mapping using the CONNECTOR'S OWN reference artifact
                  if (update) {
                      if (newAPI != null) {
                          APIUtil.addApiExternalApiMapping(newAPI.getUuid(),
                                  environment.getUuid(), discoveredAPI.getReferenceArtifact());
                      } else {
                          APIUtil.updateApiExternalApiMapping(importedApi.getApi().getUuid(),
                                  environment.getUuid(), discoveredAPI.getReferenceArtifact());
                      }
                  } else {
                      APIUtil.addApiExternalApiMapping(importedApi.getApi().getUuid(),
                              environment.getUuid(), discoveredAPI.getReferenceArtifact());
                  }
                  log.info("Successfully imported new API: " + api.getId().getApiName()
                          + " from environment: " + environment.getName());
              } catch (Exception e) {
                  log.error("Error importing API with ID: " + apiId
                          + " from environment: " + environment.getName(), e);
              }
          }
      } finally {
          PrivilegedCarbonContext.endTenantFlow();
      }
  }

  /**
   * Updates existing WSO2 APIs whose definitions have changed on the external gateway.
   *
   * @param apiIds       the external gateway API identifiers selected by the user
   * @param environment  the federated environment
   * @param organization the organization
   * @throws APIManagementException if an error occurs during update
   */
  @Override
  public void updateExternalAPIs(List<String> apiIds, Environment environment, String organization)
          throws APIManagementException {
      FederatedAPIDiscovery discovery = getFederatedAPIDiscovery(environment, organization);
      String adminUsername = APIUtil.getTenantAdminUserName(organization);
      FederatedGatewayUtil.startTenantFlow(organization, adminUsername);
      try {
          List<DiscoveredAPI> allDiscoveredAPIs = discovery.discoverAPI();
          Map<String, DiscoveredAPI> apiLookup = new HashMap<>();
          for (DiscoveredAPI discovered : allDiscoveredAPIs) {
              API api = discovered.getApi();
              if (api.getUuid() != null) {
                  apiLookup.put(api.getUuid(), discovered);
              }
              apiLookup.put(api.getId().getApiName() + ":" + api.getId().getVersion(), discovered);
          }
          ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
          for (String apiId : apiIds) {
              try {
                  DiscoveredAPI discoveredAPI = apiLookup.get(apiId);
                  if (discoveredAPI == null) {
                      log.error("Could not find discovered API matching ID: " + apiId + ". Skipping.");
                      continue;
                  }
                  API api = discoveredAPI.getApi();
                  APIDTO apidto = fromAPItoDTO(api);
                  if (apidto.getPolicies() == null || apidto.getPolicies().isEmpty()) {
                      apidto.setPolicies(Collections.singletonList(DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS));
                  }
                  // Build deployment ZIP
                  JsonObject apiJson = (JsonObject) new Gson().toJsonTree(apidto);
                  apiJson = CommonUtil.addTypeAndVersionToFile(ImportExportConstants.TYPE_API,
                          ImportExportConstants.APIM_VERSION, apiJson);
                  InputStream apiZip = FederatedGatewayUtil.createZipAsInputStream(
                          apiJson.toString(), api.getSwaggerDefinition(),
                          FederatedGatewayUtil.createDeploymentYaml(environment),
                          apidto.getName());
                  // Import as UPDATE (update=true)
                  ImportedAPIDTO importedApi = importExportAPI.importAPI(apiZip, false,
                          true, true, true,
                          new String[]{APIConstants.APIM_PUBLISHER_SCOPE, APIConstants.APIM_CREATOR_SCOPE},
                          organization);
                  // Update the mapping using the CONNECTOR'S OWN reference artifact
                  APIUtil.updateApiExternalApiMapping(importedApi.getApi().getUuid(),
                        environment.getUuid(), discoveredAPI.getReferenceArtifact());
                  log.info("Successfully updated API: " + api.getId().getApiName()
                          + " from environment: " + environment.getName());
              } catch (Exception e) {
                  log.error("Error updating API with ID: " + apiId
                          + " from environment: " + environment.getName(), e);
              }
          }
      } finally {
          PrivilegedCarbonContext.endTenantFlow();
      }
  }
}
