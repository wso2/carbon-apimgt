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
import { Switch, Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';
import qs from 'qs';

import AuthManager from '../../../data/AuthManager';
import ResourceNotFound from '../Errors/ResourceNotFound';
/**
 * DEPRECATED: This authorization check is done in App -> Protected component, hence deprecating use of this
 *
 * @class AuthCheck
 * @extends {Component}
 */
class AuthCheck extends Component {
    constructor(props) {
        super(props);
        this.state = { isLogged: AuthManager.getUser() };
        if (props.response) {
            const status = props.response.status;
            this.state.isAuthorize = status !== 401;
            this.state.resourceNotFound = status == 404;
        }
    }

    /**
     *
     *
     * @returns
     * @memberof AuthCheck
     */
    render() {
        if (!this.state.isLogged || this.state.isAuthorize === false) {
            const params = qs.stringify({ referrer: this.props.location.pathname });
            return (
                <Switch>
                    <Redirect to={{ pathname: '/login', search: params }} />
                </Switch>
            );
        } else if (this.state.resourceNotFound) {
            return <ResourceNotFound {...this.props} />;
        } else {
            return <div>{this.props.children}</div>;
        }
    }
}

AuthCheck.propTypes = {
    response: PropTypes.object,
};

export default AuthCheck;
