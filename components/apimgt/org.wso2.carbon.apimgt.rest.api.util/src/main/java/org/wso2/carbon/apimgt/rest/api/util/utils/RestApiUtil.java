/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorListItemDTO;
import org.wso2.carbon.context.CarbonContext;

import javax.validation.ConstraintViolation;
import java.util.*;

public class RestApiUtil {

    private static final Log log = LogFactory.getLog(RestApiUtil.class);

    public static APIProvider getProvider() throws APIManagementException {
        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        return APIManagerFactory.getInstance().getAPIProvider(loggedInUser);
    }

    public static <T> ErrorDTO getConstraintViolationErrorDTO(Set<ConstraintViolation<T>> violations) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage("Constraint Violation");
        List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<>();
        for (ConstraintViolation violation : violations) {
            ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
            errorListItemDTO.setMessage(violation.getPropertyPath() + ": " + violation.getMessage());
            errorListItemDTOs.add(errorListItemDTO);
        }
        errorDTO.setError(errorListItemDTOs);
        return errorDTO;
    }

    public static boolean isUUID(String apiId) {
        try {
            UUID.fromString(apiId);
            return true;
        } catch (IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug(apiId + " is not a valid UUID");
            }
            return false;
        }

    }

    public static ErrorDTO getAuthenticationErrorDTO(String message) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(message);
        return errorDTO;
    }

    public static APIConsumer getConsumer(String subscriberName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    public static String getLoggedInUsername() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public static String getLoggedInUserTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Following 3 methods are temporary added to rest API Util
     * Ideally they should move to DCR, RR and Introspection API implementation
     *
     * @param api
     * @param swagger
     * @return
     */
    public static boolean registerResource(API api, String swagger) {

        APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();
        Set<URITemplate> uriTemplates = null;
        try {
            uriTemplates = definitionFromSwagger20.getURITemplates(api, swagger);
        } catch (APIManagementException e) {
            log.error("Error while parsing swagger content to get URI Templates" + e.getMessage());
        }
        api.setUriTemplates(uriTemplates);
        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        Map registeredResource = null;
        try {
            registeredResource = keyManager.getResourceByApiId(api.getId().toString());
        } catch (APIManagementException e) {
            log.error("Error while getting registered resources for API: " + api.getId().toString() + e.getMessage());
        }
        //Add new resource if not exist
        if (registeredResource == null) {
            boolean isNewResourceRegistered = false;
            try {
                isNewResourceRegistered = keyManager.registerNewResource(api, null);
            } catch (APIManagementException e) {
                log.error("Error while registering new resource for API: " + api.getId().toString() + e.getMessage());
            }
            if (!isNewResourceRegistered) {
                log.error("New resource not registered for API: " + api.getId());
            }
        }
        //update existing resource
        else {
            String resourceId = (String) registeredResource.get("resourceId");
            try {
                keyManager.updateRegisteredResource(api, registeredResource);
            } catch (APIManagementException e) {
                log.error("Error while updating resource");
            }
        }
        return true;
    }

    public static OAuthApplicationInfo registerOAuthApplication(OAuthAppRequest appRequest) {
        //Create Oauth Application - Dynamic client registration service
        AMDefaultKeyManagerImpl impl = new AMDefaultKeyManagerImpl();
        OAuthApplicationInfo returnedAPP = null;
        try {
            returnedAPP = impl.createApplication(appRequest);
        } catch (APIManagementException e) {
            log.error("Cannot create OAuth application from provided information, for APP name: " +
                    appRequest.getOAuthApplicationInfo().getClientName());
        }
        return returnedAPP;
    }

    public static OAuthApplicationInfo retrieveOAuthApplication(String consumerKey) {
        //Create Oauth Application - Dynamic client registration service
        AMDefaultKeyManagerImpl impl = new AMDefaultKeyManagerImpl();
        OAuthApplicationInfo returnedAPP = null;
        try {
            returnedAPP = impl.retrieveApplication(consumerKey);
        } catch (APIManagementException e) {
            log.error("Error while retrieving OAuth application information for Consumer Key: " + consumerKey);
        }
        return returnedAPP;
    }

}
