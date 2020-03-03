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
import React, { useState, useEffect } from 'react';
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
import SelectAppPanel from '../ApiConsole/SelectAppPanel';
import Application from '../../../../data/Application';
import Api from '../../../../data/api';

const useStyles = makeStyles((theme) => ({
    paper: {
        margin: theme.spacing(1),
        padding: theme.spacing(1),
        height: theme.spacing(100),
    },
    titleSub: {
        marginLeft: theme.spacing(2),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    root: {
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
    centerItems: {
        margin: 'auto',
    },
    inputAdornmentStart: {
        minWidth: theme.spacing(18),
    },
}));


/**
 *
 * @param {*} props
 */
export default function GraphQLAuthentication(props) {
    const classes = useStyles();

    const {
        api,
        accessToken,
        setAccessTocken,
        authorizationHeader,
        securitySchemeType,
        setSecuritySchemeType,
        prefix,
        isApiKeyEnabled,
        selectedEnvironment,
        setSelectedEnvironment,
        environments,
        setURLss,
        environmentObject,
        setFound,
    } = props;

    const user = AuthManager.getUser();
    const [showToken, setShowToken] = useState(false);
    const [subscriptions, setSubscriptions] = useState(null);
    const [selectedApplication, setSelectedApplication] = useState();
    const [selectedKeyType, setSelectedKeyType] = useState('PRODUCTION');
    const [keys, setKeys] = useState();
    const isPrototypedAPI = api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped';

    useEffect(() => {
        const apiID = api.id;
        const apiClient = new Api();
        const promiseGraphQL = apiClient.getGraphQLSchemaByAPIId(apiID);

        promiseGraphQL
            .then(() => {
                if (user != null) {
                    return apiClient.getSubscriptions(apiID);
                } else {
                    return null;
                }
            })
            .then((subscriptionsResponse) => {
                if (subscriptionsResponse != null) { //
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
                                    const sKType1 = 'SANDBOX';
                                    setSelectedKeyType(sKType1);
                                } else if (appKeys.get('PRODUCTION')) {
                                    const sKType2 = 'PRODUCTION';
                                    setSelectedKeyType(sKType2);
                                }
                                setKeys(appKeys);
                            });
                    }
                    setSubscriptions(subs);
                }
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
    }, []);

    /**
     *
     * @param {React.SyntheticEvent} event
     */
    const handleChanges = (event) => {
        if (event.target.name === 'selectedApplication') {
            const promiseApp = Application.get(event.target.value);
            let keyType;

            setSelectedApplication(event.target.value);

            if (subscriptions != null && subscriptions.find((sub) => sub.applicationId
                === selectedApplication).status === 'PROD_ONLY_BLOCKED') {
                const sKType1 = 'SANDBOX';
                setSelectedKeyType(sKType1);
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
            setSelectedKeyType(event.target.value);

            if (keys.get(event.target.value)) {
                const { accessToken: accessTokenValue } = keys.get(event.target.value).token;
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
        setSelectedEnvironment(event.target.value);
        const urls = environmentObject.find((elm) => event.target.value === elm.environmentName).URLs;
        setURLss(urls);
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
        setAccessTocken(event.target.value);
    };

    /**
     * Set the security scheme type for selected security scheme type
     * @param {*} event
     */
    const handlesecuritySchemeType = (event) => {
        setSecuritySchemeType(event.target.value);
    };


    return (
        <>
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
                                    id='api.console.require.access.token'
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
                            <Box display='block' />
                            {user && subscriptions != null && subscriptions.length > 0 && (
                                <SelectAppPanel
                                    selectedApplication={selectedApplication}
                                    selectedKeyType={selectedKeyType}
                                    handleChanges={handleChanges}
                                    subscriptions={subscriptions}
                                />
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
                                                        id='Apis.Details.GraphQLConsole.GraphQLAuthentication.env'
                                                    />
                                                )}
                                                value={selectedEnvironment}
                                                name='selectedEnvironment'
                                                onChange={handleEnvironemtChange}
                                                helperText={(
                                                    <FormattedMessage
                                                        defaultMessage='Please select an environment'
                                                        id='Apis.Details.GraphQLConsole.SelectAppPanel.select.an.env'
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
                                <Grid x={12} md={6} className={classes.centerItems} item>
                                    <TextField
                                        fullWidth
                                        margin='normal'
                                        variant='outlined'
                                        label={<FormattedMessage id='access.token' defaultMessage='Access Token' />}
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
                                        }}
                                    />
                                </Grid>
                                <Grid x={12} md={6} className={classes.centerItems}>
                                    {isApiKeyEnabled && (
                                        <FormControl component='fieldset'>
                                            <RadioGroup
                                                name='securityScheme'
                                                value={securitySchemeType}
                                                onChange={handlesecuritySchemeType}
                                                row
                                            >
                                                <FormControlLabel
                                                    value='OAUTH'
                                                    control={<Radio />}
                                                    label='OAUTH'
                                                />
                                                <FormControlLabel
                                                    value='API-KEY'
                                                    control={<Radio />}
                                                    label='API-KEY'
                                                />
                                            </RadioGroup>
                                        </FormControl>
                                    )}
                                </Grid>
                            </Box>
                        </Grid>
                    )}
            </Grid>
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
