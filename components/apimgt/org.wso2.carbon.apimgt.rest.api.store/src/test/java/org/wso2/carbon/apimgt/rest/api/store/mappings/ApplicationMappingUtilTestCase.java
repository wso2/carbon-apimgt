/*
 *
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
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApplicationMappingUtilTestCase {


    @Test
    public void testFromApplicationsToDTO() {

        Application application1 = new Application("application1", "user1");
        application1.setId(UUID.randomUUID().toString());
        application1.setDescription("application 1");
        application1.setStatus("ACTIVE");
        application1.setPolicy(new APIPolicy("GOLD"));

        Application application2 = new Application("application2", "user1");
        application2.setId(UUID.randomUUID().toString());
        application2.setDescription("application 2");
        application2.setStatus("ACTIVE");
        application2.setPolicy(new APIPolicy("GOLD"));

        Application application3 = new Application("application3", "user1");
        application3.setId(UUID.randomUUID().toString());
        application3.setDescription("application 3");
        application3.setStatus("ACTIVE");
        application3.setPolicy(new APIPolicy("GOLD"));

        List<Application> applicationList = new ArrayList<>();
        applicationList.add(application1);
        applicationList.add(application2);
        applicationList.add(application3);

        ApplicationMappingUtil applicationMappingUtil = new ApplicationMappingUtil();
        ApplicationListDTO applicationListDTO =
                applicationMappingUtil.fromApplicationsToDTO(applicationList, 10, 0);

        Assert.assertEquals(applicationListDTO.getList().get(0).getName(), "application1");
    }

    @Test
    public void testFromApplicationToDTO() {

        String applicationID = UUID.randomUUID().toString();

        Application application1 = new Application("application1", "user1");
        application1.setId(applicationID);
        application1.setDescription("application 1");
        application1.setStatus("ACTIVE");
        application1.setPolicy(new APIPolicy("GOLD"));

        ApplicationMappingUtil applicationMappingUtil = new ApplicationMappingUtil();

        ApplicationDTO applicationDTO = applicationMappingUtil.fromApplicationToDTO(application1);

        Assert.assertEquals(applicationDTO.getApplicationId(), applicationID);
    }

    @Test
    public void testFromDTOtoApplication() {

        String applicationID = UUID.randomUUID().toString();

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(applicationID);
        applicationDTO.setName("application123");
        applicationDTO.setThrottlingTier("GOLD");
        applicationDTO.setDescription("sample application");
        applicationDTO.setPermission("No Permissions");
        applicationDTO.setLifeCycleStatus("PUBLISHED");

        ApplicationMappingUtil applicationMappingUtil = new ApplicationMappingUtil();

        Application application = applicationMappingUtil.fromDTOtoApplication(applicationDTO, "user1");

        Assert.assertEquals(application.getId(), applicationID);
    }
}
