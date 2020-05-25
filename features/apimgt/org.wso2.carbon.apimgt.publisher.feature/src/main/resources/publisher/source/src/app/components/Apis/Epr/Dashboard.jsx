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

import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { useIntl, FormattedMessage } from 'react-intl';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Link from '@material-ui/core/Link';
import { Link as RouteLink } from 'react-router-dom';
import Hidden from '@material-ui/core/Hidden';
import Divider from '@material-ui/core/Divider';
import ContentBase from 'AppComponents/Apis/Epr/Addons/ContentBase';
import LaunchIcon from '@material-ui/icons/Launch';

const useStyles = makeStyles((theme) => ({
    rootGrid: {
        flexGrow: 1,
    },
    paper: {
        padding: theme.spacing(2),
        textAlign: 'center',
        color: theme.palette.text.secondary,
    },
    root: {
        minHeight: 225,
        minWidth: 275,
        '& h2': {
            fontWeight: 200,
        },
    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    title: {
        fontSize: 14,
    },
    pos: {
        marginBottom: 12,
    },
    divider: {
        backgroundColor: '#ccc',
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
    },
    outLink: {
        width: 10,
    },
}));

/**
 * Render progress inside a container centering in the container.
 * @returns {JSX} Loading animation.
 */
export default function Dashboard() {
    const classes = useStyles();
    return (
        <Box display='flex' width='100%' flexDirection='column'>

            <ContentBase title='Endpoint Registry' pageStyle='paperLess'>

                <div className={classes.rootGrid}>
                    <Grid container spacing={3}>
                        <Hidden only='sm'><Grid item xs={2} /></Hidden>
                        <Grid item xs={12} sm={6} md={4} lg={4}>
                            <Card className={classes.root}>
                                <CardContent>
                                    <Typography variant='h5' component='h2'>
                                        <FormattedMessage
                                            id='Apis.Epr.Dashboard.wso2.registry'
                                            defaultMessage='WSO2 Registry'
                                        />
                                    </Typography>
                                    <Divider className={classes.divider} />
                                    <Typography variant='body2' component='p'>
                                        <FormattedMessage
                                            id='Apis.Epr.Dashboard.wso2.registry.description'
                                            defaultMessage={'The Endpoint Registry is the metadata storage holding information about endpoints.'
                                            + 'Endpoint registry specification for plugin any service registry and default built-in'
                                            + 'implementation for  API Manager '}
                                        />
                                        <Link href='https://apim.docs.wso2.com/en/latest/' style={{ display: 'inline-flex' }}>
                                            <FormattedMessage
                                                id='Apis.Epr.Dashboard.wso2.registry.learn.more'
                                                defaultMessage='learn more'
                                            />
                                            <LaunchIcon className={classes.outLink} />
                                        </Link>
                                    </Typography>
                                </CardContent>
                                <CardActions>
                                    <RouteLink to='/endpoint-registry/list'>
                                        <Button variant='contained' size='small' color='primary'>
                                            <FormattedMessage
                                                id='Apis.Epr.Dashboard.wso2.registry.explore'
                                                defaultMessage='Explore'
                                            />
                                        </Button>
                                    </RouteLink>
                                </CardActions>
                            </Card>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} lg={4}>
                            <Card className={classes.root}>
                                <CardContent>
                                    <Typography variant='h5' component='h2'>
                                        <FormattedMessage
                                            id='Apis.Epr.Dashboard.k8s.endpoints'
                                            defaultMessage='K8S Endpoints'
                                        />
                                    </Typography>
                                    <Divider className={classes.divider} />
                                    <Typography variant='body2' component='p'>
                                        <FormattedMessage
                                            id='Apis.Epr.Dashboard.k8s.description'
                                            defaultMessage={'The Endpoint Registry is the metadata storage holding information about endpoints.'
                                            + 'Endpoint registry specification for plugin any service registry and default built-in'
                                            + 'implementation for  API Manager '}
                                        />

                                        <Link href='https://apim.docs.wso2.com/en/latest/' style={{ display: 'inline-flex' }}>
                                            <FormattedMessage
                                                id='Apis.Epr.Dashboard.k8s.learn.more'
                                                defaultMessage='learn more'
                                            />
                                            <LaunchIcon className={classes.outLink} />
                                        </Link>
                                    </Typography>
                                </CardContent>
                                <CardActions>
                                    <RouteLink to='/endpoint-registry/list'>
                                        <Button variant='contained' size='small' color='primary'>
                                            <FormattedMessage
                                                id='Apis.Epr.Dashboard.k8s.explore'
                                                defaultMessage='Explore'
                                            />
                                        </Button>
                                    </RouteLink>
                                </CardActions>
                            </Card>
                        </Grid>

                    </Grid>
                </div>
            </ContentBase>
        </Box>
    );
}
