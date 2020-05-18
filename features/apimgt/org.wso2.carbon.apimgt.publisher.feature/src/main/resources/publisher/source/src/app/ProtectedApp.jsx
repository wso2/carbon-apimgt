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
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
// import MaterialDesignCustomTheme from 'AppComponents/Shared/CustomTheme';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Api from 'AppData/api';
import User from 'AppData/User';
import Utils from 'AppData/Utils';
import Base from 'AppComponents/Base';
import AuthManager from 'AppData/AuthManager';
import Header from 'AppComponents/Base/Header';
import Avatar from 'AppComponents/Base/Header/avatar/Avatar';
import Themes from 'Themes';
import AppErrorBoundary from 'AppComponents/Shared/AppErrorBoundary';
import RedirectToLogin from 'AppComponents/Shared/RedirectToLogin';
import { IntlProvider } from 'react-intl';
import { AppContextProvider } from 'AppComponents/Shared/AppContext';
import SettingsBase from 'AppComponents/Apis/Settings/SettingsBase';
import Progress from 'AppComponents/Shared/Progress';
import Configurations from 'Config';

const Apis = lazy(() => import('AppComponents/Apis/Apis' /* webpackChunkName: "DeferredAPIs" */));
const DeferredAPIs = () => (
    <Suspense fallback={<Progress message='Loading components ...' />}>
        <Apis />
    </Suspense>
);
const theme = createMuiTheme(Themes.light);

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
            clientId: Utils.getCookieWithoutEnvironment(User.CONST.PUBLISHER_CLIENT_ID),
            sessionStateCookie: Utils.getCookieWithoutEnvironment(User.CONST.PUBLISHER_SESSION_STATE),
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
        } else {
            // If no user data available , Get the user info from existing token information
            // This could happen when OAuth code authentication took place and could send
            // user information via redirection
            const userPromise = AuthManager.getUserFromToken();
            userPromise.then((loggedUser) => this.setState({ user: loggedUser }));
            settingPromise.then((settingsNew) => this.setState({ settings: settingsNew }));
        }
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
     * Invoke checksession oidc endpoint.
     */
    checkSession() {
        setInterval(() => {
            const { clientId, sessionStateCookie } = this.state;
            const msg = clientId + ' ' + sessionStateCookie;
            document.getElementById('iframeOP').contentWindow.postMessage(msg, Configurations.idp.origin);
        }, 2000);
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const { user = AuthManager.getUser(), messages } = this.state;
        const header = <Header avatar={<Avatar user={user} />} user={user} />;
        const { settings, clientId } = this.state;
        const checkSessionURL = Configurations.idp.checkSessionEndpoint + '?client_id='
        + clientId + '&redirect_uri=https://' + window.location.host
        + Configurations.app.context + '/services/auth/callback/login';
        if (!user) {
            return (
                <IntlProvider locale={language} messages={messages}>
                    <RedirectToLogin />
                </IntlProvider>
            );
        }
        return (
            <MuiThemeProvider theme={theme}>
                <AppErrorBoundary>
                    <Base header={header}>
                        <iframe
                            title='iframeOP'
                            id='iframeOP'
                            src={checkSessionURL}
                            width='0px'
                            height='0px'
                        />
                        {settings ? (
                            <AppContextProvider value={{ settings, user }}>
                                <Switch>
                                    <Redirect exact from='/' to='/apis' />
                                    <Route path='/apis' component={DeferredAPIs} />
                                    <Route path='/api-products' component={DeferredAPIs} />
                                    <Route path='/settings' component={SettingsBase} />
                                    <Route component={ResourceNotFound} />
                                </Switch>
                            </AppContextProvider>
                        ) : (
                            <Progress message='Loading Settings ...' />
                        )}
                    </Base>
                </AppErrorBoundary>
            </MuiThemeProvider>
        );
    }
}

Protected.propTypes = {
    user: PropTypes.shape({}).isRequired,
};
