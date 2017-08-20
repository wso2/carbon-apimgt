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

import React from 'react'
import {Link, withRouter} from "react-router-dom";
import AuthManager from '../../../data/AuthManager.js';
import qs from 'qs'

const Header = (props) => {
    let params = qs.stringify({referrer: props.location.pathname});
    return (
        <header className="header header-default">
            <div className="container-fluid">
                <div id="nav-icon1" className="menu-trigger navbar-left " data-toggle="sidebar"
                     data-target="#left-sidebar" data-container=".page-content-wrapper" data-container-divide="true"
                     aria-expanded="false" rel="sub-nav">
                </div>
                <div className="pull-left brand">
                    <Link to="/">
                        <span>APIM Admin Portal</span>
                    </Link>
                </div>
                <ul className="nav navbar-right">
                    <li className="visible-inline-block">
                        <a className="dropdown" data-toggle="dropdown" aria-expanded="false">
                <span className="icon fw-stack">
                  <i className="fw fw-circle fw-stack-2x"/>
                  <i className="fw fw-user fw-stack-1x fw-inverse"/>
                </span>
                            <span className="hidden-xs add-margin-left-1x" id="logged-in-username">
                                { AuthManager.getUser() ? AuthManager.getUser().name : ""}
                                <span className="caret"/></span>
                        </a>
                        <ul className="dropdown-menu dropdown-menu-right slideInDown" role="menu">
                            <li><a href="#">Profile Settings</a></li>
                            <li>
                                <Link to={{pathname: '/logout', search: params}}>Logout</Link>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
        </header >
    );
};

// Using `withRouter` helper from React-Router-Dom to get the current user location to be used with logout action,
// We pass the current path in referrer parameter to redirect back the user to where he/she was after login.
// DOC: https://github.com/ReactTraining/react-router/blob/master/packages/react-router/docs/api/withRouter.md
export default withRouter(Header)