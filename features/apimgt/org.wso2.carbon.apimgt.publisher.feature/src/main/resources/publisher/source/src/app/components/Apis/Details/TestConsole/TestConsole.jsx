/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import 'AppComponents/Shared/testconsole.css';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import 'swagger-ui-react/swagger-ui.css';
import MenuItem from '@material-ui/core/MenuItem';
import Progress from 'AppComponents/Shared/Progress';
import Api from 'AppData/api';
import Grid from '@material-ui/core/Grid';
import InputAdornment from '@material-ui/core/InputAdornment';
import Box from '@material-ui/core/Box';
import Icon from '@material-ui/core/Icon';
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import ApiContext, { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import SwaggerUI from './SwaggerUI';

/**
 * @inheritdoc
 * @param {*} theme theme
 */
const styles = (theme) => ({
    centerItems: {
        margin: 'auto',
    },
    categoryHeading: {
        marginBottom: theme.spacing(2),
        marginLeft: theme.spacing(-5),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    buttonIcon: {
        marginRight: 10,
    },
    paper: {
        margin: theme.spacing(1),
        padding: theme.spacing(1),
        '& span, & h5, & label, & td, & li, & div, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
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
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    tryoutHeading: {
        fontWeight: 400,
    },
    noDataMessage: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#888888',
        width: '100%',
    },
    swaggerUIPaper: {
        showTryout: true,
        swaggerUIBackground: '#efefef',
        documentBackground: '#efefef',
        tokenTextBoxBackground: '#efefef',
    },
    stateButton: {
        marginRight: theme.spacing(),
    },
    head: {
        fontWeight: 200,
        marginBottom: 20,
    },
    emptyBox: {
        marginTop: theme.spacing(2),
    },
    content: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        marginLeft: theme.custom.leftMenuWidth,
        paddingBottom: theme.spacing(3),
    },
    actions: {
        padding: '20px 0',
        '& button': {
            marginLeft: 0,
        },
    },
    helpText: {
        paddingTop: theme.spacing(1),
    },
    messageBox: {
        marginTop: 20,
    },
    tokenType: {
        margin: 'auto',
        display: 'flex',
        '& .MuiButton-contained.Mui-disabled span.MuiButton-label': {
            color: '#999999',
        },
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
    tooltip: {
        marginLeft: theme.spacing(1),
    },
    menuItem: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    warningIcon: {
        color: '#ff9a00',
        fontSize: 25,
        marginRight: 10,
    },
});

/**
 * @class TestConsole
 * @extends {React.Component}
 */
class TestConsole extends React.Component {
    /**
     *
     * @param {*} props the props parameters
     */
    constructor(props) {
        super(props);
        this.state = {
            securitySchemeType: 'internalkey',
            showToken: false,
        };
        this.setSecurityScheme = this.setSecurityScheme.bind(this);
        this.setSelectedEnvironment = this.setSelectedEnvironment.bind(this);
        this.updateSwagger = this.updateSwagger.bind(this);
        this.generateKey = this.generateKey.bind(this);
        this.accessTokenProvider = this.accessTokenProvider.bind(this);
        this.handleChanges = this.handleChanges.bind(this);
        this.handleClickShowToken = this.handleClickShowToken.bind(this);
    }

    /**
     * @memberof ApiConsole
     */
    componentDidMount() {
        const { apiObj } = this.props;
        const restApi = new Api();
        let apiData;
        let environments;
        let selectedEnvironment;
        let swagger;
        let urls;
        const promisedAPI = restApi.getDeployedRevisions(apiObj.id);
        promisedAPI
            .then((apiResponse) => {
                environments = apiResponse.body.map((env) => { return env.name; });
                if (environments && environments.length > 0) {
                    [selectedEnvironment] = environments;
                    this.setState({
                        environments,
                        selectedEnvironment,

                    });
                }
                return Api.getAPIById(apiObj.id);
            })
            .then((apiResponse) => {
                apiData = apiResponse.obj;
                this.setState({
                    api: apiData,
                });
                if (environments && environments.length > 0) {
                    [selectedEnvironment] = environments;
                    return Api.getSwaggerByAPIIdAndEnvironment(apiResponse.obj.id, selectedEnvironment);
                } else {
                    return Api.getSwaggerByAPIId(apiResponse.obj.id);
                }
            })
            .then((swaggerResponse) => {
                swagger = swaggerResponse.obj;
                this.setState({
                    swagger,
                });
                return Api.getSettings();
            })
            .then((settingsObj) => {
                if (settingsObj.environment) {
                    urls = settingsObj.environment.map((envo) => {
                        const env = {
                            name: envo.name,
                            endpoints: {
                                http: envo.endpoints.http + apiData.context + '/' + apiData.version,
                                https: envo.endpoints.https + apiData.context + '/' + apiData.version,
                            },
                        };
                        return env;
                    });
                }
                this.setState({
                    settings: urls,
                    apiSettings: settingsObj,
                });
                this.setState({
                    swagger,
                });
            });
    }

    /**
     * Set SecurityScheme value
     * @memberof ApiConsole
     */
    setSecurityScheme(securityScheme) {
        this.setState({ securitySchemeType: securityScheme });
    }

    /**
     * Set Selected Environment
     * @memberof ApiConsole
     */
    setSelectedEnvironment(selectedEnvironment) {
        this.setState({ selectedEnvironment });
    }

    /**
     * Load the swagger file of the given environment
     * @memberof ApiConsole
     */
    updateSwagger(environment) {
        let urls;
        const {
            api,
        } = this.state;

        const promiseSwagger = Api.getSwaggerByAPIIdAndEnvironment(api.id, environment);
        const settingPromise = Api.getSettings();
        settingPromise
            .then((settingsNew) => {
                if (settingsNew.environment) {
                    urls = settingsNew.environment.map((envo) => {
                        const env = {
                            name: envo.name,
                            endpoints: {
                                http: envo.endpoints.http + api.context + '/' + api.version,
                                https: envo.endpoints.https + api.context + '/' + api.version,
                            },
                        };
                        return env;
                    });
                }
                this.setState({
                    settings: urls,
                });
            });
        promiseSwagger
            .then((swaggerResponse) => {
                this.setState({ swagger: swaggerResponse.obj });
            });
    }

    /**
    * Generate Internal-Token
    */
    generateKey() {
        let key;
        const {
            api,
        } = this.state;
        const promisedAPI = Api.generateInternalKey(api.id);
        promisedAPI
            .then((apiResponse) => {
                key = apiResponse.obj.apikey;
                this.setState({
                    key,
                    showToken: false,
                });
            });
        return key;
    }

    /**
     *
     * Handle onClick of shown access token
     * @memberof TryOutController
     */
    handleClickShowToken() {
        const {
            showToken,
        } = this.state;
        if (showToken) {
            this.setState({
                showToken: false,
            });
        } else {
            this.setState({
                showToken: true,
            });
        }
    }

    /**
     *
     * Provids the access token to the Swagger UI
     * @returns {*} access token
     * @memberof ApiConsole
     */
    accessTokenProvider() {
        const {
            securitySchemeType,
        } = this.state;
        if (securitySchemeType === 'internalkey') {
            return this.state.key;
        }
        return null;
    }

    /**
     * Handle onChange of inputs
     * @param {*} event event
     * @memberof TryOutController
     */
    handleChanges(event) {
        const { target } = event;
        const { value } = target;
        this.setSelectedEnvironment(value);
        this.updateSwagger(value);
    }

    /**
     * @inheritdoc
     * @memberof ApiConsole
     */
    render() {
        const { classes } = this.props;
        const {
            api, swagger, securitySchemeType, selectedEnvironment, environments, settings,
            showToken, apiSettings,
        } = this.state;
        const authorizationHeader = 'Internal-Key';
        if (!api || !securitySchemeType || !selectedEnvironment || !environments || !swagger || !settings
            || !apiSettings) {
            return <Progress />;
        }
        const authHeader = `${authorizationHeader}`;
        if (!swagger.openapi) {
            for (let i = 0; i < apiSettings.environment.length; i++) {
                if (apiSettings.environment[i].name === selectedEnvironment) {
                    const val = apiSettings.environment[i].endpoints.https.split('//')[1];
                    swagger.host = val;
                }
            }
            const basePath = api.context + '/' + api.version;
            swagger.basePath = basePath;
            swagger.schemes = ['https'];
        } else {
            let servers = [];
            let httpUrls = [];
            let httpsUrls = [];
            for (let i = 0; i < settings.length; i++) {
                if (environments.includes(settings[i].name)) {
                    if (settings[i].name === selectedEnvironment) {
                        httpUrls = httpUrls.concat({ url: settings[i].endpoints.http });
                        httpsUrls = httpsUrls.concat({ url: settings[i].endpoints.https });
                    }
                }
            }
            servers = httpUrls.concat(httpsUrls);
            swagger.servers = servers;
        }
        return (
            <>
                <Typography variant='h4' component='h1' className={classes.titleSub}>
                    <FormattedMessage id='Apis.Details.ApiConsole.ApiConsole.title' defaultMessage='Try Out' />
                </Typography>
                <Grid x={12} md={6} className={classes.centerItems}>
                    <Box display='flex' justifyContent='center' className={classes.gatewayEnvironment}>
                        <Grid x={12} md={6} item>
                            <Typography variant='h5' color='textPrimary' className={classes.categoryHeading}>
                                <FormattedMessage
                                    id='api.console.security.heading'
                                    defaultMessage='Security'
                                />
                            </Typography>
                            <Typography variant='h6' color='textSecondary' className={classes.tryoutHeading}>
                                <FormattedMessage
                                    id='api.console.security.type.heading'
                                    defaultMessage='Internal key'
                                />
                            </Typography>
                            <TextField
                                fullWidth
                                label={(
                                    <FormattedMessage
                                        id='internal.token'
                                        defaultMessage='Internal Token'
                                    />
                                )}
                                type={showToken ? 'text' : 'password'}
                                value={this.state.key}
                                id='margin-dense'
                                helperText='Enter access Token'
                                margin='normal'
                                variant='outlined'
                                name='internal'
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
                                            style={{
                                                minWidth: (authHeader.length * 6),
                                            }}
                                            position='start'
                                        >
                                            {`${authorizationHeader}`}
                                        </InputAdornment>
                                    ),
                                }}
                            />
                            <>
                                <Button
                                    onClick={this.generateKey}
                                    variant='contained'
                                    className={classes.genKeyButton}
                                    name='internalToken'
                                >
                                    <FormattedMessage
                                        id='Apis.Details.ApiCOnsole.generate.test.key'
                                        defaultMessage='GET TEST KEY '
                                    />
                                </Button>
                            </>
                        </Grid>
                    </Box>
                    <Box display='flex' justifyContent='center' className={classes.gatewayEnvironment}>
                        <Grid xs={12} md={6} item>
                            {((environments && environments.length > 0))
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
                                            id='environment'
                                            label={(
                                                <FormattedMessage
                                                    defaultMessage='Environment'
                                                    id='Apis.Details.ApiConsole.environment'
                                                />
                                            )}
                                            value={selectedEnvironment || (environments && environments[0])}
                                            name='selectedEnvironment'
                                            onChange={this.handleChanges}
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
                                        </TextField>
                                    </>
                                )}
                        </Grid>
                    </Box>
                    <SwaggerUI
                        api={this.state.api}
                        accessTokenProvider={this.accessTokenProvider}
                        spec={swagger}
                        authorizationHeader={authorizationHeader}
                    />
                </Grid>
            </>
        );
    }
}
TestConsole.propTypes = {
    classes: PropTypes.shape({
        paper: PropTypes.string.isRequired,
        titleSub: PropTypes.string.isRequired,
        grid: PropTypes.string.isRequired,
        userNotificationPaper: PropTypes.string.isRequired,
        buttonIcon: PropTypes.string.isRequired,
        lcState: PropTypes.shape({}).isRequired,
        theme: PropTypes.shape({}).isRequired,
        intl: PropTypes.shape({
            formatMessage: PropTypes.func,
        }).isRequired,
    }).isRequired,
};

TestConsole.contextType = ApiContext;

export default withAPI(withStyles(styles)(TestConsole));
