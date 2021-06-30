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
import RadioGroup from '@material-ui/core/RadioGroup';
import clsx from 'clsx';
import Radio from '@material-ui/core/Radio';
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
import Collapse from '@material-ui/core/Collapse';
import IconButton from '@material-ui/core/IconButton';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Certificates from 'AppComponents/KeyManagers/Certificates';
import BlockingProgress from 'AppComponents/Shared/BlockingProgress';


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
    importButton: {
        position: 'absolute',
        top: 0,
        right: 0,
        display: 'flex',
        flexDirection: 'row',
        textAlign: 'right',
        alignItems: 'center',
        paddingTop: 10,
        '& p': {
            marginRight: 10,
        },
    },
    expand: {
        transform: 'rotate(0deg)',
        marginLeft: 'auto',
        transition: theme.transitions.create('transform', {
            duration: theme.transitions.duration.shortest,
        }),
    },
    expandOpen: {
        transform: 'rotate(180deg)',
    },
}));

const residentKeyManagerName = 'Resident Key Manager';

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
        case 'wellKnownEndpoint':
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
    const [importingConfig, setImportingConfig] = useState(false);
    const [isResidentKeyManager, setIsResidentKeyManager] = useState(false);
    const { match: { params: { id } }, history } = props;
    const { settings } = useAppContext();

    const defaultKMType = (settings.keyManagerConfiguration
    && settings.keyManagerConfiguration.length > 0)
        ? settings.keyManagerConfiguration[0].type : '';

    const [initialState] = useState({
        name: '',
        description: '',
        displayName: '',
        type: defaultKMType,
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
        tokenValidation: [
            {
                id: 1, type: '', value: '', enable: true,
            },
        ],
        enabled: true,
        scopesClaim: '',
        consumerKeyClaim: '',
        additionalProperties: {},
        certificates: {
            type: 'PEM',
            value: '',
        },
        wellKnownEndpoint: '',
    });
    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        name, description, type, displayName, wellKnownEndpoint,
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
            settings.keyManagerConfiguration.map(({
                type: key, defaultConsumerKeyClaim, defaultScopesClaim, configurations,
            }) => {
                if (key === keyManagerType) {
                    if (!id) {
                        if (defaultConsumerKeyClaim) {
                            dispatch({ field: 'consumerKeyClaim', value: defaultConsumerKeyClaim });
                        }
                    }
                    if (defaultScopesClaim) {
                        dispatch({ field: 'scopesClaim', value: defaultScopesClaim });
                    }
                    setKeyManagerConfiguration(configurations);
                    return true;
                } else {
                    return false;
                }
            });
        }
    };
    useEffect(() => {
        if (id) {
            restApi.keyManagerGet(id).then((result) => {
                let editState;
                if (result.body.name !== null) {
                    const newTokenValidation = (result.body.tokenValidation.length === 0)
                        ? [
                            {
                                id: 1, type: '', value: '', enable: true,
                            },
                        ] : result.body.tokenValidation;

                    editState = {
                        ...result.body, tokenValidation: newTokenValidation,
                    };

                    if (result.body.name === residentKeyManagerName) {
                        setIsResidentKeyManager(true);
                    }
                }
                dispatch({ field: 'all', value: editState });
                updateKeyManagerConnectorConfiguration(editState.type);
            });
        } else {
            updateKeyManagerConnectorConfiguration(defaultKMType);
        }
    }, []);

    const hasErrors = (fieldName, fieldValue, validatingActive) => {
        let error = false;
        if (!validatingActive) {
            return (false);
        }
        switch (fieldName) {
            case 'name':
                if (fieldValue === '') {
                    error = `Key Manager name ${intl.formatMessage({
                        id: 'KeyManagers.AddEditKeyManager.is.empty.error',
                        defaultMessage: ' is empty',
                    })}`;
                } else if (fieldValue !== '' && /\s/g.test(fieldValue)) {
                    error = intl.formatMessage({
                        id: 'KeyManagers.AddEditKeyManager.space.error',
                        defaultMessage: 'Key Manager name contains white spaces.',
                    });
                }
                break;
            case 'keyconfig':
            case 'displayName':
            case 'issuer':
            case 'clientRegistrationEndpoint':
            case 'introspectionEndpoint':
            case 'tokenEndpoint':
            case 'revokeEndpoint':
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
            if (e.target.name === 'enableSelfValidationJWT') {
                dispatch({ field: e.target.name, value: e.target.value === 'selfValidate' });
            } else {
                dispatch({ field: e.target.name, value: e.target.value });
            }
        }
    };

    const formHasErrors = (validatingActive = false) => {
        let connectorConfigHasErrors = false;
        keymanagerConnectorConfigurations.forEach((connector) => {
            if (connector.required && (!additionalProperties[connector.name]
                || additionalProperties[connector.name] === '')) {
                connectorConfigHasErrors = true;
            }
        });

        if (hasErrors('name', name, validatingActive)
            || hasErrors('displayName', displayName, validatingActive)
            || connectorConfigHasErrors
            || hasErrors('issuer', issuer, validatingActive)
            || hasErrors('clientRegistrationEndpoint', clientRegistrationEndpoint, validatingActive)
            || hasErrors('introspectionEndpoint', introspectionEndpoint, validatingActive)
            || hasErrors('tokenEndpoint', tokenEndpoint, validatingActive)
            || hasErrors('revokeEndpoint', revokeEndpoint, validatingActive)
        ) {
            return true;
        } else {
            return false;
        }
    };
    const formSaveCallback = () => {
        setValidating(true);
        if (!isResidentKeyManager && formHasErrors(true)) {
            Alert.error(intl.formatMessage({
                id: 'KeyManagers.AddEditKeyManager.form.has.errors',
                defaultMessage: 'One or more fields contain errors.',
            }));
            return false;
        }
        setSaving(true);

        let promisedAddKeyManager;
        const newTokenValidation = (tokenValidation.length > 0 && tokenValidation[0].type === '')
            ? [] : tokenValidation;


        const keymanager = {
            ...state, tokenValidation: newTokenValidation,
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
    const setTokenValidations = (value) => {
        dispatch({ field: 'tokenValidation', value });
    };
    const importKMConfig = () => {
        const payload = { url: wellKnownEndpoint, type };
        setImportingConfig(true);
        restApi.keyManagersDiscover(payload).then((result) => {
            const { obj: { value } } = result;
            for (const key of Object.keys(value)) {
                if (key === 'name' || key === 'description' || key === 'displayName') {
                    value[key] = state[key];
                } else if (value[key] === null && key === 'additionalProperties') {
                    value[key] = {};
                } else if (value[key] === null
                    && (key === 'enableMapOAuthConsumerApps'
                    || key === 'enableOAuthAppCreation'
                    || key === 'enableSelfValidationJWT'
                    || key === 'enableTokenEncryption'
                    || key === 'enableTokenGeneration'
                    || key === 'enableTokenHashing'
                    || key === 'enabled'
                    )) {
                    value[key] = false;
                } else if (value[key] === null) {
                    value[key] = '';
                } else if (key === 'tokenValidation' && value[key] && value[key].length === 0) {
                    value[key] = state.tokenValidation;
                }
                if (key === 'id') {
                    delete value[key];
                }
            }
            dispatch({ field: 'all', value });
            updateKeyManagerConnectorConfiguration(value.type);
            setImportingConfig(false);
        }).catch((e) => {
            const { response } = e;
            if (response.body) {
                Alert.error(response.body.description);
            }
            setImportingConfig(false);
        });
    };
    const [expanded, setExpanded] = React.useState(false);

    const handleExpandClick = () => {
        setExpanded(!expanded);
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
            {importingConfig && (
                <BlockingProgress message={intl.formatMessage({
                    id: 'KeyManagers.AddEditKeyManager.importing.message',
                    defaultMessage: 'Importing...',
                })}
                />
            )}
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
                                defaultMessage='Provide name and description of the Key Manager.'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            {!isResidentKeyManager && (
                                <Grid container>
                                    <Grid item xs={6}>
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
                                                id: 'KeyManagers.AddEditKeyManager.form.name.help',
                                                defaultMessage: 'Name of the Key Manager.',
                                            })}
                                        />
                                    </Grid>
                                    <Grid item xs={6}>
                                        <Box ml={1}>
                                            <TextField
                                                autoFocus={!!id}
                                                margin='dense'
                                                name='displayName'
                                                fullWidth
                                                variant='outlined'
                                                value={displayName}
                                                onChange={onChange}
                                                label={(
                                                    <span>
                                                        <FormattedMessage
                                                            id='Admin.KeyManager.label.DisplayName'
                                                            defaultMessage='Display Name'
                                                        />
                                                        <span className={classes.error}>*</span>
                                                    </span>
                                                )}
                                                error={hasErrors('displayName', displayName, validating)}
                                                helperText={hasErrors('displayName', displayName, validating)
                                            || intl.formatMessage({
                                                id: 'KeyManagers.AddEditKeyManager.form.displayName.help',
                                                defaultMessage: 'Display Name of the Key Manager.',
                                            })}
                                            />
                                        </Box>
                                    </Grid>
                                </Grid>
                            )}


                            <TextField
                                multiline
                                rows={4}
                                rowsMax={10}
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
                                    id: 'KeyManagers.AddEditKeyManager.form.description.help',
                                    defaultMessage: 'Description of the Key Manager.',
                                })}
                            />
                            {!isResidentKeyManager && (
                                <>
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
                                            {settings.keyManagerConfiguration.map((keymanager) => (
                                                <MenuItem key={keymanager.type} value={keymanager.type}>
                                                    {keymanager.displayName || keymanager.type}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                        <FormHelperText>
                                            {hasErrors('type', type, validating) || (
                                                <FormattedMessage
                                                    defaultMessage='Select Key Manager Type'
                                                    id='KeyManagers.AddEditKeyManager.form.type.help'
                                                />
                                            )}
                                        </FormHelperText>
                                    </FormControl>
                                    <Box display='flex' mt={2} alignItems='flex-start'>
                                        <TextField
                                            margin='dense'
                                            name='wellKnownEndpoint'
                                            fullWidth
                                            variant='outlined'
                                            value={wellKnownEndpoint}
                                            onChange={onChange}
                                            label={(
                                                <FormattedMessage
                                                    id='KeyManagers.AddEditKeyManager.form.wellKnownUrl'
                                                    defaultMessage='Well-known URL'
                                                />
                                            )}
                                            helperText={intl.formatMessage({
                                                id: 'KeyManagers.AddEditKeyManager.form.wellKnownUrl.help',
                                                defaultMessage: 'Provide a well-known URL and discover'
                                            + ' the Key Manager information.',
                                            })}
                                        />
                                        <Box ml={1}>
                                            <Button
                                                margin='dense'
                                                variant='outlined'
                                                disabled={!wellKnownEndpoint}
                                                onClick={importKMConfig}
                                            >
                                                <FormattedMessage
                                                    id='KeyManagers.AddEditKeyManager.form.import.button'
                                                    defaultMessage='Import'
                                                />
                                            </Button>
                                        </Box>

                                    </Box>
                                    <TextField
                                        margin='dense'
                                        name='issuer'
                                        fullWidth
                                        variant='outlined'
                                        value={issuer}
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='KeyManagers.AddEditKeyManager.form.Issuer'
                                                    defaultMessage='Issuer'
                                                />
                                                <span className={classes.error}>*</span>
                                            </span>
                                        )}
                                        error={hasErrors('issuer', issuer, validating)}
                                        helperText={hasErrors('issuer', issuer, validating) || intl.formatMessage({
                                            id: 'KeyManagers.AddEditKeyManager.form.issuer.help',
                                            defaultMessage: 'E.g.,: https://localhost:9443/oauth2/token',
                                        })}
                                    />
                                </>
                            )}
                        </Box>
                    </Grid>
                    {!isResidentKeyManager && (
                        <>
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
                                    + 'the token endpoint for this Key Manager.'}
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={12} md={12} lg={9}>
                                <Box component='div' m={1}>
                                    <TextField
                                        margin='dense'
                                        name='clientRegistrationEndpoint'
                                        fullWidth
                                        variant='outlined'
                                        value={clientRegistrationEndpoint}
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='KeyManagers.AddEditKeyManager.form.clientRegistrationEndpoint'
                                                    defaultMessage='Client Registration Endpoint'
                                                />
                                                <span className={classes.error}>*</span>
                                            </span>
                                        )}
                                        error={hasErrors('clientRegistrationEndpoint',
                                            clientRegistrationEndpoint, validating)}
                                        helperText={hasErrors('clientRegistrationEndpoint',
                                            clientRegistrationEndpoint, validating)
                                || intl.formatMessage({
                                    id: 'KeyManagers.AddEditKeyManager.form.clientRegistrationEndpoint.help',
                                    defaultMessage: 'E.g., https://localhost:9444/client-registration/v0.17/register',
                                })}
                                    />
                                    <TextField
                                        margin='dense'
                                        name='introspectionEndpoint'
                                        fullWidth
                                        variant='outlined'
                                        value={introspectionEndpoint}
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='KeyManagers.AddEditKeyManager.form.introspectionEndpoint'
                                                    defaultMessage='Introspection Endpoint'
                                                />
                                                <span className={classes.error}>*</span>
                                            </span>
                                        )}
                                        error={hasErrors('introspectionEndpoint', introspectionEndpoint, validating)}
                                        helperText={hasErrors('introspectionEndpoint',
                                            introspectionEndpoint,
                                            validating)
                                || intl.formatMessage({
                                    id: 'KeyManagers.AddEditKeyManager.form.introspectionEndpoint.help',
                                    defaultMessage: 'E.g., https://localhost:9443/oauth2/introspect',
                                })}
                                    />
                                    <TextField
                                        margin='dense'
                                        name='tokenEndpoint'
                                        fullWidth
                                        variant='outlined'
                                        value={tokenEndpoint}
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='KeyManagers.AddEditKeyManager.form.tokenEndpoint'
                                                    defaultMessage='Token Endpoint'
                                                />
                                                <span className={classes.error}>*</span>
                                            </span>
                                        )}
                                        error={hasErrors('tokenEndpoint', tokenEndpoint, validating)}
                                        helperText={hasErrors('tokenEndpoint', tokenEndpoint, validating)
                                || intl.formatMessage({
                                    id: 'KeyManagers.AddEditKeyManager.form.tokenEndpoint.help',
                                    defaultMessage: 'E.g., https://localhost:9443/oauth2/token',
                                })}
                                    />
                                    <TextField
                                        margin='dense'
                                        name='revokeEndpoint'
                                        fullWidth
                                        variant='outlined'
                                        value={revokeEndpoint}
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='KeyManagers.AddEditKeyManager.form.revokeEndpoint'
                                                    defaultMessage='Revoke Endpoint'
                                                />
                                                <span className={classes.error}>*</span>
                                            </span>
                                        )}
                                        error={hasErrors('revokeEndpoint', revokeEndpoint, validating)}
                                        helperText={hasErrors('revokeEndpoint', revokeEndpoint, validating)
                                || intl.formatMessage({
                                    id: 'KeyManagers.AddEditKeyManager.form.revokeEndpoint.help',
                                    defaultMessage: 'E.g., https://localhost:9443/oauth2/revoke',
                                })}
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
                                        helperText={intl.formatMessage({
                                            id: 'KeyManagers.AddEditKeyManager.form.userInfoEndpoint.help',
                                            defaultMessage: 'E.g., https://localhost:9443/oauth2/userInfo',
                                        })}
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
                                        helperText={intl.formatMessage({
                                            id: 'KeyManagers.AddEditKeyManager.form.authorizeEndpoint.help',
                                            defaultMessage: 'E.g., https://localhost:9443/oauth2/userinfo',
                                        })}
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
                                        helperText={intl.formatMessage({
                                            id: 'KeyManagers.AddEditKeyManager.form.scopeManagementEndpoint.help',
                                            defaultMessage: 'E.g, https://localhost:9443/oauth2/scope',
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
                                        id='KeyManagers.AddEditKeyManager.claim.uris'
                                        defaultMessage='Claim URIs'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.claim.uris.description'
                                        defaultMessage='Provide claim URIs for consumer key and scopes.'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={12} md={12} lg={9}>
                                <Box component='div' m={1}>
                                    <TextField
                                        margin='dense'
                                        name='consumerKeyClaim'
                                        label={(
                                            <FormattedMessage
                                                id='Admin.KeyManager.label.ConsumerKey.Claim'
                                                defaultMessage='Consumer Key Claim URI'
                                            />
                                        )}
                                        fullWidth
                                        variant='outlined'
                                        value={consumerKeyClaim}
                                        onChange={onChange}
                                        helperText={intl.formatMessage({
                                            id: 'KeyManagers.AddEditKeyManager.form.consumerKeyClaim.help',
                                            defaultMessage: 'Provide consumer key claim URIs.',
                                        })}
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
                                        helperText={intl.formatMessage({
                                            id: 'KeyManagers.AddEditKeyManager.form.scopesClaim.help',
                                            defaultMessage: 'Provide scope claim URI.',
                                        })}
                                    />
                                </Box>
                            </Grid>
                        </>
                    )}
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
                                    + ' Key Manager. Press enter to add each grant.'}
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
                                            id: 'KeyManagers.AddEditKeyManager.form.claim.help',
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
                                id='KeyManagers.AddEditKeyManager.certificate'
                                defaultMessage='Certificates'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='KeyManagers.AddEditKeyManager.certificate.description'
                                defaultMessage='Upload or provide the certificate inline.'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Certificates certificates={certificates} dispatch={dispatch} />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {(keymanagerConnectorConfigurations && keymanagerConnectorConfigurations.length > 0) && (
                        <>
                            <Grid item xs={12} md={12} lg={3}>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.connector.configurations'
                                        defaultMessage='Connector Configurations'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.connector.configurations.description'
                                        defaultMessage='Provide connection params for the selected Key Manager.'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={12} md={12} lg={9}>
                                <Box component='div' m={1}>
                                    <KeyManagerConfiguration
                                        keymanagerConnectorConfigurations={keymanagerConnectorConfigurations}
                                        additionalProperties={cloneDeep(additionalProperties)}
                                        setAdditionalProperties={setAdditionalProperties}
                                        hasErrors={hasErrors}
                                        validating={validating}
                                    />
                                </Box>
                            </Grid>
                            <Grid item xs={12}>
                                <Box marginTop={2} marginBottom={2}>
                                    <hr className={classes.hr} />
                                </Box>
                            </Grid>
                        </>
                    ) }
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
                                defaultMessage='Advanced options for the Key Manager'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Grid container>
                                <Grid item xs={6} md={4} lg={4}>
                                    <FormControlLabel
                                        value='enableTokenGeneration'
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
                                                defaultMessage='Token Generation'
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
                                                defaultMessage='Out Of Band Provisioning'
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
                                                defaultMessage='Oauth App Creation'
                                            />
                                        )}

                                        labelPlacement='end'
                                    />
                                </Grid>
                            </Grid>
                        </Box>
                        <Box component='div' m={1}>
                            <Box display='flex' marginTop={3} marginBottom={2}>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.token.validation.method'
                                        defaultMessage='Token Validation Method'
                                    />
                                </Typography>
                            </Box>
                            <Box>
                                <FormControl component='fieldset'>
                                    <RadioGroup
                                        aria-label={intl.formatMessage({
                                            id: 'KeyManagers.AddEditKeyManager.token.validation.method',
                                            defaultMessage: 'Token Validation Method',
                                        })}
                                        name='enableSelfValidationJWT'
                                        value={enableSelfValidationJWT ? 'selfValidate' : 'useIntrospect'}
                                        onChange={onChange}
                                        style={{ flexDirection: 'row', paddingLeft: 16 }}
                                    >
                                        <FormControlLabel
                                            value='selfValidate'
                                            control={<Radio />}
                                            label={intl.formatMessage({
                                                id: 'KeyManagers.AddEditKeyManager.selfvalidate',
                                                defaultMessage: 'Self validate JWT',
                                            })}
                                        />
                                        <FormControlLabel
                                            value='useIntrospect'
                                            control={<Radio />}
                                            label={intl.formatMessage({
                                                id: 'KeyManagers.AddEditKeyManager.useIntrospect',
                                                defaultMessage: 'Use introspect',
                                            })}
                                        />
                                    </RadioGroup>
                                </FormControl>
                            </Box>
                            <Box display='flex' marginTop={3} marginBottom={2}>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.token.handling.options'
                                        defaultMessage='Token Handling Options'
                                    />
                                </Typography>
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
                            <Box display='flex' marginTop={3} marginBottom={2}>
                                <Typography
                                    color='inherit'
                                    variant='subtitle2'
                                    component='a'
                                    onClick={handleExpandClick}
                                    style={{ cursor: 'pointer' }}
                                >
                                    <FormattedMessage
                                        id='KeyManagers.AddEditKeyManager.claim.mappings.title'
                                        defaultMessage='Claim Mappings'
                                    />
                                </Typography>
                                <IconButton
                                    className={clsx(classes.expand, {
                                        [classes.expandOpen]: expanded,
                                    })}
                                    onClick={handleExpandClick}
                                    aria-expanded={expanded}
                                    aria-label='show more'
                                >
                                    <ExpandMoreIcon />
                                </IconButton>
                            </Box>
                            <Box>
                                <Collapse in={expanded} timeout='auto' unmountOnExit>
                                    <ClaimMappings
                                        claimMappings={cloneDeep(claimMapping)}
                                        setClaimMapping={setClaimMapping}
                                    />
                                </Collapse>
                                {!expanded && (
                                    <Typography
                                        color='inherit'
                                        variant='caption'
                                        component='div'
                                        style={{ paddingLeft: 16 }}
                                    >
                                        <FormattedMessage
                                            id='KeyManagers.AddEditKeyManager.claim.mappings.hidden.help'
                                            defaultMessage='Expand to add edit claim mappings'
                                        />
                                    </Typography>
                                )}
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
