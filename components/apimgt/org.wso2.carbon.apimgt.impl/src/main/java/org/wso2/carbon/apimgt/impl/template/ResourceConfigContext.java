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

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

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
            context.put("apiStatus", api.getStatus());
            context.put("faultSequence", faultSeqExt != null ? faultSeqExt : api.getFaultSequence());
        } else if (apiProduct != null) {
            //Here we aggregate duplicate resourceURIs of an API and populate httpVerbs set in the uri template
            List<APIProductResource> productResources = apiProduct.getProductResources();
            List<APIProductResource> aggregateResources = new ArrayList<APIProductResource>();
            List<String> uriTemplateNames = new ArrayList<String>();

            for (APIProductResource productResource : productResources) {
                URITemplate uriTemplate = productResource.getUriTemplate();
                String productResourceKey = productResource.getApiIdentifier() + ":" + uriTemplate.getUriTemplate();
                if (uriTemplateNames.contains(productResourceKey)) {
                    for (APIProductResource resource : aggregateResources) {
                        String resourceKey = resource.getApiIdentifier() + ":" + resource.getUriTemplate().getUriTemplate();
                        if (resourceKey.equals(productResourceKey)) {
                            resource.getUriTemplate().setHttpVerbs(uriTemplate.getHTTPVerb());
                        }
                    }
                } else {
                    uriTemplate.setHttpVerbs(uriTemplate.getHTTPVerb());
                    aggregateResources.add(productResource);
                    uriTemplateNames.add(productResourceKey);
                }
            }

            context.put("apiStatus", apiProduct.getState());
            context.put("aggregates", aggregateResources);
        }

        return context;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
