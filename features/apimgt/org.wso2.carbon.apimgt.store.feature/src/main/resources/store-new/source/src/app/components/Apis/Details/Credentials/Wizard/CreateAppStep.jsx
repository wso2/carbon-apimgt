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
import ApplicationCreateForm from 'AppComponents/Shared/AppsAndKeys/ApplicationCreateForm';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

const createAppStep = (props) => {
    const [throttlingPolicyList, setThrottlingPolicyList] = useState([]);
    const [applicationRequest, setApplicationRequest] = useState({
        name: '',
        throttlingPolicy: '',
        description: '',
        tokenType: null,
    });
    const [isNameValid, setIsNameValid] = useState(true);
    const [notFound, setNotFound] = useState(false);
    const {
        currentStep, setCreatedApp, decrementStep, nextStep, incrementStep,
    } = props;

    const validateName = (value) => {
        if (!value || value.trim() === '') {
            setIsNameValid({ isNameValid: false });
            return Promise.reject(new Error('Application name is required'));
        }
        setIsNameValid({ isNameValid: true });
        return Promise.resolve(true);
    };

    useEffect(() => {
        const api = new API();
        const promiseTiers = api.getAllTiers('application');
        promiseTiers
            .then((response) => {
                const newThrottlingPolicyList = response.body.list.map(item => item.name);
                const newRequest = { ...applicationRequest };
                if (newThrottlingPolicyList.length > 0) {
                    [newRequest.throttlingPolicy] = newThrottlingPolicyList;
                }
                setThrottlingPolicyList(newThrottlingPolicyList);
                setApplicationRequest(newRequest);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                }
            });
    }, []);

    // creates an application application if current step is 1
    useEffect(() => {
        if (nextStep === 1 && nextStep > currentStep) {
            const api = new API();
            validateName(applicationRequest.name)
                .then(() => api.createApplication(applicationRequest))
                .then((response) => {
                    const data = JSON.parse(response.data);
                    const appCreated = { value: data.applicationId, label: data.name };
                    console.log('Application created successfully.');
                    setCreatedApp(appCreated);
                    incrementStep('current');
                })
                .catch((error) => {
                    const { response } = error;
                    if (response && response.body) {
                        const message = response.body.description || 'Error while creating the application';
                        Alert.error(message);
                    } else {
                        Alert.error(error.message);
                    }
                    decrementStep();
                    console.error('Error while creating the application');
                });
        }
    }, [nextStep]);

    if (currentStep === 0) {
        return (
            <ApplicationCreateForm
                throttlingPolicyList={throttlingPolicyList}
                applicationRequest={applicationRequest}
                updateApplicationRequest={setApplicationRequest}
                validateName={validateName}
                isNameValid={isNameValid}
            />
        );
    }
    return '';
};

export default createAppStep;
