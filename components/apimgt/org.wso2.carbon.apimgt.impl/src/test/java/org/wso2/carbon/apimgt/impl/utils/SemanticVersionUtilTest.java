/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIVersionValidationFailureException;
import org.wso2.carbon.apimgt.impl.dto.SemVersion;

public class SemanticVersionUtilTest {

    private String mockAPIUUID = "64ac09914d850605e92dc7bc";

    @Test
    public void testValidateValidVersionWithPatchCompAndPrefix() throws Exception {
        String version = "v1.2.3";
        SemVersion semVersion = SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
        Assert.assertEquals(semVersion.getMajor(), 1);
        Assert.assertEquals(semVersion.getMinor(), 2);
        Assert.assertEquals(java.util.Optional.ofNullable(semVersion.getPatch()), java.util.Optional.ofNullable(3));
    }

    @Test
    public void testValidateValidVersionWithPrefix() throws Exception {
        String version = "v1.2";
        SemVersion semVersion = SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
        Assert.assertEquals(1, semVersion.getMajor());
        Assert.assertEquals(2, semVersion.getMinor());
        Assert.assertEquals(null, semVersion.getPatch());
    }

    @Test
    public void testValidateValidVersionWithPatchComp() throws Exception {
        String version = "1.2.3";
        SemVersion semVersion = SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
        Assert.assertEquals(semVersion.getMajor(), 1);
        Assert.assertEquals(semVersion.getMinor(), 2);
        Assert.assertEquals(java.util.Optional.ofNullable(semVersion.getPatch()), java.util.Optional.ofNullable(3));
    }

    @Test
    public void testValidateValidVersion() throws Exception {
        String version = "1.2";
        SemVersion semVersion = SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
        Assert.assertEquals(1, semVersion.getMajor());
        Assert.assertEquals(2, semVersion.getMinor());
        Assert.assertEquals(null, semVersion.getPatch());
    }

    @Test(expected = APIVersionValidationFailureException.class)
    public void testValidateInvalidVersionWithPrefix() throws Exception {
        String version = "v1.n";
        SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
    }

    @Test(expected = APIVersionValidationFailureException.class)
    public void testValidateVersionWithInvalidPrefix() throws Exception {
        String version = "a1.2";
        SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
    }
    @Test(expected = APIVersionValidationFailureException.class)
    public void testValidateInvalidVersionWithPrefixAndOnlyMajorComp() throws Exception {
        String version = "v1";
        SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
    }

    @Test(expected = APIVersionValidationFailureException.class)
    public void testValidateInvalidVersionWithOnlyMajorComp() throws Exception {
        String version = "1";
        SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
    }

    @Test(expected = APIVersionValidationFailureException.class)
    public void testValidateInvalidVersionWithRandomChars() throws Exception {
        String version = "1.0.0-beta";
        SemanticVersionUtil.validateAndGetVersionComponents(version, mockAPIUUID);
    }
}
