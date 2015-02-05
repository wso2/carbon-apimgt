/*
* Copyright 2005,2006 WSO2, Inc. http://wso2.com
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.throttle.core.factory.CallerContextFactory;

/**
 * Controls the access of remote callers according to the controlling  policy .
 * This provides abstraction that need to control access based on caller IP or caller domain name.
 * This implementation is thread safe.
 */

public class RoleBasedAccessRateController {

    private static Log log = LogFactory.getLog(RoleBasedAccessRateController.class.getName());

    private static final String ACCESS_DENIED_TEMPORALLY =
            "You cannot access this service since you have exceeded the allocated quota.";

    private static final String ACCESS_DENIED =
            "You cannot access this service since you have been prohibited permanently.";

    /* The Object for used to lock in synchronizing */
    private final Object lock = new Object();

    private boolean debugOn = false;  //is debug enable

    public RoleBasedAccessRateController() {
        debugOn = log.isDebugEnabled();
    }

    /**
     * To check whether caller can access not not base on the controlling  policy
     *
     * @param throttleContext - current states of throttle - RunTime Data
     * @param roleID        - Identifier for remote caller - ex: ip or domainname
     * @return  access information
     * @throws ThrottleException
     */
    public AccessInformation canAccess(ThrottleContext throttleContext,
                                       String consumerKey, String roleID) throws ThrottleException {

        String type = "role";

        ThrottleConfiguration throttleConfigurationBean =
                throttleContext.getThrottleConfiguration();
        AccessInformation accessInformation = new AccessInformation();

        if (throttleConfigurationBean == null) {
            if (debugOn) {
                log.debug("Couldn't find Throttle Configuration!! - Throttling will not occur");
            }
            accessInformation.setAccessAllowed(true);
            return accessInformation;
        }

        if (roleID == null) {
            String msg = "Couldn't find consumer role!! - Access will be denied ";
            if (debugOn) {
                log.debug(msg);
            }
            accessInformation.setAccessAllowed(false);
            accessInformation.setFaultReason(msg);
            return accessInformation;
        }

        CallerConfiguration configuration =
                throttleConfigurationBean.getCallerConfiguration(roleID);
        if (configuration == null) {
            if (debugOn) {
                log.debug("Caller configuration couldn't find for " + type
                        + " and for caller " + roleID);
            }
            accessInformation.setAccessAllowed(true);
            return accessInformation;
        }
        if (configuration.getAccessState() == ThrottleConstants.ACCESS_DENIED) {
            log.info(ACCESS_DENIED);
            accessInformation.setAccessAllowed(false);
            accessInformation.setFaultReason(ACCESS_DENIED);
            return accessInformation;
        } else if (configuration.getAccessState() == ThrottleConstants.ACCESS_ALLOWED) {
            accessInformation.setAccessAllowed(true);
            return accessInformation;
        } else if (configuration.getAccessState() == ThrottleConstants.ACCESS_CONTROLLED) {
            synchronized (lock) {
                CallerContext caller = throttleContext.getCallerContext(consumerKey);
                if (caller == null) {
                    //if caller has not already registered ,then create new caller description and
                    //set it in throttle
                    caller = CallerContextFactory.createCaller(ThrottleConstants.ROLE_BASE, consumerKey);
                }
                if (caller != null) {
                    long currentTime = System.currentTimeMillis();

                    if (!caller.canAccess(throttleContext, configuration, currentTime)) {
                        //if current caller cannot access , then perform cleaning
                        log.info(ACCESS_DENIED_TEMPORALLY);
                        throttleContext.processCleanList(currentTime);
                        accessInformation.setAccessAllowed(false);
                        accessInformation.setFaultReason(ACCESS_DENIED_TEMPORALLY);
                        return accessInformation;
                    } else {
                        if (debugOn) {
                            log.debug("Access  from " + type + " " + roleID + " is successful.");
                        }
                        accessInformation.setAccessAllowed(true);
                        return accessInformation;
                    }
                } else {
                    if (debugOn) {
                        log.debug("Caller " + type + " not found! " + roleID);
                    }
                    accessInformation.setAccessAllowed(true);
                    return accessInformation;
                }
            }
        }
        accessInformation.setAccessAllowed(true);
        return accessInformation;
    }
}