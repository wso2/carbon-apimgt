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

import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.ballerina.caching.util.BTestUtils;

import java.io.File;
import java.io.IOException;

/**
 * Cache Test class which handles caching test for ballerina native cache implementation for API Manager
 *
 * @since 0.10-SNAPSHOT
 */

public class ListFilesTestCase {

    private ProgramFile bLangProgram;
    final String home = System.getProperty("user.home");

    @BeforeClass
    public void setup() {
        File notADir = new File("samples/util/notADir");
        notADir.delete();
        boolean dirCreated = (new File("samples/util/testDir")).mkdirs();
        if (dirCreated) {
            File a = new File("samples/util/testDir/a.json");
            File b = new File("samples/util/testDir/b.txt");
            try {
                a.createNewFile();
                b.createNewFile();
            } catch (IOException ioe) {
            }
        }
        bLangProgram = BTestUtils.parseBalFile("samples/util/listJsonFiles.bal");
    }

    @Test(description = "Returns a string array of json file names")
    public void testListJsonFiles() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "listJsonFiles", args);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).size(), 0);
        Assert.assertTrue(returns[1] instanceof BStringArray);
        BStringArray d1Files = (BStringArray) returns[1];
        Assert.assertEquals(d1Files.size(), 1);
        Assert.assertEquals(d1Files.get(0), "a.json");
    }

    @AfterClass
    public void deleteTestDirectories() {
        File testDir = new File("samples/util/testDir");
        String[] jsonFiles = testDir.list();
        for (String s : jsonFiles) {
            File currentFile = new File(testDir.getPath(), s);
            currentFile.delete();
        }
        testDir.delete();
    }
}
