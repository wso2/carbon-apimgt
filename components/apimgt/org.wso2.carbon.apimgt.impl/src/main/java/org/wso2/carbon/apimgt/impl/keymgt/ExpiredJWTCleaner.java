/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.keymgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.util.Date;

/**
 *  Class responsible for removing expired revoked tokens from DB
 */
public class ExpiredJWTCleaner implements Runnable {

    private static final Log log = LogFactory.getLog(ExpiredJWTCleaner.class);
    private static long lastUpdatedTime;
    private static final long DURATION = 3600000;

    @Override
    public void run() {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdatedTime < DURATION) { // double checked locking to avoid unnecessary locking
            return;
        }
        synchronized (ExpiredJWTCleaner.class) {
            // Only run the cleanup if the last cleanup was was performed more than 1 hour ago
            if (currentTime - lastUpdatedTime < DURATION) {
                return;
            }
            cleanExpiredTokens();
        }
    }

    private void cleanExpiredTokens() {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        try {
            //Remove expired JWTs from revoke table
            apiMgtDAO.removeExpiredJWTs();
            lastUpdatedTime = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("Last JWT token cleanup performed at :" + new Date(lastUpdatedTime));

            }
        } catch (APIManagementException e) {
            log.error("Unable to cleanup expired JWT tokens from revoke table", e);
        }
    }
}
