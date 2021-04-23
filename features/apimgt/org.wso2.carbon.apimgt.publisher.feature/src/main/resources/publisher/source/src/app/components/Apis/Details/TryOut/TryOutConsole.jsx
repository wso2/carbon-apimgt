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

import React, {
    useState, useEffect, useCallback, useReducer, useMemo, Suspense, lazy,
} from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import 'swagger-ui-react/swagger-ui.css';
import MenuItem from '@material-ui/core/MenuItem';
import cloneDeep from 'lodash.clonedeep';
import Api from 'AppData/api';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import CircularProgress from '@material-ui/core/CircularProgress';
import Paper from '@material-ui/core/Paper';
import { Link } from 'react-router-dom';
import LaunchIcon from '@material-ui/icons/Launch';
import TextField from '@material-ui/core/TextField';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Utils from 'AppData/Utils';
import { usePublisherSettings } from 'AppComponents/Shared/AppContext';
import Alert from 'AppComponents/Shared/MuiAlert';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import CONSTS from 'AppData/Constants';

// disabled because webpack magic comment for chunk name require to be in the same line
// eslint-disable-next-line max-len
const SwaggerUI = lazy(() => import('AppComponents/Apis/Details/TryOut/SwaggerUI' /* webpackChunkName: "TryoutConsoleSwaggerUI" */));

dayjs.extend(relativeTime);

const tasksReducer = (state, action) => {
    const { name, status } = action;
    // In the case of a key collision, the right-most (last) object's value wins out
    return { ...state, [name]: { ...state[name], ...status } };
};

/**
 * @class TryOutConsole
 * @extends {React.Component}
 */
