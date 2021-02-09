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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

/**
 * Set endpoint config in context
 */
public class EndpointConfigContext extends ConfigContextDecorator {

    private API api;
    private JSONObject endpoint_config;

    public EndpointConfigContext(ConfigContext context, API api) {
        super(context);
        this.api = api;
    }

    @Override
    public void validate() throws APITemplateException, APIManagementException {
        super.validate();

        JSONParser parser = new JSONParser();
        //check if endpoint config exists
        String config_json = api.getEndpointConfig();

        if (config_json != null && !"".equals(config_json)) {
            try {
                Object config = parser.parse(config_json);
                this.endpoint_config = (JSONObject) config;
            } catch (ParseException e) {
                this.handleException("Unable to pass the endpoint JSON config");
            }
        }
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        context.put("endpoint_config", this.endpoint_config);
        context.put("endpointKey", this.getEndpointKey(api));

        return context;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get the endpoint key name
     *
     * @param api API that the endpoint belong
     * @return String of endpoint key
     */
    private String getEndpointKey(API api) {
        return api.getId().getApiName() + "--v" + api.getId().getVersion();
    }
}
