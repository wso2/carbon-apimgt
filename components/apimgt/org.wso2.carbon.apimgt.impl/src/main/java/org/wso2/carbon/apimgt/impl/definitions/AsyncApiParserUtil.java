package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.models.AaiDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.*;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class AsyncApiParserUtil {
    
    private static final APIDefinition asyncApiParser = new AsyncApiParser();
    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);

    public static APIDefinitionValidationResponse validateAsyncAPISpecification(
            String schemaToBeValidated, boolean returnJSONContent) throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = asyncApiParser.validateAPIDefinition(schemaToBeValidated, returnJSONContent);
        final String asyncAPIKeyNotFound = "#: required key [asyncapi] not found";

        if (!validationResponse.isValid()) {
            for (ErrorHandler errorItem : validationResponse.getErrorItems()) {
                if (asyncAPIKeyNotFound.equals(errorItem.getErrorMessage())) {    //change it other way
                    addErrorToValidationResponse(validationResponse, "#: attribute [asyncapi] should be present");
                    return validationResponse;
                }
            }
        }

        return validationResponse;
    }

    public static APIDefinitionValidationResponse validateAsyncAPISpecificationByURL(
            String url, boolean returnJSONContent) throws APIManagementException{

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        try {
            URL urlObj = new URL(url);
            HttpClient httpClient = APIUtil.getHttpClient(urlObj.getPort(), urlObj.getProtocol());
            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                Object obj = yamlReader.readValue(urlObj, Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                String json = jsonWriter.writeValueAsString(obj);
                validationResponse = validateAsyncAPISpecification(json, returnJSONContent);
            } else {
                validationResponse.setValid(false);
                validationResponse.getErrorItems().add(ExceptionCodes.ASYNCAPI_URL_NO_200);
            }
        } catch (IOException e) {
            ErrorHandler errorHandler = ExceptionCodes.ASYNCAPI_URL_MALFORMED;
            //log the error and continue since this method is only intended to validate a definition
            log.error(errorHandler.getErrorDescription(), e);

            validationResponse.setValid(false);
            validationResponse.getErrorItems().add(errorHandler);
        }

        return validationResponse;
    }

    public static void updateValidationResponseAsSuccess(
            APIDefinitionValidationResponse validationResponse,
            String originalAPIDefinition,
            String asyncAPIVersion,
            String title,
            String version,
            String context,
            String description,
            List<String> endpoints
    ) {
        validationResponse.setValid(true);
        validationResponse.setContent(originalAPIDefinition);
        APIDefinitionValidationResponse.Info info = new APIDefinitionValidationResponse.Info();
        info.setOpenAPIVersion(asyncAPIVersion);
        info.setName(title);
        info.setVersion(version);
        info.setContext(context);
        info.setDescription(description);
        info.setEndpoints(endpoints);
        validationResponse.setInfo(info);
    }

    public static ErrorItem addErrorToValidationResponse(
            APIDefinitionValidationResponse validationResponse, String errMessage) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setMessage(errMessage);
        validationResponse.getErrorItems().add(errorItem);
        return errorItem;
    }

    /**
     *  This method saves api definition json in the registry
     *
     * @param api               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     * @param registry          user registry
     * @throws  APIManagementException
     */
    public static void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry)
            throws APIManagementException{
        String apiName = api.getId().getApiName();
        String apiVersion = api.getId().getVersion();
        String apiProviderName = api.getId().getProviderName();

        try{
            String resourcePath = APIUtil.getAsyncAPIDefinitionFilePath(apiName, apiVersion, apiProviderName);
            resourcePath = resourcePath + APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME;
            Resource resource;
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(resourcePath);
            }
            resource.setContent(apiDefinitionJSON);
            resource.setMediaType(APIConstants.APPLICATION_JSON_MEDIA_TYPE);          //add a constant for app.json
            registry.put(resourcePath, resource);

            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }

            APIUtil.clearResourcePermissions(resourcePath, api.getId(), ((UserRegistry) registry).getTenantId());
            APIUtil.setResourcePermissions(apiProviderName, api.getVisibility(), visibleRoles, resourcePath);

        } catch (RegistryException e) {
            handleException("Error while adding AsyncApi Definition for " + apiName + "-" + apiVersion, e);
        }
    }

    /**
     * This method returns api definition json for given api
     *
     * @param apiIdentifier api identifier
     * @param registry user registry
     * @return api definition json as json string
     * @throws APIManagementException
     */
    public static String getAPIDefinition(Identifier apiIdentifier, Registry registry) throws APIManagementException {
        String resourcePath = "";

        if (apiIdentifier instanceof APIIdentifier) {
            resourcePath = APIUtil.getAsyncAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                    apiIdentifier.getProviderName());
        }
        JSONParser parser = new JSONParser();
        String apiDocContent = null;
        try {
          if (registry.resourceExists(resourcePath + APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME)) {
              Resource apiDocResource = registry.get(resourcePath + APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME);
              apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
              parser.parse(apiDocContent);
          } else {
              if (log.isDebugEnabled()) {
                  log.debug("Resource" + APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME + " not found at "
                            + resourcePath);
              }
          }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving AsyncAPI Definition for " + apiIdentifier.getName() + "-"
                            + apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException(
                    "Error while parsing AsyncAPI Definition for " + apiIdentifier.getName() + "-"
                            + apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiDocContent;
    }

    public static API loadTopicsFromAsyncAPIDefinition(API api, String definitionJSON) {
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        uriTemplates.clear();

        AaiDocument definition = (AaiDocument) Library.readDocumentFromJSONString(definitionJSON);
        if (definition.getChannels().size() > 0) {
            for (String topic : definition.channels.keySet()) {
                /*if (definition.channels.get(topic).publish != null && definition.channels.get(topic).subscribe != null) {
                    URITemplate uriTemplateSub = new URITemplate();
                    uriTemplateSub.setUriTemplate(topic);
                    uriTemplateSub.setHTTPVerb("SUBSCRIBE");
                    uriTemplateSub.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    uriTemplateSub.setThrottlingTier(APIConstants.UNLIMITED_TIER);
                    uriTemplates.add(uriTemplateSub);
                    URITemplate uriTemplatePub = new URITemplate();
                    uriTemplatePub.setUriTemplate(topic);
                    uriTemplatePub.setHTTPVerb("PUBLISH");
                    uriTemplatePub.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    uriTemplatePub.setThrottlingTier(APIConstants.UNLIMITED_TIER);
                    uriTemplates.add(uriTemplatePub);
                } else if (definition.channels.get(topic).publish != null) {
                    URITemplate uriTemplate = new URITemplate();
                    uriTemplate.setUriTemplate(topic);
                    uriTemplate.setHTTPVerb("PUBLISH");
                    uriTemplate.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    uriTemplate.setThrottlingTier(APIConstants.UNLIMITED_TIER);
                    uriTemplates.add(uriTemplate);
                } else if (definition.channels.get(topic).subscribe != null) {
                    URITemplate uriTemplate = new URITemplate();
                    uriTemplate.setUriTemplate(topic);
                    uriTemplate.setHTTPVerb("SUBSCRIBE");
                    uriTemplate.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    uriTemplate.setThrottlingTier(APIConstants.UNLIMITED_TIER);
                    uriTemplates.add(uriTemplate);
                }*/

                if (definition.channels.get(topic).subscribe != null) {
                    URITemplate uriTemplate = new URITemplate();
                    uriTemplate.setUriTemplate(topic);
                    uriTemplate.setHTTPVerb("SUBSCRIBE");
                    uriTemplate.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    uriTemplate.setThrottlingTier(APIConstants.UNLIMITED_TIER);
                    uriTemplates.add(uriTemplate);
                }
            }
        }

        api.setUriTemplates(uriTemplates);
        return api;
    }
}
