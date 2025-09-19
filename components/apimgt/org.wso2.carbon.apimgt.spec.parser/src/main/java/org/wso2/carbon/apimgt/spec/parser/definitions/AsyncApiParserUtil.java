package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AsyncApiParserUtil {
    
    private static final APIDefinition asyncApiParser = new AsyncApiParser();
    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);
    private static final String PATH_SEPARATOR = "/";

    @UsedByMigrationClient
    public static APIDefinitionValidationResponse validateAsyncAPISpecification(
            String schemaToBeValidated, boolean returnJSONContent) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Validating AsyncAPI specification with returnJSONContent: " + returnJSONContent);
        }

        APIDefinitionValidationResponse validationResponse = asyncApiParser.validateAPIDefinition(schemaToBeValidated, returnJSONContent);
        final String asyncAPIKeyNotFound = "#: required key [asyncapi] not found";

        if (!validationResponse.isValid()) {
            if (log.isDebugEnabled()) {
                log.debug("AsyncAPI validation failed with " + validationResponse.getErrorItems().size() + " errors");
            }
            for (ErrorHandler errorItem : validationResponse.getErrorItems()) {
                if (asyncAPIKeyNotFound.equals(errorItem.getErrorMessage())) {
                    if (log.isDebugEnabled()) {
                        log.debug("AsyncAPI key not found in specification, adding custom error message");
                    }
                    addErrorToValidationResponse(validationResponse, "#: attribute [asyncapi] should be present");
                    return validationResponse;
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("AsyncAPI specification validation successful");
            }
        }

        return validationResponse;
    }

    public static APIDefinitionValidationResponse validateAsyncAPISpecificationByURL(
            String url, HttpClient httpClient, boolean returnJSONContent) throws APIManagementException{

        if (log.isDebugEnabled()) {
            log.debug("Validating AsyncAPI specification from URL: " + url);
        }

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();

            if (log.isDebugEnabled()) {
                log.debug("Received HTTP response with status code: " + statusCode);
            }

            if (HttpStatus.SC_OK == statusCode) {
                ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                Object obj = yamlReader.readValue(new URL(url), Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                String json = jsonWriter.writeValueAsString(obj);
                
                if (log.isDebugEnabled()) {
                    log.debug("Successfully retrieved and converted AsyncAPI specification from URL");
                }
                
                validationResponse = validateAsyncAPISpecification(json, returnJSONContent);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to retrieve AsyncAPI specification - HTTP status: " + statusCode);
                }
                validationResponse.setValid(false);
                validationResponse.getErrorItems().add(ExceptionCodes.ASYNCAPI_URL_NO_200);
            }
        } catch (IOException e) {
            ErrorHandler errorHandler = ExceptionCodes.ASYNCAPI_URL_MALFORMED;
            log.error("Failed to retrieve AsyncAPI specification from URL: " + url + ". " + errorHandler.getErrorDescription(), e);

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
            List<String> endpoints,
            List<URITemplate> uriTemplates)
    {
        validationResponse.setValid(true);
        validationResponse.setContent(originalAPIDefinition);
        APIDefinitionValidationResponse.Info info = new APIDefinitionValidationResponse.Info();
        info.setOpenAPIVersion(asyncAPIVersion);
        info.setName(title);
        info.setVersion(version);
        info.setContext(context);
        info.setDescription(description);
        info.setEndpoints(endpoints);
        info.setUriTemplates(uriTemplates);
        validationResponse.setInfo(info);
    }

    public static ErrorItem addErrorToValidationResponse(
            APIDefinitionValidationResponse validationResponse, String errMessage) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setMessage(errMessage);
        validationResponse.getErrorItems().add(errorItem);
        return errorItem;
    }
}
