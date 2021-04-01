/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

import SwaggerClient from 'swagger-client';
import { Mutex } from 'async-mutex';
import Configurations from 'Config';
import AuthManager from 'AppData/AuthManager';
import Utils from './Utils';

/**
 * This class expose single swaggerClient instance created using the given swagger URL (Publisher, Store, ect ..)
 * it's highly unlikely to change the REST API Swagger definition (swagger.json) file on the fly,
 * Hence this singleton class help to preserve consecutive swagger client object creations saving redundant IO
 * operations.
 */
class ServiceCatalogClient {
    /**
     * @param {Object} environment - Environment to get host for the swagger-client's spec property.
     * @param {{}} args - Accept as an optional argument for ServiceCatalogClient constructor. Merge the given args with
     *  default args.
     */
    constructor(environment, args = {}) {
        this.environment = environment || Utils.getCurrentEnvironment();

        if (!ServiceCatalogClient.spec) {
            SwaggerClient.http.withCredentials = true;
            ServiceCatalogClient.spec = SwaggerClient.resolve({
                url: Utils.getServiceCatalogSwaggerURL(),
                requestInterceptor: (request) => {
                    request.headers.Accept = 'text/yaml';
                },
            });
        }
        this._client = ServiceCatalogClient.spec.then((resolved) => {
            const argsv = Object.assign(args, {
                spec: this._fixSpec(resolved.spec),
                requestInterceptor: this._getRequestInterceptor(),
                responseInterceptor: this._getResponseInterceptor(),
            });
            SwaggerClient.http.withCredentials = true;
            return new SwaggerClient(argsv);
        });
        this._client.catch(AuthManager.unauthorizedErrorHandler);
        this.mutex = new Mutex();
    }

    /**
     * Expose the private _client property to public
     * @returns {ServiceCatalogClient} an instance of ServiceCatalogClient class
     */
    get client() {
        return this._client;
    }

    /**
     * Get the ETag of a given resource key from the session storage
     * @param {String} key - key of resource.
     * @returns {String} ETag value for the given key
     */
    static getETag(key) {
        return sessionStorage.getItem('etag_' + key);
    }

    /**
     * Add an ETag to a given resource key into the session storage
     * @param key {string} key of resource.
     * @param etag {string} etag value to be stored against the key
     */
    static addETag(key, etag) {
        sessionStorage.setItem('etag_' + key, etag);
    }

    /**
     * Get Scope for a particular resource path
     *
     * @param resourcePath resource path of the action
     * @param resourceMethod resource method of the action
     */
    static getScopeForResource(resourcePath, resourceMethod) {
        if (!ServiceCatalogClient.spec) {
            SwaggerClient.http.withCredentials = true;
            ServiceCatalogClient.spec = SwaggerClient.resolve({ url: Utils.getServiceCatalogSwaggerURL() });
        }
        return ServiceCatalogClient.spec.then((resolved) => {
            return (
                resolved.spec.paths[resourcePath]
                && resolved.spec.paths[resourcePath][resourceMethod]
                && resolved.spec.paths[resourcePath][resourceMethod].security[0].OAuth2Security[0]
            );
        });
    }

    /**
     * Temporary method to fix the hostname attribute Till following issues get fixed ~tmkb
     * https://github.com/swagger-api/swagger-js/issues/1081
     * https://github.com/swagger-api/swagger-js/issues/1045
     * @param spec {JSON} : Json object of the specification
     * @returns {JSON} : Fixed specification
     * @private
     */
    _fixSpec(spec) {
        const updatedSpec = spec;
        updatedSpec.servers = [{ url: window.origin + '/api/am/service-catalog/v0' }];
        return updatedSpec;
    }

    _getResponseInterceptor() {
        return (data) => {
            if (data.headers.etag) {
                ServiceCatalogClient.addETag(data.url, data.headers.etag);
            }

            // If an unauthenticated response is received, we check whether the token is valid by introspecting it.
            // If it is not valid, we need to clear the stored tokens (in cookies etc) in the browser by redirecting the
            //   user to logout.
            if (data.status === 401 && data.body != null && data.body.description === 'Unauthenticated request') {
                const userData = AuthManager.getUserFromToken();
                userData.catch((error) => {
                    console.error('Error occurred while checking token status. Hence redirecting to login', error);
                    window.location = Configurations.app.context + Utils.CONST.LOGOUT_CALLBACK;
                });
            }
            return data;
        };
    }

    /**
     *
     *
     * @returns
     * @memberof ServiceCatalogClient
     */
    _getRequestInterceptor() {
        return (request) => {
            const existingUser = AuthManager.getUser(this.environment.label);
            if (!existingUser) {
                console.log('User not found. Token refreshing failed.');
                return request;
            }
            let existingToken = AuthManager.getUser(this.environment.label).getPartialToken();
            const refToken = AuthManager.getUser(this.environment.label).getRefreshPartialToken();
            if (existingToken) {
                request.headers.authorization = 'Bearer ' + existingToken;
                return request;
            } else {
                console.log('Access token is expired. Trying to refresh.');
                if (!refToken) {
                    console.log('Refresh token not found. Token refreshing failed.');
                    return request;
                }
            }

            const env = this.environment;
            const promise = new Promise((resolve, reject) => {
                this.mutex.acquire().then((release) => {
                    existingToken = AuthManager.getUser(env.label).getPartialToken();
                    if (existingToken) {
                        request.headers.authorization = 'Bearer ' + existingToken;
                        release();
                        resolve(request);
                    } else {
                        AuthManager.refresh(env).then((res) => res.json())
                            .then(() => {
                                request.headers.authorization = 'Bearer '
                                    + AuthManager.getUser(env.label).getPartialToken();
                                release();
                                resolve(request);
                            }).catch((error) => {
                                console.error('Error:', error);
                                release();
                                reject();
                            })
                            .finally(() => {
                                release();
                            });
                    }
                });
            });

            if (ServiceCatalogClient.getETag(request.url)
                && (request.method === 'PUT' || request.method === 'DELETE' || request.method === 'POST')) {
                request.headers['If-Match'] = ServiceCatalogClient.getETag(request.url);
            }
            return promise;
        };
    }
}

ServiceCatalogClient.spec = null;

export default ServiceCatalogClient;
