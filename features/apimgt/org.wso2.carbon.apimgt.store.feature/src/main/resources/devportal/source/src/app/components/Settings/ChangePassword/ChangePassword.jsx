/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useReducer } from 'react';
import { FormattedMessage } from 'react-intl';
import AuthManager from 'AppData/AuthManager';
import Settings from 'Settings';
import Joi from '@hapi/joi';
import { Box, Grid } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import ChangePasswordBase from './ChangePasswordBase';
import PageNotFound from 'AppComponents/Base/Errors/PageNotFound'
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import Progress from 'AppComponents/Shared/Progress';
import { useSettingsContext } from 'AppComponents/Shared/SettingsContext';

const useStyles = makeStyles((theme) => ({
    mandatoryStarText: {
        '& label>span:nth-child(1)': {
            color: 'red',
        },
    },
    passwordChangeForm: {
        '& > span, & div, & p, & input': {
            color: theme.palette.getContrastText(theme.palette.primary.main),
        },
    },
}));

/**
 * Reducer
 * @param {JSON} state State
 * @returns {Promise}.
 */
function reducer(state, { field, value }) {
    return {
        ...state,
        [field]: value,
    };
}

const ChangePassword = () => {
    const {
        settings: {
            IsPasswordChangeEnabled,
            userStorePasswordPattern,
            passwordPolicyPattern,
            passwordPolicyMinLength,
            passwordPolicyMaxLength,
        }
    } = useSettingsContext();
    const classes = useStyles();
    const username = AuthManager.getUser().name;
    const initialState = {
        currentPassword: undefined,
        newPassword: undefined,
        repeatedNewPassword: undefined,
    };
    const [state, dispatch] = useReducer(reducer, initialState);
    const { currentPassword, newPassword, repeatedNewPassword } = state;
    const passwordChangeGuideEnabled = false || Settings.passwordChange.guidelinesEnabled;
    let passwordChangeGuide = [];
    if (passwordChangeGuideEnabled) {
        passwordChangeGuide = Settings.passwordChange.policyList;
    }

    const validateCurrentPasswordChange = () => {
        if (currentPassword === '') {
            return true;
        } else {
            return false;
        }
    };

    const validatePasswordChange = () => {
        // Validate against min, max legths if available.
        // also check whether empty.
        let legthCheckSchema = Joi.string().empty();
        if (passwordPolicyMinLength && passwordPolicyMinLength !== -1) {
            legthCheckSchema = legthCheckSchema.min(passwordPolicyMinLength);
        }
        if (passwordPolicyMaxLength && passwordPolicyMaxLength !== -1) {
            legthCheckSchema = legthCheckSchema.max(passwordPolicyMaxLength);
        }
        const LengthValidationError = legthCheckSchema.validate(newPassword).error;
        if (LengthValidationError) {
            const errorType = LengthValidationError.details[0].type;
            if (errorType === 'string.empty') {
                return (
                    <FormattedMessage
                        id='Change.Password.password.empty'
                        defaultMessage='Password is empty'
                    />
                );
            } else if (errorType === 'string.min') {
                return (
                    <FormattedMessage
                        id='Change.Password.password.length.short'
                        defaultMessage='Password is too short!'
                    />
                );
            } else if (errorType === 'string.max') {
                return (
                    <FormattedMessage
                        id='Change.Password.password.length.long'
                        defaultMessage='Password is too long!'
                    />
                );
            }
        }

        // Validate against user store password pattern regex, if available.
        if (userStorePasswordPattern) {
            const userStoreSchema = Joi.string().pattern(new RegExp(userStorePasswordPattern));
            const userStoreValidationError = userStoreSchema.validate(newPassword).error;
            if (userStoreValidationError) {
                const errorType = userStoreValidationError.details[0].type;
                if (errorType === 'string.pattern.base') {
                    return (
                        <FormattedMessage
                            id='Change.Password.password.pattern.invalid'
                            defaultMessage='Invalid password pattern'
                        />
                    );
                }
            }
        }

        // Validate against password policy pattern regex, if available.
        if (passwordPolicyPattern) {
            const passwordPolicySchema = Joi.string().pattern(new RegExp(passwordPolicyPattern));
            const passwordPolicyValidationError = passwordPolicySchema.validate(newPassword).error;
            if (passwordPolicyValidationError) {
                const errorType = passwordPolicyValidationError.details[0].type;
                if (errorType === 'string.pattern.base') {
                    return (
                        <FormattedMessage
                            id='Change.Password.password.pattern.invalid'
                            defaultMessage='Invalid password pattern'
                        />
                    );
                }
            }
        }

        return false;
    };

    const validateRepeatedPassword = () => {
        if (repeatedNewPassword && newPassword !== repeatedNewPassword) {
            return (
                <FormattedMessage
                    id='Change.Password.password.mismatch'
                    defaultMessage={'Password doesn\'t match'}
                />
            );
        }
    };

    const handleChange = ({ target: { name: field, value } }) => {
        dispatch({ field, value });
    };

    const handleSave = () => {
        if (repeatedNewPassword && newPassword !== repeatedNewPassword) {
            Alert.error(
                <FormattedMessage
                    id='Change.Password.password.mismatch'
                    defaultMessage={'Password doesn\'t match'}
                />
            );
        } else {
            const restApi = new API();
            return restApi
                .changePassword(currentPassword, newPassword)
                .then((res) => {
                    Alert.success(
                        <FormattedMessage
                            id='Change.Password.password.changed.success'
                            defaultMessage='User password changed successfully. Please use the new password on next sign in'
                        />
                    );
                    window.history.back();
                })
                .catch((error) => {
                    const errorCode = error.response.body.code;
                    switch (errorCode) {
                        case 901450:
                            Alert.error(
                                <FormattedMessage
                                    id='Change.Password.password.change.disabled'
                                    defaultMessage='Password change disabled'
                                />
                            );
                            break;
                        case 901451:
                            Alert.error(
                                <FormattedMessage
                                    id='Change.Password.current.password.incorrect'
                                    defaultMessage='Current password is incorrect'
                                />
                            );
                            break;
                        case 901452:
                            Alert.error(
                                <FormattedMessage
                                    id='Change.Password.password.pattern.invalid'
                                    defaultMessage='Invalid password pattern'
                                />
                            );
                            break;
                    }
                });
        }
    };

    const title = (
        <>
            <Typography variant='h5' component='h1'>
                <FormattedMessage
                    id='Change.Password.title'
                    defaultMessage='Change Password'
                />
                {': '
                    + username}
            </Typography>
            <Typography variant='caption'>
                <FormattedMessage
                    id='Change.Password.description'
                    defaultMessage={'Change your own password.'
                        + ' Required fields are marked with an asterisk ( * )'}
                />
            </Typography>
            {passwordChangeGuideEnabled && passwordChangeGuide.length > 0
                ? (
                    <Typography variant='body2'>
                        <FormattedMessage
                            id='Change.Password.password.policy'
                            defaultMessage='Password policy:'
                        />
                        <ul style={{ marginTop: '-4px', marginBottom: '-8px' }}>
                            {passwordChangeGuide.map((rule) => {
                                return (
                                    <li>
                                        {rule}
                                    </li>
                                );
                            })}
                        </ul>
                    </Typography>
                )
                : null}
        </>
    );

    if (IsPasswordChangeEnabled === undefined) {
        return <Progress />;
    }

    // If the user is eligible to change the password, display password change form.
    // otherwise, display page not found.
    if (IsPasswordChangeEnabled) {
        return (
            <ChangePasswordBase title={title}>
                <Box py={2} display='flex' justifyContent='center'>
                    <Grid item xs={10} md={9}>
                        <Box component='div' m={2}>
                            <Grid
                                container
                                mt={2}
                                spacing={2}
                                direction='column'
                                justify='center'
                                alignItems='flex-start'
                            >
                                <TextField
                                    classes={{
                                        root: classes.mandatoryStarText,
                                    }}
                                    required
                                    id='current-password'
                                    autoFocus
                                    margin='dense'
                                    name='currentPassword'
                                    value={currentPassword}
                                    onChange={handleChange}
                                    label={<FormattedMessage id='Settings.ChangePasswordForm.current.password' defaultMessage='Current Password' />}
                                    fullWidth
                                    error={validateCurrentPasswordChange()}
                                    helperText={<FormattedMessage id='Settings.ChangePasswordForm.enter.current.password' defaultMessage='Enter Current Password' />}
                                    variant='outlined'
                                    type='password'
                                />
                                <TextField
                                    classes={{
                                        root: classes.mandatoryStarText,
                                    }}
                                    margin='dense'
                                    id='new-password'
                                    name='newPassword'
                                    value={newPassword}
                                    onChange={handleChange}
                                    label={
                                        <FormattedMessage id='Settings.ChangePasswordForm.new.password' defaultMessage='New Password' />
                                    }
                                    required
                                    fullWidth
                                    error={validatePasswordChange()}
                                    helperText={validatePasswordChange()
                                        || <FormattedMessage id='Settings.ChangePasswordForm.enter.new.password' defaultMessage='Enter a New Password' />}
                                    variant='outlined'
                                    type='password'
                                />
                                <TextField
                                    classes={{
                                        root: classes.mandatoryStarText,
                                    }}
                                    margin='dense'
                                    id='repeated-new-password'
                                    name='repeatedNewPassword'
                                    value={repeatedNewPassword}
                                    onChange={handleChange}
                                    label={
                                        <FormattedMessage id='Settings.ChangePasswordForm.confirm.new.password' defaultMessage='Confirm new Password' />
                                    }
                                    required
                                    fullWidth
                                    error={validateRepeatedPassword()}
                                    helperText={validateRepeatedPassword()
                                        || <FormattedMessage id='Settings.ChangePasswordForm.confirmationOf.new.password' defaultMessage='Confirmation of new Password' />}
                                    variant='outlined'
                                    type='password'
                                />

                                <Box my={2} display='flex' flexDirection='row'>
                                    <Box mr={1}>
                                        <Button
                                            color='primary'
                                            variant='contained'
                                            onClick={handleSave}
                                            className={classes.passwordChangeForm}
                                        >
                                            <FormattedMessage
                                                id='Settings.ChangePasswordForm.Save.Button.text'
                                                defaultMessage='Save'
                                            />
                                        </Button>
                                    </Box>
                                    <Box mx={1}>
                                        <Button
                                            onClick={() => window.history.back()}
                                        >
                                            <FormattedMessage
                                                id='Settings.ChangePasswordForm.Cancel.Button.text'
                                                defaultMessage='Cancel'
                                            />
                                        </Button>
                                    </Box>
                                </Box>
                            </Grid>
                        </Box>
                    </Grid>
                </Box>
            </ChangePasswordBase>
        );
    } else {
        return <PageNotFound />;
    }

};

export default ChangePassword;
