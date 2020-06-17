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
import { FormattedMessage, injectIntl } from 'react-intl';
import Joi from '@hapi/joi';
import { Box, Grid } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import Container from '@material-ui/core/Container';
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    headingWrapper: {
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        paddingLeft: theme.spacing(),
        '& span, & h5, & label, & input, & td, & li': {
            color: theme.palette.getContrastText(theme.palette.background.default),
        },
    },
    applicationForm: {
        '& span, & div, & p, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        }
    }
}));

const SettingsBase = (props) => {
    const classes = useStyles();
    const { username } = props.match.params;
    const [oldPassword, setOldPassword] = useState();
    const [newPassword, setNewPassword] = useState();
    const [repeatedNewPassword, setRepeatedNewPassword] = useState();

    const validateOldPasswordChange = () => {
        if (oldPassword === '') {
            return true;
        } else {
            return false;
        }
    };

    const validatePasswordChange = (pwd) => {
        const schema = Joi.string().pattern(new RegExp('^[a-zA-Z0-9]{3,30}$')).empty();
        const validationError = schema.validate(pwd).error;
        if (validationError) {
            const errorType = validationError.details[0].type;
            if (errorType === 'any.empty') {
                return 'Password is empty';
            }
            if (errorType === 'string.pattern.base') {
                return 'Invalid password';
            }
        }
        return false;
    };

    const validateRepeatedPassword = () => {
        if (repeatedNewPassword && newPassword !== repeatedNewPassword) {
            return 'Password doesn\'t match';
        }
    };

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
        console.log('TAG', oldPassword, newPassword, repeatedNewPassword);

    };

    return (
        <Box width={1} display='flex' justifyContent='center'>
            <Box width={0.5} display='flex' alignItems='center' m={5}>
                <Grid justify="center" container spacing={5}>
                    <Grid item md={12} className={classes.createTitle}>
                        <Typography variant='h5'>
                            <FormattedMessage
                                id='Change.Password.title'
                                defaultMessage='Change Password'
                            />{': ' + username}
                        </Typography>
                        <Typography variant='caption'>
                            <FormattedMessage
                                id='Change.Password.description'
                                defaultMessage='Change your own password. Password should be length of 3-30, no special characters'
                            />
                        </Typography>
                    </Grid>
                    <Grid item md={12} className={classes.applicationContent}>
                        <Paper elevation={0}>
                            <Container fixed>
                                <form noValidate autoComplete='off' className={classes.applicationForm}>
                                    <Box component='div' m={2}>
                                        <Grid container
                                            mt={2}
                                            spacing={2}
                                            direction="column"
                                            justify="flex-start"
                                            alignItems="flex-start">
                                            <TextField
                                                autoFocus
                                                margin='dense'
                                                name='oldPassword'
                                                value={oldPassword}
                                                onChange={handleChange}
                                                label={(
                                                    <span>
                                                        <FormattedMessage id='Settings.ChangePasswordForm.old.password' defaultMessage='Old Password' />
                                                        <span className={classes.error}>*</span>
                                                    </span>
                                                )}
                                                fullWidth
                                                error={validateOldPasswordChange(oldPassword)}
                                                helperText={'Enter Old Password'}
                                                variant='outlined'
                                                type='password'
                                            />
                                            <TextField
                                                margin='dense'
                                                name='newPassword'
                                                value={newPassword}
                                                onChange={handleChange}
                                                label={(
                                                    <span>
                                                        <FormattedMessage id='Settings.ChangePasswordForm.new.password' defaultMessage='New Password' />
                                                        <span className={classes.error}>*</span>
                                                    </span>
                                                )}
                                                fullWidth
                                                error={validatePasswordChange(newPassword)}
                                                helperText={validatePasswordChange(newPassword) || 'Enter a New Password'}
                                                variant='outlined'
                                                type='password'
                                            />
                                            <TextField
                                                margin='dense'
                                                name='repeatedNewPassword'
                                                value={repeatedNewPassword}
                                                onChange={handleChange}
                                                label={(
                                                    <span>
                                                        <FormattedMessage id='Settings.ChangePasswordForm.confirm.new.password' defaultMessage='Confirm new Password' />
                                                        <span className={classes.error}>*</span>
                                                    </span>
                                                )}
                                                fullWidth
                                                error={validateRepeatedPassword(repeatedNewPassword)}
                                                helperText={validateRepeatedPassword(repeatedNewPassword) || 'Confirmation of new Password'}
                                                variant='outlined'
                                                type='password'
                                            />

                                            <Box m={2} display='flex' flexDirection='row'>
                                                <Box mx={1}>
                                                    <Button
                                                        color='primary'
                                                        variant='contained'
                                                        onClick={handleSave}
                                                    >
                                                        Save
                                                    </Button>
                                                </Box>

                                                <Box mx={1}>
                                                    <Button
                                                        onClick={() => window.history.back()}
                                                    >
                                                        Cancel
                                                    </Button>
                                                </Box>
                                            </Box>
                                        </Grid>
                                    </Box>
                                </form>
                            </Container>
                        </Paper>
                    </Grid>
                </Grid>
            </Box>
        </Box>
    );
};

SettingsBase.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default injectIntl(SettingsBase);
