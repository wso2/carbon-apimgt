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

import React, { useState } from 'react';
import { FormattedMessage } from 'react-intl';
import Joi from '@hapi/joi';
import { Box, Grid } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import ChangePasswordBase from './ChangePasswordBase';
import AuthManager from 'AppData/AuthManager';
import Settings from 'Settings';

const useStyles = makeStyles((theme) => ({
    mandatoryStarText: {
        '& label>span:nth-child(1)': {
            color: 'red',
        },
    },
    passwordChangeForm: {
        '& span, & div, & p, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        }
    }
}));

const ChangePassword = () => {
    const classes = useStyles();
    const username = AuthManager.getUser().name;
    const [oldPassword, setOldPassword] = useState();
    const [newPassword, setNewPassword] = useState();
    const [repeatedNewPassword, setRepeatedNewPassword] = useState();
    const passwordChangeGuideEnabled = false || Settings.passwordChange.guidelinesEnabled;
    let passwordChangeGuide = [];
    if (passwordChangeGuideEnabled) {
        passwordChangeGuide = Settings.passwordChange.guidelines;
    }

    const validateOldPasswordChange = () => {
        if (oldPassword === '') {
            return true;
        } else {
            return false;
        }
    };

    const validatePasswordChange = () => {
        // todo: update password validation policy
        const schema = Joi.string().empty();
        const validationError = schema.validate(newPassword).error;
        if (validationError) {
            const errorType = validationError.details[0].type;
            if (errorType === 'string.empty') {
                return (
                    < FormattedMessage
                        id='Change.Password.password.empty'
                        defaultMessage='Password is empty'
                    />
                );
            }
        }
        return false;
    };

    const validateRepeatedPassword = () => {
        if (repeatedNewPassword && newPassword !== repeatedNewPassword) {
            return (
                < FormattedMessage
                    id='Change.Password.password.mismatch'
                    defaultMessage={'Password doesn\'t match'}
                />);
        }
    };

    // todo: move to reducers
    const handleChange = ({ target: { name: field, value } }) => {
        switch (field) {
            case 'oldPassword':
                setOldPassword(value);
                break;
            case 'newPassword':
                setNewPassword(value);
                break;
            case 'repeatedNewPassword':
                setRepeatedNewPassword(value);
                break;
            default:
                break;
        }
    };

    const handleSave = () => {
        alert(oldPassword + ' ' + newPassword + ' ' + repeatedNewPassword);
    };

    const title = (
        <>
            <Typography variant='h5'>
                <FormattedMessage
                    id='Change.Password.title'
                    defaultMessage='Change Password'
                />{': ' + username}
            </Typography>
            <Typography variant='caption'>
                <FormattedMessage
                    id='Change.Password.description'
                    defaultMessage={'Change your own password.'
                        + ' Required fields are marked with an asterisk ( * )'}
                />
            </Typography>
            {passwordChangeGuide.length > 0 ?
                <Typography variant='body2'>
                    <FormattedMessage
                        id='Change.Password.password.policy'
                        defaultMessage={'Password policy:'}
                    />
                    <ul style={{ marginTop: '-4px', marginBottom: '-8px' }}>
                        {passwordChangeGuide.map(rule => {
                            return (
                                <li>
                                    Rule {rule}
                                </li>
                            );
                        })}
                    </ul>
                </Typography> :
                null
            }
        </>
    )

    return (
        <ChangePasswordBase title={title}>
            <Box py={2} display='flex' justifyContent='center'>
                <Grid item xs={10} md={9}>
                    {/* replace with mui component */}
                    <form noValidate autoComplete='off' className={classes.passwordChangeForm}>
                        <Box component='div' m={2}>
                            <Grid container
                                mt={2}
                                spacing={2}
                                direction="column"
                                justify="center"
                                alignItems="flex-start">
                                <TextField
                                    classes={{
                                        root: classes.mandatoryStarText,
                                    }}
                                    required
                                    autoFocus
                                    margin='dense'
                                    name='oldPassword'
                                    value={oldPassword}
                                    onChange={handleChange}
                                    label={<FormattedMessage id='Settings.ChangePasswordForm.old.password' defaultMessage='Old Password' />}
                                    fullWidth
                                    error={validateOldPasswordChange()}
                                    helperText={<FormattedMessage id='Settings.ChangePasswordForm.enter.old.password' defaultMessage='Enter Old Password' />}
                                    variant='outlined'
                                    type='password'
                                />
                                <TextField
                                    classes={{
                                        root: classes.mandatoryStarText,
                                    }}
                                    margin='dense'
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
                    </form>
                </Grid>
            </Box>
        </ChangePasswordBase>
    );
};

export default ChangePassword;
