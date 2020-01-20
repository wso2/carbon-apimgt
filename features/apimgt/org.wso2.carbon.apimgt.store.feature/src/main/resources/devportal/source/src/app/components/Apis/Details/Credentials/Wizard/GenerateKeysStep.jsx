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
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import KeyConfiguration from 'AppComponents/Shared/AppsAndKeys/KeyConfiguration';
import Application from 'AppData/Application';
import API from 'AppData/api';
import { FormattedMessage, injectIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import cloneDeep from 'lodash.clonedeep';
import ButtonPanel from './ButtonPanel';

const useStyles = makeStyles((theme) => ({
    keyConfigWrapper: {
        paddingLeft: theme.spacing(4),
    },
    radioWrapper: {
        flexDirection: 'row',
    },
}));

const generateKeysStep = (props) => {
    const keyStates = {
        COMPLETED: 'COMPLETED',
        APPROVED: 'APPROVED',
        CREATED: 'CREATED',
        REJECTED: 'REJECTED',
    };
    const [selectedType, setSelectedType] = useState('PRODUCTION');
    const [notFound, setNotFound] = useState(false);
    const [nextActive, setNextActive] = useState(true);
    const [isUserOwner, setIsUserOwner] = useState(false);

    const [keyRequest, setKeyRequest] = useState({
        keyType: 'PRODUCTION',
        serverSupportedGrantTypes: [],
        supportedGrantTypes: [],
        callbackUrl: '',
    });

    const {
        currentStep, createdApp, incrementStep, setCreatedKeyType, intl,
        setStepStatus, stepStatuses,
    } = props;

    /**
    * @param {*} event event
    * @param {*} currentTab current tab
    * @memberof Wizard
    */
    const handleRadioChange = (event) => {
        const newKeyType = event.target.value;
        setSelectedType(newKeyType);
        const newKeyRequest = cloneDeep(keyRequest);
        newKeyRequest.keyType = newKeyType;
        setKeyRequest(newKeyRequest);
    };

    useEffect(() => {
        setIsUserOwner(true);
        const api = new API();
        const promisedSettings = api.getSettings();
        promisedSettings
            .then((response) => {
                const newRequest = cloneDeep(keyRequest);
                newRequest.serverSupportedGrantTypes = response.obj.grantTypes;
                newRequest.supportedGrantTypes = response.obj.grantTypes.filter((item) => item !== 'authorization_code'
                    && item !== 'implicit');
                setKeyRequest(newRequest);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    setNotFound({ notFound: true });
                }
            });
    }, []);

    const generateKeys = () => {
        Application.get(createdApp.value).then((application) => {
            return application.generateKeys(
                keyRequest.keyType, keyRequest.supportedGrantTypes,
                keyRequest.callbackUrl,
            );
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
    const classes = useStyles();

    return (
        <>
            <div className={classes.keyConfigWrapper}>
                <FormControl component='fieldset' className={classes.formControl}>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            defaultMessage='Key Type'
                            id='Apis.Details.Credentials.Wizard.GenerateKeysStep.keyType'
                        />
                    </FormLabel>
                    <RadioGroup value={selectedType} onChange={handleRadioChange} classes={{ root: classes.radioWrapper }}>
                        <FormControlLabel
                            value='PRODUCTION'
                            control={<Radio />}
                            label={intl.formatMessage({
                                defaultMessage: 'PRODUCTION',
                                id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.production',
                            })}
                        />
                        <FormControlLabel
                            value='SANDBOX'
                            control={<Radio />}
                            label={intl.formatMessage({
                                defaultMessage: 'SANDBOX',
                                id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.sandbox',
                            })}
                        />
                    </RadioGroup>
                </FormControl>
                <KeyConfiguration
                    updateKeyRequest={setKeyRequest}
                    keyRequest={keyRequest}
                    keyType={selectedType}
                    isUserOwner={isUserOwner}
                    setGenerateEnabled={setNextActive}
                />
            </div>
            <ButtonPanel
                classes={classes}
                currentStep={currentStep}
                handleCurrentStep={generateKeys}
                nextActive={nextActive}
            />
        </>
    );
};

export default injectIntl(generateKeysStep);
