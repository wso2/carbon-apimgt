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

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Used to generate API meta info related template
 */
public class APIConfigContext extends ConfigContext {

    private API api;
    private String packageName;
    private String serviceNamePrefix = "";
    public APIConfigContext(API api, String packageName) {
        this.api = api;
        this.packageName = packageName;
    }

    @Override
    public void validate() throws APITemplateException {
        //see if api name ,version, context sets
        if (api.getName().isEmpty() || api.getContext().isEmpty() || api.getVersion().isEmpty()) {
            throw new APITemplateException("API property validation failed", ExceptionCodes.TEMPLATE_EXCEPTION);
        }

        //adding string prefix if api name starting with a number
        if (api.getName().matches("^(\\d)+")) {
            serviceNamePrefix = "prefix_";
        }
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context = new VelocityContext();
        context.put("version", api.getVersion());
        context.put("apiContext", api.getContext().startsWith("/") ? api.getContext() : "/" + api.getContext());
        LocalDateTime ldt = api.getCreatedTime();
        Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date res = Date.from(instant);
        String serviceName = serviceNamePrefix + api.getName() + "_" + res.getTime();
        if (serviceName.contains(" ")) {
            serviceName = serviceName.replaceAll(" ", "_");
        }
        context.put("serviceName", serviceName);
        context.put("package", packageName);
        return context;
    }

}
