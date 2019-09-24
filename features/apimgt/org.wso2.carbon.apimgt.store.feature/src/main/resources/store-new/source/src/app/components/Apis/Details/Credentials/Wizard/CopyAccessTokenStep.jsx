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

import React from 'react';
import ViewToken from 'AppComponents/Shared/AppsAndKeys/ViewToken';
import ButtonPanel from './ButtonPanel';

const copyAccessTokenStep = (props) => {
    const {
        currentStep, createdToken, updateSubscriptionData, classes, handleClickToggle, handleReset, handleRedirectTest,
    } = props;

    const completeStep = () => {
        handleClickToggle('openNew', updateSubscriptionData);
    };

    return (
        <React.Fragment>
            <ViewToken token={{...createdToken, isOauth:true}} />
            <ButtonPanel
                classes={classes}
                currentStep={currentStep}
                handleCurrentStep={completeStep}
                handleReset={handleReset}
                handleRedirectTest={handleRedirectTest}
            />
        </React.Fragment>
    );
};

export default copyAccessTokenStep;
