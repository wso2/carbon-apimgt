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
import Swagger from 'swagger-client'
import AuthManager from './AuthManager'

/**
 * This class expose single swaggerClient instance created using the given swagger URL (Publisher, Store, ect ..)
 * it's highly unlikely to change the REST API Swagger definition (swagger.json) file on the fly,
 * Hence this singleton class help to preserve consecutive swagger client object creations saving redundant IO operations.
 */
class SingleClient {
    /**
     * Check for already created instance of the class, in the `SingleClient._instance` variable,
     * and return single instance if already exist, else assign `SingleClient._instance` to current instance and return
     * @param {{}} args : Accept as an optional argument for SwaggerClient constructor.Merge the given args with default args.
     * @returns {SingleClient|*|null}
     */
    constructor(args = {}) {
        if (SingleClient._instance) {
            return SingleClient._instance;
        }
        const authorizations = {
            OAuth2Security: {
                token: {access_token: AuthManager.getUser().getPartialToken()}
            }
        };
        let promisedResolve = Swagger.resolve({url: this._getSwaggerURL()});
        this._client = promisedResolve.then(
            resolved => {
                const argsv = Object.assign(args,
                    {
                        spec: this._fixSpec(resolved.spec),
                        authorizations: authorizations,
                        requestInterceptor: this._getRequestInterceptor(),
                        responseInterceptor: this._getResponseInterceptor()
                    });
                return new Swagger(argsv);
            }
        );
        this._client.catch(AuthManager.unauthorizedErrorHandler);
        SingleClient._instance = this;
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
        spec.host = window.location.host; //TODO: Set hostname according to the APIM environment selected by user ~tmkb
        return spec;
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

    _getResponseInterceptor() {
        return (data) => {
            if (data.headers.etag) {
                SingleClient.addETag(data.url, data.headers.etag);
            }
            return data;
        }
    }

    _getRequestInterceptor() {
        return (data) => {
            if (SingleClient.getETag(data.url) && (data.method === "PUT" || data.method === "DELETE" || data.method === "POST")) {
                data.headers["If-Match"] = SingleClient.getETag(data.url);
            }
            return data;
        }
    }

    /**
     * Expose the private _client property to public
     * @returns {SwaggerClient} an instance of SwaggerClient class
     */
    get client() {
        return this._client;
    }

    _getSwaggerURL() {
        /* TODO: Read this from configuration ~tmkb*/
        return window.location.protocol + "//" + window.location.host + "/api/am/publisher/v1.0/apis/swagger.yaml";
    }
}

SingleClient._instance = null; // A private class variable to preserve the single instance of a swaggerClient

export default SingleClient