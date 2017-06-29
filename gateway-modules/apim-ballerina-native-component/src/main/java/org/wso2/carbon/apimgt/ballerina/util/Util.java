/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.ballerina.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility class for ballerina native component
 */
public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    /**
     * Write File into File system
     *
     * @param path    file path
     * @param content API config
     */
    public static boolean saveFile(String path, String content) {

        try (OutputStream outputStream = new FileOutputStream(new File(path))) {
            IOUtils.write(content, outputStream, "UTF-8");
            return true;
        } catch (IOException e) {
            log.error("Error saving API configuration in " + path, e);
        }
        return false;
    }

    /**
     * Utility for Ip to Long convrsion
     * @param ip ip value
     * @return return long value of Ip
     */
    public static long ipToLong(String ip) {
        long ipAddressinLong = 0;
        if (ip != null) {
            //convert ipaddress into a long
            String[] ipAddressArray = ip.split("\\.");    //split by "." and add to an array

            for (int i = 0; i < ipAddressArray.length; i++) {
                int power = 3 - i;
                long ipAddress = Long.parseLong(ipAddressArray[i]);   //parse to long
                ipAddressinLong += ipAddress * Math.pow(256, power);
            }
        }
        return ipAddressinLong;
    }
}
