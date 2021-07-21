/*
*  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;

public class APITemplateBuilderImplTest {
    
    @Test
    public void testAdditionalPropertyWithStoreVisibilityReplacement() {
        API api = new API(new APIIdentifier("admin", "API", "1.0"));
        api.addProperty("test__display", "true");
        api.addProperty("property", "true");

        APITemplateBuilderImpl builder = new APITemplateBuilderImpl(api);
        JSONObject modifiedProperties = builder.getModifiedProperties(api.getAdditionalProperties());
        Assert.assertEquals("Additional Properties count mismatched", 3, modifiedProperties.size());
        Assert.assertNotNull("Converted additional Property is not available", modifiedProperties.get("test"));
        Assert.assertEquals("Converted property does not have the original value", api.getProperty("test__display"),
                modifiedProperties.get("test"));

    }
    @Test
    public void testAdditionalPropertyWithoutStoreVisibility() {
        API api = new API(new APIIdentifier("admin", "API", "1.0"));

        APITemplateBuilderImpl builder = new APITemplateBuilderImpl(api);
        JSONObject modifiedProperties = builder.getModifiedProperties(api.getAdditionalProperties());
        Assert.assertEquals("Additional Properties count mismatched", 0, modifiedProperties.size());

        api.addProperty("testproperty", "true");
        modifiedProperties = builder.getModifiedProperties(api.getAdditionalProperties());
        Assert.assertEquals("Additional Properties count mismatched", 1, modifiedProperties.size());
    }

}
