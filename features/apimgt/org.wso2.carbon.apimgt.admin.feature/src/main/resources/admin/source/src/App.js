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
import {Base, Login, Logout, Tasks} from './app/components/'
import {PageNotFound} from './app/components/Base/Errors'
import AuthManager from './app/data/AuthManager'
import qs from 'qs'
import 'antd/dist/antd.css'
import './App.css'

import ThrottlingPolicies from './app/components/ThrottlingPolicies'
import Security from './app/components/Security'

/**
 * Render protected application paths
 */
class Protected extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        // Note: AuthManager.getUser() method is a passive check, which simply check the user availability in browser storage,
        // Not actively check validity of access token from backend
        if (AuthManager.getUser()) {
            return (
                <Base>
                    <Switch>
                        <Redirect exact from="/" to="/policies"/>
                        <Route path={"/policies"} component={ThrottlingPolicies}/>
                        <Route path={"/tasks"} component={Tasks}/>
                        <Route path={"/security"} component={Security}/>
                        <Route component={PageNotFound}/>
                    </Switch>
                </Base>
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
const AdminPortal = (props) => {
    return (
        <Router basename="/admin">
            <Switch>
                <Route path={"/login"} render={() => <Login appName={"admin"} appLabel={"ADMIN PORTAL"} />}/>
                <Route path={"/logout"} component={Logout}/>
                <Route component={Protected}/>
            </Switch>
        </Router>
    );
}

export default AdminPortal;