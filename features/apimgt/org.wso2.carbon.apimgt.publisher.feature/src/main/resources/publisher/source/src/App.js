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

import React, {Component} from 'react'

import {BrowserRouter as Router, Redirect, Route, Switch} from 'react-router-dom'
// import {Apis, Base, Login, Logout, Endpoints} from './app/components'
import {PageNotFound} from './app/components/Base/Errors'
// import ApiCreate from './app/components/Apis/Create/ApiCreate'
import AuthManager from './app/data/AuthManager'
import qs from 'qs'
import Axios from 'axios';
import {getAsyncComponent} from 'async-react-component';
import 'antd/dist/antd.css'
import './App.css'
import Utils from "./app/data/Utils";
import ConfigManager from "./app/data/ConfigManager";
import {MuiThemeProvider, createMuiTheme} from 'material-ui/styles';
import MaterialDesignCustomTheme from './app/components/Shared/CustomTheme'
const themes = [];
let darkTheme = createMuiTheme({
    palette: {
        type: 'dark', // Switching the dark mode on is a single property value change.
    },
});
let lightTheme = createMuiTheme({
    palette: {
        type: 'light', // Switching the dark mode on is a single property value change.
    },
});
darkTheme.palette.background.active = 'rgba(27, 94, 32, 1)';
darkTheme.palette.background.appBar = 'rgba(63, 81, 181, 1)';
lightTheme.palette.background.active = 'rgba(165, 214, 167, 1)';
lightTheme.palette.background.appBar = 'rgba(33, 150, 243, 1)';
lightTheme.palette.background.contentFrame = 'rgba(227, 242, 253, 1)';
themes.push(darkTheme);
themes.push(lightTheme);
themes.push(createMuiTheme(MaterialDesignCustomTheme));


const Apis = () => import(/* webpackChunkName: "apis" */ './app/components/Apis/Apis');
const Endpoints = () => import(/* webpackChunkName: "endpoints" */ './app/components/Endpoints');
const ApiCreate = () => import(/* webpackChunkName: "create" */ './app/components/Apis/Create/ApiCreate');
const Base = () => import(/* webpackChunkName: "base" */ './app/components/Base');
const BaseLayout = getAsyncComponent(Base);
const Login = () => import(/* webpackChunkName: "login" */  './app/components/Login/Login');
const Logout = () => import(/* webpackChunkName: "logout" */ './app/components/Logout');

/**
 * Render protected application paths
 */
class Protected extends Component {
    constructor(props) {
        super(props);
        this.state = {
            showLeftMenu: false,
            themeIndex: 0,
        };
        this.environments = [];
    }

    componentDidMount() {
        ConfigManager.getConfigs().environments.then(response => {
            this.environments = response.data.environments;
            this.handleEnvironmentQueryParam();
        });
    }


    componentWillMount() {
        let storedThemeIndex = localStorage.getItem("themeIndex");
        if (storedThemeIndex) {
            this.setState({themeIndex: parseInt(storedThemeIndex)})
        }
    }
    /**
     * Change the theme index incrementally
     */
    setTheme() {
        this.setState({theme: themes[this.state.themeIndex % 3]});
        this.state.themeIndex++;
        localStorage.setItem("themeIndex", this.state.themeIndex);
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

        if (!environmentName || Utils.getCurrentEnvironment() === environmentName) {
            // no environment query param or the same environment
            return environmentName;
        }

        let environmentId = Utils.getEnvironmentID(this.environments, environmentName);
        if (environmentId === -1) {
            return environmentName;
        }

        let environment = this.environments[environmentId];
        Utils.setEnvironment(environment);
        return environmentName;
    }

    render() {
        const environmentName = this.handleEnvironmentQueryParam();
        // Note: AuthManager.getUser() method is a passive check, which simply check the user availability in browser storage,
        // Not actively check validity of access token from backend

        if (AuthManager.getUser(environmentName)) {
            return (
                <MuiThemeProvider theme={themes[this.state.themeIndex % 3]}>
                    <BaseLayout setTheme={() => this.setTheme()}>
                        <Switch>
                            <Redirect exact from="/" to="/apis"/>
                            <Route path={"/apis"} component={getAsyncComponent(Apis)}/>
                            <Route path={"/endpoints"} component={getAsyncComponent(Endpoints)}/>
                            <Route path={"/api/create"} component={getAsyncComponent(ApiCreate)}/>
                            <Route component={PageNotFound}/>
                        </Switch>
                    </BaseLayout>
                </MuiThemeProvider>
            );
        }

        let params = qs.stringify({referrer: this.props.location.pathname});
        return (
            <Redirect to={{pathname: '/login', search: params}}/>
        );
    }
}

/**
 * Define base routes for the application
 */
class Publisher extends Component {

    render() {
        return (
            <Router basename="/publisher">
                <Switch>
                    <Route path={"/login"} component={getAsyncComponent(Login)}/>
                    <Route path={"/logout"} component={getAsyncComponent(Logout)}/>
                    <Route component={Protected}/>
                </Switch>
            </Router>
        );
    }
}

export default Publisher;
