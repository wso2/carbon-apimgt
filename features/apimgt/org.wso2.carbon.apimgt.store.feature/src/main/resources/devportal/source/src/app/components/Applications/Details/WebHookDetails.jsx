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

import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles/index';
import { matchPath } from 'react-router';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Divider from '@material-ui/core/Divider';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import _ from 'lodash';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import CancelIcon from '@material-ui/icons/Cancel';
import RemoveCircleIcon from '@material-ui/icons/RemoveCircle';

dayjs.extend(relativeTime);

const useStyles = makeStyles((theme) => (
    {
        buttonIcon: {
            marginRight: 10,
        },
        paper: {
            margin: theme.spacing(1),
            padding: theme.spacing(1),
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
        },
        root: {
            padding: theme.spacing(3),
            '& h5': {
                color: theme.palette.getContrastText(theme.palette.background.default),
            },
        },
        titleWrapper: {
            display: 'flex',
            alignItems: 'center',
            paddingBottom: theme.spacing(2),
            '& h5': {
                marginRight: theme.spacing(1),
            },
        },
        listWrapper: {
            width: '50%',
        },
        subscriptionRow: {
            paddingLeft: '16px',
        },
        callbackurl: {
            fontSize: '12px',
        },
        subscriptionTimestamp: {
            float: 'right',
        },
        SubscriptionHeader: {
            paddingBottom: '0px',
            paddingLeft: '0px',
            paddingTop: '0px',
        },
    }
));

/**
 * @param {JSON} props props passed from parent
 * @returns {JSX} jsx output
 */
export default function WebHookDetails(props) {
    const classes = useStyles();
    const { location: { pathname } } = props;
    const match = matchPath(pathname, {
        path: '/applications/:applicationId/webhooks/:apiId',
        exact: true,
        strict: false,
    });
    const { applicationId } = props.match.params;
    const { apiId } = match.params;

    const [subscribedTopics, setSubscribedTopics] = useState('');

    const getLogoForDeliveryStatus = (subscription) => {
        switch (subscription.deliveryStatus) {
            case 1:
                return <CheckCircleIcon style={{ color: 'green', fontSize: '14px', paddingTop: '3px' }} />;
            case 2:
                return <CancelIcon style={{ color: 'red', fontSize: '14px', paddingTop: '3px' }} />;
            default:
                return <RemoveCircleIcon style={{ color: 'black', fontSize: '14px', paddingTop: '3px' }} />;
        }
    };

    useEffect(() => {
        const apiClient = new Api();
        const promisedSubscriptions = apiClient.getWebhookubScriptions(apiId, applicationId);
        promisedSubscriptions.then((response) => {
            const sortedSubscriptions = _.groupBy(response.obj.list, 'topic');
            setSubscribedTopics(sortedSubscriptions);
        }).catch((error) => {
            console.log(error);
            Alert.error('Error while retrieving webhook subscriptions');
        });
    }, []);

    return (
        <div className={classes.root}>
            <div className={classes.titleWrapper}>
                <Typography variant='h5' className={classes.keyTitle}>
                    <FormattedMessage
                        id='Applications.Details.Subscriptions.api.webhooks'
                        defaultMessage='Web Hooks'
                    />
                </Typography>
            </div>
            <List className={classes.listWrapper}>
                {Object.keys(subscribedTopics).length < 1
                && (
                    <Typography color='textPrimary' display='block'>
                        <FormattedMessage
                            id='Applications.Details.Subscriptions.api.webhooks.subscriptions.unavailable'
                            defaultMessage='No Webhook subscriptions available at this time.'
                        />
                    </Typography>
                )}
                {Object.keys(subscribedTopics).map((key) => (
                    <>
                        <ListItem className={classes.SubscriptionHeader}>
                            <ListItemText primary={key} />
                        </ListItem>
                        {subscribedTopics[key].map((subscription) => (
                            <Grid container direction='row'>
                                <Grid item xs={1}>
                                    {getLogoForDeliveryStatus(subscription)}
                                </Grid>
                                <Grid item xs={8}>
                                    <Typography
                                        color='textPrimary'
                                        display='block'
                                        className={classes.callbackurl}
                                    >
                                        {subscription.callBackUrl}
                                    </Typography>
                                </Grid>
                                <Grid item xs={3}>
                                    {subscription.deliveryTime ? (
                                        <Typography
                                            color='textSecondary'
                                            display='block'
                                            variant='caption'
                                            className={classes.subscriptionTimestamp}
                                        >
                                            {subscription.deliveryTime}
                                        </Typography>
                                    ) : (
                                        <Typography
                                            color='textSecondary'
                                            display='block'
                                            variant='caption'
                                            className={classes.subscriptionTimestamp}
                                        >
                                            <FormattedMessage
                                                id='Applications.Details.Subscriptions.api.webhooks.delivery.time.unavailable'
                                                defaultMessage='Delivery data unavailable'
                                            />
                                        </Typography>
                                    )}
                                </Grid>
                                <Divider component='li' />
                            </Grid>
                        ))}
                        <Divider component='li' />
                    </>
                ))}
            </List>
        </div>
    );
}
