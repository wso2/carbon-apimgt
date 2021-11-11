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
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class AsyncApiParserUtil {
    
    private static final APIDefinition asyncApiParser = new AsyncApiParser();
    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);
    private static final String PATH_SEPARATOR = "/";

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
}
