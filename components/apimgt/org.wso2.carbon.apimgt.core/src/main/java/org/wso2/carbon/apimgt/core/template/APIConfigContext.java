/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.template;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.util.Collections;
import java.util.Map;

/**
 * Used to generate API meta info related template
 */
public class APIConfigContext extends ConfigContext {

    private String name;
    private String context;
    private String version;
    private String id;
    private String packageName;
    private String serviceNamePrefix = "";
    private Map<String, Endpoint> apiEndpoints = Collections.emptyMap();

    public APIConfigContext(API api, String packageName) {
        this.name = api.getName();
        this.context = api.getContext();
        this.version = api.getVersion();
        this.packageName = packageName;
        this.id = api.getId();
        apiEndpoints = api.getEndpoint();
    }

    public APIConfigContext(CompositeAPI compositeAPI, String gatewayPackageName) {
        this.id = compositeAPI.getId();
        this.name = compositeAPI.getName();
        this.context = compositeAPI.getContext();
        this.version = compositeAPI.getVersion();
        this.packageName = gatewayPackageName;
    }

    @Override
    public void validate() throws APITemplateException {
        //see if api name ,version, context sets
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(context) || StringUtils.isEmpty(version)) {
            throw new APITemplateException("API property validation failed", ExceptionCodes.TEMPLATE_EXCEPTION);
        }

        //adding string prefix if api name starting with a number
        if (Character.isDigit(name.charAt(0))) {
            serviceNamePrefix = "prefix_";
        }
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context = new VelocityContext();
        context.put("version", version);
        context.put("apiContext", this.context.startsWith("/") ? this.context : "/" + this.context);
        context.put("apiEndpoint", apiEndpoints);
        String serviceName = serviceNamePrefix + this.name + "_" + id.replaceAll("-", "_");
        if (serviceName.contains(" ")) {
            serviceName = serviceName.replaceAll(" ", "_");
        }
        context.put("serviceName", serviceName);
        context.put("package", packageName);
        return context;
    }

}
