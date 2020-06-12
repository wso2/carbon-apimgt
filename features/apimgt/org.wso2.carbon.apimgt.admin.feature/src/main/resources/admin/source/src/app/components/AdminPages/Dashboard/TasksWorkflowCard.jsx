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

import React, { useEffect, useState } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { Link as RouterLink } from 'react-router-dom';
import { Card } from '@material-ui/core';
import Avatar from '@material-ui/core/Avatar';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import CardContent from '@material-ui/core/CardContent';
import Divider from '@material-ui/core/Divider';
import Link from '@material-ui/core/Link';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import AddToQueueRoundedIcon from '@material-ui/icons/AddToQueueRounded';
import AirplayRoundedIcon from '@material-ui/icons/AirplayRounded';
import DeviceHubIcon from '@material-ui/icons/DeviceHub';
import FormatShapesRoundedIcon from '@material-ui/icons/FormatShapesRounded';
import LaunchIcon from '@material-ui/icons/Launch';
import PersonAddRoundedIcon from '@material-ui/icons/PersonAddRounded';
import RssFeedRoundedIcon from '@material-ui/icons/RssFeedRounded';
import Api from 'AppData/api';
import Configurations from 'Config';
import moment from 'moment';
import Alert from 'AppComponents/Shared/Alert';

const useStyles = makeStyles((theme) => ({
    root: {
        minWidth: 275,
        minHeight: 270,
        textAlign: 'center',

    },
    title: {
        fontSize: 20,
        fontWeight: 'fontWeightBold',
    },
    avatar: {
        width: theme.spacing(4),
        height: theme.spacing(4),
    },
}));

/**
 * Render progress inside a container centering in the container.
 * @returns {JSX} Loading animation.
 */
