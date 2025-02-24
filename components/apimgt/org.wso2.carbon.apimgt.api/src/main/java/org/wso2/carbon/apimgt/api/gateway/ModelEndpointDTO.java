/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.gateway;

/**
 * DTO representing a model endpoint in API Gateway.
 */
public class ModelEndpointDTO {

    private String model;
    private String endpointId;
    private double weight;

    /**
     * Gets the model name.
     *
     * @return the model name
     */
    public String getModel() {

        return model;
    }

    /**
     * Sets the model name.
     *
     * @param model the model name to set
     */
    public void setModel(String model) {

        this.model = model;
    }

    /**
     * Gets the endpoint ID.
     *
     * @return the endpoint ID
     */
    public String getEndpointId() {

        return endpointId;
    }

    /**
     * Sets the endpoint ID.
     *
     * @param endpointId the endpoint ID to set
     */
    public void setEndpointId(String endpointId) {

        this.endpointId = endpointId;
    }

    /**
     * Gets the weight of the endpoint.
     *
     * @return the weight
     */
    public double getWeight() {

        return weight;
    }

    /**
     * Sets the weight of the endpoint.
     *
     * @param weight the weight to set
     */
    public void setWeight(double weight) {

        this.weight = weight;
    }
}
