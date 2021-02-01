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

package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the util class which consists of all the functions for importing API Product.
 */
public class APIProductImportUtil {

    private static final Log log = LogFactory.getLog(APIProductImportUtil.class);

    private APIProductImportUtil() {
    }

    /**
     * This method imports an API Product.
     *
     * @param pathToArchive             Location of the extracted folder of the API Product
     * @param currentUser               The current logged in user
     * @param isDefaultProviderAllowed  Decision to keep or replace the provider
     * @param apiProvider               API provider
     * @throws APIImportExportException If there is an error in importing an API Product
     */
    public static void importAPIProduct(String pathToArchive, String currentUser, boolean isDefaultProviderAllowed,
                                 APIProvider apiProvider, Boolean overwriteAPIProduct, Boolean overwriteAPIs, Boolean isImportAPIs)
            throws APIImportExportException {

        String jsonContent;
        APIProduct importedApiProduct = null;
        APIProduct targetApiProduct; //target API Product when overwriteAPIProduct is true
        ApiTypeWrapper apiTypeWrapper;
        String prevProvider;
        String apiProductName;
        String apiProductVersion;
        String currentTenantDomain;
        String currentStatus;
        String targetStatus;
        String lifecycleAction;

        try {
            // Get API Definition as JSON
            jsonContent = APIAndAPIProductCommonUtil.getAPIDefinitionAsJson(pathToArchive);
            if (jsonContent == null) {
                throw new IOException("Cannot find API Product definition. api.json or api.yaml should present");
            }
            JsonElement configElement = new JsonParser().parse(jsonContent);
            JsonObject configObject = configElement.getAsJsonObject();

            // Initially, when importing the API Product, it only contains the subscription tier names without the
            // details. So, by matching with the names, correct subscription tiers with the details should be added
            // to the API Product before doing further processing.
            APIAndAPIProductCommonUtil.setSubscriptionTiers(configObject, apiProvider);
            configElement = configObject;

            // Locate the "providerName" within the "id" and set it as the current user
            JsonObject apiProductId = configObject.getAsJsonObject(APIImportExportConstants.ID_ELEMENT);

            prevProvider = apiProductId.get(APIImportExportConstants.PROVIDER_ELEMENT).getAsString();
            apiProductName = apiProductId.get(APIImportExportConstants.API_PRODUCT_NAME_ELEMENT).getAsString();
            apiProductVersion = apiProductId.get(APIImportExportConstants.VERSION_ELEMENT).getAsString();
            // Remove spaces of API Product Name/version if present
            if (apiProductName != null && apiProductVersion != null) {
                apiProductId.addProperty(APIImportExportConstants.API_PRODUCT_NAME_ELEMENT,
                        apiProductName = apiProductName.replace(" ", ""));
                apiProductId.addProperty(APIImportExportConstants.VERSION_ELEMENT,
                        apiProductVersion = apiProductVersion.replace(" ", ""));
            } else {
                throw new IOException("API Product Name (id.apiProductName) and Version (id.version) must be provided in api.yaml");
            }

            String prevTenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(prevProvider));
            currentTenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(currentUser));

            // If the original provider is preserved,
            if (isDefaultProviderAllowed) {
                if (!StringUtils.equals(prevTenantDomain, currentTenantDomain)) {
                    String errorMessage = "Tenant mismatch! Please enable preserveProvider property "
                            + "for cross tenant API Product Import.";
                    throw new APIMgtAuthorizationFailedException(errorMessage);
                }
                importedApiProduct = new Gson().fromJson(configElement, APIProduct.class);
            } else {
                String currentUserWithDomain = APIUtil.replaceEmailDomain(currentUser);
                apiProductId.addProperty(APIImportExportConstants.PROVIDER_ELEMENT, currentUserWithDomain);

                importedApiProduct = new Gson().fromJson(configElement, APIProduct.class);
                // Replace context to match with current provider
                apiTypeWrapper = new ApiTypeWrapper(importedApiProduct);
                APIAndAPIProductCommonUtil.setCurrentProviderToAPIProperties(apiTypeWrapper, currentTenantDomain, prevTenantDomain);
            }

