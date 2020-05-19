/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, {
    useEffect, useState, useContext,
} from 'react';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import withStyles from '@material-ui/core/styles/withStyles';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import {
    Radio, RadioGroup, FormControlLabel, FormControl, CircularProgress, Tooltip,
} from '@material-ui/core';
import HelpOutline from '@material-ui/icons/HelpOutline';
import IconButton from '@material-ui/core/IconButton';
import InputAdornment from '@material-ui/core/InputAdornment';
import Icon from '@material-ui/core/Icon';
import AuthManager from 'AppData/AuthManager';
import MenuItem from '@material-ui/core/MenuItem';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import { ApiContext } from '../ApiContext';
import Progress from '../../../Shared/Progress';
import Api from '../../../../data/api';
import Application from '../../../../data/Application';
import SelectAppPanel from './SelectAppPanel';

/**
 * @inheritdoc
 * @param {*} theme theme
 */
const styles = makeStyles((theme) => ({
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
    tryoutHeading: {
        fontWeight: 400,
    },
    genKeyButton: {
        width: theme.spacing(20),
        height: theme.spacing(5),
        marginTop: theme.spacing(2.5),
        marginLeft: theme.spacing(2),
    },
    gatewayEnvironment: {
        marginTop: theme.spacing(4),
    },
    categoryHeading: {
        marginBottom: theme.spacing(2),
        marginLeft: theme.spacing(-5),
    },
    tooltip: {
        marginLeft: theme.spacing(1),
    },
    menuItem: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
}));

/**
 * TryOut component
 *
 * @class TryOutController
 * @extends {Component}
 */
