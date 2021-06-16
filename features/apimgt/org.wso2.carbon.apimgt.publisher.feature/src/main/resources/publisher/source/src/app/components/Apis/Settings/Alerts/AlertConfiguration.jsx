/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import {
    Button,
    CircularProgress,
    Collapse,
    Grid,
    Icon,
    IconButton,
    MenuItem,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    TextField,
    Typography,
    withStyles,
    InputAdornment,
    Fab,
} from '@material-ui/core';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import base64url from 'base64url';
import { FormattedMessage, injectIntl } from 'react-intl';
import Alert from 'AppComponents/Shared/Alert';

const alertPropertyMap = {
    AbnormalResponseTime: 'thresholdResponseTime',
    AbnormalBackendTime: 'thresholdBackendTime',
};

const styles = (theme) => ({
    addBtn: {
        display: 'flex',
        alignItems: 'center',
    },
    configAddBtnContainer: {
        display: 'flex',
        paddingBottom: theme.spacing(2),
    },
    configWrapper: {
        padding: theme.spacing(2),
    },
    configNameHeading: {
        marginBottom: theme.spacing(1),
        borderBottom: '#cccccc 1px inset',
    },
});

/**
 * Alert Configuration component.
 * This component is used to list add and delete the alert configurations.
 *
 * @param {any} props The input props.
 * @return {any} The HTML representation of the component.
 * */
