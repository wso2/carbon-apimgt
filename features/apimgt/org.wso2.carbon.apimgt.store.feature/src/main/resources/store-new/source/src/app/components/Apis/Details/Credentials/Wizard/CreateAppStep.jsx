import React, { useState, useEffect } from 'react';
import API from 'AppData/api';
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
                    // eslint-disable-next-line react/no-unused-state
                    this.setState({ notFound: true });
                }
            });
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
    if (currentStep > 1) {
        return '';
    } else if (currentStep === 1) {
        console.log('generated ');
    }
    return (
        <ApplicationCreateForm
            throttlingPolicyList={throttlingPolicyList}
            applicationRequest={applicationRequest}
            updateApplicationRequest={setApplicationRequest}
            validateName={validateName}
            isNameValid={isNameValid}
        />
    );
};

export default createAppStep;
