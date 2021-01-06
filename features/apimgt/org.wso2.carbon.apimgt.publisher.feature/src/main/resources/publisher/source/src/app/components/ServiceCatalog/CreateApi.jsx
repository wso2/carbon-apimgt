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
import Grid from '@material-ui/core/Grid';
import Dialog from '@material-ui/core/Dialog';
import TextField from '@material-ui/core/TextField';
import APIValidation from 'AppData/APIValidation';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, useIntl } from 'react-intl';
import ServiceCatalog from 'AppData/ServiceCatalog';

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

/**
 * Create API Component for the Service Catalog
 * @param {any} props prop values
 * @returns {object} Create API Dialog
 */
function CreateApi(props) {
    const {
        serviceId,
        history,
        isOverview,
        serviceDisplayName,
    } = props;
    const classes = useStyles();
    const intl = useIntl();

    const [open, setOpen] = useState(false);
    const toggleOpen = () => {
        setOpen(!open);
    };
    const handleClose = () => {
        setOpen(false);
    };

    const [isFormValid, setIsFormValid] = useState(false);

    const initialState = {
        name: '',
        context: '',
        version: '',
    };

    const [state, dispatch] = useReducer(reducer, initialState);

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

    const runAction = () => {
        const promisedCreateApi = ServiceCatalog.createApiFromService(serviceId, state);
        promisedCreateApi.then((data) => {
            const apiInfo = data;
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.CreateApi.api.created.successfully',
                defaultMessage: 'API created from service successfully!',
            }));
            setOpen(!open);
            history.push(`/apis/${apiInfo.id}/overview`);
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while creating API from service',
                id: 'ServiceCatalog.CreateApi.error.create.api',
            }));
        });
    };

    return (
        <>
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
            <Dialog
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
                            </Grid>
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions className={classes.actionButtonStyle}>
                    <Button onClick={toggleOpen} color='primary'>
                        <FormattedMessage
                            id='ServiceCatalog.CreateApi.cancel.btn'
                            defaultMessage='Cancel'
                        />
                    </Button>
                    <Button
                        onClick={runAction}
                        color='primary'
                        variant='contained'
                        disabled={!isFormValid}
                    >
                        <FormattedMessage
                            id='ServiceCatalog.CreateApi.update.btn'
                            defaultMessage='Create API'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

CreateApi.defaultProps = {
    isOverview: false,
};

CreateApi.propTypes = {
    serviceId: PropTypes.string.isRequired,
    serviceDisplayName: PropTypes.string.isRequired,
    isOverview: PropTypes.bool,
};

export default CreateApi;