const AlertConfiguration = (props) => {
    const {
        alertType,
        api,
        alertName,
        classes,
        intl,
        setIsWorkerNodeDown,
        setSubscribedAlerts,
        subscribedAlerts,
    } = props;
    const [alertConfiguration, setAlertConfiguration] = useState([]);
    const [apis, setApis] = useState();
    const [selectedAPIName, setSelectedAPIName] = useState();
    const [apiNames, setAPINames] = useState(new Set());
    const [apiVersions, setAPIVersions] = useState([]);
    const [selectedAPIVersion, setSelectedAPIVersion] = useState();
    const [value, setValue] = useState(300);
    const [isProcessing, setProcessing] = useState({});
    const [collapseOpen, setCollapseOpen] = useState(false);

    useEffect(() => {
        const alertConfigPromise = api.getAlertConfigurations(alertType);
        const apisPromise = api.all();
        const apiProductsPromise = api.allProducts();
        Promise.all([alertConfigPromise, apisPromise, apiProductsPromise])
            .then((response) => {
                let apisList = response[1].body.list;
                const productsList = response[2].body.list;
                apisList = apisList.concat(productsList);
                const apiNamesSet = new Set();
                apisList.forEach((tmpApi) => {
                    apiNamesSet.add(tmpApi.name);
                });
                setAPINames(apiNamesSet);
                setApis(apisList);
                setAlertConfiguration(response[0].body);
            })
            .catch((err) => {
                console.log(err);
                setIsWorkerNodeDown(true);
            });
    }, []);

    useEffect(() => {
        setSubscribedAlerts(subscribedAlerts.map((alert) => (alert.name === alertType
            ? { ...alert, configuration: alertConfiguration } : alert)));
    }, [alertConfiguration]);

    /**
     * Handles the API Name select event.
     * Once the api name is selected, the api versions list is populated.
     * @param {string} name The selected api name.
     * */
    const handleAPINameSelect = (name) => {
        setSelectedAPIName(name);
        const availableVersions = apis.filter((tmpApi) => tmpApi.name === name);
        setAPIVersions(availableVersions);
    };

    /**
     * Get the alert configuration of the alert type.
     * This method is called after a configuration addition or deletion.
     *
     * @param {string} action : The action that is being performed.
     * */
    const getAlertConfig = (action) => {
        api.getAlertConfigurations(alertType).then((response) => {
            setAlertConfiguration(response.body);
        }).catch().finally(() => {
            setProcessing({ [action]: false });
        });
    };

    /**
     * Handles the configuration add operation.
     * */
    const handleAddConfiguration = () => {
        setProcessing({ add: true });
        const configId = base64url.encode(selectedAPIName + '#' + selectedAPIVersion);
        const propertyName = alertPropertyMap[alertType];
        const alertConfig = {
            apiName: selectedAPIName,
            apiVersion: selectedAPIVersion,
            [propertyName]: value,
        };
        api.putAlertConfiguration(alertType, alertConfig, configId)
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Settings.Alert.AlertConfiguration.alert.config.add.success.msg',
                    defaultMessage: 'Alert Configuration added successfully',
                }));
                setSelectedAPIName('');
                setSelectedAPIVersion('');
            })
            .catch(() => {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Settings.Alert.AlertConfiguration.alert.config.add.error.msg',
                    defaultMessage: 'Error occurred while adding alert configuration',
                }));
            })
            .finally(() => {
                getAlertConfig('add');
            });
    };

    /**
     * Handles the configuration delet operation for a selected configuration.
     *
     * @param {strng} id The configuration id.
     * */
    const handleDeleteConfiguration = (id) => {
        setProcessing({ delete: id });
        api.deleteAlertConfiguration(alertType, id).then(() => {
            Alert.info(intl.formatMessage({
                id: 'Apis.Settings.Alert.AlertConfiguration.alert.config.delete.success.msg',
                defaultMessage: 'Alert Configuration deleted successfully',
            }));
        }).catch(() => {
            Alert.error(intl.formatMessage({
                id: 'Apis.Settings.Alert.AlertConfiguration.alert.config.delete.error.msg',
                defaultMessage: 'Error occurred while deleting the configuration.',
            }));
        }).finally(() => {
            getAlertConfig('delete');
        });
    };

    if (!apis || !alertConfiguration) {
        return <CircularProgress />;
    }
    return (
        <>
            <>
                <Button onClick={() => setCollapseOpen(!collapseOpen)} color='primary'>
                    <Typography className={classes.addBtn}>
                        <Icon color='primary'>
                            add
                        </Icon>
                        <FormattedMessage
                            id='Apis.Settings.Alert.AlertConfiguration.add'
                            defaultMessage='New Configuration'
                        />
                    </Typography>
                </Button>
                <Collapse in={collapseOpen} className={classes.configWrapper}>
                    <Grid container spacing={1}>
                        <Grid item xs>
                            <TextField
                                id='outlined-select-api-name'
                                select
                                fullWidth
                                required
                                label={(
                                    <FormattedMessage
                                        id='Apis.Settings.Alerts.AlertConfiguration.api.name.label'
                                        defaultMessage='API Name'
                                    />
                                )}
                                className={classes.textField}
                                value={selectedAPIName}
                                onChange={(event) => handleAPINameSelect(event.target.value)}
                                SelectProps={{
                                    MenuProps: {
                                        className: classes.menu,
                                    },
                                }}
                                helperText={(
                                    <FormattedMessage
                                        id='Apis.Settings.Alerts.AlertConfiguration.select.api.helper'
                                        defaultMessage='Select the API Name'
                                    />
                                )}
                                variant='outlined'
                            >
                                {apiNames && Array.from(apiNames).map((name) => {
                                    return (
                                        <MenuItem key={name} value={name}>
                                            {name}
                                        </MenuItem>
                                    );
                                })}
                            </TextField>
                        </Grid>
                        <Grid item xs>
                            <TextField
                                id='outlined-select-api-version'
                                select
                                fullWidth
                                required
                                label={(
                                    <FormattedMessage
                                        id='Apis.Settings.Alerts.AlertConfiguration.api.version.label'
                                        defaultMessage='API Version'
                                    />
                                )}
                                className={classes.textField}
                                value={selectedAPIVersion}
                                onChange={(event) => setSelectedAPIVersion(event.target.value)}
                                SelectProps={{
                                    MenuProps: {
                                        className: classes.menu,
                                    },
                                }}
                                helperText={(
                                    <FormattedMessage
                                        id='Apis.Settings.Alerts.AlertConfiguration.select.version.helper'
                                        defaultMessage='Select API Version'
                                    />
                                )}
                                variant='outlined'
                            >
                                {apiVersions && apiVersions.map((selected) => {
                                    return (
                                        <MenuItem
                                            key={selected.version ? selected.version : '1.0.0'}
                                            value={selected.version ? selected.version : '1.0.0'}
                                        >
                                            {selected.version ? selected.version : '1.0.0'}
                                        </MenuItem>
                                    );
                                })}
                            </TextField>
                        </Grid>
                        <Grid item xs>
                            <TextField
                                id='outlined-value'
                                type='number'
                                fullWidth
                                required
                                label={alertName}
                                className={classes.textField}
                                value={value}
                                onChange={(event) => setValue(event.target.value)}
                                variant='outlined'
                                endAdornment={<InputAdornment position='end'>ms</InputAdornment>}
                                helperText={(
                                    <FormattedMessage
                                        id='Apis.Settings.Alerts.AlertConfiguration.threshold.value.helper'
                                        defaultMessage='Enter threshold value.'
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item className={classes.configAddBtnContainer}>
                            <Fab
                                disabled={!selectedAPIName || !selectedAPIVersion || !value || isProcessing.add}
                                color='primary'
                                size='medium'
                                onClick={handleAddConfiguration}
                            >
                                <Icon>
                                    {isProcessing.add && <CircularProgress size={15} />}
                                    add
                                </Icon>
                            </Fab>
                        </Grid>
                    </Grid>
                </Collapse>
            </>
            <>
                <Typography className={classes.configNameHeading}>
                    <FormattedMessage
                        id='Apis.Settings.Alerts.AlertConfiguration.configuration'
                        defaultMessage='{name} Configurations'
                        values={{ name: alertName }}
                    />
                </Typography>
                {alertConfiguration.length === 0 ? (
                    <InlineMessage height={80}>
                        <div className={classes.contentWrapper}>
                            <Typography>
                                <FormattedMessage
                                    id='Apis.Settings.Alerts.AlertConfiguration.no.config.message'
                                    defaultMessage={'You do not have any configurations. Click on {newConfig} button'
                                    + ' to add a configuration.'}
                                    values={{
                                        newConfig: <b>New Configuration</b>,
                                    }}
                                />
                            </Typography>
                        </div>
                    </InlineMessage>
                )
                    : (
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Apis.Settings.Alerts.AlertConfiguration.api.name'
                                            defaultMessage='API Name'
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Apis.Settings.Alerts.AlertConfiguration.api.version'
                                            defaultMessage='API Version'
                                        />
                                    </TableCell>
                                    <TableCell>{alertName}</TableCell>
                                    <TableCell />
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {alertConfiguration.map((configuration) => {
                                    return (
                                        <TableRow id={configuration.configurationId}>
                                            <TableCell>{configuration.configuration.apiName}</TableCell>
                                            <TableCell>{configuration.configuration.apiVersion}</TableCell>
                                            <TableCell>
                                                {configuration.configuration[alertPropertyMap[alertType]]}
                                            </TableCell>
                                            <TableCell>
                                                <IconButton
                                                    onClick={() => handleDeleteConfiguration(
                                                        configuration.configurationId,
                                                    )}
                                                >
                                                    {isProcessing.delete === configuration.configurationId
                                                        ? <CircularProgress size={15} />
                                                        : (
                                                            <Icon>
                                                        delete
                                                            </Icon>
                                                        )}
                                                </IconButton>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    )}
            </>
        </>
    );
};

AlertConfiguration.propTypes = {
    alertType: PropTypes.string.isRequired,
    alertName: PropTypes.string.isRequired,
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    setSubscribedAlerts: PropTypes.func.isRequired,
    subscribedAlerts: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
    setIsWorkerNodeDown: PropTypes.func.isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(AlertConfiguration));
