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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This Class Used for API Controller related operations.
 */
public class APIControllerUtil {

    /**
     * Method will check the archive and extract environment related params.
     *
     * @param pathToArchive String of the archive project
     * @return JsonObject of environment parameters
     * @throws IOException
     */
    public static JsonObject resolveAPIControllerEnvParams(String pathToArchive) throws IOException {

        String jsonParamsContent;
        jsonParamsContent = ImportUtils
                .getFileContentAsJson(pathToArchive + ImportExportConstants.INTERMEDIATE_PARAMS_FILE_LOCATION);
        if (StringUtils.isEmpty(jsonParamsContent)) {
            return null;
        }
        JsonElement paramsElement = new JsonParser().parse(jsonParamsContent);
        return paramsElement.getAsJsonObject();
    }

    /**
     * Method retrieve the params configurations dependent APIs of an API Product.
     *
     * @param path Path of the archive project
     * @return JsonObject of environment parameters of the dependent APIs
     * @throws IOException If an error occurs when resolving API controller environment parameters
     */
    public static JsonObject getDependentAPIsParams(String path) throws IOException {

        JsonObject paramsConfigObject = APIControllerUtil.resolveAPIControllerEnvParams(path);
        JsonObject dependentAPIsParams = null;
        if (paramsConfigObject != null && paramsConfigObject.has(ImportExportConstants.DEPENDENT_APIS_FIELD)) {
            dependentAPIsParams = paramsConfigObject.get(ImportExportConstants.DEPENDENT_APIS_FIELD).getAsJsonObject();
        }
        return dependentAPIsParams;
    }

    /**
     * Method retrieve the params configurations for a dependent API of an API Product specified by the.
     * API directory name
     *
     * @param dependentAPIsParams Env params array of dependent APIs of the API Product
     * @param apiDirectoryName    Dependent API directory name
     * @return JsonObject of environment parameters of the dependent API
     */
    public static JsonObject getDependentAPIParams(JsonObject dependentAPIsParams, String apiDirectoryName) {

        if (dependentAPIsParams.has(apiDirectoryName)) {
            return dependentAPIsParams.get(apiDirectoryName).getAsJsonObject();
        }
        return null;
    }

