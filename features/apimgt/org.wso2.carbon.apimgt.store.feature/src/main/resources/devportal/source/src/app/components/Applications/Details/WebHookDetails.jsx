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
            width: '40%'
        },
        subscriptionRow: {
            paddingLeft: '16px',
        },
        subscriptionUrl: {
            fontSize: '14px',
        },
        subscriptionTimestamp: {
            float: 'right'
        },
        SubscriptionHeader: {
            paddingBottom: '0px'
        }

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
    const [value, setValue] = React.useState('existing');

    const handleChange = (event) => {
        setValue(event.target.value);
    };

    function handleURLCopy(url) {
        const textElement = document.createElement('textarea');
        document.body.appendChild(textElement);
        textElement.value = url;
        textElement.select();
        document.execCommand("copy");
        document.body.removeChild(textElement);
        Alert.info(url + ' was copied to clipboard');
    }

    function getRelativeTIme(standardTime) {
        return moment(standardTime,"YYYY-MM-DD HH:mm:ss").fromNow();
    }

    useEffect(() => {
        const api = new Api();
        const promisedTopics = api.getAllTopics(apiId);
        // TODO: get all topics available for an web-sub API
        promisedTopics.then((response) => {

        }).catch((error) => {

        });

        const response_all_topics = {
            count: 2,
            list: [
                {name: 'order_books', apiId: 'api-id-123', subscribeURL: 'www.host.com'},
                {name: 'order_dvds', apiId: 'api-id-123', subscribeURL: 'www.xyz.com'}
            ]
        };
        setAllTopics(response_all_topics);


        // TODO: get all topics an particular application has subscribed of an API
        const promisedSubscriptions = api.getTopicSubscriptions(apiId, applicationId);
        promisedSubscriptions.then((response) => {

        }).catch((error) => {

        });
        const subscriptions = {
            count: 3,
            list: [
                {
                    name: 'orderbooks',
                    callBackUrl: 'www.google.com',
                    deliveryTime: '2016-03-07 15:13:49',
                    deliveryStatus: 1
                },
                {
                    name: 'orderbooks',
                    callBackUrl: 'www.msn.com',
                    deliveryTime: '2018-03-07 15:13:49',
                    deliveryStatus: 1
                },
                {
                    name: 'orderSomethingElse',
                    callBackUrl: 'www.myspace.com',
                    deliveryTime: '2019-03-07 15:13:49',
                    deliveryStatus: 1
                },
            ]
        };
        const sortedSubscriptions = _.groupBy(subscriptions.list, 'name');
        // Object.entries(sortedSubscriptions).forEach(([key, value])=> {console.log(key); value.map((topic, index) => {console.log(topic.callback)})});
        setSubscribedTopics(sortedSubscriptions);
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
                {
                    Object.keys(subscribedTopics).map((key, i) => (
                        <>
                            <ListItem className={classes.SubscriptionHeader}>
                                <ListItemText primary={key}/>
                            </ListItem>
                            {subscribedTopics[key].map((topic, index) => (
                                <Grid container className={classes.subscriptionRow} direction="row">
                                    <Grid item xs={1}>
                                        <CheckCircleIcon style={{color: 'green', fontSize: '14px', paddingTop: '5px'}}/>
                                    </Grid>
                                    <Grid item xs={8} className={classes.subscriptionUrl}>
                                        <Typography
                                            color="textPrimary"
                                            display="block"
                                        >{topic.callBackUrl}</Typography>
                                    </Grid>
                                    <Grid item xs={3}>
                                        <Typography
                                            color="textSecondary"
                                            display="block"
                                            variant="caption"
                                            className={classes.subscriptionTimestamp}>
                                            {getRelativeTIme(topic.deliveryTime)}
                                        </Typography>
                                    </Grid>
                                    <Divider component="li"/>
                                </Grid>
                            ))}
                            <Divider component="li"/>
                        </>
                    ))
                }
                {/*{subscribedTopics.topics.map((topic, index) => (*/}
                {/*<>*/}
                {/*<ListItem>*/}
                {/*<ListItemIcon>*/}
                {/*<CheckCircleIcon style={{ color: 'green' }} />*/}
                {/*</ListItemIcon>*/}
                {/*<ListItemText primary={topic.topic_name} secondary={topic.call_back}/>*/}
                {/*<Typography*/}
                {/*color="textSecondary"*/}
                {/*display="block"*/}
                {/*variant="caption"*/}
                {/*>Few seconds ago</Typography>*/}
                {/*</ListItem>*/}
                {/*<Divider component="li"/>*/}
                {/*</>*/}
                {/*))}*/}
            </List>
            }
            {allTopics && value === 'all' &&
            <List className={classes.listWrapper}>
                <Divider/>
                {allTopics.list.map((topic, index) => (
                    <>
                        <ListItem>
                            <ListItemText primary={topic.name}/>
                            <Button
                                size="small"
                                variant='outlined'
                                className={classes.buttonElm}
                                onClick={() => handleURLCopy(topic.subscribeURL)}
                            >
                                <FormattedMessage
                                    id={'Applications.Details.Subscriptions.api.webhooks.subscribe'}
                                    defaultMessage='Subscribe to topic'
                                />
                            </Button>
                        </ListItem>
                        <Divider component="li"/>
                    </>
                ))}
            </List>
            }
        </div>
    );
}
