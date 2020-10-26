/**
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import cloneDeep from 'lodash.clonedeep';
import ServiceCatalogClientFactory from './ServiceCatalogClientFactory';
import Utils from './Utils';
import Resource from './Resource';

/**
  * An abstract representation of a Service Catalog
  */
class ServiceCatalog extends Resource {
    constructor(kwargs) {
        super();
        const properties = kwargs;
        Utils.deepFreeze(properties);
        this._data = properties;
        for (const key in properties) {
            if (Object.prototype.hasOwnProperty.call(properties, key)) {
                this[key] = properties[key];
            }
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
     *
     * Instance method of the ServiceCatalog class to provide raw JSON object
     * which is Service body friendly to use with REST api requests
     * Use this method instead of accessing the private _data object for
     * converting to a JSON representation of an API object.
     * Note: This is deep coping, Use sparingly, Else will have a bad impact on performance
     * Basically this is the revers operation in constructor.
     * This method simply iterate through all the object properties (excluding the properties in `excludes` list)
     * and copy their values to new object.
     * So use this method with care!!
     * @memberof API
     * @param {Array} [userExcludes=[]] List of properties that are need to be excluded from the generated JSON object
     * @returns {JSON} JSON representation of the API
     */
    toJSON(userExcludes = []) {
        let copy = {},
            excludes = [...userExcludes];
        for (var prop in this) {
            if (!excludes.includes(prop)) {
                copy[prop] = cloneDeep(this[prop]);
            }
        }
        return copy;
    }

    /**
     * Get details of a given Service Entry
     * @param id {string} UUID of the Service Entry.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else Service Entry invoke promise.
     */
    static get() {
        const serviceCatalog = new ServiceCatalogClientFactory().getServiceCatalogClient(Utils.getCurrentEnvironment())
            .client;
        return serviceCatalog.then((client) => {
            return client.apis['API Category (Collection)'].get_api_categories(
                {
                    serviceId: id,
                },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Get details of a given Service Entry
     * @param id {string} UUID of the Service Entry.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else Service Entry invoke promise.
     */
    static get(id) {
        const serviceCatalog = new ServiceCatalogClientFactory().getServiceCatalogClient(Utils.getCurrentEnvironment())
            .client;
        return serviceCatalog.then((client) => {
            return client.apis['API Category (Collection)'].get_api_categories(
                {
                    serviceId: id,
                },
                this._requestMetaData(),
            );
        });
    }
}

export default ServiceCatalog;
