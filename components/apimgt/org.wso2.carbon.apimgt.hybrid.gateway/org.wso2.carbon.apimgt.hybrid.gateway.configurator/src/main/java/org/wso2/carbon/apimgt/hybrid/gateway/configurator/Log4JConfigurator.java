/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.configurator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * This class is used to replace configurations in log4j.properties
 */
public class Log4JConfigurator {

    private static final Log log = LogFactory.getLog(Log4JConfigurator.class);
    private static final String LOG4J_PROPERTIES = "log4j.properties";

    /**
     * Configure Log4j properties file
     *
     * @param configDirPath String
     */
    public void configure(String configDirPath) {
        String log4jFilePath = configDirPath + File.separator + LOG4J_PROPERTIES;
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = new FileOutputStream(new File(log4jFilePath), true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            String loggerLines
                    = "\n\nlog4j.logger.org.wso2.carbon.databridge.agent.endpoint.DataEndpointConnectionWorker=FATAL"
                    + "\nlog4j.logger.org.wso2.carbon.databridge.agent.endpoint.DataEndpointGroup=FATAL";
            bufferedWriter.write(loggerLines);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            log.error("Error occurred while adding the loggers to file : " + LOG4J_PROPERTIES, e);
            Runtime.getRuntime().exit(1);
        } finally {
            String msg = "Error occurred while closing writer resources for file :" + LOG4J_PROPERTIES;
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                log.warn(msg);
            }
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
            } catch (IOException e) {
                log.warn(msg);
            }
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                log.warn(msg);
            }
        }
    }


}
