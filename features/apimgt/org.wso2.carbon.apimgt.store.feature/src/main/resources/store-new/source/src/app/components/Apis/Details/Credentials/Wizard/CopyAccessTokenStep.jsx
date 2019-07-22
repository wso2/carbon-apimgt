import React from 'react';
import ViewToken from 'AppComponents/Shared/AppsAndKeys/ViewToken';

const copyAccessTokenStep = (props) => {
    const { currentStep, createdToken } = props;
    if (currentStep > 4) {
        console.log('access token copied');
    } else if (currentStep === 4) {
        return (
            <ViewToken token={createdToken} />
        );
    }
    return '';
};

export default copyAccessTokenStep;
