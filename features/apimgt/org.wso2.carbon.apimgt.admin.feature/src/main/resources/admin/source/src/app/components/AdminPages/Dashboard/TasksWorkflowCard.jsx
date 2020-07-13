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
import DeviceHubIcon from '@material-ui/icons/DeviceHub';
import DnsRoundedIcon from '@material-ui/icons/DnsRounded';
import PeopleIcon from '@material-ui/icons/People';
import PermMediaOutlinedIcon from '@material-ui/icons/PhotoSizeSelectActual';
import PublicIcon from '@material-ui/icons/Public';
import SettingsEthernetIcon from '@material-ui/icons/SettingsEthernet';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import moment from 'moment';

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
    approveButton: {
        textDecoration: 'none',
        backgroundColor: theme.palette.success.light,
        margin: theme.spacing(0.5),
    },
    rejectButton: {
        textDecoration: 'none',
        backgroundColor: theme.palette.error.light,
        margin: theme.spacing(0.5),
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

    // Fetch all workflow tasks
    const fetchAllWorkFlows = () => {
        const promiseUserSign = restApi.workflowsGet('AM_USER_SIGNUP');
        const promiseStateChange = restApi.workflowsGet('AM_API_STATE');
        const promiseAppCreation = restApi.workflowsGet('AM_APPLICATION_CREATION');
        const promiseSubCreation = restApi.workflowsGet('AM_SUBSCRIPTION_CREATION');
        const promiseSubUpdate = restApi.workflowsGet('AM_SUBSCRIPTION_UPDATE');
        const promiseRegProd = restApi.workflowsGet('AM_APPLICATION_REGISTRATION_PRODUCTION');
        const promiseRegSb = restApi.workflowsGet('AM_APPLICATION_REGISTRATION_SANDBOX');
        Promise.all([promiseUserSign, promiseStateChange, promiseAppCreation, promiseSubCreation,
            promiseSubUpdate, promiseRegProd, promiseRegSb])
            .then(([resultUserSign, resultStateChange, resultAppCreation, resultSubCreation,
                resultSubUpdate, resultRegProd, resultRegSb]) => {
                const userCreation = resultUserSign.body.list;
                const stateChange = resultStateChange.body.list;
                const applicationCreation = resultAppCreation.body.list;
                const subscriptionCreation = resultSubCreation.body.list;
                const subscriptionUpdate = resultSubUpdate.body.list;
                const registration = resultRegProd.body.list.concat(resultRegSb.body.list);
                setAllTasksSet({
                    userCreation,
                    stateChange,
                    applicationCreation,
                    subscriptionCreation,
                    subscriptionUpdate,
                    registration,
                });
            });
    };

    useEffect(() => {
        fetchAllWorkFlows();
    }, []);

    // Component to be displayed when there's no task available
    // Note: When workflow is not enabled, this will be displayed
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

    // Compact task card component's individual category component
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

    // Component to be displayed when there are more than 4 tasks available
    // Renders the total task count, each task category remaining task count and links
    const compactTasksCard = () => {
        const compactTaskComponentDetails = [
            {
                icon: PeopleIcon,
                path: '/tasks/user-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.userCreation.name',
                    defaultMessage: 'User Creation',
                }),
                count: allTasksSet.userCreation.length,
            },
            {
                icon: DnsRoundedIcon,
                path: '/tasks/application-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.applicationCreation.name',
                    defaultMessage: 'Application Creation',
                }),
                count: allTasksSet.applicationCreation.length,
            },
            {
                icon: PermMediaOutlinedIcon,
                path: '/tasks/subscription-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.subscriptionCreation.name',
                    defaultMessage: 'Subscription Creation',
                }),
                count: allTasksSet.subscriptionCreation.length,
            },
            {
                icon: PermMediaOutlinedIcon,
                path: '/tasks/subscription-update',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.subscriptionUpdate.name',
                    defaultMessage: 'Subscription Update',
                }),
                count: allTasksSet.subscriptionUpdate.length,
            },
            {
                icon: PublicIcon,
                path: '/tasks/application-registration',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.applicationRegistration.name',
                    defaultMessage: 'Application Registration',
                }),
                count: allTasksSet.registration.length,
            },
            {
                icon: SettingsEthernetIcon,
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

    // Approve/Reject button onClick handler
    const updateStatus = (referenceId, value) => {
        const body = {
            status: value,
        };
        restApi.updateWorkflow(referenceId, body)
            .then(() => {
                Alert.success(
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.card.task.update.success'
                        defaultMessage='Task status updated successfully'
                    />,
                );
            })
            .catch(() => {
                Alert.error(
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.card.task.update.failed'
                        defaultMessage='Task status updated failed'
                    />,
                );
            })
            .finally(() => {
                fetchAllWorkFlows();
            });
    };

    // Renders the approve/reject buttons with styles
    const getApproveRejectButtons = (referenceId) => {
        return (
            <Box>
                <Button
                    onClick={() => { updateStatus(referenceId, 'APPROVED'); }}
                    className={classes.approveButton}
                    variant='contained'
                    size='small'
                >
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.fewerTasks.card.task.accept'
                        defaultMessage='Accept'
                    />
                </Button>
                <Button
                    onClick={() => { updateStatus(referenceId, 'REJECTED'); }}
                    className={classes.rejectButton}
                    variant='contained'
                    size='small'
                >
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.fewerTasks.card.task.reject'
                        defaultMessage='Reject'
                    />
                </Button>
            </Box>
        );
    };

    // Fewer task component's application creation task element
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
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.application.createdBy.prefix'
                                    defaultMessage='Application Created by '
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
                    {getApproveRejectButtons(task.referenceId)}
                </Box>
            );
        });
    };

    // Fewer task component's user creation task element
    const getUserCreationFewerTaskComponent = () => {
        // User Creation tasks related component generation
        return allTasksSet.userCreation.map((task) => {
            return (
                <Box display='flex' alignItems='center' mt={1}>
                    <Box flexGrow={1}>
                        <Typography variant='subtitle2'>
                            {task.properties.tenantAwareUserName}
                        </Typography>
                        <Box display='flex'>
                            <Typography variant='body2'>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.user.createdOn.prefix'
                                    defaultMessage='User Created on '
                                />
                            </Typography>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                &nbsp;
                                {task.properties.tenantDomain}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                {moment(task.createdTime).fromNow()}
                            </Typography>
                        </Box>
                    </Box>
                    {getApproveRejectButtons(task.referenceId)}
                </Box>
            );
        });
    };

    // Fewer task component's subscription creation task element
    const getSubscriptionCreationFewerTaskComponent = () => {
        // Subscription Creation tasks related component generation
        return allTasksSet.subscriptionCreation.map((task) => {
            return (
                <Box display='flex' alignItems='center' mt={1}>
                    <Box flexGrow={1}>
                        <Typography variant='subtitle2'>
                            {task.properties.apiName + '-' + task.properties.apiVersion}
                        </Typography>
                        <Box display='flex'>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                {task.properties.applicationName + ','}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.subscription.subscribedBy'
                                    defaultMessage='Subscribed by'
                                />
                            </Typography>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                &nbsp;
                                {task.properties.subscriber}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                {moment(task.createdTime).fromNow()}
                            </Typography>
                        </Box>
                    </Box>
                    {getApproveRejectButtons(task.referenceId)}
                </Box>
            );
        });
    };

    // Fewer task component's subscription creation task element
    const getSubscriptionUpdateFewerTaskComponent = () => {
        // Subscription Update tasks related component generation
        return allTasksSet.subscriptionUpdate.map((task) => {
            return (
                <Box display='flex' alignItems='center' mt={1}>
                    <Box flexGrow={1}>
                        <Typography variant='subtitle2'>
                            {task.properties.apiName + '-' + task.properties.apiVersion}
                        </Typography>
                        <Box display='flex'>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                {task.properties.applicationName + ','}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.subscription.subscribedBy'
                                    defaultMessage='Subscribed by'
                                />
                            </Typography>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                &nbsp;
                                {task.properties.subscriber}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                {moment(task.createdTime).fromNow()}
                            </Typography>
                        </Box>
                    </Box>
                    {getApproveRejectButtons(task.referenceId)}
                </Box>
            );
        });
    };

    // Fewer task component's registration creation task element
    const getRegistrationCreationFewerTaskComponent = () => {
        // Registration Creation tasks related component generation
        return allTasksSet.registration.map((task) => {
            let keyType;
            if (task.properties.keyType === 'PRODUCTION') {
                keyType = (
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.fewerTasks.card.registration.creation.keyType.Production'
                        defaultMessage='Production'
                    />
                );
            } else if (task.properties.keyType === 'SANDBOX') {
                keyType = (
                    <FormattedMessage
                        id='Dashboard.tasksWorkflow.fewerTasks.card.registration.creation.keyType.SandBox'
                        defaultMessage='SandBox'
                    />
                );
            } else {
                keyType = task.properties.keyType;
            }
            return (
                <Box display='flex' alignItems='center' mt={1}>
                    <Box flexGrow={1}>
                        <Typography variant='subtitle2'>
                            {task.properties.applicationName}
                        </Typography>
                        <Box display='flex'>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                {keyType}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.registration.key.generated.by'
                                    defaultMessage='Key generated by'
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
                    {getApproveRejectButtons(task.referenceId)}
                </Box>
            );
        });
    };

    // Fewer task component's api state change task element
    const getStateChangeFewerTaskComponent = () => {
        // State Change tasks related component generation
        return allTasksSet.stateChange.map((task) => {
            return (
                <Box display='flex' alignItems='center' mt={1}>
                    <Box flexGrow={1}>
                        <Typography variant='subtitle2'>
                            {task.properties.apiName + '-' + task.properties.apiVersion}
                        </Typography>
                        <Box display='flex'>
                            <Typography variant='body2'>
                                <FormattedMessage
                                    id='Dashboard.tasksWorkflow.fewerTasks.card.stateChangeAction.prefix'
                                    defaultMessage='State Change Action:'
                                />
                                &nbsp;
                            </Typography>
                            <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                {task.properties.action}
                                &nbsp;
                            </Typography>
                            <Typography variant='body2'>
                                {moment(task.createdTime).fromNow()}
                            </Typography>
                        </Box>
                    </Box>
                    {getApproveRejectButtons(task.referenceId)}
                </Box>
            );
        });
    };

    // Component to be displayed when there are 4 or less remaining tasks
    // Renders some details of the task and approve/reject buttons
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
                    {getUserCreationFewerTaskComponent()}
                    {getSubscriptionCreationFewerTaskComponent()}
                    {getSubscriptionUpdateFewerTaskComponent()}
                    {getRegistrationCreationFewerTaskComponent()}
                    {getStateChangeFewerTaskComponent()}
                </CardContent>
            </Card>
        );
    };

    // Render the card depending on the number of all remaining tasks
    const cnt = getAllTaskCount();
    if (cnt > 4) {
        return compactTasksCard();
    } else if (cnt > 0) {
        return fewerTasksCard();
    } else {
        return noTasksCard;
    }
}
