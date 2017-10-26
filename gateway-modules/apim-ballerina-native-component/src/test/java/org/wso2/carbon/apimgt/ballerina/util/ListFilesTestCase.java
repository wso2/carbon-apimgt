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


//        //      /home/sabeena
//        File notADir = new File(home + "/notADir");
//        notADir.delete();
//
//        //boolean allJsonDirCreated = (new File("/home/sabeena/Desktop/allJsonDir")).mkdirs();
//        boolean allJsonDirCreated = (new File(home + "/allJsonDir")).mkdirs();
//        boolean noJsonDirCreated = (new File(home + "/noJsonDir")).mkdirs();
//
//        if (allJsonDirCreated) {
//            File a = new File(home + "/allJsonDir/a.json");
//            File b = new File(home + "/allJsonDir/b.json");
//
//            try {
//                a.createNewFile();
//                b.createNewFile();
//            } catch (IOException ioe) {
//            }
//        }
//
//        if (noJsonDirCreated) {
//            File a = new File(home + "/noJsonDir/a.txt");
//            File b = new File(home + "/noJsonDir/b.txt");
//
//            try {
//                a.createNewFile();
//                b.createNewFile();
//            } catch (IOException ioe) {
//            }
//        }

        //      /home/sabeena
        File notADir = new File("samples/util/notADir");
        notADir.delete();

        //boolean allJsonDirCreated = (new File("/home/sabeena/Desktop/allJsonDir")).mkdirs();
        //boolean allJsonDirCreated = (new File("samples/util/allJsonDir")).mkdirs();
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

//        if (noJsonDirCreated) {
//            File a = new File("samples/util/noJsonDir/a.txt");
//            File b = new File("samples/util/noJsonDir/b.txt");
//
//            try {
//                a.createNewFile();
//                b.createNewFile();
//            } catch (IOException ioe) {
//            }
//        }

        bLangProgram = BTestUtils.parseBalFile("samples/util/listJsonFiles.bal");
    }

    @Test(description = "Returns a string array of json file names")
    public void testListJsonFiles() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "listJsonFiles", args);

        //Assert.assertNull(returns[0]);
        //Assert.assertTrue(returns[0] == null);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).size(), 0);


        Assert.assertTrue(returns[1] instanceof BStringArray);
        BStringArray d1Files = (BStringArray) returns[1];
        Assert.assertEquals(d1Files.size(), 1);

        Assert.assertEquals(d1Files.get(0), "a.json");
        //Assert.assertEquals(d1Files.get(1), "b.json");

//        Assert.assertTrue(returns[2] instanceof BStringArray);
//        Assert.assertEquals(((BStringArray) returns[2]).size(), 0);


//        BStringArray d3Files = (BStringArray) returns[2];
//        Assert.assertEquals(d3Files.size(), 2);
//        Assert.assertEquals(d3Files.get(0), "d");
//        Assert.assertEquals(d3Files.get(1), "e");

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

//        File noJsonDir = new File("samples/util/noJsonDir");
//        String[] txtFiles = noJsonDir.list();
//        for (String s : txtFiles) {
//            File currentFile = new File(noJsonDir.getPath(), s);
//            currentFile.delete();
//        }
//        noJsonDir.delete();
    }
}
