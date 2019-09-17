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

import React, { useEffect, useState } from 'react';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import KeyConfiguration from 'AppComponents/Shared/AppsAndKeys/KeyConfiguration';
import Application from 'AppData/Application';
import { injectIntl } from 'react-intl';
import ButtonPanel from './ButtonPanel';
import API from "AppData/api";

const generateKeysStep = (props) => {
    const keyStates = {
        COMPLETED: 'COMPLETED',
        APPROVED: 'APPROVED',
        CREATED: 'CREATED',
        REJECTED: 'REJECTED',
    };
    const [tab, setTab] = useState(0);
    const [notFound, setNotFound] = useState(false);
    const [isUserOwner, setIsUserOwner] = useState(false);

    const [keyRequest, setKeyRequest] = useState({
        keyType: 'PRODUCTION',
        serverSupportedGrantTypes: [],
        supportedGrantTypes: [],
        callbackUrl: '',
    });

    const {
        currentStep, createdApp, incrementStep, setCreatedKeyType, intl,
        setStepStatus, stepStatuses, classes,
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
        setIsUserOwner(true);
        const api = new API();
        const promisedSettings = api.getSettings();
        promisedSettings
            .then((response) => {
                const newRequest = { ...keyRequest };
                newRequest.serverSupportedGrantTypes = response.obj.grantTypes;
                newRequest.supportedGrantTypes = response.obj.grantTypes.filter(item => item !== 'authorization_code'
                    && item !== 'implicit');
                setKeyRequest(newRequest);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }, []);

    const generateKeys = () => {
        Application.get(createdApp.value).then((application) => {
            return application.generateKeys(keyRequest.keyType, keyRequest.supportedGrantTypes,
                keyRequest.callbackUrl);
        }).then((response) => {
            if (response.keyState === keyStates.CREATED || response.keyState === keyStates.REJECTED) {
                setStepStatus(stepStatuses.BLOCKED);
            } else {
                incrementStep();
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
        });
    };

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
                        isUserOwner={isUserOwner}
                    />
                </div>
            )}
            {tab === 1 && (
                <div>
                    <KeyConfiguration
                        updateKeyRequest={setKeyRequest}
                        keyRequest={keyRequest}
                        keyType='SANDBOX'
                        isUserOwner={isUserOwner}
                    />
                </div>
            )}
            <ButtonPanel
                classes={classes}
                currentStep={currentStep}
                handleCurrentStep={generateKeys}
            />
        </React.Fragment>
    );
};

export default injectIntl(generateKeysStep);
