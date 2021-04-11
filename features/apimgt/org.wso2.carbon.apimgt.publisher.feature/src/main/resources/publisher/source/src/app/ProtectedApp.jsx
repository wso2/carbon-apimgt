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

import React, { Component, Suspense, lazy } from 'react';
import PropTypes from 'prop-types';
import { Redirect, Route, Switch } from 'react-router-dom';
import { ThemeProvider } from '@material-ui/core/styles';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
// import MaterialDesignCustomTheme from 'AppComponents/Shared/CustomTheme';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Api from 'AppData/api';
import Base from 'AppComponents/Base';
import AuthManager from 'AppData/AuthManager';
import userThemes from 'userCustomThemes';
import defaultTheme from 'AppData/defaultTheme';
import AppErrorBoundary from 'AppComponents/Shared/AppErrorBoundary';
import RedirectToLogin from 'AppComponents/Shared/RedirectToLogin';
import { IntlProvider } from 'react-intl';
import { AppContextProvider } from 'AppComponents/Shared/AppContext';
import ServiceCatalogRouting from 'AppComponents/ServiceCatalog/ServiceCatalogRouting';
import Progress from 'AppComponents/Shared/Progress';
import Configurations from 'Config';
import Scopes from 'AppComponents/Scopes/Scopes';
import merge from 'lodash/merge';

const Apis = lazy(() => import('AppComponents/Apis/Apis' /* webpackChunkName: "DeferredAPIs" */));
const DeferredAPIs = () => (
    <Suspense fallback={<Progress per={30} message='Loading components ...' />}>
        <Apis />
    </Suspense>
);
/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage;

/**
 * Render protected application paths, Implements container presenter pattern
 */
export default class Protected extends Component {
    /**
     * Creates an instance of Protected.
     * @param {any} props @inheritDoc
     * @memberof Protected
     */
    constructor(props) {
        super(props);
        this.state = {
            settings: null,
            theme: null,
        };
        this.environments = [];
        this.checkSession = this.checkSession.bind(this);
        this.handleMessage = this.handleMessage.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Protected
     */
    componentDidMount() {
        const user = AuthManager.getUser();
        const api = new Api();
        const settingPromise = api.getSettings();
        window.addEventListener('message', this.handleMessage);
        if (user) {
            this.setState({ user });
            settingPromise.then((settingsNew) => this.setState({ settings: settingsNew }));
            this.checkSession();
            if (user.name && user.name.indexOf('@') !== -1) {
                const tenant = user.name.split('@')[user.name.split('@').length - 1];
                this.setTenantTheme(tenant);
            } else {
                this.setState({ theme: userThemes.light });
            }
        } else {
            // If no user data available , Get the user info from existing token information
            // This could happen when OAuth code authentication took place and could send
            // user information via redirection
            const userPromise = AuthManager.getUserFromToken();
            userPromise.then((loggedUser) => {
                if (loggedUser.name && loggedUser.name.indexOf('@') !== -1) {
                    const tenant = loggedUser.name.split('@')[loggedUser.name.split('@').length - 1];
                    this.setTenantTheme(tenant);
                } else {
                    this.setState({ theme: userThemes.light });
                }
                this.setState({ user: loggedUser });
            });
            settingPromise.then((settingsNew) => this.setState({ settings: settingsNew }));
        }
    }

    /**
     * Load Theme file.
     *
     * @param {string} tenant tenant name
     */
    setTenantTheme(tenant) {
        if (tenant && tenant !== '' && tenant !== 'carbon.super') {
            fetch(`${Configurations.app.context}/site/public/tenant_themes/${tenant}/apim-publisher/defaultTheme.json`)
                .then((response) => {
                    if (!response.ok) {
                        throw new Error('HTTP error ' + response.status);
                    }
                    return response.json();
                })
                .then((data) => {
                    if (data && data.light) {
                        this.setState({ theme: data.light });
                    } else {
                        console.log('Error loading teant theme. Loading the default theme.');
                        this.setState({ theme: userThemes.light });
                    }
                })
                .catch(() => {
                    console.log('Error loading teant theme. Loading the default theme.');
                    this.setState({ theme: userThemes.light });
                });
        } else {
            this.setState({ theme: userThemes.light });
        }
    }

    /**
     * Generate page title from theme config.
     * @param {object} theme object.
     * @returns {JSX} link dom tag.
     */
    getTitle(localTheme) {
        const {
            custom: {
                title: {
                    prefix, suffix,
                },
            },
        } = localTheme;
        return (prefix + suffix);
    }

    /**
     * Handle iframe message
     * @param {event} e Event
     */
    handleMessage(e) {
        if (e.data === 'changed') {
            window.location = Configurations.app.context + '/services/auth/login?not-Login';
        }
    }

    /**
     * Invoke check session OIDC endpoint.
     */
    checkSession() {
        if (Configurations.app.singleLogout && Configurations.app.singleLogout.enabled) {
            setInterval(() => {
                // Check session will only trigger if user is available
                const { clientId, sessionState } = AuthManager.getUser().getAppInfo();
                const msg = clientId + ' ' + sessionState;
                document.getElementById('iframeOP').contentWindow.postMessage(msg, Configurations.idp.origin);
            }, Configurations.app.singleLogout.timeout);
        }
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const { user = AuthManager.getUser(), messages } = this.state;
        const { settings } = this.state;
        const { theme } = this.state;
        if (!user) {
            return (
                <IntlProvider locale={language} messages={messages}>
                    <RedirectToLogin />
                </IntlProvider>
            );
        }
        if (!theme) {
            return (<Progress />);
        }
        return (
            <ThemeProvider theme={createMuiTheme(defaultTheme)}>
                <ThemeProvider theme={(currentTheme) => createMuiTheme(
                    merge(currentTheme, (typeof theme === 'function' ? theme(currentTheme) : theme)),
                )}
                >
                    <AppErrorBoundary>
                        <Base user={user}>
                            {settings ? (
                                <AppContextProvider value={{
                                    settings, user,
                                }}
                                >
                                    <Switch>
                                        <Redirect exact from='/' to='/apis' />
                                        <Route path='/apis' component={DeferredAPIs} />
                                        <Route path='/api-products' component={DeferredAPIs} />
                                        <Route path='/scopes' component={Scopes} />
                                        <Route path='/service-catalog' component={ServiceCatalogRouting} />
                                        <Route component={ResourceNotFound} />
                                    </Switch>
                                </AppContextProvider>
                            ) : (
                                <Progress per={20} message='Loading Settings ...' />
                            )}
                        </Base>
                    </AppErrorBoundary>
                </ThemeProvider>
            </ThemeProvider>
        );
    }
}

Protected.propTypes = {
    user: PropTypes.shape({}).isRequired,
};
