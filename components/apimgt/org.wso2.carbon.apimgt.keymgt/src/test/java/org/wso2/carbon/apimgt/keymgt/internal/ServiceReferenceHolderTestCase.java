/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.wso2.carbon.apimgt.keymgt.internal;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;

public class ServiceReferenceHolderTestCase {

    @Test
    public void testGetInstance() throws Exception {

        ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
        Assert.assertTrue(serviceReferenceHolder instanceof ServiceReferenceHolder);
    }

    @Test
    public void testAPIManagerConfigurationService() throws Exception {

        ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
        serviceReferenceHolder.setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (new APIManagerConfiguration()));
        APIManagerConfigurationService apiManagerConfigurationService = serviceReferenceHolder
                .getAPIManagerConfigurationService();
        Assert.assertTrue(apiManagerConfigurationService instanceof APIManagerConfigurationService);
    }


}