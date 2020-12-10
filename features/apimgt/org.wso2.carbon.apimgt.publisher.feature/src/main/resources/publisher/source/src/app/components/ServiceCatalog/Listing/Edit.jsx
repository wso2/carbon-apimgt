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
import { FormattedMessage, useIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import ServiceCatalog from 'AppData/ServiceCatalog';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import Container from '@material-ui/core/Container';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import { Progress } from 'AppComponents/Shared';
import Joi from '@hapi/joi';

const useStyles = makeStyles((theme) => ({
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
    },
    buttonWrapper: {
        paddingTop: 10,
    },
    buttonSection: {
        paddingTop: theme.spacing(1),
    },
    headingSpacing: {
        marginTop: theme.spacing(3),
    },
}));

/**
 * Reducer
 * @param {JSON} state State.
 * @returns {Promise} Promised state.
 */
function reducer(state, { field, value }) {
    switch (field) {
        case 'displayName':
        case 'description':
        case 'serviceUrl':
        case 'definitionType':
            return { ...state, [field]: value };
        case 'initialize':
            return value;
        default:
            return state;
    }
}

/**
* Service Catalog service edit
* @returns {any} Returns the rendered UI for service edit.
*/
function Edit(props) {
    const intl = useIntl();
    const classes = useStyles();
    const { match, history } = props;
    const serviceId = match.params.service_uuid;
    const [service, setService] = useState(null);
    const [schemaTypeList, setSchemaTypeList] = useState([]);
    const [validity, setValidity] = useState({});

    const initialState = {
        id: '',
        name: '',
        displayName: '',
        description: '',
        serviceUrl: '',
        definitionType: '',
    };

    const [state, dispatch] = useReducer(reducer, initialState);

    // Get Service Details
    const getService = () => {
        const promisedService = ServiceCatalog.getServiceById(serviceId);
        promisedService.then((data) => {
            setService(data);
            dispatch({ field: 'initialize', value: data });
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error While Loading Service',
                id: 'ServiceCatalog.Listing.Edit.error.loading.service',
            }));
        });
        return null;
    };

    useEffect(() => {
        getService();
    }, []);

    const {
        id,
        name,
        displayName,
        description,
        serviceUrl,
        definitionType,
    } = state;

    useEffect(() => {
        const settingPromise = ServiceCatalog.getSettings();
        settingPromise.then((response) => {
            setSchemaTypeList(response.schemaTypes);
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while retrieving schema types',
                id: 'ServiceCatalog.Listing.Edit.error.retrieve.service.schema.types',
            }));
        });
    }, []);

    const handleChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const validate = (fieldName, value) => {
        let error = '';
        const schema = Joi.string().regex(/^[^~!@#;:%^*()+={}|\\<>"',&$\s+]*$/);
        switch (fieldName) {
            case 'displayName':
                if (value === '') {
                    error = intl.formatMessage({
                        id: 'ServiceCatalog.Listing.Edit.service.display.name.empty',
                        defaultMessage: 'Service display name is empty ',
                    });
                } else if (value.length > 60) {
                    error = intl.formatMessage({
                        id: 'ServiceCatalog.Listing.Edit.service.display.name.too.long',
                        defaultMessage: 'Service display name is too long ',
                    });
                } else if (schema.validate(value).error) {
                    error = intl.formatMessage({
                        id: 'ServiceCatalog.Listing.Edit.service.display.name.invalid.character',
                        defaultMessage: 'Service display name contains one or more illegal characters ',
                    });
                } else {
                    error = '';
                }
                setValidity({
                    ...validity,
                    displayName: error,
                });
                break;
            case 'serviceUrl':
                error = value === '' ? intl.formatMessage({
                    id: 'ServiceCatalog.Listing.Edit.service.url.empty',
                    defaultMessage: 'Service Url is empty ',
                }) : '';
                setValidity({
                    ...validity,
                    serviceUrl: error,
                });
                break;
            case 'definitionType':
                error = value === '' ? intl.formatMessage({
                    id: 'ServiceCatalog.Listing.Edit.service.definition.type.empty',
                    defaultMessage: 'Schema Type is empty ',
                }) : '';
                setValidity({
                    ...validity,
                    definitionType: error,
                });
                break;
            default:
                break;
        }
        return error;
    };

    const getAllFormErrors = () => {
        let errorText = '';
        const serviceDisplayNameErrors = validate('displayName', displayName);
        const serviceUrlErrors = validate('serviceUrl', serviceUrl);
        const definitionTypeErrors = validate('definitionType', definitionType);
        errorText += serviceDisplayNameErrors + serviceUrlErrors + definitionTypeErrors;
        return errorText;
    };

    /**
     * Function for updating a given service entry
     */
    const onEdit = () => {
        const updateServicePromise = ServiceCatalog.updateService(id, state);
        updateServicePromise.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.service.updated.successfully',
                defaultMessage: 'Service updated successfully!',
            }));
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while updating service',
                id: 'ServiceCatalog.Listing.Listing.error.update',
            }));
        });
    };

    /**
     * Function for updating a given service entry
     */
    function doneEditing() {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
        } else {
            onEdit(state.id, state);
            // Redirect to listing page
            history.push('/service-catalog');
        }
    }

    const listingRedirect = () => {
        history.push('/service-catalog');
    };

    if (!service) {
        return <Progress per={90} message='Loading Service ...' />;
    }

    return (
        <>
            <Container maxWidth='md'>
                <Box mb={3} className={classes.headingSpacing}>
                    <Typography variant='h4'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Edit.heading'
                            defaultMessage='Edit Service'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Edit.heading.caption'
                            defaultMessage='Edit the service in the service catalog'
                        />
                    </Typography>
                </Box>
                <Paper elevation={1}>
                    <Box px={8} py={5}>
                        <form noValidate autoComplete='off'>
                            <TextField
                                name='name'
                                label={(
                                    <>
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.service.name.label'
                                            defaultMessage='Service Name'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </>
                                )}
                                value={name}
                                variant='outlined'
                                margin='normal'
                                fullWidth
                                helperText={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Edit.service.name.helper.text'
                                        defaultMessage='Name of the service'
                                    />
                                )}
                                disabled
                            />
                            <TextField
                                name='displayName'
                                label={(
                                    <>
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.service.display.name.label'
                                            defaultMessage='Service Display Name'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </>
                                )}
                                value={displayName}
                                variant='outlined'
                                margin='normal'
                                error={validity.displayName}
                                fullWidth
                                helperText={validity.displayName ? validity.displayName
                                    : (
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.service.display.name.helper.text'
                                            defaultMessage='Display name of the service'
                                        />
                                    )}
                                InputProps={{
                                    id: 'itest-id-servicedisplayname-input',
                                    onBlur: ({ target: { value } }) => {
                                        validate('displayName', value);
                                    },
                                }}
                                onChange={handleChange}
                            />
                            <TextField
                                name='description'
                                label={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Edit.service.description.label'
                                        defaultMessage='Service description'
                                    />
                                )}
                                value={description}
                                variant='outlined'
                                margin='normal'
                                fullWidth
                                helperText={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Edit.service.description.helper.text'
                                        defaultMessage='Description of the service'
                                    />
                                )}
                                onChange={handleChange}
                                multiline
                            />
                            <TextField
                                name='serviceUrl'
                                label={(
                                    <>
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.service.url.label'
                                            defaultMessage='Service URL'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </>
                                )}
                                value={serviceUrl}
                                fullWidth
                                variant='outlined'
                                margin='normal'
                                error={validity.serviceUrl}
                                helperText={validity.serviceUrl ? validity.serviceUrl
                                    : (
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.service.url.text'
                                            defaultMessage='URL of the Service'
                                        />
                                    )}
                                InputProps={{
                                    id: 'itest-id-serviceurl-input',
                                    onBlur: ({ target: { value } }) => {
                                        validate('serviceUrl', value);
                                    },
                                }}
                                onChange={handleChange}
                            />
                            <TextField
                                name='definitionType'
                                select
                                label={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Edit.schema.type.label'
                                        defaultMessage='Schema Type'
                                    />
                                )}
                                value={definitionType}
                                fullWidth
                                variant='outlined'
                                margin='normal'
                                error={validity.definitionType}
                                helperText={validity.definitionType ? validity.definitionType
                                    : (
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.schema.type.text'
                                            defaultMessage='Schema Type of the Service'
                                        />
                                    )}
                                InputProps={{
                                    id: 'itest-id-definitionType-input',
                                    onBlur: ({ target: { value } }) => {
                                        validate('definitionType', value);
                                    },
                                }}
                                SelectProps={{
                                    MenuProps: {
                                        anchorOrigin: {
                                            vertical: 'bottom',
                                            horizontal: 'left',
                                        },
                                        getContentAnchorEl: null,
                                    },
                                }}
                                onChange={handleChange}
                            >
                                {schemaTypeList.map((schema) => (
                                    <MenuItem
                                        id={schema}
                                        key={schema}
                                        value={schema}
                                    >
                                        <ListItemText primary={schema} />
                                    </MenuItem>
                                ))}
                            </TextField>
                        </form>
                        <div className={classes.buttonWrapper}>
                            <Grid
                                container
                                direction='row'
                                alignItems='flex-start'
                                spacing={1}
                                className={classes.buttonSection}
                            >
                                <Grid item>
                                    <Button
                                        onClick={doneEditing}
                                        color='primary'
                                        variant='contained'
                                    >
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.update.btn'
                                            defaultMessage='Update'
                                        />
                                    </Button>
                                </Grid>
                                <Grid item>
                                    <Button onClick={listingRedirect} color='primary'>
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.cancel.btn'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Grid>
                            </Grid>
                        </div>
                    </Box>
                </Paper>
            </Container>
        </>
    );
}
Edit.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
};

export default Edit;
