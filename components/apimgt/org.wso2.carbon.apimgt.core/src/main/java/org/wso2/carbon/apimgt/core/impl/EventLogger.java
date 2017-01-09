/*
 *
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
package org.wso2.carbon.apimgt.core.impl;


import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.models.Component;
import org.wso2.carbon.apimgt.core.models.Event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * An Observer which is used to observe possible APIMObservables and logs any event occurs there.
 */
public class EventLogger implements EventObserver {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EventObserver.class);

    private static final EventLogger eventLoggerObj = new EventLogger();
    private File relativeDirectory;
    private File logFile;
    private Logger logger;

    private EventLogger() {
        this.createLogFile();
        this.configureLogger();
    }

    private void createLogFile() {
        try {
            relativeDirectory = new File("event-logs");
            boolean isDirCreated = relativeDirectory.mkdirs();
            if (isDirCreated || Files.exists(relativeDirectory.toPath())) {
                logFile = new File(relativeDirectory, "logFile.txt");
                boolean isFileCreated = logFile.createNewFile();
                if (isFileCreated || Files.exists(logFile.toPath())) {
                    logger = Logger.getLogger("MyLog");
                }
            }
        } catch (IOException e) {
            log.error("Cannot create new log file.");
        }
    }

    private void configureLogger() {
        try {
            // This block configure the logger with handler and formatter
            FileHandler fh = new FileHandler(logFile.getAbsolutePath());
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            log.error("Cannot add file handler.");
        }
    }

    @Override
    public void captureEvent(Component component, Event event) {
        // the following statement is used to log any events
        logger.info("Component Name: " + component.getComponentName() + " - Event: "
                + event + "\n");
    }

    public File getRelativeDirectory() {
        return relativeDirectory;
    }

    public File getLogFileName() {
        return logFile;
    }

    public static EventLogger getEventLoggerObject() {
        return eventLoggerObj;
    }
}
