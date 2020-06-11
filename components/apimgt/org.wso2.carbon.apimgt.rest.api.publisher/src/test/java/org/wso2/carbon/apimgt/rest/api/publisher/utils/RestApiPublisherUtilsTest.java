/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * This is class contains test cases for {@link RestApiPublisherUtils}.
 */
public class RestApiPublisherUtilsTest {

    /**
     * This test method checks the behaviour of validateAdditionalProperties method when different inputs are given.
     */
    @Test
    public void testValidateAdditionalProperties() {
        Map<String, String> additionalProperties = new HashMap<>();
        Assert.assertTrue("When additional properties is null, error message is send back",
                RestApiPublisherUtils.validateAdditionalProperties(null).isEmpty());
        Assert.assertTrue("When additional properties is empty hash map, error message is send back",
                RestApiPublisherUtils.validateAdditionalProperties(additionalProperties).isEmpty());

        additionalProperties.put("testing", "yes, this is testing");
        additionalProperties.put("hello", "Hello World");
        Assert.assertTrue("Validation issue has happened for correct set of additional properties",
                RestApiPublisherUtils.validateAdditionalProperties(additionalProperties).isEmpty());
        additionalProperties.put("test adadad", "adadada");
        Assert.assertTrue("Validation issue has not happened for in-correct set of additional properties",
                RestApiPublisherUtils.validateAdditionalProperties(additionalProperties).contains("space character"));
        additionalProperties.remove("test adadad");
        additionalProperties.put("lcstate", "hello");
        Assert.assertTrue("Validation issue has not happened for in-correct set of additional properties",
                RestApiPublisherUtils.validateAdditionalProperties(additionalProperties)
                        .contains("conflicts with the reserved keywords"));
    }
}
