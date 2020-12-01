/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React from 'react';
import { MenuItem, TextField } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';

export default function SelectEndpoint(props) {
    const {
        onChange,
        endpoint,
        isEndpointEmpty,
        endpoints,
    } = props;
    const iff = (condition, then, otherwise) => (condition ? then : otherwise);
    return (
        <TextField
            autoFocus
            error={isEndpointEmpty || endpoints.length === 0}
            helperText={endpoints.length === 0 ? (
                <FormattedMessage
                    id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.endpoint.empty.error'
                    defaultMessage='Save endpoints before adding the Certificate'
                />
            ) : iff(isEndpointEmpty, <FormattedMessage
                id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.endpoint.error'
                defaultMessage='Endpoint should not be empty'
            />, <FormattedMessage
                id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.endpoint.helpertext'
                defaultMessage='Endpoint for the Certificate'
            />)}
            required
            id='certificateEndpoint'
            label={(
                <FormattedMessage
                    id='Apis.Details.Endpoints.GeneralConfiguration.UploadCertificate.endpoint'
                    defaultMessage='Endpoint'
                />
            )}
            value={endpoint}
            placeholder='Endpoint'
            onChange={(event) => onChange(event.target.value)}
            margin='normal'
            variant='outlined'
            fullWidth
            select
        >
            {endpoints !== null && endpoints.map((ep) => {
                if (ep) {
                    return (<MenuItem value={ep.url}>{ep.url}</MenuItem>);
                }
                return null;
            })}
        </TextField>
    );
}

SelectEndpoint.propTypes = {
    endpoints: PropTypes.shape({}).isRequired,
    onChange: PropTypes.func.isRequired,
    classes: PropTypes.shape({}).isRequired,
    endpoint: PropTypes.string.isRequired,
    isEndpointEmpty: PropTypes.string.isRequired,
};
