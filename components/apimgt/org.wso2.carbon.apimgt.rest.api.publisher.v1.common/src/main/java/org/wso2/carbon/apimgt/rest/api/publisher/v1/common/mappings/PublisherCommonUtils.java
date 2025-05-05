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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.schema.validation.SchemaValidationError;
import graphql.schema.validation.SchemaValidator;
import io.swagger.v3.parser.ObjectMapperFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIComplianceException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.TokenBasedThrottlingCountHolder;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.model.AIConfiguration;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIEndpointInfo;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.api.model.OrganizationTiers;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SOAPToRestSequence;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceDryRunInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionStringComparator;
import org.wso2.carbon.apimgt.impl.wsdl.SequenceGenerator;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesMapDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMaxTpsTokenBasedThrottlingConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdvertiseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseGraphQLInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.spec.parser.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.spec.parser.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.apimgt.api.model.policy.PolicyConstants.AI_API_QUOTA_TYPE;
import static org.wso2.carbon.apimgt.api.model.policy.PolicyConstants.EVENT_COUNT_TYPE;

import static org.wso2.carbon.apimgt.impl.APIConstants.GOVERNANCE_COMPLIANCE_ERROR_MESSAGE;
import static org.wso2.carbon.apimgt.impl.APIConstants.GOVERNANCE_COMPLIANCE_KEY;
import static org.wso2.carbon.apimgt.impl.APIConstants.PUBLISH;
import static org.wso2.carbon.apimgt.impl.APIConstants.REPUBLISH;

/**
 * This is a publisher rest api utility class.
 */
public class PublisherCommonUtils {

    private static final Log log = LogFactory.getLog(PublisherCommonUtils.class);
    public static final String SESSION_TIMEOUT_CONFIG_KEY = "sessionTimeOut";
    static APIMGovernanceService apimGovernanceService = ServiceReferenceHolder.getInstance()
            .getAPIMGovernanceService();
    private static String graphQLIntrospectionQuery = null;


    /**
     * Update API and API definition.
     *
     * @param originalAPI    existing API
     * @param apiDtoToUpdate DTO object with updated API data
     * @param apiProvider    API Provider
     * @param tokenScopes    token scopes
     * @param response       response of the API definition validation
     * @return updated API
     * @throws APIManagementException If an error occurs while updating the API and API definition
     * @throws ParseException         If an error occurs while parsing the endpoint configuration
     * @throws CryptoException        If an error occurs while encrypting the secret key of API
     * @throws FaultGatewaysException If an error occurs while updating manage of an existing API
     */
    public static API updateApiAndDefinition(API originalAPI, APIDTO apiDtoToUpdate, APIProvider apiProvider,
                                             String[] tokenScopes, APIDefinitionValidationResponse response)
            throws APIManagementException, ParseException, CryptoException, FaultGatewaysException {

        return updateApiAndDefinition(originalAPI, apiDtoToUpdate, apiProvider, tokenScopes, response, true);
    }

    /**
     * Update API and API definition. Soap to rest sequence is updated on demand.
     *
     * @param originalAPI                 existing API
     * @param apiDtoToUpdate              DTO object with updated API data
     * @param apiProvider                 API Provider
     * @param tokenScopes                 token scopes
     * @param generateSoapToRestSequences Option to generate soap to rest sequences.
     * @param response                    response of the API definition validation
     * @return updated API
     * @throws APIManagementException If an error occurs while updating the API and API definition
     * @throws ParseException         If an error occurs while parsing the endpoint configuration
     * @throws CryptoException        If an error occurs while encrypting the secret key of API
     * @throws FaultGatewaysException If an error occurs while updating manage of an existing API
     */
    public static API updateApiAndDefinition(API originalAPI, APIDTO apiDtoToUpdate, APIProvider apiProvider,
                                             String[] tokenScopes, APIDefinitionValidationResponse response,
                                             boolean generateSoapToRestSequences)
            throws APIManagementException, ParseException, CryptoException, FaultGatewaysException {

        API apiToUpdate = prepareForUpdateApi(originalAPI, apiDtoToUpdate, apiProvider, tokenScopes);
        String organization = RestApiCommonUtil.getLoggedInUserTenantDomain();
        ArtifactType artifactType = ArtifactType.API;
        if (!PublisherCommonUtils.isStreamingAPI(apiDtoToUpdate) && !APIConstants.APITransportType.GRAPHQL.toString()
                .equalsIgnoreCase(apiDtoToUpdate.getType().toString())) {
            artifactType = ArtifactType.API;
            prepareForUpdateSwagger(originalAPI.getUuid(), response, false, apiProvider, organization,
                    response.getParser(), apiToUpdate, generateSoapToRestSequences);
        } else if (APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(
                apiDtoToUpdate.getType().toString())) {
            artifactType = ArtifactType.API;
        }

        Map<String, String> complianceResult = checkGovernanceComplianceSync(originalAPI.getUuid(),
                APIMGovernableState.API_UPDATE, artifactType, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        PublisherCommonUtils.checkGovernanceComplianceAsync(originalAPI.getUuid(), APIMGovernableState.API_UPDATE,
                ArtifactType.API, organization);
        apiProvider.updateAPI(apiToUpdate, originalAPI);
        return apiProvider.getAPIbyUUID(originalAPI.getUuid(), originalAPI.getOrganization());
    }

    /**
     * Update an API.
     *
     * @param originalAPI    Existing API
     * @param apiDtoToUpdate New API DTO to update
     * @param apiProvider    API Provider
     * @param tokenScopes    Scopes of the token
     * @throws ParseException         If an error occurs while parsing the endpoint configuration
     * @throws CryptoException        If an error occurs while encrypting the secret key of API
     * @throws APIManagementException If an error occurs while updating the API
     * @throws FaultGatewaysException If an error occurs while updating manage of an existing API
     */
    public static API updateApi(API originalAPI, APIDTO apiDtoToUpdate, APIProvider apiProvider, String[] tokenScopes,
            OrganizationInfo orginfo)
            throws ParseException, CryptoException, APIManagementException, FaultGatewaysException {
        API apiToUpdate = prepareForUpdateApi(originalAPI, apiDtoToUpdate, apiProvider, tokenScopes);
       
        if (orginfo != null && orginfo.getOrganizationId() != null) {
            String visibleOrgs = apiToUpdate.getVisibleOrganizations();
            if (!StringUtils.isEmpty(visibleOrgs) && APIConstants.VISIBLE_ORG_ALL.equals(visibleOrgs)) {
                // IF visibility is all
                apiToUpdate.setVisibleOrganizations(APIConstants.VISIBLE_ORG_ALL);
            } else if (StringUtils.isEmpty(visibleOrgs) || APIConstants.VISIBLE_ORG_NONE.equals(visibleOrgs)) {
                // IF visibility is none 
                apiToUpdate.setVisibleOrganizations(orginfo.getOrganizationId()); // set to current org
            } else {
                // add current id to existing visibility list
                visibleOrgs = visibleOrgs + "," + orginfo.getOrganizationId();
                apiToUpdate.setVisibleOrganizations(visibleOrgs);
            }
            OrganizationTiers parentOrgTiers = new OrganizationTiers(orginfo.getOrganizationId(),
                    apiToUpdate.getAvailableTiers());
            Set<OrganizationTiers> currentOrganizationTiers = apiToUpdate.getAvailableTiersForOrganizations();
            if (currentOrganizationTiers == null) {
                currentOrganizationTiers = new HashSet<>();
            }
            currentOrganizationTiers.add(parentOrgTiers);
            apiToUpdate.setAvailableTiersForOrganizations(currentOrganizationTiers);
        }
        
        apiProvider.updateAPI(apiToUpdate, originalAPI);
        API apiUpdated = apiProvider.getAPIbyUUID(originalAPI.getUuid(), originalAPI.getOrganization());
        
        if (apiUpdated.getVisibleOrganizations() != null) {
            List<String> orgList = new ArrayList<>(Arrays.asList(apiUpdated.getVisibleOrganizations().split(",")));
            orgList.remove(orginfo.getOrganizationId());  // remove current user org
            String visibleOrgs = StringUtils.join(orgList, ',');
            apiUpdated.setVisibleOrganizations(visibleOrgs);
        }
        // Remove parentOrgTiers from OrganizationTiers list
        Set<OrganizationTiers> updatedOrganizationTiers = apiUpdated.getAvailableTiersForOrganizations();
        if (updatedOrganizationTiers != null) {
            updatedOrganizationTiers.removeIf(tier -> tier.getOrganizationID().equals(orginfo.getOrganizationId()));
            apiUpdated.setAvailableTiersForOrganizations(updatedOrganizationTiers);
        }

        if (apiUpdated != null && !StringUtils.isEmpty(apiUpdated.getEndpointConfig())) {
            JsonObject endpointConfig = JsonParser.parseString(apiUpdated.getEndpointConfig()).getAsJsonObject();
            if (!APIConstants.ENDPOINT_TYPE_SEQUENCE.equals(
                    endpointConfig.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE).getAsString()) && (
                    APIConstants.API_TYPE_HTTP.equals(apiUpdated.getType()) || APIConstants.API_TYPE_SOAPTOREST.equals(
                            apiUpdated.getType()))) {
                apiProvider.deleteSequenceBackendByRevision(apiUpdated.getUuid(), "0");
            }
        }
        return apiUpdated;
    }

    /**
     * @param api           API of the Custom Backend
     * @param apiProvider   API Provider
     * @param endpointType  Endpoint Type of the Custom Backend (SANDBOX, PRODUCTION)
     * @param customBackend Custom Backend
     * @param contentDecomp Header Content of the Request
     * @throws APIManagementException If an error occurs while updating the API and API definition
     */
    public static void updateCustomBackend(API api, APIProvider apiProvider, String endpointType,
                                           InputStream customBackend, String contentDecomp)
            throws APIManagementException {
        String fileName = getFileNameFromContentDisposition(contentDecomp);
        if (fileName == null) {
            throw new APIManagementException(
                    "Error when retrieving Custom Backend file name of API: " + api.getId().getApiName());
        }
        String customBackendUUID = UUID.randomUUID().toString();
        try {
            String customBackendStr = IOUtils.toString(customBackend);
            apiProvider.updateCustomBackend(api.getUuid(), endpointType, customBackendStr, fileName, customBackendUUID);
        } catch (IOException ex) {
            throw new APIManagementException("Error retrieving sequence backend of API: " + api.getUuid(), ex);
        }
    }

