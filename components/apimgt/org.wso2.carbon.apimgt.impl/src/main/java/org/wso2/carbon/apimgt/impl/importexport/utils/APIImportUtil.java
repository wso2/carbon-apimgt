/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.lifecycle.LifeCycle;
import org.wso2.carbon.apimgt.impl.importexport.lifecycle.LifeCycleTransition;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
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
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class provides the functions utilized to import an API from an API archive.
 */
public final class APIImportUtil {

    private static final Log log = LogFactory.getLog(APIImportUtil.class);
    private static final String IN = "in";
    private static final String OUT = "out";
    private static final String SOAPTOREST = "SoapToRest";

    /**
     * This method returns the lifecycle action which can be used to transit from currentStatus to targetStatus.
     *
     * @param tenantDomain  Tenant domain
     * @param currentStatus Current status to do status transition
     * @param targetStatus  Target status to do status transition
     * @return Lifecycle action or null if target is not reachable
     * @throws APIImportExportException If getting lifecycle action failed
     */
    private static String getLifeCycleAction(String tenantDomain, String currentStatus, String targetStatus,
            APIProvider provider) throws APIImportExportException {

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
                        if (APIImportExportConstants.NODE_TRANSITION.equals(transition.getNodeName())) {
                            Node target = transition.getAttributes().getNamedItem("target");
                            Node action = transition.getAttributes().getNamedItem("event");
                            if (target != null && action != null) {
                                lifeCycleTransition.addTransition(target.getNodeValue().toLowerCase(), action.getNodeValue());
                            }
                        }
                    }
                    lifeCycle.addLifeCycleState(id.getNodeValue().toLowerCase(), lifeCycleTransition);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            String errorMessage = "Error parsing APILifeCycle for tenant: " + tenantDomain;
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Error parsing unsupported encoding for APILifeCycle in tenant: " + tenantDomain;
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error reading APILifeCycle for tenant: " + tenantDomain;
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (APIManagementException e) {
            String errorMessage = "Error retrieving APILifeCycle for tenant: " + tenantDomain;
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }

