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

package org.wso2.carbon.apimgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.API_YAML_FILE_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.DEPLOYMENT_ENVIRONMENTS_FILE_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.DEPLOYMENT_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.DISPLAY_ON_DEVPORTAL_OPTION;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.SWAGGER_YAML_FILE_NAME;
import static org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants.TYPE_DEPLOYMENT_ENVIRONMENTS;
import static org.wso2.carbon.apimgt.util.FederatedGatewayConstants.DISCOVERED_API_LIST;
import static org.wso2.carbon.apimgt.util.FederatedGatewayConstants.PUBLISHED_API_LIST;

public class FederatedGatewayUtil {
    private static Log log = LogFactory.getLog(FederatedGatewayUtil.class);

    /**
     * Initializes tenant flow using current organization context.
     */
    public static void startTenantFlow(String organization) throws APIManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        context.setTenantDomain(organization);
        String adminUsername = APIUtil.getAdminUsername();
        context.setUsername(adminUsername);
        context.setTenantId(APIUtil.getTenantId(adminUsername));
    }

    public static void deleteAPI(String apiUUID, String organization, Environment environment) {
        try {
            APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            provider.deleteAPIRevisions(apiUUID, organization);
            provider.deleteAPI(apiUUID, organization);
            log.debug("Deleted API: " + apiUUID + " organization: " + organization + " from environment: "
                    + environment.getName());
        } catch (APIManagementException e) {
            log.error("Error deleting API: " + apiUUID + " organization: " + organization, e);
        }
    }

    public static InputStream createZipAsInputStream(String apiYaml, String swaggerYaml, String deploymentYaml,
                                                     String zipName) throws IOException {
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
        byte[] data = content.getBytes();
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
        yamlRoot.put("version", "v4.3.0");
        yamlRoot.put("data", deploymentEnvData);

        Yaml yaml = new Yaml();
        return yaml.dump(yamlRoot);
    }

    public static Map<String, List<String>> getDiscoveredAPIsFromFederatedGateway(Environment environment, String organization,
                                                                                  String providerName) throws APIManagementException {
        GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
        Map<String, List<String>> apisDeployedInGateway = new HashMap<>();
        List<String> discoveredAPIs = new ArrayList<>();
        List<String> publishedAPIs = new ArrayList<>();
        APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(providerName);
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = gatewayArtifactsMgtDAO
                .retrieveGatewayArtifactsByLabel(new String[]{environment.getName()}, organization);
        for (APIRuntimeArtifactDto apiRuntimeArtifactDto : apiRuntimeArtifactDtoList) {
           API api =  provider.getAPIbyUUID(apiRuntimeArtifactDto.getApiId(), organization);
           if (api != null && !api.isInitiatedFromGateway()) {
                publishedAPIs.add(apiRuntimeArtifactDto.getName() + ":" + apiRuntimeArtifactDto.getVersion());
            } else {
               discoveredAPIs.add(apiRuntimeArtifactDto.getName() + ":" + apiRuntimeArtifactDto.getVersion());
           }
        }
        apisDeployedInGateway.put(DISCOVERED_API_LIST, discoveredAPIs);
        apisDeployedInGateway.put(PUBLISHED_API_LIST, publishedAPIs);
        return apisDeployedInGateway;
    }
}
