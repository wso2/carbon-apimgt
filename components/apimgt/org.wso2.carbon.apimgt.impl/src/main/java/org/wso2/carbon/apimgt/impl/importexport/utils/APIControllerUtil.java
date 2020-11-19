package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class APIControllerUtil {

    private static final Log log = LogFactory.getLog(APIAndAPIProductCommonUtil.class);

    /**
     * Method created to resolve the api controller related environment parameters
     *
     * @param configObject JsonObject of the imported yaml file
     * @return JsonObject of environment parameters
     * @throws APIImportExportException
     */
    public static JsonObject resolveAPIControllerEnvParams(JsonObject configObject) {

        JsonPrimitive paramsJsonPrim = configObject.getAsJsonPrimitive("params");

        if (paramsJsonPrim == null) {
            return null;
        } else {
            String envParams = String.valueOf(paramsJsonPrim).substring(1, String.valueOf(paramsJsonPrim).length() - 1);
            JsonObject paramsObject = new JsonParser().parse(envParams.replace("\\\"", "\""))
                    .getAsJsonObject();
            return paramsObject;
        }
    }

    /**
     * Retrieve API params file as JSON.
     *
     * @param pathToArchive Path to API or API Product archive
     * @return String Json string of environment parameters
     * @throws IOException If an error occurs while reading the file
     */
    public static String getParamsDefinitionAsJSON(String pathToArchive) throws IOException {

        String jsonContent = null;
        String pathToYamlFile = pathToArchive + APIImportExportConstants.YAML_API_PARAMS_FILE_LOCATION;
        String pathToJsonFile = pathToArchive + APIImportExportConstants.JSON_API_PARAMS_FILE_LOCATION;

        // load yaml representation first if it is present
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

    public static API injectEnvParamsToAPI(API importedApi, JsonObject envParams, String pathToArchive) throws APIImportExportException {

        if (envParams == null) {
            return importedApi;
        }

        // if endpointType field is not specified in the api_params.yaml, it will be considered as HTTP/REST
        String endpointType = envParams.get("EndpointType").getAsString();
        String updatedEndpointType;
        if (envParams.get("EndpointType").isJsonNull() || StringUtils.isEmpty(endpointType)) {
            updatedEndpointType = "rest";
        } else {
            updatedEndpointType = envParams.get("EndpointType").getAsString();
        }

        //Handle multiple end points
        JsonObject configObject = setupMultipleEndpoints(envParams, updatedEndpointType);
        String endPointConfig = configObject.toString();
        importedApi.setEndpointConfig(endPointConfig);

        //handle gateway environments
        if (!envParams.get("GatewayEnvironments").isJsonNull()) {
            Set<String> environments = setupGatewayEnvironments(envParams.get("GatewayEnvironments").getAsJsonArray());
            importedApi.setEnvironmentList(environments);
        }

        //handle mutualSSL certificates
        JsonElement clientCertificates = envParams.get("MutualSslCerts");
        if (clientCertificates != null) {
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
        JsonElement endpointCertificates = envParams.get("Certs");
        if (endpointCertificates != null) {
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
        JsonObject securityConfigs = envParams.getAsJsonObject("Security");
        if (securityConfigs != null) {
            handleEndpointSecurityConfigs(envParams, importedApi);
        }
        return importedApi;
    }


    private static void handleEndpointSecurityConfigs(JsonObject envParams, API importedApi) throws APIImportExportException {
        // If the user has set (either true or false) the enabled field under security in api_params.yaml,
        // the following code should be executed.
        JsonObject security = envParams.getAsJsonObject("Security");
        String securityEnabled = security.get("enabled").getAsString();
        boolean isSecurityEnabled = Boolean.parseBoolean(securityEnabled);
        //set endpoint security details to API
        importedApi.setEndpointSecured(isSecurityEnabled);

        // If endpoint security is enabled
        if (isSecurityEnabled) {
            // Check whether the username, password and type fields have set in api_params.yaml
            JsonElement username = security.get("username");
            JsonElement password = security.get("password");
            JsonElement type = security.get("type");

            if (username == null) {
                throw new APIImportExportException("You have enabled endpoint security but the username is not found " +
                        "in the api_params.yaml. Please specify usename field for" +
                        envParams.get("Name").getAsString() + " and continue...");
            } else if (password == null) {
                throw new APIImportExportException("You have enabled endpoint security but the password is not found " +
                        "in the api_params.yaml. Please specify password field for" +
                        envParams.get("Name").getAsString() + " and continue...");
            } else if (type == null) {
                throw new APIImportExportException("You have enabled endpoint security but the password is not found " +
                        "in the api_params.yaml. Please specify password field for" +
                        envParams.get("Name").getAsString() + " and continue...");
            } else {
                importedApi.setEndpointUTUsername(username.getAsString());
                importedApi.setEndpointUTPassword(password.getAsString());
                //setup security type (basic or digest)
                if (StringUtils.equals(type.getAsString(), "digest")) {
                    importedApi.setEndpointAuthDigest(Boolean.TRUE);
                } else if (StringUtils.equals(type.getAsString(), "basic")) {
                    importedApi.setEndpointAuthDigest(Boolean.FALSE);
                } else {
                    // If the type is not either basic or digest, return an error
                    throw new APIImportExportException("Invalid endpoint security type found in the api_params.yaml. " +
                            "Should be either basic or digest" + "Please specify correct security types field for"
                            + envParams.get("Name").getAsString() + " and continue...");
                }
            }
        }
    }

    private static Set<String> setupGatewayEnvironments(JsonArray gatewayEnvironments) {

        Set<String> environments = new HashSet<>();
        for (int i = 0; i < gatewayEnvironments.size(); i++) {
            environments.add(gatewayEnvironments.get(i).getAsString());
        }
        return environments;
    }

    private static JsonObject setupMultipleEndpoints(JsonObject envParams, String endpointType) throws APIImportExportException {

        //default production and sandbox endpoints
        JsonObject defaultProductionEndpoint = new JsonObject();
//        defaultProductionEndpoint.addProperty("config","null");
        defaultProductionEndpoint.addProperty("url", "http://localhost:8080");
        JsonObject defaultSandboxEndpoint = new JsonObject();
//        defaultSandboxEndpoint.addProperty("config","null");
        defaultSandboxEndpoint.addProperty("url", "http://localhost:8081");

        JsonObject multipleEndpointsConfig = null;
        String routingPolicy = null;

        // if the endpoint routing policy or the endpoints field is not specified and
        // if the endpoint type is AWS or Dynamic
        if (!envParams.get("EndpointRoutingPolicy").isJsonNull()) {
            routingPolicy = envParams.get("EndpointRoutingPolicy").getAsString();
        }
        if (StringUtils.isEmpty(routingPolicy)) {
            JsonObject endpoints = null;
            if (!envParams.get("Endpoints").isJsonNull()) {
                endpoints = envParams.get("Endpoints").getAsJsonObject();
            }
            // if endpoint type is Dynamic
            if (StringUtils.equals(endpointType, "dynamic")) {
                JsonObject updatedDynamicEndpointParams = new JsonObject();
                //replace url property in dynamic endpoints
                defaultProductionEndpoint.addProperty("url", "default");
                defaultSandboxEndpoint.addProperty("url", "default");

                updatedDynamicEndpointParams.addProperty("endpoint_type", "default");
                updatedDynamicEndpointParams.addProperty("failOver", Boolean.FALSE.toString());
                handleEndpointValues(endpoints, updatedDynamicEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
                multipleEndpointsConfig = updatedDynamicEndpointParams;

            } else if (StringUtils.equals(endpointType, "aws")) {                // if endpoint type is AWS Lambda
                //if aws config is not provided
                if (envParams.get("AWSLambdaEndpoints").isJsonNull()) {
                    throw new APIImportExportException("Please specify awsLambdaEndpoints field for " +
                            envParams.get("Name").getAsString() + " and continue...");
                }
                JsonObject awsEndpointParams = envParams.get("AWSLambdaEndpoints").getAsJsonObject();
                JsonObject updatedAwsEndpointParams = new JsonObject();
                updatedAwsEndpointParams.addProperty("endpoint_type", "awslambda");
                //if the access method is provided with credentials
                if (StringUtils.equals(awsEndpointParams.get("access_method").getAsString(), "stored")) {
                    updatedAwsEndpointParams.add("access_method", awsEndpointParams.get("access_method"));
                    updatedAwsEndpointParams.add("amznRegion", awsEndpointParams.get("amznRegion"));
                    updatedAwsEndpointParams.add("amznAccessKey", awsEndpointParams.get("amznAccessKey"));
                    updatedAwsEndpointParams.add("amznSecretKey", awsEndpointParams.get("amznSecretKey"));
                } else {
                    //if the credentials are not provided the default will be used
                    updatedAwsEndpointParams.addProperty("access_method", "role-supplied");
                }
                handleEndpointValues(endpoints, updatedAwsEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
                multipleEndpointsConfig = updatedAwsEndpointParams;
            }
        }

        // if endpoint type is HTTP/REST
        if (StringUtils.equals(endpointType, "http") || StringUtils.equals(endpointType, "rest")) {

            // if the endpoint routing policy is not specified, but the endpoints field is specified, this is the usual scenario
            JsonObject updatedRESTEndpointParams = new JsonObject();
            JsonObject endpoints = null;
            if (!envParams.get("Endpoints").isJsonNull()) {
                endpoints = envParams.get("Endpoints").getAsJsonObject();
            }
            if (StringUtils.isEmpty(routingPolicy)) {
                updatedRESTEndpointParams.addProperty("endpoint_type", "http");
                handleEndpointValues(endpoints, updatedRESTEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
            } else if (StringUtils.equals(routingPolicy, "load_balanced")) {   //if the routing policy is specified and it is load balanced

                //get load balanced configs from params
                JsonElement loadBalancedConfigElement = envParams.get("LoadBalanceEndpoints");
                JsonObject loadBalancedConfigs;
                if (loadBalancedConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify loadBalanceEndpoints field for " +
                            envParams.get("Name").getAsString() + " and continue...");
                } else {
                    loadBalancedConfigs = loadBalancedConfigElement.getAsJsonObject();

                }

                updatedRESTEndpointParams.addProperty("endpoint_type", "load_balance");
                updatedRESTEndpointParams.addProperty("algoClassName", "org.apache.synapse.endpoints.algorithms.RoundRobin");
                // If the user has specified this as "transport", this should be removed.
                // Otherwise APIM won't recognize this as "transport".
                String tt = loadBalancedConfigs.get("sessionManagement").getAsString();
                System.out.println(tt);
                if (!StringUtils.equals(loadBalancedConfigs.get("sessionManagement").getAsString(), "transport")) {
                    updatedRESTEndpointParams.add("sessionManagement", loadBalancedConfigs.get("sessionManagement"));
                }
                updatedRESTEndpointParams.add("sessionTimeOut", loadBalancedConfigs.get("sessionTimeOut"));
                handleEndpointValues(loadBalancedConfigs, updatedRESTEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);

            } else if (StringUtils.equals(routingPolicy, "failover")) {  //if the routing policy is specified and it is failover

                //get failover configs from params
                JsonElement failoverConfigElement = envParams.get("FailoverEndpoints");
                JsonObject failoverConfigs;
                if (failoverConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify failoverEndpoints field for " +
                            envParams.get("Name").getAsString() + " and continue...");
                } else {
                    failoverConfigs = failoverConfigElement.getAsJsonObject();
                }
                updatedRESTEndpointParams.addProperty("endpoint_type", "failover");
                updatedRESTEndpointParams.addProperty("failOver", Boolean.TRUE.toString());
                updatedRESTEndpointParams.add("production_failovers", failoverConfigs.get("production_failovers"));
                updatedRESTEndpointParams.add("sandbox_failovers", failoverConfigs.get("sandbox_failovers"));
                handleEndpointValues(failoverConfigs, updatedRESTEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
            }
            multipleEndpointsConfig = updatedRESTEndpointParams;
        }

        // if endpoint type is HTTP/SOAP
        if (StringUtils.equals(endpointType, "soap")) {

            JsonObject updatedSOAPEndpointParams = new JsonObject();
            JsonObject endpoints = null;
            if (!envParams.get("Endpoints").isJsonNull()) {
                endpoints = envParams.get("Endpoints").getAsJsonObject();
            }
            // if the endpoint routing policy is not specified, but the endpoints field is specified
            if (StringUtils.isEmpty(routingPolicy)) {
                updatedSOAPEndpointParams.addProperty("endpoint_type", "soap");
                handleEndpointValues(endpoints, updatedSOAPEndpointParams, defaultProductionEndpoint, defaultSandboxEndpoint);
            } else if (StringUtils.equals(routingPolicy, "load_balanced")) {    // if the endpoint routing policy is specified as load balanced

                //get load balanced configs from params
                JsonElement loadBalancedConfigElement = envParams.get("LoadBalanceEndpoints");
                JsonObject loadBalancedConfigs;
                if (loadBalancedConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify loadBalanceEndpoints field for " +
                            envParams.get("Name").getAsString() + " and continue...");
                } else {
                    loadBalancedConfigs = loadBalancedConfigElement.getAsJsonObject();

                }
                updatedSOAPEndpointParams.addProperty("endpoint_type", "load_balance");
                updatedSOAPEndpointParams.addProperty("algoClassName", "org.apache.synapse.endpoints.algorithms.RoundRobin");
                updatedSOAPEndpointParams.add("sessionTimeOut", loadBalancedConfigs.get("sessionTimeOut"));
                // If the user has specified this as "transport", this should be removed.
                // Otherwise APIM won't recognize this as "transport".
                if (!StringUtils.equals(loadBalancedConfigs.get("sessionManagement").getAsString(), "transport")) {
                    updatedSOAPEndpointParams.add("sessionManagement", loadBalancedConfigs.get("sessionManagement"));
                }
                updatedSOAPEndpointParams.add("production_endpoints", handleSoapFailoverAndLoadBalancedEndpointValues
                        (loadBalancedConfigs.get("production_endpoints").getAsJsonArray()));
                updatedSOAPEndpointParams.add("sandbox_endpoints", handleSoapFailoverAndLoadBalancedEndpointValues
                        (loadBalancedConfigs.get("sandbox_endpoints").getAsJsonArray()));

            } else if (StringUtils.equals(routingPolicy, "failover")) {  //if the routing policy is specified and it is failover

                //get failover configs from params
                JsonElement failoverConfigElement = envParams.get("FailoverEndpoints");
                JsonObject failoverConfigs;
                if (failoverConfigElement.isJsonNull()) {
                    throw new APIImportExportException("Please specify failoverEndpoints field for " +
                            envParams.get("Name").getAsString() + " and continue...");
                } else {
                    failoverConfigs = failoverConfigElement.getAsJsonObject();
                }
                updatedSOAPEndpointParams.addProperty("endpoint_type", "failover");
                updatedSOAPEndpointParams.addProperty("failOver", Boolean.TRUE.toString());
                updatedSOAPEndpointParams.add("production_failovers", handleSoapFailoverAndLoadBalancedEndpointValues
                        (failoverConfigs.get("production_failovers").getAsJsonArray()));
                updatedSOAPEndpointParams.add("sandbox_failovers", handleSoapFailoverAndLoadBalancedEndpointValues
                        (failoverConfigs.get("sandbox_failovers").getAsJsonArray()));
                updatedSOAPEndpointParams.add("production_endpoints", handleSoapProdAndSandboxEndpointValues
                        (failoverConfigs.get("production_endpoints").getAsJsonObject()));
                updatedSOAPEndpointParams.add("sandbox_endpoints", handleSoapProdAndSandboxEndpointValues
                        (failoverConfigs.get("sandbox_endpoints").getAsJsonObject()));
            }
            multipleEndpointsConfig = updatedSOAPEndpointParams;
        }

        return multipleEndpointsConfig;
    }

    private static void handleEndpointValues(JsonObject endpointConfigs, JsonObject updatedEndpointParams,
                                             JsonObject defaultProductionEndpoint, JsonObject defaultSandboxEndpoint) {
        //check api params file to get provided endpoints
        if (endpointConfigs == null) {
            updatedEndpointParams.add("production_endpoints", defaultProductionEndpoint);
            updatedEndpointParams.add("sandbox_endpoints", defaultSandboxEndpoint);
        } else {
            //handle production endpoints
            if (endpointConfigs.get("production_endpoints") == null) {
                updatedEndpointParams.add("production_endpoints", defaultProductionEndpoint);
            } else {
                updatedEndpointParams.add("production_endpoints", endpointConfigs.get("production_endpoints"));
            }
            //handle sandbox endpoints
            if (endpointConfigs.get("sandbox_endpoints") == null) {
                updatedEndpointParams.add("sandbox_endpoints", defaultSandboxEndpoint);
            } else {
                updatedEndpointParams.add("sandbox_endpoints", endpointConfigs.get("sandbox_endpoints"));
            }
        }
    }

    private static JsonArray handleSoapFailoverAndLoadBalancedEndpointValues(JsonArray failoverEndpoints) {

        for (JsonElement endpoint : failoverEndpoints) {
            JsonObject endpointObject = endpoint.getAsJsonObject();
            endpointObject.addProperty("endpoint_type", "address");
        }
        return failoverEndpoints;
    }

    private static JsonObject handleSoapProdAndSandboxEndpointValues(JsonObject soapEndpoint) {

        soapEndpoint.addProperty("endpoint_type", "address");
        return soapEndpoint;
    }

    private static void handleClientCertificates(JsonArray certificates, APIIdentifier apiIdentifier, String pathToArchive) throws IOException {

        JsonArray updatedCertificates = new JsonArray();

        for (JsonElement certificate : certificates) {
            JsonObject certObject = certificate.getAsJsonObject();
            String alias = certObject.get("alias").getAsString();
            // add api identifier details
            JsonObject jsonApiIdentifier = new JsonObject();
            jsonApiIdentifier.addProperty("apiName", apiIdentifier.getApiName());
            jsonApiIdentifier.addProperty("version", apiIdentifier.getVersion());
            jsonApiIdentifier.addProperty("providerName", apiIdentifier.getProviderName());
            certObject.add("apiIdentifier", jsonApiIdentifier);
            //replace certificate element with updated info
            updatedCertificates.add(certObject);

            //create certificate in the path
            String content = "-----BEGIN CERTIFICATE-----\n" + certObject.get("certificate").getAsString()
                    + "\n-----END CERTIFICATE-----";
            String certPath = pathToArchive + "/Client-certificates/" + alias + ".crt";
            generateFiles(certPath, content);
        }

        //generate meta-data yaml file
        String yamlContent = CommonUtil.jsonToYaml(updatedCertificates.toString());
        generateFiles(pathToArchive + "/Client-certificates/client_certificates.yaml", yamlContent);

    }

    private static void handleEndpointCertificates(JsonArray certificates, String pathToArchive) throws IOException {


        for (JsonElement certificate : certificates) {
            //create certificate in the path
            JsonObject certObject = certificate.getAsJsonObject();
            String alias = certObject.get("alias").getAsString();
            String content = "-----BEGIN CERTIFICATE-----\n" + certObject.get("certificate").getAsString()
                    + "\n-----END CERTIFICATE-----";
            String certPath = pathToArchive + "/Endpoint-certificates/" + alias + ".crt";
            generateFiles(certPath, content);
        }

        //generate meta-data yaml file
        String yamlContent = CommonUtil.jsonToYaml(certificates.toString());
        generateFiles(pathToArchive + "/Endpoint-certificates/endpoint_certificates.yaml", yamlContent);
    }


    //later move this to CommonUtil.java
    public static void generateFiles(String filePath, String content) throws IOException {
        FileOutputStream fos = null;
        File file;
        try {
            //Specify the file path here
            file = new File(filePath);
            fos = new FileOutputStream(file);

            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] bytesArray = content.getBytes();

            fos.write(bytesArray);
            fos.flush();

        } catch (IOException e) {
            String errorMessage = "Error while generating meta information of client certificates from path.";
            throw new IOException(errorMessage, e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                String errorMessage = "Error while generating meta information of client certificates from path.";
                throw new IOException(errorMessage, e);
            }
        }
    }

}
