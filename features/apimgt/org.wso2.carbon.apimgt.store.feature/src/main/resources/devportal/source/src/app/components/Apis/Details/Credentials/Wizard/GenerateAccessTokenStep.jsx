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
import Tokens from 'AppComponents/Shared/AppsAndKeys/Tokens';
import Application from 'AppData/Application';
import { makeStyles } from '@material-ui/core/styles';
import { defineMessages, useIntl } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Alert from 'AppComponents/Shared/Alert';
import ButtonPanel from './ButtonPanel';

const useStyles = makeStyles((theme) => ({
    tokenWrapper: {
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(7),
    },
    title: {
        paddingLeft: theme.spacing(2),
    },
}));

const generateAccessTokenStep = (props) => {
    const intl = useIntl();
    const [keyType, setKeyType] = useState('PRODUCTION');
    const [selectedTab, setSelectedTab] = useState('Resident Key Manager');
    const [subscriptionScopes, setSubscriptionScopes] = useState([]);

    const [accessTokenRequest, setAccessTokenRequest] = useState({
        timeout: 3600,
        scopesSelected: [],
        keyType: '',
    });
    const {
        currentStep, createdApp, setCreatedToken, incrementStep, createdKeyType, createdSelectedTab,
    } = props;

    useEffect(() => {
        const newRequest = { ...accessTokenRequest, keyType: createdKeyType };
        setKeyType(createdKeyType);
        setSelectedTab(createdSelectedTab);
        setAccessTokenRequest(newRequest);
    }, [createdKeyType, createdSelectedTab]);

    useEffect(() => {
        Application.get(createdApp.value)
            .then((application) => {
                application.getKeys().then(() => {
                    const subscriptionScopesList = application.subscriptionScopes
                        .map((scope) => { return scope.key; });
                    setSubscriptionScopes(subscriptionScopesList);
                });
            }).catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Credentials.Wizard.GenerateAccessTokenStep.error.404',
                        defaultMessage: 'Resource not found',
                    }));
                }
            });
    }, []);

    const generateAccessToken = () => {
        setSelectedTab(createdSelectedTab);
        Application.get(createdApp.value)
            .then((application) => {
                return application.generateToken(
                    selectedTab,
                    accessTokenRequest.keyType,
                    accessTokenRequest.timeout,
                    accessTokenRequest.scopesSelected,
                );
            })
            .then((response) => {
                console.log('token generated successfully ' + response);
                setCreatedToken(response);
                incrementStep();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Credentials.Wizard.GenerateAccessTokenStep.error.404',
                        defaultMessage: 'Resource not found',
                    }));
                }
            });
    };
    const classes = useStyles();
    const messages = defineMessages({
        dataInfo: {
            id: 'Apis.Details.Credentials.Wizard.GenerateAccessTokenStep',
            defaultMessage: 'Generate Access Token for {keyType} environment',
        },
    });

    return (
        <>
            <div className={classes.tokenWrapper}>
                <Typography variant='subtitle1' component='div' className={classes.title}>
                    {intl.formatMessage(messages.dataInfo, { keyType })}
                </Typography>
                <Tokens
                    updateAccessTokenRequest={setAccessTokenRequest}
                    accessTokenRequest={accessTokenRequest}
                    subscriptionScopes={subscriptionScopes}
                />
                <ButtonPanel
                    classes={classes}
                    currentStep={currentStep}
                    handleCurrentStep={generateAccessToken}
                />
            </div>
        </>
    );
};

export default generateAccessTokenStep;
