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

import com.google.gson.Gson;
import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.EndpointConfigDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;

/**
 * Set endpoint config in context.
 */
public class EndpointConfigContext extends ConfigContextDecorator {

    private API api;
    private APIProduct apiProduct;
    private JSONObject endpointConfig;
    private EndpointConfigDTO endpointConfigDTO;

    public EndpointConfigContext(ConfigContext context, API api) {

        super(context);
        this.api = api;
    }

    public EndpointConfigContext(ConfigContext configcontext, APIProduct apiProduct, API api) {

        super(configcontext);
        this.api = api;
        this.apiProduct = apiProduct;
    }

    public EndpointConfigContext(ConfigContext configcontext, APIProduct apiProduct, API api,
                                 EndpointConfigDTO endpointConfigDTO) {

        super(configcontext);
        this.api = api;
        this.apiProduct = apiProduct;
        this.endpointConfigDTO = endpointConfigDTO;
    }

    @Override
    public void validate() throws APITemplateException, APIManagementException {

        super.validate();

        JSONParser parser = new JSONParser();

        if (this.endpointConfigDTO != null) {
            this.endpointConfig = new JSONObject(new Gson().fromJson(new Gson().toJson(this.endpointConfigDTO),
                    JSONObject.class));
        } else {
            String configJson = api.getEndpointConfig();
            if (configJson != null && !configJson.isEmpty()) {
                try {
                    this.endpointConfig = (JSONObject) parser.parse(configJson);
                    if (APIConstants.ENDPOINT_TYPE_AWSLAMBDA.equals(
                            endpointConfig.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
                        processLambdaConfig(api, endpointConfig);
                    }
                } catch (ParseException e) {
                    this.handleException("Unable to parse the endpoint JSON config");
                }
            }
        }
    }

    @Override
    public VelocityContext getContext() {

        VelocityContext context = super.getContext();
        context.put("endpoint_config", this.endpointConfig);
        if (apiProduct == null) {
            context.put("endpointKey", this.getEndpointKey(api));
        } else {
            context.put("endpointKey", this.getEndpointKey(apiProduct, api));
        }

        return context;
    }

    private String getEndpointKey(APIProduct apiProduct, API api) {

        return getEndpointKey(apiProduct.getId().getName(), apiProduct.getId().getVersion()).concat("--")
                .concat(api.getUuid());
    }

    /**
     * Get the endpoint key name.
     *
     * @param api API that the endpoint belong
     * @return String of endpoint key
     */
    private String getEndpointKey(API api) {

        return getEndpointKey(api.getId().getApiName(), api.getId().getVersion());
    }

    /**
     * Get the endpoint key name.
     *
     * @return String of endpoint key
     */
    private String getEndpointKey(String name, String version) {

        return name + "--v" + version;
    }

    /**
     * @param api       API to which the endpoint belongs
     * @param awsConfig Endpoint config of AWS Lambda API
     * @return Updated endpoint config
     */
    private JSONObject processLambdaConfig(API api, JSONObject awsConfig) {

        String awsAlias = GatewayUtils.retrieveAWSCredAlias(api.getId().getApiName(),
                api.getId().getVersion(), APIConstants.ENDPOINT_TYPE_AWSLAMBDA);
        awsConfig.put("awsAlias", awsAlias);
        return awsConfig;
    }

}
