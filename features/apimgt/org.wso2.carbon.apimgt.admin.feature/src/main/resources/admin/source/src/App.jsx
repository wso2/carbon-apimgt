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
import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom';
import qs from 'qs';
import PropTypes from 'prop-types';
import 'antd/dist/antd.css';

import { Base, Login, Logout, Tasks } from './app/components/';
import { PageNotFound } from './app/components/Base/Errors';
import AuthManager from './app/data/AuthManager';
import './App.css';
import ThrottlingPolicies from './app/components/ThrottlingPolicies';
import Security from './app/components/Security';
import Utils from './app/data/Utils';
import ConfigManager from './app/data/ConfigManager';

/**
 * Render protected application paths
 */
class Protected extends Component {
    constructor(props) {
        super(props);
        this.state = { environments: [] };
        this.environmentName = '';
    }

    componentDidMount() {
        ConfigManager.getConfigs()
            .environments.then((response) => {
                const { environments } = response.data;
                this.setState({ environments });
            })
            .catch((error) => {
                console.error('Error while receiving environment configurations : ', error);
            });
    }

    /**
     * Change the environment with "environment" query parameter
     */
    handleEnvironmentQueryParam() {
        let queryString = this.props.location.search;
        queryString = queryString.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const queryParams = qs.parse(queryString);
        const environmentName = queryParams.environment;

        if (!environmentName || this.environmentName === environmentName) {
            // no environment query param or the same environment
            return;
        }

        const environmentId = Utils.getEnvironmentID(this.state.environments, environmentName);
        if (environmentId === -1) {
            console.error('Invalid environment name in environment query parameter.');
            return;
        }

        const environment = this.state.environments[environmentId];
        Utils.setEnvironment(environment);
        this.environmentName = environmentName;
    }

    render() {
        this.handleEnvironmentQueryParam();
        // Note: AuthManager.getUser() method is a passive check, which simply check
        // the user availability in browser storage,
        // Not actively check validity of access token from backend
        if (AuthManager.getUser()) {
            return (
                <Base>
                    <Switch>
                        <Redirect exact from='/' to='/policies' />
                        <Route path='/policies' component={ThrottlingPolicies} />
                        <Route path='/tasks' component={Tasks} />
                        <Route path='/security' component={Security} />
                        <Route component={PageNotFound} />
                    </Switch>
                </Base>
            );
        }

        const params = qs.stringify({ referrer: this.props.location.pathname });
        return <Redirect to={{ pathname: '/login', search: params }} />;
    }
}

Protected.propTypes = {
    location: PropTypes.shape({
        search: PropTypes.string,
        pathname: PropTypes.string,
    }).isRequired,
};

/**
 * Define base routes for the application
 */
const AdminPortal = () => {
    return (
        <Router basename='/admin'>
            <Switch>
                <Route path='/login' render={() => <Login appName='admin' appLabel='ADMIN PORTAL' />} />
                <Route path='/logout' component={Logout} />
                <Route component={Protected} />
            </Switch>
        </Router>
    );
};

export default AdminPortal;
