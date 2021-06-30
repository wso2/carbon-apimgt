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
/* TODO: Move this file to components/Shared/ location ~tmkb */
import React from 'react';
import PropTypes from 'prop-types';
import APIClient from 'AppData/APIClient';
import AuthManager from 'AppData/AuthManager';

const resourcePath = {
    APIS: '/apis',
    API_PRODUCTS: '/api-products',
    SINGLE_API: '/apis/{apiId}',
    SINGLE_API_PRODUCT: '/api-products/{apiProductId}',
    API_SWAGGER: '/apis/{apiId}/swagger',
    API_WSDL: '/apis/{apiId}/wsdl',
    API_GW_CONFIG: '/apis/{apiId}/gateway-config',
    API_THUMBNAIL: '/apis/{apiId}/thumbnail',
    API_COPY: '/apis/copy-api',
    API_LC_HISTORY: '/apis/{apiId}/lifecycle-history',
    API_CHANGE_LC: '/apis/change-lifecycle',
    API_LC: '/apis/{apiId}/lifecycle',
    API_LC_PENDING_TASK: '/apis/{apiId}/lifecycle/lifecycle-pending-task',
    API_DEF: '/apis/import-definition',
    API_VALIDATE_DEF: '/apis/validate-definition',
    API_DOCS: '/apis/{apiId}/documents',
    API_DOC: "'/apis/{apiId}/documents/{documentId}'",
    API_DOC_CONTENT: "'/apis/{apiId}/documents/{documentId}/content'",
    EXPORT_APIS: '/export/apis',
    IMPORT_APIS: '/import/apis',
    SUBSCRIPTION: '/subscriptions',
    SUBSCRIPTIONS: '/subscriptions',
    BLOCK_SUBSCRIPTION: '/subscriptions/block-subscription',
    UNBLOCK_SUBSCRIPTION: '/subscriptions/unblock-subscription',
    POLICIES: "'/policies/{tierLevel}'",
    POLICY: "'/policies/{tierLevel}/{tierName}'",
    ENDPOINTS: '/endpoints',
    ENDPOINT: '/endpoints/{endpointId}',
    LABLES: '/labels',
    WORKFLOW: '/workflows/{workflowReferenceId}',
    SERVICE_DISCOVERY: '/external-resources/services',
    SERVICES: '/services',
    SINGLE_SERVICE: '/services/{serviceId}',
    SINGLE_SERVICE_DEFINITION: '/services/{serviceId}/definition',
    IMPORT_SERVICE: '/services/import',
    EXPORT_SERVICE: '/services/export',
};

const resourceMethod = {
    POST: 'post',
    PUT: 'put',
    GET: 'get',
    DELETE: 'delete',
    HEAD: 'head',
};

/**
 * Show element iff user has proper scope for the view/action
 * @class ScopeValidation
 * @extends {React.Component}
 */
export default class ScopeValidation extends React.Component {
    /**
     *
     * Get scope for resources
     * @static
     * @param {String} resourcePath
     * @param {String} resourceMethod
     * @returns Boolean
     * @memberof AuthManager
     */
    static hasScopes(currentResourcePath, currentResourceMethod) {
        const userScopes = AuthManager.getUser().scopes;
        const validScope = APIClient.getScopeForResource(currentResourcePath, currentResourceMethod);
        return validScope.then((scopes) => {
            for (const scope of scopes) {
                if (userScopes.includes(scope)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Creates an instance of ScopeValidation.
     * @param {any} props @inheritDoc
     * @memberof ScopeValidation
     */
    constructor(props) {
        super(props);
        this.state = {};
    }

    /**
     * @inheritDoc
     * @memberof ScopeValidation
     */
    componentDidMount() {
        const { resourcePath: currentResourcePath, resourceMethod: currentResourceMethod } = this.props;
        const hasScope = ScopeValidation.hasScopes(currentResourcePath, currentResourceMethod);
        hasScope.then((haveScope) => {
            this.setState({ haveScope });
        });
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Return react component
     * @memberof ScopeValidation
     */
    render() {
        const { children } = this.props;
        const { haveScope } = this.state;
        if (haveScope) {
            return children || null;
        }
        return null;
    }
}

ScopeValidation.propTypes = {
    children: PropTypes.node.isRequired,
    resourcePath: PropTypes.string.isRequired,
    resourceMethod: PropTypes.string.isRequired,
};

export { ScopeValidation, resourceMethod, resourcePath };
