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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.ballerina.caching.util.BTestUtils;

/**
 * Cache Test class which handles caching test for ballerina native cache implementation for API Manager
 *
 * @since 0.10-SNAPSHOT
 */

public class GetKeysTestCase {
    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/util/jsonGetKeys.bal");
    }

    @Test(description = "Get keys from a JSON object")
    public void testGetKeys() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testGetKeys", args);

        Assert.assertTrue(returns[0] instanceof BStringArray);
        BStringArray keys = (BStringArray) returns[0];
        Assert.assertEquals(keys.size(), 3);
        Assert.assertEquals(keys.get(0), "fname");
        Assert.assertEquals(keys.get(1), "lname");
        Assert.assertEquals(keys.get(2), "age");

        Assert.assertTrue(returns[1] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[1]).size(), 0);

        Assert.assertTrue(returns[2] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[2]).size(), 0);

        Assert.assertTrue(returns[3] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[3]).size(), 0);

    }

}
