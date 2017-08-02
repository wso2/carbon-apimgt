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

package org.wso2.carbon.apimgt.ballerina.maps;

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
public class MapRemoveTestCase {
    private ProgramFile bLangProgram;
    private static final String s1 = "WSO2 Inc.";

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/maps/mapRemoveTest.bal");
    }

    @Test
    public void testCacheOperations() {
        //Create arguments to initiate map put
        BValue[] args = {new BString("testMapEntry"), new BString("testMapValue")};
        //Test ballerina map put, remove and get
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testMapRemove", args);
        //Assert if cache entry is BValue
        Assert.assertTrue(returns[0] instanceof BValue);
    }
}
