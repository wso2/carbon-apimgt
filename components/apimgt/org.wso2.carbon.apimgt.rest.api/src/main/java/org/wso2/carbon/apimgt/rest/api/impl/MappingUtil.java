/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.rest.api.model.API;

/**
 * Created by jo on 9/13/15.
 */
public class MappingUtil {

    protected static API mapAPI(org.wso2.carbon.apimgt.api.model.API model){
        API api = new API();
        api.setContext(model.getContext());
        api.setName(model.getId().getApiName());
        api.setDescription(model.getDescription());
        api.setVersion(model.getId().getVersion());
        api.setProvider(model.getId().getProviderName());

        return api;
    }
}
