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
    const api = new Api();

    const [userCretionTasks, setUserCreationTasks] = useState();
    const [applicationCreationTasks, setApplicationCreationTasks] = useState();
    const [subscriptionCreationTasks, setSubscriptionCreationTasks] = useState();
    const [applicationRegistrationTasks, setApplicationRegistrationTasks] = useState();
    const [apiStateChangeTasks, setApiStateChangeTasks] = useState();
    const [allTasksCount, setAllTasksCount] = useState(0);

    const [tasksList, setTasksList] = useState();
    // const restApi = new API();
    // todo: delete dev components
    const mockTasksList = [
        {
            name: 'DefaultApplication',
            createdBy: 'Admin',
            age: '3 hours ago',
        },
        {
            name: 'DefaultApplication',
            createdBy: 'Admin',
            age: '3 hours ago',
        },
        {
            name: 'DefaultApplication',
            createdBy: 'Admin',
            age: '3 hours ago',
        },
        {
            name: 'DefaultApplication',
            createdBy: 'Admin',
            age: '3 hours ago',
        },
        {
            name: 'DefaultApplication',
            createdBy: 'Admin',
            age: '3 hours ago',
        },
    ];

    const fetchAllWorkFlows = () => {
        let tasksCounter = 0;
        api.workflowsGet('AM_USER_SIGNUP')
            .then((result) => {
                setUserCreationTasks(result.body.list);
                tasksCounter += result.body.count;
            });
        api.workflowsGet('AM_API_STATE')
            .then((result) => {
                setApiStateChangeTasks(result.body.list);
                tasksCounter += result.body.count;
            });
        api.workflowsGet('AM_APPLICATION_CREATION')
            .then((result) => {
                setApplicationCreationTasks(result.body.list);
                tasksCounter += result.body.count;
            });
        api.workflowsGet('AM_USER_SIGNUP')
            .then((result) => {
                setUserCreationTasks(result.body.list);
                tasksCounter += result.body.count;
            });
        api.workflowsGet('AM_USER_SIGNUP')
            .then((result) => {
                setUserCreationTasks(result.body.list);
                tasksCounter += result.body.count;
            });
        setAllTasksCount(tasksCounter);
    };

    useEffect(() => {
        // todo: do api calls and set tasksList
        setTasksList(mockTasksList);
        // setTasksList([]);
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
                        {numberOfTasks}
                        <FormattedMessage
                            id='Dashboard.tasksWorkflow.compactTasks.card.numberOfPendingTasks.postFix'
                            defaultMessage=' Pending tasks'
                        />
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
            },
            {
                icon: AddToQueueRoundedIcon,
                path: '/tasks/application-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.applicationCreation.name',
                    defaultMessage: 'Application Creation',
                }),
            },
            {
                icon: RssFeedRoundedIcon,
                path: '/tasks/subscription-creation',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.subscriptionCreation.name',
                    defaultMessage: 'Subscription Creation',
                }),
            },
            {
                icon: AirplayRoundedIcon,
                path: '/tasks/application-registration',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.applicationRegistration.name',
                    defaultMessage: 'Application Registration',
                }),
            },
            {
                icon: FormatShapesRoundedIcon,
                path: '/tasks/api-state-change',
                name: intl.formatMessage({
                    id: 'Dashboard.tasksWorkflow.compactTasks.apiStateChange.name',
                    defaultMessage: 'API State Change',
                }),
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
                                ##
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
                        {compactTaskComponentDetails.map((component) => {
                            return getCompactTaskComponent(component.icon, component.path, component.name, '#');
                        })}
                    </Box>
                </CardContent>
            </Card>
        );
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
                                #
                            </Typography>
                        </Box>
                    </Box>

                    <Divider light />

                    {tasksList.map((task) => {
                        return (
                            <Box display='flex' alignItems='center' mt={1}>
                                <Box flexGrow={1}>
                                    <Typography variant='subtitle2'>
                                        {task.name}
                                    </Typography>
                                    <Box display='flex'>
                                        <Typography variant='body2'>
                                            <FormattedMessage
                                                id='Dashboard.tasksWorkflow.fewerTasks.card.createdBy.prefix'
                                                defaultMessage='Created by &nbsp;'
                                            />
                                        </Typography>
                                        <Typography style={{ 'font-weight': 'bold' }} variant='body2'>
                                            {task.createdBy}
                                                &nbsp;
                                        </Typography>
                                        <Typography variant='body2'>
                                            {task.age}
                                        </Typography>
                                    </Box>
                                </Box>
                                <Box>
                                    <Button>
                                        <FormattedMessage
                                            id='Dashboard.tasksWorkflow.fewerTasks.card.task.accept'
                                            defaultMessage='Accept'
                                        />
                                    </Button>
                                    <Button>
                                        <FormattedMessage
                                            id='Dashboard.tasksWorkflow.fewerTasks.card.task.reject'
                                            defaultMessage='Reject'
                                        />
                                    </Button>
                                </Box>
                            </Box>
                        );
                    })}
                </CardContent>
            </Card>
        );
    };

    if (tasksList) {
        if (tasksList.length === 0) {
            return noTasksCard;
        } else if (tasksList.length <= 4) {
            return fewerTasksCard();
        } else {
            return compactTasksCard();
        }
    } else {
        return tasksDisabledCard;
    }
}
