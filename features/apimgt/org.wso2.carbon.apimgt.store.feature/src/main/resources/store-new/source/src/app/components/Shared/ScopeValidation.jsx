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

import React from 'react';
import PropTypes from 'prop-types';

import AuthManager from '../../data/AuthManager';

const resourcePaths = {
    APIS: '/apis',
    SINGLE_API: '/apis/{apiId}',
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
    APPLICATIONS: '/applications',
    SINGLE_APPLICATION: '/applications/{applicationId}',
    APPLICATION_GENERATE_KEYS: '/applications/{applicationId}/generate-keys',
    APPLICATION_GENERATE_KEY_TYPE: '/applications/{applicationId}/keys/{keyType}',
    EXPORT_APIS: '/export/apis',
    IMPORT_APIS: '/import/apis',
    SUBSCRIPTION: '/subscriptions',
    SINGLE_SUBSCRIPTION: '/subscriptions/{subscriptionId}',
    SUBSCRIPTIONS: '/subscriptions',
    BLOCK_SUBSCRIPTION: '/subscriptions/block-subscription:',
    UNBLOCK_SUBSCRIPTION: '/subscriptions/unblock-subscription',
    POLICIES: "'/policies/{tierLevel}'",
    POLICY: "'/policies/{tierLevel}/{tierName}'",
    ENDPOINTS: '/endpoints',
    ENDPOINT: '/endpoints/{endpointId}',
    LABLES: '/labels',
    WORKFLOW: '/workflows/{workflowReferenceId}',
    SERVICE_DISCOVERY: '/external-resources/services',
};

const resourceMethods = {
    POST: 'post',
    PUT: 'put',
    GET: 'get',
    DELETE: 'delete',
};

/**
 * Show element iff user has proper scope for the view/action
 * @class ScopeValidation
 * @extends {React.Component}
 */
class ScopeValidation extends React.Component {
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
        const { resourcePath, resourceMethod } = this.props;
        const hasScope = AuthManager.hasScopes(resourcePath, resourceMethod);
        if (hasScope) {
            hasScope.then((haveScope) => {
                this.setState({ haveScope });
            });
        }
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Return react component
     * @memberof ScopeValidation
     */
    render() {
        const { children } = this.props;
        if (this.state.haveScope) {
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

export {
    ScopeValidation,
    resourceMethods,
    resourcePaths,
};
