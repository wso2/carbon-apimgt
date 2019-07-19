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

import React, { useEffect, useState } from 'react';
import {
    Grid,
    TextField,
    Typography,
    withStyles,
    MenuItem,
    FormControl,
    InputLabel,
    Select,
    Input, Button, DialogActions,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
    PaperProps: {
        style: {
            maxHeight: (ITEM_HEIGHT * 4.5) + ITEM_PADDING_TOP, width: 250,
        },
    },
};
const styles = theme => ({
    formControl: {
        width: '500px',
    },
    subTitle: {
        fontSize: '1rem',
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
        marginRight: theme.spacing.unit,
        width: '45%',
    },
});
/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The input props
 * @returns {any} The HTML representation of the compoenent.
 */
function SuspendTimeoutConfig(props) {
    const { classes, intl, advanceConfig, isSOAPEndpoint } = props;
    const [advanceConfiguration, setAdvanceConfiguration] = useState(advanceConfig);
    const [suspendErrCodes, setSuspendErrCodes] = useState([]);
    const [timeoutErrCodes, setTimeoutErrCodes] = useState([]);
    const [timeoutAction, setTimeoutAction] = useState('');
    const ERRCODES = [
        {
            key: '101001',
            value: '101001 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.receiver.io.error.receiving',
                defaultMessage: 'Receiver IO error receiving',
            }),
        },
        {
            key: '101500',
            value: '101500 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.sender.io.error.sending',
                defaultMessage: 'Sender IO Error Sending',
            }),
        },
        {
            key: '101000',
            value: '101000 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.receiver.io.error.sending',
                defaultMessage: 'Retriever IO Error Sending',
            }),
        },
        {
            key: '101501',
            value: '101501 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.sender.io.error.receiving',
                defaultMessage: 'Sender IO Error Receiving',
            }),
        },
        {
            key: '101503',
            value: '101503 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.connection.failed',
                defaultMessage: 'Connection Failed',
            }),
        },
        {
            key: '101504',
            value: '101504 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.connection.timed.out',
                defaultMessage: 'Connection Timed Out',
            }),
        },
        {
            key: '101505',
            value: '101505 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.connection.closed',
                defaultMessage: 'Connection Closed',
            }),
        },
        {
            key: '101506',
            value: '101506 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.tpp.protocol.violation',
                defaultMessage: 'TTP Protocol Violation',
            }),
        },
        {
            key: '101507',
            value: '101507 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.connect.cancel',
                defaultMessage: 'Connect Cancel',
            }),
        },
        {
            key: '101508',
            value: '101508 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.connect.timeout',
                defaultMessage: 'Connect Timeout',
            }),
        },
        {
            key: '101509',
            value: '101509 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.send.abort',
                defaultMessage: 'Send Abort',
            }),
        },
        {
            key: '101510',
            value: '101510 : ' + intl.formatMessage({
                id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.response.processing.failure',
                defaultMessage: 'Response Processing Failure',
            }),
        }];
    const ACTIONITEMS = [
        intl.formatMessage({
            id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.none',
            defaultMessage: 'None',
        }),
        intl.formatMessage({
            id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.execute.fault.sequence',
            defaultMessage: 'Execute Fault Sequence',
        }),
        intl.formatMessage({
            id: 'Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.discard.message',
            defaultMessage: 'Discard Message',
        })];

    const defaultAdvanceConfig = {
        actionDuration: '',
        actionSelect: 'fault',
        factor: '1',
        retryDelay: '23123',
        retryErroCode: ['101500', '101503', '101505', '101506'],
        retryTimeOut: '222',
        suspendDuration: '2123',
        suspendErrorCode: ['101001', '101501', '101504', '101508'],
        suspendMaxDuration: '',
    };

    useEffect(() => {
        setAdvanceConfiguration(() => {
            if (!advanceConfig) {
                return { ...defaultAdvanceConfig };
            }
            return
        });
    }, [props]);

    const handleErrCodeSelect = (event) => {
        setSuspendErrCodes(event.target.value);
    };
    const handleTimeoutErrCodeSelect = (event) => {
        setTimeoutErrCodes(event.target.value);
    };
    const handleTimeoutActionSelect = (event) => {
        setTimeoutAction(event.target.value);
    };
    return (
        <Grid container direction='column' className={classes.configContainer}>
            {(isSOAPEndpoint) ? (
                <Grid item container className={classes.configSubContainer}>
                    <Typography className={classes.subTitle}>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.message.content'
                            defaultMessage='Message Content'
                        />
                    </Typography>
                    <FormControl className={classes.formControl}>
                        <InputLabel htmlFor='err-code-select'>
                            <FormattedMessage
                                id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.format.select'
                                defaultMessage='Format'
                            />
                        </InputLabel>
                        <Select
                            multiple
                            autoWidth={false}
                            value={suspendErrCodes}
                            onChange={handleErrCodeSelect}
                            input={<Input id='err-code-select' />}
                            MenuProps={MenuProps}
                            variant='outlined'
                        >
                            {ERRCODES.map(code => (
                                <MenuItem key={code.key} value={code.key}>
                                    {code.value}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <FormControl className={classes.formControl}>
                        <InputLabel htmlFor='err-code-select'>
                            <FormattedMessage
                                id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.optimize.select'
                                defaultMessage='Optimize'
                            />
                        </InputLabel>
                        <Select
                            multiple
                            autoWidth={false}
                            value={suspendErrCodes}
                            onChange={handleErrCodeSelect}
                            input={<Input id='err-code-select' />}
                            MenuProps={MenuProps}
                            variant='outlined'
                        >
                            {ERRCODES.map(code => (
                                <MenuItem key={code.key} value={code.key}>
                                    {code.value}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Grid>
            ) : (<div />)}
            <Grid item container className={classes.configSubContainer}>
                <Typography className={classes.subTitle}>
                    <FormattedMessage id='Endpoint.Suspension.State' defaultMessage='Endpoint Suspension State' />
                </Typography>
                <FormControl className={classes.formControl}>
                    <InputLabel htmlFor='err-code-select'>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.error.code'
                            defaultMessage='Error Code'
                        />
                    </InputLabel>
                    <Select
                        multiple
                        autoWidth={false}
                        value={suspendErrCodes}
                        onChange={handleErrCodeSelect}
                        input={<Input id='err-code-select' />}
                        MenuProps={MenuProps}
                        variant='outlined'
                    >
                        {ERRCODES.map(code => (
                            <MenuItem key={code.key} value={code.key}>
                                {code.value}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <TextField
                    className={classes.textField}
                    id='initial-duration-input'
                    label={
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.initial.duration.ms'
                            defaultMessage='Initial Duration (ms)'
                        />
                    }
                    margin='normal'
                    type='number'
                />
                <TextField
                    className={classes.textField}
                    id='max-duration-input'
                    label={
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.max.duration.ms'
                            defaultMessage='Max Duration (ms)'
                        />
                    }
                    margin='normal'
                    type='number'
                />
                <TextField
                    className={classes.textField}
                    id='factor-input'
                    label={
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.factor'
                            defaultMessage='Factor'
                        />
                    }
                    margin='normal'
                />
            </Grid>
            <Grid item container className={classes.configSubContainer}>
                <Typography className={classes.subTitle}>
                    <FormattedMessage
                        id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.endpoint.timeout.state'
                        defaultMessage='Endpoint Timeout State'
                    />
                </Typography>
                <FormControl className={classes.formControl}>
                    <InputLabel htmlFor='err-code-select'>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.error.code'
                            defaultMessage='Error Code'
                        />
                    </InputLabel>
                    <Select
                        multiple
                        autoWidth={false}
                        value={timeoutErrCodes}
                        onChange={handleTimeoutErrCodeSelect}
                        input={<Input id='err-code-select' />}
                        MenuProps={MenuProps}
                    >
                        {ERRCODES.map(code => (
                            <MenuItem key={code.key} value={code.key}>
                                {code.value}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <TextField
                    className={classes.textField}
                    id='retries-input'
                    label={
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.retries.before.suspension'
                            defaultMessage='Retries Before Suspension'
                        />
                    }
                    type='number'
                    margin='normal'
                />
                <TextField
                    className={classes.textField}
                    id='retry-delay-input'
                    label={<FormattedMessage
                        id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.retry.delay.ms'
                        defaultMessage='Retry Delay (ms)'
                    />}
                    type='number'
                    margin='normal'
                />
            </Grid>
            <Grid item container className={classes.configSubContainer}>
                <Typography className={classes.subTitle}>
                    <FormattedMessage id='Connection.Timeout' defaultMessage='Connection Timeout' />
                </Typography>
                <FormControl className={classes.formControl}>
                    <InputLabel htmlFor='err-code-select'>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.action'
                            defaultMessage='Action'
                        />
                    </InputLabel>
                    <Select
                        autoWidth={false}
                        value={timeoutAction}
                        onChange={handleTimeoutActionSelect}
                        input={<Input id='err-code-select' />}
                        MenuProps={MenuProps}
                    >
                        {ACTIONITEMS.map(item => (
                            <MenuItem key={item} value={item}>
                                {item}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <TextField
                    className={classes.textField}
                    id='duration-input'
                    label={<FormattedMessage
                        id='Apis.Details.EndpointsNew.AdvancedConfig.SuspendTimeoutConfig.duration.ms'
                        defaultMessage='Duration (ms)'
                    />}
                    type='number'
                    margin='normal'
                />
            </Grid>
            <Grid>
                <Button onClick={() => setAdvanceConfigOpen(false)} color='primary'>
                    <FormattedMessage
                        id='Apis.Details.EndpointsNew.EndpointOverview.loadbalance.config.cancel.button'
                        defaultMessage='Close'
                    />
                </Button>
                <Button onClick={() => saveAdvanceConfiguration(false)} color='primary' autoFocus>
                    <FormattedMessage
                        id='Apis.Details.EndpointsNew.EndpointOverview.loadbalance.config.save.button'
                        defaultMessage='Save'
                    />
                </Button>
            </Grid>
        </Grid>
    );
}

SuspendTimeoutConfig.propTypes = {
    classes: PropTypes.shape({
        configContainer: PropTypes.shape({}),
        configSubContainer: PropTypes.shape({}),
        subTitle: PropTypes.shape({}),
        formControl: PropTypes.shape({}),

    }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(SuspendTimeoutConfig));
