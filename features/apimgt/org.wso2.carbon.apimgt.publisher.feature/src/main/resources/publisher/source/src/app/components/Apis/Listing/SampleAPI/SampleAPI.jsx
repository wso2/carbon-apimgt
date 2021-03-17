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
import Redirect from 'react-router-dom/Redirect';
import CircularProgress from '@material-ui/core/CircularProgress';
import Modal from '@material-ui/core/Modal';
import Backdrop from '@material-ui/core/Backdrop';
import Fade from '@material-ui/core/Fade';
import { FormattedMessage, useIntl } from 'react-intl';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import AuthManager from 'AppData/AuthManager';
import { usePublisherSettings } from 'AppComponents/Shared/AppContext';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import { makeStyles } from '@material-ui/core/styles';
import { getSampleAPIData, getSampleOpenAPI } from './SamplePizzaShack.js';

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

const tasksReducer = (state, action) => {
    const { name, status } = action;
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
    const intl = useIntl();
    const [tasksStatus, tasksStatusDispatcher] = useReducer(tasksReducer, {
        create: { inProgress: false, completed: false, errors: false },
        update: { inProgress: false, completed: false, errors: false },
        revision: { inProgress: false, completed: false, errors: false },
        deploy: { inProgress: false, completed: false, errors: false },
        publish: { inProgress: false, completed: false, errors: false },
    });
    const [showStatus, setShowStatus] = useState(false);
    const [newSampleAPI, setNewSampleAPI] = useState();
    const classes = useStyles();
    const publisherSettings = usePublisherSettings();

    const taskManager = async (promisedTask, name) => {
        tasksStatusDispatcher({ name, status: { inProgress: true } });
        let taskResult;
        try {
            taskResult = await promisedTask;
        } catch (error) {
            console.error(error);
            tasksStatusDispatcher({ name, status: { error } });
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
            Alert.info('API Revision Deployed Successfully');

            // Deploy a revision of sample API -- 5th API call
            await taskManager(sampleAPI.publish(), 'publish');
            Alert.info(intl.formatMessage({
                id: 'Apis.Listing.SampleAPI.SampleAPI.published',
                defaultMessage: 'Sample PizzaShackAPI API published successfully',
            }));
        } else {
            Alert.info(intl.formatMessage({
                id: 'Apis.Listing.SampleAPI.SampleAPI.created',
                defaultMessage: 'Sample PizzaShackAPI API created successfully',
            }));
        }
    };

    const allDone = !AuthManager.isNotPublisher() ? Object.values(tasksStatus)
        .map((tasks) => tasks.completed)
        .reduce((done, current) => current && done) : tasksStatus.create.completed;
    if (allDone) {
        const url = '/apis/' + newSampleAPI.id + '/overview';
        return <Redirect to={url} />;
    }
    const inProgressTask = Object.entries(tasksStatus).find(([, status]) => status.inProgress === true);
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
                    <Box className={classes.statusBox} p={2}>
                        <CircularProgress />
                        {inProgressTask && (
                            <Box color='success.main'>
                                {`${inProgressTask[0]}ing sample API . . .`}
                            </Box>
                        )}
                    </Box>
                </Fade>
            </Modal>
        </>
    );
};

export default SampleAPI;
