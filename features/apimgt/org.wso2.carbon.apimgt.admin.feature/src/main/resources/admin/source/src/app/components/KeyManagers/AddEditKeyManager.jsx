/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useReducer, useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { makeStyles } from '@material-ui/core/styles';
import Checkbox from '@material-ui/core/Checkbox';
import ChipInput from 'material-ui-chip-input';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import { useIntl, FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { Link as RouterLink } from 'react-router-dom';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import {
    Typography, FormControlLabel, MenuItem,
} from '@material-ui/core';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import cloneDeep from 'lodash.clonedeep';
import Button from '@material-ui/core/Button';
import KeyValidations from 'AppComponents/KeyManagers/KeyValidations';
import isEmpty from 'lodash.isempty';
import Select from '@material-ui/core/Select';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import KeyManagerConfiguration from 'AppComponents/KeyManagers/KeyManagerConfiguration';
import ClaimMappings from 'AppComponents/KeyManagers/ClaimMapping';
import CircularProgress from '@material-ui/core/CircularProgress';
import FormHelperText from '@material-ui/core/FormHelperText';
import Cetificates from 'AppComponents/KeyManagers/Cetificates';

const useStyles = makeStyles((theme) => ({
    root: {
        marginBottom: theme.spacing(10),
    },
    error: {
        color: theme.palette.error.dark,
    },
    hr: {
        border: 'solid 1px #efefef',
    },
    labelRoot: {
        position: 'relative',
    },
    FormControlRoot: {
        width: '100%',
    },
    select: {
        padding: '10.5px 14px',
    },
    chipInputRoot: {
        border: 'solid 1px #ccc',
        borderRadius: 10,
        padding: 10,
        width: '100%',
        '& :before': {
            borderBottom: 'none',
        },
    },
    '@global': {
        '.MuiFormControl-root': {
            marginTop: '20px',
        },
        '.MuiFormControl-root:first-child': {
            marginTop: '0',
        },
    },
    chipHelper: {
        position: 'absolute',
        marginTop: '-5px',
    },
    chipContainer: {
        marginBottom: 8,
    },
}));


/**
 * Reducer
 * @param {JSON} state The second number.
 * @returns {Promise}
 */
function reducer(state, newValue) {
    const { field, value } = newValue;
    switch (field) {
        case 'name':
        case 'description':
        case 'type':
        case 'introspectionEndpoint':
        case 'clientRegistrationEndpoint':
        case 'tokenEndpoint':
        case 'revokeEndpoint':
        case 'userInfoEndpoint':
        case 'authorizeEndpoint':
        case 'issuer':
        case 'scopeManagementEndpoint':
        case 'enableTokenGeneration':
        case 'enableTokenEncryption':
        case 'enableTokenHashing':
        case 'enableMapOAuthConsumerApps':
        case 'enableOAuthAppCreation':
        case 'enableSelfValidationJWT':
        case 'claimMapping':
        case 'additionalProperties':
        case 'availableGrantTypes':
        case 'tokenValidation':
        case 'displayName':
        case 'consumerKeyClaim':
        case 'scopesClaim':
        case 'certificates':
            return { ...state, [field]: value };
        case 'all':
            return value;
        default:
            return newValue;
    }
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function AddEditKeyManager(props) {
    const classes = useStyles();
    const intl = useIntl();
    const [saving, setSaving] = useState(false);
    const { match: { params: { id } }, history } = props;

    const [initialState] = useState({
        name: '',
        description: '',
        displayName: '',
        type: 'select',
        introspectionEndpoint: '',
        clientRegistrationEndpoint: '',
        tokenEndpoint: '',
        revokeEndpoint: '',
        userInfoEndpoint: '',
        authorizeEndpoint: '',
        issuer: '',
        scopeManagementEndpoint: '',
        availableGrantTypes: [],
        enableTokenGeneration: true,
        enableMapOAuthConsumerApps: true,
        enableOAuthAppCreation: true,
        enableSelfValidationJWT: true,
        claimMapping: [],
        tokenValidation: [],
        enabled: true,
        scopesClaim: '',
        consumerKeyClaim: '',
        additionalProperties: {},
        certificates: {
            type: 'PEM',
            value: '',
        },
    });
    const { settings } = useAppContext();
    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        name, description, type, displayName,
        introspectionEndpoint, clientRegistrationEndpoint,
        tokenEndpoint, revokeEndpoint,
        userInfoEndpoint, authorizeEndpoint,
        issuer, scopeManagementEndpoint, availableGrantTypes, consumerKeyClaim, scopesClaim,
        enableTokenGeneration, enableMapOAuthConsumerApps, certificates,
        enableOAuthAppCreation, enableSelfValidationJWT, claimMapping, tokenValidation, additionalProperties,
    } = state;
    const [validating, setValidating] = useState(false);
    const [keymanagerConnectorConfigurations, setKeyManagerConfiguration] = useState([]);
    const restApi = new API();
    const updateKeyManagerConnectorConfiguration = (keyManagerType) => {
        if (settings.keyManagerConfiguration) {
            settings.keyManagerConfiguration.map(({ type: key, configurations }) => {
                if (key === keyManagerType) {
                    setKeyManagerConfiguration(configurations);
                    return true;
                } else {
                    return false;
                }
            });
        }
    };
    useEffect(() => {
        restApi.keyManagerGet(id).then((result) => {
            let editState;
            if (result.body.name !== null) {
                editState = {
                    ...result.body,
                };
            }
            dispatch({ field: 'all', value: editState });
            updateKeyManagerConnectorConfiguration(editState.type);
        });
    }, []);

    const hasErrors = (fieldName, fieldValue, validatingActive) => {
        let error = false;
        if (!validatingActive) {
            return (false);
        }
        switch (fieldName) {
            case 'name':
                if (fieldValue === '') {
                    error = `Key manager name ${intl.formatMessage({
                        id: 'KeyManagers.AddEditKeyManager.is.empty.error',
                        defaultMessage: ' is empty',
                    })}`;
                } else if (fieldValue !== '' && /\s/g.test(fieldValue)) {
                    error = intl.formatMessage({
                        id: 'KeyManagers.AddEditKeyManager.space.error',
                        defaultMessage: 'Key manager name contains white spaces.',
                    });
                }
                break;
            case 'type':
                error = fieldValue === 'select'
                    ? 'Select a key manager type. If the list is empty please refer the documentation.'
                    : false;
                break;
            case 'keyconfig':
                if (fieldValue === '') {
                    error = intl.formatMessage({
                        id: 'KeyManagers.AddEditKeyManager.is.empty.error.key.config',
                        defaultMessage: 'Required field is empty.',
                    });
                }
                break;
            default:
                break;
        }
        return error;
    };

    const onChange = (e) => {
        if (e.target.type === 'checkbox') {
            dispatch({ field: e.target.name, value: e.target.checked });
        } else {
            if (e.target.name === 'type') {
                updateKeyManagerConnectorConfiguration(e.target.value);
            }
            dispatch({ field: e.target.name, value: e.target.value });
        }
    };

    const formHasErrors = (validatingActive = false) => {
        if (hasErrors('name', name, validatingActive)
            || hasErrors('type', type, validatingActive)) {
            return true;
        } else {
            return false;
        }
    };
    const formSaveCallback = () => {
        setValidating(true);
        if (formHasErrors(true)) {
            Alert.error(intl.formatMessage({
                id: 'KeyManagers.AddEditKeyManager.form.has.errors',
                defaultMessage: 'One or more fields contain errors.',
            }));
            return false;
        }
        setSaving(true);

        let promisedAddKeyManager;

        const keymanager = {
            ...state,
        };

        if (id) {
            promisedAddKeyManager = restApi.updateKeyManager(id, keymanager);
        } else {
            promisedAddKeyManager = restApi.addKeyManager(keymanager);
            promisedAddKeyManager
                .then(() => {
                    return (intl.formatMessage({
                        id: 'KeyManager.add.success',
                        defaultMessage: 'Key Manager added successfully.',
                    }));
                });
        }
        promisedAddKeyManager.then(() => {
            if (id) {
                Alert.success(`${displayName} ${intl.formatMessage({
                    id: 'KeyManager.edit.success',
                    defaultMessage: ' - Key Manager edited successfully.',
                })}`);
            } else {
                Alert.success(`${displayName} ${intl.formatMessage({
                    id: 'KeyManager.add.success.msg',
                    defaultMessage: ' - Key Manager added successfully.',
                })}`);
            }
            setSaving(false);
            history.push('/settings/key-managers/');
        }).catch((e) => {
            const { response } = e;
            if (response.body) {
                Alert.error(response.body.description);
            }
            setSaving(false);
        });
        return true;
    };
    const setClaimMapping = (updatedClaimMappings) => {
        dispatch({ field: 'claimMapping', value: updatedClaimMappings });
    };
    const setAdditionalProperties = (key, value) => {
        const clonedAdditionalProperties = cloneDeep(additionalProperties);
        clonedAdditionalProperties[key] = value;
        dispatch({ field: 'additionalProperties', value: clonedAdditionalProperties });
    };
    const addTokenValidation = () => {
        const tokenValidationId = tokenValidation.length + 1;
        const emptyTokenValidation = {
            id: tokenValidationId, type: '', value: '', enable: true,
        };
        const tokenValidationClone = cloneDeep(tokenValidation);
        tokenValidationClone.push(emptyTokenValidation);
        dispatch({ field: 'tokenValidation', value: tokenValidationClone });
    };
    const setTokenValidations = (value) => {
        dispatch({ field: 'tokenValidation', value });
    };

    return (
        <ContentBase
            pageStyle='half'
            title={
                id ? `${intl.formatMessage({
                    id: 'KeyManagers.AddEditKeyManager.title.edit',
                    defaultMessage: 'Key Manager - Edit ',
                })} ${name}` : intl.formatMessage({
                    id: 'KeyManagers.AddEditKeyManager.title.new',
                    defaultMessage: 'Key Manager - Create new',
                })
            }
            help={<div />}
        >


            <Box component='div' m={2} className={classes.root}>
                <Grid container spacing={2}>
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.general.details'
                                defaultMessage='General Details'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.general.details.description'
                                defaultMessage='Provide name and description of the key manager.'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <TextField
                                autoFocus
                                margin='dense'
                                name='name'
                                label={(
                                    <span>
                                        <FormattedMessage
                                            id='KeyManagers.AddEditKeyManager.form.name'
                                            defaultMessage='Name'
                                        />

                                        <span className={classes.error}>*</span>
                                    </span>
                                )}
                                fullWidth
                                variant='outlined'
                                value={name}
                                disabled={!!id}
                                onChange={onChange}
                                error={hasErrors('name', name, validating)}
                                helperText={hasErrors('name', name, validating) || intl.formatMessage({
                                    id: 'Throttling.Advanced.AddEdit.form.name.help',
                                    defaultMessage: 'Name of the key manager.',
                                })}
                            />
                            <TextField
                                margin='dense'
                                name='displayName'
                                label={(
                                    <FormattedMessage
                                        id='Admin.KeyManager.label.DisplayName'
                                        defaultMessage='Display Name'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={displayName}
                                onChange={onChange}
                            />

                            <TextField
                                margin='dense'
                                name='description'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.description'
                                        defaultMessage='Description'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={description}
                                onChange={onChange}
                                helperText={intl.formatMessage({
                                    id: 'Throttling.Advanced.AddEdit.form.description.help',
                                    defaultMessage: 'Description of the key manager.',
                                })}
                            />
                            <FormControl
                                variant='outlined'
                                className={classes.FormControlRoot}
                                error={hasErrors('type', type, validating)}
                            >
                                <InputLabel classes={{ root: classes.labelRoot }}>
                                    <FormattedMessage
                                        defaultMessage='Key Manager Type'
                                        id='Admin.KeyManager.form.type'
                                    />
                                    <span className={classes.error}>*</span>
                                </InputLabel>
                                <Select
                                    name='type'
                                    value={type}
                                    onChange={onChange}
                                    classes={{ select: classes.select }}
                                >
                                    <MenuItem value='select'>
                                        <FormattedMessage
                                            defaultMessage='Select Key Manager Type'
                                            id='Admin.KeyManager.form.type.select'
                                        />
                                    </MenuItem>
                                    {settings.keyManagerConfiguration.map((keymanager) => (
                                        <MenuItem key={keymanager.type} value={keymanager.type}>
                                            {keymanager.type}
                                        </MenuItem>
                                    ))}
                                </Select>
                                <FormHelperText>
                                    {hasErrors('type', type, validating) || (
                                        <FormattedMessage
                                            defaultMessage='Select Key Manager Type'
                                            id='Throttling.Advanced.AddEdit.form.type.help'
                                        />
                                    )}
                                </FormHelperText>
                            </FormControl>
                            <TextField
                                margin='dense'
                                name='issuer'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.Issuer'
                                        defaultMessage='Issuer'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={issuer}
                                onChange={onChange}
                                helperText={intl.formatMessage({
                                    id: 'Throttling.Advanced.AddEdit.form.issuer.help',
                                    defaultMessage: 'Ex: https://localhost:9443/oauth2/token',
                                })}
                            />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.endpoints'
                                defaultMessage='Key Manager Endpoints'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.endpoints.description'
                                defaultMessage={'Configure endpoints such as client registration endpoint, '
                                    + 'the token endpoint for this key manager.'}
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <TextField
                                margin='dense'
                                name='clientRegistrationEndpoint'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.clientRegistrationEndpoint'
                                        defaultMessage='Client Registration Endpoint'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={clientRegistrationEndpoint}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='introspectionEndpoint'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.introspectionEndpoint'
                                        defaultMessage='Introspection Endpoint'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={introspectionEndpoint}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='tokenEndpoint'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.tokenEndpoint'
                                        defaultMessage='Token Endpoint'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={tokenEndpoint}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='revokeEndpoint'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.revokeEndpoint'
                                        defaultMessage='Revoke Endpoint'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={revokeEndpoint}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='userInfoEndpoint'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.userInfoEndpoint'
                                        defaultMessage='UserInfo Endpoint'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={userInfoEndpoint}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='authorizeEndpoint'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.authorizeEndpoint'
                                        defaultMessage='Authorize Endpoint'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={authorizeEndpoint}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='scopeManagementEndpoint'
                                label={(
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.form.scopeManagementEndpoint'
                                        defaultMessage='Scope Management Endpoint'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={scopeManagementEndpoint}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='consumerKeyClaim'
                                label={(
                                    <FormattedMessage
                                        id='Admin.KeyManager.label.ConsumerKey.Claim'
                                        defaultMessage='ConsumerKey Claim URI'
                                    />
                                )}
                                fullWidth
                                variant='outlined'
                                value={consumerKeyClaim}
                                onChange={onChange}
                            />
                            <TextField
                                margin='dense'
                                name='scopesClaim'
                                label={(
                                    <FormattedMessage
                                        id='Admin.KeyManager.label.Scopes.Claim'
                                        defaultMessage='Scopes Claim URI'
                                    />
                                )}

                                fullWidth
                                variant='outlined'
                                value={scopesClaim}
                                onChange={onChange}
                            />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.grant.types'
                                defaultMessage='Grant Types'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.grant.types.description'
                                defaultMessage={'Add the supported grant types by the'
                                    + ' key manager. Press enter to add each grant.'}
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <ChipInput
                                classes={{
                                    root: classes.chipInputRoot,
                                    helperText: classes.chipInputHelpText,
                                    chipContainer: classes.chipContainer,
                                }}
                                value={availableGrantTypes}
                                onAdd={(grantType) => {
                                    availableGrantTypes.push(grantType);
                                }}
                                onDelete={(grantToDelete) => {
                                    const filteredGrantTypes = availableGrantTypes.filter(
                                        (grantType) => grantType !== grantToDelete,
                                    );
                                    dispatch({ field: 'availableGrantTypes', value: filteredGrantTypes });
                                }}
                                helperText={(
                                    <div className={classes.chipHelper}>
                                        {intl.formatMessage({
                                            id: 'Throttling.Advanced.AddEdit.form.claim.help',
                                            defaultMessage: 'Type Available Grant Types and '
                                            + 'press Enter/Return to add them.',
                                        })}
                                    </div>
                                )}
                            />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.cetificate'
                                defaultMessage='Certificates'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.cetificate.description'
                                defaultMessage='Upload or provide the certificate inline.'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Cetificates certificates={certificates} dispatch={dispatch} />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.advanced'
                                defaultMessage='Advanced Configurations'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.advanced.description'
                                defaultMessage='Advanced options for the key manager'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Grid container>
                                <Grid item xs={6} md={4} lg={4}>
                                    <FormControlLabel
                                        value='EnableTokenGeneration'
                                        control={(
                                            <Checkbox
                                                checked={enableTokenGeneration}
                                                onChange={onChange}
                                                name='enableTokenGeneration'
                                                color='primary'
                                            />
                                        )}
                                        label={(
                                            <FormattedMessage
                                                id='Admin.KeyManager.label.Enable.TokenGen'
                                                defaultMessage='Enable Token Generation'
                                            />
                                        )}
                                        labelPlacement='end'
                                    />
                                </Grid>
                                <Grid item xs={6} md={4} lg={4}>
                                    <FormControlLabel
                                        value='enableMapOAuthConsumerApps'
                                        control={(
                                            <Checkbox
                                                checked={enableMapOAuthConsumerApps}
                                                onChange={onChange}
                                                name='enableMapOAuthConsumerApps'
                                                color='primary'
                                            />
                                        )}
                                        label={(
                                            <FormattedMessage
                                                id='Admin.KeyManager.label.Enable.OutOfBandProvisioning'
                                                defaultMessage='Enable Out Of Band Provisioning'
                                            />
                                        )}
                                        labelPlacement='end'
                                    />
                                </Grid>
                                <Grid item xs={6} md={4} lg={4}>
                                    <FormControlLabel
                                        value='enableOAuthAppCreation'
                                        control={(
                                            <Checkbox
                                                checked={enableOAuthAppCreation}
                                                onChange={onChange}
                                                name='enableOAuthAppCreation'
                                                color='primary'
                                            />
                                        )}
                                        label={(
                                            <FormattedMessage
                                                id='Admin.KeyManager.label.Enable.EnableOAithAppCreation'
                                                defaultMessage='Enable Oauth App Creation'
                                            />
                                        )}

                                        labelPlacement='end'
                                    />
                                </Grid>
                                <Grid item xs={6} md={4} lg={4}>
                                    <FormControlLabel
                                        value='enableSelfValidationJWT'
                                        control={(
                                            <Checkbox
                                                checked={enableSelfValidationJWT}
                                                onChange={onChange}
                                                name='enableSelfValidationJWT'
                                                color='primary'
                                            />
                                        )}
                                        label={(
                                            <FormattedMessage
                                                id='Admin.KeyManager.label.Self.Validate.JWT'
                                                defaultMessage='Self Validate JWT'
                                            />
                                        )}
                                        labelPlacement='end'
                                    />
                                </Grid>
                            </Grid>
                        </Box>
                        <Box component='div' m={1}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary
                                    expandIcon={<ExpandMoreIcon />}
                                    aria-controls='panel1a-content'
                                    id='panel1a-header'
                                >
                                    <Typography className={classes.heading}>
                                        <FormattedMessage
                                            id='KeyManagers.AddEditKeyManager.connector.configurations'
                                            defaultMessage='Connector Configurations'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <Box component='div' m={2}>
                                    <KeyManagerConfiguration
                                        keymanagerConnectorConfigurations={keymanagerConnectorConfigurations}
                                        additionalProperties={cloneDeep(additionalProperties)}
                                        setAdditionalProperties={setAdditionalProperties}
                                        hasErrors={hasErrors}
                                        validating={validating}
                                    />
                                </Box>
                            </ExpansionPanel>
                            <ExpansionPanel>
                                <ExpansionPanelSummary
                                    expandIcon={<ExpandMoreIcon />}
                                    aria-controls='panel1a-content'
                                    id='panel1a-header'
                                >
                                    <Typography className={classes.heading}>
                                        <FormattedMessage
                                            id='KeyManagers.AddEditKeyManager.claim.mappings.title'
                                            defaultMessage='Claim Mappings'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ClaimMappings
                                    claimMappings={cloneDeep(claimMapping)}
                                    setClaimMapping={setClaimMapping}
                                />
                            </ExpansionPanel>
                            <Box display='flex' marginTop={3} marginBottom={2}>
                                <Box flex='1'>
                                    <Typography color='inherit' variant='subtitle2' component='div'>
                                        <FormattedMessage
                                            id='KeyManagers.AddEditKeyManager.token.handling.options'
                                            defaultMessage='Token Handling Options'
                                        />
                                    </Typography>
                                </Box>
                                <Button
                                    size='small'
                                    variant='contained'
                                    onClick={addTokenValidation}
                                >
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.add.new'
                                        defaultMessage='Add New Token Handling Option'
                                    />
                                </Button>
                            </Box>
                            <Box>
                                {(isEmpty(tokenValidation)
                                    || (
                                        <KeyValidations
                                            tokenValidations={tokenValidation}
                                            setTokenValidations={setTokenValidations}
                                        />
                                    ))}
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box component='span' m={1}>
                            <Button variant='contained' color='primary' onClick={formSaveCallback}>
                                {saving ? (<CircularProgress size={16} />) : (
                                    <>
                                        {id ? (
                                            <FormattedMessage
                                                id='KeyManagers.AddEditKeyManager.form.update.btn'
                                                defaultMessage='Update'
                                            />
                                        ) : (
                                            <FormattedMessage
                                                id='KeyManagers.AddEditKeyManager.form.add'
                                                defaultMessage='Add'
                                            />
                                        )}
                                    </>
                                )}
                            </Button>
                        </Box>
                        <RouterLink to='/settings/key-managers'>
                            <Button variant='contained'>
                                <FormattedMessage
                                    id='KeyManagers.AddEditKeyManager.form.cancel'
                                    defaultMessage='Cancel'
                                />
                            </Button>
                        </RouterLink>
                    </Grid>
                </Grid>
            </Box>


        </ContentBase>
    );
}

AddEditKeyManager.defaultProps = {
    dataRow: null,
};

AddEditKeyManager.propTypes = {
    dataRow: PropTypes.shape({
        id: PropTypes.string.isRequired,
        description: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
    }),
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
};

export default AddEditKeyManager;
