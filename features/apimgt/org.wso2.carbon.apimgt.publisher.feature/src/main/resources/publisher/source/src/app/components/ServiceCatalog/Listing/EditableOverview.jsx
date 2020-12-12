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
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Configurations from 'Config';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Tooltip from '@material-ui/core/Tooltip';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Container from '@material-ui/core/Container';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Switch from '@material-ui/core/Switch';
import FormControl from '@material-ui/core/FormControl';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Box from '@material-ui/core/Box';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import { Link } from 'react-router-dom';
import Paper from '@material-ui/core/Paper';
import moment from 'moment';
import Joi from '@hapi/joi';
import PropTypes from 'prop-types';
import LocalOfferOutlinedIcon from '@material-ui/icons/LocalOfferOutlined';

const useStyles = makeStyles((theme) => ({
    preview: {
        height: theme.spacing(16),
        marginBottom: theme.spacing(3),
        marginLeft: theme.spacing(1),
    },
    contentWrapper: {
        marginTop: theme.spacing(3),
        alignItems: 'center',
    },
    iconAligner: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    tableIcon: {
        width: theme.spacing(3),
    },
    bodyStyle: {
        marginLeft: theme.spacing(2),
        marginBottom: theme.spacing(3),
    },
    contentTopBarStyle: {
        display: 'flex',
    },
    table: {
        minWidth: '100%',
    },
    iconTextWrapper: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    versionBarStyle: {
        marginTop: theme.spacing(1),
        display: 'flex',
    },
    topBarDetailsSectionStyle: {
        marginLeft: theme.spacing(5),
    },
    versionStyle: {
        marginLeft: theme.spacing(1),
    },
    topMarginSpacing: {
        marginTop: theme.spacing(2),
    },
    apiUsageStyle: {
        marginTop: theme.spacing(3),
    },
    headingSpacing: {
        marginTop: theme.spacing(3),
    },
    buttonWrapper: {
        paddingTop: 10,
    },
    buttonSection: {
        paddingTop: theme.spacing(1),
        marginLeft: theme.spacing(2),
    },
    nameStyle: {
        fontSize: 14,
    },
    paperStyle: {
        marginBottom: theme.spacing(3),
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
        case 'securityType':
        case 'mutualSSLEnabled':
            return { ...state, [field]: value };
        case 'initialize':
            return value;
        default:
            return state;
    }
}

/**
 * Service Catalog Overview / Edit Page
 *
 * @param {any} props props
 * @returns {any} Overview page of a service
 */
function EditableOverview(props) {
    const classes = useStyles();
    const intl = useIntl();
    const { match, history } = props;
    const serviceId = match.params.service_uuid;
    const [service, setService] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [notFound, setNotFound] = useState(true);
    const [schemaTypeList, setSchemaTypeList] = useState([]);
    const [securityTypeList, setSecurityTypeList] = useState([]);

    const [validity, setValidity] = useState({});

    const initialState = {
        id: '',
        name: '',
        displayName: '',
        description: '',
        serviceUrl: '',
        definitionType: '',
        securityType: '',
        mutualSSLEnabled: false,
    };

    const [state, dispatch] = useReducer(reducer, initialState);

    const {
        id,
        displayName,
        description,
        serviceUrl,
        definitionType,
        securityType,
        mutualSSLEnabled,
    } = state;

    const handleChange = (e) => {
        if (e.target.name === 'mutualSSLEnabled') {
            dispatch({ field: e.target.name, value: e.target.checked });
        } else {
            dispatch({ field: e.target.name, value: e.target.value });
        }
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
                    id: 'ServiceCatalog.Listing.Overview.service.definition.type.empty',
                    defaultMessage: 'Schema Type is empty ',
                }) : '';
                setValidity({
                    ...validity,
                    definitionType: error,
                });
                break;
            case 'securityType':
                error = value === '' ? intl.formatMessage({
                    id: 'ServiceCatalog.Listing.Overview.service.security.type.empty',
                    defaultMessage: 'Security Type is empty ',
                }) : '';
                setValidity({
                    ...validity,
                    securityType: error,
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
        const securityTypeErrors = validate('securityType', securityType);
        errorText += serviceDisplayNameErrors + serviceUrlErrors + definitionTypeErrors + securityTypeErrors;
        return errorText;
    };

    const onEditFromOverview = () => {
        setIsEditing(true);
    };

    const overviewRedirect = () => {
        setIsEditing(false);
        history.push(`/service-catalog/${serviceId}/overview`);
    };

    // Get Service Details
    const getService = () => {
        const promisedService = ServiceCatalog.getServiceById(serviceId);
        promisedService.then((data) => {
            setService(data);
            dispatch({ field: 'initialize', value: data });
            setNotFound(false);
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while loading service',
                id: 'ServiceCatalog.Listing.EditableOverview.error.loading.service',
            }));
        });
        return null;
    };

    useEffect(() => {
        getService();
    }, []);

    useEffect(() => {
        const settingPromise = ServiceCatalog.getSettings();
        settingPromise.then((response) => {
            setSchemaTypeList(response.schemaTypes);
            setSecurityTypeList(response.securityTypes);
        }).catch(() => {
            Alert.error(intl.formatMessage({
                id: 'ServiceCatalog.Listing.EditableOverview.error.retrieve.settings.data',
                defaultMessage: 'Error while retrieving settings data',
            }));
        });
    }, []);

    const listingRedirect = () => {
        history.push('/service-catalog');
    };

    /**
     * Function for updating a given service entry
     */
    const onEdit = () => {
        const updateServicePromise = ServiceCatalog.updateService(id, state);
        updateServicePromise.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.EditableOverview.service.successful.update',
                defaultMessage: 'Service updated successfully!',
            }));
        }).catch(() => {
            Alert.error(intl.formatMessage({
                id: 'ServiceCatalog.Listing.EditableOverview.error.update',
                defaultMessage: 'Error while updating service',
            }));
        });
    };

    /**
     * Parent function for updating a given service entry
     */
    function doneEditing() {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
        } else {
            onEdit(id, state);
            // Redirect to read only overview page
            overviewRedirect();
        }
    }

    const renderContent = () => {
        if (isEditing) {
            return (
                <FormControl component='fieldset'>
                    <FormGroup>
                        <FormControlLabel
                            control={(
                                <Switch
                                    checked={mutualSSLEnabled}
                                    onChange={handleChange}
                                    inputProps={{ 'aria-label': 'primary checkbox' }}
                                    name='mutualSSLEnabled'
                                />
                            )}
                            label={(
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.EditableOverview.mutual.ssl.enable.label'
                                    defaultMessage='Enable'
                                />
                            )}
                        />
                    </FormGroup>
                </FormControl>
            );
        } else if (service.mutualSSLEnabled) {
            return (
                <FormattedMessage
                    id='ServiceCatalog.Listing.EditableOverview.mutual.ssl.enabled'
                    defaultMessage='Enabled'
                />
            );
        } else {
            return (
                <FormattedMessage
                    id='ServiceCatalog.Listing.EditableOverview.mutual.ssl.disabled'
                    defaultMessage='Disabled'
                />
            );
        }
    };

    if (!service) {
        return <Progress per={90} message='Loading Service ...' />;
    }

    if (notFound) {
        return <ResourceNotFound />;
    }

    let serviceTypeIcon = (
        <img
            className={classes.preview}
            src={Configurations.app.context + '/site/public/images/restAPIIcon.png'}
            alt='Type API'
        />
    );
    if (service.definitionType === 'OAS3' || service.definitionType === 'OAS2') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.EditableOverview.service.type.rest.tooltip'
                        defaultMessage='REST Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/swaggerIcon.svg'}
                    alt='Type Rest API'
                />
            </Tooltip>
        );
    } else if (service.definitionType === 'GRAPHQL_SDL') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.EditableOverview.service.type.graphql.tooltip'
                        defaultMessage='GraphQL Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/graphqlIcon.svg'}
                    alt='Type GraphQL API'
                />
            </Tooltip>
        );
    } else if (service.definitionType === 'ASYNC_API') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.EditableOverview.service.type.async.tooltip'
                        defaultMessage='Async API Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/asyncAPIIcon.jpeg'}
                    alt='Type Async API'
                />
            </Tooltip>
        );
    } else if (service.definitionType === 'WSDL1' || service.definitionType === 'WSDL2') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.EditableOverview.service.type.soap.tooltip'
                        defaultMessage='SOAP Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/restAPIIcon.png'}
                    alt='Type SOAP API'
                />
            </Tooltip>
        );
    }

    return (
        <>
            <Container maxWidth='md'>
                <Box mb={3} className={classes.headingSpacing}>
                    <Breadcrumbs aria-label='breadcrumb'>
                        <Link color='inherit' to='/service-catalog'>
                            <FormattedMessage
                                id='ServiceCatalog.Listing.EditableOverview.parent.breadcrumb'
                                defaultMessage='Service Catalog'
                            />
                        </Link>
                        {!isEditing ? (
                            <Typography color='textPrimary'>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.EditableOverview.readonly.breadcrumb'
                                    defaultMessage='Overview'
                                />
                            </Typography>
                        ) : (
                            <Typography color='textPrimary'>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.EditableOverview.edit.breadcrumb'
                                    defaultMessage='Edit'
                                />
                            </Typography>
                        )}
                    </Breadcrumbs>
                </Box>
                <Paper elevation={1} className={classes.paperStyle}>
                    <Box px={8} py={5}>
                        <div>
                            <Grid container spacing={1}>
                                <Grid item md={10}>
                                    <div className={classes.contentTopBarStyle}>
                                        {serviceTypeIcon}
                                        <div className={classes.topBarDetailsSectionStyle}>
                                            <div className={classes.versionBarStyle}>
                                                {!isEditing ? (
                                                    <Typography className={classes.heading} variant='h5'>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.EditableOverview.display.name'
                                                            defaultMessage='{serviceDisplayName}'
                                                            values={{ serviceDisplayName: service.displayName }}
                                                        />
                                                    </Typography>
                                                ) : (
                                                    <Typography>
                                                        <TextField
                                                            name='displayName'
                                                            value={displayName}
                                                            margin='normal'
                                                            fullWidth
                                                            className={classes.nameStyle}
                                                            error={validity.displayName}
                                                            helperText={validity.displayName ? validity.displayName
                                                                : (
                                                                    <FormattedMessage
                                                                        id='ServiceCatalog.Listing.EditableOverview.dn'
                                                                        defaultMessage='Display name of the service'
                                                                    />
                                                                )}
                                                            InputProps={{
                                                                id: 'itest-id-servicedisplayname-input',
                                                                onBlur: ({ target: { value } }) => {
                                                                    validate('displayName', value);
                                                                },
                                                                style: { fontSize: 20 },
                                                            }}
                                                            onChange={handleChange}
                                                        />
                                                    </Typography>
                                                )}
                                            </div>
                                            <div className={classes.versionBarStyle}>
                                                <LocalOfferOutlinedIcon />
                                                <Typography className={classes.versionStyle}>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.EditableOverview.service.version'
                                                        defaultMessage='{serviceVersion}'
                                                        values={{ serviceVersion: service.version }}
                                                    />
                                                </Typography>
                                            </div>
                                            <div className={classes.apiUsageStyle} primary>
                                                <Typography color='primary'>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.EditableOverview.service.usage'
                                                        defaultMessage='Used by {usage} API(s)'
                                                        values={{ usage: service.usage }}
                                                    />
                                                </Typography>
                                            </div>
                                        </div>
                                    </div>
                                </Grid>
                                <Grid item md={2}>
                                    <Box display='flex' flexDirection='column'>
                                        { !isEditing ? (
                                            <Button
                                                color='primary'
                                                variant='contained'
                                                className={classes.topMarginSpacing}
                                            >
                                                <Typography>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.EditableOverview.create.api'
                                                        defaultMessage='Create API'
                                                    />
                                                </Typography>
                                            </Button>
                                        ) : (
                                            <Button
                                                color='primary'
                                                variant='contained'
                                                className={classes.topMarginSpacing}
                                                disabled
                                            >
                                                <Typography>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.EditableOverview.create.api'
                                                        defaultMessage='Create API'
                                                    />
                                                </Typography>
                                            </Button>
                                        )}
                                    </Box>
                                </Grid>
                            </Grid>
                        </div>
                        <div className={classes.bodyStyle}>
                            <Grid container spacing={1}>
                                { (service.description && service.description !== '') ? (
                                    <>
                                        <Grid item md={12}>
                                            {!isEditing ? (
                                                <Typography>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.EditableOverview.service.description'
                                                        defaultMessage='{description}'
                                                        values={{ description: service.description }}
                                                    />
                                                </Typography>
                                            ) : (
                                                <TextField
                                                    name='description'
                                                    value={description}
                                                    margin='normal'
                                                    fullWidth
                                                    multiline
                                                    helperText={(
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.EditableOverview.description.ht'
                                                            defaultMessage='Description of the Service'
                                                        />
                                                    )}
                                                    onChange={handleChange}
                                                />
                                            )}
                                        </Grid>
                                    </>
                                ) : (
                                    <Grid item md={12}>
                                        {isEditing && (
                                            <TextField
                                                name='description'
                                                value={description}
                                                margin='normal'
                                                fullWidth
                                                multiline
                                                helperText={(
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.EditableOverview.no.description.text'
                                                        defaultMessage='Description of the Service'
                                                    />
                                                )}
                                                onChange={handleChange}
                                            />
                                        )}
                                    </Grid>
                                )}
                            </Grid>
                            <div className={classes.contentWrapper}>
                                <Table className={classes.table}>
                                    <TableBody>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>link</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.EditableOverview.service.url'
                                                            defaultMessage='Service URL'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            { isEditing ? (
                                                <TableCell>
                                                    <TextField
                                                        name='serviceUrl'
                                                        value={serviceUrl}
                                                        margin='normal'
                                                        fullWidth
                                                        error={validity.serviceUrl}
                                                        helperText={validity.serviceUrl}
                                                        InputProps={{
                                                            id: 'itest-id-serviceurl-input',
                                                            onBlur: ({ target: { value } }) => {
                                                                validate('serviceUrl', value);
                                                            },
                                                        }}
                                                        onChange={handleChange}
                                                    />
                                                </TableCell>
                                            )
                                                : (<TableCell>{service.serviceUrl}</TableCell>)}
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>code</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.EditableOverview.definition.type'
                                                            defaultMessage='Schema Type'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            {isEditing ? (
                                                <TableCell>
                                                    <TextField
                                                        name='definitionType'
                                                        select
                                                        value={definitionType}
                                                        variant='outlined'
                                                        fullWidth
                                                        margin='normal'
                                                        error={validity.definitionType}
                                                        helperText={validity.definitionType}
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
                                                            style: {
                                                                height: 60,
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
                                                </TableCell>
                                            )
                                                : (<TableCell>{service.definitionType}</TableCell>)}
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>security</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.EditableOverview.security.type'
                                                            defaultMessage='Security Type'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            {isEditing ? (
                                                <TableCell>
                                                    <TextField
                                                        name='securityType'
                                                        select
                                                        value={securityType}
                                                        variant='outlined'
                                                        fullWidth
                                                        margin='normal'
                                                        error={validity.securityType}
                                                        helperText={validity.securityType}
                                                        InputProps={{
                                                            id: 'itest-id-securityType-input',
                                                            onBlur: ({ target: { value } }) => {
                                                                validate('securityType', value);
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
                                                            style: {
                                                                height: 60,
                                                            },
                                                        }}
                                                        onChange={handleChange}
                                                    >
                                                        {securityTypeList.map((type) => (
                                                            <MenuItem
                                                                id={type}
                                                                key={type}
                                                                value={type}
                                                            >
                                                                <ListItemText primary={type} />
                                                            </MenuItem>
                                                        ))}
                                                    </TextField>
                                                </TableCell>
                                            )
                                                : (<TableCell>{service.securityType}</TableCell>)}
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>sync_alt</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.EditableOverview.mutual.ssl'
                                                            defaultMessage='Mutual SSL'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                { renderContent() }
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>timeline</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.EditableOverview.created.time'
                                                            defaultMessage='Created Time'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{moment(service.createdTime).fromNow()}</TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </div>
                        </div>
                        <div className={classes.buttonWrapper}>
                            <Grid
                                container
                                direction='row'
                                alignItems='flex-start'
                                spacing={1}
                                className={classes.buttonSection}
                            >
                                <Grid item>
                                    {isEditing ? (
                                        <Button
                                            onClick={doneEditing}
                                            color='primary'
                                            variant='contained'
                                        >
                                            <FormattedMessage
                                                id='ServiceCatalog.Listing.EditableOverview.save.btn'
                                                defaultMessage='Save'
                                            />
                                        </Button>
                                    )
                                        : (
                                            <Button
                                                onClick={onEditFromOverview}
                                                color='primary'
                                                variant='outlined'
                                            >
                                                <FormattedMessage
                                                    id='ServiceCatalog.Listing.EditableOverview.edit.btn'
                                                    defaultMessage='Edit'
                                                />
                                            </Button>
                                        )}
                                </Grid>
                                <Grid item>
                                    {isEditing ? (
                                        <Button onClick={overviewRedirect} color='primary'>
                                            <FormattedMessage
                                                id='ServiceCatalog.Listing.EditableOverview.cancel.btn'
                                                defaultMessage='Cancel'
                                            />
                                        </Button>
                                    )
                                        : (
                                            <Button onClick={listingRedirect} color='primary'>
                                                <FormattedMessage
                                                    id='ServiceCatalog.Listing.EditableOverview.back.btn'
                                                    defaultMessage='Go Back'
                                                />
                                            </Button>
                                        )}
                                </Grid>
                            </Grid>
                        </div>
                    </Box>
                </Paper>
            </Container>
        </>
    );
}

EditableOverview.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
};

export default EditableOverview;
