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
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';

const ResourceNotFound = (props) => {
    const { response } = props;
    const message = props.message || {};

    return (
        <div>
            <div className='message message-danger'>
                <Typography variant='title' gutterBottom>
                    { message.title }
                </Typography>
                <Typography variant='subheading' gutterBottom>
                    { message.body }
                    <span style={{ color: 'green' }}> {response ? response.statusText : ''} </span>
                </Typography>
                {message.more}
            </div>
        </div>
    );
};

ResourceNotFound.defaultProps = {
    message: {
        title: <FormattedMessage
            id='Base.Errors.ResourceNotfound.default_tittle'
            defaultMessage='ResourceNotFound'
        />,
        body: <FormattedMessage
            id='Base.Errors.ResourceNotfound.default_body'
            defaultMessage={'No Error message give, Please give a proper error message when using `ResourceNotFound` ' +
            'Component'}
        />,
    },
};

ResourceNotFound.propTypes = {
    response: PropTypes.shape({
        statusText: PropTypes.string,
    }).isRequired,
    message: PropTypes.shape({
        title: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.instanceOf(FormattedMessage),
        ]),
        body: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.instanceOf(FormattedMessage),
        ]),
    }),
};

export default ResourceNotFound;
