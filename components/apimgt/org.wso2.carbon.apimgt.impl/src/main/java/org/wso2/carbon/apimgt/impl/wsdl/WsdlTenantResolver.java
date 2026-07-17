/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.wsdl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * Resolves the current tenant domain for gating WSDL/schema references through the network access-control
 * policy. Falls back to the super tenant domain when no CarbonContext is available on the current thread
 * (e.g. a non-request or pooled thread), so a missing thread-local degrades gracefully instead of failing
 * the parse/import. This only selects WHICH tenant's policy applies; it never bypasses the gate.
 */
final class WsdlTenantResolver {

    private static final Logger log = LoggerFactory.getLogger(WsdlTenantResolver.class);

    private WsdlTenantResolver() {
    }

    static String resolveTenantDomain() {
        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            return StringUtils.isBlank(tenantDomain) ? MultitenantConstants.SUPER_TENANT_DOMAIN_NAME : tenantDomain;
        } catch (NullPointerException e) {
            log.warn("CarbonContext tenant domain was unavailable while resolving the tenant for WSDL/schema "
                    + "reference policy evaluation; falling back to the super tenant domain ("
                    + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME + ").");
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
    }
}