            // Check whether the API resources are valid
            checkAPIProductResourcesValid(pathToArchive, currentUser, apiProvider, importedApiProduct);

            if (isImportAPIs) {
                // Import dependent APIs only if it is asked
                importDependentAPIs(pathToArchive, currentUser, isDefaultProviderAllowed, apiProvider, overwriteAPIs, importedApiProduct);
            }

            // Store imported API Product status
            targetStatus = importedApiProduct.getState();
            if (Boolean.TRUE.equals(overwriteAPIProduct)) {
                String provider = APIUtil
                        .getAPIProviderFromAPINameVersionTenant(apiProductName, apiProductVersion, currentTenantDomain);
                APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(APIUtil.replaceEmailDomain(provider), apiProductName,
                        apiProductVersion);
                // Checking whether the API Product exists
                if (!apiProvider.isAPIProductAvailable(apiProductIdentifier)) {
                    String errorMessage = "Error occurred while updating. API Product: " + apiProductName + StringUtils.SPACE
                            + APIConstants.API_DATA_VERSION + ": " + apiProductVersion + " not found";
                    throw new APIMgtResourceNotFoundException(errorMessage);
                }
                targetApiProduct = apiProvider.getAPIProduct(apiProductIdentifier);
                // Store target API Product status
                currentStatus = targetApiProduct.getState();

                // Since the overwrite should be done, the imported API Product Identifier should be equal to the
                // target API Product Identifier
                importedApiProduct.setID(targetApiProduct.getId());
            } else {
                if (apiProvider.isAPIProductAvailable(importedApiProduct.getId())
                        || apiProvider.isApiNameWithDifferentCaseExist(apiProductName)) {
                    String errorMessage = "Error occurred while adding the API Product. A duplicate API Product already exists " +
                            "for " + importedApiProduct.getId().getName() + '-' + importedApiProduct.getId().getVersion();
                    throw new APIMgtResourceAlreadyExistsException(errorMessage);
                }

                if (apiProvider.isContextExist(importedApiProduct.getContext())) {
                    String errMsg = "Error occurred while adding the API Product [" + importedApiProduct.getId().getName()
                            + '-' + importedApiProduct.getId().getVersion() + "]. A duplicate context["
                            + importedApiProduct.getContext() + "] already exists";
                    throw new APIMgtResourceAlreadyExistsException(errMsg);
                }

                // Initialize to PUBLISHED when import
                currentStatus = APIStatus.PUBLISHED.toString();
            }
            // Set the status of imported API to PUBLISHED (importing API Product) or current status of target API Product when updating
            importedApiProduct.setState(currentStatus);

            // Check whether targetStatus is reachable from current status, if not throw an exception
            if (!currentStatus.equals(targetStatus)) {
                lifecycleAction = APIAndAPIProductCommonUtil.getLifeCycleAction(currentTenantDomain, currentStatus, targetStatus, apiProvider);
                if (lifecycleAction == null) {
                    String errMsg = "Error occurred while importing the API Product. " + targetStatus + " is not reachable from "
                            + currentStatus;
                    throw new APIImportExportException(errMsg);
                }
            }

            // Only Product level throttling available for API Products (No resource level throttling)
            if (importedApiProduct.getProductLevelPolicy() != null) {
                apiProvider.validateProductThrottlingTier(importedApiProduct, currentTenantDomain);
            }

            if (Boolean.FALSE.equals(overwriteAPIProduct)) {
                // Add API Product in PUBLISHED state
                Map<API, List<APIProductResource>> apiToProductResourceMapping = apiProvider.addAPIProductWithoutPublishingToGateway(importedApiProduct);
                apiProvider.addAPIProductSwagger(apiToProductResourceMapping, importedApiProduct);
                APIProductIdentifier createdAPIProductIdentifier = importedApiProduct.getId();
                APIProduct createdProduct = apiProvider.getAPIProduct(createdAPIProductIdentifier);
                apiProvider.saveToGateway(createdProduct);
            }

            String swaggerContent = APIAndAPIProductCommonUtil.loadSwaggerFile(pathToArchive);

