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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Used to generate API meta info related template
 */
public class APIConfigContext extends ConfigContext {

    private String apiName;
    private String context;
    private String version;
    private LocalDateTime createdTime;
    private String packageName;
    private String serviceNamePrefix = "";
    public APIConfigContext(String apiName, String context, String version, LocalDateTime createdTime,
                            String packageName) {
        this.apiName = apiName;
        this.context = context;
        this.version = version;
        this.createdTime = createdTime;
        this.packageName = packageName;
    }

    @Override
    public void validate() throws APITemplateException {
        //see if api name ,version, context sets
        if (apiName.isEmpty() || context.isEmpty() || version.isEmpty()) {
            throw new APITemplateException("API property validation failed", ExceptionCodes.TEMPLATE_EXCEPTION);
        }

        //adding string prefix if api name starting with a number
        if (apiName.matches("^(\\d)+")) {
            serviceNamePrefix = "prefix_";
        }
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("version", version);
        velocityContext.put("apiContext", context.startsWith("/") ? context : "/" + context);
        LocalDateTime ldt = createdTime;
        Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date res = Date.from(instant);
        String serviceName = serviceNamePrefix + apiName + "_" + res.getTime();
        if (serviceName.contains(" ")) {
            serviceName = serviceName.replaceAll(" ", "_");
        }
        velocityContext.put("serviceName", serviceName);
        velocityContext.put("package", packageName);
        return velocityContext;
    }

}
