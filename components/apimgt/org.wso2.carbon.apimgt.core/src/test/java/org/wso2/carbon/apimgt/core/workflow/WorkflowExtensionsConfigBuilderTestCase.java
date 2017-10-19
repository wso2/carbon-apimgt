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

package org.wso2.carbon.apimgt.core.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;

public class WorkflowExtensionsConfigBuilderTestCase {
    
    private static final Logger log = LoggerFactory.getLogger(WorkflowExtensionsConfigBuilderTestCase.class);

    @Test(description = "Test situation where workflow config file loading during a missing config file")
    public void testWorkflowConfigWithoutConfigFile() {
      
        WorkflowConfig config = WorkflowExtensionsConfigBuilder.getWorkflowConfig();  
        
        Assert.assertNotNull(config.getApplicationCreation(), "Default application creation workflow not set");
        Assert.assertNotNull(config.getSubscriptionCreation(), "Default subscription creation workflow not set");
        Assert.assertNotNull(config.getApplicationDeletion(), "Default application deletion workflow not set");
        Assert.assertNotNull(config.getSubscriptionDeletion(), "Default subscription deletion workflow not set");     
        
        WorkflowExtensionsConfigBuilder obj = new WorkflowExtensionsConfigBuilder();
        Assert.assertNotNull(obj);
        
    }

}
