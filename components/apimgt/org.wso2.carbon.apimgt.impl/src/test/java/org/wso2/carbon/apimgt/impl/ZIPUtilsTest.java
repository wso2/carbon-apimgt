/*
*  Copyright (c) 2005-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl;


import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ZIPUtilsTest {


    @Test
    public void zipDirTest() throws IOException {
        ZIPUtils zipUtils = new ZIPUtils();
        File tempFile = File.createTempFile("XYZ", "Directory");
        tempFile.delete();
        tempFile.mkdirs();
        File tempFile2 = new File(tempFile.getAbsolutePath() + File.separator + "Test");
        tempFile2.createNewFile();
        tempFile2.delete();
        tempFile2.mkdirs();
        new File(tempFile.getAbsolutePath() + File.separator + "Test" + File.separator + "demo.txt").createNewFile();
        System.out.println(tempFile.getAbsolutePath());
        String zipFileName = tempFile.getAbsolutePath() + File.separator + ".." + File.separator + "temp.zip";
        zipUtils.zipDir(tempFile.getAbsolutePath(), zipFileName);
        Assert.assertTrue(new File(zipFileName).exists());
        tempFile.deleteOnExit();
        new File(zipFileName).deleteOnExit();
    }
}
