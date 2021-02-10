/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.workflow.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;


/*
* This class will act as data-publisher for workflow events.Reason for not re-using the usage
* publisher bundle is there's a maven cyclic dependency appears if we import it from impl bundle.
*/

public class APIMgtWorkflowDataPublisher {

    private static final Log log = LogFactory.getLog(APIMgtWorkflowDataPublisher.class);
    static APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIManagerConfiguration();
    static APIManagerAnalyticsConfiguration analyticsConfig = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIAnalyticsConfiguration();
    boolean enabled = APIUtil.isAnalyticsEnabled();
    boolean skipEventReceiverConnection = analyticsConfig.isSkipEventReceiverConnection();
    private static String wfStreamName;
    private static String wfStreamVersion;

    public APIMgtWorkflowDataPublisher() {
        if (!enabled || skipEventReceiverConnection || analyticsConfig.isSkipWorkFlowEventReceiverConnection()) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
        }

        wfStreamName = config.getFirstProperty(APIConstants.API_WF_STREAM_NAME);
        wfStreamVersion = config.getFirstProperty(APIConstants.API_WF_STREAM_VERSION);
        if (wfStreamName == null || wfStreamVersion == null) {
            log.error("Workflow stream name or version is null. Check api-manager.xml");
        }

    }

    public boolean publishEvent(WorkflowDTO workflowDTO) {
        // ignore data publishing
        return true;
    }

    public Object createPayload(WorkflowDTO workflowDTO) {
        return new Object[]{workflowDTO.getWorkflowReference(),
                            workflowDTO.getStatus().toString(), workflowDTO.getTenantDomain(),
                            workflowDTO.getWorkflowType(), workflowDTO.getCreatedTime(),
                            workflowDTO.getUpdatedTime()};
    }

    public static String getWFStreamName() {
        return wfStreamName;
    }

    public static String getStreamID() {
        return getWFStreamName() + ":" + getWFStreamVersion();
    }

    public static String getWFStreamVersion() {
        return wfStreamVersion;
    }

}
