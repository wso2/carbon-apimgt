/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.core.models;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.List;
/**
 * WorkflowExecutorInfo contains the configurations related to a specific workflow type
 */
@Configuration(description = "Workflow executor info")
public class WorkflowExecutorInfo {
    
    @Element(description = "executor class name")
    private String executor;
    
    @Element(description = "properties passed to the executor")
    private List<WorkflowConfigProperties> property;
    
    public String getExecutor() {
        return executor;
    }
    public void setExecutor(String executor) {
        this.executor = executor;
    }
    public List<WorkflowConfigProperties> getProperty() {
        return property;
    }
    public void setProperty(List<WorkflowConfigProperties> property) {
        this.property = property;
    }
    @Override
    public String toString() {
        return "WFExecutorInfo [executor=" + executor + ", property=" + property + "]";
    }    

}
