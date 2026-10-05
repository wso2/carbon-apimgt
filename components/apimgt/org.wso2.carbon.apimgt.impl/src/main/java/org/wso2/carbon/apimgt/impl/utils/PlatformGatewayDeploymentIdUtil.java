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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Generates a stable deployment identifier for a platform gateway deployment.
 */
public final class PlatformGatewayDeploymentIdUtil {

    private PlatformGatewayDeploymentIdUtil() {
    }

    public static String generate(String apiId, String gatewayEnvUuid, String revisionId) {
        if (StringUtils.isAnyBlank(apiId, gatewayEnvUuid, revisionId)) {
            return null;
        }
        String key = apiId.trim() + ":" + gatewayEnvUuid.trim() + ":" + revisionId.trim();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
