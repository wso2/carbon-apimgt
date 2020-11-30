/**
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";
import APIClientFactory from "./APIClientFactory";
import Resource from "./Resource";
import Utils from "./Utils";

/***
 * Class to expose Subscription {Resource} related operations
 */

export default class Subscription extends Resource {

    constructor() {
        super();
        this.client = new APIClientFactory().getAPIClient(Utils.getEnvironment().label).client;
    }

    /**
     * Get all Subscriptions
     * @param apiId id of the API
     * @param applicationId id of the application 
     * @param limit subscription count to return
     * @returns {promise} With all subscription for given applicationId or apiId.
     */
    getSubscriptions(apiId, applicationId, limit = 25) {
        var promise_get = this.client.then((client) => {
            return client.apis["Subscriptions"].get_subscriptions(
                { apiId: apiId, applicationId: applicationId, limit });
        }
        );
        return promise_get;
    }

    /**
     * Get a single subscription
     * @param subscriptionUUID subscription UUID
     */
    getSubscription(subscriptionUUID) {
        var promised_subscription = this.client.then((client) => {
            return client.apis["Subscriptions"].get_subscriptions__subscriptionId_(
                { subscriptionId: subscriptionUUID });
        }
        );
        return promised_subscription;
    }

    /**
    * Get pending invoice if available * @param {*} subscriptionUUID
    */
    getMonetizationInvoice(subscriptionUUID) {
        const promiseInvoice = this.client.then(client => {
            return client.apis['API Monetization'].get_subscriptions__subscriptionId__usage( 
                {
                    subscriptionId: subscriptionUUID 
                }
            );
        });
        return promiseInvoice;
    }

    /**
    * Delete subscription
    * @param subscriptionId id of the subscription
    * @returns {promise} With 200 OK.
    */
    deleteSubscription(subscriptionId) {
        const promised_delete_subscription = this.client.then((client) => {
            return client.apis["Subscriptions"].delete_subscriptions__subscriptionId_(
                {
                    subscriptionId: subscriptionId,
                }
            );
        });
        return promised_delete_subscription;
    }

   /**
    * Update subscription
    * @param subscriptionId id of the subscription
    * @param throttlingPolicy throttling tier
    * @returns {promise} With 200 OK.
    */
    updateSubscription(applicationId, apiId, subscriptionId, throttlingPolicy, status, requestedThrottlingPolicy) {
        const promised_update_subscription = this.client.then((client) => {
        let subscriptionData = null;

            subscriptionData = {
                applicationId, apiId, subscriptionId, throttlingPolicy: throttlingPolicy, status, requestedThrottlingPolicy
            };

            const payload = { 
                subscriptionId: subscriptionId,
            };
            return client.apis.Subscriptions.put_subscriptions__subscriptionId_(
                payload,
                { requestBody: subscriptionData },
                { 'Content-Type': 'application/json' });
        });
        return promised_update_subscription;
    }
}

