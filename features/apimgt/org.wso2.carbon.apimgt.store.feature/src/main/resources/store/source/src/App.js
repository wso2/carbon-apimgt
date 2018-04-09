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
import {Apis, ApplicationCreate, Applications, Base, Login, Logout} from './app/components'
import {PageNotFound} from './app/components/Base/Errors'
import AuthManager from './app/data/AuthManager'
import qs from 'qs'

// import 'typeface-roboto'
import Utils from "./app/data/Utils";
import ConfigManager from "./app/data/ConfigManager";
import {MuiThemeProvider, createMuiTheme} from 'material-ui/styles';
import DarkTheme from './app/components/Shared/DarkTheme'
import LightTheme from './app/components/Shared/LightTheme'
const themes = [];

themes.push(createMuiTheme(LightTheme));
themes.push(createMuiTheme(DarkTheme));

// import './materialize.css'

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
        /* TODO: need to fix the header to avoid conflicting with messages ~tmkb*/
    }

    componentDidMount() {
        ConfigManager.getConfigs().environments.then(response => {
            this.environments = response.data.environments;
            this.handleEnvironmentQueryParam();
        }).catch(error => {
            console.error('Error while receiving environment configurations : ', error);
        });
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

        let environmentId = Utils.getEnvironmentID(this.environments, environmentName);
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
        let storedThemeIndex = localStorage.getItem("themeIndex");
        if (storedThemeIndex) {
            this.setState({themeIndex: parseInt(storedThemeIndex)})
        }
    }
    setTheme() {
        this.setState({theme: themes[this.state.themeIndex % 3]});
        this.state.themeIndex++;
        localStorage.setItem("themeIndex", this.state.themeIndex);
    }

    render() {
        const environmentName = this.handleEnvironmentQueryParam();
        // Note: AuthManager.getUser() method is a passive check, which simply check the user availability in browser storage,
        // Not actively check validity of access token from backend
        if (AuthManager.getUser(environmentName)) {
            return (
                <MuiThemeProvider theme={themes[this.state.themeIndex % 2]}>
                    <Base setTheme={() => this.setTheme()}>
                        <Switch>
                            <Redirect exact from="/" to="/apis"/>
                            <Route path={"/apis"} component={Apis}/>
                            <Route path={"/applications"} component={Applications}/>
                            <Route path={"/application/create"} component={ApplicationCreate}/>
                            <Route component={PageNotFound}/>
                        </Switch>
                    </Base>
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

const Store = (props) => {
    return (
        <Router basename="/store">
            <Switch>
                <Route path={"/login"} render={() => <Login appName={"store"} appLabel={"STORE"}/>}/>
                <Route path={"/logout"} component={Logout}/>
                <Route component={Protected}/>
            </Switch>
        </Router>
    );
}

export default Store;