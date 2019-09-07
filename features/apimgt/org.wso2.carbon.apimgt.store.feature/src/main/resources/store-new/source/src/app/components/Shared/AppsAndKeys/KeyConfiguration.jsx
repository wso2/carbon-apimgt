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

import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { FormattedMessage, injectIntl } from 'react-intl';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';

const styles = theme => ({
    FormControl: {
        padding: theme.spacing.unit * 2,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing.unit * 2,
        width: '100%',
    },
    button: {
        marginLeft: theme.spacing.unit * 1,
    },
    quotaHelp: {
        position: 'relative',
    },
    checkboxWrapper: {
        display: 'flex',
    },
    checkboxWrapperColumn: {
        display: 'flex',
        flexDirection: 'row',
    },
    group: {
        flexDirection: 'row',
    },
});
/**
 *
 *
 * @class KeyConfiguration
 * @extends {React.Component}
 */
class KeyConfiguration extends React.Component {
    /**
     * This method is used to handle the updating of key generation
     * request object.
     * @param {*} field field that should be updated in key request
     * @param {*} event event fired
     */
    handleChange(field, event) {
        const { keyRequest, updateKeyRequest } = this.props;
        const newRequest = { ...keyRequest };
        const { target: currentTarget } = event;
        let newGrantTypes = [...newRequest.supportedGrantTypes];

        switch (field) {
            case 'callbackUrl':
                newRequest.callbackUrl = currentTarget.value;
                break;
            case 'grantType':
                if (currentTarget.checked) {
                    newGrantTypes = [...newGrantTypes, currentTarget.id];
                } else {
                    newGrantTypes = newRequest.supportedGrantTypes.filter(item => item !== currentTarget.id);
                }
                newRequest.supportedGrantTypes = newGrantTypes;
                break;
            default:
                break;
        }
        updateKeyRequest(newRequest);
    }

    /**
     *
     *
     * @returns {Component}
     * @memberof KeyConfiguration
     */
    render() {
        const {
            classes, keyRequest, notFound, intl, isUserOwner,
        } = this.props;
        const { supportedGrantTypes, callbackUrl } = keyRequest;
        if (notFound) {
            return <ResourceNotFound />;
        }

        let isRefreshChecked = false;
        let isPasswordChecked = false;
        let isImplicitChecked = false;
        let isCodeChecked = false;
        if (supportedGrantTypes) {
            isRefreshChecked = supportedGrantTypes.includes('refresh_token');
            isPasswordChecked = supportedGrantTypes.includes('password');
            isImplicitChecked = supportedGrantTypes.includes('implicit');
            isCodeChecked = supportedGrantTypes.includes('authorization_code');
        }

        return (
            <React.Fragment>
                <FormControl className={classes.FormControl} component='fieldset'>
                    <InputLabel shrink htmlFor='age-label-placeholder' className={classes.quotaHelp}>
                        <FormattedMessage id='grant.types' defaultMessage='Grant Types' />
                    </InputLabel>
                    <div className={classes.checkboxWrapper}>
                        <div className={classes.checkboxWrapperColumn}>
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        id='refresh_token'
                                        checked={isRefreshChecked}
                                        onChange={e => this.handleChange('grantType', e)}
                                        value='refresh_token'
                                        disabled={!isUserOwner}
                                    />
                                )}
                                label={intl.formatMessage({
                                    defaultMessage: 'Refresh Token',
                                    id: 'Shared.AppsAndKeys.KeyConfiguration.refresh.token',
                                })}
                            />
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        id='password'
                                        checked={isPasswordChecked}
                                        value='password'
                                        onChange={e => this.handleChange('grantType', e)}
                                        disabled={!isUserOwner}
                                    />
                                )}
                                label='Password'
                            />
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        id='implicit'
                                        checked={isImplicitChecked}
                                        value='implicit'
                                        onChange={e => this.handleChange('grantType', e)}
                                        disabled={!isUserOwner}
                                    />
                                )}
                                label={intl.formatMessage({
                                    defaultMessage: 'Implicit',
                                    id: 'Shared.AppsAndKeys.KeyConfiguration.implicit',
                                })}
                            />
                        </div>
                        <div className={classes.checkboxWrapperColumn}>
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        id='authorization_code'
                                        checked={isCodeChecked}
                                        value='authorization_code'
                                        onChange={e => this.handleChange('grantType', e)}
                                        disabled={!isUserOwner}
                                    />
                                )}
                                label={intl.formatMessage({
                                    defaultMessage: 'Code',
                                    id: 'Shared.AppsAndKeys.KeyConfiguration.code',
                                })}
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox id='client_credentials' checked disabled value='client_credentials' />
                                }
                                label={intl.formatMessage({
                                    defaultMessage: 'Client Credential',
                                    id: 'Shared.AppsAndKeys.KeyConfiguration.code',
                                })}
                            />
                        </div>
                    </div>
                    <FormHelperText>
                        <FormattedMessage
                            defaultMessage={`The application can use the following grant types to generate 
                            Access Tokens. Based on the application requirement,you can enable or disable 
                            grant types for this application.`}
                            id='Shared.AppsAndKeys.KeyConfiguration.the.application.can'
                        />
                    </FormHelperText>
                </FormControl>

                {
                    <FormControl className={classes.FormControlOdd}>
                        <TextField
                            id='callbackURL'
                            fullWidth
                            onChange={e => this.handleChange('callbackUrl', e)}
                            label='Callback URL'
                            placeholder='http://url-to-webapp'
                            className={classes.textField}
                            margin='normal'
                            value={callbackUrl}
                            disabled={!isUserOwner}
                        />
                        <FormHelperText>
                            <FormattedMessage
                                defaultMessage={`Callback URL is a redirection URI in the client
                                application which is used by the authorization server to send the
                                client's user-agent (usually web browser) back after granting access.`}
                                id='Shared.AppsAndKeys.KeyConfiguration.callback.url'
                            />
                        </FormHelperText>
                    </FormControl>
                }
            </React.Fragment>
        );
    }
}

export default injectIntl(withStyles(styles)(KeyConfiguration));
