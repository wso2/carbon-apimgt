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
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { Radio, RadioGroup, FormControlLabel, FormControl } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import InputAdornment from '@material-ui/core/InputAdornment';
import Icon from '@material-ui/core/Icon';
import AuthManager from 'AppData/AuthManager';
import Paper from '@material-ui/core/Paper';
import MenuItem from '@material-ui/core/MenuItem';
import Button from '@material-ui/core/Button';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import Box from '@material-ui/core/Box';
import { ApiContext } from '../ApiContext';
import Progress from '../../../Shared/Progress';
import Api from '../../../../data/api';
import SwaggerUI from './SwaggerUI';
import Application from '../../../../data/Application';
import SelectAppPanel from './SelectAppPanel';
/**
 * @inheritdoc
 * @param {*} theme theme
 */
const styles = theme => ({
    buttonIcon: {
        marginRight: 10,
    },
    centerItems: {
        margin: 'auto',
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
});
/**
 *
 *
 * @class ApiConsole
 * @extends {React.Component}
 */
class ApiConsole extends React.Component {
    static contextType = ApiContext;

    /**
     *Creates an instance of ApiConsole.
     * @param {*} props properties
     * @memberof ApiConsole
     */
    constructor(props) {
        super(props);
        this.state = {
            showToken: false,
            securitySchemeType: 'OAUTH',
        };
        this.handleChanges = this.handleChanges.bind(this);
        this.accessTokenProvider = this.accessTokenProvider.bind(this);
        this.handleClickShowToken = this.handleClickShowToken.bind(this);
        this.updateSwagger = this.updateSwagger.bind(this);
        this.updateAccessToken = this.updateAccessToken.bind(this);
        this.updateApplication = this.updateApplication.bind(this);
    }

    /**
     * @memberof ApiConsole
     */
    componentDidMount() {
        const { api } = this.context;
        const apiID = api.id;
        const user = AuthManager.getUser();
        let apiData;
        let environments;
        let labels;
        let selectedEnvironment;
        let swagger;
        let subscriptions;
        let selectedApplication;
        let keys;
        let selectedKeyType = 'PRODUCTION';
        let accessToken;

        this.apiClient = new Api();
        const promiseAPI = this.apiClient.getAPIById(apiID);

        promiseAPI
            .then((apiResponse) => {
                apiData = apiResponse.obj;
                if (apiData.endpointURLs) {
                    environments = apiData.endpointURLs.map((endpoint) => { return endpoint.environmentName; });
                }
                if (apiData.labels) {
                    labels = apiData.labels.map((label) => { return label.name; });
                }
                if (environments && environments.length > 0) {
                    [selectedEnvironment] = environments;
                    return this.apiClient.getSwaggerByAPIIdAndEnvironment(apiID, selectedEnvironment);
                } else if (labels && labels.length > 0) {
                    [selectedEnvironment] = labels;
                    return this.apiClient.getSwaggerByAPIIdAndLabel(apiID, selectedEnvironment);
                } else {
                    return this.apiClient.getSwaggerByAPIId(apiID);
                }
            })
            .then((swaggerResponse) => {
                swagger = swaggerResponse.obj;
                if (user != null) {
                    return this.apiClient.getSubscriptions(apiID);
                } else {
                    return null;
                }
            })
            .then((subscriptionsResponse) => {
                if (subscriptionsResponse != null) {
                    subscriptions = subscriptionsResponse.obj.list.filter(item => item.status === 'UNBLOCKED'
                    || item.status === 'PROD_ONLY_BLOCKED');

                    if (subscriptions && subscriptions.length > 0) {
                        selectedApplication = subscriptions[0].applicationId;
                        const promiseApp = Application.get(selectedApplication);

                        promiseApp
                            .then((application) => {
                                return application.getKeys();
                            })
                            .then((appKeys) => {
                                if (appKeys.get('SANDBOX')) {
                                    selectedKeyType = 'SANDBOX';
                                    ({ accessToken } = appKeys.get('SANDBOX').token);
                                } else if (appKeys.get('PRODUCTION')) {
                                    selectedKeyType = 'PRODUCTION';
                                    ({ accessToken } = appKeys.get('PRODUCTION').token);
                                }

                                this.setState({
                                    api: apiData,
                                    swagger,
                                    subscriptions,
                                    environments,
                                    labels,
                                    selectedEnvironment,
                                    selectedApplication,
                                    keys: appKeys,
                                    selectedKeyType,
                                    accessToken,
                                });
                            });
                    } else {
                        this.setState({
                            api: apiData,
                            swagger,
                            subscriptions,
                            environments,
                            labels,
                            selectedEnvironment,
                            selectedApplication,
                            keys,
                            selectedKeyType,
                            accessToken,
                        });
                    }
                } else {
                    this.setState({
                        api: apiData,
                        swagger,
                        subscriptions,
                        environments,
                        labels,
                        selectedEnvironment,
                        selectedApplication,
                        keys,
                        selectedKeyType,
                        accessToken,
                    });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     * Handle onClick of shown access token
     * @memberof ApiConsole
     */
    handleClickShowToken() {
        const { showToken } = this.state;
        this.setState({ showToken: !showToken });
    }

    /**
     *
     * Provids the access token to the Swagger UI
     * @returns {*} access token
     * @memberof ApiConsole
     */
    accessTokenProvider() {
        const { accessToken } = this.state;
        return accessToken;
    }

    /**
     * Handle onChange of inputs
     * @param {*} event event
     * @memberof ApiConsole
     */
    handleChanges(event) {
        const { target } = event;
        const { name, value } = target;
        switch (name) {
            case 'selectedEnvironment':
                this.setState({ [name]: value }, this.updateSwagger);
                break;
            case 'selectedApplication':
                this.setState({ [name]: value }, this.updateApplication);
                break;
            case 'selectedKeyType':
                this.setState({ [name]: value }, this.updateAccessToken);
                break;
            case 'securityScheme':
                this.setState({ securitySchemeType: value });
                break;
            default:
                this.setState({ [name]: value });
        }
    }

    /**
     * Load the swagger file of the selected environemnt
     * @memberof ApiConsole
     */
    updateSwagger() {
        const {
            selectedEnvironment, api, environments,
        } = this.state;
        let promiseSwagger;

        if (selectedEnvironment) {
            if (environments.includes(selectedEnvironment)) {
                promiseSwagger = this.apiClient.getSwaggerByAPIIdAndEnvironment(api.id, selectedEnvironment);
            } else {
                promiseSwagger = this.apiClient.getSwaggerByAPIIdAndLabel(api.id, selectedEnvironment);
            }
        } else {
            promiseSwagger = this.apiClient.getSwaggerByAPIId(api.id);
        }
        promiseSwagger.then((swaggerResponse) => {
            this.setState({ swagger: swaggerResponse.obj });
        });
    }

    /**
     * Load the access token for given key type
     * @memberof ApiConsole
     */
    updateAccessToken() {
        const { keys, selectedKeyType } = this.state;
        let accessToken;

        if (keys.get(selectedKeyType)) {
            ({ accessToken } = keys.get(selectedKeyType).token);
        }
        this.setState({ accessToken });
    }

    /**
     * Load the selected application information
     * @memberof ApiConsole
     */
    updateApplication() {
        const { selectedApplication, selectedKeyType, subscriptions } = this.state;
        const promiseApp = Application.get(selectedApplication);
        let accessToken;
        let keyType;

        if (subscriptions != null && subscriptions.find(sub => sub.applicationId
            === selectedApplication).status === 'PROD_ONLY_BLOCKED') {
            this.setState({ selectedKeyType: 'SANDBOX' });
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
                    ({ accessToken } = appKeys.get(keyType).token);
                }
                this.setState({ accessToken, keys: appKeys });
            });
    }

    /**
     * @inheritdoc
     * @memberof ApiConsole
     */
    render() {
        const { classes } = this.props;
        const {
            api, notFound, swagger, accessToken, showToken, subscriptions, selectedApplication, selectedKeyType,
            selectedEnvironment, environments, labels, securitySchemeType,
        } = this.state;
        const user = AuthManager.getUser();
        const downloadSwagger = JSON.stringify({ ...swagger });
        const downloadLink = 'data:text/json;charset=utf-8, ' + encodeURIComponent(downloadSwagger);
        const fileName = 'swagger.json';

        if (api == null || swagger == null) {
            return <Progress />;
        }
        if (notFound) {
            return 'API Not found !';
        }
        let isApiKeyEnabled = false;
        let authorizationHeader = api.authorizationHeader ? api.authorizationHeader : 'Authorization';
        let prefix = 'Bearer';
        if (api && api.securityScheme) {
            isApiKeyEnabled = api.securityScheme.includes('api_key');
            if (isApiKeyEnabled && securitySchemeType === 'API-KEY') {
                authorizationHeader = 'apikey';
                prefix = '';
            }
        }
        const isPrototypedAPI = api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped';

        return (
            <React.Fragment>
                <Typography variant='h4' className={classes.titleSub}>
                    <FormattedMessage id='Apis.Details.ApiConsole.ApiConsole.title' defaultMessage='Try Out' />
                </Typography>
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
                                            id='api.console.require.access.token'
                                            defaultMessage={'You need an access token to try the API. Please log '
                                            + 'in and subscribe to the API to generate an access token. If you already '
                                            + 'have an access token, please provide it below.'}
                                        />
                                    </Typography>
                                </Paper>
                            </Grid>
                        )}
                        {!isPrototypedAPI &&
                        <Grid xs={12} md={12} item>
                            <Box display='block'>
                                {user && subscriptions && subscriptions.length > 0 && (
                                    <SelectAppPanel
                                        subscriptions={subscriptions}
                                        handleChanges={this.handleChanges}
                                        selectedApplication={selectedApplication}
                                        selectedKeyType={selectedKeyType}
                                        // selectedEnvironment={selectedEnvironment}
                                        // environments={environments}
                                    />
                                )}
                                {subscriptions && subscriptions.length === 0 && (
                                    <Box display='flex' justifyContent='center'>
                                        <Typography variant='body1' gutterBottom>
                                            <FormattedMessage
                                                id='Apis.Details.ApiConsole.ApiConsole.please.subscribe.to.application'
                                                defaultMessage='Please subscribe to an application'
                                            />
                                        </Typography>
                                    </Box>

                                )}
                                <Box display='flex' justifyContent='center'>
                                    <Grid xs={12} md={6} item>
                                        {((environments && environments.length > 0) || (labels && labels.length > 0))
                                        && (
                                            <TextField
                                                fullWidth
                                                select
                                                label={<FormattedMessage
                                                    defaultMessage='Environment'
                                                    id='Apis.Details.ApiConsole.environment'
                                                />}
                                                value={selectedEnvironment}
                                                name='selectedEnvironment'
                                                onChange={this.handleChanges}
                                                helperText={<FormattedMessage
                                                    defaultMessage='Please select an environment'
                                                    id='Apis.Details.ApiConsole.SelectAppPanel.select.an.environment'
                                                />}
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
                                                    environments.map(env => (
                                                        <MenuItem value={env} key={env}>
                                                            {env}
                                                        </MenuItem>
                                                    )))}
                                                {labels && labels.length > 0 && (
                                                    <MenuItem value='' disabled>
                                                        <em>
                                                            <FormattedMessage
                                                                id='micro.gateways'
                                                                defaultMessage='Microgateways'
                                                            />
                                                        </em>
                                                    </MenuItem>
                                                )}
                                                {labels && (
                                                    labels.map(label => (
                                                        <MenuItem value={label} key={label}>
                                                            {label}
                                                        </MenuItem>
                                                    ))
                                                )}
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
                                            onChange={this.handleChanges}
                                            type={showToken ? 'text' : 'password'}
                                            value={accessToken || ''}
                                            helperText={
                                                <FormattedMessage
                                                    id='enter.access.token'
                                                    defaultMessage='Enter access Token'
                                                />}
                                            InputProps={{
                                                endAdornment: (
                                                    <InputAdornment position='end'>
                                                        <IconButton
                                                            edge='end'
                                                            aria-label='Toggle token visibility'
                                                            onClick={this.handleClickShowToken}
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
                                            <FormControl component='fieldset' >
                                                <RadioGroup
                                                    name='securityScheme'
                                                    value={securitySchemeType}
                                                    onChange={this.handleChanges}
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
                            </Box>
                        </Grid>
                        }

                        <Grid container>
                            <Grid xs={10} item />
                            <Grid xs={2} item>
                                <a href={downloadLink} download={fileName}>
                                    <Button size='small'>
                                        <CloudDownloadRounded className={classes.buttonIcon} />
                                        <FormattedMessage
                                            id='Apis.Details.APIConsole.APIConsole.download.swagger'
                                            defaultMessage='Swagger ( /swagger.json )'
                                        />
                                    </Button>
                                </a>
                            </Grid>
                        </Grid>
                    </Grid>
                </Paper>
                <Paper className={classes.paper}>
                    <SwaggerUI
                        api={this.state.api}
                        accessTokenProvider={this.accessTokenProvider}
                        spec={swagger}
                        authorizationHeader={authorizationHeader}
                    />
                </Paper>
            </React.Fragment>
        );
    }
}

ApiConsole.propTypes = {
    classes: PropTypes.shape({
        paper: PropTypes.string.isRequired,
        titleSub: PropTypes.string.isRequired,
        grid: PropTypes.string.isRequired,
        userNotificationPaper: PropTypes.string.isRequired,
        inputAdornmentStart: PropTypes.string.isRequired,
        buttonIcon: PropTypes.string.isRequired,
        centerItems: PropTypes.string.isRequired,
    }).isRequired,
};

export default withStyles(styles)(ApiConsole);
