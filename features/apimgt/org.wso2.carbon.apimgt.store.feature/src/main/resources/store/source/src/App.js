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
import Loadable from 'react-loadable';
import ApplicationEdit from './app/components/Applications/Edit/ApplicationEdit';

// import 'typeface-roboto'
import Utils from "./app/data/Utils";
import ConfigManager from "./app/data/ConfigManager";
import {MuiThemeProvider, createMuiTheme} from '@material-ui/core/styles';
import AnonymousView from "./app/components/AnonymousView/AnonymousView";
import SignUp from "./app/components/AnonymousView/SignUp";
import PrivacyPolicy from "./app/components/Policy/PrivacyPolicy";
import CookiePolicy from "./app/components/Policy/CookiePolicy";
import {addLocaleData, defineMessages, IntlProvider} from 'react-intl';
import Configurations from "Config";
import Progress from './app/components/Shared/Progress';

const LoadableProtectedApp = Loadable({
    loader: () => import(// eslint-disable-line function-paren-newline
        /* webpackChunkName: "ProtectedApp" */
        /* webpackPrefetch: true */
            './app/ProtectedApp'),
    loading: Progress,
});

/**
 * Define base routes for the application
 */

class Store extends React.Component {
    constructor(props) {
        super(props);	 
        LoadableProtectedApp.preload();
    }
    render() {
        return (
            <Router basename="/store">
                <Switch>
                    <Route path={"/login"} render={() => <Login appName={"store"} appLabel={"STORE"}/>}/>
                    <Route path={"/logout"} component={Logout}/>
                    <Route path={"/sign-up"} component={SignUp}/>
                    <Route path={"/policy/privacy-policy"} component={PrivacyPolicy}/>
                    <Route path={"/policy/cookie-policy"} component={CookiePolicy}/>
                    <Route component={LoadableProtectedApp}/>
                </Switch>
            </Router>
        );
    }
 
};

export default Store;
