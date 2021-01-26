/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.internal;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;


/**
 *  ServiceReferenceHolder Test Class
 *
 */
public class ServiceReferenceHolderTest {

    @Test
    public void getInstance() throws Exception {
        ServiceReferenceHolder.getInstance();
    }

    @Test
    public void getRealmService() throws Exception {
        ServiceReferenceHolder.getInstance().getRealmService();
    }

    @Test
    public void getAPIManagerConfigurationService() throws Exception {
        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
    }

    @Test public void setAPIManagerConfigurationService() throws Exception {
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
    }

    @Test
    public void setTaskService() throws Exception {
        TaskService taskService = Mockito.mock(TaskService.class);
        ServiceReferenceHolder.getInstance().setTaskService(taskService);
    }

    @Test
    public void getTaskService() throws Exception {
        ServiceReferenceHolder.getInstance().getTaskService();
    }

    @Test
    public void setRealmService() throws Exception {
        RealmService realmService = Mockito.mock(RealmService.class);
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

}
