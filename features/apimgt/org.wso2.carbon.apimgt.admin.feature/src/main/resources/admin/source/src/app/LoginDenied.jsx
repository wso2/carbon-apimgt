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
import Configurations from 'Config';
import { FormattedMessage } from 'react-intl';

const messageStyles = {
    width: 400,
    position: 'absolute',
    left: '50%',
    marginLeft: -200,
    textAlign: 'center',
    padding: 10,
    paddingLeft: 100,
    paddingRight: 100,
    border: 'solid 1px #ddd',
    borderRadius: 20,
    background: '#efefef',
    top: '20%',
    boxShadow: '#efefef',
    fontWeight: 200,
    fontSize: 15,
    color: '#444',
    fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
};

const headerStyle = {
    fontWeight: 400,
    fontSize: 20,
};

const buttonStyleRetry = {
    padding: '5px 15px',
    margin: 20,
    borderRadius: 5,
    textTransform: 'uppercase',
    color: '#15b8cf',
    border: 'solid 1px #15b8cf',
    background: '#efefef',
};

const buttonStyleLogout = {
    padding: '5px 15px',
    margin: 20,
    borderRadius: 5,
    textTransform: 'uppercase',
    color: '#000',
    background: '#15b8cf',
};

function onRetry() {
    window.location = Configurations.app.context;
}

function onLogout() {
    window.location = Configurations.app.context + '/services/logout';
}

/**
 *
 * Handle LoginDenied action from APP
 * @class LoginDenied
 * @extends {Component}
 */
class LoginDenied extends Component {
    /**
     * @memberof LoginDenied
     */
    componentDidMount() {
        document.body.style.backgroundColor = '#dfdfdf';
    }

    /**
     * @memberof LoginDenied
     */
    componentWillUnmount() {
        document.body.style.backgroundColor = null;
    }

    /**
     *
     * @returns {React.Component} Render LoginDenied
     * @memberof LoginDenied
     */
    render() {
        return (
            <div style={messageStyles}>
                <h5 style={headerStyle}>
                    <FormattedMessage
                        id='LoginDenied.title'
                        defaultMessage='Error 403 : Forbidden'
                    />
                </h5>
                <br />
                <p>
                    <FormattedMessage
                        id='LoginDenied.message'
                        defaultMessage={'The server could not verify '
                            + 'that you are authorized to access the requested resource.'}
                    />
                </p>
                <button type='button' onClick={onRetry} style={buttonStyleRetry}>
                    <FormattedMessage
                        id='LoginDenied.retry'
                        defaultMessage='Retry'
                    />
                </button>
                <button type='button' onClick={onLogout} style={buttonStyleLogout}>
                    <FormattedMessage
                        id='LoginDenied.logout'
                        defaultMessage='Logout'
                    />
                </button>
            </div>
        );
    }
}

export default LoginDenied;
