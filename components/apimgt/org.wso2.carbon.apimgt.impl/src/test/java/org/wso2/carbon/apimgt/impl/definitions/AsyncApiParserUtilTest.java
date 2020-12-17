package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.ErrorHandler;

import java.io.File;
import java.util.ArrayList;

public class AsyncApiParserUtilTest {

    @Test
    public void testValidateAsyncAPISpecification() throws Exception {
        // definition with no errors
        String asyncAPIDefinition = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions"  + File.separator + "asyncAPI" + File.separator + "sampleWebSocket.json"),
                "UTF-8");
        APIDefinitionValidationResponse validationResponse = AsyncApiParserUtil.validateAsyncAPISpecification(asyncAPIDefinition, true);
        Assert.assertTrue(validationResponse.isValid());
        Assert.assertNotNull(validationResponse.getJsonContent());

        // definition with definition errors
        String asyncAPIDefinition2 = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "asyncAPI" + File.separator + "sampleWebSocketWithDefErrors.json"),
                "UTF-8");
        APIDefinitionValidationResponse response2 = AsyncApiParserUtil.validateAsyncAPISpecification(asyncAPIDefinition2, true);
        Assert.assertFalse(response2.isValid());
        //Assert.assertNotNull(response2.getJsonContent());
        Assert.assertTrue(response2.getErrorItems().size() > 0);

        // definition with multiple channels and multiple servers
        String asyncAPIDefinition3 = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "asyncAPI" + File.separator + "sampleWebSocketWithCustomErrors.json"),
                "UTF-8");
        APIDefinitionValidationResponse response3 = AsyncApiParserUtil.validateAsyncAPISpecification(asyncAPIDefinition3, true);
        Assert.assertFalse(response3.isValid());
        Assert.assertTrue(response3.getErrorItems().size() > 0);
        ArrayList<String> errorMessages = new ArrayList<>();
        for (ErrorHandler errorItem : response3.getErrorItems()) {
            errorMessages.add(errorItem.getErrorMessage());
        }
        Assert.assertTrue(errorMessages.contains("#:The AsyncAPI definition should contain only a single server for websockets"));
        Assert.assertTrue(errorMessages.contains("#:The AsyncAPI definition should contain only a single channel for websockets"));
    }

    //@Test
    public void testValidateAsyncAPISpecificationByURL() throws Exception {
        //definition URL with no errors
        String definitionURL = "https://raw.githubusercontent.com/ZiyamSanthosh/AsyncAPI_WebSocket_Example/main/SampleWebSocket.yml";
        APIDefinitionValidationResponse validationResponse = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(definitionURL, true);
        Assert.assertTrue(validationResponse.isValid());
        Assert.assertNotNull(validationResponse.getJsonContent());

        //definition URL with errors
        String definitionURL2 = "https://raw.githubusercontent.com/ZiyamSanthosh/AsyncAPI_WebSocket_Example/main/SampleWebSocketWithErrors.yml";
        APIDefinitionValidationResponse validationResponse2 = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(definitionURL2, true);
        Assert.assertFalse(validationResponse2.isValid());
    }

}
