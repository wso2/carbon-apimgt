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

/**
 * Set the uri templates as the resources
 */
public class ResourceConfigContext extends ConfigContextDecorator {
    //private static final Log log = LogFactory.getLog(ResourceConfigContext.class);

    private API api;

    public ResourceConfigContext(ConfigContext context, API api) {
        super(context);
        this.api = api;
    }

    public void validate() throws APIManagementException {
        if (api.getUriTemplates() == null || api.getUriTemplates().size() == 0) {
            throw new APIManagementException("At least one resource is required");
        }
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        context.put("resources", api.getUriTemplates());
        context.put("faultSequence", api.getFaultSequence());
        context.put("apiStatus", api.getStatus());

        return context;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
