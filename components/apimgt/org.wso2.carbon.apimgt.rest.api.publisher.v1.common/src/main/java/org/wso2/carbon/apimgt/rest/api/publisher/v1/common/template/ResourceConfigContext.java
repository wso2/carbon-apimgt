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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;


/**
 * Set the uri templates as the resources
 */
public class ResourceConfigContext extends ConfigContextDecorator {

    //private static final Log log = LogFactory.getLog(ResourceConfigContext.class);

    private API api;
    private APIProduct apiProduct;
    private String faultSeqExt;

    public ResourceConfigContext(ConfigContext context, API api) {

        super(context);
        this.api = api;
    }

    public ResourceConfigContext(ConfigContext context, APIProduct apiProduct) {

        super(context);
        this.apiProduct = apiProduct;
    }

    public void validate() throws APIManagementException {

        if (api != null) {
            if (api.getUriTemplates() == null || api.getUriTemplates().isEmpty()) {
                throw new APIManagementException("At least one resource is required");
            }

            this.faultSeqExt = APIUtil.getFaultSequenceName(api);
        }
    }

    public VelocityContext getContext() {

        VelocityContext context = super.getContext();

        if (api != null) {
            context.put("resources", api.getUriTemplates());
            context.put("apiType", api.getType());
            context.put("faultSequence", faultSeqExt != null ? faultSeqExt : api.getFaultSequence());
        } else if (apiProduct != null) {
            context.put("apiType", apiProduct.getType());
            context.put("aggregates", apiProduct.getProductResources());
        }

        return context;
    }
}
