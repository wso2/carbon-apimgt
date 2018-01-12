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
import LoadingAnimation from './app/components/Base/Loading/Loading.js';
import {getAsyncComponent} from 'async-react-component';
import 'antd/dist/antd.css'
import {message} from 'antd'
import './App.css'
import Utils from "./app/data/Utils";


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
            authConfigs: null
        };
        message.config({top: '48px'}); // .custom-header height + some offset
        /* TODO: need to fix the header to avoid conflicting with messages ~tmkb*/
    }

    handleResponse = (response) => {
        this.setState({
            authConfigs: response.data.members
        });
    };

    /**
     * Handle invalid login url in localStorage - environment object
     * @param reject
     */
    handleReject = reject => {
        console.log("Error: Single Sign On:\n", reject);
        Utils.setEnvironment(); //Set Default environment
        Axios.get(Utils.getAppLoginURL()).then(this.handleResponse); //Try login
    };

    /**
     * Change the visibility state of left side navigation menu bar
     * @param {boolean} status : Whether or not to show or hide left side navigation menu
     */
    setLeftMenu(status) {
        this.setState({
            showLeftMenu: status
        });
    }

    render() {
        // Note: AuthManager.getUser() method is a passive check, which simply check the user availability in browser storage,
        // Not actively check validity of access token from backend
        if (AuthManager.getUser()) {
            return (
                <BaseLayout>
                    <Switch>
                        <Redirect exact from="/" to="/apis"/>
                        <Route path={"/apis"} component={getAsyncComponent(Apis)}/>
                        <Route path={"/endpoints"} component={getAsyncComponent(Endpoints)}/>
                        <Route path={"/api/create"} component={getAsyncComponent(ApiCreate)}/>
                        <Route component={PageNotFound}/>
                    </Switch>
                </BaseLayout>
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