        // Retrieve lifecycle action
        LifeCycleTransition transition = lifeCycle.getTransition(currentStatus.toLowerCase());
        if (transition != null) {
            return transition.getAction(targetStatus.toLowerCase());
        }
        return null;
    }

    /**
     * Load a swagger document from archive. This method lookup for swagger as YAML or JSON.
     *
     * @param pathToArchive Path to archive
     * @return Swagger content as a JSON
     * @throws IOException When swagger document not found
     */
    private static String loadSwaggerFile(String pathToArchive) throws IOException {

        if (CommonUtil.checkFileExistence(pathToArchive + APIImportExportConstants.YAML_SWAGGER_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug("Found swagger file " + pathToArchive + APIImportExportConstants.YAML_SWAGGER_DEFINITION_LOCATION);
            }
            String yamlContent = FileUtils.readFileToString(
                    new File(pathToArchive + APIImportExportConstants.YAML_SWAGGER_DEFINITION_LOCATION));
            return CommonUtil.yamlToJson(yamlContent);
        } else if (CommonUtil.checkFileExistence(pathToArchive + APIImportExportConstants.JSON_SWAGGER_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug("Found swagger file " + pathToArchive + APIImportExportConstants.JSON_SWAGGER_DEFINITION_LOCATION);
            }
            return FileUtils.readFileToString(
                    new File(pathToArchive + APIImportExportConstants.JSON_SWAGGER_DEFINITION_LOCATION));
        }
        throw new IOException("Missing swagger file. Either swagger.json or swagger.yaml should present");
    }

    /**
     * Load the graphQL schema definition from archive.
     *
     * @param pathToArchive Path to archive
     * @return Schema definition content
     * @throws IOException When SDL file not found
     */
    private static String loadGraphqlSDLFile(String pathToArchive) throws IOException {
        if (CommonUtil.checkFileExistence(pathToArchive + APIImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION)) {
            if (log.isDebugEnabled()) {
                log.debug("Found graphQL sdl file " + pathToArchive
                        + APIImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION);
            }
            return FileUtils.readFileToString(
                    new File(pathToArchive, APIImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION));
        }
        throw new IOException("Missing graphQL schema definition file. schema.graphql should be present.");
    }

    /**
     * This method imports an API.
     *
     * @param pathToArchive            location of the extracted folder of the API
     * @param currentUser              the current logged in user
     * @param isDefaultProviderAllowed decision to keep or replace the provider
     * @throws APIImportExportException if there is an error in importing an API
     */
    public static void importAPI(String pathToArchive, String currentUser, boolean isDefaultProviderAllowed,
            APIProvider apiProvider, Boolean overwrite)
            throws APIImportExportException {

        String jsonContent = null;
        API importedApi = null;
        API targetApi; //target API when overwrite is true
        String prevProvider;
        String apiName;
        String apiVersion;
        String currentTenantDomain;
        String currentStatus;
        String targetStatus;
        String lifecycleAction = null;
        String pathToYamlFile = pathToArchive + APIImportExportConstants.YAML_API_FILE_LOCATION;
        String pathToJsonFile = pathToArchive + APIImportExportConstants.JSON_API_FILE_LOCATION;
        UserRegistry registry;
        int tenantId = APIUtil.getTenantId(currentUser);

        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);

            // load yaml representation first if it is present
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
            if (jsonContent == null) {
                throw new IOException("Cannot find API definition. api.json or api.yaml should present");
            }
            JsonElement configElement = new JsonParser().parse(jsonContent);
            JsonObject configObject = configElement.getAsJsonObject();

            //locate the "providerName" within the "id" and set it as the current user
            JsonObject apiId = configObject.getAsJsonObject(APIImportExportConstants.ID_ELEMENT);

            prevProvider = apiId.get(APIImportExportConstants.PROVIDER_ELEMENT).getAsString();
            apiName = apiId.get(APIImportExportConstants.NAME_ELEMENT).getAsString();
            apiVersion = apiId.get(APIImportExportConstants.VERSION_ELEMENT).getAsString();
            // Remove spaces of API Name/version if present
            if (apiName != null && apiVersion != null) {
                apiId.addProperty(APIImportExportConstants.NAME_ELEMENT,
                        apiName = apiName.replace(" ", ""));
                apiId.addProperty(APIImportExportConstants.VERSION_ELEMENT,
                        apiVersion = apiVersion.replace(" ", ""));
            } else {
                throw new IOException("API Name (id.apiName) and Version (id.version) must be provided in api.yaml");
            }

            String prevTenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(prevProvider));
            currentTenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(currentUser));

            // If the original provider is preserved,
            if (isDefaultProviderAllowed) {
                if (!StringUtils.equals(prevTenantDomain, currentTenantDomain)) {
                    String errorMessage = "Tenant mismatch! Please enable preserveProvider property "
                            + "for cross tenant API Import.";
                    throw new APIMgtAuthorizationFailedException(errorMessage);
                }
                importedApi = new Gson().fromJson(configElement, API.class);
            } else {
                String prevProviderWithDomain = APIUtil.replaceEmailDomain(prevProvider);
                String currentUserWithDomain = APIUtil.replaceEmailDomain(currentUser);
                apiId.addProperty(APIImportExportConstants.PROVIDER_ELEMENT, currentUserWithDomain);
                if (configObject.get(APIImportExportConstants.WSDL_URL) != null) {
                    // If original provider is not preserved, replace provider name in the wsdl URL
                    // with the current user with domain name
                    configObject.addProperty(APIImportExportConstants.WSDL_URL,
                            configObject.get(APIImportExportConstants.WSDL_URL).getAsString()
                                    .replace(prevProviderWithDomain, currentUserWithDomain));
                }

                importedApi = new Gson().fromJson(configElement, API.class);
                //Replace context to match with current provider
                setCurrentProviderToAPIProperties(importedApi, currentTenantDomain, prevTenantDomain);
            }

            // Store imported API status
            targetStatus = importedApi.getStatus();
            if (Boolean.TRUE.equals(overwrite)) {
                String provider = APIUtil
                        .getAPIProviderFromAPINameVersionTenant(apiName, apiVersion, currentTenantDomain);
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), apiName,
                        apiVersion);
                // Checking whether the API exists
                if (!apiProvider.isAPIAvailable(apiIdentifier)) {
                    String errorMessage = "Error occurred while updating. API: " + apiName + StringUtils.SPACE
                            + APIConstants.API_DATA_VERSION + ": " + apiVersion + " not found";
                    throw new APIMgtResourceNotFoundException(errorMessage);
                }
                targetApi = apiProvider.getAPI(apiIdentifier);
                // Store target API status
                currentStatus = targetApi.getStatus();
            } else {
                if (apiProvider.isAPIAvailable(importedApi.getId())
                        || apiProvider.isApiNameWithDifferentCaseExist(apiName)) {
                    String errorMessage = "Error occurred while adding the API. A duplicate API already exists " +
                            "for " + importedApi.getId().getApiName() + '-' + importedApi.getId().getVersion();
                    throw new APIMgtResourceAlreadyExistsException(errorMessage);
                }

                if (apiProvider.isContextExist(importedApi.getContext())) {
                    String errMsg = "Error occurred while adding the API [" + importedApi.getId().getApiName()
                            + '-' + importedApi.getId().getVersion() + "]. A duplicate context["
                            + importedApi.getContext() + "] already exists";
                    throw new APIMgtResourceAlreadyExistsException(errMsg);
                }

                // Initialize to CREATED when import
                currentStatus = APIStatus.CREATED.toString();
            }
            //set the status of imported API to CREATED (importing API) or current status of target API when updating
            importedApi.setStatus(currentStatus);

            // check whether targetStatus is reachable from current status, if not throw an exception
            if (!currentStatus.equals(targetStatus)) {
                lifecycleAction = getLifeCycleAction(currentTenantDomain, currentStatus, targetStatus, apiProvider);
                if (lifecycleAction == null) {
                    String errMsg = "Error occurred while importing the API. " + targetStatus + " is not reachable from "
                            + currentStatus;
                    log.error(errMsg);
                    throw new APIImportExportException(errMsg);
                }
            }

            Set<Tier> allowedTiers;
            Set<Tier> unsupportedTiersList;
            allowedTiers = apiProvider.getTiers();

            if (!(allowedTiers.isEmpty())) {
                unsupportedTiersList = Sets.difference(importedApi.getAvailableTiers(), allowedTiers);

                //If at least one unsupported tier is found, it should be removed before adding API
                if (!(unsupportedTiersList.isEmpty())) {
                    //Process is continued with a warning and only supported tiers are added to the importer API
                    unsupportedTiersList.forEach(unsupportedTier ->
                            log.warn("Tier name : " + unsupportedTier.getName() + " is not supported."));
                    //Remove the unsupported tiers before adding the API
                    importedApi.removeAvailableTiers(unsupportedTiersList);
                }
            }
            if (Boolean.FALSE.equals(overwrite)) {
                //Add API in CREATED state
                importedApi.setAsDefaultVersion(false);
                apiProvider.addAPI(importedApi);
            }

            //Swagger definition will only be available of API type HTTP. Web socket API does not have it.
            if (!APIConstants.APITransportType.WS.toString().equalsIgnoreCase(importedApi.getType())) {
                String swaggerContent = loadSwaggerFile(pathToArchive);

                // Check whether any of the resources should be removed from the API when updating,
                // that has already been used in API Products
                List<APIResource> resourcesToRemove = apiProvider.getResourcesToBeRemovedFromAPIProducts(importedApi.getId(),
                        swaggerContent);
                // Do not allow to remove resources from API Products, hence throw an exception
                if (!resourcesToRemove.isEmpty()) {
                    throw new APIImportExportException("Cannot remove following resource paths " +
                            resourcesToRemove.toString() + " because they are used by one or more API Products");
                }
                //preProcess swagger definition
                swaggerContent = OASParserUtil.preProcess(swaggerContent);

                addSwaggerDefinition(importedApi.getId(), swaggerContent, apiProvider);
                //If graphQL API, import graphQL schema definition to registry
                if (StringUtils.equals(importedApi.getType(), APIConstants.APITransportType.GRAPHQL.toString())) {
                    String schemaDefinition = loadGraphqlSDLFile(pathToArchive);
                    addGraphqlSchemaDefinition(importedApi, schemaDefinition, apiProvider);
                } else {
                    //Load required properties from swagger to the API
                    APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerContent);
                    Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(swaggerContent);
                    for (URITemplate uriTemplate : uriTemplates) {
                        Scope scope = uriTemplate.getScope();
                        if (scope != null && !(APIUtil.isWhiteListedScope(scope.getKey()))
                                && apiProvider.isScopeKeyAssigned(importedApi.getId(), scope.getKey(), tenantId)) {
                            String errorMessage =
                                    "Error in adding API. Scope " + scope.getKey() + " is already assigned by another API.";
                            log.error(errorMessage);
                            throw new APIImportExportException(errorMessage);
                        }
                    }
                    importedApi.setUriTemplates(uriTemplates);
                    Set<Scope> scopes = apiDefinition.getScopes(swaggerContent);
                    importedApi.setScopes(scopes);
                }
            }
            // This is required to make url templates and scopes get effected
            apiProvider.updateAPI(importedApi);

            //Since Image, documents, sequences and WSDL are optional, exceptions are logged and ignored in implementation
            addAPIImage(pathToArchive, importedApi, apiProvider);
            addAPIDocuments(pathToArchive, importedApi, apiProvider);
            addAPISequences(pathToArchive, importedApi, registry);
            addAPISpecificSequences(pathToArchive, importedApi, registry);
            addAPIWsdl(pathToArchive, importedApi, apiProvider, registry);
            addEndpointCertificates(pathToArchive, importedApi, apiProvider, tenantId);
            addSOAPToREST(pathToArchive, importedApi, registry);

            if (apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Importing client certificates.");
                }
                addClientCertificates(pathToArchive, apiProvider);
            }

            // Change API lifecycle if state transition is required
            if (StringUtils.isNotEmpty(lifecycleAction)) {
                log.info("Changing lifecycle from " + currentStatus + " to " + targetStatus);
                apiProvider.changeAPILCCheckListItems(importedApi.getId(),
                        APIImportExportConstants.REFER_REQUIRE_RE_SUBSCRIPTION_CHECK_ITEM, true);
                apiProvider.changeLifeCycleStatus(importedApi.getId(), lifecycleAction);
                //Change the status of the imported API to targetStatus
                importedApi.setStatus(targetStatus);
            }
        } catch (IOException e) {
            //Error is logged and APIImportExportException is thrown because adding API and swagger are mandatory steps
            String errorMessage = "Error while reading API meta information from path: " + pathToArchive;
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API: " + importedApi.getId().getApiName();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while getting governance registry for tenant: " + tenantId;
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (APIManagementException e) {
            String errorMessage = "Error while importing API: ";
            if (importedApi != null) {
                errorMessage += importedApi.getId().getApiName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION
                        + ": " + importedApi.getId().getVersion();
            }
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Replace original provider name from imported API properties with the logged in username
     * This method is used when "preserveProvider" property is set to false.
     *
     * @param importedApi    Imported API
     * @param currentDomain  current domain name
     * @param previousDomain original domain name
     */
    private static void setCurrentProviderToAPIProperties(API importedApi, String currentDomain, String previousDomain) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(currentDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(previousDomain)) {
            importedApi.setContext(importedApi.getContext().replace(APIConstants.TENANT_PREFIX + previousDomain,
                    StringUtils.EMPTY));
            importedApi.setContextTemplate(importedApi.getContextTemplate().replace(APIConstants.TENANT_PREFIX
                    + previousDomain, StringUtils.EMPTY));
        } else if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(currentDomain) &&
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(previousDomain)) {
            importedApi.setContext(APIConstants.TENANT_PREFIX + currentDomain + importedApi.getContext());
            importedApi.setContextTemplate(APIConstants.TENANT_PREFIX + currentDomain + importedApi.getContextTemplate());
        } else if (!StringUtils.equalsIgnoreCase(currentDomain, previousDomain)) {
            importedApi.setContext(importedApi.getContext().replace(previousDomain, currentDomain));
            importedApi.setContextTemplate(importedApi.getContextTemplate().replace(previousDomain, currentDomain));
        }
    }

    /**
     * This method update the API with the icon to be displayed at the API store.
     *
     * @param pathToArchive location of the extracted folder of the API
     * @param importedApi   the imported API object
     */
    private static void addAPIImage(String pathToArchive, API importedApi, APIProvider apiProvider) {

        //Adding image icon to the API if there is any
        File imageFolder = new File(pathToArchive + APIImportExportConstants.IMAGE_FILE_LOCATION);
        File[] fileArray = imageFolder.listFiles();
        if (imageFolder.isDirectory() && fileArray != null) {
            //This loop locates the icon of the API
            for (File imageFile : fileArray) {
                if (imageFile != null && imageFile.getName().contains(APIConstants.API_ICON_IMAGE)) {
                    updateAPIWithThumbnail(imageFile, importedApi, apiProvider);
                    //the loop is terminated after successfully locating the icon
                    break;
                }
            }
        }
    }

    /**
     * This method update the API with the thumbnail image from imported API.
     *
     * @param imageFile   Image file
     * @param importedApi API to update
     * @param apiProvider API Provider
     */
    private static void updateAPIWithThumbnail(File imageFile, API importedApi, APIProvider apiProvider) {

        APIIdentifier apiIdentifier = importedApi.getId();
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
            String thumbPath = APIUtil.getIconPath(apiIdentifier);
            String thumbnailUrl = apiProvider.addResourceFile(importedApi.getId(), thumbPath, apiImage);
            importedApi.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl,
                    apiIdentifier.getProviderName()));
            APIUtil.setResourcePermissions(apiIdentifier.getProviderName(), null, null, thumbPath);
            apiProvider.updateAPI(importedApi);
        } catch (FaultGatewaysException e) {
            //This is logged and process is continued because icon is optional for an API
            log.error("Failed to update API after adding icon. ", e);
        } catch (APIManagementException e) {
            log.error("Failed to add icon to the API: " + apiIdentifier.getApiName(), e);
        } catch (FileNotFoundException e) {
            log.error("Icon for API: " + apiIdentifier.getApiName() + " is not found.", e);
        } catch (IOException e) {
            log.error("Failed to import icon for API:" + apiIdentifier.getApiName());
        }
    }

    /**
     * This method adds the documents to the imported API.
     *
     * @param pathToArchive location of the extracted folder of the API
     * @param importedApi   the imported API object
     */
    private static void addAPIDocuments(String pathToArchive, API importedApi, APIProvider apiProvider) {

        String jsonContent = null;
        String pathToYamlFile = pathToArchive + APIImportExportConstants.YAML_DOCUMENT_FILE_LOCATION;
        String pathToJsonFile = pathToArchive + APIImportExportConstants.JSON_DOCUMENT_FILE_LOCATION;
        APIIdentifier apiIdentifier = importedApi.getId();
        Documentation[] documentations;
        String docDirectoryPath = pathToArchive + File.separator + APIImportExportConstants.DOCUMENT_DIRECTORY;
        try {
            //remove all documents associated with the API before update
            List<Documentation> documents = apiProvider.getAllDocumentation(apiIdentifier);
            if (documents != null) {
                for (Documentation documentation : documents) {
                    apiProvider.removeDocumentation(apiIdentifier, documentation.getId());
                }
            }
            //load document file if exists
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
            if (jsonContent == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No document definition found, Skipping documentation import for API: "
                            + importedApi.getId().getApiName());
                }
                return;
            }

            documentations = new Gson().fromJson(jsonContent, Documentation[].class);
            //For each type of document separate action is performed
            for (Documentation doc : documentations) {

                String docSourceType = doc.getSourceType().toString();
                boolean docContentExists = Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(docSourceType)
                        || Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(docSourceType);
                String inlineContent = null;

                if (docContentExists) {
                    try (FileInputStream inputStream = new FileInputStream(docDirectoryPath + File.separator
                            + APIImportExportConstants.INLINE_DOCUMENT_DIRECTORY + File.separator + doc.getName())) {

                        inlineContent = IOUtils.toString(inputStream, APIImportExportConstants.CHARSET);
                    }
                } else if (APIImportExportConstants.FILE_DOC_TYPE.equalsIgnoreCase(docSourceType)) {
                    String filePath = doc.getFilePath();
                    try (FileInputStream inputStream = new FileInputStream(docDirectoryPath + File.separator
                            + APIImportExportConstants.FILE_DOCUMENT_DIRECTORY + File.separator + filePath)) {
                        String docExtension = FilenameUtils.getExtension(pathToArchive + File.separator
                                + APIImportExportConstants.DOCUMENT_DIRECTORY + File.separator + filePath);
                        ResourceFile apiDocument = new ResourceFile(inputStream, docExtension);
                        String visibleRolesList = importedApi.getVisibleRoles();
                        String[] visibleRoles = new String[0];
                        if (visibleRolesList != null) {
                            visibleRoles = visibleRolesList.split(",");
                        }
                        String filePathDoc = APIUtil.getDocumentationFilePath(apiIdentifier, filePath);
                        APIUtil.setResourcePermissions(importedApi.getId().getProviderName(),
                                importedApi.getVisibility(), visibleRoles, filePathDoc);
                        doc.setFilePath(apiProvider.addResourceFile(importedApi.getId(), filePathDoc, apiDocument));
                    } catch (FileNotFoundException e) {
                        //this error is logged and ignored because documents are optional in an API
                        log.error("Failed to locate the document files of the API: " + apiIdentifier.getApiName(), e);
                        continue;
                    }
                }

                //Add documentation accordingly.
                apiProvider.addDocumentation(apiIdentifier, doc);

                if (docContentExists) {
                    //APIProvider.addDocumentationContent method handles both create/update documentation content
                    apiProvider.addDocumentationContent(importedApi, doc.getName(), inlineContent);
                }
            }
        } catch (FileNotFoundException e) {
            //this error is logged and ignored because documents are optional in an API
            log.error("Failed to locate the document files of the API: " + apiIdentifier.getApiName(), e);
        } catch (APIManagementException | IOException e) {
            //this error is logged and ignored because documents are optional in an API
            log.error("Failed to add Documentations to API: " + apiIdentifier.getApiName(), e);
        }
    }

    /**
     * This method adds API sequences to the imported API. If the sequence is a newly defined one, it is added.
     *
     * @param pathToArchive location of the extracted folder of the API
     * @param importedApi   the imported API object
     */
    private static void addAPISequences(String pathToArchive, API importedApi, Registry registry) {

        String inSequenceFileName = importedApi.getInSequence() + APIConstants.XML_EXTENSION;
        String inSequenceFileLocation = pathToArchive + APIImportExportConstants.IN_SEQUENCE_LOCATION
                + inSequenceFileName;
        String regResourcePath;

        //Adding in-sequence, if any
        if (CommonUtil.checkFileExistence(inSequenceFileLocation)) {
            regResourcePath = APIConstants.API_CUSTOM_INSEQUENCE_LOCATION + inSequenceFileName;
            addSequenceToRegistry(false, registry, inSequenceFileLocation, regResourcePath);
        }

        String outSequenceFileName = importedApi.getOutSequence() + APIConstants.XML_EXTENSION;
        String outSequenceFileLocation = pathToArchive + APIImportExportConstants.OUT_SEQUENCE_LOCATION
                + outSequenceFileName;

        //Adding out-sequence, if any
        if (CommonUtil.checkFileExistence(outSequenceFileLocation)) {
            regResourcePath = APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION + outSequenceFileName;
            addSequenceToRegistry(false, registry, outSequenceFileLocation, regResourcePath);
        }

        String faultSequenceFileName = importedApi.getFaultSequence() + APIConstants.XML_EXTENSION;
        String faultSequenceFileLocation = pathToArchive + APIImportExportConstants.FAULT_SEQUENCE_LOCATION
                + faultSequenceFileName;

        //Adding fault-sequence, if any
        if (CommonUtil.checkFileExistence(faultSequenceFileLocation)) {
            regResourcePath = APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION + faultSequenceFileName;
            addSequenceToRegistry(false, registry, faultSequenceFileLocation, regResourcePath);
        }
    }

    /**
     * This method adds API Specific sequences added through the Publisher to the imported API. If the specific
     * sequence already exists, it is updated.
     *
     * @param pathToArchive location of the extracted folder of the API
     * @param importedApi   the imported API object
     */
    private static void addAPISpecificSequences(String pathToArchive, API importedApi, Registry registry) {

        String regResourcePath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                + importedApi.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR
                + importedApi.getId().getApiName() + RegistryConstants.PATH_SEPARATOR
                + importedApi.getId().getVersion() + RegistryConstants.PATH_SEPARATOR;

        String inSequenceFileName = importedApi.getInSequence();
        String inSequenceFileLocation = pathToArchive + APIImportExportConstants.IN_SEQUENCE_LOCATION
                + APIImportExportConstants.CUSTOM_TYPE + File.separator + inSequenceFileName;

        //Adding in-sequence, if any
        if (CommonUtil.checkFileExistence(inSequenceFileLocation + APIConstants.XML_EXTENSION)) {
            String inSequencePath = APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN + RegistryConstants.PATH_SEPARATOR
                    + inSequenceFileName;
            addSequenceToRegistry(true, registry, inSequenceFileLocation + APIConstants.XML_EXTENSION, regResourcePath + inSequencePath);
        }

        String outSequenceFileName = importedApi.getOutSequence() + APIConstants.XML_EXTENSION;
        String outSequenceFileLocation = pathToArchive + APIImportExportConstants.OUT_SEQUENCE_LOCATION
                + APIImportExportConstants.CUSTOM_TYPE + File.separator + outSequenceFileName;

        //Adding out-sequence, if any
        if (CommonUtil.checkFileExistence(outSequenceFileLocation)) {
            String outSequencePath = APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT + RegistryConstants.PATH_SEPARATOR
                    + outSequenceFileName;
            addSequenceToRegistry(true, registry, outSequenceFileLocation, regResourcePath + outSequencePath);
        }

        String faultSequenceFileName = importedApi.getFaultSequence() + APIConstants.XML_EXTENSION;
        String faultSequenceFileLocation = pathToArchive + APIImportExportConstants.FAULT_SEQUENCE_LOCATION
                + APIImportExportConstants.CUSTOM_TYPE + File.separator + faultSequenceFileName;

        //Adding fault-sequence, if any
        if (CommonUtil.checkFileExistence(faultSequenceFileLocation)) {
            String faultSequencePath = APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT + RegistryConstants.PATH_SEPARATOR
                    + faultSequenceFileName;
            addSequenceToRegistry(true, registry, faultSequenceFileLocation, regResourcePath + faultSequencePath);
        }
    }

    /**
     * This method adds the sequence files to the registry. This updates the API specific sequences if already exists.
     *
     * @param isAPISpecific        whether the adding sequence is API specific
     * @param registry             the registry instance
     * @param sequenceFileLocation location of the sequence file
     */
    private static void addSequenceToRegistry(Boolean isAPISpecific, Registry registry, String sequenceFileLocation,
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
                File sequenceFile = new File(sequenceFileLocation);
                try (InputStream seqStream = new FileInputStream(sequenceFile);) {
                    byte[] inSeqData = IOUtils.toByteArray(seqStream);
                    Resource inSeqResource = registry.newResource();
                    inSeqResource.setContent(inSeqData);
                    registry.put(regResourcePath, inSeqResource);
                }
            }
        } catch (RegistryException e) {
            //this is logged and ignored because sequences are optional
            log.error("Failed to add sequences into the registry : " + regResourcePath, e);
        } catch (IOException e) {
            //this is logged and ignored because sequences are optional
            log.error("I/O error while writing sequence data to the registry : " + regResourcePath, e);
        }
    }

    /**
     * This method adds the WSDL to the registry, if there is a WSDL associated with the API.
     *
     * @param pathToArchive location of the extracted folder of the API
     * @param importedApi   the imported API object
     */
    private static void addAPIWsdl(String pathToArchive, API importedApi, APIProvider apiProvider, Registry registry) {

        String wsdlFileName = importedApi.getId().getApiName() + "-" + importedApi.getId().getVersion()
                + APIConstants.WSDL_FILE_EXTENSION;
        String wsdlPath = pathToArchive + APIImportExportConstants.WSDL_LOCATION + wsdlFileName;

        if (CommonUtil.checkFileExistence(wsdlPath)) {
            try {
                URL wsdlFileUrl = new File(wsdlPath).toURI().toURL();
                importedApi.setWsdlUrl(wsdlFileUrl.toString());
                APIUtil.createWSDL(registry, importedApi);
                apiProvider.updateAPI(importedApi);
            } catch (MalformedURLException e) {
                //this exception is logged and ignored since WSDL is optional for an API
                log.error("Error in getting WSDL URL. ", e);
            } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
                //this exception is logged and ignored since WSDL is optional for an API
                log.error("Error in putting the WSDL resource to registry. ", e);
            } catch (APIManagementException e) {
                //this exception is logged and ignored since WSDL is optional for an API
                log.error("Error in creating the WSDL resource in the registry. ", e);
            } catch (FaultGatewaysException e) {
                //This is logged and process is continued because WSDL is optional for an API
                log.error("Failed to update API after adding WSDL. ", e);
            }
        }
    }

    /**
     * This method adds Swagger API definition to registry.
     *
     * @param apiId          Identifier of the imported API
     * @param swaggerContent Content of Swagger file
     * @throws APIImportExportException if there is an error occurs when adding Swagger definition
     */
    private static void addSwaggerDefinition(APIIdentifier apiId, String swaggerContent, APIProvider apiProvider)
            throws APIImportExportException {

        try {
            apiProvider.saveSwagger20Definition(apiId, swaggerContent);
        } catch (APIManagementException e) {
            String errorMessage = "Error in adding Swagger definition for the API: " + apiId.getApiName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiId.getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * This method adds GraphQL schema definition to the registry.
     *
     * @param api              API to import
     * @param schemaDefinition Content of schema definition
     * @param apiProvider      API Provider
     * @throws APIManagementException if there is an error occurs when adding schema definition
     */
    private static void addGraphqlSchemaDefinition(API api, String schemaDefinition, APIProvider apiProvider)
            throws APIManagementException {
        apiProvider.saveGraphqlSchemaDefinition(api, schemaDefinition);
    }

    /**
     * This method import endpoint certificate.
     *
     * @param pathToArchive location of the extracted folder of the API
     * @param importedApi   the imported API object
     * @throws APIImportExportException If an error occurs while importing endpoint certificates from file
     */
    private static void addEndpointCertificates(String pathToArchive, API importedApi, APIProvider apiProvider,
                                                int tenantId)
            throws APIImportExportException {

        String jsonContent = null;
        String pathToYamlFile = pathToArchive + APIImportExportConstants.YAML_ENDPOINTS_CERTIFICATE_FILE;
        String pathToJsonFile = pathToArchive + APIImportExportConstants.JSON_ENDPOINTS_CERTIFICATE_FILE;

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
            JsonElement configElement = new JsonParser().parse(jsonContent);
            JsonArray certificates = configElement.getAsJsonArray().getAsJsonArray();
            certificates.forEach(certificate -> updateAPIWithCertificate(certificate, apiProvider, importedApi,
                    tenantId));
        } catch (IOException e) {
            String errorMessage = "Error in reading " + APIImportExportConstants.YAML_ENDPOINTS_CERTIFICATE_FILE
                    + " file";
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Import client certificates for Mutual SSL related configuration
     *
     * @param pathToArchive location of the extracted folder of the API
     * @throws APIImportExportException
     */
    private static void addClientCertificates(String pathToArchive, APIProvider apiProvider)
            throws APIImportExportException {
        String jsonContent = null;
        String pathToYamlFile = pathToArchive + APIImportExportConstants.YAML_CLIENT_CERTIFICATE_FILE;
        String pathToJsonFile = pathToArchive + APIImportExportConstants.JSON_CLIENT_CERTIFICATE_FILE;

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
                return;
            }
            Gson gson = new Gson();
            List<ClientCertificateDTO> certificateMetadataDTOS = gson.fromJson(jsonContent,
                    new TypeToken<ArrayList<ClientCertificateDTO>>(){}.getType());
            for (ClientCertificateDTO certDTO : certificateMetadataDTOS) {
                apiProvider.addClientCertificate(
                        APIUtil.replaceEmailDomainBack(certDTO.getApiIdentifier().getProviderName()),
                        certDTO.getApiIdentifier(), certDTO.getCertificate(), certDTO.getAlias(),
                        certDTO.getTierName());
            }
        } catch (IOException e) {
            String errorMessage = "Error in reading " + APIImportExportConstants.YAML_ENDPOINTS_CERTIFICATE_FILE
                    + " file";
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (APIManagementException e) {
            String errorMessage = "Error while importing client certificate";
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
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
                                                 int tenantId) {

        String certificateContent = certificate.getAsJsonObject()
                .get(APIImportExportConstants.CERTIFICATE_CONTENT_JSON_KEY).getAsString();
        String alias = certificate.getAsJsonObject().get(APIImportExportConstants.ALIAS_JSON_KEY).getAsString();
        String endpoint = certificate.getAsJsonObject().get(APIImportExportConstants.HOSTNAME_JSON_KEY)
                .getAsString();
        try {
            if (apiProvider.isCertificatePresent(tenantId, alias)
                    || (ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode() ==
                    (apiProvider.addCertificate(APIUtil.replaceEmailDomainBack(importedApi.getId().getProviderName()),
                            certificateContent, alias, endpoint)))) {
                apiProvider.updateCertificate(certificateContent, alias);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while importing certificate endpoint [" + endpoint + " ]" + "alias ["
                    + alias + " ] tenant user ["
                    + APIUtil.replaceEmailDomainBack(importedApi.getId().getProviderName()) + "]";
            log.error(errorMessage, e);
        }
    }

    /**
     * This method adds API sequences to the imported API. If the sequence is a newly defined one, it is added.
     *
     * @param pathToArchive location of the extracted folder of the API
     */
    private static void addSOAPToREST(String pathToArchive, API importedApi, Registry registry)
            throws APIImportExportException {

        String inFlowFileLocation = pathToArchive + File.separator + SOAPTOREST + File.separator + IN;
        String outFlowFileLocation = pathToArchive + File.separator + SOAPTOREST + File.separator + OUT;

        //Adding in-sequence, if any
        if (CommonUtil.checkFileExistence(inFlowFileLocation)) {
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
            try {
                // Import inflow mediation logic
                Path inFlowDirectory = Paths.get(inFlowFileLocation);
                ImportMediationLogic(inFlowDirectory, registry, soapToRestLocationIn);

                // Import outflow mediation logic
                Path outFlowDirectory = Paths.get(outFlowFileLocation);
                ImportMediationLogic(outFlowDirectory, registry, soapToRestLocationOut);

            } catch (DirectoryIteratorException e) {
                throw new APIImportExportException("Error in importing SOAP to REST mediation logic", e);
            }
        }
    }

    /**
     * Method created to add inflow and outflow mediation logic
     *
     * @param flowDirectory      inflow and outflow directory
     * @param registry           Registry
     * @param soapToRestLocation folder location
     * @throws APIImportExportException
     */
    private static void ImportMediationLogic(Path flowDirectory, Registry registry, String soapToRestLocation)
            throws APIImportExportException {
        InputStream inputFlowStream = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(flowDirectory)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                String method = "";
                if (fileName.split(".xml").length != 0) {
                    method = fileName.split(".xml")[0]
                            .substring(file.getFileName().toString().lastIndexOf("_") + 1);
                }
                inputFlowStream = new FileInputStream(file.toFile());
                byte[] inSeqData = IOUtils.toByteArray(inputFlowStream);
                Resource inSeqResource = (Resource) registry.newResource();
                inSeqResource.setContent(inSeqData);
                inSeqResource.addProperty(SOAPToRESTConstants.METHOD, method);
                inSeqResource.setMediaType("text/xml");
                registry.put(soapToRestLocation + RegistryConstants.PATH_SEPARATOR + file.getFileName(),
                        inSeqResource);
                IOUtils.closeQuietly(inputFlowStream);
            }
        } catch (IOException | DirectoryIteratorException e) {
            throw new APIImportExportException("Error in importing SOAP to REST mediation logic", e);
        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            throw new APIImportExportException("Error in storing imported SOAP to REST mediation logic", e);
        } finally {
            IOUtils.closeQuietly(inputFlowStream);
        }
    }
}

