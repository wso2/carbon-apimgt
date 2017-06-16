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
import ReactDom from 'react-dom'
import {BrowserRouter as Router, Route, Link, Switch} from 'react-router-dom'

import {Apis, Landing, Base, Login, Logout} from './app/components/index'
import {PageNotFound} from './app/components/Base/Errors/index'
import Utils from '../src/app/data/utils.js'
import ApiCreate from './app/components/Apis/Create/ApiCreate'

import './App.css'


const BApis = (props) => {
    return (
        <Base>
            <Apis/>
        </Base>
    );
}

const BApiCreate = (props) => {
    return (
        <Base>
            <ApiCreate/>
        </Base>
    );
}

class Publisher extends Component {
    constructor() {
        super();
    }

    componentDidMount() {
        //Utils.autoLogin(); // TODO: Remove once login page is implemented

    }

    render() {
        return (
            <Router basename="/publisher_new">
                    <Switch>
                        <Route exact path={"/"} component={BApiCreate}/>
                        <Route path={"/apis"} component={BApis}/>
                        <Route path={"/api/create"} component={BApiCreate}/>
                        <Route path={"/login"} component={Login}/>
                        <Route path={"/logout"} component={Logout}/>
                        <Route component={PageNotFound}/>
                    </Switch>
            </Router>
        );
    }
}

export default Publisher;
