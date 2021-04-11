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


import CONSTS from 'AppData/Constants';
import APIClientFactory from './APIClientFactory';
import Resource from './Resource';
import Wsdl from './Wsdl';
import Utils from './Utils';

/**
 * An abstract representation of an API
 */
export default class API extends Resource {
    /**
     * @constructor
     * @param {string} access_key - Access key for invoking the backend REST API call.
     */
    constructor() {
        super();
        this.client = new APIClientFactory().getAPIClient(Utils.getEnvironment().label).client;
        this.wsdlClient = new Wsdl(this.client);
        this._requestMetaData = Resource._requestMetaData;
    }

    /**
     * (TODO: need to ask for fallback sequence as well tmkb)
     * Get list of all the available APIs, If the call back is given
     * It will be invoked upon receiving the response from REST service.Else will return a promise.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAllAPIs(params = {}, callback = null) {
        const promiseGetAll = this.client.then((client) => {
            return client.apis.APIs.get_apis(params, this._requestMetaData());
        });
        if (callback) {
            return promiseGetAll.then(callback);
        } else {
            return promiseGetAll;
        }
    }

    /**
     * Get details of a given API
     * @param id {string} UUID of the api.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAPIById(id, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.APIs.get_apis__apiId_({ apiId: id }, this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /*
     Get the inline content of a given document
     */
    getInlineContentOfDocument(api_id, docId) {
        const promised_getDocContent = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                documentId: docId,
            };
            return client.apis['API Documents'].get_apis__apiId__documents__documentId__content(payload);
        });
        return promised_getDocContent;
    }

    /**
     * Get the Documents of an API
     * @param id {String} UUID of the API in which the documents needed
     * @param callback {function} Function which needs to be called upon success of getting documents
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getDocumentsByAPIId(id, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis['API Documents'].get_apis__apiId__documents({ apiId: id }, this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get the Documents of an API
     * @param {string} apiId api id.
     * @param {string} documentId document id.
     * @returns {promise} promise to get the document.
     */
    getDocumentByDocId(apiId, documentId) {
        const promiseGet = this.client.then((client) => {
            const payload = {
                apiId,
                documentId,
            };
            return client.apis['Documents'].get_apis__apiId__documents__documentId_(payload, this._requestMetaData());
        });
        return promiseGet;
    }

    /**
     * Get the Document content of an API by document Id
     * @param api_id {String} UUID of the API in which the document needed
     * @param docId {String} UUID of the Document need to view
     * @param callback {function} Function which needs to be called upon success of of getting document.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getFileForDocument(api_id, docId) {
        const promised_getDocContent = this.client.then((client) => {
            const payload = {
                apiId: api_id,
                documentId: docId,
                Accept: 'application/octet-stream',
            };
            return client.apis['API Documents'].get_apis__apiId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });
        return promised_getDocContent;
    }

    /**
     * Get the swagger of an API
     * @param apiId {String} UUID of the API in which the swagger is needed
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSwaggerByAPIId(apiId, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.APIs.get_apis__apiId__swagger({ apiId }, this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get the schema of an GraphQL API
     * @param apiId {String} UUID of the API in which the schema is needed
     * @param callback {function} Function which needs to be called upon success of the retrieving schema
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getGraphQLSchemaByAPIId(apiId, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.APIs.get_apis__apiId__graphql_schema({ apiId }, this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get the swagger of an API
     * @param apiId {String} UUID of the API in which the swagger is needed
     * @param environmentName {String} API environment name
     * @param callback {function} Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSwaggerByAPIIdAndEnvironment(apiId, environmentName, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.APIs.get_apis__apiId__swagger({ apiId, environmentName }, this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get application by id
     * @param id {String} UUID of the application
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getApplication(id, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.Applications.get_applications__applicationId_(
                { applicationId: id },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get application by id
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated Use Application.all method instead
     */
    getAllApplications(callback = null, limit = 25) {
        const promiseGet = this.client.then((client) => {
            return client.apis.Applications.get_applications({ limit }, this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get application by id
     * @param policyLevel
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAllTiers(policyLevel, callback = null) {
        const promiseGetAll = this.client.then((client) => {
            return client.apis['Throttling Policies'].get_throttling_policies__policyLevel_(
                { policyLevel },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGetAll.then(callback);
        } else {
            return promiseGetAll;
        }
    }

    /**
     * Get all application attributes
     * @param {function} callback which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getAllApplicationAttributes() {
        return this.client.then((client) => {
            return client.apis.Settings.get_settings_application_attributes(this._requestMetaData());
        });
    }

    /**
     * Create application
     * @param {object} application content of the application
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    createApplication(application) {
        return this.client.then((client) => {
            const payload = { requestBody: application };
            const args = { 'Content-Type': 'application/json' };
            return client.apis.Applications.post_applications({}, payload, args);
        });
    }

    /**
     * Update an application
     * @param application content that need to updated with the application id
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    updateApplication(application, callback = null) {
        const promiseGet = this.client.then((client) => {
            const payload = { applicationId: application.applicationId };
            return client.apis.Applications.put_applications__applicationId_(
                payload,
                { requestBody: application },
                this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Add new comment to an existing API
     * @param apiId apiId of the api to which the comment is added
     * @param comment comment text
     */
    addComment(apiId, comment, replyTo) {
        return this.client.then((client) => {
            const payload = { apiId, replyTo };
            return client.apis.Comments.addCommentToAPI(
                payload,
                { requestBody: comment },
                this._requestMetaData()
            );
        });
    }

    /**
     * Get all comments for a particular API
     * @param apiId api id of the api to which the comment is added
     */
    getAllComments(apiId, limit, offset) {
        return this.client.then((client) => {
            return client.apis.Comments.getAllCommentsOfAPI({ apiId, limit: limit, offset: offset }, this._requestMetaData());
        });
    }

    /**
     * Delete a comment belongs to a particular API
     * @param apiId api id of the api to which the comment belongs to
     * @param commentId comment id of the comment which has to be deleted
     */
    deleteComment(apiId, commentId) {
        return this.client.then((client) => {
            return client.apis.Comments.deleteComment({ apiId, commentId }, this._requestMetaData());
        });
    }

    /**
     * Update a comment belongs to a particular API
     * @param apiId apiId of the api to which the comment is added
     * @param commentId comment id of the comment which has to be updated
     * @param commentInfo comment text
     */
    updateComment(apiId, commentId, commentInfo, callback = null) {
        const promise = this.client.then((client) => {
            return client.apis['Comment (Individual)'].put_apis__apiId__comments__commentId_(
                { apiId, commentId, body: commentInfo },
                this._requestMetaData(),
            );
        }).catch((error) => {
            console.error(error);
        });
        if (callback) {
            return promise.then(callback);
        } else {
            return promise;
        }
    }
    
    /**
     * Get all replies for a particular comment
     * @param {string} apiId api id of the api for which the comment is added
     * @param {string} commentId id of the comment
     * @param {string} limit number of replies to retrieve
     * @param {string} offset the starting point of replies
     * @returns {promise} promise
     */
    getAllCommentReplies(apiId, commentId, limit, offset) {
        return this.client.then((client) => {
            return client.apis.Comments.getRepliesOfComment({
                commentId, apiId, limit, offset,
            }, this._requestMetaData());
        });
    }

    /**
     * Get all replies for a particular comment
     * @param {string} apiId api id of the api for which the comment is added
     * @param {string} commentId id of the comment
     * @param {string} limit number of replies to retrieve
     * @param {string} offset the starting point of replies
     * @returns {promise} promise
     */
    getAllCommentReplies(apiId, commentId, limit, offset) {
        return this.client.then((client) => {
            return client.apis.Comments.getRepliesOfComment({
                commentId, apiId, limit, offset,
            }, this._requestMetaData());
        });
    }

    /**
     * Get Rating details for a partiuclar API
     * @param {apiId} apiId of the api
     * @returns {promise} promise
     */
    getRatingFromUser(apiId) {
        const promiseGet = this.client.then((client) => {
            return client.apis.Ratings.get_apis__apiId__ratings({ apiId }, this._requestMetaData());
        }).catch((error) => {
            console.error(error);
        });
        return promiseGet;
    }

    /**
     * Remove Rating details for a partiuclar API for the logged in user
     * @param {apiId} apiId of the api
     * @returns {promise} promise
     */
    removeRatingOfUser(apiId) {
        const promiseDelete = this.client.then((client) => {
            return client.apis.Ratings.delete_apis__apiId__user_rating({ apiId }, this._requestMetaData());
        }).catch((error) => {
            console.error(error);
        });
        return promiseDelete;
    }

    /**
     * Add Rating for a partiuclar API by the logged in user
     * @param {apiId} apiId of the api
     * @param {ratingInfo} ratingInfo user rating for the api
     * @returns {promise} promise
     */
    addRating(apiId, ratingInfo) {
        const promise = this.client.then((client) => {
            return client.apis.Ratings.put_apis__apiId__user_rating(
                { apiId },
                { requestBody: ratingInfo },
                this._requestMetaData(),
            );
        }).catch((error) => {
            console.error(error);
        });
        return promise;
    }

    /**
     * Generate application keys
     * @param applicationId id of the application that needs to generate the keys
     * @param request_content payload of generate key request
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated Use Application.generateKeys() instead
     */
    generateKeys(applicationId, requestContent, callback = null) {
        const promiseGet = this.client.then((client) => {
            const payload = { applicationId };
            return client.apis.Applications.post_applications__applicationId__generate_keys(
                payload,
                { requestBody: requestContent },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Generate token
     * @param applicationId id of the application that needs to generate the token
     * @param request_content payload of generate token request
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated Use Application.generateToken() instead
     */
    generateToken(applicationId, requestContent, callback = null) {
        const promiseGet = this.client.then((client) => {
            const payload = { applicationId, body: requestContent };
            return client.apis.Applications.post_applications__applicationId__generate_token(
                payload,
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get keys of an application
     * @param applicationId id of the application that needs to get the keys
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated Use Application.getKeys() instead
     */
    getKeys(applicationId, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.Applications.get_applications__applicationId__keys(
                { applicationId },
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    generateApiKey(applicationId, keyType, validityPeriod, restrictions) {
        const promiseGet = this.client.then((client) => {
            const payload = {
                applicationId, keyType,
            };
            return client.apis['API Keys'].post_applications__applicationId__api_keys__keyType__generate(
                payload,
                {
                    requestBody: {
                        validityPeriod: validityPeriod,
                        additionalProperties: restrictions
                    }
                },
                this._requestMetaData(),
            );
        });
        return promiseGet;
    }

    /**
     * Get keys of an application
     * @param applicationId id of the application that needs to get the keys
     * @param limit subscription count to return
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSubscriptions(apiId, applicationId, limit = 25, callback = null) {
        const payload = { apiId };
        if (applicationId) {
            payload[applicationId] = applicationId;
        }
        payload['limit'] = limit;
        const promisedGet = this.client.then((client) => {
            return client.apis.Subscriptions.get_subscriptions(payload, this._requestMetaData());
        });
        if (callback) {
            return promisedGet.then(callback);
        } else {
            return promisedGet;
        }
    }

    /**
     * Get webhook subscriptions for a web hook Api.
     * @param apiId of the web hook api which holds the topics
     * @param applicationId of the application making the subscription
     * @returns promise
     */
    getWebhookubScriptions(apiId, applicationId) {
        var promisedTopicSubscriptionGet = this.client.then((client) => {
            return client.apis["Webhooks"].get_webhooks_subscriptions(
                { apiId: apiId, applicationId: applicationId });
        }
        );
        return promisedTopicSubscriptionGet;
    }

    /**
     * Get all topics available for a specified webhook API.
     * @param apiId of the web hook api
     * @returns promise
     */
    getAllTopics(apiId) {
        const payload = { apiId };
        const promisedTopicGet = this.client.then((client) => {
            return client.apis.Topics.get_apis__apiId__topics(payload);
        });
        return promisedTopicGet;
    }

    /**
     * Create a subscription
     * @param {string} apiId id of the API that needs to be subscribed
     * @param {string} applicationId id of the application that needs to be subscribed
     * @param {string} policy throttle policy applicable for the subscription
     * @param {function} callback callback url
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    subscribe(apiId, applicationId, policy, callback = null) {
        const promiseCreateSubscription = this.client.then((client) => {
            let subscriptionData = null;

            subscriptionData = {
                apiId, applicationId, throttlingPolicy: policy,
            };

            const payload = { requestBody: subscriptionData };
            return client.apis.Subscriptions.post_subscriptions({}, payload, { 'Content-Type': 'application/json' });
        });
        if (callback) {
            return promiseCreateSubscription.then(callback);
        } else {
            return promiseCreateSubscription;
        }
    }

    /**
     * Get the SDK generation supported languages.
     * @returns {Promise} List of languages that supports SDK generation by swagger-codegen
     */
    getSdkLanguages() {
        const promiseLanguages = this.client.then((client) => {
            return client.apis.SDKs.get_sdk_gen_languages({}, this._requestMetaData());
        });
        return promiseLanguages;
    }

    /**
     * Get the SDK for the API with the specified apiId and language.
     * @returns {Promise} Zip file for the generated SDK.
     */
    getSdk(apiId, language) {
        const payload = { apiId, language };

        const promiseSdk = this.client.then((client) => {
            return client.apis.SDKs.get_apis__apiId__sdks__language_(payload, this._requestMetaData());
        });
        return promiseSdk;
    }

    /**
     * Get details of a given throttling policy
     * @param id {string} name of the tier.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getTierByName(name, level, callback = null) {
        const payload = { policyId: name, policyLevel: level };
        const promiseGet = this.client.then((client) => {
            return client.apis['Throttling Policies'].get_throttling_policies__policyLevel___policyId_(
                payload,
                this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Create new user
     * @param body {JSON object} {username:"", password:"", firstName:"", lastName:"", email:""}
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    createUser(body) {
        const payload = { body };
        const promise = this.client.then((client) => {
            return client.apis['Sign Up'].post_self_signup(payload, { 'Content-Type': 'application/json' });
        });
        return promise;
    }

    /**
     * Get all tags
     * @returns {promise} promise all tags of APIs
     */
    getAllTags() {
        const promiseGet = this.client.then((client) => {
            return client.apis.Tags.get_tags(this._requestMetaData());
        }).catch((error) => {
            console.error(error);
        });
        return promiseGet;
    }

    /**
     * Get the thumnail of an API
     *
     * @param id {string} UUID of the api
     */
    getAPIThumbnail(id) {
        const promised_getAPIThumbnail = this.client.then((client) => {
            return client.apis.APIs.get_apis__apiId__thumbnail({
                apiId: id,
            },
                this._requestMetaData());
        });

        return promised_getAPIThumbnail;
    }

    /**
     * method to search apis and documents based on content
     * @param {Object} params APIs, Documents filtering parameters i:e { "name": "MyBank API"}
     * @returns {Promise} promise object return from SwaggerClient-js
     * @memberof API
     */
    search(params) {
        return this.client.then((client) => {
            return client.apis['Unified Search'].get_search(params, Resource._requestMetaData());
        });
    }


    /**
     * Returns the WSDL API client
     *
     * @return {Wsdl} WSDL API client
     */
    getWsdlClient() {
        return this.wsdlClient;
    }

    /**
     * method to get Developer Portal settings such as grant types, scopes, application sharing settings etc
     * Settings API can be invoked with and without access token, When a token is not present it will return the public
     * settings info, when a valid token is present it will return all the settings info.
     * @returns {Promise} promise object return from SwaggerClient-js
     * @memberof API
     */
    getSettings() {
        return this.client.then((client) => {
            return client.apis.Settings.get_settings(this._requestMetaData());
        });
    }

    /**
     * @static
     * Get the registered key managers.
     * @return {Promise}
     * */
    getKeyManagers() {
        return this.client.then((client) => {
            return client.apis['Key Managers (Collection)'].get_key_managers(this._requestMetaData());
        });
    }

    /**
     * @static
     * Get all API Categories of the given tenant
     * @return {Promise}
     * */
    apiCategories(params) {
        return this.client.then((client) => {
            return client.apis['API Category (Collection)'].get_api_categories(
                params, this._requestMetaData());
        });
    }

    /**
     * Get API recommendations for a given user.
     * @param {string} userId The username.
     * @return {Promise}
     * */
    getApiRecommendations(params = {}) {
        const promiseGet = this.client.then((client) => {
            return client.apis.Recommendations.get_recommendations(params, this._requestMetaData());
        });
        return promiseGet;
    }
    /**
     * Get the complexity related details of an API
     */

    getGraphqlPoliciesComplexity(id) {
        const promisePolicies = this.client.then(client => {
            return client.apis['GraphQL Policies'].get_apis__apiId__graphql_policies_complexity(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promisePolicies.then(response => response.body);
    }

    /**
     * Retrieve all types and fields of a GraphQL Schema
     */
    getGraphqlPoliciesComplexityTypes(id) {
        const promisePolicies = this.client.then(client => {
            return client.apis['GraphQL Policies'].get_apis__apiId__graphql_policies_complexity_types(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promisePolicies.then(response => response.body);
    }

    /**
     * Change password
     */
    changePassword(currentPwd, newPwd, callback = null) {
        const promiseChangePassword = this.client.then((client) => {
            const payload = { currentPassword: currentPwd, newPassword: newPwd };
            return client.apis.Users.changeUserPassword({}, { requestBody: payload }, this._requestMetaData());
        });
        if (callback) {
            return promiseChangePassword.then(callback);
        } else {
            return promiseChangePassword;
        }
    }
}
