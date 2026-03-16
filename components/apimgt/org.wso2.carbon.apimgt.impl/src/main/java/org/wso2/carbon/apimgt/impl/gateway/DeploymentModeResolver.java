/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package org.wso2.carbon.apimgt.impl.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves deployment target environment names into Synapse gateway labels and platform gateway IDs.
 * Used by the Publisher REST flow so that a single "deploy to these environments" request can
 * target both Synapse and platform gateways via {@link org.wso2.carbon.apimgt.impl.APIGatewayManager#deployToGateway}.
 * <p>
 * Uses a single batch lookup: {@code listGatewaysByOrganization(organization)} once, then in-memory
 * name -> gateway map so we avoid N per-name lookups. For each request name: if it is in the platform
 * map, add the gateway ID; otherwise treat as a Synapse label.
 */
public final class DeploymentModeResolver {

    private static final Log log = LogFactory.getLog(DeploymentModeResolver.class);

    private DeploymentModeResolver() {
    }

    /**
     * Resolves the given environment names into Synapse labels and platform gateway IDs for the organization.
     *
     * @param organization     tenant/organization domain
     * @param environmentNames selected environment names (e.g. from Publisher deploy request)
     * @return resolution result with {@link DeploymentTargets#getSynapseLabels()} and
     *         {@link DeploymentTargets#getPlatformGatewayIds()}
     */
    public static DeploymentTargets resolve(String organization, Set<String> environmentNames) {
        if (log.isInfoEnabled()) {
            log.info("Resolving deployment targets for organization: " + organization);
        }
        Set<String> synapseLabels = new HashSet<>();
        Set<String> platformGatewayIds = new HashSet<>();

        if (environmentNames == null || environmentNames.isEmpty()) {
            return new DeploymentTargets(synapseLabels, platformGatewayIds);
        }

        PlatformGatewayService platformGatewayService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();

        // Single batch lookup: one list call per org instead of N getGatewayByNameAndOrganization calls.
        Map<String, PlatformGateway> nameToPlatformGateway = null;
        if (platformGatewayService != null) {
            try {
                List<PlatformGateway> gateways = platformGatewayService.listGatewaysByOrganization(organization);
                if (gateways != null && !gateways.isEmpty()) {
                    nameToPlatformGateway = gateways.stream()
                            .filter(gw -> gw != null && StringUtils.isNotBlank(gw.getName()))
                            .collect(Collectors.toMap(gw -> gw.getName().trim(), gw -> gw, (a, b) -> a));
                }
            } catch (APIManagementException e) {
                log.warn("Failed to resolve platform gateway names, defaulting to Synapse labels", e);
            }
        }

        for (String name : environmentNames) {
            if (StringUtils.isBlank(name)) {
                continue;
            }
            String trimmed = name.trim();
            if (nameToPlatformGateway != null) {
                PlatformGateway gw = nameToPlatformGateway.get(trimmed);
                if (gw != null && gw.getId() != null) {
                    platformGatewayIds.add(gw.getId());
                    continue;
                }
            }
            synapseLabels.add(trimmed);
        }

        if (log.isDebugEnabled()) {
            log.debug("Resolved " + synapseLabels.size() + " Synapse labels and " + platformGatewayIds.size()
                    + " platform gateway IDs for organization: " + organization);
        }
        return new DeploymentTargets(synapseLabels, platformGatewayIds);
    }

    /**
     * Result of resolving deployment targets: Synapse gateway labels and platform gateway IDs.
     */
    public static final class DeploymentTargets {
        private final Set<String> synapseLabels;
        private final Set<String> platformGatewayIds;

        public DeploymentTargets(Set<String> synapseLabels, Set<String> platformGatewayIds) {
            this.synapseLabels = synapseLabels != null ? new HashSet<>(synapseLabels) : new HashSet<>();
            this.platformGatewayIds = platformGatewayIds != null ? new HashSet<>(platformGatewayIds) : new HashSet<>();
        }

        public Set<String> getSynapseLabels() {
            return new HashSet<>(synapseLabels);
        }

        public Set<String> getPlatformGatewayIds() {
            return new HashSet<>(platformGatewayIds);
        }
    }
}