export default function TasksWorkflowCard() {
    const classes = useStyles();
    const intl = useIntl();
    const restApi = new Api();
    const [allTasksSet, setAllTasksSet] = useState({});

    /**
    * Calculate total task count
    * @returns {int} total task count
    */
    function getAllTaskCount() {
        let counter = 0;
        for (const task in allTasksSet) {
            if (allTasksSet[task]) {
                counter += allTasksSet[task].length;
            }
        }
        return counter;
    }

    const fetchAllWorkFlows = () => {
        const promiseUserSign = restApi.workflowsGet('AM_USER_SIGNUP');
        const promiseStateChange = restApi.workflowsGet('AM_API_STATE');
        const promiseAppCreation = restApi.workflowsGet('AM_APPLICATION_CREATION');
        const promiseSubCreation = restApi.workflowsGet('AM_SUBSCRIPTION_CREATION');
        const promiseRegProd = restApi.workflowsGet('AM_APPLICATION_REGISTRATION_PRODUCTION');
        const promiseRegSb = restApi.workflowsGet('AM_APPLICATION_REGISTRATION_SANDBOX');
        Promise.all([promiseUserSign, promiseStateChange, promiseAppCreation, promiseSubCreation,
            promiseRegProd, promiseRegSb])
            .then(([resultUserSign, resultStateChange, resultAppCreation, resultSubCreation,
                resultRegProd, resultRegSb]) => {
                const userCreation = resultUserSign.body.list;
                const stateChange = resultStateChange.body.list;
                const applicationCreation = resultAppCreation.body.list;
                const subscriptionCreation = resultSubCreation.body.list;
                const registration = resultRegProd.body.list + resultRegSb.body.list;
                setAllTasksSet({
                    userCreation,
                    stateChange,
                    applicationCreation,
                    subscriptionCreation,
                    registration,
                });
            });
    };

    useEffect(() => {
        fetchAllWorkFlows();
    }, []);

    const tasksDisabledCard = (
        <Card className={classes.root}>
            <CardContent>

                <Box mt={2}>
                    <DeviceHubIcon color='secondary' style={{ fontSize: 60 }} />
                </Box>

                <Typography className={classes.title} gutterBottom>
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.tasksDisabled.card.title'
                        defaultMessage='Enable workflow to manage tasks'
                    />
                </Typography>

                <Typography variant='body2' component='p'>
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.tasksDisabled.card.description'
                        defaultMessage='Manage workflow tasks, increase productivity and enhance
                        competitiveness by enabling developers to easily deploy
                        business processes and models.'
                    />
                </Typography>

                <Box mt={3}>
                    <Button
                        size='small'
                        variant='contained'
                        color='primary'
                        target='_blank'
                        href={Configurations.app.docUrl
                            + 'learn/consume-api/manage-subscription/advanced-topics/'
                            + 'adding-an-api-subscription-workflow/#adding-an-api-subscription-workflow'}
                    >
                        <FormattedMessage
                            id='Dashboard.tasksWorkflow.tasksDisabled.card.how.to.enable.workflows.link.text'
                            defaultMessage='How to Enable Worflows'
                        />
                        <LaunchIcon fontSize='inherit' />
                    </Button>
                </Box>
            </CardContent>
        </Card>
    );

    const noTasksCard = (
        <Card className={classes.root}>
            <CardContent>

                <Box mt={2}>
                    <DeviceHubIcon color='secondary' style={{ fontSize: 60 }} />
                </Box>

                <Typography className={classes.title} gutterBottom>
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.noTasks.card.title'
                        defaultMessage='All the pending tasks completed'
                    />
                </Typography>

                <Typography variant='body2' component='p'>
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.noTasks.card.description'
                        defaultMessage='Manage workflow tasks, increase productivity and enhance
                        competitiveness by enabling developers to easily deploy
                        business processes and models.'
                    />
                </Typography>
            </CardContent>
        </Card>
    );

    const getCompactTaskComponent = (IconComponent, path, name, numberOfTasks) => {
        return (
            <Box alignItems='center' display='flex' width='50%' my='1%'>
                <Box mx={1}>
                    <Avatar className={classes.avatar}>
                        <IconComponent fontSize='inherit' />
                    </Avatar>
                </Box>
                <Box flexGrow={1}>
                    <Link component={RouterLink} to={path} color='inherit'>
                        <Typography>
                            {name}
                        </Typography>
                    </Link>
                    <Typography variant='body2' gutterBottom>
                        {numberOfTasks + ' '}
                        {numberOfTasks === 1
                            ? (
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.compactTasks.card.numberOfPendingTasks.postFix.singular'
                                    defaultMessage=' Pending task'
                                />
                            ) : (
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.compactTasks.card.numberOfPendingTasks.postFix.plural'
                                    defaultMessage=' Pending tasks'
                                />
                            )}
                    </Typography>
                </Box>
            </Box>
        );
    };

    const compactTasksCard = () => {
        const compactTaskComponentDetails = [
            {
                icon: PersonAddRoundedIcon,
                path: '/tasks/user-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.userCreation.name',
                    defaultMessage: 'User Creation',
                }),
                count: allTasksSet.userCreation.length,
            },
            {
                icon: AddToQueueRoundedIcon,
                path: '/tasks/application-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.applicationCreation.name',
                    defaultMessage: 'Application Creation',
                }),
                count: allTasksSet.applicationCreation.length,
            },
            {
                icon: RssFeedRoundedIcon,
                path: '/tasks/subscription-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.subscriptionCreation.name',
                    defaultMessage: 'Subscription Creation',
                }),
                count: allTasksSet.subscriptionCreation.length,
            },
            {
                icon: AirplayRoundedIcon,
                path: '/tasks/application-registration',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.applicationRegistration.name',
                    defaultMessage: 'Application Registration',
                }),
                count: allTasksSet.registration.length,
            },
            {
                icon: FormatShapesRoundedIcon,
                path: '/tasks/api-state-change',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.apiStateChange.name',
                    defaultMessage: 'API State Change',
                }),
                count: allTasksSet.stateChange.length,
            },
        ];
        return (
            <Card className={classes.root} style={{ textAlign: 'left' }}>
                <CardContent>
                    <Box display='flex'>
                        <Box flexGrow={1}>
                            <Typography className={classes.title} gutterBottom>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.compactTasks.card.title'
                                    defaultMessage='Pending tasks'
                                />
                            </Typography>
                        </Box>
                        <Box>
                            <Typography className={classes.title} gutterBottom>
                                {getAllTaskCount()}
                            </Typography>
                        </Box>
                    </Box>

                    <Divider light />

                    <Box
                        display='flex'
                        flexWrap='wrap'
                        mt={2}
                        bgcolor='background.paper'

                    >
                        {compactTaskComponentDetails.map((c) => {
                            return getCompactTaskComponent(c.icon, c.path, c.name, c.count);
                        })}
                    </Box>
                </CardContent>
            </Card>
        );
    };

    const updateStatus = (referenceId, value) => {
        const body = {
            status: value,
            description: 'Approve workflow request',
        };
        restApi.updateWorkflow(referenceId, body)
            .then(() => {
                Alert.success(
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.ApplicationCreation.update.success'
                        defaultMessage='Application creation status updated successfully'
                    />,
                );
            })
            .catch(() => {
                Alert.error(
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.ApplicationCreation.update.failed'
                        defaultMessage='Application creation approval failed'
                    />,
                );
            })
            .finally(() => {
                fetchAllWorkFlows();
            });
    };

    const getApplicationCreationFewerTaskComponent = () => {
        // Application Creation tasks related component generation
        return allTasksSet.applicationCreation.map((task) => {
            return (
                <Box display='flex' alignItems='center' mt={1}>
                    <Box flexGrow={1}>
                        <Typography variant='subtitle2'>
                            {task.properties.applicationName}
                        </Typography>
                        <Box display='flex'>
                            <Typography variant='body2'>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.createdBy.prefix'
                                    defaultMessage='Created by '
                                />
                            </Typography>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                &nbsp;
                                {task.properties.userName}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                {moment(task.createdTime).fromNow()}
                            </Typography>
                        </Box>
                    </Box>
                    <Box>
                        <Button
                            onClick={() => { updateStatus(task.referenceId, 'APPROVED'); }}
                        >
                            <FormattedMessage
                                id='Dashboard.tasksWorkflow.fewerTasks.card.task.accept'
                                defaultMessage='Accept'
                            />
                        </Button>
                        <Button
                            onClick={() => { updateStatus(task.referenceId, 'REJECTED'); }}
                        >
                            <FormattedMessage
                                id='Dashboard.tasksWorkflow.fewerTasks.card.task.reject'
                                defaultMessage='Reject'
                            />
                        </Button>
                    </Box>
                </Box>
            );
        });
    };

    const fewerTasksCard = () => {
        return (
            <Card className={classes.root} style={{ textAlign: 'left' }}>
                <CardContent>
                    <Box display='flex'>
                        <Box flexGrow={1}>
                            <Typography className={classes.title} gutterBottom>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.title'
                                    defaultMessage='Pending tasks'
                                />
                            </Typography>
                        </Box>
                        <Box>
                            <Typography className={classes.title} gutterBottom>
                                {getAllTaskCount()}
                            </Typography>
                        </Box>
                    </Box>

                    <Divider light />
                    {getApplicationCreationFewerTaskComponent()}
                </CardContent>
            </Card>
        );
    };

    const cnt = getAllTaskCount();
    if (cnt > 4) {
        return compactTasksCard();
    } else if (cnt > 0) {
        return fewerTasksCard();
    } else {
        return noTasksCard;
    }
}
