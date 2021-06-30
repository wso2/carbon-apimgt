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

import React, { Suspense, lazy } from 'react';
import {
    BrowserRouter as Router, Redirect, Route, Switch,
} from 'react-router-dom';
import AuthManager from 'AppData/AuthManager';
import CONSTS from 'AppData/Constants';
import qs from 'qs';
import Utils from 'AppData/Utils';
import Logout from 'AppComponents/Logout';
import Progress from 'AppComponents/Shared/Progress';
import AdminRootErrorBoundary from 'AppComponents/Shared/AdminRootErrorBoundary';
import Configurations from 'Config';
import { IntlProvider } from 'react-intl';
import RedirectToLogin from 'AppComponents/Shared/RedirectToLogin';
// Localization
import UnexpectedError from 'AppComponents/Base/Errors/UnexpectedError';
import LoginDenied from './app/LoginDenied';


const ProtectedApp = lazy(() => import('./app/ProtectedApp' /* webpackChunkName: "ProtectedApps" */));

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
 * Define base routes for the application
 * @returns {React.Component} base routes for the application
 */
class Admin extends React.Component {
    /**
     *Creates an instance of Admin.
     * @param {*} props
     * @memberof Admin
     */
    constructor(props) {
        super(props);
        const { search } = window.location;
        const queryString = search.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const queryParams = qs.parse(queryString);
        const { environment = Utils.getCurrentEnvironment().label } = queryParams;
        this.state = {
            userResolved: false,
            user: AuthManager.getUser(environment),
            messages: {},
        };
        this.updateUser = this.updateUser.bind(this);
        this.loadLocale = this.loadLocale.bind(this);
    }

    /**
     * Initialize i18n.
     */
    componentDidMount() {
        const locale = languageWithoutRegionCode || language;
        this.loadLocale(locale);
        const user = AuthManager.getUser();
        if (user) {
            this.setState({ user, userResolved: true });
        } else {
            // If no user data available , Get the user info from existing token information
            // This could happen when OAuth code authentication took place and could send
            // user information via redirection
            const userPromise = AuthManager.getUserFromToken();
            userPromise
                .then((loggedUser) => {
                    if (loggedUser != null) {
                        this.setState({ user: loggedUser, userResolved: true });
                    } else {
                        console.log('User returned with null, redirect to login page');
                        this.setState({ userResolved: true });
                    }
                })
                .catch((error) => {
                    if (error && error.message === CONSTS.errorCodes.INSUFFICIENT_PREVILEGES) {
                        this.setState({ userResolved: true, notEnoughPermission: true });
                    } else if (error && error.message === CONSTS.errorCodes.UNEXPECTED_SERVER_ERROR) {
                        this.setState({ userResolved: true, unexpectedServerError: true });
                    } else {
                        console.log('Error: ' + error + ',redirecting to login page');
                        this.setState({ userResolved: true });
                    }
                });
        }
    }

    /**
     *
     *
     * @param {User} user
     * @memberof Admin
     */
    updateUser(user) {
        this.setState({ user });
    }

    /**
     * Load locale file.
     *
     * @param {string} locale Locale name
     */
    loadLocale(locale) {
        // Skip loading the locale file if the language code is english,
        // Because we have used english defaultMessage in the FormattedText component
        // and en.json is generated from those default messages, Hence no point of fetching it
        fetch(`${Configurations.app.context}/site/public/locales/${locale}.json`)
            .then((resp) => resp.json())
            .then((messages) => this.setState({ messages }));
    }

    /**
     *
     *
     * @returns {React.Component} Render complete app component
     * @memberof Admin
     */
    render() {
        const {
            user, userResolved, messages, notEnoughPermission, unexpectedServerError,
        } = this.state;
        const locale = languageWithoutRegionCode || language;
        if (!userResolved) {
            return <Progress message='Resolving user ...' />;
        }
        return (
            <IntlProvider locale={locale} messages={messages}>
                <AdminRootErrorBoundary appName='Admin Application'>
                    <Router basename={Configurations.app.context}>
                        <Switch>
                            <Redirect exact from='/login' to='/' />
                            <Route path='/logout' component={Logout} />
                            <Route
                                render={() => {
                                    if (notEnoughPermission) {
                                        return <LoginDenied />;
                                    } else if (unexpectedServerError) {
                                        return <UnexpectedError />;
                                    } else if (!user) {
                                        return <RedirectToLogin />;
                                    }
                                    return (
                                        <Suspense fallback={<Progress message='Loading app ...' />}>
                                            <ProtectedApp user={user} />
                                        </Suspense>
                                    );
                                }}
                            />
                        </Switch>
                    </Router>
                </AdminRootErrorBoundary>
            </IntlProvider>
        );
    }
}

export default Admin;
