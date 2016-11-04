/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.util;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.Application;

/**
 *  This class contains REST API Store related utility operations
 */
public class RestAPIStoreUtils {

    private static final Logger log = LoggerFactory.getLogger(RestAPIStoreUtils.class);

    /**
     * check whether current logged in user has access to the specified application
     *
     * @param application Application object
     * @return true if current logged in consumer has access to the specified application
     */
    public static boolean isUserAccessAllowedForApplication(Application application) {
        String username = "DUMMY_USER";//RestApiUtil.getLoggedInUsername();

        if (application != null) {
            //if groupId is null or empty, it is not a shared app
            if (StringUtils.isEmpty(application.getGroupId())) {
                //if the application is not shared, its subscriber and the current logged in user must be same
                if (application.getSubscriber() != null && application.getSubscriber().getName().equals(username)) {
                    return true;
                }
            } else {
                String userGroupId = "DUMMY_GROUP";//RestApiUtil.getLoggedInUserGroupId();
                //if the application is a shared one, application's group id and the user's group id should be same
                if (application.getGroupId().equals(userGroupId)) {
                    return true;
                }
            }
        }
        return false;
    }


}
