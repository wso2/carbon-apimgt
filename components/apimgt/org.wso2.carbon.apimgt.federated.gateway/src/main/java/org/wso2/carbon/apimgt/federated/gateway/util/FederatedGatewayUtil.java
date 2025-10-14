/*
 *
 * Copyright (c) WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.apimgt.federated.gateway.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.wso2.carbon.apimgt.impl.APIConstants.DELEM_COLON;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.API_YAML_FILE_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.DEPLOYMENT_ENVIRONMENTS_FILE_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.DEPLOYMENT_ENVIRONMENT_VERSION;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.DEPLOYMENT_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.DISPLAY_ON_DEVPORTAL_OPTION;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.SWAGGER_YAML_FILE_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.TYPE_DEPLOYMENT_ENVIRONMENTS;
import static org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayConstants.DISCOVERED_API_LIST;
import static org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayConstants.PUBLISHED_API_LIST;

public class FederatedGatewayUtil {
    private static Log log = LogFactory.getLog(FederatedGatewayUtil.class);

    /**
     * Initializes tenant flow using current organization context.
     */
    public static void startTenantFlow(String organization, String adminUsername) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Starting tenant flow for organization: " + organization);
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        context.setTenantDomain(organization);
        context.setUsername(adminUsername);
        context.setTenantId(APIUtil.getTenantId(adminUsername));
        if (log.isDebugEnabled()) {
            log.debug("Started tenant flow for organization: " + organization);
        }
    }

    public static void deleteDeployment(String apiUUID, String organization, Environment environment) {
        try {
            APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            provider.deleteAPIRevisions(apiUUID, organization, true);
            log.debug("Deleted Revision for: " + apiUUID + " organization: " + organization + " from environment: "
                    + environment.getName());
        } catch (APIManagementException e) {
            log.error("Error deleting Revision for API: " + apiUUID + " organization: " + organization, e);
        }
    }

    public static API createNewAPIVersion(String apiUUID, String newVersion, String organization)
            throws APIManagementException {
        if (Objects.isNull(newVersion) || newVersion.trim().isEmpty() ) {
            throw new APIManagementException("Invalid new API version format: " + newVersion + " for API: " + apiUUID);
        }
        APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                getThreadLocalCarbonContext().getUsername());
        if (log.isDebugEnabled()) {
            log.debug("Created new API version for: " + apiUUID + " in organization: " + organization);
        }
        return provider.createNewAPIVersion(apiUUID, newVersion, true, organization);
    }

    public static InputStream createZipAsInputStream(String apiYaml, String swaggerYaml, String deploymentYaml,
                                                     String zipName) throws IOException {
        if (apiYaml == null || swaggerYaml == null || deploymentYaml == null) {
            throw new IllegalArgumentException("Input parameters cannot be null for API: " + zipName);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {

            // Add api.yaml
            addToZip(zos, zipName + "/" + API_YAML_FILE_NAME, apiYaml);

            // Add Definitions/swagger.yaml
            addToZip(zos, zipName + "/" + SWAGGER_YAML_FILE_NAME, swaggerYaml);

            // Add deployment_environments.yaml
            addToZip(zos, zipName + "/" + DEPLOYMENT_ENVIRONMENTS_FILE_NAME, deploymentYaml);
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private static void addToZip(ZipOutputStream zos, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zos.putNextEntry(entry);
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        zos.write(data, 0, data.length);
        zos.closeEntry();
    }

    public static String createDeploymentYaml(Environment environment) {
        List<Map<String, String>> deploymentEnvData = new ArrayList<>();
        Map<String, String> envEntry = new LinkedHashMap<>();
        envEntry.put(DISPLAY_ON_DEVPORTAL_OPTION, "true");
        envEntry.put(DEPLOYMENT_NAME, environment.getName());
        deploymentEnvData.add(envEntry);

        Map<String, Object> yamlRoot = new LinkedHashMap<>();
        yamlRoot.put("type", TYPE_DEPLOYMENT_ENVIRONMENTS);
        yamlRoot.put("version", DEPLOYMENT_ENVIRONMENT_VERSION);
        yamlRoot.put("data", deploymentEnvData);

        Yaml yaml = new Yaml();
        return yaml.dump(yamlRoot);
    }

    /**
     * Retrieves the UUID of an API based on its name, the admin username, and the organization.
     *
     * @param apiName       The name of the API in the format "APIName:APIVersion".
     * @param adminUsername The username of the admin who owns the API.
     * @param organization  The name of the organization to which the API belongs.
     * @return The UUID of the API as a String.
     * @throws APIManagementException If the API name format is invalid or an error occurs while retrieving the UUID.
     */
    public static String getAPIUUID(String apiName, String adminUsername, String organization)
            throws APIManagementException {
        String[] parts = apiName.split(DELEM_COLON, apiName.lastIndexOf(DELEM_COLON));
        if (parts.length != 2) {
            throw new APIManagementException("Invalid API identifier format: " + apiName);
        }

        APIIdentifier apiIdentifier = new APIIdentifier(adminUsername, parts[0], parts[1]);
        String uuid = APIUtil.getUUIDFromIdentifier(apiIdentifier, organization);
        if (log.isDebugEnabled()) {
            log.debug("Retrieved UUID: " + uuid + " for API: " + apiName + " in organization: " + organization);
        }
        return uuid;
    }

    /**
     * Retrieves a map of discovered and published APIs from a specified federated gateway environment
     * for the given organization.
     *
     * @param environment The federated gateway environment from which the API data should be retrieved.
     * @param organization The organization for which the API data is to be retrieved.
     * @return A map containing two entries:
     *         - "DISCOVERED_API_LIST" mapped to a map of discovered APIs.
     *         - "PUBLISHED_API_LIST" mapped to a map of published APIs.
     *         Each inner map is structured as a mapping of API identifiers to their corresponding {@code ApiResult} objects.
     * @throws APIManagementException If an error occurs while fetching the API data from the gateway.
     */
    public static Map<String, Map<String, ApiResult>> getDiscoveredAPIsFromFederatedGateway(
            Environment environment, String organization) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving discovered and published APIs from environment: " + environment.getName()
                    + " in organization: " + organization);
        }
        Map<String, Map<String, ApiResult>> apisDeployedInGateway = new HashMap<>(2);

        apisDeployedInGateway.put(DISCOVERED_API_LIST,
                buildApiMap(APIUtil.getAPIsDeployedInGatewayEnvironmentByOrg(environment.getName(), organization, true)));

        apisDeployedInGateway.put(PUBLISHED_API_LIST,
                buildApiMap(APIUtil.getAPIsDeployedInGatewayEnvironmentByOrg(environment.getName(), organization, false)));

        return apisDeployedInGateway;
    }

    /**
     * Builds a map of API identifiers to their corresponding {@code ApiResult} objects
     * from the provided list of {@code ApiResult}.
     * The API identifier is constructed as a combination of the API's name and version
     * separated by a colon delimiter.
     *
     * @param apiResults A list of {@code ApiResult} objects from which the map is to be created.
     * @return A map with keys as concatenated API identifiers (name + ":" + version)
     *         and values as the corresponding {@code ApiResult} objects.
     */
    private static Map<String, ApiResult> buildApiMap(List<ApiResult> apiResults) {
        return apiResults.stream()
                .collect(Collectors.toMap(
                        api -> api.getName() + DELEM_COLON + api.getVersion(),
                        Function.identity()));
    }

}
