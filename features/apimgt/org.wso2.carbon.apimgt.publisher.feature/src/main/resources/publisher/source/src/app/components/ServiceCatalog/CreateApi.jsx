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
import React, { useState, useReducer } from 'react';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import MenuItem from '@material-ui/core/MenuItem';
import Grid from '@material-ui/core/Grid';
import Dialog from '@material-ui/core/Dialog';
import AddCircleIcon from '@material-ui/icons/AddCircle';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import TextField from '@material-ui/core/TextField';
import { useHistory } from 'react-router-dom';
import APIValidation from 'AppData/APIValidation';
import Alert from 'AppComponents/Shared/Alert';
import Banner from 'AppComponents/Shared/Banner';
import { FormattedMessage, useIntl } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';

import API from 'AppData/api';

const useStyles = makeStyles((theme) => ({
    buttonStyle: {
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
        marginRight: theme.spacing(2),
    },
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
    },
    actionButtonStyle: {
        marginBottom: theme.spacing(2),
        marginRight: theme.spacing(2),
    },
    mandatoryLabelStyle: {
        marginLeft: theme.spacing(2),
        marginBottom: theme.spacing(2),
    },
    textStyle: {
        fontSize: 11,
    },
    topMarginSpacing: {
        marginTop: theme.spacing(2),
    },
}));

/**
 *
 * Return the actual API context that will be exposed in the gateway.
 * If the context value contains `{version}` placeholder text it will be replaced with the actual version value.
 * If there is no such placeholder text in the context, The version will be appended to the context
 * i:e /context/version
 * Parameter expect an object containing `context` and `version` properties.
 * @param {String} context API Context
 * @param {String} version API Version string
 * @returns {String} Derived actual context string
 */
function actualContext({ context, version }) {
    let initialContext = '{context}/{version}';
    if (context) {
        initialContext = context;
        if (context.indexOf('{version}') < 0) {
            initialContext = context + '/{version}';
        }
    }
    if (version) {
        initialContext = initialContext.replace('{version}', version);
    }
    return initialContext;
}

/**
 * This method used to  compare the context values
 * @param {*} value  input value
 * @param {*} result resulted value
 * @returns {Boolean} true or false
 */
function checkContext(value, result) {
    const contextVal = value.includes('/') ? value.toLowerCase() : '/' + value.toLowerCase();
    if (contextVal === '/' + result.toLowerCase().slice(result.toLowerCase().lastIndexOf('/') + 1)
     || contextVal === result.toLowerCase()) {
        return true;
    }
    return false;
}

/**
 * Reducer
 * @param {JSON} state State.
 * @returns {Promise} Promised state.
 */
function reducer(state, { field, value }) {
    switch (field) {
        case 'name':
        case 'context':
        case 'version':
            return { ...state, [field]: value };
        default:
            return state;
    }
}

const protocols = [
    {
        displayName: 'WebSocket',
        value: 'WS',
    },
    {
        displayName: 'WebSub',
        value: 'WEBSUB',
    },
    {
        displayName: 'SSE',
        value: 'SSE',
    },
];

/**
 * Create API Component for the Service Catalog
 * @param {any} props prop values
 * @returns {object} Create API Dialog
 */
