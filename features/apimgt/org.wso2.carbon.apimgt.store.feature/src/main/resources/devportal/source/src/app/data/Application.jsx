/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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


import APIClientFactory from './APIClientFactory';
import Resource from './Resource';
import Utils from './Utils';

/**
 * Class to expose Application {Resource} related operations i:e: Get all Application , Delete, Generate Keys ect..
 * @param {string} name Application name
 * @param {string} description Application description
 * @param {string} throttlingTier Application throttling tier
 * @param {string} kwargs Arguments
 */
export default class Application extends Resource {
    constructor(name, description, throttlingTier, kwargs) {
        super();
        this.id = kwargs ? kwargs.applicationId : null;
        this.client = new APIClientFactory().getAPIClient(Utils.getEnvironment().label).client;
        this.productionKeys = new Map();
        this.sandboxKeys = new Map();
        this.productionTokens = new Map();
        this.sandboxTokens = new Map();
        for (const key in kwargs) {
            if (kwargs.hasOwnProperty(key)) {
                if (key === 'keys') {
                    this._setKeys(kwargs[key]);
                    continue;
                }
                this[key] = kwargs[key];
            }
        }
    }

    /** *
     * Set this.keys object by iterating the keys array received from REST API
     * @param {Array} keys  An array of keys object containing either PRODUCTION or/and SANDBOX key information
     * @private
     */
    _setKeys(keys) {
        for (const keyObj of keys) {
            if (keyObj.keyType === 'PRODUCTION') {
                this.productionKeys.set(keyObj.keyManager, keyObj);
            } else {
                this.sandboxKeys.set(keyObj.keyManager, keyObj);
            }
        }
    }

    /** *
     * Set this.tokens object by iterating the keys array received from REST API
     * @param {Array} keys  An array of keys object containing either PRODUCTION or/and SANDBOX key information
     * @private
     */
    _setTokens(keys) {
        for (const keyObj of keys) {
            if (keyObj.keyType === 'PRODUCTION') {
                this.productionTokens.set(keyObj.keyManager, keyObj.token);
            } else {
                this.sandboxTokens.set(keyObj.keyManager, keyObj.token);
            }
        }
    }

    /** *
     * Get keys of the current instance of an application
     * @param  {string} keyType Key type either `Production` or `SandBox`
     * @returns {promise} Set the fetched CS/CK into current instance and return keys array as Promise object
     */
    getKeys(keyType) {
        return this.client.then((client) => client.apis['Application Keys']
            .get_applications__applicationId__oauth_keys({ applicationId: this.applicationId }))
            .then((keysResponse) => {
                const keys = keysResponse.obj.list;
                this._setKeys(keys);
                this._setTokens(keys);
                if (keyType === 'PRODUCTION'){
                    return this.productionKeys;
                } else {
                    return this.sandboxKeys;
                }         
            });
    }

    /** *
     * Generate token for this application instance
     * @param {string} type token type
     * @param {string} validityPeriod token validityPeriod
     * @param {string} selectedScopes token scopes
     * @returns {promise} Set the generated token into current
     * instance and return tokenObject received as Promise object
     */
    generateToken(selectedTab, type, validityPeriod, selectedScopes) {
        const promiseToken = this.getKeys()
            .then(() => this.client)
            .then((client) => {
                let keys;
                if (type === 'PRODUCTION') {
                    keys = this.productionKeys.get(selectedTab); 
                } else {
                    keys = this.sandboxKeys.get(selectedTab); 
                }
                const keyMappingId = keys.keyMappingId;
                let accessToken;
                if (type === 'PRODUCTION') {
                    accessToken = this.productionTokens.get(selectedTab); 
                } else {
                    accessToken = this.sandboxTokens.get(selectedTab); 
                }
                const requestContent = {
                    consumerSecret: keys.consumerSecret,
                    validityPeriod,
                    revokeToken: accessToken.accessToken,
                    scopes: selectedScopes,
                    additionalProperties: keys.additionalProperties,
                };
                const payload = { applicationId: this.id, keyMappingId: keyMappingId };
                const body = { requestBody: requestContent };
                return client.apis['Application Tokens']
                    .post_applications__applicationId__oauth_keys__keyMappingId__generate_token(payload, body);
            });
        return promiseToken.then((tokenResponse) => {
            const token = tokenResponse.obj;
            if (type === 'PRODUCTION') {
                this.productionTokens.set(selectedTab, token);
            } else {
                this.sandboxTokens.set(selectedTab, token);
            }
            return token;
        });
    }

