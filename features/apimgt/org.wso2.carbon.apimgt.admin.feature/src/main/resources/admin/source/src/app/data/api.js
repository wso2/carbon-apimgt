/**
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import AuthManager from './AuthManager'
import SingleClient from './SingleClient'

/**
 * An abstract representation of an API
 */
class API {
    /**
     * @constructor
     * @param {string} access_key - Access key for invoking the backend REST API call.
     */
    constructor() {
        this.client = new SingleClient().client;
    }

    /**
     *
     * @param data
     * @returns {object} Metadata for API request
     * @private
     */
    _requestMetaData(data = {}) {
        AuthManager.refreshTokenOnExpire(); /* TODO: This should be moved to an interceptor ~tmkb*/
        let metaData = {
            requestContentType: data['Content-Type'] || "application/json"
        };
        if (data['Accept']) {
            console.console.log(data['Accept']);
            metaData.responseContentType = data['Accept'];
        }
        return metaData;
    }

    /**
     * Get All Workflows.
     * @param type {String} type of the workflow task
     * @returns {Promise} Promised all list of workflows
     */
    getWorkflows(type) {
        return this.client.then(
            (client) => {
                return client.apis["Workflows (Collection)"].get_workflows(
                    {workflowType:type}, this._requestMetaData());
            }
        );
    }

    /**
     * Complete workflow.
     * @param id {String} external workflow reference id
     * @param status {String} status of the task.
     * @returns {Promise} Promised workflow response
     */
    completeWorkflow(id, status) {
        let body = {
                 status: status
               }
        return this.client.then(
            (client) => {
                return client.apis["Workflows (Individual)"].put_workflows__workflowReferenceId_(
                  {
                      workflowReferenceId: id,
                      body: body,
                      'Content-Type': 'application/json'
                  }, this._requestMetaData());
            }
        );
    }

    /**
     * Get subscription level policies.
     * @returns {Promise} Promised policies response
     */
    getSubscriptionLevelPolicies() {
        return this.client.then(
            (client) => {
                return client.apis["Subscription Policies"].get_policies_throttling_subscription(
                    {}, this._requestMetaData());
            }
        );
    }

    /**
     * Get subscription level policy.
     * @returns {Promise} Promised policies response
     */
    getSubscriptionLevelPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Subscription Policies"].get_policies_throttling_subscription__id_(
                    {id: id}, this._requestMetaData());
            }
        );
    }

    /**
     * Delete subscription level policy.
     * @returns {Promise} Promised policies response
     */
    deleteSubscriptionLevelPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Subscription Policies"].delete_policies_throttling_subscription__id_(
                    {id: id}, this._requestMetaData());
            }
        );
    }

    /**
     * update subscription level policy.
     * @returns {Promise} Promised policies response
     */
    updateSubscriptionLevelPolicy(id, body) {
      let payload = {id: id, body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Subscription Policies"].put_policies_throttling_subscription__id_(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Create subscription level policy
     * @returns {Promise} Promised policies response
     */
    createSubscriptionLevelPolicy(body) {
      let payload = {body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Subscription Policies"].post_policies_throttling_subscription(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Get api level policies.
     * @returns {Promise} Promised policies response
     */
    getAPILevelPolicies() {
        return this.client.then(
            (client) => {
                return client.apis["Advanced Policies"].get_policies_throttling_advanced(
                    {}, this._requestMetaData());
            }
        );
    }

    /**
     * Get api level policy.
     * @returns {Promise} Promised policies response
     */
    getAPILevelPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Advanced Policies"].get_policies_throttling_advanced__id_(
                    {id: id}, this._requestMetaData());
            }
        );
    }

    /**
     * Delete API level policy.
     * @returns {Promise} Promised policies response
     */
    deleteAPILevelPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Advanced Policies"].delete_policies_throttling_advanced__id_(
                    {id: id}, this._requestMetaData());
            }
        );
    }

    /**
     * update API level policy.
     * @returns {Promise} Promised policies response
     */
    updateAPILevelPolicy(id, body) {
      let payload = {id: id, body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Advanced Policies"].put_policies_throttling_advanced__id_(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Create API level policy
     * @returns {Promise} Promised policies response
     */
    createAPILevelPolicy(body) {
      let payload = {body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Advanced Policies"].post_policies_throttling_advanced(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Get Application level policies.
     * @returns {Promise} Promised policies response
     */
    getApplicationLevelPolicies() {
        return this.client.then(
            (client) => {
                return client.apis["Application Policies"].get_policies_throttling_application(
                    {}, this._requestMetaData());
            }
        );
    }

    /**
     * Get Application level policy.
     * @returns {Promise} Promised policies response
     */
    getApplicationLevelPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Application Policies"].get_policies_throttling_application__id_(
                    {id: id}, this._requestMetaData());
            }
        );
    }

    /**
     * Delete Application level policy.
     * @returns {Promise} Promised policies response
     */
    deleteApplicationLevelPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Application Policies"].delete_policies_throttling_application__id_(
                    {id: id}, this._requestMetaData());
            }
        );
    }

    /**
     * update Application level policy.
     * @returns {Promise} Promised policies response
     */
    updateApplicationLevelPolicy(id, body) {
      let payload = {id: id, body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Application Policies"].put_policies_throttling_application__id_(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Create Application level policy
     * @returns {Promise} Promised policies response
     */
    createApplicationLevelPolicy(body) {
      let payload = {body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Application Policies"].post_policies_throttling_application(
                    payload, this._requestMetaData());
            }
        );
    }
}

export default API
