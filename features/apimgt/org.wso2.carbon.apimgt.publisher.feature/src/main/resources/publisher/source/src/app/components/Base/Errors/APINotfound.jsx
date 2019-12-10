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

import React from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';

const APINotFound = (props) => {
    return (
        <div>
            <div className='message message-danger'>
                <h4>
                    <i className='icon fw fw-error' />
                    <FormattedMessage
                        id='Base.Errors.APINotfound.header'
                        defaultMessage='404 API Not Found!'
                    />
                </h4>
                <p>
                    <FormattedMessage
                        id='Base.Errors.APINotfound.message'
                        defaultMessage='Can&apos;t find an API associate with the given API ID'
                    />
                    <span style={{ color: 'green' }}>
                        {' '}
                        {props.match.params.apiUUID}
                        {' '}
                    </span>
                </p>
            </div>
        </div>
    );
};

APINotFound.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            apiUUID: PropTypes.string,
        }),
    }).isRequired,
};
export default APINotFound;
