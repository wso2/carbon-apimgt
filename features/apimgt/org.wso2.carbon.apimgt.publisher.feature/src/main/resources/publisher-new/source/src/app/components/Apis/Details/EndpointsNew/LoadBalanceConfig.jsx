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

import React, { useState } from 'react';
import { TextField, MenuItem, Grid } from '@material-ui/core';
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

/**
 * The component for loadbalance endpoint configuration.
 * @param {any} props The props that are being passed.
 * @returns {any} The HTML contents of the Configuration component.
 */
function LoadBalanceConfig(props) {
    const [algorithm, setAlgorithm] = useState(algorithms[0]); // TODO: GEt from the props
    const [sessionMgtOp, setSessionManagement] = useState(sessionManagementOps[0]); // TODO: GEt from the props
    const [epTimeout, setEpTimeout] = useState(300);
    const [customAlogrithm, setCustomAlgorithm] = useState('');

    const handleAlgorithmChange = (event) => {
        const index = event.target.value;
        setAlgorithm(algorithms[index]);
    };

    const handleSessionMgtChange = (event) => {
        const index = event.target.value;
        setSessionManagement(sessionManagementOps[index]);
    };

    const handleTimeoutChange = (event) => {
        const timeoutValue = event.target.value;
        setEpTimeout(timeoutValue);
    };

    const customAlogrithmChange = (event) => {
        const customAlogrithmVal = event.target.value;
        setCustomAlgorithm(customAlogrithmVal);
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
                    value={algorithm.key}
                    onChange={handleAlgorithmChange}
                    helperText='Please select the Loadbalance Algorithm.'
                    margin='normal'
                >
                    {algorithms.map(algo => (
                        <MenuItem key={algo.key} value={algo.key} selected={algorithm}>
                            {algo.value}
                        </MenuItem>
                    ))}
                </TextField>
                {(algorithm.id === 1) ?
                    <TextField
                        id='customAlgoInput'
                        label={<FormattedMessage
                            id='Apis.Details.EndpointsNew.LoadBalanceConfig.class.name.for.algorithm'
                            defaultMessage='Class Name for Algorithm'
                        />}
                        value={customAlogrithm}
                        onChange={customAlogrithmChange}
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
                    value={sessionMgtOp.key}
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
                    value={epTimeout}
                    onChange={handleTimeoutChange}
                    type='number'
                    placeholder='300'
                    margin='normal'
                />
            </Grid>
        </div>
    );
}

LoadBalanceConfig.propTypes = {
    api: PropTypes.shape({}).isRequired,
};

export default injectIntl(LoadBalanceConfig);
