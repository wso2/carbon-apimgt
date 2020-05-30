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

package org.wso2.carbon.apimgt.impl.endpoint.registry.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;

import java.io.IOException;
import java.net.URL;

/**
 * This class provides the functionality of validating OpenAPI definitions
 */
public class OASDefinitionValidator implements DefinitionValidator {
    private static final Log log = LogFactory.getLog(OASDefinitionValidator.class);

    @Override
    public boolean validate(URL definitionUrl) {
        boolean isValid = false;
        try {
            String definitionContent = IOUtils.toString(definitionUrl.openStream());
            isValid = validate(definitionContent);
        } catch (IOException e) {
            log.error("Error in reading content in the definition URL: " + definitionUrl, e);
        }
        return isValid;
    }

    @Override
    public boolean validate(byte[] definition) {
        String definitionContent = new String(definition);
        return validate(definitionContent);
    }

    @Override
    public boolean validate(String definition) {
        boolean isValid = false;
        try {
            APIDefinitionValidationResponse response =
                    OASParserUtil.validateAPIDefinition(definition, false);
            isValid = response.isValid();
        } catch (APIManagementException | ClassCastException e) {
            log.error("Unable to parse the OpenAPI definition", e);
        }
        return isValid;
    }
}
