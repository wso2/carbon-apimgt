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
import Autocomplete from '@material-ui/lab/Autocomplete';
import Settings from 'Settings';

const alertPropertyMap = {
    AbnormalRequestsPerMin: 'requestCount',
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
        marginBottom: theme.spacing(),
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
    } = props;
    const [alertConfiguration, setAlertConfiguration] = useState([]);
    const [apis, setApis] = useState();
    const [selectedAPIName, setSelectedAPIName] = useState();
    const [apiNames, setAPINames] = useState();
    const [apiVersions, setAPIVersions] = useState([]);
    const [applications, setApplications] = useState([]);
    const [selectedAPIVersion, setSelectedAPIVersion] = useState();
    const [value, setValue] = useState();
    const [isProcessing, setProcessing] = useState({});
    const [collapseOpen, setCollapseOpen] = useState(false);
    const [selectedApplicationName, setSelectedApplicationName] = useState();

    useEffect(() => {
        const alertConfigPromise = api.getAlertConfigurations(alertType);
        const apisPromise = api.getAllAPIs({ limit: Settings.app.alertMaxAPIGetLimit });
        Promise.all([alertConfigPromise, apisPromise])
            .then((response) => {
                const apisList = response[1].body.list;
                const apiNameList = apisList.map((apiResp) => { return { label: apiResp.name }; });
                setAPINames(apiNameList);
                setApis(apisList);
                setAlertConfiguration(response[0].body);
            })
            .catch((err) => {
                console.log(err);
                setIsWorkerNodeDown(true);
            });
    }, []);

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
     * Handles the api version select event.
     * In this method, get the subscriptions of the selected api+version and set to the state.
     * @param {string} version The selected api version.
     * */
    const handleApiVersionSelect = (version) => {
        setSelectedAPIVersion(version);
        const existingAPI = apis.filter((tmpAPi) => {
            return tmpAPi.name === selectedAPIName && tmpAPi.version === version;
        });
        if (existingAPI.length > 0) {
            api.getSubscriptions(existingAPI[0].id).then((res) => {
                const subscribedApps = res.body.list.map((subscription) => {
                    return subscription.applicationInfo;
                });
                setApplications(subscribedApps);
            }).catch((err) => {
                console.log(err);
            });
        }
    };

    /**
     * Get the alert configuration of the alert type.
     * This method is called after a configuration addition or deletion.
     *
     * @param {string} action The action that is being performed.
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
        const configId = base64url.encode(selectedAPIName + '#' + selectedAPIVersion + '#' + selectedApplicationName);
        const alertConfig = {
            apiName: selectedAPIName,
            apiVersion: selectedAPIVersion,
            applicationName: selectedApplicationName,
            requestCount: value,
        };
        api.putAlertConfiguration(alertType, alertConfig, configId)
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Settings.Alert.AlertConfiguration.alert.config.add.success.msg',
                    defaultMessage: 'Alert Configuration added successfully',
                }));
            })
            .catch(() => {
                Alert.error(intl.formatMessage({
                    id: 'Settings.Alert.AlertConfiguration.alert.config.add.error.msg',
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
                id: 'Settings.Alert.AlertConfiguration.alert.config.delete.success.msg',
                defaultMessage: 'Alert Configuration deleted successfully',
            }));
        }).catch(() => {
            Alert.error(intl.formatMessage({
                id: 'Settings.Alert.AlertConfiguration.alert.config.delete.error.msg',
                defaultMessage: 'Error occurred while deleting the configuration.',
            }));
        }).finally(() => {
            getAlertConfig('delete');
        });
    };

    const isAddingDissabled = () => {
        return !selectedAPIName || !selectedAPIVersion || !selectedApplicationName || !value || isProcessing.add;
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
                            id='Settings.Alert.AlertConfiguration.add'
                            defaultMessage='New Configuration'
                        />
                    </Typography>
                </Button>
                <Collapse in={collapseOpen} className={classes.configWrapper}>
                    <Grid container spacing={1}>
                        <Grid item xs>
                            <Autocomplete
                                id='combo-box-demo'
                                options={apiNames}
                                getOptionLabel={(option) => option.label}
                                className={classes.textField}
                                value={selectedAPIName}
                                onChange={(event, { label }) => handleAPINameSelect(label)}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        fullWidth
                                        required
                                        label={(
                                            <FormattedMessage
                                                id='Settings.Alerts.AlertConfiguration.api.name.label'
                                                defaultMessage='API Name'
                                            />
                                        )}
                                        helperText={(
                                            <FormattedMessage
                                                id='Settings.Alerts.AlertConfiguration.select.api.helper'
                                                defaultMessage='Select the API Name'
                                            />
                                        )}
                                        variant='outlined'
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs>
                            <TextField
                                id='outlined-select-api-version'
                                select
                                fullWidth
                                required
                                label={(
                                    <FormattedMessage
                                        id='Settings.Alerts.AlertConfiguration.api.version.label'
                                        defaultMessage='API Version'
                                    />
                                )}
                                className={classes.textField}
                                value={selectedAPIVersion}
                                onChange={(event) => handleApiVersionSelect(event.target.value)}
                                SelectProps={{
                                    MenuProps: {
                                        className: classes.menu,
                                    },
                                }}
                                helperText={(
                                    <FormattedMessage
                                        id='Settings.Alerts.AlertConfiguration.select.version.helper'
                                        defaultMessage='Select API Version'
                                    />
                                )}
                                variant='outlined'
                            >
                                {apiVersions && apiVersions.map((selected) => {
                                    return (
                                        <MenuItem key={selected.version} value={selected.version}>
                                            {selected.version}
                                        </MenuItem>
                                    );
                                })}
                            </TextField>
                        </Grid>
                        <Grid item xs>
                            <TextField
                                id='outlined-select-applications'
                                select
                                fullWidth
                                required
                                label={(
                                    <FormattedMessage
                                        id='Settings.Alerts.AlertConfiguration.applications.label'
                                        defaultMessage='Application'
                                    />
                                )}
                                className={classes.textField}
                                value={selectedApplicationName}
                                onChange={(event) => setSelectedApplicationName(event.target.value)}
                                SelectProps={{
                                    MenuProps: {
                                        className: classes.menu,
                                    },
                                }}
                                helperText={(
                                    <FormattedMessage
                                        id='Settings.Alerts.AlertConfiguration.select.application.helper'
                                        defaultMessage='Select Application'
                                    />
                                )}
                                variant='outlined'
                            >
                                {applications && applications.map((applicationInfo) => {
                                    return (
                                        <MenuItem key={applicationInfo.applicationId} value={applicationInfo.name}>
                                            {applicationInfo.name}
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
                                label={(
                                    <FormattedMessage
                                        id='Settings.Alerts.AlertConfiguration.request.count.label'
                                        defaultMessage='Request Count.'
                                    />
                                )}
                                className={classes.textField}
                                value={value}
                                onChange={(event) => setValue(event.target.value)}
                                variant='outlined'
                                endAdornment={<InputAdornment position='end'>ms</InputAdornment>}
                                helperText={(
                                    <FormattedMessage
                                        id='Settings.Alerts.AlertConfiguration.threshold.value.helper'
                                        defaultMessage='Enter Request Count.'
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item className={classes.configAddBtnContainer}>
                            <Fab
                                disabled={isAddingDissabled()}
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
                        id='Settings.Alerts.AlertConfiguration.configuration'
                        defaultMessage='{name} Configurations'
                        values={{ name: alertName }}
                    />
                </Typography>
                {alertConfiguration.length === 0 ? (
                    <InlineMessage height={80}>
                        <div className={classes.contentWrapper}>
                            <Typography>
                                <FormattedMessage
                                    id='Settings.Alerts.AlertConfiguration.no.config.message'
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
                                            id='Settings.Alerts.AlertConfiguration.api.name'
                                            defaultMessage='API Name'
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Settings.Alerts.AlertConfiguration.api.version'
                                            defaultMessage='API Version'
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Settings.Alerts.AlertConfiguration.app.name'
                                            defaultMessage='Application Name'
                                        />
                                    </TableCell>
                                    <TableCell>{alertName}</TableCell>
                                    <TableCell />
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {alertConfiguration.map((configuration) => {
                                    return (
                                        <TableRow id={configuration.configurationId} key={configuration.configurationId}>
                                            <TableCell>{configuration.configuration.apiName}</TableCell>
                                            <TableCell>{configuration.configuration.apiVersion}</TableCell>
                                            <TableCell>{configuration.configuration.applicationName.includes('deleted')?
                                            'Deleted':configuration.configuration.applicationName}</TableCell>
                                            <TableCell>
                                                {configuration.configuration[alertPropertyMap[alertType]]}
                                            </TableCell>
                                            <TableCell>
                                                <IconButton
                                                    onClick={() => handleDeleteConfiguration(configuration.configurationId)}
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
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(AlertConfiguration));
