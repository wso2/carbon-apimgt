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
import { createBrowserHistory } from 'history';
import { Router } from 'react-router';
import isPlainObject from 'lodash.isplainobject';
import SettingsContext from 'AppComponents/Shared/SettingsContext';
import Settings from 'Settings';
import queryString from 'query-string';
import PropTypes from 'prop-types';

/**
 * This class wrapps the default router object of react router and adds custom history function
 * to track the history and uses that to add an interceptor which is called in every route.
 * @class BrowserRouter
 * @extends {React.Component}
 */
class BrowserRouter extends React.Component {
    /**
     * Creates an instance of BrowserRouter.
     * @param {*} props properties
     * @memberof BrowserRouter
     */
    constructor(props) {
        super(props);
        this.historyEnhancer = (originalHistory) => {
            return {
                ...originalHistory,
                push: (path, ...args) => originalHistory.push(this.pathInterceptor(path), ...args),
                replace: (path, ...args) => originalHistory.replace(this.pathInterceptor(path), ...args),
            };
        };
        this.history = this.historyEnhancer(createBrowserHistory(this.props));
    }

    /**
     * Interceptor that is called in every route call. This will get the tenant
     * domain from the context and append it to the query param list
     * @param {*} originalPath request path or object with path details
     * @memberof BrowserRouter
     * @returns {String} returns the updated path
     */
    pathInterceptor = (originalPath) => {
        const { app: { customUrl: { tenantDomain: customUrlEnabledDomain } } } = Settings;
        if (customUrlEnabledDomain !== 'null') {
            return originalPath;
        }
        const { tenantDomain } = this.context;
        let path = '';
        let queryStringsRaw = '';
        if (isPlainObject(originalPath)) {
            path = originalPath.pathname;
            queryStringsRaw = originalPath.search;
        } else {
            [path, queryStringsRaw] = originalPath.split('?');
        }
        const queryObject = queryString.parse(queryStringsRaw);
        if (!queryObject.tenant && tenantDomain) {
            queryObject.tenant = tenantDomain;
        }
        return `${path}?${queryString.stringify(queryObject)}`;
    };

    /**
     * @inheritdoc
     * @memberof BrowserRouter
     * @returns {Component}
     */
    render() {
        const { children } = this.props;
        return (
            <Router history={this.history}>
                {children}
            </Router>
        );
    }
}
BrowserRouter.contextType = SettingsContext;

BrowserRouter.propTypes = {
    children: PropTypes.node.isRequired,
};

export default BrowserRouter;
