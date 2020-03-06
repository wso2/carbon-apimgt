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

import React, { useState, useEffect, useContext } from 'react';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import AuthManager from 'AppData/AuthManager';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Icon from '@material-ui/core/Icon';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import InputAdornment from '@material-ui/core/InputAdornment';
import IconButton from '@material-ui/core/IconButton';
import {
    Radio, RadioGroup, FormControlLabel, FormControl,
} from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import CircularProgress from '@material-ui/core/CircularProgress';
import SelectAppPanel from '../ApiConsole/SelectAppPanel';
import Application from '../../../../data/Application';
import { ApiContext } from '../ApiContext';
import Api from '../../../../data/api';

const useStyles = makeStyles((theme) => ({
    buttonIcon: {
        marginRight: 10,
    },
    centerItems: {
        margin: 'auto',
    },
    tokenType: {
        margin: 'auto',
        display: 'flex',
    },
    inputAdornmentStart: {
        minWidth: theme.spacing(18),
    },
    paper: {
        margin: theme.spacing(1),
        padding: theme.spacing(1),
    },
    grid: {
        marginTop: theme.spacing(4),
        marginBottom: theme.spacing(4),
        paddingRight: theme.spacing(2),
        justifyContent: 'center',
    },
    userNotificationPaper: {
        padding: theme.spacing(2),
    },
    titleSub: {
        marginLeft: theme.spacing(2),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    usernameField: {
        width: '100%',
    },
    passwordField: {
        width: '100%',
        marginLeft: theme.spacing(1),
    },
}));


/**
 *
 * @param {*} props
 */
export default function GraphQLAuthentication(props) {
    const classes = useStyles();

    const {
        accessToken,
        setAccessTocken,
        authorizationHeader,
        securitySchemeType,
        setSecuritySchemeType,
        prefix,
        isApiKeyEnabled,
        isBasicAuthEnabled,
        isOAuthEnabled,
        setURLs,
        environmentObject,
        setFound,
        username,
        setUserName,
        password,
        setPassword,
    } = props;

    const { api } = useContext(ApiContext);
    const user = AuthManager.getUser();
    const [showToken, setShowToken] = useState(false);
    const [subscriptions, setSubscriptions] = useState(null);
    const [selectedApplication, setSelectedApplication] = useState('');
    const [selectedKeyType, setSelectedKeyType] = useState('PRODUCTION');
    const environments = api.endpointURLs.map((endpoint) => endpoint.environmentName);
    const [selectedEnvironment, setSelectedEnvironment] = useState(environments[0]);
    const [keys, setKeys] = useState();
    const isPrototypedAPI = api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped';
    useEffect(() => {
        const apiID = api.id;
        const apiClient = new Api();

        if (user != null) {
            const promiseSubscription = apiClient.getSubscriptions(apiID);

            promiseSubscription
                .then((subscriptionsResponse) => {
                    const subs = subscriptionsResponse.obj.list.filter(
                        (item) => item.status === 'UNBLOCKED' || item.status === 'PROD_ONLY_BLOCKED',
                    );

                    if (subs && subs.length > 0) {
                        const sApplication = subs[0].applicationId;
                        setSelectedApplication(sApplication);
                        const promiseApp = Application.get(sApplication);
                        promiseApp
                            .then((application) => {
                                return application.getKeys();
                            })
                            .then((appKeys) => {
                                if (appKeys.get('SANDBOX')) {
                                    setSelectedKeyType('SANDBOX');
                                } else if (appKeys.get('PRODUCTION')) {
                                    setSelectedKeyType('PRODUCTION');
                                }
                                setKeys(appKeys);
                            });
                    }
                    setSubscriptions(subs);
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.error(error);
                    }
                    const { status } = error;
                    if (status === 404) {
                        setFound(true);
                    }
                });
        }
    }, []);

    /**
     *
     * @param {React.SyntheticEvent} event
     */
    const handleChanges = (event) => {
        const { value, name } = event.target;
        if (name === 'selectedApplication') {
            const promiseApp = Application.get(value);
            let keyType;

            setSelectedApplication(value);

            if (subscriptions != null && subscriptions.find((sub) => sub.applicationId
                === selectedApplication).status === 'PROD_ONLY_BLOCKED') {
                setSelectedKeyType('SANDBOX');
                keyType = 'SANDBOX';
            } else {
                keyType = selectedKeyType;
            }

            promiseApp
                .then((application) => {
                    return application.getKeys();
                })
                .then((appKeys) => {
                    if (appKeys.get(keyType)) {
                        const { accessToken: accessTokenValue } = appKeys.get(keyType).token;
                        setAccessTocken(accessTokenValue);
                    } else {
                        setAccessTocken('');
                    }
                    setKeys(appKeys);
                });
        } else {
            setSelectedKeyType(value);

            if (keys.get(value)) {
                const { accessToken: accessTokenValue } = keys.get(value).token;
                setAccessTocken(accessTokenValue);
            } else {
                setAccessTocken('');
            }
        }
    };


    /**
     * Set the environment and URLs for selected environment name
     * @param {*} event
     */
    const handleEnvironemtChange = (event) => {
        const { value } = event.target;
        setSelectedEnvironment(value);
        const urls = environmentObject.find((elm) => value === elm.environmentName).URLs;
        setURLs(urls);
    };


    /**
     * Handle onClick of shown access token
     */
    const handleClickShowToken = () => {
        setShowToken(!showToken);
    };


    /**
     * Load the access token for selected key type
     * @param {*} event
     */
    const handleaccessTockenChanges = (event) => {
        const { value } = event.target;
        setAccessTocken(value);
    };

    /**
     * Set the security scheme type for selected security scheme type
     * @param {*} event
     */
    const handlesecuritySchemeType = (event) => {
        const { value } = event.target;
        setSecuritySchemeType(value);
    };

    const handleUserName = (event) => {
        const { value } = event.target;
        setUserName(value);
    };

    const handlePassword = (event) => {
        const { value } = event.target;
        setPassword(value);
    };


    return (
        <>
            <Paper className={classes.paper}>
                <Grid container className={classes.grid}>
                    {!isPrototypedAPI && !user && (
                        <Grid item md={6}>
                            <Paper className={classes.userNotificationPaper}>
                                <Typography variant='h5' component='h3'>
                                    <Icon>warning</Icon>
                                    {' '}
                                    <FormattedMessage id='notice' defaultMessage='Notice' />
                                </Typography>
                                <Typography component='p'>
                                    <FormattedMessage
                                        id={'Apis.Details.GraphQLConsole.'
                                            + 'GraphQLAuthentication.require.access.token'}
                                        defaultMessage={'You need an access token to try the API. Please log '
                                            + 'in and subscribe to the API to generate an access token. If you already '
                                            + 'have an access token, please provide it below.'}
                                    />
                                </Typography>
                            </Paper>
                        </Grid>
                    )}
                    {!isPrototypedAPI
                        && (
                            <Grid xs={12} md={12} item>
                                <Box display='block'>
                                    {user && subscriptions && subscriptions.length > 0 && (
                                        <SelectAppPanel
                                            subscriptions={subscriptions}
                                            handleChanges={handleChanges}
                                            selectedApplication={selectedApplication}
                                            selectedKeyType={selectedKeyType}
                                        />
                                    )}
                                    {user && subscriptions === null && (
                                        <Box display='flex' justifyContent='center'>
                                            <CircularProgress size={35} />
                                        </Box>
                                    )}
                                    {subscriptions && subscriptions.length === 0 && (
                                        <Box display='flex' justifyContent='center'>
                                            <Typography variant='body1' gutterBottom>
                                                <FormattedMessage
                                                    id={'Apis.Details.GraphQLConsole.'
                                                        + 'GraphQLAuthentication.please.subscribe.to.application'}
                                                    defaultMessage='Please subscribe to an application'
                                                />
                                            </Typography>
                                        </Box>
                                    )}

                                    <Box display='flex' justifyContent='center'>
                                        <Grid xs={12} md={6} item>
                                            {(environments && environments.length > 0)
                                                && (
                                                    <TextField
                                                        fullWidth
                                                        select
                                                        label={(
                                                            <FormattedMessage
                                                                defaultMessage='Environment'
                                                                id={'Apis.Details.GraphQLConsole.'
                                                                    + 'GraphQLAuthentication.env'}
                                                            />
                                                        )}
                                                        value={selectedEnvironment}
                                                        name='selectedEnvironment'
                                                        onChange={handleEnvironemtChange}
                                                        helperText={(
                                                            <FormattedMessage
                                                                defaultMessage='Please select an environment'
                                                                id={'Apis.Details.GraphQLConsole.'
                                                                    + 'SelectAppPanel.select.env'}
                                                            />
                                                        )}
                                                        margin='normal'
                                                        variant='outlined'
                                                    >
                                                        {environments && environments.length > 0 && (
                                                            <MenuItem value='' disabled>
                                                                <em>
                                                                    <FormattedMessage
                                                                        id='api.gateways'
                                                                        defaultMessage='API Gateways'
                                                                    />
                                                                </em>
                                                            </MenuItem>
                                                        )}
                                                        {environments && (
                                                            environments.map((env) => (
                                                                <MenuItem value={env} key={env}>
                                                                    {env}
                                                                </MenuItem>
                                                            )))}
                                                    </TextField>
                                                )}
                                        </Grid>
                                    </Box>

                                    <Box display='block' justifyContent='center'>
                                        <Grid x={12} md={6} className={classes.tokenType} item>
                                            {securitySchemeType === 'BASIC' ? (
                                                <>
                                                    <TextField
                                                        margin='normal'
                                                        variant='outlined'
                                                        className={classes.usernameField}
                                                        label={
                                                            <FormattedMessage id='username' defaultMessage='Username' />
                                                        }
                                                        name='username'
                                                        onChange={handleUserName}
                                                        value={username || ''}

                                                    />
                                                    <TextField
                                                        margin='normal'
                                                        variant='outlined'
                                                        className={classes.passwordField}
                                                        label={
                                                            <FormattedMessage id='password' defaultMessage='Password' />
                                                        }
                                                        name='password'
                                                        onChange={handlePassword}
                                                        value={password || ''}

                                                    />
                                                </>
                                            ) : (
                                                <TextField
                                                    fullWidth
                                                    margin='normal'
                                                    variant='outlined'
                                                    label={(
                                                        <FormattedMessage
                                                            id='access.token'
                                                            defaultMessage='Access Token'
                                                        />
                                                    )}
                                                    name='accessToken'
                                                    onChange={handleaccessTockenChanges}
                                                    type={showToken ? 'text' : 'password'}
                                                    value={accessToken || ''}
                                                    helperText={(
                                                        <FormattedMessage
                                                            id='enter.access.token'
                                                            defaultMessage='Enter access Token'
                                                        />
                                                    )}
                                                    InputProps={{
                                                        endAdornment: (
                                                            <InputAdornment position='end'>
                                                                <IconButton
                                                                    edge='end'
                                                                    aria-label='Toggle token visibility'
                                                                    onClick={handleClickShowToken}
                                                                >
                                                                    {showToken ? <Icon>visibility_off</Icon>
                                                                        : <Icon>visibility</Icon>}
                                                                </IconButton>
                                                            </InputAdornment>
                                                        ),
                                                        startAdornment: (
                                                            <InputAdornment
                                                                className={classes.inputAdornmentStart}
                                                                position='start'
                                                            >
                                                                {`${authorizationHeader}: ${prefix}`}
                                                            </InputAdornment>
                                                        ),
                                                        // eslint-disable-next-line indent
                                                        }}
                                                />
                                            )}
                                        </Grid>
                                        <Grid x={12} md={6} className={classes.centerItems}>
                                            {(isApiKeyEnabled || isBasicAuthEnabled || isOAuthEnabled) && (
                                                <FormControl component='fieldset'>
                                                    <RadioGroup
                                                        name='securityScheme'
                                                        value={securitySchemeType}
                                                        onChange={handlesecuritySchemeType}
                                                        row
                                                    >
                                                        {isOAuthEnabled && (
                                                            <FormControlLabel
                                                                value='OAUTH'
                                                                control={<Radio />}
                                                                label='Referenced (OAuth)'
                                                            />
                                                        )}
                                                        {isBasicAuthEnabled && (
                                                            <FormControlLabel
                                                                value='BASIC'
                                                                control={<Radio />}
                                                                label='Basic'
                                                            />
                                                        )}
                                                        {isApiKeyEnabled && (
                                                            <FormControlLabel
                                                                value='API-KEY'
                                                                control={<Radio />}
                                                                label='API Key'
                                                            />
                                                        )}
                                                    </RadioGroup>
                                                </FormControl>
                                            )}
                                        </Grid>
                                    </Box>

                                </Box>
                            </Grid>
                        )}
                </Grid>
            </Paper>
        </>
    );
}


GraphQLAuthentication.propTypes = {
    classes: PropTypes.shape({
        paper: PropTypes.string.isRequired,
        titleSub: PropTypes.string.isRequired,
        grid: PropTypes.string.isRequired,
        userNotificationPaper: PropTypes.string.isRequired,
        root: PropTypes.string.isRequired,
        inputAdornmentStart: PropTypes.string.isRequired,
        centerItems: PropTypes.string.isRequired,
    }).isRequired,
};
