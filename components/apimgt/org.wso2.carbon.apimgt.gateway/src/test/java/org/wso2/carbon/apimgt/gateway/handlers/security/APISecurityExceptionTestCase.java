/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test class for APISecurityException
 */
public class APISecurityExceptionTestCase {
    @Test(expected = APISecurityException.class)
    public void testResourceNotFoundExceptionWithMessage() throws APISecurityException {
        throw new APISecurityException(1000, "error msg");
    }

    @Test(expected = APISecurityException.class)
    public void testResourceNotFoundExceptionWithessageAndThrowable() throws APISecurityException {
        throw new APISecurityException(1000, "", Mockito.mock(Throwable.class));
    }

    @Test
    public void testGetErrorCode() {
        Assert.assertEquals(1000, new APISecurityException(1000, "").getErrorCode());
    }

}
