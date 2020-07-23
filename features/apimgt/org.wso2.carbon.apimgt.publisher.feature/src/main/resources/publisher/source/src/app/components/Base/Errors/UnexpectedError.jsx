/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const buttonStyleLogout = {
    padding: '5px 15px',
    margin: 20,
    borderRadius: 5,
    textTransform: 'uppercase',
    color: '#000',
    background: '#15b8cf',
};

function onLogout() {
    window.location = Configurations.app.context + '/services/logout';
}

class UnexpectedError extends Component {
    componentDidMount() {
        document.body.style.backgroundColor = '#dfdfdf';
    }

    componentWillUnmount() {
        document.body.style.backgroundColor = null;
    }

    render() {
        const { message, description } = this.props;
        return (
            <div style={messageStyles}>
                <h5 style={headerStyle}>
                    {message}
                </h5>
                <br />
                <p>
                    {description}
                </p>
                <button type='button' onClick={onLogout} style={buttonStyleLogout}>
                    <FormattedMessage
                        id='UnexpectedError.logout'
                        defaultMessage='Logout'
                    />
                </button>
            </div>
        );
    }
}

UnexpectedError.defaultProps = {
    message: (<FormattedMessage
        id='UnexpectedError.title'
        defaultMessage='Internal Server Error'
    />),
    description: (<FormattedMessage
        id='UnexpectedError.message'
        defaultMessage='Error occurred due to invalid request'
    />),
};

export default UnexpectedError;