    /**
     * This method will be used to add Extracted environment parameters to the imported Api object.
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

        // if endpointType field is not specified in the params file, it will be considered as HTTP/REST
        JsonElement endpointTypeElement = envParams.get(ImportExportConstants.ENDPOINT_TYPE_FIELD);
        String endpointType = null;
        if (endpointTypeElement != null) {
            endpointType = endpointTypeElement.getAsString();
        } else {
            endpointType = ImportExportConstants.REST_TYPE_ENDPOINT;
        }

        //Handle multiple end points
        JsonObject jsonObject = setupMultipleEndpoints(envParams, endpointType);

        // Handle endpoint security configs
        if (envParams.get(ImportExportConstants.ENDPOINT_SECURITY_FIELD) != null) {
            handleEndpointSecurityConfigs(envParams, jsonObject);
        }

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> endpointConfig;
        try {
            endpointConfig = mapper.readValue(jsonObject.toString(), HashMap.class);
            convertValuesToStrings(endpointConfig);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while reading endpointConfig information in the params file.";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }
        importedApiDto.setEndpointConfig(endpointConfig);


        //handle mutualSSL certificates
        handleMutualSslCertificates(envParams, importedApiDto, null, importedApi.getId(), pathToArchive);

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

        // handle available subscription policies
        JsonElement policies = envParams.get(ImportExportConstants.POLICIES_FIELD);
        if (policies != null && !policies.isJsonNull()) {
            handleSubscriptionPolicies(policies, importedApiDto, null);
        }
        return importedApiDto;
    }

    /**
     * This method will be used to convert any integer values to strings in a particular map of values.
     *
     * @param keyValuePairs Map to be validated and converted
     */
    private static void convertValuesToStrings(Map<String, Object> keyValuePairs) {
        Iterator it = keyValuePairs.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            if (pair.getValue() instanceof Integer && pair.getKey() instanceof String) {
                keyValuePairs.replace(pair.getKey().toString(), pair.getValue().toString());
            }
            if (pair.getValue() instanceof Map) {
                convertValuesToStrings((Map) pair.getValue());
            }
        }
    }

    /**
     * This method will be used to generate ClientCertificates and meta information related to client certs.
     *
     * @param envParams             Env params object with required parameters
     * @param importedApiDto        Imported API DTO (this will be null for API Products)
     * @param importedApiProductDto Imported API Product DTO (this will be null for APIs)
     * @param identifier            API Identifier/API Product Identifier of the imported API/API Product
     * @param pathToArchive         String of the archive project
     * @throws APIManagementException If an error while generating client certificate information
     */
    private static void handleMutualSslCertificates(JsonObject envParams, APIDTO importedApiDto,
                                                    APIProductDTO importedApiProductDto, Identifier identifier,
                                                    String pathToArchive)
            throws APIManagementException {

        JsonElement clientCertificates = envParams.get(ImportExportConstants.MUTUAL_SSL_CERTIFICATES_FIELD);
        if (clientCertificates != null) {
            try {
                List<String> apiSecurity = (importedApiDto != null) ?
                        importedApiDto.getSecurityScheme() :
                        importedApiProductDto.getSecurityScheme();
                if (!apiSecurity.isEmpty()) {
                    if (!apiSecurity.contains(ImportExportConstants.MUTUAL_SSL_ENABLED)) {
                        // if the apiSecurity field does not have mutualssl type, append it
                        apiSecurity.add(ImportExportConstants.MUTUAL_SSL_ENABLED);
                    }
                } else {
                    // if the apiSecurity field is empty, assign the value as "mutualssl"
                    apiSecurity.add(ImportExportConstants.MUTUAL_SSL_ENABLED);
                }

                if (importedApiDto != null) {
                    importedApiDto.securityScheme(apiSecurity);
                } else {
                    importedApiProductDto.securityScheme(apiSecurity);
                }

                String jsonString = clientCertificates.toString();
                handleClientCertificates(new JsonParser().parse(jsonString).getAsJsonArray(), identifier,
                        pathToArchive);
            } catch (IOException e) {
                //Error is logged and when generating certificate details and certs in the archive
                String errorMessage = "Error while generating meta information of client certificates from path.";
                throw new APIManagementException(errorMessage, e, ExceptionCodes.ERROR_READING_PARAMS_FILE);
            }
        }
    }

    /**
     * This method will be used to add extracted environment parameters to the imported API Product DTO object.
     *
     * @param importedApiProductDto API Product DTO object to be imported
     * @param envParams             Env params object with required parameters
     * @return APIProductDTO Updated API Product DTO Object
     */
    public static APIProductDTO injectEnvParamsToAPIProduct(APIProductDTO importedApiProductDto, JsonObject envParams,
                                                            String pathToArchive)
            throws APIManagementException {

        if (envParams == null || envParams.isJsonNull()) {
            return importedApiProductDto;
        }

        APIProduct importedApiProduct =
                APIMappingUtil.fromDTOtoAPIProduct(importedApiProductDto, importedApiProductDto.getProvider());
        //handle mutualSSL certificates
        handleMutualSslCertificates(envParams, null, importedApiProductDto, importedApiProduct.getId(), pathToArchive);

        // handle available subscription policies
        JsonElement policies = envParams.get(ImportExportConstants.POLICIES_FIELD);
        if (policies != null && !policies.isJsonNull()) {
            handleSubscriptionPolicies(policies, null, importedApiProductDto);
        }
        return importedApiProductDto;
    }

    /**
     * This method will be used to add Endpoint security related environment parameters to imported Api object.
     *
     * @param envParams      Env params object with required parameters
     * @param endpointConfig Endpoint config object to be updated
     * @throws APIManagementException If an error occurs when setting security env parameters
     */
    private static void handleEndpointSecurityConfigs(JsonObject envParams, JsonObject endpointConfig)
            throws APIManagementException {
        // If the user has set (either true or false) the enabled field under security in the params file,
        // the following code should be executed.
        JsonObject security = envParams.getAsJsonObject(ImportExportConstants.ENDPOINT_SECURITY_FIELD);
        if (security == null) {
            return;
        }

        String[] endpointTypes = { APIConstants.ENDPOINT_SECURITY_PRODUCTION, APIConstants.ENDPOINT_SECURITY_SANDBOX };
        for (String endpointType : endpointTypes) {
            if (security.has(endpointType)) {
                JsonObject endpointSecurityDetails = security.get(endpointType).getAsJsonObject();
                if (endpointSecurityDetails.has(APIConstants.ENDPOINT_SECURITY_ENABLED) && (
                        endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_ENABLED) != null
                                || !endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_ENABLED).isJsonNull())) {
                    boolean securityEnabled = endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_ENABLED)
                            .getAsBoolean();

                    // Set endpoint security details to API
                    if (securityEnabled) {
                        String endpointSecurityType;
                        if (endpointSecurityDetails.has(APIConstants.ENDPOINT_SECURITY_TYPE) && (
                                endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_TYPE) != null
                                        || !endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_TYPE)
                                        .isJsonNull())) {
                            // Check whether the type is defined in the params file
                            JsonElement type = endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_TYPE);
                            endpointSecurityType = type.getAsString();
                        } else {
                            throw new APIManagementException(
                                    "You have enabled endpoint security but the type is not found "
                                            + "in the params file. Please specify type field and continue...",
                                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
                        }

                        // Setup security type (basic, digest or oauth)
                        endpointSecurityDetails.remove(APIConstants.ENDPOINT_SECURITY_TYPE);
                        if (StringUtils.equals(endpointSecurityType.toLowerCase(),
                                APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST)) {
                            endpointSecurityDetails.addProperty(APIConstants.ENDPOINT_SECURITY_TYPE,
                                    APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST.toUpperCase());
                            validateEndpointSecurityUsernamePassword(endpointSecurityDetails);
                        } else if (StringUtils.equals(endpointSecurityType.toLowerCase(),
                                APIConstants.ENDPOINT_SECURITY_TYPE_BASIC)) {
                            endpointSecurityDetails.addProperty(APIConstants.ENDPOINT_SECURITY_TYPE,
                                    APIConstants.ENDPOINT_SECURITY_TYPE_BASIC.toUpperCase());
                            validateEndpointSecurityUsernamePassword(endpointSecurityDetails);
                        } else if (StringUtils.equals(endpointSecurityType.toLowerCase(),
                                APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH)) {
                            endpointSecurityDetails.addProperty(APIConstants.ENDPOINT_SECURITY_TYPE,
                                    APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH.toUpperCase());
                            validateEndpointSecurityOauth(endpointSecurityDetails);
                        } else {
                            // If the type is not either basic or digest, return an error
                            throw new APIManagementException("Invalid endpoint security type found in the params file. "
                                    + "Should be either basic, digest or oauth. "
                                    + "Please specify correct security types field and continue...",
                                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
                        }
                    } else {
                        endpointSecurityDetails.addProperty(APIConstants.ENDPOINT_SECURITY_TYPE,
                                ImportExportConstants.ENDPOINT_NONE_SECURITY_TYPE);
                    }
                }
            } else {
                // Even though the security field is defined, if either production/sandbox is not defined
                // under that,set endpoint security to none. Otherwise the security will be blank if you
                // check from the UI.
                JsonObject endpointSecurityForNotDefinedEndpointType = new JsonObject();
                endpointSecurityForNotDefinedEndpointType.addProperty(APIConstants.ENDPOINT_SECURITY_TYPE,
                        ImportExportConstants.ENDPOINT_NONE_SECURITY_TYPE);
                endpointSecurityForNotDefinedEndpointType.addProperty(APIConstants.ENDPOINT_SECURITY_ENABLED,
                        Boolean.FALSE);
                security.add(endpointType, endpointSecurityForNotDefinedEndpointType);
            }
        }
        endpointConfig.add(APIConstants.ENDPOINT_SECURITY, security);
    }

    /**
     * Check whether the username, password and type fields have set in the params file
     *
     * @param endpointSecurityDetails Endpoint security details per endpoint type
     * @throws APIManagementException If an error occurs when reading the security env parameters
     */
    private static void validateEndpointSecurityUsernamePassword(JsonObject endpointSecurityDetails)
            throws APIManagementException {
        if (!endpointSecurityDetails.has(APIConstants.ENDPOINT_SECURITY_USERNAME)
                || endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_USERNAME) == null
                || endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_USERNAME).isJsonNull()) {
            throw new APIManagementException("You have enabled endpoint security but the username is not found "
                    + "in the params file. Please specify username field and continue...",
                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }
        if (!endpointSecurityDetails.has(APIConstants.ENDPOINT_SECURITY_PASSWORD)
                || endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_PASSWORD) == null
                || endpointSecurityDetails.get(APIConstants.ENDPOINT_SECURITY_PASSWORD).isJsonNull()) {
            throw new APIManagementException("You have enabled endpoint security but the password is not found "
                    + "in the params file. Please specify password field and continue...",
                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }
    }

    /**
     * Validate the neccessary OAuth 2.0 endpoint security parameters have set in the params file
     *
     * @param endpointSecurityDetails Endpoint security details per endpoint type
     * @throws APIManagementException If an error occurs when reading the security env parameters
     */
    private static void validateEndpointSecurityOauth(JsonObject endpointSecurityDetails)
            throws APIManagementException {

        if (endpointSecurityDetails.has(APIConstants.OAuthConstants.GRANT_TYPE) && (
                endpointSecurityDetails.get(APIConstants.OAuthConstants.GRANT_TYPE) != null
                        || !endpointSecurityDetails.get(APIConstants.OAuthConstants.GRANT_TYPE).isJsonNull())) {
            String grantType = endpointSecurityDetails.get(APIConstants.OAuthConstants.GRANT_TYPE).getAsString();

            endpointSecurityDetails.remove(APIConstants.OAuthConstants.GRANT_TYPE);
            if (StringUtils.equals(grantType.toLowerCase(), APIConstants.OAuthConstants.PASSWORD.toLowerCase())) {
                validateEndpointSecurityUsernamePassword(endpointSecurityDetails);
                endpointSecurityDetails.addProperty(APIConstants.OAuthConstants.GRANT_TYPE,
                        APIConstants.OAuthConstants.PASSWORD);
            }
            if (StringUtils.equals(grantType.toLowerCase(),
                    APIConstants.OAuthConstants.CLIENT_CREDENTIALS.toLowerCase())) {
                endpointSecurityDetails.addProperty(APIConstants.OAuthConstants.GRANT_TYPE,
                        APIConstants.OAuthConstants.CLIENT_CREDENTIALS);
            }
        } else {
            throw new APIManagementException("You have enabled oauth endpoint security but the grant type is not found "
                    + "in the params file. Please specify grantType field and continue...",
                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }

        if (!endpointSecurityDetails.has(APIConstants.OAuthConstants.OAUTH_CLIENT_ID)
                || endpointSecurityDetails.get(APIConstants.OAuthConstants.OAUTH_CLIENT_ID) == null
                || endpointSecurityDetails.get(APIConstants.OAuthConstants.OAUTH_CLIENT_ID).isJsonNull()) {
            throw new APIManagementException("You have enabled oauth endpoint security but the client id is not found "
                    + "in the params file. Please specify clientId field and continue...",
                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }

        if (!endpointSecurityDetails.has(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                || endpointSecurityDetails.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET) == null
                || endpointSecurityDetails.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).isJsonNull()) {
            throw new APIManagementException(
                    "You have enabled oauth endpoint security but the client secret is not found "
                            + "in the params file. Please specify clientSecret field and continue...",
                    ExceptionCodes.ERROR_READING_PARAMS_FILE);
        }
    }

    /**
     * This method will add the defined available subscription policies in an environment to the particular imported
     * API.
     *
     * @param importedApiDto        API DTO object to be updated
     * @param importedApiProductDto API Product DTO object to be updated
     * @param policies              policies with the values
     */
    private static void handleSubscriptionPolicies(JsonElement policies, APIDTO importedApiDto,
                                                   APIProductDTO importedApiProductDto) {

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
        // If the policies are not defined in params file, the values in the api.yaml should be considered.
        // Hence, this if statement will prevent setting the policies in api.yaml to an empty array if the policies
        // are not properly defined in the params file
        if (policiesListToAdd.size() > 0) {
            if (importedApiDto != null) {
                importedApiDto.setPolicies(policiesListToAdd);
            } else {
                importedApiProductDto.setPolicies(policiesListToAdd);
            }
        }
    }

    /**
     * This method will be used to add gateway environments to imported Api object.
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
     * This method will be used to extract endpoint configurations from the env params file.
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
     * This method will handle the Dynamic and AWS endpoint configs.
     *
     * @param envParams                 Json object of Env parameters
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     * @param endpointType              String of endpoint type
     * @return JsonObject with Dynamic or AWS Endpoint configs
     * @throws APIManagementException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject handleDynamicAndAwsEndpoints(JsonObject envParams, JsonObject defaultProductionEndpoint,
                                                           JsonObject defaultSandboxEndpoint, String endpointType)
            throws APIManagementException {

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

            // if endpoint type is AWS Lambda
        } else if (ImportExportConstants.AWS_TYPE_ENDPOINT.equals(endpointType)) {
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
     * This method will handle the HTTP/REST endpoint configurations.
     *
     * @param envParams                 Json object of Env parameters
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     * @param routingPolicy             String of endpoint routing policy
     * @return JsonObject with HTTP/REST Endpoint configs
     * @throws APIManagementException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject handleRestEndpoints(String routingPolicy, JsonObject envParams,
                                                  JsonObject defaultProductionEndpoint,
                                                  JsonObject defaultSandboxEndpoint) throws APIManagementException {

        // if the endpoint routing policy is not specified, but the endpoints field is specified, this is the usual
        // scenario
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
            JsonElement failOverConfigElement = envParams.get(ImportExportConstants.FAILOVER_TYPE_ENDPOINT);
            JsonObject loadBalancedConfigs;
            if (loadBalancedConfigElement == null) {
                throw new APIManagementException(
                        "Please specify loadBalanceEndpoints for the environment and continue...",
                        ExceptionCodes.ERROR_READING_PARAMS_FILE);
            } else {
                loadBalancedConfigs = loadBalancedConfigElement.getAsJsonObject();
            }
            if (failOverConfigElement != null) {
                updatedRESTEndpointParams.addProperty(ImportExportConstants.FAILOVER_TYPE_ENDPOINT,
                        failOverConfigElement.getAsBoolean());
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
     * This method will handle the HTTP/SOAP endpoint configurations.
     *
     * @param envParams                 Json object of Env parameters
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     * @param routingPolicy             String of endpoint routing policy
     * @return JsonObject with HTTP/SOAP Endpoint configs
     * @throws APIManagementException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject handleSoapEndpoints(String routingPolicy, JsonObject envParams,
                                                  JsonObject defaultProductionEndpoint,
                                                  JsonObject defaultSandboxEndpoint) throws APIManagementException {

        JsonObject updatedSOAPEndpointParams = new JsonObject();
        JsonObject endpoints = null;
        if (envParams.get(ImportExportConstants.ENDPOINTS_FIELD) != null) {
            endpoints = envParams.get(ImportExportConstants.ENDPOINTS_FIELD).getAsJsonObject();
        }
        // if the endpoint routing policy is not specified, but the endpoints field is specified
        if (StringUtils.isEmpty(routingPolicy)) {
            updatedSOAPEndpointParams.addProperty(ImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    ImportExportConstants.SOAP_ENDPOINT_TYPE_FOR_JSON);
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
                updatedSOAPEndpointParams.add(ImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY,
                        handleSoapFailoverAndLoadBalancedEndpointValues(sandboxEndpoints.getAsJsonArray()));
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
     * This method will production and sandbox endpoint values.
     *
     * @param endpointConfigs           Endpoint configurations to be updated
     * @param updatedEndpointParams     Updated endpoint parameters object
     * @param defaultProductionEndpoint Default production endpoint json object
     * @param defaultSandboxEndpoint    Default sandbox endpoint json object
     */
    private static void handleEndpointValues(JsonObject endpointConfigs, JsonObject updatedEndpointParams,
                                             JsonObject defaultProductionEndpoint, JsonObject defaultSandboxEndpoint)
            throws APIManagementException {

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
     * This method will be used to extract endpoint configurations from env params file.
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
     * This method will be used to extract endpoint configurations from env params file.
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
     * This method will be used to generate ClientCertificates and meta information related to client certs.
     *
     * @param certificates  JsonArray of client-certificates
     * @param identifier    API Identifier/API Product Identifier of the imported API/API Product
     * @param pathToArchive String of the archive project
     * @throws IOException            If an error occurs when generating new certs and yaml file or when moving certs
     * @throws APIManagementException If an error while generating new directory
     */
    private static void handleClientCertificates(JsonArray certificates, Identifier identifier,
                                                 String pathToArchive) throws IOException, APIManagementException {

        APIIdentifier apiIdentifier = new APIIdentifier(identifier.getProviderName(), identifier.getName(),
                identifier.getVersion());
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
            if (CommonUtil.checkFileExistence(metadataFilePath + ImportExportConstants.YAML_EXTENSION)) {
                File oldFile = new File(metadataFilePath + ImportExportConstants.YAML_EXTENSION);
                oldFile.delete();
            }
            if (CommonUtil.checkFileExistence(metadataFilePath + ImportExportConstants.JSON_EXTENSION)) {
                File oldFile = new File(metadataFilePath + ImportExportConstants.JSON_EXTENSION);
                oldFile.delete();
            }
            CommonUtil.writeDtoToFile(metadataFilePath, ExportFormat.JSON,
                    ImportExportConstants.TYPE_CLIENT_CERTIFICATES, jsonElement);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
    }

    /**
     * This method will be used to generate Endpoint certificates and meta information related to endpoint certs.
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
            if (CommonUtil.checkFileExistence(metadataFilePath + ImportExportConstants.YAML_EXTENSION)) {
                File oldFile = new File(metadataFilePath + ImportExportConstants.YAML_EXTENSION);
                oldFile.delete();
            }
            if (CommonUtil.checkFileExistence(metadataFilePath + ImportExportConstants.JSON_EXTENSION)) {
                File oldFile = new File(metadataFilePath + ImportExportConstants.JSON_EXTENSION);
                oldFile.delete();
            }
            CommonUtil.writeDtoToFile(metadataFilePath, ExportFormat.JSON,
                    ImportExportConstants.TYPE_ENDPOINT_CERTIFICATES, updatedCertsArray);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
    }
}
