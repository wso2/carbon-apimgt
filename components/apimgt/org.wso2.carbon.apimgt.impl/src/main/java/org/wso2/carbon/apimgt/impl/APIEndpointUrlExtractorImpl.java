/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIEndpointUrlExtractor;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.api.model.endpointurlextractor.EndpointUrl;
import org.wso2.carbon.apimgt.api.model.endpointurlextractor.HostInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.VHostUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.utils.APIEndpointUrlExtractorUtils.getLoggedInUserConsumer;

/**
 * This class implements the API endpoint URLs extractor functionality.
 */
public class APIEndpointUrlExtractorImpl implements APIEndpointUrlExtractor {

    private static final Log log = LogFactory.getLog(APIEndpointUrlExtractorImpl.class);

    public APIEndpointUrlExtractorImpl() {
    }

    @Override
    public List<EndpointUrl> getApiEndpointUrlsForEnv(ApiTypeWrapper apiTypeWrapper, String organization,
                                                      String environmentName) throws APIManagementException {
        List<EndpointUrl> endpointUrls = new ArrayList<>();

        List<HostInfo> hostInfos = new ArrayList<>(
                getHostInfoForEnv(apiTypeWrapper, organization, environmentName));

        for (HostInfo hostInfo : hostInfos) {
            endpointUrls.addAll(getEndpointUrlsForHost(apiTypeWrapper, organization, hostInfo));
        }
        return endpointUrls;
    }

    /**
     * Get the list of host information specific to the given tenantDomain/organization and environment.
     *
     * @param apiTypeWrapper The API or APIProduct wrapper
     * @param organization The name of the organization
     * @param environmentName The name of the environment
     * @return List of host information specific to the given tenantDomain/organization and environment
     * @throws APIManagementException
     */
    protected List<HostInfo> getHostInfoForEnv(ApiTypeWrapper apiTypeWrapper, String organization,
                                               String environmentName) throws APIManagementException {
        List<HostInfo> hostInfos = new ArrayList<>();

        APIConsumer apiConsumer = getLoggedInUserConsumer();

        Map<String, String> domains = new HashMap<>();
        if (organization != null) {
            domains = APIUtil.getDomainMappings(organization,
                    APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
        }

        if (!domains.isEmpty()) {
            // Custom gateway URL of tenant/organization
            String customUrl = domains.get(APIConstants.CUSTOM_URL);

            if (!StringUtils.contains(customUrl, "://")) {
                customUrl = APIConstants.HTTPS_PROTOCOL_URL_PREFIX + customUrl;
            }

            VHost vHostFromCustomDomain = VHost.fromEndpointUrls(new String[]{customUrl});
            hostInfos.add(new HostInfo(vHostFromCustomDomain.getHost(), true));
        } else {
            Map<String, Environment> allEnvironments = APIUtil.getEnvironments(organization);
            Environment environment = allEnvironments.get(environmentName);

            if (environment == null) {
                throw new APIMgtResourceNotFoundException("Could not find provided environment '"
                        + environmentName + "'");
            }

            String host = "";

            List<APIRevisionDeployment> revisionDeployments =
                    apiConsumer.getAPIRevisionDeploymentListOfAPI(apiTypeWrapper.getUuid());
            for (APIRevisionDeployment revisionDeployment : revisionDeployments) {
                if (!revisionDeployment.isDisplayOnDevportal()) {
                    continue;
                }
                if (StringUtils.equals(revisionDeployment.getDeployment(), environmentName)) {
                    host = revisionDeployment.getVhost();
                }
            }

            VHost vHostFromEnvironment = VHostUtils.getVhostFromEnvironment(environment, host);
            hostInfos.add(new HostInfo(vHostFromEnvironment.getHost(), false));
        }
        return hostInfos;
    }

    /**
     * Get the API endpoint URLs specific to the given tenantDomain/organization and host information.
     *
     * @param apiTypeWrapper The API or APIProduct wrapper
     * @param organization The name of the organization
     * @param hostInfo The host information
     * @return List of endpoint URLs specific to the given tenantDomain/organization and host information
     * @throws APIManagementException
     */
    protected List<EndpointUrl> getEndpointUrlsForHost(ApiTypeWrapper apiTypeWrapper, String organization,
                                                       HostInfo hostInfo) throws APIManagementException {
        List<EndpointUrl> endpointUrls = new ArrayList<>();

        String context = apiTypeWrapper.getContext();
        Boolean isDefaultVersion = false;

        if (apiTypeWrapper.getIsDefaultVersion() != null && apiTypeWrapper.getIsDefaultVersion()) {
            context = context.replaceAll("/" + apiTypeWrapper.getVersion() + "$", "");
            isDefaultVersion = true;
        }

        boolean isWs = StringUtils.equalsIgnoreCase("WS", apiTypeWrapper.getType());
        boolean isGQLSubscription = StringUtils.equalsIgnoreCase("GRAPHQL", apiTypeWrapper.getType())
                && apiTypeWrapper.isGraphQLSubscriptionsAvailable();

        if (hostInfo.getIsCustomDomain()) {
            context = context.replace("/t/" + organization, "");
        }
        if (!isWs && !isGQLSubscription) {
            if (apiTypeWrapper.getTransports().contains(APIConstants.HTTP_PROTOCOL)) {
                String httpHost = hostInfo.getHost();
                String httpUrl = APIConstants.HTTP_PROTOCOL + "://" + httpHost + context;
                endpointUrls.add(
                        new EndpointUrl(httpUrl, httpHost, context, APIConstants.HTTP_PROTOCOL, isDefaultVersion));
            }
            if (apiTypeWrapper.getTransports().contains(APIConstants.HTTPS_PROTOCOL)) {
                String httpsHost = hostInfo.getHost();
                String httpsUrl = APIConstants.HTTPS_PROTOCOL + "://" + httpsHost + context;
                endpointUrls.add(
                        new EndpointUrl(httpsUrl, httpsHost, context, APIConstants.HTTPS_PROTOCOL, isDefaultVersion));
            }
        }
        if (isWs || isGQLSubscription) {
            String wsHost = hostInfo.getHost();
            String wsUrl = APIConstants.WS_PROTOCOL + "://" + wsHost + context;
            endpointUrls.add(
                    new EndpointUrl(wsUrl, wsHost, context, APIConstants.WS_PROTOCOL, isDefaultVersion));

            String wssHost = hostInfo.getHost();
            String wssUrl = APIConstants.WSS_PROTOCOL + "://" + wssHost + context;
            endpointUrls.add(
                    new EndpointUrl(wssUrl, wssHost, context, APIConstants.WSS_PROTOCOL, isDefaultVersion));
        }
        return endpointUrls;
    }
}
