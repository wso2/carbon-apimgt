/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.ballerina.caching;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
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
 *  @since 0.10-SNAPSHOT
 */
public class CacheTest {
    private ProgramFile bLangProgram;
    private static final String s1 = "WSO2 Inc.";

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/cache/cacheTest.bal");
    }

    @Test
    public void testCacheOperations() {
        //Create arguments to initiate cache
        BValue[] args = {new BString("cacheName"), new BString("15"), new BString("cacheKey"),
                new BString("cacheValue")};
        //Test ballerina cache create, put and get for BString
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testCache", args);
        //Assert if cache entry is BValue
        Assert.assertTrue(returns[0] instanceof BValue);
        final String expected = "cacheValue";
        //Assert if return entry matched with exact entry we put there
        Assert.assertEquals(returns[0].stringValue(), expected);

        //Test ballerina Boolean values.
        BValue[] argsBoolean = {new BString("cacheName"), new BString("15"), new BString("cacheKey"),
                new BBoolean(false)};
        BValue[] returnsBoolean = BLangFunctions.invokeNew(bLangProgram, "testCache", argsBoolean);
        Assert.assertTrue(returnsBoolean[0] instanceof BBoolean);

        //Test ballerina JSON values.
        BValue[] argsJSON = {new BString("cacheName"), new BString("15"), new BString("cacheKey"),
                new BJSON("{}")};
        BValue[] returnsJSON = BLangFunctions.invokeNew(bLangProgram, "testCache", argsJSON);
        Assert.assertTrue(returnsJSON[0] instanceof BJSON);
    }
    @Test
    public void testCacheForNonExistence() {
        ProgramFile bLangProgram1 = BTestUtils.parseBalFile("samples/cache/nonExistenceEntryCacheTest.bal");

        //Create arguments to initiate cache
        BValue[] args = {new BString("cacheName"), new BString("15"), new BString("cacheKey")};
        //Test ballerina cache create, put and get for BString
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram1, "testCacheForNonExistence", args);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        BBoolean value = (BBoolean) returns[0];
        Assert.assertFalse(value.booleanValue());
    }
}
