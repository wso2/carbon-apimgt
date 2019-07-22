import React, { useState, useEffect } from 'react';
import ApplicationCreateForm from 'AppComponents/Shared/AppsAndKeys/ApplicationCreateForm';

const createAppStep = (props) => {
    const [throttlingPolicyList, setThrottlingPolicyList] = useState([]);
    const [applicationRequest, setApplicationRequest] = useState({
        name: '',
        throttlingPolicy: '',
        description: '',
        tokenType: null,
    });
    const [isNameValid, setIsNameValid] = useState(true);

    useEffect(() => {
        const { throttlingPolicyList: newThrottlingPolicyList } = props;
        const newRequest = { ...applicationRequest };
        if (newThrottlingPolicyList.length > 0) {
            [newRequest.throttlingPolicy] = newThrottlingPolicyList;
        }
        setThrottlingPolicyList(newThrottlingPolicyList);
        setApplicationRequest(newRequest);
    }, []);

    const validateName = (value) => {
        if (!value || value.trim() === '') {
            setIsNameValid({ isNameValid: false });
            return Promise.reject(new Error('Application name is required'));
        }
        setIsNameValid({ isNameValid: true });
        return Promise.resolve(true);
    };

    const { currentStep } = props;
    if (currentStep === 1) {
        console.log('application created');
    } else if (currentStep === 0) {
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
