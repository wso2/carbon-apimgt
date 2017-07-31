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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test class for saving a file
 */
public class SaveFileTestCase {

    private static File tempFile = null;

    @BeforeClass
    public void setup() throws IOException {
        //create a temp file to save the content.
        tempFile = File.createTempFile("testTextFile", ".txt");
        tempFile.deleteOnExit();
    }

    @Test(description = "Save file test case")
    public void saveFile() {
        String path = tempFile.getAbsolutePath();
        String content = "Content for save file test case.";
        //the below call should be successful since the path is valid (it should return true)
        boolean saveFileStatus = Util.saveFile(path, content);
        Assert.assertTrue(saveFileStatus);
        path = "ThisIsAnInvalidPath" + File.separator + "InvalidFile.txt";
        //the below call should fail since the path is invalid (it should return false)
        saveFileStatus = Util.saveFile(path, content);
        Assert.assertFalse(saveFileStatus);
        path = "";
        //the below call should fail since the path is empty (it should return false)
        saveFileStatus = Util.saveFile(path, content);
        Assert.assertFalse(saveFileStatus);
    }
}
