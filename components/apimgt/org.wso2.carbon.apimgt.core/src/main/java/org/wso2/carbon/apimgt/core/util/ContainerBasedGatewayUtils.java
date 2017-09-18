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

import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Class to hold Util methods used in container based Gateway
 */
public class ContainerBasedGatewayUtils {

    private static APIMConfigurations apimConfigurations = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
    //todo : temporary
    private static String saTokenFile = apimConfigurations.getContainerGatewayConfigs()
         .getKubernetesGatewayConfigurations().getSaTokenFile();

    /**
     * Get Access Token of the Service Account reading from a File
     *
     * @return Access Token of Service Account as A String
     * @throws GatewayException   If there is a failure to get the Access Token of the Service Account
     */
    public static String getServiceAccountAccessToken() throws GatewayException {

        //todo : get this read from the kube secret
        String token;
         File tokenFile = new File(saTokenFile);
        try (InputStream inputStream = new FileInputStream(tokenFile);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            token = bufferedReader.readLine();

        } catch (FileNotFoundException e) {
            throw new GatewayException("Service Account Access Token file is not found in the given location", e,
                    ExceptionCodes.FILE_NOT_FOUND_IN_LOCATION);
        } catch (IOException e) {
            throw new GatewayException("Error in Reading Access Token File. " + saTokenFile, e,
                    ExceptionCodes.FILE_READING_EXCEPTION);
        }
        return token;

    }
}
