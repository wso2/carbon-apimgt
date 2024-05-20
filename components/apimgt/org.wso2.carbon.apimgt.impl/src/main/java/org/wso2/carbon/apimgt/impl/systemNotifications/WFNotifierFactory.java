/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.systemNotifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;

public class WFNotifierFactory {

    private static final Log log = LogFactory.getLog(WFNotifierFactory.class);

    public static AbstractWFNotifier getNotifier(String notifierType) {
        if (notifierType == null) {
            return null;
        }
        if (notifierType.equals(WorkflowConstants.WF_TYPE_AM_API_STATE)) {
            return new APIStateChangeWFNotifier();
        } else if (notifierType.equals(WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE)) {
            return new APIProductStateChangeWFNotifier();
        } else if (notifierType.equalsIgnoreCase(WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT)) {
            return new APIRevisionDeploymentWFNotifier();
        } else if (notifierType.equalsIgnoreCase(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION)) {
            return new ApplicationCreationWFNotifier();
        } else if (notifierType.equalsIgnoreCase(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION)) {
            return new ApplicationRegistrationProductionWFNotifier();
        } else if (notifierType.equalsIgnoreCase(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX)) {
            return new ApplicationRegistrationSandboxWFNotifier();
        } else if (notifierType.equalsIgnoreCase(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION)) {
            return new SubscriptionCreationWFNotifier();
        } else if (notifierType.equalsIgnoreCase(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE)) {
            return new SubscriptionUpdateWFNotifier();
        } else if (notifierType.equalsIgnoreCase(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION)) {
            return new SubscriptionDeletionWFNotifier();
        }
        return null;
    }
}
