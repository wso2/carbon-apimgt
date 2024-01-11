/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.api.model.Environment;

public class VHostUtils {

    /**
     * Get VHost object of given environment and given host name
     *
     * @param environment Gateway environment name
     * @param host Host name of the VHost
     * @return VHost object of the given host
     */
    public static VHost getVhostFromEnvironment(Environment environment, String host) {
        // If there are any inconstancy (if VHost not found) use default VHost
        VHost defaultVhost = new VHost();
        defaultVhost.setHost(host);
        defaultVhost.setHttpContext("");
        defaultVhost.setHttpsPort(APIConstants.HTTPS_PROTOCOL_PORT);
        defaultVhost.setHttpPort(APIConstants.HTTP_PROTOCOL_PORT);
        defaultVhost.setWsPort(APIConstants.WS_PROTOCOL_PORT);
        defaultVhost.setWsHost(host);
        defaultVhost.setWssPort(APIConstants.WSS_PROTOCOL_PORT);
        defaultVhost.setWssHost(host);

        if (host == null && environment.getVhosts().size() > 0) {
            // VHost is NULL set first Vhost (set in deployment toml)
            return environment.getVhosts().get(0);
        }

        return environment.getVhosts().stream()
                .filter(v -> StringUtils.equals(v.getHost(), host))
                .findAny()
                .orElse(defaultVhost);
    }

    /**
     * Resolve vhost to default vhost if the given vhost is null
     *
     * @param environmentName Environment name
     * @param vhost Host of the vhost
     * @return Resolved vhost
     * @throws APIManagementException if failed to find the read only environment
     */
    public static String resolveIfNullToDefaultVhost(String environmentName, String vhost) throws APIManagementException {
        if (StringUtils.isEmpty(vhost)) {
            return APIUtil.getDefaultVhostOfReadOnlyEnvironment(environmentName).getHost();
        }
        return vhost;
    }

    /**
     * Resolve vhost to null if the given vhost is the default (first) vhost of read only environment
     *
     * @param environmentName Environment name
     * @param vhost Host of the vhost
     * @return Resolved vhost
     * @throws APIManagementException if failed to find the read only environment
     */
    public static String resolveIfDefaultVhostToNull(String environmentName, String vhost) throws APIManagementException {
        // set VHost as null, if it is the default vhost of the read only environment
        if (APIUtil.getReadOnlyEnvironments().get(environmentName) != null
                && StringUtils.equalsIgnoreCase(vhost,
                APIUtil.getDefaultVhostOfReadOnlyEnvironment(environmentName).getHost())) {
            return null;
        }
        return vhost;
    }
}
