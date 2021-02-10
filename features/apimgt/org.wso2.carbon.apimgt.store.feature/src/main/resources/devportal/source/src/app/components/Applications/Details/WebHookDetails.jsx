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

import React, {useState, useEffect} from 'react';
import {makeStyles} from "@material-ui/core/styles/index";
import {matchPath} from "react-router";
import Typography from '@material-ui/core/Typography';
import {FormattedMessage} from 'react-intl';
import Radio from '@material-ui/core/Radio';
import {RadioGroup} from '@material-ui/core';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Divider from '@material-ui/core/Divider';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/Alert';
import Button from '@material-ui/core/Button';
import Api from 'AppData/api';
import _ from 'lodash'
import moment from 'moment'
import Box from '@material-ui/core/Box';
import CopyToClipboard from 'react-copy-to-clipboard'
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import TextField from '@material-ui/core/TextField';
import CancelIcon from '@material-ui/icons/Cancel';
import RemoveCircleIcon from '@material-ui/icons/RemoveCircle';

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
            width: '50%'
        },
        subscriptionRow: {
            paddingLeft: '16px',
        },
        callbackurl: {
            fontSize: '12px',
        },
        subscriptionTimestamp: {
            float: 'right'
        },
        SubscriptionHeader: {
            paddingBottom: '0px',
            paddingLeft: '0px',
            paddingTop: '0px',
        },
        topicRow: {
            paddingTop: '0px',
            paddingBottom: '0px',
            paddingLeft: '0px',
            paddingRight: '0px'
        },
        bootstrapRoot: {
            padding: 0,
            'label + &': {
                marginTop: theme.spacing(3),
            },
        },
        bootstrapInput: {
            borderRadius: 4,
            backgroundColor: theme.palette.common.white,
            border: '1px solid #ced4da',
            padding: '5px 12px',
            marginTop: '11px',
            width: 240,
            transition: theme.transitions.create(['border-color', 'box-shadow']),
            '&:focus': {
                borderColor: '#80bdff',
                boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
            },
            fontSize: 12,
        },
    }
));

