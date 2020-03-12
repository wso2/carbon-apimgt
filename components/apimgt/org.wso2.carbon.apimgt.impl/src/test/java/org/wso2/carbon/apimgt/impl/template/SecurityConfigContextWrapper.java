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

package org.wso2.carbon.apimgt.impl.template;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

/**
 * Wrapper class for SecurityConfigContext
 */
public class SecurityConfigContextWrapper extends SecurityConfigContext {

    private APIManagerConfiguration apiManagerConfiguration;

    public SecurityConfigContextWrapper(ConfigContext context, API api,
                                        APIManagerConfiguration apiManagerConfiguration) {
        super(context, api);
        this.apiManagerConfiguration = apiManagerConfiguration;
    }

    public SecurityConfigContextWrapper(ConfigContext context, APIProduct apiProduct,
                                        APIManagerConfiguration apiManagerConfiguration) {
        super(context, apiProduct);
        this.apiManagerConfiguration = apiManagerConfiguration;
    }
    protected APIManagerConfiguration getApiManagerConfiguration() {
        return apiManagerConfiguration;
    }
}
