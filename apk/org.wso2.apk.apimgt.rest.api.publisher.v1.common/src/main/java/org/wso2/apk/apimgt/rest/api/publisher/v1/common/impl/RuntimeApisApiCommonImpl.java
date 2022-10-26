/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.publisher.v1.common.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.apk.apimgt.api.*;
import org.wso2.apk.apimgt.api.dto.APIEndpointValidationDTO;
import org.wso2.apk.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.apk.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.apk.apimgt.api.dto.EnvironmentPropertiesDTO;
import org.wso2.apk.apimgt.api.model.*;
import org.wso2.apk.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.apk.apimgt.api.model.graphql.queryanalysis.GraphqlSchemaType;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.APIManagerFactory;
import org.wso2.apk.apimgt.impl.definitions.AsyncApiParserUtil;
import org.wso2.apk.apimgt.impl.definitions.OAS3Parser;
import org.wso2.apk.apimgt.impl.definitions.OASParserUtil;
import org.wso2.apk.apimgt.impl.restapi.Constants;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.mappings.*;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.crypto.CryptoTool;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.crypto.CryptoToolException;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.crypto.CryptoToolUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.publisher.v1.common.mappings.*;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RuntimeApisApiCommonImpl {

    public static final String MESSAGE = "message";
    public static final String ERROR_WHILE_UPDATING_API = "Error while updating API : ";

    private RuntimeApisApiCommonImpl() {

    }

    private static final Log log = LogFactory.getLog(RuntimeApisApiCommonImpl.class);
    private static final String HTTP_STATUS_LOG = "HTTP status ";
    private static final String AUDIT_ERROR = "Error while parsing the audit response";

    public static Object getAllRuntimeAPIs(Integer limit, Integer offset, String sortBy, String sortOrder, String query,
                                           String organization) throws APIManagementException {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        sortBy = sortBy != null ? sortBy : RestApiConstants.DEFAULT_SORT_CRITERION;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;

        //revert content search back to normal search by name to avoid doc result complexity and to comply with
        // REST api practices
        if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
            query = query
                    .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                            APIConstants.NAME_TYPE_PREFIX + ":");
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        Map<String, Object> result;

        result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit, sortBy, sortOrder);

        Set<API> apis = (Set<API>) result.get("apis");
        allMatchedApis.addAll(apis);

        apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);

        //Add pagination section in the response
        Object totalLength = result.get("length");
        int length = 0;
        if (totalLength != null) {
            length = (Integer) totalLength;
        }

        APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);
        return apiListDTO;
    }



    public static GraphQLQueryComplexityInfoDTO getGraphQLPolicyComplexityOfRuntimeAPI(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.GRAPHQL_API, api.getType());

        String currentApiUuid;
        // Resolve whether an API or a corresponding revision
        APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
        } else {
            currentApiUuid = apiId;
        }
        GraphqlComplexityInfo graphqlComplexityInfo = apiProvider.getComplexityDetails(currentApiUuid);
        return GraphqlQueryAnalysisMappingUtil.fromGraphqlComplexityInfotoDTO(graphqlComplexityInfo);
    }


    public static GraphQLSchemaDTO getRuntimeAPIGraphQLSchema(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        APIIdentifier apiIdentifier;
        if (apiProvider.checkAPIUUIDIsARevisionUUID(apiId) != null) {
            apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId, organization).getId();
        } else {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        }
        String schemaContent = apiProvider.getGraphqlSchemaDefinition(apiId, organization);
        GraphQLSchemaDTO dto = new GraphQLSchemaDTO();
        dto.setSchemaDefinition(schemaContent);
        dto.setName(apiIdentifier.getProviderName() + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR +
                apiIdentifier.getApiName() + apiIdentifier.getVersion() + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION);
        return dto;
    }




    private static void validateRUntimeAPIOperationsPerLC(String status, String[] tokenScopes) throws APIManagementException {

        boolean updatePermittedForPublishedDeprecated = false;

        for (String scope : tokenScopes) {
            if (RestApiConstants.PUBLISHER_SCOPE.equals(scope)
                    || RestApiConstants.API_IMPORT_EXPORT_SCOPE.equals(scope)
                    || RestApiConstants.API_MANAGE_SCOPE.equals(scope)
                    || RestApiConstants.ADMIN_SCOPE.equals(scope)) {
                updatePermittedForPublishedDeprecated = true;
                break;
            }
        }
        if (!updatePermittedForPublishedDeprecated && (
                APIConstants.PUBLISHED.equals(status)
                        || APIConstants.DEPRECATED.equals(status))) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.API_UPDATE_FORBIDDEN_PER_LC, status));
        }
    }

    public static GraphQLSchemaTypeListDTO getGraphQLPolicyComplexityTypesOfRuntimeAPI(String apiId, String organization)
            throws APIManagementException {

        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.GRAPHQL_API, api.getType());
        String schemaContent = apiProvider.getGraphqlSchemaDefinition(apiId, organization);
        List<GraphqlSchemaType> typeList = graphql.extractGraphQLTypeList(schemaContent);
        return GraphqlQueryAnalysisMappingUtil.fromGraphqlSchemaTypeListtoDTO(typeList);
    }

    /**
     * @param apiId        API ID
     * @param organization Organization
     * @return JSONObject with arns
     * @throws SdkClientException     if AWSLambda SDK throws an error
     * @throws APIManagementException
     */
    public static JSONObject getAmazonResourceNamesOfRuntimeAPI(String apiId, String organization)
            throws SdkClientException, APIManagementException {

        JSONObject arns = new JSONObject();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        String endpointConfigString = api.getEndpointConfig();
        if (StringUtils.isNotEmpty(endpointConfigString)) {
            JSONParser jsonParser = new JSONParser();
            JSONObject endpointConfig;
            try {
                endpointConfig = (JSONObject) jsonParser.parse(endpointConfigString);
            } catch (ParseException e) {
                throw new APIManagementException("Error while parsing endpoint config",
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
            if (endpointConfig != null
                    && endpointConfig.containsKey(APIConstants.AMZN_ACCESS_KEY)
                    && endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)
                    && endpointConfig.containsKey(APIConstants.AMZN_REGION)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_ARN)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_SESSION_NAME)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_REGION)) {
                String accessKey = (String) endpointConfig.get(APIConstants.AMZN_ACCESS_KEY);
                String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                String region = (String) endpointConfig.get(APIConstants.AMZN_REGION);
                String roleArn = (String) endpointConfig.get(APIConstants.AMZN_ROLE_ARN);
                String roleSessionName = (String) endpointConfig.get(APIConstants.AMZN_ROLE_SESSION_NAME);
                String roleRegion = (String) endpointConfig.get(APIConstants.AMZN_ROLE_REGION);
                try {
                    AWSLambda awsLambdaClient = getAWSLambdaClient(accessKey, secretKey, region,
                            roleArn, roleSessionName, roleRegion);
                    if (awsLambdaClient == null) {
                        return (JSONObject) Collections.emptyMap();
                    }
                    ListFunctionsResult listFunctionsResult = awsLambdaClient.listFunctions();
                    List<FunctionConfiguration> functionConfigurations = listFunctionsResult.getFunctions();
                    arns.put("count", functionConfigurations.size());
                    JSONArray list = new JSONArray();
                    for (FunctionConfiguration functionConfiguration : functionConfigurations) {
                        list.put(functionConfiguration.getFunctionArn());
                    }
                    arns.put("list", list);
                    return arns;
                } catch (CryptoToolException e) {
                    throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.ENDPOINT_CRYPTO_ERROR,
                            "Error while decrypting AWS Lambda secret key"));
                }
            }
        }
        return (JSONObject) Collections.emptyMap();
    }

    /**
     * @param accessKey       AWS access key
     * @param secretKey       AWS secret key
     * @param region          AWS region
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     */
    private static AWSLambda getAWSLambdaClient(String accessKey, String secretKey, String region,
                                                String roleArn, String roleSessionName, String roleRegion)
            throws CryptoToolException {

        AWSLambda awsLambdaClient;
        if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
            awsLambdaClient = getARNsWithIAMRole(roleArn, roleSessionName, roleRegion);
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey) &&
                StringUtils.isNotEmpty(region)) {
            awsLambdaClient = getARNsWithStoredCredentials(accessKey, secretKey, region,
                    roleArn, roleSessionName, roleRegion);
            return awsLambdaClient;
        } else {
            log.error("Missing AWS Credentials");
            return null;
        }
    }

    /**
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     */
    private static AWSLambda getARNsWithIAMRole(String roleArn, String roleSessionName, String roleRegion) {

        AWSLambda awsLambdaClient;
        if (log.isDebugEnabled()) {
            log.debug("Using temporary credentials supplied by the IAM role attached to AWS " +
                    "instance");
        }
        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                && StringUtils.isEmpty(roleRegion)) {
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .build();
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                && StringUtils.isNotEmpty(roleRegion)) {
            String stsRegion = String.valueOf(Regions.getCurrentRegion());
            AWSSecurityTokenService awsSTSClient;
            if (StringUtils.isEmpty(stsRegion)) {
                awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                        .build();
            } else {
                awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://sts."
                                + stsRegion + ".amazonaws.com", stsRegion))
                        .build();
            }
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult assumeRoleResult = awsSTSClient.assumeRole(roleRequest);
            Credentials sessionCredentials = assumeRoleResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(roleRegion)
                    .build();
            return awsLambdaClient;
        } else {
            log.error("Missing AWS STS configurations");
            return null;
        }
    }

    /**
     * @param accessKey       AWS access key
     * @param secretKey       AWS secret key
     * @param region          AWS region
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     */
    private static AWSLambda getARNsWithStoredCredentials(String accessKey, String secretKey, String region,
                                                          String roleArn, String roleSessionName, String roleRegion)
            throws CryptoToolException {

        AWSLambda awsLambdaClient;
        if (log.isDebugEnabled()) {
            log.debug("Using user given stored credentials");
        }
        if (secretKey.length() == APIConstants.AWS_ENCRYPTED_SECRET_KEY_LENGTH) {
            CryptoTool cryptoTool = CryptoToolUtil.getDefaultCryptoTool();
            secretKey = new String(cryptoTool.base64DecodeAndDecrypt(secretKey),
                    StandardCharsets.UTF_8);
        }
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                && StringUtils.isEmpty(roleRegion)) {
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(region)
                    .build();
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                && StringUtils.isNotEmpty(roleRegion)) {
            AWSSecurityTokenService awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://sts."
                            + region + ".amazonaws.com", region))
                    .build();
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult assumeRoleResult = awsSTSClient.assumeRole(roleRequest);
            Credentials sessionCredentials = assumeRoleResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(roleRegion)
                    .build();
            return awsLambdaClient;
        } else {
            log.error("Missing AWS STS configurations");
            return null;
        }
    }

    public static AuditReportDTO getAuditReportOfRuntimeAPI(String apiId, String organization) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        // Get configuration file, retrieve API token and collection id
        JSONObject securityAuditPropertyObject = apiProvider.getSecurityAuditAttributesFromConfig(username);
        JSONObject responseJson = getAuditReport(api, securityAuditPropertyObject, apiDefinition, organization);
        AuditReportDTO auditReportDTO = new AuditReportDTO();
        auditReportDTO.setReport((String) responseJson.get("decodedReport"));
        auditReportDTO.setGrade((String) responseJson.get("grade"));
        auditReportDTO.setNumErrors((Integer) responseJson.get("numErrors"));
        auditReportDTO.setExternalApiId((String) responseJson.get("auditUuid"));
        return auditReportDTO;
    }

    public static Object getRuntimeAPIClientCertificateContentByAlias(String apiId, String alias, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                apiTypeWrapper, organization);
        return CertificateRestApiUtils.getDecodedCertificate(clientCertificateDTO.getCertificate());
    }



    public static CertificateInfoDTO getRuntimeAPIClientCertificateByAlias(String alias, String apiId, String organization)
            throws APIManagementException {

        CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                apiTypeWrapper, organization);
        CertificateInformationDTO certificateInformationDTO = certificateMgtUtils
                .getCertificateInfo(clientCertificateDTO.getCertificate());
        if (certificateInformationDTO != null) {
            return CertificateMappingUtil.fromCertificateInformationToDTO(certificateInformationDTO);
        } else {
            throw new APIManagementException("Certificate is empty for alias " + alias,
                    ExceptionCodes.from(ExceptionCodes.CERT_NOT_FOUND, alias));
        }
    }


    public static ClientCertificatesDTO getRuntimeAPIClientCertificates(String apiId, Integer limit, Integer offset,
                                                                        String alias, String organization)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<ClientCertificateDTO> certificates = new ArrayList<>();
        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "apiId", apiId);

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        int totalCount = apiProvider.getClientCertificateCount(tenantId);
        if (totalCount > 0) {
            APIIdentifier apiIdentifier = null;
            if (StringUtils.isNotEmpty(apiId)) {
                API api = apiProvider.getAPIbyUUID(apiId, organization);
                apiIdentifier = api.getId();
            }
            certificates = apiProvider.searchClientCertificates(tenantId, alias, apiIdentifier, organization);
        }

        ClientCertificatesDTO certificatesDTO = CertificateRestApiUtils
                .getPaginatedClientCertificates(certificates, limit, offset, query);
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(totalCount);
        certificatesDTO.setPagination(paginationDTO);
        return certificatesDTO;
    }


    /**
     * @param api                         API
     * @param securityAuditPropertyObject audit security properties
     * @param apiDefinition               API definition
     * @param organization                user organization
     * @return JSONObject containing audit response
     * @throws APIManagementException when there's an unexpected response
     * @throws IOException            when http client fails
     */
    public static JSONObject getAuditReport(API api, JSONObject securityAuditPropertyObject,
                                            String apiDefinition, String organization)
            throws APIManagementException {

        boolean isDebugEnabled = log.isDebugEnabled();
        APIIdentifier apiIdentifier = api.getId();
        String apiToken = (String) securityAuditPropertyObject.get("apiToken");
        String collectionId = (String) securityAuditPropertyObject.get("collectionId");
        String baseUrl = (String) securityAuditPropertyObject.get("baseUrl");

        if (baseUrl == null) {
            baseUrl = APIConstants.BASE_AUDIT_URL;
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // Retrieve the uuid from the database
        String auditUuid = apiProvider.getAuditApiId(api.getUuid());
        if (auditUuid != null) {
            updateAuditApi(apiDefinition, apiToken, auditUuid, baseUrl, isDebugEnabled);
        } else {
            auditUuid = createAuditApi(collectionId, apiToken, apiIdentifier, apiDefinition, baseUrl,
                    isDebugEnabled, organization);
        }
        String getUrl = baseUrl + "/" + auditUuid + APIConstants.ASSESSMENT_REPORT;

        try {
            URL getReportUrl = new URL(getUrl);
            CloseableHttpClient getHttpClient = (CloseableHttpClient) APIUtil
                    .getHttpClient(getReportUrl.getPort(), getReportUrl.getProtocol());
            HttpGet httpGet = new HttpGet(getUrl);
            // Set the header properties of the request
            httpGet.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            httpGet.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
            httpGet.setHeader(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
            // Code block for the processing of the response
            try (CloseableHttpResponse response = getHttpClient.execute(httpGet)) {
                if (isDebugEnabled) {
                    log.debug(HTTP_STATUS_LOG + response.getStatusLine().getStatusCode());
                }
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    String inputLine;
                    StringBuilder responseString = new StringBuilder();

                    while ((inputLine = reader.readLine()) != null) {
                        responseString.append(inputLine);
                    }
                    reader.close();
                    JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
                    String report = responseJson.get(APIConstants.DATA).toString();
                    String grade = (String) ((JSONObject) ((JSONObject) responseJson.get(APIConstants.ATTR))
                            .get(APIConstants.DATA)).get(APIConstants.GRADE);
                    Integer numErrors = Integer.valueOf(
                            (String) ((JSONObject) ((JSONObject) responseJson.get(APIConstants.ATTR))
                                    .get(APIConstants.DATA)).get(APIConstants.NUM_ERRORS));
                    String decodedReport = new String(Base64Utils.decode(report), StandardCharsets.UTF_8);
                    JSONObject output = new JSONObject();
                    output.put("decodedReport", decodedReport);
                    output.put("grade", grade);
                    output.put("numErrors", numErrors);
                    output.put("auditUuid", auditUuid);
                    return output;
                }
            }
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INTERNAL_ERROR);
        } catch (ParseException e) {
            throw new APIManagementException(AUDIT_ERROR, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return (JSONObject) Collections.emptyMap();
    }


    /**
     * @param outputStream  HTTP output stream
     * @param apiIdentifier API Identifier object
     * @param apiDefinition API Definition of API
     * @param collectionId  Collection ID in which the Definition should be sent to
     */
    private static void writeAuditResponse(OutputStream outputStream, APIIdentifier apiIdentifier,
                                           String apiDefinition, String collectionId) {

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
        // Name property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"name\"")
                .append(APIConstants.MULTIPART_LINE_FEED).append(APIConstants.MULTIPART_LINE_FEED)
                .append(apiIdentifier.getApiName()).append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        // Specfile property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"specfile\"; filename=\"swagger.json\"")
                .append(APIConstants.MULTIPART_LINE_FEED)
                .append(APIConstants.HEADER_CONTENT_TYPE + ": " + APIConstants.APPLICATION_JSON_MEDIA_TYPE)
                .append(APIConstants.MULTIPART_LINE_FEED).append(APIConstants.MULTIPART_LINE_FEED)
                .append(apiDefinition).append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        // CollectionID property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"cid\"").append(APIConstants.MULTIPART_LINE_FEED)
                .append(APIConstants.MULTIPART_LINE_FEED).append(collectionId)
                .append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY + "--")
                .append(APIConstants.MULTIPART_LINE_FEED);
        writer.close();
    }

    private static void handleAuditCreateError(HttpURLConnection httpConn)
            throws IOException, ParseException, APIManagementException {

        if (httpConn.getErrorStream() != null) {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder responseString = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                responseString.append(inputLine);
            }
            reader.close();
            httpConn.disconnect();
            JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
            String errorMessage = httpConn.getResponseMessage();
            if (responseJson.containsKey(MESSAGE)) {
                errorMessage = (String) responseJson.get(MESSAGE);
            }
            throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                            httpConn.getResponseCode() + " - " + errorMessage,
                    ExceptionCodes.AUDIT_RETRIEVE_FAILED);
        } else {
            throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                            httpConn.getResponseCode() + " - " + httpConn.getResponseMessage(),
                    ExceptionCodes.AUDIT_RETRIEVE_FAILED);
        }
    }



    public static WSDLInfoDTO getWSDLInfoOfRuntimeAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        WSDLInfoDTO wsdlInfoDTO = APIMappingUtil.getWsdlInfoDTO(api);
        if (wsdlInfoDTO == null) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.NO_WSDL_AVAILABLE_FOR_API,
                            api.getId().getApiName(), api.getId().getVersion()));
        }
        return wsdlInfoDTO;
    }

    public static LifecycleHistoryDTO getRuntimeAPILifecycleHistory(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api;
        APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            api = apiProvider.getAPIbyUUID(apiRevision.getApiUUID(), organization);
        } else {
            api = apiProvider.getAPIbyUUID(apiId, organization);
        }
        return PublisherCommonUtils.getLifecycleHistoryDTO(api.getUuid(), apiProvider);
    }

    public static LifecycleStateDTO getRuntimeAPILifecycleState(String apiId, String organization)
            throws APIManagementException {

        return getLifecycleState(apiId, organization);
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId        API Id
     * @param organization organization
     * @return API Lifecycle state information
     */
    private static LifecycleStateDTO getLifecycleState(String apiId, String organization)
            throws APIManagementException {

        APIIdentifier apiIdentifier;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (apiProvider.checkAPIUUIDIsARevisionUUID(apiId) != null) {
            apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId, organization).getId();
        } else {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        }
        return PublisherCommonUtils.getLifecycleStateInformation(apiIdentifier, organization);
    }

    public static String getRuntimeAPISwagger(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return RestApiCommonUtil.retrieveSwaggerDefinition(apiId, api, apiProvider);
    }


    /**
     * Send HTTP HEAD request to test the endpoint url
     *
     * @param urlVal url for which the HEAD request is sent
     * @return APIEndpointValidationDTO Response DTO containing validity information of the HEAD request made
     * to test the endpoint url
     */
    public static ApiEndpointValidationResponseDTO validateRUntimeAPIEndpoint(String urlVal)
            throws APIManagementException {

        URL url;
        try {
            url = new URL(urlVal);
        } catch (MalformedURLException e) {
            throw new APIManagementException("URL is malformed",
                    e, ExceptionCodes.from(ExceptionCodes.URI_PARSE_ERROR, "Malformed url"));
        }
        if (url.getProtocol().matches("https")) {
//            ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();
//            String trustStorePath = serverConfig.getFirstProperty("Security.TrustStore.Location");
//            String trustStorePassword = serverConfig.getFirstProperty("Security.TrustStore.Password");
//            String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
//            String keyStoreType = serverConfig.getFirstProperty("Security.KeyStore.Type");
//            String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
            //TODO get truststore configs properly
            String trustStorePath = "Security.TrustStore.Location";
            String trustStorePassword = "Security.TrustStore.Password";
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

            //TODO get keystore configs properly
            String keyStore = "Security.KeyStore.Location";
            String keyStoreType = "Security.KeyStore.Type";
            String keyStorePassword = "Security.KeyStore.Password";
            System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
            System.setProperty("javax.net.ssl.keyStore", keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        }

        APIEndpointValidationDTO apiEndpointValidationDTO = new APIEndpointValidationDTO();
        org.apache.http.client.HttpClient client = APIUtil.getHttpClient(urlVal);
        HttpHead method = new HttpHead(urlVal);

        try {
            HttpResponse response = client.execute(method);
            apiEndpointValidationDTO.setStatusCode(response.getStatusLine().getStatusCode());
            apiEndpointValidationDTO.setStatusMessage(
                    HttpStatus.getStatusText(response.getStatusLine().getStatusCode()));
        } catch (UnknownHostException e) {
            log.error("UnknownHostException occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationDTO.setError("Unknown Host");
        } catch (IOException e) {
            log.error("Error occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationDTO.setError("Connection error");
        } finally {
            method.releaseConnection();
        }

        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO = new ApiEndpointValidationResponseDTO();
        apiEndpointValidationResponseDTO.setError("");
        apiEndpointValidationResponseDTO = APIMappingUtil.fromEndpointValidationToDTO(apiEndpointValidationDTO);
        return apiEndpointValidationResponseDTO;
    }

    public static ResourcePathListDTO getRuntimeAPIResourcePaths(String apiId, Integer limit, Integer offset)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        List<ResourcePath> apiResourcePaths = apiProvider.getResourcePathsOfAPI(apiIdentifier);

        ResourcePathListDTO dto = APIMappingUtil.fromResourcePathListToDTO(apiResourcePaths, limit, offset);
        APIMappingUtil.setPaginationParamsForAPIResourcePathList(dto, offset, limit, apiResourcePaths.size());
        return dto;
    }

    public static OpenAPIDefinitionValidationResponseDTO validateRuntimeAPIOpenAPIDefinition(Boolean returnContent, String url,
                                                                                             InputStream fileInputStream,
                                                                                             String inlineApiDefinition,
                                                                                             String fileName)
            throws APIManagementException {
        // Validate and retrieve the OpenAPI definition
        Map<String, Object> validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileName,
                inlineApiDefinition, returnContent, false);

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        if (!validationResponseDTO.isIsValid()) {
            List<ErrorListItemDTO> errors = validationResponseDTO.getErrors();
            for (ErrorListItemDTO error : errors) {
                log.error("Error while parsing OpenAPI definition. Error code: " + error.getCode() + ". Error: "
                        + error.getDescription());
            }
        }
        return validationResponseDTO;
    }

    public static WSDLValidationResponseDTO validateRuntimeAPIWSDLDefinition(String url, InputStream fileInputStream,
                                                                             String fileName) throws APIManagementException {

        Map<String, Object> validationResponseMap = validateWSDL(url, fileInputStream, fileName, false);
        return (WSDLValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
    }

    public static Map<String, Object> validateRuntimeAPIOpenAPIDefinition(String url, InputStream inputStream, String fileName,
                                                                          String apiDefinition, Boolean returnContent,
                                                                          Boolean isServiceAPI) throws APIManagementException {
        //validate inputs
        handleInvalidParams(inputStream, fileName, url, apiDefinition, isServiceAPI);

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, returnContent);
        } else if (inputStream != null) {
            try {
                if (fileName != null) {
                    if (fileName.endsWith(".zip")) {
                        validationResponse =
                                OASParserUtil.extractAndValidateOpenAPIArchive(inputStream, returnContent);
                    } else {
                        String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                    }
                } else {
                    String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                }
            } catch (IOException e) {
                throw new APIManagementException("Error while processing the file input",
                        e, ExceptionCodes.from(ExceptionCodes.OPENAPI_PARSE_EXCEPTION));
            }
        } else if (apiDefinition != null) {
            validationResponse = OASParserUtil.validateAPIDefinition(apiDefinition, returnContent);
        }

        OpenAPIDefinitionValidationResponseDTO responseDTO =
                APIMappingUtil.getOpenAPIDefinitionValidationResponseFromModel(validationResponse, returnContent);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    /**
     * @param url           URL of the OpenAPI definition
     * @param inputStream   OpenAPI definition file
     * @param apiDefinition OpenAPI definition
     * @param fileName      Filename of the definition file
     * @param returnContent Whether to return json or not
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException when file parsing fails
     */
    public static APIDefinitionValidationResponse validateRuntimeAPIOpenAPIDefinition(String url, InputStream inputStream,
                                                                                      String apiDefinition, String fileName,
                                                                                      boolean returnContent)
            throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, returnContent);
        } else if (inputStream != null) {
            try {
                if (fileName != null) {
                    if (fileName.endsWith(".zip")) {
                        validationResponse =
                                OASParserUtil.extractAndValidateOpenAPIArchive(inputStream, returnContent);
                    } else {
                        String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                    }
                } else {
                    String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                }
            } catch (IOException e) {
                throw new APIManagementException("Error while processing the file input",
                        e, ExceptionCodes.from(ExceptionCodes.OPENAPI_PARSE_EXCEPTION));
            }
        } else if (apiDefinition != null) {
            validationResponse = OASParserUtil.validateAPIDefinition(apiDefinition, returnContent);
        }

        return validationResponse;
    }

    /**
     * Validates the provided WSDL and reset the streams as required
     *
     * @param fileInputStream file input stream
     * @param fileName        File name
     * @param url             WSDL url
     * @return WSDL validation response
     * @throws APIManagementException when error occurred during the operation
     */
    private static WSDLValidationResponse validateWSDLAndReset(InputStream fileInputStream, String fileName, String url)
            throws APIManagementException {

        Map<String, Object> validationResponseMap = validateWSDL(url, fileInputStream, fileName, false);
        WSDLValidationResponse validationResponse =
                (WSDLValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (validationResponse.getWsdlInfo() == null) {
            // Validation failure
            throw new APIManagementException(validationResponse.getError());
        }
        return validationResponse;
    }


    public static ResourceFile getWSDLOfRuntimeAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        return apiProvider.getWSDL(apiId, organization);
    }

    public static void updateWSDLOfAPI(String apiId, InputStream fileInputStream, String fileName, String contentType,
                                       String url, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        WSDLValidationResponse validationResponse = validateWSDLAndReset(fileInputStream, fileName, url);
        if (StringUtils.isNotBlank(url)) {
            apiProvider.addWSDLResource(apiId, null, url, organization);
        } else {
            ByteArrayInputStream wsdl = validationResponse.getWsdlProcessor().getWSDL();
            ResourceFile wsdlResource = ApisApiCommonImpl.getWSDLResource(wsdl, contentType);
            apiProvider.addWSDLResource(apiId, wsdlResource, null, organization);
        }
    }


    public static AsyncAPISpecificationValidationResponseDTO validateRUntimeAsyncAPISpecification(Boolean returnContent,
                                                                                                  String url,
                                                                                                  InputStream fileInputStream,
                                                                                                  String fileName)
            throws APIManagementException {
        //validate and retrieve the AsyncAPI specification
        Map<String, Object> validationResponseMap = validateAsyncAPISpecification(url, fileInputStream, fileName,
                returnContent, false);
        return (AsyncAPISpecificationValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
    }

    /**
     * Validate the provided AsyncAPI specification (via file or url) and return a Map with the validation response
     * information
     *
     * @param url             AsyncAPI specification url
     * @param fileInputStream file as input stream
     * @param returnContent   whether to return the content of the definition in the response DTO
     * @param isServiceAPI    whether the request is to create API from a service in Service Catalog
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     * of type AsyncAPISpecificationValidationResponseDTO for the REST API. A value with the key 'model' will have the
     * validation response of type APIDefinitionValidationResponse coming from the impl level
     */
    private static Map<String, Object> validateRuntimeAsyncAPISpecification(String url, InputStream fileInputStream,
                                                                            String fileName, Boolean returnContent,
                                                                            Boolean isServiceAPI)
            throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileName, url, null, isServiceAPI);

        AsyncAPISpecificationValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        if (url != null) {
            //validate URL
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(url, returnContent);
        } else if (fileInputStream != null) {
            //validate file
            if (fileName == null) {
                fileName = StringUtils.EMPTY;
            }
            String schemaToBeValidated = ApisApiCommonImpl.getSchemaToBeValidated(fileInputStream, isServiceAPI,
                    fileName);
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecification(schemaToBeValidated, returnContent);
        }

        responseDTO = APIMappingUtil.getAsyncAPISpecificationValidationResponseFromModel(validationResponse,
                returnContent);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    public static APIKeyDTO generateInternalAPIKey(String apiId) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);
        String token = apiProvider.generateApiKey(apiId);
        APIKeyDTO apiKeyDTO = new APIKeyDTO();
        apiKeyDTO.setApikey(token);
        apiKeyDTO.setValidityTime(60 * 1000);
        return apiKeyDTO;
    }


    public static GraphQLValidationResponseDTO validateRuntimeGraphQLSchema(InputStream fileInputStream, String filename) {

        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();

        try {
            String schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
            validationResponse = PublisherCommonUtils.validateGraphQLSchema(filename, schema);
        } catch (IOException | APIManagementException e) {
            validationResponse.setIsValid(false);
            validationResponse.setErrorMessage(e.getMessage());
        }
        return validationResponse;
    }

    public static List<Tier> getAPISubscriptionPolicies(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIDTO apiInfo = getAPIByID(apiId, apiProvider, organization);
        List<Tier> availableThrottlingPolicyList = ThrottlingPoliciesApiCommonImpl
                .getThrottlingPolicyList(ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(),
                        true);

        List<String> apiPolicies = apiInfo.getPolicies();
        return filterAPIThrottlingPolicies(apiPolicies, availableThrottlingPolicyList);
    }

    public static APIRevisionListDTO getRuntimeAPIRevisions(String apiId, String query) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIRevisionListDTO apiRevisionListDTO;
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        List<APIRevision> apiRevisionsList = filterAPIRevisionsByDeploymentStatus(query, apiRevisions);
        apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisionsList);
        return apiRevisionListDTO;
    }


    public static List<APIRevisionDeploymentDTO> getRuntimeAPIRevisionDeployments(String apiId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<APIRevisionDeployment> apiRevisionDeploymentsList = apiProvider.getAPIRevisionsDeploymentList(apiId);

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsList) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return apiRevisionDeploymentDTOS;
    }


    public static String getRuntimeAsyncAPIDefinition(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return RestApiCommonUtil.retrieveAsyncAPIDefinition(api, apiProvider);
    }


    public static String getEnvironmentSpecificAPIProperties(String apiId, String envId)
            throws APIManagementException {

        // validate api UUID
        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // get properties
        EnvironmentPropertiesDTO properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        return new Gson().toJson(properties);
    }


    /**
     * @param fileInputStream          File input stream for the WSDL file
     * @param url                      URL
     * @param wsdlArchiveExtractedPath Path to WSDL extracted directory
     * @param filename                 File Name
     * @return Swagger string
     * @throws APIManagementException If the WSDL file not supported
     * @throws IOException            If error occurred in converting InputStream to a byte array
     */
    public static String getRuntimeSwaggerString(InputStream fileInputStream, String url, String wsdlArchiveExtractedPath,
                                                 String filename) throws APIManagementException, IOException {

        String swaggerStr = "";
        if (StringUtils.isNotBlank(url)) {
            swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(url);
        } else if (fileInputStream != null) {
            if (filename.endsWith(".zip")) {
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlArchiveExtractedPath);
            } else if (filename.endsWith(".wsdl")) {
                byte[] wsdlContent = APIUtil.toByteArray(fileInputStream);
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlContent);
            } else {
                throw new APIManagementException(ExceptionCodes.UNSUPPORTED_WSDL_FILE_EXTENSION);
            }
        }
        return swaggerStr;
    }

    /**
     * @param wsdlInputStream WSDL file input stream
     * @param contentType     content type of the wsdl
     * @return Resource file WSDL
     */
    public static ResourceFile getRUntimeAPIWSDLResource(InputStream wsdlInputStream, String contentType) {

        ResourceFile wsdlResource;
        if (APIConstants.APPLICATION_ZIP.equals(contentType) ||
                APIConstants.APPLICATION_X_ZIP_COMPRESSED.equals(contentType)) {
            wsdlResource = new ResourceFile(wsdlInputStream, APIConstants.APPLICATION_ZIP);
        } else {
            wsdlResource = new ResourceFile(wsdlInputStream, contentType);
        }
        return wsdlResource;
    }

    /**
     * @param api API
     * @return API definition
     * @throws APIManagementException If any error occurred in generating API definition from swagger data
     */
    private static String getRuntimeApiDefinition(API api) throws APIManagementException {

        APIDefinition parser = new OAS3Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        return parser.generateAPIDefinition(swaggerData);
    }

    /**
     * @param apiPolicies                   Policy names applied to the API
     * @param availableThrottlingPolicyList All available policies
     * @return Filtered API policy list which are applied to the API
     */
    public static List<Tier> filterRuntimeAPIThrottlingPolicies(List<String> apiPolicies,
                                                                List<Tier> availableThrottlingPolicyList) {

        List<Tier> apiThrottlingPolicies = new ArrayList<>();
        if (apiPolicies != null && !apiPolicies.isEmpty()) {
            for (Tier tier : availableThrottlingPolicyList) {
                if (apiPolicies.contains(tier.getName())) {
                    apiThrottlingPolicies.add(tier);
                }
            }
        }
        return apiThrottlingPolicies;
    }

    /**
     * @param deploymentStatus Deployment status [deployed:true / deployed:false]
     * @param apiRevisions     API revisions list
     * @return Filtered API revisions according to the deploymentStatus
     */
    public static List<APIRevision> filterRuntimeAPIRevisionsByDeploymentStatus(String deploymentStatus,
                                                                                List<APIRevision> apiRevisions) {

        if ("deployed:true".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiDeployedRevisions.add(apiRevision);
                }
            }
            return apiDeployedRevisions;
        } else if ("deployed:false".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiNotDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiNotDeployedRevisions.add(apiRevision);
                }
            }
            return apiNotDeployedRevisions;
        }
        return apiRevisions;
    }

    /**
     * @param revisionId         Revision ID
     * @param environments       Environments of the organization
     * @param environment        Selected environment
     * @param displayOnDevportal Enable display on Developer Portal
     * @param vhost              Virtual Host of the revision deployment
     * @param mandatoryVHOST     Is vhost mandatory in this validation
     * @return Created {@link APIRevisionDeployment} after validations
     * @throws APIManagementException if any validation fails
     */
    public static APIRevisionDeployment mapRuntimeAPIRevisionDeploymentWithValidation(String revisionId,
                                                                                      Map<String, Environment> environments,
                                                                                      String environment,
                                                                                      Boolean displayOnDevportal,
                                                                                      String vhost, boolean mandatoryVHOST)
            throws APIManagementException {

        if (environments.get(environment) == null) {
            final String errorMessage = "Gateway environment not found: " + environment;
            throw new APIManagementException(errorMessage, ExceptionCodes.from(
                    ExceptionCodes.INVALID_GATEWAY_ENVIRONMENT, String.format("name '%s'", environment)));

        }
        if (mandatoryVHOST && StringUtils.isEmpty(vhost)) {
            // vhost is only required when deploying a revision, not required when un-deploying a revision
            // since the same scheme 'APIRevisionDeployment' is used for deploy and undeploy, handle it here.
            throw new APIManagementException("Required field 'vhost' not found in deployment",
                    ExceptionCodes.GATEWAY_ENVIRONMENT_VHOST_NOT_PROVIDED);
        }
        return mapApiRevisionDeployment(revisionId, vhost, displayOnDevportal, environment);
    }

    public static APIRevisionDeployment updateApiRevisionDeployment(String apiId, String deploymentId,
                                                                    String revisionId, String vhost,
                                                                    Boolean displayOnDevportal)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String decodedDeploymentName = ApisApiCommonImpl.getDecodedDeploymentName(deploymentId);
        APIRevisionDeployment apiRevisionDeployment = ApisApiCommonImpl.mapApiRevisionDeployment(revisionId, vhost,
                displayOnDevportal, decodedDeploymentName);
        apiProvider.updateAPIDisplayOnDevportal(apiId, revisionId, apiRevisionDeployment);
        return apiProvider.getAPIRevisionDeployment(decodedDeploymentName, revisionId);
    }

    /**
     * @param revisionId         Revision ID
     * @param vhost              Virtual Host
     * @param displayOnDevportal Enable displaying on Developer Portal
     * @param deployment         Deployment
     * @return Mapped {@link APIRevisionDeployment}
     */
    public static APIRevisionDeployment mapRuntimeApiRevisionDeployment(String revisionId, String vhost,
                                                                        Boolean displayOnDevportal, String deployment) {

        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        apiRevisionDeployment.setRevisionUUID(revisionId);
        apiRevisionDeployment.setDeployment(deployment);
        apiRevisionDeployment.setVhost(vhost);
        apiRevisionDeployment.setDisplayOnDevportal(displayOnDevportal);
        return apiRevisionDeployment;
    }

    /**
     * @param deploymentId Deployment ID
     * @return Deployment name decoded from the deploymentId
     * @throws APIMgtResourceNotFoundException If invalid or null deploymentId
     */
    public static String getDecodedDeploymentName(String deploymentId) throws APIMgtResourceNotFoundException {

        String decodedDeploymentName;
        if (deploymentId != null) {
            try {
                decodedDeploymentName = new String(Base64.getUrlDecoder().decode(deploymentId),
                        StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new APIMgtResourceNotFoundException("deployment with " + deploymentId +
                        " not found", ExceptionCodes.from(ExceptionCodes.EXISTING_DEPLOYMENT_NOT_FOUND,
                        deploymentId));
            }
        } else {
            throw new APIMgtResourceNotFoundException("deployment id not found",
                    ExceptionCodes.from(ExceptionCodes.DEPLOYMENT_ID_NOT_FOUND));
        }
        return decodedDeploymentName;
    }

    /**
     * @param fileInputStream API spec file input stream
     * @param isServiceAPI    Is service API
     * @param fileName        File name
     * @return Schema
     * @throws APIManagementException if error while reading the spec file contents
     */
    public static String getSchemaToBeValidated(InputStream fileInputStream, Boolean isServiceAPI, String fileName)
            throws APIManagementException {

        String schemaToBeValidated = null;
        if (Boolean.TRUE.equals(isServiceAPI) || fileName.endsWith(APIConstants.YAML_FILE_EXTENSION) || fileName
                .endsWith(APIConstants.YML_FILE_EXTENSION)) {
            //convert .yml or .yaml to JSON for validation
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            try {
                Object obj = yamlReader.readValue(fileInputStream, Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                schemaToBeValidated = jsonWriter.writeValueAsString(obj);
            } catch (IOException e) {
                throw new APIManagementException("Error while reading file content", e,
                        ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
            }
        } else if (fileName.endsWith(APIConstants.JSON_FILE_EXTENSION)) {
            //continue with .json
            JSONTokener jsonDataFile = new JSONTokener(fileInputStream);
            schemaToBeValidated = new org.json.JSONObject(jsonDataFile).toString();
        }
        return schemaToBeValidated;
    }


    /**
     * @param service service entry
     * @return service info JSON Object
     */
    public static JSONObject getServiceInfo(ServiceEntry service) {

        JSONObject serviceInfo = new JSONObject();
        serviceInfo.put("name", service.getName());
        serviceInfo.put("version", service.getVersion());
        serviceInfo.put("key", service.getServiceKey());
        serviceInfo.put("md5", service.getMd5());
        return serviceInfo;
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return A list of API resource mediation policies with mock scripts
     * @throws APIManagementException when an internal errors occurs
     */
    public static MockResponsePayloadListDTO getGeneratedMockScriptsOfAPI(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        Map<String, Object> examples = OASParserUtil.generateExamples(apiDefinition);

        List<APIResourceMediationPolicy> policiesList =
                (List<APIResourceMediationPolicy>) examples.get(APIConstants.MOCK_GEN_POLICY_LIST);
        return APIMappingUtil.fromMockPayloadsToListDTO(policiesList);
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return Monetized policies to plan mapping
     * @throws APIManagementException when an internal error occurs
     */
    public static APIMonetizationInfoDTO getRuntimeAPIMonetization(String apiId, String organization)
            throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when retrieving monetized plans.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        String uuid = RestApiCommonUtil.getAPIUUID(apiId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        Map<String, String> monetizedPoliciesToPlanMapping;
        try {
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            monetizedPoliciesToPlanMapping = monetizationImplementation.getMonetizedPoliciesToPlanMapping(api);
        } catch (MonetizationException e) {
            throw new APIManagementException("Error occurred while getting the Monetization mappings for API "
                    + api.getId().getApiName(), e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Error occurred while getting the Monetization mappings for API"));
        }
        return APIMappingUtil.getMonetizedTiersDTO(uuid, organization, monetizedPoliciesToPlanMapping);
    }


    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return A map of revenue details
     * @throws APIManagementException when retrieving monetization details fail
     */
    public static APIRevenueDTO getRuntimeAPIRevenue(String apiId, String organization) throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when getting revenue details.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
            String errorMessage = "API " + api.getId().getApiName() +
                    " should be in published state to configure monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_API_STATE_MONETIZATION);
        }

        try {
            Map<String, String> revenueUsageData = monetizationImplementation.getTotalRevenue(api, apiProvider);
            APIRevenueDTO apiRevenueDTO = new APIRevenueDTO();
            apiRevenueDTO.setProperties(revenueUsageData);
            return apiRevenueDTO;
        } catch (MonetizationException e) {
            String errorMessage = "Error while getting revenue information for API ID : " + apiId;
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }


    public static OperationPolicyDataListDTO getAllRuntimeAPISpecificOperationPolicies(String apiId, Integer limit,
                                                                                       Integer offset, String organization)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        // Lightweight API specific operation policy includes the policy ID and the policy specification.
        // Since policy definition is bit bulky, we don't query the definition unnecessarily.
        List<OperationPolicyData> sharedOperationPolicyLIst = apiProvider
                .getAllAPISpecificOperationPolicies(apiId, organization);
        return OperationPolicyMappingUtil.fromOperationPolicyDataListToDTO(sharedOperationPolicyLIst, offset, limit);
    }

    public static OperationPolicyDataDTO getOperationPolicyForRuntimeAPIByPolicyId(String apiId, String operationPolicyId,
                                                                                   String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate whether api exists or not
        RestApiCommonUtil.validateAPIExistence(apiId);

        OperationPolicyData existingPolicy = apiProvider
                .getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, false);
        if (existingPolicy == null) {
            throw new APIManagementException(getOperationPolicyRetrieveErrorMessage(apiId, operationPolicyId),
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
        }

        return OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(existingPolicy);
    }

    public static OperationPolicyData getRuntimeAPISpecificOperationPolicyContentByPolicyId(String apiId, String
            operationPolicyId, String organization) throws APIManagementException {

        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        OperationPolicyData policyData = apiProvider
                .getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, true);
        if (policyData == null) {
            throw new APIMgtResourceNotFoundException(getOperationPolicyRetrieveErrorMessage(apiId, operationPolicyId),
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
        }
        return policyData;
    }


    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @param sequenceType Sequence type
     * @param resourcePath Resource path
     * @param verb         HTTP verb
     * @return Resource policy
     * @throws APIManagementException when getting resource policy fails
     */
    public static ResourcePolicyListDTO getRuntimeAPIResourcePolicies(String apiId, String organization,
                                                                      String sequenceType, String resourcePath, String verb)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.API_TYPE_SOAPTOREST, api.getType());
        if (StringUtils.isEmpty(sequenceType) || !(Constants.IN_SEQUENCE.equals(sequenceType)
                || Constants.OUT_SEQUENCE.equals(sequenceType))) {
            throw new APIManagementException("Sequence type should be either of the values from 'in' or 'out'",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        String resourcePolicy = SequenceUtils.getRestToSoapConvertedSequence(api, sequenceType);

        if (StringUtils.isNotEmpty(resourcePath) && StringUtils.isNotEmpty(verb)) {
            try {
                JSONObject sequenceObj = (JSONObject) new JSONParser().parse(resourcePolicy);
                JSONObject resultJson = new JSONObject();
                String key = resourcePath + "_" + verb;
                JSONObject sequenceContent = (JSONObject) sequenceObj.get(key);
                if (sequenceContent == null) {
                    String errorMessage = "Cannot find any resource policy for Resource path : " + resourcePath +
                            " with type: " + verb;
                    throw new APIManagementException(errorMessage, ExceptionCodes.RESOURCE_NOT_FOUND);
                }
                resultJson.put(key, sequenceObj.get(key));
                resourcePolicy = resultJson.toJSONString();
            } catch (ParseException e) {
                throw new APIManagementException("Error while retrieving the resource policies for the API : " + apiId,
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
        } else if (StringUtils.isEmpty(resourcePath)) {
            throw new APIManagementException("Resource path cannot be empty for the defined verb: " + verb,
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        } else if (StringUtils.isEmpty(verb)) {
            throw new APIManagementException("HTTP verb cannot be empty for the defined resource path: " + resourcePath,
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }

        return APIMappingUtil.fromResourcePolicyStrToDTO(resourcePolicy);
    }

    public static ResourcePolicyInfoDTO getRuntimeAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
                                                                                String organization)
            throws APIManagementException {

        APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = provider.getLightweightAPIByUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.API_TYPE_SOAPTOREST, api.getType());
        if (StringUtils.isEmpty(resourcePolicyId)) {
            String errorMessage = "Resource id should not be empty to update a resource policy.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        String policyContent = SequenceUtils.getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
        return APIMappingUtil.fromResourcePolicyStrToInfoDTO(policyContent);
    }


    /**
     * @param query        Search query
     * @param organization Tenant organization
     * @throws APIManagementException when validating API existence fails
     */
    public static void validateRuntimeAPI(String query, String organization) throws APIManagementException {

        if (StringUtils.isEmpty(query)) {
            throw new APIManagementException("The query should not be empty", ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        boolean isSearchArtifactExists;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (query.contains(":")) {
            String[] queryTokens = query.split(":");
            switch (queryTokens[0]) {
                case "name":
                    isSearchArtifactExists = apiProvider.isApiNameExist(queryTokens[1], organization) ||
                            apiProvider.isApiNameWithDifferentCaseExist(queryTokens[1], organization);
                    break;
                case "context":
                default: // API version validation.
                    isSearchArtifactExists = apiProvider.isContextExist(queryTokens[1], organization);
                    break;
            }

        } else { // consider the query as api name
            isSearchArtifactExists =
                    apiProvider.isApiNameExist(query, organization) ||
                            apiProvider.isApiNameWithDifferentCaseExist(query, organization);

        }
        if (!isSearchArtifactExists) {
            throw new APIManagementException(ExceptionCodes.RESOURCE_NOT_FOUND);
        }
    }

    public static String getEnvironmentProperties(String apiId, String envId, String organization)
            throws APIManagementException {

        validateEnvironment(organization, envId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // get properties
        EnvironmentPropertiesDTO properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        return new Gson().toJson(properties);
    }


    public static void validateEnvironment(String organization, String envId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // if apiProvider.getEnvironment(organization, envId) return null, it will throw an exception
        apiProvider.getEnvironment(organization, envId);
    }

    private static Map<String, Object> validateWSDL(String url, InputStream fileInputStream, String fileName,
                                                    Boolean isServiceAPI)
            throws APIManagementException {

        handleInvalidParams(fileInputStream, fileName, url, null, isServiceAPI);
        WSDLValidationResponseDTO responseDTO;
        WSDLValidationResponse validationResponse = new WSDLValidationResponse();

        if (url != null) {
            try {
                URL wsdlUrl = new URL(url);
                validationResponse = APIMWSDLReader.validateWSDLUrl(wsdlUrl);
            } catch (MalformedURLException e) {
                throw new APIManagementException(ExceptionCodes.MALFORMED_URL);
            }
        } else if (fileInputStream != null && !isServiceAPI) {

            try {
                if (fileName.endsWith(".zip")) {
                    validationResponse = APIMWSDLReader.extractAndValidateWSDLArchive(fileInputStream);
                } else if (fileName.endsWith(".wsdl")) {
                    validationResponse = APIMWSDLReader.validateWSDLFile(fileInputStream);
                } else {
                    String errorMessage = "Unsupported extension type of file: " + fileName;
                    throw new APIManagementException(errorMessage, ExceptionCodes.UNSUPPORTED_WSDL_FILE_EXTENSION);
                }
            } catch (APIManagementException e) {
                String errorMessage = "Internal error while validating the WSDL from file:" + fileName;
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));

            }
        } else if (fileInputStream != null) {
            try {
                validationResponse = APIMWSDLReader.validateWSDLFile(fileInputStream);
            } catch (APIManagementException e) {
                String errorMessage = "Internal error while validating the WSDL definition input stream";
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
            }
        }

        responseDTO = APIMappingUtil.fromWSDLValidationResponseToDTO(validationResponse);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);

        return response;
    }

    private static void handleInvalidParams(InputStream fileInputStream, String fileName, String url,
                                            String apiDefinition, Boolean isServiceAPI) throws APIManagementException {

        String msg = "";
        boolean isFileSpecified = (fileInputStream != null && fileName != null)
                || (fileInputStream != null && isServiceAPI);
        if (url == null && !isFileSpecified && apiDefinition == null) {
            msg = "One out of 'file' or 'url' or 'inline definition' should be specified";
        }

        boolean isMultipleSpecificationGiven = (isFileSpecified && url != null) || (isFileSpecified &&
                apiDefinition != null) || (apiDefinition != null && url != null);
        if (isMultipleSpecificationGiven) {
            msg = "Only one of 'file', 'url', and 'inline definition' should be specified";
        }

        if (StringUtils.isNotBlank(msg)) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, msg));
        }
    }


    private static APIDTO getRuntimeAPIByID(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return APIMappingUtil.fromAPItoDTO(api, apiProvider);
    }

    private static String getSOAPOperation() {

        return "{\"/*\":{\"post\":{\"parameters\":[{\"schema\":{\"type\":\"string\"},\"description\":\"SOAP request.\","
                + "\"name\":\"SOAP Request\",\"required\":true,\"in\":\"body\"},"
                + "{\"description\":\"SOAPAction header for soap 1.1\",\"name\":\"SOAPAction\",\"type\":\"string\","
                + "\"required\":false,\"in\":\"header\"}],\"responses\":{\"200\":{\"description\":\"OK\"}}," +
                "\"security\":[{\"default\":[]}],\"consumes\":[\"text/xml\",\"application/soap+xml\"]}}}";
    }

    private static String getOperationPolicyRetrieveErrorMessage(String apiId, String operationPolicyId) {

        return "Couldn't retrieve an existing operation policy with ID: "
                + operationPolicyId + " for API " + apiId;
    }
}