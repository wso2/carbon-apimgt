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
import APIClientFactory from "./APIClientFactory";
import Utils from "./Utils";

/**
 * An abstract representation of an API
 */
class API {
    /**
     * @constructor
     * @param {string} access_key - Access key for invoking the backend REST API call.
     */
    constructor() {
        this.client = new APIClientFactory().getAPIClient(Utils.getEnvironment().label).client;
    }

    /**
     *
     * @param data
     * @returns {object} Metadata for API request
     * @private
     */
    _requestMetaData(data = {}) {
        AuthManager.refreshTokenOnExpire();
        /* TODO: This should be moved to an interceptor ~tmkb*/
        let metaData = {
            requestContentType: data['Content-Type'] || "application/json"
        };
        if (data['Accept']) {
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
                    {workflowType: type}, this._requestMetaData());
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

    /**
     * Get all threat protection policies
     * @returns {Promise} promised threat protection policies response
     */
    getThreatProtectionPolicies() {
        return this.client.then(
            (client) => {
                return client.apis["All Threat Protection Policies"].get_threat_protection_policies();
            }
        );
    }

    /**
     * Get a threat protection policy
     * @param id ID of the threat protection policy
     * @returns {Promise} promised threat protection policy
     */
    getThreatProtectionPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Threat Protection Policy"].get_threat_protection_policies__threatProtectionPolicyId_({threatProtectionPolicyId: id});
            }
        );
    }

    /**
     * Add a threat protection policy
     * @param policy Threat protection policy
     */
    addThreatProtectionPolicy(policy) {
        //update policy
        if (policy.uuid) {
            console.log("update policy: ");
            console.log(policy);
            return this.client.then(
                (client) => {
                    return client.apis["Update Threat Protection Policy"].post_threat_protection_policies__threatProtectionPolicyId_(
                        {threatProtectionPolicyId: policy.uuid, threatProtectionPolicy: policy}
                    )
                }
            );
        } else {
            console.log("New Policy");
            //add policy
            return this.client.then(
                (client) => {
                    return client.apis["Add Threat Protection Policy"].post_threat_protection_policies(
                        {threatProtectionPolicy: policy}
                    )
                }
            );
        }
    }

    /**
     * Delete a threat protection policy
     * @param id ID of the threat protection policy
     */
    deleteThreatProtectionPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Delete Threat Protection Policy"].delete_threat_protection_policies__threatProtectionPolicyId_({threatProtectionPolicyId: id});
            }
        );
    }

    /**
     * Create custom rule policy
     * @returns {Promise} Promised policies response
     */
    createCustomRulePolicy(body) {
        let payload = {body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Custom Rules"].post_policies_throttling_custom(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Get custom rule policies.
     * @returns {Promise} Promised policies response
     */
    getCustomRulePolicies() {
        return this.client.then(
            (client) => {
                return client.apis["Custom Rules"].get_policies_throttling_custom(
                    {}, this._requestMetaData());
            }
        );
    }

    /**
     * Delete custom rule policy.
     * @returns {Promise} Promised policies response
     */
    deleteCustomRulePolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Custom Rules"].delete_policies_throttling_custom__ruleId_(
                    {ruleId: id}, this._requestMetaData());
            }
        );
    }

    /**
     * Get custom rule policy.
     * @returns {Promise} Promised policies response
     */
    getCustomRulePolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Custom Rules"].get_policies_throttling_custom__ruleId_(
                    {ruleId: id}, this._requestMetaData());
            }
        );
    }

    /**
     * update custom rule policy.
     * @returns {Promise} Promised policies response
     */
    updateCustomRulePolicy(id, body) {
        let payload = {ruleId: id, body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Custom Rules"].put_policies_throttling_custom__ruleId_(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Create black list policy
     * @returns {Promise} Promised policies response
     */
    createBlackListPolicy(body) {
        let payload = {body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Blacklist"].post_blacklist(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Get custom rule policies.
     * @returns {Promise} Promised policies response
     */
    getBlockListPolicies() {
        return this.client.then(
            (client) => {
                return client.apis["Blacklist"].get_blacklist(
                    {}, this._requestMetaData());
            }
        );
    }

    /**
     * update black list policy.
     * @returns {Promise} Promised policies response
     */
    updateBlackListPolicy(id, body) {
        let payload = {conditionId: id, body: body, "Content-Type": "application/json"};
        return this.client.then(
            (client) => {
                return client.apis["Blacklist condition"].put_blacklist__conditionId_(
                    payload, this._requestMetaData());
            }
        );
    }

    /**
     * Delete black list policy.
     * @returns {Promise} Promised policies response
     */
    deleteBlackListPolicy(id) {
        return this.client.then(
            (client) => {
                return client.apis["Blacklist"].delete_blacklist__conditionId_(
                    {conditionId: id}, this._requestMetaData());
            }
        );
    }

}

export default API