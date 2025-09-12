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
        log.info("Activating APIM REST API Common Component");
        try {
            Map<String, JWTValidator> jwtValidatorMap = new HashMap<>();
            Map<String, TokenIssuerDto> tokenIssuerMap = APIMConfigUtil.getTokenIssuerMap();
            
            if (tokenIssuerMap == null) {
                log.warn("Token issuer map is null during component activation");
                tokenIssuerMap = new HashMap<>();
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Initializing JWT validators for " + tokenIssuerMap.size() + " token issuers");
            }
            
            tokenIssuerMap.forEach((issuer, tokenIssuer) -> {
                if (log.isDebugEnabled()) {
                    log.debug("Creating JWT validator for issuer: " + issuer);
                }
                JWTValidator jwtValidator = new JWTValidatorImpl();
                jwtValidator.loadTokenIssuerConfiguration(tokenIssuer);
                jwtValidatorMap.put(issuer, jwtValidator);
            });
            
            ServiceReferenceHolder.getInstance().setJwtValidatorMap(jwtValidatorMap);
            log.info("Successfully activated APIM REST API Common Component with " + jwtValidatorMap.size() 
                     + " JWT validators");
        } catch (Exception e) {
            log.error("Error occurred during APIM REST API Common Component activation", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("Deactivating APIM REST API Common Component");
        try {
            if (serviceRegistration != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Unregistering service registration");
                }
                serviceRegistration.unregister();
                log.info("Successfully unregistered service registration");
            }
            log.info("Successfully deactivated APIM REST API Common Component");
        } catch (Exception e) {
            log.error("Error occurred during APIM REST API Common Component deactivation", e);
        }
    }

    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY,
            policy = org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Binding APIM Configuration Service");
        }
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unbinding APIM Configuration Service");
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Binding REST API Authentication Service: " 
                      + (authenticator != null ? authenticator.getClass().getSimpleName() : "null"));
        }
        ServiceReferenceHolder.getInstance().addAuthenticator(authenticator);
    }

    protected void removeRestAPIAuthenticationService(RestAPIAuthenticator authenticator) {
        if (log.isDebugEnabled()) {
            log.debug("Unbinding REST API Authentication Service: " 
                      + (authenticator != null ? authenticator.getClass().getSimpleName() : "null"));
        }
        ServiceReferenceHolder.getInstance().removeAuthenticator(authenticator);
    }
}