const TryOutConsole = () => {
    const [api] = useAPI();
    const [apiKey, setAPIKey] = useState('');
    const [deployments, setDeployments] = useState([]);
    const [selectedDeployment, setSelectedDeployment] = useState();
    const [oasDefinition, setOasDefinition] = useState();
    const publisherSettings = usePublisherSettings();

    const [tasksStatus, tasksStatusDispatcher] = useReducer(tasksReducer, {
        generateKey: { inProgress: false, completed: false, error: false },
        getOAS: { inProgress: false, completed: false, error: false },
        getDeployments: { inProgress: false, completed: false, error: false },
    });

    const generateInternalKey = useCallback(() => {
        tasksStatusDispatcher({ name: 'generateKey', status: { inProgress: true } });
        Api.generateInternalKey(api.id).then((keyResponse) => {
            const { apikey } = keyResponse.body;
            setAPIKey(apikey);
            tasksStatusDispatcher({ name: 'generateKey', status: { inProgress: false, completed: true } });
        }).catch((error) => tasksStatusDispatcher({ name: 'generateKey', status: { error, inProgress: false } }));
    }, [api.id]);

    useEffect(() => {
        tasksStatusDispatcher({ name: 'getDeployments', status: { inProgress: true } });
        api.getDeployedRevisions().then((deploymentsResponse) => {
            tasksStatusDispatcher({ name: 'getDeployments', status: { inProgress: false, completed: true } });
            const currentDeployments = deploymentsResponse.body;
            const currentDeploymentsWithDisplayName = currentDeployments.map((deploy) => {
                const gwEnvironment = publisherSettings.environment.find((e) => e.name === deploy.name);
                const displayName = (gwEnvironment ? gwEnvironment.displayName : deploy.name);
                return { ...deploy, displayName };
            });
            setDeployments(currentDeploymentsWithDisplayName);
            if (currentDeploymentsWithDisplayName && currentDeploymentsWithDisplayName.length > 0) {
                const [initialDeploymentSelection] = currentDeploymentsWithDisplayName;
                setSelectedDeployment(initialDeploymentSelection);
            }
        }).catch((error) => tasksStatusDispatcher({ name: 'getDeployments', status: { inProgress: false, error } }));
        api.getSwagger().then((swaggerResponse) => setOasDefinition(swaggerResponse.body));
    }, []);

    const updatedOasDefinition = useMemo(() => {
        let oasCopy;
        if (selectedDeployment && oasDefinition) {
            const selectedGWEnvironment = publisherSettings.environment
                .find((env) => env.name === selectedDeployment.name);
            let selectedDeploymentVhost = selectedGWEnvironment.vhosts
                .find((vhost) => vhost.host === selectedDeployment.vhost);
            if (!selectedDeploymentVhost) {
                selectedDeploymentVhost = { ...CONSTS.DEFAULT_VHOST, host: selectedDeployment.vhost };
            }
            let pathSeparator = '';
            if (selectedDeploymentVhost.httpContext && !selectedDeploymentVhost.httpContext.startsWith('/')) {
                pathSeparator = '/';
            }
            oasCopy = cloneDeep(oasDefinition); // If not we are directly mutating the state
            if (oasDefinition.openapi) { // Assumed as OAS 3.x definition
                const servers = api.transport.map((transport) => {
                    const transportPort = selectedDeploymentVhost[`${transport}Port`];
                    if (!transportPort) {
                        console.error(`Can't find ${transport}Port `
                    + `in selected deployment ( ${selectedDeploymentVhost.name} )`);
                    }
                    const baseURL = `${transport}://${selectedDeployment.vhost}:${transportPort}`;
                    const url = `${baseURL}${pathSeparator}`
                + `${selectedDeploymentVhost.httpContext}${api.context}/${api.version}`;
                    return { url };
                });
                oasCopy.servers = servers.sort((a, b) => ((a.url > b.url) ? -1 : 1));
            } else { // Assume the API definition is Swagger 2
                let transportPort = selectedDeploymentVhost.httpsPort;
                if (api.transport.length === 1 && !api.transport.includes('https')) {
                    transportPort = selectedDeploymentVhost.httpPort;
                } else if (api.transport.length > 1) {
                    // TODO: fix When both HTTP and HTTPs transports are available can't switch the port between them
                    // ~tmkb
                    console.warn('HTTPS transport port will be used for all other transports');
                }
                const host = `${selectedDeploymentVhost.host}:${transportPort}`;
                const basePath = `${pathSeparator}${selectedDeploymentVhost.httpContext}${api.context}/${api.version}`;
                oasCopy.schemes = api.transport.slice().sort((a, b) => ((a > b) ? -1 : 1));
                oasCopy.basePath = basePath;
                oasCopy.host = host;
            }
        } else if (oasDefinition) {
            // If no deployment just show the OAS definition
            oasCopy = oasDefinition;
        }
        return oasCopy;
    }, [selectedDeployment, oasDefinition]);

    /**
     *
     * @param {React.SyntheticEventn} event
     */
    const deploymentSelectionHandler = (event) => {
        const selectedGWEnvironment = event.target.value;
        const currentSelection = deployments.find((deployment) => deployment.name === selectedGWEnvironment);
        setSelectedDeployment(currentSelection);
    };
    const decodedJWT = useMemo(() => Utils.decodeJWT(apiKey), [apiKey]);
    const isAPIRetired = api.lifeCycleStatus === 'RETIRED';
    return (
        <>
            <Typography id='itest-api-details-try-out-head' variant='h4' component='h1'>
                <FormattedMessage id='Apis.Details.ApiConsole.ApiConsole.title' defaultMessage='Try Out' />
            </Typography>
            <Paper elevation={0}>
                <Box display='flex' justifyContent='center'>
                    <Grid xs={11} md={6} item>
                        <Typography variant='h5' color='textPrimary'>
                            <FormattedMessage
                                id='api.console.security.heading'
                                defaultMessage='Security'
                            />
                        </Typography>
                        <TextField
                            fullWidth
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.TryOutConsole.token.label'
                                    defaultMessage='Internal API Key'
                                />
                            )}
                            type='password'
                            value={apiKey}
                            helperText={decodedJWT ? (
                                <Box color='success.main'>
                                    {`Expires ${dayjs.unix(decodedJWT.payload.exp).fromNow()}`}
                                </Box>
                            ) : 'Generate or provide an internal API Key'}
                            margin='normal'
                            variant='outlined'
                            name='internal'
                            multiline
                            rows={4}
                            onChange={(e) => setAPIKey(e.target.value)}
                            disabled={isAPIRetired}
                        />
                        <Button
                            onClick={generateInternalKey}
                            variant='contained'
                            color='primary'
                            disabled={tasksStatus.generateKey.inProgress || isAPIRetired}
                        >
                            <FormattedMessage
                                id='Apis.Details.ApiConsole.generate.test.key'
                                defaultMessage='Generate Key'
                            />
                        </Button>
                        {tasksStatus.generateKey.inProgress
                            && (
                                <Box
                                    display='inline'
                                    position='absolute'
                                    mt={1}
                                    ml={-8}
                                >
                                    <CircularProgress size={24} />
                                </Box>
                            )}
                    </Grid>
                </Box>
                <Box my={3} display='flex' justifyContent='center'>
                    <Grid xs={11} md={6} item>
                        {(tasksStatus.getDeployments.completed && !deployments.length && !isAPIRetired) && (
                            <Alert variant='outlined' severity='error'>
                                <FormattedMessage
                                    id='Apis.Details.ApiConsole.deployments.no'
                                    defaultMessage={'{artifactType} is not deployed yet! Please deploy '
                                    + 'the {artifactType} before trying out'}
                                    values={{ artifactType: api.isRevision ? 'Revision' : 'API' }}
                                />
                                <Link to={'/apis/' + api.id + '/deployments'}>
                                    <LaunchIcon
                                        color='primary'
                                        fontSize='small'
                                    />
                                </Link>
                            </Alert>
                        )}
                        {isAPIRetired && (
                            <Alert variant='outlined' severity='error'>
                                <FormattedMessage
                                    id='Apis.Details.ApiConsole.deployments.isAPIRetired'
                                    defaultMessage='Can not Try Out retired APIs!'
                                />
                            </Alert>
                        )}
                        {((deployments && deployments.length > 0))
                            && (
                                <>
                                    <Typography
                                        variant='h5'
                                        color='textPrimary'
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.ApiConsole.deployments.api.gateways'
                                            defaultMessage='API Gateways'
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
                                        value={(selectedDeployment && selectedDeployment.name) || ''}
                                        name='selectedEnvironment'
                                        onChange={deploymentSelectionHandler}
                                        margin='normal'
                                        variant='outlined'
                                        SelectProps={{
                                            MenuProps: {
                                                anchorOrigin: {
                                                    vertical: 'bottom',
                                                    horizontal: 'left',
                                                },
                                                getContentAnchorEl: null,
                                            },
                                        }}
                                    >
                                        {deployments.map((deployment) => (
                                            <MenuItem
                                                value={deployment.name}
                                                key={deployment.name}
                                            >
                                                {deployment.displayName}
                                            </MenuItem>
                                        ))}
                                    </TextField>
                                </>
                            )}
                    </Grid>
                </Box>
                {updatedOasDefinition ? (
                    <Suspense
                        fallback={(
                            <CircularProgress />
                        )}
                    >
                        <SwaggerUI
                            api={api}
                            accessTokenProvider={() => apiKey}
                            spec={updatedOasDefinition}
                            authorizationHeader='Internal-Key'
                        />
                    </Suspense>
                ) : <CircularProgress />}
            </Paper>
        </>
    );
};
TryOutConsole.propTypes = {
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

export default TryOutConsole;
