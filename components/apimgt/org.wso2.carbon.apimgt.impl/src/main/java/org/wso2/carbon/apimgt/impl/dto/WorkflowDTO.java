/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dto;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the DTO that will be used for storing workflow related contextual information.
 */
public class WorkflowDTO extends org.wso2.carbon.apimgt.api.dto.WorkflowDTO {
    public WorkflowDTO(){
        super();
    }
    private WorkflowStatus status;

    public WorkflowStatus getStatus() {
        return status;
    }
    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }
}
