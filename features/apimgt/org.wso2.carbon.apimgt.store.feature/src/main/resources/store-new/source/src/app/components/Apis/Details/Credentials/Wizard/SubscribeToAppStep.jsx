import React from 'react';
import SubscribeToApi from 'AppComponents/Shared/AppsAndKeys/SubscribeToApi';

const subscribeToAppStep = (props) => {
    const { currentStep, throttlingPolicyList, createdApp } = props;
    if (currentStep === 2) {
        console.log('subscribe app');
    } else if (currentStep === 1) {
        return (
            <SubscribeToApi
                throttlingPolicyList={throttlingPolicyList}
                applicationsAvailable={[createdApp]}
            />
        );
    }
    return '';
};

export default subscribeToAppStep;