function CreateApi(props) {
    const {
        isIconButton,
        isOverview,
        serviceDisplayName,
        serviceKey,
        definitionType,
        serviceVersion,
        serviceUrl,
        usage,
    } = props;
    const classes = useStyles();
    const intl = useIntl();
    const history = useHistory();
    const [open, setOpen] = useState(false);
    const [pageError, setPageError] = useState(null);
    const [type, setType] = useState('');
    const [isFormValid, setIsFormValid] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);

    /**
     * This method gets the context for the API from the service url
     *
     * @param {string} url service url
     * @returns {string} The url or the pathname of the url
     */
    function getContextFromServiceUrl(url) {
        if (url && url !== '') {
            const urlObject = url.split('://').length > 1 ? new URL(url) : null;
            if (urlObject) {
                let path = urlObject.pathname;
                if (path.endsWith('/')) {
                    path = path.slice(0, -1); // Remove leading `/` because of context validation failure
                }
                return path;
            } else {
                return url.replace(/[^a-zA-Z ]/g, ''); // we need to remove the special chars from context.
            }
        }
        return url;
    }

    const initialState = {
        name: serviceDisplayName
            ? serviceDisplayName.replace(/[&/\\#,+()$~%.'":*?<>{}\s]/g, '') + (usage === 0 ? '' : usage + 1)
            : serviceDisplayName + (usage === 0 ? '' : usage + 1),
        context: getContextFromServiceUrl(serviceUrl) + (usage === 0 ? '' : usage + 1),
        version: serviceVersion,
    };
    const [state, dispatch] = useReducer(reducer, initialState);

    const handleClose = () => {
        setOpen(false);
    };

    const handleChangeType = (event) => {
        setType(event.target.value);
    };

    const {
        name,
        context,
        version,
    } = state;

    const handleChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const [validity, setValidity] = useState({});

    const updateValidity = (newState) => {
        const formValidity = Object.entries(newState).length > 0
            && Object.entries(newState)
                .map(([, value]) => value === null || value === undefined)
                .reduce((acc, cVal) => acc && cVal);
        setIsFormValid(formValidity);
        setValidity(newState);
    };

    /**
     * Trigger the provided onValidate call back on each input validation run
     * Do the validation state aggregation and call the onValidate method with aggregated value
     * @param {string} field The input field.
     * @param {string} value Validation state object
     */
    function validate(field, value) {
        switch (field) {
            case 'name': {
                const nameValidity = APIValidation.apiName.required().validate(value, { abortEarly: false }).error;
                if (nameValidity === null) {
                    APIValidation.apiParameter.validate(field + ':' + value).then((result) => {
                        if (result.body.list.length > 0 && value.toLowerCase() === result.body.list[0]
                            .name.toLowerCase()) {
                            updateValidity({
                                ...validity,
                                name: { details: [{ message: 'Name ' + value + ' already exists' }] },
                            });
                        } else {
                            updateValidity({ ...validity, name: nameValidity });
                        }
                    });
                } else {
                    updateValidity({ ...validity, name: nameValidity });
                }
                break;
            }
            case 'context': {
                const contextValidity = APIValidation.apiContext.required().validate(value, { abortEarly: false })
                    .error;
                const apiContext = value.includes('/') ? value : '/' + value;
                if (contextValidity === null) {
                    APIValidation.apiParameter.validate(field + ':' + apiContext).then((result) => {
                        if (result.body.list.length > 0 && checkContext(value, result.body.list[0].context)) {
                            updateValidity({
                                ...validity,
                                context: { details: [{ message: apiContext + ' context already exists' }] },
                            });
                        } else {
                            updateValidity({ ...validity, context: contextValidity, version: null });
                        }
                    });
                } else {
                    updateValidity({ ...validity, context: contextValidity });
                }
                break;
            }
            case 'version': {
                const versionValidity = APIValidation.apiVersion.required().validate(value).error;
                if (versionValidity === null) {
                    const apiVersion = context.includes('/') ? context + '/' + value : '/'
                    + context + '/' + value;
                    APIValidation.apiParameter.validate('context:' + context
                    + '/' + value).then((result) => {
                        if (result.body.list.length > 0 && (
                            (result.body.list[0].version !== undefined
                            && (result.body.list[0].version.toLowerCase()
                                === value.toLowerCase())))) {
                            updateValidity({
                                ...validity,
                                version: { message: apiVersion + ' context with version already exists' },
                            });
                        } else {
                            updateValidity({ ...validity, version: versionValidity });
                        }
                    });
                } else {
                    updateValidity({ ...validity, version: versionValidity });
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    const toggleOpen = (event) => {
        validate('context', context);
        event.preventDefault();
        event.stopPropagation();
        setOpen(!open);
    };

    const runAction = async () => {
        setIsProcessing(true);
        const response = await API.policies('subscription');
        const allPolicies = response.body.list;
        let policies;
        if (allPolicies.length === 0) {
            Alert.info(intl.formatMessage({
                id: 'Apis.Create.Default.APICreateDefault.error.policies.not.available',
                defaultMessage: 'Throttling policies not available. Contact your administrator',
            }));
            throw new Error('Throttling policies not available. Contact your administrator');
        } else if (allPolicies.filter((p) => p.name === 'Unlimited').length > 0) {
            policies = ['Unlimited'];
        } else {
            policies = [allPolicies[0].name];
        }
        const promisedCreateApi = API.createApiFromService(serviceKey, { ...state, policies }, type);
        promisedCreateApi.then((data) => {
            const apiInfo = data;
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.CreateApi.api.created.successfully',
                defaultMessage: 'API created from service successfully!',
            }));
            setOpen(!open);
            history.push(`/apis/${apiInfo.id}/overview`);
        }).catch((error) => {
            if (error.response) {
                Alert.error(error.response.body.description);
                setPageError(error.response.body);
            } else {
                Alert.error(intl.formatMessage({
                    defaultMessage: 'Error while creating API from service',
                    id: 'ServiceCatalog.CreateApi.error.create.api',
                }));
                setPageError('Error while creating API from service');
            }
            console.error(error);
        }).finally(() => setIsProcessing(false));
    };

    return (
        <>
            {isIconButton && (
                <Tooltip
                    interactive
                    title={(
                        <FormattedMessage
                            id='ServiceCatalog.Listing.components.ServiceCard.create.api'
                            defaultMessage='Create API'
                        />
                    )}
                >
                    <IconButton
                        disableRipple
                        disableFocusRipple
                        color='primary'
                        onClick={toggleOpen}
                        aria-label={`Create api from ${serviceDisplayName} service`}
                    >
                        <AddCircleIcon />
                    </IconButton>
                </Tooltip>
            )}
            {!isIconButton && (
                <Button
                    color='primary'
                    variant={isOverview ? 'contained' : 'outlined'}
                    className={isOverview ? classes.topMarginSpacing : classes.buttonStyle}
                    onClick={toggleOpen}
                >
                    <Typography className={!isOverview && classes.textStyle}>
                        <FormattedMessage
                            id='ServiceCatalog.CreateApi.create.api'
                            defaultMessage='Create API'
                        />
                    </Typography>
                </Button>
            )}
            <Dialog
                onClick={(e) => { e.preventDefault(); e.stopPropagation(); }}
                open={open}
                onClose={handleClose}
                maxWidth='sm'
                fullWidth
                aria-labelledby='create-api-dialog-title'
            >
                <DialogTitle id='create-api-dialog-title'>
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='ServiceCatalog.CreateApi.create.api.dialog.title'
                            defaultMessage='Create API'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='ServiceCatalog.CreateApi.create.api.dialog.helper'
                            defaultMessage='Create API from service {serviceName}'
                            values={{ serviceName: serviceDisplayName }}
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <Grid container spacing={2}>
                        {/* Page error banner */}
                        {pageError && (
                            <>
                                <Grid item xs={12}>
                                    <Banner
                                        onClose={() => setPageError(null)}
                                        disableActions
                                        dense
                                        paperProps={{ elevation: 1 }}
                                        type='error'
                                        message={pageError}
                                    />
                                </Grid>
                                <Grid item xs={12} />
                            </>
                        )}
                        {/* end of Page error banner */}
                        <Grid item xs={12}>
                            <TextField
                                autoFocus
                                name='name'
                                label={(
                                    <>
                                        <FormattedMessage
                                            id='ServiceCatalog.CreateApi.api.name.label'
                                            defaultMessage='Name'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </>
                                )}
                                value={name}
                                variant='outlined'
                                error={validity.name}
                                fullWidth
                                helperText={
                                    validity.name
                                    && validity.name.details.map((detail, index) => {
                                        return <div style={{ marginTop: index !== 0 && '10px' }}>{detail.message}</div>;
                                    })
                                }
                                InputProps={{
                                    id: 'itest-id-apiname-input',
                                    onBlur: ({ target: { value } }) => {
                                        validate('name', value);
                                    },
                                }}
                                onChange={handleChange}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Grid container spacing={2}>
                                <Grid item md={8} xs={6}>
                                    <TextField
                                        name='context'
                                        label={(
                                            <>
                                                <FormattedMessage
                                                    id='ServiceCatalog.CreateApi.api.context.label'
                                                    defaultMessage='Context'
                                                />
                                                <sup className={classes.mandatoryStar}>*</sup>
                                            </>
                                        )}
                                        value={context}
                                        margin='normal'
                                        variant='outlined'
                                        error={validity.context}
                                        fullWidth
                                        helperText={
                                            (validity.context
                                                && validity.context.details.map((detail, index) => {
                                                    return (
                                                        <div style={{ marginTop: index !== 0 && '10px' }}>
                                                            {detail.message}
                                                        </div>
                                                    );
                                                }))
                                            || `API will be exposed in ${actualContext({ context, version })}`
                                            + ' context at the gateway'
                                        }
                                        InputProps={{
                                            id: 'itest-id-apicontext-input',
                                            onBlur: ({ target: { value } }) => {
                                                validate('context', value);
                                            },
                                        }}
                                        onChange={handleChange}
                                    />
                                </Grid>
                                <Grid item md={4} xs={6}>
                                    <TextField
                                        name='version'
                                        label={(
                                            <>
                                                <FormattedMessage
                                                    id='ServiceCatalog.CreateApi.api.version.label'
                                                    defaultMessage='Version'
                                                />
                                                <sup className={classes.mandatoryStar}>*</sup>
                                            </>
                                        )}
                                        value={version}
                                        margin='normal'
                                        variant='outlined'
                                        error={validity.version}
                                        fullWidth
                                        helperText={validity.version && validity.version.message}
                                        InputProps={{
                                            id: 'itest-id-apiversion-input',
                                            onBlur: ({ target: { value } }) => {
                                                validate('version', value);
                                            },
                                        }}
                                        onChange={handleChange}
                                    />
                                </Grid>
                                {definitionType === 'ASYNC_API' && (
                                    <Grid item md={8} xs={6}>
                                        <TextField
                                            id='version-selector'
                                            select
                                            label={(
                                                <FormattedMessage
                                                    id='ServiceCatalog.CreateApi.select.protocol'
                                                    defaultMessage='Select Protocol'
                                                />
                                            )}
                                            name='selectType'
                                            value={type}
                                            onChange={handleChangeType}
                                            margin='dense'
                                            variant='outlined'
                                            fullWidth
                                            SelectProps={{
                                                MenuProps: {
                                                    anchorOrigin: {
                                                        vertical: 'bottom',
                                                        horizontal: 'left',
                                                    },
                                                    getContentAnchorEl: null,
                                                },
                                            }}
                                        >
                                            {protocols.map((protocol) => (
                                                <MenuItem value={protocol.value} native>
                                                    {protocol.value}
                                                </MenuItem>
                                            ))}
                                        </TextField>
                                    </Grid>
                                )}
                            </Grid>
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Grid
                        container
                        direction='row'
                        justify='flex-start'
                        alignItems='center'
                        className={classes.mandatoryLabelStyle}
                    >
                        <Grid item>
                            <Typography variant='caption' display='block'>
                                <sup style={{ color: 'red' }}>*</sup>
                                {' '}
                                <FormattedMessage
                                    id='ServiceCatalog.CreateApi.mandatory.field.label'
                                    defaultMessage='Mandatory fields'
                                />
                            </Typography>
                        </Grid>
                    </Grid>
                    <Grid
                        container
                        direction='row'
                        justify='flex-end'
                        alignItems='center'
                        className={classes.actionButtonStyle}
                    >
                        <Grid item>
                            <Button disabled={isProcessing} onClick={toggleOpen} color='primary'>
                                <FormattedMessage
                                    id='ServiceCatalog.CreateApi.cancel.btn'
                                    defaultMessage='Cancel'
                                />
                            </Button>
                            <Button
                                onClick={runAction}
                                color='primary'
                                variant='contained'
                                disabled={!isFormValid || isProcessing}
                            >
                                {isProcessing ? (
                                    <FormattedMessage
                                        id='ServiceCatalog.CreateApi.update.btn.in.progress'
                                        defaultMessage='Creating API ...'
                                    />
                                ) : (
                                    <FormattedMessage
                                        id='ServiceCatalog.CreateApi.update.btn'
                                        defaultMessage='Create API'
                                    />
                                )}
                                {isProcessing && <CircularProgress size={15} />}
                            </Button>
                        </Grid>
                    </Grid>
                </DialogActions>
            </Dialog>
        </>
    );
}

CreateApi.defaultProps = {
    isOverview: false,
};

CreateApi.propTypes = {
    serviceKey: PropTypes.string.isRequired,
    serviceDisplayName: PropTypes.string.isRequired,
    definitionType: PropTypes.string.isRequired,
    serviceVersion: PropTypes.string.isRequired,
    serviceUrl: PropTypes.string.isRequired,
    isOverview: PropTypes.bool,
};

export default CreateApi;
