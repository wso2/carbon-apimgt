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

import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import ApplicationCreateForm from 'AppComponents/Shared/AppsAndKeys/ApplicationCreateForm';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import cloneDeep from 'lodash.clonedeep';
import { injectIntl, FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import ButtonPanel from './ButtonPanel';

const useStyles = makeStyles((theme) => ({
    appCreateFormWrapper: {
        paddingLeft: theme.spacing(2),
    },
    warningIcon: {
        color: '#ff9a00',
        fontSize: 43,
        marginRight: 10,
    },
}));

const createAppStep = (props) => {
    const APPLICATION_STATES = {
        CREATED: 'CREATED',
        APPROVED: 'APPROVED',
        REJECTED: 'REJECTED',
    };
    const [throttlingPolicyList, setThrottlingPolicyList] = useState([]);
    const [applicationRequest, setApplicationRequest] = useState({
        name: '',
        throttlingPolicy: '',
        description: '',
        tokenType: 'JWT',
        groups: null,
        attributes: {},
    });
    const [isNameValid, setIsNameValid] = useState(true);
    const [allAppAttributes, setAllAppAttributes] = useState(null);
    const [hasValidKM, setHasValidKM] = useState(null);
    const {
        currentStep, setCreatedApp, incrementStep, intl, setStepStatus, stepStatuses,
    } = props;
    const { api: apiObject } = useContext(ApiContext);

    const validateName = (value) => {
        if (!value || value.trim() === '') {
            setIsNameValid({ isNameValid: false });
            return Promise.reject(new Error(intl.formatMessage({
                defaultMessage: 'Application name is required',
                id: 'Apis.Details.Credentials.Wizard.CreateAppStep.application.name.is.required',
            })));
        }
        setIsNameValid({ isNameValid: true });
        return Promise.resolve(true);
    };

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof ApplicationFormHandler
     */
    const isRequiredAttribute = (name) => {
        if (allAppAttributes) {
            for (let i = 0; i < allAppAttributes.length; i++) {
                if (allAppAttributes[i].attribute === name) {
                    return allAppAttributes[i].required === 'true';
                }
            }
        }
        return false;
    };

    /**
     * @param {object} name application attribute name
     * @returns {Object} attribute value
     * @memberof ApplicationFormHandler
     */
    const getAttributeValue = (name) => {
        return applicationRequest.attributes[name];
    };

    const createApplication = () => {
        const api = new API();
        if (!applicationRequest.name || applicationRequest.name.trim() === '') {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Application name is required',
                id: 'Apis.Details.Credentials.Wizard.CreateAppStep.application.name.is.required',
            }));
            return;
        }
        api.createApplication(applicationRequest)
            .then((response) => {
                const data = response.body;
                if (data.status === APPLICATION_STATES.APPROVED) {
                    const appCreated = { value: data.applicationId, label: data.name };
                    console.log('Application created successfully.');
                    setCreatedApp(appCreated);
                    incrementStep();
                    setStepStatus(stepStatuses.PROCEED);
                } else {
                    setStepStatus(stepStatuses.BLOCKED);
                }
            })
            .catch((error) => {
                const { response } = error;
                if (response && response.body) {
                    const message = response.body.description || intl.formatMessage({
                        defaultMessage: 'Error while creating the application',
                        id: 'Apis.Details.Credentials.Wizard.CreateAppStep.error.while.creating.the.application',
                    });
                    Alert.error(message);
                } else {
                    Alert.error(error.message);
                }
                console.error('Error while creating the application');
            });
    };

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof ApplicationFormHandler
     */
    const handleAttributesChange = (name) => (event) => {
        const newApplicationRequest = cloneDeep(applicationRequest);
        newApplicationRequest.attributes[name] = event.target.value;
        setApplicationRequest(newApplicationRequest);
    };

    /**
     * add a new group function
     * @param {*} chip newly added group
     * @param {*} appGroups already existing groups
     */
    const handleAddChip = (chip, appGroups) => {
        const newRequest = { ...applicationRequest };
        let values = appGroups || [];
        values = values.slice();
        values.push(chip);
        newRequest.groups = values;
        setApplicationRequest(newRequest);
    };

    /**
     * remove a group from already existing groups function
     * @param {*} chip selected group to be removed
     * @param {*} index selected group index to be removed
     * @param {*} appGroups already existing groups
     */
    const handleDeleteChip = (chip, index, appGroups) => {
        const newRequest = { ...applicationRequest };
        let values = appGroups || [];
        values = values.filter((v) => v !== chip);
        newRequest.groups = values;
        setApplicationRequest(newRequest);
    };

    useEffect(() => {
        // Get all the tiers to populate the drop down.
        const api = new API();
        const promiseTiers = api.getAllTiers('application');
        const promisedAttributes = api.getAllApplicationAttributes();
        const promisedKeyManagers = api.getKeyManagers();

        Promise.all([promiseTiers, promisedAttributes, promisedKeyManagers])
            .then((response) => {
                const [tierResponse, allAttributes, keyManagers] = response;
                const throttlingPolicyListLocal = tierResponse.body.list.map((item) => item.name);
                const newRequest = { ...applicationRequest };
                if (throttlingPolicyListLocal.length > 0) {
                    [newRequest.throttlingPolicy] = throttlingPolicyListLocal;
                }
                const allAppAttr = [];
                allAttributes.body.list.map((item) => allAppAttr.push(item));
                if (allAttributes.length > 0) {
                    newRequest.attributes = allAppAttr.filter((item) => !item.hidden);
                }
                // Selecting the resident key manager
                const responseKeyManagerList = [];
                keyManagers.body.list.map((item) => responseKeyManagerList.push(item));

                let hasValidKMInner;
                if (responseKeyManagerList.length > 0) {
                    const responseKeyManagerListDefault = responseKeyManagerList.filter(
                        (x) => x.name === 'Resident Key Manager' && x.enabled,
                    );
                    hasValidKMInner = responseKeyManagerListDefault.length !== 0;
                }
                setHasValidKM(hasValidKMInner);
                setApplicationRequest(newRequest);
                setThrottlingPolicyList(throttlingPolicyListLocal);
                setAllAppAttributes(allAppAttr);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Credentials.Wizard.CreateAppStep.error.404',
                        defaultMessage: 'Resource not found',
                    }));
                }
            });
    }, []);

    const classes = useStyles();
    if (!hasValidKM) {
        return (
            <Box mb={1} ml={4}>
                <InlineMessage type='warn'>
                    <FormattedMessage
                        id='Apis.Details.Credentials.Wizard.CreateAppStep.default.km.msg'
                        defaultMessage={'Wizard is only accessible via the Resident Key Manager.'
                                + 'But the Resident Key Manager is disabled at the moment.'}
                    />
                </InlineMessage>
                <Box mt={2}>
                    <Link to={`/apis/${apiObject.id}/credentials`}>
                        <Button variant='contained'>
                            <FormattedMessage
                                id='Apis.Details.Credentials.Wizard.CreateAppStep.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                    </Link>
                </Box>
            </Box>
        );
    }

    return (
        <div className={classes.appCreateFormWrapper}>
            <Box px={2} display='flex' justifyContent='flex-start'>
                <Grid item xs={10} md={6}>
                    <ApplicationCreateForm
                        throttlingPolicyList={throttlingPolicyList}
                        applicationRequest={applicationRequest}
                        updateApplicationRequest={setApplicationRequest}
                        validateName={validateName}
                        isNameValid={isNameValid}
                        allAppAttributes={allAppAttributes}
                        handleAttributesChange={handleAttributesChange}
                        isRequiredAttribute={isRequiredAttribute}
                        getAttributeValue={getAttributeValue}
                        handleDeleteChip={handleDeleteChip}
                        handleAddChip={handleAddChip}
                    />
                </Grid>
            </Box>
            <ButtonPanel
                classes={classes}
                currentStep={currentStep}
                handleCurrentStep={createApplication}
            />
        </div>
    );
};

export default injectIntl(createAppStep);
