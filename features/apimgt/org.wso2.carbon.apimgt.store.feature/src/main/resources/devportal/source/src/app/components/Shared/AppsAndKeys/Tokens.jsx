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
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import Typography from '@material-ui/core/Typography';
import Chip from '@material-ui/core/Chip';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';

// Styles for Grid and Paper elements
const styles = theme => ({
    FormControl: {
        padding: theme.spacing(2),
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing(2),
        backgroundColor: theme.palette.background.paper,
        width: '100%',
    },
    quotaHelp: {
        position: 'relative',
    },
    chips: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    chip: {
        margin: theme.spacing(0.25),
    },
});

const MenuProps = {
    PaperProps: {
        style: {
            maxHeight: 224,
            width: 250,
        },
    },
};

/**
 * Used to display generate acctoken UI
 */
const tokens = (props) => {
    /**
    * This method is used to handle the updating of create application
    * request object.
    * @param {*} field field that should be updated in appliction request
    * @param {*} event event fired
    */
    const handleChange = (field, event) => {
        const { accessTokenRequest, updateAccessTokenRequest } = props;
        const newRequest = { ...accessTokenRequest };

        const { target: currentTarget } = event;

        switch (field) {
            case 'timeout':
                newRequest.timeout = currentTarget.value;
                break;
            case 'scopesSelected':
                newRequest.scopesSelected = currentTarget.value;
                break;
            case 'keyType':
                newRequest.keyType = currentTarget.value;
                break;
            default:
                break;
        }
        updateAccessTokenRequest(newRequest);
    };
    const {
        classes, intl, accessTokenRequest, subscriptionScopes,
    } = props;

    return (
        <React.Fragment>
            <FormControl margin='normal' className={classes.FormControl}>
                <TextField
                    required
                    label={intl.formatMessage({
                        defaultMessage: 'Access token validity period',
                        id: 'Shared.AppsAndKeys.Tokens.access.token',
                    })}
                    InputLabelProps={{
                        shrink: true,
                    }}
                    helperText={intl.formatMessage({
                        defaultMessage: 'You can set an expiration period to determine the validity period of '
                                + 'the token after generation. Set this to a negative value to ensure that the '
                                + 'token never expires.',
                        id: 'Shared.AppsAndKeys.Tokens.you.can.set',
                    })}
                    fullWidth
                    name='timeout'
                    onChange={e => handleChange('timeout', e)}
                    placeholder={intl.formatMessage({
                        defaultMessage: 'Enter time in milliseconds',
                        id: 'Shared.AppsAndKeys.Tokens.enter.time',
                    })}
                    value={accessTokenRequest.timeout}
                    autoFocus
                    className={classes.inputText}
                />
            </FormControl>
            <FormControl
                margin='normal'
                className={classes.FormControlOdd}
                disabled={subscriptionScopes.length === 0}
            >
                <InputLabel htmlFor='quota-helper' className={classes.quotaHelp}>
                    <FormattedMessage
                        id='Shared.AppsAndKeys.Tokens.when.you.generate.scopes'
                        defaultMessage='Scopes'
                    />

                </InputLabel>
                <Select
                    name='scopesSelected'
                    multiple
                    value={accessTokenRequest.scopesSelected}
                    onChange={e => handleChange('scopesSelected', e)}
                    input={<Input id='select-multiple-chip' />}
                    renderValue={selected => (
                        <div className={classes.chips}>
                            {selected.map(value => (
                                <Chip key={value} label={value} className={classes.chip} />
                            ))}
                        </div>
                    )}
                    MenuProps={MenuProps}
                >
                    {subscriptionScopes.map(scope => (
                        <MenuItem
                            key={scope}
                            value={scope}
                        >
                            {scope}
                        </MenuItem>
                    ))}
                </Select>
                <Typography variant='caption'>
                    <FormattedMessage
                        id='Shared.AppsAndKeys.Tokens.when.you.generate'
                        defaultMessage={'When you generate access tokens to APIs protected by scope/s,'
                            + ' you can select the scope/s and then generate the token for it. Scopes enable '
                            + 'fine-grained access control to API resources based on user roles. You define scopes to '
                            + 'an API resource. When a user invokes the API, his/her OAuth 2 bearer token cannot grant '
                            + 'access to any API resource beyond its associated scopes.'}
                    />
                </Typography>
            </FormControl>
        </React.Fragment>
    );
};
tokens.contextTypes = {
    intl: PropTypes.shape({}).isRequired,
};
export default injectIntl(withStyles(styles)(tokens));
