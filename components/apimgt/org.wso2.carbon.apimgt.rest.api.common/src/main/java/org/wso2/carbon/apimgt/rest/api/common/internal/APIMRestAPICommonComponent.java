/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl;
import org.wso2.carbon.apimgt.rest.api.common.APIMConfigUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestAPIAuthenticator;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implemented for Setting APIM Configuration Service
 */
@Component(
        name = "org.wso2.apimgt.rest.api.common",
        immediate = true)
public class APIMRestAPICommonComponent {

    private static final Log log = LogFactory.getLog(APIMRestAPICommonComponent.class);
    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) {

        Map<String, JWTValidator> jwtValidatorMap = new HashMap<>();
        Map<String, TokenIssuerDto> tokenIssuerMap = APIMConfigUtil.getTokenIssuerMap();
        tokenIssuerMap.forEach((issuer, tokenIssuer) -> {
            JWTValidator jwtValidator = new JWTValidatorImpl();
            jwtValidator.loadTokenIssuerConfiguration(tokenIssuer);
            jwtValidatorMap.put(issuer, jwtValidator);
        });
        ServiceReferenceHolder.getInstance().setJwtValidatorMap(jwtValidatorMap);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY,
            policy = org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    @Reference(
            name = "rest.api.authentication.service",
            cardinality = ReferenceCardinality.MULTIPLE,
            service = RestAPIAuthenticator.class,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeRestAPIAuthenticationService"
    )
    protected void addRestAPIAuthenticationService(RestAPIAuthenticator authenticator) {
        ServiceReferenceHolder.getInstance().addAuthenticator(authenticator);
    }

    protected void removeRestAPIAuthenticationService(RestAPIAuthenticator authenticator) {
        ServiceReferenceHolder.getInstance().removeAuthenticator(authenticator);
    }
}
