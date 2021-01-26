package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.tasks;

/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao.UploadedUsageFileInfoDAO;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;

/**
 * Uploaded Usage Cleanup Task
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, UploadedUsageFileInfoDAO.class})
public class UploadedUsageCleanUpTaskTest {

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void setProperties() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("fileRetentionDays", "20");
        UploadedUsageCleanUpTask task = new UploadedUsageCleanUpTask();
        task.setProperties(map);
    }

    @Test
    public void init() throws Exception {
        UploadedUsageCleanUpTask task = new UploadedUsageCleanUpTask();
        task.init();
    }

    @Test public void execute() throws Exception {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        PowerMockito.spy(UploadedUsageFileInfoDAO.class);
        PowerMockito.doNothing().when(UploadedUsageFileInfoDAO.class, "deleteProcessedOldFiles", any(Date.class));
        UploadedUsageCleanUpTask task = new UploadedUsageCleanUpTask();
        Map<String, String> map = new HashMap<>();
        map.put("fileRetentionDays", "20");
        task.setProperties(map);
        task.execute();
    }

}
