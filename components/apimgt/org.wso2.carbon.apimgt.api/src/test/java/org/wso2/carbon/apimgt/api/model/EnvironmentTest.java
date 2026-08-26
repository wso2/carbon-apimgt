/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import org.junit.Assert;
import org.junit.Test;

public class EnvironmentTest {

    @Test
    public void testHashCodeAndEqualsWithNullFields() {
        Environment env1 = new Environment();
        Environment env2 = new Environment();

        // Environment name is null by default; hashCode and equals must not throw NullPointerException
        try {
            int hashCode1 = env1.hashCode();
            int hashCode2 = env2.hashCode();
            Assert.assertEquals(hashCode1, hashCode2);
            Assert.assertTrue(env1.equals(env2));
            Assert.assertTrue(env1.equals(env1));
            Assert.assertFalse(env1.equals(null));
        } catch (NullPointerException e) {
            Assert.fail("hashCode() or equals() threw NullPointerException when name field is null");
        }
    }

    @Test
    public void testHashCodeAndEqualsWithValidFields() {
        Environment env1 = new Environment();
        env1.setName("Production");
        env1.setType("hybrid");

        Environment env2 = new Environment();
        env2.setName("Production");
        env2.setType("hybrid");

        Environment env3 = new Environment();
        env3.setName("Sandbox");
        env3.setType("hybrid");

        Assert.assertEquals(env1.hashCode(), env2.hashCode());
        Assert.assertTrue(env1.equals(env2));
        Assert.assertFalse(env1.equals(env3));
    }
}
