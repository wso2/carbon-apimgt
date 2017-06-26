package org.wso2.carbon.apimgt.core.template.dto;
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


import org.wso2.carbon.apimgt.core.models.Endpoint;

/**
 * hold the field require to build gateway configuration from template
 */
public class TemplateBuilderDTO {
    private String templateId;
    private String uriTemplate;
    private String httpVerb;
    private String produce;
    private String consume;
    private Endpoint productionEndpoint;
    private Endpoint sandboxEndpoint;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getProduce() {
        return produce;
    }

    public void setProduce(String produce) {
        this.produce = produce;
    }

    public String getConsume() {
        return consume;
    }

    public void setConsume(String consume) {
        this.consume = consume;
    }

    public Endpoint getProductionEndpoint() {
        return productionEndpoint;
    }

    public void setProductionEndpoint(Endpoint productionEndpoint) {
        this.productionEndpoint = productionEndpoint;
    }

    public Endpoint getSandboxEndpoint() {
        return sandboxEndpoint;
    }

    public void setSandboxEndpoint(Endpoint sandboxEndpoint) {
        this.sandboxEndpoint = sandboxEndpoint;
    }
}
