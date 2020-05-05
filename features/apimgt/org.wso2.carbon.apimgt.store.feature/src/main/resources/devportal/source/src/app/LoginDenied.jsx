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
import Settings from 'Settings';
import { FormattedMessage } from 'react-intl';
import CONSTS from './data/Constants';

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
    margin: 10,
    borderRadius: 5,
    textTransform: 'uppercase',
    color: '#15b8cf',
    border: 'solid 1px #15b8cf',
    background: '#efefef',
};

const buttonStyleLogout = {
    padding: '5px 15px',
    margin: 10,
    borderRadius: 5,
    textTransform: 'uppercase',
    color: '#000',
    background: '#15b8cf',
};

function onLogout() {
    window.location = Settings.app.context + '/services/logout';
}

function onGoToAnonymousView() {
    if (Settings.app.isPassive) {
        sessionStorage.setItem(CONSTS.ISLOGINPERMITTED, 'true');
    }
    window.location = Settings.app.context + '/services/auth/callback/logout';
}

class LoginDenied extends Component {
    componentWillMount() {
        document.body.style.backgroundColor = '#dfdfdf';
    }

    componentWillUnmount() {
        document.body.style.backgroundColor = null;
    }
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
                        defaultMessage={'You don\'t have sufficient privileges to access the Developer Portal.'}
                    />
                </p>
                <div>
                    {this.props.IsAnonymousModeEnabled ? (
                        <div>
                            <button onClick={onGoToAnonymousView} style={buttonStyleRetry}>
                                <FormattedMessage
                                    id='LoginDenied.anonymousview'
                                    defaultMessage='Go To Public Portal'
                                />
                            </button>
                            <button onClick={onLogout} style={buttonStyleLogout}>
                                <FormattedMessage
                                    id='LoginDenied.logout'
                                    defaultMessage='Logout'
                                />
                            </button>
                        </div>
                    ) : (
                        <button onClick={onLogout} style={buttonStyleLogout}>
                            <FormattedMessage
                                id='LoginDenied.logout'
                                defaultMessage='Logout'
                            />
                        </button>
                    )}
                </div>
            </div>
        );
    }
}

export default LoginDenied;
