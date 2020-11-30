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
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import Grid from '@material-ui/core/Grid';
import Dialog from '@material-ui/core/Dialog';
import TextField from '@material-ui/core/TextField';
import { makeStyles } from '@material-ui/core/styles';
import ServiceCatalog from 'AppData/ServiceCatalog';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import Joi from '@hapi/joi';

const useStyles = makeStyles((theme) => ({
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
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
        default:
            return state;
    }
}

/**
* Service Catalog service edit
* @param {any} props Props for edit function.
* @returns {any} Returns the rendered UI for service edit.
*/
function Edit(props) {
    const intl = useIntl();
    const classes = useStyles();
    const { onEdit, dataRow } = props;
    const [open, setOpen] = useState(false);
    const [schemaTypeList, setSchemaTypeList] = useState([]);
    const toggleOpen = () => {
        setOpen(!open);
    };
    const [state, dispatch] = useReducer(reducer, dataRow);
    const [validity, setValidity] = useState({});

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
    function doneEditing() {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
        } else {
            onEdit(id, state);
            setOpen(!open);
        }
    }

    return (
        <>
            <Button onClick={toggleOpen}>
                <Icon>edit</Icon>
            </Button>
            <Dialog
                open={open}
                maxWidth='sm'
                fullWidth
                aria-labelledby='confirmation-dialog-title'
            >
                <DialogTitle id='confirmation-dialog-title'>
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Edit.service'
                        defaultMessage='Edit Service'
                    />
                </DialogTitle>
                <DialogContent>
                    <Grid container spacing={2}>
                        <Grid item md={12}>
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
                                fullWidth
                                helperText={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Edit.service.name.helper.text'
                                        defaultMessage='Name of the service'
                                    />
                                )}
                                disabled
                            />
                        </Grid>
                        <Grid item md={12}>
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
                        </Grid>
                        <Grid item md={12}>
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
                        </Grid>
                        <Grid item md={12}>
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
                        </Grid>
                        <Grid item md={12}>
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
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button onClick={toggleOpen} color='primary'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Edit.cancel.btn'
                            defaultMessage='Cancel'
                        />
                    </Button>
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
                </DialogActions>
            </Dialog>
        </>
    );
}
Edit.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    dataRow: PropTypes.shape({
        id: PropTypes.string.isRequired,
        displayName: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
        definitionType: PropTypes.string.isRequired,
        serviceUrl: PropTypes.string.isRequired,
    }).isRequired,
    onEdit: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default Edit;
