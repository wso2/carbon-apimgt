/*
* Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/
package org.wso2.carbon.throttle.module.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.throttle.module.ThrottleEnguageUtils;
import org.wso2.carbon.throttle.core.ThrottleConstants;


public class OperationLevelThrottleHandler extends ThrottleHandler {


    public int getThrottleType() {
        return ThrottleConstants.OPERATION_BASED_THROTTLE;
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (msgContext.isEngaged(ThrottleConstants.THROTTLE_MODULE_NAME) &&
                !ThrottleEnguageUtils.isFilteredOutService(msgContext.getAxisServiceGroup())) {
            return super.invoke(msgContext);
        }
        return InvocationResponse.CONTINUE;
    }


}
