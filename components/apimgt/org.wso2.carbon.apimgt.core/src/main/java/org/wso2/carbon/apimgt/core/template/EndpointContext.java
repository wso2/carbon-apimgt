package org.wso2.carbon.apimgt.core.template;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.util.List;

/**
 * Used to generate endpoint gateway service using template
 */
public class EndpointContext extends ConfigContext {
    private List<Endpoint> endpoints;
    private String packageName;
    public EndpointContext(List<Endpoint> endpoints, String packageName) {
        this.endpoints = endpoints;
        this.packageName = packageName;
    }

    @Override
    public void validate() throws Exception {

    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context = new VelocityContext();
        context.put("StringUtils", StringUtils.class);
        context.put("endpoints", this.endpoints);
        context.put("package", packageName);
        return context;
    }

}