    private static String getFileNameFromContentDisposition(String contentDisposition) {
        // Split the Content-Disposition header to get the file name
        String[] parts = contentDisposition.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("filename")) {
                // Extract the file name value
                return part.split("=")[1].trim().replace("\"", "");
            }
        }
        return null;
    }


    /**
     * Prepare for API object before updating the API.
     *
     * @param originalAPI    Existing API
     * @param apiDtoToUpdate New API DTO to update
     * @param apiProvider    API Provider
     * @param tokenScopes    Scopes of the token
     * @throws ParseException         If an error occurs while parsing the endpoint configuration
     * @throws CryptoException        If an error occurs while encrypting the secret key of API
     * @throws APIManagementException If an error occurs while updating the API
     */
    private static API prepareForUpdateApi(API originalAPI, APIDTO apiDtoToUpdate, APIProvider apiProvider,
                                           String[] tokenScopes)
            throws APIManagementException, ParseException, CryptoException {

        APIIdentifier apiIdentifier = originalAPI.getId();
        // Validate if the USER_REST_API_SCOPES is not set in WebAppAuthenticator when scopes are validated
        if (tokenScopes == null) {
            throw new APIManagementException("Error occurred while updating the  API " + originalAPI.getUUID()
                    + " as the token information hasn't been correctly set internally",
                    ExceptionCodes.TOKEN_SCOPES_NOT_SET);
        }
        APIUtil.validateAPIEndpointConfig(apiDtoToUpdate.getEndpointConfig(), apiDtoToUpdate.getType().toString(),
                apiDtoToUpdate.getName());
        if (apiDtoToUpdate.getVisibility() == APIDTO.VisibilityEnum.RESTRICTED && apiDtoToUpdate.getVisibleRoles()
                .isEmpty()) {
            throw new APIManagementException("Access control roles cannot be empty when visibility is restricted",
                    ExceptionCodes.USER_ROLES_CANNOT_BE_NULL);
        }
        boolean isGraphql = originalAPI.getType() != null && APIConstants.APITransportType.GRAPHQL.toString()
                .equals(originalAPI.getType());
        boolean isAsyncAPI = originalAPI.getType() != null
                && (APIConstants.APITransportType.WS.toString().equals(originalAPI.getType())
                || APIConstants.APITransportType.WEBSUB.toString().equals(originalAPI.getType())
                || APIConstants.APITransportType.SSE.toString().equals(originalAPI.getType())
                || APIConstants.APITransportType.ASYNC.toString().equals(originalAPI.getType()));
        boolean isAIAPI = APIConstants.API_SUBTYPE_AI_API.equals(originalAPI.getSubtype());

        Scope[] apiDtoClassAnnotatedScopes = APIDTO.class.getAnnotationsByType(Scope.class);
        boolean hasClassLevelScope = checkClassScopeAnnotation(apiDtoClassAnnotatedScopes, tokenScopes);

        JSONParser parser = new JSONParser();
        String oldEndpointConfigString = originalAPI.getEndpointConfig();
        JSONObject oldEndpointConfig = null;
        if (StringUtils.isNotBlank(oldEndpointConfigString)) {
            oldEndpointConfig = (JSONObject) parser.parse(oldEndpointConfigString);
        }
        String oldProductionApiSecret = null;
        String oldSandboxApiSecret = null;

        String oldProductionApiKeyValue = null;
        String oldSandboxApiKeyValue = null;

        if (oldEndpointConfig != null) {
            if ((oldEndpointConfig.containsKey(APIConstants.ENDPOINT_SECURITY))) {
                JSONObject oldEndpointSecurity = (JSONObject) oldEndpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                if (oldEndpointSecurity != null &&
                        oldEndpointSecurity.containsKey(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION)) {
                    JSONObject oldEndpointSecurityProduction = (JSONObject) oldEndpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);

                    if (oldEndpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CLIENT_ID) != null
                            && oldEndpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                            != null) {
                        oldProductionApiSecret = oldEndpointSecurityProduction
                                .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                    } else if (oldEndpointSecurityProduction
                            .get(APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER) != null
                            && oldEndpointSecurityProduction.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null
                            && oldEndpointSecurityProduction
                            .get(APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER_TYPE) != null) {
                        oldProductionApiKeyValue = oldEndpointSecurityProduction
                                .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                    }
                }
                if (oldEndpointSecurity != null &&
                        oldEndpointSecurity.containsKey(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX)) {
                    JSONObject oldEndpointSecuritySandbox = (JSONObject) oldEndpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);

                    if (oldEndpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CLIENT_ID) != null
                            && oldEndpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                            != null) {
                        oldSandboxApiSecret = oldEndpointSecuritySandbox
                                .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                    } else if (oldEndpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER) != null
                            && oldEndpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null
                            && oldEndpointSecuritySandbox
                            .get(APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER_TYPE) != null) {
                        oldSandboxApiKeyValue = oldEndpointSecuritySandbox
                                .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                    }
                }
            }
        }

        Map endpointConfig = (Map) apiDtoToUpdate.getEndpointConfig();
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

        // OAuth 2.0 backend protection: API Key and API Secret encryption
        encryptEndpointSecurityOAuthCredentials(endpointConfig, cryptoUtil, oldProductionApiSecret, oldSandboxApiSecret,
                apiDtoToUpdate);

        encryptEndpointSecurityApiKeyCredentials(endpointConfig, cryptoUtil, oldProductionApiKeyValue,
                oldSandboxApiKeyValue, apiDtoToUpdate);
        // update endpointConfig with the provided custom sequence
        if (endpointConfig != null) {
            if (APIConstants.ENDPOINT_TYPE_SEQUENCE.equals(
                    endpointConfig.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
                try {
                    if (endpointConfig.get("sequence_path") != null) {
                        String pathToSequence = endpointConfig.get("sequence_path").toString();
                        String sequence = FileUtils.readFileToString(new File(pathToSequence),
                                Charset.defaultCharset());
                        endpointConfig.put("sequence", sequence);
                        apiDtoToUpdate.setEndpointConfig(endpointConfig);
                    }
                } catch (IOException ex) {
                    throw new APIManagementException(
                            "Error while reading Custom Sequence of API: " + apiDtoToUpdate.getId(), ex,
                            ExceptionCodes.ERROR_READING_CUSTOM_SEQUENCE);
                }
            }
        }

        // AWS Lambda: secret key encryption while updating the API
        if (apiDtoToUpdate.getEndpointConfig() != null) {
            if (endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)) {
                String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                if (!StringUtils.isEmpty(secretKey)) {
                    if (!APIConstants.AWS_SECRET_KEY.equals(secretKey)) {
                        String encryptedSecretKey = cryptoUtil.encryptAndBase64Encode(secretKey.getBytes());
                        endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                        apiDtoToUpdate.setEndpointConfig(endpointConfig);
                    } else {
                        JSONParser jsonParser = new JSONParser();
                        JSONObject originalEndpointConfig = (JSONObject) jsonParser
                                .parse(originalAPI.getEndpointConfig());
                        String encryptedSecretKey = (String) originalEndpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                        endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                        apiDtoToUpdate.setEndpointConfig(endpointConfig);
                    }
                }
            }
        }
        if (!hasClassLevelScope) {
            // Validate per-field scopes
            apiDtoToUpdate = getFieldOverriddenAPIDTO(apiDtoToUpdate, originalAPI, tokenScopes);
        }
        //Overriding some properties:
        //API Name change not allowed if OnPrem
        if (APIUtil.isOnPremResolver()) {
            apiDtoToUpdate.setName(apiIdentifier.getApiName());
        }
        apiDtoToUpdate.setVersion(apiIdentifier.getVersion());
        apiDtoToUpdate.setProvider(apiIdentifier.getProviderName());
        apiDtoToUpdate.setContext(originalAPI.getContextTemplate());
        apiDtoToUpdate.setLifeCycleStatus(originalAPI.getStatus());
        apiDtoToUpdate.setType(APIDTO.TypeEnum.fromValue(originalAPI.getType()));

        List<APIResource> removedProductResources = getRemovedProductResources(apiDtoToUpdate, originalAPI);

        if (!removedProductResources.isEmpty()) {
            throw new APIManagementException(
                    "Cannot remove following resource paths " + removedProductResources.toString()
                            + " because they are used by one or more API Products", ExceptionCodes
                    .from(ExceptionCodes.API_PRODUCT_USED_RESOURCES, originalAPI.getId().getApiName(),
                            originalAPI.getId().getVersion()));
        }

        // Validate API Security
        List<String> apiSecurity = apiDtoToUpdate.getSecurityScheme();
        //validation for tiers
        List<String> tiersFromDTO = apiDtoToUpdate.getPolicies();
        List<OrganizationPoliciesDTO> organizationPoliciesDTOs = apiDtoToUpdate.getOrganizationPolicies();
        // Remove the subscriptionless tier if other tiers are available.
        if (tiersFromDTO != null && tiersFromDTO.size() > 1) {
            String tierToDrop = null;
            for (String tier : tiersFromDTO) {
                if (tier.contains(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS)) {
                    tierToDrop = tier;
                    break;
                }
            }
            if (tierToDrop != null) {
                tiersFromDTO.remove(tierToDrop);
                apiDtoToUpdate.setPolicies(tiersFromDTO);
            }
        }
        String originalStatus = originalAPI.getStatus();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        boolean condition = ((tiersFromDTO == null || tiersFromDTO.isEmpty()
                && !(APIConstants.CREATED.equals(originalStatus)
                || APIConstants.PROTOTYPED.equals(originalStatus)))
                && !apiDtoToUpdate.getAdvertiseInfo().isAdvertised());
        if (!APIUtil.isSubscriptionValidationDisablingAllowed(tenantDomain)) {
            if (apiSecurity != null && (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) || apiSecurity
                    .contains(APIConstants.API_SECURITY_API_KEY)) && condition) {
                Set<Tier> availableThrottlingPolicyList = apiProvider.getTiers();
                tiersFromDTO = availableThrottlingPolicyList.stream()
                        .filter(tier -> isApplicableTier(tier, isAsyncAPI, isAIAPI))
                        .map(Tier::getName)
                        .findFirst()
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
                apiDtoToUpdate.setPolicies(tiersFromDTO);

                if (tiersFromDTO.isEmpty()) {
                    throw new APIManagementException(
                            "A tier should be defined if the API is not in CREATED or PROTOTYPED state",
                            ExceptionCodes.TIER_CANNOT_BE_NULL);
                }
            }
        } else {
            if (apiSecurity != null) {
                if (apiSecurity.contains(APIConstants.API_SECURITY_API_KEY) && condition) {
                    throw new APIManagementException(
                            "A tier should be defined if the API is not in CREATED or PROTOTYPED state",
                            ExceptionCodes.TIER_CANNOT_BE_NULL);
                } else if (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                    // Internally set the default tier when no tiers are defined in order to support
                    // subscription validation disabling for OAuth2 secured APIs
                    if (tiersFromDTO != null && tiersFromDTO.isEmpty()) {
                        if (isAsyncAPI) {
                            tiersFromDTO.add(APIConstants.DEFAULT_SUB_POLICY_ASYNC_SUBSCRIPTIONLESS);
                        } else {
                            tiersFromDTO.add(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS);

                        }
                        apiDtoToUpdate.setPolicies(tiersFromDTO);
                    }
                }
            }
        }

        Set<Tier> definedTiers = apiProvider.getTiers();
        if (tiersFromDTO != null && !tiersFromDTO.isEmpty()) {
            //check whether the added API's tiers are all valid
            List<String> invalidTiers = getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                throw new APIManagementException(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid",
                        ExceptionCodes.TIER_NAME_INVALID);
            }
        }

        boolean isSubscriptionValidationDisablingEnabled
                = tiersFromDTO.contains(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS)
                || tiersFromDTO.contains(APIConstants.DEFAULT_SUB_POLICY_ASYNC_SUBSCRIPTIONLESS);
        // Organization based subscription policies
        if (APIUtil.isOrganizationAccessControlEnabled()) {
            for (OrganizationPoliciesDTO organizationPoliciesDTO : organizationPoliciesDTOs) {
                List<String> organizationTiersFromDTO = organizationPoliciesDTO.getPolicies();
                if (isSubscriptionValidationDisablingEnabled) {
                    /* If subscription validation is disabled for root organization
                    it should be disabled for shared organizations */
                    organizationTiersFromDTO = tiersFromDTO;
                    organizationPoliciesDTO.setPolicies(organizationTiersFromDTO);
                } else if (organizationTiersFromDTO.contains(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS)
                        || organizationTiersFromDTO.contains(APIConstants.DEFAULT_SUB_POLICY_ASYNC_SUBSCRIPTIONLESS)) {
                    /* If subscription validation is enabled for root organization
                    it should not be disabled for shared organizations */
                    organizationTiersFromDTO = tiersFromDTO;
                    organizationPoliciesDTO.setPolicies(organizationTiersFromDTO);
                    log.warn("Subscription validation can not be disabled for the organization with ID: "
                            + organizationPoliciesDTO.getOrganizationID()
                            + ". Therefore root organization subscription policies will be assigned.");
                }
                boolean conditionForOrganization = (
                        (organizationTiersFromDTO == null || organizationTiersFromDTO.isEmpty() && !(
                                APIConstants.CREATED.equals(originalStatus) || APIConstants.PROTOTYPED.equals(
                                        originalStatus))) && !apiDtoToUpdate.getAdvertiseInfo().isAdvertised());
                if (!APIUtil.isSubscriptionValidationDisablingAllowed(tenantDomain)) {
                    if (apiSecurity != null && (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)
                            || apiSecurity.contains(APIConstants.API_SECURITY_API_KEY)) && conditionForOrganization) {
                        throw new APIManagementException("A tier should be defined if the API is not in CREATED "
                                + "or PROTOTYPED state", ExceptionCodes.TIER_CANNOT_BE_NULL);
                    }
                } else {
                    if (apiSecurity != null) {
                        if (apiSecurity.contains(APIConstants.API_SECURITY_API_KEY) && conditionForOrganization) {
                            throw new APIManagementException("A tier should be defined if the API is not in CREATED "
                                    + "or PROTOTYPED state", ExceptionCodes.TIER_CANNOT_BE_NULL);
                        } else if (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                            // Use the root organization tiers if no tiers are set
                            if (organizationTiersFromDTO != null && organizationTiersFromDTO.isEmpty()) {
                                organizationTiersFromDTO = tiersFromDTO;
                                organizationPoliciesDTO.setPolicies(organizationTiersFromDTO);
                            }
                        }
                    }
                }
                if (organizationTiersFromDTO != null && !organizationTiersFromDTO.isEmpty()) {
                    List<String> invalidTiers = getInvalidTierNames(definedTiers, organizationTiersFromDTO);
                    if (invalidTiers.size() > 0) {
                        throw new APIManagementException("Specified tier(s) " + Arrays.toString(invalidTiers.toArray())
                                + " are invalid", ExceptionCodes.TIER_NAME_INVALID);
                    }
                }
            }
            apiDtoToUpdate.setOrganizationPolicies(organizationPoliciesDTOs);
        }

        if (apiDtoToUpdate.getAccessControlRoles() != null) {
            String errorMessage = validateUserRoles(apiDtoToUpdate.getAccessControlRoles());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }
        if (apiDtoToUpdate.getVisibleRoles() != null) {
            String errorMessage = validateRoles(apiDtoToUpdate.getVisibleRoles());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }
        if (apiDtoToUpdate.getAdditionalProperties() != null) {
            String errorMessage = validateAdditionalProperties(apiDtoToUpdate.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes
                        .from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES, apiDtoToUpdate.getName(),
                                apiDtoToUpdate.getVersion()));
            }
        }
        // Validate if resources are empty
        if (apiDtoToUpdate.getOperations() == null || apiDtoToUpdate.getOperations().isEmpty()) {
            throw new APIManagementException(ExceptionCodes.NO_RESOURCES_FOUND);
        }
        API apiToUpdate = APIMappingUtil.fromDTOtoAPI(apiDtoToUpdate, apiIdentifier.getProviderName());
        if (APIConstants.PUBLIC_STORE_VISIBILITY.equals(apiToUpdate.getVisibility())) {
            apiToUpdate.setVisibleRoles(StringUtils.EMPTY);
        }
        apiToUpdate.setUUID(originalAPI.getUUID());
        apiToUpdate.setOrganization(originalAPI.getOrganization());
        validateScopes(apiToUpdate);
        validateSubscriptionAvailability(originalAPI, apiToUpdate);
        apiToUpdate.setThumbnailUrl(originalAPI.getThumbnailUrl());
        if (apiDtoToUpdate.getKeyManagers() instanceof List) {
            apiToUpdate.setKeyManagers((List<String>) apiDtoToUpdate.getKeyManagers());
        } else {
            apiToUpdate.setKeyManagers(Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS));
        }

        //preserve monetization status in the update flow
        //apiProvider.configureMonetizationInAPIArtifact(originalAPI); ////////////TODO /////////REG call

        if (!isAsyncAPI) {
            String oldDefinition = apiProvider
                    .getOpenAPIDefinition(apiToUpdate.getUuid(), originalAPI.getOrganization());
            APIDefinition apiDefinition = OASParserUtil.getOASParser(oldDefinition);
            SwaggerData swaggerData = new SwaggerData(apiToUpdate);
            String newDefinition = apiDefinition.generateAPIDefinition(swaggerData, oldDefinition);
            apiProvider.saveSwaggerDefinition(apiToUpdate, newDefinition, originalAPI.getOrganization());
            if (!isGraphql) {
                Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(newDefinition);

                //set operation policies from the original API Payload
                Set<URITemplate> uriTemplatesFromPayload = apiToUpdate.getUriTemplates();
                Map<String, List<OperationPolicy>> operationPoliciesPerURITemplate = new HashMap<>();
                for (URITemplate uriTemplate : uriTemplatesFromPayload) {
                    if (!uriTemplate.getOperationPolicies().isEmpty()) {
                        String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
                        operationPoliciesPerURITemplate.put(key, uriTemplate.getOperationPolicies());
                    }
                }

                for (URITemplate uriTemplate : uriTemplates) {
                    String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
                    if (operationPoliciesPerURITemplate.containsKey(key)) {
                        uriTemplate.setOperationPolicies(operationPoliciesPerURITemplate.get(key));
                    }
                }

                apiToUpdate.setUriTemplates(uriTemplates);
                apiToUpdate.setSwaggerDefinition(newDefinition);
            }
        } else {
            String oldDefinition = apiProvider
                    .getAsyncAPIDefinition(apiToUpdate.getUuid(), originalAPI.getOrganization());
            AsyncApiParser asyncApiParser = new AsyncApiParser();
            String updateAsyncAPIDefinition = asyncApiParser.updateAsyncAPIDefinition(oldDefinition, apiToUpdate);
            apiProvider.saveAsyncApiDefinition(originalAPI, updateAsyncAPIDefinition);
            apiToUpdate.setSwaggerDefinition(updateAsyncAPIDefinition);
        }
        apiToUpdate.setWsdlUrl(apiDtoToUpdate.getWsdlUrl());
        apiToUpdate.setGatewayType(apiDtoToUpdate.getGatewayType());

        //validate API categories
        List<APICategory> apiCategories = apiToUpdate.getApiCategories();
        List<APICategory> apiCategoriesList = new ArrayList<>();
        for (APICategory category : apiCategories) {
            category.setOrganization(originalAPI.getOrganization());
            apiCategoriesList.add(category);
        }
        apiToUpdate.setApiCategories(apiCategoriesList);
        if (apiCategoriesList.size() > 0) {
            if (!APIUtil.validateAPICategories(apiCategoriesList, originalAPI.getOrganization())) {
                throw new APIManagementException("Invalid API Category name(s) defined",
                        ExceptionCodes.from(ExceptionCodes.API_CATEGORY_INVALID, originalAPI.getId().getName()));
            }
        }

        apiToUpdate.setOrganization(originalAPI.getOrganization());
        return apiToUpdate;
    }

    private static boolean isApplicableTier(Tier tier, boolean isAsyncAPI, boolean isAIAPI) {
        if (isAsyncAPI) {
            return isAsyncAPITier(tier);
        }

        if (isAIAPI) {
            return isAIAPITier(tier);
        }

        return isRegularAPITier(tier);
    }

    /**
     * Checks if the given tier is an Async API tier.
     *
     * @param tier The tier to evaluate.
     * @return {@code true} if the tier is of type EVENT_COUNT_TYPE, otherwise {@code false}.
     */
    private static boolean isAsyncAPITier(Tier tier) {
        return EVENT_COUNT_TYPE.equals(tier.getQuotaPolicyType());
    }

    /**
     * Checks if the given tier is an AI API tier.
     *
     * @param tier The tier to evaluate.
     * @return {@code true} if the tier is of type AI_API_QUOTA_TYPE,
     *         contains the default subscription-less policy name,
     *         or has a null quota policy type. Otherwise, returns {@code false}.
     */
    private static boolean isAIAPITier(Tier tier) {
        return AI_API_QUOTA_TYPE.equals(tier.getQuotaPolicyType()) ||
                tier.getName().contains(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS) ||
                tier.getQuotaPolicyType() == null;
    }

    /**
     * Checks if the given tier is a regular API tier.
     *
     * @param tier The tier to evaluate.
     * @return {@code true} if the tier is neither an AI API tier nor an Async API tier,
     *         otherwise {@code false}.
     */
    private static boolean isRegularAPITier(Tier tier) {
        return !AI_API_QUOTA_TYPE.equals(tier.getQuotaPolicyType()) &&
                !EVENT_COUNT_TYPE.equals(tier.getQuotaPolicyType());
    }

    /**
     * This method will encrypt the Api Key
     *
     * @param endpointConfig           endpoint configuration of API
     * @param cryptoUtil               cryptography util
     * @param oldProductionApiKeyValue existing production API secret
     * @param oldSandboxApiKeyValue    existing sandbox API secret
     * @param apidto                   API DTO
     * @throws CryptoException        if an error occurs while encrypting and base64 encode
     * @throws APIManagementException if an error occurs due to a problem in the endpointConfig payload
     */
    public static void encryptEndpointSecurityApiKeyCredentials(Map endpointConfig,
                                                                CryptoUtil cryptoUtil,
                                                                String oldProductionApiKeyValue,
                                                                String oldSandboxApiKeyValue, APIDTO apidto)
            throws CryptoException, APIManagementException {

        if (endpointConfig != null) {
            if ((endpointConfig.get(APIConstants.ENDPOINT_SECURITY) != null)) {
                Map endpointSecurity = (Map) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                    Map endpointSecurityProduction = (Map) endpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);
                    String productionEndpointType = (String) endpointSecurityProduction
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(productionEndpointType)) {
                        if (endpointSecurityProduction.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null &&
                                StringUtils.isNotEmpty(endpointSecurityProduction.get(
                                        APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString()) &&
                                !endpointSecurityProduction.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE)
                                        .equals(oldProductionApiKeyValue)) {
                            String apiKeyValue = endpointSecurityProduction
                                    .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                            String encryptedApiKeyValue = cryptoUtil.encryptAndBase64Encode(apiKeyValue.getBytes());
                            endpointSecurityProduction
                                    .put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, encryptedApiKeyValue);
                        } else if (StringUtils.isNotBlank(oldProductionApiKeyValue)) {
                            endpointSecurityProduction
                                    .put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, oldProductionApiKeyValue);
                        } else {
                            String errorMessage = "ApiKey value is not provided for production endpoint security";
                            throw new APIManagementException(
                                    ExceptionCodes.from(ExceptionCodes.INVALID_ENDPOINT_CREDENTIALS, errorMessage));
                        }
                    }
                    endpointSecurity
                            .put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION, endpointSecurityProduction);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apidto.setEndpointConfig(endpointConfig);
                }
                if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                    Map endpointSecuritySandbox = (Map) endpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);
                    String sandboxEndpointType = (String) endpointSecuritySandbox
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(sandboxEndpointType)) {
                        if (endpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null
                                && StringUtils.isNotEmpty(
                                endpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE)
                                        .toString()) &&
                                !endpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).equals(
                                        oldSandboxApiKeyValue)) {
                            String apiKeyValue = endpointSecuritySandbox
                                    .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                            String encryptedApiKeyValue = cryptoUtil.encryptAndBase64Encode(apiKeyValue.getBytes());
                            endpointSecuritySandbox
                                    .put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, encryptedApiKeyValue);
                        } else if (StringUtils.isNotBlank(oldSandboxApiKeyValue)) {
                            endpointSecuritySandbox
                                    .put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, oldSandboxApiKeyValue);
                        } else {
                            String errorMessage = "ApiKey value is not provided for sandbox endpoint security";
                            throw new APIManagementException(
                                    ExceptionCodes.from(ExceptionCodes.INVALID_ENDPOINT_CREDENTIALS, errorMessage));
                        }
                    }
                    endpointSecurity
                            .put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX, endpointSecuritySandbox);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apidto.setEndpointConfig(endpointConfig);
                }
            }
        }
    }

    /**
     * This method will encrypt the OAuth 2.0 API Key and API Secret
     *
     * @param endpointConfig         endpoint configuration of API
     * @param cryptoUtil             cryptography util
     * @param oldProductionApiSecret existing production API secret
     * @param oldSandboxApiSecret    existing sandbox API secret
     * @param apidto                 API DTO
     * @throws CryptoException        if an error occurs while encrypting and base64 encode
     * @throws APIManagementException if an error occurs due to a problem in the endpointConfig payload
     */
    public static void encryptEndpointSecurityOAuthCredentials(Map endpointConfig, CryptoUtil cryptoUtil,
                                                               String oldProductionApiSecret,
                                                               String oldSandboxApiSecret, APIDTO apidto)
            throws CryptoException, APIManagementException {
        // OAuth 2.0 backend protection: API Key and API Secret encryption
        String customParametersString;
        if (endpointConfig != null) {
            if ((endpointConfig.get(APIConstants.ENDPOINT_SECURITY) != null)) {
                Map endpointSecurity = (Map) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                    Map endpointSecurityProduction = (Map) endpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);
                    String productionEndpointType = (String) endpointSecurityProduction
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    // Change default value of customParameters JSONObject to String
                    if (!(endpointSecurityProduction
                            .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) instanceof String)) {
                        LinkedHashMap<String, String> customParametersHashMap = (LinkedHashMap<String, String>)
                                endpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                        customParametersString = JSONObject.toJSONString(customParametersHashMap);
                    } else if (endpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS)
                            != null) {
                        customParametersString = (String) endpointSecurityProduction
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                    } else {
                        customParametersString = "{}";
                    }

                    endpointSecurityProduction
                            .put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParametersString);

                    if (APIConstants.OAuthConstants.OAUTH.equals(productionEndpointType)) {
                        if (endpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET) != null
                                && StringUtils.isNotBlank(
                                endpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                                        .toString())) {
                            String apiSecret = endpointSecurityProduction
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                            String encryptedApiSecret = cryptoUtil.encryptAndBase64Encode(apiSecret.getBytes());
                            endpointSecurityProduction
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, encryptedApiSecret);
                        } else if (StringUtils.isNotBlank(oldProductionApiSecret)) {
                            endpointSecurityProduction
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, oldProductionApiSecret);
                        } else {
                            String errorMessage = "Client secret is not provided for production endpoint security";
                            throw new APIManagementException(
                                    ExceptionCodes.from(ExceptionCodes.INVALID_ENDPOINT_CREDENTIALS, errorMessage));
                        }
                    }
                    endpointSecurity
                            .put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION, endpointSecurityProduction);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apidto.setEndpointConfig(endpointConfig);
                }
                if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                    Map endpointSecuritySandbox = (Map) endpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);
                    String sandboxEndpointType = (String) endpointSecuritySandbox
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    // Change default value of customParameters JSONObject to String
                    if (!(endpointSecuritySandbox
                            .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) instanceof String)) {
                        Map<String, String> customParametersHashMap = (Map<String, String>) endpointSecuritySandbox
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                        customParametersString = JSONObject.toJSONString(customParametersHashMap);
                    } else if (endpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS)
                            != null) {
                        customParametersString = (String) endpointSecuritySandbox
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                    } else {
                        customParametersString = "{}";
                    }
                    endpointSecuritySandbox
                            .put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParametersString);

                    if (APIConstants.OAuthConstants.OAUTH.equals(sandboxEndpointType)) {
                        if (endpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET) != null
                                && StringUtils.isNotBlank(
                                endpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                                        .toString())) {
                            String apiSecret = endpointSecuritySandbox
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                            String encryptedApiSecret = cryptoUtil.encryptAndBase64Encode(apiSecret.getBytes());
                            endpointSecuritySandbox
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, encryptedApiSecret);
                        } else if (StringUtils.isNotBlank(oldSandboxApiSecret)) {
                            endpointSecuritySandbox
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, oldSandboxApiSecret);
                        } else {
                            String errorMessage = "Client secret is not provided for sandbox endpoint security";
                            throw new APIManagementException(
                                    ExceptionCodes.from(ExceptionCodes.INVALID_ENDPOINT_CREDENTIALS, errorMessage));
                        }
                    }
                    endpointSecurity
                            .put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX, endpointSecuritySandbox);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apidto.setEndpointConfig(endpointConfig);
                }
            }
        }
    }

    /**
     * This method will encrypt the API Key
     *
     * @param apiEndpointDTO APIEndpointDTO
     * @param cryptoUtil    cryptography util
     * @param oldApiSecret  existing API secret
     * @param endpointConfig endpoint configuration of API
     * @throws CryptoException       if an error occurs while encrypting and base64 encode
     * @throws APIManagementException if an error occurs due to a problem in the endpointConfig payload
     */
    public static void encryptEndpointSecurityApiKeyCredentials(APIEndpointDTO apiEndpointDTO,
            CryptoUtil cryptoUtil,
            String oldApiSecret, Map endpointConfig) throws CryptoException, APIManagementException {

        if (endpointConfig != null) {
            if ((endpointConfig.get(APIConstants.ENDPOINT_SECURITY) != null)) {
                Map endpointSecurity = (Map) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                if (APIConstants.APIEndpoint.PRODUCTION.equals(
                        apiEndpointDTO.getDeploymentStage()) && endpointSecurity.get(
                        APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                    Map endpointSecurityProduction = (Map) endpointSecurity.get(
                            APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);
                    String productionEndpointType = (String) endpointSecurityProduction.get(
                            APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(productionEndpointType)) {
                        if (endpointSecurityProduction.get(
                                APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null && StringUtils.isNotEmpty(
                                endpointSecurityProduction.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE)
                                        .toString()) && !endpointSecurityProduction.get(
                                APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).equals(oldApiSecret)) {
                            String apiKeyValue = endpointSecurityProduction.get(
                                    APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                            String encryptedApiKeyValue = cryptoUtil.encryptAndBase64Encode(apiKeyValue.getBytes());
                            endpointSecurityProduction.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE,
                                    encryptedApiKeyValue);
                        } else if (StringUtils.isNotBlank(oldApiSecret)) {
                            String encryptedOldApiKeyValue = cryptoUtil.encryptAndBase64Encode(oldApiSecret.getBytes());
                            endpointSecurityProduction.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE,
                                    encryptedOldApiKeyValue);
                        } else {
                            String errorMessage = "ApiKey value is not provided for production endpoint security";
                            throw new APIManagementException(
                                    ExceptionCodes.from(ExceptionCodes.INVALID_ENDPOINT_CREDENTIALS, errorMessage));
                        }
                    }
                    endpointSecurity.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION,
                            endpointSecurityProduction);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apiEndpointDTO.setEndpointConfig(endpointConfig);
                }
                if (APIConstants.APIEndpoint.SANDBOX.equals(
                        apiEndpointDTO.getDeploymentStage()) && endpointSecurity.get(
                        APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                    Map endpointSecuritySandbox = (Map) endpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);
                    String sandboxEndpointType = (String) endpointSecuritySandbox
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(sandboxEndpointType)) {
                        if (endpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null
                                && StringUtils.isNotEmpty(
                                endpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE)
                                        .toString()) &&
                                !endpointSecuritySandbox.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).equals(
                                        oldApiSecret)) {
                            String apiKeyValue = endpointSecuritySandbox
                                    .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                            String encryptedApiKeyValue = cryptoUtil.encryptAndBase64Encode(apiKeyValue.getBytes());
                            endpointSecuritySandbox
                                    .put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, encryptedApiKeyValue);
                        } else if (StringUtils.isNotBlank(oldApiSecret)) {
                            endpointSecuritySandbox
                                    .put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, oldApiSecret);
                        } else {
                            String errorMessage = "ApiKey value is not provided for sandbox endpoint security";
                            throw new APIManagementException(
                                    ExceptionCodes.from(ExceptionCodes.INVALID_ENDPOINT_CREDENTIALS, errorMessage));
                        }
                    }
                    endpointSecurity
                            .put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX, endpointSecuritySandbox);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apiEndpointDTO.setEndpointConfig(endpointConfig);
                }
            }
        }
    }

    /**
     * Check whether the token has APIDTO class level Scope annotation.
     *
     * @return true if the token has APIDTO class level Scope annotation
     */
    private static boolean checkClassScopeAnnotation(Scope[] apiDtoClassAnnotatedScopes, String[] tokenScopes) {

        for (Scope classAnnotation : apiDtoClassAnnotatedScopes) {
            for (String tokenScope : tokenScopes) {
                if (classAnnotation.name().equals(tokenScope)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Override the API DTO field values with the user passed new values considering the field-wise scopes defined as
     * allowed to update in REST API definition yaml.
     */
    private static JSONObject overrideDTOValues(JSONObject originalApiDtoJson, JSONObject newApiDtoJson, Field field,
                                                String[] tokenScopes, Scope[] fieldAnnotatedScopes)
            throws APIManagementException {

        for (String tokenScope : tokenScopes) {
            for (Scope scopeAnt : fieldAnnotatedScopes) {
                if (scopeAnt.name().equals(tokenScope)) {
                    // do the overriding
                    originalApiDtoJson.put(field.getName(), newApiDtoJson.get(field.getName()));
                    return originalApiDtoJson;
                }
            }
        }
        throw new APIManagementException("User is not authorized to update one or more API fields. None of the "
                + "required scopes found in user token to update the field. So the request will be failed.",
                ExceptionCodes.INVALID_SCOPE);
    }

    /**
     * Get the API DTO object in which the API field values are overridden with the user passed new values.
     *
     * @throws APIManagementException
     */
    private static APIDTO getFieldOverriddenAPIDTO(APIDTO apidto, API originalAPI, String[] tokenScopes)
            throws APIManagementException {

        APIDTO originalApiDTO;
        APIDTO updatedAPIDTO;

        try {
            originalApiDTO = APIMappingUtil.fromAPItoDTO(originalAPI);

            Field[] fields = APIDTO.class.getDeclaredFields();
            ObjectMapper mapper = new ObjectMapper();
            String newApiDtoJsonString = mapper.writeValueAsString(apidto);
            JSONParser parser = new JSONParser();
            JSONObject newApiDtoJson = (JSONObject) parser.parse(newApiDtoJsonString);

            String originalApiDtoJsonString = mapper.writeValueAsString(originalApiDTO);
            JSONObject originalApiDtoJson = (JSONObject) parser.parse(originalApiDtoJsonString);

            for (Field field : fields) {
                Scope[] fieldAnnotatedScopes = field.getAnnotationsByType(Scope.class);
                String originalElementValue = mapper.writeValueAsString(originalApiDtoJson.get(field.getName()));
                String newElementValue = mapper.writeValueAsString(newApiDtoJson.get(field.getName()));

                if (!StringUtils.equals(originalElementValue, newElementValue)) {
                    originalApiDtoJson = overrideDTOValues(originalApiDtoJson, newApiDtoJson, field, tokenScopes,
                            fieldAnnotatedScopes);
                }
            }

            updatedAPIDTO = mapper.readValue(originalApiDtoJson.toJSONString(), APIDTO.class);

        } catch (IOException | ParseException e) {
            String msg = "Error while processing API DTO json strings";
            throw new APIManagementException(msg, e, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return updatedAPIDTO;
    }

    /**
     * Finds resources that have been removed in the updated API, that are currently reused by API Products.
     *
     * @param updatedDTO  Updated API
     * @param existingAPI Existing API
     * @return List of removed resources that are reused among API Products
     */
    private static List<APIResource> getRemovedProductResources(APIDTO updatedDTO, API existingAPI) {

        List<APIOperationsDTO> updatedOperations = updatedDTO.getOperations();
        Set<URITemplate> existingUriTemplates = existingAPI.getUriTemplates();
        List<APIResource> removedReusedResources = new ArrayList<>();

        for (URITemplate existingUriTemplate : existingUriTemplates) {

            // If existing URITemplate is used by any API Products
            if (!existingUriTemplate.retrieveUsedByProducts().isEmpty()) {
                String existingVerb = existingUriTemplate.getHTTPVerb();
                String existingPath = existingUriTemplate.getUriTemplate();
                boolean isReusedResourceRemoved = true;

                for (APIOperationsDTO updatedOperation : updatedOperations) {
                    String updatedVerb = updatedOperation.getVerb();
                    String updatedPath = updatedOperation.getTarget();

                    //Check if existing reused resource is among updated resources
                    if (existingVerb.equalsIgnoreCase(updatedVerb) && existingPath.equalsIgnoreCase(updatedPath)) {
                        isReusedResourceRemoved = false;
                        break;
                    }
                }

                // Existing reused resource is not among updated resources
                if (isReusedResourceRemoved) {
                    APIResource removedResource = new APIResource(existingVerb, existingPath);
                    removedReusedResources.add(removedResource);
                }
            }
        }

        return removedReusedResources;
    }

    /**
     * To validate the roles against user roles and tenant roles.
     *
     * @param inputRoles Input roles.
     * @return relevant error string or empty string.
     * @throws APIManagementException API Management Exception.
     */
    public static String validateUserRoles(List<String> inputRoles) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        boolean isMatched = false;
        String[] userRoleList = null;

        if (APIUtil.hasPermission(userName, APIConstants.Permissions.APIM_ADMIN)) {
            isMatched = true;
        } else {
            userRoleList = APIUtil.getListOfRoles(userName);
        }
        if (inputRoles != null && !inputRoles.isEmpty()) {
            if (Boolean.parseBoolean(System.getProperty(APIConstants.CASE_SENSITIVE_CHECK_PATH))) {
                String status = "";
                String roleString = String.join(",", inputRoles);
                if (userRoleList != null) {
                    for (String inputRole : inputRoles) {
                        if (!isMatched && userRoleList != null && APIUtil.compareRoleList(userRoleList, inputRole)) {
                            isMatched = true;
                        }
                    }
                    if (!APIUtil.isRoleNameExist(userName, roleString)) {
                        status = "Invalid user roles found in accessControlRole list";
                    }
                    status = isMatched && StringUtils.isBlank(status) ?
                            "" :
                            "This user does not have at least one role specified in API access control.";
                } else {
                    status = "Invalid user roles found";
                }
                return status;
            }
            if (!isMatched && userRoleList != null) {
                for (String inputRole : inputRoles) {
                    if (APIUtil.compareRoleList(userRoleList, inputRole)) {
                        isMatched = true;
                        break;
                    }
                }
                return isMatched ? "" : "This user does not have at least one role specified in API access control.";
            }

            String roleString = String.join(",", inputRoles);
            if (!APIUtil.isRoleNameExist(userName, roleString)) {
                return "Invalid user roles found in accessControlRole list";
            }
        }
        return "";
    }

    /**
     * To validate the roles against and tenant roles.
     *
     * @param inputRoles Input roles.
     * @return relevant error string or empty string.
     * @throws APIManagementException API Management Exception.
     */
    public static String validateRoles(List<String> inputRoles) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        boolean isMatched = false;
        if (inputRoles != null && !inputRoles.isEmpty()) {
            String roleString = String.join(",", inputRoles);
            isMatched = APIUtil.isRoleNameExist(userName, roleString);
            if (!isMatched) {
                return "Invalid user roles found in visibleRoles list";
            }
        }
        return "";
    }

    /**
     * To validate the additional properties.
     * Validation will be done for the keys of additional properties. Property keys should not contain spaces in it
     * and property keys should not conflict with reserved key words.
     *
     * @param additionalProperties Map<String, String>  properties to validate
     * @return error message if there is an validation error with additional properties.
     */
    public static String validateAdditionalProperties(List<APIInfoAdditionalPropertiesDTO> additionalProperties) {

        if (additionalProperties != null) {
            for (APIInfoAdditionalPropertiesDTO property : additionalProperties) {
                if (property.getName() == null || property.getValue() == null || property.isDisplay() == null) {
                    return "Property name, value or display status should not be null";
                }
                String propertyKey = property.getName();
                String propertyValue = property.getValue();
                if (propertyKey.contains(" ")) {
                    return "Property names should not contain space character. Property '" + propertyKey + "' "
                            + "contains space in it.";
                }
                if (Arrays.asList(APIConstants.API_SEARCH_PREFIXES).contains(propertyKey.toLowerCase())) {
                    return "Property '" + propertyKey + "' conflicts with the reserved keywords. Reserved keywords "
                            + "are [" + Arrays.toString(APIConstants.API_SEARCH_PREFIXES) + "]";
                }
                // Maximum allowable characters of registry property name and value is 100 and 1000. Hence we are
                // restricting them to be within 80 and 900.
                if (propertyKey.length() > 80) {
                    return "Property name can have maximum of 80 characters. Property '" + propertyKey + "' + contains "
                            + propertyKey.length() + "characters";
                }
                if (propertyValue.length() > 900) {
                    return "Property value can have maximum of 900 characters. Property '" + propertyKey + "' + "
                            + "contains a value with " + propertyValue.length() + "characters";
                }
            }
        }
        return "";
    }

    /**
     * validate user inout scopes.
     *
     * @param api api information
     * @throws APIManagementException throw if validation failure
     */
    public static void validateScopes(API api) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getInternalOrganizationId(api.getOrganization());
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        Set<org.wso2.carbon.apimgt.api.model.Scope> sharedAPIScopes = new HashSet<>();

        for (org.wso2.carbon.apimgt.api.model.Scope scope : api.getScopes()) {
            String scopeName = scope.getKey();
            if (!(APIUtil.isAllowedScope(scopeName))) {
                // Check if each scope key is already assigned as a local scope to a different API which is also not a
                // different version of the same API. If true, return error.
                // If false, check if the scope key is already defined as a shared scope. If so, do not honor the
                // other scope attributes (description, role bindings) in the request payload, replace them with
                // already defined values for the existing shared scope.
                if (apiProvider.isScopeKeyAssignedLocally(api.getId().getApiName(), scopeName, api.getOrganization())) {
                    throw new APIManagementException(
                            "Scope " + scopeName + " is already assigned locally by another API",
                            ExceptionCodes.SCOPE_ALREADY_ASSIGNED);
                } else if (apiProvider.isSharedScopeNameExists(scopeName, tenantId)) {
                    sharedAPIScopes.add(scope);
                    continue;
                }
            }

            //set display name as empty if it is not provided
            if (StringUtils.isBlank(scope.getName())) {
                scope.setName(scopeName);
            }

            //set description as empty if it is not provided
            if (StringUtils.isBlank(scope.getDescription())) {
                scope.setDescription("");
            }
            if (scope.getRoles() != null) {
                for (String aRole : scope.getRoles().split(",")) {
                    boolean isValidRole = APIUtil.isRoleNameExist(username, aRole);
                    if (!isValidRole) {
                        String errorMessage = "Role '" + aRole + "' does not exist.";
                        throw new APIManagementException(errorMessage,
                                ExceptionCodes.from(ExceptionCodes.ROLE_OF_SCOPE_DOES_NOT_EXIST, aRole));
                    }
                }
            }
        }
        apiProvider.validateSharedScopes(sharedAPIScopes, tenantDomain);
    }

    /**
     * Add API with the generated swagger from the DTO.
     *
     * @param apiDto       API DTO of the API
     * @param oasVersion   Open API Definition version
     * @param username     Username
     * @param organization Organization Identifier
     * @return Created API object
     * @throws APIManagementException Error while creating the API
     * @throws CryptoException        Error while encrypting
     */
    public static API addAPIWithGeneratedSwaggerDefinition(APIDTO apiDto, String oasVersion, String username,
                                                           String organization, OrganizationInfo orgInfo)
            throws APIManagementException, CryptoException {
        String name = apiDto.getName();
        ArtifactType artifactType = null;
        apiDto.setName(name.trim().replaceAll("\\s{2,}", " "));
        if (APIDTO.TypeEnum.ASYNC.equals(apiDto.getType())) {
            throw new APIManagementException("ASYNC API type does not support API creation from scratch",
                    ExceptionCodes.API_CREATION_NOT_SUPPORTED_FOR_ASYNC_TYPE_APIS);
        }
        boolean isWSAPI = APIDTO.TypeEnum.WS.equals(apiDto.getType());
        boolean isAsyncAPI =
                isWSAPI || APIDTO.TypeEnum.WEBSUB.equals(apiDto.getType()) ||
                        APIDTO.TypeEnum.SSE.equals(apiDto.getType()) || APIDTO.TypeEnum.ASYNC.equals(apiDto.getType());
        boolean isEgressAPI = apiDto.isEgress();
        username = StringUtils.isEmpty(username) ? RestApiCommonUtil.getLoggedInUsername() : username;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        // validate context before proceeding
        try {
            APIUtil.validateAPIContext(apiDto.getContext(), apiDto.getName());
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while importing API: " + e.getMessage(),
                    ExceptionCodes.from(ExceptionCodes.API_CONTEXT_MALFORMED_EXCEPTION, e.getMessage()));
        }

        // validate web socket api endpoint configurations
        if (isWSAPI && !PublisherCommonUtils.isValidWSAPI(apiDto)) {
            throw new APIManagementException("Endpoint URLs should be valid web socket URLs",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        // validate sandbox and production endpoints
        if (!PublisherCommonUtils.validateEndpoints(apiDto)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        // validate gateway type before proceeding
        String gatewayType = apiDto.getGatewayType();
        if (APIConstants.WSO2_APK_GATEWAY.equals(gatewayType)) {
            if (!(APIDTO.TypeEnum.HTTP.equals(apiDto.getType()) || APIDTO.TypeEnum.GRAPHQL.equals(apiDto.getType()))) {
                throw new APIManagementException("APIs of type " + apiDto.getType() + " are not supported with " +
                        "WSO2 APK", ExceptionCodes.INVALID_GATEWAY_TYPE);
            }
        }

        Map endpointConfig = (Map) apiDto.getEndpointConfig();
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

        // OAuth 2.0 backend protection: API Key and API Secret encryption
        encryptEndpointSecurityOAuthCredentials(endpointConfig, cryptoUtil, StringUtils.EMPTY, StringUtils.EMPTY,
                apiDto);

        encryptEndpointSecurityApiKeyCredentials(endpointConfig, cryptoUtil, StringUtils.EMPTY, StringUtils.EMPTY,
                apiDto);

        // AWS Lambda: secret key encryption while creating the API
        if (apiDto.getEndpointConfig() != null) {
            if (endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)) {
                String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                if (!StringUtils.isEmpty(secretKey)) {
                    String encryptedSecretKey = cryptoUtil.encryptAndBase64Encode(secretKey.getBytes());
                    endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                    apiDto.setEndpointConfig(endpointConfig);
                }
            }
        }

        API apiToAdd = prepareToCreateAPIByDTO(apiDto, apiProvider, username, organization);
        validateScopes(apiToAdd);
        //validate API categories
        List<APICategory> apiCategories = apiToAdd.getApiCategories();
        List<APICategory> apiCategoriesList = new ArrayList<>();
        for (APICategory category : apiCategories) {
            category.setOrganization(organization);
            apiCategoriesList.add(category);
        }
        apiToAdd.setApiCategories(apiCategoriesList);
        if (apiCategoriesList.size() > 0) {
            if (!APIUtil.validateAPICategories(apiCategoriesList, organization)) {
                throw new APIManagementException("Invalid API Category name(s) defined",
                        ExceptionCodes.from(ExceptionCodes.API_CATEGORY_INVALID));
            }
        }

        // Set API Key security for Egress APIs by default
        if (isEgressAPI) {
            apiToAdd.setApiSecurity(APIConstants.API_SECURITY_API_KEY);
        }

        if (!isAsyncAPI) {
            APIDefinition oasParser;
            if (RestApiConstants.OAS_VERSION_2.equalsIgnoreCase(oasVersion)) {
                oasParser = new OAS2Parser();
            } else if (RestApiConstants.OAS_VERSION_31.equalsIgnoreCase(oasVersion)) {
                oasParser = new OAS3Parser(RestApiConstants.OAS_VERSION_31);
            } else {
                oasParser = new OAS3Parser();
            }
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            String apiDefinition = oasParser.generateAPIDefinition(swaggerData);
            apiToAdd.setSwaggerDefinition(apiDefinition);
            artifactType = ArtifactType.API;
        } else {
            AsyncApiParser asyncApiParser = new AsyncApiParser();
            String asyncApiDefinition = asyncApiParser.generateAsyncAPIDefinition(apiToAdd);
            apiToAdd.setAsyncApiDefinition(asyncApiDefinition);
            artifactType = ArtifactType.API;
        }

        apiToAdd.setOrganization(organization);
        if (isAsyncAPI) {
            AsyncApiParser asyncApiParser = new AsyncApiParser();
            String apiDefinition = asyncApiParser.generateAsyncAPIDefinition(apiToAdd);
            apiToAdd.setAsyncApiDefinition(apiDefinition);
        }
        if (orgInfo != null && orgInfo.getOrganizationId() != null) {
            String visibleOrgs = apiToAdd.getVisibleOrganizations();
            if (!StringUtils.isEmpty(visibleOrgs) && APIConstants.VISIBLE_ORG_ALL.equals(visibleOrgs)) {
                // IF visibility is all
                apiToAdd.setVisibleOrganizations(APIConstants.VISIBLE_ORG_ALL);
            } else if (StringUtils.isEmpty(visibleOrgs) || APIConstants.VISIBLE_ORG_NONE.equals(visibleOrgs)) {
                // IF visibility is none 
                apiToAdd.setVisibleOrganizations(orgInfo.getOrganizationId()); // set to current org
            } else {
                // add current id to existing visibility list
                visibleOrgs = visibleOrgs + "," + orgInfo.getOrganizationId();
                apiToAdd.setVisibleOrganizations(visibleOrgs);
            }
            OrganizationTiers parentOrgTiers = new OrganizationTiers(orgInfo.getOrganizationId(),
                    apiToAdd.getAvailableTiers());
            Set<OrganizationTiers> currentOrganizationTiers = apiToAdd.getAvailableTiersForOrganizations();
            if (currentOrganizationTiers == null) {
                currentOrganizationTiers = new HashSet<>();
            }
            currentOrganizationTiers.add(parentOrgTiers);
            apiToAdd.setAvailableTiersForOrganizations(currentOrganizationTiers);
        } else {
            // Set the visibility to tenant domain if user does not belong to an organization.
            apiToAdd.setVisibleOrganizations(organization);
        }

        //adding the api
        Map<String, String> complianceResult = checkGovernanceComplianceSync(apiToAdd.getUuid(),
                APIMGovernableState.API_CREATE, artifactType, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        apiProvider.addAPI(apiToAdd);
        checkGovernanceComplianceAsync(apiToAdd.getUuid(), APIMGovernableState.API_CREATE, artifactType, organization);
        // Remove parentOrgTiers from OrganizationTiers list
        Set<OrganizationTiers> updatedOrganizationTiers = apiToAdd.getAvailableTiersForOrganizations();
        if (updatedOrganizationTiers != null) {
            updatedOrganizationTiers.removeIf(tier -> tier.getOrganizationID().equals(orgInfo.getOrganizationId()));
            apiToAdd.setAvailableTiersForOrganizations(updatedOrganizationTiers);
        }
        return apiToAdd;
    }

    /**
     * Validate endpoint configurations of {@link APIDTO} for web socket endpoints.
     *
     * @param api api model
     * @return validity of the web socket api
     */
    public static boolean isValidWSAPI(APIDTO api) {

        boolean containsEndpoint = false;
        boolean isValidProdUrl = true;
        boolean isValidSandboxUrl = true;
        if (api.getEndpointConfig() != null) {
            Map endpointConfig = (Map) api.getEndpointConfig();
            if (endpointConfig.containsKey(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                String prodEndpointUrl = String.valueOf(((Map) endpointConfig.get(
                        APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS)).get(APIConstants.API_DATA_URL));
                isValidProdUrl = prodEndpointUrl.startsWith(APIConstants.WS_PROTOCOL_URL_PREFIX)
                        || prodEndpointUrl.startsWith(APIConstants.WSS_PROTOCOL_URL_PREFIX);
                containsEndpoint = true;
            }
            if (endpointConfig.containsKey(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                String sandboxEndpointUrl = String.valueOf(((Map) endpointConfig.get(
                        APIConstants.ENDPOINT_SANDBOX_ENDPOINTS)).get(APIConstants.API_DATA_URL));
                isValidSandboxUrl = sandboxEndpointUrl.startsWith(APIConstants.WS_PROTOCOL_URL_PREFIX)
                        || sandboxEndpointUrl.startsWith(APIConstants.WSS_PROTOCOL_URL_PREFIX);
                containsEndpoint = true;
            }
        }
        return containsEndpoint && isValidProdUrl && isValidSandboxUrl;
    }

    /**
     * Validate endpoint configurations.
     *
     * @param apiDto the APIDTO object containing the endpoint configuration to validate
     * @return true if the endpoint configuration is valid, false otherwise
     */
    public static boolean validateEndpointConfigs(APIDTO apiDto) {
        Map endpointConfigsMap = (Map) apiDto.getEndpointConfig();
        if (endpointConfigsMap != null) {
            for (Object config : endpointConfigsMap.keySet()) {
                if (config instanceof String) {
                    if (SESSION_TIMEOUT_CONFIG_KEY.equals(config)) {
                        Object value = endpointConfigsMap.get(config);
                        if (value == null) {
                            continue;
                        }
                        String strVal;
                        if (value instanceof String) {
                            strVal = (String) value;
                            if (strVal.length() == 0) {
                                continue;
                            }
                        } else if (value instanceof Integer || value instanceof Long) {
                            strVal = value.toString();
                        } else if (value instanceof Double) {
                            strVal = Integer.toString(((Double) value).intValue());
                        } else {
                            return false;
                        }
                        try {
                            Long.parseLong(strVal);
                        } catch (NumberFormatException e) {
                            log.error("Failed to parse " + SESSION_TIMEOUT_CONFIG_KEY, e);
                            return false;
                        }
                        endpointConfigsMap.put(config, strVal);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Validate sandbox and production endpoint URLs.
     *
     * @param apiDto API DTO of the API
     * @return validity of URLs found within the endpoint configurations of the DTO
     */
    public static boolean validateEndpoints(APIDTO apiDto) throws APIManagementException {

        ArrayList<String> endpoints = new ArrayList<>();
        org.json.JSONObject endpointConfiguration = new org.json.JSONObject((Map) apiDto.getEndpointConfig());

        if (!endpointConfiguration.isNull(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE) && StringUtils.equals(
                endpointConfiguration.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE).toString(),
                APIConstants.ENDPOINT_TYPE_DEFAULT)) {
            // if the endpoint type is dynamic, then the validation should be skipped
            return true;
        }

        // extract sandbox endpoint URL(s)
        extractURLsFromEndpointConfig(endpointConfiguration, APIConstants.API_DATA_SANDBOX_ENDPOINTS, endpoints);

        // extract production endpoint URL(s)
        extractURLsFromEndpointConfig(endpointConfiguration, APIConstants.API_DATA_PRODUCTION_ENDPOINTS, endpoints);

        //extract external endpoint URL(s) from advertised info
        extractExternalEndpoints(apiDto, endpoints);

        return APIUtil.validateEndpointURLs(endpoints);
    }

    /**
     * Extract sandbox or production endpoint URLs from endpoint config object.
     *
     * @param endpointConfigObj Endpoint config JSON object
     * @param endpointType      Indicating whether Sandbox or Production endpoints are to be extracted
     * @param endpoints         List of URLs. Extracted URL(s), if any, are added to this list.
     */
    private static void extractURLsFromEndpointConfig(org.json.JSONObject endpointConfigObj, String endpointType,
                                                      ArrayList<String> endpoints) throws APIManagementException {
        if (!endpointConfigObj.isNull(endpointType)) {
            org.json.JSONObject endpointObj = endpointConfigObj.optJSONObject(endpointType);
            if (endpointObj != null) {
                if (endpointObj.has(APIConstants.API_DATA_URL)) {
                    endpoints.add(endpointConfigObj.getJSONObject(endpointType).getString(APIConstants.API_DATA_URL));
                } else {
                    ErrorHandler errorHandler = ExceptionCodes.from(ExceptionCodes.ENDPOINT_URL_NOT_PROVIDED,
                            endpointType);
                    throw new APIManagementException(
                            "Url is not provided for the endpoint type: " + endpointType + " in the endpoint " +
                                    "config",
                            errorHandler);
                }
            } else {
                org.json.JSONArray endpointArray = endpointConfigObj.getJSONArray(endpointType);
                for (int i = 0; i < endpointArray.length(); i++) {
                    endpoints.add((String) endpointArray.getJSONObject(i).get(APIConstants.API_DATA_URL));
                }
            }
        }
    }

    /**
     * Extract sandbox and production external endpoint URLs and external dev portal URL.
     *
     * @param apiDto    API DTO of the API
     * @param endpoints List of URLs. Extracted URL(s), if any, are added to this list.
     */
    private static void extractExternalEndpoints(APIDTO apiDto, ArrayList<String> endpoints) {

        if (apiDto != null && apiDto.getAdvertiseInfo() != null &&
                Boolean.TRUE.equals(apiDto.getAdvertiseInfo().isAdvertised())) {
            AdvertiseInfoDTO advertiseInfoDto = apiDto.getAdvertiseInfo();
            String externalProductionEndpoint = advertiseInfoDto.getApiExternalProductionEndpoint();
            if (externalProductionEndpoint != null && !externalProductionEndpoint.isEmpty()) {
                endpoints.add(externalProductionEndpoint);
            }
            String externalSandboxEndpoint = advertiseInfoDto.getApiExternalSandboxEndpoint();
            if (externalSandboxEndpoint != null && !externalSandboxEndpoint.isEmpty()) {
                endpoints.add(externalSandboxEndpoint);
            }
            String originalDevPortalUrl = advertiseInfoDto.getOriginalDevPortalUrl();
            if (originalDevPortalUrl != null && !originalDevPortalUrl.isEmpty()) {
                endpoints.add(originalDevPortalUrl);
            }
        }
    }

    public static String constructEndpointConfigForService(String serviceUrl, String protocol) {

        StringBuilder sb = new StringBuilder();
        String endpointType = APIDTO.TypeEnum.HTTP.value().toLowerCase();
        if (StringUtils.isNotEmpty(protocol) && (APIDTO.TypeEnum.SSE.equals(protocol.toUpperCase())
                || APIDTO.TypeEnum.WS.equals(protocol.toUpperCase()))) {
            endpointType = "ws";
        }
        if (StringUtils.isNotEmpty(serviceUrl)) {
            sb.append("{\"endpoint_type\": \"")
                    .append(endpointType)
                    .append("\",")
                    .append("\"production_endpoints\": {\"url\": \"")
                    .append(serviceUrl)
                    .append("\"}}");
        } // TODO Need to check on the endpoint security
        return sb.toString();
    }

    public static APIDTO.TypeEnum getAPIType(ServiceEntry.DefinitionType definitionType, String protocol)
            throws APIManagementException {
        if (ServiceEntry.DefinitionType.ASYNC_API.equals(definitionType)) {
            if (protocol.isEmpty()) {
                throw new APIManagementException("A protocol should be specified in the Async API definition",
                        ExceptionCodes.MISSING_PROTOCOL_IN_ASYNC_API_DEFINITION);
            } else if (!APIConstants.API_TYPE_WEBSUB.equals(protocol.toUpperCase()) &&
                    !APIConstants.API_TYPE_SSE.equals(protocol.toUpperCase()) &&
                    !APIConstants.API_TYPE_WS.equals(protocol.toUpperCase())) {
                throw new APIManagementException("Unsupported protocol specified in Async API Definition",
                        ExceptionCodes.UNSUPPORTED_PROTOCOL_SPECIFIED_IN_ASYNC_API_DEFINITION);
            }

        }
        switch (definitionType) {
            case WSDL1:
            case WSDL2:
                return APIDTO.TypeEnum.SOAP;
            case GRAPHQL_SDL:
                return APIDTO.TypeEnum.GRAPHQL;
            case ASYNC_API:
                return APIDTO.TypeEnum.fromValue(protocol.toUpperCase());
            default:
                return APIDTO.TypeEnum.HTTP;
        }
    }

    /**
     * Prepares the API Model object to be created using the DTO object.
     *
     * @param body         APIDTO of the API
     * @param apiProvider  API Provider
     * @param username     Username
     * @param organization Organization Identifier
     * @return API object to be created
     * @throws APIManagementException Error while creating the API
     */
    public static API prepareToCreateAPIByDTO(APIDTO body, APIProvider apiProvider, String username,
                                              String organization)
            throws APIManagementException {

        String context = body.getContext();
        //Make sure context starts with "/". ex: /pizza
        context = context.startsWith("/") ? context : ("/" + context);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(organization) &&
                !context.contains("/t/" + organization)) {
            //Create tenant aware context for API
            context = "/t/" + organization + context;
        }
        body.setContext(context);

        if (body.getAccessControlRoles() != null) {
            String errorMessage = PublisherCommonUtils.validateUserRoles(body.getAccessControlRoles());

            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }
        if (body.getAdditionalProperties() != null) {
            String errorMessage = PublisherCommonUtils.validateAdditionalProperties(body.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes
                        .from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES, body.getName(), body.getVersion()));
            }
        }
        if (body.getContext() == null) {
            throw new APIManagementException("Parameter: \"context\" cannot be null",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        } else if (body.getContext().endsWith("/")) {
            throw new APIManagementException("Context cannot end with '/' character",
                    ExceptionCodes.from(ExceptionCodes.INVALID_CONTEXT, body.getName(), body.getVersion()));
        }
        if (apiProvider.isApiNameWithDifferentCaseExist(body.getName(), organization)) {
            throw new APIManagementException(
                    "Error occurred while adding API. API with name " + body.getName() + " already exists.",
                    ExceptionCodes.from(ExceptionCodes.API_NAME_ALREADY_EXISTS, body.getName()));
        }
        if (body.getAuthorizationHeader() == null) {
            body.setAuthorizationHeader(APIUtil.getOAuthConfigurationFromAPIMConfig(APIConstants.AUTHORIZATION_HEADER));
        }
        if (body.getAuthorizationHeader() == null) {
            body.setAuthorizationHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT);
        }
        if (body.getApiKeyHeader() == null) {
            body.setApiKeyHeader(APIConstants.API_KEY_HEADER_DEFAULT);
        }

        if (body.getVisibility() == APIDTO.VisibilityEnum.RESTRICTED && body.getVisibleRoles().isEmpty()) {
            throw new APIManagementException(
                    "Valid roles should be added under 'visibleRoles' to restrict " + "the visibility",
                    ExceptionCodes.USER_ROLES_CANNOT_BE_NULL);
        }
        if (body.getVisibleRoles() != null) {
            String errorMessage = PublisherCommonUtils.validateRoles(body.getVisibleRoles());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }


        //Get all existing versions of  api been adding
        List<String> apiVersions = apiProvider.getApiVersionsMatchingApiNameAndOrganization(body.getName(),
                username, organization);
        if (apiVersions.size() > 0) {
            //If any previous version exists
            for (String version : apiVersions) {
                if (version.equalsIgnoreCase(body.getVersion())) {
                    //If version already exists
                    if (apiProvider.isDuplicateContextTemplateMatchingOrganization(context, organization)) {
                        throw new APIManagementException(
                                "Error occurred while " + "adding the API. A duplicate API already exists for "
                                        + context + " in the organization : " + organization,
                                ExceptionCodes.API_ALREADY_EXISTS);
                    } else {
                        throw new APIManagementException(
                                "Error occurred while adding API. API with name " + body.getName()
                                        + " already exists with different context" + context + " in the organization" +
                                        " : " + organization, ExceptionCodes.API_ALREADY_EXISTS);
                    }
                }
            }
        } else {
            //If no any previous version exists
            if (apiProvider.isDuplicateContextTemplateMatchingOrganization(context, organization)) {
                throw new APIManagementException(
                        "Error occurred while adding the API. A duplicate API context already exists for "
                                + context + " in the organization" + " : " + organization, ExceptionCodes
                        .from(ExceptionCodes.API_CONTEXT_ALREADY_EXISTS, context));
            }
        }

        String contextTemplate = body.getContext().contains(APIConstants.VERSION_PLACEHOLDER) ?
                body.getContext() :
                body.getContext() + "/" + APIConstants.VERSION_PLACEHOLDER;
        if (!apiProvider.isValidContext(body.getProvider(), body.getName(), contextTemplate, username, organization)) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.BLOCK_CONDITION_UNSUPPORTED_API_CONTEXT));
        }

        //Check if the user has admin permission before applying a different provider than the current user
        String provider = body.getProvider();
        if (!StringUtils.isBlank(provider) && !provider.equals(username)) {
            if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
                if (log.isDebugEnabled()) {
                    log.debug("User " + username + " does not have admin permission ("
                            + APIConstants.Permissions.APIM_ADMIN + ") hence provider (" + provider
                            + ") overridden with current user (" + username + ")");
                }
                provider = username;
            } else {
                if (!APIUtil.isUserExist(provider)) {
                    throw new APIManagementException("Specified provider " + provider + " not exist.",
                            ExceptionCodes.PARAMETER_NOT_PROVIDED);
                }
            }
        } else {
            //Set username in case provider is null or empty
            provider = username;
        }

        List<String> tiersFromDTO = body.getPolicies();

        //check whether the added API's tiers are all valid
        Set<Tier> definedTiers = apiProvider.getTiers();
        List<String> invalidTiers = getInvalidTierNames(definedTiers, tiersFromDTO);
        if (invalidTiers.size() > 0) {
            throw new APIManagementException(
                    "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid",
                    ExceptionCodes.TIER_NAME_INVALID);
        }
        APIPolicy apiPolicy = apiProvider.getAPIPolicy(username, body.getApiThrottlingPolicy());
        if (apiPolicy == null && body.getApiThrottlingPolicy() != null) {
            throw new APIManagementException("Specified policy " + body.getApiThrottlingPolicy() + " is invalid",
                    ExceptionCodes.UNSUPPORTED_THROTTLE_LIMIT_TYPE);
        }

        API apiToAdd = APIMappingUtil.fromDTOtoAPI(body, provider);
        //Overriding some properties:
        //only allow CREATED as the stating state for the new api if not status is PROTOTYPED
        if (!APIConstants.PROTOTYPED.equals(apiToAdd.getStatus())) {
            apiToAdd.setStatus(APIConstants.CREATED);
        }

        if (!apiToAdd.isAdvertiseOnly() || StringUtils.isBlank(apiToAdd.getApiOwner())) {
            //we are setting the api owner as the logged in user until we support checking admin privileges and
            //assigning the owner as a different user
            apiToAdd.setApiOwner(provider);
        }

        if (body.getKeyManagers() instanceof List) {
            apiToAdd.setKeyManagers((List<String>) body.getKeyManagers());
        } else if (body.getKeyManagers() == null) {
            apiToAdd.setKeyManagers(Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS));
        } else {
            String errMsg = "KeyManagers value needs to be an array";
            ExceptionCodes errorHandler = ExceptionCodes.KEYMANAGERS_VALUE_NOT_ARRAY;
            throw new APIManagementException(errMsg, errorHandler);
        }

        // Set default gatewayVendor
        if (body.getGatewayVendor() == null) {
            apiToAdd.setGatewayVendor(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
        }
        apiToAdd.setOrganization(organization);
        apiToAdd.setGatewayType(body.getGatewayType());
        if (body.getSubtypeConfiguration() != null && body.getSubtypeConfiguration().getSubtype() != null
                && APIConstants.API_SUBTYPE_AI_API.equals(body.getSubtypeConfiguration().getSubtype())
                && body.getSubtypeConfiguration().getConfiguration() != null) {
            apiToAdd.setAiConfiguration(new Gson().fromJson(
                    body.getSubtypeConfiguration().getConfiguration().toString(), AIConfiguration.class));
            apiToAdd.setSubtype(APIConstants.API_SUBTYPE_AI_API);
        } else {
            apiToAdd.setSubtype(APIConstants.API_SUBTYPE_DEFAULT);
        }
        apiToAdd.setEgress(body.isEgress() ? 1 : 0);
        return apiToAdd;
    }

    /**
     * Builds the token based throttling configuration from APIMaxTpsTokenBasedThrottlingConfigurationDTO.
     *
     * @param throttlingConfigDTO The APIMaxTpsTokenBasedThrottlingConfigurationDTO to extract data from.
     * @return The TokenBasedThrottlingCountHolder object.
     */
    public static TokenBasedThrottlingCountHolder buildThrottlingConfiguration(
            APIMaxTpsTokenBasedThrottlingConfigurationDTO throttlingConfigDTO) {

        TokenBasedThrottlingCountHolder throttlingConfig = new TokenBasedThrottlingCountHolder();

        if (throttlingConfigDTO.getProductionMaxPromptTokenCount() != null) {
            throttlingConfig.setProductionMaxPromptTokenCount(
                    throttlingConfigDTO.getProductionMaxPromptTokenCount().toString());
        }
        if (throttlingConfigDTO.getProductionMaxCompletionTokenCount() != null) {
            throttlingConfig.setProductionMaxCompletionTokenCount(
                    throttlingConfigDTO.getProductionMaxCompletionTokenCount().toString());
        }
        if (throttlingConfigDTO.getProductionMaxTotalTokenCount() != null) {
            throttlingConfig.setProductionMaxTotalTokenCount(
                    throttlingConfigDTO.getProductionMaxTotalTokenCount().toString());
        }
        if (throttlingConfigDTO.getSandboxMaxPromptTokenCount() != null) {
            throttlingConfig.setSandboxMaxPromptTokenCount(
                    throttlingConfigDTO.getSandboxMaxPromptTokenCount().toString());
        }
        if (throttlingConfigDTO.getSandboxMaxCompletionTokenCount() != null) {
            throttlingConfig.setSandboxMaxCompletionTokenCount(
                    throttlingConfigDTO.getSandboxMaxCompletionTokenCount().toString());
        }
        if (throttlingConfigDTO.getSandboxMaxTotalTokenCount() != null) {
            throttlingConfig.setSandboxMaxTotalTokenCount(
                    throttlingConfigDTO.getSandboxMaxTotalTokenCount().toString());
        }
        throttlingConfig.setTokenBasedThrottlingEnabled(throttlingConfigDTO
                .isIsTokenBasedThrottlingEnabled());

        return throttlingConfig;
    }

    /**
     * Builds the throttling configuration DTO from AIConfiguration.
     *
     * @return The built APIAiConfigurationThrottlingConfigurationDTO object.
     */
    public static APIMaxTpsTokenBasedThrottlingConfigurationDTO buildThrottlingConfigurationDTO(
            TokenBasedThrottlingCountHolder throttlingConfig) {

        APIMaxTpsTokenBasedThrottlingConfigurationDTO throttlingConfigurationsDTO =
                new APIMaxTpsTokenBasedThrottlingConfigurationDTO();
        try {
            if (throttlingConfig.getProductionMaxPromptTokenCount() != null) {
                throttlingConfigurationsDTO.setProductionMaxPromptTokenCount(
                        Long.parseLong(throttlingConfig.getProductionMaxPromptTokenCount()));
            }
            if (throttlingConfig.getProductionMaxCompletionTokenCount() != null) {
                throttlingConfigurationsDTO.setProductionMaxCompletionTokenCount(
                        Long.parseLong(throttlingConfig.getProductionMaxCompletionTokenCount()));
            }
            if (throttlingConfig.getProductionMaxTotalTokenCount() != null) {
                throttlingConfigurationsDTO.setProductionMaxTotalTokenCount(
                        Long.parseLong(throttlingConfig.getProductionMaxTotalTokenCount()));
            }
            if (throttlingConfig.getSandboxMaxPromptTokenCount() != null) {
                throttlingConfigurationsDTO.setSandboxMaxPromptTokenCount(
                        Long.parseLong(throttlingConfig.getSandboxMaxPromptTokenCount()));
            }
            if (throttlingConfig.getSandboxMaxCompletionTokenCount() != null) {
                throttlingConfigurationsDTO.setSandboxMaxCompletionTokenCount(
                        Long.parseLong(throttlingConfig.getSandboxMaxCompletionTokenCount()));
            }
            if (throttlingConfig.getSandboxMaxTotalTokenCount() != null) {
                throttlingConfigurationsDTO.setSandboxMaxTotalTokenCount(
                        Long.parseLong(throttlingConfig.getSandboxMaxTotalTokenCount()));
            }
            throttlingConfigurationsDTO.setIsTokenBasedThrottlingEnabled(
                    throttlingConfig.isTokenBasedThrottlingEnabled());
        } catch (NumberFormatException e) {
            log.error("Cannot convert to Long format when setting AI throttling configurations for API", e);
        }
        return throttlingConfigurationsDTO;
    }

    public static String updateAPIDefinition(String apiId, APIDefinitionValidationResponse response,
                                             ServiceEntry service, String organization)
            throws APIManagementException, FaultGatewaysException {

        if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
            return updateSwagger(apiId, response, true, organization);
        } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
            return updateAsyncAPIDefinition(apiId, response, organization);
        }
        return null;
    }

    /**
     * update AsyncPI definition of the given api.
     *
     * @param apiId        API Id
     * @param response     response of the AsyncAPI definition validation call
     * @param organization identifier of the organization
     * @return updated AsyncAPI definition
     * @throws APIManagementException when error occurred updating AsyncAPI definition
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    public static String updateAsyncAPIDefinition(String apiId, APIDefinitionValidationResponse response,
                                                  String organization)
            throws APIManagementException, FaultGatewaysException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fall if user does not have access to the API or the API does not exist
        API oldapi = apiProvider.getAPIbyUUID(apiId, organization);
        API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
        existingAPI.setOrganization(organization);
        String apiDefinition = response.getJsonContent();

        AsyncApiParser asyncApiParser = new AsyncApiParser();
        // Set uri templates
        Set<URITemplate> uriTemplates = asyncApiParser.getURITemplates(apiDefinition, APIConstants.
                API_TYPE_WS.equals(existingAPI.getType()) || !APIConstants.WSO2_GATEWAY_ENVIRONMENT.equals
                (existingAPI.getGatewayVendor()));
        if (uriTemplates == null || uriTemplates.isEmpty()) {
            throw new APIManagementException(ExceptionCodes.NO_RESOURCES_FOUND);
        }
        existingAPI.setUriTemplates(uriTemplates);

        // Update ws uri mapping
        existingAPI.setWsUriMapping(asyncApiParser.buildWSUriMapping(apiDefinition));

        //updating APi with the new AsyncAPI definition
        existingAPI.setAsyncApiDefinition(apiDefinition);
        apiProvider.saveAsyncApiDefinition(existingAPI, apiDefinition);
        Map<String, String> complianceResult = checkGovernanceComplianceSync(existingAPI.getUuid(),
                APIMGovernableState.API_UPDATE, ArtifactType.API, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        apiProvider.updateAPI(existingAPI, oldapi);

        PublisherCommonUtils.checkGovernanceComplianceAsync(existingAPI.getUuid(), APIMGovernableState.API_UPDATE,
                ArtifactType.API, organization);
        //retrieves the updated AsyncAPI definition
        return apiProvider.getAsyncAPIDefinition(existingAPI.getId().getUUID(), organization);
    }

    /**
     * update swagger definition of the given api.
     *
     * @param apiId        API Id
     * @param response     response of a swagger definition validation call
     * @param organization Organization Identifier
     * @return updated swagger definition
     * @throws APIManagementException when error occurred updating swagger
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    public static String updateSwagger(String apiId, APIDefinitionValidationResponse response, boolean isServiceAPI,
                                       String organization)
            throws APIManagementException, FaultGatewaysException {

        return updateSwagger(apiId, response, isServiceAPI, organization, true);
    }

    /**
     * update swagger definition of the given api. For Soap To Rest APIs, sequences are generated on demand.
     *
     * @param apiId                       API Id
     * @param response                    response of a swagger definition validation call
     * @param organization                Organization Identifier
     * @param generateSoapToRestSequences Option to generate soap to rest sequences.
     * @return updated swagger definition
     * @throws APIManagementException when error occurred updating swagger
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    public static String updateSwagger(String apiId, APIDefinitionValidationResponse response, boolean isServiceAPI,
                                       String organization, boolean generateSoapToRestSequences)
            throws APIManagementException, FaultGatewaysException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
        APIDefinition oasParser = response.getParser();
        prepareForUpdateSwagger(apiId, response, isServiceAPI, apiProvider, organization, oasParser, existingAPI,
                generateSoapToRestSequences);

        //Update API is called to update URITemplates and scopes of the API
        API unModifiedAPI = apiProvider.getAPIbyUUID(apiId, organization);
        existingAPI.setStatus(unModifiedAPI.getStatus());
        Map<String, String> complianceResult = checkGovernanceComplianceSync(apiId, APIMGovernableState.API_UPDATE,
                ArtifactType.API, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        apiProvider.updateAPI(existingAPI, unModifiedAPI);

        //retrieves the updated swagger definition
        String apiSwagger = apiProvider.getOpenAPIDefinition(apiId, organization); // TODO see why we need to get it
        //instead of passing same
        return oasParser.getOASDefinitionForPublisher(existingAPI, apiSwagger);
    }

    /**
     * Prepare the API object before updating swagger.
     *
     * @param apiId        API Id
     * @param response     response of a swagger definition validation call
     * @param isServiceAPI whether the API is a service API or not
     * @param apiProvider  API Provider
     * @param organization tenant domain
     * @param oasParser    OASParser for the API definition
     * @param existingAPI  existing API
     * @throws APIManagementException when error occurred updating swagger
     */
    private static void prepareForUpdateSwagger(String apiId, APIDefinitionValidationResponse response,
                                                boolean isServiceAPI, APIProvider apiProvider, String organization,
                                                APIDefinition oasParser, API existingAPI, boolean genSoapToRestSequence)
            throws APIManagementException {

        String apiDefinition = response.getJsonContent();
        if (isServiceAPI) {
            apiDefinition = oasParser.copyVendorExtensions(existingAPI.getSwaggerDefinition(), apiDefinition);
        } else {
            apiDefinition = OASParserUtil.preProcess(apiDefinition);
        }
        if (APIConstants.API_TYPE_SOAPTOREST.equals(existingAPI.getType()) && genSoapToRestSequence) {
            List<SOAPToRestSequence> sequenceList = SequenceGenerator.generateSequencesFromSwagger(apiDefinition);
            existingAPI.setSoapToRestSequences(sequenceList);
        }
        Set<URITemplate> uriTemplates = null;
        uriTemplates = oasParser.getURITemplates(apiDefinition);

        if (uriTemplates == null || uriTemplates.isEmpty()) {
            throw new APIManagementException(ExceptionCodes.NO_RESOURCES_FOUND);
        }
        Set<org.wso2.carbon.apimgt.api.model.Scope> scopes = oasParser.getScopes(apiDefinition);
        //validating scope roles
        for (org.wso2.carbon.apimgt.api.model.Scope scope : scopes) {
            String roles = scope.getRoles();
            if (roles != null) {
                for (String aRole : roles.split(",")) {
                    boolean isValidRole = APIUtil.isRoleNameExist(RestApiCommonUtil.getLoggedInUsername(), aRole);
                    if (!isValidRole) {
                        String errorMessage = "Role '" + aRole + "' Does not exist.";
                        throw new APIManagementException(errorMessage,
                                ExceptionCodes.from(ExceptionCodes.ROLE_OF_SCOPE_DOES_NOT_EXIST, aRole));
                    }
                }
            }
        }

        List<APIResource> removedProductResources = apiProvider.getRemovedProductResources(uriTemplates, existingAPI);

        if (!removedProductResources.isEmpty()) {
            throw new APIManagementException(
                    "Cannot remove following resource paths " + removedProductResources.toString()
                            + " because they are used by one or more API Products", ExceptionCodes
                    .from(ExceptionCodes.API_PRODUCT_USED_RESOURCES, existingAPI.getId().getApiName(),
                            existingAPI.getId().getVersion()));
        }

        //set existing operation policies to URI templates
        apiProvider.setOperationPoliciesToURITemplates(apiId, uriTemplates);

        existingAPI.setUriTemplates(uriTemplates);
        existingAPI.setScopes(scopes);
        try {
            ObjectMapper mapper = ObjectMapperFactory.createJson();
            JsonNode newProductionEndpointJson = mapper.readTree(apiDefinition)
                    .get(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS);
            JsonNode newSandboxEndpointJson = mapper.readTree(apiDefinition)
                    .get(APIConstants.X_WSO2_SANDBOX_ENDPOINTS);
            String existingEndpointConfigString = existingAPI.getEndpointConfig();

            if (StringUtils.isNotEmpty(existingEndpointConfigString)) { //check if endpoints are configured
                JSONObject existingEndpointConfigJson = (JSONObject) new JSONParser()
                        .parse(existingEndpointConfigString);
                if (newProductionEndpointJson != null) {
                    if (existingEndpointConfigJson.get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS) != null) {
                        //put as a value under the ENDPOINT_PRODUCTION_ENDPOINTS key
                        //if loadbalance endpoints, get relevant jsonobject from array
                        if (existingEndpointConfigJson.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE)
                                .equals(APIConstants.ENDPOINT_TYPE_LOADBALANCE)) {
                            JSONArray productionConfigsJson = (JSONArray) existingEndpointConfigJson
                                    .get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
                            for (int i = 0; i < productionConfigsJson.size(); i++) {
                                if (!(((JSONObject) productionConfigsJson.get(i)).containsKey(APIConstants
                                        .API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
                                    if (newProductionEndpointJson.has(APIConstants
                                            .ADVANCE_ENDPOINT_CONFIG)) {
                                        JsonNode advanceConfig = newProductionEndpointJson
                                                .get(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                                        ((JSONObject) productionConfigsJson.get(i))
                                                .put(APIConstants.ADVANCE_ENDPOINT_CONFIG, advanceConfig);
                                    } else {
                                        ((JSONObject) productionConfigsJson.get(i))
                                                .remove(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                                    }
                                    break;
                                }
                            }
                            existingEndpointConfigJson.put(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS,
                                    productionConfigsJson);
                        } else {
                            JSONObject productionConfigsJson = (JSONObject) existingEndpointConfigJson
                                    .get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
                            if (newProductionEndpointJson.has(APIConstants.ADVANCE_ENDPOINT_CONFIG)) {
                                JsonNode advanceConfig = newProductionEndpointJson
                                        .get(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                                productionConfigsJson.put(APIConstants.ADVANCE_ENDPOINT_CONFIG, advanceConfig);
                            } else {
                                productionConfigsJson.remove(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                            }
                            existingEndpointConfigJson.put(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS,
                                    productionConfigsJson);
                        }
                    }
                }
                if (newSandboxEndpointJson != null) {
                    if (existingEndpointConfigJson.get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS) != null) {
                        //put as a value under the ENDPOINT_SANDBOX_ENDPOINTS key
                        //if loadbalance endpoints, get relevant jsonobject from array
                        if (existingEndpointConfigJson.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE)
                                .equals(APIConstants.ENDPOINT_TYPE_LOADBALANCE)) {
                            JSONArray sandboxConfigsJson = (JSONArray) existingEndpointConfigJson
                                    .get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
                            for (int i = 0; i < sandboxConfigsJson.size(); i++) {
                                if (!(((JSONObject) sandboxConfigsJson.get(i)).containsKey(APIConstants
                                        .API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
                                    if (newSandboxEndpointJson.has(APIConstants
                                            .ADVANCE_ENDPOINT_CONFIG)) {
                                        JsonNode advanceConfig = newSandboxEndpointJson
                                                .get(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                                        ((JSONObject) sandboxConfigsJson.get(i))
                                                .put(APIConstants.ADVANCE_ENDPOINT_CONFIG, advanceConfig);
                                    } else {
                                        ((JSONObject) sandboxConfigsJson.get(i))
                                                .remove(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                                    }
                                    break;
                                }
                            }
                            existingEndpointConfigJson.put(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS,
                                    sandboxConfigsJson);
                        } else {
                            JSONObject sandboxConfigsJson = (JSONObject) existingEndpointConfigJson
                                    .get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
                            if (newSandboxEndpointJson.has(APIConstants.ADVANCE_ENDPOINT_CONFIG)) {
                                JsonNode advanceConfig = newSandboxEndpointJson
                                        .get(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                                sandboxConfigsJson.put(APIConstants.ADVANCE_ENDPOINT_CONFIG, advanceConfig);
                            } else {
                                sandboxConfigsJson.remove(APIConstants.ADVANCE_ENDPOINT_CONFIG);
                            }
                            existingEndpointConfigJson.put(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS,
                                    sandboxConfigsJson);
                        }
                    }
                }
                existingAPI.setEndpointConfig(existingEndpointConfigJson.toString());
            }
        } catch (ParseException | JsonProcessingException e) {
            throw new APIManagementException("Error when parsing endpoint configurations ", e);
        }

        PublisherCommonUtils.validateScopes(existingAPI);

        SwaggerData swaggerData = new SwaggerData(existingAPI);
        String updatedApiDefinition = oasParser.populateCustomManagementInfo(apiDefinition, swaggerData);

        //Validate API with Federated Gateway before persisting to registry
        APIUtil.validateApiWithFederatedGateway(existingAPI);

        apiProvider.saveSwaggerDefinition(existingAPI, updatedApiDefinition, organization);
        existingAPI.setSwaggerDefinition(updatedApiDefinition);
    }

    /**
     * Add GraphQL schema.
     *
     * @param originalAPI      API
     * @param schemaDefinition GraphQL schema definition to add
     * @param apiProvider      API Provider
     * @return the arrayList of APIOperationsDTOextractGraphQLOperationList
     */
    public static API addGraphQLSchema(API originalAPI, String schemaDefinition, APIProvider apiProvider)
            throws APIManagementException, FaultGatewaysException {
        API oldApi = apiProvider.getAPIbyUUID(originalAPI.getUuid(), originalAPI.getOrganization());

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        List<URITemplate> operationList = graphql.extractGraphQLOperationList(schemaDefinition);
        List<APIOperationsDTO> operationArray = APIMappingUtil
                .fromURITemplateListToOprationList(operationList);
        List<APIOperationsDTO> operationListWithOldData = APIMappingUtil
                .getOperationListWithOldData(originalAPI.getUriTemplates(), operationArray, tenantId);

        Set<URITemplate> uriTemplates = APIMappingUtil.getURITemplates(originalAPI, operationListWithOldData);
        originalAPI.setUriTemplates(uriTemplates);

        apiProvider.saveGraphqlSchemaDefinition(originalAPI.getUuid(), schemaDefinition, originalAPI.getOrganization());
        Map<String, String> complianceResult = checkGovernanceComplianceSync(originalAPI.getUuid(),
                APIMGovernableState.API_UPDATE, ArtifactType.API, originalAPI.getOrganization(), null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }

        apiProvider.updateAPI(originalAPI, oldApi);
        PublisherCommonUtils.checkGovernanceComplianceAsync(originalAPI.getUuid(), APIMGovernableState.API_CREATE,
                ArtifactType.API, originalAPI.getOrganization());
        return originalAPI;
    }

    /**
     * Validate GraphQL Schema.
     *
     * @param filename         File name of the schema
     * @param schema           GraphQL schema
     * @param url              URL of the schema
     * @param useIntrospection use introspection to obtain schema
     * @return GraphQLValidationResponseDTO
     * @throws APIManagementException when error occurred while validating GraphQL schema
     */
    public static GraphQLValidationResponseDTO validateGraphQLSchema(String filename, String schema, String url,
                                                                     Boolean useIntrospection)
            throws APIManagementException {

        String errorMessage;
        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();
        boolean isValid = false;
        try {
            if (url != null && !url.isEmpty()) {
                if (useIntrospection) {
                    schema = generateGraphQLSchemaFromIntrospection(url);
                } else {
                    schema = retrieveGraphQLSchemaFromURL(url);
                }
            } else if (filename == null) {
                throw new APIManagementException("GraphQL filename cannot be null",
                        ExceptionCodes.INVALID_GRAPHQL_FILE);
            } else if (!filename.endsWith(".graphql") && !filename.endsWith(".txt") && !filename.endsWith(".sdl")) {
                throw new APIManagementException("Unsupported extension type of file: " + filename,
                        ExceptionCodes.UNSUPPORTED_GRAPHQL_FILE_EXTENSION);
            }

            if (schema == null || schema.isEmpty()) {
                throw new APIManagementException("GraphQL Schema cannot be empty or null to validate it",
                        ExceptionCodes.GRAPHQL_SCHEMA_CANNOT_BE_NULL);
            }

            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
            GraphQLSchema graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeRegistry);
            SchemaValidator schemaValidation = new SchemaValidator();
            Set<SchemaValidationError> validationErrors = schemaValidation.validateSchema(graphQLSchema);

            if (validationErrors.toArray().length > 0) {
                errorMessage = "InValid Schema";
                validationResponse.isValid(Boolean.FALSE);
                validationResponse.errorMessage(errorMessage);
            } else {
                validationResponse.setIsValid(Boolean.TRUE);
                GraphQLValidationResponseGraphQLInfoDTO graphQLInfo = new GraphQLValidationResponseGraphQLInfoDTO();
                GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
                List<URITemplate> operationList = graphql.extractGraphQLOperationList(typeRegistry, null);
                List<APIOperationsDTO> operationArray = APIMappingUtil.fromURITemplateListToOprationList(operationList);
                graphQLInfo.setOperations(operationArray);
                GraphQLSchemaDTO schemaObj = new GraphQLSchemaDTO();
                schemaObj.setSchemaDefinition(schema);
                graphQLInfo.setGraphQLSchema(schemaObj);
                validationResponse.setGraphQLInfo(graphQLInfo);
            }
            isValid = validationResponse.isIsValid();
            errorMessage = validationResponse.getErrorMessage();
        } catch (SchemaProblem e) {
            errorMessage = e.getMessage();
        }

        if (!isValid) {
            validationResponse.setIsValid(isValid);
            validationResponse.setErrorMessage(errorMessage);
        }
        return validationResponse;
    }

    /**
     * Generate the GraphQL schema by performing an introspection query on the provided endpoint.
     *
     * @param url The URL of the GraphQL endpoint to perform the introspection query on.
     * @return The GraphQL schema as a string.
     * @throws APIManagementException If an error occurs during the schema generation process
     */
    public static String generateGraphQLSchemaFromIntrospection(String url) throws APIManagementException {
        String schema = null;
        try {
            URL urlObj = new URL(url);
            HttpClient httpClient = APIUtil.getHttpClient(urlObj.getPort(), urlObj.getProtocol());
            Gson gson = new Gson();

            if (graphQLIntrospectionQuery == null || graphQLIntrospectionQuery.isEmpty()) {
                graphQLIntrospectionQuery = APIUtil.getIntrospectionQuery();
            }
            String requestBody = gson.toJson(
                    JsonParser.parseString("{\"query\": \"" + graphQLIntrospectionQuery + "\"}"));

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody));
            HttpResponse response = httpClient.execute(httpPost);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                String schemaResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> schemaMap = gson.fromJson(schemaResponse, type);
                Document schemaDocument = new IntrospectionResultToSchema().createSchemaDefinition(
                        (Map<String, Object>) schemaMap.get("data"));
                schema = AstPrinter.printAst(schemaDocument);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Unable to generate GraphQL schema from introspection."
                                    + " Endpoint returned response code: "
                                    + response.getStatusLine().getStatusCode());
                }
            }
        } catch (MalformedURLException e) {
            log.error("Invalid GraphQL Endpoint URL. Error: ", e);
            throw new APIManagementException("Invalid GraphQL Endpoint URL: ");
        } catch (IOException e) {
            log.error("I/O error occurred while executing GraphQL Introspection request. Error: ", e);
            throw new APIManagementException("I/O error occurred while executing HTTP request " +
                    "for GraphQL introspection.");
        } catch (JsonSyntaxException e) {
            log.error("Error parsing JSON response. Error: ", e);
            throw new APIManagementException("Error parsing GraphQL Introspection JSON response.", e);
        } catch (Exception e) {
            log.error("Exception occurred while generating GraphQL schema from endpoint. Exception: ", e);
            throw new APIManagementException("Error occurred while generating GraphQL schema from introspection",
                    ExceptionCodes.GENERATE_GRAPHQL_SCHEMA_FROM_INTROSPECTION_ERROR);
        }
        return schema;
    }

    /**
     * Retrieve the GraphQL schema from the specified URL.
     *
     * @param url The URL of the GraphQL schema to retrieve.
     * @return The GraphQL schema as a string.
     * @throws APIManagementException If an error occurs while retrieving the schema
     */
    public static String retrieveGraphQLSchemaFromURL(String url) throws APIManagementException {
        String schema = null;
        try {
            URL urlObj = new URL(url);
            HttpClient httpClient = APIUtil.getHttpClient(urlObj.getPort(), urlObj.getProtocol());
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schema = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Unable to generate GraphQL schema from url." + " URL returned response code: "
                                    + response.getStatusLine().getStatusCode());
                    throw new APIManagementException("Error occurred while retrieving GraphQL schema from schema URL",
                            ExceptionCodes.RETRIEVE_GRAPHQL_SCHEMA_FROM_URL_ERROR);
                }
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception occurred while generating GraphQL schema from url. Exception: " + e.getMessage(),
                        e);
            }
            throw new APIManagementException("Error occurred while retrieving GraphQL schema from schema URL",
                    ExceptionCodes.RETRIEVE_GRAPHQL_SCHEMA_FROM_URL_ERROR);
        }
        return schema;
    }

    /**
     * Update thumbnail of an API/API Product
     *
     * @param fileInputStream Input stream
     * @param fileContentType The content type of the image
     * @param apiProvider     API Provider
     * @param apiId           API/API Product UUID
     * @param tenantDomain    Tenant domain of the API
     * @throws APIManagementException If an error occurs while updating the thumbnail
     */
    public static void updateThumbnail(InputStream fileInputStream, String fileContentType, APIProvider apiProvider,
                                       String apiId, String tenantDomain) throws APIManagementException {
        ResourceFile apiImage = new ResourceFile(fileInputStream, fileContentType);
        apiProvider.setThumbnailToAPI(apiId, apiImage, tenantDomain);
    }

    /**
     * Add document DTO.
     *
     * @param documentDto  Document DTO
     * @param apiId        API UUID
     * @param organization Identifier of an Organization
     * @return Added documentation
     * @throws APIManagementException If an error occurs when retrieving API Identifier,
     *                                when checking whether the documentation exists and when adding the documentation
     */
    public static Documentation addDocumentationToAPI(DocumentDTO documentDto, String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(documentDto);
        String documentName = documentDto.getName();
        Pattern pattern = Pattern.compile(APIConstants.REGEX_ILLEGAL_CHARACTERS_FOR_API_METADATA);
        Matcher matcher = pattern.matcher(documentName);
        if (matcher.find()) {
            throw new APIManagementException("Document name cannot contain illegal characters  " +
                    "( " + APIConstants.REGEX_ILLEGAL_CHARACTERS_FOR_API_METADATA + " )",
                    ExceptionCodes.DOCUMENT_NAME_ILLEGAL_CHARACTERS);
        }
        if (documentDto.getType() == null) {
            String errorMessage = "Documentation type cannot be empty";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.PARAMETER_NOT_PROVIDED_FOR_DOCUMENTATION, errorMessage));
        }
        if (documentDto.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils
                .isBlank(documentDto.getOtherTypeName())) {
            //check otherTypeName for not null if doc type is OTHER
            String errorMessage = "otherTypeName cannot be empty if type is OTHER.";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.PARAMETER_NOT_PROVIDED_FOR_DOCUMENTATION, errorMessage));
        }
        String sourceUrl = documentDto.getSourceUrl();
        if (documentDto.getSourceType() == DocumentDTO.SourceTypeEnum.URL && (
                org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
            String errorMessage = "Invalid document sourceUrl Format";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.PARAMETER_NOT_PROVIDED_FOR_DOCUMENTATION, errorMessage));
        }

        if (apiProvider.isDocumentationExist(apiId, documentName, organization)) {
            throw new APIManagementException("Requested document '" + documentName + "' already exists",
                    ExceptionCodes.DOCUMENT_ALREADY_EXISTS);
        }
        if (documentDto.getType() == DocumentDTO.TypeEnum.OTHER && documentDto.getOtherTypeName() != null && apiProvider
                .isAnotherOverviewDocumentationExist(apiId, null, documentDto.getOtherTypeName(), organization)) {
            throw new APIManagementException("Requested other document type _overview already exists",
                    ExceptionCodes.DOCUMENT_ALREADY_EXISTS);
        }
        documentation = apiProvider.addDocumentation(apiId, documentation, organization);

        return documentation;
    }

    /**
     * Add documentation content of inline and markdown documents.
     *
     * @param documentation Documentation
     * @param apiProvider   API Provider
     * @param apiId         API/API Product UUID
     * @param documentId    Document ID
     * @param organization  Identifier of the organization
     * @param inlineContent Inline content string
     * @throws APIManagementException If an error occurs while adding the documentation content
     */
    public static void addDocumentationContent(Documentation documentation, APIProvider apiProvider, String apiId,
                                               String documentId, String organization, String inlineContent)
            throws APIManagementException {
        DocumentationContent content = new DocumentationContent();
        content.setSourceType(DocumentationContent.ContentSourceType.valueOf(documentation.getSourceType().toString()));
        content.setTextContent(inlineContent);
        apiProvider.addDocumentationContent(apiId, documentId, organization, content);
    }

    /**
     * Add documentation content of files.
     *
     * @param inputStream  Input Stream
     * @param mediaType    Media type of the document
     * @param filename     File name
     * @param apiProvider  API Provider
     * @param apiId        API/API Product UUID
     * @param documentId   Document ID
     * @param organization organization of the API
     * @throws APIManagementException If an error occurs while adding the documentation file
     */
    public static void addDocumentationContentForFile(InputStream inputStream, String mediaType, String filename,
                                                      APIProvider apiProvider, String apiId,
                                                      String documentId, String organization)
            throws APIManagementException {
        DocumentationContent content = new DocumentationContent();
        ResourceFile resourceFile = new ResourceFile(inputStream, mediaType);
        resourceFile.setName(filename);
        content.setResourceFile(resourceFile);
        content.setSourceType(DocumentationContent.ContentSourceType.FILE);
        apiProvider.addDocumentationContent(apiId, documentId, organization, content);
    }

    /**
     * Checks whether the list of tiers are valid given the all valid tiers.
     *
     * @param allTiers     All defined tiers
     * @param currentTiers tiers to check if they are a subset of defined tiers
     * @return null if there are no invalid tiers or returns the set of invalid tiers if there are any
     */
    public static List<String> getInvalidTierNames(Set<Tier> allTiers, List<String> currentTiers) {

        List<String> invalidTiers = new ArrayList<>();
        for (String tierName : currentTiers) {
            boolean isTierValid = false;
            for (Tier definedTier : allTiers) {
                if (tierName.equals(definedTier.getName())) {
                    isTierValid = true;
                    break;
                }
            }
            if (!isTierValid) {
                invalidTiers.add(tierName);
            }
        }
        return invalidTiers;
    }

    /**
     * Update an API Product.
     *
     * @param originalAPIProduct    Existing API Product
     * @param apiProductDtoToUpdate New API Product DTO to update
     * @param apiProvider           API Provider
     * @param username              Username
     * @throws APIManagementException If an error occurs while retrieving and updating an existing API Product
     * @throws FaultGatewaysException If an error occurs while updating an existing API Product
     */
    public static APIProduct updateApiProduct(APIProduct originalAPIProduct, APIProductDTO apiProductDtoToUpdate,
                                              APIProvider apiProvider, String username, String orgId)
            throws APIManagementException, FaultGatewaysException {

        List<String> apiSecurity = apiProductDtoToUpdate.getSecurityScheme();
        //validation for tiers
        List<String> tiersFromDTO = apiProductDtoToUpdate.getPolicies();
        // Remove the subscriptionless tier if other tiers are available.
        if (tiersFromDTO != null && tiersFromDTO.size() > 1
                && tiersFromDTO.contains(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS)) {
            tiersFromDTO.remove(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS);
            apiProductDtoToUpdate.setPolicies(tiersFromDTO);
        }
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (!APIUtil.isSubscriptionValidationDisablingAllowed(tenantDomain)) {
            if (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) || apiSecurity
                    .contains(APIConstants.API_SECURITY_API_KEY)) {
                if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                    throw new APIManagementException("No tier defined for the API Product",
                            ExceptionCodes.TIER_CANNOT_BE_NULL);
                }
            }
        } else {
            if (apiSecurity.contains(APIConstants.API_SECURITY_API_KEY)) {
                if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                    throw new APIManagementException("No tier defined for the API Product",
                            ExceptionCodes.TIER_CANNOT_BE_NULL);
                }
            } else if (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                // Internally set the default tier when no tiers are defined in order to support
                // subscription validation disabling for OAuth2 secured APIs
                if (tiersFromDTO != null && tiersFromDTO.isEmpty()) {
                    tiersFromDTO.add(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS);
                    apiProductDtoToUpdate.setPolicies(tiersFromDTO);
                }
            }
        }

        //check whether the added API Products's tiers are all valid
        Set<Tier> definedTiers = apiProvider.getTiers();
        List<String> invalidTiers = PublisherCommonUtils.getInvalidTierNames(definedTiers, tiersFromDTO);
        if (!invalidTiers.isEmpty()) {
            String errorMessage = "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.TIER_NAME_INVALID_WITH_TIER_INFO,
                            Arrays.toString(invalidTiers.toArray())));
        }
        if (apiProductDtoToUpdate.getAdditionalProperties() != null) {
            String errorMessage = PublisherCommonUtils.validateAdditionalProperties(
                    apiProductDtoToUpdate.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES_WITH_ERROR,
                                originalAPIProduct.getId().getName(), originalAPIProduct.getId().getVersion(),
                                errorMessage));
            }
        }

        APIProduct product = APIMappingUtil.fromDTOtoAPIProduct(apiProductDtoToUpdate, username);
        validateSubscriptionAvailabilityForProduct(originalAPIProduct, product);
        product.setState(originalAPIProduct.getState());
        //We do not allow to modify provider,name,version  and uuid. Set the origial value
        APIProductIdentifier productIdentifier = originalAPIProduct.getId();
        product.setID(productIdentifier);
        product.setUuid(originalAPIProduct.getUuid());
        product.setOrganization(orgId);
        product.setThumbnailUrl(originalAPIProduct.getThumbnailUrl());

        Map<API, List<APIProductResource>> apiToProductResourceMapping = apiProvider.updateAPIProduct(product);
        apiProvider.updateAPIProductSwagger(originalAPIProduct.getUuid(), apiToProductResourceMapping, product, orgId);

        return apiProvider.getAPIProduct(productIdentifier);
    }

    /**
     * Add API Product with the generated swagger from the DTO.
     *
     * @param apiProductDTO API Product DTO
     * @param username      Username
     * @param organization  Identifier of the organization
     * @return Created API Product object
     * @throws APIManagementException Error while creating the API Product
     * @throws FaultGatewaysException Error while adding the API Product to gateway
     */
    public static APIProduct addAPIProductWithGeneratedSwaggerDefinition(APIProductDTO apiProductDTO, String username,
                                                                         String organization)
            throws APIManagementException, FaultGatewaysException {

        username = StringUtils.isEmpty(username) ? RestApiCommonUtil.getLoggedInUsername() : username;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // if not add product
        String provider = apiProductDTO.getProvider();
        String context = apiProductDTO.getContext();

        // Validate the API context
        APIUtil.validateAPIContext(context, apiProductDTO.getName());

        if (!StringUtils.isBlank(provider) && !provider.equals(username)) {
            if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
                if (log.isDebugEnabled()) {
                    log.debug("User " + username + " does not have admin permission ("
                            + APIConstants.Permissions.APIM_ADMIN + ") hence provider (" + provider
                            + ") overridden with current user (" + username + ")");
                }
                provider = username;
            }
        } else {
            // Set username in case provider is null or empty
            provider = username;
        }
        // validate character length
        APIUtil.validateCharacterLengthOfAPIParams(apiProductDTO.getName(), apiProductDTO.getContext(),
                provider);

        List<String> tiersFromDTO = apiProductDTO.getPolicies();
        Set<Tier> definedTiers = apiProvider.getTiers();
        List<String> invalidTiers = PublisherCommonUtils.getInvalidTierNames(definedTiers, tiersFromDTO);
        if (!invalidTiers.isEmpty()) {
            String errorMessage = "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.TIER_NAME_INVALID_WITH_TIER_INFO,
                            Arrays.toString(invalidTiers.toArray())));
        }
        if (apiProductDTO.getAdditionalProperties() != null) {
            String errorMessage = PublisherCommonUtils
                    .validateAdditionalProperties(apiProductDTO.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES_WITH_ERROR,
                                apiProductDTO.getName(), apiProductDTO.getVersion(), errorMessage));
            }
        }
        if (apiProductDTO.getVisibility() == null) {
            //set the default visibility to PUBLIC
            apiProductDTO.setVisibility(APIProductDTO.VisibilityEnum.PUBLIC);
        }

        if (apiProductDTO.getAuthorizationHeader() == null) {
            apiProductDTO.setAuthorizationHeader(
                    APIUtil.getOAuthConfigurationFromAPIMConfig(APIConstants.AUTHORIZATION_HEADER));
        }
        if (apiProductDTO.getAuthorizationHeader() == null) {
            apiProductDTO.setAuthorizationHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT);
        }

        if (apiProductDTO.getApiKeyHeader() == null) {
            apiProductDTO.setApiKeyHeader(APIConstants.API_KEY_HEADER_DEFAULT);
        }

        //isDefaultVersion is true for a new API Product.
        apiProductDTO.setIsDefaultVersion(true);
        checkDuplicateContext(apiProvider, apiProductDTO, username, organization);

        // Set default gatewayVendor
        if (apiProductDTO.getGatewayVendor() == null) {
            apiProductDTO.setGatewayVendor(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
        }

        APIProduct productToBeAdded = APIMappingUtil.fromDTOtoAPIProduct(apiProductDTO, provider);
        productToBeAdded.setOrganization(organization);
        if (!APIConstants.PROTOTYPED.equals(productToBeAdded.getState())) {
            productToBeAdded.setState(APIConstants.CREATED);
        }

        APIProductIdentifier createdAPIProductIdentifier = productToBeAdded.getId();
        List<APIProductResource> resources = productToBeAdded.getProductResources();

        for (APIProductResource apiProductResource : resources) {
            API api;
            String apiUUID;
            if (apiProductResource.getProductIdentifier() != null) {
                APIIdentifier productAPIIdentifier = apiProductResource.getApiIdentifier();
                String emailReplacedAPIProviderName = APIUtil
                        .replaceEmailDomain(productAPIIdentifier.getProviderName());
                APIIdentifier emailReplacedAPIIdentifier = new APIIdentifier(emailReplacedAPIProviderName,
                        productAPIIdentifier.getApiName(), productAPIIdentifier.getVersion());
                apiUUID = apiProvider
                        .getUUIDFromIdentifier(emailReplacedAPIIdentifier, productToBeAdded.getOrganization());
                api = apiProvider.getAPIbyUUID(apiUUID, productToBeAdded.getOrganization());
            } else {
                apiUUID = apiProductResource.getApiId();
                api = apiProvider.getAPIbyUUID(apiUUID, productToBeAdded.getOrganization());
                // if API does not exist, getLightweightAPIByUUID() method throws exception.
            }
            validateApiLifeCycleForApiProducts(api);
        }

        Map<API, List<APIProductResource>> apiToProductResourceMapping = apiProvider
                .addAPIProductWithoutPublishingToGateway(productToBeAdded);
        APIProduct createdProduct = apiProvider.getAPIProduct(createdAPIProductIdentifier);
        apiProvider.addAPIProductSwagger(createdProduct.getUuid(), apiToProductResourceMapping, createdProduct,
                organization);

        createdProduct = apiProvider.getAPIProduct(createdAPIProductIdentifier);
        return createdProduct;
    }

    /**
     * Validate subscription availability when cross tenant subscription is disabled.
     *
     * @param originalAPI Original API
     * @param apiToUpdate API to be updated
     * @throws APIManagementException If an error occurs while validating availability
     */
    public static void validateSubscriptionAvailability(API originalAPI, API apiToUpdate)
            throws APIManagementException {
        if (originalAPI.getSubscriptionAvailability() != null && apiToUpdate.getSubscriptionAvailability() != null
                && !APIUtil.isCrossTenantSubscriptionsEnabled()) {
            if (!originalAPI.getSubscriptionAvailability()
                    .equalsIgnoreCase(apiToUpdate.getSubscriptionAvailability())) {
                if (!APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT
                        .equalsIgnoreCase(apiToUpdate.getSubscriptionAvailability())) {
                    throw new APIManagementException(
                            ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WHILE_UPDATING_API,
                            "Cannot set Subscription Availability to "
                                    + apiToUpdate.getSubscriptionAvailability().toUpperCase()
                                    + " when Cross Tenant Subscription is disabled"));
                }
            }
        }
    }

    /**
     * Validate subscription availability when cross tenant subscription is disabled.
     *
     * @param originalProduct Original API Product
     * @param productToUpdate API Product to be updated
     * @throws APIManagementException If an error occurs while validating availability
     */
    public static void validateSubscriptionAvailabilityForProduct(APIProduct originalProduct,
                                                                  APIProduct productToUpdate)
            throws APIManagementException {
        if (originalProduct.getSubscriptionAvailability() != null
                && productToUpdate.getSubscriptionAvailability() != null
                && !APIUtil.isCrossTenantSubscriptionsEnabled()) {
            if (!originalProduct.getSubscriptionAvailability()
                    .equalsIgnoreCase(productToUpdate.getSubscriptionAvailability())) {
                if (!APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT
                        .equalsIgnoreCase(productToUpdate.getSubscriptionAvailability())) {
                    throw new APIManagementException(
                            ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WHILE_UPDATING_API,
                                    "Cannot set Subscription Availability to "
                                            + productToUpdate.getSubscriptionAvailability().toUpperCase()
                                            + " when Cross Tenant Subscription is disabled"));
                }
            }
        }
    }

    private static void validateApiLifeCycleForApiProducts(API api) throws APIManagementException {
        String status = api.getStatus();

        if (APIConstants.BLOCKED.equals(status) ||
                APIConstants.PROTOTYPED.equals(status) ||
                APIConstants.DEPRECATED.equals(status) ||
                APIConstants.RETIRED.equals(status)) {
            throw new APIManagementException("Cannot create API Product using API with following status: " + status,
                    ExceptionCodes.from(ExceptionCodes.API_PRODUCT_WITH_UNSUPPORTED_LIFECYCLE_API, status));
        }
    }

    private static void checkDuplicateContext(APIProvider apiProvider, APIProductDTO apiProductDTO, String username,
                                              String organization)
            throws APIManagementException {

        String context = apiProductDTO.getContext();
        //Remove the /{version} from the context.
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }

        //Make sure context starts with "/". ex: /pizzaProduct
        context = context.startsWith("/") ? context : ("/" + context);

        //Create tenant aware context for API
        if (context.startsWith("/t/" + organization)) {
            context = context.replace("/t/" + organization, "");
        }
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(organization) &&
                !context.contains("/t/" + organization)) {
            context = "/t/" + organization + context;
        }

        // Check whether the context already exists for migrated API products which were created with
        // version appended context
        String contextWithVersion = context;
        if (contextWithVersion.contains("/" + RestApiConstants.API_VERSION_PARAM)) {
            contextWithVersion = contextWithVersion.replace(RestApiConstants.API_VERSION_PARAM,
                    apiProductDTO.getVersion());
        } else {
            contextWithVersion = contextWithVersion + "/" + apiProductDTO.getVersion();
        }

        //Get all existing versions of  api product been adding
        List<String> apiVersions = apiProvider.getApiVersionsMatchingApiNameAndOrganization(apiProductDTO.getName(),
                username, organization);
        if (!apiVersions.isEmpty()) {
            //If any previous version exists
            for (String version : apiVersions) {
                if (version.equalsIgnoreCase(apiProductDTO.getVersion())) {
                    //If version already exists
                    if (apiProvider.isDuplicateContextTemplateMatchingOrganization(context, organization)) {
                        throw new APIManagementException(
                                "Error occurred while adding the API Product. A duplicate API context already exists "
                                        + "for " + context + " in the organization : " + organization,
                                ExceptionCodes.API_ALREADY_EXISTS);
                    } else {
                        throw new APIManagementException(
                                "Error occurred while adding API Product. API Product with name "
                                        + apiProductDTO.getName() + " already exists with different context " + context
                                        + " in the organization" + " : " + organization,
                                ExceptionCodes.API_ALREADY_EXISTS);
                    }
                }
            }
        } else {
            //If no any previous version exists
            if (apiProvider.isContextExistForAPIProducts(context, contextWithVersion, organization)) {
                throw new APIManagementException(
                        "Error occurred while adding the API Product. A duplicate API context already exists for "
                                + context + " in the organization" + " : " + organization, ExceptionCodes
                        .from(ExceptionCodes.API_CONTEXT_ALREADY_EXISTS, context));
            }
        }
    }

    public static boolean isStreamingAPI(APIDTO apidto) {

        return APIDTO.TypeEnum.WS.equals(apidto.getType()) || APIDTO.TypeEnum.SSE.equals(apidto.getType()) ||
                APIDTO.TypeEnum.WEBSUB.equals(apidto.getType()) || APIDTO.TypeEnum.ASYNC.equals(apidto.getType());
    }

    public static boolean isThirdPartyAsyncAPI(APIDTO apidto) {
        return APIDTO.TypeEnum.ASYNC.equals(apidto.getType()) && apidto.getAdvertiseInfo() != null &&
                apidto.getAdvertiseInfo().isAdvertised();
    }

    /**
     * Add WSDL file of an API.
     *
     * @param fileContentType Content type of the file
     * @param fileInputStream Input Stream
     * @param api             API to which the WSDL belongs to
     * @param apiProvider     API Provider
     * @param tenantDomain    Tenant domain of the API
     * @throws APIManagementException If an error occurs while adding the WSDL resource
     */
    public static void addWsdl(String fileContentType, InputStream fileInputStream, API api, APIProvider apiProvider,
                               String tenantDomain) throws APIManagementException {
        ResourceFile wsdlResource;
        if (APIConstants.APPLICATION_ZIP.equals(fileContentType) || APIConstants.APPLICATION_X_ZIP_COMPRESSED
                .equals(fileContentType)) {
            wsdlResource = new ResourceFile(fileInputStream, APIConstants.APPLICATION_ZIP);
        } else {
            wsdlResource = new ResourceFile(fileInputStream, fileContentType);
        }
        api.setWsdlResource(wsdlResource);
        apiProvider.addWSDLResource(api.getUuid(), wsdlResource, null, tenantDomain);
    }

    /**
     * Set the generated SOAP to REST sequences from the swagger file to the API and update it.
     *
     * @param swaggerContent Swagger content
     * @param api            API to update
     * @param apiProvider    API Provider
     * @param organization   Organization Identifier
     * @return Updated API Object
     * @throws APIManagementException If an error occurs while generating the sequences or updating the API
     * @throws FaultGatewaysException If an error occurs while updating the API
     */
    public static API updateAPIBySettingGenerateSequencesFromSwagger(String swaggerContent, API api,
                                                                     APIProvider apiProvider, String organization)
            throws APIManagementException, FaultGatewaysException {
        List<SOAPToRestSequence> list = SequenceGenerator.generateSequencesFromSwagger(swaggerContent);
        API updatedAPI = apiProvider.getAPIbyUUID(api.getUuid(), organization);
        updatedAPI.setSoapToRestSequences(list);
        Map<String, String> complianceResult = checkGovernanceComplianceSync(updatedAPI.getUuid(),
                APIMGovernableState.API_UPDATE, ArtifactType.API, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        PublisherCommonUtils.checkGovernanceComplianceAsync(updatedAPI.getUuid(), APIMGovernableState.API_UPDATE,
                ArtifactType.API, organization);
        return apiProvider.updateAPI(updatedAPI, api);
    }

    /**
     * Change the lifecycle state of an API or API Product identified by UUID
     *
     * @param action         LC state change action
     * @param apiTypeWrapper API Type Wrapper (API or API Product)
     * @param lcChecklist    LC state change check list
     * @param organization   Organization of logged-in user
     * @return APIStateChangeResponse
     * @throws APIManagementException Exception if there is an error when changing the LC state of API or API Product
     */
    public static APIStateChangeResponse changeApiOrApiProductLifecycle(String action, ApiTypeWrapper apiTypeWrapper,
                                                                        String lcChecklist, String organization)
            throws APIManagementException {

        String[] checkListItems = lcChecklist != null ? lcChecklist.split(APIConstants.DELEM_COMMA) : new String[0];
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        Map<String, Object> apiLCData = apiProvider.getAPILifeCycleData(apiTypeWrapper.getUuid(), organization);
        if (action.equals(PUBLISH) || action.equals(REPUBLISH)) {
            Map<String, String> complianceResult = checkGovernanceComplianceSync(apiTypeWrapper.getUuid(),
                    APIMGovernableState.API_PUBLISH, ArtifactType.API, organization, null, null);
            if (!complianceResult.isEmpty()
                    && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                    && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
                throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
            }
            PublisherCommonUtils.checkGovernanceComplianceAsync(apiTypeWrapper.getUuid(),
                    APIMGovernableState.API_PUBLISH, ArtifactType.API, organization);
        }
        String[] nextAllowedStates = (String[]) apiLCData.get(APIConstants.LC_NEXT_STATES);
        if (!ArrayUtils.contains(nextAllowedStates, action)) {
            String errorMessage = "Action '" + action + "' is not allowed. Allowed actions are "
                    + Arrays.toString(nextAllowedStates);
            throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes
                    .UNSUPPORTED_AND_ALLOWED_LIFECYCLE_ACTIONS, action, Arrays.toString(nextAllowedStates)));
        }

        //check and set lifecycle check list items including "Deprecate Old Versions" and "Require Re-Subscription".
        Map<String, Boolean> lcMap = new HashMap<>();
        for (String checkListItem : checkListItems) {
            String[] attributeValPair = checkListItem.split(APIConstants.DELEM_COLON);
            if (attributeValPair.length == 2) {
                String checkListItemName = attributeValPair[0].trim();
                boolean checkListItemValue = Boolean.parseBoolean(attributeValPair[1].trim());
                lcMap.put(checkListItemName, checkListItemValue);
            }
        }

        return apiProvider.changeLifeCycleStatus(organization, apiTypeWrapper, action, lcMap);
    }

    /**
     * Retrieve lifecycle history of API or API Product by Identifier
     *
     * @param uuid Unique UUID of API or API Product
     * @return LifecycleHistoryDTO object
     * @throws APIManagementException exception if there is an error when retrieving the LC history
     */
    public static LifecycleHistoryDTO getLifecycleHistoryDTO(String uuid, APIProvider apiProvider)
            throws APIManagementException {

        List<LifeCycleEvent> lifeCycleEvents = apiProvider.getLifeCycleEvents(uuid);
        return APIMappingUtil.fromLifecycleHistoryModelToDTO(lifeCycleEvents);
    }

    /**
     * Get lifecycle state information of API or API Product
     *
     * @param identifier   Unique identifier of API or API Product
     * @param organization Organization of logged-in user
     * @return LifecycleStateDTO object
     * @throws APIManagementException if there is en error while retrieving the lifecycle state information
     */
    public static LifecycleStateDTO getLifecycleStateInformation(Identifier identifier, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Map<String, Object> apiLCData = apiProvider.getAPILifeCycleData(identifier.getUUID(), organization);
        String apiType;
        if (identifier instanceof APIProductIdentifier) {
            apiType = APIConstants.API_PRODUCT;
        } else {
            apiType = APIConstants.API_IDENTIFIER_TYPE;
        }

        if (apiLCData == null) {
            throw new APIManagementException("Error while getting lifecycle state for " + apiType + " with ID "
                    + identifier, ExceptionCodes.from(ExceptionCodes.LIFECYCLE_STATE_INFORMATION_NOT_FOUND, apiType,
                    identifier.getUUID()));
        } else {
            boolean apiOlderVersionExist = false;
            // check whether other versions of the current API exists
            APIVersionStringComparator comparator = new APIVersionStringComparator();
            Set<String> versions =
                    apiProvider.getAPIVersions(APIUtil.replaceEmailDomain(identifier.getProviderName()),
                            identifier.getName(), organization);

            for (String tempVersion : versions) {
                if (comparator.compare(tempVersion, identifier.getVersion()) < 0) {
                    apiOlderVersionExist = true;
                    break;
                }
            }
            return APIMappingUtil.fromLifecycleModelToDTO(apiLCData, apiOlderVersionExist, apiType);
        }
    }

    /**
     * Get All endpoints of an API.
     *
     * @param uuid         Unique identifier of API
     * @param apiProvider  API Provider
     * @param organization Organization of logged-in user
     * @return APIEndpointListDTO object
     * @throws APIManagementException if there is en error while getting the API Endpoints' information
     */
    public static APIEndpointListDTO getApiEndpoints(String uuid, APIProvider apiProvider, String organization)
            throws APIManagementException {

        List<APIEndpointInfo> apiEndpointsList = apiProvider.getAllAPIEndpointsByUUID(uuid, organization);
        return APIMappingUtil.fromAPIEndpointListToDTO(apiEndpointsList, organization, false);

    }

    /**
     * Retrieve the default production or sandbox endpoint from the endpoint config of an API.
     *
     * @param apiUUID        Unique identifier of API
     * @param endpointConfig Endpoint configuration
     * @param environment    Environment of the endpoint
     * @return APIEndpointInfo object
     */
    public static APIEndpointInfo getAPIEndpointFromEndpointConfig(String apiUUID, Map<String, Object> endpointConfig,
            String environment) {

        APIEndpointInfo apiEndpointInfo = new APIEndpointInfo();
        String endpointId;
        String endpointName;
        if (Objects.equals(environment, APIConstants.APIEndpoint.PRODUCTION)) {
            endpointId = APIConstants.APIEndpoint.DEFAULT_PROD_ENDPOINT_ID;
            endpointName = APIConstants.APIEndpoint.DEFAULT_PROD_ENDPOINT_NAME;
        } else {
            endpointId = APIConstants.APIEndpoint.DEFAULT_SANDBOX_ENDPOINT_ID;
            endpointName = APIConstants.APIEndpoint.DEFAULT_SANDBOX_ENDPOINT_NAME;
        }
        apiEndpointInfo.setId(endpointId);
        apiEndpointInfo.setName(endpointName);
        apiEndpointInfo.setDeploymentStage(environment);
        apiEndpointInfo.setEndpointConfig(endpointConfig);
        return apiEndpointInfo;
    }

    /**
     * Get API endpoint information using API UUID and endpoint UUID.
     *
     * @param apiUUID             Unique identifier of API
     * @param endpointUUID        Unique identifier of endpoint
     * @param apiProvider         API Provider
     * @param preserveCredentials Preserve credentials
     * @return APIEndpointDTO object
     * @throws APIManagementException if there is en error while retrieving the API Endpoint information
     */
    public static APIEndpointDTO getAPIEndpoint(String apiUUID, String endpointUUID, APIProvider apiProvider,
            boolean preserveCredentials) throws APIManagementException, JsonProcessingException {
        String organization = RestApiCommonUtil.getLoggedInUserTenantDomain();
        API api = apiProvider.getAPIbyUUID(apiUUID, organization);
        APIEndpointInfo apiEndpoint = apiProvider.getAPIEndpointByUUID(apiUUID, endpointUUID, organization);
        if (apiEndpoint == null) {
            String endpointConfig = api.getEndpointConfig();
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> endpointConfigMap = gson.fromJson(endpointConfig, type);

            if (APIConstants.APIEndpoint.DEFAULT_PROD_ENDPOINT_ID.equals(endpointUUID)) {
                apiEndpoint = getAPIEndpointFromEndpointConfig(apiUUID, endpointConfigMap,
                        APIConstants.APIEndpoint.PRODUCTION);
            } else if (APIConstants.APIEndpoint.DEFAULT_SANDBOX_ENDPOINT_ID.equals(endpointUUID)) {
                apiEndpoint = getAPIEndpointFromEndpointConfig(apiUUID, endpointConfigMap,
                        APIConstants.APIEndpoint.SANDBOX);
            } else {
                throw new APIManagementException(
                        "Error occurred while getting Endpoint of API " + apiUUID + " endpoint UUID " + endpointUUID,
                        ExceptionCodes.API_ENDPOINT_NOT_FOUND);
            }
        }
        return APIMappingUtil.fromAPIEndpointToDTO(apiEndpoint, organization, preserveCredentials);
    }

    /**
     * Update an API Endpoint.
     *
     * @param apiId          Unique identifier of API
     * @param endpointId     Unique identifier of API
     * @param apiEndpointDTO Payload of Endpoint
     * @param organization   Organization of logged-in user
     * @return APIEndpointDTO object
     * @throws APIManagementException if there is en error while updating an API endpoint
     */
    public static APIEndpointDTO updateAPIEndpoint(String apiId, String endpointId, APIEndpointDTO apiEndpointDTO,
            String organization, APIProvider apiProvider)
            throws APIManagementException, CryptoException, JsonProcessingException {
        String oldApiEndpointSecret = null;
        APIEndpointDTO oldEndpointDto = getAPIEndpoint(apiId, endpointId, apiProvider, true);
        Map oldEndpointConfig = (Map) oldEndpointDto.getEndpointConfig();
        if (oldEndpointConfig != null) {
            if ((oldEndpointConfig.containsKey(APIConstants.ENDPOINT_SECURITY))) {
                Map oldEndpointSecurity = (Map) oldEndpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                if (APIConstants.APIEndpoint.PRODUCTION.equals(apiEndpointDTO.getDeploymentStage())) {
                    if (oldEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                        Map oldProductionEndpointSecurity = (Map) oldEndpointSecurity.get(
                                APIConstants.ENDPOINT_SECURITY_PRODUCTION);
                        if (oldProductionEndpointSecurity.get(APIConstants.OAuthConstants.OAUTH_CLIENT_ID) != null &&
                                oldProductionEndpointSecurity.get(
                                        APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET) != null) {
                            oldApiEndpointSecret = oldProductionEndpointSecurity.get(
                                    APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                        } else if (oldProductionEndpointSecurity.get(
                                APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER) != null
                                && oldProductionEndpointSecurity.get(
                                        APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null
                                && oldProductionEndpointSecurity.get(
                                        APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER_TYPE) != null) {
                            oldApiEndpointSecret = oldProductionEndpointSecurity.get(
                                    APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                        }
                    }
                } else if (APIConstants.APIEndpoint.SANDBOX.equals(apiEndpointDTO.getDeploymentStage())) {
                    if (oldEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                        Map oldSandboxEndpointSecurity = (Map) oldEndpointSecurity.get(
                                APIConstants.ENDPOINT_SECURITY_SANDBOX);
                        if (oldSandboxEndpointSecurity.get(
                                APIConstants.OAuthConstants.OAUTH_CLIENT_ID) != null && oldSandboxEndpointSecurity.get(
                                APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET) != null) {
                            oldApiEndpointSecret = oldSandboxEndpointSecurity.get(
                                    APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                        } else if (oldSandboxEndpointSecurity.get(
                                APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER) != null
                                && oldSandboxEndpointSecurity.get(
                                        APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null
                                && oldSandboxEndpointSecurity.get(
                                        APIConstants.ENDPOINT_SECURITY_API_KEY_IDENTIFIER_TYPE) != null) {
                            oldApiEndpointSecret = oldSandboxEndpointSecurity.get(
                                    APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE).toString();
                        }
                    }
                }
            }
        }

        Map endpointConfig = (Map) apiEndpointDTO.getEndpointConfig();
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

        encryptEndpointSecurityApiKeyCredentials(apiEndpointDTO, cryptoUtil, oldApiEndpointSecret, endpointConfig);

        APIEndpointInfo apiEndpoint = APIMappingUtil.fromDTOtoAPIEndpoint(apiEndpointDTO, organization);
        if (apiEndpoint.getId() == null) {
            apiEndpoint.setId(endpointId);
        }

        // extract endpoint URL
        Object endpointURLObj;
        if (APIConstants.APIEndpoint.PRODUCTION.equals(apiEndpoint.getDeploymentStage())) {
            endpointURLObj = apiEndpoint.getEndpointConfig()
                    .get(APIConstants.APIEndpoint.ENDPOINT_CONFIG_PRODUCTION_ENDPOINTS);
        } else if (APIConstants.APIEndpoint.SANDBOX.equals(apiEndpoint.getDeploymentStage())) {
            endpointURLObj = apiEndpoint.getEndpointConfig()
                    .get(APIConstants.APIEndpoint.ENDPOINT_CONFIG_SANDBOX_ENDPOINTS);
        } else {
            throw new APIManagementException("Invalid deployment stage. Deployment stage should be either " +
                    "'PRODUCTION' or 'SANDBOX'", ExceptionCodes.ERROR_ADDING_API_ENDPOINT);
        }
        String endpointURL = ((LinkedHashMap) endpointURLObj).get("url").toString();

        // validate endpoint URL
        if (!APIUtil.validateEndpointURL(endpointURL)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL detected",
                    ExceptionCodes.API_ENDPOINT_URL_INVALID);
        }

        APIEndpointInfo apiEndpointUpdated = apiProvider.updateAPIEndpoint(apiId, apiEndpoint, organization);
        if (apiEndpointUpdated == null) {
            throw new APIManagementException("Error occurred while updating endpoint with UUID " + endpointId +
                    " under API " + apiId, ExceptionCodes.ERROR_UPDATING_API_ENDPOINT);
        }
        return APIMappingUtil.fromAPIEndpointToDTO(apiEndpointUpdated, organization, false);
    }

    /**
     * Add an API Endpoint using the provided endpoint details.
     *
     * @param apiId          Unique identifier of API
     * @param apiEndpointDTO Endpoint details
     * @param organization   Organization of logged-in user
     * @param apiProvider    API Provider
     * @return Unique identifier of the added API Endpoint
     * @throws APIManagementException if there is en error while adding endpoint
     * @throws CryptoException        if an error occurs while encrypting the endpoint security credentials
     */
    public static String addAPIEndpoint(String apiId, APIEndpointDTO apiEndpointDTO, String organization,
            APIProvider apiProvider) throws APIManagementException, CryptoException {

        // API Key and API Secret encryption
        Map endpointConfig = (Map) apiEndpointDTO.getEndpointConfig();
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        encryptEndpointSecurityApiKeyCredentials(apiEndpointDTO, cryptoUtil, StringUtils.EMPTY, endpointConfig);

        APIEndpointInfo apiEndpoint = APIMappingUtil.fromDTOtoAPIEndpoint(apiEndpointDTO, organization);

        // extract endpoint URL
        Object endpointURLObj;
        if (APIConstants.APIEndpoint.PRODUCTION.equals(apiEndpoint.getDeploymentStage())) {
             endpointURLObj = apiEndpoint.getEndpointConfig()
                    .get(APIConstants.APIEndpoint.ENDPOINT_CONFIG_PRODUCTION_ENDPOINTS);
        } else if (APIConstants.APIEndpoint.SANDBOX.equals(apiEndpoint.getDeploymentStage())) {
            endpointURLObj = apiEndpoint.getEndpointConfig()
                    .get(APIConstants.APIEndpoint.ENDPOINT_CONFIG_SANDBOX_ENDPOINTS);
        } else {
            throw new APIManagementException("Invalid deployment stage. Deployment stage should be either " +
                    "'PRODUCTION' or 'SANDBOX'", ExceptionCodes.ERROR_ADDING_API_ENDPOINT);
        }
        String endpointURL = ((LinkedHashMap) endpointURLObj).get("url").toString();

        // validate endpoint URL
        if (!APIUtil.validateEndpointURL(endpointURL)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL detected",
                    ExceptionCodes.API_ENDPOINT_URL_INVALID);
        }

        if (APIConstants.APIEndpoint.PRODUCTION.equals(
                apiEndpoint.getDeploymentStage()) || APIConstants.APIEndpoint.SANDBOX.equals(
                apiEndpoint.getDeploymentStage())) {
            String apiEndpointId = apiProvider.addAPIEndpoint(apiId, apiEndpoint, organization);
            if (apiEndpointId == null) {
                throw new APIManagementException("Error occurred while getting Endpoint of API " + apiId,
                        ExceptionCodes.ERROR_ADDING_API_ENDPOINT);
            }
            return apiEndpointId;
        } else {
            throw new APIManagementException(
                    "Invalid deployment stage. Deployment stage should be either " + "'PRODUCTION' or 'SANDBOX'",
                    ExceptionCodes.ERROR_ADDING_API_ENDPOINT);
        }
    }

    /**
     * @param validationResponse Response of a Async API definition validation call
     * @param isServiceAPI       Whether this is a service API
     * @param apiDto             API DTO
     * @param service            If this is a service API, the service entry should be passed here
     * @param organization       Organization of logged-in user
     * @param apiProvider        API Provider
     * @return
     * @throws APIManagementException If an error occurs while importing the Async API definition
     */
    public static API importAsyncAPIWithDefinition(APIDefinitionValidationResponse validationResponse,
                                                   Boolean isServiceAPI, APIDTO apiDto, ServiceEntry service,
                                                   String organization, APIProvider apiProvider)
            throws APIManagementException {
        String definitionToAdd = validationResponse.getJsonContent();
        String protocol = validationResponse.getProtocol();
        if (isServiceAPI) {
            apiDto.setType(PublisherCommonUtils.getAPIType(service.getDefinitionType(), protocol));
        }
        if (!APIConstants.WSO2_GATEWAY_ENVIRONMENT.equals(apiDto.getGatewayVendor())) {
            apiDto.getPolicies().add(APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED);
            apiDto.setAsyncTransportProtocols(AsyncApiParser.getTransportProtocolsForAsyncAPI(definitionToAdd));
        }
        API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDto, apiProvider,
                RestApiCommonUtil.getLoggedInUsername(), organization);
        if (isServiceAPI) {
            apiToAdd.setServiceInfo("key", service.getServiceKey());
            apiToAdd.setServiceInfo("md5", service.getMd5());
            if (!APIConstants.API_TYPE_WEBSUB.equals(protocol.toUpperCase())) {
                apiToAdd.setEndpointConfig(
                        PublisherCommonUtils.constructEndpointConfigForService(service.getServiceUrl(), protocol));
            }
        }
        apiToAdd.setAsyncApiDefinition(definitionToAdd);

        // load topics from AsyncAPI
        apiToAdd.setUriTemplates(new AsyncApiParser().getURITemplates(definitionToAdd,
                APIConstants.API_TYPE_WS.equals(apiToAdd.getType()) ||
                        !APIConstants.WSO2_GATEWAY_ENVIRONMENT.equals(apiToAdd.getGatewayVendor())));
        apiToAdd.setOrganization(organization);
        apiToAdd.setAsyncApiDefinition(definitionToAdd);

        Map<String, String> complianceResult = checkGovernanceComplianceSync(apiToAdd.getUuid(),
                APIMGovernableState.API_CREATE, ArtifactType.API, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        apiProvider.addAPI(apiToAdd);
        PublisherCommonUtils.checkGovernanceComplianceAsync(apiToAdd.getUuid(), APIMGovernableState.API_CREATE,
                ArtifactType.API, organization);
        return apiProvider.getAPIbyUUID(apiToAdd.getUuid(), organization);
    }

    /**
     * This method is used to validate the mandatory custom properties of an API
     *
     * @param customProperties custom properties of the API
     * @param apiDto           API DTO to validate
     * @return list of erroneous property names. returns an empty array if there are no errors.
     */
    public static List<String> validateMandatoryProperties(org.json.simple.JSONArray customProperties, APIDTO apiDto) {

        List<String> errorPropertyNames = new ArrayList<>();
        Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = apiDto.getAdditionalPropertiesMap();

        for (int i = 0; i < customProperties.size(); i++) {
            JSONObject property = (JSONObject) customProperties.get(i);
            String propertyName = (String) property.get(APIConstants.CustomPropertyAttributes.NAME);
            boolean isRequired = (boolean) property.get(APIConstants.CustomPropertyAttributes.REQUIRED);

            if (isRequired) {
                APIInfoAdditionalPropertiesMapDTO mapPropertyDisplay =
                        additionalPropertiesMap.get(propertyName + "__display");
                APIInfoAdditionalPropertiesMapDTO mapProperty = additionalPropertiesMap.get(propertyName);
                if (mapProperty == null && mapPropertyDisplay == null) {
                    errorPropertyNames.add(propertyName);
                    continue;
                }
                String propertyValue = "";
                String propertyValueDisplay = "";
                if (mapProperty != null) {
                    propertyValue = mapProperty.getValue();
                }
                if (mapPropertyDisplay != null) {
                    propertyValueDisplay = mapPropertyDisplay.getValue();
                }
                if ((propertyValue == null || propertyValue.isEmpty()) &&
                        (propertyValueDisplay == null || propertyValueDisplay.isEmpty())) {
                    errorPropertyNames.add(propertyName);
                }
            }
        }
        return errorPropertyNames;
    }

    /**
     * This method is used to check governance compliance synchronously.
     *
     * @param artifactID             API ID
     * @param state                  API state
     * @param type                   API type
     * @param organization           Organization of the logged-in user
     * @param revisionId             Revision ID
     * @param artifactProjectContent Content of the artifact project
     * @return Map of compliance violations
     * @throws APIManagementException If an error occurs while checking governance compliance
     */
    public static Map<String, String> checkGovernanceComplianceSync(String artifactID, APIMGovernableState state,
                                                                    ArtifactType type, String organization,
                                                                    String revisionId, Map<RuleType,
            String> artifactProjectContent) throws APIManagementException {
        Map<String, String> responseMap = new HashMap<>(2);

        try {
            if (apimGovernanceService.isPoliciesWithBlockingActionExist(artifactID, type, state, organization)) {
                if (log.isDebugEnabled()) {
                    log.debug("Blocking policies exist for the API. Evaluating compliance synchronously.");
                }
                ArtifactComplianceInfo artifactComplianceInfo = apimGovernanceService.evaluateComplianceSync(artifactID,
                        revisionId, type, state, artifactProjectContent, organization);
                if (artifactComplianceInfo.isBlockingNecessary()) {
                    responseMap.put(GOVERNANCE_COMPLIANCE_KEY, "false");
                    responseMap.put(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE,
                            buildBadRequestResponse(artifactComplianceInfo));
                    return responseMap;
                }
            }
        } catch (APIMGovernanceException e) {
            log.error("Error occurred while executing governance compliance validation for API " + artifactID, e);
        }
        return responseMap;
    }

    /**
     * Check governance compliance for the API artifact asynchronously.
     *
     * @param artifactID   API ID
     * @param state        API state
     * @param type         API type
     * @param organization Organization of the logged-in user
     */
    public static void checkGovernanceComplianceAsync(String artifactID, APIMGovernableState state, ArtifactType type,
                                                      String organization) {
        CompletableFuture.runAsync(() -> {
            try {
                apimGovernanceService.evaluateComplianceAsync(artifactID, type, state, organization);
            } catch (APIMGovernanceException e) {
                log.error("Error occurred while scheduling governance compliance validation for " + artifactID, e);
            }
        });
    }

    /**
     * Check governance compliance for the API artifact in a dry run mode.
     *
     * @param fileInputStream API artifact input stream
     * @param organization    Organization of the logged-in user
     * @return Map of compliance violations
     */
    public static String checkGovernanceComplianceDryRun(InputStream fileInputStream,
                                                         String organization) throws APIComplianceException {

        try {
            byte[] fileBytes = IOUtils.toByteArray(fileInputStream);

            ArtifactComplianceDryRunInfo dryRunResults = apimGovernanceService
                    .evaluateComplianceDryRunSync(ArtifactType.API, fileBytes, organization);
            return ArtifactComplianceDryRunInfo.toJson(dryRunResults);
        } catch (APIMGovernanceException e) {
            throw new APIComplianceException("Error occurred while executing governance compliance validation in dry " +
                    "run mode: " + e.getMessage(), ExceptionCodes.ERROR_WHILE_EXECUTING_COMPLIANCE_DRY_RUN);
        } catch (IOException e) {
            throw new APIComplianceException("Error occurred while reading the input stream: " + e.getMessage(),
                    ExceptionCodes.ERROR_WHILE_EXECUTING_COMPLIANCE_DRY_RUN);
        }
    }

    /**
     * Build the response for the compliance check failure.
     *
     * @param artifactComplianceInfo Compliance information of the artifact
     * @return JSON response with the compliance violations
     */

    public static String buildBadRequestResponse(ArtifactComplianceInfo artifactComplianceInfo) throws
            APIManagementException {
        List<RuleViolation> blockingViolations = artifactComplianceInfo.getBlockingRuleViolations();
        List<RuleViolation> nonBlockingViolations = artifactComplianceInfo.getNonBlockingViolations();

        Map<String, List<Map<String, String>>> violations = new HashMap<>();
        violations.put("blockingViolations", new ArrayList<>());
        violations.put("nonBlockingViolations", new ArrayList<>());

        for (RuleViolation violation : blockingViolations) {
            violations.get("blockingViolations").add(getViolationMapFromViolation(violation));
        }
        for (RuleViolation violation : nonBlockingViolations) {
            violations.get("nonBlockingViolations").add(getViolationMapFromViolation(violation));
        }

        // Convert violations list to JSON object
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonViolations;
        try {
            jsonViolations = objectMapper.writeValueAsString(violations);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error generating JSON response for governance compliance ", e);
        }
        return jsonViolations;
    }

    /**
     * Get a map of violation details from a RuleViolation object.
     *
     * @param violation RuleViolation object
     * @return Map of violation details
     */
    private static Map<String, String> getViolationMapFromViolation(RuleViolation violation) {
        Map<String, String> violationDetails = new HashMap<>();
        violationDetails.put("ruleName", violation.getRuleName());
        violationDetails.put("ruleType", violation.getRuleType().name());
        violationDetails.put("violatedPath", violation.getViolatedPath());
        violationDetails.put("severity", violation.getSeverity().name());
        violationDetails.put("message", violation.getRuleMessage());
        return violationDetails;
    }

    /**
     * Execute governance on label attach.
     *
     * @param labels       List of label Ids
     * @param artifactType Type of the artifact
     * @param artifactId   Id of the artifact
     * @param organization Organization of the logged-in user
     */
    public static void executeGovernanceOnLabelAttach(List<Label> labels, String artifactType, String artifactId,
                                                      String organization) {
        List<String> labelsIdList = new ArrayList<>();
        for (Label label : labels) {
            labelsIdList.add(label.getLabelId());
        }
        try {
            apimGovernanceService.evaluateComplianceOnLabelAttach(artifactId, ArtifactType.fromString(artifactType),
                    labelsIdList, organization);
        } catch (APIMGovernanceException e) {
            log.info("Error occurred while executing governance on attached labels for API " + artifactId, e);
        }
    }

    /**
     * Clear governance data on deletion of an artifact.
     *
     * @param artifactId   Id of the artifact
     * @param artifactType Type of the artifact
     * @param organization Organization of the logged-in user
     */
    public static void clearArtifactComplianceInfo(String artifactId, String artifactType, String organization) {
        try {
            apimGovernanceService.clearArtifactComplianceInfo(artifactId, ArtifactType.fromString(artifactType),
                    organization);
        } catch (APIMGovernanceException e) {
            log.info("Error occurred while deleting governance data on deletion of  " + ArtifactType.API +
                    " " + artifactId, e);
        }
    }

    /**
     * Get the list of rulesets applicable for the API linter.
     *
     * @param apiId        API ID
     * @param apiType      API Type
     * @param organization Organization of the logged-in user
     * @return List of rulesets in JSON format
     * @throws APIManagementException If an error occurs while getting the rulesets
     */
    public static List<String> getGovernanceRulesetsForLinter(String apiId, String apiType, String organization)
            throws APIManagementException {
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectMapper yamlMapper = new YAMLMapper();

        try {
            List<Ruleset> rulesets = new ArrayList<>();
            if (apiId != null) {
                rulesets = apimGovernanceService.getApplicableRulesetsForArtifact(apiId, ArtifactType.API,
                        RuleType.API_DEFINITION, RuleCategory.SPECTRAL, organization);
            } else if (apiType != null) {
                ExtendedArtifactType extendedArtifactType = getAPIExtendedArtifactType(apiType);
                rulesets = apimGovernanceService.getApplicableRulesetsByExtendedArtifactType(extendedArtifactType,
                        RuleType.API_DEFINITION, RuleCategory.SPECTRAL, organization);
            }

            List<String> rulesetJsonList = new ArrayList<>();
            for (Ruleset ruleset : rulesets) {
                RulesetContent rulesetContent = ruleset.getRulesetContent();
                if (rulesetContent == null || rulesetContent.getContentType() == null) {
                    continue;
                }

                byte[] contentBytes = rulesetContent.getContent();
                if (contentBytes == null) {
                    continue;
                }

                String contentStr = new String(contentBytes, StandardCharsets.UTF_8);
                try {
                    if (RulesetContent.ContentType.JSON.equals(rulesetContent.getContentType())) {
                        rulesetJsonList.add(contentStr);
                    } else if (RulesetContent.ContentType.YAML.equals(rulesetContent.getContentType())) {
                        Object yamlObject = yamlMapper.readValue(contentStr, Object.class);
                        rulesetJsonList.add(jsonMapper.writeValueAsString(yamlObject));
                    }
                } catch (Exception e) {
                    throw new APIManagementException("Error processing ruleset content", e);
                }
            }
            return rulesetJsonList;
        } catch (APIMGovernanceException e) {
            throw new APIManagementException("Error occurred while getting rulesets for API linter", e);
        }
    }

    /**
     * Get the extended artifact type for the given API type.
     *
     * @param apiType API Type
     * @return ExtendedArtifactType object
     */
    public static ExtendedArtifactType getAPIExtendedArtifactType(String apiType) {
        switch (apiType.toUpperCase(Locale.ENGLISH)) {
            case "REST":
            case "HTTP":
                return ExtendedArtifactType.REST_API;
            case "WS":
            case "SSE":
            case "WEBSUB":
            case "WEBHOOK":
            case "ASYNC":
                return ExtendedArtifactType.ASYNC_API;
            default:
                return null;
        }
    }
}
