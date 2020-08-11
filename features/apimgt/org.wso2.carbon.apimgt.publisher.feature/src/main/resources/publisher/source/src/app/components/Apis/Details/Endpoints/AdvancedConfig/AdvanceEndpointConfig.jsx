/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useContext, useEffect, useState } from 'react';
import {
    Divider,
    FormControl,
    Grid,
    Input,
    InputLabel,
    MenuItem,
    Select,
    TextField,
    Typography,
    Button,
    withStyles,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { isRestricted } from 'AppData/AuthManager';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';

const itemHeight = 48;
const itemPaddingTop = 8;
const MenuProps = {
    PaperProps: {
        style: {
            maxHeight: (itemHeight * 4.5) + itemPaddingTop, width: 250,
        },
    },
};
const styles = (theme) => ({
    formControl: {
        width: '500px',
    },
    subTitle: {
        fontSize: '1rem',
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
    configContainer: {
        paddingTop: '10px',
    },
    configSubContainer: {
        paddingBottom: '10px',
        marginTop: '5px',
        padding: '5px',
    },
    textField: {
        marginRight: theme.spacing(1),
        width: '45%',
    },
    advanceDialogActions: {
        display: 'flex',
        justifyContent: 'flex-end',
    },
});

/**
 * The component for advanced endpoint configurations.
 * @param {any} props The input props
 * @returns {any} The HTML representation of the compoenent.
 */
function AdvanceEndpointConfig(props) {
    const { api } = useContext(APIContext);
    const {
        classes,
        intl,
        advanceConfig,
        isSOAPEndpoint,
        onSaveAdvanceConfig,
        onCancel,
    } = props;
    const [advanceConfigObj, setAdvanceConfig] = useState(() => {
        const config = {};
        if (isSOAPEndpoint) {
            config.format = 'soap11';
            config.optimize = 'SWA';
        }
        config.actionDuration = '30000';
        config.actionSelect = 'fault';
        config.factor = '';
        config.retryDelay = '';
        config.retryErroCode = [];
        config.retryTimeOut = '';
        config.suspendDuration = '';
        config.suspendErrorCode = [];
        config.suspendMaxDuration = '';
        return config;
    });

    /**
     * The error codes definition.
     * */
    const errorCodes = [
        {
            key: '101001',
            value: '101001 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.receiver.io.error.receiving',
                defaultMessage: 'Receiver IO error Receiving',
            }),
        },
        {
            key: '101500',
            value: '101500 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.sender.io.error.sending',
                defaultMessage: 'Sender IO Error Sending',
            }),
        },
        {
            key: '101000',
            value: '101000 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.receiver.io.error.sending',
                defaultMessage: 'Retriever IO Error Sending',
            }),
        },
        {
            key: '101501',
            value: '101501 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.sender.io.error.receiving',
                defaultMessage: 'Sender IO Error Receiving',
            }),
        },
        {
            key: '101503',
            value: '101503 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.connection.failed',
                defaultMessage: 'Connection Failed',
            }),
        },
        {
            key: '101504',
            value: '101504 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.connection.timed.out',
                defaultMessage: 'Connection Timed Out',
            }),
        },
        {
            key: '101505',
            value: '101505 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.connection.closed',
                defaultMessage: 'Connection Closed',
            }),
        },
        {
            key: '101506',
            value: '101506 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.tpp.protocol.violation',
                defaultMessage: 'TTP Protocol Violation',
            }),
        },
        {
            key: '101507',
            value: '101507 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.connect.cancel',
                defaultMessage: 'Connect Cancel',
            }),
        },
        {
            key: '101508',
            value: '101508 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.connect.timeout',
                defaultMessage: 'Connect Timeout',
            }),
        },
        {
            key: '101509',
            value: '101509 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.send.abort',
                defaultMessage: 'Send Abort',
            }),
        },
        {
            key: '101510',
            value: '101510 : ' + intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.response.processing.failure',
                defaultMessage: 'Response Processing Failure',
            }),
        }];

    /**
     * Supported action items.
     * */
    const actionItems = [
        {
            key: 'fault',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.execute.fault.sequence',
                defaultMessage: 'Execute Fault Sequence',
            }),
        },
        {
            key: 'discard',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.discard.message',
                defaultMessage: 'Discard Message',
            }),
        },
    ];

    /**
     * Message types for the address endpoint type.
     * */
    const messageTypes = [
        { key: 'soap11', value: 'SOAP 1.1' },
        { key: 'soap12', value: 'SOAP 1.2' },
        {
            key: 'POX',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.plain.old.xml',
                defaultMessage: 'Plain Old XML (POX)',
            }),
        },
        {
            key: 'REST',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.representational.state.transfer',
                defaultMessage: 'Representational State Transfer (REST)',
            }),
        },
        {
            key: 'GET',
            value: 'GET',
        },
        {
            key: 'leave-as-is',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.leave.as.is',
                defaultMessage: 'Leave As-Is',
            }),
        },
    ];

    /**
     * Address endpoint message optimizations.
     * */
    const optimizeOptions = [
        { key: 'SWA', value: 'SWA' },
        { key: 'MTOM', value: 'MTOM' },
        {
            key: 'leave-as-is',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.leave.as.is',
                defaultMessage: 'Leave As-Is',
            }),
        },
    ];

    useEffect(() => {
        setAdvanceConfig(() => {
            if (advanceConfig === {}) {
                return { ...advanceConfigObj };
            }
            return { ...advanceConfigObj, ...advanceConfig };
        });
    }, [props]);

    /**
     * Method to handle the advance endpoint field change. In each change, the advance config object is getting updated.
     *
     * @param {any} event The HTML event triggered by the element.
     * @param {string} field The HTML element that is being changed.
     * */
    const handleConfigFieldChange = (event, field) => {
        const di = { ...advanceConfigObj, [field]: event.target.value };
        setAdvanceConfig(di);
    };

    return (
        <Grid container direction='column' className={classes.configContainer}>
            {(isSOAPEndpoint) ? (
                <Grid item container className={classes.configSubContainer}>
                    <Typography className={classes.subTitle}>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.message.content'
                            defaultMessage='Message Content'
                        />
                    </Typography>
                    <FormControl className={classes.formControl}>
                        <InputLabel htmlFor='err-code-select'>
                            <FormattedMessage
                                id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.format.select'
                                defaultMessage='Format'
                            />
                        </InputLabel>
                        <Select
                            autoWidth={false}
                            value={advanceConfigObj.format}
                            onChange={(event) => handleConfigFieldChange(event, 'format')}
                            input={<Input id='err-code-select' />}
                            MenuProps={MenuProps}
                        >
                            {messageTypes.map((messageType) => (
                                <MenuItem key={messageType.key} value={messageType.key}>
                                    {messageType.value}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <FormControl className={classes.formControl}>
                        <InputLabel htmlFor='err-code-select'>
                            <FormattedMessage
                                id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.optimize.select'
                                defaultMessage='Optimize'
                            />
                        </InputLabel>
                        <Select
                            autoWidth={false}
                            value={advanceConfigObj.optimize}
                            onChange={(event) => handleConfigFieldChange(event, 'optimize')}
                            input={<Input id='err-code-select' />}
                            MenuProps={MenuProps}
                        >
                            {optimizeOptions.map((option) => (
                                <MenuItem key={option.key} value={option.key}>
                                    {option.value}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <Divider />
                </Grid>
            ) : (<div />)}
            <Grid item container className={classes.configSubContainer}>
                <Typography className={classes.subTitle}>
                    <FormattedMessage id='Endpoint.Suspension.State' defaultMessage='Endpoint Suspension State' />
                </Typography>
                <FormControl className={classes.formControl}>
                    <InputLabel htmlFor='err-code-select'>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.error.code'
                            defaultMessage='Error Code'
                        />
                    </InputLabel>
                    <Select
                        multiple
                        autoWidth={false}
                        value={advanceConfigObj.suspendErrorCode}
                        onChange={(event) => handleConfigFieldChange(event, 'suspendErrorCode')}
                        input={<Input id='err-code-select' />}
                        MenuProps={MenuProps}
                        variant='outlined'
                    >
                        {errorCodes.map((code) => (
                            <MenuItem key={code.key} value={code.key}>
                                {code.value}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <TextField
                    className={classes.textField}
                    id='initial-duration-input'
                    value={advanceConfigObj.suspendDuration}
                    onChange={(event) => handleConfigFieldChange(event, 'suspendDuration')}
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.initial.duration.ms'
                            defaultMessage='Initial Duration (ms)'
                        />
                    )}
                    margin='normal'
                    type='number'
                />
                <TextField
                    className={classes.textField}
                    id='max-duration-input'
                    value={advanceConfigObj.suspendMaxDuration}
                    onChange={(event) => handleConfigFieldChange(event, 'suspendMaxDuration')}
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.max.duration.ms'
                            defaultMessage='Max Duration (ms)'
                        />
                    )}
                    margin='normal'
                    type='number'
                />
                <TextField
                    className={classes.textField}
                    value={advanceConfigObj.factor}
                    onChange={(event) => handleConfigFieldChange(event, 'factor')}
                    id='factor-input'
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.factor'
                            defaultMessage='Factor'
                        />
                    )}
                    margin='normal'
                />
            </Grid>
            <Divider />
            <Grid item container className={classes.configSubContainer}>
                <Typography className={classes.subTitle}>
                    <FormattedMessage
                        id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.endpoint.timeout.state'
                        defaultMessage='Endpoint Timeout State'
                    />
                </Typography>
                <FormControl className={classes.formControl}>
                    <InputLabel htmlFor='err-code-select'>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.error.code'
                            defaultMessage='Error Code'
                        />
                    </InputLabel>
                    <Select
                        multiple
                        autoWidth={false}
                        value={advanceConfigObj.retryErroCode}
                        onChange={(event) => handleConfigFieldChange(event, 'retryErroCode')}
                        input={<Input id='err-code-select' />}
                        MenuProps={MenuProps}
                    >
                        {errorCodes.map((code) => (
                            <MenuItem key={code.key} value={code.key}>
                                {code.value}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <TextField
                    className={classes.textField}
                    id='retries-input'
                    value={advanceConfigObj.retryTimeOut}
                    onChange={(event) => handleConfigFieldChange(event, 'retryTimeOut')}
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.retries.before.suspension'
                            defaultMessage='Retries Before Suspension'
                        />
                    )}
                    type='number'
                    margin='normal'
                />
                <TextField
                    className={classes.textField}
                    id='retry-delay-input'
                    value={advanceConfigObj.retryDelay}
                    onChange={(event) => handleConfigFieldChange(event, 'retryDelay')}
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.retry.delay.ms'
                            defaultMessage='Retry Delay (ms)'
                        />
                    )}
                    type='number'
                    margin='normal'
                />
            </Grid>
            <Divider />
            <Grid item container className={classes.configSubContainer}>
                <Typography className={classes.subTitle}>
                    <FormattedMessage id='Connection.Timeout' defaultMessage='Connection Timeout' />
                </Typography>
                <FormControl className={classes.formControl}>
                    <InputLabel htmlFor='err-code-select'>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.action'
                            defaultMessage='Action'
                        />
                    </InputLabel>
                    <Select
                        autoWidth={false}
                        value={advanceConfigObj.actionSelect}
                        onChange={(event) => handleConfigFieldChange(event, 'actionSelect')}
                        input={<Input id='err-code-select' />}
                        MenuProps={MenuProps}
                    >
                        {actionItems.map((item) => (
                            <MenuItem key={item.key} value={item.key}>
                                {item.value}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <TextField
                    className={classes.textField}
                    id='duration-input'
                    value={advanceConfigObj.actionDuration}
                    onChange={(event) => handleConfigFieldChange(event, 'actionDuration')}
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.duration.ms'
                            defaultMessage='Duration (ms)'
                        />
                    )}
                    type='number'
                    margin='normal'
                />
            </Grid>
            <Grid className={classes.advanceDialogActions}>
                <Button
                    onClick={() => onSaveAdvanceConfig(advanceConfigObj)}
                    color='primary'
                    autoFocus
                    disabled={isRestricted(['apim:api_create'], api)}
                    variant='contained'
                    style={{ marginRight: '10px' }}
                >
                    <FormattedMessage
                        id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.config.save.button'
                        defaultMessage='Save'
                    />
                </Button>
                <Button onClick={onCancel}>
                    <FormattedMessage
                        id='Apis.Details.Endpoints.AdvancedConfig.AdvanceEndpointConfig.cancel.button'
                        defaultMessage='Close'
                    />
                </Button>
            </Grid>
        </Grid>
    );
}

AdvanceEndpointConfig.propTypes = {
    classes: PropTypes.shape({
        configContainer: PropTypes.shape({}),
        configSubContainer: PropTypes.shape({}),
        subTitle: PropTypes.shape({}),
        formControl: PropTypes.shape({}),

    }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    advanceConfig: PropTypes.shape({}).isRequired,
    isSOAPEndpoint: PropTypes.bool.isRequired,
    onSaveAdvanceConfig: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(AdvanceEndpointConfig));
