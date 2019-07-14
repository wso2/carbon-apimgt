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

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { Grid, TextField, withStyles, MenuItem } from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = theme => ({
    credentialsContainer: {
        display: 'flex',
    },
    textField: {
        marginLeft: theme.spacing.unit,
    },
    advancedConfigWrapper: {
        width: '100%',
    },
});

/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The props that are being passed
 * @returns {any} The html representation of the component.
 */
function EndpointSecurity(props) {
    const { classes, intl } = props;
    const [securitySchema, setSecuritySchema] = useState('Not-Secured');
    const [authType, setAuthType] = useState('Basic Auth');

    const securitySchemas = [
        {
            value: intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.not.secured',
                defaultMessage: 'Not-Secured',
            }),
            key: 'none',
        },
        {
            value: intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.secured',
                defaultMessage: 'Secured',
            }),
            key: 'secured',
        }];
    const authTypes = [
        {
            key: 'basic',
            value: intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.basic',
                defaultMessage: 'Basic Auth',
            }),
        },
        {
            key: 'digest',
            value: intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.digest.auth',
                defaultMessage: 'Digest Auth',
            }),
        }];

    const onSelectSecuritySchema = (event) => {
        setSecuritySchema(event.target.value);
    };

    const onSelectAuthType = (event) => {
        setAuthType(event.target.value);
    };

    return (
        <form className={classes.advancedConfigWrapper}>
            <Grid container direction='column'>
                <TextField
                    id='security-schema-select'
                    select
                    label={<FormattedMessage
                        id='Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.endpoint.security.schema'
                        defaultMessage='Endpoint Security Schema'
                    />}
                    className={classes.textField}
                    value={securitySchema}
                    onChange={onSelectSecuritySchema}
                    SelectProps={{
                        MenuProps: {
                            className: classes.menu,
                        },
                    }}
                    helperText={
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.
                            EndpointSecurity.select.endpoint.security.schema'
                            defaultMessage='Select the endpoint security schema'
                        />
                    }
                    margin='normal'
                >
                    {securitySchemas.map(option => (
                        <MenuItem key={option.key} value={option.value}>
                            {option.value}
                        </MenuItem>
                    ))}
                </TextField>
                <div hidden={securitySchema !== securitySchemas[1].value}>
                    <TextField
                        id='security-authType-select'
                        select
                        label={<FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.auth.type'
                            defaultMessage='Auth Type'
                        />}
                        className={classes.textField}
                        value={authType}
                        onChange={onSelectAuthType}
                        SelectProps={{
                            MenuProps: {
                                className: classes.menu,
                            },
                        }}
                        helperText={
                            <FormattedMessage
                                id='Apis.Details.EndpointsNew.AdvancedConfig
                                .EndpointSecurity.select.auth.type.of.endpoint'
                                defaultMessage='Select the auth type of the endpoint'
                            />
                        }
                        margin='normal'
                    >
                        {authTypes.map(type => (
                            <MenuItem key={type.key} value={type.value}>
                                {type.value}
                            </MenuItem>
                        ))}
                    </TextField>
                    <div className={classes.credentialsContainer}>
                        <TextField
                            required
                            id='auth-userName'
                            label={<FormattedMessage
                                id='Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.user.name.input'
                                defaultMessage='User Name'
                            />}
                            placeholder={
                                <FormattedMessage
                                    id='Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.user.name.placeholder'
                                    defaultMessage='User Name'
                                />}
                            className={classes.textField}
                            margin='normal'
                        />
                        <TextField
                            required
                            type='password'
                            id='auth-password'
                            label={<FormattedMessage
                                id='Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.password.input'
                                defaultMessage='Password'
                            />}
                            placeholder={
                                <FormattedMessage
                                    id='Apis.Details.EndpointsNew.AdvancedConfig.EndpointSecurity.password.placeholder'
                                    defaultMessage='Password'
                                />}
                            className={classes.textField}
                            margin='normal'
                        />
                    </div>
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
};

export default injectIntl(withStyles(styles)(EndpointSecurity));
