/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.wso2.carbon.apimgt.impl.APIConstants;

public class APIThrottleConstants {

    public static final int THROTTLE_OUT_ERROR_CODE = 900800;

    public static final int HARD_LIMIT_EXCEEDED_ERROR_CODE = 900801;
    
    public static final String API_THROTTLE_NS = "http://wso2.org/apimanager/throttling";
    public static final String API_THROTTLE_NS_PREFIX = "amt";
    
    public static final String API_THROTTLE_OUT_HANDLER = "_throttle_out_handler_";

    public static final String HARD_THROTTLING_CONFIGURATION = "hard_throttling_limits";

    public static final String PRODUCTION_HARD_LIMIT = "PRODUCTION_HARD_LIMIT";

    public static final String SANDBOX_HARD_LIMIT = "SANDBOX_HARD_LIMIT";

    public static final String THROTTLED_OUT_REASON = APIConstants.THROTTLE_OUT_REASON_KEY;

    public static final String HARD_LIMIT_EXCEEDED = APIConstants.THROTTLE_OUT_REASON_HARD_LIMIT_EXCEEDED;

    public static final int SC_TOO_MANY_REQUESTS = 429;
}
