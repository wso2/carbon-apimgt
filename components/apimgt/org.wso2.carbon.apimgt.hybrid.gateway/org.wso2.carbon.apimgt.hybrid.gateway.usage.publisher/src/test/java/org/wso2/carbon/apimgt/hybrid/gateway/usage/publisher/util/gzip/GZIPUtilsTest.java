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

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.gzip;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;

import java.io.File;
import java.io.FileOutputStream;

/**
 * GZIPUtils Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GZIPUtils.class})
public class GZIPUtilsTest {
    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void compressFile() throws Exception {
        String destPath = System.getProperty(Constants.CARBON_HOME) + File.separator +
                "api-usage-data/compressed/api-usage-data.dat.1517296920006.gz";
        String srcPath = System.getProperty(Constants.CARBON_HOME) + File.separator +
                "api-usage-data/api-usage-data.dat.1517296920006";
        GZIPUtils.compressFile(srcPath, destPath);
    }

    @Test
    public void compressFile_throwsException() throws Exception {
        String destPath = System.getProperty(Constants.CARBON_HOME) + File.separator +
                "api-usage-data/compressed/api-usage-data.dat.1517296920006.gz";
        String srcPath = System.getProperty(Constants.CARBON_HOME) + File.separator +
                "api-usage-data/api-usage-data.dat.1517296920006";
        FileOutputStream fileOutputStream = Mockito.spy(new FileOutputStream(destPath));
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);
        GZIPUtils.compressFile(srcPath, destPath);
    }

}
