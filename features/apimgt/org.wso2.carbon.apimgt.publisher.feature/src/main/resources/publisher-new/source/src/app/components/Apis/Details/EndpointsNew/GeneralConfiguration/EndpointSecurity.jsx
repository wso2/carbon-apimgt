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

import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import {
    Grid,
    TextField,
    withStyles,
    MenuItem,
    InputLabel,
    Select,
    FormControl,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = theme => ({
    credentialsContainer: {
        display: 'flex',
        justifyContent: 'space-between',
    },
    textField: {
        marginLeft: theme.spacing.unit,
    },
    advancedConfigWrapper: {
        width: '50%',
    },
});

/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The props that are being passed
 * @returns {any} The html representation of the component.
 */
function EndpointSecurity(props) {
    const {
        classes,
        intl,
        securityInfo,
        onChangeEndpointAuth,
    } = props;
    const [endpointSecurityInfo, setEndpointSecurityInfo] = useState({
        type: 'BASIC',
        username: '',
        password: '',
    });

    const authTypes = [
        {
            key: 'BASIC',
            value: intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.GeneralConfiguration.EndpointSecurity.basic',
                defaultMessage: 'Basic Auth',
            }),
        },
        {
            key: 'DIGEST',
            value: intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.GeneralConfiguration.EndpointSecurity.digest.auth',
                defaultMessage: 'Digest Auth',
            }),
        }];

    useEffect(() => {
        console.log('Use Effect security info', securityInfo);
        setEndpointSecurityInfo(securityInfo !== null ? securityInfo : endpointSecurityInfo);
    }, [props]);

    return (
        <form className={classes.advancedConfigWrapper}>
            <Grid container direction='column' form>
                <FormControl className={classes.formControl}>
                    <InputLabel htmlFor='auth-type-select'>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.EndpointSecurity.auth.type'
                            defaultMessage='Auth Type'
                        />
                    </InputLabel>
                    <Select
                        value={endpointSecurityInfo.type}
                        onChange={(event) => { onChangeEndpointAuth(event, 'type'); }}
                        inputProps={{
                            name: 'key',
                            id: 'auth-type-select',
                        }}
                    >
                        {authTypes.map(type => (
                            <MenuItem value={type.key}>
                                {type.value}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <div className={classes.credentialsContainer}>
                    <TextField
                        required
                        id='auth-userName'
                        label={<FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.EndpointSecurity.user.name.input'
                            defaultMessage='User Name'
                        />}
                        className={classes.textField}
                        margin='normal'
                        onChange={
                            event => setEndpointSecurityInfo({ ...endpointSecurityInfo, username: event.target.value })}
                        value={endpointSecurityInfo.username}
                        onBlur={(event) => { onChangeEndpointAuth(event, 'username'); }}
                    />
                    <TextField
                        required
                        type='password'
                        id='auth-password'
                        label={<FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.EndpointSecurity.password.input'
                            defaultMessage='Password'
                        />}
                        className={classes.textField}
                        margin='normal'
                        value={endpointSecurityInfo.password}
                        onChange={
                            event => setEndpointSecurityInfo({ ...endpointSecurityInfo, password: event.target.value })}
                        onBlur={(event) => { onChangeEndpointAuth(event, 'password'); }}
                    />
                </div>
            </Grid>
        </form>
    );
}

EndpointSecurity.propTypes = {
    classes: PropTypes.shape({
        advancedConfigWrapper: PropTypes.shape({}),
        textField: PropTypes.shape({}),
        menu: PropTypes.shape({}),
        credentialsContainer: PropTypes.shape({}),
    }).isRequired,
    intl: PropTypes.func.isRequired,
    securityInfo: PropTypes.shape({}).isRequired,
    onChangeEndpointAuth: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(EndpointSecurity));
