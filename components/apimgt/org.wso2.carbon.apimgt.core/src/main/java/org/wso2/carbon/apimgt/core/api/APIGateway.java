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
*
*/
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Subscription;

/**
 * The interface used to manage APIs in gateway
 */
public interface APIGateway {

    /**
     * Add API in gateway
     *
     * @param api   API artifact
     * @throws GatewayException     If there is a failure while adding API to gateway
     */
    void addAPI(API api) throws GatewayException;

    /**
     * Update API in gateway
     *
     * @param api   API artifact
     * @throws GatewayException     If there is a failure to update API in gateway
     */
    void updateAPI(API api) throws GatewayException;

    /**
     * Delete API in gateway
     *
     * @param api   API artifact
     * @throws GatewayException     If there is a failure to delete API in gateway
     */
    void deleteAPI(API api) throws GatewayException;

    /**
     * Add API subscription to gateway
     *
     * @param subscription  Subscription details
     * @throws GatewayException     If there is a failure to update subscription
     */
    void addAPISubscription(Subscription subscription) throws GatewayException;

    /**
     * Delete API subscription from gateway
     *
     * @param subscription  Subscription details
     * @throws GatewayException     If there is a failure to update subscription
     */
    void deleteAPISubscription(Subscription subscription) throws GatewayException;

    /**
     * Add endpoint to gateway
     *
     * @param endpoint Endpoint artifact
     * @throws GatewayException     If there is a failure to add endpoint to gateways
     */
    void addEndpoint(Endpoint endpoint) throws GatewayException;

    /**
     * Update endpoint in gateways
     *
     * @param endpoint  Endpoint artifact
     * @throws GatewayException     If there is a failure to update endpoint in gateway
     */
    void updateEndpoint(Endpoint endpoint) throws GatewayException;

    /**
     * Delete endpoint in gateway
     *
     * @param endpoint  Endpoint artifact
     * @throws GatewayException     If there is a failure to delete endpoint in gateway
     */
    void deleteEndpoint(Endpoint endpoint) throws GatewayException;

    /**
     * Add API state change to gateway
     *
     * @param api
     * @param status
     * @throws GatewayException
     */
    void changeAPIState(API api, String status) throws GatewayException;

}