export default function WebHookDetails(props) {
    const classes = useStyles();
    const {location: {pathname}} = props;
    let match = matchPath(pathname, {
        path: '/applications/:applicationId/webhooks/:apiId',
        exact: true,
        strict: false,
    });
    const applicationId = props.match.params.applicationId;
    const apiId = match.params.apiId;

    const [allTopics, setAllTopics] = useState('');
    const [subscribedTopics, setSubscribedTopics] = useState('');
    const [api, setApi] = useState('');
    const [value, setValue] = React.useState('existing');

    const handleChange = (event) => {
        setValue(event.target.value);
    };

    function generateGenericWHSubscriptionUrl(topicName) {
        const apiEndpointUrl = api.endpointURLs[0].URLs.https;
        return `${apiEndpointUrl}?hub.topic=${topicName}&hub.callback=https://placeholder.com&hub.mode=subscribe&hub.secret=some_secret&hub.lease_seconds=50000000`;
    }

    function onCopy(message) {
        Alert.info(message);
    }

    function getRelativeTIme(standardTime) {
        return moment(standardTime,"YYYY-MM-DD HH:mm:ss").fromNow();
    }

    useEffect(() => {
        const apiClient = new Api();

        const promisedApi = apiClient.getAPIById(apiId);
        promisedApi.then((response) => {
            setApi(response.obj);
        }).catch((error) => {
            console.log(error);
            Alert.error('Error while retrieving api data');
        });

        const promisedTopics = apiClient.getAllTopics(apiId);
        promisedTopics.then((response) => {
            console.log(response);
            setAllTopics(response.obj);
        }).catch((error) => {
            console.log(error);
            Alert.error('Error while retrieving api data');
        });

        const promisedSubscriptions = apiClient.getTopicSubscriptions(apiId, applicationId);
        promisedSubscriptions.then((response) => {
           const sortedSubscriptions = _.groupBy(response.obj.list, 'topic');
           setSubscribedTopics(sortedSubscriptions);
        }).catch((error) => {
            console.log(error);
            Alert.error('Error while retrieving api data');
        });
    }, []);

    return (
        <div className={classes.root}>
            <div className={classes.titleWrapper}>
                <Typography variant='h5' className={classes.keyTitle}>
                    <FormattedMessage
                        id='"Applications.Details.Subscriptions.api.webhooks"'
                        defaultMessage='Web Hooks'
                    />
                </Typography>
            </div>
            <RadioGroup aria-label="gender" name="gender1" value={value} row onChange={handleChange}>
                <FormControlLabel value="existing" control={<Radio/>} label="Existing"/>
                <FormControlLabel value="all" control={<Radio/>} label="All"/>
            </RadioGroup>
            {subscribedTopics && value === 'existing' &&
            <List className={classes.listWrapper}>
                <Divider/>
                {Object.keys(subscribedTopics).length < 1 &&
                <Typography color="textPrimary" display="block">
                    No Web hook Subscriptions Available
                </Typography>
                }
                {Object.keys(subscribedTopics).map((key, i) => (
                        <>
                            <ListItem className={classes.SubscriptionHeader}>
                                <ListItemText primary={key}/>
                            </ListItem>
                            {subscribedTopics[key].map((subscription, index) => (
                                <Grid container direction="row">
                                    <Grid item xs={1}>
                                        {subscription.deliveryStatus && subscription.deliveryStatus.toString() === '1' &&
                                        <CheckCircleIcon style={{color: 'green', fontSize: '14px', paddingTop: '3px'}}/>
                                        }
                                        {subscription.deliveryStatus && (subscription.deliveryStatus.toString() === '0') &&
                                        <CancelIcon style={{color: 'red', fontSize: '14px', paddingTop: '3px'}}/>
                                        }
                                        {!subscription.deliveryStatus &&
                                        <RemoveCircleIcon style={{color: 'black', fontSize: '14px', paddingTop: '3px'}}/>
                                        }
                                    </Grid>
                                    <Grid item xs={8}>
                                        <Typography
                                            color="textPrimary"
                                            display="block"
                                            className={classes.callbackurl}>
                                            {subscription.callBackUrl}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={3}>
                                        {subscription.deliveryTime ? (
                                            <Typography
                                                color="textSecondary"
                                                display="block"
                                                variant="caption"
                                                className={classes.subscriptionTimestamp}>
                                                {subscription.deliveryTime}
                                            </Typography>
                                        ): (
                                            <Typography
                                                color="textSecondary"
                                                display="block"
                                                variant="caption"
                                                className={classes.subscriptionTimestamp}>
                                                Delivery data not available
                                            </Typography>
                                        )}
                                    </Grid>
                                    <Divider component="li"/>
                                </Grid>
                            ))}
                            <Divider component="li"/>
                        </>
                    ))}
            </List>
            }
            {allTopics && value === 'all' &&
            <List className={classes.listWrapper}>
                <Divider/>
                {allTopics.list.map((topic, index) => (
                    <>
                        <ListItem className={classes.topicRow}>
                            <Grid container direction='row'>
                                <Grid item xs={6}> <ListItemText primary={topic.name} style={{marginTop: '11px'}}/> </Grid>
                                <Grid item xs={6}>
                                    <div style={{float:'right'}}>
                                    <TextField
                                        defaultValue={generateGenericWHSubscriptionUrl(topic.name)}
                                        id='bootstrap-input'
                                        InputProps={{
                                            disableUnderline: true,
                                            readOnly: true,
                                            classes: {
                                                root: classes.bootstrapRoot,
                                                input: classes.bootstrapInput,
                                            },
                                        }}
                                        InputLabelProps={{
                                            shrink: true,
                                            className: classes.bootstrapFormLabel,
                                        }}
                                    />
                                    <CopyToClipboard
                                        text={generateGenericWHSubscriptionUrl(topic.name)}
                                        onCopy={() => onCopy('Subscription url copied for ' + api.endpointURLs[0].environmentName)}
                                    >
                                        <IconButton aria-label='Copy to clipboard'>
                                            <Icon color='secondary'>file_copy</Icon>
                                        </IconButton>
                                    </CopyToClipboard>
                                    </div>
                                </Grid>
                            </Grid>
                        </ListItem>
                        <Divider component="li"/>
                    </>
                ))}
            </List>
            }
        </div>
    );
}
