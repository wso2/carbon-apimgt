/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.util;

import org.slf4j.Logger;
import org.wso2.carbon.apimgt.core.exception.APIManagementDAOException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorCode;

/**
 * Class for all utility methods
 */
public class APIUtils {

    /**
     * Log and throw exceptions. This should be used only at service level.
     *
     * @param msg Error message
     * @param log Logger to be used to log the error message
     * @throws APIManagementException
     */
    public static void logAndThrowException(String msg, Logger log) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    /**
     * Log and throw exceptions. This should be used only at service level.
     *
     * @param msg Error message
     * @param t Exception to be thrown
     * @param log Logger to be used to log the error message
     * @throws APIManagementException
     */
    public static void logAndThrowException(String msg, Throwable t, Logger log) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    /**
     * Throw DAO exceptions. This does not log anything, and should be used only at DAO level.
     *
     * @param msg Error message
     * @param t Exception to be thrown
     * @throws APIManagementDAOException
     */
    public static void throwDaoException(ErrorCode errorCode, String msg, Throwable t) throws APIManagementDAOException {
        throw new APIManagementDAOException(errorCode, msg, t);
    }

    /**
     * Checks if debug log is enabled and logs the message
     *
     * @param msg Message to be logged
     * @param log Logger to be used to log
     */
    public static void logDebug(String msg, Logger log) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }
}
