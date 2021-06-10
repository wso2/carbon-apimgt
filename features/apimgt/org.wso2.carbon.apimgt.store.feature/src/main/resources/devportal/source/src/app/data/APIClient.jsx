/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Settings from 'Settings';
import queryString from 'query-string';
import AuthManager from './AuthManager';
import Utils from './Utils';

/**
 * This class expose single swaggerClient instance created using the given swagger URL (Publisher, Store, ect ..)
 * it's highly unlikely to change the REST API Swagger definition (swagger.json) file on the fly,
 * Hence this singleton class help to preserve consecutive swagger client object creations saving redundant IO operations.
 */
class APIClient {
    /**
     * @param {String} host : Host of apis. Host for the swagger-client's spec property.
     * @param {{}} args : Accept as an optional argument for APIClient constructor.Merge the given args with default args.
     * @returns {APIClient|*|null}
     */
    constructor(host, args = {}) {
        this.host = host || location.host;
        this.environment = Utils.getCurrentEnvironment();
        const authorizations = {
            OAuth2Security: {
                token: { access_token: AuthManager.getUser() ? AuthManager.getUser().getPartialToken() : '' },
            },
        };

        SwaggerClient.http.withCredentials = true;
        const promisedResolve = SwaggerClient.resolve({ url: Utils.getSwaggerURL(), requestInterceptor: (request) => { request.headers.Accept = 'text/yaml'; } });
        APIClient.spec = promisedResolve;
        this._client = promisedResolve.then((resolved) => {
            const argsv = Object.assign(
                args,
                {
                    spec: this._fixSpec(resolved.spec),
                    authorizations,
                    requestInterceptor: this._getRequestInterceptor(),
                    responseInterceptor: this._getResponseInterceptor(),
                },
            );
            SwaggerClient.http.withCredentials = true;
            return new SwaggerClient(argsv);
        });
        this._client.catch(AuthManager.unauthorizedErrorHandler);
        this.mutex = new Mutex();
    }

    /**
     * Expose the private _client property to public
     * @returns {APIClient} an instance of APIClient class
     */
    get client() {
        return this._client;
    }

    /**
     * Get the ETag of a given resource key from the session storage
     * @param key {string} key of resource.
     * @returns {string} ETag value for the given key
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
        if (!APIClient.spec) {
            SwaggerClient.http.withCredentials = true;
            APIClient.spec = SwaggerClient.resolve({ url: Utils.getSwaggerURL() });
        }
        return APIClient.spec.then((resolved) => {
            return resolved.spec.paths[resourcePath] && resolved.spec.paths[resourcePath][resourceMethod] && resolved.spec.paths[resourcePath][resourceMethod].security[0].OAuth2Security;
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
        spec.host = this.host;
        spec.basePath = Settings.app.proxy_context_path
            ? Settings.app.proxy_context_path + spec.basePath
            : spec.basePath;
        spec.security = [{ OAuth2Security: ['apim:api_subscribe'] }];
        return spec;
    }

    _getResponseInterceptor() {
        return (data) => {
            if (data.headers.etag) {
                APIClient.addETag(data.url, data.headers.etag);
            }

            // If an unauthenticated response is received, we check whether the token is valid by introspecting it.
            // If it is not valid, we need to clear the stored tokens (in cookies etc) in the browser by redirecting the
            //   user to logout.
            if (data.status === 401 && data.obj != null && data.obj.description === 'Unauthenticated request') {
                const userData = AuthManager.getUserFromToken();
                const existingUser = AuthManager.getUser(this.environment.label);
                if (existingUser) {
                    userData.then((user) => {
                        if (user) {
                            window.location = Settings.app.context + Utils.CONST.LOGOUT_CALLBACK;
                        }
                    }).catch((error) => {
                        console.error('Error occurred while checking token status. Hence redirecting to login', error);
                        window.location = Settings.app.context + Utils.CONST.LOGOUT_CALLBACK;
                    });
                } else {
                    console.error('Attempted a call to a protected API without a proper access token');
                }
            }
            return data;
        };
    }

    /**
     * Interceptor for each request
     * @returns {Object}
     * @memberof APIClient
     */
    _getRequestInterceptor() {
        return (request) => {
            const { location } = window;
            if (location) {
                const { tenant } = queryString.parse(location.search);
                if (tenant) {
                    request.headers['X-WSO2-Tenant'] = tenant;
                }
            }

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
            const promise = this.mutex.acquire().then((release) => {
                existingToken = AuthManager.getUser(env.label).getPartialToken();
                if (existingToken) {
                    request.headers.authorization = 'Bearer ' + existingToken;
                    release();
                    return request;
                } else {
                    return AuthManager.refresh(env).then((res) => res.json())
                        .then(() => {
                            request.headers.authorization = 'Bearer '
                                + AuthManager.getUser(env.label).getPartialToken();
                            return request;
                        }).catch((error) => {
                            console.error('Error:', error);
                        })
                        .finally(() => {
                            release();
                        });
                }
            });

            if (APIClient.getETag(request.url)
                && (request.method === 'PUT' || request.method === 'DELETE' || request.method === 'POST')) {
                request.headers['If-Match'] = APIClient.getETag(request.url);
            }
            return promise;
        };
    }
}

APIClient.spec = null;

export default APIClient;
