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

//import org.ballerinalang.model.values.BStringArray;
//import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
//import org.ballerinalang.util.program.BLangFunctions;
//import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.ballerina.caching.util.BTestUtils;

/**
 * Cache Test class which handles caching test for ballerina native cache implementation for API Manager
 *
 * @since 0.10-SNAPSHOT
 */

public class ListFilesTestCase {

    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/util/listJsonFiles.bal");
    }

    @Test(description = "Returns a string array of json file names")
    public void testGetKeys() {
//        BValue[] args = {};
//        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "listJsonFiles", args);
//
//        Assert.assertTrue(returns[0] instanceof BStringArray);
//        BStringArray d1Files = (BStringArray) returns[0];
//        Assert.assertEquals(d1Files.size(), 2);
//        Assert.assertEquals(d1Files.get(0), "a");
//        Assert.assertEquals(d1Files.get(1), "b");
//
//        Assert.assertTrue(returns[1] instanceof BStringArray);
//        Assert.assertEquals(((BStringArray) returns[1]).size(), 0);
//
//        Assert.assertTrue(returns[2] instanceof BStringArray);
//        BStringArray d3Files = (BStringArray) returns[2];
//        Assert.assertEquals(d3Files.size(), 2);
//        Assert.assertEquals(d3Files.get(0), "d");
//        Assert.assertEquals(d3Files.get(1), "e");


    }
}
