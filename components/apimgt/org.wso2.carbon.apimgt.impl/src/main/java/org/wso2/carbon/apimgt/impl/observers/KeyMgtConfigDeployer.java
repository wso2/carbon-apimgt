/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.apimgt.impl.service.KeyMgtRegistrationService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This task provisions the OAuth application for the key manager management operations needed by any tenant.
 */
public class KeyMgtConfigDeployer extends AbstractAxis2ConfigurationContextObserver {

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {

        //TODO: Only register when API-M KeyManager Profile is used as the KM.
    }
}
