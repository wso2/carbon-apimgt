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
import { MuiThemeProvider, createMuiTheme } from 'material-ui/styles';
import Log from 'log4javascript';
import './App.css';
import Utils from './app/data/Utils';
import ConfigManager from './app/data/ConfigManager';
import MaterialDesignCustomTheme from './app/components/Shared/CustomTheme';
import { PageNotFound } from './app/components/Base/Errors';
import AuthManager from './app/data/AuthManager';
import ApiCreate from './app/components/Apis/Create/ApiCreate';
import Apis from './app/components/Apis/Apis';
import Endpoints from './app/components/Endpoints';
import Base from './app/components/Base';
import Login from './app/components/Login/Login';
import Logout from './app/components/Logout';
import AppErrorBoundary from './app/components/Shared/AppErrorBoundary';

const themes = [];
const darkTheme = createMuiTheme({
    palette: {
        type: 'dark', // Switching the dark mode on is a single property value change.
    },
});
const lightTheme = createMuiTheme({
    palette: {
        type: 'light', // Switching the dark mode on is a single property value change.
    },
});
darkTheme.palette.background.active = 'rgba(27, 94, 32, 1)';
darkTheme.palette.background.appBar = 'rgba(63, 81, 181, 1)';
lightTheme.palette.background.active = 'rgba(165, 214, 167, 1)';
lightTheme.palette.background.appBar = 'rgb(59, 120, 231,1)';
lightTheme.palette.background.contentFrame = 'rgba(227, 242, 253, 1)';
lightTheme.palette.text.brand = 'rgba(255,255,255,1)';
themes.push(darkTheme);
themes.push(lightTheme);
themes.push(createMuiTheme(MaterialDesignCustomTheme));

/**
 * Render protected application paths
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
            themeIndex: 0,
        };
        this.environments = [];
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
     * Change the theme index incrementally
     */
    setTheme() {
        let { themeIndex } = this.state;
        themeIndex++;
        localStorage.setItem('themeIndex', themeIndex);
        this.setState({ themeIndex });
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

        if (AuthManager.getUser(environmentName)) {
            return (
                <MuiThemeProvider theme={themes[this.state.themeIndex % 3]}>
                    <Base setTheme={() => this.setTheme()}>
                        <Switch>
                            <Redirect exact from='/' to='/apis' />
                            <Route path='/apis' component={Apis} />
                            <Route path='/endpoints' component={Endpoints} />
                            <Route path='/api/create' component={ApiCreate} />
                            <Route component={PageNotFound} />
                        </Switch>
                    </Base>
                </MuiThemeProvider>
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
