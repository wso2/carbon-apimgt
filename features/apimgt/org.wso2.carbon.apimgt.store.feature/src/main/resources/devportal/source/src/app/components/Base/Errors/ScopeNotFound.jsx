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

import React from 'react';
import { FormattedMessage } from 'react-intl';

const ScopeNotFound = (props) => {
    return (
        <div>
            <div className='message message-danger'>
                <h4>
                    <i className='icon fw fw-error' />
                    <FormattedMessage
                        id='Base.Errors.ScopeNotFound.title'
                        defaultMessage='Un-authorized Access'
                    />
                </h4>
                <p>
                    <FormattedMessage
                        id='Base.Errors.ScopeNotFound.message.first'
                        defaultMessage='Sorry, the page you are looking for '
                    />
                    <span style={{ color: 'green' }}>
                        {props.location.pathname}
                    </span>
                    <FormattedMessage
                        id='Base.Errors.ScopeNotFound.message.second'
                        defaultMessage={'is not allowed under logged in user role scopes. '
                        + 'Please login with different user with relevant permission to access this resource.'}
                    />
                </p>
            </div>
        </div>
    );
};

export default ScopeNotFound;
