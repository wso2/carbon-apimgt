/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.util;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.impl.FileEncryptionUtility;

import java.io.File;

/**
 * Class to hold Util methods used in container based Gateway
 */
public class ContainerBasedGatewayUtils {

    /**
     * Get the token after decrypting using {@link FileEncryptionUtility#readFromEncryptedFile(java.lang.String)}
     *
     * @return service account token
     * @throws GatewayException if an error occurs while resolving the token
     */
    public static String resolveToken(String encryptedTokenFileName) throws GatewayException {
        String token;
        try {
            String externalSATokenFilePath = System.getProperty(FileEncryptionUtility.CARBON_HOME)
                    + FileEncryptionUtility.SECURITY_DIR + File.separator + encryptedTokenFileName;
            token = FileEncryptionUtility.getInstance().readFromEncryptedFile(externalSATokenFilePath);
        } catch (APIManagementException e) {
            String msg = "Error occurred while resolving externally stored token";
            throw new GatewayException(msg, e, ExceptionCodes.GATEWAY_EXCEPTION);
        }
        return token;
    }
}
