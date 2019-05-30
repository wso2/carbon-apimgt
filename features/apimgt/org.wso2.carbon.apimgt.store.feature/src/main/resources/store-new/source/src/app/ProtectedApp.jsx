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
import {
    BrowserRouter as Router,
    Redirect,
    Route,
    Switch
} from 'react-router-dom';
import {
    Apis,
    ApplicationCreate,
    Applications,
    Base,
    Login,
    Logout
} from './components';
import { PageNotFound } from './components/Base/Errors';
import { ScopeNotFound } from './components/Base/Errors';
import AuthManager from './data/AuthManager';
import qs from 'qs';
import ApplicationEdit from './components/Applications/Edit/ApplicationEdit';

// import 'typeface-roboto'
import Utils from './data/Utils';
import ConfigManager from './data/ConfigManager';
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles';
import AnonymousView from './components/AnonymousView/AnonymousView';
import { addLocaleData, defineMessages, IntlProvider } from 'react-intl';
import Configurations from 'Config';

const themes = [];

themes.push(createMuiTheme(Configurations.themes.light));
themes.push(createMuiTheme(Configurations.themes.dark));

/**
 * Language.
 * @type {string}
 */
const language =
    (navigator.languages && navigator.languages[0]) ||
    navigator.language ||
    navigator.userLanguage;

/**
 * Language without region code.
 */
const languageWithoutRegionCode = language.toLowerCase().split(/[_-]+/)[0];

// import './materialize.css'

/**
 * Render protected application paths
 */
export default class ProtectedApp extends Component {
    constructor(props) {
        super(props);
        this.state = {
            showLeftMenu: false,
            themeIndex: 0,
            messages: {},
            userResolved: false,
            user: AuthManager.getUser(),
            scopesFound: false
        };
        this.environments = [];
        this.loadLocale = this.loadLocale.bind(this);
        /* TODO: need to fix the header to avoid conflicting with messages ~tmkb*/
    }

    /**
     * Load locale file.
     *
     * @param {string} locale Locale name
     * @returns {Promise} Promise
     */
    loadLocale(locale = 'en') {
        fetch(`${Utils.CONST.CONTEXT_PATH}/site/public/locales/${locale}.json`)
            .then(resp => resp.json())
            .then(data => {
                // eslint-disable-next-line global-require, import/no-dynamic-require
                addLocaleData(require(`react-intl/locale-data/${locale}`));
                this.setState({ messages: defineMessages(data) });
            });
    }

    componentDidMount() {
        ConfigManager.getConfigs()
            .environments.then(response => {
                this.environments = response.data.environments;
                //this.handleEnvironmentQueryParam(); todo: do we really need to handle environment query params here ?
            })
            .catch(error => {
                console.error(
                    'Error while receiving environment configurations : ',
                    error
                );
            });
        const user = AuthManager.getUser();
        if (user) {
            const hasViewScope = user.scopes.includes('apim:subscribe');
            if (hasViewScope) {
                this.setState({ user, userResolved: true, scopesFound: true });
            } else {
                console.log(
                    'No relevant scopes found, redirecting to Anonymous View'
                );
                this.setState({ userResolved: true });
            }
        } else {
            // If no user data available , Get the user info from existing token information
            // This could happen when OAuth code authentication took place and could send
            // user information via redirection
            const userPromise = AuthManager.getUserFromToken();
            userPromise
                .then(loggedUser => {
                    if (loggedUser != null) {
                        const hasViewScope = loggedUser.scopes.includes(
                            'apim:subscribe'
                        );
                        if (hasViewScope) {
                            this.setState({
                                user: loggedUser,
                                userResolved: true,
                                scopesFound: true
                            });
                        } else {
                            console.log(
                                'No relevant scopes found, redirecting to Anonymous View'
                            );
                            this.setState({ userResolved: true });
                        }
                    } else {
                        console.log(
                            'User returned with null, redirect to Anonymous View'
                        );
                        this.setState({ userResolved: true });
                    }
                })
                .catch(error => {
                    console.log(
                        'Error: ' + error + ',redirecting to Anonymous View'
                    );
                    this.setState({ userResolved: true });
                });
        }
    }

    /**
     * Change the environment with "environment" query parameter
     * @return {String} environment name in the query param
     */
    handleEnvironmentQueryParam() {
        let queryString = this.props.location.search;
        queryString = queryString.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        let queryParams = qs.parse(queryString);
        const environmentName = queryParams.environment;

        if (!environmentName || Utils.getEnvironment() === environmentName) {
            // no environment query param or the same environment
            return environmentName;
        }

        let environmentId = Utils.getEnvironmentID(
            this.environments,
            environmentName
        );
        if (environmentId === -1) {
            return environmentName;
        }

        let environment = this.environments[environmentId];
        Utils.setEnvironment(environment);
        return environmentName;
    }
    /**
     * Change the theme index incrementally
     */
    componentWillMount() {
        let storedThemeIndex = localStorage.getItem('themeIndex');
        if (storedThemeIndex) {
            this.setState({ themeIndex: parseInt(storedThemeIndex) });
        }
        const locale = languageWithoutRegionCode || language || 'en';
        this.loadLocale(locale);
    }
    setTheme() {
        this.setState({ theme: themes[this.state.themeIndex % 3] });
        this.state.themeIndex++;
        localStorage.setItem('themeIndex', this.state.themeIndex);
    }

    render() {
        const environmentName = this.handleEnvironmentQueryParam();
        var isScopesFound = this.state.scopesFound;
        var isUserFound = AuthManager.getUser();
        var isAuthenticated = false;
        if (isScopesFound && isUserFound) {
            isAuthenticated = true;
        }
    
        // Note: AuthManager.getUser() method is a passive check, which simply check the user availability in browser storage,
        // Not actively check validity of access token from backend
        return (
            <IntlProvider locale={language} messages={this.state.messages}>
                <MuiThemeProvider theme={themes[this.state.themeIndex % 2]}>
                    <Base setTheme={() => this.setTheme()}>
                        <Switch>
                            <Redirect exact from="/" to="/apis" />
                            <Route path={'/apis'} component={Apis} />
                            {isAuthenticated ? (
                                <React.Fragment>
                                    <Route
                                        path={'/applications'}
                                        component={Applications}
                                    />
                                    <Route
                                        path={'/application/create'}
                                        component={ApplicationCreate}
                                    />
                                    <Route
                                        path={'/application/edit/:application_id'}
                                        component={ApplicationEdit}
                                    />
                                </React.Fragment>
                            ) : [
                                isUserFound ? (
                                    <React.Fragment> 
                                        <Route
                                            path={'/applications'}
                                            component={ScopeNotFound}
                                        />
                                        <Route
                                            path={'/application/create'}
                                            component={ScopeNotFound}
                                        />
                                        <Route
                                            path={'/application/edit/:application_id'}
                                            component={ScopeNotFound}
                                        />
                                    </React.Fragment>    
                                ) : (
                                    <React.Fragment>
                                        <Route
                                            path={'/applications'}
                                            component={PageNotFound}
                                        />
                                        <Route
                                            path={'/application/create'}
                                            component={PageNotFound}
                                        />
                                        <Route
                                            path={'/application/edit/:application_id'}
                                            component={PageNotFound}
                                        />
                                        </React.Fragment> 
                                )
                            ]}
                            <Route component={PageNotFound} />
                        </Switch>
                    </Base>
                </MuiThemeProvider>
            </IntlProvider>
        );

        let params = qs.stringify({ referrer: this.props.location.pathname });
        return <Redirect to={{ pathname: '/login', search: params }} />;
    }
}