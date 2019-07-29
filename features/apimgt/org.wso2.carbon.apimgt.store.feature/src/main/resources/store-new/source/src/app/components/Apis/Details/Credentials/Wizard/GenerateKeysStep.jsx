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
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import KeyConfiguration from 'AppComponents/Shared/AppsAndKeys/KeyConfiguration';
import Application from 'AppData/Application';
import { injectIntl } from 'react-intl';

const generateKeysStep = (props) => {
    const keyStates = {
        COMPLETED: 'COMPLETED',
        APPROVED: 'APPROVED',
        CREATED: 'CREATED',
        REJECTED: 'REJECTED',
    };
    const [tab, setTab] = useState(0);
    const [notFound, setNotFound] = useState(false);

    const [keyRequest, setKeyRequest] = useState({
        keyType: 'PRODUCTION',
        supportedGrantTypes: ['client_credentials'],
        callbackUrl: 'https://wso2.am.com',
    });

    const {
        currentStep, createdApp, decrementStep, incrementStep, nextStep, setCreatedKeyType,
        intl, setStepStatus, stepStatuses,
    } = props;

    /**
    * @param {*} event event
    * @param {*} currentTab current tab
    * @memberof Wizard
    */
    const handleTabChange = (event, currentTab) => {
        const keyType = currentTab === 0
            ? intl.formatMessage({
                defaultMessage: 'PRODUCTION',
                id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.tabchange.production',
            })
            : intl.formatMessage({
                defaultMessage: 'SANDBOX',
                id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.tabchange.sandbox',
            });
        const newRequest = { ...keyRequest, keyType };
        setTab(currentTab);
        setKeyRequest(newRequest);
    };

    useEffect(() => {
        if (nextStep === 3 && nextStep > currentStep) {
            Application.get(createdApp.value).then((application) => {
                return application.generateKeys(keyRequest.keyType, keyRequest.supportedGrantTypes,
                    keyRequest.callbackUrl);
            }).then((response) => {
                if (response.keyState === keyStates.CREATED || response.keyState === keyStates.REJECTED) {
                    setStepStatus(stepStatuses.BLOCKED);
                } else {
                    incrementStep('current');
                    setCreatedKeyType(keyRequest.keyType);
                    setStepStatus(stepStatuses.PROCEED);
                    console.log('Keys generated successfully with ID : ' + response);
                }
            }).catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                }
                decrementStep();
            });
        }
    }, [nextStep]);

    if (currentStep === 2) {
        return (
            <React.Fragment>
                <Tabs value={tab} onChange={handleTabChange} fullWidth indicatorColor='secondary' textColor='secondary'>
                    <Tab label={intl.formatMessage({
                        defaultMessage: 'PRODUCTION',
                        id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.production',
                    })}
                    />
                    <Tab label={intl.formatMessage({
                        defaultMessage: 'SANDBOX',
                        id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.sandbox',
                    })}
                    />
                </Tabs>
                {tab === 0 && (
                    <div>
                        <KeyConfiguration
                            updateKeyRequest={setKeyRequest}
                            keyRequest={keyRequest}
                            keyType='PRODUCTION'
                        />
                    </div>
                )}
                {tab === 1 && (
                    <div>
                        <KeyConfiguration
                            updateKeyRequest={setKeyRequest}
                            keyRequest={keyRequest}
                            keyType='SANDBOX'
                        />
                    </div>
                )}
            </React.Fragment>
        );
    }
    return '';
};

export default injectIntl(generateKeysStep);
