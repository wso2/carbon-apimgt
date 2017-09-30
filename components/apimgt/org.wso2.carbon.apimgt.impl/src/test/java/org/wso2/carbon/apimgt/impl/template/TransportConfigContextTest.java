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

import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;

public class TransportConfigContextTest {

    @Test
    public void testTransportConfigContext() throws Exception {

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIStatus.CREATED);
        api.setContextTemplate("/");
        api.setTransports(Constants.TRANSPORT_HTTP);
        ConfigContext configcontext = new APIConfigContext(api);
        TransportConfigContext transportConfigContext = new TransportConfigContext(configcontext, api);
        Assert.assertTrue(Constants.TRANSPORT_HTTP.equalsIgnoreCase
                (transportConfigContext.getContext().get("transport").toString()));
        api.setTransports(Constants.TRANSPORT_HTTP + "," + Constants.TRANSPORT_HTTPS);
        configcontext = new APIConfigContext(api);
        transportConfigContext = new TransportConfigContext(configcontext, api);
        Assert.assertTrue(StringUtils.EMPTY.equalsIgnoreCase
                (transportConfigContext.getContext().get("transport").toString()));
    }
}
