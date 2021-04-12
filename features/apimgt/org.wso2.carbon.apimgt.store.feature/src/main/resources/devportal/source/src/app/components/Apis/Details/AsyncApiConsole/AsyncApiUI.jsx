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

import React, { useState, useEffect, useContext } from 'react';
import { ApiContext } from '../ApiContext';
import Api from '../../../../data/api';
import Progress from '../../../Shared/Progress';
import WebhookSubscriptionUI from './WebhookSubscriptionUI';
import GenericSubscriptionUI from './GenericSubscriptionUI';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import { makeStyles } from "@material-ui/core/styles/index";
import CONSTANTS from 'AppData/Constants';
import Alert from 'AppComponents/Shared/Alert';
import { useIntl } from 'react-intl';

const useStyles = makeStyles((theme) => (
    {
        endpointSelectorRoot: {
            paddingBottom: '20px',
        },
        // this styling is used to resemble the switch to swagger-ui switch
        selectList: {
            minWidth: '130px',
            maxWidth: '100%',
            border: '2px solid #41444e',
            fontFamily: 'sans-serif',
            fontSize: '14px',
            fontWeight: 700,
            padding: '2px 2px 2px 10px',
            border: '2px solid #41444e',
            borderRadius: '4px',
            color: '#3b4151',
        }
    }
));
export default function AsyncApiUI(props) {
    const classes = useStyles();
    const intl = useIntl();
    const {
        authorizationHeader,
        URLs,
        securitySchemeType,
        accessTokenProvider,
    } = props;
    const { api } = useContext(ApiContext);

    let initialEndpoint = URLs && URLs.http;
    if (api.type === CONSTANTS.API_TYPES.WS) {
        initialEndpoint = URLs && URLs.ws;
    }

    const [allTopics, setAllTopics] = useState('');
    const [endPoint, setEndpoint] = useState(initialEndpoint);

    useEffect(() => {
        const apiID = api.id;
        const apiClient = new Api();
        const promisedTopics = apiClient.getAllTopics(apiID);
        promisedTopics.then((response) => {
            setAllTopics(response.body);
        }).catch((error) => {
            console.log(error);
            Alert.error(intl.formatMessage({
                id: 'Apis.Details.AsyncApiConsole.AsyncApiUI.topics.get.error',
                defaultMessage: 'Error while retrieving topics for the API.',
            }));
        });
    }, []);

    const handleServerChange = (event) => {
        setEndpoint(event.target.value);
    };

    function generateAccessToken() {
        let token;
        if (authorizationHeader === 'apikey') {
            token = accessTokenProvider();
        } else if (securitySchemeType === 'BASIC') {
            token = 'Basic ' + accessTokenProvider();
        } else {
            token = 'Bearer ' + accessTokenProvider();
        }
        return token;
    }

    function generateGenericWHSubscriptionCurl(subscription) {
        const {
            topic, callback, secret, mode, lease,
        } = subscription;
        const token = generateAccessToken();
        if (mode === 'subscribe') {
            let curl = `curl -X POST '${endPoint}?hub.topic=${encodeURIComponent(topic)}&hub.callback=${encodeURIComponent(callback)}&hub.mode=${mode}`;
            if (secret) {
                curl += `&hub.secret=${secret}`;
            }
            if (lease) {
                curl += `&hub.lease_seconds=${lease}`;
            }
            curl += `' -H 'Authorization: ${token}'`;
            return curl;
        } else {
            return `curl -X POST '${endPoint}?hub.topic=${encodeURIComponent(topic)}&hub.callback=${encodeURIComponent(callback)}&hub.mode=${mode}' -H 'Authorization: ${token}'`;
        }
    }

    function getTopicName(topic) {
        let topicName = topic.name;
        // Remove the / from the topic name
        if (topicName.charAt(0) === '/') {
            topicName = topicName.substring(1);
        }
        return topicName;
    }

    function generateWSSubscriptionCommand(topic) {
        const token = generateAccessToken();
        if (topic.name.includes('*')) {
            return `wscat -c '${endPoint}' -H 'Authorization: ${token}'`;
        } else {
            return `wscat -c '${endPoint}/${getTopicName(topic)}' -H 'Authorization: ${token}'`;
        }
    }

    function generateSSESubscriptionCommand(topic) {
        const token = generateAccessToken();
        if (topic.name.includes('*')) {
            return `curl -X GET '${endPoint}' -H 'Authorization: ${token}'`;
        } else {
            return `curl -X GET '${endPoint}/${getTopicName(topic)}' -H 'Authorization: ${token}'`;
        }
    }

    if (!allTopics) {
        return <Progress />;
    } else {
        return (
            <>
                <FormControl className={classes.endpointSelectorRoot}>
                    <InputLabel>Servers</InputLabel>
                    <Select
                        className={classes.selectList}
                        id="api-endpoint-select"
                        value={endPoint}
                        displayEmpty
                        onChange={handleServerChange}
                    >
                        {Object.entries(URLs).map(([key, value]) => {
                            if (value) {
                                return <MenuItem value={value} key={key}>{value}</MenuItem>;
                            }
                        })}
                    </Select>
                </FormControl>

                {api.type === CONSTANTS.API_TYPES.WEBSUB && allTopics.list.map((topic, index) => (
                    <WebhookSubscriptionUI
                        topic={topic}
                        generateGenericWHSubscriptionCurl={generateGenericWHSubscriptionCurl}
                    />
                ))}
                {api.type === CONSTANTS.API_TYPES.SSE && allTopics.list.map((topic, index) => (
                    <GenericSubscriptionUI
                        generateGenericSubscriptionCommand={generateSSESubscriptionCommand}
                        topic={topic}/>
                ))}
                {api.type === CONSTANTS.API_TYPES.WS && allTopics.list.map((topic, index) => (
                    <GenericSubscriptionUI
                        generateGenericSubscriptionCommand={generateWSSubscriptionCommand}
                        topic={topic}/>
                ))}
            </>
        );
    }
}
