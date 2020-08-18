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
import { withTheme } from '@material-ui/core/styles';
import Settings from 'Settings';
import Tenants from 'AppData/Tenants';
import queryString from 'query-string';
import PropTypes from 'prop-types';
import SettingsContext from 'AppComponents/Shared/SettingsContext';
import RedirectToLogin from 'AppComponents/Login/RedirectToLogin';
import API from './data/api';
import Base from './components/Base/index';
import AuthManager from './data/AuthManager';
import Loading from './components/Base/Loading/Loading';
import Utils from './data/Utils';
import User from './data/User';
import ConfigManager from './data/ConfigManager';
import AppRouts from './AppRouts';
import TenantListing from './TenantListing';
import CONSTS from './data/Constants';
import LoginDenied from './LoginDenied';

/**
 * Render protected application paths
 */
class ProtectedApp extends Component {
    /**
     *  constructor
     * @param {*} props props passed to constructor
     */
    constructor(props) {
        super(props);
        this.state = {
            userResolved: false,
            scopesFound: false,
            tenantResolved: false,
            tenantList: [],
            clientId: Utils.getCookieWithoutEnvironment(User.CONST.DEVPORTAL_CLIENT_ID),
            sessionStateCookie: Utils.getCookieWithoutEnvironment(User.CONST.DEVPORTAL_SESSION_STATE),
        };
        this.environments = [];
        this.checkSession = this.checkSession.bind(this);
        this.handleMessage = this.handleMessage.bind(this);
        /* TODO: need to fix the header to avoid conflicting with messages ~tmkb */
    }

    /**
     *  Check if data available ,if not get the user info from existing token information
     */
    componentDidMount() {
        window.addEventListener('message', this.handleMessage);
        const { location: { search } } = this.props;
        const { setTenantDomain, setSettings } = this.context;
        const { app: { customUrl: { tenantDomain: customUrlEnabledDomain } } } = Settings;
        let tenant = null;
        if (customUrlEnabledDomain !== 'null') {
            tenant = customUrlEnabledDomain;
        } else {
            tenant = queryString.parse(search).tenant;
        }
        const tenantApi = new Tenants();
        tenantApi.getTenantsByState().then((response) => {
            const { list } = response.body;
            if (list.length > 0) {
                // Check if tenant domain is present as a query param if not retrieve the tenant list,
                // only set the list in the state
                if (tenant) {
                    this.setState({ tenantResolved: true, tenantList: list }, setTenantDomain(tenant));
                } else {
                    this.setState({ tenantResolved: true, tenantList: list });
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
                this.checkSession();
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
                            this.checkSession();
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

    handleMessage(e) {
        if (e.data === 'changed') {
            window.location = Settings.app.context + '/services/configs?loginPrompt=false';
        }
    }

    /**
     * Invoke checksession oidc endpoint.
     */
    checkSession() {
        if (Settings.app.singleLogout && Settings.app.singleLogout.enabled) {
            setInterval(() => {
                const { clientId, sessionStateCookie } = this.state;
                const msg = clientId + ' ' + sessionStateCookie;
                document.getElementById('iframeOP').contentWindow.postMessage(msg, Settings.idp.origin);
            }, Settings.app.singleLogout.timeout);
        }
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
            userResolved, tenantList, notEnoughPermission, tenantResolved, clientId,
        } = this.state;
        const checkSessionURL = Settings.idp.checkSessionEndpoint + '?client_id='
            + clientId + '&redirect_uri=' + window.location.origin
            + Settings.app.context + '/services/auth/callback/login';
        const { tenantDomain, settings } = this.context;
        if (!userResolved) {
            return <Loading />;
        }
        const { scopesFound } = this.state;
        const isUserFound = AuthManager.getUser();
        let isAuthenticated = false;
        if (scopesFound && isUserFound) {
            isAuthenticated = true;
        }
        if (notEnoughPermission) {
            return <LoginDenied IsAnonymousModeEnabled={settings.IsAnonymousModeEnabled} />;
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

        if (!isAuthenticated && !settings.IsAnonymousModeEnabled && !sessionStorage.getItem(CONSTS.ISLOGINPERMITTED)) {
            return <RedirectToLogin />;
        }

        if (settings.IsAnonymousModeEnabled && sessionStorage.getItem(CONSTS.ISLOGINPERMITTED)) {
            sessionStorage.removeItem(CONSTS.ISLOGINPERMITTED);
        }
        // check for widget=true in the query params. If it's present we render without <Base> component.
        const pageUrl = new URL(window.location);
        const isWidget = pageUrl.searchParams.get('widget');
        if (isWidget) {
            return (
                <>
                    {clientId
                    && (
                        <iframe
                            title='iframeOP'
                            id='iframeOP'
                            src={checkSessionURL}
                            width='0px'
                            height='0px'
                        />
                    )}
                    <AppRouts isAuthenticated={isAuthenticated} isUserFound={isUserFound} />
                </>
            );
        }
        /**
         * Note: AuthManager.getUser() method is a passive check, which simply
         * check the user availability in browser storage,
         * Not actively check validity of access token from backend
         * @returns {Component}
         */
        return (
            <Base>
                {clientId
                    && (
                        <iframe
                            title='iframeOP'
                            id='iframeOP'
                            src={checkSessionURL}
                            width='0px'
                            height='0px'
                        />
                    )}
                <AppRouts isAuthenticated={isAuthenticated} isUserFound={isUserFound} />
            </Base>
        );
    }
}
ProtectedApp.contextType = SettingsContext;
ProtectedApp.propTypes = {
    location: PropTypes.shape({
        search: PropTypes.string.isRequired,
    }).isRequired,
};
export default withTheme(ProtectedApp);
