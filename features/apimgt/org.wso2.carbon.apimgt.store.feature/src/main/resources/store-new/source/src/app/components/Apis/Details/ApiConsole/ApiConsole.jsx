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
import { withStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import InputAdornment from '@material-ui/core/InputAdornment';
import Icon from '@material-ui/core/Icon';
import AuthManager from 'AppData/AuthManager';
import Paper from '@material-ui/core/Paper';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import Select from '@material-ui/core/Select';
import APIProduct from 'AppData/APIProduct';
import CONSTS from 'AppData/Constants';
import { ApiContext } from '../ApiContext';
import Progress from '../../../Shared/Progress';
import Api from '../../../../data/api';
import SwaggerUI from './SwaggerUI';
import Application from '../../../../data/Application';

/**
 * @inheritdoc
 * @param {*} theme theme
 */
const styles = theme => ({
    inputAdornmentStart: {
        width: '100%',
    },
    inputText: {
        marginLeft: theme.spacing.unit * 5,
        minWidth: theme.spacing.unit * 50,
    },
    grid: {
        spacing: 20,
        marginTop: theme.spacing.unit * 4,
        marginBottom: theme.spacing.unit * 4,
        paddingLeft: theme.spacing.unit * 11,
        paddingRight: theme.spacing.unit * 2,
    },
    userNotificationPaper: {
        padding: theme.spacing.unit * 2,
    },
    formControl: {
        display: 'flex',
        flexDirection: 'row',
        paddingRight: theme.spacing.unit * 2,
    },
    gridWrapper: {
        paddingTop: theme.spacing.unit * 2,
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
        this.state = { showToken: false };
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
        const { apiType, api } = this.context;
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
        let selectedKeyType;
        let accessToken;

        if (apiType === CONSTS.API_PRODUCT_TYPE) {
            this.apiClient = new APIProduct();
        } else if (apiType === CONSTS.API_TYPE) {
            this.apiClient = new Api();
        }

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
                    subscriptions = subscriptionsResponse.obj.list.filter(item => item.status === 'UNBLOCKED');

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
        const { selectedApplication, selectedKeyType } = this.state;
        const promiseApp = Application.get(selectedApplication);
        let accessToken;

        promiseApp
            .then((application) => {
                return application.getKeys();
            })
            .then((appKeys) => {
                if (appKeys.get(selectedKeyType)) {
                    ({ accessToken } = appKeys.get(selectedKeyType).token);
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
            selectedEnvironment, environments, labels,
        } = this.state;
        const user = AuthManager.getUser();

        if (api == null || swagger == null) {
            return <Progress />;
        }
        if (notFound) {
            return 'API Not found !';
        }

        return (
            <React.Fragment>
                <Grid container className={classes.grid}>
                    {!user && (
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
                                        defaultMessage={'You require an access token to try the API. Please log '
                                        + 'in and subscribe to the API to generate an access token. If you already '
                                        + 'have an access token, please provide it below.'}
                                    />
                                </Typography>
                            </Paper>
                        </Grid>
                    )}
                    {user != null && subscriptions && (
                        <Grid container>
                            <Grid item md={4} xs={4} className={classes.gridWrapper}>
                                <FormControl className={classes.formControl} disabled={subscriptions.length === 0}>
                                    <InputLabel htmlFor='application-selection'>
                                        <FormattedMessage id='applications' defaultMessage='Applications' />
                                    </InputLabel>
                                    <Select
                                        name='selectedApplication'
                                        value={selectedApplication}
                                        onChange={this.handleChanges}
                                        input={<Input name='subscription' id='application-selection' />}
                                        fullWidth
                                    >
                                        {subscriptions.map(sub => (
                                            <MenuItem value={sub.applicationInfo.applicationId}>
                                                {sub.applicationInfo.name}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                                { subscriptions.length === 0
                                && (
                                    <FormHelperText>
                                        <FormattedMessage
                                            id='require.application.subscribe'
                                            defaultMessage='Please subscribe to an application'
                                        />
                                    </FormHelperText>
                                )
                                }
                            </Grid>
                            <Grid item md={4} xs={4} className={classes.gridWrapper}>
                                <FormControl className={classes.formControl} disabled={subscriptions.length === 0}>
                                    <InputLabel htmlFor='key-type-selection'>
                                        <FormattedMessage id='key' defaultMessage='Key' />
                                    </InputLabel>
                                    <Select
                                        name='selectedKeyType'
                                        value={selectedKeyType}
                                        onChange={this.handleChanges}
                                        input={<Input name='subscription' id='key-type-selection' />}
                                        fullWidth
                                    >
                                        <MenuItem value='PRODUCTION'>
                                            PRODUCTION
                                        </MenuItem>
                                        <MenuItem value='SANDBOX'>
                                            SANDBOX
                                        </MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                        </Grid>
                    )}
                    {((environments && environments.length > 0) || (labels && labels.length > 0)) && (
                        <Grid item md={8} xs={8} className={classes.gridWrapper}>
                            <FormControl className={classes.formControl}>
                                <InputLabel htmlFor='environment-selection'>
                                    <FormattedMessage id='environment' defaultMessage='Environment' />
                                </InputLabel>
                                <Select
                                    name='selectedEnvironment'
                                    value={selectedEnvironment}
                                    onChange={this.handleChanges}
                                    input={<Input name='environment' id='environment-selection' />}
                                    fullWidth
                                >
                                    {environments && environments.length > 0 && (
                                        <MenuItem value='' disabled>
                                            <em>
                                                <FormattedMessage id='api.gateways' defaultMessage='API Gateways' />
                                            </em>
                                        </MenuItem>
                                    )}
                                    {environments && (
                                        environments.map(env => (
                                            <MenuItem value={env}>
                                                {env}
                                            </MenuItem>
                                        )))}
                                    {labels && labels.length > 0 && (
                                        <MenuItem value='' disabled>
                                            <em>
                                                <FormattedMessage id='micro.gateways' defaultMessage='Micro Gateways' />
                                            </em>
                                        </MenuItem>
                                    )}
                                    {labels && (
                                        labels.map(label => (
                                            <MenuItem value={label}>
                                                {label}
                                            </MenuItem>
                                        ))
                                    )}
                                </Select>
                            </FormControl>
                        </Grid>
                    )}
                    <Grid container md={9} xs={8} justify='center'>
                        <Grid item md={9} xs={8} className={classes.gridWrapper}>
                            <TextField
                                margin='normal'
                                variant='outlined'
                                className={classes.inputText}
                                label={<FormattedMessage id='access.token' defaultMessage='Access Token' />}
                                name='accessToken'
                                onChange={this.handleChanges}
                                type={showToken ? 'text' : 'password'}
                                value={accessToken || ''}
                                helperText={
                                    <FormattedMessage id='enter.access.token' defaultMessage='Enter access Token' />}
                                InputProps={{
                                    endAdornment: (
                                        <InputAdornment position='end'>
                                            <IconButton
                                                edge='end'
                                                aria-label='Toggle token visibility'
                                                onClick={this.handleClickShowToken}
                                            >
                                                {showToken ? <Icon>visibility_off</Icon> : <Icon>visibility</Icon>}
                                            </IconButton>
                                        </InputAdornment>
                                    ),
                                    startAdornment: (
                                        <InputAdornment className={classes.inputAdornmentStart} position='start'>
                                            {api.authorizationHeader ? api.authorizationHeader : 'Authorization'}
                                            {' '}
                                            :Bearer
                                        </InputAdornment>
                                    ),
                                }}
                            />
                        </Grid>
                    </Grid>
                </Grid>
                <SwaggerUI accessTokenProvider={this.accessTokenProvider} spec={swagger} />
            </React.Fragment>
        );
    }
}

ApiConsole.defaultProps = {
    // handleInputs: false,
};

ApiConsole.propTypes = {
    // handleInputs: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ApiConsole);
