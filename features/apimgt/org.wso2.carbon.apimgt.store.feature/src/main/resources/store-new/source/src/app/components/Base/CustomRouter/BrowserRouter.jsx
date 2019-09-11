import React from 'react';
import { createBrowserHistory } from 'history';
import { Router } from 'react-router';
import isPlainObject from 'lodash/isplainobject';
import Settings from 'AppComponents/Shared/SettingsContext';
import queryString from 'query-string';
import PropTypes from 'prop-types';

/**
 * This class wrapps the default router object of react router and adds custom history function
 * to track the history and uses that to add an interceptor which is called in every route.
 * @class BrowserRouter
 * @extends {React.Component}
 */
class BrowserRouter extends React.Component {
    static contextType = Settings

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
     * @returns {String}
     */
    pathInterceptor = (originalPath) => {
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
BrowserRouter.propTypes = {
    children: PropTypes.node.isRequired,
};

export default BrowserRouter;
