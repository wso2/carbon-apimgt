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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal.ServiceReferenceHolder;

/**
 * This will initialise a velocity context to used in the template and populate it with api name, version and context
 * and a reference to api
 */
public class APIConfigContext extends ConfigContext {

    private API api;
    private APIProduct apiProduct;
    private static final String PRODUCT_PREFIX = "prod";

    public APIConfigContext(API api) {
        this.api = api;
    }

    public APIConfigContext(APIProduct apiProduct) {
        this.apiProduct = apiProduct;
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

        if (api != null) {
            setApiVelocityContext(api, context);
        } else if (apiProduct != null) { // API Product scenario
            setApiProductVelocityContext(apiProduct, context);
        }

        return context;
    }

    private void setApiVelocityContext(API api, VelocityContext context) {
        //set the api name version and context
        context.put("apiName", this.getAPIName(api));
        context.put("apiVersion", api.getId().getVersion());
        context.put("UUID", api.getUUID());

        // We set the context pattern now to support plugable version strategy
        // context.put("apiContext", api.getContext());
        context.put("apiContext", api.getContextTemplate());

        //the api object will be passed on to the template so it properties can be used to
        // customise how the synapse config is generated.
        context.put("apiObj", api);

        String apiSecurity = api.getApiSecurity();
        if (apiSecurity == null || apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
            context.put("apiIsOauthProtected", Boolean.TRUE);
        } else {
            context.put("apiIsOauthProtected", Boolean.FALSE);
        }
        //if API is secured with api_Key
        if (apiSecurity != null && apiSecurity.contains(APIConstants.API_SECURITY_API_KEY)) {
            context.put("apiIsApiKeyProtected", Boolean.TRUE);
        } else {
            context.put("apiIsApiKeyProtected", Boolean.FALSE);
        }
        //if API is secured with basic_auth
        if (apiSecurity != null && apiSecurity.contains(APIConstants.API_SECURITY_BASIC_AUTH)) {
            context.put("apiIsBasicAuthProtected", Boolean.TRUE);
        } else {
            context.put("apiIsBasicAuthProtected", Boolean.FALSE);
        }
        if (api.isEnabledSchemaValidation()) {
            context.put("enableSchemaValidation", Boolean.TRUE);
        } else {
            context.put("enableSchemaValidation", Boolean.FALSE);
        }
        if (api.isEnableStore()) {
            context.put("enableStore", Boolean.TRUE);
        } else {
            context.put("enableStore", Boolean.FALSE);
        }
        // API test key
        context.put("testKey", api.getTestKey());

        // Set the enable retry call with new Oauth token property
        context.put(APIConstants.ENABLE_RETRY_CALL_WITH_NEW_OAUTH_TOKEN, isRetryCallWithNewOAuthTokenEnabled());
    }

    private void setApiProductVelocityContext(APIProduct apiProduct, VelocityContext context) {
        APIProductIdentifier id = apiProduct.getId();
        //set the api name version and context
        context.put("apiName", id.getName());
        context.put("apiVersion", id.getVersion());

        // We set the context pattern now to support plugable version strategy
        // context.put("apiContext", api.getContext());
        context.put("apiContext", apiProduct.getContextTemplate());

        //the api object will be passed on to the template so it properties can be used to
        // customise how the synapse config is generated.
        context.put("apiObj", apiProduct);

        context.put("apiIsBlocked", Boolean.FALSE);

        String apiSecurity = apiProduct.getApiSecurity();
        if (apiSecurity == null || apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
            context.put("apiIsOauthProtected", Boolean.TRUE);
        } else {
            context.put("apiIsOauthProtected", Boolean.FALSE);
        }
        if (apiProduct.isEnabledSchemaValidation()) {
            context.put("enableSchemaValidation", Boolean.TRUE);
        } else {
            context.put("enableSchemaValidation", Boolean.FALSE);
        }
        if (apiProduct.isEnableStore()) {
            context.put("enableStore", Boolean.TRUE);
        } else {
            context.put("enableStore", Boolean.FALSE);
        }
        // API test key
        context.put("testKey", apiProduct.getTestKey());


        // Set the enable retry call with new Oauth token property
        context.put(APIConstants.ENABLE_RETRY_CALL_WITH_NEW_OAUTH_TOKEN, isRetryCallWithNewOAuthTokenEnabled());
    }

    public String getAPIName(API api) {
        return api.getId().getApiName();
    }

    /**
     * Checks whether retrying with a new OAuth token is enabled based on the configuration.
     *
     * @return {@code true} if retry with a new OAuth token is enabled; {@code false} otherwise.
     */
    protected boolean isRetryCallWithNewOAuthTokenEnabled() {
        String property = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getFirstProperty(
                APIConstants.MEDIATOR_CONFIG + APIConstants.OAuthConstants.OAUTH_MEDIATION_CONFIG + APIConstants.
                        OAuthConstants.ENABLE_RETRY_CALL_WITH_NEW_TOKEN);
        return Boolean.parseBoolean(property);
    }
}
