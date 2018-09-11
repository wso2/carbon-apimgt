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
import Alert from 'AppComponents/Shared/Alert';
import PropTypes from 'prop-types';

import ConfigManager from '../data/ConfigManager';
import AuthManager from '../data/AuthManager';


/**
 *
 * Handle Logout action from APP
 * @class Logout
 * @extends {Component}
 */
class Logout extends Component {
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            logoutSuccess: false,
            referrer: '/login',
        };
    }


    /**
     *
     *
     * @memberof Logout
     */
    componentDidMount() {
        ConfigManager.getConfigs()
            .environments.then((response) => {
                const { environments } = response.data;
                const promisedLogout = this.authManager.logoutFromEnvironments(environments);
                promisedLogout
                    .then(() => {
                        const newState = { logoutSuccess: true };
                        let queryString = this.props.location.search;
                        queryString = queryString.replace(/^\?/, '');
                        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
                        const params = qs.parse(queryString);
                        if (params.referrer) {
                            newState.referrer = params.referrer;
                        }
                        this.setState(newState);
                    })
                    .catch((error) => {
                        const message = 'Error while logging out';
                        Alert.error(message);
                        console.log(error);
                    });
            })
            .catch((error) => {
                console.error(error);
                Alert.error('Error while receiving environment configurations');
            });
    }

    render() {
        return this.state.logoutSuccess && <Redirect to={this.state.referrer} />;
    }
}

Logout.propTypes = {
    location: PropTypes.shape({
        search: PropTypes.string,
    }).isRequired,
};

export default Logout;
