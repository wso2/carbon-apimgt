/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to log audit logs.
 */
public class AuditLogger {
    private static final Log logger = LogFactory.getLog("APIM_GOV_AUDIT_LOG");

    /**
     * Enum for log levels.
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, FATAL
    }

    /**
     * Log a message with the default log level.
     *
     * @param topic   Topic of the log message
     * @param message Log message
     * @param args    Arguments to be replaced in the log message
     */
    public static void log(String topic, String message, Object... args) {
        log(LogLevel.INFO, topic, message, null, args);
    }

    /**
     * Log a message with the given log level.
     *
     * @param level   Log level
     * @param topic   Topic of the log message
     * @param message Log message
     * @param args    Arguments to be replaced in the log message
     */
    public static void log(LogLevel level, String topic, String message, Object... args) {
        log(level, topic, message, null, args);
    }

    /**
     * Log a message with the given log level, including the originator class.
     *
     * @param level     Log level
     * @param topic     Topic of the log message
     * @param message   Log message
     * @param exception Exception to be logged
     * @param args      Arguments to be replaced in the log message
     */
    public static void log(LogLevel level, String topic, String message, Throwable exception, Object... args) {

        String formattedMessage;
        if (topic == null || topic.isEmpty()) {
            formattedMessage = "[ General ] :: " + String.format(message, args);
        } else {
            formattedMessage = "[" + topic + "] :: " + String.format(message, args);
        }

        switch (level) {
            case DEBUG: {
                if (exception != null) {
                    logger.debug(formattedMessage, exception);
                } else {
                    logger.debug(formattedMessage);
                }
                break;
            }
            case WARN: {
                if (exception != null) {
                    logger.warn(formattedMessage, exception);
                } else {
                    logger.warn(formattedMessage);
                }
                break;
            }
            case ERROR: {
                if (exception != null) {
                    logger.error(formattedMessage, exception);
                } else {
                    logger.error(formattedMessage);
                }
                break;
            }
            case FATAL: {
                if (exception != null) {
                    logger.fatal(formattedMessage, exception);
                } else {
                    logger.fatal(formattedMessage);
                }
                break;
            }
            default: {
                if (exception != null) {
                    logger.info(formattedMessage, exception);
                } else {
                    logger.info(formattedMessage);
                }
                break;
            }
        }

    }
}