    /** *
     * Generate Consumer Secret and Consumer Key for this application instance
     * @param {string} keyType Key type either `Production` or `SandBox`
     * @param {string[]} supportedGrantTypes Grant types supported
     * @param  {string} callbackUrl callback url
     * @param  {string} tokenType Token type either `OAUTH` or `JWT`
     * @param {string} additionalProperties additional properties that needed for application.
     * @returns {promise} Set the generated token into current instance and return tokenObject
     * received as Promise object
     */
    generateKeys(keyType, supportedGrantTypes, callbackUrl, additionalProperties, keyManager) {
        const promisedKeys = this.client.then((client) => {
            const requestContent = {
                keyType, /* TODO: need to support dynamic key types ~tmkb */
                grantTypesToBeSupported: supportedGrantTypes,
                callbackUrl,
                additionalProperties,
                keyManager,
                validityTime: 3600,
                scopes: ['default'],
            };
            const payload = { applicationId: this.id };
            const body = { requestBody: requestContent };
            return client.apis['Application Keys'].post_applications__applicationId__generate_keys(payload, body);
        });
        return promisedKeys.then((keysResponse) => {
            if (keyType === 'PRODUCTION') {
                this.productionKeys.set(keyManager, keysResponse.obj);
                return this.productionKeys.get(keyManager);
            } else {
                this.sandboxKeys.set(keyManager, keysResponse.obj);
                return this.sandboxKeys.get(keyManager);
            }
        });
    }

    /** *
     * Cleanup Consumer Secret and Consumer Key for this application instance
     * @param {string} keyType Key type either `Production` or `SandBox`
     * @returns {promise} Set the generated token into current instance and return tokenObject
     * received as Promise object
     */
    cleanUpKeys(keyType, keyManager, keyMappingId) {
        const requestContent = {
            keyType, 
            keyMappingId,
            keyManager,
        };
        const payload = { applicationId: this.id, keyMappingId, body: requestContent };
        return this.client.then((client) => client.apis['Application Keys']
            .post_applications__applicationId__oauth_keys__keyMappingId__clean_up(payload))
            .then((response) => {
                if (keyType === 'PRODUCTION') {
                    this.productionKeys = new Map();
                } else {
                    this.sandboxKeys = new Map();
                }
                return response.ok;
            });
    }

    /** *
     * Generate Consumer Secret and Consumer Key for this application instance
     * @param  {string} tokenType Token Type either `OAUTH` or `JWT`
     * @param  {string} keyType Key type either `Production` or `SandBox`
     * @param {string[]} supportedGrantTypes Grant types supported
     * @param  {string} callbackUrl callback url
     * @param  {String} consumerKey Consumer key of application
     * @param  {String} consumerSecret Consumer secret of application
     * @param  {String} additionalProperties Additional properties for the oauth application
     * @returns {promise} Update the callbackURL and/or supportedGrantTypes
     */
    updateKeys(tokenType, keyType, supportedGrantTypes, callbackUrl, consumerKey, consumerSecret, additionalProperties, keyManager, keyMappingId) {
        const promisedPut = this.client.then((client) => {
            const requestContent = {
                keyManager,
                keyMappingId,
                consumerKey,
                consumerSecret,
                supportedGrantTypes,
                callbackUrl,
                keyType,
                tokenType,
                additionalProperties
            };
            const payload = { applicationId: this.id, keyMappingId };
            return client.apis['Application Keys'].put_applications__applicationId__oauth_keys__keyMappingId_(
                payload,
                { requestBody: requestContent },
            );
        });
        return promisedPut.then((keysResponse) => {
            if (keyType === 'PRODUCTION') {
                this.productionKeys.set(keyManager, keysResponse.obj);
                return this.productionKeys.get(keyManager);
            } else {
                this.sandboxKeys.set(keyManager, keysResponse.obj);
                return this.sandboxKeys.get(keyManager);
            }
        });
    }

