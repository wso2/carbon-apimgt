/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.common.internal;

import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.common.impl.OAuth2Authenticator;
import org.wso2.carbon.auth.rest.api.authenticators.RestAPIConstants;
import org.wso2.carbon.auth.rest.api.authenticators.SecurityConfigurationService;
import org.wso2.carbon.auth.rest.api.authenticators.dto.RestAPIInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Activator to register Swagger Definitions
 */
@Component(
        name = "org.wso2.carbon.apimgt.rest.api.commons",
        immediate = true
)
public class BundleActivator {

    private SecurityConfigurationService securityConfigurationService;
    private static final Logger log = LoggerFactory.getLogger(BundleActivator.class);
    private static final String[] definitions = {"publisher-api", "store-api", "admin-api", "analytics-api"};

    @Activate
    protected void start(BundleContext bundleContext) {

        initializeBasePaths();

    }

    protected void initializeBasePaths() {

        for (String definition : definitions) {
            try {
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream
                        (definition + ".yaml");
                String swaggerYaml = IOUtils.toString(inputStream);
                Swagger swagger = new SwaggerParser().parse(swaggerYaml);
                RestAPIInfo restAPIInfo = new RestAPIInfo(swagger.getBasePath(), swagger, swaggerYaml);
                Map<String, RestAPIInfo> restAPIInfoMap = securityConfigurationService.getRestAPIInfoMap();
                if (!restAPIInfoMap.containsKey(restAPIInfo.getBasePath())) {
                    restAPIInfoMap.put(restAPIInfo.getBasePath(), restAPIInfo);
                }
                Map<String, String> restapiAuthenticatorMap = new HashMap<>();
                if (swagger.getSecurityDefinitions() != null) {
                    Set<Map.Entry<String, SecuritySchemeDefinition>> securitySchemeDefinitionEntrySet = swagger
                            .getSecurityDefinitions().entrySet();
                    for (Map.Entry<String, SecuritySchemeDefinition> securitySchemeDefinitionEntry :
                            securitySchemeDefinitionEntrySet) {
                        if (securitySchemeDefinitionEntry.getValue() instanceof OAuth2Definition) {
                            restapiAuthenticatorMap.put(RestAPIConstants.AUTH_TYPE_OAUTH2, OAuth2Authenticator.class
                                    .getName());
                        }
                    }
                }
                Map<String, Map<String, String>> securityConfigurationMap =
                        securityConfigurationService.getSecurityConfiguration()
                                .getAuthenticator();
                if (!securityConfigurationMap.containsKey(swagger.getBasePath())) {
                    securityConfigurationMap.put(swagger.getBasePath(), restapiAuthenticatorMap);
                }
            } catch (IOException e) {
                log.error("Error while reading swagger definition " + definition + ".yaml", e);
            }
        }

    }

    /**
     * Get the ConfigProvider service.
     *
     * @param securityConfigurationService
     */
    @Reference(
            name = "org.wso2.carbon.auth.rest.api.authenticators.SecurityConfigurationService",
            service = SecurityConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecurityConfigurationService"
    )
    protected void setSecurityConfigurationService(SecurityConfigurationService securityConfigurationService) {

        ServiceReferenceHolder.getInstance().setSecurityConfigurationService(securityConfigurationService);
        this.securityConfigurationService = securityConfigurationService;
    }

    protected void unsetSecurityConfigurationService(SecurityConfigurationService securityConfigurationService) {

        ServiceReferenceHolder.getInstance().setSecurityConfigurationService(null);
        this.securityConfigurationService = null;
    }
}
