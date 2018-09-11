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
import PropTypes from 'prop-types';
import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom';
import qs from 'qs';
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles';
import { addLocaleData, defineMessages, IntlProvider } from 'react-intl';

import Log from 'log4javascript';
import Utils from './app/data/Utils';
import ConfigManager from './app/data/ConfigManager';
// import MaterialDesignCustomTheme from './app/components/Shared/CustomTheme';
import { PageNotFound } from './app/components/Base/Errors';
import AuthManager from './app/data/AuthManager';
import Apis from './app/components/Apis/Apis';
import Endpoints from './app/components/Endpoints';
import Base from './app/components/Base';
import Login from './app/components/Login/Login';
import Logout from './app/components/Logout';
import AppErrorBoundary from './app/components/Shared/AppErrorBoundary';
import Header from './app/components/Base/Header';
import Avatar from './app/components/Base/Header/avatar/Avatar';

const themes = [];
const darkTheme = createMuiTheme({
    palette: {
        type: 'dark', // Switching the dark mode on is a single property value change.
        background: {
            active: 'rgba(27, 94, 32, 1)',
            navBar: '#29434e',
            container: '#001114',
            paper: '#29434e',
        },
        primary: {
            light: '#315564',
            main: '#002c3a',
            dark: '#000115',
        },
    },
    overrides: {
        MuiButton: {
            textPrimary: {
                color: '#00ffe0',
                '&:hover': {
                    backgroundColor: '##00c1ff36',
                },
            },
        },
    },
});
const lightTheme = createMuiTheme({
    palette: {
        type: 'light', // Switching the dark mode on is a single property value change.
        background: {
            active: 'rgba(165, 214, 167, 1)',
            navBar: '#fafafa',
            container: '#fefefe',
            contentFrame: 'rgba(227, 242, 253, 1)',
        },
        text: {
            brand: 'rgba(255,255,255,1)',
        },
    },
});
themes.push(darkTheme);
themes.push(lightTheme);
// themes.push(createMuiTheme(MaterialDesignCustomTheme));

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
            themeIndex: 1,
            messages: {},
        };
        this.environments = [];
        this.toggleTheme = this.toggleTheme.bind(this);
        this.loadLocale = this.loadLocale.bind(this);
    }

    /**
     * Initialize i18n.
     */
    componentWillMount() {
        const locale = languageWithoutRegionCode || language || 'en';
        this.loadLocale(locale);
    }

    /**
     * @inheritDoc
     * @memberof Protected
     */
    componentDidMount() {
        const storedThemeIndex = localStorage.getItem('themeIndex');
        if (storedThemeIndex) {
            this.setState({ themeIndex: parseInt(storedThemeIndex, 10) });
        }
        ConfigManager.getConfigs()
            .environments.then((response) => {
                this.environments = response.data.environments;
                this.handleEnvironmentQueryParam();
            })
            .catch((error) => {
                Log.error('Error while receiving environment configurations : ', error);
            });
    }

    /**
     * Load locale file.
     *
     * @param {string} locale Locale name
     * @returns {Promise} Promise
     */
    loadLocale(locale = 'en') {
        fetch(`${Utils.CONST.CONTEXT_PATH}/public/app/locales/${locale}.json`)
            .then(resp => resp.json())
            .then((data) => {
                // eslint-disable-next-line global-require, import/no-dynamic-require
                addLocaleData(require(`react-intl/locale-data/${locale}`));
                this.setState({ messages: defineMessages(data) });
            });
    }

    /**
     * Change the theme index incrementally
     */
    toggleTheme() {
        this.setState(
            ({ themeIndex }) => ({ themeIndex: (themeIndex + 1) % 2 }),
            () => localStorage.setItem('themeIndex', this.state.themeIndex),
        );
    }

    /**
     * Change the environment with "environment" query parameter
     * @return {String} environment name in the query param
     */
    handleEnvironmentQueryParam() {
        let queryString = this.props.location.search;
        queryString = queryString.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const queryParams = qs.parse(queryString);
        const environmentName = queryParams.environment;

        if (!environmentName || Utils.getCurrentEnvironment() === environmentName) {
            // no environment query param or the same environment
            return environmentName;
        }

        const environmentId = Utils.getEnvironmentID(this.environments, environmentName);
        if (environmentId === -1) {
            return environmentName;
        }

        const environment = this.environments[environmentId];
        Utils.setEnvironment(environment);
        return environmentName;
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const environmentName = this.handleEnvironmentQueryParam();
        // Note: AuthManager.getUser() method is a passive check, which simply check the user availability in browser
        // storage,
        // Not actively check validity of access token from back-end
        const currentUser = AuthManager.getUser(environmentName);
        if (currentUser) {
            const header = <Header avatar={<Avatar toggleTheme={this.toggleTheme} />} user={currentUser} />;
            return (
                <IntlProvider locale={language} messages={this.state.messages}>
                    <MuiThemeProvider theme={themes[this.state.themeIndex]}>
                        <Base header={header}>
                            <Switch>
                                <Redirect exact from='/' to='/apis' />
                                <Route path='/apis' component={Apis} />
                                <Route path='/endpoints' component={Endpoints} />
                                <Route component={PageNotFound} />
                            </Switch>
                        </Base>
                    </MuiThemeProvider>
                </IntlProvider>
            );
        }

        const params = qs.stringify({ referrer: this.props.location.pathname });
        return <Redirect to={{ pathname: '/login', search: params }} />;
    }
}

Protected.propTypes = {
    location: PropTypes.shape({
        pathname: PropTypes.string.isRequired,
        search: PropTypes.string,
    }).isRequired,
};

/**
 * Define base routes for the application
 * @returns {React.Component} base routes for the application
 */
const Publisher = () => {
    return (
        <AppErrorBoundary appName='Publisher Application'>
            <Router basename='/publisher'>
                <Switch>
                    <Route path='/login' component={Login} />
                    <Route path='/logout' component={Logout} /> <Route component={Protected} />
                </Switch>
            </Router>
        </AppErrorBoundary>
    );
};

export default Publisher;
