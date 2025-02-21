/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.identity.core.AbstractIdentityTenantMgtListener;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TenantListener extends AbstractIdentityTenantMgtListener {
    private static final Log log = LogFactory.getLog(TenantListener.class);

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        log.info("Tenant was created");
        log.info(tenantInfoBean);
    }
}
