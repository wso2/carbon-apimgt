/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.client.internal;

import org.apache.axis2.context.ConfigurationContext;

public class ServiceReferenceHolder {

    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigurationContext axis2ConfigurationContext;


    public void setAxis2ConfigurationContext(ConfigurationContext axis2ConfigurationContext) {
        this.axis2ConfigurationContext = axis2ConfigurationContext;
    }

    public ConfigurationContext getAxis2ConfigurationContext() {
        return axis2ConfigurationContext;
    }

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }
}
