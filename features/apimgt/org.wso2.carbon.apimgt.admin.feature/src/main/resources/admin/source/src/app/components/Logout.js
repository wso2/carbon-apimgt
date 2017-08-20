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
import AuthManager from '../data/AuthManager'
import {Redirect} from 'react-router-dom';
import qs from 'qs'

class Logout extends Component {
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            logoutSuccess: false,
            logoutFail: null,
            referrer: "/login"
        };
    }

    componentDidMount() {
        const promisedLogout = this.authManager.logout();
        promisedLogout.then(() => {
            let newState = {logoutSuccess: true};
            let queryString = this.props.location.search;
            queryString = queryString.replace(/^\?/, '');
            let params = qs.parse(queryString);
            if (params.referrer) {
                newState['referrer'] = params.referrer;
            }
            this.setState(newState);

        }).catch((error) => {
                let message = "Error while logging out";
                console.log(message);
                this.setState({logoutFail: error});
            }
        );
    }

    render() {
        return this.state.logoutSuccess && <Redirect to={this.state.referrer}/>;
    }
}

export default Logout;

