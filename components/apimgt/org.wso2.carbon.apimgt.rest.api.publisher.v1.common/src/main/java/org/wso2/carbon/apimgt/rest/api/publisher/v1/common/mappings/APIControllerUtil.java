/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIAndAPIProductCommonUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointSecurityDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class APIControllerUtil {

    private static final Log log = LogFactory.getLog(APIAndAPIProductCommonUtil.class);

    /**
     * Method will check the archive and extract environment related params
     *
     * @param pathToArchive String of the archive project
     * @return JsonObject of environment parameters
     * @throws IOException
     */
    public static JsonObject resolveAPIControllerEnvParams(String pathToArchive) throws IOException {

        String jsonParamsContent;
        jsonParamsContent = getParamsDefinitionAsJSON(pathToArchive);
        if (StringUtils.isEmpty(jsonParamsContent)) {
            return null;
        }
        JsonElement paramsElement = new JsonParser().parse(jsonParamsContent);
        return paramsElement.getAsJsonObject();
    }

    /**
     * Retrieve API params file as JSON.
     *
     * @param pathToArchive Path to API or API Product archive
     * @return String JsonString of environment parameters
     * @throws IOException If an error occurs while reading the file
     */
    public static String getParamsDefinitionAsJSON(String pathToArchive) throws IOException {

        String jsonContent = null;
        String pathToYamlFile = pathToArchive + ImportExportConstants.YAML_API_PARAMS_FILE_LOCATION;
        String pathToJsonFile = pathToArchive + ImportExportConstants.JSON_API_PARAMS_FILE_LOCATION;

        // load yaml representation first,if it is present
        if (CommonUtil.checkFileExistence(pathToYamlFile)) {
            if (log.isDebugEnabled()) {
                log.debug("Found api params definition file " + pathToYamlFile);
            }
            String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
            if (StringUtils.isNotEmpty(yamlContent)) {
                jsonContent = CommonUtil.yamlToJson(yamlContent);
            }
        } else if (CommonUtil.checkFileExistence(pathToJsonFile)) {
            // load as a json fallback
            if (log.isDebugEnabled()) {
                log.debug("Found api params definition file " + pathToJsonFile);
            }
            jsonContent = FileUtils.readFileToString(new File(pathToJsonFile));
        }
        return jsonContent;
    }

    /**
     * This method will be used to add Extracted environment parameters to the imported Api object
     *
     * @param pathToArchive  Path to API or API Product archive
     * @param importedApiDto APIDTO object to be imported
     * @param envParams      Env params object with required parameters
     * @return API Updated API Object
     * @throws APIManagementException If an error occurs merging env parameters with api
     */
    public static APIDTO injectEnvParamsToAPI(APIDTO importedApiDto, JsonObject envParams, String pathToArchive)
            throws APIManagementException {

        if (envParams == null || envParams.isJsonNull()) {
            return importedApiDto;
        }

        API importedApi = APIMappingUtil.fromDTOtoAPI(importedApiDto, importedApiDto.getProvider());

        // if endpointType field is not specified in the api_params.yaml, it will be considered as HTTP/REST
        JsonElement endpointTypeElement = envParams.get(ImportExportConstants.ENDPOINT_TYPE_FIELD);
        String endpointType = null;
        if (endpointTypeElement != null) {
            endpointType = endpointTypeElement.getAsString();
        } else {
            endpointType = ImportExportConstants.REST_TYPE_ENDPOINT;
        }

        //Handle multiple end points
        JsonObject jsonObject = setupMultipleEndpoints(envParams, endpointType);
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> endpointConfig;
        try {
            endpointConfig = mapper.readValue(jsonObject.toString(), HashMap.class);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while reading endpointConfig information in the api_params.yaml.";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }
        importedApiDto.setEndpointConfig(endpointConfig);

        //handle gateway environments
        if (envParams.get(ImportExportConstants.GATEWAY_ENVIRONMENTS_FIELD) != null) {
            List<String> environments = setupGatewayEnvironments(
                    envParams.get(ImportExportConstants.GATEWAY_ENVIRONMENTS_FIELD).getAsJsonArray());
            importedApiDto.setGatewayEnvironments(environments);
        }

        //handle mutualSSL certificates
        JsonElement clientCertificates = envParams.get(ImportExportConstants.MUTUAL_SSL_CERTIFICATES_FIELD);
        if (clientCertificates != null) {
            try {
                List<String> apiSecurity = importedApiDto.getSecurityScheme();
                if (!apiSecurity.isEmpty()) {
                    if (!apiSecurity.contains(ImportExportConstants.MUTUAL_SSL_ENABLED)) {
                        // if the apiSecurity field does not have mutualssl type, append it
                        apiSecurity.add(ImportExportConstants.MUTUAL_SSL_ENABLED);
                    }
                } else {
                    // if the apiSecurity field is empty, assign the value as "mutualssl"
                    apiSecurity.add(ImportExportConstants.MUTUAL_SSL_ENABLED);
                }
                importedApiDto.securityScheme(apiSecurity);
                String jsonString = clientCertificates.toString();
                handleClientCertificates(new JsonParser().parse(jsonString).getAsJsonArray(), importedApi.getId(),
                        pathToArchive);
            } catch (IOException e) {
                //Error is logged and when generating certificate details and certs in the archive
                String errorMessage = "Error while generating meta information of client certificates from path.";
                throw new APIManagementException(errorMessage, e, ExceptionCodes.ERROR_READING_PARAMS_FILE);
            }
        }

        //handle endpoint certificates
        JsonElement endpointCertificates = envParams.get(ImportExportConstants.ENDPOINT_CERTIFICATES_FIELD);
        if (endpointCertificates != null) {
            try {
                String jsonString = endpointCertificates.toString();
                handleEndpointCertificates(new JsonParser().parse(jsonString).getAsJsonArray(), pathToArchive);
            } catch (IOException e) {
                //Error is logged and when generating certificate details and certs in the archive
                String errorMessage = "Error while generating meta information of client certificates from path.";
                throw new APIManagementException(errorMessage, e, ExceptionCodes.ERROR_READING_PARAMS_FILE);
            }
        }

        //handle security configs
        JsonElement securityConfigs = envParams.get(ImportExportConstants.ENDPOINT_SECURITY_FIELD);
        if (securityConfigs != null) {
            handleEndpointSecurityConfigs(envParams, importedApiDto);
        }

        // handle available subscription policies
        JsonElement policies = envParams.get(ImportExportConstants.POLICIES_FIELD);
        if (!policies.isJsonNull()) {
            handleSubscriptionPolicies(policies, importedApiDto);
        }
        return importedApiDto;
    }

    /**
     * This method will be used to add Endpoint security related environment parameters to imported Api object
     *
     * @param importedApiDto APIDTO object to be updated
     * @param envParams      Env params object with required parameters
     * @throws APIManagementException If an error occurs when setting security env parameters
     */
    private static void handleEndpointSecurityConfigs(JsonObject envParams, APIDTO importedApiDto)
            throws APIManagementException {
        // If the user has set (either true or false) the enabled field under security in api_params.yaml,
        // the following code should be executed.
        JsonObject security = envParams.getAsJsonObject(ImportExportConstants.ENDPOINT_SECURITY_FIELD);
        if (security == null) {
            return;
        }
        String securityEnabled = security.get(ImportExportConstants.ENDPOINT_SECURITY_ENABLED).getAsString();
        boolean isSecurityEnabled = Boolean.parseBoolean(securityEnabled);
        //set endpoint security details to API
        APIEndpointSecurityDTO apiEndpointSecurityDTO = new APIEndpointSecurityDTO();

        // If endpoint security is enabled
        if (isSecurityEnabled) {
            // Check whether the username, password and type fields have set in api_params.yaml
            JsonElement username = security.get(ImportExportConstants.ENDPOINT_UT_USERNAME);
            JsonElement password = security.get(ImportExportConstants.ENDPOINT_UT_PASSWORD);
            JsonElement type = security.get(ImportExportConstants.ENDPOINT_SECURITY_TYPE);

            if (username == null) {
                throw new APIManagementException("You have enabled endpoint security but the username is not found "
                        + "in the api_params.yaml. Please specify username field for and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else if (password == null) {
                throw new APIManagementException("You have enabled endpoint security but the password is not found "
                        + "in the api_params.yaml. Please specify password field for and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else if (type == null) {
                throw new APIManagementException("You have enabled endpoint security but the password is not found "
                        + "in the api_params.yaml. Please specify password field for and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else {
                apiEndpointSecurityDTO.setPassword(password.toString());
                apiEndpointSecurityDTO.setUsername(username.getAsString());
                //setup security type (basic or digest)
                if (StringUtils.equals(type.getAsString(), ImportExportConstants.ENDPOINT_DIGEST_SECURITY_TYPE)) {
                    apiEndpointSecurityDTO.setType(APIEndpointSecurityDTO.TypeEnum.DIGEST);
                } else if (StringUtils.equals(type.getAsString(), ImportExportConstants.ENDPOINT_BASIC_SECURITY_TYPE)) {
                    apiEndpointSecurityDTO.setType(APIEndpointSecurityDTO.TypeEnum.BASIC);
                } else {
                    // If the type is not either basic or digest, return an error
                    throw new APIManagementException("Invalid endpoint security type found in the api_params.yaml. "
                            + "Should be either basic or digest"
                            + "Please specify correct security types field for and continue...",
                            ExceptionCodes.ERROR_READING_PARAMS_FILE);
                }
            }
            importedApiDto.setEndpointSecurity(apiEndpointSecurityDTO);
        }
    }

    /**
     * This method will add the defined available subscription policies in an environment to the particular imported API
     *
     * @param importedApiDto API DTO object to be updated
     * @param policies       policies with the values
     * @throws APIManagementException If an error occurs when setting policies
     */
    private static void handleSubscriptionPolicies(JsonElement policies, APIDTO importedApiDto) {
        JsonArray definedPolicies = policies.getAsJsonArray();
        List<String> policiesListToAdd = new ArrayList<>();
        for (JsonElement definedPolicy : definedPolicies) {
            if (!definedPolicy.isJsonNull()) {
                String policyToAdd = definedPolicy.getAsString();
                if (!StringUtils.isEmpty(policyToAdd)) {
                    policiesListToAdd.add(definedPolicy.getAsString());
                }
            }
        }
        // If the policies are not defined in api_params.yaml, the values in the api.yaml should be considered.
        // Hence, this if statement will prevent setting the policies in api.yaml to an empty array if the policies
        // are not properly defined in the api_params.yaml
        if (policiesListToAdd.size() > 0) {
            importedApiDto.setPolicies(policiesListToAdd);
        }
    }

    /**
     * This method will be used to add gateway environments to imported Api object
     *
     * @param gatewayEnvironments Json array of gateway environments extracted from env params file
     * @return Gateway Environment list
     */
    private static List<String> setupGatewayEnvironments(JsonArray gatewayEnvironments) {

        List<String> environments = new ArrayList<>();
        for (int i = 0; i < gatewayEnvironments.size(); i++) {
            environments.add(gatewayEnvironments.get(i).getAsString());
        }
        return environments;
    }

    /**
     * This method will be used to extract endpoint configurations from the env params file
     *
     * @param endpointType Endpoint type
     * @param envParams    JsonObject of Env params  with required parameters
     * @return JsonObject with Endpoint configs
     * @throws APIManagementException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject setupMultipleEndpoints(JsonObject envParams, String endpointType)
            throws APIManagementException {

        //default production and sandbox endpoints
        JsonObject defaultProductionEndpoint = new JsonObject();
        defaultProductionEndpoint
                .addProperty(ImportExportConstants.ENDPOINT_URL, ImportExportConstants.DEFAULT_PRODUCTION_ENDPOINT_URL);
        JsonObject defaultSandboxEndpoint = new JsonObject();
        defaultSandboxEndpoint
                .addProperty(ImportExportConstants.ENDPOINT_URL, ImportExportConstants.DEFAULT_SANDBOX_ENDPOINT_URL);

        JsonObject multipleEndpointsConfig = null;
        String routingPolicy = null;

        // if the endpoint routing policy or the endpoints field is not specified and
        // if the endpoint type is AWS or Dynamic
        if (envParams.get(ImportExportConstants.ROUTING_POLICY_FIELD) != null) {
            routingPolicy = envParams.get(ImportExportConstants.ROUTING_POLICY_FIELD).getAsString();
        }

        if (StringUtils.isEmpty(routingPolicy)) {
            multipleEndpointsConfig = handleDynamicAndAwsEndpoints(envParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint, endpointType);
        }

        // if endpoint type is HTTP/REST
        if (StringUtils.equals(endpointType, ImportExportConstants.HTTP_TYPE_ENDPOINT) || StringUtils
                .equals(endpointType, ImportExportConstants.REST_TYPE_ENDPOINT)) {
            //add REST endpoint configs as endpoint configs
            multipleEndpointsConfig = handleRestEndpoints(routingPolicy, envParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);
        }

        // if endpoint type is HTTP/SOAP
        if (ImportExportConstants.SOAP_TYPE_ENDPOINT.equals(endpointType)) {
            //add SOAP endpoint configs as endpoint configs
            multipleEndpointsConfig = handleSoapEndpoints(routingPolicy, envParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);
        }
        return multipleEndpointsConfig;
    }

    /**
     * This method will handle the Dynamic and AWS endpoint configs
     *
     * @param envParams                 Json object of Env parameters
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     * @param endpointType              String of endpoint type
     * @return JsonObject with Dynamic or AWS Endpoint configs
     * @throws APIManagementException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject handleDynamicAndAwsEndpoints(JsonObject envParams, JsonObject defaultProductionEndpoint,
            JsonObject defaultSandboxEndpoint, String endpointType) throws APIManagementException {
        JsonObject endpointsObject = null;
        if (envParams.get(ImportExportConstants.ENDPOINTS_FIELD) != null) {
            endpointsObject = envParams.get(ImportExportConstants.ENDPOINTS_FIELD).getAsJsonObject();
        }
        // if the endpoint type is REST or SOAP return null
        if (ImportExportConstants.REST_TYPE_ENDPOINT.equals(endpointType) || ImportExportConstants.SOAP_TYPE_ENDPOINT
                .equals(endpointType) || ImportExportConstants.HTTP_TYPE_ENDPOINT.equals(endpointType)) {
            return null;
        }
        // if endpoint type is Dynamic
        if (ImportExportConstants.DYNAMIC_TYPE_ENDPOINT.equals(endpointType)) {
            JsonObject updatedDynamicEndpointParams = new JsonObject();
            //replace url property in dynamic endpoints
            defaultProductionEndpoint.addProperty(ImportExportConstants.ENDPOINT_URL,
                    ImportExportConstants.DEFAULT_DYNAMIC_ENDPOINT_URL);
            defaultSandboxEndpoint.addProperty(ImportExportConstants.ENDPOINT_URL,
                    ImportExportConstants.DEFAULT_DYNAMIC_ENDPOINT_URL);
            updatedDynamicEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.DEFAULT_DYNAMIC_ENDPOINT_URL);
            updatedDynamicEndpointParams
                    .addProperty(ImportExportConstants.FAILOVER_ROUTING_POLICY, Boolean.FALSE.toString());
            handleEndpointValues(endpointsObject, updatedDynamicEndpointParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);
            //add dynamic endpoint configs as endpoint configs
            return updatedDynamicEndpointParams;

        } else if (ImportExportConstants.AWS_TYPE_ENDPOINT.equals(endpointType)) {// if endpoint type is AWS Lambda
            //if aws config is not provided
            if (envParams.get(ImportExportConstants.AWS_LAMBDA_ENDPOINT_JSON_PROPERTY) == null) {
                throw new APIManagementException(
                        "Please specify awsLambdaEndpoints field for the environment and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            }
            JsonObject awsEndpointParams = envParams.get(ImportExportConstants.AWS_LAMBDA_ENDPOINT_JSON_PROPERTY)
                    .getAsJsonObject();
            JsonObject updatedAwsEndpointParams = new JsonObject();
            //if the access method is provided with credentials
            if (StringUtils
                    .equals(awsEndpointParams.get(ImportExportConstants.AWS_ACCESS_METHOD_JSON_PROPERTY).getAsString(),
                            ImportExportConstants.AWS_STORED_ACCESS_METHOD)) {
                //get the same config object for aws configs
                updatedAwsEndpointParams = awsEndpointParams;
                updatedAwsEndpointParams.remove(ImportExportConstants.AWS_ACCESS_METHOD_JSON_PROPERTY);
                updatedAwsEndpointParams.addProperty(ImportExportConstants.AWS_ACCESS_METHOD_PROPERTY,
                        ImportExportConstants.AWS_STORED_ACCESS_METHOD);
            } else {
                //if the credentials are not provided the default will be used
                updatedAwsEndpointParams.addProperty(ImportExportConstants.AWS_ACCESS_METHOD_PROPERTY,
                        ImportExportConstants.AWS_ROLE_SUPPLIED_ACCESS_METHOD);
            }
            updatedAwsEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.AWS_LAMBDA_TYPE_ENDPOINT);
            handleEndpointValues(endpointsObject, updatedAwsEndpointParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);
            //add AWS endpoint configs as endpoint configs
            return updatedAwsEndpointParams;
        } else {
            throw new APIManagementException(
                    "Please specify valid endpoint configurations for the environment and continue...",
                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }
    }

    /**
     * This method will handle the HTTP/REST endpoint configurations
     *
     * @param envParams                 Json object of Env parameters
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     * @param routingPolicy             String of endpoint routing policy
     * @return JsonObject with HTTP/REST Endpoint configs
     * @throws APIManagementException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject handleRestEndpoints(String routingPolicy, JsonObject envParams,
            JsonObject defaultProductionEndpoint, JsonObject defaultSandboxEndpoint) throws APIManagementException {

        // if the endpoint routing policy is not specified, but the endpoints field is specified, this is the usual scenario
        JsonObject updatedRESTEndpointParams = new JsonObject();
        JsonObject endpoints = null;
        if (envParams.get(ImportExportConstants.ENDPOINTS_FIELD) != null) {
            endpoints = envParams.get(ImportExportConstants.ENDPOINTS_FIELD).getAsJsonObject();
        }
        if (StringUtils.isEmpty(routingPolicy)) {
            updatedRESTEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.HTTP_TYPE_ENDPOINT);
            handleEndpointValues(endpoints, updatedRESTEndpointParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);
        } else if (ImportExportConstants.LOAD_BALANCE_ROUTING_POLICY
                .equals(routingPolicy)) {  //if the routing policy is specified and it is load balanced

            //get load balanced configs from params
            JsonElement loadBalancedConfigElement = envParams.get(ImportExportConstants.LOAD_BALANCE_ENDPOINTS_FIELD);
            JsonObject loadBalancedConfigs;
            if (loadBalancedConfigElement == null) {
                throw new APIManagementException(
                        "Please specify loadBalanceEndpoints for the environment and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else {
                loadBalancedConfigs = loadBalancedConfigElement.getAsJsonObject();
            }
            updatedRESTEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.LOAD_BALANCE_TYPE_ENDPOINT);
            updatedRESTEndpointParams.addProperty(ImportExportConstants.LOAD_BALANCE_ALGORITHM_CLASS_PROPERTY,
                    ImportExportConstants.DEFAULT_ALGORITHM_CLASS);

            JsonElement sessionManagement = loadBalancedConfigs
                    .get(ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY);
            if (sessionManagement != null) {
                // If the user has specified this as "transport", this should be removed.
                // Otherwise APIM won't recognize this as "transport".
                if (!sessionManagement.isJsonNull()) {
                    if (!StringUtils.equals(sessionManagement.getAsString(),
                            ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_TRANSPORT_TYPE)) {
                        updatedRESTEndpointParams.add(ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY,
                                loadBalancedConfigs
                                        .get(ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY));
                    }
                }
            }

            JsonElement sessionTimeOut = loadBalancedConfigs
                    .get(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY);
            if (sessionTimeOut != null) {
                updatedRESTEndpointParams.add(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY,
                        loadBalancedConfigs.get(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY));
            }
            handleEndpointValues(loadBalancedConfigs, updatedRESTEndpointParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);

        } else if (ImportExportConstants.FAILOVER_ROUTING_POLICY
                .equals(routingPolicy)) {  //if the routing policy is specified and it is failover

            //get failover configs from params
            JsonElement failoverConfigElement = envParams.get(ImportExportConstants.FAILOVER_ENDPOINTS_FIELD);
            JsonObject failoverConfigs;
            if (failoverConfigElement == null) {
                throw new APIManagementException(
                        "Please specify failoverEndpoints field for the environment and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else {
                failoverConfigs = failoverConfigElement.getAsJsonObject();
            }
            updatedRESTEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.FAILOVER_ROUTING_POLICY);
            updatedRESTEndpointParams
                    .addProperty(ImportExportConstants.FAILOVER_TYPE_ENDPOINT, Boolean.TRUE.toString());
            //check production failover endpoints
            JsonElement productionEndpoints = failoverConfigs.get(ImportExportConstants.
                    PRODUCTION_ENDPOINTS_JSON_PROPERTY);
            JsonElement productionFailOvers = failoverConfigs.get(ImportExportConstants.
                    PRODUCTION_FAILOVERS_ENDPOINTS_JSON_PROPERTY);
            if (productionFailOvers == null) {
                //if failover endpoints are not specified but general endpoints are specified
                if (productionEndpoints != null) {
                    throw new APIManagementException(
                            "Please specify production failover field for the environment and continue...",
                            ExceptionCodes.ERROR_READING_PARAMS_FILE);
                }
            } else if (!productionFailOvers.isJsonNull()) {
                updatedRESTEndpointParams
                        .add(ImportExportConstants.PRODUCTION_FAILOVERS_ENDPOINTS_PROPERTY, productionFailOvers);
            }
            //check sandbox failover endpoints
            JsonElement sandboxFailOvers = failoverConfigs.get(ImportExportConstants.
                    SANDBOX_FAILOVERS_ENDPOINTS_JSON_PROPERTY);
            JsonElement sandboxEndpoints = failoverConfigs.get(ImportExportConstants.
                    SANDBOX_ENDPOINTS_JSON_PROPERTY);
            if (sandboxFailOvers == null) {
                //if failover endpoints are not specified but general endpoints are specified
                if (sandboxEndpoints != null) {
                    throw new APIManagementException(
                            "Please specify sandbox failover field for for the environment and continue...",
                            ExceptionCodes.ERROR_READING_PARAMS_FILE);
                }
            } else if (!sandboxFailOvers.isJsonNull()) {
                updatedRESTEndpointParams
                        .add(ImportExportConstants.SANDBOX_FAILOVERS_ENDPOINTS_PROPERTY, sandboxFailOvers);
            }
            handleEndpointValues(failoverConfigs, updatedRESTEndpointParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);
        }
        return updatedRESTEndpointParams;
    }

    /**
     * This method will handle the HTTP/SOAP endpoint configurations
     *
     * @param envParams                 Json object of Env parameters
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     * @param routingPolicy             String of endpoint routing policy
     * @return JsonObject with HTTP/SOAP Endpoint configs
     * @throws APIManagementException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject handleSoapEndpoints(String routingPolicy, JsonObject envParams,
            JsonObject defaultProductionEndpoint, JsonObject defaultSandboxEndpoint) throws APIManagementException {

        JsonObject updatedSOAPEndpointParams = new JsonObject();
        JsonObject endpoints = null;
        if (envParams.get(ImportExportConstants.ENDPOINTS_FIELD) != null) {
            endpoints = envParams.get(ImportExportConstants.ENDPOINTS_FIELD).getAsJsonObject();
        }
        // if the endpoint routing policy is not specified, but the endpoints field is specified
        if (StringUtils.isEmpty(routingPolicy)) {
            updatedSOAPEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.SOAP_TYPE_ENDPOINT);
            handleEndpointValues(endpoints, updatedSOAPEndpointParams, defaultProductionEndpoint,
                    defaultSandboxEndpoint);
        } else if (ImportExportConstants.LOAD_BALANCE_ROUTING_POLICY
                .equals(routingPolicy)) {    // if the endpoint routing policy is specified as load balanced

            //get load balanced configs from params
            JsonElement loadBalancedConfigElement = envParams.get(ImportExportConstants.LOAD_BALANCE_ENDPOINTS_FIELD);
            JsonObject loadBalancedConfigs;
            if (loadBalancedConfigElement == null) {
                throw new APIManagementException(
                        "Please specify loadBalanceEndpoints field for for the environment and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else {
                loadBalancedConfigs = loadBalancedConfigElement.getAsJsonObject();
            }
            updatedSOAPEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.LOAD_BALANCE_TYPE_ENDPOINT);
            updatedSOAPEndpointParams.addProperty(ImportExportConstants.LOAD_BALANCE_ALGORITHM_CLASS_PROPERTY,
                    ImportExportConstants.DEFAULT_ALGORITHM_CLASS);

            JsonElement sessionManagement = loadBalancedConfigs
                    .get(ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY);
            if (sessionManagement != null) {
                // If the user has specified this as "transport", this should be removed.
                // Otherwise APIM won't recognize this as "transport".
                if (!sessionManagement.isJsonNull()) {
                    if (!StringUtils.equals(sessionManagement.getAsString(),
                            ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_TRANSPORT_TYPE)) {
                        updatedSOAPEndpointParams.add(ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY,
                                loadBalancedConfigs
                                        .get(ImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY));
                    }
                }
            }

            JsonElement sessionTimeOut = loadBalancedConfigs
                    .get(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY);
            if (sessionTimeOut != null) {
                updatedSOAPEndpointParams.add(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY,
                        loadBalancedConfigs.get(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY));
            }
            JsonElement productionEndpoints = loadBalancedConfigs
                    .get(ImportExportConstants.PRODUCTION_ENDPOINTS_JSON_PROPERTY);
            if (productionEndpoints != null) {
                updatedSOAPEndpointParams.add(ImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY,
                        handleSoapFailoverAndLoadBalancedEndpointValues(productionEndpoints.getAsJsonArray()));
            }
            JsonElement sandboxEndpoints = loadBalancedConfigs
                    .get(ImportExportConstants.SANDBOX_ENDPOINTS_JSON_PROPERTY);
            if (sandboxEndpoints != null) {
                updatedSOAPEndpointParams
                        .add(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY, sandboxEndpoints.getAsJsonArray());
            }

        } else if (ImportExportConstants.FAILOVER_ROUTING_POLICY
                .equals(routingPolicy)) {  //if the routing policy is specified and it is failover

            //get failover configs from params
            JsonElement failoverConfigElement = envParams.get(ImportExportConstants.FAILOVER_ENDPOINTS_FIELD);
            JsonObject failoverConfigs;
            if (failoverConfigElement == null) {
                throw new APIManagementException(
                        "Please specify failoverEndpoints field for the environment and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else {
                failoverConfigs = failoverConfigElement.getAsJsonObject();
            }
            updatedSOAPEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.FAILOVER_ROUTING_POLICY);
            updatedSOAPEndpointParams
                    .addProperty(ImportExportConstants.FAILOVER_TYPE_ENDPOINT, Boolean.TRUE.toString());
            //check production failover endpoints
            JsonElement productionFailOvers = failoverConfigs.get(ImportExportConstants.
                    PRODUCTION_FAILOVERS_ENDPOINTS_JSON_PROPERTY);
            JsonElement productionEndpoints = failoverConfigs.get(ImportExportConstants.
                    PRODUCTION_ENDPOINTS_JSON_PROPERTY);
            if (productionFailOvers == null) {
                //if failover endpoints are not specified but general endpoints are specified
                if (productionEndpoints != null) {
                    throw new APIManagementException(
                            "Please specify production failover field for the environment and continue...",
                            ExceptionCodes.ERROR_READING_PARAMS_FILE);
                }
            } else if (!productionFailOvers.isJsonNull()) {
                updatedSOAPEndpointParams.add(ImportExportConstants.PRODUCTION_FAILOVERS_ENDPOINTS_PROPERTY,
                        handleSoapFailoverAndLoadBalancedEndpointValues(productionFailOvers.getAsJsonArray()));
            }
            //check sandbox failover endpoints
            JsonElement sandboxFailOvers = failoverConfigs.get(ImportExportConstants.
                    SANDBOX_FAILOVERS_ENDPOINTS_JSON_PROPERTY);
            JsonElement sandboxEndpoints = failoverConfigs.get(ImportExportConstants.
                    SANDBOX_ENDPOINTS_JSON_PROPERTY);
            if (sandboxFailOvers == null) {
                //if failover endpoints are not specified but general endpoints are specified
                if (sandboxEndpoints != null) {
                    throw new APIManagementException(
                            "Please specify sandbox failover field for the environment and continue...",
                            ExceptionCodes.ERROR_READING_PARAMS_FILE);
                }
            } else if (!sandboxFailOvers.isJsonNull()) {
                updatedSOAPEndpointParams.add(ImportExportConstants.SANDBOX_FAILOVERS_ENDPOINTS_PROPERTY,
                        handleSoapFailoverAndLoadBalancedEndpointValues(sandboxFailOvers.getAsJsonArray()));
            }
            updatedSOAPEndpointParams.add(ImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY,
                    handleSoapProdAndSandboxEndpointValues(productionEndpoints));
            updatedSOAPEndpointParams.add(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY,
                    handleSoapProdAndSandboxEndpointValues(sandboxEndpoints));
        }
        return updatedSOAPEndpointParams;
    }

    /**
     * This method will production and sandbox endpoint values
     *
     * @param endpointConfigs           Endpoint configurations to be updated
     * @param updatedEndpointParams     Updated endpoint parameters object
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     */
    private static void handleEndpointValues(JsonObject endpointConfigs, JsonObject updatedEndpointParams,
            JsonObject defaultProductionEndpoint, JsonObject defaultSandboxEndpoint) throws APIManagementException {

        //check api params file to get provided endpoints
        if (endpointConfigs == null) {
            updatedEndpointParams.add(ImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY, defaultProductionEndpoint);
            updatedEndpointParams.add(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY, defaultSandboxEndpoint);
        } else {
            //handle production endpoints
            if (endpointConfigs.get(ImportExportConstants.PRODUCTION_ENDPOINTS_JSON_PROPERTY) != null) {
                updatedEndpointParams.add(ImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY,
                        endpointConfigs.get(ImportExportConstants.PRODUCTION_ENDPOINTS_JSON_PROPERTY));
            }
            //handle sandbox endpoints
            if (endpointConfigs.get(ImportExportConstants.SANDBOX_ENDPOINTS_JSON_PROPERTY) != null) {
                updatedEndpointParams.add(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY,
                        endpointConfigs.get(ImportExportConstants.SANDBOX_ENDPOINTS_JSON_PROPERTY));
            }
            if (updatedEndpointParams.get(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY) == null
                    && updatedEndpointParams.get(ImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY) == null) {
                throw new APIManagementException(
                        "Please specify production sandbox or endpoints for the environment and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else if ((updatedEndpointParams.get(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY) != null)
                    && (updatedEndpointParams.get(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY).isJsonNull())) {

                if ((updatedEndpointParams.get(ImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY) != null)
                        && updatedEndpointParams.get(ImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY)
                        .isJsonNull()) {
                    throw new APIManagementException(
                            "Please specify production or sandbox endpoints for the environment and continue...",
                            ExceptionCodes.ERROR_READING_PARAMS_FILE);
                }
            }
        }
    }

    /**
     * This method will be used to extract endpoint configurations from env params file
     *
     * @param failoverEndpoints JsonArray of SOAP Failover endpoints
     * @return JsonArray of updated SOAP Failover endpoints
     */
    private static JsonArray handleSoapFailoverAndLoadBalancedEndpointValues(JsonArray failoverEndpoints) {

        for (JsonElement endpoint : failoverEndpoints) {
            JsonObject endpointObject = endpoint.getAsJsonObject();
            endpointObject.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.SOAP_ENDPOINT_TYPE_FOR_JSON);
        }
        return failoverEndpoints;
    }

    /**
     * This method will be used to extract endpoint configurations from env params file
     *
     * @param soapEndpoint JsonElement of SOAP endpoints
     * @return JsonObject of updated SOAP endpoints
     */
    private static JsonObject handleSoapProdAndSandboxEndpointValues(JsonElement soapEndpoint) {
        JsonObject soapEndpointObj = null;
        if (soapEndpoint == null) {
            return null;
        } else {
            if (!soapEndpoint.isJsonNull()) {
                soapEndpointObj = soapEndpoint.getAsJsonObject();
                soapEndpointObj.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        ImportExportConstants.SOAP_ENDPOINT_TYPE_FOR_JSON);
            }
            return soapEndpointObj;
        }
    }

    /**
     * This method will be used to generate ClientCertificates and meta information related to client certs
     *
     * @param certificates  JsonArray of client-certificates
     * @param apiIdentifier APIIdentifier if the importedApi
     * @param pathToArchive String of the archive project
     * @throws IOException            If an error occurs when generating new certs and yaml file or when moving certs
     * @throws APIManagementException If an error while generating new directory
     */
    private static void handleClientCertificates(JsonArray certificates, APIIdentifier apiIdentifier,
            String pathToArchive) throws IOException, APIManagementException {

        List<ClientCertificateDTO> certs = new ArrayList<>();

        for (JsonElement certificate : certificates) {
            JsonObject certObject = certificate.getAsJsonObject();
            String alias = certObject.get(ImportExportConstants.ALIAS_JSON_KEY).getAsString();
            ClientCertificateDTO cert = new ClientCertificateDTO();
            cert.setApiIdentifier(apiIdentifier);
            cert.setAlias(alias);
            cert.setTierName(certObject.get(ImportExportConstants.CERTIFICATE_TIER_NAME_PROPERTY).getAsString());
            String certName = certObject.get(ImportExportConstants.CERTIFICATE_PATH_PROPERTY).getAsString();
            cert.setCertificate(certName);
            certs.add(cert);

            //check and create a directory
            String clientCertificatesDirectory =
                    pathToArchive + ImportExportConstants.CLIENT_CERTIFICATES_DIRECTORY_PATH;
            if (!CommonUtil.checkFileExistence(clientCertificatesDirectory)) {
                try {
                    CommonUtil.createDirectory(clientCertificatesDirectory);
                } catch (APIImportExportException e) {
                    throw new APIManagementException(e);
                }
            }
            //copy certs file from certificates
            String userCertificatesTempDirectory = pathToArchive + ImportExportConstants.DEPLOYMENT_DIRECTORY
                    + ImportExportConstants.CERTIFICATE_DIRECTORY;
            String sourcePath = userCertificatesTempDirectory + File.separator + certName;
            String destinationPath = clientCertificatesDirectory + File.separator + certName;
            if (Files.notExists(Paths.get(sourcePath))) {
                String errorMessage =
                        "The mentioned certificate file " + certName + " is not in the certificates directory";
                throw new APIManagementException(errorMessage, ExceptionCodes.ERROR_READING_PARAMS_FILE);
            }
            CommonUtil.moveFile(sourcePath, destinationPath);
        }

        JsonElement jsonElement = new Gson().toJsonTree(certs);
        //generate meta-data yaml file
        String metadataFilePath = pathToArchive + ImportExportConstants.CLIENT_CERTIFICATES_META_DATA_FILE_PATH;
        try {
            ExportUtils.writeDtoToFile(metadataFilePath, ExportFormat.JSON,
                    ImportExportConstants.TYPE_ENDPOINT_CERTIFICATES, jsonElement);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
    }

    /**
     * This method will be used to generate Endpoint certificates and meta information related to endpoint certs
     *
     * @param certificates  JsonArray of endpoint-certificates
     * @param pathToArchive String of the archive project
     * @throws IOException            If an error occurs when generating new certs and yaml file or when moving certs
     * @throws APIManagementException If an error while generating new directory
     */
    private static void handleEndpointCertificates(JsonArray certificates, String pathToArchive)
            throws IOException, APIManagementException {

        JsonArray updatedCertsArray = new JsonArray();

        for (JsonElement certificate : certificates) {
            JsonObject certObject = certificate.getAsJsonObject();
            String alias = certObject.get(ImportExportConstants.ALIAS_JSON_KEY).getAsString();
            CertificateMetadataDTO certificateMetadataDTO = new CertificateMetadataDTO();
            certificateMetadataDTO.setAlias(alias);
            certificateMetadataDTO
                    .setEndpoint(certObject.get(ImportExportConstants.CERTIFICATE_HOST_NAME_PROPERTY).getAsString());

            //Add certificate element to cert object
            JsonElement jsonElement = new Gson().toJsonTree(certificateMetadataDTO);
            JsonObject updatedCertObj = jsonElement.getAsJsonObject();
            String certName = certObject.get(ImportExportConstants.CERTIFICATE_PATH_PROPERTY).getAsString();
            updatedCertObj.addProperty(ImportExportConstants.CERTIFICATE_FILE, certName);
            updatedCertsArray.add(updatedCertObj);

            //check and create a directory
            String endpointCertificatesDirectory =
                    pathToArchive + ImportExportConstants.ENDPOINT_CERTIFICATES_DIRECTORY_PATH;
            if (!CommonUtil.checkFileExistence(endpointCertificatesDirectory)) {
                try {
                    CommonUtil.createDirectory(endpointCertificatesDirectory);
                } catch (APIImportExportException e) {
                    throw new APIManagementException(e);
                }
            }
            //copy certs file from certificates
            String userCertificatesTempDirectory = pathToArchive + ImportExportConstants.DEPLOYMENT_DIRECTORY
                    + ImportExportConstants.CERTIFICATE_DIRECTORY;
            String sourcePath = userCertificatesTempDirectory + File.separator + certName;
            String destinationPath = endpointCertificatesDirectory + File.separator + certName;
            if (Files.notExists(Paths.get(sourcePath))) {
                String errorMessage =
                        "The mentioned certificate file " + certName + " is not in the certificates directory";
                throw new APIManagementException(errorMessage, ExceptionCodes.ERROR_READING_PARAMS_FILE);
            }
            CommonUtil.moveFile(sourcePath, destinationPath);
        }

        //generate meta-data yaml file
        String metadataFilePath = pathToArchive + ImportExportConstants.ENDPOINT_CERTIFICATES_META_DATA_FILE_PATH;
        try {
            ExportUtils.writeDtoToFile(metadataFilePath, ExportFormat.JSON,
                    ImportExportConstants.TYPE_ENDPOINT_CERTIFICATES, updatedCertsArray);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
    }

}
