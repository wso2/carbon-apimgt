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
import { Redirect } from 'react-router-dom';
import qs from 'qs';
import Utils from 'AppData/Utils';
import User from 'AppData/User';

/**
 *
 * Handle Logout action from APP
 * @class Logout
 * @extends {Component}
 */
class Logout extends Component {
    /**
     *Creates an instance of Logout.
     * @param {Object} props React component props
     * @memberof Logout
     */
    constructor(props) {
        super(props);
        this.state = {
            logoutSuccess: false,
            referrer: '/apis',
        };
    }

    /**
     *
     *
     * @memberof Logout
     */
    componentDidMount() {
        const environmentName = Utils.getCurrentEnvironment().label;
        localStorage.removeItem(`${User.CONST.LOCAL_STORAGE_USER}_${environmentName}`);
        const newState = { logoutSuccess: true };
        let { search } = window.location;
        search = search.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const params = qs.parse(search);
        if (params.referrer) {
            newState.referrer = params.referrer;
        }
        this.setState(newState);
    }

    /**
     *
     *
     * @returns {React.Component} Render Logout
     * @memberof Logout
     */
    render() {
        const { logoutSuccess, referrer } = this.state;
        return logoutSuccess && <Redirect to={referrer} />;
    }
}

export default Logout;
