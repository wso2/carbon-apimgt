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

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * APIUsageFileCleanupTaskTest Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class})
public class APIUsageFileCleanupTaskTest {
    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void setProperties() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("fileRetentionDays", "20");
        APIUsageFileCleanupTask task = new APIUsageFileCleanupTask();
        task.setProperties(map);
    }

    @Test
    public void init() throws Exception {
        APIUsageFileCleanupTask task = new APIUsageFileCleanupTask();
        task.init();
    }

    @Test
    public void execute() throws Exception {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        APIUsageFileCleanupTask task = new APIUsageFileCleanupTask();
        Map<String, String> map = new HashMap<>();
        map.put("fileRetentionDays", "20");
        task.setProperties(map);
        task.execute();
    }

}
