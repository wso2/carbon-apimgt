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
import { FormattedMessage, injectIntl, useIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import Dialog from '@material-ui/core/Dialog';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import { makeStyles } from '@material-ui/core/styles';
import ServiceCatalog from 'AppData/ServiceCatalog';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';

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
        case 'name':
            return { ...state, [field]: value, displayName: value };
        case 'serviceUrl':
            return { ...state, [field]: value };
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
    const { getData, dataRow } = props;
    const [open, setOpen] = useState(false);
    const [schemaTypeList, setSchemaTypeList] = useState([]);
    const [serviceTypeList, setServiceTypeList] = useState([]);
    const toggleOpen = () => {
        setOpen(!open);
    };
    const [initialState, setInitialState] = useState(dataRow);

    const [state, dispatch] = useReducer(reducer, initialState);

    const {
        id,
        name,
        serviceUrl,
        definitionType,
    } = state;

    useEffect(() => {
        setInitialState(dataRow);
        const settingPromise = ServiceCatalog.getSettings();
        // Take from settings when backend API is implemented
        settingPromise.then(() => {
            setSchemaTypeList(['OAS2', 'OAS3', 'WSDL1', 'WSDL2', 'GRAPHQL_SDL', 'ASYNC_API']);
            setServiceTypeList(['OAS2', 'OAS3', 'WSDL1', 'WSDL2', 'GRAPHQL_SDL', 'ASYNC_API']);
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while retrieving service and schema types',
                id: 'ServiceCatalog.Listing.Edit.error.retrieve.service.schema.types',
            }));
        });
    }, []);

    const handleChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    /**
     * Function for updating a given service entry
     */
    function doneEditing() {
        const updateServicePromise = ServiceCatalog.updateService(id, state);
        updateServicePromise.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Delete.service.updated.successfully',
                defaultMessage: 'Service updated successfully!',
            }));
            // Reload the services list
            getData();
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while updating service',
                id: 'ServiceCatalog.Listing.Edit.error.update',
            }));
        });
        setOpen(!open);
    }

    return (
        <>
            <Button onClick={toggleOpen}>
                <Icon>edit</Icon>
            </Button>
            <Dialog
                open={open}
                disableBackdropClick
                disableEscapeKeyDown
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
                    <TextField
                        name='name'
                        label={(
                            <>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Edit.service.name.label'
                                    defaultMessage='Service'
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
                        onChange={handleChange}
                    />
                </DialogContent>
                <DialogContent>
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
                        helperText={(
                            <FormattedMessage
                                id='ServiceCatalog.Listing.Edit.service.url.text'
                                defaultMessage='URL of the Service'
                            />
                        )}
                        onChange={handleChange}
                    />
                </DialogContent>
                <Grid container spacing={0}>
                    <Grid item md={6}>
                        <DialogContent>
                            <TextField
                                name='definitionType'
                                select
                                label={(
                                    <>
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.service.type.label'
                                            defaultMessage='Service Type'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </>
                                )}
                                value={definitionType}
                                fullWidth
                                variant='outlined'
                                helperText={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Edit.service.type.text'
                                        defaultMessage='Type of the Service'
                                    />
                                )}
                                onChange={handleChange}
                            >
                                {serviceTypeList.map((service) => (
                                    <MenuItem
                                        id={service}
                                        key={service}
                                        value={service}
                                    >
                                        <ListItemText primary={service} />
                                    </MenuItem>
                                ))}
                            </TextField>
                        </DialogContent>
                    </Grid>
                    <Grid item md={6}>
                        <DialogContent>
                            <TextField
                                name='definitionType'
                                select
                                label={(
                                    <>
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Edit.schema.type.label'
                                            defaultMessage='Schema Type'
                                        />
                                        <sup className={classes.mandatoryStar}>*</sup>
                                    </>
                                )}
                                value={definitionType}
                                fullWidth
                                variant='outlined'
                                helperText={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Edit.schema.type.text'
                                        defaultMessage='Schema Type of the Service'
                                    />
                                )}
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
                        </DialogContent>
                    </Grid>
                </Grid>
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
    getData: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(Edit);
