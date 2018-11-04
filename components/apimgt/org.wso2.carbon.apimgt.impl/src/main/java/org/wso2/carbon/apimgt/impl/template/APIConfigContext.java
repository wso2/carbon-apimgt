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

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * This will initialise a velocity context to used in the template
 * and populate it with api name, version and context and a reference to api
 */
public class APIConfigContext extends ConfigContext {

    private API api;

    public APIConfigContext(API api) {
        this.api = api;
    }

    @Override
    public void validate() throws APITemplateException {
        //see if api name ,version, context sets
        /*if(this.getAPIName(api) && api.getContext() && api.getId().getVersion()){
            return;
        }
        else{
            this.handleException("Required API mapping not provided");
        }
        */
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context = new VelocityContext();
        //set the api name version and context
        context.put("apiName", this.getAPIName(api));
        context.put("apiVersion", api.getId().getVersion());

        // We set the context pattern now to support plugable version strategy
        // context.put("apiContext", api.getContext());
        context.put("apiContext", api.getContextTemplate());

        //the api object will be passed on to the template so it properties can be used to
        // customise how the synapse config is generated.
        context.put("apiObj", api);

        if (APIConstants.BLOCKED.equals(api.getStatus())) {
            context.put("apiIsBlocked", Boolean.TRUE);
        } else {
            context.put("apiIsBlocked", Boolean.FALSE);
        }
        String apiSecurity = api.getApiSecurity();
        if (apiSecurity == null || apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
            context.put("apiIsOauthProtected", Boolean.TRUE);
        } else {
            context.put("apiIsOauthProtected", Boolean.FALSE);
        }
        return context;
    }

    public String getAPIName(API api) {
        return api.getId().getProviderName() + "--" + api.getId().getApiName();
    }
}
