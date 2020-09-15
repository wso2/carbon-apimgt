package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.wso2.carbon.apimgt.api.*;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AsyncApiParserUtil {
    
    private static APIDefinition asyncApiParser = new AsyncApiParser();
    private static final Log log = LogFactory.getLog(OASParserUtil.class);

    public static APIDefinitionValidationResponse validateAsyncAPISpecification(
            String schemaToBeValidated, boolean returnJSONContent) throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = asyncApiParser.validateAPIDefinition(schemaToBeValidated, returnJSONContent);

        if (!validationResponse.isValid()){
            for (ErrorHandler errorItem : validationResponse.getErrorItems()){
                if (errorItem.getErrorMessage().equals("#: required key [asyncapi] not found")){
                    addErrorToValidationResponse(validationResponse, "attribute asyncapi should present");
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

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()){
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
    ){
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
            APIDefinitionValidationResponse validationResponse, String errMessage
    ){
        ErrorItem errorItem = new ErrorItem();
        errorItem.setMessage(errMessage);
        validationResponse.getErrorItems().add(errorItem);
        return errorItem;
    }
}
