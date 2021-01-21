/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import { Route, Switch, Redirect } from 'react-router-dom';
import { ThemeProvider as MuiThemeProvider } from '@material-ui/core/styles';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import Hidden from '@material-ui/core/Hidden';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import User from 'AppData/User';
import Utils from 'AppData/Utils';
import Base from 'AppComponents/Base';
import AuthManager from 'AppData/AuthManager';
import Header from 'AppComponents/Base/Header';
import Avatar from 'AppComponents/Base/Header/Avatar';
import Themes from 'Themes';
import AppErrorBoundary from 'AppComponents/Shared/AppErrorBoundary';
import RedirectToLogin from 'AppComponents/Shared/RedirectToLogin';
import { IntlProvider, injectIntl } from 'react-intl';
import { AppContextProvider } from 'AppComponents/Shared/AppContext';
import Configurations from 'Config';
import Navigator from 'AppComponents/Base/Navigator';
import RouteMenuMapping from 'AppComponents/Base/RouteMenuMapping';
import Api from 'AppData/api';
import Progress from 'AppComponents/Shared/Progress';
import Dashboard from 'AppComponents/AdminPages/Dashboard/Dashboard';
import Alert from 'AppComponents/Shared/Alert';


const theme = createMuiTheme(Themes.light);
const { drawerWidth } = Themes.light.custom;
/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage;
const allRoutes = [];

/**
 * Render protected application paths, Implements container presenter pattern
 */
class Protected extends Component {
    /**
     * Creates an instance of Protected.
     * @param {any} props @inheritDoc
     * @memberof Protected
     */
    constructor(props) {
        super(props);
        this.state = {
            settings: null,
            clientId: Utils.getCookieWithoutEnvironment(User.CONST.ADMIN_CLIENT_ID),
            sessionStateCookie: Utils.getCookieWithoutEnvironment(User.CONST.ADMIN_SESSION_STATE),
            mobileOpen: false,
            isSuperTenant: false,
        };
        this.environments = [];
        this.checkSession = this.checkSession.bind(this);
        this.handleMessage = this.handleMessage.bind(this);
        const { intl } = props;
        const routeMenuMapping = RouteMenuMapping(intl);
        for (let i = 0; i < routeMenuMapping.length; i++) {
            const childRoutes = routeMenuMapping[i].children;
            if (childRoutes) {
                for (let j = 0; j < childRoutes.length; j++) {
                    allRoutes.push(childRoutes[j]);
                }
            } else {
                allRoutes.push(routeMenuMapping[i]);
            }
        }
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
            api.getTenantInformation(user.name)
                .then((result) => {
                    const { tenantDomain } = result.body;
                    if (tenantDomain === 'carbon.super') {
                        this.setState({ isSuperTenant: true });
                    } else {
                        this.setState({ isSuperTenant: false });
                    }
                })
                .catch((error) => {
                    Alert.error(error.response.body.description);
                    console.log(error);
                });
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
        if (Configurations.app.singleLogout && Configurations.app.singleLogout.enabled) {
            setInterval(() => {
                const { clientId, sessionStateCookie } = this.state;
                const msg = clientId + ' ' + sessionStateCookie;
                document.getElementById('iframeOP').contentWindow.postMessage(msg, Configurations.idp.origin);
            }, Configurations.app.singleLogout.timeout);
        }
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const { user = AuthManager.getUser(), messages, mobileOpen } = this.state;
        const header = (
            <Header
                avatar={<Avatar user={user} />}
                user={user}
                handleDrawerToggle={() => {
                    this.setState((oldState) => ({ mobileOpen: !oldState.mobileOpen }));
                }}
            />
        );
        const { clientId, settings, isSuperTenant } = this.state;
        const checkSessionURL = Configurations.idp.checkSessionEndpoint + '?client_id='
            + clientId + '&redirect_uri=' + Configurations.idp.origin
            + Configurations.app.context + '/services/auth/callback/login';
        if (!user) {
            return (
                <IntlProvider locale={language} messages={messages}>
                    <RedirectToLogin />
                </IntlProvider>
            );
        }
        const leftMenu = (
            settings && (
                <AppContextProvider value={{ settings, user, isSuperTenant }}>
                    <>
                        <Hidden smUp implementation='js'>
                            <Navigator
                                PaperProps={{ style: { width: drawerWidth } }}
                                variant='temporary'
                                open={mobileOpen}
                                onClose={() => {
                                    this.setState((oldState) => ({ mobileOpen: !oldState.mobileOpen }));
                                }}
                            />
                        </Hidden>
                        <Hidden xsDown implementation='css'>
                            <Navigator PaperProps={{ style: { width: drawerWidth } }} />
                        </Hidden>
                    </>
                </AppContextProvider>
            )
        );
        return (
            <MuiThemeProvider theme={theme}>
                <AppErrorBoundary>
                    {settings ? (
                        <AppContextProvider value={{ settings, user, isSuperTenant }}>
                            <Base header={header} leftMenu={leftMenu}>
                                <Route>
                                    <Switch>
                                        <Redirect exact from='/' to='/dashboard' />
                                        <Route
                                            path='/dashboard'
                                            component={Dashboard}
                                        />
                                        {allRoutes.map((r) => {
                                            return <Route path={r.path} component={r.component} />;
                                        })}
                                        <Route component={ResourceNotFound} />
                                    </Switch>
                                </Route>
                            </Base>
                        </AppContextProvider>
                    ) : (
                        <Progress message='Loading Settings ...' />
                    )}
                    <iframe
                        title='iframeOP'
                        id='iframeOP'
                        src={checkSessionURL}
                        width={0}
                        height={0}
                        style={{ position: 'absolute', bottom: 0 }}
                    />
                </AppErrorBoundary>
            </MuiThemeProvider>
        );
    }
}

Protected.propTypes = {
    user: PropTypes.shape({}).isRequired,
};

export default injectIntl(Protected);
