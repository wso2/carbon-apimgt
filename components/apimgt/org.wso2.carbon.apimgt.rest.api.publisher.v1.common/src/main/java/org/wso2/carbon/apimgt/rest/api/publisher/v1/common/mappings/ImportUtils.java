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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.SoapToRestMediationDto;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.lifecycle.LifeCycle;
import org.wso2.carbon.apimgt.impl.importexport.lifecycle.LifeCycleTransition;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLQueryComplexityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class usesd to utility for Import API.
 */
public class ImportUtils {

    public static final String IN = "in";
    public static final String OUT = "out";
    private static final Log log = LogFactory.getLog(ImportUtils.class);
    private static final String SOAPTOREST = "SoapToRest";

    /**
     * This method imports an API.
     *
     * @param extractedFolderPath            Location of the extracted folder of the API
     * @param importedApiDTO                 API DTO of the importing API
     *                                       (This will not be null when importing dependent APIs with API Products)
     * @param preserveProvider               Decision to keep or replace the provider
     * @param overwrite                      Whether to update the API or not
     * @param tokenScopes                    Scopes of the token
     * @param dependentAPIParamsConfigObject Params configuration of an API (this will not be null if a dependent API
     *                                       of an
     *                                       API product wants to override the parameters)
     * @throws APIImportExportException If there is an error in importing an API
     * @@return Imported API
     */
    public static API importApi(String extractedFolderPath, APIDTO importedApiDTO, Boolean preserveProvider,
                                Boolean rotateRevision, Boolean overwrite, Boolean dependentAPIFromProduct,
                                String[] tokenScopes,
                                JsonObject dependentAPIParamsConfigObject) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        APIDefinitionValidationResponse validationResponse = null;
        String graphQLSchema = null;
        API importedApi = null;
        String currentStatus;
        String targetStatus;
        String lifecycleAction;
        GraphqlComplexityInfo graphqlComplexityInfo = null;
        int tenantId = 0;
        JsonArray deploymentInfoArray = null;
        JsonObject paramsConfigObject;

        try {
            if (importedApiDTO == null) {
                JsonElement jsonObject = retrieveValidatedDTOObject(extractedFolderPath, preserveProvider, userName,
                        ImportExportConstants.TYPE_API);
                importedApiDTO = new Gson().fromJson(jsonObject, APIDTO.class);
            }

            // If the provided dependent APIs params config is null, it means this happening when importing an API (not
            // because when importing a dependent API of an API Product). Hence, try to retrieve the definition from
            // the API folder path
            paramsConfigObject = (dependentAPIParamsConfigObject != null) ?
                    dependentAPIParamsConfigObject :
                    APIControllerUtil.resolveAPIControllerEnvParams(extractedFolderPath);
            // If above the params configurations are not null, then resolve those
            if (paramsConfigObject != null) {
                importedApiDTO = APIControllerUtil.injectEnvParamsToAPI(importedApiDTO, paramsConfigObject,
                        extractedFolderPath);
                if (!isAdvertiseOnlyAPI(importedApiDTO)) {
                    JsonElement deploymentsParam = paramsConfigObject
                            .get(ImportExportConstants.DEPLOYMENT_ENVIRONMENTS);
                    if (deploymentsParam != null && !deploymentsParam.isJsonNull()) {
                        deploymentInfoArray = deploymentsParam.getAsJsonArray();
                    }
                }
            }

            String apiType = importedApiDTO.getType().toString();

            APIProvider apiProvider = RestApiCommonUtil.getProvider(importedApiDTO.getProvider());

            // Validate swagger content except for streaming APIs
            if (!PublisherCommonUtils.isStreamingAPI(importedApiDTO)
                    && !APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(apiType)) {
                validationResponse = retrieveValidatedSwaggerDefinitionFromArchive(
                        extractedFolderPath);
            }
            // Validate the GraphQL schema
            if (APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(apiType)) {
                graphQLSchema = retrieveValidatedGraphqlSchemaFromArchive(extractedFolderPath);
            }
            // Validate the WSDL of SOAP/SOAPTOREST APIs
            if (APIConstants.API_TYPE_SOAP.equalsIgnoreCase(apiType) || APIConstants.API_TYPE_SOAPTOREST
                    .equalsIgnoreCase(apiType)) {
                validateWSDLFromArchive(extractedFolderPath, importedApiDTO);
            }
            // Validate the AsyncAPI definition of streaming APIs
            if (PublisherCommonUtils.isStreamingAPI(importedApiDTO)) {
                validationResponse = retrieveValidatedAsyncApiDefinitionFromArchive(extractedFolderPath);
            }

            String currentTenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(userName));

            // The status of the importing API should be stored separately to do the lifecycle change at the end
            targetStatus = importedApiDTO.getLifeCycleStatus();

            API targetApi = retrieveApiToOverwrite(importedApiDTO.getName(), importedApiDTO.getVersion(),
                    currentTenantDomain, apiProvider, Boolean.TRUE);

            if (isAdvertiseOnlyAPI(importedApiDTO)) {
                processAdvertiseOnlyPropertiesInDTO(importedApiDTO, tokenScopes);
            }

            // If the overwrite is set to true (which means an update), retrieve the existing API
            if (Boolean.TRUE.equals(overwrite) && targetApi != null) {
                log.info("Existing API found, attempting to update it...");
                currentStatus = targetApi.getStatus();
                // Set the status of imported API to current status of target API when updating
                importedApiDTO.setLifeCycleStatus(currentStatus);

                // If the set of operations are not set in the DTO, those should be set explicitly. Otherwise when
                // updating a "No resources found" error will be thrown. This is not a problem in the UI, since
                // when updating an API from the UI there is at least one resource (operation) inside the DTO.
                if (importedApiDTO.getOperations().isEmpty()) {
                    setOperationsToDTO(importedApiDTO, validationResponse);
                }
                importedApi = PublisherCommonUtils
                        .updateApi(targetApi, importedApiDTO, RestApiCommonUtil.getLoggedInUserProvider(), tokenScopes);
            } else {
                if (targetApi == null && Boolean.TRUE.equals(overwrite)) {
                    log.info("Cannot find : " + importedApiDTO.getName() + "-" + importedApiDTO.getVersion()
                            + ". Creating it.");
                }
                // Initialize to CREATED when import
                currentStatus = APIStatus.CREATED.toString();
                importedApiDTO.setLifeCycleStatus(currentStatus);
                importedApi = PublisherCommonUtils
                        .addAPIWithGeneratedSwaggerDefinition(importedApiDTO, ImportExportConstants.OAS_VERSION_3,
                                importedApiDTO.getProvider());
            }

            // Retrieving the life cycle action to do the lifecycle state change explicitly later
            lifecycleAction = getLifeCycleAction(currentTenantDomain, currentStatus, targetStatus, apiProvider);

            // Add/update swagger content except for streaming APIs and GraphQL APIs
            if (!PublisherCommonUtils.isStreamingAPI(importedApiDTO)
                    && !APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(apiType)) {
                // Add the validated swagger separately since the UI does the same procedure
                PublisherCommonUtils.updateSwagger(importedApi.getUuid(), validationResponse, false);
            }
            // Add the GraphQL schema
            if (APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(apiType)) {
                PublisherCommonUtils.addGraphQLSchema(importedApi, graphQLSchema, apiProvider);
                graphqlComplexityInfo = retrieveGraphqlComplexityInfoFromArchive(extractedFolderPath, graphQLSchema);
                if (graphqlComplexityInfo != null && graphqlComplexityInfo.getList().size() != 0) {
                    apiProvider.addOrUpdateComplexityDetails(importedApi.getId(), graphqlComplexityInfo);
                }
            }
            // Add/update Async API definition for streaming APIs
            if (PublisherCommonUtils.isStreamingAPI(importedApiDTO)) {
                // Add the validated Async API definition separately since the UI does the same procedure
                PublisherCommonUtils.updateAsyncAPIDefinition(importedApi.getUuid(), validationResponse);
            }

