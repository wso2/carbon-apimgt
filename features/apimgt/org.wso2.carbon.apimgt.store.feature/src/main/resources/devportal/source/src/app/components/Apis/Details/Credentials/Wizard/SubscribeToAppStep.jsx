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

import React, { useState, useEffect, useContext } from 'react';
import SubscribeToApi from 'AppComponents/Shared/AppsAndKeys/SubscribeToApi';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import { injectIntl } from 'react-intl';
import ButtonPanel from './ButtonPanel';

const subscribeToAppStep = (props) => {
    const SUBSCRIPTION_STATES = {
        ON_HOLD: 'ON_HOLD',
        UNBLOCKED: 'UNBLOCKED',
        REJECTED: 'REJECTED',
    };
    const [subscriptionRequest, setApplicationRequest] = useState({
        applicationId: '',
        apiId: '',
        throttlingPolicy: '',
    });
    const { api: apiObject } = useContext(ApiContext);
    const [newApp, setNewApp] = useState(null);
    const [throttlingPolicyList] = useState(apiObject.tiers);
    const {
        currentStep, createdApp, incrementStep, intl, setStepStatus,
        stepStatuses, classes,
    } = props;
    const subscribeToApplication = () => {
        const api = new API();
        api.subscribe(
            subscriptionRequest.apiId, subscriptionRequest.applicationId,
            subscriptionRequest.throttlingPolicy,
        )
            .then((response) => {
                if (response.body.status === SUBSCRIPTION_STATES.UNBLOCKED) {
                    console.log('Subscription created successfully with ID : ' + response.body.subscriptionId);
                    Alert.info(intl.formatMessage({
                        defaultMessage: 'Subscribed successfully',
                        id: 'Apis.Details.Credentials.Wizard.SubscribeToAppStep.subscribed.successfully',
                    }));
                    incrementStep();
                    setStepStatus(stepStatuses.PROCEED);
                } else {
                    setStepStatus(stepStatuses.BLOCKED);
                }
            })
            .catch((error) => {
                console.log('Error while creating the subscription.');
                console.error(error);
            });
    };

    useEffect(() => {
        const newSubscriptionRequest = { ...subscriptionRequest, apiId: apiObject.id };
        if (throttlingPolicyList) {
            const [tierData] = throttlingPolicyList;
            newSubscriptionRequest.throttlingPolicy = tierData.tierName;
        }
        if (createdApp) {
            newSubscriptionRequest.applicationId = createdApp.value;
        }
        setApplicationRequest(newSubscriptionRequest);
        setNewApp(createdApp);
    }, [createdApp]);

    return (
        <>
            <SubscribeToApi
                throttlingPolicyList={throttlingPolicyList}
                applicationsAvailable={[newApp]}
                subscriptionRequest={subscriptionRequest}
                updateSubscriptionRequest={setApplicationRequest}
            />
            <ButtonPanel
                classes={classes}
                currentStep={currentStep}
                handleCurrentStep={subscribeToApplication}
            />
        </>
    );
};

export default injectIntl(subscribeToAppStep);
