/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.workflow.events;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.databridge.agent.DataPublisher;

@Ignore("Ignore since event publishing is removed.")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, APIUtil.class, APIMgtWorkflowDataPublisher.class})
public class APIMgtWorkflowDataPublisherTestCase {

    private ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
    private APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
    private APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
    private APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration = Mockito.mock(APIManagerAnalyticsConfiguration.class);
    private DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);

    @Before public void setup() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfigurationService.getAPIAnalyticsConfiguration()).thenReturn(apiManagerAnalyticsConfiguration);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(true);
        PowerMockito.whenNew(DataPublisher.class).withAnyArguments().thenReturn(dataPublisher);
    }

    @Test public void testPublishEvent() throws Exception {
        APIMgtWorkflowDataPublisher apiMgtWorkflowDataPublisher = new APIMgtWorkflowDataPublisher();
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        Assert.assertTrue(apiMgtWorkflowDataPublisher.publishEvent(workflowDTO));
    }

    @Test public void testWhenAnalyticsDisabled() {
        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(false);
        APIMgtWorkflowDataPublisher apiMgtWorkflowDataPublisher = new APIMgtWorkflowDataPublisher();
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        apiMgtWorkflowDataPublisher.publishEvent(workflowDTO);
        Mockito.verify(dataPublisher, Mockito.times(0))
                .publish(Matchers.anyString(), Matchers.any(Object[].class), Matchers.any(Object[].class),
                        Matchers.any(Object[].class));
    }
}
