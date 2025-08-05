/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscovery;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscoveryService;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayMode;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.util.FederatedGatewayUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil.fromAPItoDTO;
import static org.wso2.carbon.apimgt.util.FederatedGatewayConstants.DISCOVERED_API_LIST;
import static org.wso2.carbon.apimgt.util.FederatedGatewayConstants.PUBLISHED_API_LIST;

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
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    static Map<String, ScheduledFuture<?>> scheduledDiscoveryTasks = new ConcurrentHashMap<>();
    private static Log log = LogFactory.getLog(FederatedAPIDiscoveryRunner.class);

    /**
     * Schedules the discovery of APIs at a specified interval.
     *
     * @param environment    The environment from which APIs will be discovered.
     */
    public void scheduleDiscovery(Environment environment, String organization) {
        GatewayAgentConfiguration gatewayConfiguration = org.wso2.carbon.apimgt.impl.internal.
                ServiceReferenceHolder.getInstance().
                getExternalGatewayConnectorConfiguration(environment.getGatewayType());
        if (gatewayConfiguration != null && gatewayConfiguration.getDiscoveryImplementation() != null) {
            FederatedAPIDiscovery federatedAPIDiscovery;
            if (StringUtils.isNotEmpty(gatewayConfiguration.getDiscoveryImplementation())) {
                try {
                    federatedAPIDiscovery = (FederatedAPIDiscovery)
                            Class.forName(gatewayConfiguration.getDiscoveryImplementation())
                                    .getDeclaredConstructor().newInstance();
                    federatedAPIDiscovery.init(environment, organization);
                    ScheduledFuture<?> scheduledFuture = scheduledDiscoveryTasks
                            .get(environment.getName() + organization);
                    if (environment.getMode().equals(GatewayMode.WRITE_ONLY.getMode())) {
                        log.info("Federated API discovery is disabled for environment: " + environment.getName());

                        if (scheduledFuture != null) {
                            scheduledFuture.cancel(true);
                        }
                    } else {
                        if (scheduledFuture != null) {
                            scheduledFuture.cancel(true);
                        }
                        executor.scheduleAtFixedRate(() -> {
                            try {
                                List<API> discoveredAPIs = federatedAPIDiscovery.discoverAPI();
                                if (discoveredAPIs != null && !discoveredAPIs.isEmpty()) {
                                    processDiscoveredAPIs(discoveredAPIs, environment, organization);
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("No APIs discovered in environment: " + environment.getName());
                                    }
                                }
                            } catch (Exception e) {
                                log.error("Error during federated API discovery for environment: "
                                        + environment.getName(), e);
                            }
                        }, 0, environment.getApiDiscoveryScheduledWindow(), TimeUnit.MINUTES);
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                         NoSuchMethodException | InvocationTargetException | APIManagementException e) {
                    log.error("Error while loading federated API discovery for environment "
                            + environment.getName(), e);
                } catch (Throwable e) {
                    log.error("Unexpected error while initializing federated API discovery for environment "
                            + environment.getName(), e);
                }
            }
        }
    }

    private void processDiscoveredAPIs(List<API> apisToDeployInGatewayEnv, Environment environment,
                                      String organization) {

        boolean debugLogEnabled = log.isDebugEnabled();
        try {
            FederatedGatewayUtil.startTenantFlow(organization);
            String adminUsername = APIUtil.getAdminUsername();

            Map<String, List<String>> alreadyAvailableAPIs =
                    FederatedGatewayUtil.getDiscoveredAPIsFromFederatedGateway(environment, organization,
                            adminUsername);
            List<String> discoveredAPIsFromFederatedGW = new ArrayList<>();
            List<String> alreadyDiscoveredAPIsList = alreadyAvailableAPIs.get(DISCOVERED_API_LIST);

            for (API api : apisToDeployInGatewayEnv) {
                APIDTO apidto = fromAPItoDTO(api);
                if (api == null) {
                    continue;
                }
                try {
                    String apiKey = apidto.getName() + APIConstants.DELEM_COLON + apidto.getVersion();
                    String envScopedKey = apidto.getName() + APIConstants.DELEM_UNDERSCORE
                            + environment.getName() + APIConstants.DELEM_COLON + apidto.getVersion();

                    // Determine import mode
                    boolean isPublishedAPIFromCP = alreadyAvailableAPIs.get(PUBLISHED_API_LIST).contains(apiKey) ||
                            alreadyAvailableAPIs.get(PUBLISHED_API_LIST).contains(envScopedKey);
                    boolean update = alreadyDiscoveredAPIsList.contains(apiKey) ||
                            alreadyDiscoveredAPIsList.contains(envScopedKey);
                    boolean alreadyExistsWithEnvScope = alreadyDiscoveredAPIsList.contains(envScopedKey);

                    // Adjust name if needed
                    if (alreadyExistsWithEnvScope) {
                        if (api.getDisplayName() == null) {
                            apidto.displayName(apidto.getName());
                        }
                        apidto.setName(apidto.getName() + APIConstants.DELEM_UNDERSCORE + environment.getName());
                    }

                    // Map to DTO and create ZIP
                    JsonObject apiJson = (JsonObject) new Gson().toJsonTree(apidto);
                    apiJson = CommonUtil.addTypeAndVersionToFile(ImportExportConstants.TYPE_API,
                            ImportExportConstants.APIM_VERSION, apiJson);
                    InputStream apiZip = FederatedGatewayUtil.createZipAsInputStream(
                            apiJson.toString(), api.getSwaggerDefinition(),
                            FederatedGatewayUtil.createDeploymentYaml(environment),
                            apidto.getName());

                    ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();


                    // Import API
                    importExportAPI.importAPI(apiZip, true, true, update, true,
                            new String[]{APIConstants.APIM_PUBLISHER_SCOPE, APIConstants.APIM_CREATOR_SCOPE},
                            organization);

                    // Track deployed
                    discoveredAPIsFromFederatedGW.add(alreadyExistsWithEnvScope ? envScopedKey : apiKey);
                    if (!update) {
                        alreadyDiscoveredAPIsList.add(apidto.getName() + APIConstants.DELEM_COLON
                                + apidto.getVersion());
                    }

                    log.info((update ? "Updated" : "Created") + " API: " + api.getId().getName()
                            + " in environment: " + environment.getName());

                } catch (IOException e) {
                    log.error("IO error while processing API: " + api.getId().getName()
                            + " in organization: " + organization, e);
                } catch (APIManagementException e) {
                    log.error("API management error while processing API: " + api.getId().getName()
                            + " in organization: " + organization, e);
                }
            }

            for (String apiName : alreadyDiscoveredAPIsList) {
                if (!discoveredAPIsFromFederatedGW.contains(apiName)) {
                    try {
                        String[] parts = apiName.split(APIConstants.DELEM_COLON);
                        if (parts.length < 2) {
                            log.warn("Invalid API identifier format for: " + apiName);
                            continue;
                        }

                        APIIdentifier apiIdentifier = new APIIdentifier(adminUsername, parts[0], parts[1]);
                        String apiUUID = APIUtil.getUUIDFromIdentifier(apiIdentifier, organization);

                        if (apiUUID != null) {
                            FederatedGatewayUtil.deleteAPI(apiUUID, organization, environment);
                            if (debugLogEnabled) {
                                log.debug("Removed API: " + apiName + " from environment: " + environment.getName());
                            }
                        } else {
                            if (debugLogEnabled) {
                                log.debug("API UUID not found for: " + apiName
                                        + ". Skipping removal from environment: " + environment.getName());
                            }
                        }

                    } catch (Exception e) {
                        log.error("Failed to delete API: " + apiName + " from environment: "
                                + environment.getName(), e);
                    }
                }
            }

        } catch (APIManagementException e) {
            throw new RuntimeException("Failed during federated API processing", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static void shutdown() {
        scheduledDiscoveryTasks.values().forEach(f -> f.cancel(false));
        executor.shutdown();
    }
}
