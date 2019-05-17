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

import org.json.simple.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherUtils;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.UploadedUsagePublisher;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao.UploadedUsageFileInfoDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dto.UploadedFileInfoDTO;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyInt;

/**
 * UploadedUsagePublisherExecutorTask Test
 */
@RunWith(PowerMockRunner.class) @PrepareForTest({ CarbonUtils.class, UsagePublisherUtils.class,
        UploadedUsageFileInfoDAO.class,
        UploadedUsagePublisher.class })
public class UploadedUsagePublisherExecutorTaskTest {

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test public void run() throws Exception {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        Map<String, JSONArray> streamDefinitions = null;
        List<UploadedFileInfoDTO> uploadedFileList = new ArrayList<>();
        PowerMockito.mockStatic(UsagePublisherUtils.class);
        PowerMockito.mockStatic(UploadedUsageFileInfoDAO.class);
        PowerMockito.when(UploadedUsageFileInfoDAO.getNextFilesToProcess(anyInt())).thenReturn(uploadedFileList);
        PowerMockito.when(UsagePublisherUtils.getDataPublisher()).thenReturn(dataPublisher);
        PowerMockito.when(UsagePublisherUtils.getStreamDefinitions()).thenReturn(streamDefinitions);
        UploadedUsagePublisherExecutorTask usagePublisherExecutorTask = new UploadedUsagePublisherExecutorTask();
        usagePublisherExecutorTask.run();
    }

}
