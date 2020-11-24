package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.xmlsec.signature.J;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
        if (jsonParamsContent == null) {
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
        String pathToYamlFile = pathToArchive + APIImportExportConstants.YAML_API_PARAMS_FILE_LOCATION;
        String pathToJsonFile = pathToArchive + APIImportExportConstants.JSON_API_PARAMS_FILE_LOCATION;

        // load yaml representation first,if it is present
        if (CommonUtil.checkFileExistence(pathToYamlFile)) {
            if (log.isDebugEnabled()) {
                log.debug("Found api params definition file " + pathToYamlFile);
            }
            String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
            jsonContent = CommonUtil.yamlToJson(yamlContent);
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
     * @param pathToArchive Path to API or API Product archive
     * @param importedApi   API object to be imported
     * @param envParams     Env params object with required parameters
     * @return API Updated API Object
     * @throws APIImportExportException If an error occurs merging env parameters with api
     */
    public static API injectEnvParamsToAPI(API importedApi, JsonObject envParams, String pathToArchive)
            throws APIImportExportException {

        if (envParams == null || envParams.isJsonNull()) {
            return importedApi;
        }

        // if endpointType field is not specified in the api_params.yaml, it will be considered as HTTP/REST
        String endpointType = envParams.get(APIImportExportConstants.ENDPOINT_TYPE_FIELD).getAsString();
        String updatedEndpointType;
        if (envParams.get(APIImportExportConstants.ENDPOINT_TYPE_FIELD).isJsonNull() ||
                StringUtils.isEmpty(endpointType)) {
            updatedEndpointType = APIImportExportConstants.REST_TYPE_ENDPOINT;
        } else {
            updatedEndpointType = envParams.get(APIImportExportConstants.ENDPOINT_TYPE_FIELD).getAsString();
        }

        //Handle multiple end points
        JsonObject configObject = setupMultipleEndpoints(envParams, updatedEndpointType);
        importedApi.setEndpointConfig(configObject.toString());

        //handle gateway environments
        if (!envParams.get(APIImportExportConstants.GATEWAY_ENVIRONMENTS_FIELD).isJsonNull()) {
            Set<String> environments = setupGatewayEnvironments(envParams.get(
                    APIImportExportConstants.GATEWAY_ENVIRONMENTS_FIELD).getAsJsonArray());
            importedApi.setEnvironmentList(environments);
        }

        //handle mutualSSL certificates
        JsonElement clientCertificates = envParams.get(APIImportExportConstants.MUTUAL_SSL_CERTIFICATES_FIELD);
        if (!clientCertificates.isJsonNull()) {
            try {
                String jsonString = clientCertificates.getAsString();
                handleClientCertificates(new JsonParser().parse(jsonString).getAsJsonArray(), importedApi.getId(), pathToArchive);
            } catch (IOException e) {
                //Error is logged and when generating certificate details and certs in the archive
                String errorMessage = "Error while generating meta information of client certificates from path.";
                throw new APIImportExportException(errorMessage, e);
            }
        }

        //handle endpoint certificates
        JsonElement endpointCertificates = envParams.get(APIImportExportConstants.ENDPOINT_CERTIFICATES_FIELD);
        if (!endpointCertificates.isJsonNull()) {
            try {
                String jsonString = endpointCertificates.getAsString();
                handleEndpointCertificates(new JsonParser().parse(jsonString).getAsJsonArray(), pathToArchive);
            } catch (IOException e) {
                //Error is logged and when generating certificate details and certs in the archive
                String errorMessage = "Error while generating meta information of client certificates from path.";
                throw new APIImportExportException(errorMessage, e);
            }
        }

        //handle security configs
        JsonElement securityConfigs = envParams.get(APIImportExportConstants.ENDPOINT_SECURITY_FIELD);
        if (!securityConfigs.isJsonNull()) {
            handleEndpointSecurityConfigs(envParams, importedApi);
        }
        return importedApi;
    }

    /**
     * This method will be used to add Endpoint security related environment parameters to imported Api object
     *
     * @param importedApi API object to be updated
     * @param envParams   Env params object with required parameters
     * @throws APIImportExportException If an error occurs when setting security env parameters
     */
    private static void handleEndpointSecurityConfigs(JsonObject envParams, API importedApi)
            throws APIImportExportException {
        // If the user has set (either true or false) the enabled field under security in api_params.yaml,
        // the following code should be executed.
        JsonObject security = envParams.getAsJsonObject(APIImportExportConstants.ENDPOINT_SECURITY_FIELD);
        if (security == null) {
            return;
        }
        String securityEnabled = security.get(APIImportExportConstants.ENDPOINT_SECURITY_ENABLED).getAsString();
        boolean isSecurityEnabled = Boolean.parseBoolean(securityEnabled);
        //set endpoint security details to API
        importedApi.setEndpointSecured(isSecurityEnabled);

        // If endpoint security is enabled
        if (isSecurityEnabled) {
            // Check whether the username, password and type fields have set in api_params.yaml
            JsonElement username = security.get(APIImportExportConstants.ENDPOINT_UT_USERNAME);
            JsonElement password = security.get(APIImportExportConstants.ENDPOINT_UT_PASSWORD);
            JsonElement type = security.get(APIImportExportConstants.ENDPOINT_SECURITY_TYPE);

            if (username == null) {
                throw new APIImportExportException("You have enabled endpoint security but the username is not found " +
                        "in the api_params.yaml. Please specify username field for" +
                        envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
            } else if (password == null) {
                throw new APIImportExportException("You have enabled endpoint security but the password is not found " +
                        "in the api_params.yaml. Please specify password field for" +
                        envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
            } else if (type == null) {
                throw new APIImportExportException("You have enabled endpoint security but the password is not found " +
                        "in the api_params.yaml. Please specify password field for" +
                        envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
            } else {
                importedApi.setEndpointUTUsername(username.getAsString());
                importedApi.setEndpointUTPassword(password.getAsString());
                //setup security type (basic or digest)
                if (StringUtils.equals(type.getAsString(), APIImportExportConstants.ENDPOINT_DIGEST_SECURITY_TYPE)) {
                    importedApi.setEndpointAuthDigest(Boolean.TRUE);
                } else if (StringUtils.equals(type.getAsString(), APIImportExportConstants.ENDPOINT_BASIC_SECURITY_TYPE)) {
                    importedApi.setEndpointAuthDigest(Boolean.FALSE);
                } else {
                    // If the type is not either basic or digest, return an error
                    throw new APIImportExportException("Invalid endpoint security type found in the api_params.yaml. " +
                            "Should be either basic or digest" + "Please specify correct security types field for"
                            + envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                }
            }
        }
    }

    /**
     * This method will be used to add gateway environments to imported Api object
     *
     * @param gatewayEnvironments Json array of gateway environments extracted from env params file
     * @return Gateway Environment list
     * @throws APIImportExportException If an error occurs when setting security env parameters
     */
    private static Set<String> setupGatewayEnvironments(JsonArray gatewayEnvironments) {

        Set<String> environments = new HashSet<>();
        for (int i = 0; i < gatewayEnvironments.size(); i++) {
            environments.add(gatewayEnvironments.get(i).getAsString());
        }
        return environments;
    }

    /**
     * This method will be used to extract endpoint configurations from env params file
     *
     * @param endpointType Endpoint type
     * @param envParams    Env params object with required parameters
     * @return Gateway Environment list
     * @throws APIImportExportException If an error occurs when extracting endpoint configurations
     */
    private static JsonObject setupMultipleEndpoints(JsonObject envParams, String endpointType)
            throws APIImportExportException {

        //default production and sandbox endpoints
        JsonObject defaultProductionEndpoint = new JsonObject();
        defaultProductionEndpoint.addProperty(APIImportExportConstants.ENDPOINT_URL,
                APIImportExportConstants.DEFAULT_PRODUCTION_ENDPOINT_URL);
        JsonObject defaultSandboxEndpoint = new JsonObject();
        defaultSandboxEndpoint.addProperty(APIImportExportConstants.ENDPOINT_URL,
                APIImportExportConstants.DEFAULT_SANDBOX_ENDPOINT_URL);

        JsonObject multipleEndpointsConfig = null;
        String routingPolicy = null;

        // if the endpoint routing policy or the endpoints field is not specified and
        // if the endpoint type is AWS or Dynamic
        if (!envParams.get(APIImportExportConstants.ROUTING_POLICY_FIELD).isJsonNull()) {
            routingPolicy = envParams.get(APIImportExportConstants.ROUTING_POLICY_FIELD).getAsString();
        }
        if (StringUtils.isEmpty(routingPolicy)) {
            JsonObject endpointsObject = null;
            if (!envParams.get(APIImportExportConstants.ENDPOINTS_FIELD).isJsonNull()) {
                endpointsObject = envParams.get(APIImportExportConstants.ENDPOINTS_FIELD).getAsJsonObject();
            }
            // if endpoint type is Dynamic
            if (APIImportExportConstants.DYNAMIC_TYPE_ENDPOINT.equals(endpointType)) {
                JsonObject updatedDynamicEndpointParams = new JsonObject();
                //replace url property in dynamic endpoints
                defaultProductionEndpoint.addProperty(APIImportExportConstants.ENDPOINT_URL,
                        APIImportExportConstants.DEFAULT_DYNAMIC_ENDPOINT_URL);
                defaultSandboxEndpoint.addProperty(APIImportExportConstants.ENDPOINT_URL,
                        APIImportExportConstants.DEFAULT_DYNAMIC_ENDPOINT_URL);
                updatedDynamicEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.DEFAULT_DYNAMIC_ENDPOINT_URL);
                updatedDynamicEndpointParams.addProperty(APIImportExportConstants.FAILOVER_ROUTING_POLICY,
                        Boolean.FALSE.toString());
                handleEndpointValues(endpointsObject, updatedDynamicEndpointParams,
                        defaultProductionEndpoint, defaultSandboxEndpoint);
                //add dynamic endpoint configs as endpoint configs
                multipleEndpointsConfig = updatedDynamicEndpointParams;

            } else if (APIImportExportConstants.AWS_TYPE_ENDPOINT.equals(endpointType)) {// if endpoint type is AWS Lambda
                //if aws config is not provided
                if (envParams.get(APIImportExportConstants.AWS_LAMBDA_ENDPOINT_PROPERTY).isJsonNull()) {
                    throw new APIImportExportException("Please specify awsLambdaEndpoints field for " +
                            envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                }
                JsonObject awsEndpointParams = envParams.get(APIImportExportConstants.AWS_LAMBDA_ENDPOINT_PROPERTY).getAsJsonObject();
                JsonObject updatedAwsEndpointParams = new JsonObject();
                //if the access method is provided with credentials
                if (StringUtils.equals(awsEndpointParams.get(APIImportExportConstants.AWS_ACCESS_METHOD_PROPERTY)
                        .getAsString(), APIImportExportConstants.AWS_STORED_ACCESS_METHOD)) {
                    //get the same config object for aws configs
                    updatedAwsEndpointParams = awsEndpointParams;
                } else {
                    //if the credentials are not provided the default will be used
                    updatedAwsEndpointParams.addProperty(APIImportExportConstants.AWS_ACCESS_METHOD_PROPERTY,
                            APIImportExportConstants.AWS_ROLE_SUPPLIED_ACCESS_METHOD);
                }
                updatedAwsEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.AWS_LAMBDA_TYPE_ENDPOINT);
                handleEndpointValues(endpointsObject, updatedAwsEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
                //add AWS endpoint configs as endpoint configs
                multipleEndpointsConfig = updatedAwsEndpointParams;
            }
        }

        // if endpoint type is HTTP/REST
        if (StringUtils.equals(endpointType, APIImportExportConstants.HTTP_TYPE_ENDPOINT) ||
                StringUtils.equals(endpointType, APIImportExportConstants.REST_TYPE_ENDPOINT)) {

            // if the endpoint routing policy is not specified, but the endpoints field is specified, this is the usual scenario
            JsonObject updatedRESTEndpointParams = new JsonObject();
            JsonObject endpoints = null;
            if (!envParams.get(APIImportExportConstants.ENDPOINTS_FIELD).isJsonNull()) {
                endpoints = envParams.get(APIImportExportConstants.ENDPOINTS_FIELD).getAsJsonObject();
            }
            if (StringUtils.isEmpty(routingPolicy)) {
                updatedRESTEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.HTTP_TYPE_ENDPOINT);
                handleEndpointValues(endpoints, updatedRESTEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
            } else if (APIImportExportConstants.LOAD_BALANCE_ROUTING_POLICY.equals(routingPolicy)) {  //if the routing policy is specified and it is load balanced

                //get load balanced configs from params
                JsonElement loadBalancedConfigElement = envParams.get(APIImportExportConstants.LOAD_BALANCE_ENDPOINTS_FIELD);
                JsonObject loadBalancedConfigs;
                if (loadBalancedConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify loadBalanceEndpoints field for " +
                            envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                } else {
                    loadBalancedConfigs = loadBalancedConfigElement.getAsJsonObject();
                }
                updatedRESTEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.LOAD_BALANCE_TYPE_ENDPOINT);
                updatedRESTEndpointParams.addProperty(APIImportExportConstants.LOAD_BALANCE_ALGORITHM_CLASS_PROPERTY,
                        APIImportExportConstants.DEFAULT_ALGORITHM_CLASS);

                JsonElement sessionManagement = loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY);
                if (sessionManagement != null) {
                    // If the user has specified this as "transport", this should be removed.
                    // Otherwise APIM won't recognize this as "transport".
                    if (!StringUtils.equals(sessionManagement.getAsString(), APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_TRANSPORT_TYPE)) {
                        updatedRESTEndpointParams.add(APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY,
                                loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY));
                    }
                }

                JsonElement sessionTimeOut = loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY);
                if (sessionTimeOut != null) {
                    updatedRESTEndpointParams.add(APIImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY,
                            loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY));
                }
                handleEndpointValues(loadBalancedConfigs, updatedRESTEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);

            } else if (APIImportExportConstants.FAILOVER_ROUTING_POLICY.equals(routingPolicy)) {  //if the routing policy is specified and it is failover

                //get failover configs from params
                JsonElement failoverConfigElement = envParams.get(APIImportExportConstants.FAILOVER_ENDPOINTS_FIELD);
                JsonObject failoverConfigs;
                if (failoverConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify failoverEndpoints field for " +
                            envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                } else {
                    failoverConfigs = failoverConfigElement.getAsJsonObject();
                }
                updatedRESTEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.FAILOVER_ROUTING_POLICY);
                updatedRESTEndpointParams.addProperty(APIImportExportConstants.FAILOVER_TYPE_ENDPOINT,
                        Boolean.TRUE.toString());
                //check production failover endpoints
                JsonElement productionEndpoints = failoverConfigs.get(APIImportExportConstants.
                        PRODUCTION_ENDPOINTS_PROPERTY);
                JsonElement productionFailOvers = failoverConfigs.get(APIImportExportConstants.
                        PRODUCTION_FAILOVERS_ENDPOINTS_PROPERTY);
                if (productionFailOvers == null) {
                    //if failover endpoints are not specified but general endpoints are specified
                    if (productionEndpoints != null) {
                        throw new APIImportExportException("Please specify production failover field for " +
                                envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                    }
                } else if (!productionFailOvers.isJsonNull()) {
                    updatedRESTEndpointParams.add(APIImportExportConstants.PRODUCTION_FAILOVERS_ENDPOINTS_PROPERTY,
                            handleSoapFailoverAndLoadBalancedEndpointValues(productionFailOvers.getAsJsonArray()));
                }
                //check sandbox failover endpoints
                JsonElement sandboxFailOvers = failoverConfigs.get(APIImportExportConstants.
                        SANDBOX_FAILOVERS_ENDPOINTS_PROPERTY);
                JsonElement sandboxEndpoints = failoverConfigs.get(APIImportExportConstants.
                        SANDBOX_ENDPOINTS_PROPERTY);
                if (sandboxFailOvers == null) {
                    //if failover endpoints are not specified but general endpoints are specified
                    if (sandboxEndpoints != null) {
                        throw new APIImportExportException("Please specify sandbox failover field for " +
                                envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                    }
                } else if (!sandboxFailOvers.isJsonNull()) {
                    updatedRESTEndpointParams.add(APIImportExportConstants.SANDBOX_FAILOVERS_ENDPOINTS_PROPERTY,
                            handleSoapFailoverAndLoadBalancedEndpointValues(sandboxFailOvers.getAsJsonArray()));
                }
                handleEndpointValues(failoverConfigs, updatedRESTEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
            }
            //add REST endpoint configs as endpoint configs
            multipleEndpointsConfig = updatedRESTEndpointParams;
        }

        // if endpoint type is HTTP/SOAP
        if (APIImportExportConstants.SOAP_TYPE_ENDPOINT.equals(endpointType)) {

            JsonObject updatedSOAPEndpointParams = new JsonObject();
            JsonObject endpoints = null;
            if (!envParams.get(APIImportExportConstants.ENDPOINTS_FIELD).isJsonNull()) {
                endpoints = envParams.get(APIImportExportConstants.ENDPOINTS_FIELD).getAsJsonObject();
            }
            // if the endpoint routing policy is not specified, but the endpoints field is specified
            if (StringUtils.isEmpty(routingPolicy)) {
                updatedSOAPEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.SOAP_TYPE_ENDPOINT);
                handleEndpointValues(endpoints, updatedSOAPEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
            } else if (APIImportExportConstants.LOAD_BALANCE_ROUTING_POLICY.equals(routingPolicy)) {    // if the endpoint routing policy is specified as load balanced

                //get load balanced configs from params
                JsonElement loadBalancedConfigElement = envParams.get(APIImportExportConstants.LOAD_BALANCE_ENDPOINTS_FIELD);
                JsonObject loadBalancedConfigs;
                if (loadBalancedConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify loadBalanceEndpoints field for " +
                            envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                } else {
                    loadBalancedConfigs = loadBalancedConfigElement.getAsJsonObject();

                }
                updatedSOAPEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.LOAD_BALANCE_TYPE_ENDPOINT);
                updatedSOAPEndpointParams.addProperty(APIImportExportConstants.LOAD_BALANCE_ALGORITHM_CLASS_PROPERTY,
                        APIImportExportConstants.DEFAULT_ALGORITHM_CLASS);

                JsonElement sessionManagement = loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY);
                if (sessionManagement != null) {
                    // If the user has specified this as "transport", this should be removed.
                    // Otherwise APIM won't recognize this as "transport".
                    if (!StringUtils.equals(sessionManagement.getAsString(), APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_TRANSPORT_TYPE)) {
                        updatedSOAPEndpointParams.add(APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY,
                                loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY));
                    }
                }

                JsonElement sessionTimeOut = loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY);
                if (sessionTimeOut != null) {
                    updatedSOAPEndpointParams.add(APIImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY,
                            loadBalancedConfigs.get(APIImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY));
                }
                updatedSOAPEndpointParams.add(APIImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY,
                        handleSoapFailoverAndLoadBalancedEndpointValues(loadBalancedConfigs.get(APIImportExportConstants
                                .PRODUCTION_ENDPOINTS_PROPERTY).getAsJsonArray()));
                updatedSOAPEndpointParams.add(APIImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY,
                        handleSoapFailoverAndLoadBalancedEndpointValues(loadBalancedConfigs.get(APIImportExportConstants.
                                SANDBOX_ENDPOINTS_PROPERTY).getAsJsonArray()));

            } else if (APIImportExportConstants.FAILOVER_ROUTING_POLICY.equals(routingPolicy)) {  //if the routing policy is specified and it is failover

                //get failover configs from params
                JsonElement failoverConfigElement = envParams.get(APIImportExportConstants.FAILOVER_ENDPOINTS_FIELD);
                JsonObject failoverConfigs;
                if (failoverConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify failoverEndpoints field for " +
                            envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                } else {
                    failoverConfigs = failoverConfigElement.getAsJsonObject();
                }
                updatedSOAPEndpointParams.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                        APIImportExportConstants.FAILOVER_ROUTING_POLICY);
                updatedSOAPEndpointParams.addProperty(APIImportExportConstants.FAILOVER_TYPE_ENDPOINT,
                        Boolean.TRUE.toString());
                //check production failover endpoints
                JsonElement productionFailOvers = failoverConfigs.get(APIImportExportConstants.
                        PRODUCTION_FAILOVERS_ENDPOINTS_PROPERTY);
                JsonElement productionEndpoints = failoverConfigs.get(APIImportExportConstants.
                        PRODUCTION_ENDPOINTS_PROPERTY);
                if (productionFailOvers == null) {
                    //if failover endpoints are not specified but general endpoints are specified
                    if (productionEndpoints != null) {
                        throw new APIImportExportException("Please specify production failover field for " +
                                envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                    }
                } else if (!productionFailOvers.isJsonNull()) {
                    updatedSOAPEndpointParams.add(APIImportExportConstants.PRODUCTION_FAILOVERS_ENDPOINTS_PROPERTY,
                            handleSoapFailoverAndLoadBalancedEndpointValues(productionFailOvers.getAsJsonArray()));
                }
                //check sandbox failover endpoints
                JsonElement sandboxFailOvers = failoverConfigs.get(APIImportExportConstants.
                        SANDBOX_FAILOVERS_ENDPOINTS_PROPERTY);
                JsonElement sandboxEndpoints = failoverConfigs.get(APIImportExportConstants.
                        SANDBOX_ENDPOINTS_PROPERTY);
                if (sandboxFailOvers == null) {
                    //if failover endpoints are not specified but general endpoints are specified
                    if (sandboxEndpoints != null) {
                        throw new APIImportExportException("Please specify sandbox failover field for " +
                                envParams.get(APIImportExportConstants.ENV_NAME_FIELD).getAsString() + " and continue...");
                    }
                } else if (!sandboxFailOvers.isJsonNull()) {
                    updatedSOAPEndpointParams.add(APIImportExportConstants.SANDBOX_FAILOVERS_ENDPOINTS_PROPERTY,
                            handleSoapFailoverAndLoadBalancedEndpointValues(sandboxFailOvers.getAsJsonArray()));
                }
                updatedSOAPEndpointParams.add(APIImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY,
                        handleSoapProdAndSandboxEndpointValues(productionEndpoints));
                updatedSOAPEndpointParams.add(APIImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY,
                        handleSoapProdAndSandboxEndpointValues(sandboxEndpoints));
            }
            //add SOAP endpoint configs as endpoint configs
            multipleEndpointsConfig = updatedSOAPEndpointParams;
        }
        return multipleEndpointsConfig;
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
                                             JsonObject defaultProductionEndpoint, JsonObject defaultSandboxEndpoint) {
        //check api params file to get provided endpoints
        if (endpointConfigs == null) {
            updatedEndpointParams.add(APIImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY, defaultProductionEndpoint);
            updatedEndpointParams.add(APIImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY, defaultSandboxEndpoint);
        } else {
            //handle production endpoints
            if (endpointConfigs.get(APIImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY) != null) {
                updatedEndpointParams.add(APIImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY,
                        endpointConfigs.get(APIImportExportConstants.PRODUCTION_ENDPOINTS_PROPERTY));
            }
            //handle sandbox endpoints
            if (endpointConfigs.get(APIImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY) != null) {
                updatedEndpointParams.add(APIImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY,
                        endpointConfigs.get(APIImportExportConstants.SANDBOX_ENDPOINTS_PROPERTY));
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
            endpointObject.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                    APIImportExportConstants.SOAP_ENDPOINT_TYPE_FOR_JSON);
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
        JsonObject soapEndpointObj;
        if (soapEndpoint == null) {
            soapEndpointObj = new JsonObject();
            soapEndpointObj.addProperty(APIImportExportConstants.ENDPOINT_URL, APIImportExportConstants.DEFAULT_SANDBOX_ENDPOINT_URL);
        } else {
            soapEndpointObj = soapEndpoint.getAsJsonObject();
        }

        soapEndpointObj.addProperty(APIImportExportConstants.ENDPOINT_TYPE_PROPERTY,
                APIImportExportConstants.SOAP_ENDPOINT_TYPE_FOR_JSON);
        return soapEndpointObj;
    }

    /**
     * This method will be used to generate ClientCertificates and meta information related to client certs
     *
     * @param certificates  JsonArray of client-certificates
     * @param apiIdentifier APIIdentifier if the importedApi
     * @param pathToArchive String of the archive project
     * @throws IOException If an error occurs when generating new certs and yaml file
     */
    private static void handleClientCertificates(JsonArray certificates, APIIdentifier apiIdentifier, String pathToArchive) throws IOException {

        JsonArray updatedCertificates = new JsonArray();

        for (JsonElement certificate : certificates) {
            JsonObject certObject = certificate.getAsJsonObject();
            String alias = certObject.get(APIImportExportConstants.ALIAS_JSON_KEY).getAsString();
            // add api identifier details
            JsonObject jsonApiIdentifier = new JsonObject();
            jsonApiIdentifier.addProperty(APIImportExportConstants.API_NAME_ELEMENT, apiIdentifier.getApiName());
            jsonApiIdentifier.addProperty(APIImportExportConstants.VERSION_ELEMENT, apiIdentifier.getVersion());
            jsonApiIdentifier.addProperty(APIImportExportConstants.PROVIDER_ELEMENT, apiIdentifier.getProviderName());
            certObject.add(APIImportExportConstants.CERTIFICATE_API_IDENTIFIER_PROPERTY, jsonApiIdentifier);
            //replace certificate element with updated info
            updatedCertificates.add(certObject);

            //create certificate in the path
            String content = APIImportExportConstants.CERTIFICATE_PREFIX_STRING + certObject.get(
                    APIImportExportConstants.CERTIFICATE_CERTIFICATE_CONTENT_PROPERTY).getAsString()
                    + APIImportExportConstants.CERTIFICATE_SUFFIX_STRING;
            String certificatePath = pathToArchive + APIImportExportConstants.META_INFO_DIRECTORY_PATH + alias +
                    APIImportExportConstants.CERTIFICATE_FILE_EXTENSION;
            CommonUtil.generateFiles(certificatePath, content);
        }

        //generate meta-data yaml file
        String yamlContent = CommonUtil.jsonToYaml(updatedCertificates.toString());
        CommonUtil.generateFiles(pathToArchive + APIImportExportConstants.CLIENT_CERTIFICATES_META_DATA_FILE_PATH, yamlContent);

    }

    /**
     * This method will be used to generate Endpoint certificates and meta information related to endpoint certs
     *
     * @param certificates  JsonArray of endpoint-certificates
     * @param pathToArchive String of the archive project
     * @throws IOException If an error occurs when generating new certs and yaml file
     */
    private static void handleEndpointCertificates(JsonArray certificates, String pathToArchive) throws IOException {

        for (JsonElement certificate : certificates) {
            //create certificate in the path
            JsonObject certObject = certificate.getAsJsonObject();
            String alias = certObject.get(APIImportExportConstants.ALIAS_JSON_KEY).getAsString();
            String content = APIImportExportConstants.CERTIFICATE_PREFIX_STRING + certObject.get
                    (APIImportExportConstants.CERTIFICATE_CERTIFICATE_CONTENT_PROPERTY).getAsString()
                    + APIImportExportConstants.CERTIFICATE_SUFFIX_STRING;
            String certificatePath = pathToArchive + APIImportExportConstants.META_INFO_DIRECTORY_PATH + alias +
                    APIImportExportConstants.CERTIFICATE_FILE_EXTENSION;
            CommonUtil.generateFiles(certificatePath, content);
        }

        //generate meta-data yaml file
        String yamlContent = CommonUtil.jsonToYaml(certificates.toString());
        CommonUtil.generateFiles(pathToArchive + APIImportExportConstants.ENDPOINT_CERTIFICATES_META_DATA_FILE_PATH
                , yamlContent);
    }

}
