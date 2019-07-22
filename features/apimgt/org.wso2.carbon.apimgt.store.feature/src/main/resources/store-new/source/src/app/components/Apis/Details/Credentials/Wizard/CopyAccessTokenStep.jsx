import React from 'react';
import ViewToken from 'AppComponents/Shared/AppsAndKeys/ViewToken';

const copyAccessTokenStep = (props) => {
    const { currentStep, createdToken } = props;
    if (currentStep > 4) {
        console.log('subscribe app');
    } else if (currentStep === 4) {
        return (
            <ViewToken token={createdToken} />
        );
    }
    return '';
};

export default copyAccessTokenStep;
