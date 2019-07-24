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
import Tokens from 'AppComponents/Shared/AppsAndKeys/Tokens';
import Application from 'AppData/Application';

const generateAccessTokenStep = (props) => {
    const [tab, setTab] = useState(0);
    const [subscriptionScopes, setSubscriptionScopes] = useState([]);
    const [notFound, setNotFound] = useState(false);
    const [notFound, setNotFound] = useState(false);

    const [accessTokenRequest, setAccessTokenRequest] = useState({
        timeout: 3600,
        scopesSelected: [],
        keyType: '',
    });
    const {
        currentStep, createdApp, setCreatedToke, decrementStep, nextStep, incrementStep,
    } = props;

    useEffect(() => {
        if (currentStep === 3) {
            Application.get(createdApp.value)
                .then((application) => {
                    application.getKeys().then(() => {
                        const keyType = tab === 0 ? 'PRODUCTION' : 'SANDBOX';
                        const newRequest = { ...accessTokenRequest, keyType };
                        const subscriptionScopesList = application.subscriptionScopes
                            .map((scope) => { return scope.scopeKey; });
                        setAccessTokenRequest(newRequest);
                        setSubscriptionScopes(subscriptionScopesList);
                    });
                }).catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.error(error);
                    }
                    const { status } = error;
                    if (status === 404) {
                        setNotFound(true);
                    }
                });
        }
    }, [currentStep]);

    useEffect(() => {
        if (nextStep === 4 && nextStep > currentStep) {
            Application.get(createdApp.value)
                .then((application) => {
                    return application.generateToken(accessTokenRequest.keyType,
                        accessTokenRequest.timeout,
                        accessTokenRequest.scopesSelected);
                })
                .then((response) => {
                    console.log('token generated successfully ' + response);
                    setCreatedToke(response);
                    incrementStep('current');
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.error(error);
                    }
                    const { status } = error;
                    if (status === 404) {
                        setNotFound(true);
                    }
                    decrementStep();
                });
        }
    }, [nextStep]);

    /**
    * @param {*} event event
    * @param {*} currentTab current tab
    * @memberof Wizard
    */
    const handleTabChange = (event, currentTab) => {
        const keyType = currentTab === 0 ? 'PRODUCTION' : 'SANDBOX';
        const newRequest = { ...accessTokenRequest, keyType };
        setTab(currentTab);
        setAccessTokenRequest(newRequest);
    };

    if (currentStep === 3) {
        return (
            <React.Fragment>
                <Tabs
                    value={tab}
                    onChange={handleTabChange}
                    fullWidth
                    indicatorColor='secondary'
                    textColor='secondary'
                >
                    <Tab label='PRODUCTION' />
                    <Tab label='SANDBOX' />
                </Tabs>
                {tab === 0 && (
                    <div>
                        <Tokens
                            updateAccessTokenRequest={setAccessTokenRequest}
                            accessTokenRequest={accessTokenRequest}
                            subscriptionScopes={subscriptionScopes}
                        />
                    </div>
                )}
                {tab === 1 && (
                    <div>
                        <Tokens
                            updateAccessTokenRequest={setAccessTokenRequest}
                            accessTokenRequest={accessTokenRequest}
                            subscriptionScopes={subscriptionScopes}
                        />
                    </div>
                )}
            </React.Fragment>
        );
    }
    return '';
};

export default generateAccessTokenStep;
