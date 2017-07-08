/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.core.util;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class is used to read the WSDL file using WSDL4J library.
 */

public class APIMWSDLUtils {
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    public static byte[] getWSDL(String wsdlUrl) throws APIMgtWSDLException {
        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = null;
        URLConnection conn;
        try {
            URL url = new URL(wsdlUrl);
            conn = url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.connect();

            outputStream = new ByteArrayOutputStream();
            inputStream = conn.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new APIMgtWSDLException("Error while reading content from " + wsdlUrl, e,
                    ExceptionCodes.INVALID_WSDL_URL_EXCEPTION);
        } finally {
            if (outputStream != null) {
                IOUtils.closeQuietly(outputStream);
            }
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }
}
