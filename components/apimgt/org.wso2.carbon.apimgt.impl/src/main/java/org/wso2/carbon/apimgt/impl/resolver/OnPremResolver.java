/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.resolver;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtBadRequestException;
import org.wso2.carbon.apimgt.api.APIMgtInternalException;
import org.wso2.carbon.apimgt.api.OrganizationResolver;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

public class OnPremResolver implements OrganizationResolver {
    public static final String HEADER_X_WSO2_TENANT = "x-wso2-tenant";

    @Override
    public String resolve(Map<String, Object> properties) throws APIManagementException {
        ArrayList requestedTenantDomain = (ArrayList) ((TreeMap) (properties.get(APIConstants.PROPERTY_HEADERS_KEY)))
                .get(HEADER_X_WSO2_TENANT);
        
        String tenantDomain = null;
        if (requestedTenantDomain != null) {
            String header = requestedTenantDomain.get(0).toString();
            if (StringUtils.isEmpty(header)) {
                tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            } else {
                tenantDomain = header;
            }
            try {
                if (!APIUtil.isTenantAvailable(tenantDomain)) {
                    String errorMessage = "Provided tenant domain '" + tenantDomain + "' is invalid";
                    throw new APIMgtBadRequestException(errorMessage);

                }
            } catch (UserStoreException  e) {
                String errorMessage = "Error while checking availability of tenant " + tenantDomain;
                throw new APIMgtInternalException(errorMessage);
            }
        }
        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        // Set "carbon.super" if tenantDomain is still not resolved.
        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
        }
        return tenantDomain;
    }

    @Override
    public int getInternalId(String organization) throws APIManagementException {
        int tenantId;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(organization);
        } catch (UserStoreException e) {
            throw new APIMgtInternalException("Error while accessing tenant manager ", e);
        }
        return tenantId;
    }

}
