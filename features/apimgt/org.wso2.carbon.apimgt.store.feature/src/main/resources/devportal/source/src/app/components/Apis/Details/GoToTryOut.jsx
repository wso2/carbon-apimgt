/* eslint-disable prefer-destructuring */
/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    useReducer, useContext, useState,
} from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import { FormattedMessage, useIntl } from 'react-intl';
import { useHistory } from 'react-router-dom';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import Modal from '@material-ui/core/Modal';
import Backdrop from '@material-ui/core/Backdrop';
import Fade from '@material-ui/core/Fade';
import API from 'AppData/api';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import { useTheme } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import Application from 'AppData/Application';
import Alert from 'AppComponents/Shared/Alert';
import AuthManager from 'AppData/AuthManager';
import CONSTANTS from 'AppData/Constants';
import { ApiContext } from './ApiContext';
import TaskState from './TaskState';

const useStyles = makeStyles(() => ({
    tryoutLabel: {
        whiteSpace: 'nowrap',
    },
    modal: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    statusBox: {
        outline: 'none',
    },
    asyncButton: {
        minWidth: 115,
    },
}));

const keyStates = {
    COMPLETED: 'COMPLETED',
    APPROVED: 'APPROVED',
    CREATED: 'CREATED',
    REJECTED: 'REJECTED',
};

const restApi = new API();
const initialTaskStates = {
    subscribe: { inProgress: true, completed: false, errors: false },
    prepare: { inProgress: false, completed: false, errors: false },
    generate: { inProgress: false, completed: false, errors: false },
};

const tasksReducer = (state, action) => {
    const { name, status } = action;
    if (name === 'reset') {
        return initialTaskStates;
    }
    // In the case of a key collision, the right-most (last) object's value wins out
    return { ...state, [name]: { ...state[name], ...status } };
};
/**
 *
 * @returns {JSX} rendered output
 */
