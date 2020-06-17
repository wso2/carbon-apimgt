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
import PropTypes from 'prop-types';
import Icon from '@material-ui/core/Icon';
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
import Paper from '@material-ui/core/Paper';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Box from '@material-ui/core/Box';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';

const useStyles = makeStyles((theme) => ({
    keyConfigWrapper: {
        paddingLeft: theme.spacing(4),
    },
    radioWrapper: {
        flexDirection: 'row',
    },
    paper: {
        background: 'none',
        marginBottom: theme.spacing(2),
        marginTop: theme.spacing(2),
    },
}));

function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`nav-tabpanel-${index}`}
            aria-labelledby={`nav-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box p={3}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
};

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};

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
    const [keyManagers, setKeyManagers] = useState([]);
    const [selectedTab, setSelectedTab] = useState('Default');

    const [keyRequest, setKeyRequest] = useState({
        keyType: 'PRODUCTION',
        serverSupportedGrantTypes: [],
        supportedGrantTypes: [],
        callbackUrl: '',
        validityTime: 3600,
        additionalProperties: {},
        keyManager: '',
    });

    const {
        currentStep, createdApp, incrementStep, setCreatedKeyType, intl,
        setStepStatus, stepStatuses, setCreatedSelectedTab
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

    /**
    * @param {*} event event
    * @param {*} currentTab current tab
    * @memberof Wizard
    */
   const handleTabChange = (event, newSelectedTab) => {
        setSelectedTab(newSelectedTab);
    };

    useEffect(() => {
        setIsUserOwner(true);
        const api = new API();
        const promisedKeyManagers = api.getKeyManagers();
        promisedKeyManagers
            .then((response) => {
                const responseKeyManagerList = [];
                response.body.list.map((item) => responseKeyManagerList.push(item));
                setKeyManagers(responseKeyManagerList);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
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
                keyRequest.callbackUrl, keyRequest.validityTime, 
                keyRequest.additionalProperties, keyRequest.keyManager
            );
        }).then((response) => {
            if (response.keyState === keyStates.CREATED || response.keyState === keyStates.REJECTED) {
                setStepStatus(stepStatuses.BLOCKED);
            } else {
                incrementStep();
                setCreatedKeyType(keyRequest.keyType);
                setCreatedSelectedTab(selectedTab);
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
                <Paper className={classes.paper}>
                    <Tabs
                        value={selectedTab}
                        indicatorColor="primary"
                        textColor="primary"
                        onChange={handleTabChange}
                        aria-label="key manager tabs"
                    >
                        {keyManagers.map(keymanager => (
                            <Tab label={keymanager.name} value={keymanager.name} disabled={!keymanager.enabled}/>
                        ))}
                        
                    </Tabs>
                    {keyManagers.map(keymanager => (
                        <TabPanel value={selectedTab} index={keymanager.name}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary expandIcon={<Icon>expand_more</Icon>}>
                                    <Typography className={classes.heading} variant='subtitle1'>
                                        <FormattedMessage
                                            defaultMessage='Key Configuration'
                                            id='Shared.AppsAndKeys.TokenManager.key.configuration'
                                        />
                                    </Typography>    
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails className={classes.keyConfigWrapper}>
                                    <KeyConfiguration
                                        updateKeyRequest={setKeyRequest}
                                        keyRequest={keyRequest}
                                        keyType={selectedType}
                                        isUserOwner={isUserOwner}
                                        setGenerateEnabled={setNextActive}
                                        keyManagerConfig={keymanager}
                                        selectedTab={selectedTab}
                                        isKeysAvailable={false}
                                    />
                                    <ButtonPanel
                                        classes={classes}
                                        currentStep={currentStep}
                                        handleCurrentStep={generateKeys}
                                        nextActive={nextActive}
                                    />
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                        </TabPanel>
                    ))}
                </Paper>    
            </div>    
        </>
    );
};

export default injectIntl(generateKeysStep);
