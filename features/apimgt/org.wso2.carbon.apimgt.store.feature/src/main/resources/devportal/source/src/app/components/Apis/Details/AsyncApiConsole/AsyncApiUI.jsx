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

export default function AsyncApiUI(props) {
    const {
        authorizationHeader,
        URLs,
        securitySchemeType,
        accessTokenProvider,
    } = props;
    const { api } = useContext(ApiContext);
    const [allTopics, setAllTopics] = useState('');

    useEffect(() => {
        const apiID = api.id;
        const apiClient = new Api();
        const promisedTopics = apiClient.getAllTopics(apiID);
        promisedTopics.then((response) => {
            setAllTopics(response.body);
        }).catch((error) => {
            console.log(error);
        });
    }, []);

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
        const apiEndpointUrl = URLs.http;
        if (mode === 'subscribe') {
            let curl = `curl -X POST '${apiEndpointUrl}?hub.topic=${encodeURIComponent(topic)}&hub.callback=${encodeURIComponent(callback)}&hub.mode=${mode}`;
            if (secret) {
                curl += `&hub.secret=${secret}`;
            }
            if (lease) {
                curl += `&hub.lease_seconds=${lease}`;
            }
            curl += `' -H 'Authorization: ${token}'`;
            return curl;
        } else {
            return `curl -X POST '${apiEndpointUrl}?hub.topic=${encodeURIComponent(topic)}&hub.callback=${encodeURIComponent(callback)}&hub.mode=${mode}' -H 'Authorization: ${token}'`;
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
        const apiEndpointUrl = URLs.ws;
        if (topic.name.includes('*')) {
            return `wscat -c '${apiEndpointUrl}' -H 'Authorization: ${token}'`;
        } else {
            return `wscat -c '${apiEndpointUrl}/${getTopicName(topic)}' -H 'Authorization: ${token}'`;
        }
    }

    function generateSSESubscriptionCommand(topic) {
        const token = generateAccessToken();
        const apiEndpointUrl = URLs.http;
        if (topic.name.includes('*')) {
            return `curl -X POST '${apiEndpointUrl}' -H 'Authorization: ${token}'`;
        } else {
            return `curl -X POST '${apiEndpointUrl}/${getTopicName(topic)}' -H 'Authorization: ${token}'`;
        }
    }

    if (!allTopics) {
        return <Progress />;
    } else {
        return (
            <>
                {api.type === 'WEBSUB' && allTopics.list.map((topic, index) => (
                    <WebhookSubscriptionUI
                        topic={topic}
                        generateGenericWHSubscriptionCurl={generateGenericWHSubscriptionCurl}
                    />
                ))}
                {api.type === 'SSE' && allTopics.list.map((topic, index) => (
                    <GenericSubscriptionUI
                        command={generateSSESubscriptionCommand(topic)}
                        topic={topic}/>
                ))}
                {api.type === 'WS' && allTopics.list.map((topic, index) => (
                    <GenericSubscriptionUI
                        command={generateWSSubscriptionCommand(topic)}
                        topic={topic}/>
                ))}
            </>
        );
    }
}
