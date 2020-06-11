/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.generator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.endpoint.service.EndpointAdmin;
import org.wso2.carbon.localentry.service.LocalEntryAdmin;
import org.wso2.carbon.mediation.security.vault.MediationSecurityAdminService;
import org.wso2.carbon.rest.api.service.RestApiAdmin;
import org.wso2.carbon.sequences.services.SequenceAdmin;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private ConfigurationContextService cfgCtxService;
    private APIManagerConfigurationService amConfigService;
    public ThrottleDataHolder throttleDataHolder;
    private ThrottleProperties throttleProperties;
    private ConfigurationContext axis2ConfigurationContext;
    private TracingService tracingService;
    private ServerConfigurationService serverConfigurationService;
    private RestApiAdmin restAPIAdmin;
    private SequenceAdmin sequenceAdmin;
    private LocalEntryAdmin localEntryAdmin;
    private EndpointAdmin endpointAdmin;
    private MediationSecurityAdminService mediationSecurityAdminService;
    private ThrottleDataPublisher throttleDataPublisher;
    private Map<String,AbstractAPIMgtGatewayJWTGenerator> apiMgtGatewayJWTGenerators  = new HashMap<>();
    private TracingTracer tracer;
    private JWTValidationService jwtValidationService;
    public void setThrottleDataHolder(ThrottleDataHolder throttleDataHolder) {
        this.throttleDataHolder = throttleDataHolder;
    }
    public ThrottleDataHolder getThrottleDataHolder() {
        return throttleDataHolder;
    }
    private ArtifactRetriever artifactRetriever;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        this.cfgCtxService = cfgCtxService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return cfgCtxService;
    }

    public ConfigurationContext getServerConfigurationContext() {
        return cfgCtxService.getServerConfigContext();
    }

    public APIManagerConfiguration getAPIManagerConfiguration() {
        return amConfigService.getAPIManagerConfiguration();
    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {
        return amConfigService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
        if (amConfigService != null && amConfigService.getAPIManagerConfiguration() != null){
            setThrottleProperties(amConfigService.getAPIManagerConfiguration().getThrottleProperties());
        }
    }

    public ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }

    public void setThrottleProperties(ThrottleProperties throttleProperties) {
        this.throttleProperties = throttleProperties;
    }

    public void setAxis2ConfigurationContext(ConfigurationContext axis2ConfigurationContext) {
        this.axis2ConfigurationContext = axis2ConfigurationContext;
    }

    public ConfigurationContext getAxis2ConfigurationContext() {
        return axis2ConfigurationContext;
    }

    public TracingService getTracingService() {
        return tracingService;
    }
    public void setTracingService(TracingService tracingService) {
        this.tracingService = tracingService;
    }

    /**
     * To get the server configuration service.
     *
     * @return an instance of {@link ServerConfigurationService}
     */
    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    /**
     * To set the server configuration service.
     *
     * @param serverConfigurationService {@link ServerConfigurationService}
     */
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setRestAPIAdmin(RestApiAdmin restAPIAdmin) {
        this.restAPIAdmin = restAPIAdmin;

    }

    public RestApiAdmin getRestAPIAdmin() {

        return restAPIAdmin;
    }

    public void setSequenceAdmin(SequenceAdmin sequenceAdmin) {
        this.sequenceAdmin = sequenceAdmin;

    }

    public SequenceAdmin getSequenceAdmin() {

        return sequenceAdmin;
    }

    public void setLocalEntryAdmin(LocalEntryAdmin localEntryAdmin) {
        this.localEntryAdmin = localEntryAdmin;
    }

    public LocalEntryAdmin getLocalEntryAdmin() {

        return localEntryAdmin;
    }

    public EndpointAdmin getEndpointAdmin() {

        return endpointAdmin;
    }

    public void setEndpointAdmin(EndpointAdmin endpointAdmin) {

        this.endpointAdmin = endpointAdmin;
    }

    public MediationSecurityAdminService getMediationSecurityAdminService() {

        return mediationSecurityAdminService;
    }

    public void setMediationSecurityAdminService(MediationSecurityAdminService mediationSecurityAdminService) {

        this.mediationSecurityAdminService = mediationSecurityAdminService;
    }

    public ThrottleDataPublisher getThrottleDataPublisher() {

        return throttleDataPublisher;
    }

    public void setThrottleDataPublisher(ThrottleDataPublisher throttleDataPublisher) {

        this.throttleDataPublisher = throttleDataPublisher;
    }



    public Map<String,AbstractAPIMgtGatewayJWTGenerator> getApiMgtGatewayJWTGenerator() {

        return apiMgtGatewayJWTGenerators;
    }

    public TracingTracer getTracer() {

        return tracer;
    }

    public void setTracer(TracingTracer tracer) {

        this.tracer = tracer;
    }

    public JWTValidationService getJwtValidationService() {

        return jwtValidationService;
    }

    public void setJwtValidationService(JWTValidationService jwtValidationService) {

        this.jwtValidationService = jwtValidationService;
    }

    public ArtifactRetriever getArtifactRetriever() {

        return artifactRetriever;
    }

    public void setArtifactRetriever(ArtifactRetriever artifactRetriever) {

        this.artifactRetriever = artifactRetriever;
    }
}