            tenantId = APIUtil.getTenantId(RestApiCommonUtil.getLoggedInUsername());
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);

            // Since Image, documents, sequences and WSDL are optional, exceptions are logged and ignored in
            // implementation
            ApiTypeWrapper apiTypeWrapperWithUpdatedApi = new ApiTypeWrapper(importedApi);
            addThumbnailImage(extractedFolderPath, apiTypeWrapperWithUpdatedApi, apiProvider);
            addDocumentation(extractedFolderPath, apiTypeWrapperWithUpdatedApi, apiProvider);
            addAPIWsdl(extractedFolderPath, importedApi, apiProvider, registry);
            addSOAPToREST(extractedFolderPath, importedApi, registry);

            if (!isAdvertiseOnlyAPI(importedApiDTO)) {
                addAPISequences(extractedFolderPath, importedApi, registry);
                addAPISpecificSequences(extractedFolderPath, registry, importedApi.getId());
                addEndpointCertificates(extractedFolderPath, importedApi, apiProvider, tenantId);

                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Importing client certificates.");
                }
                addClientCertificates(extractedFolderPath, apiProvider, preserveProvider,
                        importedApi.getId().getProviderName());
            }

            // Change API lifecycle if state transition is required
            if (StringUtils.isNotEmpty(lifecycleAction)) {
                apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
                log.info("Changing lifecycle from " + currentStatus + " to " + targetStatus);
                if (StringUtils.equals(lifecycleAction, APIConstants.LC_PUBLISH_LC_STATE)) {
                    apiProvider.changeAPILCCheckListItems(importedApi.getId(),
                            ImportExportConstants.REFER_REQUIRE_RE_SUBSCRIPTION_CHECK_ITEM, true);
                }
                apiProvider.changeLifeCycleStatus(importedApi.getId(), lifecycleAction);
            }
            importedApi.setStatus(targetStatus);
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            if (deploymentInfoArray == null && !isAdvertiseOnlyAPI(importedApiDTO)) {
                //If the params have not overwritten the deployment environments, yaml file will be read
                deploymentInfoArray = retrieveDeploymentLabelsFromArchive(extractedFolderPath, dependentAPIFromProduct);
            }
            List<APIRevisionDeployment> apiRevisionDeployments = getValidatedDeploymentsList(deploymentInfoArray,
                    tenantDomain, apiProvider);
            if (apiRevisionDeployments.size() > 0) {
                String importedAPIUuid = importedApi.getUuid();
                String revisionId;
                APIRevision apiRevision = new APIRevision();
                apiRevision.setApiUUID(importedAPIUuid);
                apiRevision.setDescription("Revision created after importing the API");

                try {
                    revisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);
                    if (log.isDebugEnabled()) {
                        log.debug("A new revision has been created for API " + importedApi.getId().getApiName() + "_"
                                + importedApi.getId().getVersion());
                    }
                } catch (APIManagementException e) {
                    //if the revision count is more than 5, addAPIRevision will throw an exception. If rotateRevision
                    //enabled, earliest revision will be deleted before creating a revision again
                    if (e.getErrorHandler().getErrorCode() ==
                            ExceptionCodes.from(ExceptionCodes.MAXIMUM_REVISIONS_REACHED).getErrorCode() &&
                            rotateRevision) {
                        String earliestRevisionUuid = apiProvider.getEarliestRevisionUUID(importedAPIUuid);
                        List<APIRevisionDeployment> deploymentsList =
                                apiProvider.getAPIRevisionDeploymentList(earliestRevisionUuid);
                        //if the earliest revision is already deployed in gateway environments, it will be undeployed
                        //before deleting
                        apiProvider
                                .undeployAPIRevisionDeployment(importedAPIUuid, earliestRevisionUuid, deploymentsList);
                        apiProvider.deleteAPIRevision(importedAPIUuid, earliestRevisionUuid, tenantDomain);
                        revisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);
                        if (log.isDebugEnabled()) {
                            log.debug("Revision ID: " + earliestRevisionUuid + " has been undeployed from " +
                                    deploymentsList.size() + " gateway environments and created a new revision ID: " +
                                    revisionId + " for API " + importedApi.getId().getApiName() + "_" +
                                    importedApi.getId().getVersion());
                        }
                    } else {
                        throw new APIManagementException("Error occurred while creating a new revision for the API: " +
                                importedApi.getId().getApiName(), e);
                    }
                }

                //Once the new revision successfully created, artifacts will be deployed in mentioned gateway
                //environments
                apiProvider.deployAPIRevision(importedAPIUuid, revisionId, apiRevisionDeployments);
                if (log.isDebugEnabled()) {
                    log.debug("API: " + importedApi.getId().getApiName() + "_" + importedApi.getId().getVersion() +
                            " was deployed in " + apiRevisionDeployments.size() + " gateway environments.");
                }
            } else {
                log.info("Valid deployment environments were not found for the imported artifact. Only working copy " +
                        "was updated and not deployed in any of the gateway environments.");
            }
            return importedApi;
        } catch (CryptoException | IOException e) {
            throw new APIManagementException(
                    "Error while reading API meta information from path: " + extractedFolderPath, e,
                    ExceptionCodes.ERROR_READING_META_DATA);
        } catch (FaultGatewaysException e) {
            throw new APIManagementException("Error while updating API: " + importedApi.getId().getApiName(), e);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while getting governance registry for tenant: " + tenantId, e);
        } catch (APIMgtAuthorizationFailedException e) {
            throw new APIManagementException("Please enable preserveProvider property for cross tenant API Import.", e,
                    ExceptionCodes.TENANT_MISMATCH);
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing the endpoint configuration of the API",
                    ExceptionCodes.JSON_PARSE_ERROR);
        } catch (APIManagementException e) {
            String errorMessage = "Error while importing API: ";
            if (importedApi != null) {
                errorMessage +=
                        importedApi.getId().getApiName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                                + importedApi.getId().getVersion();
            }
            throw new APIManagementException(errorMessage + StringUtils.SPACE + e.getMessage(), e);
        }
    }

    /**
     * Check whether an advertise only API
     *
     * @param importedApiDTO API DTO to import
     */
    public static boolean isAdvertiseOnlyAPI(APIDTO importedApiDTO) {
        return importedApiDTO.getAdvertiseInfo() != null && importedApiDTO.getAdvertiseInfo().isAdvertised();
    }
    
    /**
     * Process the properties specific to advertise only APIs
     *
     * @param importedApiDTO               API DTO to import
     * @param tokenScopes Scopes of the token
     */
    private static void processAdvertiseOnlyPropertiesInDTO(APIDTO importedApiDTO, String[] tokenScopes) {
        // Only the users who has admin privileges (apim:admin scope) are allowed to set the original devportal URL.
        // Otherwise, someone can set a malicious URL here.
        if (!Arrays.asList(tokenScopes).contains(RestApiConstants.ADMIN_SCOPE)) {
            log.debug("Since the user does not have the required scope: " + RestApiConstants.ADMIN_SCOPE
                    + ". Original DevPortal URL (redirect URL):" + importedApiDTO.getAdvertiseInfo()
                    .getOriginalDevPortalUrl() + " of " + importedApiDTO.getName() + "-" + importedApiDTO.getVersion()
                    + " will be removed.");
            importedApiDTO.getAdvertiseInfo().setOriginalDevPortalUrl(null);
        }
    }

    /**
     * This method is used to validate the Gateway environments from the deployment environments file. Gateway
     * environments will be validated with a set of all the labels and environments of the tenant domain. If
     * environment is not found in this set, it will be skipped with an error message in the console. This method is
     * common to both APIs and API Products
     *
     * @param deploymentInfoArray Deployment environment array found in the import artifact
     * @param tenantDomain        Tenant domain
     * @param apiProvider         Provider of the API/ API Product
     * @return a list of API/API Product revision deployments ready to be deployed.
     * @throws APIManagementException If an error occurs when validating the deployments list
     */
    private static List<APIRevisionDeployment> getValidatedDeploymentsList(JsonArray deploymentInfoArray,
                                                                           String tenantDomain, APIProvider apiProvider)
            throws APIManagementException {

        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        if (deploymentInfoArray != null && deploymentInfoArray.size() > 0) {
            Set<String> gatewayEnvironmentsSet = APIUtil.getEnvironments().keySet();

            for (int i = 0; i < deploymentInfoArray.size(); i++) {
                JsonObject deploymentJson = deploymentInfoArray.get(i).getAsJsonObject();
                JsonElement deploymentNameElement = deploymentJson.get(ImportExportConstants.DEPLOYMENT_NAME);
                if (deploymentNameElement != null) {
                    String deploymentName = deploymentNameElement.getAsString();
                    if (gatewayEnvironmentsSet.contains(deploymentName)) {
                        JsonElement displayOnDevportalElement =
                                deploymentJson.get(ImportExportConstants.DISPLAY_ON_DEVPORTAL_OPTION);
                        boolean displayOnDevportal =
                                displayOnDevportalElement == null || displayOnDevportalElement.getAsBoolean();
                        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                        apiRevisionDeployment.setDeployment(deploymentName);
                        apiRevisionDeployment.setDisplayOnDevportal(displayOnDevportal);
                        apiRevisionDeployments.add(apiRevisionDeployment);
                    } else {
                        log.error("Label " + deploymentName + " is not a defined gateway environment. Hence " +
                                "skipped without deployment");
                    }
                }

            }
        }
        return apiRevisionDeployments;
    }

    /**
     * This method sets the operations which were retrieved from the swagger definition to the API DTO.
     *
     * @param apiDto   API DTO
     * @param response API Validation Response
     * @throws APIManagementException If an error occurs when retrieving the URI templates
     */
    private static void setOperationsToDTO(APIDTO apiDto, APIDefinitionValidationResponse response)
            throws APIManagementException {

        List<URITemplate> uriTemplates = new ArrayList<>();
        uriTemplates.addAll(response.getParser().getURITemplates(response.getJsonContent()));
        List<APIOperationsDTO> apiOperationsDtos = APIMappingUtil.fromURITemplateListToOprationList(uriTemplates);
        apiDto.setOperations(apiOperationsDtos);
    }

    /**
     * This method retrieves an API to overwrite in the current tenant domain.
     *
     * @param apiName             API Name
     * @param apiVersion          API Version
     * @param currentTenantDomain Current tenant domain
     * @param apiProvider         API Provider
     * @param ignoreAndImport     This should be true if the exception should be ignored
     * @throws APIManagementException If an error occurs when retrieving the API to overwrite
     */
    private static API retrieveApiToOverwrite(String apiName, String apiVersion, String currentTenantDomain,
                                              APIProvider apiProvider, Boolean ignoreAndImport)
            throws APIManagementException {

        String provider = APIUtil.getAPIProviderFromAPINameVersionTenant(apiName, apiVersion, currentTenantDomain);
        APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), apiName, apiVersion);

        // Checking whether the API exists
        if (!apiProvider.isAPIAvailable(apiIdentifier)) {
            if (ignoreAndImport) {
                return null;
            }
            throw new APIMgtResourceNotFoundException(
                    "Error occurred while retrieving the API. API: " + apiName + StringUtils.SPACE
                            + APIConstants.API_DATA_VERSION + ": " + apiVersion + " not found", ExceptionCodes
                    .from(ExceptionCodes.API_NOT_FOUND, apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion()));
        }
        return apiProvider.getAPI(apiIdentifier);
    }

    /**
     * Process the extracted temporary directory in order to detect the flow and alter the directory structure
     * according to the flow.
     *
     * @param tempDirectory String of the temporary directory path value
     * @return Path to the extracted directory
     * @throws APIImportExportException If an error occurs while creating the directory, transferring files or
     *                                  extracting the content
     */
    public static String preprocessImportedArtifact(String tempDirectory) throws APIImportExportException {

        String tempDirectoryAbsolutePath = tempDirectory + File.separator;
        String paramsFileName =
                ImportExportConstants.INTERMEDIATE_PARAMS_FILE_LOCATION + ImportExportConstants.YAML_EXTENSION;
        boolean isParamsFileAvailable = CommonUtil.checkFileExistence(tempDirectoryAbsolutePath + paramsFileName);
        boolean isDeploymentDirectoryAvailable = CommonUtil
                .checkFileExistence(tempDirectoryAbsolutePath + ImportExportConstants.DEPLOYMENT_DIRECTORY_NAME);

        // When API controller is provided with params file
        if (isParamsFileAvailable) {
            if (!CommonUtil
                    .checkFileExistence(tempDirectoryAbsolutePath + ImportExportConstants.SOURCE_ZIP_DIRECTORY_NAME)) {
                throw new APIImportExportException("The source artifact is not provided properly");
            } else {
                String newExtractedFolderName = CommonUtil.extractArchive(
                        new File(tempDirectoryAbsolutePath + ImportExportConstants.SOURCE_ZIP_DIRECTORY_NAME),
                        tempDirectoryAbsolutePath);

                // Copy the params file to working directory
                String srcParamsFilePath = tempDirectoryAbsolutePath + paramsFileName;
                String destParamsFilePath =
                        tempDirectoryAbsolutePath + newExtractedFolderName + File.separator + paramsFileName;
                CommonUtil.copyFile(srcParamsFilePath, destParamsFilePath);
                return tempDirectoryAbsolutePath + newExtractedFolderName;
            }
        }
        //When API controller is provided with the "Deployment" directory
        if (isDeploymentDirectoryAvailable) {
            if (!CommonUtil
                    .checkFileExistence(tempDirectoryAbsolutePath + ImportExportConstants.SOURCE_ZIP_DIRECTORY_NAME)) {
                throw new APIImportExportException("The source artifact is not provided properly");
            } else {
                String newExtractedFolderName = CommonUtil.extractArchive(
                        new File(tempDirectoryAbsolutePath + ImportExportConstants.SOURCE_ZIP_DIRECTORY_NAME),
                        tempDirectoryAbsolutePath);

                // Copy the params file to working directory
                String srcParamsFilePath =
                        tempDirectoryAbsolutePath + ImportExportConstants.DEPLOYMENT_DIRECTORY_NAME + File.separator
                                + paramsFileName;
                String destParamsFilePath =
                        tempDirectoryAbsolutePath + newExtractedFolderName + File.separator + paramsFileName;
                CommonUtil.copyFile(srcParamsFilePath, destParamsFilePath);

                //move deployment directory into working directory
                String srcDeploymentDirectoryPath =
                        tempDirectoryAbsolutePath + ImportExportConstants.DEPLOYMENT_DIRECTORY_NAME;
                String destDeploymentDirectoryPath = tempDirectoryAbsolutePath + newExtractedFolderName + File.separator
                        + ImportExportConstants.DEPLOYMENT_DIRECTORY_NAME;
                CommonUtil.copyDirectory(srcDeploymentDirectoryPath, destDeploymentDirectoryPath);

                return tempDirectoryAbsolutePath + newExtractedFolderName;
            }
        }
        return tempDirectory;
    }

    public static String getArchivePathOfExtractedDirectory(String baseDirectory, InputStream uploadedInputStream)
            throws APIImportExportException {

        String uploadFileName = ImportExportConstants.UPLOAD_API_FILE_NAME;
        String absolutePath = baseDirectory + File.separator;
        CommonUtil.transferFile(uploadedInputStream, uploadFileName, absolutePath);
        String extractedFolderName = CommonUtil.extractArchive(new File(absolutePath + uploadFileName), absolutePath);
        return preprocessImportedArtifact(absolutePath + extractedFolderName);
    }

    /**
     * Extract the imported archive to a temporary folder and return the folder path of it.
     *
     * @param uploadedInputStream Input stream from the REST request
     * @return Path to the extracted directory
     * @throws APIImportExportException If an error occurs while creating the directory, transferring files or
     *                                  extracting the content
     */
    public static String getArchivePathOfExtractedDirectory(InputStream uploadedInputStream)
            throws APIImportExportException {
        // Temporary directory is used to create the required folders
        File importFolder = CommonUtil.createTempDirectory(null);
        String uploadFileName = ImportExportConstants.UPLOAD_API_FILE_NAME;
        String absolutePath = importFolder.getAbsolutePath() + File.separator;
        CommonUtil.transferFile(uploadedInputStream, uploadFileName, absolutePath);
        String extractedFolderName = CommonUtil.extractArchive(new File(absolutePath + uploadFileName), absolutePath);
        return preprocessImportedArtifact(absolutePath + extractedFolderName);
    }

    /**
     * Validate API/API Product configuration (api/api_product.yaml or api/api_product.json) and return it.
     *
     * @param pathToArchive            Path to the extracted folder
     * @param isDefaultProviderAllowed Preserve provider flag value
     * @param currentUser              Username of the current user
     * @throws APIManagementException If an error occurs while authorizing the provider or retrieving the definition
     */
    private static JsonElement retrieveValidatedDTOObject(String pathToArchive, Boolean isDefaultProviderAllowed,
                                                          String currentUser, String type)
            throws IOException, APIManagementException {

        JsonObject configObject = (StringUtils.equals(type, ImportExportConstants.TYPE_API)) ?
                retrievedAPIDtoJson(pathToArchive) :
                retrievedAPIProductDtoJson(pathToArchive);
        configObject = validatePreserveProvider(configObject, isDefaultProviderAllowed, currentUser);
        return configObject;
    }

    @NotNull
    private static JsonObject retrievedAPIDtoJson(String pathToArchive) throws IOException, APIManagementException {
        // Get API Definition as JSON
        String jsonContent =
                getFileContentAsJson(pathToArchive + ImportExportConstants.API_FILE_LOCATION);
        if (jsonContent == null) {
            throw new APIManagementException("Cannot find API definition. api.yaml or api.json should present",
                    ExceptionCodes.ERROR_FETCHING_DEFINITION_FILE);
        }
        return processRetrievedDefinition(jsonContent);
    }

    @NotNull
    private static JsonObject retrievedAPIProductDtoJson(String pathToArchive)
            throws IOException, APIManagementException {
        // Get API Product Definition as JSON
        String jsonContent = getFileContentAsJson(pathToArchive + ImportExportConstants.API_PRODUCT_FILE_LOCATION);
        if (jsonContent == null) {
            throw new APIManagementException(
                    "Cannot find API Product definition. api_product.yaml or api_product.json should present",
                    ExceptionCodes.ERROR_FETCHING_DEFINITION_FILE);
        }
        return processRetrievedDefinition(jsonContent);
    }

    /**
     * Process the retrieved api.yaml or api_product.yaml content.
     *
     * @param jsonContent Path to the extracted folder
     * @return JsonObject of processed api.yaml or api_product.yaml content
     * @throws IOException If an error occurs when the API/API Product name or version not provided
     */
    private static JsonObject processRetrievedDefinition(String jsonContent) throws IOException {

        String apiVersion;
        // Retrieving the field "data" in api.yaml/json or api_product.yaml/json and
        // convert it to a JSON object for further processing
        JsonElement configElement = new JsonParser().parse(jsonContent).getAsJsonObject().get(APIConstants.DATA);
        JsonObject configObject = configElement.getAsJsonObject();

        configObject = preProcessEndpointConfig(configObject);

        // Locate the "provider" within the "id" and set it as the current user
        String apiName = configObject.get(ImportExportConstants.API_NAME_ELEMENT).getAsString();

        // The "version" may not be available for an API Product
        if (configObject.has(ImportExportConstants.VERSION_ELEMENT)) {
            apiVersion = configObject.get(ImportExportConstants.VERSION_ELEMENT).getAsString();
        } else {
            apiVersion = ImportExportConstants.DEFAULT_API_PRODUCT_VERSION;
        }

        // Remove spaces of API/API Product name/version if present
        if (apiName != null && apiVersion != null) {
            configObject.remove(apiName);
            configObject.addProperty(ImportExportConstants.API_NAME_ELEMENT, apiName.replace(" ", ""));
            if (configObject.has(ImportExportConstants.VERSION_ELEMENT)) {
                configObject.remove(ImportExportConstants.VERSION_ELEMENT);
                configObject.addProperty(ImportExportConstants.VERSION_ELEMENT, apiVersion.replace(" ", ""));
            }
        } else {
            throw new IOException("API/API Product name and version must be provided in API/API Product definition");
        }
        return configObject;
    }

    public static APIDTO retrievedAPIDto(String pathToArchive) throws IOException, APIManagementException {

        JsonObject jsonObject = retrievedAPIDtoJson(pathToArchive);
        return new Gson().fromJson(jsonObject, APIDTO.class);
    }

    public static APIProductDTO retrieveAPIProductDto(String pathToArchive) throws IOException, APIManagementException {

        JsonObject jsonObject = retrievedAPIProductDtoJson(pathToArchive);

        return new Gson().fromJson(jsonObject, APIProductDTO.class);
    }

    /**
     * This function will preprocess endpoint config security.
     *
     * @param configObject Data object from the API/API Product configuration
     * @return API config object with pre processed endpoint config
     */
    private static JsonObject preProcessEndpointConfig(JsonObject configObject) {

        if (configObject.has(ImportExportConstants.ENDPOINT_CONFIG)) {
            JsonObject endpointConfig = configObject.get(ImportExportConstants.ENDPOINT_CONFIG).getAsJsonObject();
            if (endpointConfig.has(APIConstants.ENDPOINT_SECURITY)) {
                JsonObject endpointSecurity = endpointConfig.get(APIConstants.ENDPOINT_SECURITY).getAsJsonObject();
                if (endpointSecurity.has(APIConstants.ENDPOINT_SECURITY_SANDBOX)) {
                    JsonObject endpointSecuritySandbox = endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_SANDBOX)
                            .getAsJsonObject();
                    if (endpointSecuritySandbox.has(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS)) {
                        String customParameters = endpointSecuritySandbox
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS).toString();
                        endpointSecuritySandbox.remove(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                        endpointSecuritySandbox
                                .addProperty(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
                    }
                }
                if (endpointSecurity.has(APIConstants.ENDPOINT_SECURITY_PRODUCTION)) {
                    JsonObject endpointSecuritySandbox = endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION)
                            .getAsJsonObject();
                    if (endpointSecuritySandbox.has(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS)) {
                        String customParameters = endpointSecuritySandbox
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS).toString();
                        endpointSecuritySandbox.remove(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                        endpointSecuritySandbox
                                .addProperty(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
                    }
                }
            }
        }
        return configObject;
    }

    /**
     * Validate the provider of the API and modify the provider based on the preserveProvider flag value.
     *
     * @param configObject             Data object from the API/API Product configuration
     * @param isDefaultProviderAllowed Preserve provider flag value
     * @throws APIMgtAuthorizationFailedException If an error occurs while authorizing the provider
     */
    private static JsonObject validatePreserveProvider(JsonObject configObject, Boolean isDefaultProviderAllowed,
                                                       String currentUser) throws APIMgtAuthorizationFailedException {

        String prevProvider = configObject.get(ImportExportConstants.PROVIDER_ELEMENT).getAsString();
        String prevTenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(prevProvider));
        String currentTenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(currentUser));

        if (isDefaultProviderAllowed) {
            if (!StringUtils.equals(prevTenantDomain, currentTenantDomain)) {
                throw new APIMgtAuthorizationFailedException(
                        "Tenant mismatch! Please enable preserveProvider property for cross tenant API Import.");
            }
        } else {
            String prevProviderWithDomain = APIUtil.replaceEmailDomain(prevProvider);
            String currentUserWithDomain = APIUtil.replaceEmailDomain(currentUser);
            configObject.remove(ImportExportConstants.PROVIDER_ELEMENT);
            configObject.addProperty(ImportExportConstants.PROVIDER_ELEMENT, currentUser);

            if (configObject.get(ImportExportConstants.WSDL_URL) != null) {
                // If original provider is not preserved, replace provider name in the wsdl URL
                // with the current user with domain name
                configObject.addProperty(ImportExportConstants.WSDL_URL,
                        configObject.get(ImportExportConstants.WSDL_URL).getAsString()
                                .replace(prevProviderWithDomain, currentUserWithDomain));
            }
            configObject = setCurrentProviderToContext(configObject, currentTenantDomain, prevTenantDomain);
        }
        return configObject;
    }

    /**
     * Replace original provider name from imported API/API Product context with the logged in username
     * This method is used when "preserveProvider" property is set to false.
     *
     * @param jsonObject     Imported API or API Product
     * @param currentDomain  Current domain name
     * @param previousDomain Original domain name
     */
    public static JsonObject setCurrentProviderToContext(JsonObject jsonObject, String currentDomain,
                                                         String previousDomain) {

        String context = jsonObject.get(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT).getAsString();
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(currentDomain)
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(previousDomain)) {
            jsonObject.remove(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT);
            jsonObject.addProperty(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT,
                    context.replace(APIConstants.TENANT_PREFIX + previousDomain, StringUtils.EMPTY));
        } else if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(currentDomain)
                && MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(previousDomain)) {
            jsonObject.remove(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT);
            jsonObject.addProperty(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT,
                    APIConstants.TENANT_PREFIX + currentDomain + context);
        } else if (!StringUtils.equalsIgnoreCase(currentDomain, previousDomain)) {
            jsonObject.remove(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT);
            jsonObject.addProperty(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT,
                    context.replace(previousDomain, currentDomain));
        }
        return jsonObject;
    }

    /**
     * Retrieve API Definition as JSON.
     *
     * @param pathToArchive Path to API or API Product archive
     * @throws IOException If an error occurs while reading the file
     */
    public static String getFileContentAsJson(String pathToArchive) throws IOException {

        String jsonContent = null;
        String pathToYamlFile = pathToArchive + ImportExportConstants.YAML_EXTENSION;
        String pathToJsonFile = pathToArchive + ImportExportConstants.JSON_EXTENSION;

        // Load yaml representation first if it is present
        if (CommonUtil.checkFileExistence(pathToYamlFile)) {
            if (log.isDebugEnabled()) {
                log.debug("Found api definition file " + pathToYamlFile);
            }
            String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
            jsonContent = CommonUtil.yamlToJson(yamlContent);
        } else if (CommonUtil.checkFileExistence(pathToJsonFile)) {
            // load as a json fallback
            if (log.isDebugEnabled()) {
                log.debug("Found api definition file " + pathToJsonFile);
            }
            jsonContent = FileUtils.readFileToString(new File(pathToJsonFile));
        }
        return jsonContent;
    }

    /**
     * Validate Aysnc API definition from the archive directory and return it.
     *
     * @param pathToArchive Path to API archive
     * @return APIDefinitionValidationResponse of the Async API definition content
     * @throws APIManagementException If an error occurs while reading the file
     */
    public static APIDefinitionValidationResponse retrieveValidatedAsyncApiDefinitionFromArchive(String pathToArchive)
            throws APIManagementException {

        try {
            String asyncApiDefinition = loadAsyncApiDefinitionFromFile(pathToArchive);
            APIDefinitionValidationResponse validationResponse =
                    AsyncApiParserUtil.validateAsyncAPISpecification(asyncApiDefinition, true);
            if (!validationResponse.isValid()) {
                throw new APIManagementException(
                        "Error occurred while importing the API. Invalid AsyncAPI definition found. "
                                + validationResponse.getErrorItems());
            }
            return validationResponse;
        } catch (IOException e) {
            throw new APIManagementException("Error while reading API meta information from path: " + pathToArchive, e,
                    ExceptionCodes.ERROR_READING_META_DATA);
        }
    }

    private static String loadAsyncApiDefinitionFromFile(String pathToArchive) throws IOException {

        if (CommonUtil.checkFileExistence(pathToArchive + ImportExportConstants.JSON_ASYNCAPI_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug("Found AsyncAPI file " + pathToArchive
                        + ImportExportConstants.JSON_ASYNCAPI_DEFINITION_LOCATION);
            }
            return FileUtils
                    .readFileToString(new File(pathToArchive, ImportExportConstants.JSON_ASYNCAPI_DEFINITION_LOCATION));
        } else if (CommonUtil
                .checkFileExistence(pathToArchive + ImportExportConstants.YAML_ASYNCAPI_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug("Found AsyncAPI file " + pathToArchive
                        + ImportExportConstants.YAML_ASYNCAPI_DEFINITION_LOCATION);
            }
            return CommonUtil.yamlToJson(FileUtils.readFileToString(
                    new File(pathToArchive + ImportExportConstants.YAML_ASYNCAPI_DEFINITION_LOCATION)));
        }
        throw new IOException("Missing AsyncAPI definition file.");
    }

    /**
     * Validate GraphQL Schema definition from the archive directory and return it.
     *
     * @param pathToArchive Path to API archive
     * @throws APIImportExportException If an error occurs while reading the file
     */
    public static String retrieveValidatedGraphqlSchemaFromArchive(String pathToArchive)
            throws APIManagementException {

        File file = new File(pathToArchive + ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION);
        try {
            String schemaDefinition = loadGraphqlSDLFile(pathToArchive);
            GraphQLValidationResponseDTO graphQLValidationResponseDTO = PublisherCommonUtils
                    .validateGraphQLSchema(file.getName(), schemaDefinition);
            if (!graphQLValidationResponseDTO.isIsValid()) {
                throw new APIManagementException(
                        "Error occurred while importing the API. Invalid GraphQL schema definition found. "
                                + graphQLValidationResponseDTO.getErrorMessage());
            }
            return schemaDefinition;
        } catch (IOException e) {
            throw new APIManagementException("Error while reading API meta information from path: " + pathToArchive, e,
                    ExceptionCodes.ERROR_READING_META_DATA);
        }
    }

    /**
     * Retrieve graphql complexity information from the file and validate it with the schema.
     *
     * @param pathToArchive Path to API archive
     * @param schema        GraphQL schema
     * @return GraphQL complexity info validated with the schema
     * @throws APIManagementException If an error occurs while reading the file
     */
    private static GraphqlComplexityInfo retrieveGraphqlComplexityInfoFromArchive(String pathToArchive, String schema)
            throws APIManagementException {

        try {
            String jsonContent =
                    getFileContentAsJson(pathToArchive + ImportExportConstants.GRAPHQL_COMPLEXITY_INFO_LOCATION);
            if (jsonContent == null) {
                return null;
            }
            JsonElement configElement = new JsonParser().parse(jsonContent).getAsJsonObject().get(APIConstants.DATA);
            GraphQLQueryComplexityInfoDTO complexityDTO = new Gson().fromJson(String.valueOf(configElement),
                    GraphQLQueryComplexityInfoDTO.class);
            GraphqlComplexityInfo graphqlComplexityInfo =
                    GraphqlQueryAnalysisMappingUtil.fromDTOtoValidatedGraphqlComplexityInfo(complexityDTO, schema);
            return graphqlComplexityInfo;
        } catch (IOException e) {
            throw new APIManagementException("Error while reading graphql complexity info from path: " + pathToArchive,
                    e, ExceptionCodes.ERROR_READING_META_DATA);
        }
    }

    /**
     * Retrieve the deployment information from the file.
     *
     * @param pathToArchive Path to API/API Product archive
     * @return a JsonArray of the deployed gateway environments
     * @throws APIManagementException If an error occurs while reading the file
     */
    private static JsonArray retrieveDeploymentLabelsFromArchive(String pathToArchive, boolean dependentAPIFromProduct)
            throws APIManagementException {

        try {
            //If the artifact is a dependent API from a API Product, instead of the artifact's deployment environments,
            //products deployment environments are used.
            String jsonContent = (dependentAPIFromProduct) ?
                    getFileContentAsJson(new File(pathToArchive).getParentFile().getParent()
                            + File.separator + ImportExportConstants.DEPLOYMENT_INFO_LOCATION) :
                    getFileContentAsJson(pathToArchive + ImportExportConstants.DEPLOYMENT_INFO_LOCATION);
            if (jsonContent == null) {
                return null;
            }
            // Retrieving the field "data" in deployment_environments.yaml
            JsonElement configElement = new JsonParser().parse(jsonContent).getAsJsonObject().get(APIConstants.DATA);
            return configElement.getAsJsonArray();
        } catch (IOException e) {
            throw new APIManagementException("Error while reading deployment environments info from path: "
                    + pathToArchive, e, ExceptionCodes.ERROR_READING_META_DATA);
        }
    }

    /**
     * Validate WSDL definition from the archive directory and return it.
     *
     * @param pathToArchive Path to API archive
     * @throws APIImportExportException If an error due to an invalid WSDL definition
     */
    private static void validateWSDLFromArchive(String pathToArchive, APIDTO apiDto) throws APIManagementException {

        try {
            byte[] wsdlDefinition = loadWsdlFile(pathToArchive, apiDto);
            WSDLValidationResponse wsdlValidationResponse = APIMWSDLReader.
                    getWsdlValidationResponse(APIMWSDLReader.getWSDLProcessor(wsdlDefinition));
            if (!wsdlValidationResponse.isValid()) {
                throw new APIManagementException(
                        "Error occurred while importing the API. Invalid WSDL definition found. "
                                + wsdlValidationResponse.getError());
            }
        } catch (IOException | APIManagementException e) {
            throw new APIManagementException("Error while reading API meta information from path: " + pathToArchive, e,
                    ExceptionCodes.ERROR_READING_META_DATA);
        }
    }

    /**
     * Load the graphQL schema definition from archive.
     *
     * @param pathToArchive Path to archive
     * @return Schema definition content
     * @throws IOException When SDL file not found
     */
    private static String loadGraphqlSDLFile(String pathToArchive) throws IOException {

        if (CommonUtil.checkFileExistence(pathToArchive + ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug("Found graphQL sdl file " + pathToArchive
                        + ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION);
            }
            return FileUtils.readFileToString(
                    new File(pathToArchive, ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION));
        }
        throw new IOException("Missing graphQL schema definition file. schema.graphql should be present.");
    }

    /**
     * Load the graphQL complexity info from archive.
     *
     * @param pathToArchive Path to archive
     * @return Schema definition content
     * @throws IOException When SDL file not found
     */
    private static String loadGraphqlComplexityInfoFile(String pathToArchive) throws IOException {

        if (CommonUtil.checkFileExistence(pathToArchive + ImportExportConstants.GRAPHQL_COMPLEXITY_INFO_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug("Found graphQL complexity info file " + pathToArchive
                        + ImportExportConstants.GRAPHQL_COMPLEXITY_INFO_LOCATION);
            }
            return FileUtils.readFileToString(
                    new File(pathToArchive, ImportExportConstants.GRAPHQL_COMPLEXITY_INFO_LOCATION));
        }
        return null;
    }

    /**
     * Load the WSDL definition from archive.
     *
     * @param pathToArchive Path to archive
     * @param apiDto        API DTO to add
     * @return Schema definition content
     * @throws IOException When WSDL file not found
     */
    private static byte[] loadWsdlFile(String pathToArchive, APIDTO apiDto) throws IOException {

        String wsdlFileName = apiDto.getName() + "-" + apiDto.getVersion() + APIConstants.WSDL_FILE_EXTENSION;
        String pathToFile = pathToArchive + ImportExportConstants.WSDL_LOCATION + wsdlFileName;
        if (CommonUtil.checkFileExistence(pathToFile)) {
            if (log.isDebugEnabled()) {
                log.debug("Found WSDL file " + pathToFile);
            }
            return FileUtils.readFileToByteArray(new File(pathToFile));
        }
        throw new IOException("Missing WSDL file. It should be present.");
    }

    /**
     * Validate swagger definition from the archive directory and return it.
     *
     * @param pathToArchive Path to API or API Product archive
     * @return APIDefinitionValidationResponse of the swagger content
     * @throws APIManagementException If an error occurs while reading the file
     */
    public static APIDefinitionValidationResponse retrieveValidatedSwaggerDefinitionFromArchive(String pathToArchive)
            throws APIManagementException {

        try {
            String swaggerContent = loadSwaggerFile(pathToArchive);
            APIDefinitionValidationResponse validationResponse = OASParserUtil
                    .validateAPIDefinition(swaggerContent, Boolean.TRUE);
            if (!validationResponse.isValid()) {
                throw new APIManagementException(
                        "Error occurred while importing the API. Invalid Swagger definition found. "
                                + validationResponse.getErrorItems(), ExceptionCodes.ERROR_READING_META_DATA);
            }
            return validationResponse;
        } catch (IOException e) {
            throw new APIManagementException("Error while reading API meta information from path: " + pathToArchive, e,
                    ExceptionCodes.ERROR_READING_META_DATA);
        }
    }

    /**
     * Load a swagger document from archive. This method lookup for swagger as YAML or JSON.
     *
     * @param pathToArchive Path to archive
     * @return Swagger content as a JSON
     * @throws IOException When swagger document not found
     */
    public static String loadSwaggerFile(String pathToArchive) throws IOException {

        if (CommonUtil.checkFileExistence(pathToArchive + ImportExportConstants.YAML_SWAGGER_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Found swagger file " + pathToArchive + ImportExportConstants.YAML_SWAGGER_DEFINITION_LOCATION);
            }
            String yamlContent = FileUtils
                    .readFileToString(new File(pathToArchive + ImportExportConstants.YAML_SWAGGER_DEFINITION_LOCATION));
            return CommonUtil.yamlToJson(yamlContent);
        } else if (CommonUtil
                .checkFileExistence(pathToArchive + ImportExportConstants.JSON_SWAGGER_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Found swagger file " + pathToArchive + ImportExportConstants.JSON_SWAGGER_DEFINITION_LOCATION);
            }
            return FileUtils
                    .readFileToString(new File(pathToArchive + ImportExportConstants.JSON_SWAGGER_DEFINITION_LOCATION));
        }
        throw new IOException("Missing swagger file. Either swagger.json or swagger.yaml should present");
    }

    /**
     * This method update the API or API Product with the icon to be displayed at the API store.
     *
     * @param pathToArchive  Location of the extracted folder of the API or API Product
     * @param apiTypeWrapper The imported API object
     */
    private static void addThumbnailImage(String pathToArchive, ApiTypeWrapper apiTypeWrapper,
                                          APIProvider apiProvider) {

        //Adding image icon to the API if there is any
        File imageFolder = new File(pathToArchive + ImportExportConstants.IMAGE_FILE_LOCATION);
        File[] fileArray = imageFolder.listFiles();
        if (imageFolder.isDirectory() && fileArray != null) {
            //This loop locates the icon of the API
            for (File imageFile : fileArray) {
                if (imageFile != null) {
                    updateWithThumbnail(imageFile, apiTypeWrapper, apiProvider);
                    //the loop is terminated after successfully locating the icon
                    break;
                }
            }
        }
    }

    /**
     * This method update the API Product with the thumbnail image from imported API Product.
     *
     * @param imageFile      Image file
     * @param apiTypeWrapper API or API Product to update
     * @param apiProvider    API Provider
     */
    private static void updateWithThumbnail(File imageFile, ApiTypeWrapper apiTypeWrapper, APIProvider apiProvider) {

        Identifier identifier = apiTypeWrapper.getId();
        String fileName = imageFile.getName();
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        if (StringUtils.isBlank(mimeType)) {
            try {
                // Check whether the icon is in .json format (UI icons are stored as .json)
                new JsonParser().parse(new FileReader(imageFile));
                mimeType = APIConstants.APPLICATION_JSON_MEDIA_TYPE;
            } catch (JsonParseException e) {
                // Here the exceptions were handled and logged that may arise when parsing the .json file,
                // and this will not break the flow of importing the API.
                // If the .json is wrong or cannot be found the API import process will still be carried out.
                log.error("Failed to read the thumbnail file. ", e);
            } catch (FileNotFoundException e) {
                log.error("Failed to find the thumbnail file. ", e);
            }
        }
        try (FileInputStream inputStream = new FileInputStream(imageFile.getAbsolutePath())) {
            ResourceFile apiImage = new ResourceFile(inputStream, mimeType);
            String thumbPath = APIUtil.getIconPath(identifier);
            String thumbnailUrl = apiProvider.addResourceFile(identifier, thumbPath, apiImage);
            apiTypeWrapper.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, identifier.getProviderName()));
            APIUtil.setResourcePermissions(identifier.getProviderName(), null, null, thumbPath);
            if (apiTypeWrapper.isAPIProduct()) {
                apiProvider.updateAPIProduct(apiTypeWrapper.getApiProduct());
            } else {
                apiProvider.updateAPI(apiTypeWrapper.getApi());
            }
        } catch (FaultGatewaysException e) {
            //This is logged and process is continued because icon is optional for an API
            log.error("Failed to update API/API Product after adding icon. ", e);
        } catch (APIManagementException e) {
            log.error("Failed to add icon to the API/API Product: " + identifier.getName(), e);
        } catch (FileNotFoundException e) {
            log.error("Icon for API/API Product: " + identifier.getName() + " is not found.", e);
        } catch (IOException e) {
            log.error("Failed to import icon for API/API Product:" + identifier.getName());
        }
    }

    /**
     * This method adds the documents to the imported API or API Product.
     *
     * @param pathToArchive  Location of the extracted folder of the API or API Product
     * @param apiTypeWrapper Imported API or API Product
     */
    private static void addDocumentation(String pathToArchive, ApiTypeWrapper apiTypeWrapper, APIProvider apiProvider) {

        String jsonContent = null;
        Identifier identifier = apiTypeWrapper.getId();
        String docDirectoryPath = pathToArchive + File.separator + ImportExportConstants.DOCUMENT_DIRECTORY;

        File documentsFolder = new File(docDirectoryPath);
        File[] fileArray = documentsFolder.listFiles();
        String provider = (apiTypeWrapper.isAPIProduct()) ? apiTypeWrapper.getApiProduct().getId().getProviderName() :
                apiTypeWrapper.getApi().getId().getProviderName();
        String tenantDomain = MultitenantUtils.getTenantDomain(provider);

        try {
            // Remove all documents associated with the API before update
            List<Documentation> documents = apiProvider.getAllDocumentation(identifier);
            if (documents != null) {
                for (Documentation documentation : documents) {
                    apiProvider.removeDocumentation(identifier, documentation.getId(), tenantDomain);
                }
            }

            if (documentsFolder.isDirectory() && fileArray != null) {
                //This loop locates the documents inside each repo
                for (File documentFile : fileArray) {
                    String folderName = documentFile.getName();
                    String individualDocumentFilePath = docDirectoryPath + File.separator + folderName;
                    String pathToYamlFile = individualDocumentFilePath + ImportExportConstants.DOCUMENT_FILE_NAME
                            + ImportExportConstants.YAML_EXTENSION;
                    String pathToJsonFile = individualDocumentFilePath + ImportExportConstants.DOCUMENT_FILE_NAME
                            + ImportExportConstants.JSON_EXTENSION;

                    // Load document file if exists
                    if (CommonUtil.checkFileExistence(pathToYamlFile)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found documents definition file " + pathToYamlFile);
                        }
                        String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
                        jsonContent = CommonUtil.yamlToJson(yamlContent);
                    } else if (CommonUtil.checkFileExistence(pathToJsonFile)) {
                        //load as a json fallback
                        if (log.isDebugEnabled()) {
                            log.debug("Found documents definition file " + pathToJsonFile);
                        }
                        jsonContent = FileUtils.readFileToString(new File(pathToJsonFile));
                    }

                    JsonElement configElement = new JsonParser().parse(jsonContent).getAsJsonObject()
                            .get(APIConstants.DATA);
                    DocumentDTO documentDTO = new Gson().fromJson(configElement.getAsJsonObject(), DocumentDTO.class);

                    // Add the documentation DTO
                    Documentation documentation = apiTypeWrapper.isAPIProduct() ?
                            PublisherCommonUtils
                                    .addDocumentationToAPI(documentDTO, apiTypeWrapper.getApiProduct().getUuid()) :
                            PublisherCommonUtils.addDocumentationToAPI(documentDTO, apiTypeWrapper.getApi().getUuid());

                    // Adding doc content
                    String docSourceType = documentation.getSourceType().toString();
                    boolean docContentExists =
                            Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(docSourceType)
                                    || Documentation.DocumentSourceType.MARKDOWN.toString()
                                    .equalsIgnoreCase(docSourceType);
                    if (docContentExists) {
                        try (FileInputStream inputStream = new FileInputStream(
                                individualDocumentFilePath + File.separator + folderName)) {
                            String inlineContent = IOUtils.toString(inputStream, ImportExportConstants.CHARSET);
                            if (!apiTypeWrapper.isAPIProduct()) {
                                apiProvider.addDocumentationContent(apiTypeWrapper.getApi(), documentation.getName(),
                                        inlineContent);
                            } else {
                                apiProvider.addProductDocumentationContent(apiTypeWrapper.getApiProduct(),
                                        documentation.getName(), inlineContent);
                            }
                        }
                    } else if (ImportExportConstants.FILE_DOC_TYPE.equalsIgnoreCase(docSourceType)) {
                        String filePath = documentation.getFilePath();
                        try (FileInputStream inputStream = new FileInputStream(
                                individualDocumentFilePath + File.separator + filePath)) {
                            String docExtension = FilenameUtils.getExtension(
                                    pathToArchive + File.separator + ImportExportConstants.DOCUMENT_DIRECTORY
                                            + File.separator + filePath);
                            ResourceFile apiDocument = new ResourceFile(inputStream, docExtension);
                            String visibleRolesList = apiTypeWrapper.getVisibleRoles();
                            String[] visibleRoles = new String[0];
                            if (visibleRolesList != null) {
                                visibleRoles = visibleRolesList.split(",");
                            }
                            String filePathDoc = APIUtil.getDocumentationFilePath(identifier, filePath);
                            APIUtil.setResourcePermissions(apiTypeWrapper.getId().getProviderName(),
                                    apiTypeWrapper.getVisibility(), visibleRoles, filePathDoc);
                            documentation.setFilePath(
                                    apiProvider.addResourceFile(apiTypeWrapper.getId(), filePathDoc, apiDocument));
                            if (!apiTypeWrapper.isAPIProduct()) {
                                apiProvider.updateDocumentation(apiTypeWrapper.getApi().getUuid(), documentation,
                                        tenantDomain);
                            } else {
                                apiProvider.updateDocumentation(apiTypeWrapper.getApiProduct().getUuid(), documentation,
                                        tenantDomain);
                            }
                        } catch (FileNotFoundException e) {
                            //this error is logged and ignored because documents are optional in an API
                            log.error("Failed to locate the document files of the API/API Product: " + apiTypeWrapper
                                    .getId().getName(), e);
                            continue;
                        }
                    }

                }
            }
        } catch (FileNotFoundException e) {
            //this error is logged and ignored because documents are optional in an API
            log.error("Failed to locate the document files of the API/API Product: " + identifier.getName(), e);
        } catch (APIManagementException | IOException e) {
            //this error is logged and ignored because documents are optional in an API
            log.error("Failed to add Documentations to API/API Product: " + identifier.getName(), e);
        }
    }

    /**
     * This method adds API sequences to the imported API. If the sequence is a newly defined one, it is added.
     *
     * @param pathToArchive Location of the extracted folder of the API
     * @param importedApi   The imported API object
     * @param registry      Registry
     */
    private static void addAPISequences(String pathToArchive, API importedApi, Registry registry) throws IOException {

        String regResourcePath;

        //Adding in-sequence, if any
        String sequenceContent = retrieveSequenceContent(pathToArchive, false, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                importedApi.getInSequence());
        if (StringUtils.isNotEmpty(sequenceContent)) {
            String inSequenceFileName = importedApi.getInSequence() + APIConstants.XML_EXTENSION;
            regResourcePath = APIConstants.API_CUSTOM_INSEQUENCE_LOCATION + inSequenceFileName;
            addSequenceToRegistry(false, registry, sequenceContent, regResourcePath);
        }

        sequenceContent = retrieveSequenceContent(pathToArchive, false, APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                importedApi.getOutSequence());
        if (StringUtils.isNotEmpty(sequenceContent)) {
            String outSequenceFileName = importedApi.getOutSequence() + APIConstants.XML_EXTENSION;
            regResourcePath = APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION + outSequenceFileName;
            addSequenceToRegistry(false, registry, sequenceContent, regResourcePath);
        }

        sequenceContent = retrieveSequenceContent(pathToArchive, false, APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT,
                importedApi.getFaultSequence());
        if (StringUtils.isNotEmpty(sequenceContent)) {
            String faultSequenceFileName = importedApi.getFaultSequence() + APIConstants.XML_EXTENSION;
            regResourcePath = APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION + faultSequenceFileName;
            addSequenceToRegistry(false, registry, sequenceContent, regResourcePath);
        }

    }

    public static String retrieveSequenceContent(String pathToArchive, boolean specific, String type,
                                                 String sequenceName) {

        String sequenceFileName = sequenceName + APIConstants.XML_EXTENSION;
        String sequenceFileLocation = null;
        if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equals(type)) {
            sequenceFileLocation =
                    pathToArchive + ImportExportConstants.IN_SEQUENCE_LOCATION;
        } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equals(type)) {
            sequenceFileLocation =
                    pathToArchive + ImportExportConstants.OUT_SEQUENCE_LOCATION;

        } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT.equals(type)) {
            sequenceFileLocation =
                    pathToArchive + ImportExportConstants.FAULT_SEQUENCE_LOCATION;
        }
        if (sequenceFileLocation != null) {
            if (specific) {
                if (sequenceFileLocation.endsWith(File.separator)) {
                    sequenceFileLocation = sequenceFileLocation + ImportExportConstants.CUSTOM_TYPE;
                } else {
                    sequenceFileLocation = sequenceFileLocation + File.separator + ImportExportConstants.CUSTOM_TYPE;
                }
            }
            sequenceFileLocation = sequenceFileLocation + File.separator + sequenceFileName;
            try {
                return retrieveSequenceContentFromLocation(sequenceFileLocation);
            } catch (IOException e) {
                log.error("Failed to add sequences into the registry : " + sequenceFileLocation, e);
            }
        }
        return null;
    }

    private static String retrieveSequenceContentFromLocation(String sequenceFileLocation)
            throws IOException {

        if (CommonUtil.checkFileExistence(sequenceFileLocation)) {
            File sequenceFile = new File(sequenceFileLocation);
            try (InputStream seqStream = new FileInputStream(sequenceFile)) {
                return IOUtils.toString(seqStream);
            }
        }
        return null;
    }

    /**
     * This method adds API Specific sequences added through the Publisher to the imported API. If the specific
     * sequence already exists, it is updated.
     *
     * @param pathToArchive Location of the extracted folder of the API
     * @param registry      Registry
     * @param apiIdentifier API Identifier
     */
    private static void addAPISpecificSequences(String pathToArchive, Registry registry, APIIdentifier apiIdentifier) {

        String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
        // Getting registry API base path out of apiResourcePath
        apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
        String sequencesDirectoryPath = pathToArchive + File.separator + ImportExportConstants.SEQUENCES_RESOURCE;

        // Add multiple custom sequences to registry for each type in/out/fault
        addCustomSequencesToRegistry(sequencesDirectoryPath, apiResourcePath, registry,
                ImportExportConstants.IN_SEQUENCE_PREFIX);
        addCustomSequencesToRegistry(sequencesDirectoryPath, apiResourcePath, registry,
                ImportExportConstants.OUT_SEQUENCE_PREFIX);
        addCustomSequencesToRegistry(sequencesDirectoryPath, apiResourcePath, registry,
                ImportExportConstants.FAULT_SEQUENCE_PREFIX);
    }

    /**
     * @param sequencesDirectoryPath Location of the sequences directory inside the extracted folder of the API
     * @param apiResourcePath        API resource path in the registry
     * @param registry               Registry
     * @param type                   Sequence type (in/out/fault)
     */
    private static void addCustomSequencesToRegistry(String sequencesDirectoryPath, String apiResourcePath,
                                                     Registry registry, String type) {

        String apiSpecificSequenceFilePath =
                sequencesDirectoryPath + File.separator + type + ImportExportConstants.SEQUENCE_LOCATION_POSTFIX
                        + File.separator + ImportExportConstants.CUSTOM_TYPE;
        if (CommonUtil.checkFileExistence(apiSpecificSequenceFilePath)) {
            File apiSpecificSequencesDirectory = new File(apiSpecificSequenceFilePath);
            File[] apiSpecificSequencesDirectoryListing = apiSpecificSequencesDirectory.listFiles();
            if (apiSpecificSequencesDirectoryListing != null) {
                for (File apiSpecificSequence : apiSpecificSequencesDirectoryListing) {
                    String individualSequenceLocation =
                            apiSpecificSequenceFilePath + File.separator + apiSpecificSequence.getName();
                    // Constructing mediation resource path
                    String mediationResourcePath = apiResourcePath + RegistryConstants.PATH_SEPARATOR + type
                            + RegistryConstants.PATH_SEPARATOR + apiSpecificSequence.getName();
                    try {
                        String content = retrieveSequenceContentFromLocation(individualSequenceLocation);
                        if (StringUtils.isNotEmpty(content)) {
                            addSequenceToRegistry(true, registry, content, mediationResourcePath);
                        }
                    } catch (IOException e) {
                        log.error(
                                "I/O error while writing sequence data to the registry : " + individualSequenceLocation,
                                e);
                    }
                }
            }
        }
    }

    /**
     * This method adds the sequence files to the registry. This updates the API specific sequences if already exists.
     *
     * @param isAPISpecific   Whether the adding sequence is API specific
     * @param registry        The registry instance
     * @param sequenceContent Location of the sequence file
     * @param regResourcePath Resource path in the registry
     */
    private static void addSequenceToRegistry(Boolean isAPISpecific, Registry registry, String sequenceContent,
                                              String regResourcePath) {

        try {
            if (registry.resourceExists(regResourcePath) && !isAPISpecific) {
                if (log.isDebugEnabled()) {
                    log.debug("Sequence already exists in registry path: " + regResourcePath);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Adding Sequence to the registry path : " + regResourcePath);
                }
                byte[] inSeqData = sequenceContent.getBytes();
                Resource inSeqResource = registry.newResource();
                inSeqResource.setContent(inSeqData);
                registry.put(regResourcePath, inSeqResource);
            }
        } catch (RegistryException e) {
            //this is logged and ignored because sequences are optional
            log.error("Failed to add sequences into the registry : " + regResourcePath, e);
        }
    }

    /**
     * This method adds the WSDL to the registry, if there is a WSDL associated with the API.
     *
     * @param pathToArchive Location of the extracted folder of the API
     * @param importedApi   The imported API object
     * @param apiProvider   API Provider
     * @param registry      Registry
     */
    private static void addAPIWsdl(String pathToArchive, API importedApi, APIProvider apiProvider, Registry registry) {

        String wsdlFileName = importedApi.getId().getApiName() + "-" + importedApi.getId().getVersion()
                + APIConstants.WSDL_FILE_EXTENSION;
        String wsdlPath = pathToArchive + ImportExportConstants.WSDL_LOCATION + wsdlFileName;

        if (CommonUtil.checkFileExistence(wsdlPath)) {
            try {
                URL wsdlFileUrl = new File(wsdlPath).toURI().toURL();
                importedApi.setWsdlUrl(wsdlFileUrl.toString());
                APIUtil.createWSDL(registry, importedApi);
                apiProvider.updateAPI(importedApi);
            } catch (MalformedURLException e) {
                // this exception is logged and ignored since WSDL is optional for an API
                log.error("Error in getting WSDL URL. ", e);
            } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
                // this exception is logged and ignored since WSDL is optional for an API
                log.error("Error in putting the WSDL resource to registry. ", e);
            } catch (APIManagementException e) {
                // this exception is logged and ignored since WSDL is optional for an API
                log.error("Error in creating the WSDL resource in the registry. ", e);
            } catch (FaultGatewaysException e) {
                // This is logged and process is continued because WSDL is optional for an API
                log.error("Failed to update API after adding WSDL. ", e);
            }
        }
    }

    /**
     * This method import endpoint certificate.
     *
     * @param pathToArchive location of the extracted folder of the API
     * @param importedApi   the imported API object
     * @throws APIImportExportException If an error occurs while importing endpoint certificates from file
     */
    private static void addEndpointCertificates(String pathToArchive, API importedApi, APIProvider apiProvider,
                                                int tenantId) throws APIManagementException {

        String jsonContent = null;
        String pathToEndpointsCertificatesDirectory =
                pathToArchive + File.separator + ImportExportConstants.ENDPOINT_CERTIFICATES_DIRECTORY;
        String pathToYamlFile = pathToEndpointsCertificatesDirectory + ImportExportConstants.ENDPOINTS_CERTIFICATE_FILE
                + ImportExportConstants.YAML_EXTENSION;
        String pathToJsonFile = pathToEndpointsCertificatesDirectory + ImportExportConstants.ENDPOINTS_CERTIFICATE_FILE
                + ImportExportConstants.JSON_EXTENSION;
        try {
            // try loading file as YAML
            if (CommonUtil.checkFileExistence(pathToYamlFile)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found certificate file " + pathToYamlFile);
                }
                String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
                jsonContent = CommonUtil.yamlToJson(yamlContent);
            } else if (CommonUtil.checkFileExistence(pathToJsonFile)) {
                // load as a json fallback
                if (log.isDebugEnabled()) {
                    log.debug("Found certificate file " + pathToJsonFile);
                }
                jsonContent = FileUtils.readFileToString(new File(pathToJsonFile));
            }
            if (jsonContent == null) {
                log.debug("No certificate file found to be added, skipping certificate import.");
                return;
            }
            JsonElement configElement = new JsonParser().parse(jsonContent).getAsJsonObject().get(APIConstants.DATA);
            JsonArray certificates = addFileContentToCertificates(configElement.getAsJsonArray(),
                    pathToEndpointsCertificatesDirectory);
            for (JsonElement certificate : certificates) {
                updateAPIWithCertificate(certificate, apiProvider, importedApi, tenantId);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error in reading certificates file", e);
        }
    }

    /**
     * Add the certificate content to the object.
     *
     * @param certificates                Certificates array
     * @param pathToCertificatesDirectory File path to the certificates directory
     * @throws IOException If an error occurs while retrieving the certificate content from the file
     */
    private static JsonArray addFileContentToCertificates(JsonArray certificates, String pathToCertificatesDirectory)
            throws IOException {

        JsonArray modifiedCertificates = new JsonArray();
        for (JsonElement certificate : certificates) {
            JsonObject certificateObject = certificate.getAsJsonObject();
            String certificateFileName = certificateObject.get(ImportExportConstants.CERTIFICATE_FILE).getAsString();
            // Get the content of the certificate file from the relevant certificate file inside the certificates
            // directory and add it to the certificate
            String certificateContent = getFileContentOfCertificate(certificateFileName, pathToCertificatesDirectory);
            if (certificateObject.has(ImportExportConstants.CERTIFICATE_CONTENT_JSON_KEY)) {
                certificateObject.remove(ImportExportConstants.CERTIFICATE_CONTENT_JSON_KEY);
            }
            certificateObject.addProperty(ImportExportConstants.CERTIFICATE_CONTENT_JSON_KEY, certificateContent);
            modifiedCertificates.add(certificateObject);
        }
        return modifiedCertificates;
    }

    /**
     * Get the file content of a certificate in the Client-certificate directory.
     *
     * @param certificateFileName         Certificate file name
     * @param pathToCertificatesDirectory Path to client certificates directory
     * @return content of the certificate
     */
    private static String getFileContentOfCertificate(String certificateFileName, String pathToCertificatesDirectory)
            throws IOException {

        String certificateContent = null;
        File certificatesDirectory = new File(pathToCertificatesDirectory);
        File[] certificatesDirectoryListing = certificatesDirectory.listFiles();
        // Iterate the Endpoints certificates directory to get the relevant cert file
        if (certificatesDirectoryListing != null) {
            for (File endpointsCertificate : certificatesDirectoryListing) {
                if (StringUtils.equals(certificateFileName, endpointsCertificate.getName())) {
                    certificateContent = FileUtils.readFileToString(
                            new File(pathToCertificatesDirectory + File.separator + certificateFileName));
                    certificateContent = StringUtils.substringBetween(certificateContent,
                            APIConstants.BEGIN_CERTIFICATE_STRING, APIConstants.END_CERTIFICATE_STRING);
                }
            }
        }
        return certificateContent;
    }

    /**
     * Update API with the certificate.
     * If certificate alias already exists for tenant in database, certificate content will be
     * updated in trust store. If cert alias does not exits in database for that tenant, add the certificate to
     * publisher and gateway nodes. In such case if alias already exits in the trust store, update the certificate
     * content for that alias.
     *
     * @param certificate Certificate JSON element
     * @param apiProvider API Provider
     * @param importedApi API to import
     * @param tenantId    Tenant Id
     */
    private static void updateAPIWithCertificate(JsonElement certificate, APIProvider apiProvider, API importedApi,
                                                 int tenantId) throws APIManagementException {

        String certificateFileName = certificate.getAsJsonObject().get(ImportExportConstants.CERTIFICATE_FILE)
                .getAsString();
        String certificateContent = certificate.getAsJsonObject()
                .get(ImportExportConstants.CERTIFICATE_CONTENT_JSON_KEY).getAsString();
        if (certificateContent == null) {
            throw new APIManagementException("Certificate " + certificateFileName + "is null");
        }
        String alias = certificate.getAsJsonObject().get(ImportExportConstants.ALIAS_JSON_KEY).getAsString();
        String endpoint = certificate.getAsJsonObject().get(ImportExportConstants.ENDPOINT_JSON_KEY).getAsString();
        try {
            if (apiProvider.isCertificatePresent(tenantId, alias) || (
                    ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode() == (apiProvider
                            .addCertificate(APIUtil.replaceEmailDomainBack(importedApi.getId().getProviderName()),
                                    certificateContent, alias, endpoint)))) {
                apiProvider.updateCertificate(certificateContent, alias);
            }
        } catch (APIManagementException e) {
            log.error("Error while importing certificate endpoint [" + endpoint + " ]" + "alias [" + alias +
                    " ] tenant user [" + APIUtil.replaceEmailDomainBack(importedApi.getId().getProviderName())
                    + "]", e);
        }
    }

    /**
     * Import client certificates for Mutual SSL related configuration.
     *
     * @param pathToArchive Location of the extracted folder of the API
     * @param apiProvider   API Provider
     * @throws APIImportExportException
     */
    private static void addClientCertificates(String pathToArchive, APIProvider apiProvider, Boolean preserveProvider,
                                              String provider) throws APIManagementException {

        try {
            List<ClientCertificateDTO> certificateMetadataDTOS = retrieveClientCertificates(pathToArchive);
            for (ClientCertificateDTO certDTO : certificateMetadataDTOS) {
                APIIdentifier apiIdentifier = !preserveProvider ?
                        new APIIdentifier(provider, certDTO.getApiIdentifier().getApiName(),
                                certDTO.getApiIdentifier().getVersion()) :
                        certDTO.getApiIdentifier();
                apiProvider.addClientCertificate(APIUtil.replaceEmailDomainBack(provider), apiIdentifier,
                        certDTO.getCertificate(), certDTO.getAlias(), certDTO.getTierName());
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while importing client certificate", e);
        }
    }

    public static List<ClientCertificateDTO> retrieveClientCertificates(String pathToArchive)
            throws APIManagementException {

        String jsonContent = null;
        String pathToClientCertificatesDirectory =
                pathToArchive + File.separator + ImportExportConstants.CLIENT_CERTIFICATES_DIRECTORY;
        String pathToYamlFile = pathToClientCertificatesDirectory + ImportExportConstants.CLIENT_CERTIFICATE_FILE
                + ImportExportConstants.YAML_EXTENSION;
        String pathToJsonFile = pathToClientCertificatesDirectory + ImportExportConstants.CLIENT_CERTIFICATE_FILE
                + ImportExportConstants.JSON_EXTENSION;
        try {
            // try loading file as YAML
            if (CommonUtil.checkFileExistence(pathToYamlFile)) {
                log.debug("Found client certificate file " + pathToYamlFile);
                String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
                jsonContent = CommonUtil.yamlToJson(yamlContent);
            } else if (CommonUtil.checkFileExistence(pathToJsonFile)) {
                // load as a json fallback
                log.debug("Found client certificate file " + pathToJsonFile);
                jsonContent = FileUtils.readFileToString(new File(pathToJsonFile));
            }
            if (jsonContent == null) {
                log.debug("No client certificate file found to be added, skipping");
                return new ArrayList<>();
            }
            JsonElement configElement = new JsonParser().parse(jsonContent).getAsJsonObject().get(APIConstants.DATA);
            JsonArray modifiedCertificatesData = addFileContentToCertificates(configElement.getAsJsonArray(),
                    pathToClientCertificatesDirectory);

            Gson gson = new Gson();
            return gson.fromJson(modifiedCertificatesData, new TypeToken<ArrayList<ClientCertificateDTO>>() {
            }.getType());
        } catch (IOException e) {
            throw new APIManagementException("Error in reading certificates file", e);
        }
    }

    /**
     * This method adds API sequences to the imported API. If the sequence is a newly defined one, it is added.
     *
     * @param pathToArchive Location of the extracted folder of the API
     * @param importedApi   API
     * @param registry      Registry
     * @throws APIImportExportException If an error occurs while importing mediation logic
     */
    private static void addSOAPToREST(String pathToArchive, API importedApi, Registry registry)
            throws APIManagementException {

        List<SoapToRestMediationDto> soapToRestInMediationDtoList = retrieveSoapToRestFlowMediations(pathToArchive, IN);
        List<SoapToRestMediationDto> soapToRestOUTMediationDtoList = retrieveSoapToRestFlowMediations(pathToArchive,
                OUT);
        APIIdentifier apiId = importedApi.getId();
        String soapToRestLocationIn =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiId.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + apiId.getVersion() + RegistryConstants.PATH_SEPARATOR
                        + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_IN_RESOURCE;
        String soapToRestLocationOut =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiId.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + apiId.getVersion() + RegistryConstants.PATH_SEPARATOR
                        + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_OUT_RESOURCE;

        for (SoapToRestMediationDto soapToRestMediationDto : soapToRestInMediationDtoList) {
            importMediationLogic(soapToRestMediationDto, registry, soapToRestLocationIn);
        }
        for (SoapToRestMediationDto soapToRestMediationDto : soapToRestOUTMediationDtoList) {
            importMediationLogic(soapToRestMediationDto, registry, soapToRestLocationOut);
        }
    }

    public static List<SoapToRestMediationDto> retrieveSoapToRestFlowMediations(String pathToArchive, String type)
            throws APIManagementException {

        List<SoapToRestMediationDto> soapToRestMediationDtoList = new ArrayList<>();
        String fileLocation = null;
        if (IN.equals(type)) {
            fileLocation = pathToArchive + File.separator + SOAPTOREST + File.separator + IN;
        } else if (OUT.equals(type)) {
            fileLocation = pathToArchive + File.separator + SOAPTOREST + File.separator + OUT;
        }
        if (CommonUtil.checkFileExistence(fileLocation)) {
            Path flowDirectory = Paths.get(fileLocation);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(flowDirectory)) {
                for (Path file : stream) {
                    String fileName = file.getFileName().toString();
                    String method = "";
                    String resource = "";
                    if (fileName.split(".xml").length != 0) {
                        method =
                                fileName.split(".xml")[0].substring(file.getFileName().toString().lastIndexOf("_") + 1);
                        resource = fileName.substring(0, fileName.indexOf("_"));
                    }
                    try (InputStream inputFlowStream = new FileInputStream(file.toFile())) {
                        String content = IOUtils.toString(inputFlowStream);
                        SoapToRestMediationDto soapToRestMediationDto = new SoapToRestMediationDto(resource, method,
                                content);
                        soapToRestMediationDtoList.add(soapToRestMediationDto);
                    }
                }
            } catch (IOException e) {
                throw new APIManagementException("Error while reading mediation content", e);
            }
        }
        return soapToRestMediationDtoList;
    }

    /**
     * Method created to add inflow and outflow mediation logic.
     *
     * @param sequenceData       Inflow and outflow directory
     * @param registry           Registry
     * @param soapToRestLocation Folder location
     * @throws APIImportExportException If an error occurs while importing/storing SOAP to REST mediation logic
     */
    private static void importMediationLogic(SoapToRestMediationDto sequenceData, Registry registry,
                                             String soapToRestLocation)
            throws APIManagementException {

        String fileName = sequenceData.getResource().concat("_").concat(sequenceData.getMethod()).concat(".xml");
        try {
            byte[] inSeqData = sequenceData.getContent().getBytes();
            Resource inSeqResource = registry.newResource();
            inSeqResource.setContent(inSeqData);
            inSeqResource.addProperty(SOAPToRESTConstants.METHOD, sequenceData.getMethod());
            inSeqResource.setMediaType("text/xml");
            registry.put(soapToRestLocation + RegistryConstants.PATH_SEPARATOR + fileName, inSeqResource);

        } catch (DirectoryIteratorException e) {
            throw new APIManagementException("Error in importing SOAP to REST mediation logic", e);
        } catch (RegistryException e) {
            throw new APIManagementException("Error in storing imported SOAP to REST mediation logic", e);
        }
    }

    /**
     * This method returns the lifecycle action which can be used to transit from currentStatus to targetStatus.
     *
     * @param tenantDomain  Tenant domain
     * @param currentStatus Current status to do status transition
     * @param targetStatus  Target status to do status transition
     * @return Lifecycle action or null if target is not reachable
     * @throws APIImportExportException If getting lifecycle action failed
     */
    public static String getLifeCycleAction(String tenantDomain, String currentStatus, String targetStatus,
                                            APIProvider provider) throws APIManagementException {

        // No need to change the lifecycle if both the statuses are same
        if (StringUtils.equalsIgnoreCase(currentStatus, targetStatus)) {
            return null;
        }
        LifeCycle lifeCycle = new LifeCycle();
        // Parse DOM of APILifeCycle
        try {
            String data = provider.getLifecycleConfiguration(tenantDomain);
            DocumentBuilderFactory factory = APIUtil.getSecuredDocumentBuilder();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(inputStream);
            Element root = doc.getDocumentElement();

            // Get all nodes with state
            NodeList states = root.getElementsByTagName("state");
            int nStates = states.getLength();
            for (int i = 0; i < nStates; i++) {
                Node node = states.item(i);
                Node id = node.getAttributes().getNamedItem("id");
                if (id != null && !id.getNodeValue().isEmpty()) {
                    LifeCycleTransition lifeCycleTransition = new LifeCycleTransition();
                    NodeList transitions = node.getChildNodes();
                    int nTransitions = transitions.getLength();
                    for (int j = 0; j < nTransitions; j++) {
                        Node transition = transitions.item(j);
                        // Add transitions
                        if (ImportExportConstants.NODE_TRANSITION.equals(transition.getNodeName())) {
                            Node target = transition.getAttributes().getNamedItem("target");
                            Node action = transition.getAttributes().getNamedItem("event");
                            if (target != null && action != null) {
                                lifeCycleTransition
                                        .addTransition(target.getNodeValue().toLowerCase(), action.getNodeValue());
                            }
                        }
                    }
                    lifeCycle.addLifeCycleState(id.getNodeValue().toLowerCase(), lifeCycleTransition);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new APIManagementException("Error parsing APILifeCycle for tenant: " + tenantDomain, e);
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException(
                    "Error parsing unsupported encoding for APILifeCycle in tenant: " + tenantDomain, e);
        } catch (IOException e) {
            throw new APIManagementException("Error reading APILifeCycle for tenant: " + tenantDomain, e);
        }

        // Retrieve lifecycle action
        LifeCycleTransition transition = lifeCycle.getTransition(currentStatus.toLowerCase());
        if (transition != null) {
            return transition.getAction(targetStatus.toLowerCase());
        }
        return null;
    }

    /**
     * This method imports an API Product.
     *
     * @param extractedFolderPath Location of the extracted folder of the API Product
     * @param preserveProvider    Decision to keep or replace the provider
     * @param overwriteAPIProduct Whether to update the API Product or not
     * @param overwriteAPIs       Whether to update the dependent APIs or not
     * @param importAPIs          Whether to import the dependent APIs or not
     * @throws APIImportExportException If there is an error in importing an API
     */
    public static APIProduct importApiProduct(String extractedFolderPath, Boolean preserveProvider,
                                              Boolean rotateRevision, Boolean overwriteAPIProduct,
                                              Boolean overwriteAPIs, Boolean importAPIs, String[] tokenScopes)
            throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        String currentTenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(userName));
        APIProduct importedApiProduct = null;
        JsonArray deploymentInfoArray = null;

        try {
            JsonElement jsonObject = retrieveValidatedDTOObject(extractedFolderPath, preserveProvider, userName,
                    ImportExportConstants.TYPE_API_PRODUCT);
            APIProductDTO importedApiProductDTO = new Gson().fromJson(jsonObject, APIProductDTO.class);

            // If the provided dependent APIs params config is null, it means this happening when importing an API (not
            // because when importing a dependent API of an API Product). Hence, try to retrieve the definition from
            // the API folder path
            JsonObject paramsConfigObject = APIControllerUtil.resolveAPIControllerEnvParams(extractedFolderPath);
            // If above the params configurations are not null, then resolve those
            if (paramsConfigObject != null) {
                importedApiProductDTO = APIControllerUtil
                        .injectEnvParamsToAPIProduct(importedApiProductDTO, paramsConfigObject, extractedFolderPath);
                JsonElement deploymentsParam = paramsConfigObject.get(ImportExportConstants.DEPLOYMENT_ENVIRONMENTS);
                if (deploymentsParam != null && !deploymentsParam.isJsonNull()) {
                    deploymentInfoArray = deploymentsParam.getAsJsonArray();
                }
            }

            APIProvider apiProvider = RestApiCommonUtil.getProvider(importedApiProductDTO.getProvider());

            // Check whether the API resources are valid
            checkAPIProductResourcesValid(extractedFolderPath, userName, apiProvider, importedApiProductDTO,
                    preserveProvider);

            if (importAPIs) {
                // Import dependent APIs only if it is asked (the UUIDs of the dependent APIs will be updated here if a
                // fresh import happens)
                importedApiProductDTO = importDependentAPIs(extractedFolderPath, userName, preserveProvider,
                        apiProvider, overwriteAPIs, rotateRevision, importedApiProductDTO, tokenScopes);
            } else {
                // Even we do not import APIs, the UUIDs of the dependent APIs should be updated if the APIs are
                // already in the APIM
                importedApiProductDTO = updateDependentApiUuids(importedApiProductDTO, apiProvider,
                        currentTenantDomain);
            }

            APIProduct targetApiProduct = retrieveApiProductToOverwrite(importedApiProductDTO.getName(),
                    currentTenantDomain, apiProvider, Boolean.TRUE);

            // If the overwrite is set to true (which means an update), retrieve the existing API
            if (Boolean.TRUE.equals(overwriteAPIProduct) && targetApiProduct != null) {
                log.info("Existing API Product found, attempting to update it...");
                importedApiProduct = PublisherCommonUtils.updateApiProduct(targetApiProduct, importedApiProductDTO,
                        RestApiCommonUtil.getLoggedInUserProvider(), userName, currentTenantDomain);
            } else {
                if (targetApiProduct == null && Boolean.TRUE.equals(overwriteAPIProduct)) {
                    log.info("Cannot find : " + importedApiProductDTO.getName() + ". Creating it.");
                }
                importedApiProduct = PublisherCommonUtils
                        .addAPIProductWithGeneratedSwaggerDefinition(importedApiProductDTO,
                                importedApiProductDTO.getProvider());
            }

            // Add/update swagger of API Product
            importedApiProduct = updateApiProductSwagger(extractedFolderPath, importedApiProduct.getUuid(),
                    importedApiProduct, apiProvider, currentTenantDomain);

            // Since Image, documents and client certificates are optional, exceptions are logged and ignored in
            // implementation
            ApiTypeWrapper apiTypeWrapperWithUpdatedApiProduct = new ApiTypeWrapper(importedApiProduct);
            addThumbnailImage(extractedFolderPath, apiTypeWrapperWithUpdatedApiProduct, apiProvider);
            addDocumentation(extractedFolderPath, apiTypeWrapperWithUpdatedApiProduct, apiProvider);

            if (log.isDebugEnabled()) {
                log.debug("Mutual SSL enabled. Importing client certificates.");
            }
            addClientCertificates(extractedFolderPath, apiProvider, preserveProvider,
                    importedApiProduct.getId().getProviderName());

            if (deploymentInfoArray == null) {
                // If the params have not overwritten the deployment environments, yaml file will be read
                deploymentInfoArray = retrieveDeploymentLabelsFromArchive(extractedFolderPath, false);
            }
            List<APIRevisionDeployment> apiProductRevisionDeployments = getValidatedDeploymentsList(deploymentInfoArray,
                    currentTenantDomain, apiProvider);
            if (apiProductRevisionDeployments.size() > 0) {
                String importedAPIUuid = importedApiProduct.getUuid();
                String revisionId;
                APIRevision apiProductRevision = new APIRevision();
                apiProductRevision.setApiUUID(importedAPIUuid);
                apiProductRevision.setDescription("Revision created after importing the API Product");
                try {
                    revisionId = apiProvider.addAPIProductRevision(apiProductRevision);
                    if (log.isDebugEnabled()) {
                        log.debug("A new revision has been created for API Product " +
                                importedApiProduct.getId().getName() + "_"
                                + importedApiProduct.getId().getVersion() + " with ID: " + revisionId);
                    }
                } catch (APIManagementException e) {
                    //if the revision count is more than 5, addAPIProductRevision will throw an exception. If
                    // rotateRevision enabled, earliest revision will be deleted before creating a revision again
                    if (e.getErrorHandler().getErrorCode() ==
                            ExceptionCodes.from(ExceptionCodes.MAXIMUM_REVISIONS_REACHED).getErrorCode() &&
                            rotateRevision) {
                        String earliestRevisionUuid = apiProvider.getEarliestRevisionUUID(importedAPIUuid);
                        List<APIRevisionDeployment> deploymentsList =
                                apiProvider.getAPIRevisionDeploymentList(earliestRevisionUuid);
                        //if the earliest revision is already deployed in gateway environments, it will be undeployed
                        //before deleting
                        apiProvider
                                .undeployAPIProductRevisionDeployment(importedAPIUuid, earliestRevisionUuid,
                                        deploymentsList);
                        apiProvider.deleteAPIProductRevision(importedAPIUuid, earliestRevisionUuid);
                        revisionId = apiProvider.addAPIProductRevision(apiProductRevision);
                        if (log.isDebugEnabled()) {
                            log.debug("Revision ID: " + earliestRevisionUuid + " has been undeployed from " +
                                    deploymentsList.size() + " gateway environments and created a new revision ID: " +
                                    revisionId + " for API Product " + importedApiProduct.getId().getName() + "_" +
                                    importedApiProduct.getId().getVersion());
                        }
                    } else {
                        throw new APIManagementException(e);
                    }
                }

                //Once the new revision successfully created, artifacts will be deployed in mentioned gateway
                //environments
                apiProvider.deployAPIProductRevision(importedAPIUuid, revisionId, apiProductRevisionDeployments);
            } else {
                log.info("Valid deployment environments were not found for the imported artifact. Hence not deployed" +
                        " in any of the gateway environments.");
            }

            return importedApiProduct;
        } catch (IOException e) {
            // Error is logged and APIImportExportException is thrown because adding API Product and swagger are
            // mandatory steps
            throw new APIManagementException(
                    "Error while reading API Product meta information from path: " + extractedFolderPath, e);
        } catch (FaultGatewaysException e) {
            throw new APIManagementException(
                    "Error while updating API Product: " + importedApiProduct.getId().getName(), e);
        } catch (APIManagementException e) {
            String errorMessage = "Error while importing API Product: ";
            if (importedApiProduct != null) {
                errorMessage +=
                        importedApiProduct.getId().getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                                + importedApiProduct.getId().getVersion();
            }
            throw new APIManagementException(errorMessage + " " + e.getMessage(), e);
        }
    }

    /**
     * This method checks whether the resources in the API Product are valid.
     *
     * @param path          Location of the extracted folder of the API Product
     * @param currentUser   The current logged in user
     * @param apiProvider   API provider
     * @param apiProductDto API Product DTO
     * @throws IOException            If there is an error while reading an API file
     * @throws APIManagementException If failed to get the API Provider of an API,
     *                                or failed when checking the existence of an API
     */
    private static void checkAPIProductResourcesValid(String path, String currentUser, APIProvider apiProvider,
                                                      APIProductDTO apiProductDto, Boolean preserveProvider)
            throws IOException, APIManagementException {

        // Get dependent APIs in the API Product
        List<ProductAPIDTO> apis = apiProductDto.getApis();

        String apisDirectoryPath = path + File.separator + ImportExportConstants.APIS_DIRECTORY;
        File apisDirectory = new File(apisDirectoryPath);
        File[] apisDirectoryListing = apisDirectory.listFiles();

        if (apisDirectoryListing != null) {
            for (File apiDirectory : apisDirectoryListing) {
                String apiDirectoryPath =
                        path + File.separator + ImportExportConstants.APIS_DIRECTORY + File.separator + apiDirectory
                                .getName();
                JsonElement jsonObject = retrieveValidatedDTOObject(apiDirectoryPath, preserveProvider, currentUser,
                        ImportExportConstants.TYPE_API);
                APIDTO apiDto = new Gson().fromJson(jsonObject, APIDTO.class);
                String apiName = apiDto.getName();
                String apiVersion = apiDto.getVersion();

                String swaggerContent = loadSwaggerFile(apiDirectoryPath);
                APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerContent);
                Set<URITemplate> apiUriTemplates = apiDefinition.getURITemplates(swaggerContent);

                for (ProductAPIDTO apiFromProduct : apis) {
                    if (StringUtils.equals(apiFromProduct.getName(), apiName) && StringUtils
                            .equals(apiFromProduct.getVersion(), apiVersion)) {
                        List<APIOperationsDTO> invalidApiOperations = filterInvalidProductResources(
                                apiFromProduct.getOperations(), apiUriTemplates);

                        // If there are still product resources to be checked (which were not able to find in the
                        // dependent APIs inside the directory) check whether those are already inside APIM
                        if (!invalidApiOperations.isEmpty()) {
                            // Get the provider of the API if the API is in current user's tenant domain.
                            API api = retrieveApiToOverwrite(apiName, apiVersion,
                                    MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(currentUser)),
                                    apiProvider, Boolean.FALSE);
                            invalidApiOperations = filterInvalidProductResources(invalidApiOperations,
                                    api.getUriTemplates());
                        }

                        // invalidApiOperations is not empty means, at least one of the resources of the API
                        // Product does not have corresponding API resources neither inside the importing directory nor
                        // inside the APIM
                        if (!invalidApiOperations.isEmpty()) {
                            throw new APIMgtResourceNotFoundException(
                                    "Cannot find API resources for some API Product resources.");
                        }
                    }
                }
            }
        }
    }

    /**
     * This method filter the invalid resources in the API Product by matching with the URI Templates of a particular
     * dependent API.
     *
     * @param apiProductOperations Operations from API Product
     * @param apiUriTemplates      URI Templates of the dependent API
     *                             (either inside the import directory or already inside the APIM)
     * @return Invalid API operations
     */
    private static List<APIOperationsDTO> filterInvalidProductResources(List<APIOperationsDTO> apiProductOperations,
                                                                        Set<URITemplate> apiUriTemplates) {

        List<APIOperationsDTO> apiOperations = new ArrayList<>(apiProductOperations);
        for (URITemplate apiUriTemplate : apiUriTemplates) {
            // If the URI Template is Available in the API, remove it from the list since it is valid
            apiOperations.removeIf(
                    apiOperation -> StringUtils.equals(apiOperation.getVerb(), apiUriTemplate.getHTTPVerb())
                            && StringUtils.equals(apiOperation.getTarget(), apiUriTemplate.getUriTemplate()));
        }
        return apiOperations;
    }

    /**
     * This method imports dependent APIs of the API Product.
     *
     * @param path                     Location of the extracted folder of the API Product
     * @param currentUser              The current logged in user
     * @param isDefaultProviderAllowed Decision to keep or replace the provider
     * @param apiProvider              API provider
     * @param overwriteAPIs            Whether to overwrite the APIs or not
     * @param apiProductDto            API Product DTO
     * @param tokenScopes              Scopes of the token
     * @return Modified API Product DTO with the correct API UUIDs
     * @throws IOException              If there is an error while reading an API file
     * @throws APIImportExportException If there is an error in importing an API
     * @throws APIManagementException   If failed to get the API Provider of an API, or failed when
     *                                  checking the existence of an API
     */
    private static APIProductDTO importDependentAPIs(String path, String currentUser, boolean isDefaultProviderAllowed,
                                                     APIProvider apiProvider, boolean overwriteAPIs,
                                                     Boolean rotateRevision, APIProductDTO apiProductDto,
                                                     String[] tokenScopes) throws IOException, APIManagementException {

        JsonObject dependentAPIParamsConfigObject = null;
        // Retrieve the dependent APIs param configurations from the params file of the API Product
        JsonObject dependentAPIsParams = APIControllerUtil.getDependentAPIsParams(path);
        String apisDirectoryPath = path + File.separator + ImportExportConstants.APIS_DIRECTORY;
        File apisDirectory = new File(apisDirectoryPath);
        File[] apisDirectoryListing = apisDirectory.listFiles();

        if (apisDirectoryListing != null) {
            for (File apiDirectory : apisDirectoryListing) {
                String apiDirectoryPath =
                        path + File.separator + ImportExportConstants.APIS_DIRECTORY + File.separator + apiDirectory
                                .getName();

                // If the param configurations of the dependent APIs are available, the configurations of the current
                // API in the API directory will be retrieved if available
                if (dependentAPIsParams != null) {
                    dependentAPIParamsConfigObject = APIControllerUtil
                            .getDependentAPIParams(dependentAPIsParams, apiDirectory.getName());
                    // If the "certificates" directory is specified, copy it inside Deployment directory of the
                    // dependent API since there may be certificates required for APIs
                    String deploymentCertificatesDirectoryPath = path + ImportExportConstants.DEPLOYMENT_DIRECTORY
                            + ImportExportConstants.CERTIFICATE_DIRECTORY;
                    if (CommonUtil.checkFileExistence(deploymentCertificatesDirectoryPath)) {
                        try {
                            CommonUtil.copyDirectory(deploymentCertificatesDirectoryPath,
                                    apiDirectoryPath + ImportExportConstants.DEPLOYMENT_DIRECTORY
                                            + ImportExportConstants.CERTIFICATE_DIRECTORY);
                        } catch (APIImportExportException e) {
                            throw new APIManagementException(
                                    "Error while copying the directory " + deploymentCertificatesDirectoryPath, e);
                        }
                    }
                }

                JsonElement jsonObject = retrieveValidatedDTOObject(apiDirectoryPath, isDefaultProviderAllowed,
                        currentUser, ImportExportConstants.TYPE_API);
                APIDTO apiDtoToImport = new Gson().fromJson(jsonObject, APIDTO.class);
                API importedApi = null;
                String apiName = apiDtoToImport.getName();
                String apiVersion = apiDtoToImport.getVersion();

                if (isDefaultProviderAllowed) {
                    APIIdentifier apiIdentifier = new APIIdentifier(
                            APIUtil.replaceEmailDomain(apiDtoToImport.getProvider()), apiName, apiVersion);

                    // Checking whether the API exists
                    if (apiProvider.isAPIAvailable(apiIdentifier)) {
                        // If the API is already imported, update it if the overWriteAPIs flag is specified,
                        // otherwise do not update the API. (Just skip it)
                        if (Boolean.TRUE.equals(overwriteAPIs)) {
                            importedApi = importApi(apiDirectoryPath, apiDtoToImport, isDefaultProviderAllowed,
                                    rotateRevision, Boolean.TRUE, Boolean.TRUE, tokenScopes,
                                    dependentAPIParamsConfigObject);
                        }
                    } else {
                        // If the API is not already imported, import it
                        importedApi = importApi(apiDirectoryPath, apiDtoToImport, isDefaultProviderAllowed,
                                rotateRevision, Boolean.FALSE, Boolean.TRUE, tokenScopes,
                                dependentAPIParamsConfigObject);
                    }
                } else {
                    // Retrieve the current tenant domain of the logged in user
                    String currentTenantDomain = MultitenantUtils
                            .getTenantDomain(APIUtil.replaceEmailDomainBack(currentUser));

                    // Get the provider of the API if the API is in current user's tenant domain.
                    String apiProviderInCurrentTenantDomain = APIUtil
                            .getAPIProviderFromAPINameVersionTenant(apiName, apiVersion, currentTenantDomain);

                    if (StringUtils.isBlank(apiProviderInCurrentTenantDomain)) {
                        // If there is no API in the current tenant domain (which means the provider name is blank)
                        // then the API should be imported freshly
                        importedApi = importApi(apiDirectoryPath, apiDtoToImport, isDefaultProviderAllowed,
                                rotateRevision, Boolean.FALSE, Boolean.TRUE, tokenScopes,
                                dependentAPIParamsConfigObject);
                    } else {
                        // If there is an API already in the current tenant domain, update it if the overWriteAPIs
                        // flag is specified,
                        // otherwise do not import/update the API. (Just skip it)
                        if (Boolean.TRUE.equals(overwriteAPIs)) {
                            importedApi = importApi(apiDirectoryPath, apiDtoToImport, isDefaultProviderAllowed,
                                    rotateRevision, Boolean.TRUE, Boolean.TRUE, tokenScopes,
                                    dependentAPIParamsConfigObject);
                        }
                    }
                }
                if (importedApi == null) {
                    // Retrieve the API from the environment (This happens when you have not specified
                    // the overwrite flag, so that we should retrieve the API from inside)
                    importedApi = retrieveApiToOverwrite(apiDtoToImport.getName(), apiDtoToImport.getVersion(),
                            MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(currentUser)), apiProvider,
                            Boolean.FALSE);
                }
                updateApiUuidInApiProduct(apiProductDto, importedApi);
            }
        } else {
            String msg = "No dependent APIs supplied. Continuing ...";
            log.info(msg);
        }
        return apiProductDto;
    }

    /**
     * This method updates the UUID of the dependent API in an API Product.
     *
     * @param apiProductDto API Product DTO
     * @param importedApi   Imported API
     */
    private static APIProductDTO updateApiUuidInApiProduct(APIProductDTO apiProductDto, API importedApi) {

        APIIdentifier importedApiIdentifier = importedApi.getId();
        List<ProductAPIDTO> apis = apiProductDto.getApis();
        for (ProductAPIDTO api : apis) {
            if (StringUtils.equals(api.getName(), importedApiIdentifier.getName()) && StringUtils
                    .equals(api.getVersion(), importedApiIdentifier.getVersion())) {
                api.setApiId(importedApi.getUuid());
                break;
            }
        }
        return apiProductDto;
    }

    /**
     * This method updates the UUIDs of dependent APIs, when the dependent APIs are already inside APIM.
     *
     * @param importedApiProductDtO API Product DTO
     * @param apiProvider           API Provider
     * @param currentTenantDomain   Current tenant domain
     * @throws APIManagementException If failed failed when checking the existence of an API
     */
    private static APIProductDTO updateDependentApiUuids(APIProductDTO importedApiProductDtO, APIProvider apiProvider,
                                                         String currentTenantDomain) throws APIManagementException {

        List<ProductAPIDTO> apis = importedApiProductDtO.getApis();
        for (ProductAPIDTO api : apis) {
            API targetApi = retrieveApiToOverwrite(api.getName(), api.getVersion(), currentTenantDomain, apiProvider,
                    Boolean.FALSE);
            if (targetApi != null) {
                api.setApiId(targetApi.getUuid());
            }
        }
        return importedApiProductDtO;
    }

    /**
     * This method retrieves an API Product to overwrite in the current tenant domain.
     *
     * @param apiProductName      API Product Name
     * @param currentTenantDomain Current tenant domain
     * @param apiProvider         API Provider
     * @param ignoreAndImport     This should be true if the exception should be ignored
     * @throws APIManagementException If an error occurs when retrieving the API to overwrite
     */
    private static APIProduct retrieveApiProductToOverwrite(String apiProductName, String currentTenantDomain,
                                                            APIProvider apiProvider, Boolean ignoreAndImport)
            throws APIManagementException {

        String provider = APIUtil.getAPIProviderFromAPINameVersionTenant(apiProductName,
                ImportExportConstants.DEFAULT_API_PRODUCT_VERSION, currentTenantDomain);
        APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(APIUtil.replaceEmailDomain(provider),
                apiProductName, ImportExportConstants.DEFAULT_API_PRODUCT_VERSION);

        // Checking whether the API exists
        if (!apiProvider.isAPIProductAvailable(apiProductIdentifier)) {
            if (ignoreAndImport) {
                return null;
            }
            throw new APIMgtResourceNotFoundException(
                    "Error occurred while retrieving the API Product. API Product: " + apiProductName
                            + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                            + ImportExportConstants.DEFAULT_API_PRODUCT_VERSION + " not found");
        }
        return apiProvider.getAPIProduct(apiProductIdentifier);
    }

    /**
     * This method updates the API Product and the swagger with the correct scopes.
     *
     * @param pathToArchive      Path to the extracted folder
     * @param importedApiProduct Imported API Product
     * @param apiProvider        API Provider
     * @throws APIManagementException If an error occurs when retrieving the parser and updating the API Product
     * @throws FaultGatewaysException If an error occurs when updating the API to overwrite
     * @throws IOException            If an error occurs when loading the swagger file
     */
    private static APIProduct updateApiProductSwagger(String pathToArchive, String apiProductId, APIProduct
            importedApiProduct, APIProvider apiProvider, String orgId)
            throws APIManagementException, FaultGatewaysException, IOException {

        String swaggerContent = loadSwaggerFile(pathToArchive);

        // Load required properties from swagger to the API Product
        APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerContent);
        Set<Scope> scopes = apiDefinition.getScopes(swaggerContent);
        importedApiProduct.setScopes(scopes);

        // This is required to make scopes get effected
        Map<API, List<APIProductResource>> apiToProductResourceMapping = apiProvider
                .updateAPIProduct(importedApiProduct);
        apiProvider.updateAPIProductSwagger(apiProductId, apiToProductResourceMapping, importedApiProduct, orgId);
        return importedApiProduct;
    }
}
