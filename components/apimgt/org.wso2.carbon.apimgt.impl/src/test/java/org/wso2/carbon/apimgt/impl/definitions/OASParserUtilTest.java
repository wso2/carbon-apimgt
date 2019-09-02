package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIDefinition;

import java.io.File;
import java.util.Optional;

public class OASParserUtilTest {

    @Test
    public void testGetOASParser() throws Exception {
        String oas3 = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v3.yaml"),
                        "UTF-8");
        Optional<APIDefinition> optional = OASParserUtil.getOASParser(oas3);
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(optional.get() instanceof OAS3Parser);

        String oas2 = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v2.yaml"),
                        "UTF-8");
        optional = OASParserUtil.getOASParser(oas2);
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(optional.get() instanceof OAS2Parser);

        String oasError = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_error.json"),
                        "UTF-8");
        optional = OASParserUtil.getOASParser(oasError);
        Assert.assertFalse(optional.isPresent());

        String oasInvalid = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_invalid.yaml"),
                        "UTF-8");
        optional = OASParserUtil.getOASParser(oasInvalid);
        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testValidateAPIDefinition() {
    }

    @Test
    public void testUpdateValidationResponseAsSuccess() {
    }

    @Test
    public void testAddErrorToValidationResponse() {
    }

    @Test
    public void testGetSwaggerJsonString() {
    }

    @Test
    public void testValidateAPIDefinitionByURL() {
    }
}