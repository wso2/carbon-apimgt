/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.configurations;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.configurations.models.APIMUIConfigurations;
import org.wso2.carbon.apimgt.rest.api.configurations.utils.bean.EnvironmentConfigBean;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class provides configurations information for UI
 */
@Component(
        name = "org.wso2.carbon.apimgt.rest.api.configurations.ConfiguratoinsAPI",
        service = Microservice.class,
        immediate = true
)
@Path("/configService")
public class ConfigurationsAPI implements Microservice {

    /**
     * Get environment configurations from deployement.yaml and returns the list of environments
     *
     * @return Response List of environments: {"environments":[
     *     {"host":"localhost:9292","loginTokenPath":"/login/token","label":"Development"},
     *     {"host":"localhost:9293","loginTokenPath":"/login/token","label":"Production"}
     * ]}
     */
    @GET
    @Path("/environments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response environments() {
        APIMUIConfigurations apimUIConfigurations = ConfigurationService.getInstance().getApimUIConfigurations();

        EnvironmentConfigBean environmentConfigBean = new EnvironmentConfigBean();
        environmentConfigBean.setEnvironments(apimUIConfigurations.getEnvironments());

        return Response.ok(environmentConfigBean, MediaType.APPLICATION_JSON).build();
    }
}
