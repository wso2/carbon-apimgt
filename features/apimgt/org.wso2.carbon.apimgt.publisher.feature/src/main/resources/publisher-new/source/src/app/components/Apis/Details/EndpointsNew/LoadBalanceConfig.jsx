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
import { TextField, MenuItem, Grid, Button, withStyles } from '@material-ui/core';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';

const algorithms = [
    {
        key: 'org.apache.synapse.endpoints.algorithms.RoundRobin',
        value: 'Round-Robbin',
    },
    { key: 'other', value: 'other' },
];
const sessionManagementOps = [
    { key: 'http', value: 'Transport' },
    { key: 'soap', value: 'SOAP' },
    { key: 'simpleClientSession', value: 'Client Id' },
    { key: 'none', value: 'None' },
];

const defaultTemplateObj = {
    algoClassName: algorithms[0].key,
    algoCombo: algorithms[0].key,
    sessionManagement: sessionManagementOps[0].key,
    sessionTimeOut: 300,
};

const styles = theme => ({
    configButtonContainer: {
        display: 'flex',
        justifyContent: 'flex-end',
        paddingTop: theme.spacing.unit,
    },
});

/**
 * The component for loadbalance endpoint configuration.
 * @param {any} props The props that are being passed.
 * @returns {any} The HTML contents of the Configuration component.
 */
function LoadBalanceConfig(props) {
    const {
        algoClassName,
        algoCombo,
        sessionManagement,
        sessionTimeOut,
        handleLBConfigChange,
        closeLBConfigDialog,
        classes,
    } = props;
    const [lbConfig, setLbConfigObject] = useState(defaultTemplateObj);
    const [algoClassNameError, setAlgoClassNameError] = useState(false);

    useEffect(() => {
        setLbConfigObject(() => {
            const tmpLBConfig = { ...defaultTemplateObj };
            if (algoCombo) {
                tmpLBConfig.algoCombo = algoCombo;
            }
            if (sessionManagement) {
                tmpLBConfig.sessionManagement = sessionManagement;
            }
            if (algoClassName) {
                tmpLBConfig.algoClassName = algoClassName;
            }
            if (sessionTimeOut) {
                tmpLBConfig.sessionTimeOut = sessionTimeOut;
            }
            return tmpLBConfig;
        });
    }, [props]);

    const handleAlgorithmChange = (event) => {
        const { value } = event.target;
        setLbConfigObject({
            ...lbConfig,
            algoCombo: value,
            algoClassName: value === 'other' ? '' : defaultTemplateObj.algoClassName,
        });
    };

    const handleSessionMgtChange = (event) => {
        setLbConfigObject({ ...lbConfig, sessionManagement: event.target.value });
    };

    const handleTimeoutChange = (event) => {
        setLbConfigObject({ ...lbConfig, sessionTimeOut: event.target.value });
    };

    const customAlogrithmChange = (event) => {
        setLbConfigObject({ ...lbConfig, algoClassName: event.target.value });
    };

    const submitConfiguration = () => {
        handleLBConfigChange(lbConfig);
    };

    return (
        <div>
            <Grid container direction='column'>
                <TextField
                    id='algorithmSelect'
                    select
                    label={<FormattedMessage
                        id='Apis.Details.EndpointsNew.LoadBalanceConfig.algorithm'
                        defaultMessage='Algorithm'
                    />}
                    value={lbConfig.algoCombo}
                    onChange={handleAlgorithmChange}
                    helperText='Please select the Loadbalance Algorithm.'
                    margin='normal'
                >
                    {algorithms.map(algo => (
                        <MenuItem key={algo.key} value={algo.key} selected={lbConfig.algoCombo}>
                            {algo.value}
                        </MenuItem>
                    ))}
                </TextField>
                {(lbConfig.algoCombo === 'other') ?
                    <TextField
                        id='customAlgoInput'
                        label={<FormattedMessage
                            id='Apis.Details.EndpointsNew.LoadBalanceConfig.class.name.for.algorithm'
                            defaultMessage='Class Name for Algorithm'
                        />}
                        required
                        error={algoClassNameError}
                        value={lbConfig.algoClassName}
                        onChange={customAlogrithmChange}
                        onBlur={() => setAlgoClassNameError(lbConfig.algoClassName === '')}
                        helperText='Enter the class name of the loadbalance algorithm'
                        margin='normal'
                    /> : <div /> }
                <TextField
                    id='sessionMgtSelect'
                    select
                    label={<FormattedMessage
                        id='Apis.Details.EndpointsNew.LoadBalanceConfig.session.management'
                        defaultMessage='Session Management'
                    />}
                    value={lbConfig.sessionManagement}
                    onChange={handleSessionMgtChange}
                    helperText='Please select the Session Management mechanism.'
                    margin='normal'
                >
                    {sessionManagementOps.map(option => (
                        <MenuItem key={option.key} value={option.key}>
                            {option.value}
                        </MenuItem>
                    ))}
                </TextField>
                <TextField
                    id='sessionTimeout'
                    label={<FormattedMessage
                        id='Apis.Details.EndpointsNew.LoadBalanceConfig.session.timeout'
                        defaultMessage='Session Timeout (Millis)'
                    />}
                    value={lbConfig.sessionTimeOut}
                    onChange={handleTimeoutChange}
                    type='number'
                    placeholder='300'
                    margin='normal'
                />
            </Grid>
            <Grid className={classes.configButtonContainer}>
                <Button color='primary' onClick={closeLBConfigDialog}>
                    <FormattedMessage
                        id='Apis.Details.EndpointsNew.EndpointOverview.loadbalance.config.cancel.button'
                        defaultMessage='Close'
                    />
                </Button>
                <Button
                    color='primary'
                    autoFocus
                    onClick={submitConfiguration}
                    disabled={lbConfig.algoClassName === ''}
                >
                    <FormattedMessage
                        id='Apis.Details.EndpointsNew.EndpointOverview.loadbalance.config.save.button'
                        defaultMessage='Save'
                    />
                </Button>
            </Grid>
        </div>
    );
}

LoadBalanceConfig.propTypes = {
    api: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    algoClassName: PropTypes.string.isRequired,
    algoCombo: PropTypes.string.isRequired,
    sessionManagement: PropTypes.string.isRequired,
    sessionTimeOut: PropTypes.string.isRequired,
    handleLBConfigChange: PropTypes.func.isRequired,
    closeLBConfigDialog: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(LoadBalanceConfig));
