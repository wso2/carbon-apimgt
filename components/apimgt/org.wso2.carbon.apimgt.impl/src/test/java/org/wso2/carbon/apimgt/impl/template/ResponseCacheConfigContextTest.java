/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.template;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIConstants;

public class ResponseCacheConfigContextTest {

    @Test
    public void testResponseCacheConfigContext() throws Exception {
        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIStatus.CREATED);
        api.setContextTemplate("/");
        api.setResponseCache(APIConstants.DISABLED);
        ConfigContext configcontext = new APIConfigContext(api);
        ResponseCacheConfigContext responseCacheConfigContext = new ResponseCacheConfigContext(configcontext, api);
        Assert.assertFalse((Boolean) responseCacheConfigContext.getContext().get("responseCacheEnabled"));

        api.setResponseCache(APIConstants.ENABLED);
        api.setCacheTimeout(100);
        responseCacheConfigContext = new ResponseCacheConfigContext(configcontext, api);
        Assert.assertTrue((Boolean) responseCacheConfigContext.getContext().get("responseCacheEnabled"));
        Assert.assertEquals(100, responseCacheConfigContext.getContext().get("responseCacheTimeOut"));
    }
}
