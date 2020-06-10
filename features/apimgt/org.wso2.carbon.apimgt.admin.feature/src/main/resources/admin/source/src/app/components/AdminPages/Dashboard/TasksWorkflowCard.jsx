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

import React, { useState, useEffect } from 'react';
import API from 'AppData/api';
import { makeStyles } from '@material-ui/core/styles';
import { Card } from '@material-ui/core';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import LaunchIcon from '@material-ui/icons/Launch';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import DeviceHubIcon from '@material-ui/icons/DeviceHub';
import Link from '@material-ui/core/Link';
import PersonAddRoundedIcon from '@material-ui/icons/PersonAddRounded';
import { Link as RouterLink } from 'react-router-dom';
import AddToQueueRoundedIcon from '@material-ui/icons/AddToQueueRounded';
import RssFeedRoundedIcon from '@material-ui/icons/RssFeedRounded';
import AirplayRoundedIcon from '@material-ui/icons/AirplayRounded';
import FormatShapesRoundedIcon from '@material-ui/icons/FormatShapesRounded';
import Avatar from '@material-ui/core/Avatar';


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
    pos: {
        marginBottom: 12,
    },
    largeAvatar: {
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
    const [tasksList, setTasksList] = useState();
    // const restApi = new API();
    // todo: delete dev components
    const fewerTasksList = [
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

    useEffect(() => {
        // todo: do api calls and set tasksList
        setTasksList(fewerTasksList);
    }, []);

    const tasksDisabledCard = (
        <Card className={classes.root}>
            <CardContent>

                <Box mt={2}>
                    <DeviceHubIcon color='secondary' style={{ fontSize: 60 }} />
                </Box>

                <Typography className={classes.title} gutterBottom>
                Enable workflow to manage tasks
                </Typography>

                {/* todo make the learn more link */}
                <Typography variant='body2' component='p'>
                Manage workflow tasks, increase productivity and enhance
                 competitiveness by enabling developers to easily deploy
                 business processes and models.
                </Typography>

                {/* todo: standardize the help link with doc url */}
                <Box mt={3}>
                    <Button
                        size='small'
                        variant='contained'
                        color='primary'
                        target='_blank'
                        href='https://apim.docs.wso2.com/en/next/develop/extending-api-manager/extending-workflows/cleaning-up-workflow-tasks/'
                    >
                        How to Enable Worflows
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
                All the pending tasks completed
                </Typography>

                <Typography variant='body2' component='p'>
                Manage workflow tasks, increase productivity and enhance
                 competitiveness by enabling developers to easily deploy
                 business processes and models.
                </Typography>
            </CardContent>
        </Card>
    );

    const getCompactTaskComponent = (IconComponent, path, name, numberOfTasks) => {
        return (
            <Box alignItems='center' display='flex' width='50%' my='1%'>
                <Box mx={1}>
                    <Avatar className={classes.largeAvatar}>
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
                        {' '}
                            Pending tasks
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
                name: 'User Creation',
            },
            {
                icon: AddToQueueRoundedIcon,
                path: '/tasks/application-creation',
                name: 'Application Creation',
            },
            {
                icon: RssFeedRoundedIcon,
                path: '/tasks/subscription-creation',
                name: 'Subscription Creation',
            },
            {
                icon: AirplayRoundedIcon,
                path: '/tasks/application-registration',
                name: 'Application Registration',
            },
            {
                icon: FormatShapesRoundedIcon,
                path: '/tasks/api-state-change',
                name: 'API State Change',
            },
        ];
        return (
            <Card className={classes.root} style={{ textAlign: 'left' }}>
                <CardContent>
                    <Box display='flex'>
                        <Box flexGrow={1}>
                            <Typography className={classes.title} gutterBottom>
                            Pending Tasks
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
                            Pending Tasks
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
                                        Created by&nbsp;
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
                                    <Button>Accept</Button>
                                    <Button>Reject</Button>
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
