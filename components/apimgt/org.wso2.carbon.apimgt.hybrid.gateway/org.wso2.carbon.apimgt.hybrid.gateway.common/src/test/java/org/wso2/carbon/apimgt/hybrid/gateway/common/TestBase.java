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

package org.wso2.carbon.apimgt.hybrid.gateway.common;

import org.junit.Before;

import java.io.File;

public class TestBase {

    @Before
    public void setCarbonHome() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "carbon-home";
        File file = new File(filePath);
        if (file.exists()) {
            System.setProperty("carbon.home", file.getAbsolutePath());
        } else {
            throw new Exception("Carbon Home cannot be set. Hence Tests will not be executed");
        }
    }

}
