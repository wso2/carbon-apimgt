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

import React, { Component } from 'react';
import qs from 'qs';
import { addLocaleData, IntlProvider } from 'react-intl';
import Settings from 'Settings';
import Tenants from 'AppData/Tenants';
import SettingsContext from 'AppComponents/Shared/SettingsContext';
import queryString from 'query-string';
import PropTypes from 'prop-types';
import API from './data/api';
import Base from './components/Base/index';
import AuthManager from './data/AuthManager';
import Loading from './components/Base/Loading/Loading';
import Utils from './data/Utils';
import ConfigManager from './data/ConfigManager';
import AppRouts from './AppRouts';
import TenantListing from './TenantListing';
import CONSTS from './data/Constants';
import LoginDenied from './LoginDenied';

/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage;

/**
 * Language without region code.
 */
const languageWithoutRegionCode = language.toLowerCase().split(/[_-]+/)[0];

/**
 * Render protected application paths
 */
export default class ProtectedApp extends Component {
    static contextType = SettingsContext;

    /**
     *  constructor
     * @param {*} props props passed to constructor
     */
    constructor(props) {
        super(props);
        this.state = {
            messages: {},
            userResolved: false,
            scopesFound: false,
            tenantResolved: false,
            tenantList: [],
        };
        this.environments = [];
        this.loadLocale = this.loadLocale.bind(this);
        /* TODO: need to fix the header to avoid conflicting with messages ~tmkb */
    }

    /**
     *  Check if data available ,if not get the user info from existing token information
     */
    componentDidMount() {
        const locale = languageWithoutRegionCode || language;
        this.loadLocale(locale);

        const { location: { search } } = this.props;
        const { setTenantDomain, setSettings } = this.context;
        const { tenant } = queryString.parse(search);
        const tenantApi = new Tenants();
        tenantApi.getTenantsByState().then((response) => {
            const { list } = response.body;
            if (list.length > 0) {
                // Check if tenant domain is present as a query param if not retrieve the tenant list,
                // only set the list in the state
                if (tenant) {
                    this.setState({ tenantResolved: true, tenantList: list }, setTenantDomain(tenant));
                } else {
                    this.setState({ tenantResolved: true, tenantList: response.body.list });
                }
            } else {
                this.setState({ tenantResolved: true });
            }
        }).catch((error) => {
            console.error('error when getting tenants ' + error);
        });

        ConfigManager.getConfigs()
            .environments.then((response) => {
                this.environments = response.data.environments;
                // this.handleEnvironmentQueryParam(); todo: do we really need to handle environment query params here ?
            })
            .catch((error) => {
                console.error(
                    'Error while receiving environment configurations : ',
                    error,
                );
            });
        const user = AuthManager.getUser(); // Passive user check
        if (user) { // If token exisit in cookies and user info available in local storage
            const hasViewScope = user.scopes.includes('apim:subscribe');
            if (hasViewScope) {
                this.setState({ userResolved: true, scopesFound: true });
            } else {
                console.log('No relevant scopes found, redirecting to Anonymous View');
                this.setState({ userResolved: true, notEnoughPermission: true });
            }
        } else {
            // If no user data available , Get the user info from existing token information
            // This could happen when OAuth code authentication took place and could send
            // user information via redirection
            const userPromise = AuthManager.getUserFromToken(); // Active user check
            userPromise
                .then((loggedUser) => {
                    if (loggedUser != null) {
                        const hasViewScope = loggedUser.scopes.includes('apim:subscribe');
                        if (hasViewScope) {
                            this.setState({
                                userResolved: true,
                                scopesFound: true,
                            });
                            // Update the settings context with settings retrived from authenticated user
                            const api = new API();
                            const promisedSettings = api.getSettings();
                            promisedSettings
                                .then((response) => {
                                    setSettings(response.body);
                                }).catch((error) => {
                                    console.error(
                                        'Error while receiving settings : ',
                                        error,
                                    );
                                });
                        } else {
                            console.log('No relevant scopes found, redirecting to Anonymous View');
                            this.setState({ userResolved: true });
                        }
                    } else {
                        console.log('User returned with null, redirect to Anonymous View');
                        this.setState({ userResolved: true });
                    }
                })
                .catch((error) => {
                    if (error && error.message === CONSTS.errorCodes.INSUFFICIENT_PREVILEGES) {
                        this.setState({ userResolved: true, notEnoughPermission: true });
                    } else {
                        console.log('Error: ' + error + ',redirecting to Anonymous View');
                        this.setState({ userResolved: true });
                    }
                });
        }
    }

    /**
     * Load locale file.
     *
     * @param {string} locale Locale name
     */
    loadLocale(locale = 'en') {
        fetch(`${Settings.app.context}/site/public/locales/${locale}.json`)
            .then((resp) => resp.json())
            .then((messages) => {
                // eslint-disable-next-line global-require, import/no-dynamic-require
                addLocaleData(require(`react-intl/locale-data/${locale}`));
                this.setState({ messages });
            });
    }

    /**
     * Change the environment with "environment" query parameter
     * @return {String} environment name in the query param
     */
    handleEnvironmentQueryParam() {
        const { location } = this.props;
        const { search } = { ...location };
        const query = search.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const queryParams = qs.parse(query);
        const environmentName = queryParams.environment;

        if (!environmentName || Utils.getEnvironment() === environmentName) {
            // no environment query param or the same environment
            return environmentName;
        }

        const environmentId = Utils.getEnvironmentID(
            this.environments,
            environmentName,
        );
        if (environmentId === -1) {
            return environmentName;
        }

        const environment = this.environments[environmentId];
        Utils.setEnvironment(environment);
        return environmentName;
    }

    /**
     *  renders the compopnent
     * @returns {Component}
     */
    render() {
        const {
            userResolved, tenantList, notEnoughPermission, tenantResolved,
        } = this.state;
        const { tenantDomain } = this.context;
        if (!userResolved) {
            return <Loading />;
        }
        const { scopesFound, messages } = this.state;
        const isUserFound = AuthManager.getUser();
        let isAuthenticated = false;
        if (scopesFound && isUserFound) {
            isAuthenticated = true;
        }
        if (notEnoughPermission) {
            return <LoginDenied />;
        }

        // Waiting till the tenant list is retrieved
        if (!tenantResolved) {
            return <Loading />;
        }
        // user is redirected to tenant listing page if there are any tenants present and
        // if the user is not authenticated and if there is no tenant domain present in the context
        // tenantDomain contains INVALID when the tenant does not exist
        if (tenantList.length > 0 && (tenantDomain === 'INVALID' || (!isAuthenticated && tenantDomain === null))) {
            return <TenantListing tenantList={tenantList} />;
        }
        /**
         * Note: AuthManager.getUser() method is a passive check, which simply
         * check the user availability in browser storage,
         * Not actively check validity of access token from backend
         * @returns {Component}
         */
        return (
            <IntlProvider locale={language} messages={messages}>
                <Base>
                    <AppRouts isAuthenticated={isAuthenticated} isUserFound={isUserFound} />
                </Base>
            </IntlProvider>
        );
    }
}
ProtectedApp.propTypes = {
    location: PropTypes.shape({
        search: PropTypes.string.isRequired,
    }).isRequired,
};
