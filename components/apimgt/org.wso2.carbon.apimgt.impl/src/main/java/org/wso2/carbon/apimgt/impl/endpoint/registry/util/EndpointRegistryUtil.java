/*
 *
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.impl.endpoint.registry.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.DefinitionValidationException;
import org.wso2.carbon.apimgt.impl.endpoint.registry.constants.EndpointRegistryConstants;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EndpointRegistryUtil {

    private static final Log log = LogFactory.getLog(EndpointRegistryUtil.class);
    private static final Map<String, DefinitionValidator> definitionValidatorMap = new HashMap<>();

    static {
        definitionValidatorMap.put(EndpointRegistryConstants.DEFINITION_TYPE_OAS, new OASDefinitionValidator());
        definitionValidatorMap.put(EndpointRegistryConstants.DEFINITION_TYPE_WSDL1, new WSDLDefinitionValidator());
        definitionValidatorMap.put(EndpointRegistryConstants.DEFINITION_TYPE_WSDL2, new WSDLDefinitionValidator());
        definitionValidatorMap.put(EndpointRegistryConstants.DEFINITION_TYPE_GQL_SDL, new GraphQLDefinitionValidator());
    }

    public static void raiseResourceAlreadyExistsException(String msg) throws
            EndpointRegistryResourceAlreadyExistsException {

        log.error(msg);
        throw new EndpointRegistryResourceAlreadyExistsException(msg);
    }

    public static boolean isValidDefinition(URL definitionURL, String definitionType)
            throws DefinitionValidationException {
        DefinitionValidator definitionValidator = definitionValidatorMap.get(definitionType);
        if (definitionValidator != null) {
            return definitionValidator.validate(definitionURL);
        }
        throw new DefinitionValidationException("No Definition Validator found for the given definition type: '"
                + definitionType + "'");
    }

    public static boolean isValidDefinition(byte[] definitionContent, String definitionType)
            throws DefinitionValidationException {
        DefinitionValidator definitionValidator = definitionValidatorMap.get(definitionType);
        if (definitionValidator != null) {
            return definitionValidator.validate(definitionContent);
        }
        throw new DefinitionValidationException("No Definition Validator found for the given definition type: '"
                + definitionType + "'");
    }

}
