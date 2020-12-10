/* eslint-disable no-underscore-dangle */
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

import APIClientFactory from 'AppData/APIClientFactory';
import Resource from './Resource';

/**
 * An abstract representation of an API
 */
class Permissions extends Resource {
    /**
     *Creates an instance of Permissions to Scope mapping.
     * @param {String} role user role name
     * @param {Array} scopes list of scopes associate with above roles
     * @memberof Permissions
     */
    constructor(role, scopes) {
        super();
        this.role = role;
        this.scopes = scopes;
    }


    /**
     *
     *
     * @static
     * @returns
     * @memberof Permissions
     */
    static systemScopes() {
        const apiClient = new APIClientFactory().getAPIClient().client;
        return apiClient.then((client) => {
            return client.apis['System Scopes'].systemScopesGet();
        });
    }

    /**
     *
     *
     * @static
     * @returns
     * @memberof Permissions
     */
    static updateSystemScopes(updatedAPIPermissions) {
        const payload = [];
        for (const appScopes of Object.values(updatedAPIPermissions)) {
            for (const scopeMap of appScopes) {
                payload.push(scopeMap);
            }
        }
        const scopeMapping = { count: payload.length, list: payload };

        const apiClient = new APIClientFactory().getAPIClient().client;
        return apiClient.then((client) => {
            return client.apis['System Scopes'].updateRolesForScope({}, { requestBody: scopeMapping });
        });
    }


    /**
     *
     *
     * @static
     * @returns
     * @memberof Permissions
     */
    static getRoleAliases() {
        const apiClient = new APIClientFactory().getAPIClient().client;
        return apiClient.then((client) => {
            return client.apis['System Scopes'].get_system_scopes_role_aliases();
        });
    }


    /**
     *
     *
     * @static
     * @memberof Permissions
     */
    static updateRoleAliases(updatedRoleAliases) {
        const roleAliasesMapping = { count: updatedRoleAliases.length, list: updatedRoleAliases };
        const apiClient = new APIClientFactory().getAPIClient().client;
        return apiClient.then((client) => {
            return client.apis['System Scopes'].put_system_scopes_role_aliases({}, { requestBody: roleAliasesMapping });
        });
    }
}

export default Permissions;
