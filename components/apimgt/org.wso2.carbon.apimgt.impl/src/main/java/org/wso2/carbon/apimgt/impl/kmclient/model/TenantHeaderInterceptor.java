/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.kmclient.model;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * This class used to send X-WSO2-Tenant header into WSO2 IS rest APIS  in order to authenticate the tenant admin.
 */
public class TenantHeaderInterceptor implements RequestInterceptor {

    private final String tenantDomain;

    public TenantHeaderInterceptor(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {

        requestTemplate
                .header(APIConstants.X_WSO2_TENANT_HEADER, tenantDomain);
    }
}