            // Load required properties from swagger to the API Product
            APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerContent);
            Set<Scope> scopes = apiDefinition.getScopes(swaggerContent);
            importedApiProduct.setScopes(scopes);


            // This is required to make scopes get effected
            Map<API, List<APIProductResource>> apiToProductResourceMapping = apiProvider.updateAPIProduct(importedApiProduct);
            apiProvider.updateAPIProductSwagger(apiToProductResourceMapping, importedApiProduct);

            // Since Image, documents and client certificates are optional, exceptions are logged and ignored in implementation
            ApiTypeWrapper apiTypeWrapperWithUpdatedApiProduct = new ApiTypeWrapper(importedApiProduct);
            APIAndAPIProductCommonUtil.addAPIOrAPIProductImage(pathToArchive, apiTypeWrapperWithUpdatedApiProduct, apiProvider);
            APIAndAPIProductCommonUtil.addAPIOrAPIProductDocuments(pathToArchive, apiTypeWrapperWithUpdatedApiProduct,
                    apiProvider, currentTenantDomain);

                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Importing client certificates.");
                }
                APIAndAPIProductCommonUtil.addClientCertificates(pathToArchive, apiProvider);
        } catch (IOException e) {
            // Error is logged and APIImportExportException is thrown because adding API Product and swagger are mandatory steps
            String errorMessage = "Error while reading API Product meta information from path: " + pathToArchive;
            throw new APIImportExportException(errorMessage, e);
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API Product: " + importedApiProduct.getId().getName();
            throw new APIImportExportException(errorMessage, e);
        } catch (APIManagementException e) {
            String errorMessage = "Error while importing API Product: ";
            if (importedApiProduct != null) {
                errorMessage += importedApiProduct.getId().getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION
                        + ": " + importedApiProduct.getId().getVersion();
            }
            throw new APIImportExportException(errorMessage + " " + e.getMessage(), e);
        }
    }

    /**
     * This method imports dependent APIs of the API Product.
     *
     * @param path                     Location of the extracted folder of the API Product
     * @param currentUser              The current logged in user
     * @param isDefaultProviderAllowed Decision to keep or replace the provider
     * @param apiProvider              API provider
     * @param overwriteAPIs            Whether to overwrite the APIs or not
     * @param apiProduct               API Product
     * @throws IOException If there is an error while reading an API file
     * @throws APIImportExportException If there is an error in importing an API
     * @throws APIManagementException If failed to get the API Provider of an API, or failed when checking the existence of an API
     */
    private static void importDependentAPIs(String path, String currentUser, boolean isDefaultProviderAllowed,
                                            APIProvider apiProvider, Boolean overwriteAPIs, APIProduct apiProduct)
            throws APIImportExportException, IOException, APIManagementException {

        List<APIProductResource> apiProductResources = apiProduct.getProductResources();

        String apisDirectoryPath = path + File.separator + APIImportExportConstants.APIS_DIRECTORY;
        File apisDirectory = new File(apisDirectoryPath);
        File[] apisDirectoryListing = apisDirectory.listFiles();
        if (apisDirectoryListing != null) {
            for (File api : apisDirectoryListing) {
                String apiDirectoryPath = path + File.separator + APIImportExportConstants.APIS_DIRECTORY + File.separator + api.getName();
                // Get API Definition as JSON
                String jsonContent = APIAndAPIProductCommonUtil.getAPIDefinitionAsJson(apiDirectoryPath);
                if (jsonContent == null) {
                    throw new IOException("Cannot find API definition. api.json or api.yaml should present");
                }
                JsonElement configElement = new JsonParser().parse(jsonContent);
                JsonObject configObject = configElement.getAsJsonObject();

                // Initially, when importing the API, it only contains the subscription tier names without the
                // details. So, by matching with the names, correct subscription tiers with the details should be added
                // to the API before doing further processing.
                APIAndAPIProductCommonUtil.setSubscriptionTiers(configObject, apiProvider);

                // Locate the "providerName", "apiName" and "apiVersion" within the "id"
                JsonObject apiId = configObject.getAsJsonObject(APIImportExportConstants.ID_ELEMENT);

                String provider = apiId.get(APIImportExportConstants.PROVIDER_ELEMENT).getAsString();
                String apiName = apiId.get(APIImportExportConstants.API_NAME_ELEMENT).getAsString();
                String apiVersion = apiId.get(APIImportExportConstants.VERSION_ELEMENT).getAsString();

                if (isDefaultProviderAllowed) {
                    APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), apiName,
                            apiVersion);

                    // Checking whether the API exists
                    if (apiProvider.isAPIAvailable(apiIdentifier)) {
                        // If the API is already imported, update it if the overWriteAPIs flag is specified,
                        // otherwise do not import/update the API. (Just skip it)
                        if (Boolean.TRUE.equals(overwriteAPIs)) {
                            APIImportUtil.importAPI(apiDirectoryPath, currentUser, true, apiProvider, true);
                        }
                    } else {
                        // If the API is not already imported, import it
                        APIImportUtil.importAPI(apiDirectoryPath, currentUser, true, apiProvider, false);
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
                        APIImportUtil.importAPI(apiDirectoryPath, currentUser, false, apiProvider, false);
                        // Update the provider name of the resources of this API in the current product resources with the current user's name
                        updateProviderNameInProductResources(apiName, apiVersion, apiProductResources, currentUser);
                    } else {
                        // If there is an API already in the current tenant domain, update it if the overWriteAPIs flag is specified,
                        // otherwise do not import/update the API. (Just skip it)
                        if (Boolean.TRUE.equals(overwriteAPIs)) {
                            APIImportUtil.importAPI(apiDirectoryPath, currentUser, false, apiProvider, true);
                        }
                        // Update the provider name of the resources of this API in the current product resources
                        // with the user's name who is the provider of this API
                        updateProviderNameInProductResources(apiName, apiVersion, apiProductResources, apiProviderInCurrentTenantDomain);
                    }
                }
            }
        } else {
            String msg = "No dependent APIs supplied. Continuing ...";
            log.info(msg);
        }
    }

    /**
     * This method updates the provider name (this will be invoked only isDefaultProviderAllowed is false)
     *
     * @param apiName                   Name of the dependent API
     * @param apiVersion                Version of the dependent API
     * @param apiProductResources       List of API Product resources
     * @param providerName              API Product provider name (Change based on the value of isDefaultProviderAllowed)
     */
    private static void updateProviderNameInProductResources(String apiName, String apiVersion, List<APIProductResource> apiProductResources, String providerName) {
        for (APIProductResource apiProductResource : apiProductResources) {
            String apiNameInProductResource = apiProductResource.getApiIdentifier().getApiName();
            String apiVersionInProductResource = apiProductResource.getApiIdentifier().getVersion();
            if (StringUtils.equals(apiName, apiNameInProductResource) && StringUtils.equals(apiVersion, apiVersionInProductResource)) {
                apiProductResource.setApiIdentifier(new APIIdentifier(providerName, apiName, apiVersion));
            }
        }
    }

    /**
     * This method checks whether the resources in the API Product are valid.
     *
     * @param path                     Location of the extracted folder of the API Product
     * @param currentUser              The current logged in user
     * @param apiProvider              API provider
     * @param apiProduct               API Product
     * @throws IOException If there is an error while reading an API file
     * @throws APIManagementException If failed to get the API Provider of an API, or failed when checking the existence of an API
     */
    private static void checkAPIProductResourcesValid(String path, String currentUser, APIProvider apiProvider,
                                                      APIProduct apiProduct)
            throws IOException, APIManagementException {
        List<APIProductResource> tempProductResources = new ArrayList<>(apiProduct.getProductResources());; // To iterate
        List<APIProductResource> invalidProductResources = new ArrayList<>(apiProduct.getProductResources()); // If a product resource is valid, it will be removed from this list
        API api; // To store each API in the APIs directory
        String swaggerContent; // To store the swagger file of the API in the directory

        String apisDirectoryPath = path + File.separator + APIImportExportConstants.APIS_DIRECTORY;
        File apisDirectory = new File(apisDirectoryPath);
        File[] apisDirectoryListing = apisDirectory.listFiles();
        if (apisDirectoryListing != null) {
            for (File apiDirectory : apisDirectoryListing) {
                String apiDirectoryPath = path + File.separator + APIImportExportConstants.APIS_DIRECTORY + File.separator + apiDirectory.getName();
                // Get API Definition as JSON
                String jsonContent = APIAndAPIProductCommonUtil.getAPIDefinitionAsJson(apiDirectoryPath);
                if (jsonContent == null) {
                    throw new IOException("Cannot find API definition. api.json or api.yaml should present");
                }
                JsonElement configElement = new JsonParser().parse(jsonContent);
                JsonObject configObject = configElement.getAsJsonObject();

                // Initially, when importing the API, it only contains the subscription tier names without the
                // details. So, by matching with the names, correct subscription tiers with the details should be added
                // to the API before doing further processing.
                APIAndAPIProductCommonUtil.setSubscriptionTiers(configElement.getAsJsonObject(), apiProvider);
                configElement = configObject;

                api = new Gson().fromJson(configElement, API.class);

                swaggerContent = APIAndAPIProductCommonUtil.loadSwaggerFile(apiDirectoryPath);
                APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerContent);
                Set<URITemplate> apiUriTemplates = apiDefinition.getURITemplates(swaggerContent);

                // Iterate API Product resources
                for (APIProductResource apiProductResource : tempProductResources) {
                    // Check whether the APIs is matching
                    if (StringUtils.equals(apiProductResource.getApiName(), api.getId().getApiName()) &&
                            StringUtils.equals(apiProductResource.getApiIdentifier().getVersion(), api.getId().getVersion())) {
                        filterInvalidProductResources(apiUriTemplates, apiProductResource, invalidProductResources);
                    }
                }
            }
            // If there are still resources left, reassign them to tempProductResources to check again whether those are inside the APIM
            tempProductResources = new ArrayList<>(invalidProductResources);
        }

        // If there are still product resources to be checked (which were not able to find in the dependent APIs inside the directory)
        // check whether those are already inside APIM
        if (!tempProductResources.isEmpty()) {
            for (APIProductResource apiProductResource : tempProductResources) {

                String apiName = apiProductResource.getApiIdentifier().getApiName();
                String apiVersion = apiProductResource.getApiIdentifier().getVersion();
                String currentTenantDomain = MultitenantUtils
                        .getTenantDomain(APIUtil.replaceEmailDomainBack(currentUser));

                // Get the provider of the API if the API is in current user's tenant domain.
                String apiProviderInCurrentTenantDomain = APIUtil
                        .getAPIProviderFromAPINameVersionTenant(apiName, apiVersion, currentTenantDomain);

                // If the API Provider is available, retrieve the API
                if (StringUtils.isNotBlank(apiProviderInCurrentTenantDomain)) {
                    APIIdentifier emailReplacedAPIIdentifier = new APIIdentifier(apiProviderInCurrentTenantDomain,
                            apiName, apiVersion);
                    api = apiProvider.getAPI(emailReplacedAPIIdentifier);

                    filterInvalidProductResources(api.getUriTemplates(), apiProductResource, invalidProductResources);
                }
            }
        }

        // invalidProductResources is not empty means, at least one of the resources of the API Product does not have corresponding APIs
        // neither inside the importing directory nor inside the APIM
        if (!invalidProductResources.isEmpty()) {
            throw new APIMgtResourceNotFoundException("Cannot find APIs for some API Product resources.");
        }
    }

    /**
     * This method filter the invalid resources in the API Product by matching with the URI Templates of a particular
     * dependent API.
     *
     * @param apiUriTemplates         URI Templates of the dependent API (either inside the import directory or already inside the APIM)
     * @param apiProductResource      API Product Resource
     * @param productResources        API Product Resources list to be filtered
     */
    private static void filterInvalidProductResources(Set<URITemplate> apiUriTemplates, APIProductResource apiProductResource,
                                                                           List<APIProductResource> productResources) {
        for (URITemplate apiUriTemplate: apiUriTemplates) {
            URITemplate apiProductUriTemplate = apiProductResource.getUriTemplate();
            if (StringUtils.equals(apiProductUriTemplate.getHTTPVerb(), apiUriTemplate.getHTTPVerb()) &&
                    StringUtils.equals(apiProductUriTemplate.getUriTemplate(), apiUriTemplate.getUriTemplate())) {
                // If the URI Template is Available in the API, remove it from the list since it is valid
                productResources.remove(apiProductResource);
            }
        }
    }
}
