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

import React, { useReducer, useState } from 'react';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import Redirect from 'react-router-dom/Redirect';
import Modal from '@material-ui/core/Modal';
import Backdrop from '@material-ui/core/Backdrop';
import Fade from '@material-ui/core/Fade';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import { usePublisherSettings } from 'AppComponents/Shared/AppContext';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import TaskState from 'AppComponents/Apis/Listing/SampleAPI/components/TaskState';
import { makeStyles } from '@material-ui/core/styles';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import { useTheme } from '@material-ui/core';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import Button from '@material-ui/core/Button';

import { getSampleAPIData, getSampleOpenAPI } from 'AppData/SamplePizzaShack';


const useStyles = makeStyles({
    modal: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    statusBox: {
        outline: 'none',
    },
});

const initialTaskStates = {
    create: { inProgress: true, completed: false, errors: false },
    update: { inProgress: false, completed: false, errors: false },
    revision: { inProgress: false, completed: false, errors: false },
    deploy: { inProgress: false, completed: false, errors: false },
    publish: { inProgress: false, completed: false, errors: false },
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
 * Handle deploying a sample API (Create, Deploy and Publish)
 *
 * @class SampleAPI
 * @extends {Component}
 */

const SampleAPI = (props) => {
    const { dense } = props;
    const [tasksStatus, tasksStatusDispatcher] = useReducer(tasksReducer, initialTaskStates);
    const [showStatus, setShowStatus] = useState(false);
    const [newSampleAPI, setNewSampleAPI] = useState();
    const classes = useStyles();
    const publisherSettings = usePublisherSettings();

    const theme = useTheme();
    const isXsOrBelow = useMediaQuery(theme.breakpoints.down('xs'));

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
    /**
     *Handle onClick event for `Deploy Sample API` Button
     * @memberof SampleAPI
     */
    const handleDeploySample = async () => {
        setShowStatus(true);
        const restApi = new API();

        const sampleAPIObj = new API(getSampleAPIData());
        // Creat the sample API -- 1st API call
        const sampleAPI = await taskManager(sampleAPIObj.save(), 'create');
        setNewSampleAPI(sampleAPI);

        // Update the sample API -- 2nd API call
        await taskManager(sampleAPI.updateSwagger(getSampleOpenAPI()), 'update');

        if (!AuthManager.isNotPublisher()) {
            const revisionPayload = {
                description: 'Initial Revision',
            };

            // Creat a revision of sample API -- 3rd API call
            const sampleAPIRevision = await taskManager(
                restApi.createRevision(sampleAPI.id, revisionPayload),
                'revision',
            );
            const envList = publisherSettings.environment.map((env) => env.name);
            const deployRevisionPayload = [];
            const getFirstVhost = (envName) => {
                const env = publisherSettings.environment.find(
                    (ev) => ev.name === envName && ev.vhosts.length > 0,
                );
                return env && env.vhosts[0].host;
            };
            if (envList && envList.length > 0) {
                if (envList.includes('Default') && getFirstVhost('Default')) {
                    deployRevisionPayload.push({
                        name: 'Default',
                        displayOnDevportal: true,
                        vhost: getFirstVhost('Default'),
                    });
                } else if (getFirstVhost(envList[0])) {
                    deployRevisionPayload.push({
                        name: envList[0],
                        displayOnDevportal: true,
                        vhost: getFirstVhost(envList[0]),
                    });
                }
            }
            const revisionId = sampleAPIRevision.body.id;

            // Deploy a revision of sample API -- 4th API call
            await taskManager(restApi.deployRevision(sampleAPI.id,
                revisionId, deployRevisionPayload), 'deploy');

            // Deploy a revision of sample API -- 5th API call
            await taskManager(sampleAPI.publish(), 'publish');
        }
    };

    const allDone = !AuthManager.isNotPublisher() ? Object.values(tasksStatus)
        .map((tasks) => tasks.completed)
        .reduce((done, current) => current && done) : (tasksStatus.create.completed && newSampleAPI);
    const anyErrors = Object.values(tasksStatus).map((tasks) => tasks.errors).find((error) => error !== false);
    if (allDone && !anyErrors) {
        const url = '/apis/' + newSampleAPI.id + '/overview';
        return <Redirect to={url} />;
    }
    return (
        <>
            <LandingMenuItem
                dense={dense}
                id='itest-id-deploy-sample'
                onClick={handleDeploySample}
                component='button'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.rest.d.sample.content'
                        defaultMessage={`Sample Pizza Shack
                                    API`}
                    />
                )}
            >
                <FormattedMessage
                    id={'Apis.Listing.SampleAPI.SampleAPI.'
                        + 'rest.d.sample.title'}
                    defaultMessage='Deploy Sample API'
                />

            </LandingMenuItem>

            <Modal
                aria-labelledby='transition-modal-title'
                aria-describedby='transition-modal-description'
                className={classes.modal}
                open={showStatus}
                // onClose={handleClose}
                closeAfterTransition
                BackdropComponent={Backdrop}
                BackdropProps={{
                    timeout: 500,
                }}
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
                                completed={tasksStatus.create.completed}
                                errors={tasksStatus.create.errors}
                                inProgress={tasksStatus.create.inProgress}
                                completedMessage={(
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.popup.create.complete'
                                        defaultMessage='API created successfully!'
                                    />
                                )}
                                inProgressMessage={(
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.popup.create.inprogress'
                                        defaultMessage='Creating sample API ...'
                                    />
                                )}
                            >
                                Create API
                            </TaskState>
                            <TaskState
                                completed={tasksStatus.update.completed}
                                errors={tasksStatus.update.errors}
                                inProgress={tasksStatus.update.inProgress}
                                completedMessage={(
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.popup.update.complete'
                                        defaultMessage='API updated successfully!'
                                    />
                                )}
                                inProgressMessage={(
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.popup.update.inprogress'
                                        defaultMessage='Updating sample API ...'
                                    />
                                )}
                            >
                                Update API
                            </TaskState>
                            {!AuthManager.isNotPublisher() && (
                                <>
                                    <TaskState
                                        completed={tasksStatus.revision.completed}
                                        errors={tasksStatus.revision.errors}
                                        inProgress={tasksStatus.revision.inProgress}
                                        completedMessage={(
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.popup.revision.complete'
                                                defaultMessage='API revision created successfully!'
                                            />
                                        )}
                                        inProgressMessage={(
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.popup.revision.inprogress'
                                                defaultMessage='Creating a revision of sample API ...'
                                            />
                                        )}
                                    >
                                        Revision API
                                    </TaskState>
                                    <TaskState
                                        completed={tasksStatus.deploy.completed}
                                        errors={tasksStatus.deploy.errors}
                                        inProgress={tasksStatus.deploy.inProgress}
                                        completedMessage={(
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.popup.deploy.complete'
                                                defaultMessage='API deployed successfully!'
                                            />
                                        )}
                                        inProgressMessage={(
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.popup.deploy.inprogress'
                                                defaultMessage='Deploying sample API ...'
                                            />
                                        )}
                                    >
                                        Deploying API
                                    </TaskState>
                                    <TaskState
                                        completed={tasksStatus.publish.completed}
                                        errors={tasksStatus.publish.errors}
                                        inProgress={tasksStatus.publish.inProgress}
                                        completedMessage={(
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.popup.publish.complete'
                                                defaultMessage='API published successfully!'
                                            />
                                        )}
                                        inProgressMessage={(
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.popup.publish.inprogress'
                                                defaultMessage='Publishing sample API to developer portal ...'
                                            />
                                        )}
                                    >
                                        Publish API
                                    </TaskState>
                                </>
                            )}
                            {anyErrors && (
                                <>
                                    <Grid item xs={8} />
                                    <Grid item xs={2}>
                                        <Button
                                            onClick={() => {
                                                setShowStatus(false);
                                                tasksStatusDispatcher({ name: 'reset' });
                                            }}
                                            variant='outlined'
                                        >
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.continue.on.close'
                                                defaultMessage='Close'
                                            />
                                        </Button>
                                    </Grid>
                                    {newSampleAPI && (
                                        <Grid item xs={2}>
                                            <Link
                                                underline='none'
                                                component={RouterLink}
                                                to={`/apis/${newSampleAPI.id}/overview`}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.continue.on.error'
                                                    defaultMessage='Continue'
                                                />
                                            </Link>
                                        </Grid>
                                    )}
                                </>
                            )}
                        </Grid>
                    </Box>
                </Fade>
            </Modal>
        </>
    );
};

export default SampleAPI;
