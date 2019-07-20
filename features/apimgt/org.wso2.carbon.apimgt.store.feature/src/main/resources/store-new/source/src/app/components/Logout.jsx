/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import { injectIntl, } from 'react-intl';
import AuthManager from '../data/AuthManager';
/**
 * Logout component
 *
 * @class Logout
 * @extends {Component} Logout component
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
     * Component Mounted Lifecycle call back
     *
     * @memberof Logout
     */
    componentDidMount() {
        const promisedLogout = this.authManager.logout();
        const { location, intl } = this.props;
        promisedLogout
            .then(() => {
                const newState = { logoutSuccess: true };
                let queryString = location.search;
                queryString = queryString.replace(/^\?/, '');
                /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
                const params = qs.parse(queryString);
                if (params.referrer) {
                    newState.referrer = params.referrer;
                }
                this.setState(newState);
            })
            .catch(() => {
                console.log(intl.formatMessage({
                    id: 'Logout.error',
                    defaultMessage: 'Error while logging out'}));
            });
    }

    /**
     * Main render method
     *
     * @returns {JSX}
     * @memberof Logout
     */
    render() {
        const { logoutSuccess, referrer } = this.state;
        return logoutSuccess && <Redirect to={referrer} />;
    }
}
Logout.propTypes = {
    location: PropTypes.instanceOf(Object).isRequired,
};

export default injectIntl(Logout);
