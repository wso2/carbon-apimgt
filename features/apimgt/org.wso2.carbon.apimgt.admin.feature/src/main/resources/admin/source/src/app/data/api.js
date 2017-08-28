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
}

export default API
