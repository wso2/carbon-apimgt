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
package org.wso2.carbon.throttle.core;

import javax.xml.namespace.QName;

public final class ThrottleConstants {

    private ThrottleConstants() {

    }

    /* Throttle module name */

    public static final String THROTTLE_MODULE_NAME = "wso2throttle";

    /* Throttle namespace */

    public static final String THROTTLE_NS = "http://www.wso2.org/products/wso2commons/throttle";

    public static final String THROTTLE_NS_PREFIX = "throttle";

    /* Throttle type according to the caller type eg: ip | domain */

    public static final int IP_BASE = 0;

    public static final int DOMAIN_BASE = 1;

    public static final int ROLE_BASE = 2;

    /* Throttle type according to the scope ex : service level;,operation level and module level*/

    public static final int GLOBAL_THROTTLE = 0;

    public static final int SERVICE_BASED_THROTTLE = 1;

    public static final int OPERATION_BASED_THROTTLE = 2;

    /* The throttle policy parameters */
    /**
     * @deprecated
     */
    public static final QName THROTTLE_ASSERTION_QNAME =
            new QName(THROTTLE_NS, "ThrottleAssertion", THROTTLE_NS_PREFIX);

    public static final QName OPERATION_THROTTLE_ASSERTION_QNAME =
            new QName(THROTTLE_NS, "OperationThrottleAssertion", THROTTLE_NS_PREFIX);

    public static final QName SERVICE_THROTTLE_ASSERTION_QNAME =
            new QName(THROTTLE_NS, "ServiceThrottleAssertion", THROTTLE_NS_PREFIX);

    public static final QName MODULE_THROTTLE_ASSERTION_QNAME =
            new QName(THROTTLE_NS, "ModuleThrottleAssertion", THROTTLE_NS_PREFIX);

    public static final QName MEDIATOR_THROTTLE_ASSERTION_QNAME =
            new QName(THROTTLE_NS, "MediatorThrottleAssertion", THROTTLE_NS_PREFIX);


    public static final QName THROTTLE_TYPE_ATTRIBUTE_QNAME =
            new QName(THROTTLE_NS, "type", THROTTLE_NS_PREFIX);

    public static final String KEY_OF_DEFAULT_CONFIGURATION_FOR_OTHER = "other";

    /**
     * @deprecated
     */
    public static final String ISALLOW_PARAMETER_NAME = "IsAllow";

    public static final String ALLOW_PARAMETER_NAME = "Allow";

    public static final String CONTROL_PARAMETER_NAME = "Control";

    public static final String DENY_PARAMETER_NAME = "Deny";

    public static final String DEFAULT_THROTTLE_CONTEXT_ID = "ThrottleContextID";

    public static final String UNIT_TIME_PARAMETER_NAME = "UnitTime";

    public static final String MAXIMUM_COUNT_PARAMETER_NAME = "MaximumCount";

    public static final String PROHIBIT_TIME_PERIOD_PARAMETER_NAME = "ProhibitTimePeriod";

    public static final String ID_PARAMETER_NAME = "ID";

    public static final String MAXIMUM_CONCURRENT_ACCESS_PARAMETER_NAME = "MaximumConcurrentAccess";

    /**
     * This global throttle path can be used to point to an external global throttle policy using
     * the axis.xml
     */
    public static final String GLOBAL_THROTTLE_PATH_PARAM = "globalThrottlePolicyPath";

    /* Access states  - allow, deny or control */

    public static final int ACCESS_CONTROLLED = 0;

    public static final int ACCESS_DENIED = 1;

    public static final int ACCESS_ALLOWED = 2;

    /* The default clean up time indicates how often cleaning up process should execute  */

    public static final long DEFAULT_THROTTLE_CLEAN_PERIOD = 5 * 1000 * 60;

    /*prefix for throttle specific properties */

    public static final String THROTTLE_PROPERTY_PREFIX = "throttle_";

    /*The key for map that contains all available throttles */

    public static final String THROTTLES_MAP = "local_throttle_map";

    /* The key for looking up throttle context and configuration from the throttle ,according to the type (ip | domain) */

    public static final String IP_BASED_THROTTLE_KEY = "key_of_ip_based_throttle";

    public static final String DOMAIN_BASED_THROTTLE_KEY = "key_of_domain_based_throttle";

    public static final String ROLE_BASED_THROTTLE_KEY = "key_of_role_based_throttle";

    /* The key for level throttle */

    public static final String GLOBAL_THROTTLE_KEY = "key_of_global_throttle";

    /* suffix for key , when setting a concurrent access controller to context */

    public static final String CAC_SUFFIX = "_cac_key";

    /* the throttle id for module level throttle */

    public static final String GLOBAL_THROTTLE_ID = "id_of_global_throttle_";

    /**
     * Parameter names which are used to identify special services..
     */
    public static final String ADMIN_SERVICE_PARAM_NAME = "adminService";
    public static final String HIDDEN_SERVICE_PARAM_NAME = "hiddenService";
    public static final String DYNAMIC_SERVICE_PARAM_NAME = "dynamicService";
}
