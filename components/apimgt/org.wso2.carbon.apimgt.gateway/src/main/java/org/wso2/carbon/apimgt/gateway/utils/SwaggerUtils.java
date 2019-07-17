/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

public class SwaggerUtils {

    /**
     * Return the resource authentication scheme of the API resource.
     *
     * @param swagger swagger of the API
     * @param synCtx  The message containing resource request
     * @return the resource authentication scheme
     */
    public static String getResourceAuthenticationScheme(Swagger swagger, MessageContext synCtx) {
        String authType = null;
        Map<String, Object> vendorExtensions = getVendorExtensions(synCtx, swagger);
        if (vendorExtensions != null) {
            authType = (String) vendorExtensions.get(APIConstants.SWAGGER_X_AUTH_TYPE);
        }

        if (StringUtils.isNotBlank(authType)) {
            if (APIConstants.OASResourceAuthTypes.APPLICATION_OR_APPLICATION_USER.equals(authType)) {
                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
            } else if (APIConstants.OASResourceAuthTypes.APPLICATION_USER.equals(authType)) {
                authType = APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
            } else if (APIConstants.OASResourceAuthTypes.NONE.equals(authType)) {
                authType = APIConstants.AUTH_NO_AUTHENTICATION;
            } else if (APIConstants.OASResourceAuthTypes.APPLICATION.equals(authType)) {
                authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
            } else {
                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
            }
            return authType;
        }
        return APIConstants.NO_MATCHING_AUTH_SCHEME;
    }

    /**
     * Return the scopes bound to the API resource.
     *
     * @param swagger swagger of the API
     * @param synCtx  The message containing resource request
     * @return the scopes
     */
    public static String getScopesOfResource(Swagger swagger, MessageContext synCtx) {
        Map<String, Object> vendorExtensions = getVendorExtensions(synCtx, swagger);
        if (vendorExtensions != null) {
            return  (String) vendorExtensions.get(APIConstants.SWAGGER_X_SCOPE);
        }
        return null;
    }

    /**
     * Return the throttling tier of the API resource.
     *
     * @param swagger swagger of the API
     * @param synCtx  The message containing resource request
     * @return the resource throttling tier
     */
    public static String getResourceThrottlingTier(Swagger swagger, MessageContext synCtx) {
        String throttlingTier = null;
        Map<String, Object> vendorExtensions = getVendorExtensions(synCtx, swagger);
        if (vendorExtensions != null) {
            throttlingTier = (String) vendorExtensions.get(APIConstants.SWAGGER_X_THROTTLING_TIER);
        }
        if (StringUtils.isNotBlank(throttlingTier)) {
            return throttlingTier;
        }
        return APIConstants.UNLIMITED_TIER;
    }

    private static Map<String, Object> getVendorExtensions(MessageContext synCtx, Swagger swagger) {
        if (swagger != null) {
            String apiElectedResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            String httpMethod = (String) axis2MessageContext.getProperty(APIConstants.DigestAuthConstants.HTTP_METHOD);
            Path path = swagger.getPath(apiElectedResource);
            if (path != null) {
                switch (httpMethod) {
                    case APIConstants.HTTP_GET:
                        return path.getGet().getVendorExtensions();
                    case APIConstants.HTTP_POST:
                        return path.getPost().getVendorExtensions();
                    case APIConstants.HTTP_PUT:
                        return path.getPut().getVendorExtensions();
                    case APIConstants.HTTP_DELETE:
                        return path.getDelete().getVendorExtensions();
                    case APIConstants.HTTP_HEAD:
                        return path.getHead().getVendorExtensions();
                    case APIConstants.HTTP_OPTIONS:
                        return path.getOptions().getVendorExtensions();
                    case APIConstants.HTTP_PATCH:
                        return path.getPatch().getVendorExtensions();
                }
            }
        }
        return null;
    }
}