function TryOutController(props) {
    const {
        securitySchemeType, selectedEnvironment, environments, labels,
        productionAccessToken, sandboxAccessToken, selectedKeyType, setKeys, setSelectedKeyType,
        setSelectedEnvironment, setProductionAccessToken, setSandboxAccessToken, scopes,
        setSecurityScheme, setUsername, setPassword, username, password,
        setProductionApiKey, setSandboxApiKey, productionApiKey, sandboxApiKey, environmentObject, setURLs,
    } = props;
    const classes = styles();

    const [showToken, setShowToken] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [notFound, setNotFound] = useState(false);
    const [subscriptions, setSubscriptions] = useState([]);
    const [selectedApplication, setSelectedApplication] = useState([]);
    const { api } = useContext(ApiContext);
    const apiID = api.id;
    const restApi = new Api();

    useEffect(() => {
        let selectedEnvironments;
        let subscriptionsList;
        let selectedApplicationList;
        let keys;
        let selectedKeyTypes = 'PRODUCTION';
        let accessToken;
        const promiseSubscriptions = restApi.getSubscriptions(apiID);
        promiseSubscriptions.then((subscriptionsResponse) => {
            if (subscriptionsResponse !== null) {
                subscriptionsList = subscriptionsResponse.obj.list.filter((item) => item.status === 'UNBLOCKED'
                || item.status === 'PROD_ONLY_BLOCKED');

                if (subscriptionsList && subscriptionsList.length > 0) {
                    selectedApplicationList = subscriptionsList[0].applicationId;
                    Application.get(selectedApplicationList)
                        .then((application) => {
                            return application.getKeys();
                        })
                        .then((appKeys) => {
                            if (appKeys.get('SANDBOX')) {
                                selectedKeyTypes = 'SANDBOX';
                                ({ accessToken } = appKeys.get('SANDBOX').token);
                            } else if (appKeys.get('PRODUCTION')) {
                                selectedKeyTypes = 'PRODUCTION';
                                ({ accessToken } = appKeys.get('PRODUCTION').token);
                            }
                            setSelectedApplication(selectedApplicationList);
                            setSubscriptions(subscriptionsList);
                            setKeys(appKeys);
                            setSelectedEnvironment(selectedEnvironments, false);
                            setSelectedKeyType(selectedKeyTypes, false);
                            if (selectedKeyType === 'PRODUCTION') {
                                setProductionAccessToken(accessToken);
                            } else {
                                setSandboxAccessToken(accessToken);
                            }
                        });
                } else {
                    setSelectedApplication(selectedApplicationList);
                    setSubscriptions(subscriptionsList);
                    setKeys(keys);
                    setSelectedEnvironment(selectedEnvironment, false);
                    if (selectedKeyType === 'PRODUCTION') {
                        setProductionAccessToken(accessToken);
                    } else {
                        setSandboxAccessToken(accessToken);
                    }
                    setSelectedKeyType(selectedKeyType, false);
                }
            } else {
                setSelectedApplication(selectedApplicationList);
                setSubscriptions(subscriptionsList);
                setKeys(keys);
                setSelectedEnvironment(selectedEnvironment, false);
                if (selectedKeyType === 'PRODUCTION') {
                    setProductionAccessToken(accessToken);
                } else {
                    setSandboxAccessToken(accessToken);
                }
                setSelectedKeyType(selectedKeyType, false);
            }
        }).catch((error) => {
            if (process.env.NODE_ENV !== 'production') {
                console.error(error);
            }
            const { status } = error;
            if (status === 404) {
                setNotFound(true);
            }
        });
    }, []);


    /**
     * Generate access token
     * */
    function generateAccessToken() {
        setIsUpdating(true);
        const applicationPromise = Application.get(selectedApplication);
        applicationPromise
            .then((application) => application.generateToken(
                selectedKeyType,
                3600,
                scopes,
            ))
            .then((response) => {
                console.log('token generated successfully ' + response);
                setShowToken(false);
                if (selectedKeyType === 'PRODUCTION') {
                    setProductionAccessToken(response.accessToken);
                } else {
                    setSandboxAccessToken(response.accessToken);
                }
                setIsUpdating(false);
            })
            .catch((error) => {
                console.error(error);
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                }
                setIsUpdating(false);
            });
    }

    /**
     * Generate api key
     * */
    function generateApiKey() {
        setIsUpdating(true);
        const promisedKey = restApi.generateApiKey(selectedApplication, selectedKeyType, -1);
        promisedKey
            .then((response) => {
                console.log('Non empty response received', response);
                setShowToken(false);
                if (selectedKeyType === 'PRODUCTION') {
                    setProductionApiKey(response.body.apikey);
                } else {
                    setSandboxApiKey(response.body.apikey);
                }
                setIsUpdating(false);
            })
            .catch((error) => {
                console.log(error);
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                }
                setIsUpdating(false);
            });
    }

    /**
     *
     * Handle onClick of shown access token
     * @memberof TryOutController
     */
    function handleClickShowToken() {
        setShowToken(true);
    }

    /**
     * Load the selected application information
     * @memberof TryOutController
     */
    function updateApplication() {
        let accessToken;
        let keyType;
        if (subscriptions !== null && subscriptions.length !== 0 && selectedApplication.length !== 0) {
            if (subscriptions.find((sub) => sub.applicationId
            === selectedApplication).status === 'PROD_ONLY_BLOCKED') {
                setSelectedKeyType(selectedKeyType, false);
                keyType = 'SANDBOX';
            } else {
                keyType = selectedKeyType;
            }
        }
        Application.get(selectedApplication)
            .then((application) => {
                return application.getKeys();
            })
            .then((appKeys) => {
                if (appKeys.get(keyType)) {
                    ({ accessToken } = appKeys.get(keyType).token);
                }
                if (appKeys.get(keyType) === 'PRODUCTION') {
                    setProductionAccessToken(accessToken);
                } else {
                    setSandboxAccessToken(accessToken);
                }
                setKeys(appKeys);
            });
    }

    useEffect(() => {
        updateApplication();
    }, [selectedApplication]);

    /**
     * Handle onChange of inputs
     * @param {*} event event
     * @memberof TryOutController
     */
    function handleChanges(event) {
        const { target } = event;
        const { name, value } = target;
        switch (name) {
            case 'selectedEnvironment':
                setSelectedEnvironment(value, true);
                if (environmentObject) {
                    const urls = environmentObject.find((elm) => value === elm.environmentName).URLs;
                    setURLs(urls);
                }
                break;
            case 'selectedApplication':
                setProductionAccessToken('');
                setSandboxAccessToken('');
                setProductionApiKey('');
                setSandboxApiKey('');
                setSelectedApplication(value);
                break;
            case 'selectedKeyType':
                if (!productionAccessToken || !sandboxAccessToken) {
                    setSelectedKeyType(value, true);
                } else {
                    setSelectedKeyType(value, false);
                }
                break;
            case 'securityScheme':
                setSecurityScheme(value);
                break;
            case 'username':
                setUsername(value);
                break;
            case 'password':
                setPassword(value);
                break;
            case 'accessToken':
                if (securitySchemeType === 'API-KEY' && selectedKeyType === 'PRODUCTION') {
                    setProductionApiKey(value);
                } else if (securitySchemeType === 'API-KEY' && selectedKeyType === 'SANDBOX') {
                    setSandboxApiKey(value);
                } else if (selectedKeyType === 'PRODUCTION') {
                    setProductionAccessToken(value);
                } else {
                    setSandboxAccessToken(value);
                }
                break;
            default:
        }
    }

    const user = AuthManager.getUser();
    if (api == null) {
        return <Progress />;
    }
    if (notFound) {
        return 'API Not found !';
    }
    let isApiKeyEnabled = false;
    let isBasicAuthEnabled = false;
    let isOAuthEnabled = false;
    let authorizationHeader = api.authorizationHeader ? api.authorizationHeader : 'Authorization';
    let prefix = 'Bearer';
    if (api && api.securityScheme) {
        isApiKeyEnabled = api.securityScheme.includes('api_key');
        isBasicAuthEnabled = api.securityScheme.includes('basic_auth');
        isOAuthEnabled = api.securityScheme.includes('oauth2');
        if (isApiKeyEnabled && securitySchemeType === 'API-KEY') {
            authorizationHeader = 'apikey';
            prefix = '';
        }
    }
    const isPrototypedAPI = api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped';

    let tokenValue = '';
    if (securitySchemeType === 'API-KEY') {
        tokenValue = selectedKeyType === 'PRODUCTION' ? productionApiKey : sandboxApiKey;
    } else {
        tokenValue = selectedKeyType === 'PRODUCTION' ? productionAccessToken : sandboxAccessToken;
    }

    return (
        <>
            <Grid x={12} md={6} className={classes.centerItems}>
                <Box>
                    <Typography variant='h5' color='textPrimary' className={classes.categoryHeading}>
                        <FormattedMessage
                            id='api.console.security.heading'
                            defaultMessage='Security'
                        />
                    </Typography>
                    <Typography variant='h6' color='textSecondary' className={classes.tryoutHeading}>
                        <FormattedMessage
                            id='api.console.security.type.heading'
                            defaultMessage='Security Type'
                        />
                    </Typography>
                    {(isApiKeyEnabled || isBasicAuthEnabled || isOAuthEnabled) && (
                        <FormControl component='fieldset'>
                            <RadioGroup
                                name='securityScheme'
                                value={securitySchemeType}
                                onChange={handleChanges}
                                row
                            >
                                {isOAuthEnabled && (
                                    <FormControlLabel
                                        value='OAUTH'
                                        control={<Radio />}
                                        label={(
                                            <FormattedMessage
                                                id='Apis.Details.ApiConsole.security.scheme.oauth'
                                                defaultMessage='OAuth'
                                            />
                                        )}
                                    />
                                )}
                                {isApiKeyEnabled && (
                                    <FormControlLabel
                                        value='API-KEY'
                                        control={<Radio />}
                                        label={(
                                            <FormattedMessage
                                                id='Apis.Details.ApiConsole.security.scheme.apikey'
                                                defaultMessage='API Key'
                                            />
                                        )}
                                    />
                                )}
                                {isBasicAuthEnabled && (
                                    <FormControlLabel
                                        value='BASIC'
                                        control={<Radio />}
                                        label={(
                                            <FormattedMessage
                                                id='Apis.Details.ApiConsole.security.scheme.basic'
                                                defaultMessage='Basic'
                                            />
                                        )}
                                    />
                                )}
                            </RadioGroup>
                        </FormControl>
                    )}
                </Box>
            </Grid>
            {!isPrototypedAPI
                    && (
                        <Grid xs={12} md={12} item>
                            <Box display='block'>
                                {user && subscriptions
                                && subscriptions.length > 0 && securitySchemeType !== 'BASIC' && (
                                    <SelectAppPanel
                                        subscriptions={subscriptions}
                                        handleChanges={handleChanges}
                                        selectedApplication={selectedApplication}
                                        selectedKeyType={selectedKeyType}
                                    />
                                )}
                                {subscriptions && subscriptions.length === 0 && (
                                    <Box display='flex' justifyContent='center'>
                                        <Typography variant='body1' gutterBottom>
                                            <FormattedMessage
                                                id='Apis.Details.ApiConsole.ApiConsole.subscribe.to.application'
                                                defaultMessage='Please subscribe to an application'
                                            />
                                        </Typography>
                                    </Box>
                                )}
                                <Box display='block' justifyContent='center'>
                                    <Grid x={8} md={6} className={classes.tokenType} item>
                                        {securitySchemeType === 'BASIC' ? (
                                            <>
                                                <Grid x={12} md={12} item>
                                                    <TextField
                                                        margin='normal'
                                                        variant='outlined'
                                                        label={(
                                                            <FormattedMessage
                                                                id='username'
                                                                defaultMessage='Username'
                                                            />
                                                        )}
                                                        name='username'
                                                        onChange={handleChanges}
                                                        value={username || ''}
                                                        fullWidth
                                                    />
                                                    <TextField
                                                        margin='normal'
                                                        variant='outlined'
                                                        label={(
                                                            <FormattedMessage
                                                                id='password'
                                                                defaultMessage='Password'
                                                            />
                                                        )}
                                                        name='password'
                                                        onChange={handleChanges}
                                                        value={password || ''}
                                                        fullWidth
                                                    />
                                                </Grid>
                                            </>
                                        ) : (
                                            <TextField
                                                fullWidth
                                                margin='normal'
                                                variant='outlined'
                                                label={(
                                                    <FormattedMessage
                                                        id='access.token'
                                                        sdefaultMessage='Access Token'
                                                    />
                                                )}
                                                name='accessToken'
                                                onChange={handleChanges}
                                                type={showToken ? 'text' : 'password'}
                                                value={tokenValue || ''}
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
                                        )}
                                        {securitySchemeType !== 'BASIC' && (
                                            <>
                                                <Button
                                                    onClick={securitySchemeType === 'API-KEY' ? generateApiKey
                                                        : generateAccessToken}
                                                    color='secondary'
                                                    variant='outlined'
                                                    className={classes.genKeyButton}
                                                    disabled={!user || (subscriptions && subscriptions.length === 0)}
                                                >
                                                    {isUpdating && (
                                                        <CircularProgress size={15} />
                                                    )}
                                                    <FormattedMessage
                                                        id='Apis.Details.ApiCOnsole.generate.test.key'
                                                        defaultMessage='GET TEST KEY '
                                                    />
                                                </Button>
                                                <Tooltip
                                                    placement='right'
                                                    title={(
                                                        <FormattedMessage
                                                            id='Apis.Details.TryOutConsole.access.token.tooltip'
                                                            defaultMessage={
                                                                'You can use your existing Access Token or '
                                                + 'you can generate a new Test Key.'
                                                            }
                                                        />
                                                    )}
                                                >
                                                    <HelpOutline className={classes.tooltip} />
                                                </Tooltip>
                                            </>
                                        )}
                                    </Grid>
                                </Box>
                                <Box display='flex' justifyContent='center' className={classes.gatewayEnvironment}>
                                    <Grid xs={12} md={6} item>
                                        {((environments && environments.length > 0)
                                        || (labels && labels.length > 0))
                                    && (
                                        <>
                                            <Typography
                                                variant='h5'
                                                color='textPrimary'
                                                className={classes.categoryHeading}
                                            >
                                                <FormattedMessage
                                                    id='api.console.gateway.heading'
                                                    defaultMessage='Gateway'
                                                />
                                            </Typography>
                                            <TextField
                                                fullWidth
                                                select
                                                label={(
                                                    <FormattedMessage
                                                        defaultMessage='Environment'
                                                        id='Apis.Details.ApiConsole.environment'
                                                    />
                                                )}
                                                value={selectedEnvironment || (environments && environments[0])}
                                                name='selectedEnvironment'
                                                onChange={handleChanges}
                                                helperText={(
                                                    <FormattedMessage
                                                        defaultMessage='Please select an environment'
                                                        id='Apis.Details.ApiConsole.SelectAppPanel.environment'
                                                    />
                                                )}
                                                margin='normal'
                                                variant='outlined'
                                            >
                                                {environments && environments.length > 0 && (
                                                    <MenuItem value='' disabled className={classes.menuItem}>
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
                                                        <MenuItem
                                                            value={env}
                                                            key={env}
                                                            className={classes.menuItem}
                                                        >
                                                            {env}
                                                        </MenuItem>
                                                    )))}
                                                {labels && labels.length > 0 && (
                                                    <MenuItem value='' disabled>
                                                        <em>
                                                            <FormattedMessage
                                                                id='micro.gateways'
                                                                defaultMessage='Microgateways'
                                                                className={classes.menuItem}
                                                            />
                                                        </em>
                                                    </MenuItem>
                                                )}
                                                {labels && (
                                                    labels.map((label) => (
                                                        <MenuItem
                                                            value={label}
                                                            key={label}
                                                            className={classes.menuItem}
                                                        >
                                                            {label}
                                                        </MenuItem>
                                                    ))
                                                )}
                                            </TextField>
                                        </>
                                    )}

                                    </Grid>
                                </Box>
                            </Box>
                        </Grid>
                    )}
        </>
    );
}

TryOutController.propTypes = {
    classes: PropTypes.shape({
        paper: PropTypes.string.isRequired,
        grid: PropTypes.string.isRequired,
        inputAdornmentStart: PropTypes.string.isRequired,
        centerItems: PropTypes.string.isRequired,
    }).isRequired,
};

export default withStyles(makeStyles)(TryOutController);
