/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.rest.api;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Test util
 */
public class TestUtil {

    private final String carbonHome = "carbon.home";

    public void setupCarbonHome() throws Exception {
        this.setCarbonHome();
    }

    private void setCarbonHome() throws Exception {

        if (StringUtils.isEmpty(System.getProperty(carbonHome))) {
            String filePath =
                    "src" + File.separator + "test" + File.separator + "resources" + File.separator + "carbon-home";
            File file = new File(filePath);
            if (file.exists()) {
                System.setProperty(carbonHome, file.getAbsolutePath());
            } else {
                throw new Exception("Error while setting carbon home.");
            }
        }
    }
}
