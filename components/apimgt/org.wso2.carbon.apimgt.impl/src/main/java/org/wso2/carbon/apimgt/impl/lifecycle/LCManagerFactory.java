/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class LCManagerFactory {

    private static final Log log = LogFactory.getLog(LCManagerFactory.class);
    private static LCManagerFactory instance;

    private LCManagerFactory() {

    }

    public static LCManagerFactory getInstance() {
        if (instance == null) {
            instance = new LCManagerFactory();
        }
        return instance;
    }

    public LCManager getLCManager() throws PersistenceException, APIManagementException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        LCManager lcManager ;
        lcManager = new LCManager(tenantDomain);
        return lcManager;
    }
}
