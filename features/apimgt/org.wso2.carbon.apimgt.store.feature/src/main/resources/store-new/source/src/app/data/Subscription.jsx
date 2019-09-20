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
     * @returns {promise} With all subscription for given applicationId or apiId.
     */
    getSubscriptions(apiId, applicationId) {
        var promise_get = this.client.then((client) => {
            return client.apis["Subscriptions"].get_subscriptions(
                { apiId: apiId, applicationId: applicationId });
        }
        );
        return promise_get;
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
     * Get pending invoice
     * @param subscriptionId id of the subscription
     * @param {*} subscriptionId 
     */
    getPendingInvoice(subscriptionId) {
        const promised_get_pending_invoice = this.client.then((client) => {
            return client.apis["API Monetization"].get_subscriptions__subscriptionId__usage(
                {
                    subscriptionId: subscriptionId,
                }
            );
        });
        return promised_get_pending_invoice;
    }
}

