/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIUtil.class)
public class TierCacheServiceTestCase {

    @Test
    public void testInvalidateCache() {
        PowerMockito.mockStatic(APIUtil.class);
        TierCacheService tierCacheService = new TierCacheService();
        tierCacheService.invalidateCache("foo.com");
        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.clearTiersCache("foo.com");
    }
}
