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

/* eslint-disable */
import APIClientFactory from './APIClientFactory';
import Utils from './Utils';
import Resource from './Resource';
import cloneDeep from 'lodash.clonedeep';

/**
 * An abstract representation of an Comments
 */
class Comments extends Resource {
    constructor(parentId, content, category, kwargs) {
        super();
        let properties = kwargs;
        if (parentId instanceof Object) {
            properties = parentId;
            Utils.deepFreeze(properties);
            this._data = properties;
            for (const key in properties) {
                if (Object.prototype.hasOwnProperty.call(properties, key)) {
                    this[key] = properties[key];
                }
            }
        } else {
            this.parentId = parentId;
            this.content = content;
            this.category = category;
            this.id = null;
            this.replies = [];
        }

    }

    /**
     *
     * @param data
     * @returns {object} Metadata for API request
     * @private
     */
    _requestMetaData() {
        Resource._requestMetaData();
    }

    /**
     * Add new comment to an existing API
     * @param apiId apiId of the api to which the comment is added
     * @param commentInfo comment text
     * * TODO: remove
     */
    static add(apiId, comment, replyTo) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(),
            Utils.CONST.API_CLIENT).client;
        return apiClient.then(client => {
            return client.apis.Comments.addCommentToAPI(
                { apiId, replyTo },
                { requestBody: comment }, Resource._requestMetaData()
            );
        })
    }

    /**
     * Get all comments for a particular API
     * @param apiId api id of the api to which the comment is added
     * * TODO: remove
     */
     static all(apiId, limit, offset) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(),
            Utils.CONST.API_CLIENT).client;
        return apiClient.then(client => {
            return client.apis.Comments.getAllCommentsOfAPI({ apiId , limit, offset });
        })
    }

    /**
     * Delete a comment belongs to a particular API
     * @param apiId api id of the api to which the comment belongs to
     * @param commentId comment id of the comment which has to be deleted
     * * TODO: remove
     */
    deleteComment(apiId, commentId, callback = null) {
        let promise = this.client
            .then(client => {
                return client.apis.Comments.deleteComment(
                    { apiId: apiId, commentId: commentId },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        if (callback) {
            return promise.then(callback);
        } else {
            return promise;
        }
    }

    /**
     * Update a comment belongs to a particular API
     * @param apiId apiId of the api to which the comment is added
     * @param commentId comment id of the comment which has to be updated
     * @param commentInfo comment text
     * TODO: remove
     */
    updateComment(apiId, commentId, commentInfo, callback = null) {
        let promise = this.client
            .then(client => {
                return client.apis['Comment (Individual)'].put_apis__apiId__comments__commentId_(
                    { apiId: apiId, commentId: commentId, body: commentInfo },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        if (callback) {
            return promise.then(callback);
        } else {
            return promise;
        }
    }

}


Comments.CONSTS = {

};

Object.freeze(Comments.CONSTS);

export default Comments;
