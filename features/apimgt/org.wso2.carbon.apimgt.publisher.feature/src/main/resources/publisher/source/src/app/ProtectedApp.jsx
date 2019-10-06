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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Redirect, Route, Switch } from 'react-router-dom';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
// import MaterialDesignCustomTheme from 'AppComponents/Shared/CustomTheme';
import { PageNotFound } from 'AppComponents/Base/Errors';
import Apis from 'AppComponents/Apis/Apis';
import Api from 'AppData/api';
import Base from 'AppComponents/Base';
import AuthManager from 'AppData/AuthManager';
import Header from 'AppComponents/Base/Header';
import Avatar from 'AppComponents/Base/Header/avatar/Avatar';
import Configurations from 'Config';
import AppErrorBoundary from 'AppComponents/Shared/AppErrorBoundary';
import RedirectToLogin from 'AppComponents/Shared/RedirectToLogin';
import { IntlProvider } from 'react-intl';
import { AppContextProvider } from 'AppComponents/Shared/AppContext';
import SettingsBase from 'AppComponents/Apis/Settings/SettingsBase';

const theme = createMuiTheme(Configurations.themes.light);

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
        };
        this.environments = [];
    }

    /**
     * @inheritDoc
     * @memberof Protected
     */
    componentDidMount() {
        const user = AuthManager.getUser();
        const api = new Api();
        const settingPromise = api.getSettings();
        if (user) {
            this.setState({ user });
            settingPromise.then(settingsNew => this.setState({ settings: settingsNew }));
        } else {
            // If no user data available , Get the user info from existing token information
            // This could happen when OAuth code authentication took place and could send
            // user information via redirection
            const userPromise = AuthManager.getUserFromToken();
            userPromise.then(loggedUser => this.setState({ user: loggedUser }));
            settingPromise.then(settingsNew => this.setState({ settings: settingsNew }));
        }
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const user = this.state.user || AuthManager.getUser();
        const header = <Header avatar={<Avatar user={user} />} user={user} />;
        const { settings } = this.state;

        if (!user) {
            return (
                <IntlProvider locale={language} messages={this.state.messages}>
                    <RedirectToLogin />
                </IntlProvider>
            );
        }
        return (
            settings && (
                <AppContextProvider value={{ settings, user }}>
                    <MuiThemeProvider theme={theme}>
                        <AppErrorBoundary>
                            <Base header={header}>
                                <Switch>
                                    <Redirect exact from='/' to='/apis' />
                                    <Route path='/apis' component={Apis} />
                                    <Route path='/api-products' component={Apis} />
                                    <Route path='/settings' component={SettingsBase} />
                                    <Route component={PageNotFound} />
                                </Switch>
                            </Base>
                        </AppErrorBoundary>
                    </MuiThemeProvider>
                </AppContextProvider>
            )
        );
    }
}

Protected.propTypes = {
    user: PropTypes.shape({}).isRequired,
};
