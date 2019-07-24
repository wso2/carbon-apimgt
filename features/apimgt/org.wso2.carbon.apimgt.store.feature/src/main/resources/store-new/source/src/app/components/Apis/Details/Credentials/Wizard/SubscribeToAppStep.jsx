/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import SubscribeToApi from 'AppComponents/Shared/AppsAndKeys/SubscribeToApi';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';

const subscribeToAppStep = (props) => {
    const [subscriptionRequest, setApplicationRequest] = useState({
        applicationId: '',
        apiId: '',
        throttlingPolicy: '',
    });
    const [newApp, setNewApp] = useState(null);
    const {
        apiId, currentStep, throttlingPolicyList, createdApp, decrementStep, incrementStep,
        nextStep,
    } = props;

    useEffect(() => {
        const newSubscriptionRequest = { ...subscriptionRequest, apiId };
        if (throttlingPolicyList) {
            [newSubscriptionRequest.throttlingPolicy] = throttlingPolicyList;
        }
        if (createdApp) {
            newSubscriptionRequest.applicationId = createdApp.value;
        }
        setApplicationRequest(newSubscriptionRequest);
        setNewApp(createdApp);
    }, [createdApp]);

    // Subscribe to api when current step is 2
    useEffect(() => {
        if (nextStep === 2 && nextStep > currentStep) {
            const api = new API();
            api.subscribe(subscriptionRequest.apiId, subscriptionRequest.applicationId,
                subscriptionRequest.throttlingPolicy)
                .then((response) => {
                    console.log('Subscription created successfully with ID : ' + response.body.subscriptionId);
                    Alert.info('Subscribed successfully');
                    incrementStep('current');
                })
                .catch((error) => {
                    console.log('Error while creating the subscription.');
                    console.error(error);
                    decrementStep();
                });
        }
    }, [nextStep]);

    if (currentStep === 1) {
        return (
            <SubscribeToApi
                throttlingPolicyList={throttlingPolicyList}
                applicationsAvailable={[newApp]}
                subscriptionRequest={subscriptionRequest}
                updateSubscriptionRequest={setApplicationRequest}
            />
        );
    }
    return '';
};

export default subscribeToAppStep;