export default function GoToTryOut() {
    const user = AuthManager.getUser();
    const {
        api, subscribedApplications, applicationsAvailable, updateSubscriptionData,
    } = useContext(ApiContext);
    const defaultApplications = applicationsAvailable.filter((x) => x.label === 'DefaultApplication');
    const defaultApplication = defaultApplications.length > 0 ? defaultApplications[0] : null;
    const [tasksStatus, tasksStatusDispatcher] = useReducer(tasksReducer, initialTaskStates);
    const [showStatus, setShowStatus] = useState(false);
    const classes = useStyles();
    const intl = useIntl();
    const history = useHistory();
    const theme = useTheme();
    const isXsOrBelow = useMediaQuery(theme.breakpoints.down('xs'));
    const isAsyncAPI = (api
        && (api.type === CONSTANTS.API_TYPES.WS
            || api.type === CONSTANTS.API_TYPES.WEBSUB
            || api.type === CONSTANTS.API_TYPES.SSE));
    const isPrototypedAPI = api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped';
    const getKeyRequest = async () => {
        const promisedKeyManagers = restApi.getKeyManagers();
        return promisedKeyManagers
            .then((response) => {
                const keyRequest = {
                    keyType: 'SANDBOX',
                    supportedGrantTypes: [],
                    callbackUrl: '',
                    additionalProperties: {},
                    keyManager: '',
                };
                const responseKeyManagerList = [];
                response.body.list.map((item) => responseKeyManagerList.push(item));

                // Selecting a key manager from the list of key managers.
                let selectedKeyManager;
                if (responseKeyManagerList.length > 0) {
                    const responseKeyManagerListDefault = responseKeyManagerList.filter((x) => x.name === 'Resident Key Manager');
                    selectedKeyManager = responseKeyManagerListDefault.length > 0 ? responseKeyManagerListDefault[0]
                        : responseKeyManagerList[0];
                }

                // Setting key request
                try {
                    keyRequest.keyManager = selectedKeyManager.id;
                    keyRequest.supportedGrantTypes = selectedKeyManager.availableGrantTypes;
                    if (selectedKeyManager.availableGrantTypes.includes('implicit')
                        || selectedKeyManager.availableGrantTypes.includes('authorization_code')) {
                        keyRequest.callbackUrl = 'http://localhost';
                    }
                } catch (e) {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.error.keymanager',
                        defaultMessage: 'Error while selecting the key manager',
                    }));
                }
                return keyRequest;
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    };
    const generateKeys = async (keyRequest, applicationId) => {
        const application = await Application.get(applicationId);
        const keys = await application.getKeys(keyRequest.keyType);
        if (keys.size > 0) {
            return;
        }
        application.generateKeys(
            keyRequest.keyType, keyRequest.supportedGrantTypes,
            keyRequest.callbackUrl,
            keyRequest.additionalProperties, keyRequest.keyManager,
        ).then((response) => {
            if (response.keyState === keyStates.CREATED || response.keyState === keyStates.REJECTED) {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.GoToTryOut.error.keymanager',
                    defaultMessage: 'Key Generation is Blocked.',
                }));
            } else {
                console.log('Keys generated successfully with ID : ' + response);
            }
            return response;
        }).catch((error) => {
            if (process.env.NODE_ENV !== 'production') {
                console.log(error);
            }
            const { status } = error;
            if (status === 404) {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.GoToTryOut.error.404',
                    defaultMessage: 'Resource not found.',
                }));
            }
        });
    };

    const taskManager = async (promisedTask, name) => {
        tasksStatusDispatcher({ name, status: { inProgress: true } });
        let taskResult;
        try {
            taskResult = await promisedTask;
        } catch (errors) {
            console.error(errors);
            tasksStatusDispatcher({ name, status: { errors } });
        }
        tasksStatusDispatcher({ name, status: { inProgress: false, completed: true } });
        return taskResult;
    };

    const pushToTryout = async () => {
        await updateSubscriptionData();
        if (isAsyncAPI) {
            history.push('/apis/' + api.id + '/definition');
        } else {
            history.push('/apis/' + api.id + '/test');
        }
    };

    /**
     *Handle onClick event for `Deploy Sample API` Button
     * @memberof GoToTryOut
     */
    const handleTryOutClick = async () => {
        let throttlingPolicy = null;
        if (api.tiers && api.tiers.length > 0) {
            throttlingPolicy = api.tiers[0].tierName;
        } else {
            history.push('/apis/' + api.id + '/test');
        }
        setShowStatus(true);
        // Get the request for key generation using the key managers.
        const keyRequest = await taskManager(getKeyRequest(), 'prepare');
        // Generate consumer key and secret
        await taskManager(generateKeys(keyRequest, defaultApplication.value), 'generate');
        // Subscribe this API to the default application
        await taskManager(restApi.subscribe(
            api.id,
            defaultApplication.value,
            throttlingPolicy,
        ), 'subscribe');
    };

    Object.values(tasksStatus)
        .map((tasks) => tasks.completed)
        .reduce((done, current) => current && done);
    const anyErrors = Object.values(tasksStatus).map((tasks) => tasks.errors).find((error) => error !== false);

    const redirectButton = isAsyncAPI ? (
        <Button
            variant='outlined'
            color='primary'
            size='small'
            classes={{ root: classes.asyncButton, label: classes.tryoutLabel }}
            onClick={pushToTryout}
        >
            <FormattedMessage
                id='Apis.Details.GoToTryOut.btn.view.definition'
                defaultMessage='View Definition'
            />
        </Button>
    ) : (
        <Button
            variant='contained'
            color='primary'
            size='medium'
            classes={{ label: classes.tryoutLabel }}
            onClick={pushToTryout}
            aria-label='Go to Try Out page'
        >
            <FormattedMessage
                id='Apis.Details.GoToTryOut.btn.tryout'
                defaultMessage='Try Out'
            />
        </Button>
    );
    if (!defaultApplication
        || subscribedApplications.length > 0
        || api.advertiseInfo.advertised
        || !user
        || isAsyncAPI
        || isPrototypedAPI) {
        return (
            <>{redirectButton}</>

        );
    }
    return (
        <>
            <Button
                onClick={handleTryOutClick}
                variant='contained'
                color='primary'
                size='medium'
                aria-label='Try Out the API'
                classes={{ label: classes.tryoutLabel }}
            >
                <FormattedMessage
                    id='Apis.Details.GoToTryOut.btn.tryout'
                    defaultMessage='Try Out'
                />
            </Button>

            <Modal
                aria-label='Preparing to Try Out the API'
                className={classes.modal}
                open={showStatus}
                // onClose={handleClose}
                closeAfterTransition
                BackdropComponent={Backdrop}
                BackdropProps={{
                    timeout: 500,
                }}
                role='status'
            >
                <Fade in={showStatus}>
                    <Box
                        bgcolor='background.paper'
                        borderRadius='borderRadius'
                        width={isXsOrBelow ? 4 / 5 : 1 / 4}
                        className={classes.statusBox}
                        p={2}
                    >
                        <Grid
                            container
                            direction='row'
                            justify='center'
                            alignItems='center'
                        >
                            <TaskState
                                completed={tasksStatus.subscribe.completed}
                                errors={tasksStatus.subscribe.errors}
                                inProgress={tasksStatus.subscribe.inProgress}
                                completedMessage={(
                                    <FormattedMessage
                                        id='Apis.Details.GoToTryOut.popup.subscribe.complete.success'
                                        defaultMessage='API subscribe to DefaultApplication successfully!'
                                    />
                                )}
                                inProgressMessage={(
                                    <FormattedMessage
                                        id='Apis.Details.GoToTryOut.popup.subscribe.inprogress'
                                        defaultMessage='API subscribing to DefaultApplication ...'
                                    />
                                )}
                            >
                                <FormattedMessage
                                    id='Apis.Details.GoToTryOut.popup.subscribe.complete'
                                    defaultMessage='API subscribe to DefaultApplication'
                                />
                            </TaskState>
                            <TaskState
                                completed={tasksStatus.prepare.completed}
                                errors={tasksStatus.prepare.errors}
                                inProgress={tasksStatus.prepare.inProgress}
                                completedMessage={(
                                    <FormattedMessage
                                        id='Apis.Details.GoToTryOut.popup.prepare.complete'
                                        defaultMessage='Getting ready to generate keys'
                                    />
                                )}
                                inProgressMessage={(
                                    <FormattedMessage
                                        id='Apis.Details.GoToTryOut.popup.prepare.inprogress'
                                        defaultMessage='Gathering information to generate keys ...'
                                    />
                                )}
                            >
                                <FormattedMessage
                                    id='Apis.Details.GoToTryOut.popup.prepare.complete'
                                    defaultMessage='Getting ready to generate keys'
                                />
                            </TaskState>
                            <TaskState
                                completed={tasksStatus.generate.completed}
                                errors={tasksStatus.generate.errors}
                                inProgress={tasksStatus.generate.inProgress}
                                completedMessage={(
                                    <FormattedMessage
                                        id='Apis.Details.GoToTryOut.popup.generate.complete'
                                        defaultMessage='Consumer key and secret generated successfully!'
                                    />
                                )}
                                inProgressMessage={(
                                    <FormattedMessage
                                        id='Apis.Details.GoToTryOut.popup.generate.inprogress'
                                        defaultMessage='Generating Consumer key and secret ...'
                                    />
                                )}
                            >
                                <FormattedMessage
                                    id='Apis.Details.GoToTryOut.popup.key.secret'
                                    defaultMessage='Consumer key and secret'
                                />
                            </TaskState>
                            {anyErrors ? (
                                <Grid item xs={12}>
                                    <Button
                                        onClick={() => {
                                            setShowStatus(false);
                                            tasksStatusDispatcher({ name: 'reset' });
                                        }}
                                        variant='outlined'
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.GoToTryOut.continue.on.close'
                                            defaultMessage='Close'
                                        />
                                    </Button>
                                </Grid>
                            ) : (
                                <>
                                    <Grid item xs={12}>
                                        <Typography variant='caption'>
                                            <FormattedMessage
                                                id='Apis.Details.GoToTryOut.popup.final.message'
                                                defaultMessage={'All set to try out. Use the "Generate Keys"'
                                                + ' button to get an access token while you are on the Try Out page.'}
                                            />
                                        </Typography>

                                    </Grid>
                                    <Grid item xs={12}>
                                        <Box display='flex' pr={4}>
                                            <Box flex={1} />
                                            {redirectButton}
                                        </Box>
                                    </Grid>
                                </>
                            )}
                        </Grid>
                    </Box>
                </Fade>
            </Modal>

        </>
    );
}
