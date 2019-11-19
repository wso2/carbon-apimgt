/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useState, useEffect, useContext } from 'react';
import PropTypes from 'prop-types';
import { Grid, TextField, MenuItem } from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';

/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The props that are being passed
 * @returns {any} The html representation of the component.
 */
function EndpointSecurity(props) {
    const { api } = useContext(APIContext);
    const { intl, securityInfo, onChangeEndpointAuth } = props;
    const [endpointSecurityInfo, setEndpointSecurityInfo] = useState({
        type: 'BASIC',
        username: '',
        password: '',
    });
    const [securityValidity, setSecurityValidity] = useState();

    const authTypes = [
        {
            key: 'BASIC',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.basic',
                defaultMessage: 'Basic Auth',
            }),
        },
        {
            key: 'DIGEST',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.digest.auth',
                defaultMessage: 'Digest Auth',
            }),
        },
    ];
    useEffect(() => {
        const tmpSecurity = {};
        if (securityInfo !== null) {
            const { type, username, password } = securityInfo;
            tmpSecurity.type = type;
            tmpSecurity.username = username;
            tmpSecurity.password = password === '' ? '**********' : password;
        }
        setEndpointSecurityInfo(tmpSecurity);
    }, [props]);

    const validateAndUpdateSecurityInfo = (field) => {
        if (!endpointSecurityInfo[field]) {
            setSecurityValidity({ ...securityValidity, [field]: false });
        } else {
            setSecurityValidity({ ...securityValidity, [field]: true });
        }
        onChangeEndpointAuth(endpointSecurityInfo[field], field);
    };
    return (
        <Grid container direction='row' spacing={2}>
            <Grid item xs={6}>
                <TextField
                    disabled={isRestricted(['apim:api_create'], api)}
                    fullWidth
                    select
                    value={endpointSecurityInfo.type}
                    variant='outlined'
                    onChange={(event) => {
                        onChangeEndpointAuth(event.target.value, 'type');
                    }}
                    inputProps={{
                        name: 'key',
                        id: 'auth-type-select',
                    }}
                >
                    {authTypes.map((type) => (
                        <MenuItem value={type.key}>{type.value}</MenuItem>
                    ))}
                </TextField>
            </Grid>
            <Grid item xs={6} />

            <Grid item xs={6}>
                <TextField
                    disabled={isRestricted(['apim:api_create'], api)}
                    required
                    fullWidth
                    error={securityValidity && securityValidity.username === false}
                    helperText={
                        securityValidity && securityValidity.username === false ? (
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.no.username.error'
                                defaultMessage='Username should not be empty'
                            />
                        ) : (
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.username.message'
                                defaultMessage='Enter Username'
                            />
                        )
                    }
                    variant='outlined'
                    id='auth-userName'
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.user.name.input'
                            defaultMessage='Username'
                        />
                    )}
                    onChange={(event) => setEndpointSecurityInfo(
                        { ...endpointSecurityInfo, username: event.target.value },
                    )}
                    value={endpointSecurityInfo.username}
                    onBlur={() => validateAndUpdateSecurityInfo('username')}
                />
            </Grid>

            <Grid item xs={6}>
                <TextField
                    disabled={isRestricted(['apim:api_create'], api)}
                    required
                    fullWidth
                    error={securityValidity && securityValidity.password === false}
                    helperText={
                        securityValidity && securityValidity.password === false ? (
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.no.password.error'
                                defaultMessage='Password should not be empty'
                            />
                        ) : (
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.password.message'
                                defaultMessage='Enter Password'
                            />
                        )
                    }
                    variant='outlined'
                    type='password'
                    id='auth-password'
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.password.input'
                            defaultMessage='Password'
                        />
                    )}
                    value={endpointSecurityInfo.password}
                    onChange={(event) => setEndpointSecurityInfo(
                        { ...endpointSecurityInfo, password: event.target.value },
                    )}
                    onBlur={() => validateAndUpdateSecurityInfo('password')}
                />
            </Grid>
        </Grid>
    );
}

EndpointSecurity.propTypes = {
    intl: PropTypes.func.isRequired,
    securityInfo: PropTypes.shape({}).isRequired,
    onChangeEndpointAuth: PropTypes.func.isRequired,
};

export default injectIntl(EndpointSecurity);
