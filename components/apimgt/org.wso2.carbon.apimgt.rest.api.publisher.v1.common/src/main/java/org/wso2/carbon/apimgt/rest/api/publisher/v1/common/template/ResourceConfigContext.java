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
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

            //translate REST API's expression format into synapse expressions
            Set<URITemplate> uriTemplates = api.getUriTemplates();
            for (URITemplate uriTemplate : uriTemplates) {
                List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
                for (OperationPolicy policy : operationPolicies) {
                    String policyType = policy.getPolicyType().toString();
                    boolean isSetHeader = OperationPolicy.PolicyType.SET_HEADER.toString().equals(policyType);
                    boolean isAddQueryParam = OperationPolicy.PolicyType.ADD_QUERY_PARAM.toString().equals(policyType);

                    if (isSetHeader || isAddQueryParam) {
                        String paramValue = isSetHeader ?
                                APIConstants.HEADER_VALUE_PARAM :
                                APIConstants.QUERY_PARAM_VALUE;
                        String paramExpression = isSetHeader ?
                                APIConstants.HEADER_EXPRESSION_PARAM :
                                APIConstants.QUERY_PARAM_EXPRESSION;

                        Map<String, String> parameters = policy.getParameters();
                        String value = parameters.get(paramValue);
                        String expression = parameters.get(paramExpression);
                        if (value == null && expression != null) {
                            String xpathExpression = null;
                            String[] exp = expression.split("\\.");
                            String name = exp[2];
                            if (expression.startsWith(APIConstants.REQ_HEADER_PREFIX)) {
                                xpathExpression = "$trp:" + name;
                            } else if (expression.startsWith(APIConstants.REQ_PATH_PARAM_PREFIX)) {
                                xpathExpression = "$ctx:uri.var." + name;
                            } else if (expression.startsWith(APIConstants.REQ_QUERY_PARAM_PREFIX)) {
                                xpathExpression = "$url:" + name;
                            }

                            parameters.put(paramExpression, xpathExpression);
                        }
                    }
                }
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
            //Here we aggregate duplicate resourceURIs of an API and populate httpVerbs set in the uri template
            List<APIProductResource> productResources = new ArrayList<>(apiProduct.getProductResources());
            List<APIProductResource> aggregateResources = new ArrayList<>();
            List<String> uriTemplateNames = new ArrayList<String>();

            for (APIProductResource productResource : productResources) {
                URITemplate uriTemplate = productResource.getUriTemplate();
                String productResourceKey = productResource.getApiIdentifier() + ":" + uriTemplate.getUriTemplate();
                if (uriTemplateNames.contains(productResourceKey)) {
                    for (APIProductResource resource : aggregateResources) {
                        String resourceKey =
                                resource.getApiIdentifier() + ":" + resource.getUriTemplate().getUriTemplate();
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
            context.put("apiType", apiProduct.getType());
            context.put("aggregates", aggregateResources);
        }

        return context;
    }
}