    /**
     * Regenerate Consumer Secret for this application instance
     * @param {String} consumerKey Consumer key of application
     * @param {string} keyType Key type either `Production` or `SandBox`
     * @returns {promise} Update the consumerSecret
     */
    regenerateSecret(consumerKey, keyType, keyMappingId, keyManager) {
        const requestContent = {
            keyManager,
            keyMappingId,
            consumerKey,
            keyType,
        };
        const promisedPost = this.client.then((client) => {
            const payload = { applicationId: this.id, keyMappingId, body: requestContent };
            return client.apis['Application Keys']
                .post_applications__applicationId__oauth_keys__keyMappingId__regenerate_secret(payload);
        });
        return promisedPost.then((secretResponse) => {
            const secret = secretResponse.obj;
            if (keyType === 'PRODUCTION') {
                this.productionKeys.set(keyManager, secretResponse.obj);
            } else {
                this.sandboxKeys.set(keyManager, secretResponse.obj);
            }
            return secret;
        });
    }

    /**
     * Provide Consumer Key and Secret of Existing OAuth Apps
     *
     * @param keyType           key type, either PRODUCTION or SANDBOX
     * @param consumerKey       consumer key of the OAuth app
     * @param consumerSecret    consumer secret of the OAuth app
     * @returns {*}
     */
    provideKeys(keyType, consumerKey, consumerSecret, keyManager) {
        const promisedKeys = this.client.then((client) => {
            const requestContent = { consumerKey, consumerSecret, keyType, keyManager};
            const payload = { applicationId: this.id };
            const body = { requestBody: requestContent };
            return client.apis['Application Keys'].post_applications__applicationId__map_keys(payload, body);
        });
        return promisedKeys.then((keysResponse) => {
            if (keyType === 'PRODUCTION') {
                this.productionKeys.set(keyManager, keysResponse.obj);
                return this.productionKeys.get(keyManager);
            } else {
                this.sandboxKeys.set(keyManager, keysResponse.obj);
                return this.sandboxKeys.get(keyManager);
            }
        });
    }

    static get(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getEnvironment());
        const promisedGet = apiClient.client.then((client) => {
            return client.apis.Applications.get_applications__applicationId_(
                { applicationId: id },
                this._requestMetaData(),
            );
        });
        return promisedGet.then((response) => {
            const appJson = response.obj;
            return new Application(appJson.name, appJson.description, appJson.throttlingTier, appJson);
        });
    }

    static all(limit = 3, offset = null, sortOrder = 'asc', sortBy = 'name', query = '') {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getEnvironment());
        const promisedAll = apiClient.client.then((client) => {
            return client.apis.Applications.get_applications({
                limit, offset, sortOrder, sortBy, query,
            }, this._requestMetaData());
        });
        return promisedAll.then((response) => response.obj);
    }

    static deleteApp(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getEnvironment());
        const promisedDelete = apiClient.client.then((client) => {
            return client.apis.Applications.delete_applications__applicationId_(
                { applicationId: id },
                this._requestMetaData(),
            );
        });
        return promisedDelete.then((response) => response.ok);
    }
}

Application.KEY_TYPES = {
    PRODUCTION: 'PRODUCTION',
    SANDBOX: 'SANDBOX',
};

Application.TOKEN_TYPES = {
    JWT: { key: 'JWT', displayName: 'Self-contained (JWT)' },
    OAUTH: { key: 'OAUTH', displayName: 'Reference (Opaque)' },
};
