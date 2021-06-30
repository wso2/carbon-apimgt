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
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import FormHelperText from '@material-ui/core/FormHelperText';
import Alert from 'AppComponents/Shared/Alert';
import Application from 'AppData/Application';
import API from 'AppData/api';
import { FormattedMessage, useIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import Settings from 'Settings';
import ButtonPanel from './ButtonPanel';

const useStyles = makeStyles((theme) => ({
    keyConfigWrapper: {
        paddingLeft: theme.spacing(4),
        flexDirection: 'column',
    },
    radioWrapper: {
        flexDirection: 'row',
    },
    paper: {
        background: 'none',
        marginBottom: theme.spacing(2),
        marginTop: theme.spacing(2),
    },
    subTitle: {
        fontWeight: 400,
    },
    tabPanel: {
        '& .MuiBox-root': {
            padding: 0,
        },
    },
    hr: {
        border: 'solid 1px #efefef',
    },
    muiFormGroupRoot: {
        flexDirection: 'row',
    },
    table: {
        minWidth: '100%',
        '& td, & th': {
            padding: theme.spacing(),
        },
    },
    leftCol: {
        width: 200,
    },
    iconAligner: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
}));

function TabPanel(props) {
    const {
        children, value, index, ...other
    } = props;

    return (
        <div
            role='tabpanel'
            hidden={value !== index}
            id={`nav-tabpanel-${index}`}
            aria-labelledby={`nav-tab-${index}`}
            {...other}
        >
            {value === index && (
                <>{children}</>
            )}
        </div>
    );
}
TabPanel.defaultProps = {
    children: <div />,
};

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.number.isRequired,
    value: PropTypes.number.isRequired,
};

const generateKeysStep = (props) => {
    const intl = useIntl();

    const keyStates = {
        COMPLETED: 'COMPLETED',
        APPROVED: 'APPROVED',
        CREATED: 'CREATED',
        REJECTED: 'REJECTED',
    };
    const [nextActive, setNextActive] = useState(true);
    const [keyManager, setKeyManager] = useState(null);
    const selectedTab = 'Resident Key Manager';

    const [keyRequest, setKeyRequest] = useState({
        keyType: 'SANDBOX',
        supportedGrantTypes: [],
        callbackUrl: '',
        additionalProperties: {},
        keyManager: '',
    });

    const {
        currentStep, createdApp, incrementStep, setCreatedKeyType,
        setStepStatus, stepStatuses, setCreatedSelectedTab,
    } = props;

    useEffect(() => {
        const api = new API();
        const promisedKeyManagers = api.getKeyManagers();
        promisedKeyManagers
            .then((response) => {
                const responseKeyManagerList = [];
                response.body.list.map((item) => responseKeyManagerList.push(item));

                // Selecting a key manager from the list of key managers.
                let selectedKeyManager;
                if (responseKeyManagerList.length > 0) {
                    const responseKeyManagerListDefault = responseKeyManagerList.filter((x) => x.name === 'Resident Key Manager');
                    selectedKeyManager = responseKeyManagerListDefault.length > 0 ? responseKeyManagerListDefault[0]
                        : responseKeyManagerList[0];
                }
                setKeyManager(selectedKeyManager);

                // Setting key request
                try {
                    const newKeyRequest = { ...keyRequest };
                    newKeyRequest.keyManager = selectedKeyManager.id;
                    newKeyRequest.supportedGrantTypes = selectedKeyManager.availableGrantTypes;
                    if (selectedKeyManager.availableGrantTypes.includes('implicit')
                        || selectedKeyManager.availableGrantTypes.includes('authorization_code')) {
                        newKeyRequest.callbackUrl = 'http://localhost';
                    }
                    if (!selectedKeyManager.availableGrantTypes.includes('client_credentials')) {
                        setNextActive(false);
                    }
                    setKeyRequest(newKeyRequest);
                } catch (e) {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.error.keymanager',
                        defaultMessage: 'Error while selecting the key manager',
                    }));
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }, []);

    const generateKeys = () => {
        Application.get(createdApp.value).then((application) => {
            return application.generateKeys(
                keyRequest.keyType, keyRequest.supportedGrantTypes,
                keyRequest.callbackUrl,
                keyRequest.additionalProperties, keyRequest.keyManager,
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
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Credentials.Wizard.GenerateKeysStep.error.404',
                    defaultMessage: 'Resource not found.',
                }));
            }
        });
    };

    const classes = useStyles();

    return (
        <>
            <Box component='div' marginLeft={4}>
                <Grid container spacing={2}>
                    {keyManager && (
                        <>
                            <Grid item xs={12} md={12} lg={3}>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        defaultMessage='Key Configuration'
                                        id='Apis.Details.Credentials.Wizard.GenerateKeysStep.key.configuration'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormHelperText>
                                        <FormattedMessage
                                            defaultMessage={'These configurations are set for the purpose of the wizard.'
                                        + 'You have more control over them when you go to the application view. '}
                                            id='Apis.Details.Credentials.Wizard.GenerateKeysStep.key.configuration.help'
                                        />
                                    </FormHelperText>

                                </Typography>
                            </Grid>
                            <Grid item xs={12} md={12} lg={9}>
                                <Table className={classes.table}>
                                    <TableBody>
                                        <TableRow>
                                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                                <FormattedMessage
                                                    id='Apis.Details.Credentials.Wizard.GenerateKeysStep.config.km.name'
                                                    defaultMessage='Key Manager'
                                                />
                                            </TableCell>
                                            <TableCell>
                                                <div>{keyManager.displayName || keyManager.name}</div>
                                                <Typography variant='caption' component='div'>{keyManager.description}</Typography>
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                                <FormattedMessage
                                                    id='Apis.Details.Credentials.Wizard.GenerateKeysStep.list.environment'
                                                    defaultMessage='Environment'
                                                />
                                            </TableCell>
                                            <TableCell>Sandbox</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                                <FormattedMessage
                                                    id='Apis.Details.Credentials.Wizard.GenerateKeysStep.list.tokenEndpoint'
                                                    defaultMessage='Token Endpoint'
                                                />
                                            </TableCell>
                                            <TableCell>{keyManager.tokenEndpoint}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                                <FormattedMessage
                                                    id='Apis.Details.Credentials.Wizard.GenerateKeysStep.list.revokeEndpoint'
                                                    defaultMessage='Revoke Endpoint'
                                                />
                                            </TableCell>
                                            <TableCell>{keyManager.revokeEndpoint}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                                <FormattedMessage
                                                    id='Apis.Details.Credentials.Wizard.GenerateKeysStep.list.userInfoEndpoint'
                                                    defaultMessage='User Info Endpoint'
                                                />
                                            </TableCell>
                                            <TableCell>{keyManager.userInfoEndpoint}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                                <FormattedMessage
                                                    id='Apis.Details.Credentials.Wizard.GenerateKeysStep.list.grantTypes'
                                                    defaultMessage='Grant Types'
                                                />
                                            </TableCell>
                                            <TableCell>
                                                {keyManager.availableGrantTypes.map((gt) => (
                                                    <span>
                                                        {Settings.grantTypes[gt] || gt}
                                                        ,
                                                        {' '}
                                                    </span>
                                                ))}
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </Grid>
                        </>
                    )}
                </Grid>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <Box component='span' m={1}>
                            <ButtonPanel
                                classes={classes}
                                currentStep={currentStep}
                                handleCurrentStep={generateKeys}
                                nextActive={nextActive}
                            />
                        </Box>

                    </Grid>
                </Grid>
            </Box>
        </>
    );
};

export default generateKeysStep;
