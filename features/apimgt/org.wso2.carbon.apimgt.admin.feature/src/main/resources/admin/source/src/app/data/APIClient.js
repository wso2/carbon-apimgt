/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
"use strict";
import SwaggerClient from 'swagger-client'
import AuthManager from './AuthManager'
import Utils from "./Utils";

/**
 * This class expose single swaggerClient instance created using the given swagger URL (Admin, Store, ect ..)
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

        const authorizations = {
            OAuth2Security: {
                token: {access_token: AuthManager.getUser().getPartialToken()}
            }
        };

        SwaggerClient.http.withCredentials = true;
        let promisedResolve = SwaggerClient.resolve({url: Utils.getSwaggerURL()});
        APIClient.spec = promisedResolve;
        this._client = promisedResolve.then(
            resolved => {
                const argsv = Object.assign(args,
                    {
                        spec: this._fixSpec(resolved.spec),
                        authorizations: authorizations,
                        requestInterceptor: this._getRequestInterceptor(),
                        responseInterceptor: this._getResponseInterceptor()
                    });
                SwaggerClient.http.withCredentials = true;
                return new SwaggerClient(argsv);
            }
        );
        this._client.catch(AuthManager.unauthorizedErrorHandler);
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
        return sessionStorage.getItem("etag_" + key);
    }

    /**
     * Add an ETag to a given resource key into the session storage
     * @param key {string} key of resource.
     * @param etag {string} etag value to be stored against the key
     */
    static addETag(key, etag) {
        sessionStorage.setItem("etag_" + key, etag);
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
            APIClient.spec = SwaggerClient.resolve({url: Utils.getSwaggerURL()});
        }
        return APIClient.spec.then(
            resolved => {
                return resolved.spec.paths[resourcePath] && resolved.spec.paths[resourcePath][resourceMethod] && resolved.spec.paths[resourcePath][resourceMethod].security[0].OAuth2Security[0];
            }
        )
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
        return spec;
    }

    _getResponseInterceptor() {
        return (data) => {
            if (data.headers.etag) {
                APIClient.addETag(data.url, data.headers.etag);
            }
            return data;
        }
    }

    _getRequestInterceptor() {
        return (data) => {
            if (APIClient.getETag(data.url) && (data.method === "PUT" || data.method === "DELETE" || data.method === "POST")) {
                data.headers["If-Match"] = APIClient.getETag(data.url);
            }
            return data;
        }
    }
}

APIClient.spec = null;

export default APIClient;